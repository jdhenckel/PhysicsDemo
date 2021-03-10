package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import static com.poorfox.physicsdemo.ControlPanel.MODE_ADD;
import static com.poorfox.physicsdemo.ControlPanel.MODE_DEL;
import static com.poorfox.physicsdemo.ControlPanel.MODE_GRAB;

public class InputListener implements View.OnTouchListener

{
    MainView mainView;
    Vec2[] touch;
    int isDown;
    Vec2[] firstTouch;
    Pinch pinch;
    int capture;     // 0=none, 1=background, 2=knob, 3=body
    Knob knob;
    Body body;
    Vec2 bodyGrab;

    static final int CAPTURE_BACKGROUND = 1;
    static final int CAPTURE_KNOB = 2;
    static final int CAPTURE_BODY = 3;

    public InputListener(MainView mainView)
    {
        this.mainView = mainView;
        touch = new Vec2[10];
        for (int i = 0; i < touch.length; ++i) touch[i] = new Vec2();
        firstTouch = new Vec2[2];
        for (int j = 0; j < firstTouch.length; ++j) firstTouch[j] = new Vec2();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // A touch can be captured by one of: background, knob, or body

        int action = event.getActionMasked();
        int mode = mainView.controlPanel.mode;
        /*-----------------------------------------
           BEGIN TOUCH
         */
        if (capture==0 && action == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1)
        {
            capture = CAPTURE_BACKGROUND;
            body = null;
            Vec2 pos = new Vec2(event.getX(0), event.getY(0));
            knob = mainView.findKnob(pos);
            if (knob != null && knob.onTouchBegin())
                capture = CAPTURE_KNOB;
            else if (mode == MODE_DEL||mode==MODE_GRAB){
                body = mainView.findBody(pos);
                if (body != null)
                {
                    capture = CAPTURE_BODY;
                    bodyGrab = mainView.toBody(body, pos);
                    mainView.onGrab(body);
                }
            }
            else if (mode == MODE_ADD){
                body = mainView.addBody(mainView.toWorld(pos));
                capture = CAPTURE_BODY;
                bodyGrab = new Vec2();
                mainView.onGrab(body);
            }
        }
        int wasDown = isDown;
        isDown = 0;
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL)
            for (int i = 0; i < event.getPointerCount(); ++i)
                setTouch(event.getPointerId(i), event.getX(i), event.getY(i));
        if (isDown != wasDown)
        {
            if (capture == CAPTURE_BACKGROUND && pinch != null)
                mainView.onEndBackgroundPinch(pinch);
            startPinch();
        }
        else if (isDown != 0)
            pinch = getPinch();

        /*-----------------------------------------
           FINISH TOUCH
         */
        if (isDown == 0)
        {
            if (capture == CAPTURE_BODY && body != null)
                mainView.onReleaseBody(body);
            if (capture == CAPTURE_KNOB && knob != null)
                mainView.onReleaseKnob(knob);
            //if (capture == CAPTURE_BACKGROUND)  mainView.onRelease???;
            capture = 0;
        }
        /*-----------------------------------------
           MOVE TOUCH
         */
        else {
            if (capture == CAPTURE_KNOB && (wasDown &1)==1) {
                knob.onTouchMove(isDown,mainView.toDevice(touch[0]));
            }
            if (capture == CAPTURE_BODY && mainView.controlPanel.mode == MODE_DEL){
                body = mainView.findBody(touch[0]);
            }
        }

        return true;
    }


    private void setTouch(int id, float x, float y)
    {
        if (id >= touch.length) return;
        isDown = isDown | (1 << id);
        touch[id].set(x, y);
    }


    // Call this to reset the pan/zoom/rotation to the identity
    public void startPinch()
    {
        firstTouch[0].set(touch[0]);
        firstTouch[1].set(touch[1]);
        pinch = null;
    }


    public Pinch getPinch()
    {
        if (isDown == 1) {
            Vec2 t = touch[0].sub(firstTouch[0]);
            return new Pinch(t, new Vec2(), 0 , 1);
        }
        if (isDown == 3) {
            Vec2 t = touch[0].sub(firstTouch[0]).addLocal(touch[1]).subLocal(firstTouch[1]).mulLocal(0.5f);
            Vec2 c = touch[0].add(touch[1]).mulLocal(0.5f);
            Pinch pinch = new Pinch(t, c, 0, 1);

            Vec2 a = firstTouch[0].sub(firstTouch[1]);
            Vec2 b = touch[0].sub(touch[1]);
            float alen = a.length();
            float blen = b.length();

            if (alen > 0 && blen > 0)
            {
                pinch.rotation = Pinch.angleFrom(a,b);
                pinch.scale = blen / alen;
            }
            return pinch;
        }
        return new Pinch(1);
    }

    Matrix getPinchMatrix()
    {
        return pinch == null ? null : pinch.getMatrix();
    }

    Matrix getBackgroundPinchMatrix()
    {
        return capture != CAPTURE_BACKGROUND ? null : getPinchMatrix();
    }

    public void applyGrabForce()
    {
        if (capture != CAPTURE_BODY || body == null) return;
        Vec2 pos = Pinch.rotate(bodyGrab, body.getAngle()).addLocal(body.getPosition());
        Vec2 f = mainView.toWorld(touch[0]).subLocal(pos);
        body.applyForce(f.mul(body.getMass() * 40), pos);
        mainView.addDebugLine(pos, f.addLocal(pos));
    }

    public void drawHighlights(Canvas canvas)
    {
        if (capture != CAPTURE_BODY || body == null) return;
        BodyPainter.drawHighlights(canvas, body, mainView.scale);
    }
}
