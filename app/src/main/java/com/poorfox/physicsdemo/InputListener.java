package com.poorfox.physicsdemo;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import static com.poorfox.physicsdemo.ControlPanel.MODE_VIEW;

public class InputListener implements View.OnTouchListener

{
    MainView mainView;
    Vec2[] touch;
    int isDown;
    Vec2[] firstTouch;
    Pinch pinch;
    int capture;     // 0=none, 1=background, 2=widget, 3=body
    Widget widget;
    Body body;

    static final int CAPTURE_BACKGROUND = 1;
    static final int CAPTURE_WIDGET = 2;
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
        // A touch can be captured by one of: background, widget, or body

        int action = event.getActionMasked();
        if (capture==0 && action == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1)
        {
            body = null;
            widget = mainView.findWidget(event.getX(0), event.getY(0));
            if (widget == null && mainView.controlPanel.mode != MODE_VIEW)
                body = mainView.findBody(event.getX(0), event.getY(0));
            capture = (widget!=null) ? CAPTURE_WIDGET : (body!=null) ? CAPTURE_BODY : CAPTURE_BACKGROUND;
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

        if (isDown == 0)
        {
            if (capture == CAPTURE_BODY) mainView.onReleaseBody(body);
            if (capture == CAPTURE_WIDGET) mainView.onReleaseWidget(widget);
            capture = 0;
        }
        //mainView.printTop("Capture " + capture);

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
                float sg = Vec2.cross(a, b);
                float cc = Vec2.dot(a, b) / (alen * blen);
                float ac = cc >= 1 ? 0 : (float) Math.acos(cc);  // TODO - use mathutils ATAN2
                float theta = sg < 0 ? -ac : ac;
                pinch.rotation = theta;
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
        return pinch == null || capture != CAPTURE_BACKGROUND ? null : pinch.getMatrix();
    }

}
