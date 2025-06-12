package com.example.dijkstravisualizerapp.model;

/**
 * Represents an edge (connection) between two nodes in the graph.
 * Each edge has a weight and connects two nodes.
 */
public class Edge {
    private final Node source;
    private final Node destination;
    private final double weight;

    /**
     * Creates a new edge connecting two nodes with a specified weight.
     * @param source The source node
     * @param destination The destination node
     * @param weight The weight/cost of traversing this edge
     */
    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    // Getters
    public Node getSource() { return source; }
    public Node getDestination() { return destination; }
    public double getWeight() { return weight; }

    /**
     * Checks if this edge connects the given node.
     * @param node The node to check
     * @return true if the edge connects to the given node
     */
    public boolean connects(Node node) {
        return source == node || destination == node;
    }

    /**
     * Gets the other node connected by this edge.
     * @param node One of the nodes connected by this edge
     * @return The other node connected by this edge, or null if the given node is not connected
     */
    public Node getOtherNode(Node node) {
        if (node == source) return destination;
        if (node == destination) return source;
        return null;
    }

    @Override
    public String toString() {
        return "Edge{" +
                source.getLabel() + "->" + destination.getLabel() +
                ", weight=" + weight +
                '}';
    }
} 