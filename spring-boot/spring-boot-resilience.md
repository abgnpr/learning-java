# Spring Boot Resilience — Senior Backend Anti-Fumble Kit

For a backend engineer who can add a retry annotation but must design a
service that remains **bounded, correct, and diagnosable** when dependencies
slow down or fail. The goal is not to stack every resilience annotation. It
is to preserve the latency budget, protect scarce resources, and avoid
turning one failure into duplicate financial work or fleet-wide saturation.

**Readable reference:** [Spring Senior Backend Reference](spring-senior-core.md),
chapter 15.

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/configure · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Attempt every prompt aloud before opening its `<details>` solution. The
answer card near the end is for rehearsal after the exercises, not a
substitute for retrieval practice.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §8 → §9 → §10**
>
> Failure model · timeout budget · retry/idempotency · circuit breaker ·
> bulkheads/saturation · rate limiting/load shedding · composition/proxies ·
> safe degradation · shutdown · proof/telemetry · production scenarios.
>
> A senior resilience answer names five things: **deadline**, **bounded
> resource**, **classified failure**, **correctness rule**, and **signal**.
> “Add retry and circuit breaker” can make an outage worse if those five are
> unknown.

> **Version line.** Resilience patterns are framework-independent, but the
> annotations are not interchangeable. Spring Framework 7 provides
> `org.springframework.resilience.annotation.Retryable` and
> `@ConcurrencyLimit`, enabled with `@EnableResilientMethods`. Spring Retry's
> `org.springframework.retry.annotation.Retryable` is a separate,
> maintenance-only project used by many Boot 2.7/3 estates. Resilience4j uses
> annotations such as `io.github.resilience4j.retry.annotation.Retry` and its
> Boot 3 starter. Examples below use Resilience4j where a circuit breaker,
> rate limiter, bulkhead, or time limiter is required and explicitly label
> native Framework 7 features. Verify Boot 4 compatibility before assuming a
> Boot 3 starter is supported unchanged.

---

## 0. The one picture — contain failure before it queues · 🎯 CORE PATH

```text
incoming request (client deadline: 800 ms)
        │
        ├─ admission / rate limit / load shedding
        ▼
request executor ── bounded concurrency / queue
        │
        ├─ remaining budget passed to dependency
        ▼
retry policy ── circuit breaker ── bulkhead ── transport timeout
        │              │                 │              │
        │              │                 │              └─ bounds one attempt
        │              │                 └─ bounds concurrent pressure
        │              └─ stops calls to an unhealthy dependency
        └─ repeats only classified, safe work within the total deadline
        ▼
dependency / connection pool / database
        │
        └─ result, explicit safe fallback, or fast failure

Every boundary emits: latency · outcome · timeout · rejection · retry count
                     · breaker state · saturation · correlation/trace ID
```

- [ ] **0.1** 💭 Availability, resilience, and correctness: distinguish them
  for a payment API.
- [ ] **0.2** 💭 Why is a timeout the first resilience control to design,
  before retry or circuit breaking?
- [ ] **0.3** 🔮 A dependency hangs forever and the caller has three retries.
  How long can the request take if no attempt timeout exists?
- [ ] **0.4** 💭 Name the failure-amplification loop created by slow calls,
  growing queues, timeouts, retries, and more slow calls.
- [ ] **0.5** 💭 Which mechanisms bound **time**, **attempts**, **concurrency**,
  **rate**, and **failure propagation**?

<details><summary>Solutions 0</summary>

- 0.1 **Availability** is whether the operation can serve a valid response.
  **Resilience** is the system's ability to contain, recover from, or degrade
  through faults. **Correctness** is whether money and state obey invariants.
  A service returning a fabricated balance is available but incorrect; a
  fast explicit “temporarily unavailable” can preserve correctness.
- 0.2 Without an attempt deadline, calls can occupy threads, sockets,
  connections and locks indefinitely. Retry cannot schedule safely, and a
  breaker only observes completions. Bound each attempt and the end-to-end
  request first; then decide whether remaining time and semantics allow
  another attempt.
- 0.3 Unbounded. An attempt that never completes prevents the policy from
  reaching the next attempt. A retry count is not a time budget.
- 0.4 **Positive feedback / retry storm:** latency consumes executors and
  pools, queues add more latency, callers time out and retry, duplicate work
  increases load, and the dependency slows further. Backpressure, shedding,
  budgets and bounded retries break the loop.
- 0.5 Time → deadline/transport timeout; attempts → retry limit; concurrency
  → semaphore/thread-pool bulkhead or pool size; rate → rate limiter/admission
  control; repeated failure → circuit breaker. They solve different problems
  and need capacity-derived configuration.

</details>

---

## 1. End-to-end deadlines and attempt timeouts ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **1.1** 💭 Distinguish connection-acquisition, connect, TLS/handshake,
  response/read, write and overall request timeouts.
- [ ] **1.2** 🛠 Allocate an 800 ms API deadline across local work and two
  sequential dependencies. Include safety margin and explain why each client
  cannot independently receive 800 ms.
- [ ] **1.3** 🔮 An attempt timeout is 500 ms, retry has three total attempts,
  and backoff is 100 ms then 200 ms. What is the naive worst-case duration?
- [ ] **1.4** 💭 Deadline versus timeout: how does propagating remaining time
  prevent a downstream service from continuing useless work?
- [ ] **1.5** 🐛 A Resilience4j `TimeLimiter` times out a future and calls
  `cancel(true)`. Why might the database query or socket still run?
- [ ] **1.6** 🏭 The pool-acquisition timeout is 30 s but the API deadline is
  1 s. What does the caller see and what resource symptom should be measured?
- [ ] **1.7** 💭 Why should an interactive endpoint and a reconciliation batch
  use different timeout budgets?
- [ ] **1.8** 🐛 A controller catches `TimeoutException`, returns 500, and logs
  “payment failed.” Identify both mistakes.

<details><summary>Solutions 1</summary>

