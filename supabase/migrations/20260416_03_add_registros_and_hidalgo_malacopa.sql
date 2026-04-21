-- Metadatos por registro/noche para trucos Hidalgo y Malacopa
begin;

create table if not exists public.registros (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null default auth.uid() references public.usuarios(id) on delete cascade,
    fecha_hora timestamptz not null default timezone('utc', now()),
    lugar_nombre text not null,
    cubatas_hidalgo_total integer not null default 0,
    vomitos_total integer not null default 0,
    created_at timestamptz not null default timezone('utc', now()),
    updated_at timestamptz not null default timezone('utc', now()),
    constraint registros_cubatas_hidalgo_non_negative check (cubatas_hidalgo_total >= 0),
    constraint registros_vomitos_non_negative check (vomitos_total >= 0)
);

alter table public.consumiciones
    add column if not exists lugar_nombre text;

alter table public.consumiciones
    add column if not exists registro_id uuid;

alter table public.consumiciones
    drop constraint if exists consumiciones_registro_id_fkey;

alter table public.consumiciones
    add constraint consumiciones_registro_id_fkey
    foreign key (registro_id) references public.registros(id) on delete set null;

create index if not exists idx_registros_usuario_fecha
    on public.registros (usuario_id, fecha_hora desc);

create index if not exists idx_registros_usuario_lugar
    on public.registros (usuario_id, lugar_nombre);

create index if not exists idx_consumiciones_registro
    on public.consumiciones (registro_id);

create index if not exists idx_consumiciones_usuario_lugar
    on public.consumiciones (usuario_id, lugar_nombre);

drop trigger if exists trg_registros_updated_at on public.registros;
create trigger trg_registros_updated_at
before update on public.registros
for each row
execute function public.set_updated_at();

alter table public.registros enable row level security;

drop policy if exists "registros_select_own" on public.registros;
create policy "registros_select_own"
on public.registros
for select
using (usuario_id = auth.uid());

drop policy if exists "registros_insert_own" on public.registros;
create policy "registros_insert_own"
on public.registros
for insert
with check (usuario_id = auth.uid());

drop policy if exists "registros_update_own" on public.registros;
create policy "registros_update_own"
on public.registros
for update
using (usuario_id = auth.uid())
with check (usuario_id = auth.uid());

drop policy if exists "registros_delete_own" on public.registros;
create policy "registros_delete_own"
on public.registros
for delete
using (usuario_id = auth.uid());

commit;

