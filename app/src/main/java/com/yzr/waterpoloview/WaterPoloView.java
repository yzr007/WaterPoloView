package com.yzr.waterpoloview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Yangzr on 2016/12/25.
 */

public class WaterPoloView extends View{
    private int PoloSize;
    private float cX,cY,radius;
    //比例
    private float rate;
    //水位线
    private float levelLine;
    private float textSize;
    //波浪波峰高度，正弦周期，相位
    private float waveHeight,waveT,waveOffset;
    //一个周期分几次绘制成
    private int drawTimes;
    //每次绘制的间隔 waveT/drawTimes
    private float drawInterval;
    //几个周期能填满整个View
    private int N;

    private boolean isPlaying = false;
    private final int PLAY_WHAT = 1001;
    //每10ms运动多少个像素
    private float speed;
    private PlayHandler handler;
    private class PlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case PLAY_WHAT:
                    play();
                    break;
            }
        }
    }
    public WaterPoloView(Context context) {
        super(context);
        init(null);
    }

    public WaterPoloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaterPoloView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WaterPoloView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        paint = new Paint();
        paintWater = new Paint();
        paintWaterLight = new Paint();
        pathWater = new Path();
        pathWaterLight = new Path();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setStrokeWidth(2);
        paintWater.setAntiAlias(true);
        paintWater.setColor(Color.BLUE);
        paintWaterLight.setAntiAlias(true);
        paintWaterLight.setColor(0x990000ff);
        if(attrs != null){
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs,R.styleable.WaterPoloView);
            PoloSize = typedArray.getDimensionPixelSize(R.styleable.WaterPoloView_poloSize,100);
        }else{
            PoloSize = 100;
        }
        updateParams();
    }

    public void setPoloSize(int poloSize){
        PoloSize = poloSize;
        updateParams();
        postInvalidate();
    }

    private void updateParams(){
        rate = 0.5f;
        levelLine = PoloSize*(1-rate);
        textSize = PoloSize/5f;
        paint.setTextSize(textSize);
        waveHeight = PoloSize/20;
        waveT = PoloSize*3;
        waveOffset = 0;
        drawInterval = 20;
        drawTimes = (int) (waveT/drawInterval);
        N = (int) (PoloSize/waveT+1);
        speed = PoloSize/55f;
        cX = PoloSize/2f;
        cY =cX;
        radius = cX;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(PoloSize,PoloSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        path.addCircle(cX,cY,radius, Path.Direction.CCW);
        canvas.clipPath(path);
        drawWater(canvas);
        drawRateText(canvas);
        canvas.restore();
        drawCircle(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startPlay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPlay();
    }

    private Paint paint,paintWater,paintWaterLight;
    private Path pathWater;
    private Path pathWaterLight;
    private void drawWater(Canvas canvas){
        pathWater.reset();
        pathWaterLight.reset();
        pathWater.moveTo(0,getWaterY(0));
        pathWaterLight.moveTo(0,getWaterLightY(0));
        for(int n = 0 ; n<N ;n++) {
            for (int i = 0; i < drawTimes; i++) {
                float x = (n*waveT)+i*drawInterval;
                pathWater.lineTo(x,getWaterY(x));
                pathWaterLight.lineTo(x,getWaterLightY(x));
                if(x>=PoloSize){
                    break;
                }
            }
        }
        pathWater.lineTo(PoloSize,PoloSize);
        pathWater.lineTo(0,PoloSize);
        pathWater.close();
        pathWaterLight.lineTo(PoloSize,PoloSize);
        pathWaterLight.lineTo(0,PoloSize);
        pathWaterLight.close();
        canvas.drawPath(pathWater,paintWater);
        canvas.drawPath(pathWaterLight,paintWaterLight);
    }

    private void drawRateText(Canvas canvas){
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawText(rate*100+"%",cX,cY+textSize,paint);
    }

    private void drawCircle(Canvas canvas){
        paint.setColor(0x330000ff);
        canvas.drawCircle(cX,cY,radius,paint);
    }

    private float getWaterY(float x){
        return (float) (levelLine+waveHeight*(Math.sin((Math.PI*2/waveT)*(x+waveOffset))));
    }

    private float getWaterLightY(float x){
        return (float) (levelLine-waveHeight*(Math.sin((Math.PI*2/waveT)*(x+waveOffset))));
    }

    private void play(){
        if(isPlaying) {
            if (waveOffset < waveT) {
                waveOffset += speed;
            } else {
                waveOffset = 0;
            }
            postInvalidate();
            handler.removeMessages(PLAY_WHAT);
            handler.sendEmptyMessageDelayed(PLAY_WHAT,10);
        }
    }
    public void startPlay(){
        isPlaying = true;
        if(null == handler){
            handler = new PlayHandler();
        }
        play();
    }
    public void stopPlay(){
        isPlaying = false;
    }
}
