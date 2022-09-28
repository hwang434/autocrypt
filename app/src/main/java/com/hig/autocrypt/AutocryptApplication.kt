package com.hig.autocrypt

import android.app.Application
import android.util.Log
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AutocryptApplication: Application() {
    companion object {
        private const val TAG = "로그"
    }
    override fun onCreate() {
        Log.d(TAG, "AutocryptApplication - onCreate()")
        super.onCreate()
        NaverMapSdk.getInstance(this).setClient(
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_MAP_CLIENT_ID)
        )
    }
}