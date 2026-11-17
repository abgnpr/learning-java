# Java Backend / Platform Ecosystem Map

This is the interview-facing technology map for a backend or platform
engineer with roughly five years of experience. It is deliberately broad,
but it is **not a mandate to learn every product**. The senior signal is
knowing the fundamentals deeply, operating one coherent stack, and explaining
why an alternative would be better under different constraints.

## How to read it

- **Deep** — build with it, debug it, explain internals and trade-offs.
- **Working** — use it competently and know its common failure modes.
- **Awareness** — recognize the problem it solves and compare it at a high
  level; deepen only when a job or system needs it.
- A slash-separated family usually means **choose one deeply; recognize the
  rest**. Nobody credible claims production depth in every item below.
- Learn concepts before brands: transactions before Hibernate annotations,
  messaging semantics before Kafka settings, and telemetry before dashboards.

Current baseline: be comfortable maintaining Java 8/11 estates, strong in
Java 17/21, and aware that Java 25 is an LTS release. Do not chase every
six-month JDK release. Spring Boot 4.x has a Java 17 minimum, so actual target
versions remain an organizational compatibility decision.

## 1. Language, JDK and JVM

- **Java SE / JDK** — Deep
  What/why: The language, standard library, compiler and core tools; it is the foundation beneath every Java backend framework.

- **JVM** — Deep
  What/why: The managed runtime that executes bytecode and provides memory management, threading, class loading and portability.

- **OpenJDK** — Working
  What/why: The open-source reference implementation of Java SE and the upstream for most production JDK distributions.

- **Eclipse Temurin / Amazon Corretto / Azul Zulu / Oracle JDK** — Awareness
  What/why: Supported OpenJDK distributions; organizations choose among them for licensing, patch cadence, support and platform certification.

- **HotSpot JVM** — Working
  What/why: The dominant JVM implementation; understanding its JIT, heap and diagnostics helps explain production latency and memory behavior.

- **GraalVM** — Awareness
  What/why: A high-performance JDK and native-image toolchain used when fast startup, smaller runtime footprints or polyglot execution justify its build-time constraints.

- **Java bytecode and `javap`** — Working
  What/why: Bytecode is the JVM instruction format; inspecting it demystifies compilation, lambdas, proxies and compatibility issues.

- **Java Platform Module System (JPMS)** — Awareness
  What/why: The `module-info.java` system adds strong module boundaries and reliable configuration, mainly useful for large modular applications and JDK internals.

- **Generics** — Deep
  What/why: Compile-time type parameterization prevents unsafe casts and enables reusable APIs; type erasure and variance explain many framework edge cases.

- **Collections Framework** — Deep
  What/why: Lists, sets, maps, queues and their concurrent variants are the basic data structures behind application behavior and performance.

- **Streams API and Collectors** — Deep
  What/why: Declarative collection processing improves composition, but requires judgment about readability, allocation and parallel execution.

- **Records, sealed classes and pattern matching** — Deep
  What/why: Modern language features express immutable data and constrained domain models with less boilerplate and safer branching.

- **`Optional`** — Deep
  What/why: An explicit return type for optional values that reduces accidental null handling when used at API boundaries rather than everywhere.

- **Java Time API (`java.time`)** — Deep
  What/why: Correct date, time-zone, duration and instant handling prevents subtle financial, scheduling and serialization defects.

- **NIO.2** — Working
  What/why: Modern file, buffer and non-blocking I/O APIs support scalable network stacks and robust filesystem operations.

- **Reflection, annotations and annotation processing** — Working
  What/why: Frameworks use them for dependency injection, mapping and code generation; knowing the cost and lifecycle makes framework magic debuggable.

- **Class loaders** — Working
  What/why: They define type identity and dynamic loading; they matter in containers, plugins, instrumentation and memory-leak diagnosis.

- **Java serialization** — Awareness
  What/why: Native object serialization persists object graphs but is fragile and security-prone; recognize legacy usage and prefer explicit wire formats.

- **Java Native Interface (JNI) / Foreign Function and Memory API** — Awareness
  What/why: They connect Java to native code or memory when a library or performance constraint crosses the managed-runtime boundary.

- **Virtual threads (Project Loom)** — Working
  What/why: Lightweight JVM-managed threads make high-concurrency blocking I/O code simpler, while leaving CPU, pinning and downstream-capacity limits intact.

- **`CompletableFuture` and structured concurrency** — Working
  What/why: They coordinate asynchronous or concurrent work, cancellation and failure propagation when ordinary request-thread flow is insufficient.

- **Java Memory Model** — Deep
  What/why: Happens-before, visibility and atomicity rules determine whether concurrent Java code is actually correct.

- **`java.util.concurrent`** — Deep
  What/why: Executors, locks, atomics, concurrent collections, semaphores and synchronizers are the standard toolkit for bounded safe concurrency.

## 2. Runtime diagnostics and performance

- **Garbage collectors: G1, ZGC and Shenandoah** — Working
  What/why: Different collectors trade throughput, pause time, heap size and operational maturity; a senior engineer reads evidence before tuning one.

- **Java Flight Recorder (JFR)** — Working
  What/why: Low-overhead event recording captures allocation, locks, CPU, I/O and GC evidence from live JVMs.

- **Java Mission Control (JMC)** — Working
  What/why: The analysis UI for flight recordings turns JVM events into profiles and production-diagnostic findings.

- **`jcmd`** — Working
  What/why: The preferred multipurpose command-line interface for requesting heap, thread, class and JFR diagnostics from a running JVM.

- **`jstack`, `jmap` and `jstat`** — Working
  What/why: Classic JDK diagnostic tools expose thread dumps, heap information and GC statistics in older or constrained environments.

- **Eclipse Memory Analyzer (MAT)** — Working
  What/why: Heap-dump analysis finds dominators, retained memory and leak suspects after out-of-memory or growth incidents.

- **async-profiler** — Working
  What/why: A low-overhead CPU, allocation and lock profiler produces flame graphs without many safepoint biases of older profilers.

- **Java Microbenchmark Harness (JMH)** — Working
  What/why: The OpenJDK benchmark harness controls warm-up, dead-code elimination and JIT effects so microbenchmarks are less misleading.

- **VisualVM** — Awareness
  What/why: A convenient local JVM monitor and profiler for quick inspection, though production analysis usually needs lower-overhead tooling.

## 3. Build, dependency and developer tooling

- **Maven** — Deep
  What/why: Convention-driven build and dependency management remains the default in many enterprise Java estates.

