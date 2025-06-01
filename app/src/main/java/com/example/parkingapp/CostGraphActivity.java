package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostGraphActivity extends AppCompatActivity {

    private PieChart pieChart;
    private ParkingDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_graph);

        pieChart = findViewById(R.id.pie_chart_cost);
        database = new ParkingDatabase(this);

        setupChart();
        loadData();
    }

    private void setupChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        // Legend
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    }

    private void loadData() {
        List<ParkingSession> sessions = database.getAllParkingSessions();

        // Group costs by cost ranges
        Map<String, Float> costRanges = new HashMap<>();
        costRanges.put("$0-2", 0f);
        costRanges.put("$2-4", 0f);
        costRanges.put("$4-6", 0f);

        float totalCost = 0f;

        for (ParkingSession session : sessions) {
            float cost = calculateCostForSession(session);
            totalCost += cost;

            if (cost <= 2) {
                costRanges.put("$0-1", costRanges.get("$0-2") + 1);
            } else if (cost <= 4) {
                costRanges.put("$2-4", costRanges.get("$2-4") + 1);
            } else if (cost <= 6) {
                costRanges.put("$4-6", costRanges.get("$4-6") + 1);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : costRanges.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Cost Distribution");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();

        // Display average cost
        TextView avgCost = findViewById(R.id.tv_avg_cost);
        float average = sessions.isEmpty() ? 0 : totalCost / sessions.size();
        avgCost.setText(String.format("Average Cost: $%.2f", average));
    }

    private float calculateCostForSession(ParkingSession session) {
        // Simple pricing: $2 per hour, minimum $1
        int minutes = session.getDurationInMinutes();
        double hours = minutes / 60.0;
        return (float) Math.max(1.0, hours * 2.0);
    }
}