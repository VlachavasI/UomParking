package com.example.parkingapp; // Ensure this package matches your project's root package

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText adminUsername;
    private EditText adminPassword;
    private Button btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login); // You'll create this layout

        adminUsername = findViewById(R.id.adminUsername);
        adminPassword = findViewById(R.id.adminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = adminUsername.getText().toString().trim();
                String password = adminPassword.getText().toString().trim();

                // Simple hardcoded check for demonstration.
                // In a real app, you would query your DatabaseHelper for admin credentials
                // and use proper password hashing.
                if (username.equals("admin") && password.equals("adminpass")) {
                    Toast.makeText(AdminLoginActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class); // Navigate to Admin Dashboard
                    startActivity(intent);
                    finish(); // Close login activity
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}