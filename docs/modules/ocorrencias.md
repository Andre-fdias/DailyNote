# Módulo de Ocorrências

O módulo de Ocorrências permite o registro profissional e completo dos atendimentos realizados pela guarnição.

## Estrutura de uma Ocorrência

```
NovaOcorrencia
├── Dados Gerais (natureza, data, hora, equipe, viatura, endereço, fotos)
├── NovaVitima[] (lista de vítimas)
│     ├── Dados pessoais
│     ├── Lesões (texto + body map interativo)
│     ├── Sinais vitais
│     └── Destino de socorro
└── NovoVeiculo[] (veículos envolvidos)
      ├── Placa, modelo, cor
      ├── CRLV por OCR (câmera)
      └── Dados do proprietário/condutor
```

## Funcionalidades

### Registro de Ocorrência
1. **Dados Gerais**: Natureza, data/hora, equipe, viatura, endereço com autocompletar, histórico narrativo
2. **Geolocalização**: Captura automática do GPS com visualização no mapa
3. **Fotos**: Câmera integrada com editor de anotações (texto, setas, destaques)
4. **Apoios**: Registro de apoios externos recebidos (SAMU, PM, etc.)

### Módulo de Vítimas
- Body map interativo para marcação de lesões
- Categorias de lesão: cortante, contuso, queimadura, etc.
- Sinais vitais: PA, FC, FR, SpO2, Glasgow
- Destino: hospital, recusa atendimento, óbito
- Nome do médico e CRM para transferência hospitalar

### Módulo de Veículos
- Leitura de CRLV por OCR (câmera) — extrai dados automaticamente
- Dados do condutor e proprietário
- Fotos do veículo
- Vínculo com condutor (pesquisa na base interna)

### Exportação e Compartilhamento
- **Exportar**: Gera arquivo `.json` com toda a ocorrência (incluindo fotos em base64)
- **Compartilhar**: Envia via WhatsApp, email, ou qualquer app

### Importação
- Recebe arquivo `.json` de outro dispositivo
- Opções ao importar:
  - **Sobrescrever**: Substitui ocorrência existente
  - **Mesclar**: Atualiza campos diferentes
  - **Duplicar**: Cria nova ocorrência com novo ID

## Histórico de Ocorrências

A tabela `ocorrencias` mantém um histórico consolidado com:
- Protocolo único
- Natureza
- Coordenadas
- Data/hora
- Status (Aberta / Concluída / Arquivada)

## Consulta e Filtros

Na tela "Consultar Ocorrências":
- Filtro por data, natureza, equipe, viatura
- Busca por endereço ou protocolo
- Visualização no mapa com pinos por localização
- Acesso rápido ao PDF/relatório
