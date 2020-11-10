package hpsaturn.pollutionreporter.core.domain.errors

class ServerException(message: String = "") : Exception(message)
class PermissionException(message: String = "") : Exception(message)
class PermissionNotGrantedException(message: String = "") : Exception(message)
class ConnectionException(message: String = "") : Exception(message)
class UnexpectedException(message: String = "") : Exception(message)