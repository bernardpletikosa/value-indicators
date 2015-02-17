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
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int w = calculateSize(widthMeasureSpec, width, height);
        int h = calculateSize(heightMeasureSpec, height, width);

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

    private int calculateSize(int modeSpec, int... size) {
        int mode = MeasureSpec.getMode(modeSpec);

        switch (mode) {
            case EXACTLY:
                return size[0];
            case AT_MOST:
                return mRadius > NO_VALUE ? Math.min(mRadius * 2,
                        size[0]) : size[0] > 0 ? size[0] : size[1];
            default:
                return mRadius > NO_VALUE ? mRadius * 2 : size[0] > 0 ? size[0] : size[1];
        }
    }
}
