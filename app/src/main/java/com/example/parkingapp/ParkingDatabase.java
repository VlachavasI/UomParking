package com.example.parkingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ParkingDatabase {

    private DatabaseHelper dbHelper;
    private static final String USER_ID = "single_user";

    public ParkingDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Εισαγωγή δεδομένων στη βάση
    public void insertParkingSession(String plate, String location, String duration) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLATE, plate);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);

        db.insert(DatabaseHelper.TABLE_PARKING, null, values);
        // db.close(); // REMOVED: Do not close here
    }
    //evala ta PP
    public void addParkPoints(int points) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get a writable database instance
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, USER_ID);
        // Do NOT put points directly here for update, you'll calculate it below

        // elexos tou user
        // Use the same db object for query or get a readable one, but be consistent
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_PARK_POINTS}, // Also query for current points
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst(); // Move to the first (and only) result
            int currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARK_POINTS));
            // enhmerwsh balance
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, currentPoints + points);
            db.update(DatabaseHelper.TABLE_USER_BALANCE, values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{USER_ID});
        } else {
            // For new user, set initial points
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, points);
            db.insert(DatabaseHelper.TABLE_USER_BALANCE, null, values);
        }
        cursor.close();
        // db.close(); // REMOVED: Do not close here
    }

    // Get Park Points
    public int getParkPoints() {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Get a readable database instance
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_PARK_POINTS},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARK_POINTS));
        }
        cursor.close();
        // db.close(); // REMOVED: Do not close here
        return points;
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