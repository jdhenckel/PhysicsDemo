package com.poorfox.physicsdemo;

import android.graphics.Matrix;
import org.jbox2d.common.Vec2;

public class Transform
{
    Vec2 translate;
    Vec2 center;
    float deg;
    float scale;

    Transform(float scale)
    {
        translate = new Vec2();
        center = new Vec2();
        this.scale = scale;
    }

    Transform(Vec2 translate, Vec2 center, float deg, float scale)
    {
        this.translate = translate;
        this.center = center;
        this.deg = deg;
        this.scale = scale;
    }

    void setRadians(float rad)
    {
        deg = rad * 57.2957795131f;
    }

    float getRadians()
    {
        return deg / 57.2957795131f;
    }


    Matrix getMatrix()
    {
        Matrix m = new Matrix();
        m.setTranslate(center.x, center.y);
        m.preRotate(deg);
        m.preScale(scale, scale);
        m.preTranslate(translate.x - center.x, translate.y - center.y);
        return m;
    }

    @Override
    public String toString()
    {
        return "{t=" + translate +", c=" + center +", d=" + deg +", s=" + scale +'}';
    }
}
