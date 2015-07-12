package com.example.pisua.pisua;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.app.Activity;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.USBeaconConnection.OnResponse;
import com.THLight.USBeacon.App.Lib.USBeaconData;
import com.THLight.USBeacon.App.Lib.USBeaconList;
import com.THLight.USBeacon.App.Lib.USBeaconServerInfo;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager.OniBeaconScan;
import com.THLight.Util.THLLog;



public class MainActivity extends Activity implements  OniBeaconScan, SensorEventListener {
    private USBeaconConnection mBServer;
    private iBeaconScanManager miScaner;

    //顯示方位的textview
    private TextView direction;
    //儲存目前方位角度的變數
    private float directionNum;

    private SensorManager sensorManager;

    private TextToSpeech ttobj;
    private Timer timer;

    // 顯示當前位於哪個beacon的textview
    private TextView beacon;
    private int tempMinor;
    private double tempDistance = 99999999;
    private int counter = 0;

    private ListView lv;
    //紀錄listview哪個選項被選擇
    private int lv_position = 0;
    //listview所要顯示的文字
    private String[] vData = { "我要去beacon1", "我要去beacon2", "我要去beacon3" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //指定layout
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        miScaner = new iBeaconScanManager(this, this);

        initView();
        Init_ListView();
        Init_Speak();
        setSensor();
        //開始掃描
        Start_Scan();

    }

    //初始化textview
    private void initView() {
        direction = (TextView) findViewById(R.id.direction);
        beacon = (TextView) findViewById(R.id.beacon);
    }

    //開始掃描，設定計時器，每半秒掃描一次
    private void Start_Scan() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                miScaner.stopScaniBeacon();
                miScaner.startScaniBeacon(500);
            }
        }, 0, 500);
    }

    //初始化ListView
    private void Init_ListView() {
        lv = (ListView) findViewById(R.id.listView);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //設定listview為單選，並把要顯示的文字丟入layout
        ArrayAdapter vArrayData = new ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, vData);
        lv.setAdapter(vArrayData);
        lv.setItemChecked(0, true);
        //當listview被點選時
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //將被點選的攔位儲存
                lv_position = position;
            }

        });
    }

    //初始化語音
    private void Init_Speak() {
        ttobj = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            //語言設定為英文
                            ttobj.setLanguage(Locale.US);
                        }
                    }
                });
    }

    @Override
    public void onScaned(iBeaconData iBeaconData) {
        if(counter==3){
            beacon.setText("現在位於Beacon" + tempMinor+" 距離您"+tempDistance+"公尺");
            //beacon1 位在小lab中，beacon2位在小lab門口，beacon3位在小巫的lab門口
            switch (lv_position) {
                default:
                case 0:
                    switch (tempMinor) {
                        default:
                        case 1:
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        case 2:
                            if (directionNum>=355 || directionNum <= 15) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (directionNum>= 185 && directionNum <= 354) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum>15 && directionNum<185){
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                        case 3:
                            if (directionNum>=85  && directionNum <= 105) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (directionNum <= 84 || directionNum>= 275) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum>105&&directionNum<275) {
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                    }

                    break;
                case 1:
                    switch (tempMinor) {
                        default:
                        case 1:
                            if (directionNum>=175 && directionNum <= 195) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (directionNum>=5 && directionNum <= 174) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum<5||directionNum>195){
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                        case 2:
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        case 3:
                            if (directionNum>=85 && directionNum <= 105) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if ( directionNum <= 84 || directionNum>=275 ) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum>105&&directionNum<275){
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                    }

                    break;
                case 2:
                    switch (tempMinor) {
                        default:
                        case 1:
                            if (directionNum>=175 && directionNum <= 195) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (directionNum>=5 && directionNum <= 174) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum<5||directionNum>195){
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                        case 2:
                            if (directionNum>=265 && directionNum <= 285) {
                                Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                            } else if ( directionNum>=95 && directionNum <= 264) {
                                Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                            } else if(directionNum>285||directionNum<95){
                                Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                                ttobj.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            break;
                        case 3:
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                    }

                    break;
            }
            //歸零計數器、暫存Minor、暫存距離
            counter = 0;
            tempDistance = 99999999;
            tempMinor = 0;
        } else {
            minorDifferer(iBeaconData);
            counter = counter+1;
        }
    }

    //設定與註冊感應器
    protected void setSensor() {
        List sensors = new ArrayList();
        sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //如果有取到該手機的方位感測器，就註冊他。
        if (sensors.size() > 0) {
            //感應器註冊
            sensorManager.registerListener(this,(Sensor)sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //當方向感測器變動時，將當下數據儲存於values陣列中，將水平方位(values[0])儲存於directionNum變數中，並顯示於layout
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        directionNum=values[0];
        direction.setText(directionNum + "度");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //當感應到不同beacon時，比較距離遠近並儲存較近的一個
    public void minorDifferer(iBeaconData b){
        if(b.minor != tempMinor && b.calDistance() < tempDistance){
            tempMinor = b.minor;
            tempDistance = b.calDistance();
        }
    }

}
