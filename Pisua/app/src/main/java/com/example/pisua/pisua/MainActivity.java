package com.example.pisua.pisua;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager.OniBeaconScan;


import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import com.example.pisua.pisua.GetAngle;



public class MainActivity extends Activity implements  OniBeaconScan, SensorEventListener {
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

    //計數器所需暫存
    private int tempMinor;
    private double tempDistance = 5;
    private int tempCounter = 0;
    private int firstMinor;
    private double firstDistance;

    private ListView lv;
    //紀錄listview哪個選項被選擇
    private int lv_position = 0;
    //listview所要顯示的文字
    private String[] vData = { "我要去beacon1", "我要去beacon2", "我要去beacon3" };

    //layout中的button
    private Button button;
    private Boolean ifSpeek = true;
    private int counter = 0;

    private ParseGeoPoint sourcePoint;
    private ParseGeoPoint targetPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //指定layout
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        miScaner = new iBeaconScanManager(this, this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "eEkyWoV3SAmRZcZKw8mU0AbiTQXneGTUXA8x4pRs", "o2qgJI1CKvSmZGVzy8QbVEoBMrkXTztodZdljLFM");

        final ParseQuery<ParseObject> queryBData = ParseQuery.getQuery("Beacon_Data");
        queryBData.whereEqualTo("Minor", lv_position);
        queryBData.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d("Location", "The getFirst request failed.");
                } else {
                    Log.d("Location", "Retrieved the object.");
                    targetPoint = object.getParseGeoPoint("Location");
                    Log.e("targetPoint",targetPoint.toString());
                }
            }
        });

        initView();
        Init_ListView();
        Init_Speak();
        Init_Button();
        setSensor();
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        //開始掃描
        Start_Scan();
    }
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        miScaner.stopScaniBeacon();
        timer.cancel();
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
                if (lv_position + 1 == tempMinor || counter == 6) {
                    ifSpeek = true;
                } else {
                    counter = counter + 1;
                }
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
                ifSpeek=true;
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

    //初始化按鈕
    private void Init_Button(){
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new Button.OnClickListener(){

            @Override

            public void onClick(View v) {
                ifSpeek=true;
                tempCounter=3;
            }

        });
    }

    @Override
    public void onScaned(iBeaconData iBeaconData) {
        if(tempCounter ==5){
            if(tempMinor==0){
                tempMinor = firstMinor;
                tempDistance = firstDistance;
            }
            beacon.setText("現在位於Beacon" + tempMinor + " 距離您" + tempDistance + "公尺");
            if(ifSpeek){
                provideClue();
            }
            //歸零計數器、暫存Minor、暫存距離
            tempCounter = 0;
            tempMinor=0;
            tempDistance=5;
            ifSpeek=false;
            counter = 0;
    } else {
            minorDifferer(iBeaconData);
            tempCounter = tempCounter +1;
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
        if(b.minor != tempMinor && b.calDistance() < tempDistance) {
                tempMinor = b.minor;
                tempDistance = Math.rint(b.calDistance()*100)/100;
        }else if(tempCounter==0){
            firstMinor = b.minor;
            firstDistance = Math.rint(b.calDistance()*100)/100;
        }else if(b.calDistance()<firstDistance){
            firstMinor = b.minor;
            firstDistance = Math.rint(b.calDistance()*100)/100;
        }
    }

    private void provideClue(){
            //beacon1 位在小lab中，beacon2位在小lab門口，beacon3位在小巫的lab門口
            switch (lv_position) {
                default:
                case 0:
                    switch (tempMinor) {
                        default:
                        case 1:
                            if(tempDistance<1.5) {
                                Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                                ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }else {
                                Toast.makeText(this, "您離目的地尚有"+tempDistance+"公尺", Toast.LENGTH_LONG).show();
                                ttobj.speak("There is"+tempDistance+"left", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }
                        case 2:
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
                        case 2:
                            if(tempDistance<1.5) {
                                Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                                ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }else {
                                Toast.makeText(this, "您離目的地尚有"+tempDistance+"公尺", Toast.LENGTH_LONG).show();
                                ttobj.speak("There is"+tempDistance+"left", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }
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
                            if(tempDistance<1.5) {
                                Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                                ttobj.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }else {
                                Toast.makeText(this, "您離目的地尚有"+tempDistance+"公尺", Toast.LENGTH_LONG).show();
                                ttobj.speak("There is"+tempDistance+"left", TextToSpeech.QUEUE_FLUSH, null);
                                break;
                            }
                    }

                    break;
            }

    }

}
