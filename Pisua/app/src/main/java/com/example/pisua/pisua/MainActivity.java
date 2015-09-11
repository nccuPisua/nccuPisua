package com.example.pisua.pisua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
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
import com.example.pisua.pisua.object.parse.Beacon_Data;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements SensorEventListener, iBeaconScanManager.OniBeaconScan {

    private Handler mHandler;

    private boolean indoorMode = true;

    private final int SCAN_PERIOD = 5000;
    private final int INDOOR_CHECK_PERIOD = 20 * 1000;

    //顯示方位
    private TextView directionTextView;
    //顯示目前位於哪個beacon
    private TextView currentBeaconTextView;

    //目前方位角度
    private float directionAngle;

    //Speech Object
    private TextToSpeech textToSpeechObject;

    private iBeaconScanManager beaconScanManager;

    private SensorManager sensorManager;

    private ViewPager destinationViewPager;

    private PagerAdapter destinationAdapter;

    private List<String> destinationList = new ArrayList<>();
    //DB抓下來的beacon資料存在此
    private List<Beacon_Data> beaconDataList = new ArrayList<>();

    private iBeaconData currentBeacon;

    private double[][] pathMatrix;
    //無限大的double，僅是為了不用重複打Double.MAX_VALUE，方便用
    private double INF = Double.MAX_VALUE;

    private double angle = -1;

    private int scanedCount = 0;

    private int lastScanedCount = -1;

    private double resultAngle;

    //播放音效相關
    private AudioManager mAudioManager;
    private SoundPool soundPool;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initPathMatrix();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        player.release();
        soundPool.release();
    }

    private void init() {

        mHandler = new Handler();

        directionTextView = (TextView) findViewById(R.id.direction_text_view);
        currentBeaconTextView = (TextView) findViewById(R.id.current_beacon_text_view);

        destinationViewPager = (ViewPager) findViewById(R.id.destination_view_pager);

        mAudioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
        soundPool = new SoundPool(12, AudioManager.STREAM_MUSIC, 5);
        player = new MediaPlayer();

        destinationAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        destinationViewPager.setAdapter(destinationAdapter);
        destinationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(MainActivity.this, destinationList.get(position), Toast.LENGTH_SHORT).show();

                String mainText = destinationList.get(position);
                try{
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource("http://translate.google.com/translate_tts?tl=zh-TW&q=" + mainText +"&ie=UTF-8");
                    player.prepare();
                    player.start();
                }catch (Exception e){
                }


//                int[] soundList = new int[18] ;
//                //soundList[0] = soundPool.load(MainActivity.this, R.raw.beacon1, 1);
//                soundList[1] = soundPool.load(MainActivity.this, R.raw.beacon2, 1);
//                //soundList[2] = soundPool.load(MainActivity.this, R.raw.beacon3, 1);
//                soundList[3] = soundPool.load(MainActivity.this, R.raw.beacon4, 1);
//                //soundList[4] = soundPool.load(MainActivity.this, R.raw.beacon5, 1);
//                soundList[5] = soundPool.load(MainActivity.this, R.raw.beacon6, 1);
//                //soundList[6] = soundPool.load(MainActivity.this, R.raw.beacon7, 1);
//                soundList[7] = soundPool.load(MainActivity.this, R.raw.beacon8, 1);
//                soundList[8] = soundPool.load(MainActivity.this, R.raw.beacon9, 1);
//                soundList[9] = soundPool.load(MainActivity.this, R.raw.beacon10, 1);
//                //soundList[10] = soundPool.load(MainActivity.this, R.raw.beacon11, 1);
//                //soundList[11] = soundPool.load(MainActivity.this, R.raw.beacon12, 1);
//                soundList[12] = soundPool.load(MainActivity.this, R.raw.beacon13, 1);
//                //soundList[13] = soundPool.load(MainActivity.this, R.raw.beacon14, 1);
//                //soundList[14] = soundPool.load(MainActivity.this, R.raw.beacon15, 1);
//                soundList[15] = soundPool.load(MainActivity.this, R.raw.beacon16, 1);
//                soundList[16] = soundPool.load(MainActivity.this, R.raw.beacon17, 1);
//                soundList[17] = soundPool.load(MainActivity.this, R.raw.beacon18, 1);
//                soundPool.play(soundList[position], 1.0F, 1.0F, 0, 0, 1.0F);
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

//        textToSpeechObject = new TextToSpeech(getApplicationContext(),
//                new TextToSpeech.OnInitListener() {
//                    @Override
//                    public void onInit(int status) {
//                        if (status != TextToSpeech.ERROR) {
//                            //語言設定為英文
//                            textToSpeechObject.setLanguage(Locale.US);
//                        }
//                    }
//                });
        currentBeacon = new iBeaconData();

    }

    private void scanBeacon(final boolean enable) {
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
                scanBeacon(true);
                indoorCheck();

                int resultsSize = results.size();
                pathMatrix = new double[resultsSize][resultsSize];

                for (int i = 0; i < resultsSize; i++) {
                    if(results.get(i).getString("Destination")!=null){
                        destinationList.add(results.get(i).getString("Destination"));
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
        Log.e(MainApplication.PISUA_TAG , "onScaned");
        scanedCount++;

        if(iBeaconData.minor-1 == destinationViewPager.getCurrentItem()){
            if(iBeaconData.minor != currentBeacon.minor){
                currentBeacon = iBeaconData;
                provideClue(INF);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor + " 距離您" + iBeaconData.calDistance() + "公尺");
                }
            });
        }else if(iBeaconData.rssi<90){
            currentBeacon = iBeaconData;

            runOnUiThread(new Runnable() {
                public void run() {
                    currentBeaconTextView.setText("現在位於Beacon" + iBeaconData.minor + " 距離您" + iBeaconData.calDistance() + "公尺");
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
        int destinationMinor = pathRouting(currentBeacon.minor - 1, destinationViewPager.getCurrentItem()).get(1) + 1;
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
        Log.e("angle","cal:"+cal);
        angle = 115-cal;
        Log.e("angle","angle:"+angle);
        double result = angle-directionAngle;
        Log.e("angle","result:"+result);
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
        Log.e("angle","a:"+a);
        if(a<0 && a>-360){
            resultAngle = a+360;
        }else if(a<0 && a<-360){
            resultAngle = a+720;
        }else {
            resultAngle = a;
        }
        if(resultAngle==INF){
            //textToSpeechObject.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
            int alertId = soundPool.load(this, R.raw.alreadyhere, 1);
            soundPool.play(alertId, 1.0F, 1.0F, 0, 0, 1.0F);
        }else if(resultAngle>10 && resultAngle<=180) {
            int ang = (int)resultAngle;
            //textToSpeechObject.speak("Please turn right "+ang+" degrees", TextToSpeech.QUEUE_FLUSH, null);
            String mainText = "請右轉"+ang+"度";
            try{
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource("http://translate.google.com/translate_tts?tl=zh-TW&q=" + mainText +"&ie=UTF-8");
                player.prepare();
                player.start();
            }catch (Exception e){
            }
        }else if(resultAngle>180 && resultAngle<350) {
            int ang = (int) (360 - resultAngle);
            //textToSpeechObject.speak("Please turn left " + ang + " degrees", TextToSpeech.QUEUE_FLUSH, null);
            String mainText = "請左轉"+ang+"度";
            try{
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource("http://translate.google.com/translate_tts?tl=zh-TW&q=" + mainText +"&ie=UTF-8");
                player.prepare();
                player.start();
            }catch (Exception e){
            }
        }else if(resultAngle>0 && resultAngle<=10  || resultAngle>=350){
            //textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
            int alertId = soundPool.load(this, R.raw.forward, 1);
            soundPool.play(alertId, 1.0F, 1.0F, 0, 0, 1.0F);
        }
    }

    private class SlidePagerAdapter extends FragmentStatePagerAdapter {

        public SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return new IntroFragment(destinationList.get(i));
        }

        @Override
        public int getCount() {
            return destinationList.size();
        }
    }

    public static class IntroFragment extends Fragment {

        private String destinationTitle;

        public IntroFragment() {
        }

        @SuppressLint("ValidFragment")
        public IntroFragment(String destinationTitle) {
            this.destinationTitle = destinationTitle;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_destination, container, false);
            TextView title = (TextView) rootView.findViewById(R.id.direction_text_view);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), destinationTitle, Toast.LENGTH_SHORT).show();
                }
            });

            title.setText(destinationTitle);
            return rootView;
        }
    }
}
