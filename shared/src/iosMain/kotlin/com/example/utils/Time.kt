package com.example.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * Implémentation iOS de [getCurrentTimeMillis].
 * `NSDate` exprime le temps en secondes depuis l'epoch Unix.
 */
actual fun getCurrentTimeMillis(): Long =
  (NSDate().timeIntervalSince1970 * 1000).toLong()