- **Gradle** — Working
  What/why: A programmable, incremental build system suits complex multi-project builds and Android/JVM polyglot work.

- **Maven Wrapper / Gradle Wrapper** — Deep
  What/why: Project-pinned build launchers make developer and CI builds repeatable without relying on a machine-wide tool version.

- **Maven Central** — Working
  What/why: The primary public artifact repository for JVM libraries; dependency provenance starts here.

- **Nexus Repository / JFrog Artifactory** — Working
  What/why: Private artifact proxies and registries control caching, internal packages, promotion and supply-chain policy.

- **Bill of Materials (BOM)** — Working
  What/why: A coordinated dependency-version set prevents incompatible transitive libraries across a platform or multi-module application.

- **Maven Enforcer Plugin** — Working
  What/why: Build-time rules reject unsupported JDKs, duplicate classes and dependency-policy violations before they become environment-specific defects.

- **OpenRewrite** — Awareness
  What/why: Automated source recipes perform repeatable framework, JDK and API migrations across large Java codebases.

- **SDKMAN! / asdf** — Awareness
  What/why: Local toolchain managers switch JDK and JVM-tool versions reproducibly across projects.

- **IntelliJ IDEA / Eclipse IDE / VS Code Java** — Working
  What/why: IDEs provide refactoring, navigation, debugging and build integration; fluency reduces mechanical work but must not hide runtime behavior.

- **Git** — Deep
  What/why: Distributed source control underpins review, collaboration, release history and safe change management.

- **Javadoc** — Working
  What/why: The standard API documentation format communicates public contracts and publishes browsable reference material.

- **Lombok** — Awareness
  What/why: Compile-time annotation processing removes boilerplate, but generated behavior, IDE support and domain-model misuse require caution.

- **MapStruct** — Working
  What/why: Compile-time bean mapping produces explicit fast DTO/entity adapters without reflection-heavy runtime mapping.

## 4. Core enterprise specifications and runtimes

- **Jakarta EE** — Working
  What/why: The vendor-neutral enterprise Java specification family standardizes dependency injection, persistence, transactions, validation, REST, messaging and more.

- **Jakarta Servlet** — Deep
  What/why: The request/response and filter contract beneath traditional Java web stacks explains sessions, dispatch and the servlet thread model.

- **Jakarta Contexts and Dependency Injection (CDI)** — Working
  What/why: A standard dependency-injection and lifecycle model used by Jakarta runtimes and cloud-native Java frameworks.

- **Jakarta Persistence (JPA)** — Deep
  What/why: The ORM specification defines entity mapping, persistence contexts and queries; correct use depends on understanding SQL and transactions beneath it.

- **Jakarta Transactions (JTA)** — Working
  What/why: Standard transaction demarcation coordinates local or distributed resource work where a single JDBC transaction is insufficient.

- **Jakarta Bean Validation** — Deep
  What/why: Declarative constraint validation keeps boundary and domain rules consistent across HTTP, messaging and persistence layers.

- **Jakarta RESTful Web Services (JAX-RS)** — Working
  What/why: The standard annotation API for REST endpoints underpins Jakarta-oriented frameworks such as Quarkus and Jersey.

- **Jakarta Messaging (JMS)** — Working
  What/why: A portable Java API for queues and topics remains common in enterprise brokers and legacy integration estates.

- **Jakarta Batch** — Awareness
  What/why: A standard job, step, checkpoint and restart model exists for durable bulk processing in Jakarta runtimes.

- **Eclipse MicroProfile** — Awareness
  What/why: Complementary specifications add configuration, health, fault tolerance, metrics and REST-client conventions for Jakarta microservices.

- **Apache Tomcat** — Deep
  What/why: The dominant servlet container hosts Spring MVC and Jakarta web applications and makes connector, thread-pool and deployment behavior concrete.

- **Eclipse Jetty / Undertow** — Awareness
  What/why: Alternative embeddable HTTP and servlet runtimes provide different performance, footprint and integration trade-offs.

- **WildFly / JBoss EAP, Payara / GlassFish and Open Liberty** — Awareness
  What/why: Full Jakarta EE application servers supply standardized enterprise services and remain important in regulated and long-lived estates.

## 5. Spring and alternative application frameworks

- **Spring Framework** — Deep
  What/why: The core container supplies dependency injection, lifecycle, events, resource access, transactions, AOP and web foundations.

- **Spring Boot** — Deep
  What/why: Opinionated auto-configuration, starters and embedded runtime conventions make production Spring services fast to assemble and standardize.

- **Spring Modulith** — Working
  What/why: Module boundaries, verification and module-level events help a Spring Boot application remain a disciplined modular monolith before services are split.

- **Spring MVC** — Deep
  What/why: The servlet-based web framework handles controllers, binding, validation, filters, exceptions and synchronous HTTP APIs.

- **Spring WebFlux / Project Reactor** — Working
  What/why: Reactive streams and non-blocking I/O support high-concurrency pipelines, but add complexity and help only when the full call path benefits.

- **Reactive Streams** — Working
  What/why: The JVM interoperability specification standardizes asynchronous streams with non-blocking backpressure across Reactor, RxJava and other libraries.

- **Spring Data JPA** — Deep
  What/why: Repository abstractions reduce routine persistence code while still requiring control of fetching, queries, locking and transaction boundaries.

- **Spring Data JDBC** — Working
  What/why: A simpler aggregate-oriented persistence model avoids much ORM state and lazy-loading behavior when direct relational mapping is preferable.

- **Spring JDBC (`JdbcTemplate`)** — Deep
  What/why: Thin JDBC helpers preserve explicit SQL and transaction control without repetitive connection and exception boilerplate.

- **Spring Actuator** — Deep
  What/why: Standard health, readiness, metrics, configuration and diagnostic endpoints make services operable.

- **Spring Batch** — Working
  What/why: Chunking, jobs, steps, checkpoints, retries and restart metadata solve durable high-volume batch processing.

- **Spring Integration** — Awareness
  What/why: Enterprise integration patterns connect channels, adapters, routers and transformers when integration flow complexity warrants a framework.

- **Spring Cloud** — Working
  What/why: A family of distributed-system integrations covers configuration, clients, gateways and cloud platform concerns; use only the modules the platform still needs.

- **Spring AI** — Awareness
  What/why: Spring abstractions for models, tools, embeddings, vector stores and retrieval support enterprise AI integration without replacing core AI-system design.

- **Spring Cloud Gateway** — Working
  What/why: A programmable reactive gateway centralizes routing and cross-cutting edge policies for Spring-centric systems.

