package com.pletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.pletikosa.indicators.IndicatorView;
import com.pletikosa.indicators.R;
import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.SizeUnit;

public class PieIndicator extends IndicatorView {

    private static final float MAX_SIZE = 360f;

    protected int mMiddleX;
    protected int mMiddleY;
    protected int mRadius;
    protected int mInnerRadius;
    protected int mInnerRadiusPercent = -1;
    protected float mStartAngle;

    protected Direction mDirection;
    protected RectF mRectF = new RectF();
    protected Paint mCenterPaint = new Paint();

    public PieIndicator(Context context) {
        this(context, null);
    }

    public PieIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        setXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.Indicators, 0, 0));
    }

    private void setXmlValues(TypedArray array) {
        mCenterPaint.setColor(array.getColor(R.styleable.Indicators_centerColor,
                android.R.color.white));
        mCenterPaint.setAntiAlias(true);

        mStartAngle = array.getInt(R.styleable.Indicators_startAngle, 0);
        mDirection = Direction.values()[array.getInt(R.styleable.Indicators_circleDirection, 0)];

        mInnerRadiusPercent = array.getInt(R.styleable.Indicators_innerRadius, -1);
        if (mInnerRadiusPercent > 100)
            throw new IllegalArgumentException("InnerRadius value out of bounds");
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mMiddleX = width / 2;
        mMiddleY = height / 2;

        mRadius = mMiddleX < mMiddleY ? mMiddleX : mMiddleY;
        mInnerRadius = mInnerRadiusPercent > -1 ? (int) (mInnerRadiusPercent / 100f * mRadius) :
                mRadius / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddleX, mMiddleY, mRadius, mBackgroundPaint);

        mRectF.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);
        canvas.drawArc(mRectF, mStartAngle, mDirection == Direction.CLOCKWISE ? mCurrentValue :
                        -mCurrentValue, true,
                mMainPaint);

        canvas.drawCircle(mMiddleX, mMiddleY, mInnerRadius, mCenterPaint);
    }

    /**
     * Sets color for inner hole. For inner hole radius see #setInnerRadius
     * @param centerColor color resource id
     */
    public void setCenterPaint(int centerColor) throws IllegalArgumentException {
        if (centerColor <= 0)
            throw new IllegalArgumentException("Illegal color code.");

        mCenterPaint = new Paint(getResources().getColor(centerColor)); ;
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * Possible values are:
     * <ul>
     * <li>{@link Direction#CLOCKWISE}</li>
     * <li>{@link Direction#COUNTER_CLOCKWISE}</li>
     * </ul>
     * @param direction clockwise or counter clockwise
     */
    public void setDirection(Direction direction) throws IllegalArgumentException {
        checkArgument(direction, "direction");
        if (direction != Direction.CLOCKWISE && direction != Direction.COUNTER_CLOCKWISE)
            throw new IllegalArgumentException("Direction " + direction.name() + " not supported.");

        mDirection = direction;
        draw();
    }

    /**
     * Sets inner radius of PieIndicator. Inner radius is set as percentage of outer circle
     * radius. If radius is 0 then indicator will be without center hole..
     * @param innerRadius percentage [0, 100]
     */
    public void setInnerRadius(int innerRadius) throws IllegalArgumentException {
        if (innerRadius < 0 || innerRadius > 100)
            throw new IllegalArgumentException("InnerRadius value out of bounds");

        mInnerRadiusPercent = innerRadius;

        if (mRadius >= 0) mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);

        draw();
    }

    /**
     * Sets angle where indicator drawing will start.
     * @param startAngle [0, 360]
     */
    public void setStartingAngle(int startAngle) throws IllegalArgumentException {
        if (startAngle < 0 || startAngle > MAX_SIZE)
            throw new IllegalArgumentException("Starting angle value out of bounds.");

        mStartAngle = startAngle;
        draw();
    }

    /**
     * Sets radius of the outer circle in specified unit.
     * @param radius size in specified unit.
     */
    public void setRadius(SizeUnit unit, int radius) throws IllegalArgumentException {
        checkArgument(unit, "unit");
        checkNegativeOrZero(radius, "radius");

        mRadius = unit == SizeUnit.PX ? radius : (int) dpToPixel(radius);
        mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);

        draw();
    }

    @Override
    protected ValueAnimator.AnimatorUpdateListener getUpdateListener() {
        final float absoluteTarget = mTargetValue + Math.abs(mMinValue);

        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float maxAnimatedFraction = Math.max(animation.getAnimatedFraction(), 0.01f);
                float shift = (absoluteTarget / mValueRange) * MAX_SIZE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }
}
