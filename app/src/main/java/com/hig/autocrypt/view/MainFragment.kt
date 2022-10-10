package com.hig.autocrypt.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hig.autocrypt.MainFragmentUILoadingState
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMainBinding
import com.hig.autocrypt.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        private const val TAG: String = "로그"
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG,"MainFragment - onCreateView() called")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG,"MainFragment - onViewCreated() called")
        super.onViewCreated(view, savedInstanceState)

        setObserver()
        startDownload()
        startInitAnimation()
    }

    private fun setObserver() {
        Log.d(TAG,"MainFragment - setObserver() called")
        lifecycleScope.launchWhenStarted {
            viewModel.downloadPercentage.collectLatest { uiState ->
                when (uiState) {
                    is MainFragmentUILoadingState.Loading -> {
                        binding.lottieMainPercentageAnimation.frame = 3 * uiState.percentage
                    }
                    is MainFragmentUILoadingState.Success -> {
                        delay(100)
                        navigateToMapFragment()
                    }
                    is MainFragmentUILoadingState.Error -> {
                        Log.w(TAG, "setObserver: ${uiState.message}")
                        Toast.makeText(requireContext(), "Unknown Error is occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun startDownload() {
        Log.d(TAG,"MainFragment - startDownload() called")
        when (val uiState = viewModel.downloadPercentage.value) {
            is MainFragmentUILoadingState.Loading -> {
                // 다운로드 퍼센티지가 0이면(즉, 다운로드가 진행 중이 아니면) 다운로드를 진행하고
                // 다운로드 퍼센티지가 0이 아니면(즉, 이전 다운로드가 진행 중이면) 다운로드를 진행하지 않는다.
                Log.d(TAG,"MainFragment - startDownload percentage : ${uiState.percentage}() called")
                if (uiState.percentage != 0) {
                    return
                }
                viewModel.refreshCoronaCenterData()
            }
            is MainFragmentUILoadingState.Success -> {
                navigateToMapFragment()
            }
            is MainFragmentUILoadingState.Error -> {
                Log.w(TAG, "startDownload: ${uiState.message}")
                Toast.makeText(requireContext(), "Unknown Error is occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startInitAnimation() {
        Log.d(TAG,"MainFragment - startInitAnimation() called")
        when (val uiState = viewModel.downloadPercentage.value) {
            is MainFragmentUILoadingState.Loading -> {
                if (uiState.percentage != 0) {
                    return
                }
                viewModel.makePercentageEighty()
            }
            is MainFragmentUILoadingState.Success -> {

            }
            is MainFragmentUILoadingState.Error -> {
                Log.w(TAG, "startInitAnimation: ${uiState.message}")
                Toast.makeText(requireContext(), "Unknown Error is occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMapFragment() {
        Log.d(TAG,"MainFragment - navigateToMapFragment() called")
        findNavController().navigate(R.id.action_mainFragment_to_mapFragment)
    }
}