package org.aysin.typing

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.util.concurrent.Executors

/** Processes only the editable draft the user explicitly approves. */
class WritingStudio : Activity() {
    private lateinit var draft: EditText
    private lateinit var result: EditText
    private lateinit var endpoint: EditText
    private lateinit var style: Spinner
    private lateinit var status: TextView
    private lateinit var submit: Button
    private val worker = Executors.newSingleThreadExecutor()
    private val styles = arrayOf("Professional English", "Short English", "Audit Report", "Official Email", "Polite Follow-up", "Friendly", "Romantic", "Simple English", "Detailed English", "Fix Grammar", "Smart Reply", "Translate to Bengali")
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        title = "Writing Studio"
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,40,24,24) }
        setContentView(ScrollView(this).apply { addView(panel) })
        fun action(label: String, click: () -> Unit) = Button(this).also {
            it.text = label
            it.setOnClickListener { click() }
            panel.addView(it)
        }
        draft = EditText(this).apply { hint = "বাংলা বা English · Your draft"; minLines = 3 }
        val fromEditor = intent.action == Intent.ACTION_PROCESS_TEXT
        val selected = if (fromEditor) intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.take(8000) else null
        draft.setText(state?.getString("draft") ?: selected)
        panel.addView(draft)
        action("বাংলায় বলুন · Dictate") {
            AlertDialog.Builder(this).setMessage("Your selected speech provider will receive the microphone input and may use its online service.")
                .setNegativeButton("Cancel",null).setPositiveButton("Start") { _,_ ->
                    val voice = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD")
                    try { startActivityForResult(voice,1) }
                    catch (_: ActivityNotFoundException) { status.text = "Speech service unavailable. Type or paste your draft." }
                }.show()
        }
        style = Spinner(this).apply { adapter = ArrayAdapter(this@WritingStudio,android.R.layout.simple_spinner_dropdown_item,styles) }
        panel.addView(style)
        endpoint = EditText(this).apply {
            hint = "Your HTTPS writing proxy URL"
            setSingleLine()
            setText(getPreferences(MODE_PRIVATE).getString("endpoint",""))
        }
        panel.addView(endpoint)
        status = TextView(this)
        submit = action("Preview online rewrite") { confirmRewrite() }
        panel.addView(status)
        result = EditText(this).apply { hint = "Editable result"; minLines = 3; setText(state?.getString("result")) }
        panel.addView(result)
        if (fromEditor && !intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY,true)) {
            action("Replace selected text") {
                if (result.text.isNotBlank()) {
                    setResult(RESULT_OK,Intent().putExtra(Intent.EXTRA_PROCESS_TEXT,result.text.toString()))
                    finish()
                }
            }
        }
        action("Copy result") {
            if (result.text.isNotBlank()) {
                getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("AySin result",result.text.toString()))
                status.text = "Copied"
            }
        }
    }
    override fun onActivityResult(request: Int, code: Int, data: Intent?) {
        super.onActivityResult(request,code,data)
        if (request == 1 && code == RESULT_OK) {
            val words = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!words.isNullOrBlank()) { if (draft.length() > 0) draft.append(" "); draft.append(words) }
        }
    }
    private fun confirmRewrite() {
        val url = runCatching { URL(endpoint.text.toString().trim()) }.getOrNull()
        val input = draft.text.toString()
        if (url == null || url.protocol != "https" || url.host.isBlank() || url.userInfo != null || url.query != null || url.ref != null) {
            status.text = "Enter your HTTPS proxy address without credentials or query parameters."; return
        }
        if (input.isBlank() || input.length > 8000) { status.text = "Enter 1–8000 characters."; return }
        val mode = style.selectedItem.toString()
        AlertDialog.Builder(this).setTitle("Send draft to " + url.host + "?")
            .setMessage("The service receives the draft shown above and your chosen style: " + mode)
            .setNegativeButton("Cancel",null).setPositiveButton("Send") { _,_ ->
                getPreferences(MODE_PRIVATE).edit().putString("endpoint",url.toString()).apply()
                submit.isEnabled = false
                status.text = "Processing…"
                worker.execute {
                    val response = runCatching { rewrite(url,input,mode) }
                    runOnUiThread {
                        if (!isDestroyed && !isFinishing) {
                            submit.isEnabled = true
                            response.onSuccess { result.setText(it); status.text = "Review before copying." }
                                .onFailure { status.text = "Request failed. Check the proxy and connection. Your draft is available." }
                        }
                    }
                }
            }.show()
    }
    private fun rewrite(url: URL, input: String, mode: String): String {
        val connection = url.openConnection() as HttpsURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type","application/json; charset=utf-8")
            val payload = JSONObject().put("text",input).put("style",mode).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            require(connection.responseCode in 200..299)
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val chars = CharArray(4096)
                val body = StringBuilder()
                while (true) {
                    val n = reader.read(chars)
                    if (n < 0) break
                    require(body.length + n <= 65536)
                    body.append(chars,0,n)
                }
                JSONObject(body.toString()).getString("text").also { require(it.isNotBlank()) }
            }
        } finally { connection.disconnect() }
    }
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.putString("draft",draft.text.toString())
        state.putString("result",result.text.toString())
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}
