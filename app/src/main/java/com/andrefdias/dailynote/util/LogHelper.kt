package com.andrefdias.dailynote.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LogHelper {
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        writeLog("DEBUG", tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        writeLog("INFO", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        writeLog("WARNING", tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.e(tag, msg, tr)
            writeLog("ERROR", tag, "$msg: ${tr.localizedMessage}\n${Log.getStackTraceString(tr)}")
        } else {
            Log.e(tag, msg)
            writeLog("ERROR", tag, msg)
        }
    }

    private fun writeLog(level: String, tag: String, msg: String) {
        val ctx = appContext ?: return
        scope.launch {
            try {
                val logFile = File(ctx.cacheDir, "firenotes_logs.txt")
                val timestamp = LocalDateTime.now().format(formatter)
                val entry = "[$timestamp] [$level] [$tag] $msg\n"
                logFile.appendText(entry)
            } catch (_: Exception) {
            }
        }
    }
}
