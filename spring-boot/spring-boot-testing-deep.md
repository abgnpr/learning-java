# Spring Boot Production Testing — Senior Anti-Fumble Kit

For a backend engineer who can write JUnit tests but must prove **real Spring
wiring, database semantics, security boundaries, concurrency invariants and
failure recovery** without turning every test into a slow full-context suite.
The goal is a portfolio of evidence whose scope matches the production risk.

**Readable reference:** [Spring Senior Backend Reference](spring-senior-core.md),
chapter 17.

**Related deep drills:** [Spring Security](spring-security-basics.md) owns the
complete authN/authZ matrix; [JPA performance](spring-data-jpa-performance.md)
owns fetch-plan/query diagnosis; [transactions](spring-boot-transactions-deep.md)
owns propagation and rollback semantics. Exercises here integrate those
boundaries without copying their full question sets.

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/configure · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Attempt every prompt before opening its `<details>` solution. A green build is
not proof if the test never crossed the boundary named in its claim.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §8 → §9 → §10 → §11**
>
> Risk/scope · plain unit tests · slice/full selection · MVC · real database ·
> transaction traps · security/stubs · query budgets · concurrency/idempotency
> · contracts/isolation · production scenarios · traps.
>
> A senior testing answer states **risk, smallest truthful scope, real versus
> replaced components, failure injection, durable assertion and isolation**.

> **Version line.** Boot 2.7/3 projects commonly import slices from
> `org.springframework.boot.test.autoconfigure.*` and use Boot's `@MockBean`.
> Current Boot 4 has focused `*-test` modules/starters and packages such as
> `org.springframework.boot.webmvc.test.autoconfigure`; Framework
> `@MockitoBean` replaces the removed Boot mock annotation. Boot 3.1 introduced
> `@ServiceConnection`. Boot 4 adds `RestTestClient` and modularized
> Testcontainers/security/observability test dependencies. Imports below are
> intentionally omitted when they vary; use the running project's line.

---

## 0. The test portfolio · 🎯 CORE PATH

```text
many  plain unit tests
      domain rules · state machines · mapping · policy
       |
       v
focused Spring slices
      MVC contract · JPA mapping/query · client serialization
       |
       v
fewer full-context integration tests
      wiring · filters/proxies · real database/stubbed network
       |
       v
targeted black-box/fault/concurrency/load tests
      real server threads · deployment behavior · SLO/failure semantics

Each higher level proves a new boundary; none excuses weak lower levels.
```

- [ ] **0.1** 💭 Why is “use `@SpringBootTest` for everything” a weak strategy?
- [ ] **0.2** 💭 What is the smallest truthful test for a balance rule, JSON
  validation, repository lock query, filter ordering and graceful shutdown?
- [ ] **0.3** 🔮 A test mocks the repository and asserts one debit call. Does it
  prove the unique constraint or transaction rolled back?
- [ ] **0.4** 💭 Test double taxonomy: fake, stub, mock and spy—what behavior
  makes each useful?
- [ ] **0.5** 💭 What does a high-confidence test assert besides a returned DTO?

<details><summary>Solutions 0</summary>

- 0.1 Full contexts are slower, cache-sensitive and poor at localizing
  failures. They still replace production infrastructure unless supplied and
  may not start a real server. Use them only when the wiring/cross-layer
  boundary is what the test claims.
- 0.2 Plain unit test; `@WebMvcTest`; real-database JPA/integration test;
  security-enabled MVC/full test; deployed/random-port process test that sends
  termination under load. Choose the first scope that actually crosses the
  risk boundary.
- 0.3 No. It proves collaboration under mock assumptions. Database constraints,
  flush timing, manager/proxy behavior and rollback require the Spring/
  database boundary.
- 0.4 A fake is a working simplified implementation; a stub supplies canned
  responses; a mock verifies interactions; a spy wraps real behavior while
  observing/overriding parts. Prefer state/outcome assertions; interaction
  assertions matter when the interaction itself is the contract.
- 0.5 Assert durable business facts and forbidden effects, error contract,
  side-effect count, emitted/queued durable intent, and relevant telemetry.
  For money, “one ledger debit exists” is stronger than “repository.save was
  invoked once.”

</details>

---

## 1. Plain unit tests and design feedback · 🎯 CORE PATH

- [ ] **1.1** 🛠 Unit-test a payment state transition with no Spring or Mockito.
- [ ] **1.2** 💭 Why should `Clock`, random/id generation and external ports be
  injected?
