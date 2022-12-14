package com.hig.autocrypt.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hig.autocrypt.dto.PublicHealth
import com.hig.autocrypt.dto.Response
import com.hig.autocrypt.model.CoronaCenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor( application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG: String = "로그"
    }

    private val _downloadPercentage = MutableStateFlow<Int>(0)
    val downloadPercentage = _downloadPercentage

    private lateinit var jobOfZeroToEightyAni: Job

    private val coronaCenterRepository: CoronaCenterRepository = CoronaCenterRepository(application)

    fun makePercentageEighty() {
        Log.d(TAG,"MainViewModel - makePercentageEighty() called")
        jobOfZeroToEightyAni = viewModelScope.launch(Dispatchers.IO) {
            // Multiply i with 5%. start percentage is 5%. end percentage is 80%.
            for (i in 1..16) {
                delay(100)
                _downloadPercentage.emit(5 * i)
            }
        }
    }

    private fun makePercentageEightyToHundred() {
        Log.d(TAG,"MainViewModel - makePercentageEightyToHundred() called")
        viewModelScope.launch(Dispatchers.IO) {
            // Multiply i with 5%. start percentage is 85%. end percentage is 100%.
            for (i in 17..20) {
                _downloadPercentage.emit(5 * i)
                delay(75)
            }
        }
    }

    fun refreshCoronaCenterData() {
        Log.d(TAG,"MainViewModel - getCoronaCenter() called")
        viewModelScope.launch(Dispatchers.IO) {
            val requestDeferredList = ArrayList<Deferred<Unit>>()
            val databaseDeferredList = ArrayList<Deferred<Unit>>()

            for (page in 1..10) {
                val def = async {
                    val response = getCoronaCentersFromApi(page = page, 10)

                    // Save to room
                    response.data.forEach { publicHealth ->
                        val insertDeferred = async {
                            insertPublicHealthToRoom(publicHealth)
                        }
                        databaseDeferredList.add(insertDeferred)
                    }
                }

                requestDeferredList.add(def)
            }

            requestDeferredList.awaitAll()
            databaseDeferredList.awaitAll()

            // All database operation is done.
            // Stop animation and play animation for 80 to 100.
            jobOfZeroToEightyAni.cancelAndJoin()
            makePercentageEightyToHundred()
        }
    }

    private suspend fun insertPublicHealthToRoom(publicHealth: PublicHealth) {
        Log.d(TAG,"MainViewModel - insertPublicHealthToRoom() called")
        coronaCenterRepository.insertCoronaCenter(publicHealth)
    }

    private suspend fun getCoronaCentersFromApi(page: Int, perPage: Int = 10): Response {
        Log.d(TAG,"MainViewModel - getCoronaCentersFromApi() called")
        return coronaCenterRepository.getCoronaCenter(page, perPage)
    }
}