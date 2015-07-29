package com.example.pisua.pisua;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Route {

    private static double INF=Double.MAX_VALUE;
    //dist[i][j]=INF<==>顶点i和j之间没有边
    private double[][] dist;
    //顶点i 到 j的最短路径长度，初值是i到j的边的权重
    private int[][] path;
    private List<Integer> result=new ArrayList<Integer>();

    private static List<ParseObject> tableList;
    public static double[][] matrix;
    private static ParseGeoPoint pointX;
    private static ParseGeoPoint pointY;

    public static void Init_Matrix(){
        ParseQuery<ParseObject> table = ParseQuery.getQuery("Beacon_Data");
        table.whereExists("Minor");
        table.addAscendingOrder("Minor");
        table.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> tempList, ParseException e) {
                if (e == null) {
                    Log.d("table", "Retrieved ");
                    tableList = tempList;
                } else {
                    Log.d("table", "Error: " + e.getMessage());
                }
            }
        });

        matrix = new double[tableList.size()][tableList.size()];
        for(int i = 0; i < tableList.size(); i++){
            for(int j = 0; j<tableList.size();j++){
                matrix[i][j]=INF;
            }
        }
        for(int i = 0; i<tableList.size(); i++){
            ParseObject tempObject = tableList.get(i);
            List<Integer> tempList = tempObject.getList("route");
            for(int j = 0 ; j < tempList.size(); j++){
                int y = tempList.get(j).intValue();
                matrix[i][y] = getLength(i,y);
            }
        }
    }

    private static double getLength(int x,int y){
        ParseQuery<ParseObject> length = ParseQuery.getQuery("Beacon_Data");
        int[] minors = {x, y};
        length.whereContainedIn("Minor", Arrays.asList(minors));
        length.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> tempList, ParseException e) {
                if (e == null) {
                    pointX = tempList.get(0).getParseGeoPoint("Location");
                    pointY = tempList.get(0).getParseGeoPoint("Location");
                } else {
                    Log.d("length", "Error: " + e.getMessage());
                }
            }
        });
        return pointX.distanceInKilometersTo(pointY);
    }

    public static List<Integer> routing(int begin, int end) {
        Route graph=new Route(tableList.size());
        graph.findCheapestPath(begin, end, matrix);
        List<Integer> list=graph.result;
        System.out.println(begin+" to "+end+",the cheapest path is:");
        System.out.println(list.toString());
        System.out.println(graph.dist[begin][end]);
        return list;
    }

    public void findCheapestPath(int begin,int end,double[][] matrix){
        floyd(matrix);
        result.add(begin);
        findPath(begin, end);
        result.add(end);
    }

    public void findPath(int i,int j){
        int k=path[i][j];
        if(k==-1)return;
        findPath(i, k);   //递归
        result.add(k);
        findPath(k, j);
    }
    public void floyd(double[][] matrix){
        double size=matrix.length;
        //initialize dist and path     
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                path[i][j]=-1;
                dist[i][j]=matrix[i][j];
            }
        }
        for(int k=0;k<size;k++){
            for(int i=0;i<size;i++){
                for(int j=0;j<size;j++){
                    if(dist[i][k]!=INF&&
                            dist[k][j]!=INF&&
                            dist[i][k]+dist[k][j]<dist[i][j]){
                        dist[i][j]=dist[i][k]+dist[k][j];
                        path[i][j]=k;
                    }
                }
            }
        }

    }

    public Route(int size){   //构造函数
        this.path=new int[size][size];
        this.dist=new double[size][size];
    }

    public static void Init_vData(){
        ArrayList<String> tempData = new ArrayList<String>();
        for(int i = 0; i<tableList.size();i++){
            tempData.add("我要去Beacon"+i);
        }
        MainActivity.vData = tempData.toArray(new String[tempData.size()]);
    }
}    