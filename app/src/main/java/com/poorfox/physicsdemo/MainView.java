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
    Matrix deviceMatrix;
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
        deviceMatrix = new Matrix();
    }

    private void firstTime()
    {
        deviceWidth = getWidth();
        deviceHeight = getHeight();
        print("width = " + deviceWidth);
        print("height = " + deviceHeight);

        float scale = deviceWidth / 10;   // pixels per meter


        final float dt = 1 / 60.f;            // timestep in seconds

        mainWorld = new MainWorld(10, 10*deviceHeight/deviceWidth);

        timer = new Timer("physics update");
        timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                timing.startSim();
                mainWorld.step(dt);
                Vec2 grav = new Vec2(gravitySensor.gy, -gravitySensor.gx);
                float r = Transform.getAngleFromMatrix(cameraMatrix);
                grav = rotate(grav, r);
                mainWorld.world.setGravity(grav);
                timing.start();
                invalidate();
            }
        }, 0, (long) (dt * 1000));

        inputListener = new InputListener();
        setOnTouchListener(inputListener);
        inputListener.enableDebug(this);

        // Scale the display to width 100, with origin in UPPER left
        float deviceScale = deviceWidth / 100;
        deviceMatrix.preScale(deviceScale, deviceScale);

        // Scale the camera to width 10, with origin in LOWER left
        cameraMatrix.setTranslate(0, -deviceHeight/scale);
        cameraMatrix.preScale(scale, -scale);
        cameraMatrix.preTranslate(0, -deviceHeight/scale);

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
            cameraMatrix.postConcat(lastMatrix);
        }

        lastFingers = fingers;
        if (inputListener.isDown==1) {

            // This code is an experiment to map from device space to world space.
            float[] pts = new float[2];
            Vec2 t = inputListener.touch[0];
            pts[0] = t.x;
            pts[1] = t.y;
            log.clear();
            print("from " + pts[0] + ", "+pts[1]);
            Matrix inv = new Matrix();
            cameraMatrix.invert(inv);
            inv.mapPoints(pts);
            print("to   " + pts[0] + ", "+pts[1]);

        }
        else
            lastMatrix = inputListener.getTransform().getMatrix();

        timing.startDraw();

        canvas.save();
        canvas.setMatrix(lastMatrix);
        canvas.concat(cameraMatrix);
        mainWorld.onDraw(canvas);
        canvas.restore();

        canvas.save();
        canvas.setMatrix(deviceMatrix);
        drawWidgets(canvas);
        drawLog(canvas);
        canvas.restore();
        timing.start();
    }

    public static Vec2 rotate(Vec2 v, float radians)
    {
        float c = MathUtils.cos(radians);
        float s = MathUtils.sin(radians);
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
        Paint paint = new Paint();
        paint.setColor(0xFF80FF80);
        float f = 2.5f;
        paint.setTextSize(f);
        float i = f * 3;
        for (String s : log) {
            canvas.drawText(s, f * 2, i, paint);
            i += f;
        }
        if (log.size() > 30) log.clear();
    }

    private void drawWidgets(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(0xFF80FF80);
        for (int i = 0; i < 110; i += 10)
        {
            canvas.drawLine(0, i, 100, i, paint);
            canvas.drawLine(i, 0, i, 100, paint);
        }
        paint.setColor(0xff8f0000);
        canvas.drawCircle(98,4,3,paint);
        paint.setColor(0xFF000000);
        paint.setTextSize(2.5f);
        canvas.drawText("EDIT", 95, 4, paint);
    }

    class Widget {
        int x,y;
        String label;

    }

}
