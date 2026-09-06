import re

def fix_equipe():
    filepath = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/equipe/EquipeServicoScreen.kt'
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Update ViaturaCard to match TarefaCard pattern
    old_viatura = '''    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {'''
        
    new_viatura = '''    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {'''
            
    content = content.replace(old_viatura, new_viatura)
    
    # Need to close the Row inside ViaturaCard
    old_end = '''            Button(
                onClick = { 
                    onSaveEquipe() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Viatura")
            }
        }
    }'''
    new_end = '''            Button(
                onClick = { 
                    onSaveEquipe() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Viatura")
            }
        }
        }
    }'''
    content = content.replace(old_end, new_end)

    # Make the green dot for militares into MaterialTheme.colorScheme.secondary or primary
    content = content.replace('Color(0xFF4CAF50)', 'MaterialTheme.colorScheme.secondary')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_equipe()
