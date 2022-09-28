package com.hig.autocrypt.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hig.autocrypt.dto.PublicHealth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application): AndroidViewModel(application) {
    
    init {
        Log.d(TAG, "MapViewModel - init()")
    }

    private val _centers: MutableStateFlow<List<PublicHealth>?> = MutableStateFlow(null)
    val centers = _centers.asStateFlow()

    private val _center: MutableStateFlow<PublicHealth?> = MutableStateFlow(null)
    val center = _center.asStateFlow()

    companion object {
        private const val TAG = "로그"
    }

    private val coronaCenterRepository: CoronaCenterRepository = CoronaCenterRepository(application)

    fun initCenters() {
        Log.d(TAG, "MapViewModel - initCenters()")
        viewModelScope.launch(Dispatchers.IO) {
            val result = selectCoronaCenters()
            _centers.emit(result)
        }
    }

    private suspend fun selectCoronaCenters(): List<PublicHealth> {
        Log.d(TAG, "MapViewModel - selectCoronaCenters()")
        return coronaCenterRepository.selectCoronaCenters()
    }

    fun updateCenter(publicHealth: PublicHealth) {
        Log.d(TAG, "MapViewModel - updateCenter($publicHealth)")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "MapViewModel - updateCenter inside coroutine()")
            _center.emit(publicHealth)
        }
    }
}