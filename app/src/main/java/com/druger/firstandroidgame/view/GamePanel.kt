package com.druger.firstandroidgame.view

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.druger.firstandroidgame.MainThread
import com.druger.firstandroidgame.R
import com.druger.firstandroidgame.animation.Explosion
import com.druger.firstandroidgame.objects.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by druger on 30.07.2015.
 */
class GamePanel(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {
        const val WIDTH = 856
        const val HEIGHT = 480
        const val MOVESPEED = -5
    }

    private var smokeStartTime: Long = 0
    private var missileStartTime: Long = 0
    private lateinit var player: Player
    private lateinit var thread: MainThread
    private lateinit var bg: Background
    private lateinit var smoke: ArrayList<Smokepuff>
    private lateinit var missiles: ArrayList<Missile>
    private lateinit var topBorder: ArrayList<TopBorder>
    private lateinit var botBorder: ArrayList<BotBorder>
    private var maxBorderHeight: Int = 0
    private var minBorderHeight: Int = 0
    private var topDown = true
    private var botDown = true
    private var newGameCreated = false
    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private var progressDenom = 20
    private var rand = Random()

    private lateinit var explosion: Explosion
    private var startReset: Long = 0
    private var reset: Boolean = false
    private var disappear: Boolean = false
    private var started: Boolean = false
    private var best = 0

    init {
        holder.addCallback(this)

        //make gamePanel focusable so it can handle events
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        bg = Background(BitmapFactory.decodeResource(resources, R.drawable.grassbg1))
        player = Player(BitmapFactory.decodeResource(resources, R.drawable.helicopter), 65, 25, 3)
        smoke = ArrayList()
        missiles = ArrayList()
        topBorder = ArrayList()
        botBorder = ArrayList()

        smokeStartTime = System.nanoTime()
        missileStartTime = System.nanoTime()

        thread = MainThread(getHolder(), this)
        //we can safely start the game loop
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        var counter = 0
        while (retry && counter < 1000) {
            counter++
            try {
                thread.setRunning(false)
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {

            if (!player.isPlaying() && newGameCreated && reset) {
                player.setPlaying(true)
                player.setUp(true)
            }
            if (player.isPlaying()) {
                if (!started) started = true
                reset = false
                player.setUp(true)
            }
            return true
        }

        if (event.action == MotionEvent.ACTION_UP) {
            player.setUp(false)
            return true
        }

        return super.onTouchEvent(event)
    }

    fun update() {
        if (player.isPlaying()) {
            if (botBorder.isEmpty()) {
                player.setPlaying(false)
                return
            }
            if (topBorder.isEmpty()) {
                player.setPlaying(false)
                return
            }

            bg.update()
            player.update()

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met

            maxBorderHeight = 30 + player.getScore() / progressDenom
            // cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT / 4) maxBorderHeight = HEIGHT / 4
            minBorderHeight = 5 + player.getScore() / progressDenom

            (0..topBorder.size)
                    .filter { collision(topBorder[it], player) }
                    .forEach { player.setPlaying(false) }

            (0..botBorder.size)
                    .filter { collision(botBorder[it], player) }
                    .forEach { player.setPlaying(false) }

            updateTopBorder()
            updateBottomBorder()

            //add missiles on timer
            val missileElapsed = (System.nanoTime() - missileStartTime) / 1000000
            if (missileElapsed > (2000 - player.getScore() / 4)) {
                //first missile always goes down the middle
                if (missiles.size == 0) {
                    missiles.add(Missile(BitmapFactory.decodeResource(resources, R.drawable.missile),
                            WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13))
                } else {
                    missiles.add(Missile(BitmapFactory.decodeResource(resources, R.drawable.missile),
                            WIDTH + 10, (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight).toInt(),
                            45, 15, player.getScore(), 13))
                }
                // reset timer
                missileStartTime = System.nanoTime()
            }

            for (i in 0..missiles.size) {
                missiles[i].update()

                if (collision(missiles[i], player)) {
                    missiles.removeAt(i)
                    player.setPlaying(false)
                    break
                }
                //remove missile if it is way off the screen
                if (missiles[i].x < -100) {
                    missiles.removeAt(i)
                    break
                }
            }

            //add smoke puffs on timer
            val elapsed = (System.nanoTime() - smokeStartTime) / 1000000
            if (elapsed > 120) {
                smoke.add(Smokepuff(player.x, player.y + 10))
                smokeStartTime = System.nanoTime()
            }

            for (i in 0..smoke.size) {
                smoke[i].update()
                if (smoke[i].x < -10) {
                    smoke.removeAt(i)
                }
            }
        } else {
            player.resetDY()
            if (!reset) {
                newGameCreated = false
                startReset = System.nanoTime()
                reset = true
                disappear = true
                explosion = Explosion(BitmapFactory.decodeResource(resources, R.drawable.explosion),
                        player.x, player.y - 30, 100, 100, 25)
            }

            explosion.update()
            val resetElapsed = (System.nanoTime() - startReset) / 1000000

            if (resetElapsed > 2500 && !newGameCreated) {
                newGame()
            }
        }
    }

    private fun collision(a: GameObject, b: GameObject): Boolean {
        return Rect.intersects(a.getRectangle(), b.getRectangle())
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val scaleFactorX: Float = width / (WIDTH * 1.0F)
        val scaleFactorY: Float = height / (HEIGHT * 1.0F)
        val savedState = canvas.save()
        canvas.scale(scaleFactorX, scaleFactorY)

        bg.draw(canvas)
        if (!disappear) {
            player.draw(canvas)
        }

        for (sp in smoke) {
            sp.draw(canvas)
        }
        for (m in missiles) {
            m.draw(canvas)
        }
        for (tb in topBorder) {
            tb.draw(canvas)
        }
        for (bb in botBorder) {
            bb.draw(canvas)
        }
        if (started) {
            explosion.draw(canvas)
        }
        drawText(canvas)
        canvas.restoreToCount(savedState)
    }

    fun updateTopBorder() {
        // every 50 points, insert randomly placed top blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            topBorder.add(TopBorder(BitmapFactory.decodeResource(resources, R.drawable.brick),
                    topBorder[topBorder.size - 1].x + 20, 0,
                    ((rand.nextDouble() * maxBorderHeight) + 1).toInt()))
        }

        for (i in 0..topBorder.size) {
            topBorder[i].update()
            if (topBorder[i].x < -20) {
                //remove element of arraylist, replace it by adding a new one
                topBorder.removeAt(i)

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topBorder[topBorder.size - 1].height >= maxBorderHeight) {
                    topDown = false
                }
                if (topBorder[topBorder.size - 1].height <= minBorderHeight) {
                    topDown = true
                }
                //new border added will have larger height
                if (topDown) {
                    topBorder.add(TopBorder(BitmapFactory.decodeResource(resources,
                            R.drawable.brick), topBorder[topBorder.size - 1].x + 20,
                            0, topBorder[topBorder.size - 1].height + 1))
                }
                //new border added wil have smaller height
                else {
                    topBorder.add(TopBorder(BitmapFactory.decodeResource(resources,
                            R.drawable.brick), topBorder[topBorder.size - 1].x + 20,
                            0, topBorder[topBorder.size - 1].height - 1))
                }
            }
        }
    }

    fun updateBottomBorder() {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if (player.getScore() % 40 == 0) {
            botBorder.add(BotBorder(BitmapFactory.decodeResource(resources, R.drawable.brick),
                    botBorder[botBorder.size - 1].x + 20,
                    ((rand.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight)).toInt()))
        }

        for (i in 0..botBorder.size) {
            botBorder[i].update()
            //if border is moving off screen, remove it and add a corresponding new one
            if (botBorder[i].x < -20) {
                botBorder.removeAt(i)

                //calculate botdown which determines the direction the border is moving (up or down)
                if (botBorder[botBorder.size - 1].y <= HEIGHT - maxBorderHeight) {
                    botDown = true
                }
                if (botBorder[botBorder.size - 1].y >= HEIGHT - minBorderHeight) {
                    botDown = false
                }
                //new border added will have larger height
                if (botDown) {
                    botBorder.add(BotBorder(BitmapFactory.decodeResource(resources,
                            R.drawable.brick), botBorder[botBorder.size - 1].x + 20,
                            botBorder[botBorder.size - 1].y + 1))
                }
                //new border added wil have smaller height
                else {
                    botBorder.add(BotBorder(BitmapFactory.decodeResource(resources,
                            R.drawable.brick), botBorder[botBorder.size - 1].x + 20,
                            botBorder[botBorder.size - 1].y - 1))
                }
            }
        }
    }

    private fun newGame() {

        disappear = false

        botBorder.clear()
        topBorder.clear()

        missiles.clear()
        smoke.clear()

        minBorderHeight = 5
        maxBorderHeight = 30

        player.resetDY()
        player.resetScore()
        player.y = (HEIGHT / 2)

        if (player.getScore() > best) {
            best = player.getScore()
        }

        initTopBorder()
        initBottomBorder()
        newGameCreated = true
    }

    private fun initTopBorder() {
        var i = 0;
        while (i * 20 < WIDTH + 40) {
            if (i == 0) {
                topBorder.add(TopBorder(BitmapFactory.decodeResource(resources,
                        R.drawable.brick), i * 20, 0, 10))
            } else {
                topBorder.add(TopBorder(BitmapFactory.decodeResource(resources,
                        R.drawable.brick), i * 20, 0, topBorder[i - 1].height + 1))
            }
            i++
        }
    }

    private fun initBottomBorder() {
        var i = 0;
        while (i * 20 < WIDTH + 40) {
            if (i == 0) {
                botBorder.add(BotBorder(BitmapFactory.decodeResource(resources,
                        R.drawable.brick), i * 20, HEIGHT - minBorderHeight))
            } else {
                botBorder.add(BotBorder(BitmapFactory.decodeResource(resources,
                        R.drawable.brick), i * 20, botBorder[i - 1].y - 1))
            }
            i++
        }
    }


    fun drawText(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 30F
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10f, (HEIGHT - 10).toFloat(), paint)
        canvas.drawText("BEST: " + best, (WIDTH - 215).toFloat(), (HEIGHT - 10).toFloat(), paint)

        if (!player.isPlaying() && newGameCreated && reset) {
            val paint1 = Paint()
            paint1.textSize = 40F
            paint1.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("PRESS TO START", (WIDTH / 2 - 50).toFloat(), (HEIGHT / 2).toFloat(), paint1)

            paint1.textSize = 20F
            canvas.drawText("PRESS AND HOLD TO GO UP", (WIDTH / 2 - 50).toFloat(), (HEIGHT / 2 + 20).toFloat(), paint1)
            canvas.drawText("RELEASE TO GO DOWN", (WIDTH / 2 - 50).toFloat(), (HEIGHT / 2 + 40).toFloat(), paint1)
        }
    }
}
