package com.memio.notes

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private var selectedColor = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID)
        val prefs = getSharedPreferences("notes", MODE_PRIVATE)
        selectedColor = prefs.getInt("color_$id", 0).coerceIn(MemoWidget.colors.indices)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 40, 28, 24); setBackgroundColor(Color.rgb(255,250,235)) }
        val title = TextView(this).apply { text = "Memio ✨"; textSize = 32f; setTextColor(Color.rgb(67,47,22)) }
        root.addView(title)
        val subtitle = TextView(this).apply { text = if(id == AppWidgetManager.INVALID_APPWIDGET_ID) "Agregá un widget Memio desde la pantalla de inicio para crear una nota." else "Editá tu nota adhesiva"; textSize = 16f; setPadding(0,16,0,24) }
        root.addView(subtitle)
        if(id != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val edit = EditText(this).apply { setText(prefs.getString("text_$id", "")); hint = "Escribí tu recordatorio..."; gravity = Gravity.TOP; minLines = 6; maxLines = 12; setPadding(20,20,20,20); setBackgroundColor(MemoWidget.colors[selectedColor]) }
            root.addView(edit, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            val palette = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0,22,0,22) }
            MemoWidget.colors.forEachIndexed { index, color ->
                val button = Button(this).apply { text = "●"; textSize = 25f; setTextColor(color); setOnClickListener { selectedColor = index; edit.setBackgroundColor(color) } }
                palette.addView(button, LinearLayout.LayoutParams(0, 64, 1f))
            }
            root.addView(palette)
            val save = Button(this).apply { text = "Guardar nota"; setOnClickListener {
                prefs.edit().putString("text_$id", edit.text.toString()).putInt("color_$id", selectedColor).apply()
                MemoWidget.update(this@MainActivity, AppWidgetManager.getInstance(this@MainActivity), id)
                Toast.makeText(this@MainActivity, "Nota guardada", Toast.LENGTH_SHORT).show()
                finish()
            } }
            root.addView(save)
        }
        setContentView(root)
    }
}