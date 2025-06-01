package com.example.parkingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editPlate;
    Spinner editLocation;
    Spinner editDuration;
    Button btnStart;
    Button btnGoToAddPoints;
    Button btnGoToAdminLogin; // New button for admin login
    private ParkingDatabase db;

    // Hardcoded parking point rules
    private static final int COST_PER_HOUR_PP = 10;
    private static final int COST_PER_30_MIN_PP = 5; // 30 minutes is half an hour



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPlate = findViewById(R.id.editPlate);
        editLocation = findViewById(R.id.editLocation);
        editDuration = findViewById(R.id.editDuration);
        btnStart = findViewById(R.id.btnStart);
        btnGoToAddPoints = findViewById(R.id.btnGoToAddPoints);
        btnGoToAdminLogin = findViewById(R.id.btnGoToAdminLogin); // Initialize the new button
        db = new ParkingDatabase(this);

        String[] locations = {
                "Επιλέξτε τοποθεσία",
                "Κέντρο",
                "Βενιζέλου",
                "Παπάφη",
                "Νέα Ελβετία",
                "mikro pouli"
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

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plate = editPlate.getText().toString().trim();
                String location = editLocation.getSelectedItem().toString();
                String duration = editDuration.getSelectedItem().toString();

                if (plate.isEmpty() || location.equals("Επιλέξτε τοποθεσία") || duration.equals("Επιλέξτε διάρκεια")) {
                    Toast.makeText(MainActivity.this, "Συμπληρώστε όλα τα πεδία!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Calculate parking points needed
                int pointsNeeded = 0;
                switch (duration) {
                    case "30 λεπτά":
                        pointsNeeded = COST_PER_30_MIN_PP; // 5 PP
                        break;
                    case "1 ώρα":
                        pointsNeeded = COST_PER_HOUR_PP; // 10 PP
                        break;
                    case "2 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 2; // 20 PP
                        break;
                    case "3 ώρες":
                        pointsNeeded = COST_PER_HOUR_PP * 3; // 30 PP
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Επιλέξτε έγκυρη διάρκεια!", Toast.LENGTH_SHORT).show();
                        return;
                }

                // Check if user has enough park points
                if (db.getParkPoints() < pointsNeeded) {
                    Toast.makeText(MainActivity.this, "Δεν έχετε αρκετούς Park Points! (Απαιτούνται: " + pointsNeeded + ", Υπάρχουν: " + db.getParkPoints() + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                // Deduct points and insert parking session
                if (db.deductParkPoints(pointsNeeded)) {
                    db.insertParkingSession(plate, location, duration);
                    Toast.makeText(MainActivity.this, "Συνεδρία στάθμευσης καταχωρήθηκε! (-" + pointsNeeded + " PP)", Toast.LENGTH_LONG).show();
                    editPlate.setText(""); // Clear plate field after successful session start
                    // Optionally reset spinners or navigate away
                } else {
                    // This case should ideally be caught by the getParkPoints() check above, but good for robust error handling
                    Toast.makeText(MainActivity.this, "Αποτυχία αφαίρεσης Park Points. Παρακαλώ δοκιμάστε ξανά.", Toast.LENGTH_LONG).show();
                }

                ParkingDatabase db = new ParkingDatabase(MainActivity.this); // Re-instantiating, consider making it a field or singleton
                db.insertParkingSession(plate, location, duration);

                Toast.makeText(MainActivity.this, "Συνεδρία στάθμευσης καταχωρήθηκε!", Toast.LENGTH_SHORT).show();

            }
        });

        btnGoToAddPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPointsActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the new admin login button
        btnGoToAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class); // Will navigate to Admin Login
                startActivity(intent);
            }
        });
    }
}