package com.poorfox.physicsdemo;

import android.graphics.Canvas;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

public class MainWorld {

    World world;

    public MainWorld(float w, float h) {
        Vec2 gravity = new Vec2(0.0f, 10.0f);

        world = new World(gravity);

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
    }

    public void step(float dt)
    {
        world.step(dt, 6, 3);
    }

    public void onDraw(Canvas canvas)
    {
        for (Body body = world.getBodyList(); body != null; body = body.getNext())
        {
            BodyPainter painter = (BodyPainter) body.getUserData();
            painter.onDraw(canvas, body);
        }
    }

}
