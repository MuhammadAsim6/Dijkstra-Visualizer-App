package com.example.dijkstravisualizerapp;

public class Edge {
    Node a, b;
    double weight;

    Edge(Node a, Node b, double weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }
} 