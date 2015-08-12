package com.hanry.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint({ "InlinedApi", "NewApi", "DrawAllocation" })
public class FrontAndBackJoystickView extends View {

	@SuppressLint("DrawAllocation")
	// Constants
	public final static int INVALID_POINTER_ID = -1;
	public final static long DEFAULT_LOOP_INTERVAL = 100; // 100 ms
	public final static int ORIGIN = 1;
	public final static int FRONT = 3;
	public final static int BACK = 5;
	// Variables
	private OnFrontAndBackJoystickMoveListener onJoystickMoveListener; // Listener
	private int pointerId = INVALID_POINTER_ID;
	private int xPosition = 0; // Touch x position
	private int yPosition = 0; // Touch y position
	private double centerX = 0; // Center view x position
	private double centerY = 0; // Center view y position
	private Paint backgroundCirclePaint;
	private Paint arrowPaint;
	private Path arrowPath;
	private Paint handlerButton;
	private int joystickRadius;
	private int buttonRadius;
	private int lastPower = 0;
	private int lastDirection = ORIGIN;
	private int powerResolution = 1;
	private boolean autoReturnToCenter;

	public FrontAndBackJoystickView(Context context) {
		super(context);
	}

	public FrontAndBackJoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFrontAndBackJoystickView();
	}

	public FrontAndBackJoystickView(Context context, AttributeSet attrs,
			int defaultStyle) {
		super(context, attrs, defaultStyle);
		initFrontAndBackJoystickView();
	}

	public void setPointerId(int id) {
		this.pointerId = id;
	}

	public int getPointerId() {
		return pointerId;
	}

	public void setAutoReturnToCenter(boolean autoReturnToCenter) {
		this.autoReturnToCenter = autoReturnToCenter;
	}

	public boolean isAutoReturnToCenter() {
		return autoReturnToCenter;
	}

	protected void initFrontAndBackJoystickView() {
		backgroundCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		handlerButton = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlerButton.setColor(Color.argb(0x3f, 0x22, 0x22, 0x22));
		handlerButton.setStyle(Paint.Style.FILL);

		arrowPath = new Path();

		arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		arrowPaint.setStrokeWidth(2);
		arrowPaint.setColor(Color.WHITE);
		arrowPaint.setStyle(Paint.Style.FILL);
		setAutoReturnToCenter(true);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		// before measure, get the center of view
		xPosition = (int) getWidth() / 2;
		yPosition = (int) getWidth() / 2;
		int d = Math.min(xNew, yNew);
		buttonRadius = (int) (d / 2 * 0.25);
		joystickRadius = (int) (d / 2 * 0.75);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// setting the measured values to resize the view to a certain width and
		// height
		int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
		setMeasuredDimension(d, d);
	}

	private int measure(int measureSpec) {
		int result = 0;

		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		centerX = (getWidth()) / 2;
		centerY = (getHeight()) / 2;

		Shader shader = new RadialGradient((int) centerX, (int) centerY,
				joystickRadius, new int[] { Color.argb(0x3F, 0xFF, 0xFF, 0xFF),
						Color.argb(0x3F, 0xFF, 0xFF, 0xFF),
						Color.argb(0x3F, 0x00, 0x00, 0x00) }, null,
				Shader.TileMode.CLAMP);
		backgroundCirclePaint.setShader(shader);
		// painting the background circle
		canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
				backgroundCirclePaint);

		this.arrowPath.reset();
		this.arrowPath.moveTo((int) centerX, (int) centerY - joystickRadius);
		this.arrowPath.lineTo((int) centerX - 15, (int) centerY
				- joystickRadius + 15);
		this.arrowPath.lineTo((int) centerX, (int) centerY - joystickRadius
				+ 10);
		this.arrowPath.lineTo((int) centerX + 15, (int) centerY
				- joystickRadius + 15);
		this.arrowPath.close();
		canvas.drawPath(arrowPath, arrowPaint);

		this.arrowPath.reset();
		this.arrowPath.moveTo((int) centerX, (int) centerY + joystickRadius);
		this.arrowPath.lineTo((int) centerX - 15, (int) centerY
				+ joystickRadius - 15);
		this.arrowPath.lineTo((int) centerX, (int) centerY + joystickRadius
				- 10);
		this.arrowPath.lineTo((int) centerX + 15, (int) centerY
				+ joystickRadius - 15);
		this.arrowPath.close();
		canvas.drawPath(arrowPath, arrowPaint);

		// painting the handler button
		canvas.drawCircle((int) centerX, yPosition, buttonRadius, handlerButton);
	}

	private void returnHandleToCenter() {
		if (autoReturnToCenter) {
			final int numberOfFrames = 5;
			final double intervalsX = (0 - (this.xPosition - this.centerX))
					/ numberOfFrames;
			final double intervalsY = (0 - (this.yPosition - this.centerY))
					/ numberOfFrames;

			for (int i = 0; i < numberOfFrames; i++) {
				final int j = i;
				postDelayed(new Runnable() {
					public void run() {
						xPosition += intervalsX;
						yPosition += intervalsY;

						reportOnMoved();
						invalidate();

						if (onJoystickMoveListener != null
								&& j == numberOfFrames - 1) {
							onJoystickMoveListener.OnReturnedToCenter();
						}
					}
				}, i * 40);
			}

			if (onJoystickMoveListener != null) {
				onJoystickMoveListener.OnReleased();
			}
		}
	}

	private void reportOnMoved() {
		int currentDir = getDirection();
		int currentPow = getPower();
		if (currentDir != lastDirection
				|| Math.abs(currentPow - lastPower) >= powerResolution) {
			onJoystickMoveListener.onValueChanged(currentPow, currentDir);
			lastDirection = currentDir;
			lastPower = currentPow;
		}
	}

	private boolean processMoveEvent(MotionEvent ev) {
		if (pointerId != INVALID_POINTER_ID) {
			final int pointerIndex = ev.findPointerIndex(pointerId);

			float x = ev.getX(pointerIndex);
			float y = ev.getY(pointerIndex);

			if(x < this.centerX - this.joystickRadius){
				this.xPosition = (int)(this.centerX - this.joystickRadius);
			}else if(x > this.centerX + this.joystickRadius){
				this.xPosition = (int)(this.centerX + this.joystickRadius);
			}else{
				this.xPosition = (int)x;
			}
			
			if (y < this.centerY - this.joystickRadius) {
				this.yPosition = (int) (this.centerY - this.joystickRadius);
			} else if (y > this.centerY + this.joystickRadius) {
				this.yPosition = (int) (this.centerY + this.joystickRadius);
			} else {
				this.yPosition = (int) y;
			}

			if(Math.abs(this.yPosition - this.centerY) > 5){
				reportOnMoved();
			}
			invalidate();
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			return processMoveEvent(event);
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (pointerId != INVALID_POINTER_ID) {
				returnHandleToCenter();
				setPointerId(INVALID_POINTER_ID);
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			if (pointerId != INVALID_POINTER_ID) {
				final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == this.pointerId) {
					returnHandleToCenter();
					setPointerId(INVALID_POINTER_ID);
					return true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_DOWN: {
			if (pointerId == INVALID_POINTER_ID) {
				int x = (int) event.getX();
				if (x >= this.centerX - this.joystickRadius
						&& x <= this.centerX + joystickRadius) {
					setPointerId(event.getPointerId(0));
					return true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN: {
			if (pointerId == INVALID_POINTER_ID) {
				final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				int x = (int) event.getX(pointerId);
				if (x >= this.centerX - this.joystickRadius
						&& x <= this.centerX + joystickRadius) {
					setPointerId(pointerId);
					return true;
				}
			}
			break;
		}
		}
		return false;
	}

	private int getPower() {
		return (int) ((Math.abs(this.yPosition - centerY) / this.joystickRadius) * 100);
	}

	private int getDirection() {
		double val = yPosition - centerY;
		if (val < 1 && val > -1) {
			return ORIGIN;
		}
		if (val >= 1) {
			return BACK;
		}
		return FRONT;
	}

	public void setOnFrontAndBackJoystickMoveListener(
			OnFrontAndBackJoystickMoveListener listener) {
		this.onJoystickMoveListener = listener;
	}

	public static interface OnFrontAndBackJoystickMoveListener {
		public void onValueChanged(int power, int direction);

		public void OnReleased();

		public void OnReturnedToCenter();

	}
}