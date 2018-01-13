package com.druger.firstandroidgame.animation

import android.graphics.Bitmap

/**
* Created by druger on 04.08.2015.
*/
class Animation {
    private lateinit var frames: ArrayList<Bitmap>
    private var currentFrame: Int = 0
    private var startTime: Long = 0
    private var delay: Long = 0
    private var playedOnce: Boolean = false

    fun setFrames(frames: ArrayList<Bitmap>) {
        this.frames = frames
        currentFrame = 0
        startTime = System.nanoTime()
    }

    fun setDelay(delay: Long) {
        this.delay = delay
    }

    fun update() {
        val elapsed: Long = (System.nanoTime() - startTime) / 1000000
        if (elapsed > delay) {
            currentFrame++
            startTime = System.nanoTime()
        }
        if (currentFrame == frames.size) {
            currentFrame = 0
            playedOnce = true
        }
    }

    fun getImage(): Bitmap {
        return frames[currentFrame]
    }

    fun isPlayedOnce(): Boolean {
        return playedOnce
    }
}
