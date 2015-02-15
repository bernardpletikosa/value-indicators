package com.pletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.pletikosa.indicators.R;
import com.pletikosa.indicators.consts.Orientation;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.pletikosa.indicators.consts.Defaults.HALF_PIE_MAX_ANGLE;
import static com.pletikosa.indicators.consts.Defaults.NO_VALUE;
import static com.pletikosa.indicators.consts.Direction.CLOCKWISE;
import static com.pletikosa.indicators.consts.Orientation.EAST;
import static com.pletikosa.indicators.consts.Orientation.NORTH;
import static com.pletikosa.indicators.consts.Orientation.SOUTH;

public class HalfPieIndicator extends PieIndicator {

    protected int mWidth;
    protected int mHeight;
    protected Orientation mOrientation;

    protected RectF mBackRect = new RectF();
    protected RectF mFrontRect = new RectF();
    protected int mStartPos;
    protected int mEndPos;

    public HalfPieIndicator(Context context) {
        this(context, null);
    }

    public HalfPieIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HalfPieIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        loadXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieIndicator, 0, 0));
        mOrientation = Orientation.values()[context.getTheme().obtainStyledAttributes(attrs, R.styleable.HalfPieIndicator, 0, 0)
                .getInt(R.styleable.HalfPieIndicator_half_pie_orientation, 4)];

        if (mRadius > NO_VALUE)
            mBackRect.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w, h;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == EXACTLY)
            w = width;
        else if (widthMode == AT_MOST)
            w = mRadius > NO_VALUE ? Math.min(mRadius * 2, width) : width > 0 ? width : height;
        else
            w = mRadius > NO_VALUE ? mRadius * 2 : width > 0 ? width : height;

        if (heightMode == EXACTLY)
            h = height;
        else if (heightMode == AT_MOST)
            h = mRadius > NO_VALUE ? Math.min(mRadius * 2, height) : height > 0 ? height : width;
        else
            h = mRadius > NO_VALUE ? mRadius * 2 : height > 0 ? height : width;

        mWidth = w;
        mHeight = mOrientation == NORTH || mOrientation == SOUTH ? h / 2 : h;

        calculateCenter();
        calculateRadius();
        setHelperRects();

        setMeasuredDimension(mWidth, mHeight);
    }

    private void setHelperRects() {
        mMainRect.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);
        mFrontRect.set(mMiddleX - mInnerRadius, mMiddleY - mInnerRadius, mMiddleX + mInnerRadius, mMiddleY + mInnerRadius);

        mStartPos = calculateStartAngle();
        mEndPos = (int) (mDirection == CLOCKWISE ? HALF_PIE_MAX_ANGLE : -HALF_PIE_MAX_ANGLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float value = mDirection == CLOCKWISE ? mCurrentValue : -mCurrentValue;

        canvas.drawArc(mMainRect, mStartPos, mEndPos, true, mBackgroundPaint);
        canvas.drawArc(mMainRect, mStartPos, value, true, mMainPaint);
        canvas.drawArc(mFrontRect, mStartPos, mEndPos, true, mCenterPaint);
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * HALF_PIE_MAX_ANGLE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    /**
     * <p>Sets orientation of indicator as sides of the world (east west, north south) </p>
     * XML parameter {@link com.pletikosa.indicators.R.attr#half_pie_orientation}
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
        setHelperRects();
        requestLayout();
        draw();
    }

    /**
     * Parent method which is not used in {@link HalfPieIndicator}.
     * Use {@link #setOrientation(com.pletikosa.indicators.consts.Orientation)} instead.
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
                mMiddleY = mOrientation == SOUTH ? 0 : mHeight;
                break;
            case EAST:
            case WEST:
                mMiddleX = mOrientation == EAST ? mWidth / 2 - mRadius / 2 : mWidth / 2 + mRadius / 2;
                mMiddleY = mHeight / 2;
        }
    }

    private int calculateStartAngle() {
        switch (mOrientation) {
            case SOUTH:
                return mDirection == CLOCKWISE ? 0 : 180;
            case NORTH:
                return mDirection == CLOCKWISE ? 180 : 0;
            case EAST:
                return mDirection == CLOCKWISE ? 270 : 90;
            case WEST:
                return mDirection == CLOCKWISE ? 90 : 270;
            default:
                return mDirection == CLOCKWISE ? 0 : 180;
        }
    }
}
