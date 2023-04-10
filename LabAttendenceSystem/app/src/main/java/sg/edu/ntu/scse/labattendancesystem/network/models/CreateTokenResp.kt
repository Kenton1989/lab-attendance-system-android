package sg.edu.ntu.scse.labattendancesystem.network.models

import java.time.LocalDateTime

data class CreateTokenResp(val token: String, val expiry: LocalDateTime)