package com.pletikosa.indicators.line;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.R;
import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.SizeUnit;

public class LineIndicator extends IndicatorView {

    protected int mWidth;
    protected int mHeight;
    protected int mCornerRadius;
    protected Direction mDirection;

    public LineIndicator(Context context) {
        this(context, null);
    }

    public LineIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        final TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.Indicators, 0, 0);

        mDirection = Direction.values()[array.getInt(R.styleable.Indicators_lineDirection, 2)];
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        //if size is already set by user, don't change it
        if (mWidth <= 0) mWidth = width;
        if (mWidth <= 0) mHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float[] positions = calculatePositions();

        if (mCornerRadius > 0) {
            canvas.drawRoundRect(new RectF(0, 0, mWidth, mHeight), mCornerRadius, mCornerRadius, mBackgroundPaint);
            canvas.drawRoundRect(new RectF(positions[0], positions[1], positions[2], positions[3]),
                    mCornerRadius, mCornerRadius, mMainPaint);
        } else {
            canvas.drawRect(0, 0, mWidth, mHeight, mBackgroundPaint);
            canvas.drawRect(positions[0], positions[1], positions[2], positions[3], mMainPaint);
        }
    }

    private float[] calculatePositions() {
        switch (mDirection) {
            case LEFT_RIGHT:
                return new float[]{0, 0, mCurrentValue, mHeight};
            case RIGHT_LEFT:
                return new float[]{mCurrentValue, 0, mWidth, mHeight};
            case TOP_BOTTOM:
                return new float[]{0, 0, mWidth, mCurrentValue};
            case BOTTOM_TOP:
            default:
                return new float[]{0, mCurrentValue, mWidth, mHeight};
        }
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * Possible values are:
     * <ul>
     * <li>{@link Direction#LEFT_RIGHT}</li>
     * <li>{@link Direction#RIGHT_LEFT}</li>
     * <li>{@link Direction#BOTTOM_TOP}</li>
     * <li>{@link Direction#TOP_BOTTOM}</li>
     * </ul>
     * @param direction see possible values
     */
    public void setDirection(Direction direction) throws IllegalArgumentException {
        if (direction == null)
            throw new IllegalArgumentException("Direction can not be null.");
        if (direction == Direction.CLOCKWISE || direction == Direction.COUNTER_CLOCKWISE)
            throw new IllegalArgumentException("Direction " + direction.name() + " not supported.");

        mDirection = direction;
        draw();
    }

    /**
     * Sets indicator shape width and height in specified unit.
     * @param width  shape width
     * @param height shape height
     */
    public void setSize(SizeUnit unit, int width, int height) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(width, "width");
        checkNegativeOrZero(height, "height");

        mWidth = unit == SizeUnit.PX ? width : (int) dpToPixel(width);
        mHeight = unit == SizeUnit.PX ? height : (int) dpToPixel(height);

        draw();
    }

    /**
     * Sets corner radius for line indicator shape. Default value is 0, corners are not rounded
     * @param cornerRadius corner radius
     */
    public void setCornerRadius(SizeUnit unit, int cornerRadius) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegative(cornerRadius, "Corner radius");

        mCornerRadius = unit == SizeUnit.PX ? cornerRadius : (int) dpToPixel(cornerRadius);

        draw();
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = calculateShift(absoluteTarget, mValueRange);

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);
                postInvalidate();
            }
        };
    }

    private float calculateShift(float absoluteTarget, float absoluteRange) {
        switch (mDirection) {
            case LEFT_RIGHT:
                return (absoluteTarget / absoluteRange) * mWidth - mOldValue;
            case RIGHT_LEFT:
                return ((absoluteRange - absoluteTarget) / absoluteRange) * mWidth - mOldValue;
            case TOP_BOTTOM:
                return (absoluteTarget / absoluteRange) * mHeight - mOldValue;
            case BOTTOM_TOP:
                return ((absoluteRange - absoluteTarget) / absoluteRange) * mHeight - mOldValue;
        }
        return 0;
    }
}
