/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import com.swordfish.lemuroid.BuildConfig

fun Context.getPrivateSharedPreferences(): SharedPreferences {
    return this.getSharedPreferences(packageName + "_preference", Context.MODE_PRIVATE)
}

fun SharedPreferences.setProperty(tag: String, value: Int) {
    try {
        val spe = edit()
        spe.putInt(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.setProperty(tag: String, value: Boolean) {
    try {
        val spe = edit()
        spe.putBoolean(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.getProperty(tag: String, default_value: Int): Int {
    var result = default_value
    try {
        result = getInt(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun SharedPreferences.getProperty(tag: String, default_value: Boolean): Boolean {
    var result = default_value
    try {
        result = getBoolean(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun Context.openAppInGooglePlay(appPackageName: String? = null) {
    val packName = appPackageName ?: packageName
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (anfe: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

fun launchApp(activity: Activity, appPackageName: String): Boolean {
    return try {
        val launchIntent = activity
            .packageManager.getLaunchIntentForPackage(appPackageName)
        activity.startActivity(launchIntent)
        true
    } catch (ex: Exception) {
        false
    }
}

fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun PackageManager.isFullRoidProInstalled(): Boolean {
    return isPackageInstalled(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
}

fun Context.isRoomcordInstalled(): Boolean {
    return try {
        packageManager.getApplicationInfo(FulldiveConfigs.ROOMCORD_PACKAGE_NAME, 0).enabled
    } catch (e: Exception) {
        false
    }
}

fun isProVersion(): Boolean = BuildConfig.FLAVOR.contains("pro")

fun fromHtmlToSpanned(html: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html.orEmpty())
    }
}

fun convertSpannedToAnnotatedString(spanned: Spanned): AnnotatedString {
    return buildAnnotatedString {
        val text = spanned.toString()
        append(text)

        val spans = spanned.getSpans<Any>(0, spanned.length)
        spans.forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is android.text.style.StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> {
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        }

                        android.graphics.Typeface.ITALIC -> {
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        }
                    }
                }

                is android.text.style.ForegroundColorSpan -> {
                    addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }

                is android.text.style.URLSpan -> {
                    addStringAnnotation(
                        tag = "URL",
                        annotation = span.url,
                        start = start,
                        end = end
                    )
                    addStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline), start, end)
                }
            }
        }
    }
}

fun Context.getHexColor(id: Int): String {
    return String.format("#%06x", ContextCompat.getColor(this, id).and(0xffffff))
}

inline fun <T> T?.or(block: () -> T) = this ?: block.invoke()
