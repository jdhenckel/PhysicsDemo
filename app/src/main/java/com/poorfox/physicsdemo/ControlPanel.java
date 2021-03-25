package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import static com.poorfox.physicsdemo.Pinch.inverse;

/*
This class manages all the stuff on the screen that is NOT part of the world
 */
public class ControlPanel
{
    List<Knob> knobList;
    List<String> log;
    Paint paint;
    int mode;
    float debugLines[];
    int numDebugLines;

    static final int MODE_VIEW = 0;
    static final int MODE_ADD = 1;
    static final int MODE_GRAB = 2;
    static final int MODE_DEL = 3;

    ControlPanel()
    {
        knobList = new ArrayList<>();
        log = new ArrayList<>();
        paint = new Paint();
        debugLines = new float[100];
    }

    void initialize(int width)
    {
        int u = width / 200;
        Knob.u = u;
        knobList.add(new Knob("play", u, u, "PAUSE/PLAY"));
        int w = knobList.get(0).rect.right;
        //   knobList.add(new Knob("mode", width - w, u, "VIEW/ADD/GRAB/DEL"));
        knobList.add(new Knob("mode0", width - 4 * w, u, "VIEW"));
        knobList.add(new Knob("mode3", width - 3 * w, u, "DEL"));
        knobList.add(new Knob("mode2", width - 2 * w, u, "GRAB"));
        knobList.add(new Knob("mode1", width - w, u, "ADD"));
        knobList.add(new Knob("shape", width - w, u + w, "BALL/BOX/JOIN"));
        knobList.add(new Knob("layer", width - w, u + 2 * w, "LAYER+b"));
    }

    Knob get(String s)
    {
        for (Knob k : knobList)
            if (k.name.equalsIgnoreCase(s)) return k;
        return null;
    }

    void onDraw(Canvas canvas, float scale)
    {
        drawScale(canvas, scale);
        drawKnobs(canvas);
        drawLog(canvas);
    }


