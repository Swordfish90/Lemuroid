package com.swordfish.lemuroid.app.mobile.feature.settings.coreselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swordfish.lemuroid.lib.core.CoresSelection
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CoresSelectionFragment : Fragment() {

    @Inject
    lateinit var coresSelection: CoresSelection

    private val coresSelectionViewModel: CoresSelectionViewModel by viewModels {
        CoresSelectionViewModel.Factory(requireContext().applicationContext, coresSelection)
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
                val applicationContext = LocalContext.current.applicationContext
                val cores = coresSelectionViewModel.getSelectedCores().collectAsState(emptyList())
                val indexingInProgress = coresSelectionViewModel.indexingInProgress.observeAsState(false)
                CoresSelectionScreen(
                    cores = cores.value,
                    indexingInProgress = indexingInProgress.value,
                    onCoreChanged = { system, coreConfig ->
                        coresSelectionViewModel.changeCore(system, coreConfig, applicationContext)
                    }
                )
            }
        }
    }

    @dagger.Module
    class Module
}