- **Spring for Apache Kafka** — Deep
  What/why: Spring’s Kafka templates, listener containers, transactions and error handlers integrate Kafka safely with application lifecycles.

- **Spring AMQP** — Awareness
  What/why: Spring abstractions for RabbitMQ-style messaging simplify templates, listeners, conversion and retry behavior.

- **Spring Boot Test** — Deep
  What/why: Test slices, context bootstrapping and test utilities support focused unit, integration and application tests.

- **Spring REST Docs** — Working
  What/why: Test-generated request and response snippets keep human-written API documentation tied to verified behavior.

- **Quarkus** — Awareness
  What/why: A build-time optimized Kubernetes-oriented Java framework targets fast startup, lower memory and native-image use cases.

- **Micronaut** — Awareness
  What/why: Compile-time dependency injection and AOP reduce reflection and startup overhead for microservices and serverless applications.

- **Vert.x** — Awareness
  What/why: An event-loop toolkit supports asynchronous polyglot services when explicit reactive architecture is desired.

- **Helidon** — Awareness
  What/why: A lightweight Java microservices framework offers imperative and reactive styles with MicroProfile support.

## 6. HTTP, APIs and integration

- **HTTP/1.1, HTTP/2 and HTTP/3** — Deep
  What/why: Backend behavior rests on connection reuse, multiplexing, headers, caching, timeouts and intermediaries, regardless of framework.

- **REST** — Deep
  What/why: Resource-oriented HTTP constraints guide stable APIs, method semantics, status codes, caching and idempotency.

- **OpenAPI / Swagger UI** — Deep
  What/why: Machine-readable HTTP contracts enable documentation, validation, client generation and contract-first collaboration.

- **JSON and Jackson** — Deep
  What/why: JSON is the common web payload and Jackson is Java’s dominant mapper; configuration affects compatibility, security and data correctness.

- **XML, JAXB and Jackson XML** — Working
  What/why: XML remains common in banking and enterprise integration; binding tools map schema-shaped messages into explicit Java models.

- **SOAP and WSDL** — Awareness
  What/why: Contract-heavy XML web services persist in regulated and legacy estates where schemas, formal operations and enterprise tooling matter.

- **gRPC and Protocol Buffers** — Working
  What/why: Strongly typed binary RPC with code generation and streaming fits low-latency internal service communication.

- **GraphQL and GraphQL Java** — Awareness
  What/why: Client-selected query graphs can reduce endpoint proliferation, while shifting complexity into schema governance, authorization and query cost control.

- **WebSocket** — Working
  What/why: A persistent bidirectional connection supports interactive real-time flows that request/response HTTP cannot serve efficiently.

- **Server-Sent Events (SSE)** — Working
  What/why: A simple one-way HTTP event stream fits notifications and progress updates without full bidirectional sockets.

- **Java HTTP Client / Apache HttpClient / OkHttp** — Working
  What/why: Outbound HTTP clients need explicit connection pools, timeouts, TLS, retries and observability rather than default settings.

- **Spring `RestClient` / `WebClient`** — Deep
  What/why: Spring’s synchronous and reactive clients provide typed integration hooks, filters and codec support for downstream calls.

- **OpenFeign** — Awareness
  What/why: Declarative HTTP interfaces reduce client boilerplate but can conceal network calls and resilience behavior if poorly governed.

- **Apache Camel** — Awareness
  What/why: A large catalog of connectors and enterprise integration patterns helps route and transform data across heterogeneous systems.

- **Netty** — Working
  What/why: An asynchronous network framework powers many Java servers and clients; event-loop awareness is vital when debugging blocking or channel behavior.

- **JSON Schema / Avro / Protobuf** — Working
  What/why: Explicit payload schemas enable validation and compatibility rules across independently deployed producers and consumers.

- **Pact** — Working
  What/why: Consumer-driven contracts catch HTTP or message incompatibility before independently deployed services meet in a shared environment.

- **Spring Cloud Contract** — Awareness
  What/why: Spring’s older producer contract and stub-generation project appears in existing estates; verify its current lifecycle before choosing it for new work.

## 7. Relational data and persistence

- **JDBC** — Deep
  What/why: The standard SQL interface exposes connections, statements, result sets and transaction mechanics beneath every Java persistence abstraction.

- **Hibernate ORM** — Deep
  What/why: The dominant JPA implementation maps object state to relational data; fetching, dirty checking, caching and flush behavior must be understood.

- **R2DBC** — Awareness
  What/why: Reactive relational drivers avoid blocking JDBC calls in an end-to-end reactive stack, while giving up parts of the familiar JDBC/JPA model.

- **jOOQ** — Working
  What/why: Generated type-safe SQL keeps relational power visible and is strong for query-heavy systems where ORM abstractions fight the schema.

- **MyBatis** — Awareness
  What/why: Explicit SQL mapping offers more query control than ORM with less raw JDBC plumbing.

- **HikariCP** — Deep
  What/why: A fast JDBC connection pool protects scarce database connections; pool size, leak detection and timeouts are operational controls.

- **Flyway / Liquibase** — Deep
  What/why: Versioned database migrations make schema evolution repeatable, reviewable and deployable with application changes.

- **PostgreSQL** — Deep
  What/why: A feature-rich open-source relational database is ideal for learning indexes, plans, isolation, locking, replication and JSON alongside SQL.

- **MySQL** — Working
  What/why: A widely deployed relational database has distinct storage-engine, isolation and replication behavior worth recognizing.

- **Oracle Database** — Working
  What/why: A major enterprise and banking database brings PL/SQL, RAC, mature tooling and vendor-specific optimizer behavior.

- **Microsoft SQL Server** — Awareness
  What/why: A common enterprise relational platform matters when integrating with Microsoft-heavy estates and T-SQL systems.

- **H2 Database** — Awareness
  What/why: An embeddable database is convenient for narrow local tests, but its SQL and transaction behavior must not substitute for production-database integration tests.

- **Testcontainers databases** — Deep
  What/why: Disposable real database instances make SQL, migration and transaction tests more faithful than mocks or incompatible in-memory substitutes.

## 8. Caches, search and non-relational stores

- **Redis** — Deep
  What/why: An in-memory data platform supports cache, rate limits, locks, counters and streams, but eviction, persistence and failure semantics matter.

- **Lettuce / Jedis / Redisson** — Working
  What/why: Java Redis clients expose synchronous, reactive and higher-level distributed primitives with different threading and connection models.

- **Caffeine** — Working
  What/why: A high-performance in-process Java cache reduces local repeated work without the network and consistency cost of a distributed cache.

