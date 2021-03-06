package com.akdogan.simplestepstatistics.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.view.ContextThemeWrapper
import com.akdogan.simplestepstatistics.FALLBACK_WEEKLY_GOAL
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.repository.StepStatisticModel
import com.akdogan.simplestepstatistics.ui.StepProgressView
import com.akdogan.simplestepstatistics.ui.main.MainActivity
import kotlin.math.min
import kotlin.math.roundToInt

class StatisticsAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i("WIDGET UPDATE", "onUpdate Called")
        val callback = callBackCreator(context)
        GoogleFitCommunicator(context).accessGoogleFitStatic(0,callback)

    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val callback = callBackCreator(context)
        GoogleFitCommunicator(context).accessGoogleFitStatic(0,callback)
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
        // TODO Should be replaced with actual weekly goal from sharedprefs
        val goal = FALLBACK_WEEKLY_GOAL
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

    progressView.draw(Canvas(bitmap))
    return bitmap
}