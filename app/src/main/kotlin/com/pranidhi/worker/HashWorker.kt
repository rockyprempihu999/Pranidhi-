package com.pranidhi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ai.pranidhi.db.MyDatabase
import java.security.MessageDigest

@HiltWorker
class HashWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val db: MyDatabase
): CoroutineWorker(appContext,params){
    override suspend fun doWork(): Result {
        val uriStr = inputData.getString("CONTENT_URI") ?: return Result.failure()
        val key = inputData.getString("META_KEY") ?: uriStr
        return runCatching {
            val cr = applicationContext.contentResolver
            val digest = MessageDigest.getInstance("SHA-256")
            cr.openInputStream(android.net.Uri.parse(uriStr))?.use { ins ->
                val buf = ByteArray(64 * 1024)
                var read = ins.read(buf); var total = 0
                while (read > 0) {
                    digest.update(buf, 0, read); total += read
                    if (isStopped) { db.hashingProgressQueries.insertOrReplace(key, total.toLong(), System.currentTimeMillis()); return Result.retry() }
                    read = ins.read(buf)
                }
            }
            db.hashingProgressQueries.remove(key)
            Result.success()
        }.getOrElse { Result.retry() }
    }
    companion object {
        fun schedule(ctx: Context, uri: String, key: String) {
            val work = OneTimeWorkRequestBuilder<HashWorker>()
                .setInputData(workDataOf("CONTENT_URI" to uri, "META_KEY" to key))
                .setConstraints(Constraints.Builder().setRequiresCharging(true).build())
                .build()
            WorkManager.getInstance(ctx).enqueue(work)
        }
    }
}
