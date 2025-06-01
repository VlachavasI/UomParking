package com.example.parkingapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ParkingAreaManagementActivity extends AppCompatActivity {

    private EditText editTextLocationName;
    private EditText editTextOpeningHours;
    private EditText editTextCostPerPP;
    private Button buttonAddArea;
    private Button buttonUpdateArea;
    private Button buttonDeleteArea;
    private ListView listViewParkingAreas;

    private ParkingDatabase db; // We'll update this class for Parking Areas

    private ArrayAdapter<String> parkingAreaAdapter;
    private ArrayList<String> parkingAreaList; // To hold data for the ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_area_management);

        editTextLocationName = findViewById(R.id.editTextLocationName);
        editTextOpeningHours = findViewById(R.id.editTextOpeningHours);
        editTextCostPerPP = findViewById(R.id.editTextCostPerPP);
        buttonAddArea = findViewById(R.id.buttonAddArea);
        buttonUpdateArea = findViewById(R.id.buttonUpdateArea);
        buttonDeleteArea = findViewById(R.id.buttonDeleteArea);
        listViewParkingAreas = findViewById(R.id.listViewParkingAreas);

        db = new ParkingDatabase(this); // Initialize your database helper

        parkingAreaList = new ArrayList<>();
        parkingAreaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Simple layout for list items
                parkingAreaList
        );
        listViewParkingAreas.setAdapter(parkingAreaAdapter);

        // --- Event Listeners ---
        buttonAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ParkingAreaManagementActivity.this, "Add Area Clicked (Logic coming soon!)", Toast.LENGTH_SHORT).show();
                // After adding, call updateParkingAreaList();
            }
        });

        buttonUpdateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ParkingAreaManagementActivity.this, "Update Area Clicked (Logic coming soon!)", Toast.LENGTH_SHORT).show();
                // After updating, call updateParkingAreaList();
            }
        });

        buttonDeleteArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ParkingAreaManagementActivity.this, "Delete Area Clicked (Logic coming soon!)", Toast.LENGTH_SHORT).show();
                // After deleting, call updateParkingAreaList();
            }
        });

        listViewParkingAreas.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parkingAreaList.get(position);
            Toast.makeText(ParkingAreaManagementActivity.this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            // In a real implementation, you would parse the selectedItem
            // and populate editTextLocationName, editTextOpeningHours, editTextCostPerPP
            // for editing.
        });

        updateParkingAreaList(); // Populate the list on start
    }

    // This method will fetch and display parking areas from the database
    private void updateParkingAreaList() {
        parkingAreaList.clear();
        // Placeholder data for now. We will replace this with actual database calls.
        parkingAreaList.add("Example Location A (08:00-20:00, 10 PP/hr)");
        parkingAreaList.add("Example Location B (09:00-18:00, 12 PP/hr)");
        parkingAreaAdapter.notifyDataSetChanged();
    }
}