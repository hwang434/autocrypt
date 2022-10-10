package com.hig.autocrypt

sealed class MainFragmentUILoadingState {
    data class Loading(val percentage: Int): MainFragmentUILoadingState()
    data class Success(val isSuccess: Boolean): MainFragmentUILoadingState()
    data class Error(val message: String): MainFragmentUILoadingState()
}
