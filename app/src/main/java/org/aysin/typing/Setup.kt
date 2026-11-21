package org.aysin.typing

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*

class Setup : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,48,24,24) }
        panel.addView(TextView(this).apply { text = "AySin Typing"; textSize = 30f })
        panel.addView(Button(this).apply {
            text = "১ · কিবোর্ড চালু করুন / Enable"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        panel.addView(Button(this).apply {
            text = "২ · কিবোর্ড বাছুন / Select"
            setOnClickListener { getSystemService(InputMethodManager::class.java)?.showInputMethodPicker() }
        })
        panel.addView(Button(this).apply { text = "Writing Studio · বাংলা ভয়েস"; setOnClickListener { startActivity(Intent(this@Setup, WritingStudio::class.java)) } })
        panel.addView(Button(this).apply {
            text = "Keyboard settings · সেটিংস"
            setOnClickListener { preferences() }
        })
        panel.addView(EditText(this).apply { hint = "এখানে লিখুন · Try typing here" })
        setContentView(ScrollView(this).apply { addView(panel) })
    }
    private fun preferences() {
        val prefs = getSharedPreferences("keyboard",MODE_PRIVATE)
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,16,24,16) }
        fun caption(value: String) { panel.addView(TextView(this).apply { text = value }) }
        caption("Keyboard theme")
        val themes = listOf("Blue","AMOLED","Light")
        val theme = Spinner(this).apply {
            adapter = ArrayAdapter(this@Setup,android.R.layout.simple_spinner_dropdown_item,themes)
            setSelection(themes.indexOf(prefs.getString("theme","Blue")).coerceAtLeast(0))
        }
        panel.addView(theme)
        caption("Key height")
        val height = SeekBar(this).apply { max = 24; progress = prefs.getInt("height",46)-36 }
        panel.addView(height)
        val haptic = CheckBox(this).apply { text = "Key vibration"; isChecked = prefs.getBoolean("haptic",false) }
        panel.addView(haptic)
        val privacy = CheckBox(this).apply { text = "Incognito · hide personal snippets"; isChecked = prefs.getBoolean("incognito",false) }
        panel.addView(privacy)
        caption("Quick paste · up to three snippets, one per line")
        val snippets = EditText(this).apply { setText(prefs.getString("snippets","")); minLines = 2 }
        panel.addView(snippets)
        AlertDialog.Builder(this).setTitle("Keyboard settings").setView(ScrollView(this).apply { addView(panel) })
            .setNegativeButton("Cancel",null).setPositiveButton("Save") { _,_ ->
                prefs.edit().putString("theme",theme.selectedItem.toString()).putInt("height",36+height.progress)
                    .putBoolean("haptic",haptic.isChecked).putBoolean("incognito",privacy.isChecked)
                    .putString("snippets",snippets.text.toString().lines().filter { it.isNotBlank() }.take(3).joinToString("\n")).apply()
            }.show()
    }
}
