package com.github.bernardpletikosa.indicators;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.github.bernardpletikosa.indicators.consts.SizeUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static android.animation.ValueAnimator.AnimatorUpdateListener;
import static com.github.bernardpletikosa.indicators.consts.Defaults.DEFAULT_ANIM_DURATION;
import static com.github.bernardpletikosa.indicators.consts.Defaults.DEFAULT_MAX_VALUE;
import static com.github.bernardpletikosa.indicators.consts.Defaults.DEFAULT_MIN_VALUE;
import static com.github.bernardpletikosa.indicators.consts.Defaults.NO_VALUE;

public abstract class IndicatorView extends View {

    protected final Context mContext;

    protected float mMinValue;
    protected float mMaxValue;
    protected float mValueRange;

    protected float mOldValue;
    protected float mTargetValue;
    protected float mCurrentValue;

    protected Paint mMainPaint = new Paint();
    protected Paint mBackgroundPaint = new Paint();

    //Indicator text
    protected Paint mTextPaint = new Paint();
    protected int mTextPositionX;
    protected int mTextPositionY;
    protected boolean mTextShow = true;
    protected boolean mTextAnimate = true;
    protected boolean mTextValueDecimal = false;
    protected int mTextSize = 100;
    protected String mTextPrefix = "";
    protected String mTextSuffix = "";

