package come.asg.ominoy.app.shared.game

import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readGameplayCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readWebApiCredentials
import come.asg.ominoy.app.utils.android.isMainProcess
import come.asg.ominoy.lib.injection.AndroidWorkerInjection
import come.asg.ominoy.lib.injection.WorkerKey
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Uploads durable offline unlocks independently of the native game session. */
class RetroAchievementsPendingUploadWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val credentials = readGameplayCredentials(applicationContext)
            ?: return@withContext Result.success()
        val queue = RetroAchievementsPendingStore.snapshot(applicationContext)
            .filter { it.state == RetroAchievementsPendingStore.State.PENDING }
            .filter { it.username.equals(credentials.username, ignoreCase = true) }
            .sortedBy { it.unlockTimeMillis }
        if (queue.isEmpty()) return@withContext Result.success()

        var uploaded = 0
        for (entry in queue) {
            val outcome = submit(entry, credentials.username, credentials.token)
            when (outcome.kind) {
                OutcomeKind.SUCCESS -> {
                    RetroAchievementsPendingStore.remove(applicationContext, entry.key)
                    uploaded++
                    reconcile(entry)
                }
                OutcomeKind.AUTH_FAILURE -> {
                    RetroAchievementsPendingStore.update(applicationContext, entry.key) {
                        it.copy(lastError = outcome.error.ifBlank { "Sign in again to sync pending achievements" })
                    }
                    Timber.w("RetroAchievements pending upload paused: authentication failed")
                    return@withContext Result.success()
                }
                OutcomeKind.RETRYABLE -> {
                    val nextAttempts = entry.attemptCount + 1
                    RetroAchievementsPendingStore.update(applicationContext, entry.key) {
                        it.copy(attemptCount = nextAttempts, lastError = outcome.error)
                    }
                    return@withContext Result.retry()
                }
                OutcomeKind.PERMANENT_FAILURE -> {
                    RetroAchievementsPendingStore.update(applicationContext, entry.key) {
                        it.copy(
                            attemptCount = it.attemptCount + 1,
                            lastError = outcome.error,
                            state = RetroAchievementsPendingStore.State.FAILED,
                        )
                    }
                }
            }
        }
        Timber.i("Uploaded $uploaded pending RetroAchievements unlock(s)")
        Result.success()
    }

    private suspend fun reconcile(entry: RetroAchievementsPendingStore.PendingUnlock) {
        val web = readWebApiCredentials(applicationContext) ?: return
        if (entry.raGameId <= 0 || entry.localLibraryGameId <= 0) return
        RetroAchievementsEarnedStore.syncFromServer(
            context = applicationContext,
            libraryGameId = entry.localLibraryGameId,
            retroAchievementsGameId = entry.raGameId,
            username = web.username,
            apiKey = web.apiKey,
            gameTitle = entry.gameTitle,
            mirrorExport = false,
            downloadArtwork = false,
        )
    }

    private fun submit(
        entry: RetroAchievementsPendingStore.PendingUnlock,
        username: String,
        token: String,
    ): Outcome {
        val ageSeconds = ((System.currentTimeMillis() - entry.unlockTimeMillis).coerceAtLeast(0L) / 1000L)
            .coerceAtMost(MAX_OFFSET_SECONDS.toLong()).toInt()
        val hardcore = if (entry.hardcore) 1 else 0
        val validationInput = buildString {
            append(entry.achievementId)
            append(username)
            append(hardcore)
            if (ageSeconds > 0) {
                append(entry.achievementId)
                append(ageSeconds)
            }
        }
        val validation = RetroAchievementsPendingStore.md5(validationInput)
        val form = FormBody.Builder()
            .add("r", "awardachievement")
            .add("u", username)
            .add("t", token)
            .add("a", entry.achievementId.toString())
            .add("h", hardcore.toString())
            .apply { if (entry.rawRomMd5.isNotBlank()) add("m", entry.rawRomMd5) }
            .apply { if (ageSeconds > 0) add("o", ageSeconds.toString()) }
            .add("v", validation)
            .build()
        val request = Request.Builder()
            .url(BASE_URL)
            .header("User-Agent", "OMINOY/1.18.0 (Android; durable-achievement-sync)")
            .post(form)
            .build()
        return runCatching {
            http.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val error = runCatching { JSONObject(body).optString("Error") }.getOrDefault(body.take(200))
                when {
                    response.code == 401 || response.code == 403 || isAuthError(error) -> Outcome(OutcomeKind.AUTH_FAILURE, error)
                    response.code == 429 || response.code in 500..599 -> Outcome(OutcomeKind.RETRYABLE, "HTTP ${response.code}")
                    !response.isSuccessful -> Outcome(OutcomeKind.PERMANENT_FAILURE, "HTTP ${response.code}: $error")
                    isSuccessful(body) || isAlreadyAwarded(error) -> Outcome(OutcomeKind.SUCCESS, error)
                    else -> Outcome(OutcomeKind.PERMANENT_FAILURE, error.ifBlank { "Award request rejected" })
                }
            }
        }.getOrElse { error ->
            Outcome(OutcomeKind.RETRYABLE, error.javaClass.simpleName)
        }
    }

    private fun isSuccessful(body: String): Boolean = runCatching {
        JSONObject(body).optBoolean("Success", false)
    }.getOrDefault(false)

    private fun isAlreadyAwarded(value: String): Boolean = value.contains("User already has", ignoreCase = true)

    private fun isAuthError(value: String): Boolean = value.contains("credentials invalid", ignoreCase = true) ||
        value.contains("invalid credentials", ignoreCase = true) ||
        value.contains("not authorized", ignoreCase = true) ||
        value.contains("unauthorized", ignoreCase = true)

    private enum class OutcomeKind { SUCCESS, AUTH_FAILURE, RETRYABLE, PERMANENT_FAILURE }
    private data class Outcome(val kind: OutcomeKind, val error: String)

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(RetroAchievementsPendingUploadWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<RetroAchievementsPendingUploadWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<RetroAchievementsPendingUploadWork>()
    }

    companion object {
        private const val BASE_URL = "https://retroachievements.org/dorequest.php"
        private const val MAX_OFFSET_SECONDS = 14 * 24 * 60 * 60
        private const val UNIQUE_WORK_ID = "RetroAchievementsPendingUploadWork"
        private const val UNIQUE_PERIODIC_WORK_ID = "RetroAchievementsPendingUploadWorkPeriodic"
        private val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        fun enqueue(context: Context) {
            val appContext = context.applicationContext
            if (!appContext.isMainProcess()) {
                // GameActivity runs in :game. WorkManager belongs to the main process;
                // hand off scheduling instead of initializing it in the game process.
                appContext.sendBroadcast(
                    Intent(appContext, RetroAchievementsPendingUploadReceiver::class.java),
                )
                return
            }
            val request = OneTimeWorkRequestBuilder<RetroAchievementsPendingUploadWork>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }

        fun enqueuePeriodic(context: Context) {
            val appContext = context.applicationContext
            if (!appContext.isMainProcess()) return
            val request = PeriodicWorkRequestBuilder<RetroAchievementsPendingUploadWork>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_ID,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
