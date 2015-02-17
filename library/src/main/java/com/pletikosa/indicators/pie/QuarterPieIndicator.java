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
import static com.pletikosa.indicators.consts.Defaults.HALF_PIE_MAX_ANGLE;
import static com.pletikosa.indicators.consts.Defaults.NO_VALUE;
import static com.pletikosa.indicators.consts.Defaults.QUARTER_PIE_MAX_ANGLE;
import static com.pletikosa.indicators.consts.Direction.CLOCKWISE;
import static com.pletikosa.indicators.consts.Orientation.EAST;

public class QuarterPieIndicator extends HalfPieIndicator {

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
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mWidth = calculateSize(widthMeasureSpec, width, height);
        mHeight = calculateSize(heightMeasureSpec, height, width);

        calculateCenter();
        calculateRadius();
        setHelperRects();

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
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
                int shift = (int) ((absoluteTarget / mValueRange) * QUARTER_PIE_MAX_ANGLE - mOldValue);

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

        requestLayout();
        draw();
    }

    /**
     * Parent method which is not used in {@link QuarterPieIndicator}
     */
    @Override
    public void setStartingAngle(int startAngle) {
        // Parent method, not used.
    }

    @Override
    protected int calculateSize(int modeSpec, int... size) {
        int mode = MeasureSpec.getMode(modeSpec);

        switch (mode) {
            case EXACTLY:
                return size[0];
            case AT_MOST:
                return mRadius > NO_VALUE ? Math.min(mRadius, size[0]) : size[0] > 0 ? size[0] : size[1];
            default:
                return mRadius > NO_VALUE ? mRadius : size[0] > 0 ? size[0] : size[1];
        }
    }

    private void calculateCenter() {
        final int halfWidth = mWidth / 2;
        final int halfRadius = mRadius / 2;
        switch (mOrientation) {
            case NORTH_WEST:
            case SOUTH_WEST:
                mMiddleX = halfWidth + halfRadius;
                mMiddleY = mOrientation == Orientation.NORTH_WEST ? mHeight : 0;
                break;
            case NORTH_EAST:
            case SOUTH_EAST:
                mMiddleX = halfWidth - halfRadius;
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

    private void setHelperRects() {
        mMainRect.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);
        mFrontRect.set(mMiddleX - mInnerRadius, mMiddleY - mInnerRadius, mMiddleX + mInnerRadius, mMiddleY + mInnerRadius);

        mStartPos = calculateStartAngle();
        mEndPos = mDirection == CLOCKWISE ? QUARTER_PIE_MAX_ANGLE : -QUARTER_PIE_MAX_ANGLE;
    }

}
