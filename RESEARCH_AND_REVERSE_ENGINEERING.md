# Reference research and design decisions
These comparisons describe public documentation. They do not assert that a binary has been decompiled or that a provider works on a particular handset.

| Reference | Evidence | AySin decision |
|---|---|---|
| [Gboard](https://support.google.com/gboard/answer/7421372) | Translation from the typing workflow | Keep the source draft editable and show output before inserting. |
| [SwiftKey](https://support.microsoft.com/en-us/swiftkey-keyboard/how-to-use-microsoft-translator-with-your-microsoft-swiftkey-keyboard) | Translator integrated with the keyboard | Permit style and target language choice without uploading ordinary keystrokes. |
| [Grammarly](https://support.grammarly.com/hc/en-us/articles/24086943816845) | Selected text, prompt and explicit acceptance | Use Android PROCESS_TEXT to return only an accepted replacement to a participating editor. |
| [Ridmik](https://ridmik.com/keyboard) | Bengali-first typing experience | Direct access to vowel signs and hasanta. Phonetic composition needs separate native-speaker QA. |
| [Fleksy](https://www.fleksy.com/) | Keyboard and SDK product | Keep editing gestures optional and retain visible equivalents. |
| [Typewise](https://www.typewise.app/blog/qwerty-killer-typewise-hexagon-keyboard-app) | Alternative key geometry and error correction | Measure adjacent-key errors before changing the familiar Bengali layout. |
| [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard) | Multilingual offline IME | The typing service never submits text. Network operations belong to the explicitly invoked writing activity. |
| [FlorisBoard](https://github.com/florisboard/florisboard) | Privacy-oriented open keyboard | Split editor-session handling from rendering as layouts grow. |
| [HeliBoard](https://github.com/HeliBorg/HeliBoard) | Customizable local keyboard | Keep language resources and future dictionaries independently replaceable. |
| [FUTO](https://keyboard.futo.tech/) | Offline voice input | Detect a speech provider at runtime. Local recognition requires an installed compatible model. |

## Static inspection
| Project | Inspection task | Environment and limits |
|---|---|---|
| [JADX](https://github.com/skylot/jadx) | Inspect DEX classes in a developer-owned build | Java CLI or desktop. Decompiled output loses Kotlin syntax and can be inaccurate. |
| [Apktool](https://github.com/iBotPeaches/Apktool) | Compare packaged resource tables and decoded manifests | Java tooling. Rebuilt packages require new signatures. |
| [Androguard](https://github.com/androguard/androguard) | Inventory components and permissions in CI | Isolated Python environment. Parse untrusted inputs without running them. |
| [GhidraMCP](https://github.com/LaurieWired/GhidraMCP) | Optional AI assistance when reviewing native functions in an authorized binary | Requires Ghidra, a bridge and a client. The current Kotlin keyboard has no custom native library, so no bridge or remote model is necessary. |
| Android SDK tools | Inspect final APK manifest, component declaration and signature | Use the cloud runner's SDK and retain the output alongside the APK. |

No upstream keyboard code, proprietary dictionary, gesture library or speech model is included. The public implementation is original Kotlin. Review each individual artifact's current license and notices before importing it in a future version.
