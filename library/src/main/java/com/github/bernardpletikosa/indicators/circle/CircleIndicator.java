package com.github.bernardpletikosa.indicators.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;

public class CircleIndicator extends IndicatorView {

    protected float mRadius = 0;
    protected PointF mCenter = new PointF();

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

        float w = calculateSize(widthMeasureSpec, width, height);
        float h = calculateSize(heightMeasureSpec, height, width);

        mCenter.x = w / 2;
        mCenter.y = h / 2;

        if (mRadius <= NO_VALUE)
            mRadius = mCenter.x < mCenter.y ? mCenter.x : mCenter.y;

        setMeasuredDimension((int) w, (int) h);

        mTextPositionX = (int) mCenter.x;
        mTextPositionY = (int) (mCenter.y - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mBackgroundPaint);
        canvas.drawCircle(mCenter.x, mCenter.y, mCurrentValue, mMainPaint);

        if (mShowText)
            canvas.drawText(createText(mAnimateText), mTextPositionX, mTextPositionY, mTextPaint);
    }

    private String createText(boolean animated) {
        float val = mTargetValue;
        if (animated)
            val = (mCurrentValue / mRadius) * mValueRange - Math.abs(mMinValue);
        return mTextPrefix + String.format("%.1f", val) + mTextSuffix;
    }

    /**
     * Sets outer radius of the circle.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#circle_radius} (only in dp)
     * @param radius size in specified unit.
     */
    public void setRadius(SizeUnit unit, int radius) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(radius, "circle radius");

        mRadius = unit == SizeUnit.PX ? radius : (int) dpToPixel(radius);

        requestLayout();
        draw();
    }

    /**
     * Returns outer radius of the circle.
     * @return radius of the circle
     */
    public float getRadius() {
        return mRadius;
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);
        mUpdateText = true;

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

    private float calculateSize(int modeSpec, int... size) {
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
