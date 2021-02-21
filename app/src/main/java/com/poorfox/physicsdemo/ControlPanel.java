package com.poorfox.physicsdemo;

import android.graphics.Canvas;
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

    void initialize()
    {
        widgetList.add(new Widget(10,10,"MODE"));
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


}


