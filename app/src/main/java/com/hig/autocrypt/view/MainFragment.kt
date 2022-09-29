package com.hig.autocrypt.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMainBinding
import com.hig.autocrypt.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        private val TAG: String = "로그"
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
        startInitAnimation()
        viewModel.refreshCoronaCenterData()
    }

    private fun setObserver() {
        Log.d(TAG, "setObserver: ")
        lifecycleScope.launchWhenStarted {
            viewModel.downloadPercentage.collectLatest { percentage ->
                if (Build.VERSION.SDK_INT >= 24) {
                    binding.progressMainLoadingApi.setProgress(percentage, true)
                } else {
                    binding.progressMainLoadingApi.progress = percentage
                }

                if (percentage == 100) {
                    delay(100)
                    findNavController().navigate(R.id.action_mainFragment_to_mapFragment)
                }
            }
        }
    }

    private fun startInitAnimation() {
        Log.d(TAG,"MainFragment - startInitAnimation() viewmodel.downloadPercentage.value : ${viewModel.downloadPercentage.value} called")

        if (viewModel.downloadPercentage.value == 0) {
            viewModel.makePercentageEighty()
        }
    }
}