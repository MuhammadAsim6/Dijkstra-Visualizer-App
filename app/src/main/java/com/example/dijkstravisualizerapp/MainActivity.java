package com.example.dijkstravisualizerapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dijkstravisualizerapp.view.GraphView;

/**
 * Main activity for the Dijkstra's Algorithm Visualizer.
 * Provides the user interface for controlling the visualization.
 */
public class MainActivity extends AppCompatActivity {
    private GraphView graphView;
    private Button runButton, resetButton, stepButton, playButton, pauseButton;
    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();
        // Set up button click listeners
        setupClickListeners();
    }

    /**
     * Initialize all views from the layout.
     */
    private void initializeViews() {
        graphView = findViewById(R.id.graphView);
        runButton = findViewById(R.id.runBtn);
        resetButton = findViewById(R.id.resetBtn);
        stepButton = findViewById(R.id.stepBtn);
        playButton = findViewById(R.id.playBtn);
        pauseButton = findViewById(R.id.pauseBtn);
        infoText = findViewById(R.id.infoText);

        // Set the info text view for the graph view
        graphView.setInfoText(infoText);
    }

    /**
     * Set up click listeners for all control buttons.
     */
    private void setupClickListeners() {
        runButton.setOnClickListener(v -> graphView.runDijkstra());
        resetButton.setOnClickListener(v -> graphView.resetGraph());
        stepButton.setOnClickListener(v -> graphView.stepAnimation());
        playButton.setOnClickListener(v -> graphView.startAnimation());
        pauseButton.setOnClickListener(v -> graphView.pauseAnimation());
    }
} 