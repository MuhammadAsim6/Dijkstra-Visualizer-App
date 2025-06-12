package com.example.dijkstravisualizerapp.algorithm;

import com.example.dijkstravisualizerapp.model.Edge;
import com.example.dijkstravisualizerapp.model.Graph;
import com.example.dijkstravisualizerapp.model.Node;

import java.util.*;

/**
 * Implements Dijkstra's algorithm - a clever way to find the shortest path in a graph.
 * Think of it like finding the best route on a map, where we always take the path that looks most promising.
 */
public class DijkstraAlgorithm {
    // The graph we're working with
    private final Graph graph;
    // List to store each step of the algorithm for visualization
    private final List<DijkstraStep> steps;
    // Tracks which step we're currently showing in the animation
    private int currentStepIndex;

    /**
     * Creates a new DijkstraAlgorithm instance for the given graph.
     * We'll use this to find the shortest path in the graph.
     */
    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
        this.steps = new ArrayList<>();
        this.currentStepIndex = -1;  // Start before the first step
    }

    /**
     * Runs Dijkstra's algorithm and captures each step for visualization.
     * The algorithm works like this:
     * 1. Start at the source node (distance = 0)
     * 2. Keep track of the best distance we've found to each node
     * 3. Always explore the node that's closest to our start point
     * 4. When we find a better path to a node, update our records
     * 5. Keep going until we've checked all possible paths
     */
    public List<DijkstraStep> run() {
        // Clear any previous run and reset the graph
        steps.clear();
        currentStepIndex = -1;
        graph.resetNodes();  // Reset all nodes to their initial state

        // Initialize the start node with distance 0
        Node startNode = graph.getStartNode();
        startNode.setDistance(0);
        
        // Create a snapshot of initial distances for the first animation step
        Map<Node, Double> initialDistances = new HashMap<>();
        for (Node node : graph.getNodes()) {
            initialDistances.put(node, node.getDistance());  // All nodes start with MAX_VALUE except start
        }
        
        // Record the first step - starting our journey
        steps.add(new DijkstraStep("Starting our journey from " + startNode.getLabel(), 
                                 startNode, null, null, initialDistances));

        // Create a priority queue that always gives us the closest unvisited node
        // The comparator ensures we always get the node with smallest distance
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));
        pq.add(startNode);  // Start with our source node

        // Keep track of current distances for animation updates
        Map<Node, Double> currentDistances = new HashMap<>(initialDistances);

        // Main algorithm loop - keep going until we've checked all possible paths
        while (!pq.isEmpty()) {
            // Get the closest unvisited node from our priority queue
            Node current = pq.poll();
            if (current.isVisited()) continue;  // Skip if we've already processed this node

            // Record that we're visiting this node
            steps.add(new DijkstraStep("Exploring node " + current.getLabel(), 
                                     current, null, null, new HashMap<>(currentDistances)));
            current.setVisited(true);  // Mark this node as visited

            // Check all paths from this node to its neighbors
            for (Edge edge : graph.getEdgesForNode(current)) {
                // Get the other end of this edge
                Node neighbor = edge.getOtherNode(current);
                
                // Only process unvisited neighbors
                if (neighbor != null && !neighbor.isVisited()) {
                    // Calculate the distance to neighbor through current node
                    double newDist = current.getDistance() + edge.getWeight();
                    
                    // If we found a better path to the neighbor
                    if (newDist < neighbor.getDistance()) {
                        // Update the neighbor's distance and remember how we got here
                        neighbor.setDistance(newDist);
                        neighbor.setPrevious(current);
                        
                        // Update the priority queue to reflect the new distance
                        pq.remove(neighbor);  // Remove old entry
                        pq.add(neighbor);     // Add with new distance
                        currentDistances.put(neighbor, neighbor.getDistance());

                        // Record this improvement for animation
                        steps.add(new DijkstraStep(
                            "Found a better path to " + neighbor.getLabel() + 
                            " through " + current.getLabel() + 
                            " (new distance: " + String.format("%.2f", neighbor.getDistance()) + ")",
                            null, edge, neighbor, new HashMap<>(currentDistances)
                        ));
                    } else {
                        // Record that we checked but found no improvement
                        steps.add(new DijkstraStep(
                            "Checked path to " + neighbor.getLabel() + 
                            " through " + current.getLabel() + 
                            " - no improvement found",
                            null, edge, null, new HashMap<>(currentDistances)
                        ));
                    }
                }
            }
        }

        // Return an unmodifiable list of steps for the animation
        return Collections.unmodifiableList(steps);
    }

    // Animation control methods
    public int getCurrentStepIndex() { return currentStepIndex; }
    public int getTotalSteps() { return steps.size(); }
    
    /**
     * Get the current step in our animation
     * Returns null if we haven't started or have finished
     */
    public DijkstraStep getCurrentStep() {
        return currentStepIndex >= 0 && currentStepIndex < steps.size() ? 
               steps.get(currentStepIndex) : null;
    }

    /**
     * Move to the next step in our algorithm visualization.
     * This is like pressing 'next' in a tutorial - we'll see what happens next in our path-finding journey.
     */
    public DijkstraStep nextStep() {
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            return steps.get(currentStepIndex);
        }
        return null;  // We've reached the end
    }

    /**
     * Go back to the previous step in our visualization.
     * Useful if you want to review what just happened.
     */
    public DijkstraStep previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            return steps.get(currentStepIndex);
        }
        return null;  // We're at the beginning
    }

    /**
     * Reconstruct the shortest path from start to target.
     * We do this by following the 'previous' pointers we set during the algorithm,
     * like retracing our steps to find how we got to our destination.
     */
    public List<Edge> getFinalPath() {
        List<Edge> path = new ArrayList<>();
        Node current = graph.getTargetNode();
        
        // Follow the path backwards from target to start
        while (current != null && current.getPrevious() != null) {
            // Get the edge that connects current node to its previous node
            Edge edge = graph.getEdge(current.getPrevious(), current);
            if (edge != null) {
                path.add(edge);
            }
            // Move to the previous node in the path
            current = current.getPrevious();
        }
        
        // Reverse the path so it goes from start to target
        Collections.reverse(path);
        return path;
    }

    /**
     * Represents a single step in our algorithm visualization.
     * Each step captures what's happening at that moment:
     * - Which node we're looking at
     * - Which edge we're considering
     * - What distances we know
     * - A human-readable description of what's happening
     */
    public static class DijkstraStep {
        private final String description;      // What's happening in this step
        private final Node highlightedNode;    // The node we're focusing on
        private final Edge highlightedEdge;    // The edge we're considering
        private final Node updatedNode;        // The node whose distance we updated
        private final Map<Node, Double> nodeDistances;  // Current best distances we know

        public DijkstraStep(String description, Node highlightedNode, Edge highlightedEdge, 
                          Node updatedNode, Map<Node, Double> nodeDistances) {
            this.description = description;
            this.highlightedNode = highlightedNode;
            this.highlightedEdge = highlightedEdge;
            this.updatedNode = updatedNode;
            this.nodeDistances = nodeDistances;
        }

        // Getters for animation
        public String getDescription() { return description; }
        public Node getHighlightedNode() { return highlightedNode; }
        public Edge getHighlightedEdge() { return highlightedEdge; }
        public Node getUpdatedNode() { return updatedNode; }
        public Map<Node, Double> getNodeDistances() { return Collections.unmodifiableMap(nodeDistances); }
    }
} 