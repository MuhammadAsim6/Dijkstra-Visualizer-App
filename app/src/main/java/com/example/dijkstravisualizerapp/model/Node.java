package com.example.dijkstravisualizerapp.model;

/**
 * Represents a node (vertex) in the graph.
 * Each node has a position (x,y), a label, and properties used in Dijkstra's algorithm.
 */
public class Node {
    private float x;
    private float y;
    private final String label;
    private double distance;
    private boolean visited;
    private Node previous;

    /**
     * Creates a new node with the specified position and label.
     * @param x The x-coordinate of the node
     * @param y The y-coordinate of the node
     * @param label The label/identifier of the node
     */
    public Node(float x, float y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.distance = Double.MAX_VALUE;
        this.visited = false;
        this.previous = null;
    }

    // Getters and setters
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public String getLabel() { return label; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    public Node getPrevious() { return previous; }
    public void setPrevious(Node previous) { this.previous = previous; }

    /**
     * Resets the node's Dijkstra algorithm properties to their initial state.
     */
    public void reset() {
        this.distance = Double.MAX_VALUE;
        this.visited = false;
        this.previous = null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "label='" + label + '\'' +
                ", distance=" + (distance == Double.MAX_VALUE ? "∞" : String.format("%.2f", distance)) +
                ", visited=" + visited +
                '}';
    }
} 