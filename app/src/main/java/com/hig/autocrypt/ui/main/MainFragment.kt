package com.hig.autocrypt.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.hig.autocrypt.R
import com.hig.autocrypt.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.collectLatest

class MainFragment : Fragment() {

    companion object {
        private val TAG: String = "로그"
    }

    private lateinit var viewModel: MainViewModel
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

        setViewModel()
        setObserver()
        viewModel.makePercentageEighty()
    }

    private fun setViewModel() {
        viewModel = MainViewModel()
    }

    private fun setObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.downloadPercentage.collectLatest {
                if (Build.VERSION.SDK_INT >= 24) {
                    binding.progressMainLoadingApi.setProgress(it, true)
                } else {
                    binding.progressMainLoadingApi.progress = it
                }
            }
        }
    }
}