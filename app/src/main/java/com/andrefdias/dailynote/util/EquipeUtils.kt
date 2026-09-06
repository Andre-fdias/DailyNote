package com.andrefdias.dailynote.util

import androidx.compose.ui.graphics.Color

object EquipeUtils {
    // Resolve equipe string → display label + color
    // Handles legacy hardcoded names and the new "Nome|#Hex" format
    fun parseEquipeInfo(equipe: String): Pair<String, Color> {
        val raw = equipe.trim()
        if (raw.contains("|")) {
            val parts = raw.split("|")
            val label = parts[0].trim()
            val colorHex = parts.getOrNull(1)?.trim()
            if (!colorHex.isNullOrEmpty()) {
                try {
                    return Pair(label, Color(android.graphics.Color.parseColor(colorHex)))
                } catch (e: Exception) {
                    // Fallback if hex is invalid
                }
            }
            return Pair(label, Color(0xFF78909C))
        }
        
        val u = raw.uppercase()
        val color = when {
            u.contains("VERDE")  -> Color(0xFF388E3C)
            u.contains("AZUL")   -> Color(0xFF1565C0)
            u.contains("VERM")   -> Color(0xFFC62828)
            u.contains("AMAR")   -> Color(0xFFF9A825)
            u.contains("DEJEM")  -> Color(0xFF6A1B9A)
            u.contains("BRANCA") -> Color(0xFF546E7A)
            else                  -> Color(0xFF78909C)
        }
        return Pair(raw.ifEmpty { "Sem prontidão" }, color)
    }
}
