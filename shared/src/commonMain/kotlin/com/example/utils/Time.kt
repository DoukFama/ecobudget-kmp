package com.example.utils

/**
 * Déclaration `expect` de l'horodatage courant en millisecondes.
 *
 * `System.currentTimeMillis()` est une API de la machine virtuelle Java, interdite
 * dans `commonMain`. Chaque cible fournit l'accès à son horloge native.
 */
expect fun getCurrentTimeMillis(): Long
