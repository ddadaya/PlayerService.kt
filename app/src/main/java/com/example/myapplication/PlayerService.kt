package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PlayerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying: Boolean = false

    private var titleAndArtist: String = ""
    private var resourceId: Int = 22
    private var position: Int = 0
    private var songsArray: ArrayList<Songs>? = null

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel_id",
                "Player Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (titleAndArtist.isEmpty()) {
            if (intent != null) {
                titleAndArtist = intent.getStringExtra("titleAndArtist").toString()
                resourceId = intent.getIntExtra("resourceId", 0)
                position = intent.getIntExtra("position", 0)
                songsArray = intent.getSerializableExtra("songsArray") as? ArrayList<Songs>
            }
        }

        when (intent?.action) {
            "START" -> {
                resourceId = intent.getIntExtra("resourceId", 0)
                titleAndArtist = intent.getStringExtra("titleAndArtist").toString()
                start()
            }
            "PAUSE" -> {
                pause()
            }
            "RESUME" ->{
                resume()
            }
            "PREVIOUS" ->{
                previous()
            }
            "NEXT" ->{
                next()
            }
        }

        updateNotification()

        return START_STICKY
    }

    private fun updateNotification() {
        val notification = createNotification(isPlaying)
        startForeground(1, notification)
    }

    private fun start() {
        if (resourceId != 0) {
            if(mediaPlayer==null) {
                mediaPlayer.reset()
                mediaPlayer = MediaPlayer.create(this, resourceId)
                mediaPlayer.start()
                isPlaying = true
            } else{
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer = MediaPlayer.create(this, resourceId)
                mediaPlayer.start()
                isPlaying = true
            }
        }
    }

    private fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }

    private fun resume() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    private fun next() {
        mediaPlayer.reset()

        if(position==4){
            position=0
        } else
            position++

        resourceId= songsArray?.get(position)?.resourceId ?: 0
        titleAndArtist= (songsArray?.get(position)?.name ?: 0).toString()

        mediaPlayer = MediaPlayer.create(this, resourceId)
        mediaPlayer.start()

        isPlaying=true
    }

    private fun previous() {
        mediaPlayer.reset()

        if(position==0){
            position=4
        } else  position--

        resourceId= songsArray?.get(position)?.resourceId ?: 0
        titleAndArtist= (songsArray?.get(position)?.name ?: 0).toString()

        mediaPlayer = MediaPlayer.create(this, resourceId)
        mediaPlayer.start()

        isPlaying=true
    }


    private fun createNotification(isPlaying: Boolean): Notification {

        val notificationIntent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val pausePlayIntent =
            if (isPlaying) {
            Intent(this, PlayerService::class.java).setAction("PAUSE")
        } else {
            Intent(this, PlayerService::class.java).setAction("RESUME")
        }

        val pausePlayPendingIntent = PendingIntent.getService(this, 0, pausePlayIntent, PendingIntent.FLAG_IMMUTABLE)

        val pausePlayAction =
            if (isPlaying) {
            NotificationCompat.Action.Builder(R.drawable.pause, "Пауза", pausePlayPendingIntent)
            } else {
            NotificationCompat.Action.Builder(R.drawable.play, "Продолжить", pausePlayPendingIntent)
        }.build()

        val nextIntent = Intent(this, PlayerService::class.java).setAction("NEXT")
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        val nextAction = NotificationCompat.Action.Builder(android.R.drawable.ic_media_next,"Следующая", nextPendingIntent).build()

        val prevIntent = Intent(this, PlayerService::class.java).setAction("PREVIOUS")
        val prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
        val prevAction = NotificationCompat.Action.Builder(android.R.drawable.ic_media_previous,"Предыдущая", prevPendingIntent).build()

        val notification = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(titleAndArtist)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .addAction(prevAction)
            .addAction(pausePlayAction)
            .addAction(nextAction)
            .build()

        val intent = Intent("PLAYER_STATE_CHANGED")
        intent.putExtra("isPlaying", isPlaying)
        intent.putExtra("titleAndArtist", titleAndArtist)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        return notification
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}