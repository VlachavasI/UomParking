package com.example.parkingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // For String.format and average duration
import java.util.concurrent.TimeUnit; // For average duration calculation

public class ParkingDatabase {

    private DatabaseHelper dbHelper;
    private static final String USER_ID = "single_user";

    // Define the cost constants here. These MUST match the constants in MainActivity.
    // Ensure these values are consistent across your app.
    private static final int COST_PER_30_MIN_PP = 5; // Example value
    private static final int COST_PER_HOUR_PP = 10; // Example value

    public ParkingDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // --- Active Parking Session Operations ---

    // REVISED METHOD: insertParkingSession to accept startTimeMillis
    public boolean insertParkingSession(String plate, String location, String duration, long startTimeMillis) {
        // First, check if the plate is already parked
        if (isPlateParked(plate)) {
            Log.e("ParkingDatabase", "Attempted to insert duplicate plate: " + plate);
            return false; // Plate already exists, cannot insert a new session
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLATE, plate);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_START_TIME, startTimeMillis); // Store the start time

        long result = db.insert(DatabaseHelper.TABLE_PARKING, null, values);
        db.close();
        return result != -1;
    }

    // MODIFIED METHOD: endParkingSession to move data to history
    public boolean endParkingSession(String plate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        // 1. Retrieve the active parking session details before deleting
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PARKING,
                new String[]{
                        DatabaseHelper.COLUMN_LOCATION,
                        DatabaseHelper.COLUMN_DURATION, // This is the string like "1 ώρα"
                        DatabaseHelper.COLUMN_START_TIME
                },
                DatabaseHelper.COLUMN_PLATE + " = ?",
                new String[]{plate},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String durationString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
            long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_TIME));
            long endTime = System.currentTimeMillis(); // The actual end time of the session

            // Get the fixed points paid based on the duration string
            int pointsPaid = getPointsPaidForDurationString(durationString);

            // 2. Insert into parking history table
            ContentValues historyValues = new ContentValues();
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_PLATE, plate);
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_LOCATION, location);
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_DURATION_STR, durationString); // Store original duration string
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_START_TIME, startTime);
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_END_TIME, endTime);
            historyValues.put(DatabaseHelper.HISTORY_COLUMN_POINTS_PAID, pointsPaid); // Store the fixed points cost

            long newRowId = db.insert(DatabaseHelper.TABLE_PARKING_HISTORY, null, historyValues);

            if (newRowId != -1) {
                // 3. Successfully inserted into history, now delete from active parking table
                int rowsAffected = db.delete(
                        DatabaseHelper.TABLE_PARKING,
                        DatabaseHelper.COLUMN_PLATE + " = ?",
                        new String[]{plate}
                );
                if (rowsAffected > 0) {
                    Log.d("ParkingDatabase", "Parking session ended and moved to history for plate: " + plate);
                    success = true;
                } else {
                    Log.e("ParkingDatabase", "Failed to delete active session for plate: " + plate + " AFTER moving to history. (Data inconsistency)");
                }
            } else {
                Log.e("ParkingDatabase", "Failed to insert parking session into history for plate: " + plate);
            }
        } else {
            Log.d("ParkingDatabase", "No active parking session found for plate: " + plate + " to end.");
        }

        cursor.close();
        db.close();
        return success;
    }

    // Helper method to get points paid based on duration string (used by endParkingSession)
    private int getPointsPaidForDurationString(String duration) {
        switch (duration) {
            case "30 λεπτά": return COST_PER_30_MIN_PP;
            case "1 ώρα": return COST_PER_HOUR_PP;
            case "2 ώρες": return COST_PER_HOUR_PP * 2;
            case "3 ώρες": return COST_PER_HOUR_PP * 3;
            default:
                Log.w("ParkingDatabase", "Unknown duration string: " + duration + ". Returning 0 points.");
                return 0; // Default or error case
        }
    }

    public List<ParkingSession> getAllParkingSessions() {
        List<ParkingSession> sessions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PARKING_HISTORY, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String plate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLATE));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_COLUMN_DURATION_STR));

            sessions.add(new ParkingSession(plate, location, duration));
        }

        cursor.close();
        db.close();
        return sessions;
    }


    // --- Park Points Balance Operations (minor refactoring to avoid redundant DB close) ---

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

    // Existing methods for Parking Areas (R6) - No changes needed here for statistics fix directly
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
                                "\nHours: " + openingHours + " | Cost: " + String.format(Locale.getDefault(), "%.2f", costPerPP) + " PP/hr"
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

    // --- METHODS FOR STATISTICS (R7) - MODIFIED TO QUERY TABLE_PARKING_HISTORY ---

    // Get total number of parking sessions (from history)
    public int getTotalParkingSessions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalSessions = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PARKING_HISTORY, null);
        if (cursor.moveToFirst()) {
            totalSessions = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return totalSessions;
    }

    // Get total revenue in Park Points (from history)
    public double getTotalRevenue() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalRevenue = 0.0;
        // Sum the points paid from the history table
        String query = "SELECT SUM(" + DatabaseHelper.HISTORY_COLUMN_POINTS_PAID + ") FROM " + DatabaseHelper.TABLE_PARKING_HISTORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            totalRevenue = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return totalRevenue;
    }

    // Get the most popular parking location (from history)
    public String getMostPopularLocation() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mostPopularLocation = "N/A";

        // Query to find the location with the highest count in history
        String query = "SELECT " + DatabaseHelper.HISTORY_COLUMN_LOCATION + ", COUNT(*) AS count " +
                "FROM " + DatabaseHelper.TABLE_PARKING_HISTORY +
                " GROUP BY " + DatabaseHelper.HISTORY_COLUMN_LOCATION +
                " ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            mostPopularLocation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_COLUMN_LOCATION));
            int maxCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            mostPopularLocation += " (" + maxCount + " sessions)";
        }
        cursor.close();
        db.close();
        return mostPopularLocation;
    }

    // Get average parking duration (from history)
    public String getAverageParkingDuration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double averageMillis = 0;

        // Calculate average duration in milliseconds from history table
        String query = "SELECT AVG(" + DatabaseHelper.HISTORY_COLUMN_END_TIME + " - " + DatabaseHelper.HISTORY_COLUMN_START_TIME + ") " +
                "FROM " + DatabaseHelper.TABLE_PARKING_HISTORY;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            averageMillis = cursor.getDouble(0); // AVG returns double
        }
        cursor.close();
        db.close();

        if (averageMillis == 0) {
            return "N/A";
        }

        // Convert milliseconds to a readable format (hours, minutes)
        long hours = TimeUnit.MILLISECONDS.toHours((long) averageMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) averageMillis) % 60;

        return String.format(Locale.getDefault(), "%d hours %d minutes", hours, minutes);
    }
}