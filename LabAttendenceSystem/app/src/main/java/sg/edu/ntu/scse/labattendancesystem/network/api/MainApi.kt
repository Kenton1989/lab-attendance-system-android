package sg.edu.ntu.scse.labattendancesystem.network.api

import retrofit2.http.GET
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

    @GET("student_attendances?fields=id,session,attender,check_in_state,check_in_datetime,last_modify")
    suspend fun getStudentAttendances(
        @Query("course") courseId: Int? = null,
        @Query("group") groupId: Int? = null,
        @Query("session") sessionId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("check_in_state") checkInState: String? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<AttendanceResp>

    @GET("teacher_attendances?fields=id,session,attender,check_in_state,check_in_datetime,last_modify")
    suspend fun getTeacherAttendances(
        @Query("course") courseId: Int? = null,
        @Query("group") groupId: Int? = null,
        @Query("session") sessionId: Int? = null,
        @Query("lab") labId: Int? = null,
        @Query("check_in_state") checkInState: String? = null,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<AttendanceResp>

    @GET("groups/{id}?fields=id,name,course,lab,room_no,teachers")
    suspend fun getGroup(@Path("id") id: Int): GroupResp

    @GET("groups/{id}?fields=id,teachers")
    suspend fun getGroupTeachers(@Path("id") groupId: Int): GroupResp

    @GET("groups/{id}?fields=id,teacher_ids")
    suspend fun getGroupTeacherIds(@Path("id") groupId: Int): GroupResp

    @GET("group_students?fields=id,group_id,student,seat")
    suspend fun getStudentsOfGroup(
        @Query("group") groupId: Int,
        @Query("limit") pageLimit: Int = 200,
        @Query("offset") pageOffset: Int = 0,
        @Query("is_active") isActive: Boolean? = true,
    ): PaginatedListResp<GroupStudentResp>
}