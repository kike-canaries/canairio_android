package hpsaturn.pollutionreporter.core.domain.entities

sealed class Result<out T : Any> {
    override fun toString(): String = when (this) {
        is Success<*> -> "Success[data=$data]"
        is ErrorResult -> "Error[exception=$exception]"
        InProgress -> "Loading"
    }
}

data class Success<out T : Any>(val data: T) : Result<T>()
data class ErrorResult(val exception: Throwable) : Result<Nothing>()
object InProgress : Result<Nothing>()
