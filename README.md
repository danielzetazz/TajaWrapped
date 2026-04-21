# DrunkWrapped

DrunkWrapped es una app Android en **Kotlin + Jetpack Compose + Material 3** para registrar noches de bebida y ver su recap con una estética **Speakeasy premium**: oscura, elegante, discreta y con acentos dorados/bronce.

## Qué hace la app

- Registro rápido de consumiciones por formato:
  - Copa
  - Chupito
  - Cerveza
  - Vino
  - Garrafa
- Registro de noches/sesiones completas con:
  - lugar
  - fecha elegida antes de guardar
  - cubatas Hidalgo
  - vómitos
- Recap estilo Wrapped con:
  - filtros por **7 días**, **30 días** e **histórico**
  - resumen de trucos
  - resumen de lugares
  - lista de registros del periodo
  - detalle de cada registro al pulsarlo
- Autenticación real con **Supabase Auth** usando:
  - login por **usuario**
  - registro separado con **correo + usuario + contraseña**
  - sesión persistente entre aperturas
- Pantalla de ajustes con opciones de cuenta y experiencia.

## Línea visual

La UI sigue una identidad nocturna y premium:

- fondo oscuro profundo
- superficies sobrias y cálidas
- acentos dorados/bronce suavizados
- tipografía serif/sans-serif con jerarquía elegante
- botones y cards más refinados
- diálogos y resúmenes coherentes con el resto de la app

## Funcionalidades destacadas

### Autenticación

- Login con **usuario** y contraseña
- Registro separado con **correo**, **usuario** y contraseña
- Sesión persistente usando Supabase Auth
- Recuperación de contraseña

### Ajustes

La pantalla de ajustes permite:

- cerrar sesión
- cambiar el nombre de usuario de la cuenta
- activar/desactivar confirmación antes de registrar un día
- mantener otros ajustes visuales y de experiencia

### Registro de noche

Antes de registrar un día se puede indicar:

- lugar donde se ha bebido
- fecha del registro
- número de consumiciones
- cubatas Hidalgo
- vómitos
- detalle de bebidas robadas sin precio

### Recap / Wrapped

El apartado de estadísticas muestra:

- gastos totales
- ahorro estimado
- bebida más consumida
- total de chupitos
- resumen de trucos
- resumen de lugares
- lista de registros del periodo
- detalle por registro con su desglose

## Estructura principal

- `app/src/main/java/com/danieleivan/tajatracker/MainActivity.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/auth/AuthScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/auth/RegisterScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/auth/AuthViewModel.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/repository/AuthRepository.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/home/DrunkWrappedHomeScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/home/DrunkWrappedHomeViewModel.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/home/DrinkDraft.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/components/PremiumComponents.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/stats/WrappedStatsScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/stats/WrappedStatsViewModel.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/stats/TrucosCalculator.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/ConsumicionInsert.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/ConsumicionRow.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/RegistroInsert.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/RegistroRow.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/repository/ConsumicionesRepository.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/remote/SupabaseProvider.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/theme/Color.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/theme/Type.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/theme/Shape.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/theme/Theme.kt`

## Base de datos Supabase

### Migraciones actuales

- `supabase/migrations/20260416_03_add_registros_and_hidalgo_malacopa.sql`
- `supabase/migrations/20260421_04_auth_username_split_flow.sql`
- `supabase/migrations/20260421_05_update_my_username_function.sql`

### Tablas y piezas principales

- `consumiciones`
  - consumiciones individuales
  - relación con `registro_id`
  - lugar y fecha del consumo
- `registros`
  - sesión/noche completa
  - lugar
  - fecha
  - cubatas Hidalgo totales
  - vómitos totales
- `usuarios`
  - perfil de usuario
  - email
  - username único
  - display name

## Flujo de registro

1. El usuario elige el formato de bebida.
2. Elige cantidad.
3. Si aplica, selecciona alcohol base, mezcla y hielo.
4. Puede marcar qué cubatas son Hidalgo.
5. Indica lugar y fecha del registro.
6. Ve un resumen previo antes de guardar.
7. Confirma el registro y se guarda en Supabase.

## Flujo de login

1. El usuario entra con su **usuario** y contraseña.
2. La app resuelve el email asociado en `usuarios`.
3. Supabase Auth inicia sesión con email/password por debajo.
4. La sesión queda guardada y se restaura automáticamente en futuras aperturas.

## Configuración local

En `local.properties`:

```ini
SUPABASE_URL=https://TU-PROJECT-REF.supabase.co
SUPABASE_ANON_KEY=TU_ANON_KEY

SUPABASE_DB_HOST=aws-0-eu-west-1.pooler.supabase.com
SUPABASE_DB_PORT=6543
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres.rmzjdpnbczkvuyxshpjh
```

## Aplicar migraciones

Si usas Supabase CLI:

```powershell
supabase db push
```

Si prefieres hacerlo manualmente, copia los SQL de `supabase/migrations/` en el SQL Editor de Supabase en el orden correcto.

## Ejecutar tests rápidos

```powershell
.
gradlew.bat :app:testDebugUnitTest
```

## Compilar la app

```powershell
.
gradlew.bat :app:assembleDebug
```

## Notas

- La app está orientada a uso real con pantalla oscura obligatoria.
- El estilo visual está pensado para verse premium sin parecer un dashboard genérico.
- Si quieres adaptar más pantallas, la base visual ya está preparada para seguir creciendo.
