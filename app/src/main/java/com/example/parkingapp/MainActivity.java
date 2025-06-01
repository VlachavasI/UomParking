package com.example.parkingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editPlate;
    Spinner editLocation;
    Spinner editDuration;
    Button btnStart;
    Button btnGoToAddPoints;
    Button btnGoToAdminLogin;
    Button btnGoToUserStatistics;
    private ParkingDatabase db;
    private TextView textCurrentParkPoints;

    // Hardcoded parking point rules (ensure these match ParkingDatabase.java)
    private static final int COST_PER_HOUR_PP = 10;
    private static final int COST_PER_30_MIN_PP = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPlate = findViewById(R.id.editPlate);
        editLocation = findViewById(R.id.editLocation);
        editDuration = findViewById(R.id.editDuration);
        btnStart = findViewById(R.id.btnStart);
        btnGoToAddPoints = findViewById(R.id.btnGoToAddPoints);
        btnGoToAdminLogin = findViewById(R.id.btnGoToAdminLogin);
        textCurrentParkPoints = findViewById(R.id.textCurrentParkPoints);
        btnGoToUserStatistics = findViewById(R.id.btnGoToUserStatistics);

        db = new ParkingDatabase(this);

        String[] locations = {
                "Επιλέξτε τοποθεσία",
                "Κέντρο",
                "Βενιζέλου",
                "Παπάφη",
                "Νέα Ελβετία"
        };

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editLocation.setAdapter(locationAdapter);

        String[] durations = {
                "Επιλέξτε διάρκεια",
                "30 λεπτά",
                "1 ώρα",
                "2 ώρες",
                "3 ώρες"
        };

        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDuration.setAdapter(durationAdapter);

        // Initial update of park points display when activity starts
        updateParkPointsDisplay();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plate = editPlate.getText().toString().trim();
                String location = editLocation.getSelectedItem().toString();
                String duration = editDuration.getSelectedItem().toString();

                // 1. Input validation
                if (plate.isEmpty() || location.equals("Επιλέξτε τοποθεσία") || duration.equals("Επιλέξτε διάρκεια")) {
                    Toast.makeText(MainActivity.this, "Συμπληρώστε όλα τα πεδία!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Calculate parking points needed
                int pointsNeeded = 0;
                switch (duration) {
                    case "30 λεπτά":
                        pointsNeeded = COST_PER_30_MIN_PP;
                        break;
                    case "1 ώρα":
                        pointsNeeded = COST_PER_HOUR_PP;
                        break;
                    case "2 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 2;
                        break;
                    case "3 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 3;
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Επιλέξτε έγκυρη διάρκεια!", Toast.LENGTH_SHORT).show();
                        return;
                }

                // 3. Check if plate is already parked to prevent UNIQUE constraint violation
                if (db.isPlateParked(plate)) {
                    Toast.makeText(MainActivity.this, "Το όχημα με αυτή την πινακίδα είναι ήδη σταθμευμένο!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 4. Check if user has enough points
                int currentPoints = db.getParkPoints();
                if (currentPoints < pointsNeeded) {
                    Toast.makeText(MainActivity.this, "Δεν έχετε αρκετούς Park Points! Απαιτούνται: " + pointsNeeded + ", Διαθέσιμα: " + currentPoints, Toast.LENGTH_LONG).show();
                    return;
                }

                // 5. If all checks pass, deduct points first (as discussed, this is the order)
                boolean pointsDeducted = db.deductParkPoints(pointsNeeded);

                if (pointsDeducted) {
                    // Get the current time in milliseconds BEFORE inserting the session
                    long startTimeMillis = System.currentTimeMillis();

                    // 6. Attempt to insert parking session with the start time
                    boolean sessionInserted = db.insertParkingSession(plate, location, duration, startTimeMillis); // <--- THIS IS THE MODIFIED LINE

                    if (sessionInserted) {
                        Toast.makeText(MainActivity.this, "Συνεδρία στάθμευσης καταχωρήθηκε και " + pointsNeeded + " Park Points αφαιρέθηκαν!", Toast.LENGTH_LONG).show();
                        // Clear fields after successful insertion and deduction
                        editPlate.setText("");
                        editLocation.setSelection(0);
                        editDuration.setSelection(0);

                        // Start TimerActivity and pass necessary data
                        Intent timerIntent = new Intent(MainActivity.this, TimerActivity.class);
                        timerIntent.putExtra("plate", plate);
                        timerIntent.putExtra("location", location);
                        timerIntent.putExtra("pointsNeeded", pointsNeeded); // Pass the points
                        // No need to pass startTimeMillis to TimerActivity unless TimerActivity itself needs to display remaining time
                        // or has a timer that depends on the actual start time (which it should, but not directly for this insert issue).
                        startActivity(timerIntent);
                    } else {
                        // This else block handles the case where insertParkingSession returns false.
                        // Ideally, this should not be reached due to the `isPlateParked` check above.
                        // If it is reached, it implies a race condition or other issue.
                        // In this scenario, since points were already deducted, you would need to refund them.
                        db.addParkPoints(pointsNeeded); // Refund points
                        Toast.makeText(MainActivity.this, "Αποτυχία καταχώρησης συνεδρίας. Επιστροφή Park Points.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Αποτυχία αφαίρεσης Park Points. Παρακαλώ δοκιμάστε ξανά.", Toast.LENGTH_LONG).show();
                }

                // Always update Park Points display after any attempt to start a session
                updateParkPointsDisplay();
            }
        });

        btnGoToAddPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPointsActivity.class);
                startActivity(intent);
            }
        });

        btnGoToAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });

        btnGoToUserStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserStatisticsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateParkPointsDisplay();
    }

    private void updateParkPointsDisplay() {
        int points = db.getParkPoints();
        textCurrentParkPoints.setText("Διαθέσιμοι Park Points: " + points);
    }
}