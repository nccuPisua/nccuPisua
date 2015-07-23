package com.example.pisua.pisua;
import android.graphics.Point;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;


public class GetAngle {
	public static double calAngle(ParseGeoPoint sourcePoint,ParseGeoPoint targetPoint){
		double   res=(Math.atan2(targetPoint.getLongitude()-sourcePoint.getLongitude(),targetPoint.getLatitude()-sourcePoint.getLatitude())) / Math.PI * 180.0;
		return (res >= 0 && res <= 180) ? res : (res+=360);
	}
}
