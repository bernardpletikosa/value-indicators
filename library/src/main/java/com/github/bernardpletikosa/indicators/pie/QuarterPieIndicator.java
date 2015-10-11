package com.github.bernardpletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.Defaults;
import com.github.bernardpletikosa.indicators.consts.Orientation;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.github.bernardpletikosa.indicators.consts.Defaults.DEFAULT_CORRECTION;
import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;
import static com.github.bernardpletikosa.indicators.consts.Defaults.QUARTER_PIE_MAX_ANGLE;
import static com.github.bernardpletikosa.indicators.consts.Direction.CLOCKWISE;
import static com.github.bernardpletikosa.indicators.consts.Orientation.NORTH_EAST;
import static com.github.bernardpletikosa.indicators.consts.Orientation.NORTH_WEST;
import static com.github.bernardpletikosa.indicators.consts.Orientation.SOUTH_EAST;

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
        mWidth = mWidth == 0 ? mHeight : mWidth;
        mHeight = mHeight == 0 ? mWidth : mHeight;

        calculateCenter();
        calculateRadius();
        setHelperRects();

        setMeasuredDimension((int) mWidth, (int) mHeight);

        mTextPositionX = calculateTextX();
        mTextPositionY = calculateTextY();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float value = mDirection == CLOCKWISE ? mCurrentValue : -mCurrentValue;

        canvas.drawArc(mBackRect, mStartPos, mEndPos, true, mBackgroundPaint);
        canvas.drawArc(mMainRect, mStartPos, value, true, mMainPaint);
        canvas.drawArc(mHelpRect, mStartPos, mEndPos, true, mCenterPaint);

        drawText(canvas, mCurrentValue / Defaults.QUARTER_PIE_MAX_ANGLE * mValueRange);
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
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#quarter_pie_orientation}
     * Possible values are:
     * <ul>
     * <li>{@link Orientation#SOUTH_EAST}</li>
     * <li>{@link Orientation#SOUTH_WEST}</li>
     * <li>{@link Orientation#NORTH_EAST}</li>
     * <li>{@link Orientation#NORTH_WEST}</li>
     * </ul>
     * @param orientation {@link Orientation}
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

    @Override float calculateSize(int modeSpec, int... size) {
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

    @Override void calculateRadius() {
        if (mRadius <= 0) {
            if (mCenter.x == 0 && mCenter.y == 0)
                mRadius = Math.max(mWidth, mHeight);
            else
                mRadius = mCenter.x == 0 || mCenter.y == 0 ? Math.max(mCenter.x, mCenter.y) : Math.min(mCenter.x, mCenter.y);
        }

        if (mInnerRadiusPercent <= NO_VALUE)
            mInnerRadius = mRadius / 2;
        else
            mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);
    }

    private void calculateCenter() {
        final float halfW = mWidth / 2;
        final float halfH = mHeight / 2;
        final float halfR = Math.min(halfH, halfW);

        if (mOrientation == NORTH_WEST || mOrientation == NORTH_EAST) {
            mCenter.x = mOrientation == Orientation.NORTH_WEST ? halfW + halfR : halfW - halfR;
            mCenter.y = halfH + halfR;
        } else {
            mCenter.x = mOrientation == Orientation.SOUTH_WEST ? halfW + halfR : halfW - halfR;
            mCenter.y = halfH - halfR;
        }
    }

    private void setHelperRects() {
        mBackRect.set(mCenter.x - mRadius, mCenter.y - mRadius, mCenter.x + mRadius, mCenter.y + mRadius);
        mMainRect.set(mCenter.x - mRadius, mCenter.y - mRadius, mCenter.x + mRadius, mCenter.y + mRadius);

        PointF corr = calculateCorrectedCenter();
        mHelpRect.set(corr.x - mInnerRadius, corr.y - mInnerRadius, corr.x + mInnerRadius, corr.y + mInnerRadius);

        mStartPos = StartAngleUtil.quarterPieAngle(mOrientation, mDirection);
        mEndPos = mDirection == CLOCKWISE ? QUARTER_PIE_MAX_ANGLE : -QUARTER_PIE_MAX_ANGLE;
    }

    private PointF calculateCorrectedCenter() {
        if (mOrientation == NORTH_EAST || mOrientation == NORTH_WEST) {
            float x = mOrientation == NORTH_EAST ? mCenter.x - DEFAULT_CORRECTION : mCenter.x + DEFAULT_CORRECTION;
            float y = mCenter.y + DEFAULT_CORRECTION;
            return new PointF(x, y);
        } else {
            float x = mOrientation == SOUTH_EAST ? mCenter.x - DEFAULT_CORRECTION : mCenter.x + DEFAULT_CORRECTION;
            float y = mCenter.y - DEFAULT_CORRECTION;
            return new PointF(x, y);
        }
    }

    private int calculateTextX() {
        switch (mOrientation) {
            case NORTH_EAST:
            case SOUTH_EAST:
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                return (int) mCenter.x;
            case NORTH_WEST:
            case SOUTH_WEST:
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
                return (int) mCenter.x;
            default:
                return (int) mCenter.x;
        }
    }

    private int calculateTextY() {
        switch (mOrientation) {
            case NORTH_EAST:
            case NORTH_WEST:
                return (int) mCenter.y;
            case SOUTH_EAST:
            case SOUTH_WEST:
                return (int) ((int) mCenter.y + mTextPaint.getTextSize());
            default:
                return (int) mCenter.y;
        }
    }
}
