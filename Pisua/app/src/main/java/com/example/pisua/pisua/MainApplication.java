package com.example.pisua.pisua;

import android.app.Application;

import com.example.pisua.pisua.object.parse.Beacon_Data;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Willy on 2015/8/3.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Beacon_Data.class);
        Parse.initialize(this, ParseSettings.ApplicationId, ParseSettings.ClientId);
    }
}
