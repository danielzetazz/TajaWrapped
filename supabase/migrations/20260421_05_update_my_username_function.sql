-- Permite al usuario autenticado cambiar su username de forma segura
begin;

create or replace function public.update_my_username(p_username text)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid;
    v_email text;
    v_username text;
    v_display_name text;
begin
    v_user_id := auth.uid();
    if v_user_id is null then
        raise exception 'No autenticado';
    end if;

    v_username := lower(btrim(coalesce(p_username, '')));

    if v_username = '' then
        raise exception 'El nombre de usuario no puede estar vacío';
    end if;

    if length(v_username) < 3 then
        raise exception 'El nombre de usuario debe tener al menos 3 caracteres';
    end if;

    if v_username !~ '^[a-z0-9_\.]+$' then
        raise exception 'Usa solo letras, números, punto o guion bajo';
    end if;

    select email into v_email
    from auth.users
    where id = v_user_id;

    v_display_name := v_username;

    update public.usuarios
    set email = coalesce(public.usuarios.email, v_email),
        display_name = coalesce(nullif(btrim(public.usuarios.display_name), ''), v_display_name),
        username = coalesce(nullif(btrim(public.usuarios.username::text), ''), v_username)
    where id = v_user_id;

    if not found then
        insert into public.usuarios (id, email, display_name, username)
        values (v_user_id, v_email, v_display_name, v_username);
    end if;

    update auth.users
    set raw_user_meta_data = coalesce(raw_user_meta_data, '{}'::jsonb) || jsonb_build_object('username', v_username)
    where id = v_user_id;

    return v_username;
end;
$$;

grant execute on function public.update_my_username(text) to authenticated;

commit;

