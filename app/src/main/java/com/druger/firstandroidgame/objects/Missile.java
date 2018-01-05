package com.druger.firstandroidgame.objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.druger.firstandroidgame.animation.Animation;

import java.util.Random;

/**
 * Created by druger on 09.08.2015.
 */
public class Missile extends GameObject {
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Missile(Bitmap res, int x, int y, int width, int height, int score, int numFrames) {
        super.x = x;
        super.y = y;
        super.width = width;
        super.height = height;

        speed = 7 + (int) (rand.nextDouble()*score/30);

        //cap missile speed
        if (speed >= 40) speed = 40;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(100 - speed);
    }

    public void update(){
        x-= speed;
        animation.update();
    }

    public void draw(Canvas canvas){
        try {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getWidth() {
        //offset slightly for more realistic collision detection
        return width - 10;
    }
}
