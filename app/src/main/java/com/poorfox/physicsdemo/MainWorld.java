package com.poorfox.physicsdemo;

import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

public class MainWorld {

    World world;
    Body ball;

    public MainWorld(float w, float h) {
        Vec2 gravity = new Vec2(0.0f, 10.0f);
        //boolean doSleep = true;
        world = new World(gravity);
        ball = addBody(dynamic(w/2, h/2), fix(ball(w/14), .5f, .99f));

        // add four immovable walls (top, bottom, left, right)
        addBody(immovable(w/2, -5), fix(box(w/2, 5), .5f, .99f));
        addBody(immovable(w/2, h + 5), fix(box(w/2, 5), .5f, .99f));
        addBody(immovable(-5, h/2), fix(box(5, h/2), .5f, .99f));
        addBody(immovable(w + 5, h/2), fix(box(5, h/2), .5f, .99f));
    }

    public void step(float dt)
    {
        world.step(dt, 6, 3);
    }

    private Body addBody(BodyDef bd, FixtureDef fd) {
        Body b = world.createBody(bd);
        b.createFixture(fd);
        return b;
    }

    private CircleShape ball(float rad) {
        CircleShape shape = new CircleShape();
        shape.setRadius(rad);
        return shape;
    }

    // Create a box given the HALF sizes of the box
    // The shape origin is the center of the box.
    private PolygonShape box(float w, float h) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);
        return shape;
    }

    private FixtureDef fix(Shape shape, float dens, float rest) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.setShape(shape);
        fixtureDef.setDensity(dens);
        fixtureDef.setRestitution(rest);
        return fixtureDef;
    }

    private BodyDef dynamic(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.setPosition(new Vec2(x, y));
        bodyDef.setType(BodyType.DYNAMIC);
        bodyDef.setLinearDamping(0);
        bodyDef.setAngularDamping(0);
        return bodyDef;
    }

    private BodyDef immovable(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.setPosition(new Vec2(x, y));
        bodyDef.setType(BodyType.STATIC);
        return bodyDef;
    }

}
