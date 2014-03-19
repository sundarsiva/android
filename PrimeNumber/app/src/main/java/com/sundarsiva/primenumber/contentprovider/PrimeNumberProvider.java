package com.sundarsiva.primenumber.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.sundarsiva.primenumber.PrimeNumbers;
import com.sundarsiva.primenumber.database.PrimeDatabaseHelper;
import com.sundarsiva.primenumber.database.PrimeTable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Sundar on 3/17/14.
 */
public class PrimeNumberProvider extends ContentProvider{
    private static final String TAG = PrimeNumberProvider.class.getSimpleName();
    private PrimeDatabaseHelper database;

    // used for the UriMacher
    private static final int PRIMES = 10;
    private static final int NTH_PRIME = 20;

    private static final String AUTHORITY = "com.sundarsiva.primenumber.contentprovider";

    private static final String BASE_PATH = "primes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/primes";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/prime";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PRIMES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NTH_PRIME);
    }

    @Override
    public boolean onCreate() {
        database = new PrimeDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        checkColumns(projection);

        queryBuilder.setTables(PrimeTable.TABLE_PRIMES);

        int uriType = sURIMatcher.match(uri);
        Log.d(TAG, "uri: "+uri);
        Log.d(TAG, "uriType: "+uriType);
        int inputN = 0;
        switch (uriType) {
            case PRIMES:
                break;
            case NTH_PRIME:
                try {
                    inputN = Integer.parseInt(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot find prime numbers for input "+uri.getLastPathSegment());
                }
                queryBuilder.appendWhere(PrimeTable.COLUMN_ID + "<=" + inputN);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Log.d(TAG, "Query: "+queryBuilder.toString());

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        /* if we couldn't find as many primes as requested, calculate them,
        * insert them into the data base, update the cursor with a new query,
        * and then return the update cursor back.*/

        int nPrimes = cursor.getCount();
        if(nPrimes < inputN) {
            cursor.moveToLast();
            int lastPrime = 0;
            if(nPrimes > 0 && cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER) > 0) {
                lastPrime = cursor.getInt(cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER));
            }

            int numPrimesToFind = inputN - nPrimes;
            List<Integer> primes = PrimeNumbers.getNPrimeNumber(lastPrime, numPrimesToFind);
            int numFoundPrimes = primes.size();
            ContentValues[] bulkValues = new ContentValues[numFoundPrimes];
            for(int i = 0; i < numFoundPrimes; i++) {
                ContentValues values = new ContentValues();
                values.put(PrimeTable.COLUMN_PRIME_NUMBER, primes.get(i));
                bulkValues[i] = values;
            }
            bulkInsert(PrimeNumberProvider.CONTENT_URI, bulkValues);
            cursor = queryBuilder.query(db, projection, selection,
                    selectionArgs, null, null, sortOrder);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case PRIMES:
                id = sqlDB.insert(PrimeTable.TABLE_PRIMES, null, values);
                Log.d(TAG, "created column with id: "+id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private void checkColumns(String[] projection) {
        String[] available = { PrimeTable.COLUMN_PRIME_NUMBER,
                PrimeTable.COLUMN_ID };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