- [ ] **1.3** 🐛 A unit test needs 14 mocks and 80 lines of stubbing. What design
  signal does it provide?
- [ ] **1.4** 🔮 Does `Mockito.verify(repository).save(payment)` prove the
  payment became managed or SQL executed?
- [ ] **1.5** 💭 State-based versus interaction-based testing: when is each
  appropriate?
- [ ] **1.6** 🛠 Test an orchestration branch with simple fakes and assert one
  durable intent.

<details><summary>Solutions 1</summary>

- 1.1 Keep domain behavior ordinary Java:

  ```java
  @Test
  void pendingPaymentCanBeApprovedOnce() {
      Payment payment = Payment.pending(id, money("100.00"));

      payment.approve(FIXED_INSTANT);

      assertThat(payment.status()).isEqualTo(APPROVED);
      assertThatThrownBy(() -> payment.approve(FIXED_INSTANT))
              .isInstanceOf(IllegalStateException.class);
  }
  ```

  Spring should assemble the domain, not be required to execute its rules.
- 1.2 They make time and nondeterminism controllable and expose I/O boundaries.
  Use `Clock.fixed`, deterministic suppliers and fake ports rather than sleeps,
  static mocking or unpredictable values.
- 1.3 The class probably owns too many responsibilities or exposes unstable
  implementation collaboration. Split policy/state transition from
  orchestration and group cohesive ports; do not merely create a fixture that
  hides the 14 dependencies.
- 1.4 No. A mock is not an `EntityManager`; it has no lifecycle, dirty checking,
  flush, SQL or database constraint semantics.
- 1.5 Assert state/results for business behavior because refactors should not
  break it. Verify interaction when the boundary call is the contract—no
  switch call on validation failure, same idempotency key on retry, one outbox
  intent—or when an effect has no readable state.
- 1.6 Example fake repositories store claims/events in collections. Execute
  `create(command)`, then assert the returned id and exactly one business
  payment plus one outbox intent. A later integration test proves their atomic
  database commit.

</details>

---

## 2. Unit slice and full-context selection · 🎯 CORE PATH

- [ ] **2.1** 💭 Compare `@WebMvcTest`, `@DataJpaTest` and `@SpringBootTest`.
- [ ] **2.2** 🔮 Does `@SpringBootTest` start an HTTP server by default?
- [ ] **2.3** 💭 Plain `@Mock` versus `@MockitoBean`: what graph sees each?
- [ ] **2.4** 🐛 A slice fails because a configuration-properties bean is
  missing. Should the test become a full-context test?
- [ ] **2.5** 💭 When should the context cache be invalidated with
  `@DirtiesContext`?
- [ ] **2.6** 🛠 Select a test scope for a custom auto-configuration that must
  back off when the application supplies a bean.

<details><summary>Solutions 2</summary>

- 2.1 `@WebMvcTest` loads selected MVC infrastructure/controllers and a mock
  request path; `@DataJpaTest` loads JPA/repositories and a test transaction;
  `@SpringBootTest` finds the application configuration and loads the whole
  context. Add real server/database explicitly when those are the claim.
- 2.2 No. The default mock web environment creates a web application context
  without listening on a port. Use `webEnvironment = RANDOM_PORT` or
  `DEFINED_PORT` for a real server.
- 2.3 `@Mock` is an object owned only by the test and injected manually/
  through Mockito. `@MockitoBean` overrides or supplies a bean in the Spring
  test context so all context injection points receive it.
- 2.4 Usually no. Import/enable only the configuration required by the slice,
  for example `@EnableConfigurationProperties(PaymentProperties.class)` or a
  focused `@Import`. A slice should fail if the controller secretly depends on
  unrelated application configuration.
- 2.5 Only when a test genuinely mutates context state that cannot be reset.
  It evicts a costly cached context and slows the suite. Prefer immutable
  configuration, per-test state and resettable fakes.
- 2.6 Use `ApplicationContextRunner` with the auto-configuration, assert the
  default bean exists, then add a user configuration and assert there is one
  user bean and the default backed off. It is faster and more diagnostic than
  booting the application.

  ```java
  new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(ClientAutoConfiguration.class))
      .run(context -> assertThat(context).hasSingleBean(SignedClient.class));
  ```

</details>

---

## 3. MVC API and exception contract tests · 🎯 CORE PATH

- [ ] **3.1** 🛠 Test status, JSON, validation and service-command mapping for
  one controller.
- [ ] **3.2** 🔮 Does MockMvc prove Tomcat connector, real socket or container
  error-page behavior?
