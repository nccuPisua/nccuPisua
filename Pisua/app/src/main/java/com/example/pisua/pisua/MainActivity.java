package com.example.pisua.pisua;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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


public class MainActivity extends Activity implements SensorEventListener, iBeaconScanManager.OniBeaconScan {

    private Handler mHandler;

    private final int SCAN_PERIOD = 1000;

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

    private ListView destinationListView;

    private ArrayAdapter destinationAdapter;

    private List<String> destinationList = new ArrayList<>();

    private List<Beacon_Data> beaconDataList = new ArrayList<>();

    private iBeaconData currentBeacon;

    private double[][] pathMatrix;

    private double INF = Double.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initPathMatrix();
    }

    private void init() {

        mHandler = new Handler();

        directionTextView = (TextView) findViewById(R.id.direction_text_view);
        currentBeaconTextView = (TextView) findViewById(R.id.current_beacon_text_view);

        destinationListView = (ListView) findViewById(R.id.destination_list_view);
        destinationListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //設定listview為單選，並把要顯示的文字丟入layout

        destinationAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, destinationList);
        destinationListView.setAdapter(destinationAdapter);
        destinationListView.setItemChecked(0, true);
        destinationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
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
    }

    private void scanBeacon(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    beaconScanManager.stopScaniBeacon();

                    scanBeacon(true);
                }
            }, SCAN_PERIOD);

            beaconScanManager.startScaniBeacon(1000);
        } else {
            beaconScanManager.stopScaniBeacon();
        }
    }

    private void initPathMatrix() {
        ParseQuery<Beacon_Data> beaconDataParseQuery = ParseQuery.getQuery(Beacon_Data.class);
        beaconDataParseQuery.whereExists("Minor");
        beaconDataParseQuery.addAscendingOrder("Minor");
        beaconDataParseQuery.findInBackground(new FindCallback<Beacon_Data>() {
            @Override
            public void done(List<Beacon_Data> results, ParseException e) {
                beaconDataList.addAll(results);
                //scanBeacon(true);

                int resultsSize = results.size();
                pathMatrix = new double[resultsSize][resultsSize];

                for (int i = 1; i <= resultsSize; i++) {
                    destinationList.add("我要去Beacon" + i);
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
                for(int i=0;i<18;i++){
                    Log.e("Matrix",String.valueOf(pathMatrix[0][i]));
                    Log.e("Beacon",beaconDataList.get(i).getParseGeoPoint("Location").toString());

                }


            }
        });
    }

    private double getLength(int x, int y) {
        ParseGeoPoint pointX = null, pointY = null;
        for (Beacon_Data beaconData : beaconDataList) {
            if (beaconData.getMinor().intValue()-1 == x) {
                pointX = beaconData.getParseGeoPoint("Location");
            }
            if (beaconData.getMinor().intValue()-1 == y) {
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
    public void onScaned(iBeaconData iBeaconData) {
        if (currentBeacon == null) {
            currentBeacon = iBeaconData;

            currentBeaconTextView.setText("現在位於Beacon" + currentBeacon.minor + " 距離您" + currentBeacon.calDistance() + "公尺");
            getNextDestination();
        } else {
            if (!iBeaconData.beaconUuid.equals(currentBeacon.beaconUuid) && iBeaconData.calDistance() < currentBeacon.calDistance()) {
                currentBeacon = iBeaconData;

                currentBeaconTextView.setText("現在位於Beacon" + currentBeacon.minor + " 距離您" + currentBeacon.calDistance() + "公尺");
                getNextDestination();
            }
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

        int destinationMinor = pathRouting(currentBeacon.minor-1, destinationListView.getCheckedItemPosition()).get(1)+1;

        for (Beacon_Data beaconData : beaconDataList) {
            if (beaconData.getMinor().intValue() == destinationMinor) {
                targetPoint = beaconData.getParseGeoPoint("Location");
                break;
            }
        }

        //書達，我們算出的angle在這，你修改provideClue()函式改變呈現結果就好;
        double angle = calAngle(sourcePoint, targetPoint);

        provideClue();
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

    //書達你要改的地方在這裡
    private void provideClue() {
        //beacon1 位在小lab中，beacon2位在小lab門口，beacon3位在小巫的lab門口
        switch (destinationListView.getCheckedItemPosition()) {
            default:
            case 0:
                switch (currentBeacon.minor) {
                    default:
                    case 1:
                        if (currentBeacon.calDistance() < 1.5) {
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        } else {
                            Toast.makeText(this, "您離目的地尚有" + currentBeacon.calDistance() + "公尺", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("There is" + currentBeacon.calDistance() + "left", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        }
                    case 2:
                        if (directionAngle >= 175 && directionAngle <= 195) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle >= 5 && directionAngle <= 174) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle < 5 || directionAngle > 195) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 3:
                        if (directionAngle >= 85 && directionAngle <= 105) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle <= 84 || directionAngle >= 275) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle > 105 && directionAngle < 275) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }

                break;
            case 1:
                switch (currentBeacon.minor) {
                    default:
                    case 1:
                        if (directionAngle >= 355 || directionAngle <= 15) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle >= 185 && directionAngle <= 354) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle > 15 && directionAngle < 185) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 2:
                        if (currentBeacon.calDistance() < 1.5) {
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        } else {
                            Toast.makeText(this, "您離目的地尚有" + currentBeacon.calDistance() + "公尺", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("There is" + currentBeacon.calDistance() + "left", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        }
                    case 3:
                        if (directionAngle >= 85 && directionAngle <= 105) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle <= 84 || directionAngle >= 275) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle > 105 && directionAngle < 275) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }

                break;
            case 2:
                switch (currentBeacon.minor) {
                    default:
                    case 1:
                        if (directionAngle >= 355 || directionAngle <= 15) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle >= 185 && directionAngle <= 354) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle > 15 && directionAngle < 185) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 2:
                        if (directionAngle >= 265 && directionAngle <= 285) {
                            Toast.makeText(this, "請往前走", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please go forward", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle >= 95 && directionAngle <= 264) {
                            Toast.makeText(this, "請往右轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn right", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (directionAngle > 285 || directionAngle < 95) {
                            Toast.makeText(this, "請往左轉", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("Please turn left", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 3:
                        if (currentBeacon.calDistance() < 1.5) {
                            Toast.makeText(this, "您已經到達目的地了", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("You are already here", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        } else {
                            Toast.makeText(this, "您離目的地尚有" + currentBeacon.calDistance() + "公尺", Toast.LENGTH_LONG).show();
                            textToSpeechObject.speak("There is" + currentBeacon.calDistance() + "left", TextToSpeech.QUEUE_FLUSH, null);
                            break;
                        }
                }

                break;
        }
    }
}