- 1.1 **Pool-acquisition** bounds waiting for a reusable connection.
  **Connect** bounds establishing TCP; TLS/handshake may have its own phase.
  **Write** bounds sending the request; **read/response** bounds waiting for
  bytes after connection. An **overall deadline** caps all attempts, backoff,
  queueing and local work. Exact knobs depend on the HTTP/database client.
- 1.2 One defensible budget: 80 ms ingress/local validation, 250 ms service A,
  300 ms service B, 70 ms response work and 100 ms safety margin. Real numbers
  come from SLOs and percentiles. Giving both dependencies 800 ms permits a
  sequential path to exceed the caller's deadline before overhead or retry.
- 1.3 `500 + 100 + 500 + 200 + 500 = 1,800 ms`, plus queueing and local
  overhead. A total deadline must stop attempts whose remaining budget is too
  small; `maxAttempts` alone does not enforce it.
- 1.4 A deadline is an absolute or remaining end-to-end budget. Propagating
  it lets every hop choose a smaller local timeout, reject hopeless work, and
  avoid committing after the upstream has abandoned the response. Clock and
  protocol design matter; internal systems often propagate remaining budget.
- 1.5 Future cancellation is a request, not proof that underlying blocking
  I/O understood interruption. Configure native HTTP/JDBC/statement timeouts
  too, and verify cancellation behavior. Otherwise the caller returns while
  worker threads and sockets keep consuming capacity.
- 1.6 The caller times out around 1 s while server work may wait another 29 s,
  building a hidden queue. Measure connection-pool pending/wait time, active
  versus max connections, acquisition timeouts and request cancellations.
  Make internal waits shorter than the remaining request budget.
- 1.7 Interactive latency is user-facing and must fail quickly. A controlled
  batch can accept longer work but needs bounded item/chunk deadlines,
  checkpointing and restartability. One global timeout either harms user
  latency or makes batch processing fragile.
- 1.8 A timeout is normally a dependency/availability result, so map it to a
  deliberate contract such as 504/503 rather than generic 500. For a side
  effect, timeout means **outcome unknown**, not proven failure; query by the
  stable operation ID or retry idempotently instead of declaring failure.

</details>

---

## 2. Retry, backoff, jitter and idempotency ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **2.1** 💭 Which failures are candidates for retry? Give examples of
  transient, permanent, business, overload and ambiguous failures.
- [ ] **2.2** 🔮 Should these normally retry: validation 400, authentication
  401, rate-limit 429, connection reset, read timeout after POST, database
  deadlock, insufficient funds?
- [ ] **2.3** 💭 Define idempotency at the business-operation level. Why is
  HTTP `PUT` vocabulary insufficient proof?
- [ ] **2.4** 🛠 Design an idempotency record for `POST /payments`: stable
  key, request fingerprint, status, result and concurrency behavior.
- [ ] **2.5** 💭 Exponential backoff plus jitter: what does each solve?
- [ ] **2.6** 🔮 Ten callers each retry immediately three times against one
  failing dependency. How many attempts can one initial wave create, and why
  can nested retries multiply further?
- [ ] **2.7** 🐛 A method retries `Exception.class` five times, including
  `IllegalArgumentException` and 403. Diagnose the policy.
- [ ] **2.8** 💭 Retry a transaction deadlock: where must retry advice sit
  relative to transaction advice, and what unit is repeated?
- [ ] **2.9** 💭 Compare Spring Framework 7 `@Retryable`, Spring Retry
  `@Retryable`, and Resilience4j `@Retry`. Why must imports be named?
- [ ] **2.10** 🏭 How should `Retry-After` and a shrinking caller deadline
  influence a 429/503 retry?

<details><summary>Solutions 2</summary>

- 2.1 Retry failures that are **classified transient** and whose operation is
  safe to repeat: selected connection failures, 502/503/504, deadlock or
  serialization abort. Do not retry validation/auth/business rejection.
  Overload needs server guidance and backoff; ambiguous side effects require
  an idempotency key or status query before repetition.
- 2.2 400/401/insufficient funds: no. 429: only if policy permits, honor
  `Retry-After`, and remain within deadline. Connection reset: often transient
  but method/phase matters. POST read timeout: outcome is ambiguous; retry
  only with the same idempotency key. Deadlock: retry the whole transaction
  in a fresh boundary if the use case is idempotent.
- 2.3 Business idempotency means repeated submissions with one logical key
  produce one durable effect and a consistent response. Method semantics do
  not enforce database uniqueness, payload equality, concurrency control, or
  retention of the recorded result.
- 2.4 Persist `(tenant, operationType, key)` uniquely plus a canonical request
  hash, `IN_PROGRESS/SUCCEEDED/FAILED_RETRYABLE`, result/reference and expiry.
  The first request owns execution; same key/same hash returns or waits for
  the recorded outcome; same key/different hash is a conflict. Use an atomic
  insert/lock and align the record with the business commit or outbox.
- 2.5 Backoff spaces attempts so a dependency can recover and reduces load.
  Jitter de-synchronizes clients so they do not wake in one thundering herd.
  Cap both delay and attempts; stop when the remaining deadline cannot hold a
  useful attempt.
- 2.6 With three total attempts, one wave of ten can produce thirty calls.
  If gateway, service and client each retry three times, the dependency can
  see up to `3 × 3 × 3 = 27` attempts per original request. Assign retry
  ownership to one layer and expose budgets/attempt metadata.
- 2.7 It retries programmer defects, permanent client errors and perhaps
  unsafe writes, increasing latency and load while hiding defects. Use a
  narrow allowlist/predicate, exclude permanent/business failures, bound
  time/attempts and record exhaustion.
- 2.8 Retry must normally surround the transactional method so each attempt
  opens a **fresh transaction and persistence context**. Repeat the entire
  invariant-preserving unit, not just the final SQL statement. Make AOP order
  explicit or separate retry orchestration and transactional work into beans.
  See [Spring transactions deep](spring-boot-transactions-deep.md).
