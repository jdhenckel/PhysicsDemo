package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import org.jbox2d.common.Vec2;

public abstract class Widget
{
    Rect rect;
    String label;
    Paint paint;
    float value;
    static int u = 10;    // GLOBAL UNIT OF SCALE, set to approx native screen (w + h) / 400.

    private Widget(int x, int y, String label) {
        paint = new Paint();
        rect = new Rect(x, y, x + 20*u, y + 20*u);
        this.label = label;
        value = 0;
    }

    abstract void onDraw(Canvas canvas);

    boolean onTouchBegin(Vec2 pos) {
        return true;   // capture
    }

    void onTouchMove(Vec2 delta)
    {

    }

    void onTouchEnd(boolean inside)
    {

    }


    /*************************************************************
     * Factor methods
     */

    static Widget createButton(int x, int y, String label) {
        return new Button(x, y, label);
    }

    /*********************************************************
     * Utilities
     */

    void drawText(Canvas canvas, int color)
    {
        int w = rect.right - rect.left;
        int s = w / label.length();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(s);
        paint.setColor(color);
        canvas.drawText(label, rect.left + 0.5f * w, .5f * (rect.top + rect.bottom + s), paint);
    }

    void drawRect(Canvas canvas, int border, int grow, int shift, int r, int color)
    {
        int w = rect.right - rect.left;
        int radius = (r > 0 && w > 0) ? r * (w + grow * 2) / w : 0;
        paint.setStyle(Paint.Style.FILL);
        if (shift > 0)
        {
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rect.left + shift - grow, rect.top + shift - grow,
                    rect.right + shift + grow, rect.bottom + shift + grow, radius, radius, paint);
        }
        paint.setColor(color);
        canvas.drawRoundRect(rect.left - grow, rect.top - grow,
                rect.right + grow, rect.bottom + grow, radius, radius, paint);
        if (border > 0)
        {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(border);
            canvas.drawRoundRect(rect.left - grow, rect.top - grow,
                    rect.right + grow, rect.bottom + grow, radius, radius, paint);
        }
    }

    /**********************************************************************
     *  Subclass Implementations
     */
    private static class Button extends Widget
    {
        int r;

        Button(int x, int y, String label)
        {
            super(x, y, label);
            r = 2*u;
        }

        void onDraw(Canvas canvas)
        {
            // brown background
            drawRect(canvas, u, 5*u, 0, r, Color.rgb(122, 73, 0));
            // grey border
            drawRect(canvas, u, 0, 2*u, r, Color.rgb(63, 84, 89));
            // ivory button face
            drawRect(canvas, u, -2*u, 0, r, Color.rgb(213, 221, 224));
            // Text
            drawText(canvas, Color.rgb(52, 57, 59));
        }

        void onTouchEnd(boolean inside)
        {
            value = inside ? 1 : 0;
        }

    }

    private static class NumberBox extends Widget
    {
        int precision;

        NumberBox(int x, int y, String label, int precision)
        {
            super(x, y, label);
            precision = this.precision;
        }

        void onDraw(Canvas canvas)
        {
        }
    }

}
