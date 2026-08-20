import sys

path = r'c:\Users\andre_we17otv\AndroidStudioProjects\DailyNotes\app\src\main\java\com\andrefdias\dailynote\ui\screens\resumo\ResumoOperacionalScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

out_lines = []
in_card = False
card_started = False
for i, line in enumerate(lines):
    if 'import androidx.compose.ui.graphics.Color' in line:
        out_lines.append(line)
        if 'import androidx.compose.foundation.background' not in ''.join(lines):
            out_lines.append('import androidx.compose.foundation.background\n')
        continue
        
    if 'Card(' in line and 'ResumoViaturaCard' in ''.join(lines[max(0, i-20):i]):
        if not card_started:
            in_card = True
            card_started = True
            # insert new card header
            out_lines.append('''    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Data: \", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = "\ - \",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
''')
    elif in_card:
        if 'Spacer(modifier = Modifier.height(8.dp))' in line:
            in_card = False # done skipping old header
        continue
    else:
        out_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(out_lines)
print("Done")
