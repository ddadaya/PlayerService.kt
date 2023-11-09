package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.net.Uri
import android.provider.Settings

import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat


class SongListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {

            AlertDialog.Builder(this)
                .setTitle("Разрешить уведомления")
                .setMessage("Для получения уведомлений, пожалуйста, разрешите их в настройках приложения.")
                .setPositiveButton("Перейти к настройкам") { _, _ ->

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        val listView = findViewById<ListView>(R.id.listSongs)


        val songsArray = ArrayList<Songs>()
        songsArray.add(Songs("30 Seconds to Mars - A Beautiful Lie", R.raw.thirty_seconds_to_mars_a_beautiful_lie))
        songsArray.add(Songs("Nirvana - Smells Like Teen Spirit", R.raw.nirvana))
        songsArray.add(Songs("Godsmack - When Legends Rise", R.raw.godsmack))
        songsArray.add(Songs("Metallica - The Unforgiven", R.raw.metallica))
        songsArray.add(Songs("Linkin Park - Numb", R.raw.numb))

        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, songsArray.map { it.name })
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedSong = songsArray[position]
            val intentS = Intent(this, PlayerService::class.java)

            intentS.putExtra("titleAndArtist", selectedSong.name)
            intentS.putExtra("resourceId", selectedSong.resourceId)
            intentS.putExtra("position", position)
            intentS.putParcelableArrayListExtra("songsArray", ArrayList(songsArray))
            intentS.action = "START"
            startService(intentS)

            val intentA = Intent(this, PlayerActivity::class.java)
            intentA.putExtra("titleAndArtist", selectedSong.name)
            startActivity(intentA)

        }
    }
}