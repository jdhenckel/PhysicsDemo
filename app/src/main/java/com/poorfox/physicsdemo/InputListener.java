package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
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
    float bodyAngle;

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
        if (capture == 0 && action == ACTION_DOWN && event.getPointerCount() == 1)
        {
            capture = CAPTURE_BACKGROUND;
            body = null;
            Vec2 pos = new Vec2(event.getX(0), event.getY(0));
            knob = mainView.findKnob(pos);
            if (knob != null && knob.onTouchBegin())
                capture = CAPTURE_KNOB;
            else if (mode == MODE_DEL || mode == MODE_GRAB)
            {
                body = mainView.findBody(pos);
                if (body != null)
                {
                    capture = CAPTURE_BODY;
                    bodyGrab = mainView.toBody(body, pos);
                    mainView.onGrab(body);
                }
            }
            else if (mode == MODE_ADD)
            {
                body = mainView.addBody(mainView.toWorld(pos));
                capture = CAPTURE_BODY;
                bodyGrab = new Vec2();
                mainView.onGrab(body);
            }
        }
        int wasDown = isDown;
        isDown = 0;
        // Important note: the ACTION_UP and ACTION_CANCEL indicate ALL touches are released
        if (action != ACTION_UP && action != ACTION_CANCEL)
            for (int i = 0; i < event.getPointerCount(); ++i)
                setTouch(event.getPointerId(i), event.getX(i), event.getY(i));

        if (isDown != wasDown)
        {
            if (capture == CAPTURE_BACKGROUND && pinch != null && (wasDown&3) != 0)
                mainView.onEndBackgroundPinch(pinch);
            startPinch(3);
        }
        else if (isDown != 0)
            pinch = getPinch();

        if ((isDown & 3) > (wasDown & 3))
        {
            // Touch 1 or 2 was just pressed
            if (capture == CAPTURE_BODY && body != null)
            {
                bodyAngle = body.getAngle();
                bodyGrab = mainView.toBody(body, touch[0]);
            }
        }

        /*-----------------------------------------
           FINISH TOUCH
         */
        if (isDown == 0)
        {
            if (capture == CAPTURE_BODY && body != null)
                mainView.onReleaseBody(body);
            if (capture == CAPTURE_KNOB && knob != null)
                mainView.onReleaseKnob(knob);
            capture = 0;
        }
        /*-----------------------------------------
           MOVE TOUCH
         */
        else
        {
            pinch = getPinch();
            if (capture == CAPTURE_KNOB && (isDown & 1) == 1)
            {
                knob.onTouchMove(isDown, mainView.toDevice(touch[0]));
            }
            if (capture == CAPTURE_BODY && mainView.controlPanel.mode == MODE_DEL)
            {
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
    public void startPinch(int flag)
    {
        if ((flag&1) == 1) firstTouch[0].set(touch[0]);
        if ((flag&2) == 2) firstTouch[1].set(touch[1]);
        pinch = null;  //???
    }


    private Pinch getPinch()    // TODO --- needless object creation!
    {
        if (isDown == 1)
        {
            Vec2 t = touch[0].sub(firstTouch[0]);
            return new Pinch(t, new Vec2(), 0, 1);
        }
        if ((isDown & 2) == 2)
        {
            Vec2 t = touch[0].sub(firstTouch[0]).addLocal(touch[1]).subLocal(firstTouch[1]).mulLocal(0.5f);
            Vec2 c = touch[0].add(touch[1]).mulLocal(0.5f);
            Pinch pinch = new Pinch(t, c, 0, 1);

            Vec2 a = firstTouch[0].sub(firstTouch[1]);
            Vec2 b = touch[0].sub(touch[1]);
            float alen = a.length();
            float blen = b.length();

            if (alen > 0 && blen > 0)
            {
                pinch.rotation = Pinch.angleFrom(a, b);
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

    public void applyGrabForce(boolean isRunning)
    {
        if (capture != CAPTURE_BODY || body == null) return;
        final float rate = 0.3f;
        Vec2 r = Pinch.rotate(bodyGrab, body.getAngle());
        Vec2 handle = r.add(body.getPosition());
        Vec2 target = mainView.toWorld(touch[0]);
        mainView.addDebugLine(handle, target);
        if (isRunning)
        {
            // Drag using forces
            body.applyForce(target.subLocal(handle).mulLocal(body.getMass() * 40), handle);
        }
        else if ((isDown & 2) == 0)
        {
            // One finger drag
            float da = Pinch.angleFrom(r, target.sub(body.getPosition()));
            body.setTransform(target.subLocal(handle).mulLocal(rate).addLocal(body.getPosition()),
                    body.getAngle() + rate * da);
        }
        else if (pinch != null)
        {
            // Two finger drag
            body.setTransform(target.subLocal(handle).mulLocal(rate).addLocal(body.getPosition()),
                    bodyAngle - pinch.rotation);
        }
    }

    public void drawHighlights(Canvas canvas)
    {
        if (capture != CAPTURE_BODY || body == null) return;
        BodyPainter.drawHighlights(canvas, body, mainView.scale);
    }
}
