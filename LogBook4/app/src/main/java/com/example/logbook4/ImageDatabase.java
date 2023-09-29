package com.example.logbook4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ImageDatabase extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    public static final String DATABASE_NAME = "Image.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_database";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_IMAGE = "image_link";
    public ImageDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_IMAGE + " TEXT" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public long insertImage(String name){
        ContentValues rowValues = new ContentValues();

        rowValues.put(COLUMN_IMAGE, name);
        return db.insertOrThrow(DATABASE_NAME, null, rowValues);
    }
    long addImageURL(String image){
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IMAGE, image);
        long result = this.db.insert(TABLE_NAME,null, cv);

        return result;
    }
    Cursor readImgUrl(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query,null);
        }
        return  cursor;
    }

}
