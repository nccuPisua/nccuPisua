package com.test3.willy.myapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements iBeaconScanManager.OniBeaconScan, SensorEventListener {

    private USBeaconConnection mBServer;
    private iBeaconScanManager miScaner;

    private TextView direction;
    private int directionNum;

    private Timer timer;
    private SensorManager sensorManager;

    private ListView listview;
    private String[] listText = { "我要去beacon1", "我要去beacon2", "我要去beacon3" };
    private int listPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        miScaner = new iBeaconScanManager(this, this);

        initView();
        initListView();
        setSensor();
        startScan();
//        miScaner.startScaniBeacon(1000);
    }


    private void initView(){
        direction = (TextView)findViewById(R.id.direction_text_view);
    }

    private void initListView() {
        listview = (ListView) findViewById(R.id.listView);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter vArrayData = new ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, listText);


        listview.setAdapter(vArrayData);
        listview.setItemChecked(0, true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                listPosition = position;
            }

        });
    }

    private void startScan() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                miScaner.stopScaniBeacon();
                miScaner.startScaniBeacon(2000);
            }
        }, 0, 2000);
    }

    @Override
    public void onScaned(iBeaconData iBeaconData) {
        switch (listPosition) {
            default:
            case 0:
                    switch (iBeaconData.minor){
                    default:
                    case 1:
                        Toast.makeText(this , "您已經到達目的地了"  , Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this , "請往前走"  , Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this , "請往前走"  , Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                }

                break;
            case 1:
                switch (iBeaconData.minor){
                    default:
                    case 1:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this , "請往前走"  , Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        Toast.makeText(this , "您已經到達目的地了"  , Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this , "請往前走"  , Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                }

                break;
            case 2:
                switch (iBeaconData.minor){
                    default:
                    case 1:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this , "請往前走"  , Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        if(80<=directionNum&&directionNum<=100){
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                        }else if(0<=directionNum&&directionNum<=79||270<=directionNum&&directionNum<=360){
                            Toast.makeText(this , "請往右轉"  , Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this , "請往左轉"  , Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        Toast.makeText(this , "您已經到達目的地了"  , Toast.LENGTH_LONG).show();
                        break;
                }

                break;
        }
    }

    protected void setSensor() {
        List sensors = new ArrayList();
        sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //如果有取到該手機的方位感測器，就註冊他。
        if (sensors.size() > 0) {
            //感應器註冊
            sensorManager.registerListener(this,(Sensor)sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        direction.setText(values[0] + "度");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
