package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PlayerActivity: AppCompatActivity() {
    private val playButton: ImageButton by lazy { findViewById(R.id.playButton) }
    private val nameView: TextView by lazy { findViewById(R.id.textView) }
    private var isPlaying = true
    private var titleAndArtist = ""

    private val playerStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "PLAYER_STATE_CHANGED") {
                isPlaying = intent.getBooleanExtra("isPlaying", false)
                titleAndArtist = intent.getStringExtra("titleAndArtist").toString()

                updatePlayPauseButton(isPlaying)
                nameView.text=titleAndArtist
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        LocalBroadcastManager.getInstance(this).registerReceiver(playerStateReceiver, IntentFilter("PLAYER_STATE_CHANGED"))

        titleAndArtist = intent.getStringExtra("titleAndArtist").toString()
        nameView.text=titleAndArtist

        playButton.setImageResource(android.R.drawable.ic_media_pause)

        playButton.setOnClickListener {
            playpauseToService()
        }

        val previousButton = findViewById<ImageButton>(R.id.previousButton)
        previousButton.setOnClickListener {
            val prevIntent = Intent(this, PlayerService::class.java)
            prevIntent.action = "PREVIOUS"
            startService(prevIntent)
        }

        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        nextButton.setOnClickListener {
            val prevIntent = Intent(this, PlayerService::class.java)
            prevIntent.action = "NEXT"
            startService(prevIntent)
        }

    }

    fun playpauseToService() {
        if (isPlaying) {
            playButton.setImageResource(android.R.drawable.ic_media_play)
            isPlaying=false
            val stopIntent = Intent(this, PlayerService::class.java)
            stopIntent.action = "PAUSE"
            startService(stopIntent)
        } else {

            playButton.setImageResource(android.R.drawable.ic_media_pause)
            isPlaying=true
            val startIntent = Intent(this, PlayerService::class.java)
            startIntent.action = "RESUME"
            startService(startIntent)
        }
    }

    fun updatePlayPauseButton(isPlaying: Boolean){
        if (isPlaying) {
            playButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            playButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }
}
