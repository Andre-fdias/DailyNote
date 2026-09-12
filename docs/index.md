---
layout: default
title: FireNotes - Sistema de Gestão Operacional para Bombeiros
description: Documentação completa do aplicativo FireNotes para Android
---

<div align="center">
  <h1>🔥 FireNotes</h1>
  <p><strong>Sistema de Gestão Operacional para Bombeiros</strong></p>
  <p>
    <a href="#módulos">Módulos</a> •
    <a href="#arquitetura">Arquitetura</a> •
    <a href="#banco-de-dados">Banco de Dados</a> •
    <a href="#backup">Backup</a> •
    <a href="#configuração">Configuração</a>
  </p>
</div>

---

## 🎯 O que é o FireNotes?

O **FireNotes** é um aplicativo Android desenvolvido para bombeiros e equipes de resposta de emergência. Funciona como o sistema operacional do quartel no celular — concentrando efetivo, viaturas, escalas, ocorrências, calendário e backup em um único lugar, com funcionamento **100% offline**.

---

## 📱 Módulos

### 🏠 Dashboard (Home)
A tela principal apresenta uma visão rápida do estado atual do quartel:
- **Escala do Dia**: Turno atual, equipe escalada e viaturas em prontidão
- **Novidades de Efetivo**: Folgas, afastamentos e licenças ativas ou próximas (até 5 dias)
- **Alertas de Vencimento**: CNH, habilitação de mergulho, OVB, etc.
- **Viaturas em Prontidão**: Quais estão disponíveis e quais estão fora de operação

### 👥 Efetivo
Cadastro completo do quadro de militares:
- Dados pessoais, posto/graduação, insígnia e foto
- Especializações: mergulhador, OVB, motorista
- Integração com planilha Google Sheets (leitura de base de dados externa)
- **Afastamentos**: Licença, férias, TDI e outros — com datas de início/fim
- **Folgas Mensais**: Sincronizadas da planilha de efetivo
- Filtro por quartel, posto, situação

### 🚒 Viaturas
Gestão completa da frota:
- Cadastro com placa, modelo, tipo e foto
- Status: Operacional / Manutenção / Inoperante
- Visualização em cards com status colorido

### 📋 Escala de Serviço
Sistema completo de montagem de escalas:
- Configuração de turnos (A, B, C, D)
- Montagem de equipes com militares e viaturas
- Validação de disponibilidade
- Geração de relatório da escala

### 🚨 Ocorrências
Registro profissional de atendimentos:
- Dados gerais: natureza, data/hora, endereço, equipe, viatura
- **Vítimas**: Dados completos, lesões, destino de socorro, sinais vitais, body map interativo
- **Veículos Envolvidos**: Placa, CRLV por OCR, proprietário, condutor
- **Fotos**: Captura com câmera integrada, anotações sobre a imagem
- **Geolocalização**: Mapa com pino do local da ocorrência
- **Exportação**: JSON para compartilhar via WhatsApp/email
- **Importação**: Receber ocorrências de outros militares com opção de Sobrescrever/Mesclar/Duplicar

### 📅 Calendário
Sistema de calendário integrado:
- Eventos e tarefas locais com lembretes
- Sincronização com Google Calendar
- Notificações antecipadas configuráveis
- Popup matinal com os eventos do dia

### 📊 Agenda
Visão unificada cronológica:
- Eventos do calendário
- Afastamentos do efetivo
- Folgas mensais
- Alertas de vencimento

### 💾 Backup & Restauração
Sistema robusto de backup:
- Upload para **Google Drive** (pasta privada `appDataFolder`)
- Inclui: banco de dados, preferências, arquivos e mídias
- Progresso em tempo real via notificação Android
- Backup automático configurável (Diário / Semanal / Mensal às 08:00)
- Restauração completa com 1 toque

---

## 🏗️ Arquitetura

O projeto segue **Clean Architecture + MVVM**:

```
┌──────────────────────────────────────────┐
│               UI Layer (Compose)          │
│  Screens → ViewModels → StateFlow         │
├──────────────────────────────────────────┤
│              Domain Layer                 │
│  UseCases · Models · Repository Interfaces│
├──────────────────────────────────────────┤
│               Data Layer                  │
│  Room DB · Google Drive · Google Sheets  │
│  WorkManager · Repositories              │
└──────────────────────────────────────────┘
```

### Stack Tecnológica

| Componente | Tecnologia |
|------------|------------|
| Linguagem | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Injeção de Dependência | Dagger Hilt |
| Banco de Dados | Room (SQLite com WAL) |
| Async | Coroutines + Flow |
| Background Jobs | WorkManager |
| Mapas | MapLibre GL + OSMDroid |
| Autenticação | Google Sign-In OAuth 2.0 |
| Câmera | CameraX + UCrop |
| Biometria | BiometricPrompt API |
| HTTP | OkHttp |
| Widgets | AppWidgetProvider |

---

## 🗄️ Banco de Dados

Um único banco Room `dailynote.db` com 17 versões de migração:

```sql
militares           -- Cadastro de militares
viaturas            -- Frota de viaturas
quartel             -- Dados do quartel
equipe_servico      -- Escalas de serviço
equipe_viatura      -- Relação equipe ↔ viatura
escala_config       -- Configuração de escalas
turnos              -- Turnos configurados
calendar_eventos    -- Eventos (com sync Google Calendar)
calendar_tarefas    -- Tarefas (com sync Google Calendar)
nova_ocorrencias    -- Ocorrências registradas
nova_vitimas        -- Vítimas por ocorrência
novo_veiculos_envolvidos  -- Veículos por ocorrência
ocorrencias         -- Histórico consolidado
configuracoes       -- Preferências e configurações
backup_log          -- Histórico de backups realizados
```

---

## 💾 Backup

### O que é incluído no backup?

| Item | Incluído |
|------|----------|
| Banco de dados (todos os módulos) | ✅ |
| Calendário, eventos e tarefas | ✅ |
| Escalas e efetivo | ✅ |
| Ocorrências e vítimas | ✅ |
| Fotos de ocorrências | ✅ |
| Configurações do app | ✅ |
| Preferências e autenticação | ✅ |

### Como restaurar?
1. Vá em **Configurações → Backup**
2. Toque em **"Restaurar"**
3. Selecione o arquivo de backup do Google Drive
4. O app reinicia automaticamente com todos os dados restaurados

---

## 🔐 Segurança

- **Biometria**: Impressão digital ou reconhecimento facial
- **PIN**: 6 dígitos com bloqueio automático ao minimizar o app
- **Drive**: Backups na pasta privada `appDataFolder` — invisível e inacessível a outros apps
- **OAuth**: Token de acesso Google gerenciado pelo sistema (refresh automático)

---

## ⚙️ Configuração e Instalação

Consulte o [Guia de Configuração OAuth](OAUTH_SETUP.md) para configurar a integração com Google APIs.

```bash
# Clonar
git clone git@github.com:Andre-fdias/DailyNote.git
cd DailyNote

# Instalar no device conectado
./gradlew installDebug
```

---

## 📌 Widgets

O app disponibiliza 4 widgets para a tela inicial do Android:

- **Dashboard**: Escala e efetivo do turno atual
- **Agenda**: Próximos eventos
- **Alertas**: Vencimentos críticos
- **Ações Rápidas**: Botões de atalho

---

<div align="center">
  <p>Desenvolvido com ❤️ para o <strong>Corpo de Bombeiros</strong></p>
  <p><a href="https://github.com/Andre-fdias/DailyNote">GitHub</a> · <a href="https://github.com/Andre-fdias/DailyNote/issues">Issues</a></p>
</div>
