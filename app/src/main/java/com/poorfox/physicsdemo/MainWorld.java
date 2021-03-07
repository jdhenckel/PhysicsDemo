package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.JointEdge;

public class MainWorld {

    World world;
    int velocityIterations, positionIterations;
    boolean isRunning;
    boolean singleStep;

    public MainWorld() {
        Vec2 gravity = new Vec2(0.0f, -10.0f);
        world = new World(gravity);
    }

    public void initialize(float w, float h) {
        BodyMaker.create().ball(w/14).layer(1).addTo(world, w/2, h/2);
        BodyMaker.create().box(w/14,w/20).layer(1).addTo(world, w/2.5f, h/3);
        BodyMaker.create().box(w/14,w/20).layer(2).addTo(world, w/2.5f, h/2.5f);
        BodyMaker.create().box(w/14,w/20).layer(2).addTo(world, w/2.5f, h/1.7f);

        // add four immovable walls (top, bottom, left, right)
        float t = w/20;
        BodyMaker.create().immovable().box(w/2, t).addTo(world, w/2, 0);
        BodyMaker.create().immovable().box(w/2, t).addTo(world, w/2, h);
        BodyMaker.create().immovable().box(t, h/2).addTo(world, 0, h/2);
        BodyMaker.create().immovable().box(t, h/2).addTo(world, w, h/2);
        isRunning = true;
        velocityIterations = 6;
        positionIterations = 3;
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
                if (fix.testPoint(pos)) {
                    ((BodyPainter) b.getUserData()).selectedJoint = null;
                    return b;
                }
        }
        return null;
    }
}
