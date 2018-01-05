package com.druger.firstandroidgame.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by druger on 15.08.2015.
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int width, int height, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            if (i % 5 == 0 && i > 0) row++;
            image[i] = Bitmap.createBitmap(spritesheet, (i-(5*row))*width, row*width, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void draw(Canvas canvas){
        if (!animation.isPlayedOnce()){
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }
    }

    public void update(){
        if (!animation.isPlayedOnce()){
            animation.update();
        }
    }

    public int getHeight() {
        return height;
    }
}
