package com.druger.firstandroidgame.objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.druger.firstandroidgame.view.GamePanel;

/**
 * Created by druger on 12.08.2015.
 */
public class TopBorder extends GameObject {
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int height) {
        width = 20;
        this.height = height;

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
