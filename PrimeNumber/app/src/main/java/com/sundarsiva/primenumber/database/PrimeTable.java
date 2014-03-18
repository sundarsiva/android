package com.sundarsiva.primenumber.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Sundar on 3/17/14.
 */
public class PrimeTable {
    private static final String TAG = PrimeTable.class.getSimpleName();
    public static final String TABLE_PRIMES = "primes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRIME_NUMBER = "prime_number";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PRIMES
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PRIME_NUMBER + " integer not null "
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        Log.d(TAG, "creating db: "+DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIMES);
        onCreate(database);
    }
}
