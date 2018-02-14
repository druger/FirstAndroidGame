package com.druger.firstandroidgame.view

import android.graphics.Bitmap
import android.graphics.Canvas

/**
 * Created by druger on 02.08.2015.
 */
class Background(private var image: Bitmap) {

    private var x: Int = 0
    private var y: Int = 0
    private var dx: Int = GamePanel.MOVESPEED

    fun update() {
        x += dx
        if (x < -GamePanel.WIDTH) {
            x = 0
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(image, x.toFloat(), y.toFloat(), null)
        if (x < 0) {
            canvas.drawBitmap(image, (x + GamePanel.WIDTH).toFloat(), y.toFloat(), null)
        }
    }
}