- **Hazelcast / Infinispan** — Awareness
  What/why: Distributed in-memory data grids offer clustered caches and compute when local cache or Redis semantics do not fit.

- **MongoDB** — Working
  What/why: A document database favors flexible aggregate-shaped data, while demanding deliberate indexing, schema evolution and transaction choices.

- **Apache Cassandra / ScyllaDB** — Awareness
  What/why: Wide-column distributed stores trade query flexibility and strong consistency for horizontal write scale and availability.

- **Elasticsearch / OpenSearch** — Working
  What/why: Distributed inverted indexes power full-text search and aggregations; they complement rather than replace a system-of-record database.

- **Apache Lucene** — Awareness
  What/why: The Java search library underneath Elasticsearch/OpenSearch explains analyzers, segments, scoring and indexing fundamentals.

- **Amazon DynamoDB / Google Cloud Bigtable** — Awareness
  What/why: Managed key-value or wide-column stores provide elastic scale with access-pattern-first modeling and cloud-specific constraints.

## 9. Messaging, streaming and durable work

- **Apache Kafka** — Deep
  What/why: A partitioned replicated event log supports durable streams, replay and decoupled services; ordering, offsets, rebalancing and retention are core knowledge.

- **Kafka Streams** — Working
  What/why: An embeddable stream-processing library adds state stores, joins, windows and exactly-once processing to Kafka applications.

- **Kafka Connect** — Working
  What/why: Standardized source and sink connectors move data between Kafka and external systems without bespoke integration services.

- **Confluent Schema Registry / Apicurio Registry** — Working
  What/why: Central schema storage and compatibility enforcement keep event contracts evolvable across teams.

- **RabbitMQ** — Working
  What/why: A message broker with rich exchanges and routing is strong for work queues and messaging patterns that do not need a retained event log.

- **Apache Pulsar** — Awareness
  What/why: A distributed messaging and streaming system separates compute from storage and supports queue and retained-stream workloads with multi-tenancy.

- **ActiveMQ Artemis / IBM MQ** — Awareness
  What/why: Enterprise brokers provide JMS, durable queues and operational integration common in banking and legacy middleware.

- **Amazon SQS / SNS and Google Pub/Sub** — Working
  What/why: Managed cloud messaging removes broker operations while preserving the need to design for duplicate delivery, ordering limits and visibility leases.

- **Debezium** — Working
  What/why: Change-data-capture connectors turn database logs into events for replication, integration and outbox pipelines.

- **Apache Flink** — Awareness
  What/why: A distributed stateful stream processor handles event time, windows and large-scale continuous computation beyond ordinary consumers.

- **Quartz Scheduler** — Working
  What/why: Persistent clustered job scheduling handles calendars and triggers inside Java systems when simple timers are insufficient.

- **Temporal** — Awareness
  What/why: Durable workflow orchestration persists long-running business state and retries so code can survive worker and process failure.

- **Camunda / Flowable** — Awareness
  What/why: BPMN workflow engines make human and service processes visible, versioned and operable when business flow needs explicit modeling.

## 10. Testing, verification and code quality

- **JUnit 5** — Deep
  What/why: The standard Java test platform provides lifecycle, assertions, parameterized tests and an extension model.

- **AssertJ / Hamcrest** — Working
  What/why: Fluent, expressive assertions make failure intent clearer than large blocks of manual checks.

- **Mockito** — Deep
  What/why: Test doubles isolate collaboration behavior, though over-mocking implementation details creates brittle tests.

- **Testcontainers** — Deep
  What/why: Real disposable databases, brokers and services make integration tests portable and production-like.

- **WireMock / MockServer** — Working
  What/why: Programmable HTTP stubs test downstream errors, timeouts and contracts without depending on live services.

- **Awaitility** — Working
  What/why: Eventual assertions test asynchronous outcomes without arbitrary sleeps.

- **REST Assured** — Working
  What/why: A Java DSL exercises HTTP APIs end to end with readable requests and assertions.

- **ArchUnit** — Working
  What/why: Executable architecture rules prevent forbidden package dependencies and layering drift.

- **Toxiproxy** — Awareness
  What/why: A network fault proxy injects latency, disconnects and bandwidth limits for resilience tests.

- **Gatling / k6 / JMeter** — Working
  What/why: Load generators expose latency distributions, capacity limits and saturation behavior before production traffic does.

- **JaCoCo** — Working
  What/why: Coverage reports reveal unexecuted code, but coverage is a test-gap signal rather than a quality score.

- **PIT Mutation Testing** — Awareness
  What/why: Mutating production code checks whether tests detect behavioral faults instead of merely executing lines.

- **SpotBugs / Error Prone** — Working
  What/why: Static analysis detects bug patterns and unsafe code before runtime.

- **Checkstyle / PMD** — Working
  What/why: Automated style and maintainability rules keep a large codebase consistent without review arguments.

- **SonarQube / SonarCloud** — Working
  What/why: Centralized quality and security analysis tracks issues and policy gates across repositories.

- **OWASP Dependency-Check / Snyk / Dependabot / Renovate** — Working
  What/why: Dependency scanning and update automation reduce exposure to known vulnerable or stale third-party components.

## 11. Resilience and operability

- **Resilience4j** — Deep
  What/why: Circuit breakers, retries, rate limiters, bulkheads and time limits constrain failure propagation around remote calls.

- **Failsafe** — Awareness
  What/why: A lightweight policy library composes retries, breakers, fallbacks and hedging outside Spring-specific abstractions.

- **Micrometer** — Deep
  What/why: A vendor-neutral instrumentation facade emits application metrics to Prometheus and other monitoring backends.

- **OpenTelemetry** — Deep
  What/why: Standard APIs, SDKs and protocols correlate traces, metrics and logs across services without hard-wiring one observability vendor.

- **SLF4J** — Deep
  What/why: A logging facade decouples application code from the concrete logging implementation.

- **Logback / Log4j 2** — Working
  What/why: Logging implementations control structured output, filtering, appenders and async behavior; unsafe or excessive logging can harm systems.

- **Prometheus** — Working
  What/why: A pull-based time-series monitoring system stores labeled metrics and powers alert rules.

- **Grafana** — Working
  What/why: Dashboards and alerting visualize service-level indicators, resource saturation and business health.

- **Grafana Loki / Elastic Stack / OpenSearch Stack / Splunk** — Working
  What/why: Central log platforms make structured logs searchable across instances during incidents and audits.

