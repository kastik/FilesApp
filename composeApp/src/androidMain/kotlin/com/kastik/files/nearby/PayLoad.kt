package com.kastik.files.nearby

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import data.decompressZipFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class PayLoad(
    private val context: Context,
    private val isDownloader: Boolean = false,
    private val deleteFilesAfterWork: Boolean = false,
) : PayloadCallback() {

    var payload: Payload? = null

    private val _payloadState = MutableStateFlow<PayloadStatus>(PayloadStatus.Sending(0f))
    val payloadState = _payloadState.asStateFlow()


    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        this.payload = payload
    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        when (p1.status) {
            PayloadTransferUpdate.Status.SUCCESS -> {
                _payloadState.value = PayloadStatus.FinishedDownloading
                if (isDownloader) {
                    handleDownloadPayLoad()
                } else {
                    handUploadPayload()
                }
            }

            PayloadTransferUpdate.Status.FAILURE -> {
                _payloadState.value = PayloadStatus.Error
            }

            PayloadTransferUpdate.Status.IN_PROGRESS -> {
                _payloadState.value =
                    PayloadStatus.Sending((p1.bytesTransferred.toFloat() / p1.totalBytes))
            }

            PayloadTransferUpdate.Status.CANCELED -> {
                _payloadState.value = PayloadStatus.Canceled
            }
        }
    }

    private fun handUploadPayload() {
        val uri: Uri = payload?.asFile()?.asUri()!!
        if (deleteFilesAfterWork) {
            uri.let { context.contentResolver.delete(it, null, null) }
        }
    }

    private fun handleDownloadPayLoad() {
        val uri: Uri = payload?.asFile()?.asUri()!!
        val resolver = context.contentResolver
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resolver.openInputStream(uri).use { inputStream ->
                    inputStream?.let {
                        decompressZipFile(
                            it,
                            context.cacheDir.toString() + "/photos/",
                        ) { decopressProgress ->
                            _payloadState.value =
                                PayloadStatus.Extracting(decopressProgress.toFloat())
                        }
                    }
                }
                val files = File(context.cacheDir.toString() + "/photos/")

                files.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        exportFileToExternalStorage(context, file)
                    }
                }

            } catch (e: IOException) {
                Log.d("MyLog", "IO Exception ${e.message} ${e.cause?.cause}")
            } finally {
                if (deleteFilesAfterWork) {
                    uri.let { context.contentResolver.delete(it, null, null) }
                }
            }
        }

    }


    private fun exportFileToExternalStorage(context: Context, file: File) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.DATE_ADDED, file.lastModified())
            put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
            //put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/MyApp") // Use the appropriate directory

        }

        val externalUri: Uri? =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        externalUri?.let {
            FileInputStream(file).use { inputStream ->
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream?.write(buffer, 0, length)
                    }
                    outputStream?.flush()
                }
            }
        }
    }


    private fun getMimeType(file: File): String {
        val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(file.name)
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }
}


sealed class PayloadStatus {
    data object FinishedDownloading : PayloadStatus()
    data object Error : PayloadStatus()
    data object Canceled : PayloadStatus()
    data class Sending(val progress: Float) : PayloadStatus()
    data class Extracting(val progress: Float) : PayloadStatus()
}