    protected int mAnimationDuration;
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mTargetValue != NO_VALUE) draw();
    }

    private void setXmlValues(TypedArray array) {
        float min = array.getFloat(R.styleable.Indicators_min_value, DEFAULT_MIN_VALUE);
        float max = array.getFloat(R.styleable.Indicators_max_value, DEFAULT_MAX_VALUE);
        setRange(min, max);

        mTargetValue = array.getFloat(R.styleable.Indicators_target_value, NO_VALUE);
        mAnimationDuration = array.getInt(R.styleable.Indicators_animation_duration, DEFAULT_ANIM_DURATION);

        mTextShow = array.getBoolean(R.styleable.Indicators_text_show, true);
        mTextAnimate = array.getBoolean(R.styleable.Indicators_text_animate, true);
        mTextSize = (int) array.getDimension(R.styleable.Indicators_text_size, DEFAULT_MAX_VALUE);
        setTextIndicationDecimal(array.getBoolean(R.styleable.Indicators_text_value_decimal, false));

        mTextPrefix = array.getString(R.styleable.Indicators_text_prefix);
        if (mTextPrefix == null) mTextPrefix = "";
        mTextSuffix = array.getString(R.styleable.Indicators_text_suffix);
        if (mTextSuffix == null) mTextSuffix = "";

        setColors(array);
    }

    private void setColors(TypedArray array) {
        int color = array.getColor(R.styleable.Indicators_main_color, R.color.main);
        setMainColor(color);
        color = array.getColor(R.styleable.Indicators_background_color, R.color.background);
        setBackGroundColor(color);
        color = array.getColor(R.styleable.Indicators_text_color, R.color.text);
        setTextColor(color);
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
     * XML parameters {@link com.github.bernardpletikosa.indicators.R.attr#min_value} and
     * {@link com.github.bernardpletikosa.indicators.R.attr#max_value}
     * @param minValue minimum value to display
     * @param maxValue maximum value to display
     */
    public void setRange(float minValue, float maxValue) throws IllegalArgumentException {
        checkRange(minValue, maxValue);

        mMinValue = minValue;
        mMaxValue = maxValue;
        mValueRange = Math.abs(mMaxValue - mMinValue);
    }

    /**
     * Sets value to be indicated. Value is automatically animated when this method is used.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#target_value}
     * @param value target value
     */
    public void indicate(float value) {
        if (value > mMaxValue || value < mMinValue) {
            Log.e("IndicatorView", "Target value " + value + " is out of range!");
        } else if (mTargetValue != value) {
            mTargetValue = value;
            draw();
        }
    }

    /**
     * Sets animation duration. If set to 0 there will be no animation. Default animation
     * duration is 500 milliseconds.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#animation_duration}
     * @param duration in milliseconds
     */
    public void setAnimationDuration(int duration) throws IllegalArgumentException {
        checkNegative(duration, "animation duration");
        mAnimationDuration = duration;
    }

    /**
     * Sets main color for animating value.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#main_color}
     * @param mainColor resolved color resource
     */
    public void setMainColor(int mainColor) {
        if (mainColor == 0) return;
        mMainPaint.setColor(mainColor);
        mMainPaint.setAntiAlias(true);
    }

    /**
     * Sets background color for showing whole shape below the one that is animating value.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#background_color}
     * @param backgroundColor resolved color resource
     */
    public void setBackGroundColor(int backgroundColor) {
        if (backgroundColor == 0) return;
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setAntiAlias(true);
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
        mValueAnimator.setDuration(mAnimationDuration);

        if (mAnimationListener != null) mValueAnimator.addListener(mAnimationListener);

        if (mInterpolator == null) mInterpolator = new AccelerateDecelerateInterpolator();
        mValueAnimator.setInterpolator(mInterpolator);
        mValueAnimator.addUpdateListener(getUpdateListener());
        mValueAnimator.start();
    }

    protected void drawText(Canvas canvas, float currentValue) {
        if (!mTextShow) return;

        float val = mTextAnimate ? currentValue + mMinValue : mTargetValue;
        if (mTextValueDecimal) {
            canvas.drawText(mTextPrefix + String.format("%.1f", new BigDecimal(val).setScale(1, RoundingMode.HALF_EVEN)) + mTextSuffix,
                    mTextPositionX, mTextPositionY, mTextPaint);
        } else
            canvas.drawText(mTextPrefix + (int) val + mTextSuffix,
                    mTextPositionX, mTextPositionY, mTextPaint);
    }

    /**
     * Sets text color used if text is shown.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_color}
     * @param textColor resolved color resource
     */
    public void setTextColor(int textColor) {
        if (textColor == 0) return;

        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * Sets indicator textual presentation. Set to true to show text on top of indicator
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_show}
     * @param showText true to show text, false otherwise
     */
    public void setTextIndication(boolean showText) {
        mTextShow = showText;
    }

    /**
     * Sets indicator textual presentation with or without decimal point.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_value_decimal}
     * @param floatIndication true to show decimal values, false otherwise
     */
    public void setTextIndicationDecimal(boolean floatIndication) {
        mTextValueDecimal = floatIndication;
    }

    /**
     * Sets indicator textual presentation style. Set to true to animate text change until target
     * value, false otherwise.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_animate}
     * @param animateText true to animate text change, false otherwise
     */
    public void textIndicationAnimate(boolean animateText) {
        setTextIndication(true);
        mTextAnimate = animateText;
    }

    /**
     * Sets indicator textual presentation style. Set to true to animate text change until target
     * value, false otherwise.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_size}
     * @param unit {@link com.github.bernardpletikosa.indicators.consts.SizeUnit}
     * @param size size in specified unit.
     */
    public void setTextSize(SizeUnit unit, int size) throws IllegalArgumentException {
        checkArgument(unit, "SizeUnit");
        checkNegativeOrZero(size, "text size");

        mTextSize = unit == SizeUnit.DP ? (int) dpToPixel(size) : size;
    }

    /**
     * Sets text to show before indicator value. Default is empty string.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_prefix}
     * @param textPrefix
     */
    public void setTextPrefix(String textPrefix) {
        checkArgument(textPrefix, "Text prefix");
        mTextPrefix = textPrefix;
    }

    /**
     * Sets text to show after indicator value. Default is empty string.
     * XML parameter {@link com.github.bernardpletikosa.indicators.R.attr#text_suffix}
     * @param textSuffix
     */
    public void setTextSuffix(String textSuffix) {
        checkArgument(mTextSuffix, "Text sufffix");
        mTextSuffix = textSuffix;
    }

    /**
     * @return text suffix
     */
    public String getTextSuffix() {
        return mTextSuffix;
    }

    /**
     * @return text preffix
     */
    public String getTextPrefix() {
        return mTextPrefix;
    }

    /**
     * @return text size in pixels
     */
    public int getTextSize() {
        return mTextSize;
    }

    /**
     * @return true if text is animated, false otherwise
     */
    public boolean isAnimatingText() {
        return mTextAnimate;
    }

    /**
     * @return true if text is shown, false otherwise
     */
    public boolean isShowingText() {
        return mTextShow;
    }

    /**
     * @return minimum value
     */
    public float getMinValue() {
        return mMinValue;
    }

    /**
     * @return maximum value
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * Returns range, difference between minimum and maximum
     * @return value range
     */
    public float getValueRange() {
        return mValueRange;
    }

    public float getTargetValue() {
        return mTargetValue;
    }

    /**
     * @return animation duration in milliseconds. Default value is 500 ms
     */
    public int getAnimationDuration() {
        return mAnimationDuration;
    }

    /**
     * @return interpolator
     */
    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    /**
     * @return animation listener
     */
    public Animator.AnimatorListener getAnimationListener() {
        return mAnimationListener;
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

    protected int getScreenOrientation() {
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        return display.getOrientation();
    }

    private void checkRange(float minValue, float maxValue) {
        if (minValue >= maxValue)
            throw new IllegalArgumentException("Invalid range {minValue >= maxValue}");
    }
}
