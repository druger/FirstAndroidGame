package com.druger.firstandroidgame.objects;

import android.graphics.Rect;

/**
 * Created by druger on 04.08.2015.
 */
abstract class GameObject {
    protected open var x: Int = 0
    protected open var y: Int = 0
    protected open var dx: Int = 0
    protected open var dy: Int = 0
    protected open var width: Int = 0
    protected open var height: Int = 0

    fun getRectangle(): Rect = Rect(x, y, x + width, y + height)
}
