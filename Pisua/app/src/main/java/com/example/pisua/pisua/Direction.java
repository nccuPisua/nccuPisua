package com.example.pisua.pisua;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.List;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
/**
 * Created by Pisua on 2015/7/8.
 */
public class Direction {
    float xViewA = 0;
    float yViewA = 0;
    float zViewA = 0;

    public SensorEventListener acc_listener = new SensorEventListener() {

        //當sensor的準確性改變時會執行
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("Sensor_test", "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
        }

        //sensor座標變動時執行
        @Override
        public void onSensorChanged(SensorEvent event) {

            xViewA =  event.values[0]; //"方位: " + event.values[0];
            yViewA = event.values[1];//"手機傾斜角度: " + event.values[1];
            zViewA = event.values[2];//"滾動角度(側邊翻轉): " + event.values[2];
            /*
             * value[0]：Z軸，Sensor方位，北：0、東：90、南：180、西：270
             * value[1]：X軸，Sensor傾斜度(抬起手機頂部，X軸的值會變動)
             * value[2]：Y軸，Sensor滾動角度(側邊翻轉)
             */
        }

    };


}
