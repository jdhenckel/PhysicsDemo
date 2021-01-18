package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import org.jbox2d.common.Vec2;

import java.util.Timer;
import java.util.TimerTask;

public class MainView extends View {

    Paint background;
    Paint ball;
    int w,h;
    float x,y,vx,vy;
    float r;
    Timer timer;
    MainWorld world;
    GravitySensor gravitySensor;
    SensorManager sensorManager;
    Context mainActivity;


    public MainView(Context context)
    {
        super(context);
        mainActivity = context;
        gravitySensor = new GravitySensor();
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

        final float dt = 1 / 60.f;            // timestep in seconds
        final float scale = 100;

        world = new MainWorld(w/scale,h/scale);

        timer = new Timer("physics update");
        timer.schedule(new TimerTask() {
            public void run() {
            Vec2 pos = world.ball.getPosition();
            x = pos.x * scale;
            y = pos.y * scale;
            r = world.ball.getFixtureList().getShape().getRadius() * scale;
            world.step(dt);
            world.world.setGravity(new Vec2(gravitySensor.gx, gravitySensor.gy));
            invalidate();
            }
        }, 0, (long) (dt * 1000));
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (w == 0) firstTime();
        canvas.drawPaint(background);
        canvas.drawCircle(x, y, r, ball);
    }

    public void onPause() {
        sensorManager.unregisterListener(gravitySensor);
    }

    public void onResume() {
        if (sensorManager == null) sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (sensor != null)
            sensorManager.registerListener(gravitySensor, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
