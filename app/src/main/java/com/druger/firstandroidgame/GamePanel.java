package com.druger.firstandroidgame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by druger on 30.07.2015.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private Player player;
    private MainThread thread;
    private Background bg;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BotBorder> botBorder;
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;
    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;
    private Random rand = new Random();

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int best = 0;

    public GamePanel(Context context) {
        super(context);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<>();
        missiles = new ArrayList<>();
        topBorder = new ArrayList<>();
        botBorder = new ArrayList<>();

        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000){
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){

            if (!player.isPlaying() && newGameCreated && reset){
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.isPlaying()){
                if (!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){

        if (player.isPlaying()) {
            if (botBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            if (topBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met

            maxBorderHeight = 30 + player.getScore() / progressDenom;
            // cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            //check top border collision
            for (int i = 0; i < topBorder.size(); i++) {
                if (collision(topBorder.get(i), player))
                    player.setPlaying(false);
            }
            // check bottom border collision
            for (int i = 0; i < botBorder.size(); i++) {
                if (collision(botBorder.get(i), player))
                    player.setPlaying(false);
            }

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore()/4)){
                //first missile always goes down the middle
                if (missiles.size() == 0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                            45, 15, player.getScore(), 13));
                }
                // reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();

                if (collision(missiles.get(i), player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if (missiles.get(i).getX() < -100){
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10){
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                        player.getX(), player.getY()-30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if (resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
        }
    }

    private boolean collision(GameObject a, GameObject b) {
        return Rect.intersects(a.getRectangle(), b.getRectangle());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);
        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);

            bg.draw(canvas);
            if (!disappear) {
                player.draw(canvas);
            }
            //draw smokepuffs
            for (Smokepuff sp : smoke) {
                sp.draw(canvas);
            }
            //draw missiles
            for (Missile m : missiles) {
                m.draw(canvas);
            }
            //draw topborder
            for (TopBorder tb : topBorder) {
                tb.draw(canvas);
            }
            //draw botborder
            for (BotBorder bb : botBorder) {
                bb.draw(canvas);
            }
            //draw explosion
            if (started){
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        // every 50 points, insert randomly placed top blocks that break the pattern
        if (player.getScore() % 50 == 0){
            topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorder.get(topBorder.size()-1).getX() + 20, 0,
                    (int)((rand.nextDouble() * maxBorderHeight) + 1)));
        }

        for (int i = 0; i < topBorder.size(); i++) {
            topBorder.get(i).update();
            if (topBorder.get(i).getX() < -20){
                //remove element of arraylist, replace it by adding a new one
                topBorder.remove(i);

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topBorder.get(topBorder.size()-1).getHeight() >= maxBorderHeight){
                    topDown = false;
                }
                if (topBorder.get(topBorder.size()-1).getHeight() <= minBorderHeight){
                    topDown = true;
                }
                //new border added will have larger height
                if (topDown){
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorder.get(topBorder.size()-1).getX() + 20,
                            0, topBorder.get(topBorder.size()-1).getHeight() + 1));
                }
                //new border added wil have smaller height
                else {
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorder.get(topBorder.size()-1).getX() + 20,
                            0, topBorder.get(topBorder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder(){
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if (player.getScore() % 40 == 0){
            botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botBorder.get(botBorder.size()-1).getX() + 20,
                    (int)((rand.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }

        //update bottom border
        for (int i = 0; i < botBorder.size(); i++) {
            botBorder.get(i).update();
            //if border is moving off screen, remove it and add a corresponding new one
            if (botBorder.get(i).getX() < -20){
                botBorder.remove(i);

                //calculate botdown which determines the direction the border is moving (up or down)
                if (botBorder.get(botBorder.size()-1).getY() <= HEIGHT - maxBorderHeight){
                    botDown = true;
                }
                if (botBorder.get(botBorder.size()-1).getY() >= HEIGHT - minBorderHeight){
                    botDown = false;
                }
                //new border added will have larger height
                if (botDown){
                    botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), botBorder.get(botBorder.size()-1).getX() + 20,
                            botBorder.get(botBorder.size()-1).getY() + 1));
                }
                //new border added wil have smaller height
                else {
                    botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), botBorder.get(botBorder.size()-1).getX() + 20,
                            botBorder.get(botBorder.size()-1).getY() - 1));
                }
            }
        }
    }

    public void newGame(){

        disappear = false;

        botBorder.clear();
        topBorder.clear();

        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);

        if (player.getScore() > best){
            best = player.getScore();
        }

        //create initial borders

        //initial top border
        for (int i = 0; i*20 < WIDTH + 40; i++) {
            //first top border create
            if (i == 0){
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i*20, 0, 10));
            } else {
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i*20, 0, topBorder.get(i-1).getHeight() + 1));
            }
        }
        //initial bottom border
        for (int i = 0; i*20 < WIDTH + 40; i++) {
            //first border ever created
            if (i == 0){
                botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i*20, HEIGHT - minBorderHeight));
            } else {
                //adding borders until the initial screen is filed
                botBorder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i*20, botBorder.get(i-1).getY() - 1));
            }
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.isPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2 - 50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2 - 50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2 - 50, HEIGHT/2 + 40, paint1);
        }
    }
}
