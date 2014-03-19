package com.sundarsiva.primenumber;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundar on 3/16/14.
 */
public class PrimeNumbers {

    private static final String TAG = PrimeNumbers.class.getSimpleName();

    public static boolean isPrime(int p){
        if(p == 1) {
            return false;
        }
        for(int i = 2; i <= p/2; i++) {
            if(p % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> getNPrimeNumber(int startingFrom, int primesToFind) {
        Log.d(TAG, ">getNPrimeNumber: Finding "+primesToFind+" starting from "+startingFrom);
        List<Integer> primeNumbers = new ArrayList<Integer>();
        if(primesToFind == 0 ) {
            return primeNumbers;
        }
        int i = startingFrom+1;
        while (primeNumbers.size() < primesToFind) {
            if(isPrime(i)) {
                primeNumbers.add(i);
            }
            i++;
        }
        return primeNumbers;
    }



}
