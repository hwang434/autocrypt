package com.hig.autocrypt.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hig.autocrypt.dto.PublicHealth
import com.hig.autocrypt.model.CoronaCenterRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application): AndroidViewModel(application) {
    companion object {
        private const val TAG = "로그"
    }
    
    init {
        Log.d(TAG, "MapViewModel - init()")
    }

    private val _latLng: MutableStateFlow<LatLng> = MutableStateFlow(LatLng(37.0, 127.0))
    val latLng = _latLng.asStateFlow()

    private val _centers: MutableStateFlow<List<PublicHealth>?> = MutableStateFlow(null)
    val centers = _centers.asStateFlow()

    private val _center: MutableStateFlow<PublicHealth?> = MutableStateFlow(null)
    val center = _center.asStateFlow()

    private val _isStatusVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isStatusVisible = _isStatusVisible.asStateFlow()

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
            // if : click same marker. hide the statusbar and return
            // else : show statusBar and emit data.
            if (_center.value == publicHealth) {
                // even if same result. if status bar is gone then make visible statusBar
                if (!_isStatusVisible.value) {
                    _isStatusVisible.emit(true)
                    return@launch
                }
                _isStatusVisible.emit(false)
                return@launch
            }

            _isStatusVisible.emit(true)
            _center.emit(publicHealth)
        }
    }

    fun setLatLng(latLng: LatLng) {
        Log.d(TAG,"MapViewModel - setLocationOverlay() called")
        viewModelScope.launch(Dispatchers.IO) {
            _latLng.emit(latLng)
        }
    }
}