- [ ] **3.3** 🐛 A test invokes the controller method directly and claims to
  prove `@Valid` and `@ControllerAdvice`. Fix the scope.
- [ ] **3.4** 💭 What should an exception-contract assertion avoid?
- [ ] **3.5** 🛠 Add a random-port smoke test. What new boundary does it prove?
- [ ] **3.6** 💭 Why test unknown JSON fields, content types, dates and decimal
  representation deliberately?

<details><summary>Solutions 3</summary>

- 3.1 A current-line shape is:

  ```java
  @WebMvcTest(PaymentController.class)
  class PaymentControllerTest {
      @Autowired MockMvc mvc;
      @MockitoBean PaymentApplicationService service;

      @Test
      @WithMockUser(authorities = "PAYMENT_WRITE")
      void rejectsZeroAmount() throws Exception {
          mvc.perform(post("/payments")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
                      {"debtorAccountId":"00000000-0000-0000-0000-000000000001",
                       "creditorAccountId":"00000000-0000-0000-0000-000000000002",
                       "amount":0,"currency":"INR"}
                      """))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.code").value("REQUEST_INVALID"));

          verifyNoInteractions(service);
      }
  }
  ```

  Import the intended advice/security config as required. This test proves
  MVC deserialization, validation and error mapping without a real socket.
- 3.2 No. MockMvc enters Spring MVC in-process. Use a real server test for
  connector/TLS/low-level servlet-container error behavior.
- 3.3 Direct Java invocation bypasses MVC argument resolvers, validation and
  exception resolvers. Use `@WebMvcTest` + MockMvc/RestTestClient over MockMvc,
  then retain direct unit tests for controller-owned pure mapping if valuable.
- 3.4 Avoid stack text, internal class names, timestamps and incidental message
  wording. Assert stable status, machine code, field path and safe schema;
  verify that sensitive details are absent.
- 3.5 `@SpringBootTest(webEnvironment = RANDOM_PORT)` plus the current test
  client proves full context, actual server/filter chain, HTTP serialization
  and client/server thread separation. Dependencies remain fake/containerized
  according to configuration; it is not automatically production E2E.
- 3.6 They are compatibility/security choices. Lock the intended Jackson
  unknown-field policy, supported media types, UTC/offset handling and money
  scale/rounding so framework upgrades do not silently change the contract.

</details>

---

## 4. JPA Testcontainers and production database · 🎯 CORE PATH

- [ ] **4.1** 💭 Why can H2 give false confidence for PostgreSQL or Oracle?
- [ ] **4.2** 🛠 Wire a production-family database container through
  `@ServiceConnection`.
- [ ] **4.3** 🔮 Do service-connection details lose to
  `spring.datasource.url` in the test property file?
- [ ] **4.4** 💭 Slice with real DB versus full context with real DB: when each?
- [ ] **4.5** 🐛 The container test uses `hibernate.ddl-auto=create` while
  production uses Flyway. What is missing?
- [ ] **4.6** 💭 Static shared container versus one per test: trade-off and
  isolation requirement.
- [ ] **4.7** 🛠 Prove a pessimistic lock/unique constraint against the real
  engine.

<details><summary>Solutions 4</summary>

- 4.1 Dialect syntax, sequences/identity, collation, null ordering, JSON/time
  types, indexes, constraints, isolation and locking differ. H2 is useful for
  cheap tests, not proof of production-database semantics.
- 4.2 Boot can derive connection details:

  ```java
  @Testcontainers
  @DataJpaTest
  @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
  class PaymentRepositoryTest {
      @Container
      @ServiceConnection
      static PostgreSQLContainer<?> database =
              new PostgreSQLContainer<>("postgres:17");
  }
  ```

  Include the Boot Testcontainers test dependency/focused starter for the
  version. Explicit `replace = NONE` prevents lines/configurations that would
  otherwise substitute an embedded test database. For a generic image, the
  service name/type hint may be required.
- 4.3 Service connection details take precedence over ordinary connection
  properties. That is the point: auto-configuration consumes a typed
  `ConnectionDetails` from the container.
- 4.4 A JPA slice gives focused mapping/query/constraint feedback. A full
  context is justified when migrations, multiple data sources/managers,
  application converters/listeners or cross-layer transaction wiring are the
  risk.
- 4.5 The test proves Hibernate's generated schema, not deployability. Run the
  actual migrations from an empty database and representative upgrade
  baselines; validate mappings against the migrated schema.
