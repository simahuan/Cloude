package com.pisen.router.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.pisen.router.R;

/**
 * @author  mahuan
 * @version 1.0 2015年5月29日 下午1:25:51
 */
public class DevRoundProgressBar extends View {


	/**
	 * 画笔对象的引用
	 */
	private Paint paint;
	
	/**
	 * 圆环的颜色
	 */
	private int devRoundColor;
	
	/**
	 * 圆环进度的颜色
	 */
	private int devRoundProgressColor;
	
	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int devTextColor ;
	
	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float devTextSize = 40;
	
	/**
	 * 圆环的宽度
	 */
	private float devRoundWidth;
	
	/**
	 * 最大进度
	 */
	private int devMax;
	
	/**
	 * 当前进度
	 */
	private int devProgress = 0;
	/**
	 * 是否显示中间的进度
	 */
	private boolean devTextIsDisplayable;
	
	/** 线型渐变化 */
	private LinearGradient linearGradient;
	/**
	 * 进度的风格，实心或者空心
	 */
	private int devStyle;
	
	public static final int STROKE = 0;
	public static final int FILL = 1;
	
	public DevRoundProgressBar(Context context) {
		this(context, null);
	}

	public DevRoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DevRoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		paint = new Paint();
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.DevRoundProgressBar);
		
		//获取自定义属性和默认值
		devRoundColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devRoundColor, Color.RED);
		devRoundProgressColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devRoundProgressColor, Color.GREEN);
		devTextColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devTextColor, Color.parseColor("#FF666666"));
		devTextSize = mTypedArray.getDimension(R.styleable.DevRoundProgressBar_devTextSize, 120);
		devRoundWidth = mTypedArray.getDimension(R.styleable.DevRoundProgressBar_devRoundWidth, 5);
		devMax = mTypedArray.getInteger(R.styleable.DevRoundProgressBar_devMax, 100);
		devTextIsDisplayable = mTypedArray.getBoolean(R.styleable.DevRoundProgressBar_devTextIsDisplayable, true);
		devStyle = mTypedArray.getInt(R.styleable.DevRoundProgressBar_devStyle, 0);
		
		mTypedArray.recycle();
	}
	

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		/**
		 * 画最外层的大圆环
		 */
		int centre = getWidth()/2; //获取圆心的x坐标,获取可显示物理设备的中心点
		int radius = (int) (centre - devRoundWidth/2); //圆环的半径
		paint.setColor(devRoundColor); //设置圆环的颜色
		paint.setStyle(Paint.Style.STROKE); //设置空心
		paint.setStrokeWidth(devRoundWidth); //设置圆环的宽度
		paint.setAntiAlias(true);  //消除锯齿 
		canvas.drawCircle(centre, centre, radius, paint); //画出圆环
		
		/**
		 * 画圆弧 ，画圆环的进度
		 */
		paint.setStrokeWidth(devRoundWidth); //设置圆环的宽度
		linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), new int[] { 0xFF65E0F7,0xFF4FDBF4, 0xFF8C7DFF}, null, Shader.TileMode.REPEAT);
		paint.setShader(linearGradient);
		
		RectF oval = new RectF(centre - radius, centre - radius, centre
				+ radius, centre + radius);  //用于定义的圆弧的形状和大小的界限
		
		switch (devStyle) {
		case STROKE:{
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeCap(Cap.ROUND);
			canvas.drawArc(oval, 270, 360 *(1- devProgress / ((float)devMax)), false, paint);  //根据进度画圆弧
			break;
		}
		case FILL:{
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			if(devProgress !=0)
				canvas.drawArc(oval, 0, 360 *(1- devProgress / devMax), true, paint);  //根据进度画圆弧
			break;
		}
	 }
		
		/**
		 * 画进度百分比
		 */
		paint = new Paint();
		paint.setStrokeWidth(0); 
		paint.setStyle(Paint.Style.FILL); 
		paint.setAntiAlias(true);   
		paint.setColor(devTextColor);
		paint.setTextSize(devTextSize);
		paint.setTypeface(Typeface.SANS_SERIF); 
		int percent = (int)(((float)devProgress / (float)devMax) * 100);  //中间的进度百分比，先转换成float在进行除法运算，不然都为0
		float textWidth = paint.measureText(percent+"");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
		
		if(devTextIsDisplayable && devStyle == STROKE){
			canvas.drawText(percent+"", centre - textWidth / 2 - 10, centre + devTextSize/3, paint); //画出进度百分比
			paint = new Paint();
			paint.setStrokeWidth(0); 
			paint.setStyle(Paint.Style.FILL); 
			paint.setColor(devTextColor);
			paint.setTextSize(devTextSize/4);
			paint.setTypeface(Typeface.MONOSPACE); 
			canvas.drawText("%", centre + textWidth/2, centre +30, paint);
		}
	}
	
	
	public synchronized int getMax() {
		return devMax;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.devMax = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized int getProgress() {
		return devProgress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if(progress > devMax){
			progress = devMax;
		}
		if(progress <= devMax){
			this.devProgress = progress;
			postInvalidate();
		}
	}
	
	
	public int getCricleColor() {
		return devRoundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.devRoundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return devRoundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.devRoundProgressColor = cricleProgressColor;
	}

	public int getTextColor() {
		return devTextColor;
	}

	public void setTextColor(int textColor) {
		this.devTextColor = textColor;
	}

	public float getTextSize() {
		return devTextSize;
	}

	public void setTextSize(float textSize) {
		this.devTextSize = textSize;
	}

	public float getRoundWidth() {
		return devRoundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.devRoundWidth = roundWidth;
	}
}
