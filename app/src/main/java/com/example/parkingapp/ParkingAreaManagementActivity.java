package com.example.parkingapp;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ParkingAreaManagementActivity extends AppCompatActivity {

    private EditText editTextLocationName;
    private EditText editTextOpeningHours;
    private EditText editTextCostPerPP;
    private Button buttonAddArea;
    private Button buttonUpdateArea;
    private Button buttonDeleteArea;
    private ListView listViewParkingAreas;

    private ParkingDatabase db;

    private ArrayAdapter<String> parkingAreaAdapter;
    private ArrayList<String> parkingAreaList; // To hold data for the ListView

    private int selectedAreaId = -1; // To store the ID of the currently selected parking area for update/delete

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

        db = new ParkingDatabase(this);

        parkingAreaList = new ArrayList<>();
        parkingAreaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Simple layout for list items
                parkingAreaList
        );
        listViewParkingAreas.setAdapter(parkingAreaAdapter);

        // --- Event Listeners ---

        // Add Button Listener
        buttonAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = editTextLocationName.getText().toString().trim();
                String openingHours = editTextOpeningHours.getText().toString().trim();
                String costStr = editTextCostPerPP.getText().toString().trim();

                if (locationName.isEmpty() || openingHours.isEmpty() || costStr.isEmpty()) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double costPerPP = Double.parseDouble(costStr);
                    if (costPerPP < 0) {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Cost cannot be negative.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (db.addParkingArea(locationName, openingHours, costPerPP)) {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Parking Area Added!", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        updateParkingAreaList();
                    } else {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Failed to add area. Location might already exist.", Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Invalid cost value.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update Button Listener
        buttonUpdateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAreaId == -1) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Please select an area to update.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String locationName = editTextLocationName.getText().toString().trim();
                String openingHours = editTextOpeningHours.getText().toString().trim();
                String costStr = editTextCostPerPP.getText().toString().trim();

                if (locationName.isEmpty() || openingHours.isEmpty() || costStr.isEmpty()) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double costPerPP = Double.parseDouble(costStr);
                    if (costPerPP < 0) {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Cost cannot be negative.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (db.updateParkingArea(selectedAreaId, locationName, openingHours, costPerPP)) {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Parking Area Updated!", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        updateParkingAreaList();
                        selectedAreaId = -1; // Reset selected ID
                    } else {
                        Toast.makeText(ParkingAreaManagementActivity.this, "Failed to update area. Location name might exist.", Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Invalid cost value.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete Button Listener
        buttonDeleteArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAreaId == -1) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Please select an area to delete.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.deleteParkingArea(selectedAreaId)) {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Parking Area Deleted!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    updateParkingAreaList();
                    selectedAreaId = -1; // Reset selected ID
                } else {
                    Toast.makeText(ParkingAreaManagementActivity.this, "Failed to delete area.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ListView Item Click Listener
        listViewParkingAreas.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItemString = parkingAreaList.get(position);
            // Extract location name from the displayed string for fetching details
            // The format is "ID: X | Location: Y..."
            // Find "Location: " and then the next "|" or end of string
            int locationStart = selectedItemString.indexOf("Location: ") + "Location: ".length();
            int locationEnd = selectedItemString.indexOf("\n", locationStart); // find newline
            if (locationEnd == -1) locationEnd = selectedItemString.length(); // if no newline, take till end

            String locationName = selectedItemString.substring(locationStart, locationEnd).trim();

            ContentValues details = db.getParkingAreaDetails(locationName);
            if (details != null) {
                selectedAreaId = details.getAsInteger(DatabaseHelper.COLUMN_AREA_ID);
                editTextLocationName.setText(details.getAsString(DatabaseHelper.COLUMN_AREA_LOCATION_NAME));
                editTextOpeningHours.setText(details.getAsString(DatabaseHelper.COLUMN_AREA_OPENING_HOURS));
                editTextCostPerPP.setText(String.valueOf(details.getAsDouble(DatabaseHelper.COLUMN_AREA_COST_PER_PP)));
                Toast.makeText(ParkingAreaManagementActivity.this, "Area selected for edit.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ParkingAreaManagementActivity.this, "Could not retrieve area details.", Toast.LENGTH_SHORT).show();
                selectedAreaId = -1; // Reset if details not found
            }
        });

        updateParkingAreaList(); // Populate the list on start
    }

    // This method will fetch and display parking areas from the database
    private void updateParkingAreaList() {
        parkingAreaList.clear();
        List<String> areas = db.getAllParkingAreasForDisplay();
        parkingAreaList.addAll(areas);
        parkingAreaAdapter.notifyDataSetChanged();
    }

    // Helper method to clear input fields
    private void clearInputFields() {
        editTextLocationName.setText("");
        editTextOpeningHours.setText("");
        editTextCostPerPP.setText("");
        selectedAreaId = -1; // Reset selected ID
        editTextLocationName.requestFocus(); // Set focus back to location name
    }
}