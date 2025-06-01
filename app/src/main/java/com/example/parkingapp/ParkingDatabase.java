package com.example.parkingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList; // Import ArrayList
import java.util.List;    // Import List

public class ParkingDatabase {

    private DatabaseHelper dbHelper;
    private static final String USER_ID = "single_user"; // Existing constant

    public ParkingDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Existing method: Insert Parking Session
    public void insertParkingSession(String plate, String location, String duration) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLATE, plate);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);

        db.insert(DatabaseHelper.TABLE_PARKING, null, values);
        db.close();
    }

    // Existing method: Add Park Points
    public void addParkPoints(int points) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, USER_ID);
        values.put(DatabaseHelper.COLUMN_PARK_POINTS, points);

        // elexos tou user
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        if (cursor.getCount() > 0) {
            // enhmerwsh balance
            int currentPoints = getParkPoints();
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, currentPoints + points);
            db.update(DatabaseHelper.TABLE_USER_BALANCE, values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{USER_ID});
        } else {
            db.insert(DatabaseHelper.TABLE_USER_BALANCE, null, values);
        }
        cursor.close();
        db.close();
    }

    // Existing method: Get Park Points
    public int getParkPoints() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_PARK_POINTS},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARK_POINTS));
        }
        cursor.close();
        db.close();
        return points;
    }

    // --- NEW METHODS FOR PARKING AREAS (R6) ---

    // Method to add a new parking area
    public boolean addParkingArea(String locationName, String openingHours, double costPerPP) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AREA_LOCATION_NAME, locationName);
        values.put(DatabaseHelper.COLUMN_AREA_OPENING_HOURS, openingHours);
        values.put(DatabaseHelper.COLUMN_AREA_COST_PER_PP, costPerPP);

        long result = db.insert(DatabaseHelper.TABLE_PARKING_AREAS, null, values);
        db.close();
        // If result is -1, insertion failed
        return result != -1;
    }

    // Method to get all parking areas
    // This will return a list of formatted strings for the ListView
    public List<String> getAllParkingAreasForDisplay() {
        List<String> parkingAreaList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PARKING_AREAS,
                new String[]{
                        DatabaseHelper.COLUMN_AREA_ID,
                        DatabaseHelper.COLUMN_AREA_LOCATION_NAME,
                        DatabaseHelper.COLUMN_AREA_OPENING_HOURS,
                        DatabaseHelper.COLUMN_AREA_COST_PER_PP
                },
                null, null, null, null,
                DatabaseHelper.COLUMN_AREA_LOCATION_NAME + " ASC" // Order by location name
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_ID));
                String locationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_LOCATION_NAME));
                String openingHours = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_OPENING_HOURS));
                double costPerPP = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_COST_PER_PP));

                // Format string for display in ListView
                parkingAreaList.add(
                        "ID: " + id + " | Location: " + locationName +
                                "\nHours: " + openingHours + " | Cost: " + String.format("%.2f", costPerPP) + " PP/hr"
                );
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return parkingAreaList;
    }

    // Method to get a single parking area by location name (useful for update/delete)
    public ContentValues getParkingAreaDetails(String locationName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues details = null;
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PARKING_AREAS,
                new String[]{
                        DatabaseHelper.COLUMN_AREA_ID,
                        DatabaseHelper.COLUMN_AREA_LOCATION_NAME,
                        DatabaseHelper.COLUMN_AREA_OPENING_HOURS,
                        DatabaseHelper.COLUMN_AREA_COST_PER_PP
                },
                DatabaseHelper.COLUMN_AREA_LOCATION_NAME + " = ?",
                new String[]{locationName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            details = new ContentValues();
            details.put(DatabaseHelper.COLUMN_AREA_ID, cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_ID)));
            details.put(DatabaseHelper.COLUMN_AREA_LOCATION_NAME, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_LOCATION_NAME)));
            details.put(DatabaseHelper.COLUMN_AREA_OPENING_HOURS, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_OPENING_HOURS)));
            details.put(DatabaseHelper.COLUMN_AREA_COST_PER_PP, cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_COST_PER_PP)));
        }
        cursor.close();
        db.close();
        return details;
    }

    // Method to update an existing parking area
    public boolean updateParkingArea(int areaId, String newLocationName, String newOpeningHours, double newCostPerPP) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AREA_LOCATION_NAME, newLocationName);
        values.put(DatabaseHelper.COLUMN_AREA_OPENING_HOURS, newOpeningHours);
        values.put(DatabaseHelper.COLUMN_AREA_COST_PER_PP, newCostPerPP);

        int rowsAffected = db.update(
                DatabaseHelper.TABLE_PARKING_AREAS,
                values,
                DatabaseHelper.COLUMN_AREA_ID + " = ?",
                new String[]{String.valueOf(areaId)}
        );
        db.close();
        return rowsAffected > 0;
    }

    // Method to delete a parking area by ID
    public boolean deleteParkingArea(int areaId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(
                DatabaseHelper.TABLE_PARKING_AREAS,
                DatabaseHelper.COLUMN_AREA_ID + " = ?",
                new String[]{String.valueOf(areaId)}
        );
        db.close();
        return rowsAffected > 0;
    }
    // NEW METHOD: Deduct Park Points
    public boolean deductParkPoints(int pointsToDeduct) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        int currentPoints = getParkPoints(); // Use existing method to get current points

        if (currentPoints >= pointsToDeduct) {
            // Enough points, proceed with deduction
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, currentPoints - pointsToDeduct);
            int rowsAffected = db.update(DatabaseHelper.TABLE_USER_BALANCE, values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{USER_ID});
            return rowsAffected > 0; // Return true if deduction was successful
        } else {
            // Not enough points
            return false;
        }
    }
}