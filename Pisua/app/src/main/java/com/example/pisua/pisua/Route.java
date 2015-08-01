package com.example.pisua.pisua;

import android.util.Log;

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
    private List<Integer> result = new ArrayList<>();

    private static List<ParseObject> tableList;
    public static int tSize;
    public static double[][] matrix;
    private static ParseGeoPoint pointX;
    private static ParseGeoPoint pointY;
    private static double resultLength;

    //listview所要顯示的文字
    public static String[] vData;

    public Route(){

    }


    public void Init_Matrix(){
        ParseQuery<ParseObject> table = ParseQuery.getQuery("Beacon_Data");
        table.whereExists("Minor");
        table.addAscendingOrder("Minor");
        List<ParseObject> tempList = null;
        try {
            tempList = table.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("table", "Retrieved ");
        tableList = tempList;
        tSize = tableList.size();
        Log.e("tSize1", "" + tSize);

        Log.e("tSize2", "" + tSize);
        matrix = new double[tSize][tSize];
        for(int i = 0; i < tSize; i++){
            for(int j = 0; j < tSize;j++){
                matrix[i][j]=INF;
            }
        }
        for(int i = 0; i<tSize; i++){
            ParseObject tempObject = tableList.get(i);
            List<Integer> routeList = tempObject.getList("route");
            int tempSize = routeList.size();
//                        Log.e("route",routeList.get(0).toString());
//                        Log.e("tempSize",""+tempSize);
            for(int j = 0 ; j < tempSize; j++){
                int y = routeList.get(j)-1;
                matrix[i][y] = getLength(i,y);
            }
        }
        //Log.e("matrix", Double.toString(matrix[1][1]));

        vData = Init_vData();
        Log.e("vData",vData[1]);

    }
    private double getLength(int x,int y){
        ParseQuery<ParseObject> length = ParseQuery.getQuery("Beacon_Data");
        int[] minors = {x, y};
        length.whereContainedIn("Minor", Arrays.asList(minors));
        List<ParseObject> locateList = null;
        try {
            locateList = length.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        pointX = locateList.get(0).getParseGeoPoint("Location");
        pointY = locateList.get(1).getParseGeoPoint("Location");
        resultLength = pointX.distanceInKilometersTo(pointY);

        return resultLength;
    }

    public static List<Integer> routing(int begin, int end) {
        Route graph=new Route(tSize);
        graph.findCheapestPath(begin, end, matrix);
        List<Integer> list=graph.result;
        System.out.println(begin+" to "+end+",the cheapest path is:");
        System.out.println(list.toString());
        System.out.println(graph.dist[begin][end]);
        return list;
    }

    private void findCheapestPath(int begin,int end,double[][] matrix){
        floyd(matrix);
        result.add(begin);
        findPath(begin, end);
        result.add(end);
    }

    private void findPath(int i,int j){
        int k=path[i][j];
        if(k==-1)return;
        findPath(i, k);   //递归
        result.add(k);
        findPath(k, j);
    }
    private void floyd(double[][] matrix){
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

    public static String[] Init_vData(){
        ArrayList<String> tempData;
        tempData = new ArrayList<>();
        for(int i = 1; i<=tSize;i++){
            String s = "我要去Beacon"+i;
            tempData.add(s);
        }
        return tempData.toArray(new String[tSize]);
    }
}