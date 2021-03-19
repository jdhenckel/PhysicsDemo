package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointEdge;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class MainWorld
{

    World world;
    int velocityIterations, positionIterations;
    boolean isRunning;
    boolean singleStep;
    Vec2 limit;

    /*
         Box2D tolerances have been tuned to work well with meters-kilogram-second (MKS) units.
         Box2D has been tuned to work well with moving shapes between 0.1 and 10 meters.
         So this means objects between soup cans and buses in size should work well.
         Static shapes may be up to 50 meters long without trouble.
     */
    public MainWorld()
    {
        Vec2 gravity = new Vec2(0.0f, -10.0f);
        world = new World(gravity);
        limit = new Vec2(1000,500);
    }

    public void initialize(float w, float h)
    {
        float hx=w/14,hy=w/20;
        BodyMaker.create().ball(w / 14).layer(1).addTo(world, w / 2, h / 2);
        Body a = BodyMaker.create().box(hx,hy).layer(2).addTo(world, 5*hx,6*hy);
        Body b = BodyMaker.create().box(hx,hy).layer(2).addTo(world, 7*hx,8*hy);
        BodyMaker.create().box(hx,hy).layer(2).addTo(world, w / 2.5f, h / 1.7f);

        JointMaker.create(a,b,new Vec2(6*hx,7*hy)).revolute().limitDeg(-90,90).addTo(world);

        isRunning = true;
        velocityIterations = 6;
        positionIterations = 3;

        createBoundary();
    }

    void createBoundary()
    {
        int g = Color.DKGRAY;
        int gr = Color.GREEN;
        float i,s=25;
        for (i = -limit.x; i < limit.x+s*1.1f; i += s*2){
            BodyMaker.create().immovable().box(s,s).color(g).addTo(world, i,-limit.y-s);
            BodyMaker.create().immovable().box(s,s).color(g).addTo(world, i,limit.y+s);
            float t = i*i>8e3f?MathUtils.randomFloat(-.2f,.2f):0;
            BodyMaker.create().immovable().box(s*1.06f,s*.7f).color(gr).addTo(world, i,-s*.7f,t);
        }
        for (i = -limit.y; i < limit.y+s*1.1f; i += s*2){
            BodyMaker.create().immovable().box(s,s).color(g).addTo(world, -limit.x-s,i);
            BodyMaker.create().immovable().box(s,s).color(g).addTo(world, limit.x+s,i);
        }
    }

    public void step(float dt)
    {
        if (isRunning || singleStep)
        {
            singleStep = false;
            world.step(dt, velocityIterations, positionIterations);
        }
    }

    public void onDraw(Canvas canvas, float scale)
    {
        for (Body body = world.getBodyList(); body != null; body = body.getNext())
        {
            BodyPainter painter = (BodyPainter) body.getUserData();
            painter.onDraw(canvas, body, scale);
        }
    }

    public Body findBody(Vec2 pos, float scale)
    {
        // TODO - optimize with spatial hash?
        for (Body b = world.getBodyList(); b != null; b = b.getNext())
        {
            for (JointEdge j = b.getJointList(); j != null; j = j.next)
            {
                if (j.joint.getBodyA() != b) continue;
                Vec2 p = new Vec2();
                j.joint.getAnchorA(p);
                p = Pinch.rotate(p, b.getAngle()).addLocal(b.getPosition());
                if (MathUtils.distance(p, pos) < 20 / scale)
                {
                    ((BodyPainter) b.getUserData()).selectedJoint = j.joint;
                    return b;
                }
            }
        }
        for (Body b = world.getBodyList(); b != null; b = b.getNext())
        {
            for (Fixture fix = b.getFixtureList(); fix != null; fix = fix.getNext())
                if (fix.testPoint(pos))
                {
                    ((BodyPainter) b.getUserData()).selectedJoint = null;
                    return b;
                }
        }
        return null;
    }
}
