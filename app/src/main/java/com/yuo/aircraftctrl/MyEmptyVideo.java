package com.yuo.aircraftctrl;

import android.content.Context;
import android.util.AttributeSet;

import com.yuo.aircraftctrl.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;


public class MyEmptyVideo extends StandardGSYVideoPlayer {

    public MyEmptyVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MyEmptyVideo(Context context) {
        super(context);
    }

    public MyEmptyVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.empty_control_video;
    }

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //屏蔽触摸快进
        mChangePosition = false;

        //屏蔽触摸音量
        mChangeVolume = false;

        //屏蔽触摸亮度
        mBrightness = false;
    }

    @Override
    protected void touchDoubleUp() {
        //super.touchDoubleUp();
        //屏蔽双击暂停
    }
}