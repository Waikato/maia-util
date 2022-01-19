package maia.util

/**
 * Global debug flag for the core MOANA library.
 *
 * TODO: Remove globalness.
 */

/** Global debug flag. */
const val DEBUG = false

/**
 * Prints a message if debugging is on.
 *
 * @param message   The message to print.
 */
fun debug(message : String) {
    if (DEBUG) print(message)
}

/**
 * Prints a message and line separator if debugging is on.
 *
 * @param message   The message to print.
 */
fun debugln(message : String) {
    if (DEBUG) println(message)
}
