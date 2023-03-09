package sg.edu.ntu.scse.labattendancesystem.repository

class OperationTimeoutError(): Exception("operation timeout") {}
class UserIsNotLabError() : Exception("user is not a lab") {}
class InvalidLabRoomNumber(val roomNo: Int, val maxRoomNo: Int) : Exception("invalid lab room number: $roomNo (expecting: 1~$maxRoomNo)") {}