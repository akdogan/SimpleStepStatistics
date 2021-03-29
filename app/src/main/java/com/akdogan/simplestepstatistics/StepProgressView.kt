package com.akdogan.simplestepstatistics

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import kotlin.math.abs

class StepProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val startAngle = 270f
        private const val goalTextPaddingFactor = 0.163f
        private const val progressTextPaddingFactor = 0.036f
        private const val goalTextSizeFactor = 0.13f
        private const val progressTextSizeFactor = 0.18f
        private const val goalWidthFactor = 0.0436f
        private const val progressWidthFactor = 0.0218f
        private const val viewPaddingFactor = 0.06f//0.022f
        // Make Colors also use theme default and customizable
    }

    private var goal = 1
    private var progress = 0

    private var textPaddingProgress = 0.0f
    private var textPaddingGoal = 0.0f
    private var viewPadding = 0f
    private var sweepAngle: Float = 0.0f
    private var rect = RectF(0f, 0f, 0f, 0f)
    private var rectBackground = RectF(0f, 0f, 0f, 0f)

    // TODO: Use xml attributes for color settings
    private lateinit var goalPaint: Paint
    private fun calcGoalPaint(size: Int) {
        goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * goalWidthFactor
            color = goalColor//resources.getColor(R.color.light_blue, null)
        }
    }

    private lateinit var progressPaint: Paint
    private fun calcProgressPaint(size: Int) {
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * progressWidthFactor
            color = progressColor//resources.getColor(R.color.dark_blue, null)
        }
    }

    private lateinit var progressTextPaint: Paint
    private fun calcProgressTextPaint(size: Int) {
        progressTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = size * progressTextSizeFactor
            typeface = Typeface.create("", Typeface.BOLD)
            color = progressColor//resources.getColor(R.color.dark_blue, null)
        }
    }

    private lateinit var goalTextPaint: Paint
    private fun calcGoalTextPaint(size: Int) {
        goalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = size * goalTextSizeFactor//goalTextSize//70.0f//55.0f
            typeface = Typeface.create("", Typeface.BOLD)
            color = goalColor//resources.getColor(R.color.light_blue, null)
        }
    }

    private val solidBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        val col = getThemeColor(context, R.attr.colorSurface, Color.WHITE)
        color = col
    }

    fun setProgressBackgroundColor(@ColorInt color: Int){
        solidBackgroundPaint.color = color
    }

    fun setProgressColor(@ColorInt color: Int){
        progressColor = color
        progressPaint.color = color
        progressTextPaint.color = color
    }

    fun setGoalColor(@ColorInt color: Int){
        goalColor = color
        goalPaint.color = color
        goalTextPaint.color = color
    }

    private var goalColor = getThemeColor(
        context,
        R.attr.colorPrimary,
        Color.LTGRAY
    )

    private var progressColor = getThemeColor(
        context,
        R.attr.colorSecondary,
        Color.BLACK
    )

    @ColorInt
    private fun getThemeColor(
        context: Context,
        @AttrRes attr: Int,
        @ColorInt default: Int
    ): Int{
        val typedVal = TypedValue()
        return if (context.theme.resolveAttribute(attr, typedVal, true)){
            typedVal.data
        } else {
            default
        }
    }


    /**
     * Value that corresponds to 100% / a full circle
     * @param value must be greater than 0, otherwise the absolute value is used
     */
    fun setGoal(value: Int){
        goal = if (value > 0) value else abs(value)
        calculateSweepAngle()
        invalidate()
    }

    /**
     * Value of the current Progress. If the Progress is more than the goal, a full circle is drawn
     * @param value mut be greater than 0. If the value past is lesser, then 0 will be used instead
     */
    fun setProgress(value: Int){
        progress = if (value < 0) 0 else value
        calculateSweepAngle()
        invalidate()
    }

    fun getProgress(): Int =  progress



    private fun calculateSweepAngle(){
        sweepAngle =  360.0f * (progress.toFloat() / goal.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateSweepAngle()
        kotlin.math.min(w, h).let {
            calcProgressPaint(it)
            calcGoalPaint(it)
            calcProgressTextPaint(it)
            calcGoalTextPaint(it)
            textPaddingGoal = it * goalTextPaddingFactor
            textPaddingProgress = it * progressTextPaddingFactor
            viewPadding = it * viewPaddingFactor

            (it / 2.0f).let{ halfSize ->
                rectBackground = RectF(
                    (w / 2.0f) - halfSize,
                    (h / 2.0f) - halfSize,
                    (w / 2.0f) + halfSize,
                    (h / 2.0f) + halfSize
                )
                rect = RectF(
                    (w / 2.0f) - halfSize + viewPadding,
                    (h / 2.0f) - halfSize + viewPadding,
                    (w / 2.0f) + halfSize - viewPadding,
                    (h / 2.0f) + halfSize - viewPadding
                )
            }

            Log.i("VIEW SIZE", "RectPos is: $rect")
        }
    }


    override fun onDraw(canvas: Canvas) {

        // Draw solid background
        canvas.drawArc(rectBackground, startAngle, 360f, true, solidBackgroundPaint)
        // Draw Background Track (Goal)
        canvas.drawArc(rect, startAngle, 360f, false, goalPaint)
        // Draw Foreground Track (Progress)
        canvas.drawArc(rect, startAngle, sweepAngle, false, progressPaint)
        // Draw the labels

        canvas.drawText(resources.getString(R.string.num_format, progress), (width / 2).toFloat(), ((height / 2) - textPaddingProgress), progressTextPaint )
        canvas.drawText(resources.getString(R.string.num_format, goal), (width / 2).toFloat(), ((height / 2) + textPaddingGoal), goalTextPaint )


    }

}

fun StepProgressView.runAnimation(duration: Long = 1200){
    val animator = ObjectAnimator.ofInt(
        this,
        "progress",
        0,
        this.getProgress()
    )
    animator.duration = duration
    animator.start()
}


















