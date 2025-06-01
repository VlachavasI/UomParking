package com.example.parkingapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Όνομα και έκδοση της βάσης
    private static final String DATABASE_NAME = "ParkingDatabase";
    // *** IMPORTANT: Increment the database version ***
    private static final int DATABASE_VERSION = 4; // Changed from 3 to 4

    // Όνομα πίνακα και στήλες
    public static final String TABLE_PARKING = "ParkingSessions";
    public static final String COLUMN_PLATE = "Plate";
    public static final String COLUMN_LOCATION = "Location";
    public static final String COLUMN_DURATION = "Duration";
    public static final String COLUMN_START_TIME = "StartTime"; // Added for active sessions

    public static final String TABLE_USER_BALANCE = "UserBalance";
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_PARK_POINTS = "ParkPoints";

    // --- NEW TABLE FOR PARKING AREAS (R6) ---
    public static final String TABLE_PARKING_AREAS = "ParkingAreas";
    public static final String COLUMN_AREA_ID = "AreaId"; // Primary Key
    public static final String COLUMN_AREA_LOCATION_NAME = "LocationName";
    public static final String COLUMN_AREA_OPENING_HOURS = "OpeningHours";
    public static final String COLUMN_AREA_COST_PER_PP = "CostPerPP";

    // --- NEW TABLE for completed parking sessions history ---
    public static final String TABLE_PARKING_HISTORY = "ParkingHistory"; // Changed table name for consistency
    public static final String HISTORY_COLUMN_ID = "_id"; // Primary Key for history
    public static final String HISTORY_COLUMN_PLATE = "Plate";
    public static final String HISTORY_COLUMN_LOCATION = "Location";
    public static final String HISTORY_COLUMN_DURATION_STR = "DurationString"; // Original selected duration (e.g., "1 ώρα")
    public static final String HISTORY_COLUMN_START_TIME = "StartTime";
    public static final String HISTORY_COLUMN_END_TIME = "EndTime";
    public static final String HISTORY_COLUMN_POINTS_PAID = "PointsPaid"; // Store the points cost

    private static final String TABLE_CREATE_USER_BALANCE =
            "CREATE TABLE " + TABLE_USER_BALANCE + " (" +
                    COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_PARK_POINTS + " INTEGER DEFAULT 0);";

    // SQL για δημιουργία του πίνακα ParkingSessions (updated to include StartTime)
    private static final String TABLE_CREATE_PARKING =
            "CREATE TABLE " + TABLE_PARKING + " (" +
                    COLUMN_PLATE + " TEXT PRIMARY KEY, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_DURATION + " TEXT, " +
                    COLUMN_START_TIME + " INTEGER DEFAULT 0);"; // Added StartTime

    // --- SQL για δημιουργία του πίνακα ParkingAreas (R6) ---
    private static final String TABLE_CREATE_PARKING_AREAS =
            "CREATE TABLE " + TABLE_PARKING_AREAS + " (" +
                    COLUMN_AREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AREA_LOCATION_NAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_AREA_OPENING_HOURS + " TEXT, " +
                    COLUMN_AREA_COST_PER_PP + " REAL NOT NULL DEFAULT 0.0);";

    // --- SQL to create the new ParkingHistory table ---
    private static final String TABLE_CREATE_PARKING_HISTORY =
            "CREATE TABLE " + TABLE_PARKING_HISTORY + " (" +
                    HISTORY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    HISTORY_COLUMN_PLATE + " TEXT NOT NULL, " +
                    HISTORY_COLUMN_LOCATION + " TEXT, " +
                    HISTORY_COLUMN_DURATION_STR + " TEXT, " +
                    HISTORY_COLUMN_START_TIME + " INTEGER, " +
                    HISTORY_COLUMN_END_TIME + " INTEGER, " +
                    HISTORY_COLUMN_POINTS_PAID + " INTEGER);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Δημιουργία βάσης
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PARKING);
        db.execSQL(TABLE_CREATE_USER_BALANCE);
        db.execSQL(TABLE_CREATE_PARKING_AREAS);
        db.execSQL(TABLE_CREATE_PARKING_HISTORY); // Create the new history table
    }

    // Αν χρειαστεί να αναβαθμιστεί η βάση
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop and recreate all tables
        // This will DELETE ALL EXISTING DATA.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BALANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_AREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_HISTORY); // Drop the new history table
        onCreate(db);
    }
}