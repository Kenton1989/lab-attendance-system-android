package sg.edu.ntu.scse.labattendancesystem.network.models

import java.time.ZonedDateTime

data class CreateTokenResp(val token: String, val expiry: ZonedDateTime)