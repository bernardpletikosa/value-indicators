package com.github.bernardpletikosa.indicators.line;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;

import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;

public class LineIndicator extends IndicatorView {

    protected int mWidth;
    protected int mHeight;
    protected int mTotalWidth;
    protected int mTotalHeight;
    protected Direction mDirection;
    private int mEmptyWidth;
    private int mEmptyHeight;

    public LineIndicator(Context context) {
        this(context, null);
    }

    public LineIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        setXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.LineIndicator, 0, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w, h;
        mTotalWidth = MeasureSpec.getSize(widthMeasureSpec);
        mTotalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY)
            w = mTotalWidth;
        else if (widthMode == MeasureSpec.AT_MOST) {
            w = mWidth > NO_VALUE ? Math.min(mWidth, mTotalWidth) : mTotalWidth > 0 ? mTotalWidth : mTotalHeight;
            mTotalWidth = mWidth;
        } else {
            w = mWidth > NO_VALUE ? mWidth : mTotalWidth > 0 ? mTotalWidth : mTotalHeight;
            mTotalWidth = mWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY)
            h = mTotalHeight;
        else if (heightMode == MeasureSpec.AT_MOST) {
            h = mHeight > NO_VALUE ? Math.min(mHeight, mTotalHeight) : mTotalHeight > 0 ? mTotalHeight : mTotalWidth / 2;
            mTotalHeight = mHeight;
        } else {
            h = mHeight > NO_VALUE ? mHeight : mTotalHeight > 0 ? mTotalHeight : mTotalWidth / 2;
            mTotalHeight = mHeight;
        }

        if (mWidth <= NO_VALUE) mWidth = w;
        if (mHeight <= NO_VALUE) mHeight = h;

        setMeasuredDimension(w, h);
        setEmptyMeasures();

        mTextPositionX = (mWidth + 2 * mEmptyWidth) / 2;
        mTextPositionY = (int) ((mHeight + 2 * mEmptyHeight) / 2 -
                ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
    }

    private void setEmptyMeasures() {
        mEmptyWidth = mTotalWidth > 0 ? (mTotalWidth - mWidth) / 2 : 0;
        mEmptyHeight = mTotalHeight > 0 ? (mTotalHeight - mHeight) / 2 : 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float[] positions = calculatePositions(mEmptyWidth, mEmptyHeight);
        canvas.drawRect(mEmptyWidth, mEmptyHeight, mWidth + mEmptyWidth, mHeight + mEmptyHeight, mBackgroundPaint);
        canvas.drawRect(positions[0], positions[1], positions[2], positions[3], mMainPaint);

        drawText(canvas, calculateText());
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#line_direction}
     * Possible values are:
     * <ul>
     * <li>{@link Direction#LEFT_RIGHT}</li>
     * <li>{@link Direction#RIGHT_LEFT}</li>
     * <li>{@link Direction#BOTTOM_TOP}</li>
     * <li>{@link Direction#TOP_BOTTOM}</li>
     * </ul>
     * XML parameter {@link}
     * @param direction see possible values
     */
    public void setDirection(Direction direction) throws IllegalArgumentException {
        checkArgument(direction, "direction");
        if (direction == Direction.CLOCKWISE || direction == Direction.COUNTER_CLOCKWISE)
            throw new IllegalArgumentException("Direction " + direction.name() + " not supported.");

        mDirection = direction;

        requestLayout();
        draw();
    }

    /**
     * Sets indicator shape width and height in specified unit.
     * XML parameters {@link com.github.bernardpletikosa.indicators.R.attr#line_width} and
     * {@link com.github.bernardpletikosa.indicators.R.attr#line_height}
     * @param width  shape width
     * @param height shape height
     */
    public void setSize(SizeUnit unit, int width, int height) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(width, "width");
        checkNegativeOrZero(height, "height");

        mWidth = unit == SizeUnit.PX ? width : (int) dpToPixel(width);
        mHeight = unit == SizeUnit.PX ? height : (int) dpToPixel(height);

        requestLayout();
        draw();
    }

    /**
     * @return indicator's width in pixels
     */
    public int getIndicatorWidth() {
        return mWidth;
    }

    /**
     * @return indicator's height in pixels
     */
    public int getIndicatorHeight() {
        return mHeight;
    }

    /**
     * @return indicator's direction
     * <ul>
     * <li>{@link Direction#LEFT_RIGHT}</li>
     * <li>{@link Direction#RIGHT_LEFT}</li>
     * <li>{@link Direction#BOTTOM_TOP}</li>
     * <li>{@link Direction#TOP_BOTTOM}</li>
     * </ul>
     */
    public Direction getDirection() {
        return mDirection;
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

    private void setXmlValues(TypedArray array) {
        mWidth = (int) array.getDimension(R.styleable.LineIndicator_line_width, NO_VALUE);
        mHeight = (int) array.getDimension(R.styleable.LineIndicator_line_height, NO_VALUE);
        mDirection = Direction.values()[array.getInt(R.styleable.LineIndicator_line_direction, 2)];
    }

    //Calculates shift depending on direction
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

    //Calculates rectangle corners position
    private float[] calculatePositions(int emptyWidth, int emptyHeight) {
        switch (mDirection) {
            case LEFT_RIGHT:
                return new float[]{emptyWidth, emptyHeight, mCurrentValue + emptyWidth, mHeight + emptyHeight};
            case RIGHT_LEFT:
                return new float[]{mCurrentValue + emptyWidth, emptyHeight, mWidth + emptyWidth,
                        mHeight + emptyHeight};
            case TOP_BOTTOM:
                return new float[]{emptyWidth, emptyHeight, mWidth + emptyWidth, mCurrentValue + emptyHeight};
            case BOTTOM_TOP:
            default:
                return new float[]{emptyWidth, mCurrentValue + emptyHeight, mWidth + emptyWidth,
                        mHeight + emptyHeight};
        }
    }

    private float calculateText() {
        switch (mDirection) {
            case LEFT_RIGHT:
                return mCurrentValue / mWidth * mValueRange;
            case RIGHT_LEFT:
                return (1 - mCurrentValue / mWidth) * mValueRange;
            case TOP_BOTTOM:
                return mCurrentValue / mHeight * mValueRange;
            case BOTTOM_TOP:
            default:
                return (1 - mCurrentValue / mHeight) * mValueRange;
        }
    }
}
