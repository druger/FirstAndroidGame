package com.druger.firstandroidgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by druger on 12.08.2015.
 */
public class BotBorder extends GameObject {
    private Bitmap image;

    public BotBorder(Bitmap res, int x, int y) {
        width = 20;
        height = 200;

        this.x = x;
        this.y = y;
        dx = GamePanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update(){
        x += dx;
    }

    public void draw(Canvas canvas){
        try {
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