- 2.9 They are different libraries/packages and their attempt-count,
  enablement, backoff, event and recovery APIs differ. Framework 7 uses
  `org.springframework.resilience.annotation.Retryable` plus
  `@EnableResilientMethods`; legacy Spring Retry uses
  `org.springframework.retry.annotation.Retryable`; Resilience4j uses
  `@Retry(name = ...)`. Never infer semantics from the short annotation name.
- 2.10 Honor server guidance only if delay plus another attempt fits the
  remaining deadline. Add jitter where appropriate, cap waits, and return a
  retryable response when the caller is better placed to retry. Never sleep
  past the upstream deadline while occupying scarce server capacity.

</details>

---

## 3. Circuit breaker — stop calling, do not heal the dependency ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 💭 State the purpose of a circuit breaker and what it does not
  do.
- [ ] **3.2** 🔮 Trace `CLOSED → OPEN → HALF_OPEN → CLOSED/OPEN` including
  rejection behavior.
- [ ] **3.3** 💭 Failure-rate threshold, slow-call threshold, sliding window,
  minimum calls, open wait and half-open permits: explain each.
- [ ] **3.4** 🔮 Window size is 100 and minimum calls is 50. The first nine
  calls all fail. Must the breaker open?
- [ ] **3.5** 🐛 A breaker counts 404 “account not found” as dependency
  failure. What distortion follows?
- [ ] **3.6** 💭 Why is a breaker not a timeout, retry, concurrency limiter or
  health check?
- [ ] **3.7** 🏭 One breaker is shared across two regions of a downstream
  service; only one region is failing. What is the granularity problem?
- [ ] **3.8** 💭 Should an OPEN breaker automatically make application
  readiness DOWN? Give the trade-off.
- [ ] **3.9** 🛠 Name the metrics and events needed to tune a breaker safely.

<details><summary>Solutions 3</summary>

- 3.1 It detects a high recent rate of classified failures/slow calls and
  **fails fast**, protecting caller resources and reducing load on an
  unhealthy dependency. It does not repair the dependency, guarantee a
  fallback, cap in-flight calls, or replace transport deadlines.
- 3.2 CLOSED records permitted calls. At configured thresholds after the
  minimum sample, it opens. OPEN rejects with `CallNotPermittedException`.
  After the wait, HALF_OPEN permits a small probe set and rejects excess;
  healthy probes close it, unhealthy probes reopen it. Exact transition
  options are configuration-specific.
- 3.3 The window selects recent observations; minimum calls avoids decisions
  on tiny samples; failure/slow thresholds define unhealthy ratios; open wait
  creates recovery time; half-open permits bound probe traffic. All must match
  real volume and recovery characteristics.
- 3.4 No. The minimum sample is not met, so the rate is not yet actionable.
  This prevents noisy low-volume transitions but means timeouts still need to
  protect early calls.
- 3.5 Expected business absence makes the breaker appear unhealthy and can
  reject legitimate traffic. Classify only availability/technical outcomes
  as failures; ignored or successful business results should not trip it.
- 3.6 Timeout bounds one attempt; retry repeats it; bulkhead bounds concurrent
  pressure; health indicates service state. A breaker learns from recent
  outcomes and decides whether new calls are permitted. Compose intentionally.
- 3.7 The healthy region is blacked out because unrelated failure domains
  share state. Breakers should often align with dependency, operation,
  endpoint/region and materially different failure behavior—without creating
  unbounded high-cardinality registries.
- 3.8 Usually not automatically. If the dependency is optional, marking the
  whole instance unready can remove healthy capacity and worsen an outage.
  Readiness should fail only when the instance cannot serve its contractual
  core; expose breaker state separately and alert on it.
- 3.9 Permitted/success/failed/slow/not-permitted calls, latency, current
  state/transitions, failure categories, half-open results, fallback outcome
  and dependency saturation. Tune from production percentiles and SLOs, not
  framework defaults.

</details>

---

## 4. Bulkheads, executors, pools and saturation ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **4.1** 💭 Explain the ship-compartment analogy in resource terms.
- [ ] **4.2** 💭 Semaphore bulkhead versus fixed thread-pool bulkhead: queue,
  thread/context propagation and latency trade-offs.
- [ ] **4.3** 🔮 A servlet pool has 200 threads, dependency bulkhead permits
  100 calls, but its connection pool has 20 connections. Where does work
  queue and what should be aligned?
- [ ] **4.4** 🐛 An executor is configured with 500 threads and an unbounded
  queue “to prevent rejection.” Why is this not resilience?
- [ ] **4.5** 💭 Queue capacity, queue wait, rejection policy and caller
  deadline: how do they interact?
- [ ] **4.6** 🏭 A slow reporting dependency consumes the same executor as
  payment authorization. Design isolation.
- [ ] **4.7** 💭 Why can virtual threads improve thread scalability without
  removing the need for concurrency limits?
- [ ] **4.8** 🐛 A `CallerRunsPolicy` executes rejected remote calls on an
  event-loop thread. What can happen?
- [ ] **4.9** 🛠 What saturation signals belong on dashboards?

<details><summary>Solutions 4</summary>

- 4.1 A bulkhead partitions a bounded resource so one dependency/tenant/work
  class cannot consume all threads, permits, connections or queue slots.
  Failure stays inside a compartment and unrelated work retains capacity.
- 4.2 A semaphore limits concurrent calls while work stays on the caller's
  thread—low overhead and easier context, but blocked I/O still occupies that
  caller. A thread-pool bulkhead isolates execution with a fixed pool and
  bounded queue, adding scheduling/queue latency and context-propagation work.
  Choose based on execution model and failure domain.
- 4.3 At least 80 permitted calls can wait for a database/HTTP connection;
  more may wait at the semaphore and servlet pool. Align downstream permits,
  connection capacity and arrival rate, and make pool wait bounded and
  visible. The tightest scarce resource determines throughput.
