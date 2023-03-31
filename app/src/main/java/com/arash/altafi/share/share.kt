package com.arash.altafi.share

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arash.altafi.share.utils.GlideUtils
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.util.*

//region share image
fun Context.shareText(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.shareImage(url: String, isDownloadSuccess: (Boolean) -> Unit) {
    isDownloadSuccess.invoke(false)
    getBitmap(url = url, result = { bitmap ->
        isDownloadSuccess.invoke(true)

        val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.close()
        val bmpUri = if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(
                this, "$packageName.fileprovider", file
            )
        }

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, bmpUri)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Image")
        startActivity(shareIntent)
    })
}

fun Context.shareTextWithImage(
    applicationId: String,
    bitmap: Bitmap,
    body: String,
    title: String,
    subject: String
) {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.close()
    val bmpUri = if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(file)
    } else {
        FileProvider.getUriForFile(
            this, "$applicationId.fileprovider", file
        )
    }

    val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body)
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, bmpUri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share News")
    startActivity(shareIntent)
}
//endregion

//region share video
@SuppressLint("Range")
fun Activity.downloadAndShareVideoCache(
    videoName: String,
    videoUrl: String,
    isDownloadSuccess: (Boolean) -> Unit
) {
    isDownloadSuccess.invoke(false)

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(videoUrl))
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setAllowedOverRoaming(true)
        .setNotificationVisibility(1)
        .setTitle("Downloading video")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    // Create a File object for the downloaded file
    val directory = externalCacheDir
    val file = File(directory, "$videoName.mp4")

    if (file.exists()) {
        file.delete()
    }

    downloadRequest.setDestinationUri(Uri.fromFile(File(externalCacheDir, "$videoName.mp4")))


    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED
        var downloadProgress = 0

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadProgress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Update the UI with the download progress
            runOnUiThread {
                // Update the UI with the download progress (e.g., using a progress bar)
            }

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            runOnUiThread {
                isDownloadSuccess.invoke(true)
                Toast.makeText(this, "Video downloaded successfully", Toast.LENGTH_SHORT).show()
            }

            // Share the downloaded video file using an intent chooser
            shareVideoCache(videoName)
        } else {
            runOnUiThread {
                isDownloadSuccess.invoke(false)
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun Context.shareVideoCache(videoName: String) {
    val file = File(externalCacheDir, "$videoName.mp4")
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Video")
    shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(shareIntent)
}


@SuppressLint("Range")
fun Activity.downloadAndShareVideoDownload(
    videoName: String,
    videoUrl: String,
    isDownloadSuccess: (Boolean) -> Unit
) {
    isDownloadSuccess.invoke(false)

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(videoUrl))
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setAllowedOverRoaming(true)
        .setTitle("Downloading video")
        .setDescription("Please wait...")
        .setNotificationVisibility(1)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    // Create a File object for the downloaded file
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(directory, "$videoName.mp4")

    if (file.exists()) {
        file.delete()
    }

    downloadRequest.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "$videoName.mp4"
    )

    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED
        var downloadProgress = 0

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadProgress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Update the UI with the download progress
            runOnUiThread {
                // Update the UI with the download progress (e.g., using a progress bar)
            }

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            runOnUiThread {
                isDownloadSuccess.invoke(true)
                Toast.makeText(this, "Video downloaded successfully", Toast.LENGTH_SHORT).show()
            }

            // Share the downloaded video file using an intent chooser
            shareVideoDownload(videoName)
        } else {
            runOnUiThread {
                isDownloadSuccess.invoke(false)
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun Context.shareVideoDownload(videoName: String) {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "$videoName.mp4"
    )
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Video")
    shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(shareIntent)
}
//endregion

//region share audio
@SuppressLint("Range")
fun Activity.downloadAndShareAudioCache(
    audioName: String,
    videoUrl: String,
    isDownloadSuccess: (Boolean) -> Unit
) {
    isDownloadSuccess.invoke(false)

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(videoUrl))
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setAllowedOverRoaming(true)
        .setNotificationVisibility(1)
        .setTitle("Downloading audio")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    // Create a File object for the downloaded file
    val directory = externalCacheDir
    val file = File(directory, "$audioName.mp3")

    if (file.exists()) {
        file.delete()
    }

    downloadRequest.setDestinationUri(Uri.fromFile(File(externalCacheDir, "$audioName.mp3")))


    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED
        var downloadProgress = 0

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadProgress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Update the UI with the download progress
            runOnUiThread {
                // Update the UI with the download progress (e.g., using a progress bar)
            }

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            runOnUiThread {
                isDownloadSuccess.invoke(true)
                Toast.makeText(this, "Video downloaded successfully", Toast.LENGTH_SHORT).show()
            }

            // Share the downloaded video file using an intent chooser
            shareAudioCache(audioName)
        } else {
            runOnUiThread {
                isDownloadSuccess.invoke(false)
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun Context.shareAudioCache(audioName: String) {
    val file = File(externalCacheDir, "$audioName.mp3")
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp3"
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Audio")
    shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(shareIntent)
}


@SuppressLint("Range")
fun Activity.downloadAndShareAudioDownload(
    audioName: String,
    videoUrl: String,
    isDownloadSuccess: (Boolean) -> Unit
) {
    isDownloadSuccess.invoke(false)

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(videoUrl))
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setAllowedOverRoaming(true)
        .setTitle("Downloading video")
        .setDescription("Please wait...")
        .setNotificationVisibility(1)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    // Create a File object for the downloaded file
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(directory, "$audioName.mp3")

    if (file.exists()) {
        file.delete()
    }

    downloadRequest.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "$audioName.mp3"
    )

    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED
        var downloadProgress = 0

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadProgress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Update the UI with the download progress
            runOnUiThread {
                // Update the UI with the download progress (e.g., using a progress bar)
            }

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            runOnUiThread {
                isDownloadSuccess.invoke(true)
                Toast.makeText(this, "Video downloaded successfully", Toast.LENGTH_SHORT).show()
            }

            // Share the downloaded video file using an intent chooser
            shareAudioDownload(audioName)
        } else {
            runOnUiThread {
                isDownloadSuccess.invoke(false)
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

private fun Context.shareAudioDownload(audioName: String) {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "$audioName.mp3"
    )
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp3"
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Audio")
    shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(shareIntent)
}
//endregion

//region download any url
@SuppressLint("Range")
fun Activity.downloadUrl(
    fileName: String,
    url: String,
    isDownloadSuccess: (Boolean) -> Unit,
    fileType: (Uri, MimType) -> Unit
) {
    isDownloadSuccess.invoke(false)

    // Get the file extension from the URL
    val fileExtension = getFileExtensionFromUrl(url)

    //set Folder
    val folderDir = Environment.DIRECTORY_DOWNLOADS
    val fileNameWithExtension = "/testFolder/$fileName.$fileExtension"

    // Get the DownloadManager system service
    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Create a DownloadManager.Request object to start the download
    val downloadRequest = DownloadManager.Request(Uri.parse(url))
        .setAllowedOverMetered(true)
        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setAllowedOverRoaming(true)
        .setTitle("Downloading File")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    //delete file if exists
    val file = File(
        Environment.getExternalStoragePublicDirectory(folderDir),
        fileNameWithExtension
    )
    if (file.exists()) {
        file.delete()
    }

    //setDestination for save file
    downloadRequest.setDestinationInExternalPublicDir(
        folderDir,
        fileNameWithExtension
    )

    // Enqueue the download request and get the download ID
    val downloadId = downloadManager.enqueue(downloadRequest)

    // Create a query to check the download status
    val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

    // Start a background thread to check the download status
    Thread {
        var isDownloaded = false
        var downloadStatus = DownloadManager.STATUS_FAILED

        while (!isDownloaded) {
            // Get the download status from the DownloadManager
            val cursor = downloadManager.query(downloadQuery)
            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                isDownloaded =
                    downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED
            }
            cursor.close()

            // Wait for a short time before checking the download status again
            Thread.sleep(500)
        }

        // Show a toast message when the download is complete
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {

            val downloadedFileUri = downloadManager.getUriForDownloadedFile(downloadId)
            val mimeTypes = getFileTypeFromUri(this, downloadedFileUri)

            runOnUiThread {
                isDownloadSuccess.invoke(true)
                fileType.invoke(
                    downloadedFileUri,
                    when (mimeTypes?.substringBefore("/")) {
                        "image" -> MimType.IMAGE
                        "video" -> MimType.VIDEO
                        "audio" -> MimType.AUDIO
                        "application" -> {
                            when (mimeTypes.substringAfter("/")) {
                                "pdf" -> MimType.PDF
                                "zip" -> MimType.ZIP
                                else -> MimType.FILE
                            }
                        }
                        else -> MimType.FILE
                    }
                )
            }
        } else {
            runOnUiThread {
                isDownloadSuccess.invoke(false)
                Toast.makeText(this, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }.start()
}

enum class MimType {
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    ZIP,
    FILE
}

private fun getFileTypeFromUri(context: Context, uri: Uri): String? {
    var type: String?
    val contentResolver = context.contentResolver
    val mime = contentResolver.getType(uri)
    if (mime != null) {
        type = mime.split("/")[0]
    } else {
        val fileExtension = getFileExtensionFromUrl(uri.toString())
        type = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        if (type != null) {
            type = type.split("/")[0]
        }
    }
    return type
}
//endregion

//region save bitmap
fun Context.saveImageFromBitmapToCache(bitmap: Bitmap, imageName: String) {
    val cacheDir = cacheDir
    val cacheFile = File(cacheDir, imageName)
    val fileOutputStream = FileOutputStream(cacheFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.flush()
    fileOutputStream.close()

    // Show a toast indicating the file was saved
    val toastMessage = "Image saved to cache directory as $imageName"
    if (cacheFile.exists()) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
}

fun Context.saveImageFromBitmapToDownload(bitmap: Bitmap) {
    // If there is already a file with the same name created by another app, the app will crash
    val imageName = "${System.currentTimeMillis()}.jpg"
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val downloadFile = File(downloadDir, imageName)

    //Check if exists, delete file
    if (downloadFile.exists()) {
        downloadFile.delete()
    }

    //Save File
    val downloadFileOutputStream = FileOutputStream(downloadFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, downloadFileOutputStream)
    downloadFileOutputStream.flush()
    downloadFileOutputStream.close()

    // Notify the system that a new file was added to the downloads directory
    MediaScannerConnection.scanFile(this, arrayOf(downloadFile.absolutePath), null, null)

    // Show a toast indicating the file was saved
    if (downloadFile.exists()) {
        Toast.makeText(
            this,
            "Image saved to downloads directory as $imageName",
            Toast.LENGTH_SHORT
        ).show()
    }
}
//endregion

//region open functions
fun Context.openCall(phoneNumber: String) {
    ContextCompat.startActivity(
        this,
        Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", phoneNumber, null)
        ),
        null
    )
}

fun Context.openSMS(mobile: String, body: String = "") {
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("sms:$mobile")
    smsIntent.putExtra("sms_body", body)
    ContextCompat.startActivity(this, smsIntent, null)
}

fun Context.openEmail(
    addresses: Array<String>,
    cc: Array<String> = emptyArray(),
    bcc: Array<String> = emptyArray(),
    subject: String? = null,
    message: String? = null
) {
    //https://developer.android.com/guide/components/intents-common#ComposeEmail
    val intentGoogle = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, addresses)
        putExtra(Intent.EXTRA_CC, cc)
        putExtra(Intent.EXTRA_BCC, bcc)
        putExtra(Intent.EXTRA_SUBJECT, subject ?: "")
        putExtra(Intent.EXTRA_TEXT, message ?: "")
    }

    if (intentGoogle.resolveActivity(packageManager) != null)
        startActivity(intentGoogle)
    else {
        Log.i("test123321", "email not found")
    }

}

fun Context.openMap(lat: String, lng: String) {
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:$lat,$lng"))
    ContextCompat.startActivity(this, intent, null)
}

fun Context.openGoogleMap(lat: String, lng: String) {
    try {
        val strUri = "http://maps.google.com/maps?q=loc:$lat,$lng"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        ContextCompat.startActivity(this, intent, null)
    } catch (e: ActivityNotFoundException) {
        Log.i("test123321", "openGoogleMap")
    }
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.copyToClipboard(text: String) {
    val myClipboard: ClipboardManager? =
        ContextCompat.getSystemService(this, ClipboardManager::class.java)

    val myClip = ClipData.newPlainText("copied:", text)
    myClipboard!!.setPrimaryClip(myClip)
}

fun Context.openGoogleMapNavigation(markerLatitude: String, markerLongitude: String) =
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$markerLatitude,$markerLongitude&travelmode=driving")
        )
    )

