package com.poorfox.physicsdemo;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import org.jbox2d.common.Vec2;

public class Knob
{
    Rect rect;
    String name;
    Paint paint;
    int mode;
    boolean hover;
    static int u = 10;    // line width in pixels (etc). Set to approx native screen (w + h) / 400.
    int r;
    int value;
    String[] subText;
    int base;
    boolean bright;

    static String unicodeMap = ",cut=\u2700,pause=\u25a0,play=\u25b6";

    static String unicode(String s)
    {
        int i = unicodeMap.indexOf("," + s + "=");
        return i < 0 ? s : unicodeMap.charAt(i + 2 + s.length()) + "";
    }

    Knob(String name, int x, int y, String text)
    {
        paint = new Paint();
        rect = new Rect(x, y, x + 20 * u, y + 20 * u);
        this.name = name;
        mode = 0;
        if (text.contains("/"))
        {
            subText = text.split("/");
        }
        else if (text.endsWith("+b"))
        {
            subText = new String[]{text.substring(0, text.length() - 2)};
            base = 2;
        }
        else if (text.endsWith("+v"))
        {
            subText = new String[]{text.substring(0, text.length() - 2)};
            base = 1;
        }
        else if (text.endsWith("+f"))
        {
            subText = new String[]{text.substring(0, text.length() - 2)};
            base = 100;
        }
        else subText = new String[]{text};
     //   for (int i = 0; i < subText.length; ++i) subText[i] = unicode(subText[i]);
        r = 2 * u;
        value = 1;
        bright = !name.startsWith("mode");
    }

    void onDraw(Canvas canvas)
    {
        int dn = hover ? u : 0;
        // black shadow
        drawRect(canvas, 0, u, 2 * u, r, Color.BLACK);
        // grey border
        drawRect(canvas, u, 0, dn, r, Color.rgb(63, 84, 89));
        // ivory button face
        int c = bright?Color.rgb(213, 221, 224):Color.rgb(133, 141, 144);
        drawRect(canvas, u, -2 * u, dn, r, c);
        // Text
        c = Color.rgb(52, 57, 59);
        drawText(canvas, subText[mode], base > 0 ? 0 : 1, dn, c);
        if (base == 1)
            drawText(canvas, value + "", 2, dn, c);
        if (base == 2)
            drawText(canvas, Integer.toBinaryString(value + 16).substring(1), 2, dn, c);
        if (base > 2)
            drawText(canvas, (1.f * value / base) + "", 2, dn, c);
    }

    boolean onTouchBegin()
    {
        hover = true;
        return true;   // capture
    }

    void onTouchMove(int isDown, Vec2 v)
    {
        hover = (isDown & 1) == 1 && rect.contains((int) v.x, (int) v.y);
    }


    void onTouchEnd()
    {
        if (!hover) return;
        value = value % 7 + 1;
        mode = (mode + 1) % subText.length;
        hover = false;
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
        canvas.drawText(text, rect.left + 0.5f * w + shift, .5f * (rect.top + rect.bottom + s * halfLine) + shift, paint);
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

}
