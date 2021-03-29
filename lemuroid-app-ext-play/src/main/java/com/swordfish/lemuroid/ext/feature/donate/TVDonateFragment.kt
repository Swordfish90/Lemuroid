package com.swordfish.lemuroid.ext.feature.donate

import android.os.Bundle
import androidx.annotation.NonNull
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.swordfish.lemuroid.ext.R

class TVDonateFragment : GuidedStepSupportFragment() {

    private val tier by lazy {
        arguments?.getSerializable(EXTRA_TIER) as IAPHandler.Tier
    }

    private val tierNames by lazy {
        arguments?.getSerializable(EXTRA_TIER_NAMES) as HashMap<IAPHandler.Tier, String>
    }

    @NonNull
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val descriptionResource = if (tier == IAPHandler.Tier.NONE) {
            R.string.support_description_short
        } else {
            R.string.support_thankyou_short
        }

        val title = resources.getString(R.string.support_badge)
        val description = resources.getString(descriptionResource)
        return GuidanceStylist.Guidance(title, description, "", resources.getDrawable(tier.imageId))
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)

        IAPHandler.Tier.values()
            .filter { it.ordinal > tier.ordinal }
            .forEach {
                actions.add(
                    GuidedAction.Builder(activity)
                        .id(it.ordinal.toLong())
                        .title(tierNames[it])
                        .icon(it.imageId)
                        .build()
                )
            }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        (activity as TVDonateActivity).onTierSelected(action.id.toInt())
    }

    companion object {
        const val EXTRA_TIER = "EXTRA_TIER"
        const val EXTRA_TIER_NAMES = "EXTRA_TIER_NAMES"
    }
}
