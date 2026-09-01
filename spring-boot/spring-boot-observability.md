# Spring Boot Observability & Diagnosis — Senior Anti-Fumble Kit

For a backend engineer who can read logs but must prove **where time went**,
**which resource saturated**, and **whether customers are receiving the right
outcome**. The goal is not to emit maximum telemetry. It is to create bounded,
safe evidence that leads from an alert to a defensible diagnosis.

**Readable reference:** [Spring Senior Backend Reference](spring-senior-core.md),
chapter 16.

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/configure · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Attempt every prompt aloud before opening its `<details>` solution. The
answer card is for rehearsal after the exercises, not a replacement for
retrieval practice.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §8 → §10 → §11**
>
> Evidence model · safe structured logs · meters/cardinality · SLOs ·
> observations/traces · Actuator · probes · diagnosis · dashboards/alerts ·
> production scenarios · traps.
>
> A senior observability answer names **symptom, scope, time decomposition,
> saturated resource, correlated evidence, and verification**. “Check the
> logs” is not a diagnostic method.

> **Version line.** Spring Boot 3 replaced the old Spring Cloud Sleuth path
> with **Micrometer Observation + Micrometer Tracing**. Boot 4 modularized
> metrics/tracing support and their test modules; use the focused starter for
> the project line. Current Boot can emit ECS, GELF or Logstash structured JSON
> through `logging.structured.format.*`. The concepts and meter names are more
> stable than packages and exporter properties, so verify exact dependencies
> against the running Boot minor.

---

## 0. The evidence path · 🎯 CORE PATH

```text
customer symptom / SLO burn
          |
          v
metrics: scope and magnitude
rate · errors · latency distribution · saturation
          |
          v
trace: allocate one request's time
filter -> queue -> pool wait -> SQL/lock -> downstream
          |
          v
logs: explain the decisive event
safe identifiers · state transition · error cause
          |
          v
hypothesis -> mitigation -> verify with the same signals

business correctness remains separate:
stuck payments · outbox age · reconciliation mismatch · duplicate effects
```

- [ ] **0.1** 💭 Distinguish monitoring, telemetry and observability.
- [ ] **0.2** 💭 What does each of logs, metrics and traces answer best?
- [ ] **0.3** 🔮 CPU is 25%, but p99 latency is four seconds. Does low CPU
  rule out saturation?
- [ ] **0.4** 💭 Why can every health check be green while a payment service
  is functionally broken?
- [ ] **0.5** 💭 Give the investigation order for a new latency alert.

<details><summary>Solutions 0</summary>

- 0.1 **Telemetry** is emitted data. **Monitoring** evaluates known signals
  and alerts on expected failure modes. **Observability** is the ability to
  infer internal state and investigate novel behavior from external evidence.
  More telemetry can reduce observability if it is unsafe, unbounded or noisy.
- 0.2 Metrics cheaply establish aggregate rate, distribution and saturation.
  Traces connect and allocate one distributed operation's time. Logs explain
  discrete events with rich context. Use them together rather than forcing
  every question into one signal.
- 0.3 No. Threads can be blocked on an executor queue, JDBC connection pool,
  database lock or downstream socket while consuming little CPU. Inspect wait
  time and active/max/queued state at every finite resource.
- 0.4 Health commonly proves process/dependency reachability, not business
  correctness. The API can answer 200 while events remain unpublished,
  payments age in `PENDING`, or reconciliation diverges. Add business SLIs.
- 0.5 Confirm the customer-visible symptom and time window; scope by endpoint,
  region/version/outcome; inspect golden signals and resource saturation;
  use traces to allocate time; inspect relevant logs/SQL/locks; correlate with
  changes; mitigate; verify recovery with the same SLI and guard against
  recurrence.

</details>

---

## 1. Structured logs correlation and redaction · 🎯 CORE PATH

- [ ] **1.1** 💭 Why is a stable `event` field better than parsing free-form
  messages?
- [ ] **1.2** 🛠 Design a structured payment-state log. Which fields are safe,
  and which must never appear?
- [ ] **1.3** 🐛 A filter copies any incoming `X-Correlation-ID` into MDC and
  never clears it. Name both failures and fix the lifecycle.
- [ ] **1.4** 💭 Trace ID versus business correlation/reference: why keep both?
- [ ] **1.5** 🔮 Does hashing a PAN, email or short account number make it safe
  to log and unrestricted to retain?
- [ ] **1.6** 💭 Where should an unexpected exception be logged?
- [ ] **1.7** 🛠 Enable Boot's native structured console format and explain
  what custom Logback configuration must preserve.

