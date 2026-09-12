# Sistema de Backup

O sistema de backup do FireNotes é completo, confiável e funciona tanto manualmente quanto de forma automática em segundo plano.

## O que é incluído no backup?

O backup cria um arquivo `.zip` contendo:

| Conteúdo | Caminho no ZIP | Descrição |
|----------|----------------|-----------|
| Banco de dados | `db/dailynote.db` | Todos os dados do app |
| WAL do banco | `db/dailynote.db-wal` | Transações pendentes |
| SHM do banco | `db/dailynote.db-shm` | Memória compartilhada |
| Preferências | `sp/*.xml` | Configurações e autenticação |
| Arquivos internos | `files/**` | Fotos, documentos internos |
| Arquivos externos | `ext/**` | Mídias do app no armazenamento externo |

## Módulos Cobertos pelo Backup

✅ Militares e efetivo  
✅ Viaturas  
✅ Escalas de serviço  
✅ Ocorrências, vítimas e veículos  
✅ Fotos das ocorrências  
✅ Calendário (eventos e tarefas)  
✅ Configurações do app  
✅ Tema, biometria, PIN  
✅ Histórico de backups  

## Backup Manual

1. Abra o app → **Configurações** → **Backup**
2. Toque em **"Fazer Backup Agora"**
3. Acompanhe o progresso na barra de status e notificação Android
4. Ao concluir, a notificação confirma o sucesso

## Backup Automático

Executa automaticamente conforme a frequência configurada:

| Frequência | Comportamento |
|------------|---------------|
| Diário | Executa toda manhã às 08:00 |
| Semanal | Executa às 08:00, uma vez por semana |
| Mensal | Executa às 08:00, uma vez por mês |
| Desativado | Nenhum backup automático |

> O backup automático é agendado via **WorkManager** e resiste a reinicializações do dispositivo.

## Restauração

1. **Configurações** → **Backup** → **Restaurar**
2. Selecione o arquivo de backup na lista do Google Drive
3. Aguarde o download e extração (acompanhe na notificação)
4. O app será reiniciado automaticamente com todos os dados restaurados

## Armazenamento no Google Drive

Os backups são salvos na pasta privada `appDataFolder` do Google Drive:
- **Não visível** no Drive do usuário
- **Não acessível** por outros aplicativos
- **Gratuito** — não consome cota do Drive pessoal do usuário

## Histórico de Backups

A tela de Backup exibe o histórico completo com:
- Data e hora de cada backup
- Tamanho do arquivo (em MB)
- Status: Sucesso / Falha
- Tipo: Manual / Automático

## Arquitetura Técnica

```
SettingsViewModel
    └── triggerBackup()
          └── WorkManager.enqueue(GoogleDriveBackupWorker)
                └── GoogleDriveBackupService
                      ├── PRAGMA wal_checkpoint(FULL)
                      ├── createBackupZip() → cacheDir/Backup_YYYY_MM_DD.zip
                      └── uploadBackupToDrive(accessToken)
                            ├── Multipart/related HTTP POST
                            ├── → https://googleapis.com/upload/drive/v3/files
                            └── Notificação de progresso em tempo real
```
