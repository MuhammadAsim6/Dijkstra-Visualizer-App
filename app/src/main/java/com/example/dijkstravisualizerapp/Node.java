package com.example.dijkstravisualizerapp;


public class Node {
    float x, y;
    String label;
    double distance = Double.MAX_VALUE;
    boolean visited = false;
    Node previous;

    Node(float x, float y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }
} 