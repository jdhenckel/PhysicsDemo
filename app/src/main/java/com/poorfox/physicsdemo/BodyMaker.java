package com.poorfox.physicsdemo;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class BodyMaker
{
    BodyDef bodyDef;
    FixtureDef fixtureDef;
    int color;

    public BodyMaker() {
        bodyDef = new BodyDef();
        bodyDef.setType(BodyType.DYNAMIC);
        bodyDef.setAllowSleep(false);
        fixtureDef = new FixtureDef();
        fixtureDef.setDensity(0.5f);
        fixtureDef.setRestitution(0.5f);
        fixtureDef.getFilter().categoryBits = 65535;
    }

    public static BodyMaker create() { return new BodyMaker(); }

    public BodyMaker immovable()
    {
        bodyDef.setType(BodyType.STATIC);
        return this;
    }


    public BodyMaker color(int c)
    {
        color = c;
        return this;
    }

    public Body addTo(World world, float px, float py, float rot)
    {
        return addTo(world, new Vec2(px,py), rot);
    }

    public Body addTo(World world, float px, float py)
    {
        return addTo(world, new Vec2(px,py), 0);
    }

    public Body addTo(World world, Vec2 pos, float rot)
    {
        bodyDef.setPosition(pos);
        bodyDef.setAngle(rot);
        Body b = world.createBody(bodyDef);
        b.createFixture(fixtureDef);
        BodyPainter p = new BodyPainter(b);
        if (color != 0) p.paint.setColor(color);
        b.setUserData(p);
        return b;
    }

    public Body addTo(World world, Vec2 pos)
    {
        return addTo(world, pos, 0);
    }

    public BodyMaker box(float halfWidth, float halfHeight)
    {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);
        fixtureDef.setShape(shape);
        return this;
    }

    public BodyMaker ball(float rad)
    {
        CircleShape shape = new CircleShape();
        shape.setRadius(rad);
        fixtureDef.setShape(shape);
        return this;
    }

    public BodyMaker layer(int g)
    {
        fixtureDef.getFilter().maskBits = g;
        fixtureDef.getFilter().categoryBits = g;
        return this;
    }

}
