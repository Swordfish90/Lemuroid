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
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.app.shared.deeplink.DeepLink
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.HEAD
import retrofit2.http.Url

class ChannelHandler(
    private val appContext: Context,
    private val retrogradeDatabase: RetrogradeDatabase,
    retrofit: Retrofit,
) {
    private val thumbnailsApi = retrofit.create(ThumbnailsApi::class.java)

    private val appName = appContext.getString(R.string.lemuroid_name)

    private val channelsProjection =
        arrayOf(
            TvContractCompat.Channels._ID,
            TvContract.Channels.COLUMN_DISPLAY_NAME,
            TvContractCompat.Channels.COLUMN_BROWSABLE,
        )

    private fun getOrCreateChannelId(): Long? {
        var channelId = findChannel()

        if (channelId != null) {
            return channelId
        }

        val builder =
            Channel.Builder()
                .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(appName)
                .setAppLinkIntentUri(DeepLink.openLeanbackUri(appContext))

        val channelUri =
            appContext.contentResolver.insert(
                TvContractCompat.Channels.CONTENT_URI,
                builder.build().toContentValues(),
            ) ?: return null

        channelId = ContentUris.parseId(channelUri)

        ChannelLogoUtils.storeChannelLogo(
            appContext,
            channelId,
            convertToBitmap(appContext, R.mipmap.lemuroid_tv_channel)!!,
        )

        TvContractCompat.requestChannelBrowsable(appContext, channelId)
        return channelId
    }

    private fun convertToBitmap(
        context: Context,
        resourceId: Int,
    ): Bitmap? {
        val drawable = context.getDrawable(resourceId)
        if (drawable is VectorDrawable) {
            val bitmap: Bitmap =
                Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888,
                )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
            return bitmap
        }
        return BitmapFactory.decodeResource(context.resources, resourceId)
    }

    suspend fun update() {
        val recentGames = retrogradeDatabase.gameDao().asyncSelectFirstRecents(10)

        val channelEntries =
            recentGames.asFlow()
                .map { getChannelEntry(it) }
                .toList()

        val channelId = getOrCreateChannelId() ?: return

        val channel = Channel.Builder()
        channel.setDisplayName(appName)
            .setType(TvContractCompat.Channels.TYPE_PREVIEW)
            .setAppLinkIntentUri(DeepLink.openLeanbackUri(appContext))
            .build()

        appContext.contentResolver.delete(
            TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
            null,
            null,
        )

        appContext.contentResolver.update(
            TvContractCompat.buildChannelUri(channelId),
            channel.build().toContentValues(),
            null,
            null,
        )

        val contentValues =
            channelEntries.map { (game, thumbnail) ->
                getGameProgram(channelId, game, thumbnail).toContentValues()
            }

        if (contentValues.isNotEmpty()) {
            appContext.contentResolver.bulkInsert(
                Uri.parse("content://android.media.tv/preview_program"),
                contentValues.toTypedArray(),
            )
        }
    }

    private suspend fun getChannelEntry(game: Game): ChannelEntry {
        val hasThumbnail =
            game.coverFrontUrl
                ?.let { thumbnailsApi.thumbnailExists(it).isSuccessful }
                ?: false
        return ChannelEntry(game, hasThumbnail)
    }

    @SuppressLint("RestrictedApi")
    private fun getGameProgram(
        channelId: Long,
        game: Game,
        thumbnailExists: Boolean,
    ): PreviewProgram {
        val intent = DeepLink.launchIntentForGame(appContext, game)

        val preview =
            PreviewProgram.Builder()
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
            preview.setPosterArtUri(Uri.parse(CoverUtils.getFallbackRemoteUrl(game)))
        }

        return preview.build()
    }

    private fun findChannel(): Long? {
        val channels =
            appContext.contentResolver.query(
                TvContractCompat.Channels.CONTENT_URI,
                channelsProjection,
                null,
                null,
                null,
            )

        channels?.use {
            if (it.moveToFirst()) {
                do {
                    val channel = Channel.fromCursor(it)
                    if (appName == channel.displayName) {
                        return channel.id
                    }
                } while (it.moveToNext())
            }
        }
        return null
    }

    private data class ChannelEntry(val game: Game, val hasThumbnail: Boolean)

    interface ThumbnailsApi {
        @HEAD
        suspend fun thumbnailExists(
            @Url url: String,
        ): Response<Void>
    }
}
