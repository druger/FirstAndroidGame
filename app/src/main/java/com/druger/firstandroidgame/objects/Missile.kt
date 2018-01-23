package com.druger.firstandroidgame.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.NonNull
import com.druger.firstandroidgame.animation.Animation
import java.util.Random
import kotlin.collections.ArrayList

/**
 * Created by druger on 09.08.2015.
 */
class Missile(res: Bitmap, x: Int, y: Int, width: Int, height: Int, score: Int, numFrames: Int) : GameObject() {
    private var speed: Int = 0
    private var rand = Random()
    private var animation = Animation()
    private var spritesheet: Bitmap
    override var width: Int = super.width - 10 //offset slightly for more realistic collision detection

    init {
        super.x = x
        super.y = y
        super.width = width
        super.height = height
        spritesheet = res

        setupSpeed(score)

        val image = createSpriteSheet(numFrames)
        setupAnimation(image)
    }

    private fun setupAnimation(image: ArrayList<Bitmap>) {
        animation.setFrames(image)
        animation.setDelay(100 - speed.toLong())
    }

    @NonNull
    private fun createSpriteSheet(numFrames: Int): ArrayList<Bitmap> {
        val image: ArrayList<Bitmap> = ArrayList(numFrames)

        for (i in 0..numFrames) {
            image.add(i, Bitmap.createBitmap(spritesheet, 0, i * this.height, this.width, this.height))
        }
        return image
    }

    private fun setupSpeed(score: Int) {
        speed = 7 + (rand.nextDouble() * score / 30).toInt()

        if (speed >= 40) {
            speed = 40
        }
    }

    fun update() {
        x -= speed
        animation.update()
    }

    fun draw(canvas: Canvas) {
        try {
            canvas.drawBitmap(animation.getImage(), x.toFloat(), y.toFloat(), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
