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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import timber.log.Timber

class HomeFragment : Fragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    private lateinit var homeViewModel: HomeViewModel

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
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = HomeViewModel.Factory(requireContext().applicationContext, retrogradeDb)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Disable snapping in carousel view
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        val pagingController = EpoxyHomeController(gameInteractor, coverLoader)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recyclerview)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = pagingController.adapter

        launchOnState(Lifecycle.State.RESUMED) {
            pagingController.getActions().collect {
                Timber.d("Received home view model action + $it")
                handleEpoxyAction(it)
            }
        }

        launchOnState(Lifecycle.State.RESUMED) {
            homeViewModel.getViewStates().collect {
                pagingController.updateState(it)
            }
        }
    }

    private fun handleEpoxyAction(homeAction: EpoxyHomeController.HomeAction) {
        when (homeAction) {
            EpoxyHomeController.HomeAction.CHANGE_STORAGE_FOLDER -> handleChangeStorageFolder()
            EpoxyHomeController.HomeAction.ENABLE_NOTIFICATION_PERMISSION -> handleNotificationPermissionRequest()
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
