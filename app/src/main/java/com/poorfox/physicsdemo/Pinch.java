package com.poorfox.physicsdemo;

import android.graphics.Matrix;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class Pinch
{
    Vec2 translation;
    Vec2 center;
    float rotation;
    float scale;

    private static float[] temp = new float[9];

    Pinch()
    {
        translation = new Vec2();
        center = new Vec2();
        this.scale = 1;
    }

    void set(Vec2 translation, Vec2 center, float rotation, float scale)
    {
        this.translation = translation;
        this.center = center;
        this.rotation = rotation;
        this.scale = scale;
    }

    void reset() {
        translation.set(0,0);
        center.set(0,0);
        rotation = 0;
        scale = 1;
    }

    // return angle in radians from a to b
    public static float angleFrom(Vec2 a, Vec2 b)
    {
        return MathUtils.atan2(Vec2.cross(a, b), Vec2.dot(a, b));
    }

    float getDegrees()
    {
        return rotation * 57.2957795131f;
    }


    Matrix getMatrix()
    {
        Matrix m = new Matrix();
        m.setTranslate(center.x, center.y);
        m.preRotate(getDegrees());
        m.preScale(scale, scale);
        m.preTranslate(translation.x - center.x, translation.y - center.y);
        return m;
    }

    public static float getAngleFromMatrix(Matrix matrix)
    {
        matrix.getValues(temp);
        float c = temp[0] + temp[4];
        float s = temp[3] - temp[1];
        return MathUtils.atan2(s,c);
    }

    public static float getScaleFromMatrix(Matrix matrix)
    {
        // return average of the abs values of scaleX and scaleY
        matrix.getValues(temp);
        float a = MathUtils.sqrt(temp[0]*temp[0] + temp[1]*temp[1]);
        float b = MathUtils.sqrt(temp[3]*temp[3] + temp[4]*temp[4]);
        return 0.5f * (a + b);
    }

    public static Vec2 getTranslationFromMatrix(Matrix matrix)
    {
        matrix.getValues(temp);
        return new Vec2(temp[2], temp[5]);
    }


    public static Matrix inverse(Matrix m)
    {
        Matrix i = new Matrix();
        m.invert(i);
        return i;
    }

    public static Vec2 mul(Matrix m, Vec2 v)
    {
        m.getValues(temp);
        return new Vec2(temp[0]*v.x + temp[1]*v.y + temp[2],
                temp[3]*v.x + temp[4]*v.y + temp[5]);
    }

    public static Vec2 mul(Matrix m, float x, float y)
    {
        m.getValues(temp);
        return new Vec2(temp[0]*x + temp[1]*y + temp[2],
                temp[3]*x + temp[4]*y + temp[5]);
    }

    public static Vec2 mulVector(Matrix m, Vec2 v)
    {
        m.getValues(temp);
        return new Vec2(temp[0]*v.x + temp[1]*v.y,
                temp[3]*v.x + temp[4]*v.y);
    }


    public static Vec2 rotate(Vec2 v, float radians)
    {
        float c = MathUtils.cos(radians);
        float s = MathUtils.sin(radians);
        return new Vec2(c * v.x - s * v.y, s * v.x + c * v.y);
    }


    @Override
    public String toString()
    {
        return "{t=" + translation +", c=" + center +", r=" + rotation +", s=" + scale +'}';
    }
}
