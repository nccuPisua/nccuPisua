package com.example.pisua.pisua.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.example.pisua.pisua.MainApplication;
import com.example.pisua.pisua.R;
import com.example.pisua.pisua.adapter.SlidePagerAdapter;
import com.example.pisua.pisua.object.parse.Beacon_Data;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements SensorEventListener, iBeaconScanManager.OniBeaconScan {

    private Handler mHandler;

//    private boolean indoorMode = true;

    private final String LiderotUUID = "58508BDB-AA6E-46FB-A327-BBEC5463BE78";

    private boolean beaconScanning = false;

    private boolean englishMode = true;

    private Runnable scanRunnable;

    private long stopNavigationTime;

//    private static final int INDOOR_CHECK_PERIOD = 20 * 1000;
    private static final int SCAN_PERIOD = 5000;

    //顯示方位
    private TextView directionTextView;
    //顯示目前位於哪個beacon
    private TextView currentBeaconTextView;

    private ProgressBar calculationProgressbar;
    private ProgressBar loadingProgressbar;

    private RelativeLayout contentLayout;
    private RelativeLayout navigationLayout;
    private TextView navigationCurrentLoactionTextView;
    private TextView navigationAngleTextView;
    private TextView navigationDistanceTextView;

    //目前方位角度
    private float directionAngle;

    //Speech Object
    private TextToSpeech textToSpeechObject;

    private iBeaconScanManager beaconScanManager;

    private SensorManager sensorManager;

    private ViewPager destinationViewPager;
    private PagerTabStrip destinationViewPagerTab;

    private SlidePagerAdapter destinationAdapter;

    private List<String> destinationList = new ArrayList<>();
    private List<String> engDestinationList = new ArrayList<>();
    //DB抓下來的beacon資料存在此
    private List<Beacon_Data> beaconDataList = new ArrayList<>();
    private HashMap<String, Number> destinationMinorList = new HashMap<>();

    private iBeaconData currentBeacon;

    private double[][] pathMatrix;
    //無限大的double，僅是為了不用重複打Double.MAX_VALUE，方便用
    private double INF = Double.MAX_VALUE;

    private double angle = -1;

//    private int scanedCount = 0;

//    private int lastScanedCount = -1;

    private double resultAngle;

    private int alreadyhere_ogg, forward_ogg, left_ogg, right_ogg, degrees_ogg, hundred_ogg, ten_ogg, again_ogg, already_ogg, caculating_ogg, stop_ogg, click_ogg;

    //播放音效相關
    private SoundPool soundPool;
    private int[] soundList = new int[14];
    private int[] numberlist = new int[10];

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if(isNetworkAvailable()){
            initPathMatrix();
        }else{
            networkErrorDialog();
        }
    }

    //釋放資源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    private void init() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {

            if(englishMode){
                Toast.makeText(MainActivity.this, "Sorry, Bluetooth is unsupported by this device", Toast.LENGTH_SHORT).show();

                textToSpeechObject.speak("Sorry, Bluetooth is unsupported by this device", TextToSpeech.QUEUE_FLUSH, null);

            }else{
                Toast.makeText(MainActivity.this, "很抱歉，您的裝置不支援藍芽，無法使用本APP", Toast.LENGTH_SHORT).show();
                textToSpeechObject.speak("Sorry, Bluetooth is unsupported by this device", TextToSpeech.QUEUE_FLUSH, null);
            }

        }else{
            if (!mBluetoothAdapter.isEnabled()) {
                startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }

        soundPool = new SoundPool(35, AudioManager.STREAM_MUSIC, 5);

        alreadyhere_ogg = soundPool.load(MainActivity.this, R.raw.alreadyhere, 1);
        forward_ogg = soundPool.load(MainActivity.this, R.raw.forward, 1);
        left_ogg = soundPool.load(MainActivity.this, R.raw.left, 1);
        right_ogg = soundPool.load(MainActivity.this, R.raw.right, 1);
        degrees_ogg = soundPool.load(MainActivity.this, R.raw.degrees, 1);
        hundred_ogg = soundPool.load(MainActivity.this, R.raw.hundred, 1);
        ten_ogg = soundPool.load(MainActivity.this, R.raw.ten, 1);
        again_ogg = soundPool.load(MainActivity.this, R.raw.again, 1);
        already_ogg = soundPool.load(MainActivity.this, R.raw.already, 1);
        caculating_ogg = soundPool.load(MainActivity.this, R.raw.caculating, 1);
        stop_ogg = soundPool.load(MainActivity.this, R.raw.stop, 1);
        click_ogg = soundPool.load(MainActivity.this, R.raw.click, 1);

        numberlist[0] = soundPool.load(MainActivity.this, R.raw.zero, 1);
        numberlist[1] = soundPool.load(MainActivity.this, R.raw.one, 1);
        numberlist[2] = soundPool.load(MainActivity.this, R.raw.two, 1);
        numberlist[3] = soundPool.load(MainActivity.this, R.raw.three, 1);
        numberlist[4] = soundPool.load(MainActivity.this, R.raw.four, 1);
        numberlist[5] = soundPool.load(MainActivity.this, R.raw.five, 1);
        numberlist[6] = soundPool.load(MainActivity.this, R.raw.six, 1);
        numberlist[7] = soundPool.load(MainActivity.this, R.raw.seven, 1);
        numberlist[8] = soundPool.load(MainActivity.this, R.raw.eight, 1);
        numberlist[9] = soundPool.load(MainActivity.this, R.raw.nine, 1);

        soundList[0] = soundPool.load(MainActivity.this, R.raw.beacon1, 1);
        soundList[1] = soundPool.load(MainActivity.this, R.raw.beacon2, 1);
        soundList[2] = soundPool.load(MainActivity.this, R.raw.beacon4, 1);
        soundList[3] = soundPool.load(MainActivity.this, R.raw.beacon5, 1);
        soundList[4] = soundPool.load(MainActivity.this, R.raw.beacon6, 1);
        soundList[5] = soundPool.load(MainActivity.this, R.raw.beacon7, 1);
        soundList[6] = soundPool.load(MainActivity.this, R.raw.beacon8, 1);
        soundList[7] = soundPool.load(MainActivity.this, R.raw.beacon9, 1);
        soundList[8] = soundPool.load(MainActivity.this, R.raw.beacon10, 1);
        soundList[9] = soundPool.load(MainActivity.this, R.raw.beacon11, 1);
        soundList[10] = soundPool.load(MainActivity.this, R.raw.beacon13, 1);
        soundList[11] = soundPool.load(MainActivity.this, R.raw.beacon16, 1);
        soundList[12] = soundPool.load(MainActivity.this, R.raw.beacon17, 1);
        soundList[13] = soundPool.load(MainActivity.this, R.raw.beacon18, 1);

        mHandler = new Handler();

        scanRunnable = new Runnable() {
            @Override
            public void run() {
                beaconScanManager.stopScaniBeacon();
                beaconScanManager.startScaniBeacon(1000);
                scanBeacon(true);
            }
        };

        directionTextView = (TextView) findViewById(R.id.direction_text_view);
        currentBeaconTextView = (TextView) findViewById(R.id.current_beacon_text_view);

        calculationProgressbar = (ProgressBar) findViewById(R.id.calculating_progressbar);
        loadingProgressbar = (ProgressBar) findViewById(R.id.loading_progressbar);

        contentLayout = (RelativeLayout) findViewById(R.id.content_layout);
        navigationLayout = (RelativeLayout) findViewById(R.id.navigation_hint_layout);
        navigationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopNavigation();
            }
        });

        navigationCurrentLoactionTextView = (TextView) findViewById(R.id.navigation_current_loaction_text_view);
        navigationAngleTextView = (TextView) findViewById(R.id.navigation_angle_text_view);
        navigationDistanceTextView = (TextView) findViewById(R.id.navigation_distance_text_view);

        destinationViewPager = (ViewPager) findViewById(R.id.destination_view_pager);
        destinationViewPagerTab = (PagerTabStrip) findViewById(R.id.destination_view_pager_tab);

        destinationAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        destinationViewPager.setAdapter(destinationAdapter);

        destinationViewPager.setOnTouchListener(new View.OnTouchListener() {
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    moved = false;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    moved = true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!moved) {
                        v.performClick();
                    }
                }
                return false;
            }
        });

        destinationViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationViewPager.setVisibility(View.GONE);
                scanBeacon(true);

