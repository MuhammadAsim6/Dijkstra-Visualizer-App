package com.example.dijkstravisualizerapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.dijkstravisualizerapp.algorithm.DijkstraAlgorithm;
import com.example.dijkstravisualizerapp.model.Edge;
import com.example.dijkstravisualizerapp.model.Graph;
import com.example.dijkstravisualizerapp.model.Node;

import java.util.List;

/**
 * Custom view for visualizing the graph and Dijkstra's algorithm animation.
 */
public class GraphView extends View {
    private final Paint paint;
    private Graph graph;
    private DijkstraAlgorithm dijkstraAlgorithm;
    private TextView infoText;
    private boolean isAnimating;
    private Handler animationHandler;
    private Runnable animationRunnable;
    private List<Edge> currentAnimationPath;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        setBackgroundColor(Color.WHITE);

        // Initialize graph
        graph = Graph.createHardcodedGraph();
        dijkstraAlgorithm = new DijkstraAlgorithm(graph);
        currentAnimationPath = new java.util.ArrayList<>();

        // Set up animation handler
        animationHandler = new Handler();
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                stepAnimation();
                if (isAnimating) {
                    animationHandler.postDelayed(this, 1000); // 1 second delay
                }
            }
        };

        // Update node positions when view is ready
        post(this::updateNodePositions);
    }

    public void setInfoText(TextView infoText) {
        this.infoText = infoText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw edges
        for (Edge edge : graph.getEdges()) {
            Node source = edge.getSource();
            Node dest = edge.getDestination();

            // Set edge color based on state
            boolean isHighlightedEdge = isAnimating && dijkstraAlgorithm.getCurrentStep() != null && 
                                      dijkstraAlgorithm.getCurrentStep().getHighlightedEdge() == edge;
            boolean isAnimationPath = isAnimating && currentAnimationPath.contains(edge);
            boolean isFinalPath = !isAnimating && dijkstraAlgorithm.getFinalPath().contains(edge);

            paint.setColor(isHighlightedEdge || isAnimationPath || isFinalPath ? Color.RED : Color.GRAY);
            paint.setStrokeWidth(3);
            canvas.drawLine(source.getX(), source.getY(), dest.getX(), dest.getY(), paint);

            // Draw edge weight
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            float mx = (source.getX() + dest.getX()) / 2;
            float my = (source.getY() + dest.getY()) / 2;
            canvas.drawText(String.valueOf(edge.getWeight()), mx, my, paint);
        }

        // Draw nodes
        for (Node node : graph.getNodes()) {
            // Set node color based on state
            boolean isHighlightedNode = isAnimating && dijkstraAlgorithm.getCurrentStep() != null && 
                                      dijkstraAlgorithm.getCurrentStep().getHighlightedNode() == node;

            if (node == graph.getStartNode()) paint.setColor(Color.GREEN);
            else if (node == graph.getTargetNode()) paint.setColor(Color.BLUE);
            else if (isHighlightedNode) paint.setColor(Color.YELLOW);
            else paint.setColor(Color.CYAN);

            // Draw node circle
            canvas.drawCircle(node.getX(), node.getY(), 30, paint);

            // Draw node label
            paint.setColor(Color.BLACK);
            canvas.drawText(node.getLabel(), node.getX() - 30, node.getY() - 40, paint);

            // Draw distance if known
            if (node.getDistance() != Double.MAX_VALUE && 
                (isAnimating || (!isAnimating && dijkstraAlgorithm.getCurrentStepIndex() == dijkstraAlgorithm.getTotalSteps() - 1))) {
                paint.setColor(Color.BLACK);
                paint.setTextSize(25);
                canvas.drawText(String.format("%.2f", node.getDistance()), 
                              node.getX() - 20, node.getY() + 50, paint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateNodePositions();
    }

    private void updateNodePositions() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY) - 100;
        List<Node> nodes = graph.getNodes();
        int numberOfNodes = nodes.size();

        for (int i = 0; i < numberOfNodes; i++) {
            double angle = 2 * Math.PI * i / numberOfNodes;
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            nodes.get(i).setX(x);
            nodes.get(i).setY(y);
        }
        invalidate();
    }

    public void runDijkstra() {
        if (graph.getStartNode() == null || graph.getTargetNode() == null) {
            if (infoText != null) infoText.setText("Set both start and target nodes.");
            return;
        }

        resetForDijkstra();
        dijkstraAlgorithm.run();
        invalidate();
    }

    private void resetForDijkstra() {
        graph.resetNodes();
        currentAnimationPath.clear();
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        if (infoText != null) infoText.setText("Ready to run Dijkstra's algorithm.");
    }

    public void stepAnimation() {
        DijkstraAlgorithm.DijkstraStep step = dijkstraAlgorithm.nextStep();
        if (step != null) {
            applyAnimationStep(step);
            invalidate();
        } else {
            isAnimating = false;
            if (infoText != null) infoText.setText("Algorithm complete.");
        }
    }

    private void applyAnimationStep(DijkstraAlgorithm.DijkstraStep step) {
        // Update node distances
        for (Node node : graph.getNodes()) {
            node.setDistance(step.getNodeDistances().getOrDefault(node, Double.MAX_VALUE));
        }

        // Update animation path
        if (step.getUpdatedNode() != null && step.getUpdatedNode().getPrevious() != null) {
            Edge edge = graph.getEdge(step.getUpdatedNode(), step.getUpdatedNode().getPrevious());
            if (edge != null && !currentAnimationPath.contains(edge)) {
                currentAnimationPath.add(edge);
            }
        }

        if (infoText != null) {
            infoText.setText("Step " + (dijkstraAlgorithm.getCurrentStepIndex() + 1) + ": " + 
                           step.getDescription());
        }
    }

    public void startAnimation() {
        if (!isAnimating && dijkstraAlgorithm.getCurrentStepIndex() < dijkstraAlgorithm.getTotalSteps() - 1) {
            isAnimating = true;
            animationHandler.postDelayed(animationRunnable, 0);
        }
    }

    public void pauseAnimation() {
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        if (infoText != null && dijkstraAlgorithm.getCurrentStep() != null) {
            infoText.setText("Animation paused at step " + (dijkstraAlgorithm.getCurrentStepIndex() + 1));
        }
    }

    public void resetGraph() {
        graph = Graph.createHardcodedGraph();
        dijkstraAlgorithm = new DijkstraAlgorithm(graph);
        currentAnimationPath.clear();
        isAnimating = false;
        animationHandler.removeCallbacks(animationRunnable);
        updateNodePositions();
        if (infoText != null) infoText.setText("Graph reset.");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animationHandler.removeCallbacks(animationRunnable);
    }
} 