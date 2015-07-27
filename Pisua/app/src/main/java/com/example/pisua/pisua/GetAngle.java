package com.example.pisua.pisua;

import android.util.Log;
import java.lang.Math;
import com.parse.ParseGeoPoint;


public class GetAngle {
	public static double calAngle(ParseGeoPoint sourcePoint,ParseGeoPoint targetPoint){
		double   res = (Math.atan2(targetPoint.getLongitude()-sourcePoint.getLongitude(),targetPoint.getLatitude()-sourcePoint.getLatitude())) / Math.PI * 180.0;
		Log.e("res",String.valueOf(res));
		return (res >= 0 && res <= 180) ? res : (res+=360);
	}
}
