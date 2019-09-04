package com.yuo.aircraftctrl;

/**
 * Created by yuo on 2019/6/23.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class AirControl extends Activity {

    public MyEmptyVideo videoPlayer;//视频播放器
    OrientationUtils orientationUtils;

    private Thread writeThread;
    private Thread identifyThread;

    private BroadcastReceiver rssiReceiver;

    private int leftLevel = 0;
    private double leftAngel = 0;
    private int rightLevel = 0;
    private double rightAngel = 0;

    private int idenW;
    private int idenH;

    private boolean boolautoset = false;
    private boolean start = false;
    private boolean reiden = false;
    private boolean exit = false;
    private boolean autotracking = false;
    private boolean firstiden = false;

    private DatagramSocket socket;

    //组件定义
    private TextView Tthrottle;
    private TextView Tyaw;
    private TextView Tpitch;
    private TextView Troll;
    private TextView Tmode;
    private TextView Trssi;

    private Button Bstup;
    private Button Bstdown;
    private Button BrefreshV;
    private Button Bstleft;
    private Button Bautoset;
    private Button Bstright;
    private Button Bstabilize;
    private Button Bheight;
    private Button BautoTracking;

    private RemoterView LeftRemo;
    private RemoterView2 RightRemo;

    private IdentiView identiView;

    private ControlData controlData;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_control);
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //PlayerFactory.setPlayManager(IjkPlayerManager.class);
        initView();
        initData();
        initLeftRemo();
        initRightRemo();
        initVideo();
        BrefreshV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identiView.refresh(0,0,0,0);
                reiden = true;
                if(!start&&!exit) {
                    BrefreshV.setBackgroundColor(Color.parseColor("#ffffffff"));
                    writeThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (socket == null) {
                                    socket = new DatagramSocket(6324);
                                }
                                InetAddress serverAddress = InetAddress.getByName("192.168.1.254");
                                DatagramPacket packet;
                                while (true) {
                                    Log.i("sendPack", controlData.getStr());
                                    byte d[] = controlData.getStr().getBytes();
                                    packet = new DatagramPacket(d, d.length, serverAddress, 8888);
                                    socket.send(packet);
                                    writeThread.sleep(20);
                                }
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    writeThread.start();
                    identifyThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!exit) {
                                videoPlayer.taskShotPic(new GSYVideoShotListener() {
                                    @Override
                                    public void getBitmap(Bitmap bitmap) {
                                        int w = bitmap.getWidth();
                                        int h = bitmap.getHeight();
                                        int[] pixels = new int[w * h];
                                        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
                                        byte[] grays = new byte[w * h];
                                        int x, y;
                                        int pixel, r, g, b;
                                        for (y = 0; y < h; y++) {
                                            for (x = 0; x < w; x++) {
                                                pixel = pixels[y * w + x];
                                                r = (pixel >> 16) & 0xFF;
                                                g = (pixel >> 8) & 0xFF;
                                                b = pixel & 0xFF;
                                                grays[x + y * w] = (byte) (grays[x + y * w] | ((r * 299 + g * 587 + b * 114 + 500) / 1000) & 0xff);
                                            }
                                        }
                                        //检查bitmap正确性
//                                        IdentiView checkBitmap = (IdentiView) findViewById(R.id.checkBitmap);
//                                        checkBitmap.drawBitmap(bitmap);
                                        try{
                                            String identiResult = null;
                                            if(reiden){
                                                reiden = false;
                                                firstiden = false;
                                                identiResult = new MyJNI().Identify(grays, 1, h);
                                                Log.e("reiden---identiResult",identiResult);
                                            }else {
                                                identiResult = new MyJNI().Identify(grays, 0, h);
                                                Log.e("identiResult",identiResult);
                                            }
                                            String data[] = identiResult.split(";");
                                            int idenX = (int) (Integer.valueOf(data[0])*1320/640.0);
                                            int idenY = (int) (Integer.valueOf(data[1])*1320/640.0);
                                            int idenLen = (int) (Integer.valueOf(data[2])*1320/640.0);
                                            if(!firstiden && idenLen != 0){
                                                promptAudio();
                                                firstiden = true;
                                            }
                                            if(autotracking){
                                                autoTracking(idenX, idenY, idenLen);
                                            }
                                            identiView.refresh(idenX,idenY,idenLen,idenLen);
                                        }catch(Exception e){
                                            Log.e("identiResult","CATCH ERRORRRRRRR!!!!!");
                                        }
                                    }
                                }, true);
                            }
                        }
                    });
                    identifyThread.start();
                    start = true;
                }
            }
        });
        Bstleft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    controlData.stLeft();
                }else if (action == MotionEvent.ACTION_UP) {
                    controlData.stOff();
                }
                return false;
            }
        });
        Bstup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(controlData.getThrottle().equals("0")){
                    int action = motionEvent.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        Bstup.setBackgroundColor(Color.parseColor("#ffffffff"));
//                    controlData.stUp();
                        controlData.setLeft(0,2000);
                    }else if (action == MotionEvent.ACTION_UP) {
//                    controlData.stOff();
                        Bstup.setBackgroundColor(Color.parseColor("#4fffffff"));
                        controlData.setLeft(0,1500);
                    }
                    Tthrottle.setText("油门："+controlData.getThrottle()+"%");
                    Tyaw.setText("转向："+controlData.getYaw()+"%");
                }
                return false;
            }
        });
        Bstdown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(controlData.getThrottle().equals("0")){
                    int action = motionEvent.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
//                    controlData.stUp();
                        Bstdown.setBackgroundColor(Color.parseColor("#ffffffff"));
                        controlData.setLeft(0,1000);
                    }else if (action == MotionEvent.ACTION_UP) {
                        Bstdown.setBackgroundColor(Color.parseColor("#4fffffff"));
//                    controlData.stOff();
                        controlData.setLeft(0,1500);
                    }
                    Tthrottle.setText("油门："+controlData.getThrottle()+"%");
                    Tyaw.setText("转向："+controlData.getYaw()+"%");
                }
                return false;
            }
        });
        Bstright.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    controlData.stRight();
                }else if (action == MotionEvent.ACTION_UP) {
                    controlData.stOff();
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(rssiReceiver);
        exit = true;
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //监听wifi强度
        rssiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                WifiInfo wifiinfo = wifiManager.getConnectionInfo();
                int rssi = wifiinfo.getRssi();
                Trssi.setText("信号强度："+rssi+"dBm");
            }
        };
        registerReceiver(rssiReceiver,new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        Bstabilize.callOnClick();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        exit = true;
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }
    @Override
    public void onBackPressed() {
        videoPlayer.setVideoAllCallBack(null);
        exit = true;
        super.onBackPressed();
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus){
            idenW = identiView.getMeasuredWidth();
            idenH = identiView.getMeasuredHeight();
            Log.e("Window", "W:"+String.valueOf(idenW)+" H:"+String.valueOf(idenH));
            identiView.setMainX((idenW-idenH/3*4)/2);
        }
    }
    /**
     * 播放追踪提示音
     */
    public void promptAudio(){
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
        rt.play();
    }
    /**
     * 点击自动追踪按钮
     */
    public void onAutoTracking(View view){
        if(!autotracking) {
            BautoTracking.setBackgroundColor(Color.parseColor("#ffffffff"));
            autotracking = true;
        } else {
            BautoTracking.setBackgroundColor(Color.parseColor("#4fffffff"));
            autotracking = false;
        }
    }
    /**
     * 自动追踪逻辑
     */
    public void autoTracking(int x, int y , int len){
        int roll = 1500;
        int pitch = 1500;
        int videoW = idenH / 3 * 4;
        if(x < videoW / 10){
            roll = 1450;
        } else if(x + len > videoW /10 *9){
            roll = 1550;
        }
        if(y < idenH / 10){
            pitch = 1550;
        } else if(y + len > idenH /10 *9){
            pitch = 1450;
        }
        controlData.setRight(pitch, roll);
        Tpitch.setText("前后移："+controlData.getPitch()+"%");
        Troll.setText("左右移："+controlData.getRoll()+"%");
    }
    /**
     * 点击自动调参按钮
     */
    public void onAutoset(View view){
        if(!boolautoset) {
            Bautoset.setBackgroundColor(Color.parseColor("#ffffffff"));
            boolautoset = true;
            controlData.autosetOn();
        } else {
            Bautoset.setBackgroundColor(Color.parseColor("#4fffffff"));
            boolautoset = false;
            controlData.autosetOff();
        }
    }
    /**
     * 点击自稳按钮
     */
    public void onStabilize(View view){
        Bstabilize.setBackgroundColor(Color.parseColor("#ffffffff"));
        Bheight.setBackgroundColor(Color.parseColor("#4fffffff"));
        controlData.setMode(ControlData.STABILIZE);
        Tmode.setText("飞行模式："+controlData.getMode());
    }
    /**
     * 点击定高按钮
     */
    public void onHeight(View view){
        Bstabilize.setBackgroundColor(Color.parseColor("#4fffffff"));
        Bheight.setBackgroundColor(Color.parseColor("#ffffffff"));
        controlData.setMode(ControlData.HEIGHT);
        Tmode.setText("飞行模式："+controlData.getMode());
    }
    /**
     * 点击定高按钮
     */
    public void onAutoDown(View view){
        controlData.setMode(ControlData.AUTODOWN);
        Tmode.setText("飞行模式："+controlData.getMode());
    }
    /**
     * 绑定左摇杆事件
     */
    private void initLeftRemo(){
        LeftRemo.setOnAngleChangeListener(new RemoterView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                leftAngel = angle;
                SetLeftText();
            }

            @Override
            public void onFinish() {

            }
        });
        LeftRemo.setOnDistanceLevelListener(new RemoterView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                leftLevel = level;
                SetLeftText();
            }
        });
    }
    /**
     * 处理左摇杆文本
     */
    private void SetLeftText(){
        int yaw = (int) (leftLevel*Math.cos(Math.toRadians(leftAngel)))+1500;
        controlData.setLeft((int) (leftLevel*Math.sin(Math.toRadians(leftAngel))+1500), yaw<1500?(yaw>=1400?1500:yaw):(yaw<=1600?1500:yaw));
        Tthrottle.setText("油门："+controlData.getThrottle()+"%");
        Tyaw.setText("转向："+controlData.getYaw()+"%");
    }
    /**
     * 绑定右摇杆事件
     */
    private void initRightRemo(){
        RightRemo.setOnAngleChangeListener(new RemoterView2.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }
            @Override
            public void angle(double angle) {
                rightAngel = angle;
                SetRightText();
            }

            @Override
            public void onFinish() {

            }
        });
        RightRemo.setOnDistanceLevelListener(new RemoterView2.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                rightLevel = level;
                SetRightText();
            }
        });
    }

    /**
     * 处理右摇杆文本
     */
    private void SetRightText(){
        controlData.setRight((int) (rightLevel*Math.sin(Math.toRadians(rightAngel))+1500),(int) (rightLevel*Math.cos(Math.toRadians(rightAngel)))+1500);
        Tpitch.setText("前后移："+controlData.getPitch()+"%");
        Troll.setText("左右移："+controlData.getRoll()+"%");
    }

    /**
     * 初始化组件
     */
    private void initView() {
        Tthrottle = (TextView) findViewById(R.id.Tthrottle);
        Tyaw = (TextView) findViewById(R.id.Tyaw);
        Tpitch = (TextView) findViewById(R.id.Tpitch);
        Troll = (TextView) findViewById(R.id.Troll);
        Tmode = (TextView) findViewById(R.id.Tmode);
        Trssi = (TextView) findViewById(R.id.Trssi);
        Bstup = (Button) findViewById(R.id.Bstup);
        Bstdown = (Button) findViewById(R.id.Bstdown);
        BrefreshV = (Button) findViewById(R.id.BrefreshV);
        Bstleft = (Button) findViewById(R.id.Bstleft);
        Bautoset = (Button) findViewById(R.id.Bautoset);
        BautoTracking = (Button) findViewById(R.id.BautoTracking);
        Bstright = (Button) findViewById(R.id.Bstright);
        Bstabilize = (Button) findViewById(R.id.Bstabilize);
        Bheight = (Button) findViewById(R.id.Bheight);
        LeftRemo = (RemoterView) findViewById(R.id.LeftRemo);
        RightRemo = (RemoterView2) findViewById(R.id.RightRemo);
        identiView = (IdentiView) findViewById(R.id.iden);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        controlData = new ControlData();
    }

    /**
     * 连接视频
     */
    private void initVideo() {
        optimizationVideo();
        videoPlayer =  (MyEmptyVideo) findViewById(R.id.videoplayer);
        String vSource = "rtsp://192.168.1.70:554/user=admin_password=tlJwpbo6_channel=1_stream=0.sdp?real_stream";
        videoPlayer.setUp(vSource, true, "");
        videoPlayer.startPlayLogic();

    }

    /**
     * 优化视频加载时延
     */
    private void optimizationVideo(){
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
    }
}