//                navigationLayout.setVisibility(View.VISIBLE);


                if(englishMode){
                    Toast.makeText(MainActivity.this, "Path Calculating", Toast.LENGTH_SHORT).show();
                    textToSpeechObject.speak("Path Calculating", TextToSpeech.QUEUE_FLUSH, null);
                    calculationProgressbar.setVisibility(View.VISIBLE);
                    currentBeaconTextView.setText("Go to " + engDestinationList.get(destinationViewPager.getCurrentItem()));
                }else{
                    Toast.makeText(MainActivity.this, "計算路徑中", Toast.LENGTH_SHORT).show();
                    soundPool.autoPause();
                    soundPool.play(caculating_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    calculationProgressbar.setVisibility(View.VISIBLE);
                    currentBeaconTextView.setText("前往 " + destinationList.get(destinationViewPager.getCurrentItem()));
                }


            }
        });

        destinationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(englishMode){
                    textToSpeechObject.speak(engDestinationList.get(position), TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    soundPool.autoPause();
                    soundPool.play(soundList[position], 1.0F, 1.0F, 0, 0, 1.0F);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //如果有取到該手機的方位感測器，就註冊他。
        if (sensors.size() > 0) {
            //感應器註冊
            sensorManager.registerListener(this, (Sensor) sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        beaconScanManager = new iBeaconScanManager(this, this);
        textToSpeechObject = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            //語言設定為英文
                            textToSpeechObject.setLanguage(Locale.US);
                        }
                    }
                });
        currentBeacon = new iBeaconData();

    }

    private void scanBeacon(final boolean enable) {
        if (enable) {
            beaconScanning = true;

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(scanRunnable, SCAN_PERIOD);
        } else {
            mHandler.removeCallbacks(scanRunnable);
            beaconScanning = false;
            beaconScanManager.stopScaniBeacon();
        }
    }

    //威毅，現在是不是不需要這個函式了阿?
