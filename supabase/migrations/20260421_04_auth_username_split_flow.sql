-- Soporte de login por username + registro separado (email, username, password)
begin;

create extension if not exists citext;

alter table if exists public.usuarios
    add column if not exists email text;

alter table if exists public.usuarios
    add column if not exists username citext;

-- Backfill de email desde auth.users para filas antiguas.
update public.usuarios u
set email = au.email
from auth.users au
where u.id = au.id
  and (u.email is null or btrim(u.email) = '');

-- Backfill de username para filas antiguas sin valor.
update public.usuarios
set username = lower(split_part(coalesce(email, id::text), '@', 1))
where username is null or btrim(username::text) = '';

-- Backfill de display_name para filas antiguas sin valor.
update public.usuarios
set display_name = coalesce(
    nullif(btrim(display_name), ''),
    nullif(btrim(username::text), ''),
    lower(split_part(coalesce(email, id::text), '@', 1))
)
where display_name is null or btrim(display_name) = '';

create unique index if not exists usuarios_username_unique_idx
    on public.usuarios (lower(username::text));

create unique index if not exists usuarios_email_unique_idx
    on public.usuarios (lower(email));

-- Trigger para mantener usuarios sincronizado con auth.users.
create or replace function public.sync_usuario_from_auth()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    -- Asegura que display_name nunca se quede nulo para filas nuevas.
    -- Si auth no trae metadata, usamos username o la parte local del email.
    insert into public.usuarios (id, email, display_name, username)
    values (
        new.id,
        new.email,
        coalesce(
            nullif(new.raw_user_meta_data ->> 'display_name', ''),
            nullif(new.raw_user_meta_data ->> 'username', ''),
            lower(split_part(new.email, '@', 1))
        ),
        coalesce(
            nullif(lower(new.raw_user_meta_data ->> 'username'), ''),
            lower(split_part(new.email, '@', 1))
        )
    )
    on conflict (id) do update
    set email = excluded.email,
        display_name = coalesce(public.usuarios.display_name, excluded.display_name),
        username = coalesce(excluded.username, public.usuarios.username);

    return new;
end;
$$;

drop trigger if exists trg_sync_usuario_from_auth on auth.users;
create trigger trg_sync_usuario_from_auth
after insert or update of email, raw_user_meta_data on auth.users
for each row execute function public.sync_usuario_from_auth();

alter table public.usuarios enable row level security;

drop policy if exists "usuarios_login_lookup" on public.usuarios;
create policy "usuarios_login_lookup"
on public.usuarios
for select
to anon
using (true);

drop policy if exists "usuarios_select_own" on public.usuarios;
create policy "usuarios_select_own"
on public.usuarios
for select
to authenticated
using (id = auth.uid());

drop policy if exists "usuarios_insert_own" on public.usuarios;
create policy "usuarios_insert_own"
on public.usuarios
for insert
to authenticated
with check (id = auth.uid());

drop policy if exists "usuarios_update_own" on public.usuarios;
create policy "usuarios_update_own"
on public.usuarios
for update
to authenticated
using (id = auth.uid())
with check (id = auth.uid());

commit;

