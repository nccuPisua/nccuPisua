package com.willy.mmchocolate;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity {

    //an array that stores the pixel values
    private int intArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        intArray = new int[bitmap.getWidth()*bitmap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int totalCount = 0;
        int yellowCount = 0;
        //replace the red pixels with yellow ones
        for (int i=0; i < intArray.length; i++)
        {
            if(intArray[i] == 0){

            }else{
                totalCount++;
                int r = Color.red(intArray[i]);
                int g = Color.green(intArray[i]);
                int b = Color.blue(intArray[i]);

                if(r>200 && g>200 && b<100){
                   yellowCount++;
                }
            }
        }

        //Initialize the bitmap, with the replaced color
        bitmap = Bitmap.createBitmap(intArray, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(bitmap);
    }

}