<details><summary>Solutions 1</summary>

- 1.1 A stable event name and typed fields can be indexed, aggregated and
  alerted without depending on sentence wording. Keep the human message, but
  make queries use fields such as `event`, `outcome`, `rail` and `durationMs`.
- 1.2 A safe event records the minimum evidence:

  ```json
  {
    "event": "payment_state_changed",
    "paymentId": "safe-internal-id",
    "fromState": "PENDING",
    "toState": "SENT",
    "rail": "IMPS",
    "outcome": "SUCCESS",
    "traceId": "a91...",
    "durationMs": 42
  }
  ```

  Never log passwords, bearer/session tokens, PIN/CVV, private keys, full
  account/card numbers, raw identity documents or complete payloads. Apply
  field allowlists and centralized redaction; a developer remembering not to
  concatenate a secret is not a control.
- 1.3 The header is untrusted: it can be huge, contain line breaks or forge
  another request's id. Validate character set/length or generate a server id.
  MDC is usually thread-local; without `finally` cleanup a reused server thread
  can attach the previous caller's identity to later logs.

  ```java
  class CorrelationFilter extends OncePerRequestFilter {
      protected void doFilterInternal(HttpServletRequest request,
              HttpServletResponse response, FilterChain chain)
              throws IOException, ServletException {
          String id = valid(request.getHeader("X-Correlation-ID"))
                  ? request.getHeader("X-Correlation-ID")
                  : UUID.randomUUID().toString();
          try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", id)) {
              response.setHeader("X-Correlation-ID", id);
              chain.doFilter(request, response);
          }
      }
  }
  ```

  Prefer tracing's managed trace/span correlation where it meets the contract;
  retain this pattern for a separate external correlation id.
- 1.4 Trace/span IDs are technical and can change across sampling or async
  processing. A safe business reference survives retries and reconciliation.
  Neither is authorization evidence, and neither should expose sensitive data.
- 1.5 No. Low-entropy values can be dictionary-guessed, and pseudonymized data
  can remain regulated personal data. Follow the data classification,
  purpose, access and retention policy; prefer omission or tokenization.
- 1.6 Log it once at the boundary that owns handling, with the exception cause
  and safe context. Lower layers should translate or enrich without printing
  the same stack repeatedly. Expected business rejections generally need
  counters/audit outcomes, not ERROR stacks.
- 1.7 Current Boot supports, for example:

  ```yaml
  logging:
    structured:
      format:
        console: logstash
  ```

  Custom Logback/Log4j2 configuration must use Boot's structured-format
  system property/encoder contract; replacing the encoder blindly can disable
  the feature. Choose ECS/GELF/Logstash to match the ingestion platform.

</details>

---

## 2. Micrometer meters and cardinality · 🎯 CORE PATH

- [ ] **2.1** 💭 Choose a counter, gauge, timer, distribution summary or
  long-task timer for: accepted payments, queue depth, switch latency, batch
  size and active reconciliation job.
- [ ] **2.2** 🔮 A counter reads `1,900,000`. Is the service healthy?
- [ ] **2.3** 🛠 Instrument payment outcome and switch latency with bounded
  tags.
- [ ] **2.4** 🐛 `paymentId` and raw `/payments/94fd...` are tags. What happens
  in the application and metrics backend?
- [ ] **2.5** 💭 Why should gauges describe sampled state rather than events?
- [ ] **2.6** 💭 Registry instance versus static `Metrics`: which should
  Spring application code use?
- [ ] **2.7** 🛠 Add a defensive tag-cardinality limit. Is it the primary fix?

<details><summary>Solutions 2</summary>

- 2.1 Counter for accepted total (graph the rate); gauge for current queue
  depth; timer for latency/count; distribution summary for batch size;
  long-task timer for work that remains in progress long enough to observe.
- 2.2 No. A cumulative total lacks interval, traffic, outcome and expectation.
  Graph a rate and success/error proportions, then compare latency and
  saturation against an SLO/baseline.
- 2.3 Keep the outcome and rail domains bounded:

  ```java
  @Component
  class PaymentTelemetry {
      private final MeterRegistry registry;

      PaymentTelemetry(MeterRegistry registry) {
          this.registry = registry;
      }

      void recordOutcome(String rail, String outcome) {
          registry.counter("payments.completed",
                  "rail", rail,
                  "outcome", outcome).increment();
      }

      <T> T timeSwitch(String rail, Supplier<T> call) {
          return registry.timer("payments.switch.duration", "rail", rail)
                  .record(call);
      }
  }
  ```

  Normalize rail/outcome through enums or allowlists. Creating meter builders
  is also useful when descriptions, base units or histograms are configured.
