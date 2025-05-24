package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;
import android.location.Address;

import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity{

    private TextView txtSummary, txtCost, txtLocation;
    private Button btnPayNow;
    private String location;  // store globally


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        txtSummary = findViewById(R.id.txtSummary);
        txtCost = findViewById(R.id.txtCost);
        btnPayNow = findViewById(R.id.btnPayNow);


        // Get data from intent
        Intent intent = getIntent();
        double finalCost = intent.getDoubleExtra("finalCost", 0.0);
        String totalTime = intent.getStringExtra("totalTime");
        String location = intent.getStringExtra("location");
        txtLocation = findViewById(R.id.txtLocation);
        txtLocation.setText("Location: " + location);

        // Set values to views
        txtSummary.setText("Total Time Parked: " + totalTime);
        txtCost.setText(String.format(Locale.getDefault(), "Total Cost: $%.2f", finalCost));

        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simulate payment success
                Toast.makeText(PaymentActivity.this, "Payment Successful!", Toast.LENGTH_LONG).show();

                // You can also go back to main screen, or show receipt, etc.
                finish(); // Closes the activity
            }
        });
    }
}