package com.poorfox.physicsdemo;

import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.Vec2;

public class InputListener implements View.OnTouchListener

{
    MainView mainView;
    Vec2[] touch = new Vec2[10];
    int isDown;
    Vec2[] save2 = new Vec2[2];


    public InputListener()
    {
        touch = new Vec2[10];
        for (int i = 0; i < touch.length; ++i) touch[i] = new Vec2();
        save2 = new Vec2[2];
        for (int j = 0; j < save2.length; ++j) save2[j] = new Vec2();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int a = event.getAction() & 255;
        int wasDown = isDown;
        isDown = 0;
        if (a != 1) {
            for (int i = 0; i < event.getPointerCount(); ++i)
            {
                setTouch(event.getPointerId(i), event.getX(i), event.getY(i));
            }
        }
        if (isDown == 1 && wasDown == 0)
            save2[0].set(touch[0]);
        else if (isDown == 3 && wasDown == 1)
            startTransform();

        if (mainView != null)
            printData(a, event.getAction() >> 8);

        return true;
    }

    // Call this to reset the pan/zoom/rotation to the identity
    public void startTransform()
    {
        save2[0].set(touch[0]);
        save2[1].set(touch[1]);
    }

    /*
    return X=zoom, Y=rotation (in radians, -PI ... +PI)
     */
    public Vec2 getZoomAndRotation()
    {
        if (isDown != 3) return new Vec2(1,0);
        Vec2 a = save2[0].sub(save2[1]);
        Vec2 b = touch[0].sub(touch[1]);
        float alen = a.length();
        float blen = b.length();
        float theta = (float) Math.copySign(Math.acos(Vec2.dot(a, b) / (alen * blen)), Vec2.cross(a, b));
        return new Vec2(blen / alen, theta);
    }

    public Vec2 getPan()
    {
        return (isDown != 3) ? new Vec2() : touch[0].sub(save2[0]);
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
        if (isDown == 3)
        {
            mainView.print("-------------");
            mainView.print("save  " + ((1 & isDown) == 0 ? "-" : "*") + "  " + save2[0]);
            mainView.print("save  " + ((2 & isDown) == 0 ? "-" : "*") + "  " + save2[1]);
            mainView.print("-------------");
            Vec2 zr = getZoomAndRotation();
            mainView.print("zoom  " + zr.x);
            mainView.print("rot    " + zr.y);
            mainView.print("pan    " + getPan());
        }
    }

}
