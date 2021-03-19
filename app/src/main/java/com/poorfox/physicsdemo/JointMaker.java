package com.poorfox.physicsdemo;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class JointMaker
{
    JointDef jointDef;
    Body bodyA, bodyB;
    Vec2 pos;
    boolean enableCollision;

    JointMaker(Body a, Body b, Vec2 pos, boolean enableCollision){
        this.pos = pos;
        this.enableCollision = enableCollision;
        bodyA = a;
        bodyB = b;
    }

    static JointMaker create(Body a, Body b, Vec2 pos, boolean enableCollision){
        return new JointMaker(a,b,pos,enableCollision);
    }

    static JointMaker create(Body a, Body b, Vec2 pos){
        return new JointMaker(a,b,pos,false);
    }

    JointMaker revolute(){
        jointDef = new RevoluteJointDef();
        jointDef.collideConnected = enableCollision;
        ((RevoluteJointDef)jointDef).initialize(bodyA, bodyB, pos);
        return this;
    }

    JointMaker prismatic(Vec2 dir){
        jointDef = new PrismaticJointDef();
        jointDef.collideConnected = enableCollision;
        ((PrismaticJointDef)jointDef).initialize(bodyA, bodyB, pos, dir);
        return this;
    }

    JointMaker distanceTo(Vec2 posB){
        return distanceTo(posB, 0 ,0);
    }

    JointMaker distanceTo(Vec2 posB, float hertz, float damping){
        assert hertz <= 30 && damping <= 1 && damping >= 0;
        jointDef = new DistanceJointDef();
        jointDef.collideConnected = enableCollision;
        ((DistanceJointDef)jointDef).initialize(bodyA, bodyB, pos, posB);
        ((DistanceJointDef)jointDef).frequencyHz = hertz;
        ((DistanceJointDef)jointDef).dampingRatio = damping;
        return this;
    }

    JointMaker limit(float low, float high) {
        assert jointDef instanceof RevoluteJointDef;
        ((RevoluteJointDef) jointDef).enableLimit = true;
        ((RevoluteJointDef) jointDef).lowerAngle = low;
        ((RevoluteJointDef) jointDef).upperAngle = high;
        return this;
    }

    JointMaker limitDeg(float low, float high) {
        return limit(rads(low),rads(high));
    }


    static float rads(float degs)
    {
        return degs * 3.1415926f / 180;
    }


    Joint addTo(World world){
        return world.createJoint(jointDef);
    }
}
