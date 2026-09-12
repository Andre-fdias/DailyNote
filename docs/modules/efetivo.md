# Módulo de Efetivo

O módulo de Efetivo centraliza o gerenciamento do quadro de militares do quartel, integrando dados locais com a planilha Google Sheets oficial.

## Fonte de Dados

Os dados do efetivo são carregados a partir de uma planilha Google Sheets vinculada:

| Aba | Conteúdo |
|-----|----------|
| `Efetivo` | Dados cadastrais dos militares |
| `Folga mensal` | Folgas mensais por militar |
| `Afastamentos` | Afastamentos com data início/fim e tipo |

### Mapeamento de Colunas — Aba "Afastamentos"

| Coluna | Campo |
|--------|-------|
| A | Nome do Militar |
| B | Tipo de Afastamento |
| C | Dias de Afastamento |
| D | Data de Início |
| E | Data de Término |
| F | Observações |

## Funcionalidades

### Listagem de Militares
- Visualização em cards com foto e insígnia
- Filtro por posto/graduação, situação, especialização
- Indicadores visuais de afastamento ativo

### Card de Detalhe
Ao tocar em um militar, é exibida a ficha completa com:
- Dados pessoais e funcionais
- Especializações (mergulhador, OVB, motorista)
- **Card de Afastamentos**: lista de todos os afastamentos do militar com status (ativo / futuro / encerrado)
- Histórico de escalas

### Aba de Afastamentos (Dashboard Efetivo)
Na aba "Efetivo" do dashboard principal:
- **Afastamentos Ativos Hoje**: militares com afastamento em vigor, exibindo o tipo e a data de término
- **Próximos Afastamentos**: afastamentos com início nos próximos 30 dias
- Cada card exibe: nome do militar, tipo, data de início, data de fim

### Folgas no Card de Novidades (Home)
O card "Novidades de Efetivo" na Home lista:
- Todos os eventos que **estão em vigor hoje** ou que **iniciam nos próximos 5 dias**:
  - Folgas mensais
  - Afastamentos ativos
  - Alertas de vencimento (CNH, mergulho, OVB)
- Um militar pode aparecer em múltiplas categorias simultaneamente

## Regras de Exibição

| Situação | Exibido na Home? |
|----------|-----------------|
| Afastamento ativo hoje | ✅ Sim |
| Afastamento que inicia em até 5 dias | ✅ Sim |
| Afastamento que termina amanhã | ✅ Sim |
| Afastamento encerrado ontem | ❌ Não |
| Folga ativa hoje | ✅ Sim |
| Vencimento em até 5 dias | ✅ Sim |
