# ConsultaSeloDigital

Aplicação desktop em Java 21 para consultar selos digitais em lote nos serviços públicos do TJPB, TJRN e TJPE.

## Visão geral

O ConsultaSeloDigital centraliza a consulta de selos de diferentes tribunais em uma interface única. A aplicação aceita entrada manual ou importação de arquivos, executa as consultas em sequência, mantém a ordem original e apresenta os resultados em tabelas específicas para cada tribunal.

## Tribunais atendidos

- Tribunal de Justiça da Paraíba — TJPB;
- Tribunal de Justiça do Rio Grande do Norte — TJRN;
- Tribunal de Justiça de Pernambuco — TJPE.

O TJAL é mantido somente para leitura e exibição de históricos antigos; o código não inicia novas consultas para esse tribunal.

## Funcionalidades

- consulta individual ou em lote;
- importação de selos por arquivos TXT, CSV e XLSX;
- normalização e validação dos identificadores conforme o tribunal;
- acompanhamento do progresso da consulta;
- estratégias e parsers específicos para TJPB, TJRN e TJPE;
- visualização de resultados e detalhes;
- geração de QR Code;
- histórico de consultas;
- exportação de resultados e detalhes em PDF e XLSX;
- interface desktop com tema FlatLaf.

## Tecnologias

- Java 21;
- Java Swing;
- Java HTTP Client;
- FlatLaf;
- Jackson Databind;
- Apache PDFBox;
- Apache POI;
- ZXing;
- Maven;
- `jpackage` para o instalador Windows configurado no `pom.xml`.

## Arquitetura

O projeto separa cada integração por meio da interface `ConsultaTribunal` e da `TribunalFactory`. Cada tribunal possui sua própria estratégia de consulta e seu próprio parser, enquanto `ConsultaSeloService` coordena o processamento em lote e a notificação de progresso.

```text
src/main/java/com/selodigital/
├── config/            # configurações da aplicação
├── consulta/          # integrações, estratégias e parsers
├── exportacao/        # geração de PDF e XLSX
├── historico/         # histórico de consultas
├── interfacegrafica/  # janelas e componentes Swing
├── modelo/            # modelos do domínio
├── theme/             # tema visual
└── util/              # HTTP, QR Code, ícones e normalização
```

## Requisitos

- JDK 21;
- Apache Maven;
- conexão com a internet;
- disponibilidade dos serviços públicos consultados;
- ambiente Windows configurado para a geração do instalador `.exe`, quando essa etapa for utilizada.

## Compilação

Para compilar as classes:

```bash
mvn clean compile
```

Para executar o empacotamento definido no `pom.xml`:

```bash
mvn clean package
```

O empacotamento inclui um JAR com dependências e a etapa configurada com `jpackage` para geração do instalador Windows.

## Uso básico

1. Selecione o tribunal.
2. Informe os selos manualmente ou importe um arquivo TXT, CSV ou XLSX.
3. Inicie a consulta e acompanhe o progresso.
4. Analise os resultados e abra os detalhes quando necessário.
5. Exporte os dados em PDF ou XLSX.

## Observações

- As consultas dependem da disponibilidade e da estrutura das páginas públicas dos tribunais.
- Alterações nos serviços externos podem exigir atualização das estratégias ou dos parsers.
- Os resultados devem ser conferidos no serviço oficial quando usados em um processo que exija validação formal.
