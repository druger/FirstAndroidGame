package com.druger.firstandroidgame

import android.graphics.Canvas
import android.view.SurfaceHolder

import com.druger.firstandroidgame.view.GamePanel

/**
 * Created by druger on 30.07.2015.
 */
class MainThread(private var surfaceHolder: SurfaceHolder, private var gamePanel: GamePanel) : Thread() {
    companion object {
        private const val FPS = 30
        lateinit var canvas: Canvas
    }

    private var averageFPS: Double = 0.0
    private var running: Boolean = false

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        var totalTime = 0L
        var frameCount = 0
        val targetTime = 1000 / FPS

        while (running) {
            startTime = System.nanoTime()

            try {
                canvas = this.surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    this.gamePanel.update()
                    this.gamePanel.draw(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                sleep(waitTime)
            } catch (ignored: Exception) {
            }

            totalTime += System.nanoTime() - startTime
            frameCount++
            if (frameCount == FPS) {
                averageFPS = (1000 / ((totalTime / frameCount) / 1000000)).toDouble()
                frameCount = 0
                totalTime = 0
                System.out.println(averageFPS)
            }
        }
    }

    fun setRunning(run: Boolean) {
        running = run
    }
}
