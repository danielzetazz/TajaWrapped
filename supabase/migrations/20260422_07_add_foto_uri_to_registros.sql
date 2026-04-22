-- Añade foto_uri a registros para poder guardar la foto de recuerdo por noche
begin;

alter table public.registros
    add column if not exists foto_uri text;

commit;

-- Fuerza la recarga del schema cache de PostgREST tras aplicar la migración.
notify pgrst, 'reload schema';

