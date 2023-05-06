package sg.edu.ntu.scse.labattendancesystem.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.*
import sg.edu.ntu.scse.labattendancesystem.LabAttendanceSystemApplication
import sg.edu.ntu.scse.labattendancesystem.database.models.DbStudentAttendance
import sg.edu.ntu.scse.labattendancesystem.database.models.DbTeacherAttendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDbStudentAttendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.toDbTeacherAttendance
import sg.edu.ntu.scse.labattendancesystem.domain.models.toNewAttendanceReq
import sg.edu.ntu.scse.labattendancesystem.domain.models.toUpdateAttendanceReq
import sg.edu.ntu.scse.labattendancesystem.network.models.AttendanceResp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

abstract class AttendanceSyncer<AttendanceT>(
    private val app: LabAttendanceSystemApplication,
    private val lastDownloadTimeKey: Preferences.Key<String>,
    private val lastUploadTimeKey: Preferences.Key<String>,
) : TimestampBasedSyncer<AttendanceResp, AttendanceT>(
    INIT_SYNC_TIMESTAMP, INIT_SYNC_TIMESTAMP
) {
    companion object {
        private val INIT_SYNC_TIMESTAMP: ZonedDateTime =
            ZonedDateTime.parse("2000-01-01T00:00:00+08:00")
    }

    protected val db = app.database.mainDao()
    protected val api = app.apiServices.main

    private val syncStore = app.syncPreferenceDataStore

    override suspend fun getSavedLastUploadTime(): ZonedDateTime {
        return readSyncTime(lastUploadTimeKey)
    }

    override suspend fun getSavedLastDownloadTime(): ZonedDateTime {
        return readSyncTime(lastDownloadTimeKey)
    }

    override suspend fun saveLastUploadTime(timestamp: ZonedDateTime) {
        writeSyncTime(lastUploadTimeKey, timestamp)
    }

    override suspend fun saveLastDownloadTime(timestamp: ZonedDateTime) {
        writeSyncTime(lastDownloadTimeKey, timestamp)
    }

    override fun getRemoteLastModify(item: AttendanceResp): ZonedDateTime = item.lastModify!!

    private suspend fun writeSyncTime(key: Preferences.Key<String>, value: ZonedDateTime) {
        app.syncPreferenceDataStore.edit {
            it[key] = value.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    private suspend fun readSyncTime(key: Preferences.Key<String>): ZonedDateTime {
        return app.syncPreferenceDataStore.data
            .map {
                val s = it[key]
                if (s == null) INIT_SYNC_TIMESTAMP
                else ZonedDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME)
            }
            .first()
    }

    class Student(
        app: LabAttendanceSystemApplication,
        private val labId: Int,
    ) : AttendanceSyncer<DbStudentAttendance>(
        app,
        LAST_DOWNLOAD_TIME_KEY,
        LAST_UPLOAD_TIME_KEY,
    ) {
        companion object {
            private val LAST_DOWNLOAD_TIME_KEY =
                stringPreferencesKey("student_attendance_last_download_time")
            private val LAST_UPLOAD_TIME_KEY =
                stringPreferencesKey("student_attendance_last_upload_time")
//            private val TAG = "${AttendanceSyncer::class.java.simpleName}.${Student::class.java.simpleName}"
        }

        override suspend fun getRemoteDataUpdatedAfter(timestamp: ZonedDateTime): Collection<AttendanceResp> {
            return api.getStudentAttendance(
                labId = labId,
                lastModifyAfter = timestamp
            ).results
        }

        override suspend fun getRemoteItem(item: DbStudentAttendance): AttendanceResp? {
            return if (item.id == null) {
                val res = api.getStudentAttendance(
                    sessionId = item.sessionId,
                    attenderId = item.attenderId,
                    pageLimit = 1,
                )
                if (res.count > 0) res.results.first()
                else null
            } else {
                api.getStudentAttendance(item.id)
            }
        }

        override suspend fun getLocalDataUpdatedAfter(timestamp: ZonedDateTime): Collection<DbStudentAttendance> {
            return db.getRawStudentAttendances(lastModifyAfter = timestamp).first()
        }

        override suspend fun getLocalItem(item: AttendanceResp): DbStudentAttendance? {
            val res = db.getRawStudentAttendances(
                attenderId = item.attenderId,
                sessionId = item.sessionId,
            ).first()
            return if (res.isEmpty()) null else res.first()
        }

        override fun getLocalLastModify(item: DbStudentAttendance): ZonedDateTime = item.lastModify


        override suspend fun insertToRemoteItem(data: DbStudentAttendance) {
            api.createStudentAttendance(data.toNewAttendanceReq())
        }

        override suspend fun updateToRemoteItem(data: DbStudentAttendance) {
            api.updateStudentAttendance(data.id!!, data.toUpdateAttendanceReq())
        }

        override suspend fun insertToLocalItem(data: Collection<AttendanceResp>) {
            val validAttendances = data.asFlow()
                .filter { db.hasSession(it.sessionId!!).first() }
                .map { it.toDbStudentAttendance() }
                .toList()
            db.insertStudentAttendances(validAttendances)
        }

        override suspend fun updateToLocalItem(data: Collection<AttendanceResp>) {
            db.updateStudentAttendances(data.map { it.toDbStudentAttendance() })
        }

        override fun isNewLocalItem(item: DbStudentAttendance): Boolean = item.id == null

        override suspend fun deleteLocalItem(item: DbStudentAttendance) {
            db.deleteStudentAttendances(listOf(item))
        }
    }

    class Teacher(
        app: LabAttendanceSystemApplication,
        private val labId: Int,
    ) : AttendanceSyncer<DbTeacherAttendance>(
        app,
        LAST_DOWNLOAD_TIME_KEY,
        LAST_UPLOAD_TIME_KEY,
    ) {
        companion object {
            private val LAST_DOWNLOAD_TIME_KEY =
                stringPreferencesKey("teacher_attendance_last_download_time")
            private val LAST_UPLOAD_TIME_KEY =
                stringPreferencesKey("teacher_attendance_last_upload_time")
        }


        override suspend fun getRemoteDataUpdatedAfter(timestamp: ZonedDateTime): Collection<AttendanceResp> {
            return api.getTeacherAttendance(
                labId = labId,
                lastModifyAfter = timestamp
            ).results
        }

        override suspend fun getRemoteItem(item: DbTeacherAttendance): AttendanceResp? {
            return if (item.id == null) {
                val res = api.getTeacherAttendance(
                    sessionId = item.sessionId,
                    attenderId = item.attenderId,
                    pageLimit = 1,
                )
                if (res.count > 0) res.results.first()
                else null
            } else {
                api.getTeacherAttendance(item.id)
            }
        }

        override suspend fun getLocalDataUpdatedAfter(timestamp: ZonedDateTime): Collection<DbTeacherAttendance> {
            return db.getRawTeacherAttendances(lastModifyAfter = timestamp).first()
        }

        override suspend fun getLocalItem(item: AttendanceResp): DbTeacherAttendance? {
            val res = db.getRawTeacherAttendances(
                attenderId = item.attenderId,
                sessionId = item.sessionId,
            ).first()
            return if (res.isEmpty()) null else res.first()
        }

        override fun getLocalLastModify(item: DbTeacherAttendance): ZonedDateTime = item.lastModify

        override suspend fun insertToRemoteItem(data: DbTeacherAttendance) {
            api.createTeacherAttendance(data.toNewAttendanceReq())
        }

        override suspend fun updateToRemoteItem(data: DbTeacherAttendance) {
            api.updateTeacherAttendance(data.id!!, data.toUpdateAttendanceReq())
        }

        override suspend fun insertToLocalItem(data: Collection<AttendanceResp>) {
            val validAttendances = data.asFlow()
                .filter { db.hasSession(it.sessionId!!).first() }
                .map { it.toDbTeacherAttendance() }
                .toList()
            db.insertTeacherAttendances(validAttendances)
        }

        override suspend fun updateToLocalItem(data: Collection<AttendanceResp>) {
            db.updateTeacherAttendances(data.map { it.toDbTeacherAttendance() })
        }

        override fun isNewLocalItem(item: DbTeacherAttendance): Boolean = item.id == null

        override suspend fun deleteLocalItem(item: DbTeacherAttendance) {
            db.deleteTeacherAttendances(listOf(item))
        }
    }
}