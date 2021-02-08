package com.poorfox.physicsdemo;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class InputListener implements View.OnTouchListener

{
    MainView mainView;
    Vec2[] touch = new Vec2[10];
    int isDown;
    Vec2[] firstTouch = new Vec2[2];


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

    //????????????????????????
    public Matrix getTransform()
    {
        if (isDown == 1) {
            Matrix m = new Matrix();
            Vec2 t = touch[0].sub(firstTouch[0]);
            m.setTranslate(t.x, t.y);
            return m;
        }
        if (isDown == 3) {
            Matrix m = new Matrix();
            Vec2 t = touch[0].sub(firstTouch[0]);
            m.setTranslate(t.x, t.y);
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
                //m.setRotate(toDegrees(theta));
                float s = blen / alen;
                Vec2 c = touch[0].add(touch[1]).mulLocal(0.5f);
                m.setTranslate(t.x-c.x, t.y-c.y);
                m.postScale(s, s);
                m.postTranslate(c.x, c.y);
                //mainView.print("scale " + s);
            }
            return m;
        }
        return null;
    }

    public TRS getTRS()
    {
        if (isDown == 1) {
            Vec2 t = touch[0].sub(firstTouch[0]);
            return TRS.create(t.x, t.y);
        }
        if (isDown == 3) {
            Vec2 t = touch[0].sub(firstTouch[0]);
            t.addLocal(touch[1]).subLocal(firstTouch[1]).mulLocal(0.5f);
            TRS trs = TRS.create(t.x, t.y);
            Vec2 a = firstTouch[0].sub(firstTouch[1]);
            Vec2 b = touch[0].sub(touch[1]);
            Vec2 c = touch[0].add(touch[1]).mulLocal(0.5f);
            trs.cx = c.x; trs.cy = c.y;

            float alen = a.length();
            float blen = b.length();
            if (alen > 0 && blen > 0)
            {
                float sg = Vec2.cross(a, b);
                float cc = Vec2.dot(a, b) / (alen * blen);
                float ac = cc >= 1 ? 0 : (float) Math.acos(cc);  // TODO - use mathutils ATAN2
                float theta = sg < 0 ? -ac : ac;
                trs.setRad(theta);
                trs.s = blen / alen;
            }
            return trs;
        }
        return null;
    }

    float toDegrees(float rad)
    {
        return rad * 180.f / 3.1415926f;
    }

    /*
    return X=zoom, Y=rotation (in radians, -PI ... +PI)
     *
    public Vec2 getZoomAndRotation()
    {
        return zoomRotate;
    }

    public Vec2 getPan()
    {
        return pan;
    }

    // Return the midpoint of the first two touch points
    public Vec2 getCenter()
    {
        Vec2 c = new Vec2();
        if ((isDown & 1) == 1) c.set(touch[0]);
        if ((isDown & 2) == 2) c.addLocal(touch[1]).mulLocal(0.5f);
        return c;
    }


    private void calculate()
    {
        if ((isDown & 1) == 0) pan.set(0,0);
        else pan.set(touch[0].sub(firstTouch[0]));

        if ((isDown & 3) == 3)
        {
            float alen = a.length();
            float blen = b.length();
            if (alen > 0 && blen > 0)
            {
                zoomRotate.set(blen / alen, theta);
                return;
            }
        }
        zoomRotate.set(1, 0);
    }

     */

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
//            Vec2 zr = getZoomAndRotation();
  //          mainView.print("zoom  " + zr.x);
    //        mainView.print("rot    " + zr.y);
      //      mainView.print("pan    " + getPan());
        }
    }

}
