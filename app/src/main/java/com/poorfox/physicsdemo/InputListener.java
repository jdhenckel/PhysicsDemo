package com.poorfox.physicsdemo;

import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.Vec2;

public class InputListener implements View.OnTouchListener

{
    MainView mainView;
    Vec2[] touch;
    int isDown;
    Vec2[] firstTouch;


    public InputListener()
    {
        touch = new Vec2[10];
        for (int i = 0; i < touch.length; ++i) touch[i] = new Vec2();
        firstTouch = new Vec2[2];
        for (int j = 0; j < firstTouch.length; ++j) firstTouch[j] = new Vec2();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int action = event.getActionMasked();
        int wasDown = isDown;
        isDown = 0;
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL)
            for (int i = 0; i < event.getPointerCount(); ++i)
                setTouch(event.getPointerId(i), event.getX(i), event.getY(i));
        if (isDown != wasDown)
            startTransform();

        //if (mainView != null)            printData(action, event.getActionIndex());

        return true;
    }


    // Call this to reset the pan/zoom/rotation to the identity
    public void startTransform()
    {
        firstTouch[0].set(touch[0]);
        firstTouch[1].set(touch[1]);
    }


    public Transform getTransform()
    {
        if (isDown == 1) {
            Vec2 t = touch[0].sub(firstTouch[0]);
            return new Transform(t, new Vec2(), 0 , 1);
        }
        if (isDown == 3) {
            Vec2 t = touch[0].sub(firstTouch[0]).addLocal(touch[1]).subLocal(firstTouch[1]).mulLocal(0.5f);
            Vec2 c = touch[0].add(touch[1]).mulLocal(0.5f);
            Transform transform = new Transform(t, c, 0, 1);

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
                transform.setRadians(theta);
                transform.scale = blen / alen;
            }
            return transform;
        }
        return new Transform(1);
    }


    private void setTouch(int id, float x, float y)
    {
        if (id >= touch.length) return;
        isDown = isDown | (1 << id);
        touch[id].set(x, y);
    }

    public void enableDebug(MainView mainView)
    {
        this.mainView = mainView;
    }

    void printData(int a, int b)
    {
        mainView.log.clear();
        mainView.print("touch  " + a + (b == 0 ? "" : "  " + b));
        for (int j = 0, m = 1; m <= isDown; ++j, m *= 2)
        {
            mainView.print("data  " + ((m & isDown) == 0 ? "-" : "*") + "  " + touch[j]);
        }
        if (isDown > 0)
        {
            mainView.print("-------------");
            mainView.print("save  " + ((1 & isDown) == 0 ? "-" : "*") + "  " + firstTouch[0]);
            mainView.print("save  " + ((2 & isDown) == 0 ? "-" : "*") + "  " + firstTouch[1]);
            mainView.print("-------------");
        }
    }

}
