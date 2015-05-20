package com.github.bernardpletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.Defaults;
import com.github.bernardpletikosa.indicators.consts.Orientation;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.github.bernardpletikosa.indicators.consts.Defaults.DEFAULT_CORRECTION;
import static com.github.bernardpletikosa.indicators.consts.Defaults.HALF_PIE_MAX_ANGLE;
import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;
import static com.github.bernardpletikosa.indicators.consts.Direction.CLOCKWISE;
import static com.github.bernardpletikosa.indicators.consts.Orientation.EAST;
import static com.github.bernardpletikosa.indicators.consts.Orientation.NORTH;
import static com.github.bernardpletikosa.indicators.consts.Orientation.SOUTH;
import static com.github.bernardpletikosa.indicators.consts.Orientation.WEST;

public class HalfPieIndicator extends PieIndicator {

    protected float mWidth;
    protected float mHeight;
    protected int mStartPos;
    protected int mEndPos;

    protected RectF mBackRect = new RectF();
    protected RectF mHelpRect = new RectF();
    protected Orientation mOrientation;

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

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        mWidth = calculateSize(widthMeasureSpec, width, height);
        mHeight = calculateSize(heightMeasureSpec, height, width);

        calculateCenter();
        calculateRadius();
        setHelperRects();

        setMeasuredDimension((int) mWidth, (int) mHeight);

        mTextPositionX = calculateTextX();
        mTextPositionY = calculateTextY();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float value = mDirection == CLOCKWISE ? mCurrentValue : -mCurrentValue;

        canvas.drawArc(mBackRect, mStartPos, mEndPos, true, mBackgroundPaint);
        canvas.drawArc(mMainRect, mStartPos, value, true, mMainPaint);
        canvas.drawArc(mHelpRect, mStartPos, mEndPos, true, mCenterPaint);

        if (mShowText)
            canvas.drawText(createText(mAnimateText), mTextPositionX, mTextPositionY, mTextPaint);
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue - mMinValue;

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                int shift = (int) ((absoluteTarget / mValueRange) * HALF_PIE_MAX_ANGLE - mOldValue);

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    /**
     * <p>Sets orientation of indicator as sides of the world (east west, north south) </p>
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#half_pie_orientation}
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
     * Use {@link #setOrientation(com.github.bernardpletikosa.indicators.consts.Orientation)} instead.
     */
    @Override
    public void setStartingAngle(int startAngle) {
        // Parent method, not used.
    }

    /**
     * @return indicator's orientation
     * <ul>
     * <li>{@link Orientation#EAST}</li>
     * <li>{@link Orientation#WEST}</li>
     * <li>{@link Orientation#NORTH}</li>
     * <li>{@link Orientation#SOUTH}</li>
     * </ul>
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    @Override float calculateSize(int modeSpec, int... size) {
        int mode = MeasureSpec.getMode(modeSpec);

        float diameter = mOrientation == EAST || mOrientation == WEST ? mRadius * 2 : mRadius;
        if (getScreenOrientation() != 0)
            diameter = mOrientation == EAST || mOrientation == WEST ? mRadius : mRadius * 2;

        switch (mode) {
            case EXACTLY:
                return size[0];
            case AT_MOST:
                return mRadius > NO_VALUE ? Math.min(diameter,
                        size[0]) : size[0] > 0 ? size[0] : size[1];
            default:
                return mRadius > NO_VALUE ? diameter : size[0] > 0 ? size[0] : size[1];
        }
    }

    private void calculateCenter() {
        final float halfW = mWidth / 2;
        final float halfR = mRadius / 2;
        final float halfH = mHeight / 2;

        if (mOrientation == NORTH || mOrientation == SOUTH) {
            mCenter.x = halfW;
            mCenter.y = mOrientation == SOUTH ? halfH - halfR : halfH + halfR;
        } else {
            mCenter.x = mOrientation == EAST ? halfW - halfR : halfW + halfR;
            mCenter.y = halfH;
        }
    }

    private void setHelperRects() {
        mBackRect.set(mCenter.x - mRadius, mCenter.y - mRadius, mCenter.x + mRadius, mCenter.y + mRadius);
        mMainRect.set(mCenter.x - mRadius, mCenter.y - mRadius, mCenter.x + mRadius, mCenter.y + mRadius);

        PointF corr = calculateCorrectedCenter();
        mHelpRect.set(corr.x - mInnerRadius, corr.y - mInnerRadius, corr.x + mInnerRadius, corr.y + mInnerRadius);

        mStartPos = StartAngleUtil.halfPieAngle(mOrientation, mDirection);
        mEndPos = mDirection == CLOCKWISE ? HALF_PIE_MAX_ANGLE : -HALF_PIE_MAX_ANGLE;
    }

    private PointF calculateCorrectedCenter() {
        float x = mOrientation == EAST ? mCenter.x - DEFAULT_CORRECTION : mOrientation == WEST ?
                mCenter.x + DEFAULT_CORRECTION : mCenter.x;
        float y = mOrientation == NORTH ? mCenter.y + DEFAULT_CORRECTION : mOrientation == SOUTH ?
                mCenter.y - DEFAULT_CORRECTION : mCenter.y;

        return new PointF(x, y);
    }

    private String createText(boolean animated) {
        float val = mTargetValue;
        if (animated)
            val = (mCurrentValue / Defaults.HALF_PIE_MAX_ANGLE) * mValueRange + mMinValue;
        return mTextPrefix + String.format("%.1f", val) + mTextSuffix;
    }

    private int calculateTextX() {
        switch (mOrientation) {
            case EAST:
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                return (int) mCenter.x;
            case WEST:
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
                return (int) mCenter.x;
            case NORTH:
            case SOUTH:
                mTextPaint.setTextAlign(Paint.Align.CENTER);
            default: return (int) mCenter.x;
        }
    }

    private int calculateTextY() {
        switch (mOrientation) {
            case NORTH: return (int) mCenter.y;
            case SOUTH: return (int) (mCenter.y + mTextPaint.getTextSize());
            default: return (int) mCenter.y;
        }
    }
}
