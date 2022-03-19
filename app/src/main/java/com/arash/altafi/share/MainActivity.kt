package com.arash.altafi.share

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        btn_share_text.setOnClickListener {
            this.share(txtArash.text.toString())
        }

        btn_share_image_text.setOnClickListener {
            this.shareTextWithImage(applicationInfo.packageName, imgArash.drawable.toBitmap(), "text body", "text title", "text subject")
        }

        btn_open_url.setOnClickListener {
            this.openURL("https://arashaltafi.ir/")
        }

        btn_open_call.setOnClickListener {
            this.openCall("+989187677641")
        }

        btn_open_sms.setOnClickListener {
            this.openSMS("+989187677641", "test")
        }

        btn_open_email.setOnClickListener {
            this.openEmail(arrayOf("arashaltafi1377@gmail.com"), arrayOf("cc"), arrayOf("bcc"), "subject", "message")
        }

        btn_open_app_info.setOnClickListener {
            this.openAppInfoSetting()
        }

        btn_open_map.setOnClickListener {
            this.openMap("35.700951008708145" , "51.391142781009755")
        }

        btn_open_google_map.setOnClickListener {
            this.openGoogleMap("35.700951008708145" , "51.391142781009755")
        }

    }

}