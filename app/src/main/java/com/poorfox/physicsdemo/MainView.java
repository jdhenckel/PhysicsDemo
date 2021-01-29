package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import org.jbox2d.common.Vec2;

import java.util.Timer;
import java.util.TimerTask;

public class MainView extends View {

    int deviceWidth, deviceHeight;
    float scale;
    Timer timer;
    MainWorld mainWorld;
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
        deviceWidth = getWidth();
        deviceHeight = getHeight();
        scale = 1000;

        final float dt = 1 / 60.f;            // timestep in seconds

        mainWorld = new MainWorld(deviceWidth / scale, deviceHeight / scale);

        timer = new Timer("physics update");
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                mainWorld.step(dt);
                mainWorld.world.setGravity(new Vec2(gravitySensor.gx, gravitySensor.gy));
                invalidate();
            }
        }, 0, (long) (dt * 1000));
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (deviceWidth == 0) firstTime();
        canvas.scale(scale, scale);
        mainWorld.onDraw(canvas);
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
