package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swordfish.lemuroid.lib.bios.BiosManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class BiosSettingsFragment : Fragment() {

    @Inject
    lateinit var biosManager: BiosManager

    private val biosSettingsViewModel: BiosSettingsViewModel by viewModels {
        BiosSettingsViewModel.Factory(biosManager)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState = biosSettingsViewModel.uiState.collectAsState()
                BiosScreen(uiState.value)
            }
        }
    }

    @dagger.Module
    class Module
}