- 4.4 It moves overload into memory and scheduler contention. An unbounded
  queue hides pressure, produces huge tail latency and stale work, and can
  exhaust heap. Use capacity-derived pools, small bounded queues, deadlines
  and explicit rejection/load shedding.
- 4.5 Queueing is part of the deadline. Reject when capacity cannot complete
  work usefully; do not let items wait beyond the caller's budget. Record
  queue wait separately from execution, and choose a rejection policy that is
  safe for the calling thread model.
- 4.6 Give reporting a separate bounded executor/bulkhead and connection
  allocation, lower priority/rate and perhaps asynchronous job contract.
  Reserve payment capacity; do not let optional report slowness consume the
  authorization path's threads or pool.
- 4.7 Virtual threads make blocking threads cheaper, but the database,
  downstream service, sockets and heap remain finite. Without admission
  control, cheap callers can overwhelm expensive dependencies faster. Use
  semaphores, Framework 7 `@ConcurrencyLimit`, or another explicit limiter.
- 4.8 The event loop blocks on remote I/O, preventing it from processing many
  unrelated channels and causing system-wide latency. Rejection policies must
  respect thread roles; fail fast or move deliberately to a blocking executor.
- 4.9 Active/max threads, queue depth/capacity, oldest/average queue wait,
  task duration, rejection count, semaphore permits/wait, connection active/
  idle/pending, acquisition latency/timeouts, event-loop lag, CPU and heap.

</details>

---

## 5. Rate limiting, backpressure and load shedding ⭐⭐ · 🎯 CORE PATH

- [ ] **5.1** 💭 Rate limiting versus authentication, authorization and quota.
- [ ] **5.2** 💭 Inbound versus outbound rate limiting: whose capacity does
  each protect?
- [ ] **5.3** 🔮 A rate limiter allows 100 requests/second but each call takes
  5 seconds and the dependency supports 100 concurrent calls. Is rate alone
  sufficient?
- [ ] **5.4** 💭 Token bucket/fixed-window awareness: why do burst semantics
  matter even when average rate is safe?
- [ ] **5.5** 🐛 The limiter waits up to 10 seconds for a permit on a 1-second
  endpoint. Diagnose.
- [ ] **5.6** 🏭 How would you avoid one tenant consuming a shared payment API
  while preventing unbounded per-tenant limiter state?
- [ ] **5.7** 💭 Backpressure versus dropping/shedding: when should a service
  reject immediately rather than queue?
- [ ] **5.8** 💭 Which HTTP responses/headers communicate throttling or
  temporary overload, and what retry behavior should clients use?

<details><summary>Solutions 5</summary>

- 5.1 Authentication establishes identity; authorization grants operations;
  rate limiting controls request admission over time; quota accounts for a
  longer-period allowance/contract. Identity can be an input to a limit, but
  a permitted rate does not grant access.
- 5.2 Inbound limits protect your service/fairness and abuse boundary.
  Outbound limits protect a dependency contract and your share of it. Edge,
  service and dependency controls can coexist but must not create conflicting
  hidden queues.
- 5.3 No. At 100/s with 5 s service time, Little's-Law intuition implies
  roughly 500 concurrent in-flight calls, far beyond 100. Pair rate with
  concurrency limits and timeouts; latency changes the safe arrival rate.
- 5.4 A burst can exhaust threads/connections before the average window looks
  unhealthy. Define capacity for burst size and refill rate, or smooth/reject
  bursts. Financial operations may require fairness by tenant and operation.
- 5.5 Permit wait already exceeds the caller's deadline, so accepted work is
  stale before execution. Use zero/small bounded wait within remaining time,
  fail fast and tell clients when a retry may be useful.
- 5.6 Use authenticated tenant identity, bounded per-tenant buckets plus a
  global limit, sensible defaults and eviction/centralized gateway support.
  Cap cardinality and do not accept attacker-controlled arbitrary limiter
  names as permanent registry keys.
- 5.7 Backpressure slows producers when the protocol and bounded buffers can
  propagate demand. Reject when queues are full, deadlines cannot be met, the
  producer cannot be slowed, or accepting work threatens critical traffic.
  For asynchronous durable work, a broker can be the explicit buffer.
- 5.8 `429 Too Many Requests` is usual for a rate policy; `503 Service
  Unavailable` for temporary inability to serve, sometimes with
  `Retry-After`. Clients use bounded exponential backoff/jitter, honor server
  guidance and keep the same idempotency key for side effects.

</details>

---

## 6. Composition, aspect ordering and proxy traps ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **6.1** 💭 Propose an ordering for timeout, retry, circuit breaker and
  bulkhead, then explain why there is no universal annotation order.
- [ ] **6.2** 🔮 If the breaker sits inside retry, does it record individual
  attempts or the final logical call? What if it sits outside?
- [ ] **6.3** 💭 Why can retry outside a semaphore bulkhead release/reacquire
  a permit between attempts while retry inside may hold capacity differently?
- [ ] **6.4** 🐛 `this.callBank()` carries `@Retry` and `@CircuitBreaker` but is
  invoked inside the same bean. What happens?
- [ ] **6.5** 🐛 A fallback method returns the wrong type and omits one original
  argument. When is the mistake discovered?
- [ ] **6.6** 💭 Annotation composition versus programmatic decorators: when
  is explicit functional composition preferable?
- [ ] **6.7** 🐛 Retry wraps `@Transactional` in one environment but reverses
  after an upgrade. Why is correctness at risk?
- [ ] **6.8** 💭 How should context—trace ID, security identity, MDC and
  deadline—cross a thread-pool bulkhead?
- [ ] **6.9** 🛠 Give a minimal Resilience4j configuration for one dependency
  with explicitly classified retry, breaker and semaphore bulkhead.

<details><summary>Solutions 6</summary>

