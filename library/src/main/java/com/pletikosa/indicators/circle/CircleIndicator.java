package com.pletikosa.indicators.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.consts.SizeUnit;

public class CircleIndicator extends IndicatorView {

    protected int[] mMiddle = new int[2];
    protected int mCircleRadius = 0;

    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mMiddle[0] = width / 2;
        mMiddle[1] = height / 2;

        if (mCircleRadius <= 0)
            mCircleRadius = mMiddle[0] < mMiddle[1] ? mMiddle[0] : mMiddle[1];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddle[0], mMiddle[1], mCircleRadius, mBackgroundPaint);
        canvas.drawCircle(mMiddle[0], mMiddle[1], mCurrentValue, mMainPaint);
    }

    /**
     * Sets outer radius of the circle.
     * @param radius size in specified unit.
     */
    public void setRadius(SizeUnit unit, int radius) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(radius, "circle radius");

        mCircleRadius = unit == SizeUnit.PX ? radius : (int) dpToPixel(radius);
        draw();
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * mCircleRadius - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }
}