- 2.4 Every unique tag combination creates another meter/time series. Heap,
  scrape payload, storage and query cost can grow without bound. Use a URI
  template and bounded dimensions; put a safe identifier on sampled traces or
  controlled logs instead.
- 2.5 A gauge is sampled and intermediate changes can disappear. It is right
  for "queue has 17 items now," wrong for "17 payments happened." Also retain
  a strong reference to the observed state/object where the registry API
  requires it; otherwise the gauge can disappear or return no value.
- 2.6 Inject the Spring-managed `MeterRegistry`. Static `Metrics` uses the
  global registry, can bypass Boot customizers/exporters and creates confusing
  tests.
- 2.7 A last-resort guard can deny excessive values:

  ```java
  @Bean
  MeterFilter paymentTagLimit() {
      return MeterFilter.maximumAllowableTags(
              "payments.completed", "rail", 12,
              MeterFilter.deny());
  }
  ```

  The primary fix is a bounded semantic tag model. A limiter prevents damage
  but can silently discard evidence if the model remains wrong; alert on the
  guard activating.

</details>

---

## 3. Distributions golden signals and SLOs · 🎯 CORE PATH

- [ ] **3.1** 💭 Why are average and maximum latency insufficient?
- [ ] **3.2** 💭 Client-side percentile versus histogram buckets: which can be
  aggregated across replicas?
- [ ] **3.3** 🛠 Define an SLI/SLO for payment-status reads. Name eligible
  traffic, success, latency threshold and window.
- [ ] **3.4** 💭 Map latency, traffic, errors and saturation to concrete Spring
  service signals.
- [ ] **3.5** 🔮 Success latency is low because every timeout is excluded.
  Is the dashboard honest?
- [ ] **3.6** 💭 Why alert on error-budget burn rather than one raw threshold?

<details><summary>Solutions 3</summary>

- 3.1 The average hides a suffering tail; max is dominated by one outlier and
  sample interval. Inspect a distribution—p50/p95/p99 and SLO bucket counts—
  split by outcome and bounded operation dimensions.
- 3.2 Histogram bucket counts can be summed across instances and used to
  approximate fleet percentiles. Percentiles precomputed in each process are
  generally not aggregatable. Configure expected ranges/SLO buckets to control
  cost and usefulness.

  ```yaml
  management:
    metrics:
      distribution:
        percentiles-histogram:
          http.server.requests: true
        slo:
          http.server.requests: 100ms,250ms,500ms,1s
  ```

- 3.3 Example: "Over a rolling 28 days, 99.9% of authenticated, syntactically
  valid payment-status reads reaching this service return the contractually
  successful response within 400 ms." Define how 404/business-not-ready,
  client cancellation, probes and load tests enter the denominator.
- 3.4 Latency: server/downstream/DB timers and pool wait. Traffic: request,
  message and attempt rate. Errors: HTTP/server result and business outcome.
  Saturation: active/max threads/connections, queue age/depth, rejects, lock
  wait, CPU/GC. Retry attempt rate belongs beside original traffic.
- 3.5 No. A timeout is a customer-visible slow failure and must remain visible
  in outcome and total-duration/error SLIs. Maintain separate successful
  latency when useful, but never let it hide failed tail traffic.
- 3.6 Error budget connects reliability to a promised window and traffic
  volume. Multi-window burn alerts distinguish a sharp incident from a slow
  leak and avoid paging on one harmless error or a naturally rising counter.

</details>

---

## 4. Observations tracing and context propagation · 🎯 CORE PATH

- [ ] **4.1** 💭 Observation, meter, span and trace: relate them.
- [ ] **4.2** 🛠 Create a custom observation for fraud evaluation with one
  low-cardinality and one high-cardinality value.
- [ ] **4.3** 🔮 Will trace propagation work if code calls
  `RestClient.create()` instead of the auto-configured builder?
- [ ] **4.4** 🐛 Trace IDs disappear inside a custom executor. Why, and what
  are the safe fixes?
- [ ] **4.5** 💭 Should `paymentId` be low-cardinality, high-cardinality or
  absent? Does “high-cardinality” remove privacy obligations?
- [ ] **4.6** 💭 How should an HTTP producer and Kafka consumer relate traces?
- [ ] **4.7** 💭 What does sampling change about the role of metrics?

<details><summary>Solutions 4</summary>

- 4.1 `Observation` is one instrumentation lifecycle with context and handlers.
  A meter handler can produce a timer/counter; a tracing handler can produce a
  span. Spans form a trace across process boundaries. The registry and
  conventions determine naming/tags rather than business code knowing every
  exporter.
