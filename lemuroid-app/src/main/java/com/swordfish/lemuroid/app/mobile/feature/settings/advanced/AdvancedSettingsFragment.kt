package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AdvancedSettingsFragment : Fragment() {

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    private val advancedSettingsViewModel: AdvancedSettingsViewModel by viewModels {
        AdvancedSettingsViewModel.Factory(requireContext())
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
                val uiState = advancedSettingsViewModel.uiState.collectAsState()

                AdvancedSettingsScreen(
                    cacheState = uiState.value?.cache,
                    onResetSettings = { handleResetSettings() }
                )
            }
        }
    }

    private fun handleResetSettings() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_settings_warning_message_title)
            .setMessage(R.string.reset_settings_warning_message_description)
            .setPositiveButton(R.string.ok) { _, _ ->
                settingsInteractor.resetAllSettings()
                navigateBackToSettings()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun navigateBackToSettings() {
        findNavController().popBackStack(R.id.navigation_settings, false)
    }

    @dagger.Module
    class Module
}
