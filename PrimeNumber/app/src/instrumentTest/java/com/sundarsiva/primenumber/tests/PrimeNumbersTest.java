package com.sundarsiva.primenumber.tests;

import com.sundarsiva.primenumber.PrimeNumbers;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by ssivasub on 3/16/14.
 */
public class PrimeNumbersTest extends TestCase {

    public void testGetPrimeNumbers() {
        List<Integer> primeNumbers = PrimeNumbers.getFirstNPrimeNumbers(0);
        assertNotNull(primeNumbers);
        assertEquals(primeNumbers.size(), 0);

        primeNumbers = PrimeNumbers.getFirstNPrimeNumbers(4);
        assertEquals(primeNumbers.size(), 4);
        assertEquals(primeNumbers.get(0).intValue(), 2);
        assertEquals(primeNumbers.get(1).intValue(), 3);
        assertEquals(primeNumbers.get(2).intValue(), 5);
        assertEquals(primeNumbers.get(3).intValue(), 7);
    }


    public void testIsPrime() {
        assertFalse(PrimeNumbers.isPrime(1));
        assertTrue(PrimeNumbers.isPrime(2));
        assertTrue(PrimeNumbers.isPrime(3));
        assertFalse(PrimeNumbers.isPrime(4));
        assertTrue(PrimeNumbers.isPrime(59));
    }

}
