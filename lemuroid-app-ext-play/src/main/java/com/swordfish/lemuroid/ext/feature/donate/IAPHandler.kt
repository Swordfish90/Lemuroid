package com.swordfish.lemuroid.ext.feature.donate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.gen.rxbilling.client.RxBillingImpl
import com.gen.rxbilling.connection.BillingClientFactory
import com.gen.rxbilling.lifecycle.BillingConnectionManager
import com.swordfish.lemuroid.ext.R
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class IAPHandler(private val applicationContext: Context) {

    enum class Tier(val skuId: String?, val colorId: Int, val imageId: Int, val displayNameId: Int?) {
        NONE(
            null,
            R.color.grey,
            R.drawable.ic_no_badge,
            null
        ),
        BRONZE(
            "lemuroid.support.badge.bronze",
            R.color.bronze,
            R.drawable.ic_bronze_badge,
            R.string.support_bronze
        ),
        SILVER(
            "lemuroid.support.badge.silver",
            R.color.silver,
            R.drawable.ic_silver_badge,
            R.string.support_silver
        ),
        GOLD(
            "lemuroid.support.badge.gold",
            R.color.gold,
            R.drawable.ic_gold_badge,
            R.string.support_gold
        )
    }

    private val rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))

    fun attach(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(BillingConnectionManager(rxBilling))
    }

    fun isPremium(): Flowable<Tier> {
        return allPurchasesFlowable()
            .map { purchases ->
                Tier.values().toList().reversed()
                    .firstOrNull { tier ->
                        purchases.any { it.sku == tier.skuId }
                    } ?: Tier.NONE
            }
            .subscribeOn(Schedulers.io())
            .doOnNext { logInfo("isPremium tier changed to: $it") }
            .doOnError { logError("onError in isPremium flowable", it) }
    }

    private fun allPurchasesFlowable(): Flowable<List<Purchase>> {
        return rxBilling
            .getPurchases(BillingClient.SkuType.INAPP)
            .toFlowable()
            .concatWith(rxBilling.observeUpdates().map { it.purchases })
            .subscribeOn(Schedulers.io())
    }

    fun acknowledgePurchases(): Completable {
        return allPurchasesFlowable()
            .concatMap { purchases ->
                Flowable.fromIterable(purchases)
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .filter { !it.isAcknowledged }
            }
            .flatMapCompletable { purchase ->
                rxBilling.acknowledge(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                )
                    .doOnComplete { logInfo("Purchase for ${purchase.sku} was acknowledge") }
                    .doOnError { logError("Purchase was not acknowledge for product ${purchase.sku}", it) }
            }
    }

    fun getDetails(): Single<List<SkuDetails>> {
        return rxBilling.getSkuDetails(
            SkuDetailsParams.newBuilder()
                .setSkusList(Tier.values().toList().mapNotNull { it.skuId })
                .setType(BillingClient.SkuType.INAPP)
                .build()
        )
            .subscribeOn(Schedulers.io())
            .doOnSuccess { logInfo("Details for skus retrieved: $it") }
            .doOnError { logError("Error while retrieving details for skus", it) }
    }

    fun getDisplayName(tier: Tier, details: List<SkuDetails>): String {
        if (tier.displayNameId == null)
            return ""

        val price = details
            .firstOrNull { it.sku == tier.skuId }
            ?.price ?: ""

        return applicationContext.resources.getString(tier.displayNameId, price)
    }

    private fun loadSkuDetails(skuId: String): Maybe<SkuDetails> {
        return rxBilling.getSkuDetails(
            SkuDetailsParams.newBuilder()
                .setSkusList(listOf(skuId))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        )
            .subscribeOn(Schedulers.io())
            .doOnSuccess { logInfo("Details for sku $skuId retrieved: $it") }
            .doOnError { logError("Error while retrieving details for $skuId", it) }
            .flatMapMaybe {
                Maybe.fromCallable { it.firstOrNull() }
            }
    }

    fun launchPurchaseFlow(activity: Activity, skuId: String): Completable {
        return loadSkuDetails(skuId)
            .flatMapCompletable { skuDetails ->
                rxBilling.launchFlow(
                    activity,
                    BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                )
            }
            .doOnSubscribe { logInfo("Purchase flow started for sku: $skuId") }
            .doOnError { logError("Purchase flow failed with error: $it", it) }
            .doAfterTerminate { logInfo("Purchase flow completed for sku: $skuId") }
    }

    private fun logInfo(message: String) {
        if (VERBOSE) {
            Log.i(TAG_LOG, message)
        }
    }

    private fun logError(message: String, throwable: Throwable) {
        if (VERBOSE) {
            Log.e(TAG_LOG, message, throwable)
        }
    }

    companion object {
        val IS_SUPPORTED = true

        fun launchDonateScreen(activity: Activity) {
            activity.startActivity(Intent(activity, DonateActivity::class.java))
        }

        fun launchTVDonateScreen(activity: Activity) {
            activity.startActivity(Intent(activity, TVDonateActivity::class.java))
        }

        // This has to be tested on Google Play release builds, let's keep this for convenience.
        private const val VERBOSE = true
        private const val TAG_LOG = "IAPHandler"
    }
}
