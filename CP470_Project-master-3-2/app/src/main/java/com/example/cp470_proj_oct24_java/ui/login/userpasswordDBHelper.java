package com.example.cp470_proj_oct24_java.ui.login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//database for the user name and password
public class userpasswordDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CP-470project.db";
    private static final int DATABASE_VERSION = 8; // Increment from 1 to 2
    private static final String TABLE_NAME = "users";
    private static final String COL_1 = "EMAIL";
    private static final String COL_2 = "PASSWORD";

    public userpasswordDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, EMAIL TEXT, PASSWORD TEXT)";
        db.execSQL(createTable);
        String savePhotosTable = "CREATE TABLE photos (ID INTEGER PRIMARY KEY AUTOINCREMENT, image BLOB NOT NULL)";
        db.execSQL(savePhotosTable);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS photos");
        onCreate(db);
    }

    //add user credentials to DB.
    public boolean insertLoginCredentials(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, email);
        contentValues.put(COL_2, password);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    //validates that the user exists in the DB.
    public boolean validate_user(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_1, COL_2},
                COL_1 + "=?", new String[]{email}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String user_password = cursor.getString(cursor.getColumnIndex(COL_2));
                cursor.close();
                return user_password.equals(password);
            }
            cursor.close();
        }
        return false;
    }
}
