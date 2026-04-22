-- Lugares permanentes por usuario para evitar duplicados como 'El comedia' vs 'Sala la comedia'
begin;

create table if not exists public.lugares (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null default auth.uid() references public.usuarios(id) on delete cascade,
    nombre text not null,
    nombre_normalizado text not null,
    created_at timestamptz not null default timezone('utc', now()),
    updated_at timestamptz not null default timezone('utc', now()),
    constraint lugares_nombre_non_empty check (btrim(nombre) <> ''),
    constraint lugares_nombre_normalizado_non_empty check (btrim(nombre_normalizado) <> '')
);

create unique index if not exists idx_lugares_usuario_nombre_normalizado
    on public.lugares (usuario_id, nombre_normalizado);

create index if not exists idx_lugares_usuario_created_at
    on public.lugares (usuario_id, created_at desc);

-- Relleno inicial desde los registros existentes para que la droplist no empiece vacía.
insert into public.lugares (usuario_id, nombre, nombre_normalizado)
select distinct
    r.usuario_id,
    btrim(r.lugar_nombre),
    lower(btrim(r.lugar_nombre))
from public.registros r
where r.lugar_nombre is not null
  and btrim(r.lugar_nombre) <> ''
on conflict (usuario_id, nombre_normalizado) do nothing;

-- Mantener updated_at coherente.
drop trigger if exists trg_lugares_updated_at on public.lugares;
create trigger trg_lugares_updated_at
before update on public.lugares
for each row
execute function public.set_updated_at();

alter table public.lugares enable row level security;

drop policy if exists "lugares_select_own" on public.lugares;
create policy "lugares_select_own"
on public.lugares
for select
using (usuario_id = auth.uid());

drop policy if exists "lugares_insert_own" on public.lugares;
create policy "lugares_insert_own"
on public.lugares
for insert
with check (usuario_id = auth.uid());

drop policy if exists "lugares_update_own" on public.lugares;
create policy "lugares_update_own"
on public.lugares
for update
using (usuario_id = auth.uid())
with check (usuario_id = auth.uid());

drop policy if exists "lugares_delete_own" on public.lugares;
create policy "lugares_delete_own"
on public.lugares
for delete
using (usuario_id = auth.uid());

create or replace function public.delete_my_lugar(p_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
    delete from public.lugares
    where id = p_id
      and usuario_id = auth.uid();
end;
$$;

grant execute on function public.delete_my_lugar(uuid) to authenticated;

commit;

