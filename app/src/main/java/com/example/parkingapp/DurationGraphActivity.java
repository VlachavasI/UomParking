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

public class DurationGraphActivity extends AppCompatActivity {

    private BarChart barChart;
    private ParkingDatabase database;
    private TextView tvAvgDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration_graph);

        barChart = findViewById(R.id.bar_chart_duration);
        tvAvgDuration = findViewById(R.id.tv_avg_duration);
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
        barChart.setExtraOffsets(5f, 10f, 5f, 10f);

        // X-axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); // Rotate labels for better readability

        // Y-axis configuration
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f); // Set minimum interval to 1 minute
        leftAxis.setSpaceTop(15f); // Add space at the top
    }

    private void loadData() {
        // Get all parking sessions
        List<ParkingSession> sessions = database.getAllParkingSessions();

        if (sessions.isEmpty()) {
            // Handle empty data case
            tvAvgDuration.setText("Average Duration: No data available");
            return;
        }

        // Group sessions by location and calculate average duration
        Map<String, List<Integer>> locationDurations = new HashMap<>();
        int totalDurationMinutes = 0;
        int validSessions = 0;

        for (ParkingSession session : sessions) {
            String location = session.getLocation();
            int durationMinutes = session.getDurationInMinutes();

            if (durationMinutes > 0) { // Only count valid durations
                if (!locationDurations.containsKey(location)) {
                    locationDurations.put(location, new ArrayList<>());
                }
                locationDurations.get(location).add(durationMinutes);
                totalDurationMinutes += durationMinutes;
                validSessions++;
            }
        }

        // Calculate average durations for each location
        Map<String, Double> locationAverages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : locationDurations.entrySet()) {
            String location = entry.getKey();
            List<Integer> durations = entry.getValue();

            double average = durations.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            locationAverages.put(location, average);
        }

        // Create chart entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        double maxValue = 0;

        for (Map.Entry<String, Double> entry : locationAverages.entrySet()) {
            double avgDuration = entry.getValue();
            entries.add(new BarEntry(index, (float) avgDuration));
            labels.add(entry.getKey());
            maxValue = Math.max(maxValue, avgDuration);
            index++;
        }

        if (entries.isEmpty()) {
            tvAvgDuration.setText("Average Duration: No valid data");
            return;
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Average Duration by Location (minutes)");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(10f);

        // Format values to show as integers (minutes)
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f min", value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        // Set data to chart
        barChart.setData(data);

        // Configure Y-axis with proper maximum value
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMaximum((float) (maxValue + (maxValue * 0.2f))); // Add 20% padding to top

        // Configure X-axis labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        // Set visible range if you have many bars
        if (labels.size() > 5) {
            barChart.setVisibleXRangeMaximum(5f);
            barChart.moveViewToX(0);
        }

        // Animate chart
        barChart.animateY(1200);
        barChart.invalidate(); // Refresh chart

        // Calculate and display overall average duration
        double overallAverage = validSessions > 0 ? (double) totalDurationMinutes / validSessions : 0;
        String avgText = String.format("Average Duration: %.1f minutes", overallAverage);

        // Convert to hours and minutes if duration is long
        if (overallAverage >= 60) {
            int hours = (int) (overallAverage / 60);
            int minutes = (int) (overallAverage % 60);
            avgText = String.format("Average Duration: %dh %dm (%.1f minutes)",
                    hours, minutes, overallAverage);
        }

        tvAvgDuration.setText(avgText);
    }

}