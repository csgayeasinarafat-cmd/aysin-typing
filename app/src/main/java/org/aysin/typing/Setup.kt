package org.aysin.typing

import android.app.Activity
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
            setOnClickListener { getSystemService(InputMethodManager::class.java).showInputMethodPicker() }
        })
        panel.addView(EditText(this).apply { hint = "এখানে লিখুন · Try typing here" })
        setContentView(panel)
    }
}
