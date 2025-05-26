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
        db.close();
    }
    //evala ta PP
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

    // Get Park Points
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
}