- **Jaeger / Grafana Tempo** — Working
  What/why: Distributed trace backends reveal request paths, latency and failure across service boundaries.

- **Datadog / New Relic / Dynatrace / AppDynamics** — Awareness
  What/why: Commercial observability and APM suites combine telemetry, agents, dashboards and incident workflows; depth in one transfers to the others.

- **PagerDuty / Opsgenie** — Awareness
  What/why: Alert routing and on-call workflow systems turn actionable monitoring signals into owned incident response.

## 12. Security and identity

- **Spring Security** — Deep
  What/why: The dominant Spring security framework implements filter chains, method authorization, OAuth clients and resource servers.

- **OAuth 2.0** — Deep
  What/why: Delegated authorization grants scoped access without sharing a user’s credentials with every client.

- **OpenID Connect (OIDC)** — Deep
  What/why: An identity layer over OAuth standardizes login, identity claims, discovery and session flows.

- **JSON Web Token (JWT) / JSON Web Key Set (JWKS)** — Deep
  What/why: Signed claims and published keys support portable token verification, with expiry, rotation and revocation trade-offs.

- **Keycloak / Okta / Auth0 / cloud identity providers** — Working
  What/why: Identity providers centralize login, federation, tokens, roles and policy instead of every service implementing identity.

- **TLS and mutual TLS** — Deep
  What/why: Transport encryption authenticates servers—and sometimes clients—while protecting data in motion.

- **Java Cryptography Architecture (JCA/JCE)** — Working
  What/why: Standard providers expose hashing, signatures, encryption and secure randomness; high-level safe constructions should be preferred over custom crypto.

- **Java KeyStore / `keytool`** — Working
  What/why: JVM trust and key stores hold certificates and private keys used for TLS and signing.

- **HashiCorp Vault / cloud secret managers** — Working
  What/why: Central secret systems provide access control, rotation and audit instead of credentials in code or static configuration.

- **OWASP ASVS / OWASP Top 10** — Working
  What/why: Security verification standards and common-risk catalogs guide threat modeling, testing and secure review.

- **OWASP ZAP / Burp Suite** — Awareness
  What/why: Dynamic web security tools probe a running application for input, session and configuration weaknesses.

- **Trivy / Grype** — Working
  What/why: Container and filesystem scanners detect known vulnerabilities and insecure artifacts before deployment.

- **Sigstore Cosign / software bill of materials (SBOM)** — Awareness
  What/why: Signing and component inventories improve artifact provenance, verification and incident response in the software supply chain.

## 13. Containers, orchestration and delivery

- **Linux** — Deep
  What/why: Most Java services run on Linux; processes, files, sockets, signals, limits and resource evidence are essential operational knowledge.

- **Bash, `curl`, `jq`, `openssl`, `dig`, `ss` and `lsof`** — Working
  What/why: Small command-line tools inspect HTTP, JSON, TLS, DNS, sockets and files when production diagnosis cannot wait for an IDE.

- **Docker / Podman** — Deep
  What/why: OCI containers package application processes and dependencies into reproducible deployable images.

- **OCI images and registries** — Working
  What/why: Standard image formats and registries enable layered storage, scanning, signing and promotion between environments.

- **Buildpacks / Jib** — Working
  What/why: Java-aware image builders create reproducible layered containers without hand-maintained Dockerfiles for every service.

- **Kubernetes** — Working
  What/why: Declarative scheduling, service discovery, rollout and self-healing operate containerized services across a cluster.

- **OpenShift** — Awareness
  What/why: Red Hat’s enterprise Kubernetes platform adds integrated security, build, registry and operational conventions common in large organizations.

- **Helm / Kustomize** — Working
  What/why: Packaging and overlay tools manage environment-specific Kubernetes manifests without copying entire configurations.

- **NGINX / HAProxy / Envoy** — Working
  What/why: Reverse proxies and load balancers terminate connections, route traffic and enforce edge or service-mesh policies.

- **Istio / Linkerd** — Awareness
  What/why: Service meshes move mTLS, traffic policy and telemetry into infrastructure, at the cost of another distributed layer to operate.

- **Jenkins** — Working
  What/why: A highly extensible automation server remains widespread for enterprise build, test and deployment pipelines.

- **GitHub Actions / GitLab CI / Azure Pipelines** — Working
  What/why: Repository-integrated CI systems automate verification, packaging and deployment on every change.

- **Argo CD / Flux** — Working
  What/why: GitOps controllers reconcile cluster state from version-controlled declarations and make deployment drift visible.

- **Argo Rollouts / Flagger** — Awareness
  What/why: Progressive-delivery controllers automate canaries, blue-green releases and metric-based rollback.

- **Terraform / OpenTofu** — Working
  What/why: Declarative infrastructure as code makes cloud resources reviewable, repeatable and recoverable.

- **Ansible** — Awareness
  What/why: Agentless configuration automation remains useful for servers and middleware outside fully containerized platforms.

## 14. Cloud building blocks

- **AWS / Azure / Google Cloud** — Working in one, Awareness in the others
  What/why: Cloud platforms supply managed compute, networking, identity, data, messaging and observability; concepts transfer but failure modes and APIs differ.

- **Virtual machines: EC2 / Azure VMs / Compute Engine** — Working
  What/why: VMs remain the basic isolation and capacity unit for legacy services, custom middleware and controlled runtime environments.

- **Managed containers: EKS/ECS/Fargate, AKS and GKE/Cloud Run** — Working
  What/why: Managed schedulers reduce control-plane work while retaining application responsibility for health, scaling and resource limits.

- **Serverless functions: Lambda / Azure Functions / Cloud Functions** — Awareness
  What/why: Event-driven short-lived compute removes server management but imposes startup, duration, concurrency and integration constraints.

- **Object storage: S3 / Azure Blob Storage / Cloud Storage** — Working
  What/why: Durable inexpensive object stores hold documents, exports, backups and event payloads outside transactional databases.

- **Managed relational databases: RDS/Aurora / Azure SQL / Cloud SQL** — Working
  What/why: Managed databases offload routine infrastructure while schema, queries, capacity, failover and recovery remain engineering responsibilities.

- **Managed caches: ElastiCache / Azure Cache for Redis / Memorystore** — Awareness
  What/why: Cloud-managed Redis reduces operations but does not remove cache consistency, eviction and failover design.

- **Cloud queues and events: SQS/SNS/EventBridge, Service Bus/Event Grid and Pub/Sub** — Working
  What/why: Managed asynchronous primitives decouple services and absorb bursts while imposing provider-specific delivery guarantees.

