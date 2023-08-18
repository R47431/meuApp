package com.example.time

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class MusicPlayer(private val context: Context, private val musicUri: Uri) {
    private var mediaPlayer: MediaPlayer? = null

    init {
        mediaPlayer = MediaPlayer.create(context, musicUri)
        mediaPlayer?.setOnCompletionListener {
            stopPlayback()
        }
    }

    fun startPlayback() {
        mediaPlayer?.start()
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
    }
}

