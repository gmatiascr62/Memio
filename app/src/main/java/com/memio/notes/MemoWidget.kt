package com.memio.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import org.json.JSONArray

class MemoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }
    companion object {
        val colors = intArrayOf(
            Color.rgb(255,241,154), Color.rgb(255,192,213),
            Color.rgb(174,226,255), Color.rgb(193,238,190), Color.WHITE
        )
        fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
            val linked = prefs.getString("linked_$id", null)
            val all = try { JSONArray(prefs.getString("all_notes", "[]")) } catch (_: Exception) { JSONArray() }
            var text = "Tocá para elegir una nota ✎"
            var color = 0
            for (i in 0 until all.length()) {
                val note = all.optJSONObject(i) ?: continue
                if (note.optString("id") == linked) {
                    text = note.optString("text")
                    color = note.optInt("color", 0).coerceIn(colors.indices)
                    break
                }
            }
            val views = RemoteViews(context.packageName, R.layout.widget_note)
            views.setTextViewText(R.id.note_text, text)
            views.setInt(R.id.note_text, "setBackgroundResource", intArrayOf(R.drawable.note_color_0, R.drawable.note_color_1, R.drawable.note_color_2, R.drawable.note_color_3, R.drawable.note_color_4)[color])
            val intent = Intent(context, MainActivity::class.java)
                .putExtra("widget_id", id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            views.setOnClickPendingIntent(R.id.note_text,
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            manager.updateAppWidget(id, views)
        }
    }
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val edit = context.getSharedPreferences("notes", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { edit.remove("linked_$it") }
        edit.apply()
    }
}