- **Cloud identity: IAM / Entra ID / Cloud IAM** — Deep in the chosen cloud
  What/why: Identity and least-privilege policy control every workload and operator action and are a primary security boundary.

- **Cloud monitoring: CloudWatch / Azure Monitor / Cloud Monitoring** — Working
  What/why: Provider-native logs, metrics and audit trails are the first evidence source for managed infrastructure incidents.

## 15. Architecture-supporting standards and adjacent JVM tools

- **C4 Model / PlantUML / Mermaid** — Working
  What/why: Lightweight architecture diagrams communicate system context, containers, components and important runtime sequences.

- **Architecture Decision Records (ADRs)** — Deep
  What/why: Short version-controlled decisions preserve context, alternatives and consequences behind important technical choices.

- **Backstage** — Awareness
  What/why: A developer portal catalogs services, ownership, documentation and golden-path templates for an internal platform.

- **OpenFeature** — Awareness
  What/why: A vendor-neutral feature-flag API decouples application code from a specific flag provider.

- **Unleash / LaunchDarkly** — Awareness
  What/why: Feature-management systems separate deployment from release and support gradual exposure and emergency disablement.

- **Kotlin** — Awareness
  What/why: A concise null-safe JVM language interoperates with Java and is common in Android and some Spring backends.

- **Scala** — Awareness
  What/why: A functional/object-oriented JVM language underpins ecosystems such as Apache Spark and Akka/Pekko.

- **Groovy** — Awareness
  What/why: A dynamic JVM language appears in Gradle scripts, Jenkins pipelines, Spock tests and legacy applications.

- **Spock** — Awareness
  What/why: A Groovy-based specification testing framework offers expressive data-driven tests for Java and Groovy systems.

- **Akka / Apache Pekko** — Awareness
  What/why: Actor and stream toolkits model concurrent distributed systems, but introduce a specialized programming and operational model.

## Five project options worth building by hand

Do **one flagship deeply**, then add one or two narrower projects. Five shallow
repositories look weaker than one system whose failures, measurements and
trade-offs can survive a 45-minute interrogation. “By hand” means writing the
domain model, state transitions, concurrency control, failure handling,
instrumentation and tests yourself. It does **not** mean writing your own
database, broker, cryptography or Kubernetes scheduler.

### 1. PayRail — idempotent payment orchestration and double-entry ledger

**Best single flagship for a backend engineer.** It looks like a real system,
has hard correctness constraints, and naturally opens conversations about
transactions, messaging and failure.

**Key tech:** Java, Spring Boot, Spring MVC, Spring Data JPA, PostgreSQL,
HikariCP, Flyway, Apache Kafka, Spring for Apache Kafka, gRPC, Protocol Buffers,
Spring Security, OAuth 2.0, OpenID Connect, Resilience4j, OpenTelemetry,
Micrometer, Prometheus, Grafana, JUnit 5, Testcontainers, Docker

Build:

- A payment intent API with a state machine: created, authorized, captured,
  failed, reversed and refunded.
- Idempotency keys with request fingerprinting, concurrent-request handling and
  stored responses—not a map in memory.
- A double-entry ledger whose postings always balance and use integer minor
  units or decimal money, never floating point.
- PostgreSQL transactions, optimistic and pessimistic locking experiments,
  unique constraints and isolation-level tests.
- A transactional outbox, Kafka publisher, idempotent consumer, retry policy,
  dead-letter handling and replay tool.
- A fake bank connector with timeouts, uncertain outcomes, duplicate callbacks
  and reconciliation against a settlement file.
- REST externally, one gRPC internal path, OAuth/OIDC, audit events, metrics,
  traces and structured logs.

Prove it:

- Property tests for conservation of money and failure tests for crash points
  between database commit, event publication and callback processing.
- A load report with throughput, p50/p95/p99 latency, database-pool saturation
  and one optimization justified by evidence.
- ADRs explaining the state machine, outbox, consistency model and why this is
  not “exactly once.”

Avoid: a CRUD wallet with a Kafka dependency but no duplicate, concurrency,
reconciliation or crash tests.

### 2. Chronos — durable distributed job scheduler and worker platform

**Best project for concurrency and platform depth.** It proves that threads,
queues and distributed ownership are more than interview vocabulary.

**Key tech:** Java, Virtual Threads, `java.util.concurrent`, Spring Boot,
Spring MVC, Server-Sent Events, Spring Data JDBC, PostgreSQL, HikariCP, Flyway,
Apache Kafka, Micrometer, OpenTelemetry, Prometheus, Grafana, JUnit 5,
Awaitility, Testcontainers, Toxiproxy, Docker, Kubernetes

Build:

- APIs to submit, schedule, cancel and inspect jobs; persist definitions and
  executions in PostgreSQL.
- Multiple workers claiming jobs through leases, heartbeats and fencing tokens
  so a paused or partitioned worker cannot complete stale work.
- Bounded queues, worker pools or virtual threads, priorities, fair tenancy,
  backpressure and graceful shutdown.
- At-least-once execution with idempotency hooks, exponential backoff, jitter,
  retry budgets, dead-letter state and manual replay.
- Leader election only for scheduling decisions; keep execution horizontally
  scalable and show what happens when the leader dies.
- Progress via SSE, Micrometer metrics, OpenTelemetry traces and an operator
  endpoint for stuck jobs.

Prove it:

- Deterministic clock-based tests, concurrent claim tests and process-kill tests
  showing lease recovery without double finalization.
- A saturation experiment that demonstrates bounded memory and predictable
  rejection or delay rather than an unbounded queue.
- An ADR comparing database polling, Kafka and a workflow engine such as
  Temporal, including when to buy rather than build.

Avoid: a single-node `ScheduledExecutorService` wrapper presented as a
distributed scheduler.

Optional infrastructure-depth extension: build a separate tiny WAL-backed
queue or key-value component with NIO.2, checksummed records, an in-memory
index, crash recovery and compaction. Benchmark it against the PostgreSQL path,
but do not put an educational storage engine on the scheduler’s production-like
critical path or present it as a sixth portfolio project.

### 3. Gatehouse — multi-tenant API gateway and policy control plane

**Best project for networking, security and resilience.** Keep it a learning
gateway, not a claim that hand-written edge software should replace Envoy or a
managed gateway in production.

**Key tech:** Java, Spring Cloud Gateway, Spring WebFlux, Project Reactor,
Netty, Redis, Lettuce, Redisson, Caffeine, Spring Security, OAuth 2.0, OpenID
Connect, JWT, JWKS, Keycloak, Resilience4j, OpenTelemetry, Micrometer,
Prometheus, Grafana, WireMock, Toxiproxy, Gatling, Docker

