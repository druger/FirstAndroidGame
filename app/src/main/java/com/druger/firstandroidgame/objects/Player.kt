package com.druger.firstandroidgame.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.NonNull
import com.druger.firstandroidgame.animation.Animation
import com.druger.firstandroidgame.view.GamePanel


/**
 * Created by druger on 04.08.2015.
 */
class Player(res: Bitmap, w: Int, h: Int, numFrames: Int) : GameObject() {
    private var spritesheet: Bitmap
    private var score: Int
    private var up: Boolean = false
    private var playing: Boolean = false
    private var animation = Animation()
    private var startTime: Long

    init {
        x = 100
        y = GamePanel.HEIGHT / 2
        dy = 0
        score = 0
        height = h
        width = w
        spritesheet = res

        val image = createSpriteSheet (numFrames)
        setupAnimation(image)
        startTime = System.nanoTime()
    }

    private fun setupAnimation(image: ArrayList<Bitmap>) {
        animation.setFrames(image)
        animation.setDelay(10)
    }

    @NonNull
    private fun createSpriteSheet(numFrames: Int): ArrayList<Bitmap> {
        val image: ArrayList<Bitmap> = ArrayList(numFrames)

        for (i in 0..numFrames) {
            image.add(i, Bitmap.createBitmap(spritesheet, i * width, 0, width, height))
        }
        return image
    }

    fun setUp(up: Boolean) {
        this.up = up
    }

    fun update() {
        val elapsed = (System.nanoTime() - startTime) / 1000000
        if (elapsed > 100) {
            score++
            startTime = System.nanoTime()
        }
        animation.update()

        if (up) {
            dy -= 1
        } else dy += 1

        if (dy > 14) dy = 14
        if (dy < -14) dy = -14

        y += dy * 2
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(animation.getImage(), x.toFloat(), y.toFloat(), null)
    }

    fun isPlaying(): Boolean {
        return playing
    }

    fun getScore(): Int {
        return score
    }

    fun setPlaying(playing: Boolean) {
        this.playing = playing
    }

    fun resetDY() {
        dy = 0
    }

    fun resetScore() {
        score = 0
    }
}