- 4.6 A static/shared container amortizes startup and works well when every
  test resets or uniquely scopes data. Per-test containers maximize isolation
  but are expensive. Never trade speed for order-dependent dirty data.
- 4.7 Use separate transactions/connections, a short lock timeout and a barrier
  so contention is real. Assert the second operation blocks/times out or the
  unique insert loses as the contract specifies; H2 behavior is irrelevant.

</details>

---

## 5. Test-managed transaction traps · 🎯 CORE PATH

- [ ] **5.1** 🔮 A `@DataJpaTest` saves an invalid entity, never flushes, and
  passes. Can production commit still fail?
- [ ] **5.2** 🛠 Rewrite the assertion to prove a database constraint.
- [ ] **5.3** 💭 Test transaction, application transaction and physical DB
  transaction: distinguish them.
- [ ] **5.4** 🔮 A random-port test method is `@Transactional`. Will its rollback
  undo a server request's committed insert?
- [ ] **5.5** 🐛 A test queries the same entity manager after bulk DML and sees
  old state. Fix the verification.
- [ ] **5.6** 💭 How do `REQUIRES_NEW`, another data source and messaging escape
  ordinary test rollback?
- [ ] **5.7** 💭 When use `@Commit`/`@Rollback(false)`?

<details><summary>Solutions 5</summary>

- 5.1 Yes. ORM write-behind can defer SQL until flush/commit. Spring rolls the
  test-managed transaction back without a production-style commit, so a
  constraint or callback failure may never execute.
- 5.2 Force the unit of work to the database:

  ```java
  repository.save(invalidPayment);
  assertThatThrownBy(entityManager::flush)
          .isInstanceOf(PersistenceException.class);
  ```

  Clear before a later read when proving database-visible state. Prefer a
  precise Spring/provider exception where stable.
- 5.3 The TestContext framework opens a transaction around the test thread.
  application `REQUIRED` work on that thread may join it; the transaction
  manager maps it to a physical resource transaction. Different threads,
  managers or `REQUIRES_NEW` scopes may not share it.
- 5.4 No. The HTTP server handles the request on another thread with its own
  transaction and commits normally. Clean up explicitly, isolate schema/data
  or call a reset fixture API—not an untrusted production endpoint.
- 5.5 Bulk DML bypasses managed state. Flush compatible pending changes,
  execute bulk work, then `clear()`/`refresh()` or verify in a fresh context.
- 5.6 `REQUIRES_NEW` commits independently; another manager/resource is not
  controlled by the test transaction; a published message is external. Test
  cleanup must understand every durable boundary.
- 5.7 Rarely, when commit behavior itself—deferred constraint, trigger,
  after-commit callback—is the subject. Isolate and clean data explicitly.
  Do not make a whole suite commit merely to resemble production.

</details>

---

## 6. Security and external dependency contracts · 🎯 CORE PATH

- [ ] **6.1** 🛠 Test unauthenticated, insufficient-authority and authorized
  requests. Which return 401 and 403?
- [ ] **6.2** 💭 `@WithMockUser` versus `jwt()` request post-processor: what does
  each prove and not prove?
- [ ] **6.3** 🛠 Test valid and invalid CSRF for a cookie-authenticated write.
- [ ] **6.4** 🐛 All controller tests disable filters. What security regression
  can ship?
- [ ] **6.5** 🛠 Stub a dependency that delays beyond the read timeout and then
  returns 503. What must be asserted beyond the final response?
- [ ] **6.6** 💭 Java-client mock versus HTTP stub: what boundary differs?
- [ ] **6.7** 🏭 The stub accepts a payment then drops the response. Design the
  ambiguity/idempotency test.

<details><summary>Solutions 6</summary>

- 6.1 Missing/invalid authentication is 401 through the entry point; a valid
  identity denied authority is 403 through the access-denied handler; allowed
  identity reaches MVC. Also test wrong tenant/account at the method/query
  boundary. See the [Security kit](spring-security-basics.md) for the complete
  matrix.

  ```java
  mvc.perform(get("/payments/{id}", id))
      .andExpect(status().isUnauthorized());

  mvc.perform(get("/payments/{id}", id)
          .with(jwt().authorities(new SimpleGrantedAuthority("OTHER"))))
      .andExpect(status().isForbidden());
  ```

- 6.2 `@WithMockUser` populates a username/password-style mocked
  `Authentication` for method/MVC authorization and needs no real user. `jwt()`
  creates resource-server JWT authentication with chosen claims/authorities.
  Neither verifies a real signature, issuer/audience decoder or authorization
  server; retain a controlled decoder/issuer integration test.
