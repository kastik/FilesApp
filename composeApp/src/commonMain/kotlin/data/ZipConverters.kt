package data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

suspend fun zip(
    filesToCompress: List<File>,
    outputZipFilePath: String,
    onFileProcessed: (Int) -> Unit,
) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(1024)
        try {
            val fos = FileOutputStream(outputZipFilePath)
            val zos = ZipOutputStream(fos)
            filesToCompress.forEach { file ->
                println("Processing ${file.name} to $outputZipFilePath")
                onFileProcessed(filesToCompress.indexOf(file) + 1)
                val ze = ZipEntry(file.name)
                val lastModifiedTime = FileTime.fromMillis(file.lastModified())
                ze.creationTime = lastModifiedTime
                ze.lastModifiedTime = lastModifiedTime
                ze.lastAccessTime = lastModifiedTime
                zos.putNextEntry(ze)
                val `in` = FileInputStream(file)
                while (true) {
                    val len = `in`.read(buffer)
                    if (len <= 0) break
                    zos.write(buffer, 0, len)
                }
                `in`.close()
            }
            zos.closeEntry()
            zos.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

suspend fun decompressZipFile(
    inputStream: InputStream,
    outputDirPath: String,
    onFileProcessed: (Int) -> Unit,
) {
    val buffer = ByteArray(1024)
    val outputDir = File(outputDirPath)

    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }

    withContext(Dispatchers.IO) {
        ZipInputStream(inputStream).use { zipInputStream ->
            var zipEntry: ZipEntry?
            var fileCount = 0
            var totalFiles = 0

            // First pass: Count the total number of entries
            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (!zipEntry!!.isDirectory) {
                    totalFiles++
                }
                zipInputStream.closeEntry()
            }

            // Reset the stream to the beginning
            inputStream.reset()
            ZipInputStream(inputStream).use { zipStream ->
                while (zipStream.nextEntry.also { zipEntry = it } != null) {
                    val newFile = File(outputDir, zipEntry!!.name)
                    if (zipEntry!!.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        // Ensure parent directories exist
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fileOutputStream ->
                            var len: Int
                            while (zipStream.read(buffer).also { len = it } > 0) {
                                fileOutputStream.write(buffer, 0, len)
                            }
                        }
                        fileCount++
                        onFileProcessed(fileCount * 100 / totalFiles) // Update progress
                    }
                    zipStream.closeEntry()
                }
            }
        }
    }
}