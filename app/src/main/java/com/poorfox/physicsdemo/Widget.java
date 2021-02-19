package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Widget
{
    Rect rect;
    int r;
    String label;
    Paint paint;
    static int u = 10;    // GLOBAL UNIT OF SCALE, set to approx native screen (w + h) / 400.

    Widget(int x, int y, String label)
    {
        paint = new Paint();
        rect = new Rect(x, y, x + 20*u, y + 20*u);
        r = 2*u;
        this.label = label;
    }

    void onDraw(Canvas canvas)
    {
        // brown background
        drawRect(canvas, u, 5*u, 0, Color.rgb(122, 73, 0));
        // grey border
        drawRect(canvas, u, 0, 2*u, Color.rgb(63, 84, 89));
        // ivory button face
        drawRect(canvas, u, -2*u, 0, Color.rgb(213, 221, 224));
        // Text
        drawText(canvas, Color.rgb(52, 57, 59));
    }

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

    void drawRect(Canvas canvas, int border, int grow, int shift, int color)
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

}