- 6.1 A common intent is one total logical call whose retry creates bounded
  attempts, each protected by dependency concurrency and transport timeout,
  while the breaker observes either attempts or final calls deliberately.
  Changing order changes metrics, permit holding, breaker samples and
  transaction boundaries. Resilience4j's documented annotation nesting is
  `Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(function)))))` by
  default, and its aspect-order properties can change precedence. Start from
  desired semantics, then configure and test the actual chain instead of
  treating that default as universal.
- 6.2 Breaker inside retry sees each attempt and may open during one logical
  request. Breaker outside normally sees the final result after retry and
  undercounts dependency attempts. Neither is universally correct: decide
  what “call” means for protection and metrics.
- 6.3 With retry outside, one attempt can release its permit during backoff,
  allowing other traffic; with bulkhead outside, the logical call may retain
  admission across retries/backoff. Exact decorator order matters for
  fairness, amplification and capacity.
- 6.4 The self-call bypasses Spring AOP, so neither aspect is applied. Put the
  advised boundary on an externally invoked method or another injected bean,
  or use explicit decorators. See [Spring Boot basics](spring-boot-basics.md).
- 6.5 Usually at runtime when fallback resolution is attempted. Resilience4j
  fallback behaves like typed catch selection and must match the original
  parameters plus an optional exception and compatible return type. Test
  every fallback path; startup success is not enough proof.
- 6.6 Use decorators/templates when order must be obvious, per-call policy is
  dynamic, a method is self-invoked/unmanaged, or one wants explicit metric
  scope. Annotations are concise for stable bean boundaries but hide order
  behind multiple proxies/aspects and external configuration.
- 6.7 A deadlock attempt must end its failed transaction before retry. If the
  transaction surrounds all attempts, later attempts may reuse rollback-only
  state. Separate retry coordinator and transactional attempt, and test that
  each attempt receives a fresh transaction rather than relying on accidental
  default advice precedence.
- 6.8 Capture and propagate only the required context with framework-supported
  task decorators/context-propagation tools; clear it after execution. Never
  assume `ThreadLocal` state follows executor work, and never leak a previous
  request's MDC/security context into a reused thread.
- 6.9 One illustrative shape—numbers require capacity evidence:

  ```yaml
  resilience4j:
    retry:
      instances:
        bankClient:
          maxAttempts: 3
          waitDuration: 100ms
          retryExceptions:
            - java.io.IOException
          ignoreExceptions:
            - com.example.PermanentBankRejection
    circuitbreaker:
      instances:
        bankClient:
          slidingWindowSize: 50
          minimumNumberOfCalls: 20
          failureRateThreshold: 50
          slowCallDurationThreshold: 400ms
          slowCallRateThreshold: 60
          waitDurationInOpenState: 10s
          permittedNumberOfCallsInHalfOpenState: 5
    bulkhead:
      instances:
        bankClient:
          maxConcurrentCalls: 20
          maxWaitDuration: 0
  ```

  The HTTP client still needs native connection, acquisition and response
  timeouts. Add jitter/programmatic interval logic if the chosen configuration
  surface does not express the policy needed.

</details>

---

## 7. Graceful degradation and safe fallbacks ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **7.1** 💭 What makes a fallback semantically safe rather than merely
  available?
- [ ] **7.2** 🔮 Balance service is unavailable. May a payment authorization
  fallback use yesterday's cached balance?
- [ ] **7.3** 🏭 Give safe fallback examples for product recommendations,
  statement generation and payment initiation.
- [ ] **7.4** 🐛 Every exception is caught by a generic fallback returning an
  empty list. What failures does this hide?
- [ ] **7.5** 💭 Fail-open versus fail-closed for sanctions screening,
  authentication and an optional personalization service.
- [ ] **7.6** 💭 Cached fallback: what metadata and constraints must accompany
  stale data?
- [ ] **7.7** 🏭 A request is accepted for asynchronous completion. What must
  be durable before returning 202?
- [ ] **7.8** 🛠 Define a response contract that distinguishes business
  rejection, technical unavailability, pending/unknown and accepted work.

<details><summary>Solutions 7</summary>

- 7.1 It preserves the operation's business invariant and contract under the
  specific failure. It must be authorized, bounded, observable, distinguish
  stale/pending data and never invent a success that the source of truth did
  not confirm.
- 7.2 Normally no. Stale balance can approve unavailable funds and create
  financial loss. Fail explicitly or use a formally designed offline-risk
  policy with limits, reconciliation and accountable business approval—not a
  generic cache fallback.
- 7.3 Recommendations can return a smaller cached/default set; statements can
  accept a durable generation job and provide a status resource; payment
  initiation can persist an idempotent pending command/outbox and return its
  stable reference, but must not claim completion before authoritative state.
- 7.4 Programmer defects, authorization failures, bad requests, mapping bugs
  and data corruption become fake “no data” successes. Catch only failures for
  which the fallback is valid; preserve causal error classification and alert
  on fallback use.
- 7.5 Sanctions and authentication normally fail closed because bypass
  violates security/compliance. Optional personalization can fail open by
  omitting personalization. The choice is a business/security decision with
  risk limits, not a library default.
- 7.6 Include source timestamp/version, maximum staleness, tenant/user scope,
  authorization rules, invalidation behavior and an explicit stale indicator
  where users need it. Never serve cross-tenant or revoked-sensitive data.
- 7.7 Persist the command/job and its stable idempotency key plus enough state
  to resume, deduplicate and expose status. If enqueue and database state span
  resources, use an outbox or another deliberate coordination design.
- 7.8 Use domain-specific states/statuses: validation/business rejection
  (stable client-visible reason), technical unavailable/retryable, `PENDING`
  or `UNKNOWN` with operation ID and status query, and `ACCEPTED` only after
  durable ownership. Do not collapse all into 200 or generic 500.

</details>

---

## 8. Graceful shutdown and draining ⭐⭐ · 🎯 CORE PATH

- [ ] **8.1** 💭 Trace a rolling shutdown from readiness removal/SIGTERM to
  web-server drain, lifecycle stop and process exit.
