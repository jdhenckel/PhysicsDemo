package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/*
This class manages all the stuff on the screen that is NOT part of the world
 */
public class ControlPanel
{
    List<Widget> widgetList;
    List<String> log;
    Paint paint;
    int mode;

    static final int MODE_EDIT = 0;
    static final int MODE_VIEW = 1;
    static final int MODE_SELECT = 2;

    ControlPanel()
    {
        widgetList = new ArrayList<>();
        log = new ArrayList<>();
        paint = new Paint();
    }

    void initialize(int width)
    {
        int u = width / 200;
        Widget.u = u;
        widgetList.add(new Widget(u,u,"PLAY"));
        int w = widgetList.get(0).rect.right;
        widgetList.add(new Widget(width-w-u,u,"MODE"));
        widgetList.add(new Widget(width-w-u,u+w,"Z+"));
        widgetList.add(new Widget(width-w-u,u+2*w,"Z-"));
    }

    void onDraw(Canvas canvas)
    {
        drawGrid(canvas);
        drawWidgets(canvas);
        drawLog(canvas);
    }


    void drawLog(Canvas canvas)
    {
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
        for (Widget w : widgetList)
            w.onDraw(canvas);
    }

    Widget findWidget(int x, int y)
    {
        for (Widget w : widgetList)
            if (w.rect.contains(x, y)) return w;
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


