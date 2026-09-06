filepath = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/viatura/ViaturaScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add missing imports
imports_to_add = [
    'import androidx.compose.foundation.Image',
    'import androidx.compose.ui.draw.clip',
    'import androidx.compose.ui.res.painterResource',
    'import androidx.compose.ui.layout.ContentScale'
]

for imp in imports_to_add:
    if imp not in content:
        content = content.replace('import androidx.compose.ui.text.font.FontWeight', f'import androidx.compose.ui.text.font.FontWeight\n{imp}')

# Fix fully qualified names
content = content.replace('androidx.compose.foundation.layout.Box', 'Box')
content = content.replace('androidx.compose.foundation.Image', 'Image')
content = content.replace('androidx.compose.ui.res.painterResource', 'painterResource')
content = content.replace('androidx.compose.ui.draw.clip', 'clip')
content = content.replace('androidx.compose.ui.layout.ContentScale', 'ContentScale')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
