package com.memio.notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.widget.ImageView

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(255, 241, 154)
        window.navigationBarColor = Color.rgb(255, 241, 154)
        val image = ImageView(this).apply {
            setImageResource(R.drawable.memio_splash)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Memio"
        }
        setContentView(image)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 850)
    }
}