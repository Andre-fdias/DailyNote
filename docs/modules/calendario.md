# Módulo de Calendário

O módulo de Calendário oferece gerenciamento completo de eventos e tarefas com sincronização opcional com o Google Calendar.

## Tipos de Itens

| Tipo | Descrição |
|------|-----------|
| `CalendarEvento` | Evento com data/hora de início e fim, local, descrição |
| `CalendarTarefa` | Tarefa com prazo, prioridade e status de conclusão |

## Sincronização com Google Calendar

### Configuração
1. Faça login com sua conta Google em **Configurações → Conta**
2. Autorize o escopo `calendar.events`
3. Toque em **"Sincronizar Calendário"** na tela do calendário

### Comportamento da Sincronização
- Eventos criados localmente → enviados ao Google Calendar
- Eventos do Google Calendar → importados para o app
- Eventos com `googleEventId` preenchido estão sincronizados
- Conflitos são resolvidos pela data de modificação mais recente

## Notificações

O sistema de notificações é altamente configurável:

| Configuração | Opções |
|-------------|--------|
| Antecedência | 5, 10, 15, 30, 60 minutos |
| Som | Habilitado / Desabilitado |
| Vibração | Habilitada / Desabilitada |
| Badge | Número de eventos pendentes no ícone |
| Popup matinal | Exibe eventos do dia ao abrir o app |

## Agenda Unificada

A tela de Agenda combina em ordem cronológica:
- Eventos do calendário
- Tarefas com prazo
- Afastamentos do efetivo
- Folgas mensais
- Alertas de vencimento

## Estrutura do Banco de Dados

### calendar_eventos
```sql
id             TEXT PRIMARY KEY
titulo         TEXT
descricao      TEXT
dataInicio     TEXT
dataFim        TEXT
local          TEXT
cor            INTEGER
googleEventId  TEXT  -- NULL se não sincronizado
```

### calendar_tarefas
```sql
id             TEXT PRIMARY KEY
titulo         TEXT
descricao      TEXT
prazo          TEXT
concluida      INTEGER (0/1)
prioridade     TEXT (BAIXA/MEDIA/ALTA)
googleEventId  TEXT
```

### calendar_settings
```sql
id                          TEXT PRIMARY KEY
mostrarPopupInicial         INTEGER
badgeHabilitado             INTEGER
somHabilitado               INTEGER
vibracaoHabilitada          INTEGER
lembretesAntecipadosMinutos INTEGER
popupExibidoHoje            TEXT
calendarioConfigurado       INTEGER
```
