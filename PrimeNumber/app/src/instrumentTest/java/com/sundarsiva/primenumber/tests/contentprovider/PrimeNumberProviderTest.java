package com.sundarsiva.primenumber.tests.contentprovider;

import android.content.ContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.sundarsiva.primenumber.contentprovider.PrimeNumberProvider;
import com.sundarsiva.primenumber.database.PrimeTable;

/**
 * Created by Sundar on 3/18/14.
 */
public class PrimeNumberProviderTest extends ProviderTestCase2<PrimeNumberProvider> {

    public PrimeNumberProviderTest() {
        super(PrimeNumberProvider.class, "com.sundarsiva.primenumber.contentprovider");
    }
    String[] mProjection;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProjection = new String[]{ PrimeTable.COLUMN_ID, PrimeTable.COLUMN_PRIME_NUMBER };
    }

    public void testQuery() {
        ContentProvider provider = getProvider();
        Uri getPrimes = Uri.parse(PrimeNumberProvider.CONTENT_URI + "/" + 3);
        Cursor cursor = provider.query(getPrimes, mProjection, null, null, null);
        assertNotNull(cursor);
        assertEquals(cursor.getColumnCount(), 2);
        assertEquals(cursor.getCount(), 3);
        assertEquals(cursor.getColumnIndex(PrimeTable.COLUMN_ID), 0);
        assertEquals(cursor.getColumnIndex(PrimeTable.COLUMN_PRIME_NUMBER), 1);

        try {
            getPrimes = Uri.parse(PrimeNumberProvider.CONTENT_URI + "/" + "INVALID");
            provider.query(getPrimes, mProjection, null, null, null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