- 6.3 With Spring Security MockMvc support:

  ```java
  mvc.perform(post("/profile").with(csrf()))
      .andExpect(status().isNoContent());
  mvc.perform(post("/profile").with(csrf().useInvalidToken()))
      .andExpect(status().isForbidden());
  ```

  A stateless bearer API that intentionally disables CSRF should test that
  chain instead of adding tokens mechanically.
- 6.4 Matcher ordering, default-deny gaps, CSRF/CORS, filter exception shape,
  header policy and authentication setup can break while all tests pass.
  Keep focused unsecured controller tests only where useful and a security-
  enabled matrix for every protected route family.
- 6.5 Use WireMock/MockWebServer or equivalent to control actual network
  behavior. Assert client timeout, total deadline, attempt count/timing,
  idempotency header reuse, breaker/metric classification and safe final error;
  also verify no retry for permanent 4xx.

  ```java
  stubFor(post(urlEqualTo("/switch/debits"))
          .withHeader("Idempotency-Key", equalTo(operationKey))
          .willReturn(aResponse()
                  .withFixedDelay(800)
                  .withStatus(503)));

  assertThatThrownBy(() -> client.debit(command))
          .isInstanceOf(SwitchUnavailableException.class);
  verify(2, postRequestedFor(urlEqualTo("/switch/debits"))
          .withHeader("Idempotency-Key", equalTo(operationKey)));
  ```

  The configured read timeout/backoff/deadline must make two attempts a valid
  expectation; measure elapsed time with a tolerant bound, not an exact
  millisecond assertion.
- 6.6 A Java mock proves code branching at the client interface. An HTTP stub
  proves URI, method, headers/signature, serialization, status mapping,
  connection/read timeouts and retry behavior. TLS/DNS/proxy behavior may need
  an even higher environment.
- 6.7 Configure the stub to record/commit the first idempotency key and close or
  delay its response. The service must not send a new business reference;
  retry/status enquiry reuses the stable key. Assert one remote effect and a
  modeled confirmed/pending/unknown local outcome.

</details>

---

## 7. Statement counts N+1 and realistic cardinality · 🎯 CORE PATH

- [ ] **7.1** 💭 Why can a one-parent fixture never prove absence of N+1?
- [ ] **7.2** 🛠 Design a repository query-budget regression test.
- [ ] **7.3** 🔮 Does “one SQL statement” prove the query is efficient?
- [ ] **7.4** 🐛 A test reads associations before resetting Hibernate
  statistics. Why is the count meaningless?
- [ ] **7.5** 💭 What should remain stable in an assertion across Hibernate
  upgrades?
- [ ] **7.6** 🛠 Test pagination with duplicate sort keys and concurrent-like
  inserts.

<details><summary>Solutions 7</summary>

- 7.1 N+1 growth appears when association access repeats per parent. Use 10–20
  parents with distinct children/rails, clear the persistence context, execute
  the real mapper/serializer use case and consume all results.
- 7.2 Enable isolated Hibernate statistics or a datasource query-count tool,
  prepare data, flush/clear, reset the counter, execute, then assert result and
  a bounded statement count:

  ```java
  statistics.clear();
  List<PaymentSummary> rows = service.listPending();
  assertThat(rows).hasSize(20);
  assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
  ```

  Global statistics require test isolation from concurrent database activity.
- 7.3 No. It may return a Cartesian explosion, scan millions of rows, sort to
  disk or transfer wide unused columns. Inspect row/cardinality and the real
  database execution plan/latency as risk warrants.
- 7.4 Fixture setup/lazy access has already incremented the shared counter.
  Flush and clear setup, reset immediately before the operation, avoid
  unrelated parallel work and force all lazy/result consumption before read.
- 7.5 Assert the use-case query budget and result, not exact whitespace/aliases
  of generated SQL. Provider changes can alter harmless SQL while preserving
  the bounded round-trip contract.
- 7.6 Insert multiple rows with the same timestamp and order by timestamp plus
  unique id. Page/seek across the boundary and assert no gaps/duplicates.
  For keyset pagination, insert a newer row between reads and confirm the
  cursor contract remains coherent.

</details>

---

## 8. Concurrency deadlock and idempotency tests · 🎯 CORE PATH

- [ ] **8.1** 🛠 Prove an optimistic conflict with two real transactions.
- [ ] **8.2** 🐛 Two threads share one `EntityManager` and sometimes fail.
  Identify the invalid setup.
