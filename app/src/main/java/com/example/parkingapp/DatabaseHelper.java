package com.example.parkingapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Όνομα και έκδοση της βάσης
    private static final String DATABASE_NAME = "ParkingDatabase";
    private static final int DATABASE_VERSION = 1;

    // Όνομα πίνακα και στήλες
    public static final String TABLE_PARKING = "ParkingSessions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PLATE = "Plate";
    public static final String COLUMN_LOCATION = "Location";
    public static final String COLUMN_DURATION = "Duration";

    // SQL για δημιουργία του πίνακα
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PARKING + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PLATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_DURATION + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Δημιουργία βάσης
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    // Αν χρειαστεί να αναβαθμιστεί η βάση
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING);
        onCreate(db);
    }
}
