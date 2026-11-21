package org.aysin.typing

import android.graphics.Color
import android.content.ClipboardManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.*

class Keyboard : InputMethodService() {
    private val prefs by lazy { getSharedPreferences("keyboard",MODE_PRIVATE) }
    private var toolsOpen = false
    private var emoji = false
    private var bangla = false
    private var shifted = false
    private var symbols = false
    private var page = 0
    private var phonetic = false
    private var roman = ""
    private var privateField = false
    private fun finishWord() {
        currentInputConnection?.finishComposingText()
        roman = ""
    }
    private fun typeKey(value: String) {
        val ic = currentInputConnection ?: return
        if (phonetic && !privateField && !symbols && value.length == 1 && value[0].isLetter()) {
            roman += value
            ic.setComposingText(Phonetic.convert(roman),1)
        } else { finishWord(); ic.commitText(value,1) }
    }
    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart,oldSelEnd,newSelStart,newSelEnd,candidatesStart,candidatesEnd)
        if (roman.isNotEmpty() && (candidatesEnd < 0 || newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) finishWord()
    }
    private val bengali = listOf(
        listOf("অআইঈউঊঋএঐওঔ", "কখগঘঙচছজঝঞ", "টঠডঢণতথদধন", "পফবভমযরলশষস", "হড়ঢ়য়ৎংঃঁ্"),
        listOf("ািীুূৃেৈোৌ", "০১২৩৪৫৬৭৮৯", "।,!?-ঃঁ্")
    )
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        shifted = false
        roman = ""
        toolsOpen = false
        emoji = false
        val type = attribute?.inputType ?: 0
        val variation = type and InputType.TYPE_MASK_VARIATION
        privateField = when (type and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            InputType.TYPE_CLASS_TEXT -> variation in setOf(InputType.TYPE_TEXT_VARIATION_PASSWORD,InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
            else -> false
        }
        if (privateField) { phonetic = false; bangla = false }
    }
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info,restarting)
        setInputView(layout())
    }
    override fun onCreateInputView() = layout()
    private fun layout(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(when (prefs.getString("theme","Blue")) {
                "AMOLED" -> Color.BLACK
                "Light" -> Color.rgb(232,237,244)
                else -> Color.rgb(18,27,44)
            })
        }
        fun row() = LinearLayout(this).also { panel.addView(it) }
        fun key(line: LinearLayout, label: String, weight: Float = 1f, press: () -> Unit) {
            val button = Button(this).apply {
                text = label
                isAllCaps = false
                val light = prefs.getString("theme","Blue") == "Light"
                setTextColor(if (light) Color.rgb(23,39,64) else Color.WHITE)
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 6f * resources.displayMetrics.density
                    setColor(when {
                        light -> Color.WHITE
                        prefs.getString("theme","Blue") == "AMOLED" -> Color.rgb(31,31,31)
                        else -> Color.rgb(43,62,87)
                    })
                }
                textSize = 17f
                minWidth = 0
                minimumWidth = 0
                setPadding(0,0,0,0)
                setOnClickListener {
                    if (prefs.getBoolean("haptic",false)) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    press()
                }
            }
            line.addView(button, LinearLayout.LayoutParams(0,(prefs.getInt("height",46) * resources.displayMetrics.density).toInt(),weight).apply { setMargins(1,2,1,2) })
        }
        if (toolsOpen && !privateField) {
            val editing = row()
            for ((label,id) in listOf("Cut" to android.R.id.cut,"Copy" to android.R.id.copy,"All" to android.R.id.selectAll,
                    "Undo" to android.R.id.undo,"Redo" to android.R.id.redo)) {
                key(editing,label) { finishWord(); currentInputConnection?.performContextMenuAction(id) }
            }
            val more = row()
            key(more,"Paste") {
                val clip = getSystemService(ClipboardManager::class.java)?.primaryClip
                val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).text else null
                if (text != null) { finishWord(); currentInputConnection?.commitText(text,1) }
            }
            key(more,"←") { finishWord(); sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT) }
            key(more,"→") { finishWord(); sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT) }
            key(more,"Emoji") { finishWord(); emoji = !emoji; setInputView(layout()) }
            if (!prefs.getBoolean("incognito",false)) {
                val saved = prefs.getString("snippets","").orEmpty().lines().filter { it.isNotBlank() }.take(3)
                if (saved.isNotEmpty()) {
                    val snippets = row()
                    saved.forEach { value -> key(snippets,value.take(12)) { finishWord(); currentInputConnection?.commitText(value,1) } }
                }
            }
        }
        if (emoji && !privateField) {
            val emojis = row()
            for (value in listOf("🙂","👍","❤️","🙏","✅","🎉")) key(emojis,value) { finishWord(); currentInputConnection?.commitText(value,1) }
        }
        val letters = when {
            symbols -> listOf("1234567890","@#$%&*()","!?/:;.,+-")
            bangla -> bengali[page]
            else -> listOf("1234567890","qwertyuiop","asdfghjkl","zxcvbnm",",.!?")
        }
        for (lettersInRow in letters) {
            val line = row()
            for (letter in lettersInRow) {
                val value = if (shifted && !bangla) letter.uppercase() else letter.toString()
                key(line,value) { typeKey(value) }
            }
        }
        if (!privateField) {
            val modes = row()
            key(modes,"Edit") { finishWord(); toolsOpen = !toolsOpen; if (!toolsOpen) emoji = false; setInputView(layout()) }
            key(modes,if (phonetic) "✓ Phonetic · ami → আমি" else "Phonetic · ami → আমি") {
                finishWord(); phonetic = !phonetic; bangla = false; symbols = false; setInputView(layout())
            }
        }
        val controls = row()
        fun redraw() = setInputView(layout())
        key(controls,if (bangla) "EN" else "বাংলা") { finishWord(); phonetic = false; bangla = !bangla; symbols = false; redraw() }
        key(controls,if (bangla) "কার" else "⇧") { if (bangla) page = 1-page else shifted = !shifted; redraw() }
        key(controls,"123") { finishWord(); symbols = !symbols; redraw() }
        key(controls,"Space",2f) { typeKey(" ") }
        key(controls,"⌫") {
            currentInputConnection?.let { ic ->
                if (roman.isNotEmpty()) {
                    roman = roman.dropLast(1)
                    ic.setComposingText(Phonetic.convert(roman),1)
                    if (roman.isEmpty()) ic.finishComposingText()
                } else if (!ic.getSelectedText(0).isNullOrEmpty()) ic.commitText("",1)
                else ic.deleteSurroundingTextInCodePoints(1,0)
            }
        }
        key(controls,"↵") { finishWord(); if (!sendDefaultEditorAction(true)) currentInputConnection?.commitText("\n",1) }
        return panel
    }
}