//    private void indoorCheck() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (scanedCount > lastScanedCount) {
//                    lastScanedCount = scanedCount;
//                    if (!indoorMode) {
//                        indoorMode = true;
//                    }
//                } else {
//                    if (indoorMode) {
//                        indoorMode = false;
//                    }
//                }
//
//                indoorCheck();
//            }
//        }, INDOOR_CHECK_PERIOD);
//    }

    private void initPathMatrix() {
        ParseQuery<Beacon_Data> beaconDataParseQuery = ParseQuery.getQuery(Beacon_Data.class);
        beaconDataParseQuery.whereExists("Minor");
        beaconDataParseQuery.addAscendingOrder("Minor");
        beaconDataParseQuery.findInBackground(new FindCallback<Beacon_Data>() {
            @Override
            public void done(List<Beacon_Data> results, ParseException e) {
                beaconDataList.addAll(results);
//                scanBeacon(true);
//                indoorCheck();

                int resultsSize = results.size();
                pathMatrix = new double[resultsSize][resultsSize];

                for (int i = 0; i < resultsSize; i++) {
                    if (results.get(i).getString("Destination") != null) {
                        destinationList.add(results.get(i).getString("Destination"));
                        engDestinationList.add(results.get(i).getString("EngDestination"));
                        destinationMinorList.put(results.get(i).getString("Destination"), results.get(i).getMinor());
                    }
                }
                if(englishMode){
                    destinationAdapter.setDestinationList(engDestinationList);
                }else{
                    destinationAdapter.setDestinationList(destinationList);
                }

                destinationAdapter.notifyDataSetChanged();
                for (int i = 0; i < resultsSize; i++) {
                    for (int j = 0; j < resultsSize; j++) {
                        pathMatrix[i][j] = INF;
                    }
                }
                for (int i = 0; i < resultsSize; i++) {
                    Beacon_Data beaconData = results.get(i);
                    List<Integer> routeList = beaconData.getRoute();
                    for (int j = 0; j < routeList.size(); j++) {
                        int y = routeList.get(j) - 1;
                        pathMatrix[i][y] = getLength(i, y);
                    }
                }

                loadingProgressbar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                if(englishMode){
                    Toast.makeText(MainActivity.this, "System is already, please slide to choose destination, and click to start guiding", Toast.LENGTH_SHORT).show();
                    textToSpeechObject.speak("System is already, please slide to choose destination, and click to start guiding", TextToSpeech.QUEUE_FLUSH, null);
                }else{
                    Toast.makeText(MainActivity.this, "系統啟動，左右滑動選擇目的地並點擊螢幕開始導航", Toast.LENGTH_SHORT).show();

                    //威毅，這邊分兩個檔案是因為之前不知道是因為太長還是怎樣，播到一半就會被切掉。 分成兩個檔案，用handler延遲後這邊目前沒問題
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            soundPool.play(already_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                            try {
                                Thread.sleep(5000);
                                soundPool.play(click_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }, 2000);
                }




//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        soundPool.play(click_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
//                    }
//                }, 5000);

            }
        });
    }

    private double getLength(int x, int y) {
        ParseGeoPoint pointX = null, pointY = null;
        for (Beacon_Data beaconData : beaconDataList) {
            if (beaconData.getMinor().intValue() - 1 == x) {
                pointX = beaconData.getParseGeoPoint("Location");
            }
            if (beaconData.getMinor().intValue() - 1 == y) {
                pointY = beaconData.getParseGeoPoint("Location");
            }
        }

        if (pointX != null && pointY != null) {
            return pointX.distanceInKilometersTo(pointY);
        } else {
            return -1;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        directionAngle = values[0];

        if(englishMode){
            directionTextView.setText(directionAngle + "degrees");
        }else{
            directionTextView.setText(directionAngle + "度");
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onScaned(final iBeaconData iBeaconData) {
        Log.e(MainApplication.PISUA_TAG, "onScaned");
        if (beaconScanning && iBeaconData.beaconUuid.equals(LiderotUUID)) {
//            scanedCount++;

            runOnUiThread(new Runnable() {
                public void run() {
                    if(englishMode){
                        navigationCurrentLoactionTextView.setText("You're at " + engDestinationList.get(iBeaconData.minor-1) + "\n Still have " + Math.round(iBeaconData.calDistance()) + " meters far");
                    }else{
                        navigationCurrentLoactionTextView.setText("現在位於 " + destinationList.get(iBeaconData.minor-1) + "\n 距離您 " + Math.round(iBeaconData.calDistance()) + " 公尺");
                    }

                }
            });

            int minor = destinationMinorList.get(destinationList.get(destinationViewPager.getCurrentItem())).intValue();
            if (iBeaconData.minor == minor) {
                if (iBeaconData.minor != currentBeacon.minor) {
                    currentBeacon = iBeaconData;
                    provideClue(INF);
                }
            } else if (iBeaconData.calDistance() < 100) {
                currentBeacon = iBeaconData;
                getNextDestination();
            }

            calculationProgressbar.setVisibility(View.GONE);
            navigationLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getNextDestination() {
        ParseGeoPoint sourcePoint = null, targetPoint = null;
        for (Beacon_Data beaconData : beaconDataList) {
            if (beaconData.getMinor().intValue() == currentBeacon.minor) {
                sourcePoint = beaconData.getParseGeoPoint("Location");
                break;
            }
        }
        int destinationMinor = pathRouting(currentBeacon.minor - 1, destinationMinorList.get(destinationList.get(destinationViewPager.getCurrentItem())).intValue() - 1).get(1) + 1;
        Log.e(MainApplication.PISUA_TAG, "dMinor : " + String.valueOf(destinationMinor));
        for (Beacon_Data beaconData : beaconDataList) {
            if (beaconData.getMinor().intValue() == destinationMinor) {
                targetPoint = beaconData.getParseGeoPoint("Location");
                break;
            }
        }
        Log.e(MainApplication.PISUA_TAG, "tPoint : " + targetPoint.toString());

        Log.e("tPoint", targetPoint.toString());
        Log.e(MainApplication.PISUA_TAG, "source point:" + sourcePoint.toString());
        double cal = calAngle(sourcePoint, targetPoint);
        Log.e("angle", "cal:" + cal);
        angle = 115 - cal;
        Log.e("angle", "angle:" + angle);
        double result = angle - directionAngle;
        Log.e("angle", "result:" + result);
        provideClue(result);
    }

    private List<Integer> pathRouting(int begin, int end) {
        List<Integer> result = new ArrayList<>();
        //dist[i][j]=INF<==>頂點I和J之間沒有邊
        double[][] dist = new double[beaconDataList.size()][beaconDataList.size()];
        //頂點I到J的最短路徑長度，初值是I到J邊的權重
        int[][] path = new int[beaconDataList.size()][beaconDataList.size()];

        //initialize dist and path
        for (int i = 0; i < pathMatrix.length; i++) {
            for (int j = 0; j < pathMatrix.length; j++) {
                path[i][j] = -1;
                dist[i][j] = pathMatrix[i][j];
            }
        }
        for (int k = 0; k < pathMatrix.length; k++) {
            for (int i = 0; i < pathMatrix.length; i++) {
                for (int j = 0; j < pathMatrix.length; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        path[i][j] = k;
                    }
                }
            }
        }

        result.add(begin);
        findPath(path, result, begin, end);
        result.add(end);
        Log.e(MainApplication.PISUA_TAG, "path:" + result);
        return result;
    }


    private void findPath(int[][] path, List<Integer> result, int i, int j) {
        int k = path[i][j];
        if (k == -1) return;
        findPath(path, result, i, k);
        result.add(k);
        findPath(path, result, k, j);
    }

    private double calAngle(ParseGeoPoint sourcePoint, ParseGeoPoint targetPoint) {
        double res = (Math.atan2(targetPoint.getLongitude() - sourcePoint.getLongitude(), targetPoint.getLatitude() - sourcePoint.getLatitude())) / Math.PI * 180.0;
        return (res >= 0 && res <= 180) ? res : (res += 360);
    }

    private void provideClue(double a) {
        Log.e("angle", "a:" + a);
        if (a < 0 && a > -360) {
            resultAngle = a + 360;
        } else if (a < 0 && a < -360) {
            resultAngle = a + 720;
        } else {
            resultAngle = a;
        }
        if (resultAngle == INF) {
            soundPool.autoPause();
            soundPool.play(alreadyhere_ogg, 1.0F, 1.0F, 0, 0, 1.0F);

        } else if (resultAngle > 10 && resultAngle <= 180) {
            final int ang = (int) resultAngle;

            if(englishMode){
                textToSpeechObject.speak("Please turn right " + ang + " degrees", TextToSpeech.QUEUE_FLUSH, null);
                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("Please turn right " + ang + " degrees");
                    }
                });
            }else{
                soundPool.autoPause();
                soundPool.play(right_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                //威毅，目前speakAngle都播不出聲音，只有"請往右轉"
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        speakAngle(ang);
                    }
                }, 1000);

                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("請往右轉 " + ang + "度");
                    }
                });
            }


        } else if (resultAngle > 180 && resultAngle < 350) {
            final int ang = (int) (360 - resultAngle);
            if(englishMode){
                textToSpeechObject.speak("Please turn left " + ang + " degrees", TextToSpeech.QUEUE_FLUSH, null);
                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("Please turn left " + ang + " degrees");
                    }
                });
            }else{
                soundPool.autoPause();
                soundPool.play(left_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                //威毅，目前speakAngle都播不出聲音，只有"請往左轉"
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        speakAngle(ang);
                    }
                }, 1000);

                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("請往左轉 " + ang + "度");
                    }
                });
            }



        } else if (resultAngle > 0 && resultAngle <= 10 || resultAngle >= 350) {

            if(englishMode){
                textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("Please go forward");
                    }
                });
            }else{
                //這裡會一直重複講
                soundPool.autoPause();
                soundPool.play(forward_ogg, 1.0F, 1.0F, 0, 0, 1.0F);

                runOnUiThread(new Runnable() {
                    public void run() {
                        navigationAngleTextView.setText("請往前走");
                    }
                });
            }

        }
    }

    private void StopNavigation() {
        if ((System.currentTimeMillis() - stopNavigationTime) > 5000) {

            if(englishMode){
                Toast.makeText(MainActivity.this, "Click again to stop guiding", Toast.LENGTH_SHORT).show();
                textToSpeechObject.speak("click again to stop guiding", TextToSpeech.QUEUE_FLUSH, null);
            }else{
                //點擊間隔大於兩秒才停止
                Toast.makeText(MainActivity.this, "再按一次停止導航", Toast.LENGTH_SHORT).show();
                soundPool.autoPause();
                soundPool.play(again_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
            }
            stopNavigationTime = System.currentTimeMillis();

        } else {
            //連續點擊兩次，確定停止導航

            navigationLayout.setVisibility(View.GONE);
            calculationProgressbar.setVisibility(View.GONE);
            destinationViewPager.setVisibility(View.VISIBLE);
            scanBeacon(false);

            if(englishMode){
                Toast.makeText(MainActivity.this, "Stop Guiding", Toast.LENGTH_SHORT).show();
                textToSpeechObject.speak("Stop Guiding", TextToSpeech.QUEUE_FLUSH, null);
                currentBeaconTextView.setText("Click to select destination");
            }else{
                Toast.makeText(MainActivity.this, "停止導航", Toast.LENGTH_SHORT).show();
                soundPool.autoPause();
                soundPool.play(stop_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                currentBeaconTextView.setText("點擊選擇目的地");
            }

        }
    }

    //以此函式將算出的int角度轉換成相對應的數字語音並念出來
    private void speakAngle(int ang) {
        //威毅，目前主要問題在這個函式，由於要能播出角度，會需要依序播很多次聲音，但目前用handler的方法很蠢而且也放不出來@@
        if (ang > 9 && ang < 100) {
            final int tens = ang / 10;
            final int units = ang % 10;

            if (units == 0) {
                soundPool.play(numberlist[tens], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(ten_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
            } else {
                soundPool.play(numberlist[tens], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(ten_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[units], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1500);
            }
        } else {
            final int hundreds = ang / 100;
            final int tens = (ang / 10) % 10;
            final int units = ang % 10;

            if (tens == 0 && units == 0) {
                soundPool.play(numberlist[hundreds], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(hundred_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
            } else if (units == 0 && tens != 0) {
                soundPool.play(numberlist[hundreds], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(hundred_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[tens], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(ten_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 2000);
            } else if (tens == 0 && units != 0) {
                soundPool.play(numberlist[hundreds], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(hundred_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[tens], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[units], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 2000);
            } else {
                soundPool.play(numberlist[hundreds], 1.0F, 1.0F, 0, 0, 1.0F);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(hundred_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[tens], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1000);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(ten_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 1500);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(numberlist[units], 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 2000);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        soundPool.play(degrees_ogg, 1.0F, 1.0F, 0, 0, 1.0F);
                    }
                }, 2500);
            }
        }
    }

    private void networkErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        if(englishMode){
            textToSpeechObject.speak("Failed to load data, Please check your Internet connection and reload", TextToSpeech.QUEUE_FLUSH, null);
            builder.setTitle("Failed to load data")
                    .setMessage("Please check your Internet connection and click to reload")
                    .setCancelable(false)
                    .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isNetworkAvailable()) {
                                initPathMatrix();
                            } else {
                                networkErrorDialog();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }else{
            textToSpeechObject.speak("Failed to load data, Please check your Internet connection and reload", TextToSpeech.QUEUE_FLUSH, null);
            builder.setTitle("讀取資料失敗")
                    .setMessage("請開啟網路後，點擊確定重新載入")
                    .setCancelable(false)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isNetworkAvailable()) {
                                initPathMatrix();
                            } else {
                                networkErrorDialog();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }
}
