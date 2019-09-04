package com.yuo.aircraftctrl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class IdentiView extends View {
    private int recX = 0;
    private int recY = 0;
    private int recXLen = 0;
    private int recYLen = 0;
    private int mainX = 0;
    private static int borderLen = 5;
    private boolean draw_bitmap = false;
    Bitmap bitmap;

    public IdentiView(Context context) {
        super(context);
    }

    public IdentiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IdentiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(draw_bitmap){
            Paint mAreaBackgroundPaint = new Paint();
            mAreaBackgroundPaint.setAntiAlias(true);
            canvas.drawBitmap(bitmap,0,0,mAreaBackgroundPaint);
        }
        else {
            // 设置画笔的基本属性
            Paint paint = new Paint();
            // 设置画笔的颜色
            paint.setColor(Color.RED);
            // 设置画笔填充的样式
            paint.setStyle(Paint.Style.FILL);
            // 设置画笔的宽度
            paint.setStrokeWidth(3);

            // 设置画布
            canvas.drawLine(recX, recY, recX+recXLen, recY, paint);
            canvas.drawLine(recX, recY, recX, recY+recYLen, paint);
            canvas.drawLine(recX+recXLen, recY, recX+recXLen, recY+recYLen, paint);
            canvas.drawLine(recX, recY+recYLen, recX+recXLen, recY+recYLen, paint);
        }
    }

    public void refresh(int recX, int recY, int recXLen, int recYLen){
        this.recX = mainX + recX;
        this.recY = recY;
        this.recXLen = recXLen;
        this.recYLen = recYLen;
        invalidate();
    }

    public void drawBitmap(Bitmap bitmap){
        this.draw_bitmap = true;
        this.bitmap = bitmap;
        invalidate();
    }

    public void setMainX(int mainX){
        this.mainX = mainX;
    }
}
