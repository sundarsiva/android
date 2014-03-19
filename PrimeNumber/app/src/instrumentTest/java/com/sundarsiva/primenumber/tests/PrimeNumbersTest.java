package com.sundarsiva.primenumber.tests;

import com.sundarsiva.primenumber.PrimeNumbers;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class PrimeNumbersTest extends TestCase {

    public void testGetNPrimeNumbers(){
        List<Integer> primeNumbers = PrimeNumbers.getNPrimeNumber(0, 0);
        assertNotNull(primeNumbers);
        assertEquals(primeNumbers.size(), 0);

        primeNumbers = PrimeNumbers.getNPrimeNumber(5, 4);
        assertEquals(primeNumbers.size(), 4);
        assertEquals(primeNumbers.get(0).intValue(), 7);
        assertEquals(primeNumbers.get(1).intValue(), 11);
        assertEquals(primeNumbers.get(2).intValue(), 13);
        assertEquals(primeNumbers.get(3).intValue(), 17);
    }


    public void testIsPrime() {
        assertFalse(PrimeNumbers.isPrime(1));
        assertTrue(PrimeNumbers.isPrime(2));
        assertTrue(PrimeNumbers.isPrime(3));
        assertFalse(PrimeNumbers.isPrime(4));
        assertTrue(PrimeNumbers.isPrime(59));
    }

}
