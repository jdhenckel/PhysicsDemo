package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

/**
 * The draws a body.  The "painter" is stored in the body.setUserData()
 */
public class BodyPainter
{
    Paint paint;
    Path path;

    public BodyPainter(Body body)
    {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(randomColor());
    }

    public static int randomColor()
    {
        int hue = (int)(Math.random() * 360);
        return Color.HSVToColor(255, new float[] { hue, 1, 1 });
    }

    public void onDraw(Canvas canvas, Body body)
    {
        for (Fixture fix = body.getFixtureList(); fix != null; fix = fix.getNext())
        {
            Shape shape = fix.getShape();
            if (shape == null) continue;
            Vec2 pos = body.getPosition();
            Vec2 vel = body.getLinearVelocity();
            float ang = body.getAngle();

            switch (shape.getType()) {
            case CIRCLE:
                canvas.drawCircle(pos.x, pos.y, shape.getRadius(), paint);
                break;
            case POLYGON:
                if (path == null) path = createPath((PolygonShape)shape);
                canvas.save();
                canvas.translate(pos.x, pos.y);
                canvas.rotate(toDegrees(ang));
                canvas.drawPath(path, paint);
                canvas.restore();
                break;
            }
        }
    }


    private static float toDegrees(float radians)
    {
        return radians * 180 / 3.14159265359f;
    }


    private static Path createPath(PolygonShape poly)
    {
        Path path = new Path();
        int n = poly.getVertexCount();
        if (n > 0)
        {
            Vec2[] v = poly.getVertices();
            path.moveTo(v[0].x, v[0].y);
            for (int i = 1; i < n; ++i) path.lineTo(v[i].x, v[i].y);
            path.close();
        }
        return path;
    }

}
