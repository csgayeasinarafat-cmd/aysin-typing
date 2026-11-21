"""Run against a clean Android emulator. The input method is selected only in this emulator."""
import re
import subprocess
import time
from xml.etree import ElementTree

PACKAGE = "org.aysin.typing"
IME = PACKAGE + "/.Keyboard"

def adb(*args):
    return subprocess.check_output(["adb", *args], text=True, timeout=25)

def nodes():
    xml = adb("exec-out", "uiautomator", "dump", "/dev/tty")
    start = xml.index("<?xml")
    end = xml.index("</hierarchy>") + len("</hierarchy>")
    return ElementTree.fromstring(xml[start:end]).findall(".//node")

def wait_for(predicate, description):
    last = []
    for _ in range(8):
        try:
            last = nodes()
        except (ValueError, ElementTree.ParseError):
            continue
        for node in last:
            if predicate(node):
                return node
        time.sleep(0.4)
    raise AssertionError(description + " missing. Visible: " + repr([n.get("text") for n in last]))

def tap(node):
    left, top, right, bottom = map(int, re.findall(r"\d+", node.get("bounds")))
    adb("shell", "input", "tap", str((left + right)//2), str((top + bottom)//2))

def key(label):
    tap(wait_for(lambda n: n.get("text") == label and n.get("clickable") == "true", label))

def editor(expected):
    wait_for(lambda n: n.get("class") == "android.widget.EditText" and n.get("text") == expected, expected)
    print("PASS editor =", expected, flush=True)

adb("shell", "ime", "enable", IME)
adb("shell", "ime", "set", IME)
adb("shell", "am", "start", "-W", "-n", PACKAGE + "/.Setup")
tap(wait_for(lambda n: n.get("class") == "android.widget.EditText", "practice field"))
key("h")
key("i")
editor("hi")
key("Space")
key("বাংলা")
key("ক")
key("কার")
key("া")
editor("hi কা")
key("Space")
key("Phonetic · ami → আমি")
for letter in "ami":
    key(letter)
editor("hi কা আমি")
key("Space")
key("⌫")
editor("hi কা আমি")
adb("shell", "input", "keyevent", "4")
key("Writing Studio · বাংলা ভয়েস")
wait_for(lambda n: n.get("text") == "Preview online rewrite", "Writing Studio")
print("PASS Writing Studio", flush=True)
