import os

file_path = r'C:\Users\andre_we17otv\AndroidStudioProjects\DailyNotes\app\src\main\java\com\andrefdias\dailynote\ui\screens\ocorrencias\OcorrenciaOpcoesScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    'Ãª': 'ê',
    'Ã£': 'ã',
    'Ã­': 'í',
    'Ã³': 'ó',
    'Ã§': 'ç',
    'Ã¢': 'â',
    'Ã©': 'é',
    'Ã¡': 'á',
    'Ã ': 'à',
    'Ãƒ': 'Ã',
    'Ã‰': 'É',
    'ÃŠ': 'Ê',
    'Ã‡': 'Ç',
    'Ã“': 'Ó',
    'Ãš': 'Ú',
    'Ã\xad': 'í',
    'Ã¢': 'â',
    'â€¢': '•',
    'âœ…': '✅',
    'â¬œ': '⬜',
    'Ã\x80': 'À',
    'Ã\x81': 'Á',
    'Ã\x82': 'Â',
    'Ã\x83': 'Ã',
    'Ã\x87': 'Ç',
    'Ã\x89': 'É',
    'Ã\x8a': 'Ê',
    'Ã\x8d': 'Í',
    'Ã\x93': 'Ó',
    'Ã\x94': 'Ô',
    'Ã\x95': 'Õ',
    'Ã\x9a': 'Ú',
    'Ã\xa0': 'à',
    'Ã\xa1': 'á',
    'Ã\xa2': 'â',
    'Ã\xa3': 'ã',
    'Ã\xa7': 'ç',
    'Ã\xa9': 'é',
    'Ã\xaa': 'ê',
    'Ã\xad': 'í',
    'Ã\xb3': 'ó',
    'Ã\xb4': 'ô',
    'Ã\xb5': 'õ',
    'Ã\xba': 'ú'
}

for bad, good in replacements.items():
    content = content.replace(bad, good)

# Also fix specific known words just in case
content = content.replace('OcorrÃªncia', 'Ocorrência')
content = content.replace('TalÃ£o', 'Talão')
content = content.replace('VeÃ­culos', 'Veículos')
content = content.replace('VÃ­timas', 'Vítimas')
content = content.replace('HistÃ³rico', 'Histórico')
content = content.replace('EvidÃªncias', 'Evidências')
content = content.replace('EndereÃ§o', 'Endereço')
content = content.replace('GuarniÃ§Ã£o', 'Guarnição')
content = content.replace('FunÃ§Ã£o', 'Função')
content = content.replace('InstituiÃ§Ã£o', 'Instituição')
content = content.replace('ConcluÃ­do', 'Concluído')
content = content.replace('NÃ£o', 'Não')
content = content.replace('NÃƒO', 'NÃO')
content = content.replace('INICIÃ DO', 'INICIADO')
content = content.replace('CONCLUÃ DO', 'CONCLUÍDO')
content = content.replace('ClassificaÃ§Ã£o', 'Classificação')
content = content.replace('LocalizaÃ§Ã£o', 'Localização')
content = content.replace('SaÃ­da', 'Saída')
content = content.replace('ProprietÃ¡rio', 'Proprietário')
content = content.replace('Ã gua', 'Água')
content = content.replace('ConcessionÃ¡ria', 'Concessionária')
content = content.replace('ElÃ©trica', 'Elétrica')
content = content.replace('PolÃ­cia', 'Polícia')
content = content.replace('IdentificaÃ§Ã£o', 'Identificação')
content = content.replace('ResponsÃ¡vel', 'Responsável')
content = content.replace('digitaÃ§Ã£o', 'digitação')
content = content.replace('VocÃª', 'Você')
content = content.replace('NÃºmero', 'Número')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
