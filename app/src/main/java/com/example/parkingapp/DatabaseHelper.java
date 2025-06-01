package com.example.parkingapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Όνομα και έκδοση της βάσης
    private static final String DATABASE_NAME = "ParkingDatabase";
    // *** IMPORTANT: Increment the database version ***
    private static final int DATABASE_VERSION = 3; // Changed from 2 to 3

    // Όνομα πίνακα και στήλες
    public static final String TABLE_PARKING = "ParkingSessions";
    public static final String COLUMN_PLATE = "Plate";
    public static final String COLUMN_LOCATION = "Location";
    public static final String COLUMN_DURATION = "Duration";
    public static final String TABLE_USER_BALANCE = "UserBalance";
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_PARK_POINTS = "ParkPoints";

    // --- NEW TABLE FOR PARKING AREAS (R6) ---
    public static final String TABLE_PARKING_AREAS = "ParkingAreas";
    public static final String COLUMN_AREA_ID = "AreaId"; // Primary Key
    public static final String COLUMN_AREA_LOCATION_NAME = "LocationName";
    public static final String COLUMN_AREA_OPENING_HOURS = "OpeningHours";
    public static final String COLUMN_AREA_COST_PER_PP = "CostPerPP";

    private static final String TABLE_CREATE_USER_BALANCE =
            "CREATE TABLE " + TABLE_USER_BALANCE + " (" +
                    COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_PARK_POINTS + " INTEGER DEFAULT 0);";

    // SQL για δημιουργία του πίνακα ParkingSessions
    private static final String TABLE_CREATE_PARKING =
            "CREATE TABLE " + TABLE_PARKING + " (" +
                    COLUMN_PLATE + " TEXT PRIMARY KEY, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_DURATION + " TEXT);";

    // --- SQL για δημιουργία του πίνακα ParkingAreas (R6) ---
    private static final String TABLE_CREATE_PARKING_AREAS =
            "CREATE TABLE " + TABLE_PARKING_AREAS + " (" +
                    COLUMN_AREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AREA_LOCATION_NAME + " TEXT UNIQUE NOT NULL, " + // Location name should be unique
                    COLUMN_AREA_OPENING_HOURS + " TEXT, " +
                    COLUMN_AREA_COST_PER_PP + " REAL NOT NULL DEFAULT 0.0);"; // Use REAL for decimal cost

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Δημιουργία βάσης
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PARKING);
        db.execSQL(TABLE_CREATE_USER_BALANCE);
        db.execSQL(TABLE_CREATE_PARKING_AREAS); // Add the new table here
    }

    // Αν χρειαστεί να αναβαθμιστεί η βάση
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BALANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_AREAS); // Drop the new table as well
        // Recreate tables
        onCreate(db);
    }
}