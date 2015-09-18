package com.example.pisua.pisua.activity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private static Handler mHandler;

    private boolean indoorMode = true;

    private static final int INDOOR_CHECK_PERIOD = 20 * 1000;
    private static final int SCAN_PERIOD = 5000;

    //顯示方位
    private TextView directionTextView;
    //顯示目前位於哪個beacon
    private TextView currentBeaconTextView;

    //目前方位角度
    private float directionAngle;

    //Speech Object
    private TextToSpeech textToSpeechObject;

    private static iBeaconScanManager beaconScanManager;

    private SensorManager sensorManager;

    private ViewPager destinationViewPager;
    private PagerTabStrip destinationViewPagerTab;

    private PagerAdapter destinationAdapter;

    private List<String> destinationList = new ArrayList<>();
    //DB抓下來的beacon資料存在此
    private List<Beacon_Data> beaconDataList = new ArrayList<>();
    private HashMap<String, Number> destinationMinorList = new HashMap<>();

    private iBeaconData currentBeacon;

    private double[][] pathMatrix;
    //無限大的double，僅是為了不用重複打Double.MAX_VALUE，方便用
    private double INF = Double.MAX_VALUE;

    private double angle = -1;

    private int scanedCount = 0;

    private int lastScanedCount = -1;

    private double resultAngle;

    private int alreadyhere_ogg, forward_ogg;

    //播放音效相關
    private SoundPool soundPool;
    private int[] soundList = new int[14];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initPathMatrix();

    }

    //釋放資源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    private void init() {

        soundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 5);

        alreadyhere_ogg = soundPool.load(MainActivity.this, R.raw.alreadyhere, 1);
        forward_ogg = soundPool.load(MainActivity.this, R.raw.forward, 1);

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

        directionTextView = (TextView) findViewById(R.id.direction_text_view);
        currentBeaconTextView = (TextView) findViewById(R.id.current_beacon_text_view);

        destinationViewPager = (ViewPager) findViewById(R.id.destination_view_pager);
        destinationViewPagerTab = (PagerTabStrip) findViewById(R.id.destination_view_pager_tab);

        destinationAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        destinationViewPager.setAdapter(destinationAdapter);
        destinationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                soundPool.autoPause();
                soundPool.play(soundList[position], 1.0F, 1.0F, 0, 0, 1.0F);
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

    public static void scanBeacon(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    beaconScanManager.stopScaniBeacon();
                    beaconScanManager.startScaniBeacon(1000);
                    scanBeacon(true);
                }
            }, SCAN_PERIOD);
        } else {
            beaconScanManager.stopScaniBeacon();
        }
    }

    private void indoorCheck() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanedCount > lastScanedCount) {
                    lastScanedCount = scanedCount;
                    if (!indoorMode) {
                        indoorMode = true;
                    }
                } else {
                    if (indoorMode) {
                        indoorMode = false;
                    }
                }

                indoorCheck();
            }
        }, INDOOR_CHECK_PERIOD);
    }

    private void initPathMatrix() {
        ParseQuery<Beacon_Data> beaconDataParseQuery = ParseQuery.getQuery(Beacon_Data.class);
        beaconDataParseQuery.whereExists("Minor");
        beaconDataParseQuery.addAscendingOrder("Minor");
        beaconDataParseQuery.findInBackground(new FindCallback<Beacon_Data>() {
            @Override
            public void done(List<Beacon_Data> results, ParseException e) {
                beaconDataList.addAll(results);
//                scanBeacon(true);
                indoorCheck();

                int resultsSize = results.size();
                pathMatrix = new double[resultsSize][resultsSize];

                for (int i = 0; i < resultsSize; i++) {
                    if (results.get(i).getString("Destination") != null) {
                        destinationList.add(results.get(i).getString("Destination"));
                        destinationMinorList.put(results.get(i).getString("Destination"), results.get(i).getMinor());
                    }
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

                Toast.makeText(MainActivity.this, "系統啟動，左右滑動選擇目的地並點擊", Toast.LENGTH_SHORT).show();
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
        directionTextView.setText(directionAngle + "度");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onScaned(final iBeaconData iBeaconData) {
        Log.e(MainApplication.PISUA_TAG, "onScaned");
        scanedCount++;

        runOnUiThread(new Runnable() {
            public void run() {
                currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor + "\n 距離您" + Math.round(iBeaconData.calDistance()) + "公尺");
            }
        });

        int minor = destinationMinorList.get(destinationList.get(destinationViewPager.getCurrentItem())).intValue();
        if (iBeaconData.minor == minor) {
            if (iBeaconData.minor != currentBeacon.minor) {
                currentBeacon = iBeaconData;
                provideClue(INF);
            }
            runOnUiThread(new Runnable() {
                public void run() {
//                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor + " 距離您" + iBeaconData.calDistance() + "公尺");
//                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor);
                }
            });
        } else if (iBeaconData.calDistance()<1) {
            currentBeacon = iBeaconData;

            runOnUiThread(new Runnable() {
                public void run() {
//                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor + " \n距離您" + iBeaconData.calDistance() + "公尺");
//                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor);
                }
            });
            getNextDestination();
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
//            textToSpeechObject.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);

            soundPool.autoPause();
            soundPool.play(alreadyhere_ogg, 1.0F, 1.0F, 0, 0, 1.0F);

        } else if (resultAngle > 10 && resultAngle <= 180) {
            int ang = (int) resultAngle;
            textToSpeechObject.speak("Please turn right " + ang + " degrees", TextToSpeech.QUEUE_FLUSH, null);
        } else if (resultAngle > 180 && resultAngle < 350) {
            int ang = (int) (360 - resultAngle);
            textToSpeechObject.speak("Please turn left " + ang + " degrees", TextToSpeech.QUEUE_FLUSH, null);
        } else if (resultAngle > 0 && resultAngle <= 10 || resultAngle >= 350) {
//            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
//            soundPool.autoPause();

            soundPool.play(forward_ogg, 1.0F, 1.0F, 0, 0, 1.0F);

        }
    }

}
