package com.ti.lprf;

import java.util.Date;

/** A simple Data Point class for the GraphModel/Display, 
 * just because it is that much faster than Pair */
public class DataPoint {
	float second;
	Date first;
	public DataPoint(Date f, float s){
		first = f;
		second = s;
	}
	public DataPoint(){
	
	}
}