- 4.2 Use a bounded rail as a metric tag and keep the request id trace-only:

  ```java
  RiskDecision evaluate(String rail, String safeRequestId,
          Supplier<RiskDecision> call) {
      return Observation.createNotStarted("risk.evaluate", observations)
              .lowCardinalityKeyValue("rail", rail)
              .highCardinalityKeyValue("risk.request.id", safeRequestId)
              .observe(call);
  }
  ```

  An `ObservationConvention` is preferable when a shared library needs a
  stable documented semantic contract.
- 4.3 Not automatically. Boot adds tracing interceptors/customization through
  its managed `RestClient.Builder`, `RestTemplateBuilder` and
  `WebClient.Builder`. Manually constructed clients can omit propagation and
  client observations.
- 4.4 Trace/MDC context is scoped, commonly through thread-local state. A raw
  executor changes threads. Use framework-instrumented executors/task
  decorators or Micrometer Context Propagation, or pass explicit business
  context. Always restore/clear around pooled threads. Do not copy every
  `ThreadLocal` blindly.
- 4.5 A safe payment id may be a high-cardinality trace/log field if it is
  essential and policy permits; never a metric tag. High-cardinality is a
  telemetry classification, not consent, encryption or a retention waiver.
- 4.6 Inject standard trace context in record headers using supported
  instrumentation and keep a separate business/event id in the schema.
  Consumer processing can be a linked/new span after async delay/redelivery;
  one infinitely long synchronous-looking trace is not required.
- 4.7 Sampling means many requests have no stored detailed trace. Metrics must
  still describe the complete aggregate population and trigger detection;
  exemplars can connect a histogram bucket to a sampled trace when supported.

</details>

---

## 5. Actuator exposure custom signals and security · 🎯 CORE PATH

- [ ] **5.1** 💭 What does Actuator add, and which endpoints should be exposed
  publicly?
- [ ] **5.2** 🐛 `management.endpoints.web.exposure.include=*` is reachable on
  the public application port. Name the risks.
- [ ] **5.3** 🛠 Configure a narrow endpoint exposure and show health details
  only to authorized operators.
- [ ] **5.4** 💭 Why are `/env`, `/configprops`, `/heapdump`, `/threaddump`,
  `/mappings` and writable `/loggers` sensitive?
- [ ] **5.5** 🛠 Add a custom observation rather than a one-off timer and say
  when a custom health indicator is justified.
- [ ] **5.6** 🔮 Does exposing `/actuator/metrics` automatically configure a
  Prometheus scrape/export registry?
- [ ] **5.7** 🛠 Implement a cheap cached-state health indicator for an outbox
  publisher; why must it not query the outbox table on every probe?

<details><summary>Solutions 5</summary>

- 5.1 Actuator supplies operational endpoints and production integration for
  health, metrics, logging, conditions and more. Public exposure should
  usually be only the minimum probe endpoint required by the platform. Put
  sensitive management access behind network and Spring Security controls.
- 5.2 It reveals configuration/architecture/resource data and may allow
  runtime changes. Heap/thread dumps can contain secrets and customer data;
  loggers can enable sensitive verbose output. A separate port alone is not
  security if its network is reachable.
