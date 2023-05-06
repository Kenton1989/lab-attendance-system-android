package sg.edu.ntu.scse.labattendancesystem.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.HttpException
import java.time.Duration
import java.time.ZonedDateTime

abstract class TimestampBasedSyncer<RemoteT, LocalT>(
    initLastDownloadTime: ZonedDateTime,
    initLastUploadTime: ZonedDateTime
) {
    companion object {
        private val FRESHNESS_BUFFER_TIME: Duration = Duration.ofMinutes(1)
        private val TAG: String = TimestampBasedSyncer::class.java.simpleName
    }

    private var lastUploadTime: ZonedDateTime = initLastUploadTime
        private set(newTime) {
            synchronized(this) {
                if (newTime < field) {
                    return
                }
                field = newTime
            }
        }

    private var lastDownloadTime: ZonedDateTime = initLastDownloadTime
        private set(newTime) {
            synchronized(this) {
                if (newTime < field) {
                    return
                }
                field = newTime
            }
        }

    private val _syncing = MutableStateFlow(false)

    protected abstract suspend fun getSavedLastUploadTime(): ZonedDateTime
    protected abstract suspend fun getSavedLastDownloadTime(): ZonedDateTime
    protected abstract suspend fun saveLastUploadTime(timestamp: ZonedDateTime)
    protected abstract suspend fun saveLastDownloadTime(timestamp: ZonedDateTime)

    protected abstract suspend fun getRemoteDataUpdatedAfter(timestamp: ZonedDateTime): Collection<RemoteT>
    protected abstract suspend fun getRemoteItem(item: LocalT): RemoteT?
    protected abstract fun getRemoteLastModify(item: RemoteT): ZonedDateTime

    protected abstract suspend fun getLocalDataUpdatedAfter(timestamp: ZonedDateTime): Collection<LocalT>
    protected abstract suspend fun getLocalItem(item: RemoteT): LocalT?
    protected abstract suspend fun deleteLocalItem(item: LocalT)
    protected abstract fun getLocalLastModify(item: LocalT): ZonedDateTime
    protected abstract fun isNewLocalItem(item: LocalT): Boolean

    protected abstract suspend fun insertToRemoteItem(data: LocalT)
    protected abstract suspend fun updateToRemoteItem(data: LocalT)
    protected abstract suspend fun insertToLocalItem(data: Collection<RemoteT>)
    protected abstract suspend fun updateToLocalItem(data: Collection<RemoteT>)

    suspend fun sync() {
        downloadAll()
        uploadAll()
    }

    private suspend fun downloadAll() {
        lastDownloadTime = getSavedLastDownloadTime()

        val startTime = ZonedDateTime.now()

        val remoteData = getRemoteDataUpdatedAfter(
            lastDownloadTime - FRESHNESS_BUFFER_TIME
        )
        Log.d(TAG, "downloadAll: downloaded ${remoteData.size} items from remote")

        val toInsert = mutableListOf<RemoteT>()
        val toUpdate = mutableListOf<RemoteT>()

        for (remoteItem in remoteData) {
            val localItem = getLocalItem(remoteItem)
            if (localItem == null) {
                toInsert.add(remoteItem)
            } else if (getLocalLastModify(localItem) < getRemoteLastModify(remoteItem)) {
                toUpdate.add(remoteItem)
            }
        }

        Log.d(TAG, "downloadAll: insert ${toInsert.size} items, update ${toUpdate.size} items")

        insertToLocalItem(toInsert)
        updateToLocalItem(toUpdate)

        saveLastDownloadTime(startTime)
        lastDownloadTime = startTime
    }

    private suspend fun uploadAll() {
        lastUploadTime = getSavedLastUploadTime()

        val startTime = ZonedDateTime.now()

        val localData = getLocalDataUpdatedAfter(
            lastUploadTime - FRESHNESS_BUFFER_TIME
        ).sortedBy { getLocalLastModify(it) }

        Log.d(TAG, "uploadAll: ${localData.size} items to upload")

        for (localItem in localData) {
            val remoteItem = getRemoteItem(localItem)
            val localLastModify = getLocalLastModify(localItem)

//            Log.d(TAG, "saving $localItem")
//            Log.d(TAG, "remote is $remoteItem")

            if (remoteItem == null || isNewLocalItem(localItem)) {
                try {
                    insertToRemoteItem(localItem)
                } catch (e: HttpException) {
                    if (e.code() < 500) {
                        Log.d(TAG, "failed to insert, delete local $localItem, $e")
                        deleteLocalItem(localItem)
                    } else {
                        throw e
                    }
                }
            } else if (localLastModify > getRemoteLastModify(remoteItem)) {
                try {
                    updateToRemoteItem(localItem)
                } catch (e: HttpException) {
                    if (e.code() < 500) {
                        Log.d(TAG, "failed to update, overwrite local $localItem, $e")
                        updateToLocalItem(listOf(remoteItem))
                    } else {
                        throw e
                    }
                }
            } else if (localLastModify < getRemoteLastModify(remoteItem)) {
                updateToLocalItem(listOf(remoteItem))
            }

            val syncTime = if (localLastModify < startTime) localLastModify else startTime
            lastUploadTime = syncTime
        }
        lastUploadTime = startTime
        saveLastUploadTime(lastUploadTime)
    }
}