- [ ] **8.2** 🔮 The grace period is 20 s but a request timeout is 60 s. Can
  every accepted request finish?
- [ ] **8.3** 💭 What happens to scheduled tasks, executor queues, Kafka
  consumers and in-flight database work during shutdown?
- [ ] **8.4** 🐛 A pod receives SIGKILL after its orchestration grace period.
  Why did Spring's graceful-shutdown configuration not save it?
- [ ] **8.5** 💭 Boot 2.7 versus current Boot graceful-shutdown default: what
  must a legacy-service answer mention?
- [ ] **8.6** 🏭 How do idempotency and consumer offset/ack ordering make
  forced termination recoverable?
- [ ] **8.7** 🛠 Which shutdown timings must be aligned across load balancer,
  readiness, application lifecycle and orchestrator?

<details><summary>Solutions 8</summary>

- 8.1 Mark the instance unready/drain it from routing, receive SIGTERM, stop
  admitting new work, let accepted work finish within the phase timeout,
  stop `SmartLifecycle` components/executors/consumers in controlled order,
  close the context/resources, then exit. Exact web-server rejection behavior
  varies; test the deployed platform.
- 8.2 No. The application can be forced to terminate at 20 s. Keep per-request
  deadlines below the usable drain window or make long work durable and
  resumable; account for pre-stop/load-balancer propagation time too.
- 8.3 They need explicit lifecycle semantics: stop fetching new messages,
  decide whether queued tasks drain or reject, interrupt/cancel safely, commit
  only completed units, and close pools after users stop. Fire-and-forget work
  without durable ownership can be lost.
- 8.4 Graceful handling requires the process to receive a catchable shutdown
  signal and enough time. SIGKILL cannot be handled. Align Kubernetes
  `terminationGracePeriodSeconds`, pre-stop/drain delay and Spring lifecycle
  timeout, and fix work that cannot finish or resume.
- 8.5 Boot 2.7 supports graceful shutdown but requires
  `server.shutdown=graceful`; current Boot documentation says it is enabled by
  default and `server.shutdown=immediate` disables it. Always state the
  deployed version and configure/test rather than assuming.
- 8.6 Persist side effects before acknowledging/committing the message; after
  forced death, unacknowledged work is redelivered. A stable idempotency key or
  unique business constraint prevents a duplicate effect. Shutdown improves
  the common path; replay safety handles the forced path.
- 8.7 Align endpoint deadline, executor/consumer drain timeout,
  `spring.lifecycle.timeout-per-shutdown-phase`, load-balancer deregistration
  propagation, pre-stop hook and orchestrator termination grace. Leave margin
  for context/resource closure and measure forced terminations.

</details>

---

## 9. Configuration, testing and observability ⭐⭐ · 🎯 CORE PATH

- [ ] **9.1** 💭 Which policy values belong in typed configuration and what
  validation should reject at startup?
- [ ] **9.2** 🛠 Design tests for timeout, retry classification, idempotency,
  breaker transition, bulkhead rejection and fallback correctness.
- [ ] **9.3** 🐛 A test mocks the client to throw instantly and claims the
  timeout/bulkhead behavior works. What did it fail to prove?
- [ ] **9.4** 💭 Why are retry count and fallback count insufficient without
  original-call volume and outcome?
- [ ] **9.5** 🛠 Name low-cardinality metric tags and dangerous high-cardinality
  values.
- [ ] **9.6** 💭 What should be logged once per logical call versus once per
  attempt?
- [ ] **9.7** 🏭 How would you load-test saturation without damaging a real
  downstream system?
- [ ] **9.8** 💭 Which alerts indicate an emerging resilience failure before
  outright errors spike?

<details><summary>Solutions 9</summary>

- 9.1 Externalize per-dependency deadlines, attempts, backoff caps, breaker
  windows/thresholds, concurrency, queue and rate values. Validate positive
  durations, attempts ≥ 1, thresholds/ranges, queue bounds and cross-field
  rules such as attempt budget fitting total deadline. Defaults must be safe,
  not silent infinity.
- 9.2 Use a controllable stub/toxiproxy-like dependency for latency, reset,
  status and partial-response faults; a fake clock where supported; concurrent
  duplicate requests against a real database; deterministic breaker windows;
  executor/pool saturation; and explicit assertions on attempts, deadlines,
  side-effect count, response contract and metrics.
- 9.3 It proves exception classification only. It does not hold threads,
  consume permits, queue work, exercise real transport timeout/cancellation,
  race concurrent callers, or verify resource release after a slow response.
- 9.4 Ten retries from ten calls differs radically from ten retries from one
  call. Use rates/ratios and final outcomes: original requests, dependency
  attempts, retry amplification, exhausted calls, recovered calls, latency
  added, fallback correctness and duplicate-prevention conflicts.
- 9.5 Safe tags: dependency/operation from a bounded enum, outcome category,
  exception family, breaker name/state, attempt bucket. Dangerous tags: raw
  URL, account/payment/idempotency key, exception message, tenant/customer ID
  or arbitrary downstream host.
- 9.6 Per attempt: attempt number, dependency, timeout/budget, classified
  failure and duration—often debug/metric/event rather than noisy error.
  Per logical call: final outcome, total attempts/latency, fallback, stable
  operation/correlation ID and exhaustion reason. Redact financial data.
- 9.7 Point the service at an isolated controllable stub with production-like
  connection/executor limits. Increase arrival rate and injected latency,
  verify bounded queues/memory, critical-traffic isolation, rejection and
  recovery. Never use production dependency failure as the test harness.
- 9.8 Rising p95/p99, pool acquisition/queue wait, active/max saturation,
  slow-call rate, retry amplification, rejected/not-permitted calls, half-open
  flapping, fallback rate, pending operation age and shutdown timeouts. Alert
  on sustained SLO risk, not every single retry.

</details>

---

