package maia.util

/*
 * Utilities for working with strings.
 */

/**
 * Returns the given string repeated the given number
 * of times.
 *
 * @param string
 *          The string to repeat.
 * @param times
 *          The number of times to repeat the string.
 * @return
 *          The repetitive string.
 */
inline fun repeatString(string: String, times: Int): String {
    // If repeating zero times, the result is always the empty string
    if (times == 0) return ""

    require(times > 0) {
        "Can't repeat a string a negative number of times (${times})"
    }

    // The empty string repeated any number of times is the empty string
    if (string == "") return ""

    require(times <= Int.MAX_VALUE / string.length) {
        "Repeating '${string}' $times times would overflow the resulting string"
    }

    return buildString(string.length * times) {
        repeat(times) {
            append(string)
        }
    }
}

/**
 * Extension function which defines string-multiplication
 * in the Python way (the same string repeated the given
 * number of times).
 *
 * @receiver
 *          The string to repeat.
 * @param times
 *          The number of times to repeat the string.
 * @return
 *          The repetitive string.
 */
inline operator fun String.times(times : Int) : String = repeatString(this, times)

/**
 * Extension function which defines string-multiplication
 * in the Python way (the same string repeated the given
 * number of times).
 *
 * @receiver
 *          The number of times to repeat the string.
 * @param string
 *          The string to repeat.
 * @return
 *          The repetitive string.
 */
inline operator fun Int.times(string : String) : String = repeatString(string, this)

/**
 * Indents a string.
 *
 * @receiver
 *          The string to indent.
 * @param amount
 *          The number of times to indent.
 * @param indentation
 *          The string to use to indent with.
 * @return
 *          The indented string.
 */
fun String.indent(amount : Int = 1, indentation : String = " ") : String {
    val finalIndentation = repeatString(indentation, amount)
    return split("\n").joinToString("\n") {
        finalIndentation + it
    }
}
