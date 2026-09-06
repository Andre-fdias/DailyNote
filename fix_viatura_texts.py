import re

filepath = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/viatura/ViaturaScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add expandedMenu variable
pattern_items = r'(items\(viaturas, key = \{ it\.id \}\) \{ viatura ->)'
replacement_items = r'\1\n                    var expandedMenu by remember { mutableStateOf(false) }'
content = re.sub(pattern_items, replacement_items, content)


# 2. Replace Atendimento / Local section
pattern_texts = r'Text\(\s*text = "Atendimento:(.*?)color = MaterialTheme\.colorScheme\.onSurfaceVariant\s*\)'

replacement_texts = '''Text(
                                            text = f"Unidade: {viatura.unidade}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = f"Posto: {viatura.posto}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = f"Atendimento: {viatura.tipoAtendimento}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )'''
replacement_texts = replacement_texts.replace("f\"", "\"$").replace("{", "{").replace("}", "}")

content = re.sub(pattern_texts, replacement_texts, content, flags=re.DOTALL)


# 3. Replace IconButton with Box + DropdownMenu
pattern_icon = r'(IconButton\(onClick = \{ \}, modifier = Modifier\.size\(24\.dp\)\.offset\(x = 8\.dp, y = \(-8\)\.dp\)\) \{\s*Icon\(Icons\.Default\.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\})'

replacement_icon = '''Box {
                                            IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            DropdownMenu(
                                                expanded = expandedMenu,
                                                onDismissRequest = { expandedMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Editar") },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.selectViatura(viatura)
                                                        showDialog = true 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Excluir", color = Color(0xFFEF5350)) },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.deleteViatura(viatura) 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350)) }
                                                )
                                            }
                                        }'''

content = re.sub(pattern_icon, replacement_icon, content, flags=re.DOTALL)


# 4. Remove the edit/delete buttons row
pattern_buttons = r'(Row\(horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\) \{\s*OutlinedIconButton.*?\Icon\(Icons\.Default\.Delete.*?\}\s*\})'
content = re.sub(pattern_buttons, '', content, flags=re.DOTALL)


with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
