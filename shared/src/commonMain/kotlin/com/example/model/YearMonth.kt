package com.example.model

import com.example.utils.getCurrentTimeMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Modèle immuable représentant un mois spécifique pour la navigation budgétaire.
 *
 * Réécrit sans `java.util.Calendar` ni `java.text.SimpleDateFormat` (interdits dans
 * `commonMain`) grâce à la bibliothèque multiplateforme **kotlinx-datetime**.
 *
 * @property year Année (ex: 2026).
 * @property month Index du mois de 0 (Janvier) à 11 (Décembre).
 */
data class YearMonth(
    val year: Int,
    val month: Int
) {
    /**
     * Libellé formaté en français (ex: "Août 2026").
     */
    val displayLabel: String
        get() = "${FRENCH_MONTH_NAMES[month]} $year"

    /**
     * Retourne le YearMonth précédent.
     */
    fun previous(): YearMonth {
        return if (month == 0) {
            YearMonth(year - 1, 11)
        } else {
            YearMonth(year, month - 1)
        }
    }

    /**
     * Retourne le YearMonth suivant.
     */
    fun next(): YearMonth {
        return if (month == 11) {
            YearMonth(year + 1, 0)
        } else {
            YearMonth(year, month + 1)
        }
    }

    /**
     * Vérifie si un timestamp millisecondes appartient à ce mois précis.
     */
    fun containsTimestamp(timestamp: Long): Boolean {
        val dateTime = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return dateTime.year == year && dateTime.month.number - 1 == month
    }

    companion object {
        private val FRENCH_MONTH_NAMES = listOf(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        )

        /**
         * Crée le YearMonth courant à partir de l'horloge native.
         */
        fun current(): YearMonth {
            val dateTime = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                .toLocalDateTime(TimeZone.currentSystemDefault())
            return YearMonth(year = dateTime.year, month = dateTime.month.number - 1)
        }

        /**
         * Crée le YearMonth correspondant à un timestamp.
         */
        fun fromTimestamp(timestamp: Long): YearMonth {
            val dateTime = Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            return YearMonth(year = dateTime.year, month = dateTime.month.number - 1)
        }
    }
}
