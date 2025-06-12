package com.example.dijkstravisualizerapp;
// GraphView.java - Custom View for drawing and interacting with the graph.

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.*;

public class GraphView extends View {

    // Paint object for drawing on the canvas
    private final Paint paint = new Paint();
    // Lists to hold nodes, edges, and the path found by Dijkstra's algorithm
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final List<Edge> dijkstraPath = new ArrayList<>();

    // Nodes representing the start and target for Dijkstra's algorithm
    private Node startNode = null, targetNode = null;
    // TextView to display information to the user
    private TextView infoText;

    // Fields for animation
    private List<DijkstraAnimationStep> animationSteps = new ArrayList<>();
    private int currentStepIndex = -1;
    private boolean isAnimating = false;
    private Handler animationHandler = new Handler();
    private Runnable animationRunnable;
    private List<Edge> currentAnimationPath = new ArrayList<>(); // To store path edges during animation

    // Constructor
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Set background color to white
        setBackgroundColor(Color.WHITE);
        createHardcodedGraphStructure(); // Create graph structure without positions initially

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                stepAnimation();
                if (isAnimating) {
                    animationHandler.postDelayed(this, 1000); // Delay for animation step (1 second)
                }
            }
        };
    }

    // Method to set the TextView for displaying information
    public void setInfoText(TextView infoText) {
        this.infoText = infoText;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        // Enable anti-aliasing for smoother drawing
        paint.setAntiAlias(true);

        // Draw edges
        for (Edge e : edges) {
            // Set color based on whether it's part of the current step's highlight or final path
            boolean isHighlightedEdge = isAnimating && currentStepIndex != -1 && animationSteps.get(currentStepIndex).highlightedEdge == e;
            boolean isAnimationPath = isAnimating && currentAnimationPath.contains(e); // Check if edge is in current animation path
            boolean isFinalPath = !isAnimating && dijkstraPath.contains(e);

            if (isHighlightedEdge || isAnimationPath || isFinalPath) {
                 paint.setColor(Color.RED); // Highlight/Path color for edges
            } else {
                paint.setColor(Color.GRAY);
            }
            paint.setStrokeWidth(3);
            // Draw the line for the edge
            canvas.drawLine(e.a.x, e.a.y, e.b.x, e.b.y, paint);
            // Set color and text size for edge weight text
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            // Calculate midpoint of the edge for placing text
            float mx =(e.a.x + e.b.x) /2;
            float my= (e.a.y + e.b.y)/ 2;
            // Draw the edge weight
            canvas.drawText(String.valueOf(e.weight),mx, my, paint);
        }

        // Draw nodes
        for (Node n : nodes){
            // Set color based on its state and animation state
            boolean isHighlightedNode = isAnimating && currentStepIndex != -1 && animationSteps.get(currentStepIndex).highlightedNode == n;

            if (n == startNode) paint.setColor(Color.GREEN);
            else if (n == targetNode) paint.setColor(Color.BLUE);
            else if (isHighlightedNode) {
                paint.setColor(Color.YELLOW); // Highlight color for nodes
            } else {
                paint.setColor(Color.CYAN);
            }
            // Draw the node circle
            canvas.drawCircle(n.x, n.y, 30,paint);
            // Set color for node label text
            paint.setColor(Color.BLACK);
            // Draw the node label
            canvas.drawText(n.label, n.x - 30, n.y - 40, paint);

            // Draw distance if known in animation or after completion
            if (n.distance != Double.MAX_VALUE && (isAnimating || (!isAnimating && currentStepIndex == animationSteps.size() - 1))) {
                 paint.setColor(Color.BLACK);
                 paint.setTextSize(25);
                 canvas.drawText(String.format("%.2f", n.distance), n.x - 20, n.y + 50, paint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Update node positions when the view size changes
        updateNodePositions(w, h);
    }

    // Method to update node positions (called from onSizeChanged)
    private void updateNodePositions(int width, int height) {
         if (nodes.isEmpty()) return; // No nodes to position

         int centerX = width / 2;
         int centerY = height / 2;
         int radius = Math.min(centerX, centerY) - 100; // Adjust radius as needed
         int numberOfNodes = nodes.size();

         for (int i = 0; i < numberOfNodes; i++) {
             double angle = 2 * Math.PI * i / numberOfNodes;
             float x = (float) (centerX + radius * Math.cos(angle));
             float y = (float) (centerY + radius * Math.sin(angle));
             nodes.get(i).x = x;
             nodes.get(i).y = y;
         }
         invalidate(); // Redraw the view with new positions
    }

    // Runs Dijkstra's algorithm to find the shortest path from startNode to targetNode
    public void runDijkstra() {
        // Ensure both start and target nodes are set
        if (startNode == null || targetNode == null) {
            if (infoText != null) infoText.setText("Set both start and target nodes.");
            return;
        }

        resetForDijkstra(); // Reset nodes and clear previous path/animation
        animationSteps.clear();
        currentStepIndex = -1;
        isAnimating = false; // Ensure not animating during setup
        currentAnimationPath.clear(); // Clear animation path

        // Dijkstra's algorithm implementation for animation
        startNode.distance = 0;
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        pq.add(startNode);

        Map<Node, Double> initialDistances = new HashMap<>();
        for(Node n : nodes) initialDistances.put(n, n.distance);
        animationSteps.add(new DijkstraAnimationStep("Starting Dijkstra from " + startNode.label, startNode, null, null, new HashMap<>(initialDistances)));

        Map<Node, Double> currentDistances = new HashMap<>(initialDistances);

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.visited) continue;

            animationSteps.add(new DijkstraAnimationStep("Visiting node " + current.label, current, null, null, new HashMap<>(currentDistances)));

            current.visited = true;

            for (Edge e : edges) {
                Node neighbor = null;
                if (e.a == current) neighbor = e.b;
                else if (e.b == current) neighbor = e.a;

                if (neighbor != null && !neighbor.visited) {
                    double newDist = current.distance + e.weight;
                    if (newDist < neighbor.distance) {
                        neighbor.distance = newDist;
                        neighbor.previous = current;
                        // Remove and re-add to update priority - inefficient but works for visualization
                        pq.remove(neighbor);
                        pq.add(neighbor);
                        currentDistances.put(neighbor, neighbor.distance);

                        animationSteps.add(new DijkstraAnimationStep("Relaxing edge " + current.label + "-" + neighbor.label + ", updated distance to " + neighbor.label + " to " + String.format("%.2f", neighbor.distance), null, e, neighbor, new HashMap<>(currentDistances)));
                    } else {
                         animationSteps.add(new DijkstraAnimationStep("Relaxing edge " + current.label + "-" + neighbor.label + ", no improvement.", null, e, null, new HashMap<>(currentDistances)));
                    }
                }
            }
        }

        // Trace back the shortest path after the algorithm finishes and add to steps
        List<Edge> finalPathEdges = new ArrayList<>();
        Node pathNode = targetNode;
        while (pathNode != null && pathNode.previous != null) {
            Edge pathEdge = getEdge(pathNode.previous, pathNode);
            if (pathEdge != null) {
                 finalPathEdges.add(pathEdge);
            }
            pathNode = pathNode.previous;
        }
        Collections.reverse(finalPathEdges);

        animationSteps.add(new DijkstraAnimationStep("Algorithm finished. Tracing shortest path.", null, null, null, new HashMap<>(currentDistances)));
        // Path highlighting steps are now handled in applyAnimationStep

        if (targetNode.distance == Double.MAX_VALUE) {
            animationSteps.add(new DijkstraAnimationStep("No path found.", null, null, null, new HashMap<>(currentDistances)));
        } else {
            animationSteps.add(new DijkstraAnimationStep("Shortest distance: " + String.format("%.2f", targetNode.distance), null, null, null, new HashMap<>(currentDistances)));
        }

        // Animation is ready, the user can now step through or play
         if (infoText != null) infoText.setText("Dijkstra algorithm ready to run animation.");
         invalidate();
    }

    // Helper method to reset nodes for Dijkstra
    private void resetForDijkstra() {
         dijkstraPath.clear();
         animationSteps.clear();
         currentStepIndex = -1;
         isAnimating = false;
         animationHandler.removeCallbacks(animationRunnable);
         currentAnimationPath.clear(); // Clear animation path
         for (Node n : nodes) {
            n.distance = Double.MAX_VALUE;
            n.visited = false;
            n.previous = null;
        }
        invalidate();
    }

    // Step through the animation
    public void stepAnimation() {
        if (currentStepIndex < animationSteps.size() - 1) {
            currentStepIndex++;
            applyAnimationStep(animationSteps.get(currentStepIndex));
            invalidate();
        } else {
            isAnimating = false;
            animationHandler.removeCallbacks(animationRunnable);
             if (infoText != null && animationSteps.size() > 0) {
                 infoText.setText(animationSteps.get(animationSteps.size() - 1).description);
            }
             // After animation, set the final path for drawing
             setFinalDijkstraPath();
             currentAnimationPath.clear(); // Clear animation path at the end
             invalidate();
        }
    }

    // Apply a specific animation step's state to the graph view
    private void applyAnimationStep(DijkstraAnimationStep step) {
        // Apply the state from the animation step to node distances
        for(Node n : nodes) {
             n.distance = step.nodeDistances.getOrDefault(n, Double.MAX_VALUE); // Apply recorded distance or infinity
        }
         // The highlighted node and edge will be handled in onDraw based on currentStepIndex
         if (infoText != null) {
             infoText.setText("Step " + (currentStepIndex + 1) + ": " + step.description);
         }

        // Update the current animation path based on the step's updated node and its previous node
        if (step.updatedNode != null && step.updatedNode.previous != null) {
            Edge edgeToAdd = getEdge(step.updatedNode, step.updatedNode.previous);
            if (edgeToAdd != null && !currentAnimationPath.contains(edgeToAdd)) {
                 currentAnimationPath.add(edgeToAdd);
            }
        }
    }

    // Start the animation playback
    public void startAnimation() {
        if (!isAnimating && currentStepIndex < animationSteps.size() - 1) {
            isAnimating = true;
            animationHandler.postDelayed(animationRunnable, 0); // Start immediately or with a small delay
        }
    }

    // Pause the animation playback
    public void pauseAnimation() {
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        if (infoText != null && currentStepIndex != -1) {
             infoText.setText("Animation Paused at Step " + (currentStepIndex + 1));
        }
    }

    // Set the final path for drawing after animation
    private void setFinalDijkstraPath() {
         dijkstraPath.clear();
         Node current = targetNode;
         while (current != null && current.previous != null) {
             Edge pathEdge = getEdge(current.previous, current);
             if (pathEdge != null) {
                  dijkstraPath.add(pathEdge);
             }
             current = current.previous;
         }
         Collections.reverse(dijkstraPath);
    }

    // Helper method to get a specific edge between two nodes
    private Edge getEdge(Node a, Node b) {
        for (Edge e : edges) {
            if ((e.a == a && e.b == b) || (e.a == b && e.b == a)) {
                return e;
            }
        }
        return null;
    }

    // Helper method to get the weight of an edge between two nodes (used in getEdge and runDijkstra)
    private double getEdgeWeight(Node a, Node b) {
        Edge edge = getEdge(a, b);
        return edge != null ? edge.weight : Double.MAX_VALUE;
    }

    // Resets the graph by clearing all nodes, edges, and the Dijkstra path
    public void resetGraph() {
        nodes.clear();
        edges.clear();
        dijkstraPath.clear();
        startNode = targetNode = null;
        animationSteps.clear();
        currentStepIndex = -1;
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        currentAnimationPath.clear(); // Clear animation path
        // Redraw the view to show the empty graph
        invalidate();
        // Update the info text
        if (infoText != null) infoText.setText("Graph reset.");
        createHardcodedGraphStructure(); // Recreate the graph structure
    }

    // Creates the nodes and edges for the hardcoded graph
    private void createHardcodedGraphStructure() {
        nodes.clear();
        edges.clear();
        dijkstraPath.clear();
        startNode = targetNode = null;
        animationSteps.clear();
        currentStepIndex = -1;
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        currentAnimationPath.clear();

        // Create 10 nodes
        for (int i = 0; i < 10; i++) {
            nodes.add(new Node(0, 0, String.valueOf(i)));
        }

        // Add edges with weights as provided
        addEdge(0, 1, 12);
        addEdge(0, 2, 7);
        addEdge(0, 3, 3);
        addEdge(0, 4, 15);
        addEdge(0, 5, 9);
        addEdge(0, 6, 17);
        addEdge(0, 7, 6);
        addEdge(0, 8, 11);
        addEdge(0, 9, 4);
        addEdge(1, 2, 14);
        addEdge(1, 3, 5);
        addEdge(1, 4, 8);
        addEdge(1, 5, 19);
        addEdge(1, 6, 2);
        addEdge(1, 7, 13);
        addEdge(1, 8, 10);
        addEdge(1, 9, 16);
        addEdge(2, 3, 6);
        addEdge(2, 4, 12);
        addEdge(2, 5, 1);
        addEdge(2, 6, 18);
        addEdge(2, 7, 7);
        addEdge(2, 8, 20);
        addEdge(2, 9, 3);
        addEdge(3, 4, 11);
        addEdge(3, 5, 14);
        addEdge(3, 6, 8);
        addEdge(3, 7, 2);
        addEdge(3, 8, 15);
        addEdge(3, 9, 5);
        addEdge(4, 5, 17);
        addEdge(4, 6, 6);
        addEdge(4, 7, 13);
        addEdge(4, 8, 9);
        addEdge(4, 9, 10);
        addEdge(5, 6, 4);
        addEdge(5, 7, 12);
        addEdge(5, 8, 7);
        addEdge(5, 9, 18);
        addEdge(6, 7, 1);
        addEdge(6, 8, 16);
        addEdge(6, 9, 3);
        addEdge(7, 8, 19);
        addEdge(7, 9, 11);
        addEdge(8, 9, 2);

        // Set default start and target nodes
        startNode = nodes.get(0);
        targetNode = nodes.get(9);

        // Update node positions if view dimensions are available
        if (getWidth() > 0 && getHeight() > 0) {
            updateNodePositions(getWidth(), getHeight());
        }

         if (infoText != null) infoText.setText("Hardcoded graph structure created.");
    }

    // Helper method to add an edge by node index
    private void addEdge(int uIndex, int vIndex, double weight) {
        if (uIndex >= 0 && uIndex < nodes.size() && vIndex >= 0 && vIndex < nodes.size()) {
             edges.add(new Edge(nodes.get(uIndex), nodes.get(vIndex), weight));
        }
    }

    // Helper class to represent a step in Dijkstra's algorithm for animation
    private static class DijkstraAnimationStep {
        String description;
        Node highlightedNode;
        Edge highlightedEdge;
        Node updatedNode;
        Map<Node, Double> nodeDistances; // To store distances at this step

        DijkstraAnimationStep(String description, Node highlightedNode, Edge highlightedEdge, Node updatedNode, Map<Node, Double> nodeDistances) {
            this.description = description;
            this.highlightedNode = highlightedNode;
            this.highlightedEdge = highlightedEdge;
            this.updatedNode = updatedNode;
            this.nodeDistances = nodeDistances;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up the handler to prevent memory leaks
        animationHandler.removeCallbacks(animationRunnable);
    }
}
