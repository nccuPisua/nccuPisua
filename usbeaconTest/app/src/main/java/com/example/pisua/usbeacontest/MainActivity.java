package com.example.pisua.usbeacontest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.util.ArrayList;
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

import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.USBeaconConnection.OnResponse;
import com.THLight.USBeacon.App.Lib.USBeaconData;
import com.THLight.USBeacon.App.Lib.USBeaconList;
import com.THLight.USBeacon.App.Lib.USBeaconServerInfo;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager.OniBeaconScan;
import com.THLight.Util.THLLog;

public class MainActivity extends Activity implements OnResponse, OniBeaconScan {

    private USBeaconConnection mBServer;
    private iBeaconScanManager miScaner;

    private TextToSpeech ttobj;
    private Timer timer;
    private ListView lv;
    private ArrayAdapter<String> vArrayData;
    private String[] items;

    //private int lv_position = 0;

    //private String[] vData = { "Ë™™Ë©±", "??ãÁ∂≤???", "??ãFacebook" };

//    private enum Dist
//    {
//        Immediate,
//        Near,
//        Far,
//        VeryFar,
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init_ListView();
        Init_Beacon();
        Init_Speak();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Start_Scan();
    }

    private void Start_Scan() {

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                miScaner.startScaniBeacon(1000);
            }
        },0,1000);
                // public void run() {
                // miScaner.startScaniBeacon(2000);
                // }
                // } , 0, 2000);
    }

    private void Init_ListView() {
        lv = (ListView) findViewById(R.id.listView);
        //lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        ArrayAdapter vArrayData = new ArrayAdapter(this,
//                android.R.layout.simple_list_item_single_choice, vData);
        items = new String[3];
        // Ë®≠Â?? ListView ??ÑÊé•?î∂?ô®, ??öÁÇ∫?Å∏??ÖÁ?Ñ‰?ÜÊ??
        vArrayData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        lv.setAdapter(vArrayData);
//        lv.setItemChecked(0, true);
//        lv.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                lv_position = position;
//            }
//
//        });
    }

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
        USBeaconList BList = mBServer.getUSBeaconList();
        for (final USBeaconData data : BList.getList()) {
            if (data.beaconUuid.equals(iBeaconData.beaconUuid) && iBeaconData.major == 1) {
                switch (iBeaconData.minor){
                    default:
                    case 1:
                        if (iBeaconData.calDistance() < 1.00){
                            ttobj.speak("hello " + data.name, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        items[0] = iBeaconData.beaconUuid+""+iBeaconData.calDistance()+"meters";
                        break;
                    case 2:
                        if (iBeaconData.calDistance() < 1.00){
                            ttobj.speak("hello " + data.name, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        items[1] = iBeaconData.beaconUuid+""+iBeaconData.calDistance()+"meters";
                    case 3:
                        if (iBeaconData.calDistance() < 1.00){
                            ttobj.speak("hello " + data.name, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        items[2] = iBeaconData.beaconUuid+""+iBeaconData.calDistance()+"meters";
                }
            }
        }
    }
/*    @Override
    public void onScaned(iBeaconData iBeaconData) {
        Log.e("test", "onScaned");
        USBeaconList BList = mBServer.getUSBeaconList();

        for (final USBeaconData data : BList.getList()) {
            if (data.beaconUuid.equals(iBeaconData.beaconUuid)) {

                MainActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        TextView edt_name = (TextView) findViewById(R.id.title);
                        edt_name.setText(data.name.toString());
                    }
                });

                switch (lv_position) {
                    default:
                    case 0:
                        ttobj.speak("hello " + data.name, TextToSpeech.QUEUE_FLUSH, null);
                        break;
                    case 1:
                        String strURL1 = "http://www.nccu.edu.tw";
                        Intent ie = new Intent(Intent.ACTION_VIEW,Uri.parse(strURL1));
                        startActivity(ie);
                        break;
                    case 2:
                        Intent intent = new Intent();
                        PackageManager manager = getPackageManager();
                        intent = manager.getLaunchIntentForPackage("com.facebook.katana");
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(intent);
                        break;
                }
            }
            break;
        }
        // timer.cancel();
    }*/

    public void Search(View view) {
        miScaner.stopScaniBeacon();
        miScaner.startScaniBeacon(2000);
    }

}