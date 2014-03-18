package com.sundarsiva.primenumber.tests;

import com.sundarsiva.primenumber.Util;

import junit.framework.TestCase;

/**
 * Created by Sundar on 3/16/14.
 */
public class UtilTest extends TestCase {

    public void testGetNumberSuffix() {
        assertEquals(Util.addNumberSuffix(1), "1st");
        assertEquals(Util.addNumberSuffix(2), "2nd");
        assertEquals(Util.addNumberSuffix(3), "3rd");
        assertEquals(Util.addNumberSuffix(4), "4th");
        assertEquals(Util.addNumberSuffix(11), "11th");
        assertEquals(Util.addNumberSuffix(12), "12th");
        assertEquals(Util.addNumberSuffix(13), "13th");
        assertEquals(Util.addNumberSuffix(14), "14th");
        assertEquals(Util.addNumberSuffix(21), "21st");
        assertEquals(Util.addNumberSuffix(22), "22nd");
    }

}
