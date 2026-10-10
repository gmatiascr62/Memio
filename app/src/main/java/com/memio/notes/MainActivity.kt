package com.memio.notes

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ComponentName
import android.os.Build
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private val cream = Color.rgb(255, 250, 235)
    private val ink = Color.rgb(66, 48, 32)
    private val prefs by lazy { getSharedPreferences("notes", Context.MODE_PRIVATE) }
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = cream
        window.navigationBarColor = cream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            // window.insetsController falla con NullPointerException si se llama antes de setContentView;
            // decorView crea la vista de la ventana primero.
            window.decorView.windowInsetsController?.setSystemBarsAppearance(light, light)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        widgetId = intent.getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID)
        showHome()
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val linked = prefs.getString("linked_$widgetId", null)
            window.decorView.post {
                if (linked != null && indexOf(notes(), linked) >= 0) editNote(linked, widgetId) else chooseNote(widgetId)
            }
        }
    }

    private fun indexOf(notes: JSONArray, id: String): Int {
        for (i in 0 until notes.length()) if (notes.optJSONObject(i)?.optString("id") == id) return i
        return -1
    }

    /** Desde Android 15 la app se dibuja de borde a borde: dejamos espacio para las barras del sistema. */
    private fun applySystemBarPadding(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(left + insets.systemWindowInsetLeft, top + insets.systemWindowInsetTop,
                    right + insets.systemWindowInsetRight, bottom + insets.systemWindowInsetBottom)
            }
            insets
        }
    }

    private fun notes(): JSONArray = try {
        JSONArray(prefs.getString("all_notes", "[]"))
    } catch (_: Exception) { JSONArray() }

    private fun save(notes: JSONArray) {
        prefs.edit().putString("all_notes", notes.toString()).apply()
        val manager = AppWidgetManager.getInstance(this)
        val component = ComponentName(this, MemoWidget::class.java)
        manager.getAppWidgetIds(component).forEach { MemoWidget.update(this, manager, it) }
    }

    private fun label(text: String, size: Float, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(ink)
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
        }
        applySystemBarPadding(root, dp(20), dp(24), dp(20), dp(16))
        root.addView(label("Memio ✨", 30f, true))
        root.addView(label("Tus ideas, siempre a la vista", 15f).apply {
            setPadding(0, dp(6), 0, dp(18))
        })
        val create = Button(this).apply {
            text = "＋  Nueva nota"
            setOnClickListener { editNote(null) }
        }
        create.isAllCaps = false
        create.setTextColor(Color.WHITE)
        create.background = getDrawable(R.drawable.rounded_button)
        root.addView(create)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val all = notes()
        if (all.length() == 0) {
            list.addView(label("Todavía no tenés notas. Tocá «Nueva nota» para empezar.", 17f).apply {
                setPadding(0, dp(36), 0, 0)
            })
        }
        for (i in 0 until all.length()) {
            val note = all.optJSONObject(i) ?: continue
            val noteId = note.optString("id")
            val color = note.optInt("color", 0).coerceIn(MemoWidget.colors.indices)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(16), dp(18), dp(12))
                elevation = dp(3).toFloat()
                background = android.graphics.drawable.GradientDrawable().apply { setColor(MemoWidget.colors[color]); cornerRadius = dp(18).toFloat() }
            }
            card.addView(label(note.optString("text").ifBlank { "Nota vacía" }, 19f).apply {
                minHeight = dp(65)
                gravity = Gravity.CENTER
                setOnClickListener { editNote(noteId) }
            })
            val actions = LinearLayout(this)
            actions.addView(Button(this).apply {
                text = "Editar"
                setOnClickListener { editNote(noteId) }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
            actions.addView(Button(this).apply {
                text = "Eliminar"
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).setMessage("¿Eliminar esta nota?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar") { _, _ ->
                            deleteNote(noteId)
                            showHome()
                        }.show()
                }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
            card.addView(actions)
            card.addView(Button(this).apply {
                isAllCaps = false
                text = "📌 Agregar a inicio"
                setOnClickListener { pinNote(noteId) }
            })
            list.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(10), 0, dp(10))
            })
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(label("Tocá «Agregar a inicio» en cualquier nota para colocarla como widget.", 13f))
        setContentView(root)
    }

    private fun deleteNote(noteId: String) {
        val updated = notes()
        val index = indexOf(updated, noteId)
        if (index < 0) return
        updated.remove(index)
        val manager = AppWidgetManager.getInstance(this)
        manager.getAppWidgetIds(ComponentName(this, MemoWidget::class.java)).forEach { id ->
            if (prefs.getString("linked_$id", null) == noteId) prefs.edit().remove("linked_$id").apply()
        }
        save(updated)
    }

    /**
     * Abre el editor de la nota con ese [noteId], o de una nota nueva si es null.
     * Si se abre desde un widget ([fromWidget]), ofrece cambiar la nota que muestra.
     */
    private fun editNote(noteId: String?, fromWidget: Int = AppWidgetManager.INVALID_APPWIDGET_ID) {
        val existing = noteId?.let { id -> notes().let { it.optJSONObject(indexOf(it, id)) } }
        var chosen = (existing?.optInt("color", 0) ?: 0).coerceIn(MemoWidget.colors.indices)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), 0)
        }
        val input = EditText(this).apply {
            hint = "Escribí tu recordatorio..."
            setText(existing?.optString("text") ?: "")
            minLines = 4
            maxLines = 9
            gravity = Gravity.TOP
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(MemoWidget.colors[chosen])
        }
        layout.addView(input)
        val palette = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        MemoWidget.colors.forEachIndexed { i, color ->
            palette.addView(TextView(this).apply {
                text = "●"
                textSize = 32f
                gravity = Gravity.CENTER
                setTextColor(color)
                setOnClickListener { chosen = i; input.setBackgroundColor(color) }
            }, LinearLayout.LayoutParams(0, dp(64), 1f))
        }
        layout.addView(palette)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Nueva nota" else "Editar nota")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val updated = notes()
                val note = JSONObject()
                    .put("id", existing?.optString("id") ?: java.util.UUID.randomUUID().toString())
                    .put("text", input.text.toString())
                    .put("color", chosen)
                // Se busca por id al guardar: si la nota se borró mientras tanto, se guarda como nueva.
                val index = existing?.let { indexOf(updated, it.optString("id")) } ?: -1
                if (index < 0) updated.put(note) else updated.put(index, note)
                save(updated)
                showHome()
            }
        if (existing != null && fromWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
            dialog.setNeutralButton("Cambiar nota") { _, _ -> chooseNote(fromWidget) }
        }
        dialog.show()
    }

    private fun pinNote(noteId: String) {
        val manager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !manager.isRequestPinAppWidgetSupported) {
            AlertDialog.Builder(this).setMessage("Tu pantalla de inicio no permite agregar widgets desde la app. Mantené presionado un espacio vacío → Widgets → Memio.")
                .setPositiveButton("Entendido", null).show()
            return
        }
        val callbackIntent = Intent(this, PinWidgetReceiver::class.java)
            .setAction("com.memio.notes.PIN_WIDGET")
            .putExtra("note_id", noteId)
        val callback = PendingIntent.getBroadcast(this, noteId.hashCode(), callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        val provider = ComponentName(this, MemoWidget::class.java)
        val accepted = manager.requestPinAppWidget(provider, null, callback)
        if (!accepted) Toast.makeText(this, "No se pudo solicitar el widget", Toast.LENGTH_LONG).show()
    }

    private fun chooseNote(id: Int) {
        val all = notes()
        if (all.length() == 0) {
            AlertDialog.Builder(this).setTitle("Primero creá una nota")
                .setMessage("Creá una nota desde Memio y después tocá el widget para elegirla.")
                .setPositiveButton("Crear nota") { _, _ -> editNote(null) }
                .setNegativeButton("Después", null).show()
            return
        }
        val names = Array(all.length()) { i ->
            all.optJSONObject(i)?.optString("text")?.take(55)?.ifBlank { "Nota vacía" } ?: "Nota"
        }
        AlertDialog.Builder(this).setTitle("Elegí la nota del widget")
            .setItems(names) { _, index ->
                val noteId = all.optJSONObject(index)?.optString("id") ?: return@setItems
                prefs.edit().putString("linked_$id", noteId).apply()
                MemoWidget.update(this, AppWidgetManager.getInstance(this), id)
                Toast.makeText(this, "Nota fijada en el inicio", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancelar", null).show()
    }
}