fun Context.openNeshanNavigation(markerLatitude: String, markerLongitude: String) =
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("nshn:$markerLatitude,$markerLongitude")
        )
    )

fun Context.openChooseNavigation(markerLatitude: String, markerLongitude: String) =
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo: $markerLatitude,$markerLongitude")
        )
    )

fun Context.openDeveloperOption() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
}

/**
 * @param launcher: usually start activity
 */
fun <T> Activity.restartApp(launcher: Class<T>, bundle: Bundle? = null) {
    Intent(this, launcher).apply {
        bundle?.let {
            putExtras(it)
        }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(this)
    }

    finish()
    Runtime.getRuntime().exit(0)
}

fun Context.openFile(uri: Uri?, fileMimType: String?) {
    try {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, fileMimType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    } catch (e: Exception) {
        Log.e("uiTest OpenFile", "openFile: $e.message")
    }
}

fun Context.getBitmap(
    url: Any,
    result: ((Bitmap) -> Unit),
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(this).getBitmapRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            error(errorRes)
        }
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}

fun Context.shareApp() {
    val app: ApplicationInfo = applicationContext.applicationInfo
    val filePath: String = app.sourceDir
    val intent = Intent(Intent.ACTION_SEND)

    // MIME of .apk is "application/vnd.android.package-archive".
    // but Bluetooth does not accept this. Let's use "*/*" instead.
    intent.type = "*/*"


    // Append file and send Intent
    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
    startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
}

