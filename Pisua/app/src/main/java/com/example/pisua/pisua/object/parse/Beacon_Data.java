package com.example.pisua.pisua.object.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Willy on 2015/8/3.
 */

@ParseClassName("Beacon_Data")
public class Beacon_Data extends ParseObject {

    public Number getMinor() {
        return getNumber("Minor");
    }

    public void setMinor(Number minor) {
        put("Minor", minor);
    }

    public List<Integer> getRoute(){
        return getList("route");
    }

}
