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
    String location, duration, plate;
    private ParkingDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPlate = findViewById(R.id.editPlate);
        editLocation = findViewById(R.id.editLocation);
        editDuration = findViewById(R.id.editDuration);
        btnStart = findViewById(R.id.btnStart);
        btnGoToAddPoints = findViewById(R.id.btnGoToAddPoints);
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


                ParkingDatabase db = new ParkingDatabase(MainActivity.this);
                db.insertParkingSession(plate, location, duration);

                Toast.makeText(MainActivity.this, "Συνεδρία στάθμευσης καταχωρήθηκε!", Toast.LENGTH_SHORT).show();

            }
        });

        btnGoToAddPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimerActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("time", duration);
                intent.putExtra("license", plate);
                startActivity(intent);
            }
        });

    }
}