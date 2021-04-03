package com.swordfish.lemuroid.ext.feature.donate

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.android.billingclient.api.SkuDetails
import com.swordfish.lemuroid.common.animationDuration
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class TVDonateActivity : FragmentActivity() {

    private lateinit var iapHandler: IAPHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iapHandler = IAPHandler(applicationContext)
        iapHandler.attach(this)

        iapHandler.acknowledgePurchases()
            .autoDispose(scope())
            .subscribe()

        iapHandler.isPremium()
            .flatMapSingle { tier -> iapHandler.getDetails().map { tier to it } }
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(animationDuration().toLong(), TimeUnit.MILLISECONDS)
            .delay(animationDuration().toLong(), TimeUnit.MILLISECONDS)
            .autoDispose(scope())
            .subscribe { (tier, details) -> refresh(tier, details) }

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, TVLoadingFragment())
            .commit()
    }

    private fun refresh(currentTier: IAPHandler.Tier, details: List<SkuDetails>) {
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, buildDonationFragment(currentTier, details))
            .commit()
    }

    fun onTierSelected(tierOrdinal: Int) {
        val chosenTier = IAPHandler.Tier.values()[tierOrdinal]
        iapHandler.launchPurchaseFlow(this, chosenTier.skuId!!)
            .autoDispose(scope())
            .subscribe()
    }

    private fun buildDonationFragment(
        tier: IAPHandler.Tier,
        details: List<SkuDetails>
    ): TVDonateFragment {
        val folderFragment = TVDonateFragment()
        folderFragment.arguments = Bundle().apply {
            putSerializable(TVDonateFragment.EXTRA_TIER, tier)
            putSerializable(TVDonateFragment.EXTRA_TIER_NAMES, buildTierNamesMap(details))
        }
        return folderFragment
    }

    private fun buildTierNamesMap(details: List<SkuDetails>): HashMap<IAPHandler.Tier, String> {
        val pairs = IAPHandler.Tier.values()
            .map { it to iapHandler.getDisplayName(it, details) }
            .toTypedArray()

        return hashMapOf(*pairs)
    }
}
