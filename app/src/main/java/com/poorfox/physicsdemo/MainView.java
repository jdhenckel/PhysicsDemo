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
    //float scale;
    //Vec2 zoomRotate;
    //Vec2 pan;
    //Matrix cameraMatrix;
    TRS camTRS;
    Timer timer;
    MainWorld mainWorld;
    InputListener inputListener;
    int lastFingers;
    //Matrix lastMatrix;
    TRS lastTRS;
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
        log = new ArrayList<String>();
        timing = new Timing();
        //zoomRotate = new Vec2(1000, 0);
        //pan = new Vec2(0, 0);
        //cameraMatrix = new Matrix();
    }

    private void firstTime()
    {
        deviceWidth = getWidth();
        deviceHeight = getHeight();

        float scale = 4;//1000;  // pixels per meter ?

        //cameraMatrix.setScale(scale, scale);
        camTRS = new TRS(1);
        //cameraMatrix.setTranslate(0, 0);
        //cameraMatrix.setRotate(90);

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
        if (lastFingers != fingers && lastTRS != null)
        {
            //camTRS = combine(lastTRS, camTRS);
            //cameraMatrix.postConcat(lastMatrix);
            //lastMatrix = null;

        }

        lastFingers = fingers;
        //lastMatrix = inputListener.getTransform();
        camTRS = inputListener.getTRS();
        if (camTRS == null) camTRS = new TRS(1);
        //camTRS.s *= 150;
        Vec2 cc = camTRS.getC().sub(camTRS.getT());

        //---------------------

        timing.startDraw();

        canvas.save();
        canvas.translate(camTRS.tx,camTRS.ty);
        canvas.translate(cc.x, cc.y );
        canvas.rotate(camTRS.deg);
        canvas.scale(camTRS.s,camTRS.s );
        canvas.translate(-cc.x, -cc.y );
        mainWorld.onDraw(canvas);
        canvas.restore();
        drawWidgets(canvas);
        drawLog(canvas);
        timing.start();
    }


    //canvas.translate(500,500);
    //canvas.rotate(45);
    //canvas.translate(deviceWidth/2, deviceHeight/2);
    //canvas.translate(     .72f,1.28f);
    //      canvas.translate(  cc.x, cc.y   );
    //canvas.scale(250,250);
//        canvas.translate(  -cc.x,-cc.y   );
//        canvas.translate(     -.72f,-1.28f);
    //new Vec2(camTRS.cx,camTRS.cy);
        /*
                lastTRS = TRS.create(700,1200, 0, 1000);
        canvas.translate(  bc.x,bc.y);
        */
    //print(deviceWidth + ", " + deviceHeight);
    //canvas.scale(1000,1000);
    //canvas.setMatrix(lastMatrix);
    //canvas.concat(cameraMatrix);


        /*
        Vec2 p = inputListener.getPan();
        Vec2 zr = inputListener.getZoomAndRotation();
        Vec2 center = inputListener.getCenter();

        // ??????????????
        //Mat22 m = Mat22.createRotationalTransform(zr.y);
        //Vec2 g = m.mul(center);
        //p.addLocal(center.sub(g));


        pan.addLocal(p);
        //zoomRotate.x *= zr.x;
        zoomRotate.y += zr.y;

        // the rotation will be around the body origin but it should be around the
        // center of touch. so this will adjust the pan to move the body origin to
        // the right place to keep the center from moving.

        Vec2 cp = center.sub(pan);
        Vec2 cpr = rotate(cp, zr.y);
        pan.subLocal(cpr.sub(cp));

        inputListener.startTransform();;

         */

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
