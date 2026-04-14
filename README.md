# DrunkWrapped (Android)

Pantalla principal en Jetpack Compose orientada a accesibilidad extrema para registrar consumiciones:

- Modo oscuro obligatorio
- Alto contraste
- Textos grandes
- Botones gigantes (objetivos tactiles >= 84dp)

Tambien incluye una pantalla de estadisticas estilo Wrapped con animaciones y resumen de consumo.

## Flujo de registro

1. Elegir formato: Copa, Chupito, Cerveza, Vino, Garrafa.
2. Elegir alcohol base.
3. Si el formato es Copa o Garrafa, elegir refresco de mezcla.
4. Elegir si lleva hielo con controles grandes SI/NO.
5. Pulsar **REGISTRAR**.

## Estructura principal

- `app/src/main/java/com/danieleivan/tajatracker/MainActivity.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/home/DrunkWrappedHomeScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/home/DrunkWrappedHomeViewModel.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/stats/WrappedStatsScreen.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/stats/WrappedStatsViewModel.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/ConsumicionInsert.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/model/ConsumicionRow.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/repository/ConsumicionesRepository.kt`
- `app/src/main/java/com/danieleivan/tajatracker/data/remote/SupabaseProvider.kt`
- `app/src/main/java/com/danieleivan/tajatracker/ui/theme/Theme.kt`

## Configuracion Supabase local

En `local.properties`:

```ini
SUPABASE_URL=https://TU-PROJECT-REF.supabase.co
SUPABASE_ANON_KEY=TU_ANON_KEY

SUPABASE_DB_HOST=aws-0-eu-west-1.pooler.supabase.com
SUPABASE_DB_PORT=6543
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres.rmzjdpnbczkvuyxshpjh
```

La app inserta en la tabla `consumiciones` usando el SDK de Supabase Kotlin (PostgREST).

## Ejecutar tests rapidos

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## Ejecutar app

```powershell
.\gradlew.bat :app:assembleDebug
```

