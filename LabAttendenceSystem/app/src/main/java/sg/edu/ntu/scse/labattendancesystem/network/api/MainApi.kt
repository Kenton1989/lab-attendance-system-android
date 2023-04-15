package sg.edu.ntu.scse.labattendancesystem.network.api

import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sg.edu.ntu.scse.labattendancesystem.network.models.GroupResp
import sg.edu.ntu.scse.labattendancesystem.network.models.GroupStudentResp
import sg.edu.ntu.scse.labattendancesystem.network.models.PaginatedListResp
import sg.edu.ntu.scse.labattendancesystem.network.models.SessionResp
import java.time.LocalDateTime

interface MainApi {
    @GET("sessions?fields=id,group,start_datetime,end_datetime")
    suspend fun getSessions(
        @Query("group") groupId: Int? = null,
        @Query("course") courseId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("start_datetime_after") minStartDateTime: LocalDateTime? = null,
        @Query("start_datetime_before") maxStartDateTime: LocalDateTime? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<SessionResp>

    @GET("groups/{id}?fields=id,name,course,lab,room_no,teachers")
    suspend fun getGroup(@Path("id") id: Int): GroupResp

    @GET("group_students?fields=id,group_id,student,seat")
    suspend fun getStudentsOfGroup(
        @Query("group") groupId: Int,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): List<GroupStudentResp>
}