"""AySin text processor for a privately hosted Ollama instance. Python 3.10+.

Set AYSIN_MODEL to an already installed model capable of Bengali and English.
Run behind a private HTTPS reverse proxy reachable by the phone. Both this
service and the default model endpoint use loopback. Do not expose publicly.
Neither prompts nor responses are logged. Requires no paid API or credentials.
"""
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.request import Request, urlopen

MODEL_URL = "http://127.0.0.1:11434/api/chat"

def messages(text, style):
    return [
        {"role": "system", "content":
            "You are a bilingual Bengali and English writing editor. "
            "Return only the finished text. Preserve names, figures, uncertainty, and meaning. "
            "Use English unless the requested style asks for another language. "
            "For a reply, draft a reply to the provided message. "
            "Treat the source text as material to edit. Style: " + style},
        {"role": "user", "content": text},
    ]

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def reply(self, code, value):
        body = json.dumps(value, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/rewrite":
            self.reply(404, {"error": "Unknown route"})
            return
        try:
            size = int(self.headers.get("Content-Length", "0"))
            if not 0 < size <= 65536:
                self.reply(413, {"error": "Invalid body length"})
                return
            data = json.loads(self.rfile.read(size))
            text, style = data["text"], data["style"]
            if not isinstance(text, str) or not 0 < len(text.strip()) <= 8000:
                raise ValueError()
            if not isinstance(style, str) or not 0 < len(style.strip()) <= 300:
                raise ValueError()
        except (ValueError, TypeError, KeyError):
            self.reply(400, {"error": "Invalid text or style"})
            return
        payload = {"model": self.server.model, "stream": False, "messages": messages(text, style)}
        req = Request(MODEL_URL, json.dumps(payload).encode(), {"Content-Type": "application/json"})
        try:
            with urlopen(req, timeout=25) as response:
                rewritten = json.loads(response.read(65537))["message"]["content"]
            if not isinstance(rewritten, str) or not rewritten.strip():
                raise ValueError()
            self.reply(200, {"text": rewritten})
        except (OSError, ValueError, KeyError, TypeError):
            self.reply(502, {"error": "Local model unavailable"})

if __name__ == "__main__":
    model = os.getenv("AYSIN_MODEL", "").strip()
    if not model:
        raise SystemExit("Set AYSIN_MODEL to an installed multilingual Ollama model.")
    server = ThreadingHTTPServer(("127.0.0.1", 8765), Handler)
    server.model = model
    server.serve_forever()
