package sg.edu.ntu.scse.labattendancesystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class AssignableLiveData<T>(initLiveData: LiveData<T> = MutableLiveData()) {
    private val _liveData = MediatorLiveData<T>()
    private var lastLiveData: LiveData<T>

    val liveData: LiveData<T>
        get() = _liveData

    init {
        addSource(initLiveData)
        lastLiveData = liveData
    }

    @Synchronized
    fun setDataSource(newData: LiveData<T>) {
        _liveData.removeSource(lastLiveData)
        addSource(newData)
    }

    fun setConstSource(value: T) {
        setDataSource(MutableLiveData(value))
    }

    private fun addSource(liveData: LiveData<T>) {
        _liveData.addSource(liveData) { _liveData.value = liveData.value }
        _liveData.value = liveData.value
        lastLiveData = liveData
    }

}