package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingSessionsGraphActivity extends AppCompatActivity {

    private BarChart barChart;
    private ParkingDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_sessions_graph);

        barChart = findViewById(R.id.bar_chart_sessions);
        database = new ParkingDatabase(this);

        setupChart();
        loadData();
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(false);

        // Customize appearance
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);

        // Add margins to prevent overflow
        barChart.setExtraOffsets(5f, 10f, 5f, 5f);

        // X-axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Y-axis configuration
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f); // Set minimum interval to 1
        leftAxis.setSpaceTop(15f); // Add space at the top
    }

    private void loadData() {
        // Get parking sessions by location
        List<ParkingSession> sessions = database.getAllParkingSessions();

        // Group sessions by location
        Map<String, Integer> locationCounts = new HashMap<>();
        for (ParkingSession session : sessions) {
            String location = session.getLocation();
            locationCounts.put(location, locationCounts.getOrDefault(location, 0) + 1);
        }

        // Create chart entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        int maxValue = 0;

        for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
            int value = entry.getValue();
            entries.add(new BarEntry(index, value));
            labels.add(entry.getKey());
            maxValue = Math.max(maxValue, value);
            index++;
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Parking Sessions by Location");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        // Set data to chart
        barChart.setData(data);

        // Configure Y-axis with proper maximum value
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMaximum(maxValue + (maxValue * 0.2f)); // Add 20% padding to top

        // If you have specific maximum you want to set, uncomment this:
        // leftAxis.setAxisMaximum(50f); // Set your desired maximum value

        // Configure X-axis labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        // Set visible range if you have many bars
        if (labels.size() > 6) {
            barChart.setVisibleXRangeMaximum(6f);
            barChart.moveViewToX(0);
        }

        // Animate chart
        barChart.animateY(1000);
        barChart.invalidate(); // Refresh chart

        // Show total count
        TextView totalSessions = findViewById(R.id.tv_total_sessions);
        totalSessions.setText("Total Sessions: " + sessions.size());
    }
}