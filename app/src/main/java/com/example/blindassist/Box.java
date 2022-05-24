package com.example.blindassist;

import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.util.List;

public class Box {
    public String desc;
    public String locale;
    public Polygon polygon;
    //public List<List<Integer>> coordinates;
    public Box(Polygon polygon, String desc, String locale) {
        this.polygon = polygon;
        this.desc = desc;
        this.locale = locale;
    }
}
