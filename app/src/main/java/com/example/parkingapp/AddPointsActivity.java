package com.example.parkingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddPointsActivity extends AppCompatActivity {

    private EditText editAmount;
    private TextView textParkPoints;
    private Button btnAddPoints;
    private ParkingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_points);

        editAmount = findViewById(R.id.editAmount);
        textParkPoints = findViewById(R.id.textParkPoints);
        btnAddPoints = findViewById(R.id.btnAddPoints);
        db = new ParkingDatabase(this);

        // emfanisi PP
        updateParkPointsDisplay();

        btnAddPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = editAmount.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(AddPointsActivity.this, "Παρακαλώ εισάγετε ποσό!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int amount;
                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddPointsActivity.this, "Εισάγετε έγκυρο αριθμό!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (amount <= 0) {
                    Toast.makeText(AddPointsActivity.this, "Το ποσό πρέπει να είναι μεγαλύτερο από 0!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // metatropi se 1 : 3
                int points = amount*3;
                db.addParkPoints(points);
                Toast.makeText(AddPointsActivity.this, points + " Park Points προστέθηκαν!", Toast.LENGTH_SHORT).show();
                updateParkPointsDisplay();
                editAmount.setText("");
            }
        });
    }

    private void updateParkPointsDisplay() {
        int points = db.getParkPoints();
        textParkPoints.setText("Current Park Points: " + points);
    }
}