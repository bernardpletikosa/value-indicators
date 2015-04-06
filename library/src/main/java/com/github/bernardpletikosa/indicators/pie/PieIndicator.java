package com.github.bernardpletikosa.indicators.pie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.bernardpletikosa.indicators.IndicatorView;
import com.github.bernardpletikosa.indicators.R;
import com.github.bernardpletikosa.indicators.consts.Defaults;
import com.github.bernardpletikosa.indicators.consts.Direction;
import com.github.bernardpletikosa.indicators.consts.SizeUnit;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;

public class PieIndicator extends IndicatorView {

    protected float mRadius;
    protected float mInnerRadius;
    protected int mInnerRadiusPercent = NO_VALUE;
    protected int mStartAngle;

    protected Direction mDirection;
    protected RectF mMainRect = new RectF();
    protected Paint mCenterPaint = new Paint();
    protected PointF mCenter = new PointF();

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
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float w = calculateSize(widthMeasureSpec, width, height);
        float h = calculateSize(heightMeasureSpec, height, width);

        mCenter.x = w / 2;
        mCenter.y = h / 2;

        calculateRadius();

        setMeasuredDimension((int) w, (int) h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mBackgroundPaint);

        mMainRect.set(mCenter.x - mRadius, mCenter.y - mRadius, mCenter.x + mRadius, mCenter.y + mRadius);
        canvas.drawArc(mMainRect, mStartAngle, mDirection == Direction.CLOCKWISE ? mCurrentValue :
                -mCurrentValue, true, mMainPaint);

        canvas.drawCircle(mCenter.x, mCenter.y, mInnerRadius, mCenterPaint);
    }

    /**
     * Sets color for inner hole. For inner hole radius see #setInnerRadius
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#pie_center_paint}
     * @param centerColor resolved color resource
     */
    public void setCenterPaint(int centerColor) throws IllegalArgumentException {
        if (centerColor == 0) return;
        mCenterPaint = new Paint(centerColor);
    }

    /**
     * <p>Sets direction for drawing indicator in clockwise or counter clockwise direction.</p>
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#pie_direction}
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
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#pie_inner_radius}
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
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#pie_start_angle}
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
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#pie_radius}
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

    /**
     * @return indicator's radius in pixels
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * @return indicator's inner radius in pixels
     */
    public float getInnerRadius() {
        return mInnerRadius;
    }

    /**
     * @return indicator's inner radius in percentage of total radius
     */
    public int getInnerRadiusPercent() {
        return mInnerRadiusPercent;
    }

    /**
     * @return starting angle of indication
     */
    public int getStartAngle() {
        return mStartAngle;
    }

    /**
     * @return indicator's direction
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
                float shift = (absoluteTarget / mValueRange) * Defaults.PIE_MAX_ANGLE - mOldValue;

                mCurrentValue = mOldValue + (shift * maxAnimatedFraction);

                postInvalidate();
            }
        };
    }

    void loadXmlValues(TypedArray array) {
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

    void calculateRadius() {
        if (mRadius <= NO_VALUE)
            mRadius = mCenter.x < mCenter.y ? mCenter.x : mCenter.y;

        if (mInnerRadiusPercent <= NO_VALUE)
            mInnerRadius = mRadius / 2;
        else
            mInnerRadius = (int) (mInnerRadiusPercent / 100f * mRadius);
    }

    float calculateSize(int modeSpec, int... size) {
        int mode = MeasureSpec.getMode(modeSpec);

        final float diameter = mRadius * 2;
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
}
