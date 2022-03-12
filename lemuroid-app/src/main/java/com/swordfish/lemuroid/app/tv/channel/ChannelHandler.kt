package com.swordfish.lemuroid.app.tv.channel

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.media.tv.TvContract
import android.net.Uri
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.deeplink.DeepLink
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.HEAD
import retrofit2.http.Url

class ChannelHandler(
    private val appContext: Context,
    private val retrogradeDatabase: RetrogradeDatabase,
    retrofit: Retrofit
) {
    private val thumbnailsApi = retrofit.create(ThumbnailsApi::class.java)

    private val appName = appContext.getString(R.string.lemuroid_name)

    private fun getOrCreateChannelId(): Long? {
        var channelId = findChannel()

        if (channelId != null)
            return channelId

        val builder = Channel.Builder()
            .setType(TvContractCompat.Channels.TYPE_PREVIEW)
            .setDisplayName(appName)
            .setAppLinkIntentUri(DeepLink.openLeanbackUri(appContext))

        val channelUri = appContext.contentResolver.insert(
            TvContractCompat.Channels.CONTENT_URI,
            builder.build().toContentValues()
        ) ?: return null

        channelId = ContentUris.parseId(channelUri)

        ChannelLogoUtils.storeChannelLogo(
            appContext,
            channelId,
            convertToBitmap(appContext, R.mipmap.lemuroid_tv_channel)!!
        )

        TvContractCompat.requestChannelBrowsable(appContext, channelId)
        return channelId
    }

    private fun convertToBitmap(context: Context, resourceId: Int): Bitmap? {
        val drawable = context.getDrawable(resourceId)
        if (drawable is VectorDrawable) {
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
            return bitmap
        }
        return BitmapFactory.decodeResource(context.resources, resourceId)
    }

    fun update(): Completable {
        return retrogradeDatabase.gameDao()
            .rxSelectFirstRecents(10)
            .firstElement()
            .filter { it.isNotEmpty() }
            .flatMapObservable { it.toObservable() }
            .flatMapSingle { game ->
                if (game.coverFrontUrl != null) {
                    thumbnailsApi.thumbnailExists(game.coverFrontUrl!!).map { game to it.isSuccessful }
                } else {
                    Single.just(game to false)
                }
            }
            .toList()
            .doOnSuccess {
                val channelId = getOrCreateChannelId() ?: return@doOnSuccess

                val channel = Channel.Builder()
                channel.setDisplayName(appName)
                    .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                    .setAppLinkIntentUri(DeepLink.openLeanbackUri(appContext))
                    .build()

                appContext.contentResolver.delete(
                    TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
                    null,
                    null
                )

                appContext.contentResolver.update(
                    TvContractCompat.buildChannelUri(channelId),
                    channel.build().toContentValues(),
                    null,
                    null
                )

                val contentValues = it.map { (game, thumbnail) ->
                    getGameProgram(channelId, game, thumbnail).toContentValues()
                }

                if (contentValues.isNotEmpty()) {
                    appContext.contentResolver.bulkInsert(
                        Uri.parse("content://android.media.tv/preview_program"),
                        contentValues.toTypedArray()
                    )
                }
            }
            .ignoreElement()
    }

    @SuppressLint("RestrictedApi")
    private fun getGameProgram(
        channelId: Long,
        game: Game,
        thumbnailExists: Boolean
    ): PreviewProgram {
        val intent = DeepLink.launchIntentForGame(appContext, game)

        val preview = PreviewProgram.Builder()
            .setChannelId(channelId)
            .setTitle(game.title)
            .setDescription(game.developer)
            .setIntent(intent)
            .setStartTimeUtcMillis(game.lastPlayedAt ?: 0)
            .setType(TvContractCompat.PreviewPrograms.TYPE_GAME)
            .setPosterArtAspectRatio(TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_1_1)

        if (game.coverFrontUrl != null && thumbnailExists) {
            preview.setPosterArtUri(Uri.parse(game.coverFrontUrl))
        } else {
            preview.setPosterArtUri(Uri.parse(CoverLoader.getFallbackRemoteUrl(game)))
        }

        return preview.build()
    }

    private val CHANNELS_PROJECTION = arrayOf(
        TvContractCompat.Channels._ID,
        TvContract.Channels.COLUMN_DISPLAY_NAME,
        TvContractCompat.Channels.COLUMN_BROWSABLE
    )

    private fun findChannel(): Long? {
        val channels = appContext.contentResolver.query(
            TvContractCompat.Channels.CONTENT_URI,
            CHANNELS_PROJECTION,
            null,
            null,
            null
        )

        appContext.getString(R.string.app_name)

        channels?.use {
            if (it.moveToFirst())
                do {
                    val channel = Channel.fromCursor(it)
                    if (appName == channel.displayName) {
                        return channel.id
                    }
                } while (it.moveToNext())
        }
        return null
    }

    interface ThumbnailsApi {
        @HEAD
        fun thumbnailExists(@Url url: String): Single<Response<Void>>
    }
}
