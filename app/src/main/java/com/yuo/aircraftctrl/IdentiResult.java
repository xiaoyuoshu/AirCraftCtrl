package com.yuo.aircraftctrl;

public class IdentiResult {
    private int returnCode;
    private int Box_x;
    private int Box_y;
    private int Box_x_len;
    private int Box_y_len;
    public IdentiResult(){}
    public IdentiResult(int returnCode, int Box_x, int Box_y, int Box_x_len, int Box_y_len){
        this.returnCode = returnCode;
        this.Box_x = Box_x;
        this.Box_y = Box_y;
        this.Box_x_len = Box_x_len;
        this.Box_y_len = Box_y_len;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public int getBox_x() {
        return Box_x;
    }

    public int getBox_y() {
        return Box_y;
    }

    public int getBox_x_len() {
        return Box_x_len;
    }

    public int getBox_y_len() {
        return Box_y_len;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public void setBox_x(int box_x) {
        Box_x = box_x;
    }

    public void setBox_y(int box_y) {
        Box_y = box_y;
    }

    public void setBox_x_len(int box_x_len) {
        Box_x_len = box_x_len;
    }

    public void setBox_y_len(int box_y_len) {
        Box_y_len = box_y_len;
    }

    public String toString(){
        return "returnCode->" + returnCode + ", Box_x->" + Box_x + ", Box_y->" + Box_y + ", Box_x_len->" + Box_x_len + ", Box_y_len->" + Box_y_len;
    }
}
