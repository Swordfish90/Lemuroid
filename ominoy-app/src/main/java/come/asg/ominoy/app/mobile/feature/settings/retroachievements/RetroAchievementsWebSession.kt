package come.asg.ominoy.app.mobile.feature.settings.retroachievements

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView

/**
 * Owns the app-private WebView session used only for RetroAchievements key setup.
 * WebView cookies/storage are scoped to this Android app and are not shared with Chrome.
 * The username marker is not a credential; it prevents a prior app account from inheriting
 * the persisted site session after an account switch.
 */
object RetroAchievementsWebSession {
    private const val PREFS = "retro_achievements_web_session"
    private const val ACCOUNT = "account_username"

    fun prepareForAccount(
        context: Context,
        username: String,
    ) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getString(ACCOUNT, null)
        if (!previous.equals(username, ignoreCase = true)) {
            clearWebData(context)
            prefs.edit().putString(ACCOUNT, username).apply()
        }
    }

    fun clearForLogout(context: Context) {
        clearWebData(context)
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun clearWebData(context: Context) {
        val cookies = CookieManager.getInstance()
        cookies.removeAllCookies(null)
        cookies.flush()
        WebStorage.getInstance().deleteAllData()
        runCatching {
            WebView(context.applicationContext).apply {
                clearCache(true)
                clearHistory()
                destroy()
            }
        }
    }
}
