package com.example.todolistapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Mengatur VideoView untuk memutar video
        val splashVideoView = findViewById<VideoView>(R.id.splashVideoView)
        val videoPath = "android.resource://${packageName}/raw/splashvideo" // splashvideo berada di folder res/raw
        val uri = Uri.parse(videoPath)
        splashVideoView.setVideoURI(uri)

        // Mengatur ukuran video agar sesuai dengan layar
        splashVideoView.setOnPreparedListener { mediaPlayer ->
            val videoWidth = mediaPlayer.videoWidth
            val videoHeight = mediaPlayer.videoHeight
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            val videoProportion = videoWidth.toFloat() / videoHeight
            val screenProportion = screenWidth.toFloat() / screenHeight

            val layoutParams = splashVideoView.layoutParams

            if (videoProportion > screenProportion) {
                layoutParams.width = screenWidth
                layoutParams.height = (screenWidth / videoProportion).toInt()
            } else {
                layoutParams.width = (screenHeight * videoProportion).toInt()
                layoutParams.height = screenHeight
            }

            splashVideoView.layoutParams = layoutParams
        }

        splashVideoView.setOnCompletionListener {
            // Pindah ke LoginActivity setelah video selesai
            navigateToLogin()
        }
        splashVideoView.start()

        // Optional: Jika video selesai sebelum 3 detik, tetap pindah setelah 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            if (!splashVideoView.isPlaying) {
                navigateToLogin()
            }
        }, 3000) // 3 detik
    }

    private fun navigateToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