fun Context.openAppInfoSetting() {
    //redirect user to app Settings
    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    i.addCategory(Intent.CATEGORY_DEFAULT)
    i.data = Uri.parse("package:$packageName")
    ContextCompat.startActivity(this, i, null)
}

fun Context.openURL(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.i("test123321", "action_view_browser_not_found")
    }
}

fun Context.openInternalURL(url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent: CustomTabsIntent = builder.build()
    customTabsIntent.launchUrl(this, Uri.parse(url))
}

fun Context.openDownloadURL(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    when {
        this.isInstalled("com.android.chrome") -> intent.setPackage("com.android.chrome")
        this.isInstalled("org.mozilla.firefox") -> intent.setPackage("org.mozilla.firefox")
        this.isInstalled("com.opera.mini.android") -> intent.setPackage("com.opera.mini.android")
        this.isInstalled("com.opera.mini.android.Browser") -> intent.setPackage("com.opera.mini.android.Browser")
        else -> this.openURL(url)
    }
    startActivity(intent)
}

fun Context.openApplication(packageName: String, isInstalled: ((Boolean) -> Unit)) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage(packageName)
        startActivity(intent)
        isInstalled.invoke(true)
    } catch (e: Exception) {
        isInstalled.invoke(false)
    }
}

private fun Context.isInstalled(packageName: String): Boolean {
    return try {
        this.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
//endregion