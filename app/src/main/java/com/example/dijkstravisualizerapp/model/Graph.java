package com.example.dijkstravisualizerapp.model;

import java.util.*;

/**
 * Represents a graph with nodes and edges.
 * Provides methods for graph operations and Dijkstra's algorithm.
 */
public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private Node startNode;
    private Node targetNode;

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    // Getters
    public List<Node> getNodes() { return Collections.unmodifiableList(nodes); }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }
    public Node getStartNode() { return startNode; }
    public Node getTargetNode() { return targetNode; }

    /**
     * Adds a node to the graph.
     * @param node The node to add
     */
    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    /**
     * Adds an edge to the graph.
     * @param edge The edge to add
     */
    public void addEdge(Edge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
            // Ensure both nodes are in the graph
            addNode(edge.getSource());
            addNode(edge.getDestination());
        }
    }

    /**
     * Sets the start and target nodes for Dijkstra's algorithm.
     * @param start The start node
     * @param target The target node
     */
    public void setStartAndTarget(Node start, Node target) {
        if (nodes.contains(start) && nodes.contains(target)) {
            this.startNode = start;
            this.targetNode = target;
        } else {
            throw new IllegalArgumentException("Start and target nodes must be in the graph");
        }
    }

    /**
     * Gets all edges connected to a node.
     * @param node The node to get edges for
     * @return List of edges connected to the node
     */
    public List<Edge> getEdgesForNode(Node node) {
        List<Edge> nodeEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.connects(node)) {
                nodeEdges.add(edge);
            }
        }
        return nodeEdges;
    }

    /**
     * Gets the edge between two nodes if it exists.
     * @param node1 First node
     * @param node2 Second node
     * @return The edge between the nodes, or null if no edge exists
     */
    public Edge getEdge(Node node1, Node node2) {
        for (Edge edge : edges) {
            if ((edge.getSource() == node1 && edge.getDestination() == node2) ||
                (edge.getSource() == node2 && edge.getDestination() == node1)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Resets all nodes to their initial state for Dijkstra's algorithm.
     */
    public void resetNodes() {
        for (Node node : nodes) {
            node.reset();
        }
    }

    /**
     * Creates a graph with the hardcoded structure for the Dijkstra visualizer.
     * @return A new graph with the predefined structure
     */
    public static Graph createHardcodedGraph() {
        Graph graph = new Graph();
        
        // Create nodes
        for (int i = 0; i < 10; i++) {
            graph.addNode(new Node(0, 0, String.valueOf(i)));
        }

        // Add edges with weights
        int[][] edgeData = {
            {0,1,12}, {0,2,7}, {0,3,3}, {0,4,15}, {0,5,9}, {0,6,17}, {0,7,6}, {0,8,11}, {0,9,4},
            {1,2,14}, {1,3,5}, {1,4,8}, {1,5,19}, {1,6,2}, {1,7,13}, {1,8,10}, {1,9,16},
            {2,3,6}, {2,4,12}, {2,5,1}, {2,6,18}, {2,7,7}, {2,8,20}, {2,9,3},
            {3,4,11}, {3,5,14}, {3,6,8}, {3,7,2}, {3,8,15}, {3,9,5},
            {4,5,17}, {4,6,6}, {4,7,13}, {4,8,9}, {4,9,10},
            {5,6,4}, {5,7,12}, {5,8,7}, {5,9,18},
            {6,7,1}, {6,8,16}, {6,9,3},
            {7,8,19}, {7,9,11},
            {8,9,2}
        };

        for (int[] edge : edgeData) {
            Node source = graph.nodes.get(edge[0]);
            Node dest = graph.nodes.get(edge[1]);
            graph.addEdge(new Edge(source, dest, edge[2]));
        }

        // Set default start and target nodes
        graph.setStartAndTarget(graph.nodes.get(0), graph.nodes.get(9));

        return graph;
    }
} 