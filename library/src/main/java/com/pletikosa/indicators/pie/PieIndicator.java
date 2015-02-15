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
import com.pletikosa.indicators.consts.Defaults;
import com.pletikosa.indicators.consts.Direction;
import com.pletikosa.indicators.consts.SizeUnit;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.pletikosa.indicators.consts.Defaults.NO_VALUE;

public class PieIndicator extends IndicatorView {

    protected int mMiddleX;
    protected int mMiddleY;
    protected int mRadius;
    protected int mInnerRadius;
    protected int mInnerRadiusPercent = NO_VALUE;
    protected float mStartAngle;

    protected Direction mDirection;
    protected RectF mMainRect = new RectF();
    protected Paint mCenterPaint = new Paint();

    public PieIndicator(Context context) {
        this(context, null);
    }

    public PieIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        loadXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieIndicator, 0, 0));
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

        calculateRadius();

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mMiddleX, mMiddleY, mRadius, mBackgroundPaint);

        mMainRect.set(mMiddleX - mRadius, mMiddleY - mRadius, mMiddleX + mRadius, mMiddleY + mRadius);
        canvas.drawArc(mMainRect, mStartAngle, mDirection == Direction.CLOCKWISE ? mCurrentValue :
                -mCurrentValue, true, mMainPaint);

        canvas.drawCircle(mMiddleX, mMiddleY, mInnerRadius, mCenterPaint);
    }

    /**
     * Sets color for inner hole. For inner hole radius see #setInnerRadius
     * XML parameter {@link com.pletikosa.indicators.R.attr#pie_center_paint}
     * @param centerColor color resource id
     */
    public void setCenterPaint(int centerColor) throws IllegalArgumentException {
        mCenterPaint = new Paint(getResources().getColor(centerColor)); ;
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * XML parameter {@link com.pletikosa.indicators.R.attr#pie_direction}
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
        
        requestLayout();
        draw();
    }

    /**
     * Sets inner radius of PieIndicator. Inner radius is set as percentage of outer circle
     * radius. If radius is 0 then indicator will be without center hole..
     * XML parameter {@link com.pletikosa.indicators.R.attr#pie_inner_radius}
     * @param innerRadius percentage [0, 100]
     */
    public void setInnerRadius(int innerRadius) throws IllegalArgumentException {
        if (innerRadius < 0 || innerRadius > 100)
            throw new IllegalArgumentException("InnerRadius value out of bounds");

        mInnerRadiusPercent = innerRadius;

        if (mRadius >= 0) mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);

        requestLayout();
        draw();
    }

    /**
     * Sets angle where indicator drawing will start.
     * XML parameter {@link com.pletikosa.indicators.R.attr#pie_start_angle}
     * @param startAngle [0, 360]
     */
    public void setStartingAngle(int startAngle) throws IllegalArgumentException {
        if (startAngle < 0 || startAngle > Defaults.PIE_MAX_ANGLE)
            throw new IllegalArgumentException("Starting angle value out of bounds.");

        mStartAngle = startAngle;

        requestLayout();
        draw();
    }

    /**
     * Sets radius of the outer circle in specified unit.
     * XML parameter {@link com.pletikosa.indicators.R.attr#pie_radius}
     * @param radius size in specified unit.
     */
    public void setRadius(SizeUnit unit, int radius) throws IllegalArgumentException {
        checkArgument(unit, "unit");
        checkNegativeOrZero(radius, "radius");

        mRadius = unit == SizeUnit.PX ? radius : (int) dpToPixel(radius);
        mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);

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
                float shift = (absoluteTarget / mValueRange) * Defaults.PIE_MAX_ANGLE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    protected void loadXmlValues(TypedArray array) {
        mCenterPaint.setColor(array.getColor(R.styleable.PieIndicator_pie_center_paint, android.R.color.white));
        mCenterPaint.setAntiAlias(true);

        mStartAngle = array.getInt(R.styleable.PieIndicator_pie_start_angle, 0);
        mDirection = Direction.values()[array.getInt(R.styleable.PieIndicator_pie_direction,
                0)];

        mRadius = (int) array.getDimension(R.styleable.PieIndicator_pie_radius, NO_VALUE);
        mInnerRadiusPercent = array.getInt(R.styleable.PieIndicator_pie_inner_radius, NO_VALUE);
        if (mInnerRadiusPercent > 100)
            throw new IllegalArgumentException("InnerRadius value out of bounds");
    }

    protected void calculateRadius() {
        if (mRadius <= NO_VALUE)
            mRadius = mMiddleX < mMiddleY ? mMiddleX : mMiddleY;
       
        if (mInnerRadiusPercent <= NO_VALUE)
            mInnerRadius = mRadius / 2;
        else
            mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);
    }
}
