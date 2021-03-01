package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.util.Timer;
import java.util.TimerTask;

import static com.poorfox.physicsdemo.Pinch.inverse;
import static com.poorfox.physicsdemo.Pinch.mul;

public class MainView extends View
{
    boolean firstTime;
    InputListener inputListener;
    int width, height, fps;
    Matrix cameraMatrix;
    Timer timer;
    Matrix deviceMatrix;
    ControlPanel controlPanel;
    MainActivity mainActivity;
    Timing timing;
    MainWorld mainWorld;

    public MainView(Context context)
    {
        super(context);
        mainActivity = (MainActivity) context;
        timing = new Timing();
        cameraMatrix = new Matrix();
        deviceMatrix = new Matrix();
        firstTime = true;
        controlPanel = new ControlPanel();
    }


    private void startAnimation(int fps)
    {
        this.fps = fps;
        if (timer == null) timer = new Timer("mainView.timer");
        else timer.cancel();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                invalidate();
            }
        }, 0, 1000 / fps);
    }

    private void initialize()
    {
        firstTime = false;
        width = getWidth();
        height = getHeight();        //  view is rotated 90 deg, so the "height" is actually the width.

        mainWorld = mainActivity.mainWorld;
        inputListener = new InputListener(this);
        setOnTouchListener(inputListener);

        // Scale the camera to width 10, with origin in LOWER left
        int scale = height / 10;   // pixels per meter
        cameraMatrix.setRotate(90);
        cameraMatrix.preScale(scale, -scale);
        controlPanel.initialize(height);

        // Translate the device origin to the TOP left
        deviceMatrix.setRotate(90);
        deviceMatrix.preTranslate(0, -width);
        startAnimation(60);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (firstTime) initialize();

        worldStep();

        canvas.save();
        canvas.setMatrix(inputListener.getBackgroundPinchMatrix());
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
        mainWorld.step(1.f / fps);
        timing.stopSim();
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
        return controlPanel.findWidget((int) y, width - (int) x);
    }

    public Body findBody(float x, float y)
    {
        Vec2 pos = mul(inverse(cameraMatrix), new Vec2(x, y));
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

    public void onEndBackgroundPinch(Pinch pinch)
    {
        cameraMatrix.postConcat(pinch.getMatrix());
    }
}
