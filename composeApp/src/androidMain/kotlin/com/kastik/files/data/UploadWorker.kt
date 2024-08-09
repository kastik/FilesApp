package com.kastik.files.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import com.kastik.files.nearby.Connection
import com.kastik.files.nearby.PayLoad
import com.kastik.files.nearby.PayloadStatus
import data.zip
import filesToCompress
import getDataDir
import okhttp3.internal.wait
import java.io.File

class UploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val devices: Set<String>,
) :
    CoroutineWorker(appContext, workerParams) {
    private val notificationManager =
        appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        try {
            val progress = "Starting Download"
            setForeground(createForegroundInfo(progress))

            zip(
                filesToCompress = filesToCompress(),
                outputZipFilePath = getDataDir() + "/myZip.zip",
                onFileProcessed = {}) //TODO GET PROGRESS

            val filePayload: Payload =
                Payload.fromFile(File(getDataDir() + "/myZip.zip")) //TODO find a safer way todo this
            filePayload.setParentFolder("filesApp")
            filePayload.setFileName("myZip.zip")
            filePayload.setSensitive(true)
            val payLoad = PayLoad(
                context = applicationContext,
                isDownloader = false,
                deleteFilesAfterWork = false
            )
            val connectionClient = Nearby.getConnectionsClient(applicationContext)
            val connection = Connection(connectionClient, payLoad, null, devices)
            while (true) {
                when (payLoad.payloadState.value) {
                    PayloadStatus.Canceled -> Result.failure()
                    PayloadStatus.Error -> Result.failure()
                    PayloadStatus.FinishedDownloading -> Result.success()
                    else -> kotlin.Result.wait()
                }
            }
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "service_channel"
        val title = "Running"
        val cancel = "Cancel"
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        createChannel()


        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            //.setSmallIcon()
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(1, notification)
    }

    private fun createChannel() {
        val name = "Service Channel"
        val descriptionText =
            "This notification channel will inform users about zipping/unzipping and upload/download of data"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("service_channel", name, importance)
        mChannel.description = descriptionText
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)


    }

}
