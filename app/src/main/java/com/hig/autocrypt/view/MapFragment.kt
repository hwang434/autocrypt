package com.hig.autocrypt.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMapBinding
import com.hig.autocrypt.model.MapViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MapFragment : Fragment() {
    companion object {
        private const val TAG = "로그"
        private const val LOCATION_REQUEST_INTERVAL = 60 * 1000L
    }

    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MapFragment - onCreate()")
        super.onCreate(savedInstanceState)
        initFusedLocationClient()
        initLocationManager()
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
            lifecycleScope.launchWhenStarted {
                if (isLocationPermissionGranted()) {
                    if (!isGpsEnabled()) {
                        Toast.makeText(requireContext(), "To See the current location. You need to Turn on Gps", Toast.LENGTH_SHORT).show()
                        return@launchWhenStarted
                    }

                    while (true) {
                        requestLocation()
                        delay(LOCATION_REQUEST_INTERVAL)
                    }
                }
            }
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

    override fun onLowMemory() {
        Log.d(TAG,"MapFragment - onLowMemory() called")
        super.onLowMemory()
        mapView.onLowMemory()
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
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        return locationManager.isLocationEnabled
    }

    private fun requestLocation() {
        Log.d(TAG,"MapFragment - requestLocation() called")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnCompleteListener { locationTask ->
            if (!locationTask.isSuccessful) {
                Toast.makeText(requireContext(), "request location fail.", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }

            val locationResult = locationTask.result
            Log.d(TAG,"MapFragment - locationResult : ${locationResult}() called")
            when (locationResult) {
                null -> {
                    Toast.makeText(requireContext(), "We can not know your location.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    mapViewModel.setLatLng(latLng = LatLng(locationResult.latitude, locationResult.longitude))
                }
            }
        }
    }

    private fun refreshCentersFlow() {
        Log.d(TAG, "MapFragment - refreshCentersFlow()")
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