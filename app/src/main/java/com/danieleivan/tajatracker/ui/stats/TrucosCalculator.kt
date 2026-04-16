package com.danieleivan.tajatracker.ui.stats

import com.danieleivan.tajatracker.data.model.ConsumicionRow
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class TrucoProgress(
    val nombre: String,
    val descripcion: String,
    val veces: Int,
    val trackeable: Boolean = true,
    val nota: String? = null
)

data class TrucosSummary(
    val totalActivaciones: Int,
    val trucosDesbloqueados: Int,
    val detalles: List<TrucoProgress>
)

fun calculateTrucosSummary(
    rows: List<ConsumicionRow>,
    hidalgoTotal: Int = 0,
    malacopaTotal: Int = 0
): TrucosSummary {
    val groupedByDay = rows.groupBy { row ->
        row.fechaHora
            ?.let(::parseLocalDate)
            ?: LocalDate.MIN
    }

    val tripleC = groupedByDay.count { (_, dayRows) ->
        dayRows.any { it.isBeer() } &&
            dayRows.any { it.isCubata() } &&
            dayRows.any { it.isShot() }
    }

    val hatTrick = groupedByDay.count { (_, dayRows) ->
        val bases = dayRows
            .asSequence()
            .filter { it.isCubata() }
            .map { it.alcoholBase.trim().lowercase() }
            .toSet()
        bases.contains("ron") &&
            bases.contains("whisky") &&
            (bases.contains("jaggermeister") || bases.contains("jagermeister"))
    }

    val reyesMagos = groupedByDay.count { (_, dayRows) ->
        dayRows.any { it.isOrangeCubata() } &&
            dayRows.any { it.isWhiteCubata() } &&
            dayRows.any { it.isBrownCubata() }
    }

    val goleador = groupedByDay.count { (_, dayRows) -> dayRows.size > 5 }

    val borracho = groupedByDay.count { (_, dayRows) ->
        dayRows.any { it.isWine() } && dayRows.any { it.isBeer() }
    }

    val sinverguenza = rows.count { it.esRobado && it.isCubata() }

    val detalles = listOf(
        TrucoProgress(
            nombre = "Triple C",
            descripcion = "Cerveza + Cubata + Chupito en el mismo dia",
            veces = tripleC
        ),
        TrucoProgress(
            nombre = "Hat Trick",
            descripcion = "Ron + Whisky + Jaggermeister en cubatas el mismo dia",
            veces = hatTrick
        ),
        TrucoProgress(
            nombre = "Reyes Magos",
            descripcion = "Cubata Naranja + Blanco + Marron en el mismo dia",
            veces = reyesMagos
        ),
        TrucoProgress(
            nombre = "Hidalgo",
            descripcion = "Cubata de 1 solo buche",
            veces = hidalgoTotal.coerceAtLeast(0)
        ),
        TrucoProgress(
            nombre = "Goleador",
            descripcion = "Mas de 5 bebidas en un dia",
            veces = goleador
        ),
        TrucoProgress(
            nombre = "Eso es de borracho",
            descripcion = "Vino + Cerveza en el mismo dia",
            veces = borracho
        ),
        TrucoProgress(
            nombre = "Malacopa",
            descripcion = "Seguir bebiendo despues de potar",
            veces = malacopaTotal.coerceAtLeast(0)
        ),
        TrucoProgress(
            nombre = "Sinverguenza",
            descripcion = "Robar una copa",
            veces = sinverguenza
        )
    )

    return TrucosSummary(
        totalActivaciones = detalles.sumOf { it.veces },
        trucosDesbloqueados = detalles.count { it.veces > 0 },
        detalles = detalles
    )
}

private fun parseLocalDate(value: String): LocalDate? {
    return runCatching { OffsetDateTime.parse(value).toLocalDate() }
        .recoverCatching {
            Instant.parse(value)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }
        .getOrNull()
}

private fun ConsumicionRow.isCubata(): Boolean {
    return formato.equals("copa", ignoreCase = true) || formato.equals("garrafa", ignoreCase = true)
}

private fun ConsumicionRow.isBeer(): Boolean = formato.equals("cerveza", ignoreCase = true)

private fun ConsumicionRow.isShot(): Boolean = formato.equals("chupito", ignoreCase = true)

private fun ConsumicionRow.isWine(): Boolean = formato.equals("vino", ignoreCase = true)

private fun ConsumicionRow.isOrangeCubata(): Boolean {
    return isCubata() && mezcla.equals("naranja", ignoreCase = true)
}

private fun ConsumicionRow.isWhiteCubata(): Boolean {
    if (!isCubata()) return false
    val normalizedMixer = mezcla?.trim()?.lowercase().orEmpty()
    return normalizedMixer == "limon" || normalizedMixer == "tonica" || normalizedMixer == "energetica"
}

private fun ConsumicionRow.isBrownCubata(): Boolean {
    return isCubata() && mezcla.equals("cola", ignoreCase = true)
}