Build:

- A Netty or Spring Cloud Gateway data plane that proxies HTTP, preserves
  correlation context and streams bodies without unbounded buffering.
- OIDC/JWT validation with JWKS caching and key rotation; tenant-aware RBAC and
  an auditable policy decision.
- Per-tenant token-bucket and sliding-window rate limits backed by Redis, with a
  stated fail-open or fail-closed policy.
- Local and distributed policy/configuration caches with versioned invalidation,
  bounded staleness and protection against cache stampedes and hot keys.
- Route configuration, weighted canaries, timeouts, circuit breakers, bounded
  retries and request-size limits.
- A separate control API that validates and versions configuration, then pushes
  or publishes safe updates to gateway instances.

Prove it:

- Load tests for fairness and tail latency, malformed-token tests, downstream
  fault injection and a zero-downtime configuration rollout.
- Eviction, stale-read, Redis-failure, stampede and hot-key experiments that
  demonstrate when the gateway serves old policy, bypasses cache or rejects.
- Threat model, API contract, dashboards and ADRs comparing local versus
  distributed rate limiting and build versus buy.

Avoid: implementing cryptographic primitives, a password database or a toy
controller that merely forwards requests with `WebClient`.

### 4. Bridge — zero-downtime legacy migration and reconciliation harness

**Best architect-level modernization project.** It demonstrates how to move a
live system safely instead of drawing a clean greenfield diagram.

**Key tech:** Java, Spring Boot, Spring MVC, Spring Data JDBC, PostgreSQL,
Flyway, Debezium, Apache Kafka, Kafka Connect, Spring for Apache Kafka,
Confluent Schema Registry, Avro, Pact, OpenTelemetry, Micrometer, Prometheus,
Grafana, JUnit 5, Testcontainers, Toxiproxy, Docker

Build:

- A deliberately small legacy monolith and schema, plus a new Spring Boot
  service owning one extracted business capability.
- Debezium CDC into Kafka, schema registry compatibility, a replayable consumer
  and a quarantine path for poison records.
- Backfill with checkpoints, then live catch-up; record watermarks and prove no
  rows were silently skipped.
- Shadow reads or dark traffic, field-by-field reconciliation, mismatch
  reporting and an explicit cutover/rollback mechanism.
- Contract tests and anti-corruption adapters so the new model does not copy
  every legacy assumption.

Prove it:

- Demonstrate schema evolution, connector restart, duplicated events, delayed
  events and rollback after traffic has begun moving.
- Publish a migration runbook with entry checks, abort thresholds, recovery
  steps and post-cutover verification.

Avoid: calling a one-time SQL copy a migration strategy, or claiming dual write
without showing how partial failure is repaired.

### 5. BankCore — core ledger and live banking-operations engine

**Best architect-level capstone, and the most ambitious option here.** Build a
bounded deposit-account and transaction core—not a pretend replacement for an
entire commercial core-banking product. The strong design signal is that money
does not become eventually consistent merely because the surrounding system
uses distributed services.

**Domain catalog:** [BankCore banking feature catalog](bankcore-feature-catalog.md)
maps the banking capabilities found in the Bancslink frontend XML and identifies
a bounded, interview-worthy implementation slice.

**Key tech:** Java, Spring Boot, Spring Modulith, Spring MVC, Spring Data JDBC,
jOOQ, PostgreSQL, HikariCP, Flyway, Spring Batch, Apache Kafka, Spring for
Apache Kafka, Kafka Streams, Confluent Schema Registry, Avro, Spring Security,
OAuth 2.0, OpenID Connect, Keycloak, TLS, mutual TLS, JCA, JCE, HashiCorp
Vault, OpenTelemetry, Micrometer, Prometheus, Grafana, Logback, Grafana Loki,
JUnit 5, Testcontainers, PIT, ArchUnit, Toxiproxy, Gatling, Docker, Kubernetes

Keep the integrity boundary explicit:

```text
Channels / payment adapters
            |
       API + identity
            |
   authoritative posting core ---- PostgreSQL journal + balances
            |                                  |
      transactional outbox                audit archive
            |
          Kafka
       /     |       \
reconciliation  reporting  notifications
       |
operator console + durable end-of-day controller
```

Build the banking kernel:

- Customer references and deposit accounts with opened, frozen, dormant and
  closed states; keep full KYC/document management outside the project.
- A configurable savings/current-account product with currency, minimum
  balance, overdraft, fee and interest rules.
- Ledger balance, available balance and holds/reservations with precisely
  defined invariants.
- An append-only double-entry journal, chart of accounts and atomic posting
  batches in which total debits always equal total credits per currency.
- Deposit, withdrawal, internal transfer, hold, release, capture, fee, interest,
  reversal and adjustment transactions. Corrections create compensating
  entries; posted journal rows are never edited or deleted.
- Separate transaction time, booking date, value date and bank business date;
  include holidays and cut-off rules so backdated and after-cut-off behavior is
  deliberate.
- Idempotency keys, request fingerprints, unique business references, sequence
  numbers and optimistic/pessimistic concurrency tests that prevent duplicate
  posting and lost updates.
- Posting-rule versioning so an old transaction can always be explained under
  the rule set that produced it.

Distribute the system carefully:

- Begin the account, product, posting, business-calendar and end-of-day domains
  as modules around one authoritative transactional ledger. A network call
  inside every debit/credit is not sophistication.
- Separate payment adapters, reconciliation, reporting, notifications, audit
  archival and operator tooling because they have different scaling, data and
  failure requirements.
- Publish committed facts through a transactional outbox and Kafka. Every
  consumer stores its deduplication state and tolerates replay and reordering.
- Build materialized read models for statements and operations; never make an
  eventually consistent search or reporting store the source of balance truth.
- Build one stateful Kafka Streams projection for live transaction summaries or
  reconciliation signals; handle event time, windows, late events, state-store
  recovery and full replay without treating the projection as balance truth.
- Define what would justify ledger partitioning, how accounts would be assigned
  to shards and why cross-shard transfers are harder. Do not implement sharding
  until a measured single-ledger limit earns it.

Implement live day operations:

- A bank-controlled business clock and calendar rather than scattered calls to
  the host machine’s current date.
- Start-of-day, cut-off and end-of-day states with explicit rules for what
  online transactions may continue during each state.
- Durable, dependency-ordered end-of-day steps for fee assessment, interest
  accrual/capitalization, dormant-account processing, ledger aggregation,
  trial-balance validation, statement generation, reconciliation and exports.
