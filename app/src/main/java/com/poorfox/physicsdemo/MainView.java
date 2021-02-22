package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.util.Timer;
import java.util.TimerTask;

import static com.poorfox.physicsdemo.InputListener.CAPTURE_PINCH;
import static com.poorfox.physicsdemo.Pinch.inverse;
import static com.poorfox.physicsdemo.Pinch.mul;

public class MainView extends View
{
    boolean firstTime;
    int width, height;
    Matrix cameraMatrix;
    Timer timer;
    Matrix deviceMatrix;
    MainWorld mainWorld;
    InputListener inputListener;
    GravitySensor gravitySensor;
    SensorManager sensorManager;
    ControlPanel controlPanel;
    MainActivity mainActivity;
    Timing timing;
    boolean backgroundSim;

    public MainView(Context context)
    {
        super(context);
        mainActivity = (MainActivity) context;
        gravitySensor = new GravitySensor();
        timing = new Timing();
        cameraMatrix = new Matrix();
        deviceMatrix = new Matrix();
        firstTime = true;
        controlPanel = new ControlPanel();
        mainWorld = new MainWorld();
    }

    private void startWorldSimulation()
    {
        final float dt = 1 / 60.f;
        timer = new Timer("physics update");
        timer.schedule(new TimerTask()  // AtFixedRate
        {
            public void run()
            {
                if (backgroundSim) worldStep();
                invalidate();
            }
        }, 0, (long) (dt * 1000));
    }

    private void initialize()
    {
        firstTime = false;
        width = getWidth();
        height = getHeight();        //  view is rotated 90 deg, so the "height" is actually the width.

        mainWorld.initialize(10, 7);
        inputListener = new InputListener(this);
        setOnTouchListener(inputListener);

        // Scale the camera to width 10, with origin in LOWER left
        float scale = height / 10;   // pixels per meter
        cameraMatrix.setRotate(90);
        cameraMatrix.preScale(scale, -scale);
        controlPanel.initialize(height);

        // Translate the device origin to the TOP left
        deviceMatrix.setRotate(90);
        deviceMatrix.preTranslate(0, -width);
        startWorldSimulation();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (firstTime) initialize();

        if (!backgroundSim) worldStep();

        Matrix pinchMatrix = null;
        if (inputListener.capture == CAPTURE_PINCH)
            pinchMatrix = inputListener.getPinchMatrix();


        canvas.save();
        canvas.setMatrix(pinchMatrix);
        canvas.concat(cameraMatrix);
        mainWorld.onDraw(canvas);
        canvas.restore();

        timing.startDraw();
        canvas.save();
        canvas.setMatrix(deviceMatrix);
        controlPanel.onDraw(canvas);
        canvas.restore();

        timing.stopDraw();
        controlPanel.drawTiming(canvas, timing);
    }


    void worldStep()
    {
        timing.startSim();
        mainWorld.step(1 / 60.f);
        timing.stopSim();
    }

    public void onPause()
    {
        sensorManager.unregisterListener(gravitySensor);
    }

    public void onResume()
    {
        if (sensorManager == null)
            sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (sensor != null)
            sensorManager.registerListener(gravitySensor, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void print(String s)
    {
        controlPanel.log.add(s);
    }
    public void printTop(String s)
    {
        controlPanel.log.clear();
        print(s);
    }

    public Widget findWidget(float x, float y)
    {
        // Caution, this is doing an implicit inverse of the deviceMatrix
        return controlPanel.findWidget((int)y,width - (int)x);
    }

    public Body findBody(float x, float y)
    {
        Vec2 pos = mul(inverse(cameraMatrix), new Vec2(x,y));
        return mainWorld.findBody(pos);
    }

    public void onReleaseBody(Body body)
    {
        print("release body");
    }

    public void onReleaseWidget(Widget widget)
    {
        print("release widget");
        if (widget.label.equalsIgnoreCase("play"))
            mainWorld.isRunning = !mainWorld.isRunning;
        if (widget.label.equalsIgnoreCase("mode"))
            controlPanel.mode = (controlPanel.mode % 3) + 1;
    }

    public void onEndPinch(Pinch pinch)
    {
        cameraMatrix.postConcat(pinch.getMatrix());
    }
}
