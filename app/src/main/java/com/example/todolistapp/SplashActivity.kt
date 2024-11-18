package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Memuat animasi fade-in dan zoom-in
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

        // Menjalankan animasi pada logo
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        logoImageView.startAnimation(fadeIn)
        logoImageView.startAnimation(zoomIn)

        // Durasi splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Pindah ke LoginActivity setelah splash screen selesai
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 detik
    }
}
