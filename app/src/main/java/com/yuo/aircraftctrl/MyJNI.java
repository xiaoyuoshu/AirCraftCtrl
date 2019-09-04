package com.yuo.aircraftctrl;

import android.graphics.Bitmap;

public class MyJNI {
    static {
        System.loadLibrary("JniTest");
    }
    public static native String sayHello();
    public static native String trainBMP(int bmpNum, String bmp1, String bmp2, String bmp3, String bmp4, String bmp5, String bmp6, String bmp7, String bmp8, String bmp9);
    public static native String Identify(byte[] pixels, int w, int h);
}
