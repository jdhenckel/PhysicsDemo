package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
This class manages all the stuff on the screen that is NOT part of the world
 */
public class ControlPanel
{
    HashMap<String, Knob> widgetList;
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
        widgetList = new HashMap<>();
        log = new ArrayList<>();
        paint = new Paint();
        debugLines = new float[100];
    }

    void initialize(int width)
    {
        int u = width / 200;
        Knob.u = u;
        widgetList.put("play", new Knob(u, u, "pause/play"));
        int w = widgetList.get("play").rect.right;
        widgetList.put("mode", new Knob(width - w - u, u, "VIEW/ADD/GRAB/DEL"));
        widgetList.put("shape", new Knob(width - w - u, u + w, "BALL/BOX/JOIN"));
        widgetList.put("layer", new Knob(width - w - u, u + 2*w, "LAYER+b"));
    }

    Knob get(String s) {
        return widgetList.containsKey(s)?widgetList.get(s):null;
    }

    void onDraw(Canvas canvas)
    {
        drawGrid(canvas);
        drawWidgets(canvas);
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

    void drawGrid(Canvas canvas)
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

    void drawWidgets(Canvas canvas)
    {
        int i = 0;
        for (Knob w : widgetList.values())
        {
            w.onDraw(canvas);
            ++i;
            if (mode != 1 && i == 2) break;
        }
    }

    void drawDebugLines(Canvas canvas, float scale)
    {
        if (numDebugLines > 0)
        {
            paint.setColor(0xFFFFFF80);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5/scale);
            canvas.drawLines(debugLines, 0, numDebugLines, paint);
        }
        numDebugLines = 0;
    }


    // Call this twice to make a line
    public void addDebugPoint(Vec2 p)
    {
        if (numDebugLines + 2 <= debugLines.length) {
            debugLines[numDebugLines++] = p.x;
            debugLines[numDebugLines++] = p.y;
        }
    }


    Knob findWidget(Vec2 v)
    {
        for (Knob w : widgetList.values())
            if (w.rect.contains((int)v.x, (int)v.y)) return w;
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

}


