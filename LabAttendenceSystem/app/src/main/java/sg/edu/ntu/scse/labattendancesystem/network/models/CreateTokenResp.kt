package sg.edu.ntu.scse.labattendancesystem.network.models

import java.time.OffsetDateTime

data class CreateTokenResp(val token: String, val expiry: OffsetDateTime)