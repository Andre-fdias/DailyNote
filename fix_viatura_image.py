import re

def fix_viatura_image():
    filepath = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/viatura/ViaturaScreen.kt'
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the entire items(viaturas) block to replace
    pattern = re.compile(r'(items\(viaturas, key = \{ it\.id \}\) \{ viatura ->)(.*?)(^\s*if \(showDialog\) \{)', re.DOTALL | re.MULTILINE)
    
    new_card_content = '''items(viaturas, key = { it.id }) { viatura ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val viaturaColor = when (viatura.tipo) {
                            "AT" -> Color(0xFFE53935)
                            "UR" -> Color(0xFF1E88E5)
                            "ABS" -> Color(0xFFFB8C00)
                            "COM" -> Color(0xFF8E24AA)
                            else -> Color(0xFF757575)
                        }
                        val viaturaEmoji = when (viatura.tipo) {
                            "AT" -> "🔥"
                            "UR" -> "⚕️"
                            "ABS" -> "🚘"
                            "COM" -> "📡"
                            else -> "🚒"
                        }
                        val tipoFull = when (viatura.tipo) {
                            "AT" -> "Auto Tanque"
                            "UR" -> "Unidade de Resgate"
                            "ABS" -> "Auto Bomba Salvamento"
                            "COM" -> "Centro de Comunicação"
                            else -> "Outros"
                        }

                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(viaturaColor)
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left side info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(viaturaColor, RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = viaturaEmoji, fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = viatura.prefixo, 
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    androidx.compose.foundation.layout.Box(
                                                        modifier = Modifier
                                                            .background(viaturaColor, RoundedCornerShape(16.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = viatura.tipo, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tipoFull, 
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Atendimento: ", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Local:  - ", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Right side image & 3-dots
                                    Column(horizontalAlignment = Alignment.End) {
                                        IconButton(onClick = { }, modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val imgRes = when(viatura.tipo) {
                                            "AT" -> com.andrefdias.dailynote.R.drawable.viatura_at
                                            "UR" -> com.andrefdias.dailynote.R.drawable.viatura_ur
                                            "ABS" -> com.andrefdias.dailynote.R.drawable.viatura_abs
                                            "COM" -> com.andrefdias.dailynote.R.drawable.viatura_com
                                            else -> com.andrefdias.dailynote.R.drawable.viatura_at
                                        }
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = imgRes),
                                            contentDescription = "Imagem da Viatura",
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(80.dp)
                                                .androidx.compose.ui.draw.clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val statusColor = when(viatura.status) {
                                        "Operacional" -> Color(0xFF4CAF50)
                                        "Manutenção", "Baixada" -> Color(0xFFEF5350)
                                        "Em ocorrência" -> Color(0xFFFF9800)
                                        "Reserva" -> Color(0xFF9E9E9E)
                                        "Ativo" -> Color(0xFF2196F3)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(statusColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = viatura.status,
                                                color = statusColor,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedIconButton(
                                            onClick = {
                                                viewModel.selectViatura(viatura)
                                                showDialog = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                        OutlinedIconButton(
                                            onClick = { viewModel.deleteViatura(viatura) },
                                            modifier = Modifier.size(36.dp),
                                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add the Legend at the bottom of the list
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "AT" to Color(0xFFE53935),
                            "UR" to Color(0xFF1E88E5),
                            "ABS" to Color(0xFFFB8C00),
                            "COM" to Color(0xFF8E24AA),
                            "Outros" to Color(0xFF757575)
                        ).forEach { (label, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.size(8.dp).background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        '''
    
    new_content = pattern.sub(new_card_content + r'\n        \3', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)

fix_viatura_image()
