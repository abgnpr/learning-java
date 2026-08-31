# Spring Senior Backend Core — Build Checklist

This tracks **material construction**, not interview readiness. A checked
kit exists, is linked from the repository indexes, and covers the stated
scope. Its own rep scorecard remains the authority on whether it has been
studied aloud.

## Built foundation

- [x] [Spring Boot core](spring-boot-basics.md) — container/DI basics,
  configuration, auto-configuration, MVC, validation, JPA vocabulary,
  transaction proxies, test slices and production-awareness topics.
- [x] [Spring Security senior core](spring-security-basics.md) — filter
  chains, authentication, route/method authorization, SpEL, sessions,
  JWT resource server, CSRF/CORS, 401/403 boundaries and security tests.
  - [x] Concrete `@PreFilter` and `@PostFilter` drills, including
    `filterTarget`, `filterObject`, filtering versus rejection, query-time
    tenant constraints and paging/performance traps.
- [x] [JPA/Hibernate performance core](spring-data-jpa-performance.md) —
  persistence context and entity states, mappings, fetch plans/N+1,
  projections, pagination, batching/bulk DML, locking, caches, OSIV,
  diagnostics and query-budget tests.
- [x] [Deep transactions core](spring-boot-transactions-deep.md) — logical
  and physical scopes, rollback-only behavior, propagation/resource costs,
  isolation/deadlocks, programmatic boundaries, remote calls, idempotency,
  database/Kafka coordination, outbox, saga and production diagnostics.
- [x] [Resilience core](spring-boot-resilience.md) — deadline budgets,
  retry/idempotency, circuit breakers, bulkheads, rate limits, saturation,
  safe degradation, graceful shutdown and fault/load testing.
- [x] [Container internals core](spring-container-internals.md) — metadata,
  definitions/registry, factory/context, refresh and bean lifecycle,
  post-processors, proxy creation, `FactoryBean` and configuration modes.

## Required senior-core kits

Do not mark a kit complete merely because another note mentions some of its
vocabulary. For the current banking-MNC target, build the unchecked kits in
this interview-payoff order: **observability → production testing**. The
numbered sections retain the original gap taxonomy.

### 1. Deep transactions — complete

- [x] Create `spring-boot-transactions-deep.md`.
- [x] Logical transaction scopes versus the physical resource transaction.
- [x] `REQUIRED` participation, rollback-only state and
  `UnexpectedRollbackException`.
- [x] `REQUIRES_NEW`: suspension, independent outcome, extra connection and
  connection-pool exhaustion/deadlock risk.
- [x] `NESTED`: savepoints, JDBC transaction-manager support and provider/
  database limitations.
- [x] Rollback rules, checked exceptions and caught-exception traps.
- [x] Isolation, `readOnly`, timeout and explicit transaction-manager
  selection in multi-datasource applications.
- [x] Declarative transactions versus `TransactionTemplate` and transaction
  synchronization callbacks.
- [x] Remote calls outside database transactions; connection/lock holding and
  ambiguous timeouts.
- [x] Database deadlocks, deterministic lock ordering and bounded retry of an
  idempotent whole unit of work.
- [x] Why one local database transaction cannot atomically cover Kafka or
  another service.
- [x] Transactional outbox, saga/compensation and idempotent consumers.
- [x] Production scenarios, trap wall, answer card and rep scorecard.

### 2. Container internals — complete

- [x] Create `spring-container-internals.md`.
- [x] Configuration metadata from scanning, `@Configuration`, `@Bean`, XML
  awareness and programmatic registration.
- [x] `BeanDefinition` as a creation recipe and
  `BeanDefinitionRegistry` as the metadata registry.
- [x] `BeanFactory` creation/wiring responsibilities versus the additional
  environment, event, resource and lifecycle facilities of
  `ApplicationContext`.
- [x] The context refresh sequence: definition loading, factory
  post-processing, post-processor registration, singleton creation,
  lifecycle callbacks and ready events.
- [x] `BeanFactoryPostProcessor` versus `BeanPostProcessor`, including why
  definitions are changed before instances and proxies after instantiation.
- [x] AOP proxy creation and why the injected bean may differ from the raw
  target instance.
