package com.swordfish.lemuroid.app.tv.folderpicker

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import com.swordfish.lemuroid.app.tv.shared.BaseTVActivity

class TVFolderPickerActivity : BaseTVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == savedInstanceState) {
            val storageFragment = TVFolderPickerStorageFragment()
            GuidedStepSupportFragment.addAsRoot(this, storageFragment, android.R.id.content)
        }
    }

    fun navigateTo(folder: String) {
        val folderFragment = buildFolderFragment(folder)
        GuidedStepSupportFragment.add(supportFragmentManager, folderFragment)
    }

    private fun buildFolderFragment(folder: String): TVFolderPickerFolderFragment {
        val folderFragment = TVFolderPickerFolderFragment()
        folderFragment.arguments =
            Bundle().apply {
                putString(TVFolderPickerFolderFragment.EXTRA_FOLDER, folder)
            }
        return folderFragment
    }

    companion object {
        const val RESULT_DIRECTORY_PATH = "RESULT_DIRECTORY_PATH"
    }
}
