package sg.edu.ntu.scse.labattendancesystem.network

open class ApiException(msg: String = "network API error") : Exception(msg) {}

class UnauthenticatedError() : ApiException("unauthorized") {}
