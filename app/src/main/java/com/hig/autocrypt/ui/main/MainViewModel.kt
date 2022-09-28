package com.hig.autocrypt.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
}