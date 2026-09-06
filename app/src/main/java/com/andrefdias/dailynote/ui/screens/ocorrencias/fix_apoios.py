import os
import re

file_path = r'C:\Users\andre_we17otv\AndroidStudioProjects\DailyNotes\app\src\main\java\com\andrefdias\dailynote\ui\screens\ocorrencias\OcorrenciaOpcoesScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix common corrupted characters in ApoiosModuleView
content = content.replace('Polcia', 'Polícia')
content = content.replace('Instituiǜo', 'Instituição')
content = content.replace('Concessionǭria', 'Concessionária')
content = content.replace('ElǸtrica', 'Elétrica')
content = content.replace('?gua', 'Água')
content = content.replace('Identificaǜo', 'Identificação')
content = content.replace('Responsǭvel', 'Responsável')

# Also fix the buttons at the bottom.
# Replace the old buttons with correctly padded ones.
old_buttons = '''
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(com.google.gson.Gson().toJson(apoiosList.toList())) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Concluir")
            }
        }
'''

new_buttons = '''
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(com.google.gson.Gson().toJson(apoiosList.toList())) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
'''

# Wait! Did I already replace the buttons with 56.dp and 'Salvar' earlier?
# Let's just do a regex replace to catch any button pattern at the end of ApoiosModuleView
pattern = r'Spacer\(Modifier\.height\(16\.dp\)\)[\s\S]*?Row\(modifier = Modifier\.fillMaxWidth\(\)[\s\S]*?Text\("(Cancelar|Concluir|Salvar)"\)[\s\S]*?\}'
content = re.sub(pattern, new_buttons.strip(), content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
