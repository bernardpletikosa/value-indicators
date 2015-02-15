package com.pletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pletikosa.indicators.R;
import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.Orientation;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.pletikosa.indicators.consts.Defaults.NO_VALUE;
import static com.pletikosa.indicators.consts.Defaults.QUARTER_PIE_MAX_ANGLE;

public class QuarterPieIndicator extends HalfPieIndicator {

    protected int mWidth;
    protected int mHeight;
    protected Orientation mOrientation;

    public QuarterPieIndicator(Context context) {
        this(context, null);
    }

    public QuarterPieIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuarterPieIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        loadXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieIndicator, 0, 0));
        mOrientation = Orientation.values()[context.getTheme().obtainStyledAttributes(attrs, R.styleable.QuarterPieIndicator, 0, 0)
                .getInt(R.styleable.QuarterPieIndicator_quarter_pie_orientation, 4)];
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
            w = mRadius > NO_VALUE ? Math.min(mRadius, width) : width > 0 ? width : height;
        else
            w = mRadius > NO_VALUE ? mRadius : width > 0 ? width : height;

        if (heightMode == EXACTLY)
            h = height;
        else if (heightMode == AT_MOST)
            h = mRadius > NO_VALUE ? Math.min(mRadius, height) : height > 0 ? height : width;
        else
            h = mRadius > NO_VALUE ? mRadius : height > 0 ? height : width;

        mWidth = w;
        mHeight = h;

        calculateCenter();
        calculateRadius();

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddleX, mMiddleY, mRadius, mBackgroundPaint);

        mMainRect.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);

        int startAngle = calculateStartAngle();
        float value = mDirection == Direction.CLOCKWISE ? mCurrentValue : -mCurrentValue;
        canvas.drawArc(mMainRect, startAngle, value, true, mMainPaint);

        canvas.drawCircle(mMiddleX, mMiddleY, mInnerRadius, mCenterPaint);
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * QUARTER_PIE_MAX_ANGLE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    /**
     * <p>Sets orientation of indicator as sides of the world (east west, north south) </p>
     * XML parameter {@link com.pletikosa.indicators.R.attr#quarter_pie_orientation}
     * Possible values are:
     * <ul>
     * <li>{@link com.pletikosa.indicators.consts.Orientation#SOUTH_EAST}</li>
     * <li>{@link com.pletikosa.indicators.consts.Orientation#SOUTH_WEST}</li>
     * <li>{@link com.pletikosa.indicators.consts.Orientation#NORTH_EAST}</li>
     * <li>{@link com.pletikosa.indicators.consts.Orientation#NORTH_WEST}</li>
     * </ul>
     * @param orientation {@link com.pletikosa.indicators.consts.Orientation}
     */
    public void setOrientation(Orientation orientation) throws IllegalArgumentException {
        checkArgument(orientation, "orientation");

        mOrientation = orientation;
        calculateCenter();
        draw();
    }

    /**
     * Parent method which is not used in {@link QuarterPieIndicator}
     */
    @Override
    public void setStartingAngle(int startAngle) {
        // Parent method, not used.
    }

    private void calculateCenter() {
        switch (mOrientation) {
            case NORTH_WEST:
            case SOUTH_WEST:
                mMiddleX = mWidth;
                mMiddleY = mOrientation == Orientation.NORTH_WEST ? mHeight : 0;
                break;
            case NORTH_EAST:
            case SOUTH_EAST:
                mMiddleX = 0;
                mMiddleY = mOrientation == Orientation.NORTH_EAST ? mHeight : 0;
        }
    }

    private int calculateStartAngle() {
        int startAngle = 0;
        switch (mOrientation) {
            case NORTH_EAST:
                startAngle = 270; break;
            case SOUTH_EAST:
                startAngle = 0; break;
            case SOUTH_WEST:
                startAngle = 90; break;
            case NORTH_WEST:
                startAngle = 180; break;
        }

        return mDirection == Direction.CLOCKWISE ? startAngle : (startAngle + 90) % 360;
    }
}
