package com.druger.firstandroidgame.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.NonNull

/**
* Created by druger on 15.08.2015.
*/
class Explosion(res: Bitmap, private var x: Int, private var y: Int, private var width: Int,
                private var height: Int, numFrames: Int) {

    private var row: Int = 0
    private var animation = Animation()
    private var spriteSheet: Bitmap = res

    init {
        val image = createSpriteSheet(numFrames)
        setupAnimation(image)
    }

    private fun setupAnimation(image: ArrayList<Bitmap>) {
        animation.setFrames(image)
        animation.setDelay(10)
    }

    @NonNull
    private fun createSpriteSheet(numFrames: Int): ArrayList<Bitmap> {

        val image = ArrayList<Bitmap>(numFrames)

        for (i in 0..numFrames) {
            if (i % 5 == 0 && i > 0) {
                row++
            }
            image.add(i, Bitmap.createBitmap(spriteSheet, (i - (5 * row)) * this.width, row * this.width, this.width, this.height))
        }
        return image
    }

    fun draw(canvas: Canvas) {
        if (!animation.isPlayedOnce()) {
            canvas.drawBitmap(animation.getImage(), x.toFloat(), y.toFloat(), null)
        }
    }

    fun update() {
        if (!animation.isPlayedOnce()) {
            animation.update()
        }
    }
}
