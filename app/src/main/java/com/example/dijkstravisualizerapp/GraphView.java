package com.example.dijkstravisualizerapp;
// GraphView.java - Custom View for drawing and interacting with the graph.

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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
    // Nodes selected during the process of adding an edge
    private Node selectedNodeA = null, selectedNodeB = null;
    // TextView to display information to the user
    private TextView infoText;
    // Current mode of interaction (Add Node, Add Edge, Set Start, Set Target)
    private Mode currentMode = Mode.ADD_NODE;

    // Enum defining the different interaction modes
    public enum Mode {
        ADD_NODE, ADD_EDGE, SET_START, SET_TARGET
    }

    // Constructor
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Set background color to white
        setBackgroundColor(Color.WHITE);
    }

    // Method to set the TextView for displaying information
    public void setInfoText(TextView infoText) {
        this.infoText = infoText;
    }

    // Method to change the current interaction mode
    public void setMode(Mode mode) {
        currentMode = mode;
        // Reset selected nodes when mode changes
        selectedNodeA = null;
        selectedNodeB = null;
        // Update the info text based on the new mode
        if (infoText != null) {
            if (currentMode == Mode.ADD_EDGE) {
                infoText.setText("Mode: ADD_EDGE. Select the first node.");
            } else {
                infoText.setText("Mode: " + mode.name());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        // Enable anti-aliasing for smoother drawing
        paint.setAntiAlias(true);

        // Draw edges
        for (Edge e : edges) {
            // Set color and stroke for edges
            paint.setColor(Color.GRAY);
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

        // Draw Dijkstra path in red
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);
        for (Edge e : dijkstraPath) {
            canvas.drawLine(e.a.x, e.a.y, e.b.x, e.b.y, paint);
        }

        // Draw nodes
        for (Node n : nodes){
            // Set node color based on its state (start, target, selected, or normal)
            if (n == startNode) paint.setColor(Color.GREEN);
            else if (n == targetNode) paint.setColor(Color.BLUE);
            else if (n == selectedNodeA || n == selectedNodeB) paint.setColor(Color.YELLOW);
            else paint.setColor(Color.CYAN);
            // Draw the node circle
            canvas.drawCircle(n.x, n.y, 30,paint);
            // Set color for node label text
            paint.setColor(Color.BLACK);
            // Draw the node label
            canvas.drawText(n.label, n.x - 30, n.y - 40, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only process touch down events
        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
        // Get touch coordinates
        float x = event.getX();
        float y = event.getY();
        // Get the node that was clicked, if any
        Node clicked = getNodeAt(x, y);

        // Handle touch based on the current mode
        switch (currentMode) {
            case ADD_NODE:
                // If no node was clicked, create a new node at the touch location
                if (clicked == null) {
                    Node newNode = new Node(x, y, "N" + nodes.size());
                    nodes.add(newNode);
                }
                break;

            case ADD_EDGE:
                // If a node was clicked
                if (clicked != null) {
                    // If the first node for the edge hasn't been selected yet, select it
                    if (selectedNodeA == null) {
                        selectedNodeA = clicked;
                        if (infoText != null) infoText.setText("Mode: ADD_EDGE. Select the second node.");
                    // If the second node hasn't been selected and is different from the first, select it
                    } else if (selectedNodeB == null && clicked != selectedNodeA) {
                        selectedNodeB = clicked;
                        // Ask for the edge weight and handle adding the edge
                        // askForEdgeWeight handles resetting infoText and selected nodes
                        askForEdgeWeight(selectedNodeA, selectedNodeB);
                    }
                }
                break;

            case SET_START:
                // If a node was clicked, set it as the start node
                if (clicked != null) {
                    startNode = clicked;
                    if (infoText != null) infoText.setText("Start: " + clicked.label);
                }
                break;

            case SET_TARGET:
                // If a node was clicked, set it as the target node
                if (clicked != null) {
                    targetNode = clicked;
                    if (infoText != null) infoText.setText("Target: " + clicked.label);
                }
                break;
        }

        // Redraw the view after a touch event
        invalidate();
        return true;
    }

    // Shows a dialog to ask the user for the weight of a new edge
    private void askForEdgeWeight(Node a, Node b) {
        // Check for self-loops
        if (a == b) {
            if (infoText != null) infoText.setText("Cannot add a self-loop.");
            selectedNodeA = selectedNodeB = null;
            invalidate();
            return;
        }

        // Check for duplicate edges (allowing edges in both directions)
        for (Edge e : edges) {
            if ((e.a == a && e.b == b) || (e.a == b && e.b == a)) {
                if (infoText != null) infoText.setText("Edge already exists between these nodes.");
                selectedNodeA = selectedNodeB = null;
                invalidate();
                return;
            }
        }

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Edge Weight");

        // Set up the input field for the weight
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the OK button to add the edge with the entered weight
        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                // Parse the entered weight
                double weight = Double.parseDouble(input.getText().toString());
                // Check if the weight is non-negative
                if (weight < 0) {
                    if (infoText != null) infoText.setText("Edge weight must be positive.");
                    selectedNodeA = selectedNodeB = null;
                    invalidate();
                    return;
                }
                // Add the new edge to the list
                edges.add(new Edge(a, b, weight));
                // Reset selected nodes and info text
                selectedNodeA = selectedNodeB = null;
                if (infoText != null) setMode(Mode.ADD_EDGE); // Reset info text after adding edge
                // Redraw the view
                invalidate();
            } catch (NumberFormatException e) {
                // Handle invalid input (not a number)
                if (infoText != null) infoText.setText("Invalid weight. Please enter a number.");
                selectedNodeA = selectedNodeB = null;
                invalidate();
            }
        });

        // Set up the Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Reset selected nodes and info text on cancel
            selectedNodeA = selectedNodeB = null;
            if (infoText != null) setMode(Mode.ADD_EDGE); // Reset info text after canceling
            dialog.cancel();
        });

        // Show the dialog
        builder.show();
    }

    // Finds and returns a node at the given coordinates, if within a certain radius
    private Node getNodeAt(float x, float y) {
        // Iterate through all nodes
        for (Node n : nodes) {
            // Check if the touch coordinates are within the node's radius (squared distance for efficiency)
            if (Math.pow(n.x - x, 2) + Math.pow(n.y - y, 2) <= 900) { // Radius is 30, 30^2 = 900
                return n; // Return the clicked node
            }
        }
        return null; // Return null if no node was clicked
    }

    // Runs Dijkstra's algorithm to find the shortest path from startNode to targetNode
    public void runDijkstra() {
        // Ensure both start and target nodes are set
        if (startNode == null || targetNode == null) {
            if (infoText != null) infoText.setText("Set both start and target nodes.");
            return;
        }

        // Clear any previously found path
        dijkstraPath.clear();
        // Initialize nodes for Dijkstra's algorithm
        for (Node n : nodes) {
            n.distance = Double.MAX_VALUE; // Set initial distance to infinity
            n.visited = false; // Mark all nodes as not visited
            n.previous = null; // Clear previous node in the path
        }

        // Set the distance of the start node to 0
        startNode.distance = 0;
        // Create a priority queue to store nodes to visit, prioritized by distance
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        // Add the start node to the priority queue
        pq.add(startNode);

        // Process nodes in the priority queue until it's empty
        while (!pq.isEmpty()) {
            // Get the node with the smallest distance from the priority queue
            Node current = pq.poll();
            // If the node has already been visited, skip it
            if (current.visited) continue;
            // Mark the current node as visited
            current.visited = true;

            // Iterate through all edges to find neighbors of the current node
            for (Edge e : edges) {
                Node neighbor = null;
                // Check if the current node is one of the endpoints of the edge
                if (e.a == current) neighbor = e.b;
                else if (e.b == current) neighbor = e.a;

                // If a neighbor is found and it hasn't been visited yet
                if (neighbor != null && !neighbor.visited) {
                    // Calculate the new distance to the neighbor through the current node
                    double newDist = current.distance + e.weight;
                    // If the new distance is shorter than the current recorded distance to the neighbor
                    if (newDist < neighbor.distance) {
                        // Update the neighbor's distance
                        neighbor.distance = newDist;
                        // Set the current node as the previous node in the shortest path to the neighbor
                        neighbor.previous = current;
                        // Add the neighbor to the priority queue (or update its priority if already there)
                        pq.add(neighbor);
                    }
                }
            }
        }

        // Trace back the shortest path from the target node to the start node
        Node current = targetNode;
        while (current != null && current.previous != null) {
            // Add the edge between the current node and its previous node to the path
            dijkstraPath.add(new Edge(current.previous, current,
                    getEdgeWeight(current.previous, current)));
            // Move to the previous node
            current = current.previous;
        }

        // Reverse the path to display from start to target
        Collections.reverse(dijkstraPath);

        // Redraw the view to show the path
        invalidate();

        // Display the result in the info text
        if (targetNode.distance == Double.MAX_VALUE) {
            if (infoText != null) infoText.setText("No path found.");
        } else {
            if (infoText != null) infoText.setText("Shortest distance: " + String.format("%.2f", targetNode.distance));
        }
    }

    // Helper method to get the weight of an edge between two nodes
    private double getEdgeWeight(Node a, Node b) {
        for (Edge e : edges) {
            // Check if the edge connects the two nodes in either direction
            if ((e.a == a && e.b == b) || (e.a == b && e.b == a)) {
                return e.weight;
            }
        }
        return Double.MAX_VALUE; // Should not happen if path is valid
    }

    // Resets the graph by clearing all nodes, edges, and the Dijkstra path
    public void resetGraph() {
        nodes.clear();
        edges.clear();
        dijkstraPath.clear();
        startNode = targetNode = null;
        selectedNodeA = selectedNodeB = null;
        // Redraw the view to show the empty graph
        invalidate();
        // Update the info text
        if (infoText != null) infoText.setText("Graph reset.");
    }
} 