- [x] `FactoryBean<T>` product versus the factory itself and the `&beanName`
  lookup distinction.
- [x] `@Configuration(proxyBeanMethods = true/false)`, inter-bean method
  calls and lite configuration trade-offs.
- [x] Ordering, early instantiation and circular-dependency failure modes.
- [x] Production scenarios, trap wall, answer card and rep scorecard.

### 3. Production testing

- [ ] Create `spring-boot-testing-deep.md` without duplicating the Security
  and JPA kits' detailed drills.
- [ ] Plain unit tests versus Spring slices versus full integration tests.
- [ ] Selection matrix for `@WebMvcTest`, `@DataJpaTest` and
  `@SpringBootTest`.
- [ ] Testcontainers with the production database and
  `@ServiceConnection`.
- [ ] Test-managed transaction rollback traps: deferred flush, false
  positives and client/server-thread transaction boundaries.
- [ ] Security tests using `@WithMockUser`, request post-processors, CSRF and
  JWT support.
- [ ] External dependency stubs with WireMock or equivalent, including
  timeout/error contracts.
- [ ] Repository statement-count/N+1 regression tests and realistic data
  cardinality.
- [ ] Concurrency tests for optimistic conflicts, atomic updates and
  deadlock/retry behavior.
- [ ] Idempotency tests covering duplicate and concurrent requests.
- [ ] Contract tests, deterministic fixtures/`@Sql`, test isolation and
  parallel-test safety.
- [ ] Production scenarios, trap wall, answer card and rep scorecard.

### 4. Resilience — complete

- [x] Create `spring-boot-resilience.md`.
- [x] End-to-end timeout budgets before retries; connect, read and pool-wait
  timeouts.
- [x] Retry only transient failures and only when the operation is
  idempotent or protected by an idempotency key.
- [x] Exponential backoff, jitter, attempt limits and retry amplification.
- [x] Circuit breaker states and failure/slow-call thresholds.
- [x] Semaphore/thread-pool bulkheads and executor isolation.
- [x] Rate limiting as overload/abuse control rather than authentication.
- [x] Connection-pool and executor saturation, queue growth, rejection and
  backpressure/load shedding.
- [x] Graceful degradation, fallbacks and avoiding stale/unsafe financial
  answers.
- [x] Graceful shutdown interaction with traffic draining and in-flight
  work.
- [x] Resilience4j configuration, aspect ordering and `@Retryable` proxy
  traps.
- [x] Production scenarios, trap wall, answer card and rep scorecard.

### 5. Observability and diagnosis

- [ ] Create `spring-boot-observability.md`.
- [ ] Structured logs with stable fields, redaction and correlation/trace
  IDs.
- [ ] Micrometer counters, gauges, timers and distribution percentiles;
  tag-cardinality control.
- [ ] Distributed tracing with Micrometer Tracing/OpenTelemetry and context
  propagation across HTTP, executors and messaging.
- [ ] Actuator exposure/security, custom observations and health indicators.
- [ ] Liveness versus readiness and dependency-health design.
- [ ] Golden signals: latency, traffic, errors and saturation.
- [ ] Diagnose a slow endpoint across controller/filter time, executor queue,
  connection-pool wait, SQL/lock time and downstream calls.
- [ ] Dashboards and alerts based on service objectives rather than raw
  totals.
- [ ] Production scenarios, trap wall, answer card and rep scorecard.

## Completion gate for every new kit

- [ ] Uses the repository drill format: 🔮 predict, 🛠 build, 🐛 fix and
  💭 explain, with answers under `<details>`.
- [ ] Has a Pareto core path, version boundary, primary official references,
  production scenarios, trap wall, answer card and rep scorecard.
- [ ] Cross-links related material instead of copying large explanations.
- [ ] Is added to `README.md`, `CLAUDE.md` and the Spring Boot extension
  queue.
- [ ] Passes `git diff --check` and `python3 tools/check-links.py`.

## Outside this checklist

Messaging, caching/scheduling, API design, WebFlux, native/AOT, Spring Batch
and Spring Cloud remain job-description-driven extensions. They are useful
breadth, but they are not required to close the nine senior-core gaps tracked
here.
