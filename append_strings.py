import os

def append_strings(xml_path, txt_path):
    with open(txt_path, 'r', encoding='utf-8') as f:
        new_strings = f.read()
    
    with open(xml_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "</resources>" in content:
        content = content.replace("</resources>", new_strings + "\n</resources>")
        with open(xml_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {xml_path}")
    else:
        print(f"Could not find </resources> in {xml_path}")

append_strings("app/src/main/res/values/strings.xml", "/home/tuananh/Code/TADaManager/tada-animated-mascot/strings_en_add.txt")
append_strings("app/src/main/res/values-vi-rVN/strings.xml", "/home/tuananh/Code/TADaManager/tada-animated-mascot/strings_vi_add.txt")
