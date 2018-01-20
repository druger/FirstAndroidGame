package com.druger.firstandroidgame.objects;

import android.graphics.Bitmap
import android.graphics.Canvas
import com.druger.firstandroidgame.view.GamePanel

/**
 * Created by druger on 12.08.2015.
 */
class BotBorder(res: Bitmap, x: Int, y: Int) : GameObject() {
    private var image: Bitmap

    init {
        width = 20
        height = 200
        this.x = x
        this.y = y
        dx = GamePanel.MOVESPEED
        image = Bitmap.createBitmap(res, 0, 0, width, height)
    }

    fun update() {
        x += dx;
    }

    fun draw(canvas: Canvas) {
        try {
            canvas.drawBitmap(image, x.toFloat(), y.toFloat(), null);
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}
