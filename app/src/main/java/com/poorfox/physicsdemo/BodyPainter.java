package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointEdge;

/**
 * The draws a body.  The "painter" is stored in the body.setUserData()
 */
public class BodyPainter
{
    Paint paint;
    Path path;
    Joint selectedJoint;
    float thickness;
    static Paint jointPaint;
    static Paint highlightPaint;

    static  {
        jointPaint = new Paint();
        jointPaint.setStyle(Paint.Style.FILL);
        jointPaint.setColor(0xFF777777);
        highlightPaint = new Paint();
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(0xFFFFFF40);
    }

    public BodyPainter(float thickness)
    {
        this.thickness = thickness;   // Approx average radius of the shape
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(randomColor());
    }

    public static int randomColor()
    {
        int hue = (int)(Math.random() * 360);
        return Color.HSVToColor(255, new float[] { hue, 1, 1 });
    }

    public void onDraw(Canvas canvas, Body body, float scale)
    {
        Vec2 pos = body.getPosition();
        Vec2 vel = body.getLinearVelocity();
        float ang = body.getAngle();

        for (Fixture fix = body.getFixtureList(); fix != null; fix = fix.getNext())
        {
            drawShape(canvas,fix.getShape(),pos,ang, paint);
        }

        // Note, assume all joints to background will set A=body and B=background
        for (JointEdge j = body.getJointList(); j != null; j = j.next)
        {
            if (j.joint.getBodyA() == body)
                drawJoint(canvas, j.joint, jointPaint);
        }
    }

    static void drawJoint(Canvas canvas, Joint joint, Paint paint)
    {
        Vec2 pA = new Vec2();
        joint.getAnchorA(pA);
        float r = jointSize(thickness(joint.getBodyA()), thickness(joint.getBodyB()));
        canvas.drawCircle(pA.x, pA.y, r, paint);
        Vec2 pB = new Vec2();
        joint.getAnchorB(pB);
        float d = MathUtils.distance(pA,pB);
        if (d > r) canvas.drawCircle(pB.x, pB.y, r, paint);
        if (d > r*2.5f) {
            paint.setStrokeWidth(r/4);
            canvas.drawLine(pA.x, pA.y, pB.x, pB.y, paint);
        }
    }

    static float jointSize(float t1, float t2) {
        return MathUtils.min(MathUtils.min(t1, t2) * .33f, MathUtils.max(t1, t2) * .1f);
    }

    static float thickness(Body b)
    {
        BodyPainter p = (BodyPainter) b.getUserData();
        return p==null?5.f:p.thickness;
    }

    void drawShape(Canvas canvas, Shape shape, Vec2 pos, float ang, Paint paint){
        if (shape == null) return;
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

    static void drawHighlights(Canvas canvas, Body body, float scale)
    {
        if (body==null) return;
        BodyPainter bp = (BodyPainter) body.getUserData();

        Vec2 pos = body.getPosition();
        float ang = body.getAngle();
        highlightPaint.setStrokeWidth(5/scale);

        if (bp.selectedJoint == null)
        {
            for (Fixture fix = body.getFixtureList(); fix != null; fix = fix.getNext())
                bp.drawShape(canvas, fix.getShape(), pos, ang, highlightPaint);
        }
        else
        {
            drawJoint(canvas, bp.selectedJoint, highlightPaint);
        }
    }


    public static float toDegrees(float radians)
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
