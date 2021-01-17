package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MainView extends View {

    Paint background;
    Paint ball;
    int w,h;
    float x,y,vx,vy;
    float r;
    Timer timer;

    public MainView(Context context)
    {
        super(context);
    }

    private void firstTime()
    {
        w = getWidth();
        h = getHeight();
        x = w/2;
        y = h/2;
        vx = vy = 2;
        r = Math.min(x,y)/7;
        background = new Paint();
        background.setStyle(Paint.Style.FILL);
        background.setColor(Color.WHITE);
        ball = new Paint();
        ball.setStyle(Paint.Style.FILL);
        ball.setColor(0xFFCD5C5C);

        timer = new Timer("physics update");
        timer.schedule(new TimerTask() {
            public void run() {
                float avx = Math.abs(vx);
                float avy = Math.abs(vy);
                float minv = 0.01f;
                x += vx;
                y += vy;
                float dt = 0.1f;
                if (x + r > w || x < r) vx = avx * Math.signum(r - x);
                if (y + r > h || y < r) vy = avy * Math.signum(r - y);
                invalidate();
            }
        }, 0, 15);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (w == 0) firstTime();
        canvas.drawPaint(background);
        canvas.drawCircle(x, y, r, ball);
    }
}
