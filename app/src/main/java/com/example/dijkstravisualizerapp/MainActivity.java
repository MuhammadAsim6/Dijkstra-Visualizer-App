package com.example.dijkstravisualizerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GraphView graphView;
    private Button runButton, resetButton, setStartBtn, setTargetBtn, addEdgeBtn, addNodeBtn;
    private TextView infoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graphView =findViewById(R.id.graphView);
        runButton =findViewById(R.id.runBtn);
        resetButton= findViewById(R.id.resetBtn);

        setStartBtn= findViewById(R.id.setStartBtn);
        setTargetBtn =findViewById(R.id.setTargetBtn);
        addEdgeBtn = findViewById(R.id.addEdgeBtn);
        addNodeBtn= findViewById(R.id.addNodeBtn);
        infoText = findViewById(R.id.infoText);

        graphView.setInfoText(infoText);

        runButton.setOnClickListener(v -> graphView.runDijkstra());
        resetButton.setOnClickListener(v -> graphView.resetGraph());
        setStartBtn.setOnClickListener(v -> graphView.setMode(GraphView.Mode.SET_START));

        setTargetBtn.setOnClickListener(v -> graphView.setMode(GraphView.Mode.SET_TARGET));
        addEdgeBtn.setOnClickListener(v -> graphView.setMode(GraphView.Mode.ADD_EDGE));
        addNodeBtn.setOnClickListener(v -> graphView.setMode(GraphView.Mode.ADD_NODE));
    }
} 