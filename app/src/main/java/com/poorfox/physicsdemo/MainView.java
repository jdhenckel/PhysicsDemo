package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainView extends View {

    int deviceWidth, deviceHeight;
    Matrix cameraMatrix;
    Matrix lastMatrix;
    int lastFingers;
    Timer timer;
    MainWorld mainWorld;
    InputListener inputListener;
    GravitySensor gravitySensor;
    SensorManager sensorManager;
    Context mainActivity;
    List<String> log;
    Timing timing;

    public MainView(Context context)
    {
        super(context);
        mainActivity = context;
        gravitySensor = new GravitySensor();
        log = new ArrayList<>();
        timing = new Timing();
        cameraMatrix = new Matrix();
    }

    private void firstTime()
    {
        deviceWidth = getWidth();
        deviceHeight = getHeight();

        float scale = 4;//1000;  // pixels per meter ?

        final float dt = 1 / 60.f;            // timestep in seconds

        mainWorld = new MainWorld(deviceWidth / scale, deviceHeight / scale);

        timer = new Timer("physics update");
        timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                timing.startSim();
                mainWorld.step(dt);
                mainWorld.world.setGravity(new Vec2(gravitySensor.gx, gravitySensor.gy));
                timing.start();
                invalidate();
            }
        }, 0, (long) (dt * 1000));

        inputListener = new InputListener();
        setOnTouchListener(inputListener);
        inputListener.enableDebug(this);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (deviceWidth == 0) firstTime();

        // ---- handle touches

        int fingers = inputListener.isDown;

        // At the end of the gesture, "burn" the input matrix into the camera matrix
        if (lastFingers != fingers && lastMatrix != null)
        {
            cameraMatrix.preConcat(lastMatrix);
        }

        lastFingers = fingers;
        lastMatrix = inputListener.getTransform().getMatrix();
        //---------------------

        timing.startDraw();

        canvas.save();
        canvas.setMatrix(cameraMatrix);
        canvas.concat(lastMatrix);
        mainWorld.onDraw(canvas);
        canvas.restore();
        drawWidgets(canvas);
        drawLog(canvas);
        timing.start();
    }



    public static Vec2 rotate(Vec2 v, float angle)
    {
        float c = MathUtils.cos(angle);
        float s = MathUtils.sin(angle);
        return new Vec2(c * v.x - s * v.y, s * v.x + c * v.y);
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

    public void print(String s)
    {
        log.add(s);
    }

    private void drawLog(Canvas canvas)
    {
        // for convenience normalize width to 100
        canvas.save();
        canvas.scale(deviceWidth/100.f, deviceWidth/100.f);
        Paint paint = new Paint();
        paint.setColor(0xFF80FF80);
        float f = 1.6f * (deviceHeight + deviceWidth) / deviceWidth;
        paint.setTextSize(f);
        float i = f * 3;
        for (String s : log) {
            canvas.drawText(s, f * 2, i, paint);
            i += f;
        }
        if (log.size() > 30) log.clear();
        canvas.restore();
    }

    private void drawWidgets(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(0xFF80FF80);
        for (int i = 0; i < 2000; i += 200)
        {
            canvas.drawLine(0, i, deviceWidth, i, paint);
            canvas.drawLine(i, 0, i, deviceHeight, paint);
        }
    }

    class Widget {
        int x,y;
        String label;

    }

}
