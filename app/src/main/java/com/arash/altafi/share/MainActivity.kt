package com.arash.altafi.share

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.arash.altafi.share.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() = binding.apply {
        txtArash.setOnClickListener {
            copyToClipboard(txtArash.text.toString())
        }

        btnShareText.setOnClickListener {
            share(txtArash.text.toString())
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

}