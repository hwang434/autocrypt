package com.hig.autocrypt.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hig.autocrypt.dto.Response
import com.hig.autocrypt.model.CoronaCenterRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    companion object {
        private const val TAG: String = "로그"
    }

    private val _downloadPercentage = MutableStateFlow<Int>(0)
    val downloadPercentage = _downloadPercentage

    private val _isResponseEnd = MutableStateFlow<String>("hi")
    val isResponseEnd = _isResponseEnd

    private val _isInsertedToDatabase = MutableStateFlow<Boolean>(false)
    val isInsertedToDatabase = _isInsertedToDatabase

    private val coronaCenterRepository: CoronaCenterRepository = CoronaCenterRepository()

    fun makeInsertedToDatabaseEnd() {
        Log.d(TAG,"MainViewModel - makeInsertedToDatabaseEnd() called")
        viewModelScope.launch(Dispatchers.IO) {
            delay(3000)
            _isInsertedToDatabase.emit(true)
        }
    }

    fun makePercentageEighty() {
        Log.d(TAG,"MainViewModel - makePercentageEighty() called")
        viewModelScope.launch(Dispatchers.IO) {
            // Multiply i with 5%. start percentage is 5%. end percentage is 80%.
            for (i in 1..16) {
                delay(100)
                _downloadPercentage.emit(5 * i)
            }
        }
    }

    fun makePercentageEightyToHundred() {
        Log.d(TAG,"MainViewModel - makePercentageEightyToHundred() called")
        viewModelScope.launch(Dispatchers.IO) {
            // Multiply i with 5%. start percentage is 85%. end percentage is 100%.
            for (i in 17..20) {
                delay(100)
                _downloadPercentage.emit(5 * i)
            }
        }
    }

    fun saveCoronaCenterData() {
        Log.d(TAG,"MainViewModel - getCoronaCenter() called")
        viewModelScope.launch(Dispatchers.IO) {
            val deferredList = Array<Deferred<Response>?>(10) { null }
            for (page in 1..10) {
                val def = async {
                    // get center info
                    val result = getCoronaData(page = page, 10)
                    // Save to room
                    // todo() save data to room
                    result
                }

                deferredList[page - 1] = def
            }

            // Wait until all operation is done.
            deferredList.forEach {
                Log.d(TAG,"MainViewModel - ${it?.await()?.data} called")
            }
        }
    }

    private suspend fun getCoronaData(page: Int, perPage: Int = 10): Response {
        return coronaCenterRepository.getCoronaCenter(page, perPage)
    }
}