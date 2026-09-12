# Módulo de Escalas de Serviço

O módulo de Escalas gerencia a montagem dos turnos operacionais, associando militares e viaturas a cada guarnição.

## Estrutura de Dados

```
EscalaConfig
  └── Turnos (A, B, C, D...)
        └── EquipeServico (Guarnição do turno)
              ├── Militares escalados
              └── EquipeViatura (Viatura + militares por viatura)
```

## Funcionalidades

### Configuração de Turnos
- Criação de ciclos de turno (A, B, C, D, etc.)
- Definição do oficial de dia, subtenente e demais funções
- Seleção de militares disponíveis (exclui afastados)

### Montagem de Guarnição
- Seleção de militares por função
- Atribuição de viaturas à guarnição
- Para cada viatura: definição da guarnição embarcada (comandante, motorista, etc.)

### Visualização
- Card de escala atual no Dashboard
- Lista de escalas futuras
- Exportação da escala (JSON)

## Integração com Efetivo

O sistema cruza automaticamente:
- Militares com **afastamento ativo** → marcados como indisponíveis
- Militares com **folga** → marcados como indisponíveis
- Indicação visual no seletor de militares

## Validações

| Regra | Comportamento |
|-------|---------------|
| Militar afastado | Não aparece na seleção |
| Militar em folga | Não aparece na seleção |
| Viatura inoperante | Marcada com aviso |
| Guarnição incompleta | Alerta ao salvar |
