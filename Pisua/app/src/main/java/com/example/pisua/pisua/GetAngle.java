package com.example.pisua.pisua;
import android.graphics.Point;


public class GetAngle {
	public static double calAngle(Point sourcePoint,Point targetPoint){
		double   res=(Math.atan2(targetPoint.y-sourcePoint.y,targetPoint.x-sourcePoint.x)) / Math.PI * 180.0;
		return (res >= 0 && res <= 180) ? res : (res+=360);
	}
}
