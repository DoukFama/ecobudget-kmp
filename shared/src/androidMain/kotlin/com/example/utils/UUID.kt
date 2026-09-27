package com.example.utils

import java.util.UUID

/**
 * Implémentation Android de [generateUUID].
 * L'environnement Android tourne sur une machine virtuelle Java classique.
 */
actual fun generateUUID(): String = UUID.randomUUID().toString()
