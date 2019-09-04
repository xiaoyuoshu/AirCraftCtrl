package com.yuo.aircraftctrl;

import android.util.Log;

public class ControlData {
    private int throttle;//ch3
    private int yaw;//ch4
    private int pitch;//ch2
    private int roll;//ch1
    private int mode;//ch5
    private int stpitch;//ch7
    private int stroll;//ch8
    private int autoset;//ch6
    public static int STABILIZE = 1900;
    public static int HEIGHT = 1700;
    public static int AUTODOWN = 1010;
    public ControlData(){
        this.throttle = 1000;//油门
        this.yaw = 1500;//所有操作居中
        this.pitch = 1500;
        this.roll = 1500;
        this.stpitch = 1500;
        this.stroll = 1500;

        this.autoset = 1000;//不启用自动调参
        this.mode = 2000;//自稳模式
    }


    /**
     * 返回八通道格式化字符串
     * @return
     */
    public String getStr(){
        String for_return = String.valueOf(roll) + ";"
                + String.valueOf(pitch) + ";"
                + String.valueOf(throttle) + ";"
                + String.valueOf(yaw) + ";"
                + String.valueOf(mode) + ";"
                + String.valueOf(stpitch) + ";"
                + String.valueOf(autoset) + ";"
                + String.valueOf(stroll) + ";\n";
        return for_return;
    }

    /**
     * 设置左摇杆控制的值
     * @param throttle
     * @param yaw
     */
    public void setLeft(int throttle, int yaw){
        if(throttle > 1950) {
            this.throttle = 2000;
        } else if(throttle < 1050){
            this.throttle = 1000;
        } else {
            this.throttle = throttle;
        }
        if(yaw > 1950) {
            this.yaw = 2000;
        } else if(yaw < 1050){
            this.yaw = 1000;
        } else {
            this.yaw = yaw;
        }
    }

    /**
     * 设置右摇杆控制的值
     * @param pitch
     * @param roll
     */
    public void setRight(int pitch, int roll){
        if(pitch > 1950) {
            this.pitch = 2000;
        } else if(pitch < 1050){
            this.pitch = 1000;
        } else {
            this.pitch = pitch;
        }
        if(roll > 1950) {
            this.roll = 2000;
        } else if(roll < 1050){
            this.roll = 1000;
        } else {
            this.roll = roll;
        }
    }


    /**
     * 设置飞行模式
     * @param mode
     */
    public void setMode(int mode){
        this.mode = mode;
    }

    /**
     * 打开自动调参
     */
    public void autosetOn(){
        this.autoset = 2000;
    }

    /**
     * 关闭自动调参
     */
    public void autosetOff(){
        this.autoset = 1000;
    }


    /**
     * 云台升
     */
    public void stUp(){
        this.stpitch = 1480;
    }

    /**
     * 云台降
     */
    public void stDown(){
        this.stpitch = 1520;
    }

    /**
     * 云台左
     */
    public void stLeft(){
        this.stroll = 1480;
    }

    /**
     * 云台右
     */
    public void stRight(){
        this.stroll = 1520;
    }

    /**
     * 云台停止
     */
    public void stOff(){
        this.stroll = 1500;
        this.stpitch = 1500;
    }

    /**
     * 获取油门百分比
     * @return
     */
    public String getThrottle() {
        return String.valueOf((int) ((throttle-1000)/10));
    }

    /**
     * 获取pitch轴百分比
     * @return
     */
    public String getPitch() {
        String for_return = "";
        if(pitch > 1500){
            for_return += "↑";
        }
        if(pitch < 1500){
            for_return += "↓";
        }
        return for_return+String.valueOf((int) (Math.abs(pitch-1500)/5));
    }

    /**
     * 获取roll轴百分比
     * @return
     */
    public String getRoll() {
        String for_return = "";
        if(roll < 1500){
            for_return += "←";
        }
        if(roll > 1500){
            for_return += "→";
        }
        return for_return+String.valueOf((int) (Math.abs(roll-1500)/5));
    }

    /**
     * 获取yaw轴百分比
     * @return
     */
    public String getYaw() {
        String for_return = "";
        if(yaw < 1500){
            for_return += "←";
        }
        if(yaw > 1500){
            for_return += "→";
        }
        return for_return+String.valueOf((int) (Math.abs(yaw-1500)/5));
    }

    /**
     * 获取当前模式
     * @return
     */
    public String getMode() {
        if(mode == AUTODOWN){
            return "降落";
        }
        return mode==HEIGHT?"定高":"自稳";
    }
}
