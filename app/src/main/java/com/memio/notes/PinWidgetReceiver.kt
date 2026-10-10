package com.memio.notes

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PinWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.memio.notes.PIN_WIDGET") return
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val noteId = intent.getStringExtra("note_id") ?: return
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
        context.getSharedPreferences("notes", Context.MODE_PRIVATE)
            .edit().putString("linked_$id", noteId).apply()
        MemoWidget.update(context, AppWidgetManager.getInstance(context), id)
    }
}
