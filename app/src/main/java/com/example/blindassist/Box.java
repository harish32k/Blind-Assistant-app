package com.example.blindassist;

public class Box {
    public Double xmin;
    public Double ymin;
    public Double xmax;
    public Double ymax;
    public String label;
    public Box(String name, Double xmin, Double ymin, Double xmax, Double ymax) {
        this.label = label;
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }
}
