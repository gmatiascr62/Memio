package com.memio.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class MemoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }
    companion object {
        val colors = intArrayOf(Color.rgb(255,241,154), Color.rgb(255,192,213), Color.rgb(174,226,255), Color.rgb(193,238,190), Color.WHITE)
        fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
            val text = prefs.getString("text_$id", "Tocá para escribir ✎") ?: ""
            val color = prefs.getInt("color_$id", 0).coerceIn(colors.indices)
            val views = RemoteViews(context.packageName, R.layout.widget_note)
            views.setTextViewText(R.id.note_text, text)
            views.setInt(R.id.note_text, "setBackgroundColor", colors[color])
            val intent = Intent(context, MainActivity::class.java).putExtra("widget_id", id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            views.setOnClickPendingIntent(R.id.note_text, PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            manager.updateAppWidget(id, views)
        }
    }
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val edit = context.getSharedPreferences("notes", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { edit.remove("text_$it").remove("color_$it") }
        edit.apply()
    }
}