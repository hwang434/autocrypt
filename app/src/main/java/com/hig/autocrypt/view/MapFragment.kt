package com.hig.autocrypt.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMapBinding
import com.hig.autocrypt.model.MapViewModel
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
    }

    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MapFragment - onCreate()")
        super.onCreate(savedInstanceState)
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

            setObserver()
            refreshCentersFlow()
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
            mapViewModel.center.collectLatest {
                Log.d(TAG, "MapFragment - mapViewModel.center.collectLatest")
                Log.d(TAG, "MapFragment - center : $it()")
                when (it) {
                    null -> {
                        binding.constraintLayoutMapForMarkerStatusContainer.visibility = View.GONE
                    }
                    else -> {
                        binding.constraintLayoutMapForMarkerStatusContainer.visibility = View.VISIBLE
                    }
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
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
        cameraUpdate.animate(CameraAnimation.Fly)
        return cameraUpdate
    }

    private fun moveCamera(cameraUpdate: CameraUpdate) {
        naverMap.moveCamera(cameraUpdate)
    }
}