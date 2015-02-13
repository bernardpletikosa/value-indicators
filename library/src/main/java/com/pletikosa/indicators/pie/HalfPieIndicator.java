package com.pletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.Orientation;

public class HalfPieIndicator extends PieIndicator {

    private static final float MAX_ANGLE = 180f;

    protected int mWidth;
    protected int mHeight;
    protected Orientation mOrientation;

    public HalfPieIndicator(Context context) {
        this(context, null);
    }

    public HalfPieIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HalfPieIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mOrientation = Orientation.SOUTH;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mWidth = width;
        mHeight = height;

        calculateCenter();

        if (mRadius <= 0)
            mRadius = mMiddleX < mMiddleY ? mMiddleX : mMiddleY;
        if (mInnerRadius <= 0)
            mInnerRadius = mInnerRadiusPercent > -1 ? (int) (mInnerRadiusPercent / 100f * mRadius) : mRadius / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddleX, mMiddleY, mRadius, mBackgroundPaint);

        mRectF.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);

        int startAngle = calculateStartAngle();
        float value = mDirection == Direction.CLOCKWISE ? mCurrentValue : -mCurrentValue;
        canvas.drawArc(mRectF, startAngle, value, true, mMainPaint);

        canvas.drawCircle(mMiddleX, mMiddleY, mInnerRadius, mCenterPaint);
    }

    private int calculateStartAngle() {
        int startAngle = 0;
        switch (mOrientation) {
            case SOUTH:
                startAngle = 180; break;
            case NORTH:
                startAngle = 0; break;
            case EAST:
                startAngle = 90; break;
            case WEST:
                startAngle = 270; break;
        }

        return mDirection == Direction.CLOCKWISE ? startAngle : (startAngle + 180) % 360;
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * MAX_ANGLE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    /**
     * <p>Sets orientation of indicator as sides of the world (east west, north south) </p>
     * Possible values are:
     * <ul>
     * <li>{@link Orientation#EAST}</li>
     * <li>{@link Orientation#WEST}</li>
     * <li>{@link Orientation#NORTH}</li>
     * <li>{@link Orientation#SOUTH}</li>
     * </ul>
     * @param orientation {@link Orientation}
     */
    public void setOrientation(Orientation orientation) throws IllegalArgumentException {
        checkArgument(orientation, "orientation");

        mOrientation = orientation;
        calculateCenter();
        draw();
    }

    /**
     * Parent method which is not used in {@link HalfPieIndicator}
     */
    @Override
    public void setStartingAngle(int startAngle) {
        // Parent method, not used.
    }

    private void calculateCenter() {
        switch (mOrientation) {
            case SOUTH:
            case NORTH:
                mMiddleX = mWidth / 2;
                mMiddleY = mOrientation == Orientation.SOUTH ? mHeight : 0;
                break;
            case EAST:
            case WEST:
                mMiddleX = mOrientation == Orientation.EAST ? mWidth : 0;
                mMiddleY = mHeight / 2;
        }
    }
}