## 10. Senior production scenarios ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **10.1** 🏭 A bank API goes from 80 ms to 2 s. Your service's request
  threads and connection pool saturate. Give the first containment actions
  and the diagnosis path.
- [ ] **10.2** 🏭 A deployment causes duplicate payment submissions although
  clients used the same idempotency key. Where do you investigate?
- [ ] **10.3** 🏭 The breaker opens every Monday at peak and closes off-peak.
  Is increasing the failure threshold a defensible first fix?
- [ ] **10.4** 🏭 A fallback cache makes availability green while customers
  see stale account status. What failed operationally?
- [ ] **10.5** 🏭 Retry metrics triple after a release, but final error rate is
  flat. Why is this still dangerous?
- [ ] **10.6** 🏭 One tenant's reconciliation job starves interactive traffic.
  Design admission, isolation and fairness.
- [ ] **10.7** 🏭 Half-open probes are expensive writes. How do you avoid
  unsafe “health test” side effects?
- [ ] **10.8** 🏭 A graceful deployment still loses executor work. Give the
  likely lifecycle and durability failures.
- [ ] **10.9** 🏭 A dependency times out, then completes the debit. Your retry
  receives “duplicate key.” What response should the service produce?
- [ ] **10.10** 🏭 Propose a resilience review checklist for a new downstream
  integration.

<details><summary>Solutions 10</summary>

- 10.1 Contain with short evidence-based deadlines, bounded concurrency and
  queue, load shedding and disabled/limited unsafe retries. Preserve critical
  traffic. Trace controller/filter time → executor queue → connection acquire
  → connect/read → downstream spans; inspect dependency latency/errors and
  local CPU/GC. A breaker may fail fast after evidence but is not the first
  substitute for timeouts.
- 10.2 Verify key scope and stable reuse through gateway/service, canonical
  request hash, uniqueness constraint, atomic ownership, record/business
  commit boundary, retention/expiry, race behavior and whether deployment
  changed tenant/operation namespacing. Count durable effects, not responses.
- 10.3 No. Peak-only opening points to capacity, timeout, rate or dependency
  saturation. Raising thresholds can allow more failing/slow calls and worsen
  it. Inspect slow/failure categories, pool waits, volume and dependency SLO;
  fix capacity/admission or budgets, then tune from evidence.
- 10.4 The fallback violated freshness/business semantics and observability
  reported technical success instead of degraded correctness. Expose stale/
  fallback outcomes, cap age, alert on use and fail explicitly when stale data
  cannot safely answer the operation.
- 10.5 Hidden retries consume downstream capacity, inflate tail latency and
  may be masking a regression until volume rises. Track attempts per logical
  call, recovered/exhausted rates and dependency load; fix the new failure
  rather than celebrating final success.
- 10.6 Separate endpoint/work class, tenant-aware global + per-tenant rate
  limits, bounded batch concurrency/executor/connection allocation, small
  chunks/checkpoints and priority for interactive work. Prefer durable async
  jobs and publish progress rather than holding request threads.
- 10.7 Half-open traffic should be normal idempotent requests, safe read
  probes, or controlled operations with stable keys—not invented debits.
  Limit probe concurrency and classify outcomes. A breaker probe is real
  traffic unless the dependency provides a side-effect-free health endpoint.
- 10.8 Executor did not participate in lifecycle, queue was in memory,
  shutdown stopped it before web/consumer intake, grace was shorter than work,
  or the orchestrator killed early. Use managed executors/lifecycle ordering,
  stop intake first, durable queues/outbox for must-run work, idempotent replay
  and aligned timeouts.
- 10.9 Treat duplicate as evidence of the same operation, fetch the stored
  authoritative result by idempotency/business reference, and return that
  result or `PENDING/UNKNOWN` status—not a second debit, generic conflict or
  false failure.
- 10.10 Contract/SLO and failure modes; idempotency/status query; end-to-end
  and transport budgets; retry ownership/classification/backoff; breaker
  granularity; concurrency/rate/pool limits; queue/rejection; safe fallback;
  security/data rules; shutdown/replay; metrics/traces/logs; fault/load tests;
  runbook and owners.

</details>

---

## 11. Rapid-fire trap wall 🔮 · all keep

- [ ] **11.1** Does three retries mean at most three retries after the first
  call in every library?
- [ ] **11.2** Does a circuit breaker limit concurrent calls?
- [ ] **11.3** Does a time limiter guarantee that blocking I/O stopped?
- [ ] **11.4** Is every 5xx safe to retry?
- [ ] **11.5** Is a read timeout proof that a POST had no effect?
- [ ] **11.6** Does exponential backoff remove the need for jitter?
- [ ] **11.7** Is an unbounded executor queue a safe way to avoid rejection?
- [ ] **11.8** Does a rate limiter protect against slow-call concurrency?
- [ ] **11.9** Is an OPEN breaker evidence that your service itself must be
  removed from readiness?
- [ ] **11.10** Is an empty response a safe universal fallback?
- [ ] **11.11** Do resilience annotations work on same-bean self-invocation?
- [ ] **11.12** Are all annotations named `@Retryable` the same API?
- [ ] **11.13** Can retry wrap one rollback-only transaction across attempts?
- [ ] **11.14** Are virtual threads a replacement for downstream admission
  control?
- [ ] **11.15** Is `429` authentication failure?
- [ ] **11.16** Should a rejected bulkhead call be retried immediately?
- [ ] **11.17** Does graceful shutdown make in-memory fire-and-forget work
  durable?
- [ ] **11.18** Is Boot 2.7 graceful shutdown enabled by the same default as
  current Boot?
- [ ] **11.19** Should a fallback turn a sanctions-screening outage into
  approval?
- [ ] **11.20** Is a breaker threshold chosen from intuition sufficient
  without minimum-call/window behavior?

<details><summary>Solutions 11</summary>

- 11.1 **No. Some count total attempts; Framework 7 names retries after the
  initial call. Read the actual API.**
