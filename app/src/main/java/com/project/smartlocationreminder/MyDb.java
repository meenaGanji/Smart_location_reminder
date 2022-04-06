package com.project.smartlocationreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


import com.google.android.gms.common.internal.service.Common;

import java.util.ArrayList;
import java.util.List;

public class MyDb extends SQLiteOpenHelper {

    static final int DB_VERSION = 1;
    static final String DB_NAME = "My_DB";
    static final String TABLE_REMINDER = "table_reminder";
    Context context;


    public MyDb(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_REMINDER + "("
                + "id_auto" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "location_id" + " TEXT, "
                + "title" + " TEXT, "
                + "date" + " TEXT, "
                + "time" + " TEXT, "
                + "latitude" + " DOUBLE,"
                + "longitude" + " DOUBLE " + ")";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDER);
        onCreate(db);

    }

    public List<Reminder> getReminderList() {
        SQLiteDatabase db = getWritableDatabase();
        List<Reminder> listAlarm = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String locationId = cursor.getString(1);
                String title = cursor.getString(2);
                String date = cursor.getString(3);
                String time = cursor.getString(4);
                double latitude = cursor.getDouble(5);
                double longitude = cursor.getDouble(6);


                listAlarm.add(new Reminder(locationId, title, date, time, latitude, longitude));
            } while (cursor.moveToNext());
        }

        return listAlarm;
    }


    public void deleteReminder(String locationID) {

        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(TABLE_REMINDER, "location_id" + " = ?",
                new String[]{String.valueOf(locationID)});
        if (delete == 1) {
            Toast.makeText(context, "Deleted Location", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Location Not Deleted", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }


    public void addReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("location_id", reminder.getLocation_id());
        values.put("title", reminder.getTitle());
        values.put("date", reminder.getDate());
        values.put("time", reminder.getTime());
        values.put("latitude", reminder.getLatitude());
        values.put("longitude", reminder.getLongitude());

        long insert = db.insert(TABLE_REMINDER, null, values);
        if (insert == -1) {
            Toast.makeText(context, "Reminder not Added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Reminder Added!", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }


    public void updateReminder(String locationId, Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("location_id", reminder.getLocation_id());
        values.put("title", reminder.getTitle());
        values.put("date", reminder.getDate());
        values.put("time", reminder.getTime());
        values.put("latitude", reminder.getLatitude());
        values.put("longitude", reminder.getLongitude());
        db.update(TABLE_REMINDER, values, "location_id = ?", new String[]{locationId});

    }


    public Reminder getCurrentReminder(String locationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE "
                + "location_id" + "= '" + locationId + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                return new Reminder(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6)
                );
            } while (cursor.moveToNext());
        }
        return null;
    }
}
