package com.yuo.aircraftctrl;


/**
 * Created by yuo on 2019/6/23.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private ImageView imgView[];
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private String imp[] = new String[9];
    private int imgNum = 0;

    //所需权限
//    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = new ImageView[9];
        imgView[0] = (ImageView) findViewById(R.id.image1);
        imgView[1] = (ImageView) findViewById(R.id.image2);
        imgView[2] = (ImageView) findViewById(R.id.image3);
        imgView[3] = (ImageView) findViewById(R.id.image4);
        imgView[4] = (ImageView) findViewById(R.id.image5);
        imgView[5] = (ImageView) findViewById(R.id.image6);
        imgView[6] = (ImageView) findViewById(R.id.image7);
        imgView[7] = (ImageView) findViewById(R.id.image8);
        imgView[8] = (ImageView) findViewById(R.id.image9);
        imp[0] = "";
        imp[1] = "";
        imp[2] = "";
        imp[3] = "";
        imp[4] = "";
        imp[5] = "";
        imp[6] = "";
        imp[7] = "";
        imp[8] = "";

    }

    public void onClick(View v) {
        //调用相册
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE);
    }

    public void onConnect(View v){
        Intent intent = new Intent(MainActivity.this, AirControl.class);
        startActivity(intent);
    }

    public void onTrain(View v){
        Log.e("JNI", new MyJNI().trainBMP(imgNum,imp[0],imp[1],imp[2],imp[3],imp[4],imp[5],imp[6],imp[7],imp[8]));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            c.close();
            showImage(imagePath);
        }
    }

    //加载图片
    private void showImage(String imagePath) {

        imp[imgNum] = imagePath;

        Bitmap bm = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm = BitmapFactory.decodeStream(fs);
        imgView[imgNum].setImageBitmap(bm);
        imgNum++;
    }
}
