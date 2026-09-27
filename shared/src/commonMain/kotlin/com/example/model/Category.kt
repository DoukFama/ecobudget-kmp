package com.example.model

/**
 * Représente les catégories obligatoires pour la classification des dépenses dans EcoBudget.
 *
 * Le libellé localisé n'est plus porté par `labelResId: Int` (identifiant de la classe `R`
 * d'Android, non multiplateforme). La correspondance vers le catalogue de ressources
 * partagé est exposée par `com.example.resources.labelRes`.
 */
enum class Category(
    val emoji: String
) {
    TRANSPORT("🚌"),
    ALIMENTATION("🍱"),
    LOISIRS("🎾"),
    LOGEMENT("🏠")
}
