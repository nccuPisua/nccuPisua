package com.example.pisua.pisua;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.content.Context;

import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.USBeaconConnection.OnResponse;
import com.THLight.USBeacon.App.Lib.USBeaconData;
import com.THLight.USBeacon.App.Lib.USBeaconList;
import com.THLight.USBeacon.App.Lib.USBeaconServerInfo;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager.OniBeaconScan;
import com.THLight.Util.THLLog;

import com.example.pisua.pisua.Direction;



public class MainActivity extends Activity implements OnResponse, OniBeaconScan {
    private USBeaconConnection mBServer;
    private iBeaconScanManager miScaner;
    private TextToSpeech ttobj;
    private Timer timer;
    private ListView lv;
    private int lv_position = 0;
    private String[] vData = { "�ڭn�hbeacon1", "�ڭn�hbeacon2", "�ڭn�hbeacon3" };
    private enum Dist
    {
        Immediate,
        Near,
        Far,
        VeryFar,
    }

    //���V�һ��ܼ�
    SensorManager sm = null;
    Direction direction = new Direction();
    private TextView TV;
    private Timer timerForDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init_Beacon();
        Init_ListView();
//        Init_Speak();
        //���osensor service
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        //���UListener(SensorEventListener,sensor���P������,�A�X��sensor�ļ��ܤƲv)
        sm.registerListener(direction.acc_listener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
        //���լO�_�����
        DirectionTimer();
    }

    //�����ե�timer
    public void DirectionTimer(){
        //�qlayout��ID���wtextview
        TV = (TextView) findViewById(R.id.test);
        //��l��timer
         timerForDirection = new Timer();
        //�]�wTimer���u�@
         timerForDirection.schedule(new TimerTask() {
         public void run() {
             runOnUiThread(new Runnable() {
                 public void run()
                 {
                     TV.setText(Float.toString(direction.xViewA));
                 }
             });
//             Toast.makeText(this , direction.xViewA  , Toast.LENGTH_LONG).show();
//             TV.setText(Float.toString(direction.xViewA));
//             TV.setText("123");
         }
         } , 0, 500);
    }

//    private void Start_Scan() {
//        // timer = new Timer();
//        // timer.schedule(new TimerTask() {
//        // public void run() {
//        // miScaner.startScaniBeacon(2000);
//        // }
//        // } , 0, 2000);
//    }

    //��l��Beacon
    private void Init_Beacon() {
        miScaner = new iBeaconScanManager(this, this);
        USBeaconServerInfo serverInfo = new USBeaconServerInfo();
        serverInfo.serverUrl = "http://www.usbeacon.com.tw/api/func";
        serverInfo.queryUuid = UUID
                .fromString("5BA9B9C3-1448-44AF-9180-86D16527E6CD");

        String STORE_PATH = Environment.getExternalStorageDirectory()
                .toString() + "/USBeaconSamples/";

        File file = new File(STORE_PATH);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this,
                        "Create folder(" + STORE_PATH + ") failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        serverInfo.downloadPath = STORE_PATH;

        mBServer = new USBeaconConnection();
        mBServer.setServerInfo(serverInfo, this);
        mBServer.checkForUpdates();
    }

    //��l��ListView
    private void Init_ListView() {
        lv = (ListView) findViewById(R.id.listView);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter vArrayData = new ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, vData);


        lv.setAdapter(vArrayData);
        lv.setItemChecked(0, true);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                lv_position = position;
            }

        });
    }

    //��l�ƻy��
    private void Init_Speak() {
        ttobj = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            ttobj.setLanguage(Locale.US);
                        }
                    }
                });
    }

    @Override
    public void onResponse(int arg0) {
        switch (arg0) {
            case USBeaconConnection.MSG_NETWORK_NOT_AVAILABLE:
                break;

            case USBeaconConnection.MSG_HAS_UPDATE:
                mBServer.downloadBeaconListFile();
                break;

            case USBeaconConnection.MSG_HAS_NO_UPDATE:
                break;

            case USBeaconConnection.MSG_DOWNLOAD_FINISHED:
                break;

            case USBeaconConnection.MSG_DOWNLOAD_FAILED:
                break;

            case USBeaconConnection.MSG_DATA_UPDATE_FINISHED: {
                USBeaconList BList = mBServer.getUSBeaconList();

                if (null == BList) {
                    THLLog.d("debug", "update failed.");
                } else if (BList.getList().isEmpty()) {
                    THLLog.d("debug", "this account doesn't contain any devices.");
                } else {
                    THLLog.d("debug", "update finished");

                    // for(USBeaconData data : BList.getList())
                    // {
                    // THLLog.d("debug", "Name("+ data.name+ "), Ver("+ data.major+
                    // "."+ data.minor+ ")");
                    // }
                }
            }
            break;
            case USBeaconConnection.MSG_DATA_UPDATE_FAILED:
                break;
        }
    }

    @Override
    public void onScaned(iBeaconData iBeaconData) {
        Log.e("test", "onScaned");
        USBeaconList BList = mBServer.getUSBeaconList();

        for (final USBeaconData data : BList.getList()) {
            if (data.beaconUuid.equals(iBeaconData.beaconUuid)) {

//                MainActivity.this.runOnUiThread(new Runnable(){
//                    @Override
//                    public void run() {
//                        TextView edt_name = (TextView) findViewById(R.id.edt_name);
//                        edt_name.setText(data.name.toString());
//                    }
//                });

                switch (lv_position) {
                    default:
                    case 0:
                        switch (iBeaconData.minor){
                            default:
                            case 0:
                                Toast.makeText(this , "�z�w�g��F�ت��a�F"  , Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 2:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                        }

                        break;
                    case 1:
                        switch (iBeaconData.minor){
                            default:
                            case 0:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 1:
                                Toast.makeText(this , "�z�w�g��F�ت��a�F"  , Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                        }

                        break;
                    case 2:
                        switch (iBeaconData.minor){
                            default:
                            case 0:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 1:
                                if(80<=direction.xViewA&&direction.xViewA<=100){
                                    Toast.makeText(this , "�Щ��e��"  , Toast.LENGTH_LONG).show();
                                }else if(0<=direction.xViewA&&direction.xViewA<=79||270<=direction.xViewA&&direction.xViewA<=360){
                                    Toast.makeText(this , "�Щ��k��"  , Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(this , "�Щ�����"  , Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 2:
                                Toast.makeText(this , "�z�w�g��F�ت��a�F"  , Toast.LENGTH_LONG).show();
                                break;
                        }

                        break;
                }
            }
            break;
        }
        // timer.cancel();
    }

    public void Search(View view) {
        miScaner.stopScaniBeacon();
        miScaner.startScaniBeacon(2000);
    }


}
