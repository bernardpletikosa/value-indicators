package com.pletikosa.indicators;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import static android.animation.ValueAnimator.AnimatorUpdateListener;

public abstract class IndicatorView extends View {

    private static final float DEFAULT_MIN_VALUE = 0;
    private static final float DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_ANIMATION_DURATION = 500;

    private final Context mContext;

    protected float mMinValue;
    protected float mMaxValue;
    protected float mValueRange;

    protected float mOldValue;
    protected float mTargetValue;
    protected float mCurrentValue;

    protected Paint mMainPaint = new Paint();
    protected Paint mBackgroundPaint = new Paint();

    protected int mDuration;
    protected Interpolator mInterpolator;
    protected ValueAnimator mValueAnimator;
    protected Animator.AnimatorListener mAnimationListener;

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        mContext = context;

        setXmlValues(context.getTheme().obtainStyledAttributes(attrs, R.styleable.Indicators, 0, 0));
    }

    private void setXmlValues(TypedArray array) {
        mMinValue = array.getFloat(R.styleable.Indicators_minValue, DEFAULT_MIN_VALUE);
        mMaxValue = array.getFloat(R.styleable.Indicators_maxValue, DEFAULT_MAX_VALUE);

        mValueRange = Math.abs(mMaxValue - mMinValue);

        mDuration = array.getInt(R.styleable.Indicators_animationDuration,
                DEFAULT_ANIMATION_DURATION);

        setColors(array);
    }

    private void setColors(TypedArray a) {
        mMainPaint.setColor(a.getColor(R.styleable.Indicators_mainColor,
                android.R.color.tab_indicator_text));
        mMainPaint.setAntiAlias(true);

        mBackgroundPaint.setColor(a.getColor(R.styleable.Indicators_backgroundColor,
                android.R.color.secondary_text_dark_nodisable));
        mBackgroundPaint.setAntiAlias(true);
    }

    /**
     * Sets {@link android.view.animation.Interpolator}.
     * Default interpolator is{@link android.view.animation.AccelerateDecelerateInterpolator}
     * @param interpolator see {@link android.view.animation.Interpolator}
     */
    public void setInterpolator(Interpolator interpolator) throws IllegalArgumentException {
        checkArgument(interpolator, "interpolator");
        mInterpolator = interpolator;
    }

    /**
     * Sets {@link android.animation.Animator.AnimatorListener}.
     */
    public void setAnimationListener(Animator.AnimatorListener listener) throws IllegalArgumentException {
        checkArgument(listener, "AnimationListener");
        mAnimationListener = listener;
    }

    /**
     * Method sets values range i.e. minimum and maximum values to display.
     * Default values are [0, 100].
     * @param minValue minimum value to display
     * @param maxValue maximum value to display
     */
    public void setRange(int minValue, int maxValue) throws IllegalArgumentException {
        if (minValue >= maxValue)
            throw new IllegalArgumentException("Invalid range {minValue >= maxValue}");

        mMinValue = mTargetValue = minValue;
        mMaxValue = maxValue;
        mValueRange = Math.abs(mMaxValue - mMinValue);
    }

    /**
     * Sets value to be indicated.
     * Value is automatically indicated when this value is set.
     * @param target target value
     */
    public void indicate(float target) {
        if (target > mMaxValue || target < mMinValue) {
            Log.e("IndicatorView", "Target value " + target + " is out of range!");
        } else if (mTargetValue != target) {
            mTargetValue = target;
            draw();
        }
    }

    /**
     * Sets animation duration. If set to 0 there will be no animation. Default animation
     * duration is 500 milliseconds.
     * @param duration in milliseconds
     */
    public void setAnimationDuration(int duration) throws IllegalArgumentException {
        checkNegative(duration, "animation duration");
        mDuration = duration;
    }

    /**
     * Sets main color for animating value.
     * @param mainColor color resource id
     */
    public void setMainColor(int mainColor) throws IllegalArgumentException {
        if (mainColor <= 0)
            throw new IllegalArgumentException("Illegal color code.");

        mMainPaint = new Paint(getResources().getColor(mainColor));
    }

    /**
     * Sets background color for showing whole shape below the one that is animating value.
     * @param backgroundColor color resource id
     */
    public void setBackGroundColor(int backgroundColor) throws IllegalArgumentException {
        if (backgroundColor <= 0)
            throw new IllegalArgumentException("Illegal color code.");

        mBackgroundPaint = new Paint(getResources().getColor(backgroundColor));
    }

    /**
     * Returns animating state.
     * @return true if animation is in progress, false otherwise
     */
    public boolean isAnimating() {
        return mValueAnimator != null && mValueAnimator.isRunning();
    }

    /**
     * Method cancels animation in progress/
     * @return true if animation is stopped, false otherwise
     */
    public boolean cancelAnimating() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            return true;
        }

        return false;
    }

    /**
     * Main method for animating value changes. It uses {@link android.animation.ValueAnimator}
     * to time animations correctly. This method is automatically called when value is set
     * with {@link #indicate(float)} method
     * Uses {@link AnimatorUpdateListener} provided by #getUpdateListener method.
     */
    public void draw() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) return;

        if (isAnimating()) cancelAnimating();

        mOldValue = mCurrentValue;

        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.setDuration(mDuration);

        if (mAnimationListener != null) mValueAnimator.addListener(mAnimationListener);

        if (mInterpolator == null) mInterpolator = new AccelerateDecelerateInterpolator();
        mValueAnimator.setInterpolator(mInterpolator);
        mValueAnimator.addUpdateListener(getUpdateListener());
        mValueAnimator.start();
    }

    /**
     * Method must be overriden for every specific type of indicator.
     * Use {@link AnimatorUpdateListener#onAnimationUpdate(android.animation.ValueAnimator)} to
     * calculate current value before every drawing cycle.
     * @return {@link AnimatorUpdateListener}
     */
    protected abstract AnimatorUpdateListener getUpdateListener();

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     * @param dp value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return float value to represent px equivalent to dp depending on device density
     */
    protected float dpToPixel(float dp) {
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    protected void checkArgument(Object argument, String name) {
        if (argument == null)
            throw new IllegalArgumentException(name + " can't be null.");
    }

    protected void checkNegative(int argument, String name) {
        if (argument < 0)
            throw new IllegalArgumentException("Argument " + name + " can't be less than 0.");
    }

    protected void checkNegativeOrZero(int argument, String name) {
        if (argument <= 0)
            throw new IllegalArgumentException("Argument " + name + " can't be less than 0.");
    }
}
