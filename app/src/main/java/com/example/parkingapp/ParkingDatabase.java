package com.example.parkingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ParkingDatabase {

    private DatabaseHelper dbHelper;
    private static final String USER_ID = "single_user";

    public ParkingDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // REVISED METHOD: Insert Parking Session
    public boolean insertParkingSession(String plate, String location, String duration) {
        // First, check if the plate is already parked
        if (isPlateParked(plate)) {
            return false; // Plate already exists, cannot insert a new session
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLATE, plate);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);

        long result = db.insert(DatabaseHelper.TABLE_PARKING, null, values);
        db.close(); // Close the database after the insert operation
        return result != -1; // Return true if insertion was successful
    }

    // New private helper method to get park points WITHOUT closing the database
    // This is used internally by other methods that already have an open DB connection
    private int getParkPointsInternal(SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_PARK_POINTS},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARK_POINTS));
        }
        cursor.close();
        // IMPORTANT: DO NOT CLOSE DB HERE. The caller method will handle closing it.
        return points;
    }

    // Public Get Park Points method (original behaviour for external calls)
    public int getParkPoints() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int points = getParkPointsInternal(db); // Use the internal helper
        db.close(); // Close the database here as this is a standalone public call
        return points;
    }

    // Existing method: Add Park Points - REVISED to use getParkPointsInternal
    public void addParkPoints(int points) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get writable DB
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, USER_ID);

        // Check if user balance exists
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BALANCE,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{USER_ID}, null, null, null);

        if (cursor.getCount() > 0) {
            // Update existing balance
            int currentPoints = getParkPointsInternal(db); // Use internal helper, pass current db
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, currentPoints + points);
            db.update(DatabaseHelper.TABLE_USER_BALANCE, values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{USER_ID});
        } else {
            // Insert new balance
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, points); // Set points for new user
            db.insert(DatabaseHelper.TABLE_USER_BALANCE, null, values);
        }
        cursor.close(); // Close the cursor associated with the query
        db.close(); // Close the database after all operations in this method are complete
    }

    // Existing methods for Parking Areas (R6)
    public boolean addParkingArea(String locationName, String openingHours, double costPerPP) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AREA_LOCATION_NAME, locationName);
        values.put(DatabaseHelper.COLUMN_AREA_OPENING_HOURS, openingHours);
        values.put(DatabaseHelper.COLUMN_AREA_COST_PER_PP, costPerPP);

        long result = db.insert(DatabaseHelper.TABLE_PARKING_AREAS, null, values);
        db.close();
        return result != -1;
    }

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
                DatabaseHelper.COLUMN_AREA_LOCATION_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_ID));
                String locationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_LOCATION_NAME));
                String openingHours = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_OPENING_HOURS));
                double costPerPP = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_COST_PER_PP));

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

    // --- METHODS FOR STATISTICS (R7) ---

    // Helper method to convert duration string to minutes
    private int convertDurationToMinutes(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }
        durationStr = durationStr.toLowerCase();
        if (durationStr.contains("30 λεπτά")) {
            return 30;
        } else if (durationStr.contains("1 ώρα")) {
            return 60;
        } else if (durationStr.contains("2 ώρες")) {
            return 120;
        } else if (durationStr.contains("3 ώρες")) {
            return 180;
        }
        return 0; // Default or error case
    }

    // Get total number of parking sessions
    public int getTotalParkingSessions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalSessions = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PARKING, null);
        if (cursor.moveToFirst()) {
            totalSessions = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return totalSessions;
    }

    // Get total revenue in Park Points
    public double getTotalRevenue() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalRevenue = 0.0;

        String query = "SELECT " +
                DatabaseHelper.TABLE_PARKING + "." + DatabaseHelper.COLUMN_DURATION + ", " +
                DatabaseHelper.TABLE_PARKING_AREAS + "." + DatabaseHelper.COLUMN_AREA_COST_PER_PP +
                " FROM " + DatabaseHelper.TABLE_PARKING +
                " INNER JOIN " + DatabaseHelper.TABLE_PARKING_AREAS +
                " ON " + DatabaseHelper.TABLE_PARKING + "." + DatabaseHelper.COLUMN_LOCATION + " = " +
                DatabaseHelper.TABLE_PARKING_AREAS + "." + DatabaseHelper.COLUMN_AREA_LOCATION_NAME;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String durationStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
                double costPerPP = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA_COST_PER_PP));

                int durationMinutes = convertDurationToMinutes(durationStr);
                totalRevenue += (durationMinutes / 60.0) * costPerPP;

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return totalRevenue;
    }

    // Get the most popular parking location
    public String getMostPopularLocation() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mostPopularLocation = "N/A";
        int maxCount = 0;

        String query = "SELECT " + DatabaseHelper.COLUMN_LOCATION + ", COUNT(*) AS count " +
                "FROM " + DatabaseHelper.TABLE_PARKING +
                " GROUP BY " + DatabaseHelper.COLUMN_LOCATION +
                " ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            mostPopularLocation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            maxCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            mostPopularLocation += " (" + maxCount + " sessions)";
        }
        cursor.close();
        db.close();
        return mostPopularLocation;
    }

    // Get average parking duration in minutes (or formatted string)
    public String getAverageParkingDuration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalDurationMinutes = 0;
        int totalSessions = 0;

        String query = "SELECT " + DatabaseHelper.COLUMN_DURATION +
                " FROM " + DatabaseHelper.TABLE_PARKING;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String durationStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
                totalDurationMinutes += convertDurationToMinutes(durationStr);
                totalSessions++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (totalSessions > 0) {
            double averageMinutes = totalDurationMinutes / totalSessions;
            long hours = (long) (averageMinutes / 60);
            long minutes = (long) (averageMinutes % 60);
            return String.format("%d hours %d minutes", hours, minutes);
        } else {
            return "N/A";
        }
    }
    public boolean isPlateParked(String plate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PARKING,
                new String[]{DatabaseHelper.COLUMN_PLATE},
                DatabaseHelper.COLUMN_PLATE + " = ?",
                new String[]{plate},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close(); // Close the database after the read operation
        return exists;
    }

    // METHOD: Deduct Park Points - REVISED to use getParkPointsInternal and manage DB closing
    public boolean deductParkPoints(int pointsToDeduct) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get writable DB
        ContentValues values = new ContentValues();

        int currentPoints = getParkPointsInternal(db); // Use internal helper, pass current db

        if (currentPoints >= pointsToDeduct) {
            values.put(DatabaseHelper.COLUMN_PARK_POINTS, currentPoints - pointsToDeduct);
            int rowsAffected = db.update(DatabaseHelper.TABLE_USER_BALANCE, values,
                    DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{USER_ID});
            db.close(); // Close the database after all operations in this method
            return rowsAffected > 0;
        } else {
            db.close(); // Close the database even if deduction fails, to release the connection
            return false;
        }
    }
}