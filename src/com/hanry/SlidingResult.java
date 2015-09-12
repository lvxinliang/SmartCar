package com.hanry;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;

public class SlidingResult {
	private String direction = DIRECTION_HORIZONTAL;
	private static int precision = 10;
	private int value = 0;
	
	public static String DIRECTION_HORIZONTAL = "horizontal";
	public static  String DIRECTION_VERTICAL = "vertical";

	public static SlidingResult parseSlidingResult(Point touchPointFirst,
			Point touchPointSecond, DisplayMetrics displayMetrics) {
		SlidingResult slidingResult = new SlidingResult();
		
    	Log.i("handleViewTouch", "first x:" + touchPointFirst.x + ", y:" + touchPointFirst.y);
    	Log.i("handleViewTouch", "second x:" + touchPointSecond.x + ", y:" + touchPointSecond.y);
    	int diffX = touchPointSecond.x - touchPointFirst.x;
    	int diffY = touchPointSecond.y - touchPointFirst.y;
    	int absX = Math.abs(diffX);
    	int absY = Math.abs(diffY);

    	if(absX > absY){
    		slidingResult.direction = DIRECTION_HORIZONTAL;
    		if(absX >= precision){
    			slidingResult.value = diffX;
    		}
    	}else{
    		slidingResult.direction = DIRECTION_VERTICAL;
    		if(absY >= precision){
    			slidingResult.value = -diffY;
    		}
    	}
		return slidingResult;
	}

	public String getDirection() {
		return direction;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SlidingResult [direction=" + direction + ", value=" + value
				+ "]";
	}

}
