<div align="center">

# 🔥 FireNotes

**Sistema de Gestão Operacional para Bombeiros**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetbrains)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

[📖 Documentação Completa](https://andre-fdias.github.io/DailyNote) · [🐛 Reportar Bug](https://github.com/Andre-fdias/DailyNote/issues) · [💡 Solicitar Feature](https://github.com/Andre-fdias/DailyNote/issues)

</div>

---

## 📋 Sobre o Projeto

**FireNotes** é um aplicativo Android completo desenvolvido para bombeiros e equipes de resposta de emergência. Centraliza toda a gestão operacional do quartel em um único sistema mobile: efetivo, viaturas, ocorrências, escalas de serviço, calendário, e muito mais — funcionando **offline-first** com sincronização opcional via Google Drive.

### ✨ Principais Funcionalidades

| Módulo | Descrição |
|--------|-----------|
| 🏠 **Dashboard** | Visão geral do quartel com alertas, prontidão e avisos do dia |
| 👥 **Efetivo** | Cadastro completo de militares com insígnias, especializações e afastamentos |
| 🚒 **Viaturas** | Gestão de frota com status, fotos e histórico de manutenção |
| 📋 **Escala de Serviço** | Montagem de equipes, viaturas e turnos com validação |
| 🚨 **Ocorrências** | Registro detalhado com vítimas, veículos, fotos georreferenciadas e exportação |
| 📅 **Calendário** | Eventos e tarefas sincronizados com Google Calendar |
| 📊 **Agenda** | Visualização unificada de eventos, folgas e afastamentos |
| 💾 **Backup** | Backup automático e manual para Google Drive com progresso em tempo real |
| ⚙️ **Configurações** | Tema, biometria, PIN, frequência de backup e preferências |

---

## 🛠️ Tecnologias Utilizadas

```
Linguagem       → Kotlin
UI              → Jetpack Compose + Material 3
Arquitetura     → MVVM + Clean Architecture
Injeção de Dep. → Dagger Hilt
Banco de Dados  → Room (SQLite WAL)
Assíncrono      → Coroutines + Flow
Tarefas BG      → WorkManager
Mapas           → MapLibre GL + OSMDroid
Auth            → Google Sign-In (OAuth 2.0)
Backup          → Google Drive REST API v3
Câmera          → CameraX + UCrop
Biometria       → BiometricPrompt
```

---

## 🏗️ Arquitetura

O projeto segue **Clean Architecture** com separação em três camadas:

```
app/
├── data/
│   ├── local/          # Room DB, DAOs, Entities
│   ├── repository/     # Implementações dos repositórios
│   ├── service/        # Google Drive, Google Sheets
│   ├── worker/         # WorkManager (backup agendado)
│   └── receiver/       # BroadcastReceivers
│
├── domain/
│   ├── model/          # Modelos de negócio puros
│   ├── repository/     # Interfaces dos repositórios
│   └── calendar/       # Lógica do calendário/notificações
│
└── ui/
    ├── screens/        # Telas organizadas por feature
    ├── navigation/     # NavGraph e rotas
    └── widgets/        # App Widgets Android
```

---

## 🗄️ Banco de Dados

O aplicativo utiliza um único banco de dados SQLite (`dailynote.db`) gerenciado pelo Room:

| Tabela | Descrição |
|--------|-----------|
| `militares` | Cadastro de militares |
| `viaturas` | Frota de viaturas |
| `quartel` | Dados do quartel |
| `equipe_servico` | Escalas de serviço |
| `equipe_viatura` | Relação equipe-viatura |
| `escala_config` / `turnos` | Configuração das escalas |
| `calendar_eventos` | Eventos do calendário |
| `calendar_tarefas` | Tarefas do calendário |
| `nova_ocorrencias` | Ocorrências registradas |
| `nova_vitimas` | Vítimas por ocorrência |
| `novo_veiculos_envolvidos` | Veículos por ocorrência |
| `ocorrencias` | Histórico consolidado |
| `configuracoes` | Preferências do app |
| `backup_log` | Histórico de backups |

---

## 💾 Sistema de Backup

O backup é **completo** e inclui:
- ✅ Banco de dados (`dailynote.db` + WAL)
- ✅ SharedPreferences (configurações e autenticação)
- ✅ Arquivos internos (fotos, documentos)
- ✅ Arquivos externos do app (mídias)

### Backup Manual
Configurações → Backup → **Fazer Backup Agora**  
Exibe barra de progresso em tempo real via notificação Android.

### Backup Automático
Executa diariamente às **08:00** via WorkManager.  
Frequência configurável: Diário / Semanal / Mensal / Desativado.

### Restauração
Configurações → Backup → **Restaurar** → Selecionar arquivo do Drive.

---

## 📱 Widgets Android

O app oferece 4 widgets para a tela inicial:

| Widget | Descrição |
|--------|-----------|
| `Dashboard` | Resumo do turno atual |
| `Agenda` | Próximos eventos do dia |
| `Alertas` | Avisos e vencimentos críticos |
| `Ações Rápidas` | Acesso rápido às principais funções |

---

## 🔐 Segurança

- Autenticação por **Biometria** (impressão digital / face)
- Autenticação por **PIN** numérico de 6 dígitos
- Bloqueio automático ao enviar o app para background
- Dados de backup armazenados na pasta privada `appDataFolder` do Google Drive (não acessível por terceiros)

---

## 🚀 Como Executar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17+
- Android SDK API 26+
- Conta Google (para funcionalidades de sincronização)

### Configuração

1. Clone o repositório:
   ```bash
   git clone git@github.com:Andre-fdias/DailyNote.git
   cd DailyNote
   ```

2. Configure o OAuth 2.0 no [Google Cloud Console](https://console.cloud.google.com):
   - Crie um projeto e habilite: Drive API, Calendar API, Sheets API
   - Crie credenciais OAuth para Android com o SHA-1 do seu keystore
   - Consulte [`docs/OAUTH_SETUP.md`](docs/OAUTH_SETUP.md) para instruções detalhadas

3. Sincronize o projeto no Android Studio e execute no dispositivo:
   ```bash
   ./gradlew installDebug
   ```

---

## 📖 Documentação

A documentação completa está disponível em **[andre-fdias.github.io/DailyNote](https://andre-fdias.github.io/DailyNote)**:

- [Arquitetura Técnica](docs/architecture.md)
- [Banco de Dados](docs/database.md)
- [Configuração OAuth](docs/OAUTH_SETUP.md)
- [Guia do Backup](docs/backup.md)
- [Módulo de Efetivo](docs/modules/efetivo.md)
- [Módulo de Ocorrências](docs/modules/ocorrencias.md)
- [Módulo de Calendário](docs/modules/calendario.md)
- [Módulo de Escalas](docs/modules/escalas.md)

---

## 🤝 Contribuição

Este é um projeto proprietário para uso interno operacional. Para sugestões ou bugs, abra uma [Issue](https://github.com/Andre-fdias/DailyNote/issues).

---

<div align="center">

**Desenvolvido com ❤️ para o Corpo de Bombeiros**

</div>