- [ ] **8.3** 💭 How does a barrier make a concurrency test deterministic?
- [ ] **8.4** 🛠 Test an atomic insufficient-balance update under contention.
- [ ] **8.5** 🛠 Test same-key duplicate payment requests sequentially and
  concurrently.
- [ ] **8.6** 💭 How do you test deadlock retry without accepting flakiness?
- [ ] **8.7** 🔮 If both concurrent HTTP responses are 200, can two debits still
  exist?

<details><summary>Solutions 8</summary>

- 8.1 Use two executor tasks, each entering its own `TransactionTemplate`/
  connection. Both load version N, signal a barrier, then update and flush.
  Assert one commit and one optimistic-lock failure; read in a fresh
  transaction and verify the domain invariant.
- 8.2 `EntityManager`/persistence context is not thread-safe. Inject it per
  Spring transaction in each thread; do not capture/share one test instance.
- 8.3 It forces both operations past the read/claim point before either writes,
  creating the intended overlap instead of hoping scheduler timing exposes it.
  Give awaits a timeout so a defect fails rather than hangs CI.
- 8.4 Start two transactions against the same account with total requested
  amount greater than balance. Execute the conditional update or lock design.
  Assert successes match available funds, balance never goes negative and
  ledger count/value equals committed effects.
- 8.5 Sequential: same key/payload returns the same stable outcome; same key/
  different fingerprint conflicts. Concurrent: release N requests together,
  then assert exactly one idempotency claim, payment and ledger effect and
  compatible responses/pending contract.
- 8.6 Against the production engine, deliberately acquire two rows in opposite
  order with barriers and short lock timeouts. Assert one victim, bounded
  whole-operation retry and final state. Separately fix production lock order;
  the test is proof of recovery, not permission to preserve the deadlock.
- 8.7 Yes. Response equality is not a durable invariant. Query the database/
  ledger and remote stub effect count after all requests finish.

</details>

---

## 9. Contracts fixtures isolation and parallel safety · 🎯 CORE PATH

- [ ] **9.1** 💭 Consumer-driven contract versus integration/E2E test: what
  does each prove?
- [ ] **9.2** 🛠 Use `@Sql` or a fixture builder without creating a giant shared
  data universe.
- [ ] **9.3** 🐛 Tests pass alone but fail in parallel because they set system
  properties and use the same account. Fix ownership.
- [ ] **9.4** 💭 Why should tests control clock, time zone, locale and random?
- [ ] **9.5** 💭 How should database cleanup work for random-port committed
  tests?
- [ ] **9.6** 🔮 Does repeating a flaky test three times make the suite safer?
- [ ] **9.7** 🛠 Validate an event schema's backward compatibility and semantic
  idempotency key.

<details><summary>Solutions 9</summary>

- 9.1 A contract test proves an independently deployable consumer/provider
  agrees on request/event shape and examples. Integration proves this
  implementation's adapters and infrastructure. E2E proves a small set of
  deployed paths/routing. None alone proves latency or every business rule.
- 9.2 Keep setup local and readable:

  ```java
  @Sql(scripts = "/sql/payment-list-fixture.sql",
       executionPhase = BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/payment-cleanup.sql",
       executionPhase = AFTER_TEST_METHOD)
  class PaymentQueryTest { }
  ```

  Builders should state only relevant differences. Avoid one global fixture
  whose mutation/order makes every test depend on every other test.
- 9.3 System properties/static stubs are process-global and the row is shared.
  Prefer context-scoped properties, instance-bound stubs and unique tenant/
  business keys; serialize only tests that truly own an unavoidable global
  resource and document why.
- 9.4 Date boundaries, money formatting, scheduled expiry and generated ids
  otherwise vary by machine/time. Inject `Clock`, set explicit zone/locale at
  the boundary and seed controlled randomness; never repair timing with sleep.
- 9.5 Use isolated database/schema per suite, deterministic truncation after
  the server call, or unique test-run tenants and later cleanup. Test-managed
  rollback cannot reach the server transaction. Cleanup must be safe on test
  failure and must never point at a non-test database.
- 9.6 No. Retry hides race/order/resource leakage and spends CI time. Quarantine
  only with ownership and deadline while diagnosing synchronization, global
  state, data isolation, clock or resource exhaustion.
- 9.7 Validate old consumer fixtures against the new schema, required/default
  fields and enum evolution. Assert stable `eventId`/business key survives
  redelivery and that a consumer transaction deduplicates it. Shape
  compatibility without replay semantics is incomplete.

</details>

---

## 10. Senior production scenarios · 🎯 CORE PATH

