package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(requireContext(), retrogradeDb)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        handleNotificationPermissionResponse(it)
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
                val state = homeViewModel.getViewStates().collectAsState(HomeViewModel.UIState())
                HomeScreen(
                    state = state.value,
                    onGameClicked = { gameInteractor.onGamePlay(it) },
                    onEnableNotificationsClicked = { handleNotificationPermissionRequest() },
                    onSetDirectoryClicked = { handleChangeStorageFolder() }
                )
            }
        }
    }

    private fun handleChangeStorageFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }

    private fun handleNotificationPermissionRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun handleNotificationPermissionResponse(isGranted: Boolean) {
        if (!isGranted) {
            requireContext().displayDetailsSettingsScreen()
        }
    }

    private fun isNotificationsPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        val permissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        )

        return permissionResult == PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.updateNotificationPermission(isNotificationsPermissionGranted())
    }

    @dagger.Module
    class Module
}
