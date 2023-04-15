package sg.edu.ntu.scse.labattendancesystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {
    protected fun <T> MutableLiveData<T>.load(
        handleError: (Throwable, String) -> Unit = { err, _ -> throw err },
        request: suspend () -> Flow<T>
    ): MutableLiveData<T> {

        val handleErrorOnMain = CoroutineExceptionHandler { _, error ->
            viewModelScope.launch(Dispatchers.Main) {
                val errMsg = error.localizedMessage ?: "Error occurred. Please try again."
                handleError(error, errMsg)
            }
        }

        viewModelScope.launch(Dispatchers.IO + handleErrorOnMain) {
            request().collect { response ->
                withContext(Dispatchers.Main) {
                    this@load.value = response
                }
            }
        }
        return this
    }

    protected fun <T1, T2, R> combine(
        v1: LiveData<T1>, v2: LiveData<T2>, combineF: (T1?, T2?) -> R?
    ): LiveData<R> = MediatorLiveData<R>().apply {
        addSource(v1) { value = combineF(it, v2.value) }
        addSource(v2) { value = combineF(v1.value, it) }
    }
}