    void drawLog(Canvas canvas)
    {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF80FF80);
        float f = 60;
        paint.setTextSize(f);
        float i = f * 6;
        for (String s : log)
        {
            canvas.drawText(s, f * 2, i, paint);
            i += f;
        }
        if (log.size() > 30) log.clear();
    }

    void drawGrid_old(Canvas canvas)
    {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(0xFF80FF80);
        int height = canvas.getHeight();
        int width = canvas.getHeight();
        for (int i = 0; i < height; i += 200)
        {
            canvas.drawLine(0, i, height, i, paint);
            canvas.drawLine(i, 0, i, width, paint);
        }
    }

    void drawGrid(Canvas canvas, Matrix cameraMatrix)
    {
        final int subDiv = 5;
        final int minDiv = 4;
        float scale = Pinch.getScaleFromMatrix(cameraMatrix);
        RectF rect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        inverse(cameraMatrix).mapRect(rect);
        Vec2 p0 = new Vec2(MathUtils.min(rect.left, rect.right), MathUtils.min(rect.top, rect.bottom));
        Vec2 p1 = new Vec2(MathUtils.max(rect.left, rect.right), MathUtils.max(rect.top, rect.bottom));
        float diag = MathUtils.distance(p0, p1);
        float unit = (float) Math.pow(subDiv, Math.floor(Math.log(diag / minDiv) / Math.log(subDiv)));
        p0.set(p0.x - p0.x % unit - unit, p0.y - p0.y % unit - unit);
        p1.set(p1.x + 2 * unit, p1.y + 2 * unit);

        float alpha1 = minDiv * unit / diag;
        float alpha2 = (alpha1 + 1) / 2;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1 / scale);
        paint.setColor(Color.argb(alpha2, .5f, 1, .5f));

        for (float x = p0.x; x < p1.x; x += unit)
            canvas.drawLine(x, p0.y, x, p1.y, paint);

        for (float y = p0.y; y < p1.y; y += unit)
            canvas.drawLine(p0.x, y, p1.x, y, paint);

        float v = unit / subDiv;
        paint.setColor(Color.argb(alpha1, .5f, 1, .5f));

        for (float x = p0.x; x < p1.x; x += v)
            canvas.drawLine(x, p0.y, x, p1.y, paint);

        for (float y = p0.y; y < p1.y; y += v)
            canvas.drawLine(p0.x, y, p1.x, y, paint);
    }

    void drawScale(Canvas canvas, float scale)
    {
        int w = knobList.get(0).rect.right;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7);
        paint.setColor(0xFFFFFF80);

        final double subDiv = 5;
        float unit = (float) Math.pow(subDiv, Math.floor(Math.log(2 * w / scale) / Math.log(subDiv)));

        drawArrow(canvas, 2.1f * w, w / 2, 2.1f * w + unit * scale, w / 2, w / 8, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(format(unit, 3, "m"), 2.1f * w, .35f * w, paint);
    }


    static String format(float value, int sigDig, String unit)
    {
        if (unit == null) unit = "";
        if (unit.length() > 0)
        {
            if (value < .01)
            {
                value = value * 1000;
                unit = "m" + unit;
            }
            else if (value < 1)
            {
                value = value * 100;
                unit = "c" + unit;
            }
            else if (value > 1000)
            {
                value = value / 1000;
                unit = "k" + unit;
            }
        }
        return (new BigDecimal(value)).round(new MathContext(sigDig)).toPlainString() + unit;
    }

    static void drawArrow(Canvas canvas, float x, float y, float x2, float y2, float w, Paint paint)
    {
        canvas.drawLine(x, y, x2, y2, paint);
        if (w > 0)
        {
            Vec2 h = new Vec2(x - x2, y - y2);
            h.mulLocal(w / h.length());
            canvas.drawLine(x2, y2, x2 + h.x + h.y, y2 - h.x + h.y, paint);
            canvas.drawLine(x2, y2, x2 + h.x - h.y, y2 + h.x + h.y, paint);
        }
    }

    void drawKnobs(Canvas canvas)
    {
        int i = 0;
        for (Knob knob : knobList)
        {
            knob.onDraw(canvas);
            ++i;
            if (mode != 1 && i == 5) break;
        }
    }

    void drawDebugLines(Canvas canvas, float scale)   // TODO -- compute this based on canvas.getClipBounds???
    {
        if (numDebugLines > 0)
        {
            paint.setColor(0xFFFFFF80);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5 / scale);
            canvas.drawLines(debugLines, 0, numDebugLines, paint);
        }
        numDebugLines = 0;
    }


    // Call this twice to make a line
    public void addDebugPoint(Vec2 p)
    {
        if (numDebugLines + 2 <= debugLines.length)
        {
            debugLines[numDebugLines++] = p.x;
            debugLines[numDebugLines++] = p.y;
        }
    }


    Knob findKnob(Vec2 v)
    {
        for (Knob knob : knobList)
            if (knob.rect.contains((int) v.x, (int) v.y)) return knob;
        return null;
    }


    public void drawTiming(Canvas canvas, Timing timing)
    {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.LTGRAY);
        int h = canvas.getHeight() * 4 / 5;
        int i = h / 100;
        int dtUs = 16666;
        canvas.drawRect(2 * i, 10 * i, 5 * i, 10 * i + h, paint);

        paint.setColor(Color.RED);
        int s = timing.simUs * h / dtUs;
        canvas.drawRect(3 * i, 10 * i, 4 * i, 10 * i + s, paint);

        paint.setColor(Color.BLUE);
        int g = timing.gapUs * h / dtUs;
        int d = timing.drawUs * h / dtUs;
        canvas.drawRect(3 * i, 10 * i + s + g, 4 * i, 10 * i + s + g + d, paint);

        paint.setColor(0xffaf5000);
        int t = timing.totalUs * h / dtUs;
        canvas.drawRect(3 * i, 10 * i + t - i, 4 * i, 10 * i + t, paint);
    }

    public void setMode(int m)
    {
        get("mode" + mode).bright = false;
        get("mode" + m).bright = true;
        mode = m;
    }
}


