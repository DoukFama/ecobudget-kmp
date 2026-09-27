package com.example.utils

import platform.Foundation.NSUUID

/**
 * Implémentation iOS de [generateUUID].
 * iOS n'embarque pas la bibliothèque Java : on utilise la structure native `NSUUID`.
 */
actual fun generateUUID(): String = NSUUID().UUIDString()
