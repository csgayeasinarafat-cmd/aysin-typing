# AySin Typing development build
Enable AySin Typing under Android keyboard settings, select it, then use the practice field. Switch between EN and বাংলা; কার reveals vowel signs. Normal typing works offline. No dictionary or message history is collected.

Open Writing Studio from the launcher or select text in an editor that offers Android’s text-processing menu. Dictate in Bangla using an installed recognition activity, or type directly. Choose a style and review the editable draft. Online processing requires your own HTTPS proxy. The app sends a POST containing the JSON string fields text and style after confirmation. The proxy must return a JSON object with a text string. Provider credentials belong on that server. No hosted proxy or subscription is included. Review the result before copying or replacing the selection.

A compatible recognition service and Bengali model are device-dependent. On phones without recognition, manual entry remains available. The keyboard requires API 24+. Basic phonetic input supports ami → আমি and bangla → বাংলা. Physical Huawei Y8p validation, full Avro compatibility, offline model downloads, gesture input, rich clipboard, release signing and the remaining advanced features are pending.

## Private model adapter
The optional provider/ollama_proxy.py adapter runs beside Ollama on a machine you control. Set AYSIN_MODEL to an installed bilingual model and run python3 provider/ollama_proxy.py. It listens on loopback port 8765. Use a private HTTPS reverse proxy and set the Android endpoint to its /rewrite route. Keep access restricted to your private network. Ordinary typing and phonetics work without this adapter.

## Build evidence
The Actions job runs Kotlin unit tests, Android lint, APK compilation, package inspection and signature verification. Development previews use a disposable debug signer. Use your own stable signing key for continuing installations.
