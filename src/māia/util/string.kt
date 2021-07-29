package māia.util

/*
 * Utilities for working with strings.
 */

/**
 * Extension functions which defines string-multiplication
 * in the Python way (the same string repeated the given
 * number of times).
 *
 * @receiver    The string to repeat.
 * @param x     The number of times to repeat the string.
 * @return      The repeated string.
 */
operator fun String.times(x : Int) : String {
    if (x < 0) throw IllegalArgumentException("Can't multiply a string a negative number of times ($x)")
    return buildString {
        for (i in 0 until x) append(this@times)
    }
}

/**
 * Extension functions which defines string-multiplication
 * in the Python way (the same string repeated the given
 * number of times).
 *
 * @receiver    The number of times to repeat the string.
 * @param x     The string to repeat.
 * @return      The repeated string.
 */
operator fun Int.times(x : String) : String = x * this

/**
 * Indents a string.
 *
 * @receiver            The string to indent.
 * @param amount        The number of times to indent.
 * @param indentation   The string to use to indent with.
 * @return              The indented string.
 */
fun String.indent(amount : Int = 1, indentation : String = " ") : String {
    val finalIndentation = amount * indentation
    return split("\n").joinToString("\n") {
        finalIndentation + it
    }
}
