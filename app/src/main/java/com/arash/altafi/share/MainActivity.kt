package com.arash.altafi.share

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.arash.altafi.share.databinding.ActivityMainBinding
import com.arash.altafi.share.utils.PermissionUtils
import com.arash.altafi.share.utils.loadCompat
import com.arash.altafi.share.utils.toGone
import com.arash.altafi.share.utils.toShow

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val listOfUrl = listOf(
        "https://arashaltafi.ir/url_sample/jpg.jpg",
        "https://arashaltafi.ir/url_sample/png.png",
        "https://arashaltafi.ir/url_sample/mp4.mp4",
        "https://arashaltafi.ir/url_sample/mpeg.mpeg",
        "https://arashaltafi.ir/url_sample/pdf.pdf",
        "https://arashaltafi.ir/url_sample/mp3.mp3",
        "https://arashaltafi.ir/url_sample/zip.zip",
    )

    private val registerStorageResult = PermissionUtils.register(
        this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is not granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    private val registerStorageResultAndroid13 = PermissionUtils.register(
        this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (
                    permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
                    permissions[Manifest.permission.READ_MEDIA_VIDEO] == true &&
                    permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission storage is not granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() = binding.apply {
        btnGetPermissionFile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (
                    !PermissionUtils.isGranted(
                        this@MainActivity,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                ) {
                    PermissionUtils.requestPermission(
                        this@MainActivity, registerStorageResultAndroid13,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission is granted sucess",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (!PermissionUtils.isGranted(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            PermissionUtils.requestPermission(
                                this@MainActivity, registerStorageResult,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        } else {
                            intentToSetting()
                        }
                    } else {
                        PermissionUtils.requestPermission(
                            this@MainActivity, registerStorageResult,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "permission is granted sucess",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        txtCopyClipBoard.setOnClickListener {
            copyToClipboard(txtCopyClipBoard.text.toString())
        }

        btnShareText.setOnClickListener {
            shareText(txtCopyClipBoard.text.toString())
        }

        btnShareImageText.setOnClickListener {
            shareTextWithImage(
                applicationInfo.packageName,
                imgArash.drawable.toBitmap(),
                "text body",
                "text title",
                "text subject"
            )
        }

        btnShareImage.setOnClickListener {
            shareImage("https://arashaltafi.ir/url_sample/jpg.jpg") { isDownloadSuccess ->
                if (isDownloadSuccess) progressBarImage.toGone() else progressBarImage.toShow()
            }
        }

        btnSaveImageFromBitmapToDownload.setOnClickListener {
            saveImageFromBitmapToDownload(imgArash.drawable.toBitmap())
        }

        btnSaveImageFromBitmapToCache.setOnClickListener {
            saveImageFromBitmapToCache(imgArash.drawable.toBitmap(), "imageName.jpg")
        }

        btnShareVideoCache.setOnClickListener {
            downloadAndShareVideoCache(
                "videoName",
                "https://arashaltafi.ir/url_sample/mp4.mp4"
            ) { isDownloadSuccess ->
                if (isDownloadSuccess) progressVideoCache.toGone() else progressVideoCache.toShow()
            }
        }

        btnShareVideoDownload.setOnClickListener {
            downloadAndShareVideoDownload(
                "videoName",
                "https://arashaltafi.ir/url_sample/mp4.mp4"
            ) { isDownloadSuccess ->
                if (isDownloadSuccess) progressVideoDownload.toGone() else progressVideoDownload.toShow()
            }
        }

        btnShareAudioCache.setOnClickListener {
            downloadAndShareAudioCache(
                "audioName",
                "https://arashaltafi.ir/url_sample/mp3.mp3"
            ) { isDownloadSuccess ->
                if (isDownloadSuccess) progressAudioCache.toGone() else progressAudioCache.toShow()
            }
        }

        btnShareAudioDownload.setOnClickListener {
            downloadAndShareAudioDownload(
                "audioName",
                "https://arashaltafi.ir/url_sample/mp3.mp3"
            ) { isDownloadSuccess ->
                if (isDownloadSuccess) progressAudioDownload.toGone() else progressAudioDownload.toShow()
            }
        }

        btnDownloadUrl.setOnClickListener {
            downloadUrl("fileName", listOfUrl.random(), {
                if (it) progressDownloadUrl.toGone() else progressDownloadUrl.toShow()
            }, { uri, type ->
                Toast.makeText(this@MainActivity, "type: $type", Toast.LENGTH_SHORT).show()
                when (type) {
                    MimType.IMAGE -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is IMAGE",
                            Toast.LENGTH_SHORT
                        ).show()
                        imgArash.loadCompat(uri)
                    }
                    MimType.VIDEO -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is VIDEO",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    MimType.AUDIO -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is AUDIO",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    MimType.PDF -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    MimType.ZIP -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is ZIP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    MimType.FILE -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Type is FILE",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }

        btnOpenUrl.setOnClickListener {
            openURL("https://arashaltafi.ir/")
        }

        btnOpenInternalUrl.setOnClickListener {
            openInternalURL("https://arashaltafi.ir/")
        }

        btnOpenUrlCustom.setOnClickListener {
            openDownloadURL("https://arashaltafi.ir/")
        }

        btnOpenCall.setOnClickListener {
            openCall("+989187677641")
        }

        btnOpenSms.setOnClickListener {
            openSMS("+989187677641", "test")
        }

        btnOpenEmail.setOnClickListener {
            openEmail(
                arrayOf("arashaltafi1377@gmail.com"),
                arrayOf("cc"),
                arrayOf("bcc"),
                "subject",
                "message"
            )
        }

        btnOpenAppInfo.setOnClickListener {
            openAppInfoSetting()
        }

        btnOpenMap.setOnClickListener {
            openMap("35.700951008708145", "51.391142781009755")
        }

        btnOpenGoogleMap.setOnClickListener {
            openGoogleMap("35.700951008708145", "51.391142781009755")
        }

        btnOpenGoogleMapNavigation.setOnClickListener {
            openGoogleMapNavigation("35.700951008708145", "51.391142781009755")
        }

        btnOpenNeshanNavigation.setOnClickListener {
            openNeshanNavigation("35.700951008708145", "51.391142781009755")
        }

        btnChooseNavigation.setOnClickListener {
            openChooseNavigation("35.700951008708145", "51.391142781009755")
        }

        btnOpenDeveloperOption.setOnClickListener {
            openDeveloperOption()
        }

        btnRestartApp.setOnClickListener {
            restartApp(MainActivity::class.java)
        }

        btnOpenAppInstagram.setOnClickListener {
            openApplication("com.instagram.android") {
                if (it.not()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Instagram not installed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun intentToSetting() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }

}