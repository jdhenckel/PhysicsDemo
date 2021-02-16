package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainView extends View
{
    boolean firstTime;
    int width, height;
    Matrix cameraMatrix;
    Matrix lastMatrix;
    int lastFingers;
    Timer timer;
    Matrix deviceMatrix;
    MainWorld mainWorld;
    InputListener inputListener;
    GravitySensor gravitySensor;
    SensorManager sensorManager;
    MainActivity mainActivity;
    List<String> log;
    Timing timing;

    public MainView(Context context)
    {
        super(context);
        mainActivity = (MainActivity) context;
        gravitySensor = new GravitySensor();
        log = new ArrayList<>();
        timing = new Timing();
        cameraMatrix = new Matrix();
        deviceMatrix = new Matrix();
        firstTime = true;
    }

    private void startWorldSimulation()
    {
        final float dt = 1 / 60.f;
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
    }

    private void initialize()
    {
        firstTime = false;
        width = getWidth();
        height = getHeight();

        mainWorld = new MainWorld(10, 5); // 10* height / width);
        inputListener = new InputListener();
        setOnTouchListener(inputListener);
        inputListener.enableDebug(this);

        // Scale the camera to width 10, with origin in LOWER left
        float scale = height / 10;   // pixels per meter
        cameraMatrix.setRotate(90);
        cameraMatrix.preScale(scale, -scale);

        // Translate the device origin to the TOP left
        //deviceMatrix.setTranslate(0, 500);
//        deviceMatrix.preRotate(90);
        deviceMatrix.setRotate(90);
        deviceMatrix.preTranslate(0, -width);
        startWorldSimulation();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (firstTime) initialize();

        // ---- handle touches

        int fingers = inputListener.isDown;

        // At the end of the gesture, "burn" the input matrix into the camera matrix
        if (lastFingers != fingers && lastMatrix != null)
        {
            cameraMatrix.postConcat(lastMatrix);
        }

        lastFingers = fingers;
        if (inputListener.isDown==1)
        {
            // This code is an experiment to map from device space to world space.
            float[] pts = new float[2];
            Vec2 t = inputListener.touch[0];
            pts[0] = t.x;
            pts[1] = t.y;
            log.clear();
            print("from " + pts[0] + ", " + pts[1]);
            Matrix inv = new Matrix();
            cameraMatrix.invert(inv);
            inv.mapPoints(pts);
            print("to   " + pts[0] + ", " + pts[1]);
        }
        //else
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
        float f = 30;
        paint.setTextSize(f);
        float i = f * 3 - height;
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
        for (int i = 0; i < height; i += 200)
        {
            canvas.drawLine(0, i, height, i, paint);
            canvas.drawLine(i, 0, i, width, paint);
        }
        paint.setColor(0xffff8080);
        canvas.drawCircle(400,400,100,paint);

        //paint.setColor(0xFF000000);
        //paint.setTextSize(70);
        //canvas.drawText("EDIT", 400-50,400+20, paint);

        Widget edit = new Widget(19,19,"EDIT");
        edit.onDraw(canvas);

    }

    class Widget {
        int x,y,w,h;
        String label;
        Paint paint;

        Widget(int x, int y, String label)
        {
            this.x = x; this.y = y; this.label = label;
            w = h = 200;
            paint = new Paint();
        }

        void onDraw(Canvas canvas)
        {
            paint.setColor(Color.rgb(173, 177, 179));
            //canvas.drawRoundRect(-10,-10,290,290,29,29,paint);
            drawRect(canvas, 0, 0, -50, -50, 50);
            paint.setColor(Color.rgb(10,10,10));
            drawRect(canvas, 20, 20);
            paint.setColor(Color.rgb(63, 84, 89));
            drawRect(canvas, 0, 0);
            paint.setColor(Color.rgb(213, 221, 224));
            drawRect(canvas, 0, 0, 20, 20, 20);
            int s = Math.min(h/2, w / label.length());
            paint.setTextSize(s*1.3f);
            paint.setColor(Color.rgb(52, 57, 59));
            canvas.drawText(label, x + 0.13f * w, y + .5f * (h + s), paint);
        }

        void drawRect(Canvas canvas,int tx,int ty) { drawRect(canvas, tx, ty, 0, 0, 20); }

        void drawRect(Canvas canvas,int tx,int ty,int ex, int ey, int r)
        {
            canvas.drawRoundRect(x+tx+ex,y+ty+ey,tx-ex+x+w,ty-ey+y+h,r,r,paint);
        }

    }

}
