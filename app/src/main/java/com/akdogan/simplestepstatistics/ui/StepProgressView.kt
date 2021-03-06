package com.akdogan.simplestepstatistics.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.akdogan.simplestepstatistics.R
import kotlin.math.abs

class StepProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val startAngle = 270f

        // Scaling Values
        private const val goalTextPaddingFactor = 0.163f
        private const val progressTextPaddingFactor = 0.036f
        private const val goalTextSizeFactor = 0.13f
        private const val progressTextSizeFactor = 0.18f
        private const val goalWidthFactor = 0.0436f
        private const val progressWidthFactor = 0.0218f
        private const val viewPaddingFactor = 0.06f

        // Default Fallback Colors
        private const val goalColorDefault = Color.LTGRAY
        private const val progressColorDefault = Color.DKGRAY
        private const val circleBackgroundColorDefault = Color.WHITE
    }

    private var goal = 1
    private var progress = 0

    private var textPaddingProgress = 0.0f
    private var textPaddingGoal = 0.0f
    private var viewPadding = 0f
    private var sweepAngle: Float = 0.0f
    private var rect = RectF(0f, 0f, 0f, 0f)
    private var rectBackground = RectF(0f, 0f, 0f, 0f)

    private var goalPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = goalColor
    }

    private fun calcGoalPaint(size: Int) {
        goalPaint.strokeWidth = size * goalWidthFactor
    }

    private var progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = progressColor
    }

    private fun calcProgressPaint(size: Int) {
        progressPaint.strokeWidth = size * progressWidthFactor
    }

    private var progressTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
        color = progressColor
    }

    private fun calcProgressTextPaint(size: Int) {
        progressTextPaint.textSize = size * progressTextSizeFactor
    }

    private var goalTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("", Typeface.BOLD)
        color = goalColor
    }

    private fun calcGoalTextPaint(size: Int) {
        goalTextPaint.textSize = size * goalTextSizeFactor
    }

    private var circleBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = circleBackgroundColor
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.StepProgressView) {
            goalColor = getColor(
                R.styleable.StepProgressView_goalColor,
                getThemeColor(context, R.attr.colorPrimary, goalColorDefault)
            )
            progressColor = getColor(
                R.styleable.StepProgressView_progressColor,
                getThemeColor(context, R.attr.colorSecondary, progressColorDefault)
            )
            circleBackgroundColor = getColor(
                R.styleable.StepProgressView_circleBackgroundColor,
                getThemeColor(context, R.attr.colorSurface, circleBackgroundColorDefault)
            )

        }
    }

    @ColorInt
    var goalColor = 0
        set(value) {
            field = value
            goalPaint.color = value
            goalTextPaint.color = value
            invalidate()
        }

    @ColorInt
    var progressColor = 0
        set(value) {
            field = value
            progressPaint.color = value
            progressTextPaint.color = value
        }

    @ColorInt
    var circleBackgroundColor = 0
        set(value) {
            field = value
            circleBackgroundPaint.color = value
        }


    @ColorInt
    private fun getThemeColor(
        context: Context,
        @AttrRes attr: Int,
        @ColorInt default: Int
    ): Int {
        val typedVal = TypedValue()
        return if (context.theme.resolveAttribute(attr, typedVal, true)) {
            typedVal.data
        } else {
            default
        }
    }


    /**
     * Value that corresponds to 100% / a full circle
     * @param value must be greater than 0, otherwise the absolute value is used
     */
    fun setGoal(value: Int) {
        goal = if (value > 0) value else abs(value)
        calculateSweepAngle()
        invalidate()
    }

    /**
     * Value of the current Progress. If the Progress is more than the goal, a full circle is drawn
     * @param value mut be greater than 0. If the value past is lesser, then 0 will be used instead
     */
    fun setProgress(value: Int) {
        progress = if (value < 0) 0 else value
        calculateSweepAngle()
        invalidate()
    }

    fun getProgress(): Int = progress


    private fun calculateSweepAngle() {
        sweepAngle = 360.0f * (progress.toFloat() / goal.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateSweepAngle()
        val availableWidth = w - paddingStart - paddingEnd
        val availableHeight = h - paddingTop - paddingBottom
        // Calculating sizes with the available view size after taking system padding into account
        kotlin.math.min(availableWidth, availableHeight).let {
            calcProgressPaint(it)
            calcGoalPaint(it)
            calcProgressTextPaint(it)
            calcGoalTextPaint(it)
            textPaddingGoal = it * goalTextPaddingFactor
            textPaddingProgress = it * progressTextPaddingFactor
            viewPadding = it * viewPaddingFactor

            // Calculating draw rectangles to take assymetric padding into account, scale to keep ratio
            val left = ((availableWidth / 2) - (it / 2) + paddingLeft).toFloat()
            val top = ((availableHeight / 2) - (it / 2) + paddingTop).toFloat()
            val right = left + it
            val bottom = top + it
            rectBackground = RectF(
                left,
                top,
                right,
                bottom
            )
            rect = RectF(
                left + viewPadding,
                top + viewPadding,
                right - viewPadding,
                bottom - viewPadding
            )
        }
    }


    override fun onDraw(canvas: Canvas) {
        // Draw solid background
        canvas.drawArc(rectBackground, startAngle, 360f, true, circleBackgroundPaint)
        // Draw Background Track (Goal)
        canvas.drawArc(rect, startAngle, 360f, false, goalPaint)
        // Draw Foreground Track (Progress)
        canvas.drawArc(rect, startAngle, sweepAngle, false, progressPaint)
        // Draw the labels
        // Calculate label x/y positions relative to the paintable rect to take padding and position
        // into account
        val x = rectBackground.left + ((rectBackground.right - rectBackground.left) / 2)
        val y = rectBackground.top + ((rectBackground.bottom - rectBackground.top) / 2)
        canvas.drawText(
            resources.getString(R.string.num_format, progress),
            x,
            (y - textPaddingProgress),
            progressTextPaint
        )
        canvas.drawText(
            resources.getString(R.string.num_format, goal),
            x,
            (y + textPaddingGoal),
            goalTextPaint
        )
    }
}

fun StepProgressView.runAnimation(duration: Long = 1200) {
    val animator = ObjectAnimator.ofInt(
        this,
        "progress",
        0,
        this.getProgress()
    )
    animator.duration = duration
    animator.start()
}


















