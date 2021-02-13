package com.poorfox.physicsdemo;

import android.graphics.Matrix;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class Transform
{
    Vec2 translation;
    Vec2 center;
    float rotation;
    float scale;

    Transform(float scale)
    {
        translation = new Vec2();
        center = new Vec2();
        this.scale = scale;
    }

    Transform(Vec2 translation, Vec2 center, float rotation, float scale)
    {
        this.translation = translation;
        this.center = center;
        this.rotation = rotation;
        this.scale = scale;
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
        float[] m = new float[9];
        matrix.getValues(m);
        float c = m[0] + m[4];
        float s = m[3] - m[1];
        return MathUtils.atan2(s,c);
    }

    public static float getScaleFromMatrix(Matrix matrix)
    {
        float[] m = new float[9];
        matrix.getValues(m);
        return 0.5f * (MathUtils.sqrt(m[0]*m[0] + m[1]*m[1]) + MathUtils.sqrt(m[3]*m[3] + m[4]*m[4]));
    }

    public static Vec2 getTranslationFromMatrix(Matrix matrix)
    {
        float[] m = new float[9];
        matrix.getValues(m);
        return new Vec2(m[2], m[5]);
    }



    @Override
    public String toString()
    {
        return "{t=" + translation +", c=" + center +", r=" + rotation +", s=" + scale +'}';
    }
}
