package com.poorfox.physicsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import java.util.Timer;
import java.util.TimerTask;

import static com.poorfox.physicsdemo.ControlPanel.MODE_DEL;
import static com.poorfox.physicsdemo.ControlPanel.MODE_GRAB;
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
    float scale;       // pixels per meter

    public MainView(Context context)
    {
        super(context);
        mainActivity = (MainActivity) context;
        timing = new Timing();
        cameraMatrix = new Matrix();
        deviceMatrix = new Matrix();
        firstTime = true;
        controlPanel = new ControlPanel();
        scale = 200;
    }


    private void startAnimation(int fps)
    {
        this.fps = fps;
        if (timer == null) timer = new Timer("mainView.timer");
        else timer.cancel();
        if (fps <= 0) return;
        timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                invalidate();
            }
        }, 0, 1000 / fps);
    }

    void stopAnimation()
    {
        startAnimation(0);
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
        scale = height / 10;   // pixels per meter
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
        adjustScale();
        inputListener.applyGrabForce();

        worldStep();

        canvas.save();
        canvas.setMatrix(inputListener.getBackgroundPinchMatrix());
        canvas.concat(cameraMatrix);
        mainWorld.onDraw(canvas, scale);
        controlPanel.drawDebugLines(canvas, scale);
        inputListener.drawHighlights(canvas);
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

    void adjustScale(){
        scale = Pinch.getScaleFromMatrix(cameraMatrix);
        float meters = height / scale;
        float rate = 0.2f;
        float ds;
        if (meters < 0.1f) ds = meters / (meters + rate * (0.11f - meters));
        else if (meters > 1000.f) ds = meters / (meters - rate * (meters - 900.f));
        else return;
        Vec2 c = toWorld(new Vec2(width/2, height/2));
        cameraMatrix.preScale(ds,ds,c.x,c.y);
        scale = Pinch.getScaleFromMatrix(cameraMatrix);
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

    Vec2 toDevice(Vec2 v)
    {
        // implicit inverse of the deviceMatrix
        return  new Vec2((int) v.y, width - (int) v.x);
    }

    Vec2 toWorld(Vec2 v)
    {
        return mul(inverse(cameraMatrix), v);
    }

    Vec2 toBody(Body body, Vec2 v)
    {
        Vec2 w = toWorld(v).subLocal(body.getPosition());
        return Pinch.rotate(w, -body.getAngle());
    }

    public Knob findWidget(Vec2 v)
    {
        return controlPanel.findWidget(toDevice(v));
    }

    public Body findBody(Vec2 v)
    {
        return mainWorld.findBody(toWorld(v), scale);
    }

    public void onReleaseBody(Body body)
    {
        body.setLinearDamping(0);
        body.setAngularDamping(0);
        if (controlPanel.mode == MODE_DEL)
            mainWorld.world.destroyBody(body);
    }


    public void onReleaseWidget(Knob knob)
    {
        knob.onTouchEnd();
        String name = knob.name.toLowerCase();
        if (name.startsWith("pause")) {
            mainWorld.isRunning = knob.mode == 0;
            mainWorld.singleStep = knob.mode == 1;
        }
        if (name.startsWith("view"))
            controlPanel.mode = knob.mode;
    }

    public void onEndBackgroundPinch(Pinch pinch)
    {
        cameraMatrix.postConcat(pinch.getMatrix());
    }

    // Adds a line in World space
    public void addDebugLine(Vec2 p1, Vec2 p2)
    {
        controlPanel.addDebugPoint(p1);
        controlPanel.addDebugPoint(p2);
    }

    public void onGrab(Body body)
    {
        if (body.getType() != BodyType.DYNAMIC) // || controlPanel.mode != MODE_GRAB)
            return;
        body.setLinearDamping(10.f);    //  ?????
        body.setAngularDamping(10.f);
    }

    public Body addBody(Vec2 pos)
    {
        float r = 100/scale;
        BodyMaker m = BodyMaker.create();
        switch (controlPanel.get("shape").mode){
        case 0: m.ball(r); break;
        case 1: m.box(2*r,r); break;
        //case 2: m.joint(); break;
        }
        m.layer(controlPanel.get("layer").value);
        return m.addTo(mainWorld.world, pos);
    }
}
