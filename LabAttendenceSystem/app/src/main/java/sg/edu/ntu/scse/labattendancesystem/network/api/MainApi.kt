package sg.edu.ntu.scse.labattendancesystem.network.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import sg.edu.ntu.scse.labattendancesystem.network.models.*
import java.time.ZonedDateTime

interface MainApi {
    @GET("sessions/{id}?fields=id,group,start_datetime,end_datetime,is_compulsory,allow_late_check_in,check_in_deadline_mins")
    suspend fun getSession(@Path("id") id: Int): SessionResp

    @GET("sessions?fields=id,group,start_datetime,end_datetime,is_compulsory,allow_late_check_in,check_in_deadline_mins")
    suspend fun getSessions(
        @Query("group") groupId: Int? = null,
        @Query("course") courseId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("start_datetime_after") startDateTimeAfter: ZonedDateTime? = null,
        @Query("start_datetime_before") startDateTimeBefore: ZonedDateTime? = null,
        @Query("end_datetime_after") endDateTimeAfter: ZonedDateTime? = null,
        @Query("end_datetime_before") endDateTimeBefore: ZonedDateTime? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<SessionResp>

    @GET("student_attendances?fields=id,session,session_id,attender,attender_id,check_in_state,check_in_datetime,last_modify")
    suspend fun getStudentAttendance(
        @Query("course") courseId: Int? = null,
        @Query("group") groupId: Int? = null,
        @Query("session") sessionId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("attender") attenderId: Int? = null,
        @Query("check_in_state") checkInState: String? = null,
        @Query("last_modify_before") lastModifyBefore: ZonedDateTime? = null,
        @Query("last_modify_after") lastModifyAfter: ZonedDateTime? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<AttendanceResp>

    @GET("student_attendances/{id}")
    suspend fun getStudentAttendance(@Path("id") id: Int): AttendanceResp

    @POST("student_attendances")
    suspend fun createStudentAttendance(@Body body: NewAttendanceReq): AttendanceResp

    @PATCH("student_attendances/{id}")
    suspend fun updateStudentAttendance(
        @Path("id") id: Int,
        @Body body: UpdateAttendanceReq,
    ): AttendanceResp

    @GET("teacher_attendances?fields=id,session,session_id,attender,attender_id,check_in_state,check_in_datetime,last_modify")
    suspend fun getTeacherAttendance(
        @Query("course") courseId: Int? = null,
        @Query("group") groupId: Int? = null,
        @Query("session") sessionId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("attender") attenderId: Int? = null,
        @Query("check_in_state") checkInState: String? = null,
        @Query("last_modify_before") lastModifyBefore: ZonedDateTime? = null,
        @Query("last_modify_after") lastModifyAfter: ZonedDateTime? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<AttendanceResp>

    @GET("teacher_attendances/{id}")
    suspend fun getTeacherAttendance(@Path("id") id: Int): AttendanceResp

    @POST("teacher_attendances")
    suspend fun createTeacherAttendance(@Body body: NewAttendanceReq): AttendanceResp

    @PATCH("teacher_attendances/{id}")
    suspend fun updateTeacherAttendance(
        @Path("id") id: Int,
        @Body body: UpdateAttendanceReq,
    ): AttendanceResp

    @GET("groups/{id}?fields=id,name,course,lab,room_no,teachers")
    suspend fun getGroup(@Path("id") id: Int): GroupResp

    @GET("groups/{id}?fields=id,teachers")
    suspend fun getGroupTeachers(@Path("id") groupId: Int): GroupResp

    @GET("group_students?fields=id,group_id,student,seat")
    suspend fun getStudentsOfGroup(
        @Query("group") groupId: Int,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<GroupStudentResp>
}