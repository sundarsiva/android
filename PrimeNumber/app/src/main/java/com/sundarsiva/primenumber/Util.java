package com.sundarsiva.primenumber;

/**
 * Created by Sundar on 3/16/14.
 */
public class Util {
    public static String addNumberSuffix(int num) {
        String suffix = "th";
        if (num >= 11 && num <= 13) {
            return num + suffix;
        }
        switch (num % 10) {
            case 1:
                suffix = "st";
                break;
            case 2:
                suffix = "nd";
                break;
            case 3:
                suffix = "rd";
                break;
            default:
                suffix = "th";
                break;
        }
        return num + suffix;
    }

}
