package com.druger.firstandroidgame.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Created by druger on 07.08.2015.
 */
class Smokepuff(x: Int, y: Int) : GameObject() {
    var radius: Int = 5

    init {
        super.x = x
        super.y = y
    }

    fun update() {
        x -= 10
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL

        canvas.drawCircle((x - radius).toFloat(), (y - radius).toFloat(), radius.toFloat(), paint)
        canvas.drawCircle((x - radius + 2).toFloat(), (y - radius - 2).toFloat(), radius.toFloat(), paint)
        canvas.drawCircle((x - radius + 4).toFloat(), (y - radius + 1).toFloat(), radius.toFloat(), paint)
    }
}
