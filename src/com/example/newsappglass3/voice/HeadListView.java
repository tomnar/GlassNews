package com.example.newsappglass3.voice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class HeadListView extends ListView implements SensorEventListener {

	private static final float INVALID_X = 10;
	private Sensor mSensor;
	private int mLastAccuracy;
	private SensorManager mSensorManager;
	private float mStartX = INVALID_X;
	private static final int SENSOR_RATE_uS = 400000;
	private static final float VELOCITY = (float) (Math.PI / 180 * 2); // scroll one item per 2�


	public HeadListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public HeadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HeadListView(Context context) {
		super(context);
		init();
	}

	public void init() {
		if (isInEditMode())
			return;

		mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	}

	public void activate() {
		if (mSensor == null)
			return;

		mStartX = INVALID_X;
        lastPosition = -1;
		mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
	}

	public void deactivate() {
		mSensorManager.unregisterListener(this);
		mStartX = INVALID_X;
        lastPosition = -1;
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == VISIBLE && needsScrolling()) activate();
		else deactivate();
	}

//	@Override //Not called in CardScrollView
//	protected void onDisplayHint(int hint) {
//		super.onDisplayHint(hint);
//
//		if (hint == VISIBLE && needsScrolling()) activate();
//		else deactivate();
//	}


	private boolean needsScrolling() {
		View a = getChildAt(0),
				b = getChildAt(getChildCount() - 1);

		return (a != null && b != null) && (getListPaddingTop() < a.getTop() ||	b.getBottom() > getBottom() - getListPaddingBottom());
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		mLastAccuracy = accuracy;
	}

    protected int lastPosition = -1;

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] mat = new float[9],
				orientation = new float[3];

		if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			return;

		SensorManager.getRotationMatrixFromVector(mat, event.values);
		SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
		SensorManager.getOrientation(mat, orientation);

		float z = orientation[0], // see https://developers.google.com/glass/develop/gdk/location-sensors/index
			  x = orientation[1],
			  y = orientation[2];

		if (mStartX == INVALID_X)
			mStartX = x;

		int position = (int) ((mStartX - x) * -1 / VELOCITY);

		if (position < 0)
			mStartX = x;
		else if (position > getCount()) {
            float mEndX = (getCount() * VELOCITY) + mStartX;
            mStartX += x - mEndX;
        }

        if (lastPosition != position) {
            smoothScrollToPosition(position);
            lastPosition = position;
        }
	}

}