- 11.2 **No. Use a bulkhead/concurrency limiter.**
- 11.3 **No. Configure and test native transport/resource cancellation.**
- 11.4 **No. Classify method, status and semantics; some failures are
  permanent or ambiguous.**
- 11.5 **No. The result is unknown; query or retry idempotently.**
- 11.6 **No. Jitter prevents synchronized retry waves.**
- 11.7 **No. It hides overload as memory use and tail latency.**
- 11.8 **No. Pair rate with concurrency and time bounds.**
- 11.9 **No. Base readiness on whether the instance can serve its core
  contract.**
- 11.10 **No. It can hide defects and violate response semantics.**
- 11.11 **No. Proxy advice is bypassed.**
- 11.12 **No. Framework 7 and Spring Retry use different packages; Resilience4j
  uses `@Retry`.**
- 11.13 **No. Each transactional retry normally needs a fresh transaction.**
- 11.14 **No. Dependencies and memory remain finite.**
- 11.15 **No. It is admission/rate policy, commonly for an authenticated or
  anonymous identity.**
- 11.16 **Usually no. Immediate retry repeats overload; use bounded backoff,
  remaining budget and retry ownership.**
- 11.17 **No. Durable ownership and replay/idempotency do.**
- 11.18 **No. Boot 2.7 requires `server.shutdown=graceful`; current Boot
  enables graceful shutdown by default.**
- 11.19 **No. Security/compliance paths normally fail closed.**
- 11.20 **No. Sample size, window, traffic and classified outcomes determine
  transition behavior.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Timeout strategy? | Start with the caller's end-to-end deadline, reserve local/safety time, give every queue/pool/transport attempt a smaller bound, propagate remaining budget and verify underlying cancellation. |
| When retry? | Only a classified transient failure, only when repeat is safe or idempotency-protected, with bounded attempts, exponential backoff/jitter and enough remaining deadline. |
| Ambiguous timeout? | Timeout means unknown, not failed. Reuse the stable operation key and query/return the authoritative recorded outcome. |
| Circuit breaker? | A state machine over recent classified failure/slow-call outcomes that fails fast while unhealthy and admits bounded half-open probes. It is not a timeout or concurrency limiter. |
| Bulkhead? | Partition bounded concurrency/executor/connection capacity so one dependency or workload cannot starve unrelated traffic; bound queues and reject when useful completion is impossible. |
| Rate limiter? | Admission over time, separate from authN/authZ. Use global and fair scoped limits, define burst/wait semantics, and pair it with concurrency bounds. |
| Safe fallback? | One that preserves authorization and business invariants, labels stale/pending state, is bounded/observable, and never fabricates financial success. |
| Annotation trap? | Advice applies only through a Spring proxy; self-invocation bypasses it. Name the annotation package and test aspect order because retry, breaker, transaction and bulkhead nesting changes semantics. |
| Saturation diagnosis? | Split total latency into queue, pool acquisition, dependency and local execution; inspect active/max, queue age/depth, rejects, connection pending and retry amplification. |
| Graceful shutdown? | Stop admission, drain accepted work within an aligned grace period, stop consumers/executors in order, and rely on durable replay plus idempotency for forced termination. |

---

## Primary references

- [Spring Framework resilience features](https://docs.spring.io/spring-framework/reference/core/resilience.html)
- [Spring Retry project status and documentation](https://github.com/spring-projects/spring-retry)
- [Resilience4j Spring Boot 2/3 integration](https://resilience4j.readme.io/docs/getting-started-3)
- [Resilience4j circuit breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Resilience4j retry](https://resilience4j.readme.io/docs/retry)
- [Resilience4j bulkhead](https://resilience4j.readme.io/docs/bulkhead)
- [Resilience4j rate limiter](https://resilience4j.readme.io/docs/ratelimiter)
- [Resilience4j time limiter](https://resilience4j.readme.io/docs/timeout)
- [Spring Boot graceful shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html)
- [Spring Framework REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)

---

## Extensions — after the senior core

- Adaptive concurrency limits and queueing-theory capacity models.
- Hedged requests for carefully selected idempotent reads.
- Service-mesh/gateway resilience ownership and avoiding duplicated retries.
- Distributed rate limiting, fairness algorithms and quota accounting.
- Chaos engineering, fault injection and steady-state hypotheses.
- Multi-region failover, brownout modes and dependency-aware routing.
- Reactive retry/backpressure/context behavior in WebFlux/Reactor.
- Spring Cloud CircuitBreaker abstraction and provider portability.

---

## How to drill this kit

1. Draw §0 and allocate a concrete 800 ms request budget without looking.
2. For each retry scenario, say **failure class → repeat safety → delay → stop
   condition → final contract**.
3. Build one stub dependency that can delay, reset, return 429/503 and commit
   before dropping the response. Prove side-effect count under retries.
4. Saturate one dependency and show unrelated traffic stays within its SLO.
5. Send SIGTERM under load and verify admission, in-flight completion, forced
   replay and duplicate prevention.
6. Re-run §§2, 4, 6, 10 and 11 blind after a week.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 failure-containment model | ⬜ | ⬜ | ⬜ |
| §1 deadlines/timeouts | ⬜ | ⬜ | ⬜ |
| §2 retry/idempotency | ⬜ | ⬜ | ⬜ |
| §3 circuit breaker | ⬜ | ⬜ | ⬜ |
| §4 bulkheads/saturation | ⬜ | ⬜ | ⬜ |
| §5 rate/backpressure/shedding | ⬜ | ⬜ | ⬜ |
| §6 composition/proxies | ⬜ | ⬜ | ⬜ |
| §7 degradation/fallbacks | ⬜ | ⬜ | ⬜ |
| §8 graceful shutdown | ⬜ | ⬜ | ⬜ |
| §9 testing/telemetry | ⬜ | ⬜ | ⬜ |
| §10 production scenarios | ⬜ | ⬜ | ⬜ |
| §11 trap wall | ⬜ | ⬜ | ⬜ |
