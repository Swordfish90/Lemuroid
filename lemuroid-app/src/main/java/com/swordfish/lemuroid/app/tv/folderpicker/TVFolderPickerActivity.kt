package com.swordfish.lemuroid.app.tv.folderpicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.annotation.NonNull
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.tv.shared.BaseTVActivity
import java.io.File

class TVFolderPickerActivity : BaseTVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == savedInstanceState) {
            val folderFragment = buildFragment(Environment.getExternalStorageDirectory().absolutePath)
            GuidedStepSupportFragment.addAsRoot(this, folderFragment, android.R.id.content)
        }
    }

    fun navigateTo(folder: String) {
        val folderFragment = buildFragment(folder)
        GuidedStepSupportFragment.add(supportFragmentManager, folderFragment)
    }

    private fun buildFragment(folder: String): TVFolderViewFragment {
        val folderFragment = TVFolderViewFragment()
        folderFragment.arguments = Bundle().apply {
            putString(TVFolderViewFragment.EXTRA_FOLDER, folder)
        }
        return folderFragment
    }

    class TVFolderViewFragment : GuidedStepSupportFragment() {
        private lateinit var directory: File

        override fun onCreate(savedInstanceState: Bundle?) {
            directory = File(arguments?.getString(EXTRA_FOLDER)
                    ?: throw IllegalArgumentException("EXTRA_FOLODER cannot be null"))

            super.onCreate(savedInstanceState)
        }

        @NonNull
        override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
            val title = directory.name
            val description = resources.getString(R.string.tv_folder_picker_title)
            return Guidance(title, "", description, null)
        }

        override fun onCreateButtonActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
            super.onCreateButtonActions(actions, savedInstanceState)
            addAction(actions, ACTION_CHOOSE, resources.getString(R.string.tv_folder_picker_action_choose), "")
            addAction(actions, ACTION_CANCEL, resources.getString(R.string.tv_folder_picker_action_cancel), "")
        }

        override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
            super.onCreateActions(actions, savedInstanceState)

            directory.listFiles()
                ?.filter { it.isDirectory }
                ?.forEach {
                    addAction(actions, ACTION_NAVIGATE, it.name, it.absolutePath)
                }
        }

        override fun onGuidedActionClicked(action: GuidedAction) {
            when (action.id) {
                ACTION_CHOOSE -> {
                    val resultIntent = Intent().apply {
                        putExtra(RESULT_DIRECTORY_PATH, directory.absolutePath)
                    }

                    activity?.setResult(Activity.RESULT_OK, resultIntent)
                    activity?.finish()
                }
                ACTION_CANCEL -> activity?.finish()
                else -> (activity as TVFolderPickerActivity).navigateTo(action.description.toString())
            }
        }

        private fun addAction(actions: MutableList<GuidedAction>, id: Long, title: String, desc: String) {
            actions.add(GuidedAction.Builder(activity)
                .id(id)
                .title(title)
                .description(desc)
                .build())
        }

        companion object {
            private const val ACTION_CHOOSE = 0L
            private const val ACTION_CANCEL = 1L
            private const val ACTION_NAVIGATE = 2L

            const val EXTRA_FOLDER = "EXTRA_FOLDER"
        }
    }

    companion object {
        const val RESULT_DIRECTORY_PATH = "RESULT_DIRECTORY_PATH"
    }
}
