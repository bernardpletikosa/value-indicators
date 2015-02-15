package com.pletikosa.indicators.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.R;
import com.pletikosa.indicators.consts.SizeUnit;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.pletikosa.indicators.consts.Defaults.NO_VALUE;

public class CircleIndicator extends IndicatorView {

    protected int mRadius = 0;
    protected int mMiddleX;
    protected int mMiddleY;

    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        setXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleIndicator, 0, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w, h;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

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

        mMiddleX = w / 2;
        mMiddleY = h / 2;

        if (mRadius <= NO_VALUE)
            mRadius = mMiddleX < mMiddleY ? mMiddleX : mMiddleY;

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddleX, mMiddleY, mRadius, mBackgroundPaint);
        canvas.drawCircle(mMiddleX, mMiddleY, mCurrentValue, mMainPaint);
    }

    /**
     * Sets outer radius of the circle.
     * XML parameter {@link com.pletikosa.indicators.R.attr#circle_radius} (only in dp)
     * @param radius size in specified unit.
     */
    public void setRadius(SizeUnit unit, int radius) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(radius, "circle radius");

        mRadius = unit == SizeUnit.PX ? radius : (int) dpToPixel(radius);

        requestLayout();
        draw();
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * mRadius - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    private void setXmlValues(TypedArray array) {
        mRadius = (int) array.getDimension(R.styleable.CircleIndicator_circle_radius, NO_VALUE);
    }
}
