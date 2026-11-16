package come.asg.ominoy.app.shared.diagnostics

import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/** Persists a small, private set of sanitized app crash reports for user-assisted diagnosis. */
object CrashLogStore {
    private const val DIRECTORY_NAME = "crash-logs"
    private const val MAX_REPORTS = 10
    private const val MAX_REPORT_CHARS = 120_000
    private val installLock = Any()
    private val writeLock = Any()
    private val installed = AtomicBoolean(false)

    /** Installs an app-process handler while preserving Android's previous crash handler. */
    fun install(context: Context) {
        synchronized(installLock) {
            if (!installed.compareAndSet(false, true)) return
            val appContext = context.applicationContext
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, error ->
                recordException(appContext, thread, error, "uncaught")
                if (previous != null) {
                    previous.uncaughtException(thread, error)
                } else {
                    Process.killProcess(Process.myPid())
                    exitProcess(10)
                }
            }
        }
    }

    /** Records errors intercepted by OMINOY's game-process handler as well as uncaught crashes. */
    fun recordException(
        context: Context,
        thread: Thread,
        error: Throwable,
        source: String,
    ): File? =
        runCatching {
            synchronized(writeLock) {
                val directory = File(context.applicationContext.filesDir, DIRECTORY_NAME)
                check(directory.isDirectory || directory.mkdirs()) { "Could not create crash-log folder" }
                val timestamp = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).format(Date(timestamp))
                val stackTrace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
                val report =
                    buildString {
                        appendLine("OMINOY crash report")
                        appendLine("Timestamp: $date")
                        appendLine("Source: $source")
                        appendLine("App: ${context.packageName} ${appVersion(context)}")
                        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                        appendLine("Process ID: ${Process.myPid()}")
                        appendLine("Thread: ${thread.name} (id ${thread.id})")
                        appendLine()
                        appendLine("Exception stack trace:")
                        append(stackTrace)
                    }.redactCredentials().take(MAX_REPORT_CHARS)
                val reportFile = File(directory, "crash-$timestamp-${thread.id}.txt")
                reportFile.writeText(report, Charsets.UTF_8)
                pruneOldReports(directory)
                reportFile
            }
        }.getOrNull()

    /** Returns the most recent private report, if one was captured. */
    fun latestReport(context: Context): File? =
        File(context.applicationContext.filesDir, DIRECTORY_NAME)
            .listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.extension == "txt" }
            ?.maxByOrNull(File::lastModified)

    private fun appVersion(context: Context): String =
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
            "${packageInfo.versionName.orEmpty()} ($versionCode)"
        }.getOrDefault("unknown")

    private fun pruneOldReports(directory: File) {
        directory
            .listFiles()
            ?.filter { it.isFile && it.extension == "txt" }
            ?.sortedByDescending(File::lastModified)
            ?.drop(MAX_REPORTS)
            ?.forEach { it.delete() }
    }

    private fun String.redactCredentials(): String =
        replace(Regex("(?i)(api[_-]?key|token|password)(\\s*[=:]\\s*)[^\\s&,;]+"), "$1$2[REDACTED]")
            .replace(Regex("(?i)(authorization\\s*:\\s*bearer\\s+)[^\\s]+"), "$1[REDACTED]")
            .replace(Regex("(?i)([?&]y=)[^&\\s]+"), "$1[REDACTED]")
}
