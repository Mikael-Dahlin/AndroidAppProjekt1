package com.example.translateapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Class that extends SQLiteOpenHelper.
 */
public class DBHelper extends SQLiteOpenHelper {
    // Global variables.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "translations.db";

    // Static Values for the table.
    public static final String TABLE_NAME = "translation_table";
    public static final String COLUMN_NAME_TRANSLATION_ORIGINAL = "translation_original";
    public static final String COLUMN_NAME_TRANSLATION_TRANSLATED = "translation_translated";

    /**
     * Constructor
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Runs when a database is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create the table.
        String createQuery = "CREATE TABLE "
                + TABLE_NAME +
                " (row_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_TRANSLATION_ORIGINAL + " TEXT, "
                + COLUMN_NAME_TRANSLATION_TRANSLATED + " TEXT, "
                + "UNIQUE(" + COLUMN_NAME_TRANSLATION_ORIGINAL + "));";

        db.execSQL(createQuery);
    }

    /**
     * Is called when the Version number gets updated.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    /**
     * Method for adding data to the database.
     */
    public void addData(String originalString, String translatedString) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        // Add the data to contentValues.
        contentValues.put(COLUMN_NAME_TRANSLATION_ORIGINAL, originalString);
        contentValues.put(COLUMN_NAME_TRANSLATION_TRANSLATED, translatedString);

        // .insert returns the PK for the new row
        long row_id = db.insert(TABLE_NAME, null, contentValues);

        if (row_id == -1) {
            Log.d("TApp", "addData: That did not work!");
        } else {
            Log.d("TApp", "addData: Added to DB");
        }
    }

    /**
     * Method that adds all the data from the database to the provided arraylist. (without primary key)
     * ! Not currently in use but in case the database will be shown in the app at some point.
     */
    public ArrayList<DataBean> getData(ArrayList<DataBean> dataBeans) {
        // Local variables.
        SQLiteDatabase db = this.getWritableDatabase();

        String selectAll = "SELECT " + COLUMN_NAME_TRANSLATION_ORIGINAL + ", " + COLUMN_NAME_TRANSLATION_TRANSLATED + " FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectAll, null);

        // Add row data to the provided list of DataBeans.
        if (cursor.moveToFirst()) {

            while (cursor.moveToNext()) {
                String original = cursor.getString(0);
                String translated = cursor.getString(1);

                DataBean localBean = new DataBean();
                localBean.setOriginal(original);
                localBean.setTranslated(translated);
                dataBeans.add(localBean);
            }

            // Close cursor and database.
            cursor.close();
            db.close();
        }

        return dataBeans;
    }

    /**
     * Method that gets the data from one row based on the provided primary key.
     */
    public DataBean getRow(int row_id) {
        // Local variables.
        SQLiteDatabase db = this.getWritableDatabase();

        String selectRow = "SELECT " + COLUMN_NAME_TRANSLATION_ORIGINAL + ", " + COLUMN_NAME_TRANSLATION_TRANSLATED + " FROM " + TABLE_NAME + " WHERE row_id = " + row_id;

        Cursor cursor = db.rawQuery(selectRow, null);

        // Store row data in a DataBean.
        if (cursor.moveToFirst()) {
            DataBean localBean = new DataBean();
            localBean.setOriginal(cursor.getString(0));
            localBean.setTranslated(cursor.getString(1));

            // Close cursor and database.
            cursor.close();
            db.close();

            return localBean;
        }

        // Returns null if no row found.
        return null;
    }

    /**
     * Gets a random primary key from the database table.
     */
    public int randomRowID(){
        // Local variables.
        int randomNumber;

        SQLiteDatabase db = this.getWritableDatabase();

        String selectRowIDs = "SELECT row_id FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectRowIDs, null);

        ArrayList<Integer> rowIDs = new ArrayList<>();

        // Loop through all the ids and put them in the list.
        if (cursor.moveToFirst()) {

            while (cursor.moveToNext()) {
                rowIDs.add(cursor.getInt(0));
            }

            // Close cursor and database.
            cursor.close();
            db.close();
        }

        // Generate random number based on list size and return ID for that row.
        randomNumber = (int) (Math.random() * rowIDs.size());
        return rowIDs.get(randomNumber);
    }
}