- [ ] **10.1** 🏭 A repository test passes on H2 but fails on Oracle with a
  locking/query error.
- [ ] **10.2** 🏭 Full test suite takes 40 minutes and fails when contexts exceed
  memory.
- [ ] **10.3** 🏭 Random-port test leaves data despite `@Transactional`.
- [ ] **10.4** 🏭 Two duplicate payment requests occasionally create two
  ledger rows.
- [ ] **10.5** 🏭 Security tests all pass, but production `/admin` is public
  after matcher reorder.
- [ ] **10.6** 🏭 Retry test returns success but actually calls the partner nine
  times.
- [ ] **10.7** 🏭 Migration succeeds from empty schema but fails upgrading a
  two-year-old production schema.

<details><summary>Solutions 10</summary>

- 10.1 Reproduce with a Testcontainer/vendor test database and real migration;
  inspect dialect SQL and lock capability. Keep H2 only for risks it truthfully
  models, and add the production-family regression.
- 10.2 Replace unnecessary full contexts with plain tests/slices, standardize
  context configurations so caching works, remove casual `@DirtiesContext`,
  share expensive containers safely, and profile test/context startup. Keep a
  smaller full-context wiring suite.
- 10.3 The server used another thread/transaction and committed. Use explicit
  cleanup/isolation and assert the committed state; do not rely on the client
  test's rollback.
- 10.4 Make the idempotency claim a database unique constraint acquired in the
  same transaction as the effect. Add a barrier-driven concurrent integration
  test and assert one durable payment/ledger/outbox—not only responses.
- 10.5 Some tests disabled filters or imported a different chain. Add a
  security-enabled route matrix including unknown/default routes and matcher
  order; assert 401/403/allowed paths and inspect the actual chain.
- 10.6 Multiple retry layers composed. At the HTTP stub count attempts and
  total duration, inspect gateway/client/framework policies, assign one retry
  owner and test aspect/order plus idempotency.
- 10.7 Empty-schema tests do not prove upgrade history. Restore representative
  old schemas/data, run every migration in order, validate constraints/index
  build time and backward-compatible rolling deployment behavior.

</details>

---

## 11. Rapid-fire trap wall 🔮 · all keep

- [ ] **11.1** Does `@SpringBootTest` always start a server?
- [ ] **11.2** Does `@WebMvcTest` load service/repository beans normally?
- [ ] **11.3** Is `@MockitoBean` the same as plain `@Mock`?
- [ ] **11.4** Does test rollback prove commit-time constraints?
- [ ] **11.5** Will a random-port request join the test method transaction?
- [ ] **11.6** Is `EntityManager` thread-safe?
- [ ] **11.7** Does H2 prove Oracle/PostgreSQL locking?
- [ ] **11.8** Do `@WithMockUser` and `jwt()` verify token signatures?
- [ ] **11.9** Does one fixture row expose N+1?
- [ ] **11.10** Does one SQL query prove good performance?
- [ ] **11.11** Can exact generated SQL text be a stable regression contract?
- [ ] **11.12** Is `Thread.sleep` a reliable concurrency barrier?
- [ ] **11.13** Does the same idempotency response prove one durable effect?
- [ ] **11.14** Can `REQUIRES_NEW` data be cleaned by outer test rollback?
- [ ] **11.15** Is an HTTP stub equivalent to a Java mock?
- [ ] **11.16** Does a contract test prove provider performance?
- [ ] **11.17** Does `@DirtiesContext` make tests faster or safer by default?
- [ ] **11.18** Is retrying flaky tests a root-cause fix?

<details><summary>Solutions 11</summary>

