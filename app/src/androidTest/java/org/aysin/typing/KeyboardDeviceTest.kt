package org.aysin.typing

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

class KeyboardDeviceTest {
    @Test fun enterEnglishBengaliAndPhoneticInPracticeField() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        device.executeShellCommand("settings put secure show_ime_with_hard_keyboard 1")
        device.executeShellCommand("ime enable org.aysin.typing/.Keyboard")
        device.executeShellCommand("ime set org.aysin.typing/.Keyboard")
        instrumentation.targetContext.startActivity(Intent(instrumentation.targetContext,Setup::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        val box = By.clazz("android.widget.EditText").pkg("org.aysin.typing")
        requireNotNull(device.wait(Until.findObject(box),10000)).click()
        fun key(label: String) {
            val selector = By.text(Pattern.compile(Pattern.quote(label),Pattern.CASE_INSENSITIVE))
            requireNotNull(device.wait(Until.findObject(selector),8000)) { "Missing key: " + label }.click()
        }
        fun expect(text: String) {
            device.wait(Until.hasObject(By.text(text).clazz("android.widget.EditText")),5000)
            assertEquals(text,device.findObject(box)?.text)
        }
        key("h"); key("i"); expect("hi")
        key("Space"); key("বাংলা"); key("ক"); key("কার"); key("া"); expect("hi কা")
        key("Space"); key("Phonetic · ami → আমি")
        key("a"); key("m"); key("i"); expect("hi কা আমি")
        key("Space"); key("⌫"); expect("hi কা আমি")
        device.pressBack()
        key("Writing Studio · বাংলা ভয়েস")
        check(device.wait(Until.hasObject(By.text(Pattern.compile("Preview online rewrite",Pattern.CASE_INSENSITIVE))),8000))
    }
}
