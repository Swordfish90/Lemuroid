package com.swordfish.lemuroid.app.tv.input

import android.os.Bundle
import androidx.annotation.NonNull
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.swordfish.lemuroid.R

class TVGamePadBindingFragment : GuidedStepSupportFragment() {
    @NonNull
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = requireArguments().getString(EXTRA_TITLE)
        val message = requireArguments().getString(EXTRA_MESSAGE)
        return GuidanceStylist.Guidance(title, message, null, null)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_CANCEL -> requireActivity().finish()
        }
    }

    override fun onCreateActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateButtonActions(actions, savedInstanceState)
        addAction(actions, ACTION_CANCEL, resources.getString(R.string.tv_folder_picker_action_cancel), "")
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
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_MESSAGE = "MESSAGE"
        private const val ACTION_CANCEL = 0L

        fun create(
            title: String,
            message: String,
        ): TVGamePadBindingFragment {
            return TVGamePadBindingFragment().apply {
                arguments =
                    Bundle(2).apply {
                        putString(EXTRA_TITLE, title)
                        putString(EXTRA_MESSAGE, message)
                    }
            }
        }
    }
}
