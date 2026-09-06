import re

def replace_colors(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    replacements = [
        (r'Color\(0xFF1B2333\)', 'MaterialTheme.colorScheme.surfaceVariant'),
        (r'Color\(0xFF37474F\)', 'MaterialTheme.colorScheme.outlineVariant'),
        (r'Color\(0xFFB0BEC5\)', 'MaterialTheme.colorScheme.onSurfaceVariant'),
        (r'Color\(0xFF283550\)', 'MaterialTheme.colorScheme.primaryContainer'),
        (r'Color\(0xFF1E2738\)', 'MaterialTheme.colorScheme.surface'),
        (r'Color\(0xFF232D42\)', 'MaterialTheme.colorScheme.secondaryContainer'),
        (r'Color\(0xFF90A4AE\)', 'MaterialTheme.colorScheme.onSecondaryContainer'),
        (r'Color\(0xFF1B5E20\)', 'MaterialTheme.colorScheme.tertiaryContainer'),
        (r'Color\(0xFFA5D6A7\)', 'MaterialTheme.colorScheme.onTertiaryContainer'),
        (r'Color\(0xFFE65100\)', 'MaterialTheme.colorScheme.errorContainer'),
        (r'Color\(0xFFFFCC80\)', 'MaterialTheme.colorScheme.onErrorContainer'),
        (r'Color\(0xFF64B5F6\)', 'MaterialTheme.colorScheme.onPrimaryContainer'),
        (r'Color\(0xFFD32F2F\)', 'MaterialTheme.colorScheme.error'),
        (r'Color\(0xFFE53935\)', 'MaterialTheme.colorScheme.error'),
        (r'Color\(0xFFFFB300\)', 'MaterialTheme.colorScheme.secondary'),
        (r'Color\(0xFF42A5F5\)', 'MaterialTheme.colorScheme.primary'),
        (r'Color\(0xFF9575CD\)', 'MaterialTheme.colorScheme.tertiary'),
        (r'Color\.White', 'MaterialTheme.colorScheme.onSurface'),
        (r'Color\.Gray', 'MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)')
    ]

    for old, new in replacements:
        content = re.sub(old, new, content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

replace_colors('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/resumo/ResumoOperacionalScreen.kt')
replace_colors('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/equipe/EquipeServicoScreen.kt')
