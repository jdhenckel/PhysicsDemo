package com.poorfox.physicsdemo;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class TRS
{
    float tx, ty;
    float rad;
    float deg;
    float s;
    float cx, cy;

    TRS(float s)
    {
        this.s = s;
    }

    void setRad(float rad)
    {
        this.rad = rad;
        deg = rad * 57.2957795131f;
    }

    Vec2 getT() {
        return new Vec2(tx,ty);
    }
    Vec2 getC() {
        return new Vec2(cx,cy);
    }
    void setDeg(float deg)
    {
        this.deg = deg;
        rad = deg / 57.2957795131f;
    }

    static TRS create(float tx, float ty,
                      float rad, float s,
                      float cx, float cy)
    {
        TRS a = new TRS(s);
        a.tx = tx;
        a.ty = ty;
        a.rad = rad;
        a.deg = rad * 180 / 3.1415926f;
        a.cx = cx;
        a.cy = cy;
        return a;
    }

    Vec2 mult(Vec2 a)
    {
        float co = MathUtils.cos(rad) * s;
        float si = MathUtils.sin(rad) * s;
        return new Vec2(co*a.x-si*a.y+tx,si*a.x+co*a.y+ty);
    }

    Vec2 invMult(Vec2 a)
    {
        float co = MathUtils.cos(rad) / s;
        float si = MathUtils.sin(rad) / s;
        return new Vec2(co*a.x+si*a.y-co*tx-si*ty,-si*a.x+co*a.y+si*tx-co*ty);
    }




    static TRS create(float tx, float ty)
    //float cx, float cy)
    {
        TRS a = new TRS(1);
        a.tx = tx;
        a.ty = ty;
        return a;
    }



    // multiply two TRS transformations, return new TRS
    TRS combine(TRS a, TRS b)
    {
        // TODO -- adjust center of rotation?!
        float c = MathUtils.cos(a.rad) * a.s;
        float s = MathUtils.sin(a.rad) * a.s;
        return new TRS(1);

    }
}
