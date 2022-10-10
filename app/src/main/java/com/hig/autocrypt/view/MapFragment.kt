package com.hig.autocrypt.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMapBinding
import com.hig.autocrypt.viewmodel.MapViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MapFragment : Fragment() {
    companion object {
        private const val TAG = "로그"
        private const val LOCATION_REQUEST_MIN_INTERVAL = 10 * 1000L
        private const val LOCATION_REQUEST_MAX_INTERVAL = 15 * 1000L
        private var timeOfBackClicked = 0L
    }

    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationCallback: LocationCallback

    override fun onAttach(context: Context) {
        Log.d(TAG,"MapFragment - onAttach() called")
        super.onAttach(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MapFragment - onCreate()")
        super.onCreate(savedInstanceState)
        addBackPressFinishFunction()
        initFusedLocationClient()
        initLocationManager()
        setLocationCallback()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "MapFragment - onCreateView()")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        mapView = binding.mapViewMapNaverMap

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "MapFragment - onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        binding.viewmodel = mapViewModel
        
        // 맵이 준비가 되면 불리는 콜백
        mapView.getMapAsync {
            naverMap = it
            setEvent()
            setObserver()
            refreshCentersFlow()
            startLocationRequestLoop()
        }
    }

    override fun onStart() {
        Log.d(TAG,"MapFragment - onStart() called")
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        Log.d(TAG,"MapFragment - onResume() called")
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        Log.d(TAG,"MapFragment - onPause() called")
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG,"MapFragment - onSaveInstanceState() called")
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG,"MapFragment - onStop() called")
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG,"MapFragment - onDestroyView() called")
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onDestroy() {
        Log.d(TAG,"MapFragment - onDestroy() called")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG,"MapFragment - onDetach() called")
        super.onDetach()
    }

    override fun onLowMemory() {
        Log.d(TAG,"MapFragment - onLowMemory() called")
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun addBackPressFinishFunction() {
        Log.d(TAG,"MapFragment - addBackPressFinishFunction() called")
        requireActivity().onBackPressedDispatcher.addCallback {
            val currentTime = System.currentTimeMillis()
            if (currentTime - timeOfBackClicked <= 1500) {
                requireActivity().finishAndRemoveTask()
                return@addCallback
            }

            Toast.makeText(requireContext(), "If you want to close app. Tap one more time.", Toast.LENGTH_SHORT).show()
            timeOfBackClicked = currentTime
        }
    }

    private fun setEvent() {
        Log.d(TAG,"MapFragment - setEvent() called")
        binding.buttonMapForMyLocation.setOnClickListener {
            val latLng = mapViewModel.latLng.value
            val cameraUpdate = getCameraUpdate(latLng.latitude, latLng.longitude)
            moveCamera(cameraUpdate)
        }
    }

    private fun setObserver() {
        Log.d(TAG, "MapFragment - setObserver()")
        lifecycleScope.launchWhenStarted {
            mapViewModel.centers.collectLatest { listOfPublicHealth ->
                Log.d(TAG, "MapFragment - collectLatest()")
                if (listOfPublicHealth == null) {
                    return@collectLatest
                }

                // 마크 추가하기
                listOfPublicHealth.forEach { publicHealth ->
                    Log.d(TAG, "MapFragment - publicHealth : ${publicHealth}()")
                    Log.d(TAG, "MapFragment - 마크 추가()")
                    val marker = Marker()
                    marker.position = LatLng(publicHealth.lat, publicHealth.lng)
                    marker.map = naverMap
                    marker.captionText = publicHealth.facilityName
                    marker.width = Marker.SIZE_AUTO
                    marker.height = Marker.SIZE_AUTO

                    when (publicHealth.centerType) {
                        "중앙/권역" -> {
                            marker.icon = MarkerIcons.BLACK
                            marker.iconTintColor = Color.BLUE
                        }
                        "지역" -> {
                            marker.icon = MarkerIcons.BLACK
                            marker.iconTintColor = Color.GREEN
                        }
                        else -> {
                            marker.icon = MarkerIcons.LIGHTBLUE
                            marker.iconTintColor = Color.YELLOW
                        }
                    }

                    marker.setOnClickListener {
                        mapViewModel.updateCenter(publicHealth)
                        val cameraUpdate = getCameraUpdate(publicHealth.lat, publicHealth.lng)
                        moveCamera(cameraUpdate)
                        true
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mapViewModel.isStatusVisible.collectLatest {
                if (it) {
                    binding.constraintLayoutMapForMarkerStatusContainer.visibility = View.VISIBLE
                } else {
                    binding.constraintLayoutMapForMarkerStatusContainer.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mapViewModel.latLng.collectLatest {
                naverMap.locationOverlay.isVisible = true
                naverMap.locationOverlay.position = it
            }
        }
    }

    private fun initFusedLocationClient() {
        Log.d(TAG,"MapFragment - initFusedLocationClient() called")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
    }

    private fun initLocationManager() {
        Log.d(TAG,"MapFragment - initLocationManager() called")
        locationManager = ContextCompat.getSystemService(requireContext(), LocationManager::class.java) as LocationManager
    }

    private fun setLocationCallback() {
        Log.d(TAG,"MapFragment - setLocationCallback() called")
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { lastLocation ->
                    Log.d(TAG,"MapFragment - onLocationResult() latitude : ${lastLocation.latitude} longtitude : ${lastLocation.longitude}")
                    mapViewModel.setLatLng(latLng = LatLng(lastLocation.latitude, lastLocation.longitude))
                }
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        Log.d(TAG,"MapFragment - isLocationPermissionGranted() called")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {
            requireActivity().requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            return false
        }

        return true
    }

    private fun isGpsEnabled(): Boolean {
        Log.d(TAG,"MapFragment - isGpsEnabled() called")
        return if (Build.VERSION.SDK_INT >= 28) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun requestLocation() {
        Log.d(TAG,"MapFragment - requestLocation() called")
        fusedLocationClient.requestLocationUpdates(
            LocationRequest
                .create()
                .setInterval(LOCATION_REQUEST_MIN_INTERVAL)
                .setMaxWaitTime(LOCATION_REQUEST_MAX_INTERVAL),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startLocationRequestLoop() {
        lifecycleScope.launchWhenStarted {
            if (!isLocationPermissionGranted()) {
                return@launchWhenStarted
            }

            if (!isGpsEnabled()) {
                Toast.makeText(requireContext(), "To See the current location. You need to Turn on Gps", Toast.LENGTH_SHORT).show()
                return@launchWhenStarted
            }

            requestLocation()
        }
    }

    private fun refreshCentersFlow() {
        Log.d(TAG, "MapFragment - refreshCentersFlow()")
        if (mapViewModel.centers.value != null) {
            Log.d(TAG,"MapFragment - data already saved.() called")
            return
        }

        lifecycleScope.launchWhenStarted {
            mapViewModel.initCenters()
        }
    }

    // Need to move camera.
    private fun getCameraUpdate(lat: Double, lng: Double): CameraUpdate {
        Log.d(TAG,"MapFragment - getCameraUpdate() called")
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
        cameraUpdate.animate(CameraAnimation.Fly)
        return cameraUpdate
    }

    private fun moveCamera(cameraUpdate: CameraUpdate) {
        Log.d(TAG,"MapFragment - moveCamera() called")
        naverMap.moveCamera(cameraUpdate)
    }
}