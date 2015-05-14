package com.github.bernardpletikosa.indicators.triangle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;

import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;

public class TriangleIndicator extends IndicatorView {

    protected int mWidth;
    protected int mHeight;
    protected int mTotalWidth;
    protected int mTotalHeight;
    protected Direction mDirection;

    private int mEmptyWidth;
    private int mEmptyHeight;
    private Path mBackgroundPath = new Path();
    private Path mMainPath = new Path();

    public TriangleIndicator(Context context) {
        this(context, null);
    }

    public TriangleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriangleIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        setXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.Triangle, 0, 0));
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
        setHelperPath();

        mTextPositionX = (int) (mEmptyWidth + (2f / 3f * mWidth));
        mTextPositionY = (int) ((2f / 3f * mHeight) + mEmptyHeight -
                ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
    }

    private void setHelperPath() {
        mEmptyWidth = mTotalWidth > 0 ? (mTotalWidth - mWidth) / 2 : 0;
        mEmptyHeight = mTotalHeight > 0 ? (mTotalHeight - mHeight) / 2 : 0;

        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mMainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        setBackgroundPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final PointF[] positions = calculatePositions();
        mMainPath.reset();
        mMainPath.moveTo(positions[0].x, positions[0].y);
        mMainPath.lineTo(positions[1].x, positions[1].y);
        mMainPath.lineTo(positions[2].x, positions[2].y);
        mMainPath.close();

        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        canvas.drawPath(mMainPath, mMainPaint);

        if (mShowText)
            canvas.drawText(createText(mAnimateText), mTextPositionX, mTextPositionY, mTextPaint);
    }

    private String createText(boolean animated) {
        float val = mTargetValue;
        if (animated)
            val = (mCurrentValue / mWidth) * mValueRange - Math.abs(mMinValue);
        return mTextPrefix + String.format("%.1f", val) + mTextSuffix;
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#line_direction}
     * Possible values are:
     * <ul>
     * <li>{@link Direction#LEFT_RIGHT}</li>
     * <li>{@link Direction#RIGHT_LEFT}</li>
     * </ul>
     * XML parameter {@link}
     * @param direction see possible values
     */
    public void setDirection(Direction direction) throws IllegalArgumentException {
        checkArgument(direction, "direction");
        if (direction != Direction.LEFT_RIGHT && direction != Direction.RIGHT_LEFT)
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
        mWidth = (int) array.getDimension(R.styleable.Triangle_triangle_width, NO_VALUE);
        mHeight = (int) array.getDimension(R.styleable.Triangle_triangle_height, NO_VALUE);
        mDirection = Direction.values()[array.getInt(R.styleable
                .Triangle_triangle_direction, 2)];
    }

    //Calculates shift depending on direction
    private float calculateShift(float absoluteTarget, float absoluteRange) {
        switch (mDirection) {
            case LEFT_RIGHT:
                return (absoluteTarget / absoluteRange) * mWidth - mOldValue;
            case RIGHT_LEFT:
                return ((absoluteRange - absoluteTarget) / absoluteRange) * mWidth - mOldValue;
        }
        return 0;
    }

    //Calculates rectangle corners position
    private void setBackgroundPath() {
        switch (mDirection) {
            case LEFT_RIGHT:
                mBackgroundPath = new Path();
                mBackgroundPath.moveTo(mEmptyWidth, mEmptyHeight + mHeight);
                mBackgroundPath.lineTo(mWidth + mEmptyWidth, mEmptyHeight);
                mBackgroundPath.lineTo(mWidth + mEmptyWidth, mEmptyHeight + mHeight);
                mBackgroundPath.close();
                return;
            case RIGHT_LEFT:
                mBackgroundPath = new Path();
                mBackgroundPath.moveTo(mEmptyWidth, mEmptyHeight + mHeight);
                mBackgroundPath.lineTo(mEmptyWidth, mEmptyHeight);
                mBackgroundPath.lineTo(mWidth + mEmptyWidth, mEmptyHeight + mHeight);
                mBackgroundPath.close();
        }
    }

    //Calculates rectangle corners position
    private PointF[] calculatePositions() {
        switch (mDirection) {
            case RIGHT_LEFT:
                return new PointF[]{new PointF(mEmptyWidth + mWidth, mEmptyHeight + mHeight),
                        new PointF(mEmptyWidth + mWidth - mCurrentValue,
                                mEmptyHeight + mHeight * (1 - mCurrentValue / mWidth)),
                        new PointF(mEmptyWidth + mWidth - mCurrentValue, mEmptyHeight + mHeight)};
            case LEFT_RIGHT:
                return new PointF[]{new PointF(mEmptyWidth, mEmptyHeight + mHeight),
                        new PointF(mEmptyWidth + mCurrentValue, mEmptyHeight + mHeight * (1 - mCurrentValue / mWidth)),
                        new PointF(mEmptyWidth + mCurrentValue, mEmptyHeight + mHeight)};
        }
        return new PointF[]{};
    }
}
