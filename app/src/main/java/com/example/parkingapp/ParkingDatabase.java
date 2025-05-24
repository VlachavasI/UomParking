package com.example.parkingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ParkingDatabase {

    private DatabaseHelper dbHelper;

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
}
