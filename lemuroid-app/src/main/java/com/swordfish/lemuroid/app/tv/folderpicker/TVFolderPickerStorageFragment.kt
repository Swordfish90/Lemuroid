package com.swordfish.lemuroid.app.tv.folderpicker

import android.os.Bundle
import android.os.Environment
import androidx.annotation.NonNull
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.swordfish.lemuroid.R
import java.io.File

class TVFolderPickerStorageFragment : GuidedStepSupportFragment() {
    @NonNull
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = resources.getString(R.string.tv_folder_storage_title)
        val description = resources.getString(R.string.tv_folder_picker_title)
        return GuidanceStylist.Guidance(title, "", description, null)
    }

    override fun onCreateButtonActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateButtonActions(actions, savedInstanceState)
        addAction(
            actions,
            ACTION_CANCEL,
            resources.getString(R.string.tv_folder_picker_action_cancel),
            "",
        )
    }

    override fun onCreateActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateActions(actions, savedInstanceState)
        val storageRoots =
            runCatching { retrieveStorageRoots() }.getOrNull()
                ?: listOf(Environment.getExternalStorageDirectory())

        storageRoots
            .forEachIndexed { index, file ->
                val storageName =
                    if (index == 0) {
                        resources.getString(R.string.tv_folder_storage_primary)
                    } else {
                        resources.getString(R.string.tv_folder_storage_secondary, index.toString())
                    }
                addAction(actions, ACTION_NAVIGATE, storageName, file.absolutePath)
            }
    }

    private fun retrieveStorageRoots(): List<File> {
        return requireContext().getExternalFilesDirs(null)
            .filterNotNull()
            .map { it.absolutePath }
            .map { File(it.substring(0, it.indexOf("/Android/data/"))) }
            .filter { it.exists() }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_CANCEL -> activity?.finish()
            else -> (activity as TVFolderPickerActivity).navigateTo(action.description.toString())
        }
    }

    private fun addAction(
        actions: MutableList<GuidedAction>,
        id: Long,
        title: String,
        desc: String,
    ) {
        actions.add(
            GuidedAction.Builder(activity)
                .id(id)
                .title(title)
                .description(desc)
                .build(),
        )
    }

    companion object {
        private const val ACTION_CANCEL = 1L
        private const val ACTION_NAVIGATE = 2L
    }
}
