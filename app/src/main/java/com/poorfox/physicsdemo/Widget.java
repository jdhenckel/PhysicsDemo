package com.poorfox.physicsdemo;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import org.jbox2d.common.Vec2;

public abstract class Widget
{
    Rect rect;
    String name;
    Paint paint;
    int mode;
    boolean hover;
    static int u = 10;    // GLOBAL UNIT OF SCALE, set to approx native screen (w + h) / 400.

    private Widget(int x, int y, String name) {
        paint = new Paint();
        rect = new Rect(x, y, x + 20*u, y + 20*u);
        this.name = name;
        mode = 0;
    }

    abstract void onDraw(Canvas canvas);


    boolean onTouchBegin() {
        hover = true;
        return true;   // capture
    }

    void onTouchMove(int isDown, Vec2 v)
    {
        hover = (isDown&1)==1 && rect.contains((int)v.x, (int)v.y);
    }


    void onTouchEnd()
    {
        hover = false;
    }

    /*************************************************************
     * Factor methods
     */

    static Button createButton(int x, int y, String label) {
        return new Button(x, y, label);
    }

    /*********************************************************
     * Utilities
     */

    void drawText(Canvas canvas, String text, int halfLine, int shift, int color)
    {
        int w = rect.right - rect.left;
        int s = w / Math.max(text.length(), 4);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(s);
        paint.setColor(color);
        canvas.drawText(text, rect.left + 0.5f * w + shift, .5f * (rect.top + rect.bottom + s*halfLine) + shift, paint);
    }

    @SuppressLint("NewApi")
    void drawRect(Canvas canvas, int border, int grow, int shift, int r, int color)
    {
        int w = rect.right - rect.left;
        int radius = (r > 0 && w > 0) ? r * (w + grow * 2) / w : 0;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(rect.left + shift - grow, rect.top + shift - grow,
                rect.right + shift + grow, rect.bottom + shift + grow, radius, radius, paint);
        if (border > 0)
        {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(border);
            canvas.drawRoundRect(rect.left + shift - grow, rect.top + shift - grow,
                    rect.right + shift + grow, rect.bottom + shift + grow, radius, radius, paint);
        }
    }

    /**********************************************************************
     *  Subclass Implementations
     */
    private static class Button extends Widget
    {
        int r;
        int value;
        String[] subText;
        boolean showValue;

        Button(int x, int y, String label)
        {
            super(x, y, label);
            if (label.contains("/")){
                subText = label.split("/");
            }
            else if (label.endsWith("+v")){
                subText = new String[]{label.substring(0,label.length()-2)};
                showValue=true;
            }
            else subText = new String[]{label};
            r = 2*u;
            value=1;
        }

        void onDraw(Canvas canvas)
        {
            int dn = hover ? u : 0;
            // black shadow
            drawRect(canvas, 0, u, 2*u, r, Color.BLACK);
            // grey border
            drawRect(canvas, u, 0, dn, r, Color.rgb(63, 84, 89));
            // ivory button face
            drawRect(canvas, u, -2*u, dn, r, Color.rgb(213, 221, 224));
            // Text
            drawText(canvas, subText[mode], showValue?0:1, dn, Color.rgb(52, 57, 59));
            if (showValue)
                drawText(canvas, value+"", 2, dn, Color.rgb(52, 57, 59));
        }

        void onTouchEnd()
        {
            if (!hover) return;
            super.onTouchEnd();
            value = value % 7 + 1;
            mode = (mode + 1) % subText.length;
        }

    }

}
