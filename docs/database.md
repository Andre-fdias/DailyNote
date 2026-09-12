# Estrutura do Banco de Dados (Room)

O aplicativo armazena todos os seus registros de forma persistente através do Room (`AppDatabase`).

## Módulos Principais

### Ocorrências
- `nova_ocorrencias`: Guarda o cabeçalho (data, natureza, talao, guarnicao, endereço).
- `nova_vitimas`: Registra pacientes/vítimas, integrando com o diagrama corporal (Json serializado na coluna `lesoesEstruturadasJson`).
- `novo_veiculos_envolvidos`: Detalhamento de veículos, proprietários e OCR.

### Mapa Força e Recursos
- `quartel`, `viaturas`, `militares`: Cadastro base.
- `equipe_servico`, `equipe_viatura`: Escala de serviço e guarnições formatadas em JSON.

### Agenda e Configurações
- `calendar_eventos`, `calendar_tarefas`: Planejamento diário da rotina militar.
- `configuracoes`, `backup_log`: Histórico e definições legadas (em migração para o DataStore).

## Migrações (Migrations)
Sempre que uma nova entidade ou coluna for adicionada, a versão (`version`) no `@Database` é incrementada e um script de `Migration` deve ser fornecido obrigatoriamente. A função de destruição (`fallbackToDestructiveMigration`) foi removida para garantir retenção perpétua de dados em ambiente de produção.
