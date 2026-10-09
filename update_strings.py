import re
import os

def insert_strings(filepath, strings):
    if not os.path.exists(filepath):
        print(f"{filepath} not found")
        return
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    # insert before </resources>
    if "</resources>" in content:
        content = content.replace("</resources>", strings + "\n</resources>")
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {filepath}")
    else:
        print(f"Could not find </resources> in {filepath}")

en_strings = """
    <string name="chat_typing">typing…</string>
    <string name="chat_patching_start">Hi there! I am patching %1$s for you 🛠️</string>
    <string name="chat_patching_25">Original file downloaded, now applying patches…</string>
    <string name="chat_patching_55">Finished %1$d/%2$d patches, almost there!</string>
    <string name="chat_patching_85">Almost done, packaging everything up…</string>
"""

vi_strings = """
    <string name="chat_typing">đang nhập…</string>
    <string name="chat_patching_start">Chào ông! Tui đang vá %1$s nè 🛠️</string>
    <string name="chat_patching_25">Tải file gốc xong rồi, giờ đắp patch vô…</string>
    <string name="chat_patching_55">Xong %1$d/%2$d patch rồi đó, ráng xíu nữa!</string>
    <string name="chat_patching_85">Sắp xong rồi, đang đóng gói lại…</string>
"""

insert_strings("app/src/main/res/values/strings.xml", en_strings)
insert_strings("app/src/main/res/values-vi-rVN/strings.xml", vi_strings)
