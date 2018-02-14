package com.druger.firstandroidgame.ui

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.druger.firstandroidgame.view.GamePanel


class GameActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(GamePanel(this))
    }
}
