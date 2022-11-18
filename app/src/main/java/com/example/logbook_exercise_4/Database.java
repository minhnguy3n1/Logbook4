package com.example.logbook_exercise_4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    private final Context context;
    public static final String DATABASE_NAME = "Logbook_4.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_database";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_IMAGE = "image_location";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_IMAGE + " TEXT);";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

    public void addImage(String location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IMAGE, location);
        long results = db.insert(TABLE_NAME, null, cv);
        if(results == -1){
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,"Added successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor getImage() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String query = "SELECT * FROM " + TABLE_NAME;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


}
