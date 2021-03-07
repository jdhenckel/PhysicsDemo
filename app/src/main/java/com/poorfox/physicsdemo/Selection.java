package com.poorfox.physicsdemo;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.Joint;

import java.util.ArrayList;

/*
modes:
  - single tap, select single
  - lasso a lot
  - drag to touch many
  - drag to keep just one
 */
public class Selection
{
    boolean single;
    ArrayList<Body> bodyList;
    ArrayList<Joint> jointList;

    Selection() {
        bodyList = new ArrayList<>();
        jointList = new ArrayList<>();
    }

    void onTouch(Vec2 pos){

    }
    void onTouchMove(Vec2 pos){

    }
    void clear() {
        bodyList.clear();
        jointList.clear();
    }
}
