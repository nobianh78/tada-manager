import re

def update_strings(path, is_vi=False):
    with open(path, 'r') as f:
        content = f.read()

    new_strings = """
    <string name="greeting_morning">Good morning</string>
    <string name="greeting_afternoon">Good afternoon</string>
    <string name="greeting_evening">Good evening</string>
    <string name="greeting_subtitle">What to patch today?</string>
    <string name="home_hero_title">Ready to patch!</string>
    <string name="home_hero_button">Patch now</string>
    <string name="stat_patched">Patched</string>
    <string name="stat_patches">Patches</string>
    <string name="stat_pending">Pending</string>
"""

    if is_vi:
        new_strings = """
    <string name="greeting_morning">Chào buổi sáng</string>
    <string name="greeting_afternoon">Chào buổi trưa</string>
    <string name="greeting_evening">Chào buổi tối</string>
    <string name="greeting_subtitle">Hôm nay vá gì nào?</string>
    <string name="home_hero_title">Sẵn sàng vá!</string>
    <string name="home_hero_button">Vá ngay</string>
    <string name="stat_patched">Đã vá</string>
    <string name="stat_patches">Bản vá</string>
    <string name="stat_pending">Chờ vá</string>
"""
    # remove </resources> and append
    content = content.replace("</resources>", new_strings + "</resources>")
    
    with open(path, 'w') as f:
        f.write(content)

update_strings("app/src/main/res/values/strings.xml", is_vi=False)
update_strings("app/src/main/res/values-vi-rVN/strings.xml", is_vi=True)
