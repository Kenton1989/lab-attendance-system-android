package sg.edu.ntu.scse.labattendancesystem.network.models

import java.util.Date

data class CreateTokenResponse(val token: String, val expiry: Date)