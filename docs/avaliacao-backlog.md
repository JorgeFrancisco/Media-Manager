# Avaliação dos itens de backlog (médio/longo prazo)

> Documento de análise — **nenhuma implementação**. Verificado contra o estado
> atual do código (versão `4.1.5.14`). Cada item traz a afirmação original, a
> evidência encontrada no código e um veredito.

## Médio prazo

### 1. Job de limpeza para registros MISSING/DELETED — ✅ procede, mas o esforço estimado está superestimado

**Afirmação:** "não existe nenhum `@Scheduled` no projeto" e "exigirá infraestrutura de agendamento".

**Verificação:**

- Literalmente não há a anotação `@Scheduled` — **mas** o projeto tem infraestrutura de agendamento própria e **deliberada** (evita `@EnableScheduling`, documentado nos Javadocs), via `ScheduledExecutorService` + `scheduleWithFixedDelay`: `ReconcileScheduler`, `QuarantinePurgeScheduler`, `InventoryWatchService`.
- Já existe **padrão de retenção configurável + purga destrutiva cuidadosa**: `ExecutionRetentionService` (`deleteOlderThanDays`/`keepLatest`, limite vindo de `AppSetting`) e `QuarantinePurgeService`/`QuarantinePurgeScheduler` (janela de retenção lida das settings).
- O reconcile de fato marca `MISSING` e não há purga desses registros de `catalog_file` → o acúmulo é real.

**Veredito:** o problema **procede** (registros `MISSING` acumulam, sem limpeza). Porém o esforço é **menor** do que o descrito: os três pilares citados como "a implementar" (agendamento, retenção configurável, purga destrutiva com cuidado) **já existem como padrão reutilizável**. É aplicar o padrão a `catalog_file` `MISSING`/`DELETED`, não construir do zero. Atenção legítima ao ponto destrutivo (validar via teste de integração, como as demais purgas).

### 2. Explorer mobile-friendly — ✅ procede

**Verificação:** apenas `pages/files.css` tem responsividade e com **um único** `@media`; o uso de `@media` é escasso no app em geral; o explorer é centrado em tabelas, breadcrumbs e drives. Não há sinal de refatoração responsiva recente.

**Veredito:** **procede**. Trabalho de UX/responsividade, sem dependências técnicas. Recomendo uma avaliação visual (drives, breadcrumb, tabela) para dimensionar o escopo.

## Longo prazo

### 1. `API_DEFAULT_PAGE_SIZE` com efeito real — ⚠️ procede, e hoje é **configuração inerte**

**Verificação:**

- A setting `API_DEFAULT_PAGE_SIZE` (`AppSettingService`) e o `Api.defaultPageSize` (config properties) existem, são semeados e editáveis na tela de configurações.
- **`defaultPageSize` não é lido em lugar nenhum** (busca retornou zero ocorrências fora da definição). Os controllers de API (`MediaController`, `DuplicateController`) usam `@PageableDefault(size = 50)` fixo; os web controllers usam constantes próprias + `PageUtils`.

**Veredito:** **procede** e, na prática, é pior que "baixo retorno": hoje é **config morta** — o usuário altera na tela e nada acontece. Dois caminhos: (a) torná-la efetiva (exige um `HandlerMethodArgumentResolver` custom substituindo a resolução de paginação do Spring Data, como descrito) — baixo retorno; ou (b) **remover** a setting inerte — limpeza barata que elimina uma inconsistência de UX. Sugiro decidir entre (a) e (b) em vez de deixar como está.

### 2. Bytecode Enhancement do Hibernate — ⚠️ procede, e a relevância **subiu um pouco**

**Verificação:**

- Sem `hibernate-enhance-maven-plugin` no `pom.xml`.
- Há 5 `@OneToOne(fetch = LAZY)`. Os do lado filho com `@MapsId` (`CatalogFileLocation`, `ExecutionMetrics`, `MediaMetadata`, `Photo`, `Video`) conseguem lazy real (a FK/PK é conhecida).
- Porém a extração recente do `execution_metrics` adicionou em `Execution` um `@OneToOne(mappedBy = "execution", fetch = LAZY)` **inverso e opcional**. Sem bytecode enhancement, um `@OneToOne` inverso opcional **não** fica lazy — o Hibernate carrega `execution_metrics` de forma **eager** a cada load de `Execution`, ou seja, um SELECT extra por execução na rota **frequente** de histórico/dashboard (N+1).

**Veredito:** **procede**; impacto geral baixo, mas **subiu um degrau** por conta desse inverso na rota frequente. Duas opções: (a) adotar o plugin (resolve o inverso e demais casos), ou (b) tornar `ExecutionMetrics` **unidirecional** (apenas filho→pai, sem o campo inverso em `Execution`), eliminando o eager — mais barato e localizado. A opção (b) é a de melhor custo/benefício se o objetivo for só evitar o N+1 introduzido.

## Resumo

| Item | Procede? | Nuance principal |
|---|---|---|
| Cleanup `MISSING`/`DELETED` | Sim | Infra de agendamento/retenção/purga **já existe** — esforço menor que o estimado |
| Explorer mobile | Sim | UX puro; avaliação visual recomendada |
| `API_DEFAULT_PAGE_SIZE` | Sim | Hoje é **config inerte**; considerar **remover** em vez de só "ligar" |
| Bytecode enhancement | Sim | Relevância subiu pelo `@OneToOne` inverso do `execution_metrics` (N+1); alternativa barata é tornar `ExecutionMetrics` unidirecional |
