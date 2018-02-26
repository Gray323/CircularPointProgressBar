package com.rxd.circularpointprogressbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2018/2/24.
 */

public class CustomProgressBar extends View{

    private Paint mDarkPaint;//表示没有加速的圆点的paint
    private Paint mLightPaint;//表示已加速的圆点的paint
    private Paint mButtonPaint;//绘制button的paint
    private TextPaint pointTextPaint;//绘制分数文字的paint
    private TextPaint mTextPaint;//绘制“分”文字的paint
    private TextPaint mButtonTextPaint;//绘制按钮内文字的paint

    private int mWidth;//view的宽
    private int mHeight;//view的高

    private Path mPath;//外层圆的path路径
    private PathMeasure mPathMeasure;//外层圆的path测量类

    private float arcLength;//100分之一的圆的弧长

    /**
     * 表示当前view的状态
     */
    private enum State{
        NONE,//还没开始加速
        RUN,//正在加速中
        END,//加速结束
        STOP//停止加速
    }
    private State mCurrentState = State.NONE;//表示当前view的状态

    private ValueAnimator mValueAnimator;//属性动画
    //属性动画监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;
    //当前动画进行中的值
    private int mAnimatorValue;

    //动画默认执行时间
    private int mDefaultDuration = 5000;

    //按钮的rect
    private Rect buttonRect;
    //按钮的区域范围，用于判断点击是否落在按钮上
    private Region region;

    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initListener();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    //初始化画笔
    private void initPaint(){
        mDarkPaint = new Paint();
        mDarkPaint.setAntiAlias(true);
        mDarkPaint.setStyle(Paint.Style.FILL);
        mDarkPaint.setColor(Color.GRAY);

        mLightPaint = new Paint();
        mLightPaint.setAntiAlias(true);
        mLightPaint.setStyle(Paint.Style.FILL);
        mLightPaint.setColor(Color.WHITE);

        pointTextPaint = new TextPaint();
        pointTextPaint.setAntiAlias(true);
        pointTextPaint.setStyle(Paint.Style.FILL);
        pointTextPaint.setTextSize(200);
        pointTextPaint.setColor(Color.WHITE);
        pointTextPaint.setTextAlign(TextPaint.Align.CENTER);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(100);
        mTextPaint.setColor(Color.WHITE);

        mButtonTextPaint = new TextPaint();
        mButtonTextPaint.setAntiAlias(true);
        mButtonTextPaint.setStyle(Paint.Style.FILL);
        mButtonTextPaint.setTextSize(80);
        mButtonTextPaint.setColor(Color.parseColor("#0082D7"));
        mButtonTextPaint.setTextAlign(Paint.Align.CENTER);

        mButtonPaint = new Paint();
        mButtonPaint.setAntiAlias(true);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(Color.WHITE);
    }

    //初始化监听器
    private void initListener(){
        mValueAnimator = ValueAnimator.ofInt(0, 100).setDuration(mDefaultDuration);
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (int) animation.getAnimatedValue();
                invalidate();
            }
        };
        mAnimatorListener  = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentState = State.END;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        mValueAnimator.addUpdateListener(mUpdateListener);
        mValueAnimator.addListener(mAnimatorListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#0082D7"));
        drawCircularPoint(canvas);
        drawTextPoint(canvas);
        drawButton(canvas);
    }

    /**
     * 绘制按钮
     */
    private void drawButton(Canvas canvas){
        //获取"100分"的长度
        int buttonLength = (int) ((mTextPaint.measureText("分") + pointTextPaint.measureText("100")) / 2);
        //按钮的rect
        buttonRect = new Rect(-buttonLength + mWidth / 2, 100 + mHeight / 2, buttonLength + mWidth / 2,220 + mHeight / 2);
        Paint.FontMetricsInt fontMetrics = mButtonTextPaint.getFontMetricsInt();
        //确定baseline基线，让文字垂直居中
        int baseLine = buttonRect.centerY() + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        //绘制圆角矩形
        canvas.drawRoundRect(new RectF(buttonRect), 50, 50, mButtonPaint);
        switch (mCurrentState){
            case NONE:
                canvas.drawText("开始加速", mWidth / 2, baseLine, mButtonTextPaint);
                break;
            case RUN:
                canvas.drawText("停止加速", mWidth / 2, baseLine, mButtonTextPaint);
                break;
            case END:
                canvas.drawText("重新加速", mWidth / 2, baseLine, mButtonTextPaint);
                break;
            case STOP:
                canvas.drawText("继续加速", mWidth / 2, baseLine, mButtonTextPaint);
                break;
        }
    }

    /**
     * 画分数文字
     * @param canvas
     */
    private void drawTextPoint(Canvas canvas){
        //绘制分数
        float dx1 = mTextPaint.measureText("分") / 2;
        canvas.drawText(mAnimatorValue + "", -dx1 + mWidth / 2, 0 + mHeight / 2, pointTextPaint);
        //绘制"分"
        float dx2 = pointTextPaint.measureText(mAnimatorValue + "") / 2.0f;
        canvas.drawText("分", -dx1 + dx2 + mWidth / 2, 0 + mHeight / 2, mTextPaint);
    }

    /**
     * 画外层圆点
     * @param canvas
     */
    private void drawCircularPoint(Canvas canvas){
        //1.确定外层圆的路径
        mPath = new Path();
        RectF rectF = new RectF(-400 + mWidth / 2, -400 + mHeight / 2, 400 + mWidth / 2, 400 + mHeight / 2);
        mPath.addArc(rectF, 270, 359.9f);
        //2.测量获得圆的100分之一的长度
        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(mPath,false);
        arcLength = mPathMeasure.getLength() / 100.0f;
        //3.获取100个圆的每个圆的圆心，完成绘制
        switch (mCurrentState){
            case NONE:
                for (int i = 0; i < 100; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mDarkPaint);
                }
                break;
            case RUN:
                for (int i = 0; i < mAnimatorValue; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mLightPaint);
                }
                for (int i = mAnimatorValue; i < 100; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mDarkPaint);
                }
                break;
            case END:
                for (int i = 0; i < 100; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mLightPaint);
                }
                break;
            case STOP:
                for (int i = 0; i < mAnimatorValue; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mLightPaint);
                }
                for (int i = mAnimatorValue; i < 100; i++){
                    float pos[] = new float[2];
                    mPathMeasure.getPosTan(arcLength * i, pos, null);
                    canvas.drawCircle(pos[0], pos[1], arcLength / 4, mDarkPaint);
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                //确定按钮区域
                region = new Region(buttonRect);
                //判断是否在按钮区域内抬起
                boolean isContains = region.contains(x, y);
                if (isContains){
                    switch (mCurrentState){
                        case NONE:
                            //开始动画
                            mCurrentState = State.RUN;
                            mValueAnimator.start();
                            break;
                        case RUN:
                            //暂停动画
                            mCurrentState = State.STOP;
                            invalidate();
                            mValueAnimator.pause();
                            break;
                        case END:
                            //重新开始动画
                            mCurrentState = State.RUN;
                            mValueAnimator.start();
                            break;
                        case STOP:
                            //恢复动画
                            mCurrentState = State.RUN;
                            mValueAnimator.resume();
                            break;
                    }
                }
                break;
        }
        return true;
    }
}
