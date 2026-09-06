import re

def fix_resumo():
    filepath = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/resumo/ResumoOperacionalScreen.kt'
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Fix the Brasão icon color
    content = content.replace(
        'Icon(imageVector = androidx.compose.material.icons.Icons.Default.Shield, contentDescription = "Brasão", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))',
        'Icon(imageVector = androidx.compose.material.icons.Icons.Default.Shield, contentDescription = "Brasão", tint = Color.White, modifier = Modifier.size(24.dp))'
    )
    
    # 2. Fix the Equipe Badge text color
    content = content.replace(
        'Text(text = equipeText, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)',
        'Text(text = equipeText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)'
    )

    # 3. Add IntrinsicSize.Min to DashboardViaturaCard
    # First, locate the start of DashboardViaturaCard's Card
    # We will replace the Card's content wrapper
    old_card_start = '''    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {'''
    new_card_start = '''    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            val corFundoStr = equipe.equipeConfig?.corFundo
            val badgeColor = if (corFundoStr != null) {
                try { Color(android.graphics.Color.parseColor(corFundoStr)) } catch (e: Exception) { Color(0xFF1976D2) }
            } else {
                Color(0xFF1976D2)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(badgeColor)
            )
            Column(modifier = Modifier.weight(1f)) {'''
    
    content = content.replace(old_card_start, new_card_start)
    
    # Also we need to close the Row at the end of the Card. The end of the card is at line 422
    # But it's easier to find the end of Column
    old_card_end = '''                }
            }
        }
    }
    
    if (showBottomSheet) {'''
    new_card_end = '''                }
            }
        }
        }
    }
    
    if (showBottomSheet) {'''
    content = content.replace(old_card_end, new_card_end)
    
    # 4. Fix bottom sheet PM card to also look like TarefaCard
    pm_card_start = '''                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).let { if(!isAtivo) it.background(Color.Transparent) else it }.graphicsLayer { alpha = alphaValue },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {'''
    new_pm_card_start = '''                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).let { if(!isAtivo) it.background(Color.Transparent) else it }.graphicsLayer { alpha = alphaValue },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    val isDejemBar = me.tipoEscala == "DEJEM"
                                    val barColor = if (isDejemBar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(8.dp)
                                            .background(barColor)
                                    )
                                Row(
                                    modifier = Modifier.padding(16.dp).weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {'''
    content = content.replace(pm_card_start, new_pm_card_start)
    
    # close the extra row
    pm_card_end = '''                                        }
                                    }
                                }
                            }
                        }
                    }'''
    new_pm_card_end = '''                                        }
                                    }
                                }
                                }
                            }
                        }
                    }'''
    content = content.replace(pm_card_end, new_pm_card_end)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_resumo()
