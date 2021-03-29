package com.akdogan.simplestepstatistics.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.StepProgressView
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.repository.StepStatisticModel
import com.akdogan.simplestepstatistics.ui.main.MainActivity
import com.akdogan.simplestepstatistics.ui.main.weeklyGoal
import kotlin.math.min
import kotlin.math.roundToInt

class StatisticsAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i("WIDGET UPDATE", "onUpdate Called")
        //enqueueWidgetUpdate(context)
        val callback = callBackCreator(context)
        GoogleFitCommunicator(context).accessGoogleFitStatic(callback)

    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        //enqueueWidgetUpdate(context)
        val callback = callBackCreator(context)
        GoogleFitCommunicator(context).accessGoogleFitStatic(callback)
    }

}



/**
 * Calculates the desired size Box for rendering a StepStatisticView into a bitmap
 * The smaller one of maxWidth / maxHeight is determined and then translated to actual pixels
 * to be used as the height / width size parameter for the drawCustomImage() Function
 * @param context Context
 * @param maxWidth maxWidth as retrieved from the current Widgets Options Bundle
 * @param maxHeight maxHeight as retrieved from the current Widgets Options Bundle
 * @return the actual pixel size of maxWidth / maxHeight (whichever is smaller), to be used as size
 * parameter for the drawCustomImage() Function
 */
fun getApproximateWidgetSizeInPx(
    context: Context,
    maxWidth: Int,
    maxHeight: Int
): Int {
    return (min(maxWidth, maxHeight) * context.resources.displayMetrics.density).roundToInt()
}

internal fun callBackCreator(
    context: Context,
): (StepStatisticModel) -> Unit {
    return {
        // Extract the required values
        val progress = it.getTotalStepCount()
        val goal = weeklyGoal
        // Get the Manager and all Widget Ids to update the views for each widget
        val manager = AppWidgetManager.getInstance(context)
        manager.getAppWidgetIds(
            ComponentName(
                context,
                StatisticsAppWidgetProvider::class.java
            )
        ).forEach { widgetId ->
            // get the Views
            val views = RemoteViews(context.packageName, R.layout.statistics_appwidget)
            // Add Pending Intent to the views
            val startAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0)
            views.setOnClickPendingIntent(R.id.widget_main, pendingIntent)
            // Prepare Image with the size of the current widget
            val widgetOptions = manager.getAppWidgetOptions(widgetId)
            val size = getApproximateWidgetSizeInPx(
                context,
                widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT),
                widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            )
            // Add image to the view
            views.setImageViewBitmap(
                R.id.widget_progress_image_view, drawCustomView(
                    context,
                    goal,
                    progress,
                    size
                )
            )
            manager.updateAppWidget(widgetId, views)
        }
        Log.i("WIDGET UPDATE", "Widget callBack done")
    }
}

fun drawCustomView(context: Context, goal: Int, progress: Int, size: Int): Bitmap {
    val contextWrapper = ContextThemeWrapper(context, R.style.ThemeSimpleStepStatistics)

    val progressView = StepProgressView(contextWrapper)
    progressView.setGoal(goal)
    progressView.setProgress(progress)
    progressView.measure(size, size)
    progressView.layout(0, 0, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    val progressColor = retrieveThemeColor(
        contextWrapper,
        R.attr.colorSecondary,
        context.resources.getColor(R.color.progress_default_color, null)
    )
    val goalColor = retrieveThemeColor(
        contextWrapper,
        R.attr.colorPrimary,
        context.resources.getColor(R.color.goal_default_color, null)
    )
    progressView.setProgressBackgroundColor(retrieveThemeColor(contextWrapper, R.attr.colorSurface, Color.WHITE))
    progressView.setGoalColor(goalColor)
    progressView.setProgressColor(progressColor)
    progressView.draw(Canvas(bitmap))
    return bitmap
}



@ColorInt
private fun retrieveThemeColor(context: Context, @AttrRes attr: Int, @ColorInt fallBack: Int): Int{
    val typedVal = TypedValue()
    return if (context.theme.resolveAttribute(/*R.attr.colorSurface*/attr, typedVal, true)){
        Log.d("COLOR THEME", "context bla returned true in app widget provider")
        typedVal.data
    } else {
        fallBack
    }
}

/*views.setTextViewText(
            R.id.widget_text_view_days_list, formatDays(
                it.getDaysAsList(),
                res
            )
        )
        views.setTextViewText(
            R.id.widget_text_view_stats, formatStats(
                it.getRequiredTodayForBreakEven(),
                it.getRequiredPerDayUpcoming(),
                res
            )
        )*/
/*views.setTextViewText(
    R.id.debug_text_view,
    DateHelper.timeToDateTimeString(System.currentTimeMillis())
)*/