- 5.3 Example direction—the exact authority chain remains application-owned:

  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,prometheus
    endpoint:
      health:
        show-details: when_authorized
        roles: OPS
  ```

  Configure a dedicated security chain and network policy. Sanitize values and
  audit runtime changes.
- 5.4 They reveal properties, bean/routes, stack/heap state or allow mutation.
  Environment sanitization is defense in depth, not permission to publish the
  endpoint. Treat dump files as sensitive artifacts.
- 5.5 `Observation` can feed timer and trace handlers consistently:

  ```java
  Observation.createNotStarted("outbox.publish", observationRegistry)
          .lowCardinalityKeyValue("event.type", eventType)
          .observe(publisher::publish);
  ```

  A custom health indicator is justified when the instance can cheaply and
  reliably determine a state that changes routing/operator action. Do not run
  an expensive business query per probe or duplicate ordinary latency metrics.
- 5.6 No. The endpoint inspects registered meters. Export requires the
  appropriate registry dependency/configuration and the scrape endpoint must
  be exposed and secured/reachable appropriately.
- 5.7 Observe normal publisher work and let the probe read only cached state:

  ```java
  @Component("outboxPublisher")
  class OutboxPublisherHealth implements HealthIndicator {
      private final AtomicReference<Instant> lastSuccessfulCycle =
              new AtomicReference<>(Instant.EPOCH);
      private final Clock clock;

      OutboxPublisherHealth(Clock clock) {
          this.clock = clock;
      }

      void pollCycleSucceeded() {
          lastSuccessfulCycle.set(clock.instant());
      }

      @Override
      public Health health() {
          Duration age = Duration.between(lastSuccessfulCycle.get(), clock.instant());
          return age.compareTo(Duration.ofMinutes(2)) <= 0
                  ? Health.up().withDetail("lastCycleAgeSeconds", age.toSeconds()).build()
                  : Health.down().withDetail("lastCycleAgeSeconds", age.toSeconds()).build();
      }
  }
  ```

  Decide whether this belongs in readiness or only an operator health group;
  include it explicitly in that group. Querying the table/dependency on every
  platform probe amplifies an outage and makes the probe slow. Also expose
  outbox count/oldest age as metrics—the binary health result loses detail.

</details>

---

## 6. Liveness readiness and dependency health · 🎯 CORE PATH

- [ ] **6.1** 💭 Define liveness and readiness as platform decisions.
- [ ] **6.2** 🔮 The database is added to liveness. It fails for two minutes.
  What fleet behavior can follow?
- [ ] **6.3** 💭 Should every remote dependency be in readiness?
- [ ] **6.4** 🛠 Design probes for a payment API whose status-read path can
  work during a switch outage but new submissions cannot.
- [ ] **6.5** 💭 Why can a management port probe succeed while the application
  port is unable to accept traffic?
- [ ] **6.6** 🐛 A readiness indicator performs a five-second remote call on
  every Kubernetes probe. What is wrong?

<details><summary>Solutions 6</summary>

- 6.1 Liveness asks whether restarting this process can repair an
  unrecoverable internal state. Readiness asks whether this instance should
  receive new traffic for its advertised contract. Neither is a generic
  dependency dashboard.
- 6.2 Kubernetes can restart every otherwise healthy replica, creating a
  cascading restart storm while the database remains unavailable. External
  systems should not determine liveness.
- 6.3 No. If the service can degrade safely, removing every replica sacrifices
  useful capacity. Classify required versus optional capabilities. One service
  may need separate routing or explicit 503/pending behavior for the affected
  operation rather than global unready.
- 6.4 Keep liveness process-local. Base general readiness on whether the API
  can safely honor its declared routing contract; expose switch availability
  as a component/business signal and reject or queue new submissions
  explicitly while preserving status reads. If routing cannot distinguish
  operations, document the conservative choice.
- 6.5 A separate management connector can be healthy while the main connector,
  TLS, thread pool or route is broken. Expose probe groups on the main port
  where the platform needs end-to-end connector evidence, subject to security.
- 6.6 Probes must be cheap, bounded and resistant to amplification. The call
  consumes dependency capacity, can make readiness flap and may block probe
  threads. Prefer internal state updated by normal traffic/bounded background
  checks, with hysteresis when appropriate.

</details>

---

## 7. Diagnose a slow endpoint · 🎯 CORE PATH

- [ ] **7.1** 🏭 p99 rises from 250 ms to four seconds while average and CPU
  look normal. Give the first six checks in order.
- [ ] **7.2** 💭 How do you distinguish executor queue, JDBC pool wait, SQL
  execution, database lock wait and downstream time?
- [ ] **7.3** 🐛 SQL logs show each query takes 20 ms, so the database is ruled
  out. Why is that conclusion invalid?
- [ ] **7.4** 🔮 Servlet threads are 200, JDBC pool is 20, and 180 threads wait
  for connections. Will raising servlet threads improve throughput?
- [ ] **7.5** 💭 What evidence proves N+1 rather than one slow query?
- [ ] **7.6** 💭 After a rollback fixes latency, what closes the incident?

<details><summary>Solutions 7</summary>

- 7.1 Confirm the SLI/time window and customer scope; split endpoint/outcome/
  version/region; compare request and retry rate; inspect executor and pool
  saturation/wait; open slow traces; correlate deployment/config/dependency
  changes. Mitigate only after identifying a safe reversible action.
- 7.2 Instrument each boundary separately:

  ```text
  request total
    security/filter/controller
    executor queue age
    connection acquisition
    transaction + SQL execution
      database lock wait / plan time
    HTTP client pool wait + connect + response
    serialization
  ```

  Use executor/pool metrics, client/DB spans and database session/lock views.
  One wide "service" timer cannot allocate the wait.
- 7.3 Pool acquisition may occur before the logged SQL timer; lock wait can be
  folded into execution; 101 queries × 20 ms is two seconds; result transfer
  and ORM materialization add cost. Count statements and rows and measure pool
  wait/transaction duration.
- 7.4 No. The smaller pool/database is the current bottleneck; more request
  threads increase memory and queueing. Determine database capacity, reduce
  transaction/query cost and bound admission before changing pool ratios.
- 7.5 A representative trace or Hibernate/JDBC statistics shows one parent
  query plus repeated association queries whose count grows with result
  cardinality. Reproduce with multi-row fixtures and a statement-count budget;
  a one-row test cannot expose N+1.
- 7.6 Verify the user SLI and saturation recover, check correctness/backlog,
  preserve timeline/evidence, add a regression test or guardrail, document root
  cause and contributing controls, and assign durable follow-ups. Deployment
  success alone is not incident closure.

</details>

---

## 8. Dashboards alerts and business correctness · 🎯 CORE PATH

- [ ] **8.1** 💭 Design the first dashboard row for a payment API.
- [ ] **8.2** 🐛 An alert fires when cumulative `http.requests > 1_000_000`.
  Fix its meaning.
- [ ] **8.3** 💭 Page, ticket or dashboard: classify each signal.
- [ ] **8.4** 🛠 Name five business correctness/aging metrics beyond HTTP
  uptime.
- [ ] **8.5** 💭 Why must alert labels and runbook links remain low-cardinality
  and actionable?
- [ ] **8.6** 🏭 The error rate is low, but outbox oldest-event age grows for
  30 minutes. What does it mean?

<details><summary>Solutions 8</summary>

- 8.1 Start with user-visible request/command rate, success/business outcome,
  latency SLO distribution and error-budget burn. Put saturation and dependency
  attempt/latency immediately below. Allow drill-down by bounded operation,
  version, region and rail.
- 8.2 Alert on a rate or error ratio over a defined window, scoped to eligible
  traffic and compared with an SLO/baseline. A monotonically increasing total
  eventually crosses every fixed threshold and never recovers.
- 8.3 Page only for urgent customer/invariant harm requiring immediate human
  action; ticket for durable degradation/capacity work; dashboard for
  exploration/context. Every page needs an owner, safe first actions, links to
  evidence and a stop condition.
- 8.4 Examples: oldest pending-payment age, unpublished outbox count/oldest
  age, reconciliation mismatch count/value, duplicate command/effect count,
  compensation/reversal failure age, settlement-file lag and manual repair
  queue age. Avoid account/payment IDs as metric tags.
- 8.5 Unbounded labels create monitoring cost and fragmented alerts. An alert
  without affected service/SLO, severity, owner, evidence and safe runbook
  action transfers diagnosis into a stressful page.
- 8.6 The synchronous API can be healthy while event publication is stalled.
  Consumers see stale/missing business facts. Inspect publisher lease/polling,
  Kafka client/broker errors, poison rows and DB contention; protect retention
  and reconciliation before backlog exceeds recovery capacity.

</details>

---

## 9. Testing telemetry and operational controls

- [ ] **9.1** 🛠 Unit-test a custom observation's name and key values without
  a production exporter.
- [ ] **9.2** 💭 What should a test assert about metrics: exact implementation
  internals or the telemetry contract?
- [ ] **9.3** 🛠 Prove trace propagation through an outbound stub and through a
  custom executor.
- [ ] **9.4** 💭 How do load and fault tests validate dashboards?
- [ ] **9.5** 🐛 A test asserts a timer before the observed call completes.
  Why can its count be zero?

<details><summary>Solutions 9</summary>

- 9.1 Use Micrometer's observation-test support (`TestObservationRegistry`)
  or a simple registry/handler, execute the observation, then assert it stopped
  with the documented low/high-cardinality keys. Do not start Prometheus or
  an OTLP collector for a unit contract.
- 9.2 Assert stable semantic name, bounded key set, outcome/error recording and
  count/duration behavior. Avoid depending on exporter-normalized names,
  scrape text ordering or every framework-generated tag.
- 9.3 Build the client from Boot's auto-configured builder and make the stub
  capture standard trace headers. For the executor, install the chosen context
  propagation/task decorator and assert the child span/log correlation, then
  assert cleanup on thread reuse. Keep a smaller integration test with the
  actual tracing bridge.
- 9.4 Inject known delay, 5xx, pool saturation and backlog while sending
  controlled traffic. Verify the correct SLI changes, alerts fire at the
  intended window, traces allocate the delay, runbook queries work and the
  recovery signal clears. A dashboard first viewed during an incident is
  untested production code.
- 9.5 Timers/observations normally record on stop. Inside the timed callback,
  the sample is still active. Assert after completion; use a long-task timer
  when active work itself must be observable.

</details>

---

## 10. Senior production scenarios · 🎯 CORE PATH

- [ ] **10.1** 🏭 Trace IDs vanish only on scheduled reconciliation work.
  Diagnose without adding random headers everywhere.
- [ ] **10.2** 🏭 Prometheus series count grows by millions after a release.
  State containment, root cause and prevention.
- [ ] **10.3** 🏭 All pods restart during a database outage although their
  processes are healthy.
- [ ] **10.4** 🏭 p99 rises only for one rail; retry attempts triple while
  original request rate is flat.
- [ ] **10.5** 🏭 Operators need DEBUG for one package for ten minutes. How do
  you do it safely?
- [ ] **10.6** 🏭 HTTP success is 99.99%, but customers report missing payment
  updates.

<details><summary>Solutions 10</summary>

- 10.1 Scheduled work has no inbound HTTP context. Start a new observation for
  each bounded job/batch, propagate context through its configured executor,
  and attach safe reconciliation/batch identifiers as trace/log data—not
  metric tags. Confirm cleanup between runs.
- 10.2 Disable/deny the offending meter/tag at ingestion or via a `MeterFilter`
  to protect memory/backend; identify the unbounded value such as raw URI or
  payment id; replace it with a template/bounded dimension; add registry
  cardinality detection and review. Preserve aggregate evidence if possible.
- 10.3 An external dependency was incorrectly included in liveness. Remove it,
  restore the fleet, and represent the outage through readiness only if the
  whole contract is unservable; otherwise degrade the affected capability and
  alert on dependency/business SLIs.
- 10.4 Scope traces/metrics by bounded rail and separate original operations
  from attempts. Inspect that downstream's latency/503/timeouts and pool
  saturation. Stop amplification with one retry owner, remaining-deadline
  policy, breaker/bulkhead and safe idempotency behavior.
- 10.5 Use the secured, network-restricted Actuator loggers endpoint or managed
  configuration with audited authorization and an automatic reversion. Confirm
  the package cannot emit secrets, sample/limit volume and never expose the
  mutating endpoint publicly.
- 10.6 Inspect asynchronous business SLIs: outbox age/publication, Kafka lag,
  consumer errors/DLQ, idempotent no-op rate and reconciliation. Synchronous
  HTTP success proved acceptance, not completion of the distributed workflow.

</details>

---

## 11. Rapid-fire trap wall 🔮 · all keep

- [ ] **11.1** Are logs, metrics and traces interchangeable?
- [ ] **11.2** Is trace ID a safe metric tag?
- [ ] **11.3** Does a 200 health response prove business correctness?
- [ ] **11.4** Should database availability determine liveness?
- [ ] **11.5** Can p99 values computed independently on each pod be averaged?
- [ ] **11.6** Does `RestClient.create()` receive Boot tracing automatically?
- [ ] **11.7** Is a cumulative counter useful without a rate/window?
- [ ] **11.8** Should exception messages be metric tags?
- [ ] **11.9** Does a separate management port secure Actuator by itself?
- [ ] **11.10** Is an average enough for an SLO?
- [ ] **11.11** Does low CPU rule out saturation?
- [ ] **11.12** Should every expected business rejection be an ERROR stack?
- [ ] **11.13** Does sampling make traces sufficient for aggregate alerts?
- [ ] **11.14** Is a custom health indicator the right place for a slow query?
- [ ] **11.15** Can MDC remain set after a pooled task returns?
- [ ] **11.16** Does a high-cardinality trace field have no security cost?
- [ ] **11.17** Can an unbounded queue be monitored into safety?
- [ ] **11.18** Does HTTP success prove an outbox consumer completed?

<details><summary>Solutions 11</summary>

- 11.1 **No. Metrics aggregate, traces connect a request, logs explain events.**
- 11.2 **No. It creates an unbounded series dimension.**
- 11.3 **No. It proves only the configured probe contract.**
- 11.4 **No. Restart cannot repair the external database and can amplify it.**
- 11.5 **No. Use aggregatable histogram buckets for fleet percentiles.**
- 11.6 **No. Use the auto-configured builder or instrument it explicitly.**
- 11.7 **Not alone. Graph its rate and contextual ratios.**
- 11.8 **No. They are unbounded and may contain sensitive data.**
- 11.9 **No. Also enforce network and authentication/authorization controls.**
- 11.10 **No. It hides the tail and error distribution.**
- 11.11 **No. Threads/connections can be waiting rather than computing.**
- 11.12 **No. Model/count expected outcomes without noisy stacks.**
- 11.13 **No. Metrics must represent the unsampled population.**
- 11.14 **Usually no. Probes must be cheap/bounded; expose latency as metrics.**
- 11.15 **Yes. Always restore/clear context on thread reuse.**
- 11.16 **No. Privacy, access and retention policy still apply.**
- 11.17 **No. Bound it and reject/shed when useful completion is impossible.**
- 11.18 **No. Observe outbox age, delivery, consumer effect and reconciliation.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Logs/metrics/traces? | Metrics establish aggregate scope and saturation, traces allocate one distributed operation's time, and structured logs explain the decisive event with safe context. |
| Structured logging? | Stable event names and typed allowlisted fields; correlate with trace/business ids, redact at the source, control access/retention and clear MDC on pooled threads. |
| Meter choice? | Counter for event totals/rates, timer for count+latency, gauge for sampled current state, summary for value distributions and long-task timer for active long work. |
| Cardinality? | Every tag-value combination is a time series. Keep metric values bounded—never ids, raw URLs or messages—and reserve controlled high-cardinality detail for traces/logs. |
| Percentiles? | Fleet percentiles need aggregatable histogram buckets; per-instance client percentiles cannot simply be averaged. Choose buckets around the SLO. |
| Trace propagation? | Use Boot-instrumented client builders and context-aware executors/messaging; raw clients or threads lose propagation, and context must be restored/cleared. |
| Actuator security? | Expose only required endpoints; protect them with network plus authentication/authorization, sanitize config and treat dumps/log-level mutation as sensitive. |
| Liveness/readiness? | Liveness asks whether restart repairs this process; readiness asks whether it should receive traffic. External outage should not trigger fleet restarts. |
| Slow endpoint? | Scope the SLI, split total time into queue/pool/SQL-lock/downstream/serialization, inspect saturation and traces, correlate change, mitigate, then verify. |
| Business observability? | HTTP health is incomplete: measure stuck-state age, outbox lag, reconciliation mismatch, duplicates and compensation failures without high-cardinality tags. |

---

## Primary references

- [Spring Boot observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html)
- [Spring Boot metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)
- [Spring Boot tracing and propagation](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [Spring Boot structured logging](https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured)
- [Spring Boot Actuator endpoints and probes](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Micrometer Observation concepts](https://docs.micrometer.io/micrometer/reference/observation/introduction.html)
- [Micrometer meter concepts](https://docs.micrometer.io/micrometer/reference/concepts.html)
- [Micrometer histograms and percentiles](https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html)
- [OpenTelemetry concepts](https://opentelemetry.io/docs/concepts/)

---

## Extensions — after the senior core

- OpenTelemetry Collector pipelines, tail sampling and baggage governance.
- Exemplars, exemplified histograms and trace-to-log backend integration.
- Continuous profiling, Java Flight Recorder and async-profiler.
- eBPF/network observability and service dependency maps.
- Synthetic monitoring, real-user monitoring and canary analysis.
- Multi-window error-budget burn formulas and capacity forecasting.
- Audit logging design, immutability, access control and regulatory retention.

---

## How to drill this kit

1. Draw §0 and diagnose one slow request without saying “check logs.”
2. Classify 20 candidate fields as metric tag, trace/log field or forbidden.
3. Instrument one custom observation and prove its meter/span contract.
4. Inject pool wait, slow SQL, lock wait and downstream delay separately; show
   which signal distinguishes each.
5. Build liveness/readiness probes and simulate a database/switch outage.
6. Re-run §§2, 4, 6, 7, 10 and 11 blind after a week.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 evidence path | ⬜ | ⬜ | ⬜ |
| §1 logs/redaction/correlation | ⬜ | ⬜ | ⬜ |
| §2 meters/cardinality | ⬜ | ⬜ | ⬜ |
| §3 distributions/SLOs | ⬜ | ⬜ | ⬜ |
| §4 observation/tracing/context | ⬜ | ⬜ | ⬜ |
| §5 Actuator/security | ⬜ | ⬜ | ⬜ |
| §6 probes/dependencies | ⬜ | ⬜ | ⬜ |
| §7 slow-endpoint diagnosis | ⬜ | ⬜ | ⬜ |
| §8 dashboards/alerts/business | ⬜ | ⬜ | ⬜ |
| §9 telemetry tests | ⬜ | ⬜ | ⬜ |
| §10 production scenarios | ⬜ | ⬜ | ⬜ |
| §11 trap wall | ⬜ | ⬜ | ⬜ |
