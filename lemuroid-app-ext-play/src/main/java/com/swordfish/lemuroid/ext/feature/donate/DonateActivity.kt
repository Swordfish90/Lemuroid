package com.swordfish.lemuroid.ext.feature.donate

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.android.billingclient.api.SkuDetails
import com.google.android.material.button.MaterialButton
import com.swordfish.lemuroid.common.animationDuration
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.uber.autodispose.android.lifecycle.autoDispose
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class DonateActivity : FragmentActivity() {

    private lateinit var iapHandler: IAPHandler

    private lateinit var descriptionView: TextView
    private lateinit var badgeView: ImageView
    private lateinit var purchaseOptionsLayout: LinearLayout
    private lateinit var loadingView: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        descriptionView = findViewById(R.id.support_description)
        badgeView = findViewById(R.id.support_image)
        purchaseOptionsLayout = findViewById(R.id.purchase_options)
        loadingView = findViewById(R.id.loading)

        iapHandler = IAPHandler(applicationContext)
        iapHandler.attach(this)

        iapHandler.acknowledgePurchases()
            .autoDispose(scope())
            .subscribe()

        iapHandler.isPremium()
            .flatMapSingle { tier -> iapHandler.getDetails().map { Triple(tier, it, false) } }
            .startWith(Triple(IAPHandler.Tier.NONE, listOf(), true))
            .debounce(animationDuration().toLong(), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { (tier, details, loading) -> refresh(tier, details, loading) }
    }

    private fun createMaterialButton(tier: IAPHandler.Tier, details: List<SkuDetails>): MaterialButton {
        return MaterialButton(this, null).apply {
            setText(iapHandler.getDisplayName(tier, details))
            setTextColor(Color.BLACK)
            setBackgroundColor(resources.getColor(tier.colorId))
            setOnClickListener {
                iapHandler.launchPurchaseFlow(this@DonateActivity, tier.skuId!!)
                    .autoDispose(this@DonateActivity, Lifecycle.Event.ON_DESTROY)
                    .subscribe()
            }
        }
    }

    private fun refresh(currentTier: IAPHandler.Tier, details: List<SkuDetails>, loading: Boolean) {
        purchaseOptionsLayout.removeAllViews()
        IAPHandler.Tier.values()
            .filter { it.ordinal > currentTier.ordinal }
            .map { createMaterialButton(it, details) }
            .forEach { purchaseOptionsLayout.addView(it) }

        val description = if (currentTier == IAPHandler.Tier.NONE) {
            R.string.support_description
        } else {
            R.string.support_thankyou
        }

        descriptionView.text = Html.fromHtml(resources.getString(description))
        badgeView.setImageResource(currentTier.imageId)

        purchaseOptionsLayout.setVisibleOrGone(!loading)
        descriptionView.setVisibleOrGone(!loading)
        badgeView.setVisibleOrGone(!loading)
        loadingView.setVisibleOrGone(loading)
    }
}
