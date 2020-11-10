package hpsaturn.pollutionreporter.core.domain.entities

sealed class Result<out T : Any>
data class Success<out T : Any>(val data: T) : Result<T>()
data class ErrorResult(val exception: Throwable) : Result<Nothing>()
object InProgress : Result<Nothing>()
