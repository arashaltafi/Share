package com.arash.altafi.share

import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream


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

fun Context.openApplication(packageName: String, isInstalled: ((Boolean)-> Unit)) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage(packageName)
        startActivity(intent)
        isInstalled.invoke(true)
    } catch(e: Exception) {
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

fun Context.share(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.shareTextWithImage(applicationId: String, bitmap: Bitmap, body: String, title:String, subject: String) {
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