- Per-step checkpoints, fencing, run identifiers and logical idempotency so an
  operator can resume after a crash without applying fees or interest twice.
- A pre-close gate that refuses to advance the business date when the journal
  is unbalanced, mandatory reconciliation is incomplete or a critical step has
  unresolved failures.
- Maker-checker approval for sensitive adjustments, product-rule changes,
  transaction limits, end-of-day override and replay operations.
- An operator console that shows the active business date, posting status,
  end-of-day dependency graph, step progress, exceptions and safe recovery
  actions. The console invokes controlled APIs; it never edits database rows.

Build security as part of the model:

- OIDC authentication, short-lived credentials, role and attribute-based
  authorization, service-to-service mutual TLS and least-privilege database and
  Kafka identities.
- Encrypt sensitive fields, tokenize external account identifiers, keep keys in
  Vault or a cloud key-management service and demonstrate key rotation.
- Separate the immutable business audit trail from diagnostic application logs.
  Record actor, authorization context, reason, before/after intent, correlation
  ID and outcome without leaking credentials, full account identifiers or
  sensitive payloads.
- Hash-chain or sign exported audit batches so offline alteration is detectable;
  document that this is tamper-evidence, not magical prevention of every attack.
- Threat-model broken access control, duplicate/replayed requests, insider
  adjustment abuse, injection, secret leakage and forged service identities.

Make it operable:

- Technical telemetry: request rate, errors, latency, JVM/GC, thread or virtual-
  thread behavior, database-pool use, lock waits, Kafka lag and outbox age.
- Business telemetry: posting rate, debit/credit imbalance count, duplicate
  suppression, hold age, reconciliation breaks, suspense balance and reversals.
- End-of-day telemetry: current step, elapsed time, backlog, checkpoint age,
  failed records, estimated completion and breach against the operating window.
- Structured logs with correlation, causation, transaction and end-of-day run
  identifiers; OpenTelemetry traces across synchronous calls and Kafka headers.
- Actionable alerts, an operator runbook, backup/restore drill and declared
  recovery-point and recovery-time objectives.

Prove it:

- Property-based and randomized concurrent tests showing that every posting
  batch balances, balances equal journal-derived totals, frozen accounts cannot
  post and available funds cannot be overspent.
- Kill the process after each significant commit/publish/end-of-day checkpoint;
  restart and prove one logical outcome with no silent loss or double effect.
- Replay Kafka from an old offset, duplicate callbacks and reorder events; prove
  consumers converge without corrupting authoritative balances.
- Run end-of-day while controlled online traffic continues, inject one failed
  step, resume it, and compare final trial balances and reports to an independent
  reconciliation calculation.
- Load-test hot and ordinary accounts separately, report p50/p95/p99, database
  locks, pool saturation and Kafka lag, then justify one measured optimization.
- Restore a backup plus journal/outbox tail into a clean environment and show
  the declared recovery guarantees rather than merely writing “DR supported.”
- Exercise a single-writer regional failover with replication lag and an
  explicit recovery-point boundary; document fencing, traffic cutover and why
  multi-region active-active ledger writes remain outside scope.

Keep these non-goals visible: loans, cards, a complete general ledger, treasury,
AML decisioning, regulatory reporting, multi-region active-active ledger writes
and certification against a real banking standard. They can be integration
contracts or future ADRs. Pretending they are finished weakens the project;
showing where the correctness boundary ends strengthens it.

This capstone absorbs much of **PayRail** and **Bridge**. Build it *instead of*
those two—not as another simultaneous repository—unless their working code is
being evolved into BankCore.

## Common definition of “interview-grade”

Every chosen project should include:

- A five-minute local start (`docker compose` or an equally small command) and a
  seeded demo scenario.
- A concise README: problem, users, invariants, architecture, run steps, known
  limits and deliberate non-goals.
- OpenAPI or protobuf contracts, versioned migrations and realistic sample
  traffic.
- Unit, integration, contract and end-to-end tests; use Testcontainers for real
  infrastructure and mocks only at true process boundaries.
- One concurrency test, one duplicate-delivery test, one dependency-timeout
  test and one crash/restart test relevant to the system.
- Structured logs, RED metrics (rate, errors, duration), traces, health/readiness
  probes, a dashboard and at least one actionable alert.
- A load-test report with p50/p95/p99, throughput, resource use, the first
  bottleneck and evidence for the fix.
- One real deployment with backward-compatible API/event changes, expand-and-
  contract database migration, canary or blue-green release, health/SLO gates
  and a demonstrated rollback. This is service operability, not a separate
  developer-tooling project.
- Threat model, secret handling, least privilege, dependency scanning and no
  credentials committed to Git.
- Three to six ADRs covering genuine trade-offs, plus an operator runbook and a
  short failure-demo script.
- A small deployable architecture. Begin as a modular monolith and split a
  service only where independent scaling, ownership or failure isolation earns
  the network boundary.

## Recommended build order

1. **PayRail** if the target is backend, fintech or transaction systems and a
   bounded flagship is needed.
2. Evolve PayRail into **BankCore** only when the ledger, failure tests and
   reconciliation already work; BankCore is the architect capstone, not the
   starting line.
3. **Chronos** if concurrency, distributed systems or backend-platform roles
   dominate.
4. Add **Bridge** for architect/modernization roles or **Gatehouse** for
   API/security-heavy roles.

The strongest bounded backend/platform portfolio is usually **PayRail +
Chronos + Gatehouse**. The strongest deep fintech portfolio is
**BankCore + Chronos**, with **Bridge** added only for legacy-modernization
positioning. These
combinations cover application correctness, relational data, eventing,
concurrency, distributed coordination, security and observability without
turning the portfolio into a developer-tooling showcase or pretending that one
enormous “microservices demo” is a production estate.

## Primary references

- [Java SE and JDK 25 specifications](https://docs.oracle.com/en/java/javase/25/docs/specs/index.html)
- [Oracle Java support roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring project documentation](https://spring.io/projects)
- [Jakarta EE specifications](https://jakarta.ee/specifications/)
- [Eclipse MicroProfile specifications](https://microprofile.io/specifications/)
- [OpenTelemetry Java documentation](https://opentelemetry.io/docs/languages/java/)
- [Apache Kafka documentation](https://kafka.apache.org/documentation/)
- [Kubernetes documentation](https://kubernetes.io/docs/home/)

Vendor versions move. Recheck the relevant official compatibility matrix before
starting a real project or answering a version-specific interview question.