- 11.1 **No. Default is a mock web environment; request a real port.**
- 11.2 **No. Supply/import focused collaborators deliberately.**
- 11.3 **No. It overrides a Spring context bean; `@Mock` is test-local.**
- 11.4 **No. Flush/commit behavior must be exercised explicitly.**
- 11.5 **No. The server handles it on another thread/transaction.**
- 11.6 **No. Use a separate context per transactional thread.**
- 11.7 **No. Test the production database family.**
- 11.8 **No. They create test authentication; test decoder/issuer separately.**
- 11.9 **No. Use multiple parents with distinct associations.**
- 11.10 **No. It can scan/explode/transfer an enormous result.**
- 11.11 **Usually no. Assert bounded statements/result and relevant plan.**
- 11.12 **No. Use barriers/latches with timeouts and observable conditions.**
- 11.13 **No. Assert database/ledger/remote side-effect count.**
- 11.14 **No. It commits independently and requires explicit cleanup.**
- 11.15 **No. The stub crosses serialization/network-client behavior.**
- 11.16 **No. It proves agreed shape/examples, not latency/capacity.**
- 11.17 **No. It evicts context cache; use only for true context mutation.**
- 11.18 **No. Diagnose shared state, timing and resource isolation.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Test strategy? | Use many plain domain tests, focused MVC/JPA/client slices, fewer full-context tests and targeted real-server/concurrency/fault tests; each level must prove a new boundary. |
| `@WebMvcTest`? | Focused MVC/controller infrastructure with MockMvc; replace service ports, import intended advice/security, and assert serialization, validation, status and safe error shape. |
| `@DataJpaTest`? | Focused transactional JPA/repository slice. Use the production DB when dialect/locks/plans matter and flush/clear to avoid false positives. |
| `@SpringBootTest`? | Loads the application context; it starts a real server only with the relevant web environment and still needs realistic dependencies. |
| Testcontainers? | Disposable real infrastructure wired through typed service connections; run production migrations and isolate/reset data deterministically. |
| Transaction trap? | Test-managed rollback can skip flush/commit failures; random-port work and `REQUIRES_NEW` commit outside it. Assert in a fresh context and clean explicitly. |
| Security tests? | Cover 401, 403, allowed, cross-tenant, CSRF/CORS and JWT claim mapping; mocked authentication does not prove signature/issuer validation. |
| Dependency stub? | An HTTP stub proves wire shape, status mapping, delay/reset/timeouts, retry count and idempotency behavior that a Java mock cannot. |
| N+1 test? | Multiple-parent fixture, flush/clear/reset count, execute the real mapping path, consume results and assert a bounded statement budget. |
| Concurrency/idempotency? | Separate transactions plus barriers; assert one durable effect and invariant under optimistic conflict, atomic update, deadlock retry and concurrent duplicate keys. |

---

## Primary references

- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/)
- [Testing Spring Boot applications and slices](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [Spring Boot test modules](https://docs.spring.io/spring-boot/reference/testing/test-modules.html)
- [Spring Boot Testcontainers and service connections](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Spring Framework TestContext](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework.html)
- [Spring test-managed transactions](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html)
- [Spring bean override and `@MockitoBean`](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/bean-overriding.html)
- [Spring Security testing](https://docs.spring.io/spring-security/reference/servlet/test/)
- [Testcontainers documentation](https://java.testcontainers.org/)
- [WireMock documentation](https://wiremock.org/docs/)
- [JUnit 5 user guide](https://junit.org/junit5/docs/current/user-guide/)

---

## Extensions — after the senior core

- Spring Cloud Contract/Pact provider verification and broker workflows.
- REST Docs/OpenAPI contract generation from tests.
- Mutation testing and property-based/generative tests.
- Toxiproxy/network fault injection, chaos and steady-state hypotheses.
- Load, soak and capacity tests with coordinated-omission awareness.
- Containerized Kafka/Redis/object-store integration and event-contract tests.
- Native-image/AOT tests and platform deployment smoke tests.
- Database migration rollback/expand-contract and backup/restore drills.

---

## How to drill this kit

1. For ten production risks, name the smallest truthful test scope in one line.
2. Build one controller slice, one real-database repository slice and one
   random-port path; state exactly what each does not prove.
3. Reproduce the deferred-flush and random-port rollback traps.
4. Write barrier-driven optimistic-lock and concurrent-idempotency tests.
5. Stub timeout-after-acceptance and prove one remote business effect.
6. Re-run §§4, 5, 6, 7, 8, 10 and 11 blind after a week.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 portfolio/risk | ⬜ | ⬜ | ⬜ |
| §1 plain unit/design | ⬜ | ⬜ | ⬜ |
| §2 scope selection | ⬜ | ⬜ | ⬜ |
| §3 MVC/API | ⬜ | ⬜ | ⬜ |
| §4 real database/Testcontainers | ⬜ | ⬜ | ⬜ |
| §5 transaction traps | ⬜ | ⬜ | ⬜ |
| §6 security/dependency stubs | ⬜ | ⬜ | ⬜ |
| §7 query budgets/N+1 | ⬜ | ⬜ | ⬜ |
| §8 concurrency/idempotency | ⬜ | ⬜ | ⬜ |
| §9 contracts/isolation | ⬜ | ⬜ | ⬜ |
| §10 production scenarios | ⬜ | ⬜ | ⬜ |
| §11 trap wall | ⬜ | ⬜ | ⬜ |
