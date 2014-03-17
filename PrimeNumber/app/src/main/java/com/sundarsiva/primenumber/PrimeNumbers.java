package com.sundarsiva.primenumber;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssivasub on 3/16/14.
 */
public class PrimeNumbers {

    private static final String TAG = PrimeNumbers.class.getSimpleName();

    public static List<Integer> getFirstNPrimeNumbers(int n) {
        Log.d(TAG, ">getFirstNPrimeNumbers");
        List<Integer> primeNumbers = new ArrayList<Integer>();

        int i = 0;
        while (primeNumbers.size() < n) {
            i++;
            if(isPrime(i)) {
                primeNumbers.add(i);
            }
        }

        return primeNumbers;

    }

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
}
