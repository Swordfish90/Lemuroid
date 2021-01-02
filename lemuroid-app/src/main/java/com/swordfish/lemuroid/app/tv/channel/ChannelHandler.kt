package com.swordfish.lemuroid.app.tv.channel

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
import com.swordfish.lemuroid.app.shared.deeplink.DeepLink
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable

class ChannelHandler(
    private val appContext: Context,
    private val retrogradeDatabase: RetrogradeDatabase
) {

    private fun getOrCreateChannelId(): Long {
        var channelId = findChannel()

        if (channelId != null)
            return channelId

        val builder = Channel.Builder()
            .setType(TvContractCompat.Channels.TYPE_PREVIEW)
            .setDisplayName(DEFAULT_CHANNEL_DISPLAY_NAME)
            .setAppLinkIntentUri(DeepLink.openLeanbackUri(appContext))

        val channelUri = appContext.contentResolver.insert(
            TvContractCompat.Channels.CONTENT_URI,
            builder.build().toContentValues()
        )
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
            .doOnSuccess {
                val channelId = getOrCreateChannelId()

                val channel = Channel.Builder()
                channel.setDisplayName(DEFAULT_CHANNEL_DISPLAY_NAME)
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

                if (it.isNotEmpty())
                    it.forEach { game ->
                        val prg = getGameProgram(channelId, game)
                        appContext.contentResolver.insert(
                            Uri.parse("content://android.media.tv/preview_program"),
                            prg.toContentValues()
                        )
                    }
            }
            .ignoreElement()
    }

    private fun getGameProgram(channelId: Long, game: Game): PreviewProgram {
        val intent = DeepLink.launchIntentForGame(appContext, game)

        val preview = PreviewProgram.Builder()
            .setChannelId(channelId)
            .setTitle(game.title)
            .setDescription(game.developer)
            .setIntent(intent)
            .setType(TvContractCompat.PreviewPrograms.TYPE_GAME)
            .setPosterArtAspectRatio(TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_1_1)

        if (game.coverFrontUrl != null) {
            preview.setPosterArtUri(Uri.parse(game.coverFrontUrl))
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

        channels?.use {
            if (it.moveToFirst())
                do {
                    val channel = Channel.fromCursor(it)
                    if (DEFAULT_CHANNEL_NAME == channel.displayName) {
                        return channel.id
                    }
                } while (it.moveToNext())
        }
        return null
    }

    companion object {
        private const val DEFAULT_CHANNEL_NAME = "Lemuroid"
        private const val DEFAULT_CHANNEL_DISPLAY_NAME = "Lemuroid"
    }
}
