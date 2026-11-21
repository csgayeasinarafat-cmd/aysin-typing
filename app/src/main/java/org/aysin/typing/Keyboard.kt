package org.aysin.typing

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.*

class Keyboard : InputMethodService() {
    private var bangla = false
    private var shifted = false
    private var symbols = false
    private var page = 0
    private val bengali = listOf(
        listOf("অআইঈউঊঋএঐওঔ", "কখগঘঙচছজঝঞ", "টঠডঢণতথদধন", "পফবভমযরলশষস", "হড়ঢ়য়ৎংঃঁ্"),
        listOf("ািীুূৃেৈোৌ", "০১২৩৪৫৬৭৮৯", "।,!?-ঃঁ্")
    )
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        shifted = false
    }
    override fun onCreateInputView() = layout()
    private fun layout(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(18,27,44))
        }
        fun row() = LinearLayout(this).also { panel.addView(it) }
        fun key(line: LinearLayout, label: String, weight: Float = 1f, press: () -> Unit) {
            val button = Button(this).apply {
                text = label
                isAllCaps = false
                textSize = 17f
                minWidth = 0
                minimumWidth = 0
                setPadding(0,0,0,0)
                setOnClickListener { press() }
            }
            line.addView(button, LinearLayout.LayoutParams(0,(46 * resources.displayMetrics.density).toInt(),weight))
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
                key(line,value) { currentInputConnection?.commitText(value,1) }
            }
        }
        val controls = row()
        fun redraw() = setInputView(layout())
        key(controls,if (bangla) "EN" else "বাংলা") { bangla = !bangla; symbols = false; redraw() }
        key(controls,if (bangla) "কার" else "⇧") { if (bangla) page = 1-page else shifted = !shifted; redraw() }
        key(controls,"123") { symbols = !symbols; redraw() }
        key(controls,"Space",2f) { currentInputConnection?.commitText(" ",1) }
        key(controls,"⌫") {
            currentInputConnection?.let { ic ->
                if (!ic.getSelectedText(0).isNullOrEmpty()) ic.commitText("",1)
                else ic.deleteSurroundingTextInCodePoints(1,0)
            }
        }
        key(controls,"↵") { if (!sendDefaultEditorAction(true)) currentInputConnection?.commitText("\n",1) }
        return panel
    }
}
