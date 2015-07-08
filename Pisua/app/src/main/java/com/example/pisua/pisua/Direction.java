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

        //��sensor���ǽT�ʧ��ܮɷ|����
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("Sensor_test", "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
        }

        //sensor�y���ܰʮɰ���
        @Override
        public void onSensorChanged(SensorEvent event) {

            xViewA =  event.values[0]; //"���: " + event.values[0];
            yViewA = event.values[1];//"����ɱר���: " + event.values[1];
            zViewA = event.values[2];//"�u�ʨ���(����½��): " + event.values[2];
            /*
             * value[0]�GZ�b�ASensor���A�_�G0�B�F�G90�B�n�G180�B��G270
             * value[1]�GX�b�ASensor�ɱ׫�(��_��������AX�b���ȷ|�ܰ�)
             * value[2]�GY�b�ASensor�u�ʨ���(����½��)
             */
        }

    };


}
