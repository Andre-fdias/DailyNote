import re

def fix_colors(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace secondaryContainer with surfaceVariant for cards
    content = content.replace('containerColor = MaterialTheme.colorScheme.secondaryContainer', 'containerColor = MaterialTheme.colorScheme.surfaceVariant')
    
    # Replace primaryContainer with surfaceVariant for cards
    content = content.replace('containerColor = MaterialTheme.colorScheme.primaryContainer', 'containerColor = MaterialTheme.colorScheme.surfaceVariant')
    
    # Replace tertiaryContainer with surfaceVariant for cards
    content = content.replace('containerColor = MaterialTheme.colorScheme.tertiaryContainer', 'containerColor = MaterialTheme.colorScheme.surfaceVariant')

    # Also, we might have used secondaryContainer for the small tags inside the modal
    # al bgColor = if (isTagDejem) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    # Let's change those to a more vivid color. Maybe just use primary/secondary but with alpha? 
    # The original was copy(alpha=0.15f).
    # Let's just use MaterialTheme.colorScheme.surface for card backgrounds to make them stand out if they have elevation or borders.

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_colors('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/resumo/ResumoOperacionalScreen.kt')
fix_colors('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/equipe/EquipeServicoScreen.kt')
