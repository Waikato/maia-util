package māia.util

sealed class Result<out T, out E>

class Success<out T>(
    val value: T
) : Result<T, Nothing>()

class Failure<out E>(
    val error: E
) : Result<Nothing, E>()
