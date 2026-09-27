package com.example.utils

/**
 * Déclaration `expect` de la génération d'un identifiant unique (UUID).
 *
 * `java.util.UUID` n'existe pas dans le framework Foundation d'iOS : impossible de
 * l'appeler depuis `commonMain`. Chaque plateforme fournit donc son implémentation
 * native dans son propre *source set* (`androidMain` / `iosMain`).
 */
expect fun generateUUID(): String
