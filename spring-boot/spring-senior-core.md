# Spring Senior Backend Reference

- [How to use this book](#how-to-use-this-book)

- [0. Two flows to keep in mind](#0-two-flows-to-keep-in-mind)

- [Part I. Container and Spring Boot](#part-i-container-and-spring-boot)
  - [1. Spring and Spring Boot](#1-spring-and-spring-boot)
  - [2. Configuration and auto-configuration](#2-configuration-and-auto-configuration)
  - [3. IoC dependency injection and beans](#3-ioc-dependency-injection-and-beans)
  - [4. Container startup and extension points](#4-container-startup-and-extension-points)
  - [5. Aspect-oriented programming and Spring proxies](#5-aspect-oriented-programming-aop-and-spring-proxies)

- [Part II. HTTP and security](#part-ii-http-and-security)
  - [6. The servlet request path](#6-the-servlet-request-path)
  - [7. Spring Security architecture](#7-spring-security-architecture)
  - [8. API boundaries, validation and exceptions](#8-api-boundaries-validation-and-exceptions)
  - [9. Method authorization and ownership](#9-method-authorization-and-ownership)

- [Part III. Persistence and consistency](#part-iii-persistence-and-consistency)
  - [10. JPA and the persistence context](#10-jpa-and-the-persistence-context)
  - [11. Mapping, fetching and query performance](#11-mapping-fetching-and-query-performance)
  - [12. Concurrent writes and locking](#12-concurrent-writes-and-locking)
  - [13. Spring transactions](#13-spring-transactions)
  - [14. Cross-system consistency](#14-cross-system-consistency)

- [Part IV. Production engineering](#part-iv-production-engineering)
  - [15. Resilience](#15-resilience)
  - [16. Observability and diagnosis](#16-observability-and-diagnosis)
  - [17. Production testing](#17-production-testing)
  - [18. End-to-end banking scenarios](#18-end-to-end-banking-scenarios)

- [Primary references](#primary-references)

- [Study map](#study-map)

## How to use this book

This guide explains Spring as four connected runtime stories: how the
application starts, how a request reaches business code, how state becomes
durable, and how the service behaves in production. Read a part in order the
first time; use individual chapters later when diagnosing a particular
problem.

The companion files turn the chapters into exercises:

- [Spring Boot basics exercises](spring-boot-basics.md)

- [Container internals exercises](spring-container-internals.md)

- [Spring Security exercises](spring-security-basics.md)

- [JPA and Hibernate performance exercises](spring-data-jpa-performance.md)

- [Deep transaction exercises](spring-boot-transactions-deep.md)

- [Resilience exercises](spring-boot-resilience.md)

- [Observability exercises](spring-boot-observability.md)

- [Production testing exercises](spring-boot-testing-deep.md)

For each mechanism, be able to answer three practical questions:

1. What executes at runtime?

2. Which correctness, security or capacity promise depends on it?

3. What evidence would show that the design works or is failing?

### Reading path

1. **How the application comes alive:**
   [§1 Boot](#1-spring-and-spring-boot) →
   [§2 configuration](#2-configuration-and-auto-configuration) →
   [§3 one bean](#3-ioc-dependency-injection-and-beans) →
   [§4 context refresh and lifecycle](#4-container-startup-and-extension-points) →
   [§5 one proxied call](#5-aspect-oriented-programming-aop-and-spring-proxies).

2. **How one request reaches business code:**
   [§6 servlet path](#6-the-servlet-request-path) →
   [§7 authentication](#7-spring-security-architecture) →
   [§8 validation/errors](#8-api-boundaries-validation-and-exceptions) →
   [§9 method authorization](#9-method-authorization-and-ownership).

3. **How one business operation becomes durable:**
   [§10 persistence context](#10-jpa-and-the-persistence-context) →
   [§11 SQL/fetch behavior](#11-mapping-fetching-and-query-performance) →
   [§12 concurrent writes](#12-concurrent-writes-and-locking) →
   [§13 transaction boundary](#13-spring-transactions) →
   [§14 cross-system consistency](#14-cross-system-consistency).

4. **How that operation survives production:**
   [§15 resilience](#15-resilience) →
   [§16 observability](#16-observability-and-diagnosis) →
   [§17 testing](#17-production-testing) →
   [§18 complete incidents](#18-end-to-end-banking-scenarios).

The sequence is deliberate: start the application, handle a request, commit
its effects, and then prove that the same behavior survives failure and load.

### Version note

The examples use Jakarta APIs and the component-based configuration style of
modern Spring Boot applications. The core runtime ideas apply across Boot 3
and 4, but package names, test modules and supported Java versions can differ.
Use the documentation for the version selected by the project's build rather
than copying a current-version claim into long-lived notes.

---

## 0. Two flows to keep in mind

Most of this book fits into two flows. First, Spring builds the application.
Then the application handles requests. Keeping those flows separate makes the
details easier to place: startup explains how an object becomes a Spring bean;
request handling explains what happens when that bean is called.

### Startup: Spring builds the application

When `main` calls `SpringApplication.run(...)`, the controllers, services and
repositories do not exist yet. Spring Boot reads the application's
configuration and tells Spring which objects are needed. Spring then creates
those objects, supplies their dependencies and prepares them for use.

```text
main
  -> Spring Boot reads configuration
  -> Spring registers bean definitions
  -> Spring creates beans and injects their dependencies
  -> bean post-processors initialize or wrap some beans with proxies
  -> the web server starts
  -> the application is ready
```

By the end of startup, the application has a graph of connected objects. Some
of those objects are proxies that add behavior such as transactions, method
security, caching or asynchronous execution. Part I examines how that graph is
built and why a proxy sometimes changes what a method call does.

### Request handling: the application uses those objects

After startup, an HTTP request follows a different flow:

```text
client
  -> servlet container and filters
  -> Spring Security
  -> DispatcherServlet
  -> controller
  -> service
  -> repository and JPA
  -> connection pool
  -> database
  -> HTTP response
```

Each stage hands work to the next one. Security filters can reject a request
before a controller is called. The controller translates HTTP input into a
Java call and delegates the business work to a service. The service may be a
proxy, so transaction or authorization logic can run around the method. The
repository uses JPA and a pooled database connection to execute SQL.

Calls to another HTTP service or message broker branch away from this path.
They do not become part of a database transaction merely because the service
method has `@Transactional`; cross-system consistency needs an explicit
design, covered in Part III.

In production, logs, metrics and traces show where these flows stopped or
slowed down. Part II follows the request to the service layer, Part III follows
state into and beyond the database, and Part IV covers failure, diagnosis and
testing.

---

# Part I. Container and Spring Boot

Part I follows one application from `main` to its first service call:

```text
SpringApplication.run(...)
  -> configuration and bean definitions are collected
  -> context refresh creates and wires beans
       -> some beans are exposed through proxies
  -> application runners execute
  -> the application becomes ready
  -> another bean calls a service through its exposed reference
```

Chapters 1–4 explain how the application is assembled. Chapter 5 starts after
startup and explains what happens when a proxied bean is called.

## 1. Spring and Spring Boot

### What each one does

The Spring Framework supplies the container and the programming model. It can
create objects, connect their dependencies, manage their lifecycle and add
infrastructure around method calls. Spring MVC, transaction management and
Spring's testing support are also Framework features.

Spring Boot assembles those features into an application. It manages compatible
dependency versions, contributes configuration when suitable libraries are on
the classpath, starts an embedded server and provides production integrations
such as health checks and metrics.

| Spring Framework | Spring Boot |
|---|---|
| Defines beans, dependency injection, AOP, transactions and MVC | Configures those facilities using classpath, properties and existing beans |
| Gives explicit mechanisms | Supplies common defaults and lets application code replace them |
| Can be used without Boot | Uses the Framework underneath |

A useful way to remember the relationship is: **Spring provides the machinery;
Boot decides how much of it can be assembled automatically.**

### The application class

A typical Boot application starts here:

```java
@SpringBootApplication
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
```

`@SpringBootApplication` combines three roles:

- `@SpringBootConfiguration` marks the application's main configuration.

- `@ComponentScan` finds application components below the package containing
  the application class.

- `@EnableAutoConfiguration` imports Boot's conditional configuration.

Package placement matters because component scanning starts from the
application class's package:

```text
com.bank                         scanned
  PaymentApplication
  payments.PaymentService       found
  shared.AuditRecorder          found

com.shared.AuditRecorder        not found: outside com.bank
```

Putting the application class in the application's root package gives scanning
a clear boundary. Code outside that boundary must be included deliberately
with `@Import`, another scan, or library auto-configuration.

### Starters and auto-configuration

A starter and an auto-configuration solve different problems.

A **starter** is a dependency descriptor. For example,
`spring-boot-starter-data-jpa` brings in Spring Data JPA, Hibernate and the
supporting libraries using versions managed by Boot.

An **auto-configuration** is code. It looks at the classpath, configuration
properties and beans already registered, then contributes definitions when its
conditions match.

```text
JPA starter added
  -> JPA and database classes appear on the classpath
  -> Boot evaluates database auto-configurations
  -> matching conditions contribute DataSource, JPA and transaction beans
```

Adding a dependency can therefore change application startup even when no
application code refers to it. Conversely, having an auto-configuration class
in a jar does nothing when its conditions do not match.

Boot commonly uses `@ConditionalOnMissingBean` for defaults. If the application
already defines the relevant bean, the condition is false and Boot does not add
its default. This is called **backing off**; Boot is deciding what to register,
not replacing an object later at runtime.

### From `main` to ready

`SpringApplication.run(...)` drives the whole startup. The stable sequence is:

```text
prepare environment
  -> create ApplicationContext
  -> load application and auto-configuration definitions
  -> refresh the context and create beans
  -> start application runners
  -> publish readiness
```

First, Boot builds the `Environment` from configuration sources and determines
which profiles are active. Those values must be available early because they
affect which definitions are registered.

Next, Boot creates the appropriate `ApplicationContext` and loads definitions
from component scanning, `@Bean` methods, imports and auto-configuration. A
definition is a recipe for an object; most application objects do not exist
yet.

The context is then refreshed. During refresh, Spring processes definitions,
installs container extension points and creates the non-lazy singleton beans.
This is where dependency injection, lifecycle callbacks and proxy creation
happen. Chapters 2–4 explain the inputs to refresh and what happens inside it.

After refresh, Boot calls `ApplicationRunner` and `CommandLineRunner` beans.
When they finish, it publishes `ApplicationReadyEvent` and changes its
readiness state to accepting traffic. A runner can therefore delay readiness
or fail startup, so runner work should be bounded.

An initialized web server is not by itself proof that the application is ready.
Traffic should be controlled by a readiness probe that reflects the Boot
readiness state.

---

## 2. Configuration and auto-configuration

Configuration affects startup before most application beans exist. Boot loads
values into the `Environment`; Spring then uses those values while deciding
which definitions to register and how to construct their objects.

### External configuration

Boot can read configuration from packaged files, profile-specific files,
environment variables, system properties, command-line arguments and test
overrides. Later, higher-precedence sources can override earlier ones.

The complete precedence list is best treated as reference material. During a
failure, the useful question is: **which property source supplied the final
value?** Startup diagnostics and secured Actuator environment endpoints can
answer that question. Secret values must remain sanitized.

Command-line `--server.port=9090`, for example, overrides a packaged
`server.port` value. An environment variable is convenient in a container
platform because it changes deployment configuration without rebuilding the
application image.

### Bind related values as one type

Suppose the application needs these settings:

```yaml
payment-switch:
  base-url: https://switch.internal
  connect-timeout: 300ms
  read-timeout: 1500ms
  max-in-flight: 80
```

Bind the group to one validated type:

```java
@ConfigurationProperties("payment-switch")
@Validated
public record SwitchProperties(
        @NotNull URI baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout,
        @Min(1) int maxInFlight) { }
```

This gives the application typed values, one place for validation and a clear
description of the configuration it expects. A malformed duration or missing
URL fails startup instead of surfacing during the first request.

The properties type must also be registered. Common choices are
`@EnableConfigurationProperties(SwitchProperties.class)` on a configuration
class or `@ConfigurationPropertiesScan` on the application class.

Boot's relaxed binding maps conventional external names to Java names. For
example, these refer to the same field in the appropriate property source:

```text
payment-switch.max-in-flight
paymentSwitch.maxInFlight
PAYMENT_SWITCH_MAX_IN_FLIGHT
```

Use `@Value` for an isolated value or expression. Prefer
`@ConfigurationProperties` when several values form one configuration concept.

Secrets should come from an approved secret store or platform-provided property
source. Binding a secret into a typed object does not make it safe to log or
expose through an endpoint.

### Profiles

A profile selects a set of bean definitions or configuration files. It is
useful for infrastructure differences such as a local stub:

```java
@Bean
@Profile("local")
FraudClient localFraudClient() {
    return request -> FraudDecision.accepted();
}
```

Profiles become difficult to reason about when they encode many independent
business choices in names such as `prod-bank-a-region-2-dr`. Use typed
properties for values and explicit strategy objects for business variants.

Security-sensitive defaults should fail closed. A misspelled production
profile must not silently activate a permissive local bean.

### How auto-configuration decides

Boot discovers auto-configuration classes from import metadata in libraries;
application component scanning is not responsible for finding them. Each class
uses conditions to state when its definitions are relevant.

Common conditions include:

- `@ConditionalOnClass`: a required API is present;

- `@ConditionalOnMissingBean`: the application has not supplied its own bean;

- `@ConditionalOnProperty`: a setting enables or selects the feature;

- `@ConditionalOnWebApplication`: the expected web application type is in use.

```java
@AutoConfiguration
@ConditionalOnClass(SignedClient.class)
@EnableConfigurationProperties(SignedClientProperties.class)
class SignedClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SignedClient signedClient(SignedClientProperties properties) {
        return new SignedClient(properties.baseUrl(), properties.keyId());
    }
}
```

If `SignedClient` is absent, the auto-configuration is irrelevant. If the class
is present but the application already defines a `SignedClient`, the default
backs off. If both conditions match, the definition is registered and its bean
is created during refresh like any other bean.

### Diagnose the decision, do not guess

When an expected auto-configured bean is missing:

1. Confirm that the dependency and expected version are present.

2. Check active profiles and the resolved configuration properties.

3. Enable the condition evaluation report with `--debug`, or inspect the
   secured Actuator `conditions` endpoint.

4. Find the relevant auto-configuration and read why each condition matched or
   did not match.

5. Check whether an application bean caused the default to back off.

6. Check explicit exclusions and package scanning for application components.

Do not copy an auto-configuration class into application code merely to make
the bean appear. First identify which premise was false.

Invalid configuration and unavailable infrastructure need different policies.
A missing issuer URL or invalid timeout should normally fail startup. A valid
but temporarily unreachable dependency may instead keep the application
unready until it recovers, depending on the deployment contract.

---

## 3. IoC dependency injection and beans

The application context is a registry of named objects called **beans**. A bean
is not a special kind of Java object; it is an object whose construction and
lifecycle are managed by Spring.

### Why dependency injection exists

Without dependency injection, a class chooses and constructs its own
collaborators:

```java
class PaymentService {
    private final PaymentRepository repository =
            new OraclePaymentRepository();
}
```

`PaymentService` is now tied to one repository implementation. Replacing the
repository for a test or a different database requires changing the class.

With constructor injection, the class states what it needs and leaves the
choice to its caller:

```java
@Service
class PaymentService {
    private final PaymentRepository repository;
    private final FraudClient fraudClient;

    PaymentService(PaymentRepository repository, FraudClient fraudClient) {
        this.repository = repository;
        this.fraudClient = fraudClient;
    }
}
```

Spring creates the bean, finds matching collaborators and calls the
constructor. A unit test can make the same call with fakes. This is dependency
injection; the inversion of control is that `PaymentService` no longer controls
the assembly of the object graph.

Constructor injection is the normal choice for required dependencies. It makes
them visible, permits `final` fields and prevents a usable instance from being
created without them. A single constructor does not need `@Autowired`.

Field injection hides required inputs and makes ordinary construction awkward.
Setter injection is useful only when a dependency is genuinely optional or
replaceable after construction.

### How a class becomes a bean

Spring needs a bean definition before it can create an object. Common sources
of definitions are:

- component scanning of classes annotated with `@Component` or a specialized
  stereotype;

- `@Bean` methods in configuration classes;

- explicit imports;

- Boot auto-configuration.

The main stereotypes communicate the role of a scanned class:

| Annotation | Intended role |
|---|---|
| `@Component` | general managed component |
| `@Service` | application or business service |
| `@Repository` | persistence component; also enables eligible persistence-exception translation |
| `@Controller` | MVC controller, commonly returning views |
| `@RestController` | MVC controller whose handler return values are written to the response body |

The annotation registers the class; it does not enforce the architecture. A
controller containing SQL is still poor design even though Spring can create
it successfully.

Use a `@Bean` method when construction needs third-party code or explicit setup:

```java
@Configuration(proxyBeanMethods = false)
class TimeConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
```

Spring manages the returned `Clock` in the same way it manages a scanned bean.

### How Spring chooses a dependency

Spring first looks for beans assignable to the required type. With exactly one
candidate, injection is straightforward. With none, startup normally fails
with `NoSuchBeanDefinitionException`. With several, Spring needs more
information.

```java
interface PaymentRail {
    Receipt send(Payment payment);
}

@Component
class ImpsRail implements PaymentRail { /* ... */ }

@Component
class NeftRail implements PaymentRail { /* ... */ }
```

A constructor that requests one `PaymentRail` is ambiguous. The usual ways to
resolve it are:

- mark one candidate `@Primary` when it is the application-wide default;

- put `@Qualifier("neftRail")` at an injection point when that consumer needs
  a particular candidate;

- inject all candidates as `List<PaymentRail>` or `Map<String, PaymentRail>`
  when the application genuinely needs a set of strategies.

`@Primary` expresses a default on the producing side. `@Qualifier` expresses a
choice on the consuming side and is more specific.

```java
@Service
class BulkSalaryService {
    private final PaymentRail rail;

    BulkSalaryService(@Qualifier("neftRail") PaymentRail rail) {
        this.rail = rail;
    }
}
```

For runtime routing, do not expose bean names as business values. Convert input
to a domain type and build a domain-keyed map:

```java
enum Rail { IMPS, NEFT }

interface PaymentRail {
    Rail rail();
    Receipt send(Payment payment);
}

@Service
class RailRouter {
    private final Map<Rail, PaymentRail> rails;

    RailRouter(List<PaymentRail> implementations) {
        this.rails = implementations.stream()
                .collect(toUnmodifiableMap(PaymentRail::rail, identity()));
    }

    Receipt route(Rail rail, Payment payment) {
        PaymentRail implementation = rails.get(rail);
        if (implementation == null) {
            throw new UnsupportedRailException(rail);
        }
        return implementation.send(payment);
    }
}
```

Bean names remain wiring details, while the API works with the closed set of
values defined by `Rail`.

Use `Optional<T>` for a genuinely optional single dependency and
`ObjectProvider<T>` when lookup must be lazy or repeated. Do not use either one
to hide a dependency that the application cannot function without.

### Scope and shared state

The default bean scope is **singleton per application context**. One singleton
service can be called concurrently by many request threads, so singleton does
not mean thread-safe.

```java
@Service
class BadSequenceService {
    private long next;

    long next() {
        return ++next; // shared mutation and a data race
    }
}
```

Keep singleton services stateless. Durable counters and business state belong
in a store with the required concurrency guarantees; operational counters
belong in a metrics system.

Other scopes include prototype, request and session. Injecting a request-scoped
object into a singleton requires indirection, normally a scoped proxy or an
`ObjectProvider`, so the current request's instance is resolved at call time.

### Circular dependencies

Constructor injection exposes cycles immediately:

```text
PaymentService -> NotificationService -> PaymentService
```

Neither object can be constructed first, so startup fails. That is usually a
design problem, not a container setting to work around.

Typical corrections are:

- extract a third service that owns the workflow involving both components;

- publish a domain event when one component should announce an outcome without
  knowing its consumers;

- separate orchestration from the two capabilities being orchestrated.

`@Lazy` or `ObjectProvider<T>` can defer one lookup and break the construction
cycle, but the runtime dependency cycle still exists. Treat that as a tactical
bridge, not the design goal.

---

## 4. Container startup and extension points

Chapter 3 described the objects Spring creates. This chapter follows the
container work that turns definitions into those objects.

### Definitions are recipes

A `BeanDefinition` records how Spring should create a bean: its type or factory
method, scope, constructor arguments, qualifiers, lifecycle methods and other
metadata. A `BeanDefinitionRegistry` holds those recipes. A `BeanFactory` uses
them to create and connect objects.

An `ApplicationContext` builds on the bean factory. It adds environment and
profile support, resource loading, events, message resolution and application
lifecycle integration.

```text
component scan / @Bean / @Import / auto-configuration
                         |
                         v
                 BeanDefinitions
                         |
                         v
                    BeanFactory
                         |
                         v
                  managed objects
```

Keeping definitions separate from instances lets Spring inspect and modify the
plan before it constructs ordinary application beans.

### Refresh in three stages

`ApplicationContext.refresh()` is the central container operation during Boot
startup. Its implementation has many steps, but three stages explain the
extension model:

```text
1. COMPLETE OR CHANGE DEFINITIONS
   registry and factory post-processors inspect the recipes

2. INSTALL BEAN POST-PROCESSORS
   processors that will participate in bean creation are registered

3. CREATE REMAINING SINGLETONS
   construct -> inject -> initialize -> expose
```

Configuration-class processing, component scanning and imports contribute to
the first stage. Bean post-processors must be installed before normal beans are
created because those processors implement much of annotation injection,
lifecycle handling and proxy creation.

The final stage creates the remaining non-lazy singletons. Web-server and
lifecycle infrastructure are also completed during refresh. When refresh
succeeds, the context contains the references that other beans will use.

### The lifecycle of one bean

For one ordinary singleton, the relevant sequence is:

```text
instantiate
  -> inject dependencies and properties
  -> invoke aware callbacks
  -> BeanPostProcessor: before initialization
  -> @PostConstruct / afterPropertiesSet / custom init method
  -> BeanPostProcessor: after initialization
  -> expose the resulting reference for use
  -> @PreDestroy / destroy method when the context closes
```

A post-processor may return the original object or a replacement reference.
Spring AOP infrastructure commonly returns a proxy that wraps the original
target. Other beans then receive the proxy, not the raw target.

```text
PaymentService target
        |
        | post-processing adds transaction advice
        v
PaymentService proxy  -> reference injected into callers
```

This timing explains why an advised method should not be invoked from
`@PostConstruct`. Initialization runs on the target while the final proxy is
still being prepared, and a call through `this` would bypass that proxy anyway.

Use `@PostConstruct` for fast local initialization that requires injected
dependencies. Avoid slow network calls there: they delay context refresh and
make startup depend on remote availability. Application-level work that must
run after the context is built belongs in a bounded runner or an appropriate
lifecycle component.

Destruction callbacks run for managed singletons when the context closes
normally. Spring does not manage destruction of prototype beans after handing
them to the caller, and no callback is guaranteed after an abrupt process
termination.

### Factory post-processors and bean post-processors

The similar names refer to different stages:

| Extension point | Operates on | Typical use |
|---|---|---|
| `BeanDefinitionRegistryPostProcessor` | the definition registry | add more bean definitions |
| `BeanFactoryPostProcessor` | bean definitions | change metadata before normal objects exist |
| `BeanPostProcessor` | bean instances | injection support, lifecycle callbacks and proxy wrapping |

Creating ordinary beans from a factory post-processor is dangerous because it
pulls them into existence before all bean post-processors are installed. Such a
bean can miss later processing and proxy creation.

Application code rarely needs to implement these interfaces. Their main value
is explanatory: many Spring annotations work because infrastructure
post-processors interpret them at the correct stage.

### `@Configuration` and inter-bean calls

Consider two beans declared in one configuration class:

```java
@Configuration
class LedgerConfiguration {
    @Bean
    Ledger ledger() {
        return new Ledger();
    }

    @Bean
    PostingService postingService() {
        return new PostingService(ledger());
    }
}
```

With the default `proxyBeanMethods = true`, Spring subclasses the configuration
class. The call to `ledger()` is intercepted and returns the managed singleton
rather than constructing another `Ledger`.

When `proxyBeanMethods = false`, the class is not enhanced. Calling one `@Bean`
method from another is then an ordinary Java call and creates a second object.
The safer lite-mode style is parameter injection:

```java
@Configuration(proxyBeanMethods = false)
class LedgerConfiguration {
    @Bean
    Ledger ledger() {
        return new Ledger();
    }

    @Bean
    PostingService postingService(Ledger ledger) {
        return new PostingService(ledger);
    }
}
```

Spring resolves the parameter from the container, so the code does not depend
on configuration-class method interception. This style is explicit and is
widely used by Boot auto-configuration.

### `FactoryBean<T>`

A `FactoryBean<T>` is itself a bean that creates another object when
construction must be delegated to framework-specific factory logic. Looking up
`client` returns the product; looking up `&client` returns the factory.

Do not confuse it with `BeanFactory`: `FactoryBean` creates one kind of product,
while `BeanFactory` is the container that manages all bean definitions and
instances.

---

## 5. Aspect-oriented programming (AOP) and Spring proxies

At this point startup is complete. The container has created a
`PaymentService` and may have exposed a proxy around it. This chapter follows a
call from another bean to that exposed reference.

### Why Spring uses proxies

Transactions, method authorization, caching, retry and asynchronous execution
all need behavior around a method call. Writing that wrapper inside every
business method would mix infrastructure with business logic and repeat the
same policy across many classes.

Spring AOP places the wrapper in a proxy:

```text
caller
  -> proxy
       -> run matching interceptors
       -> call target method
       -> complete matching interceptors
  <- result or exception
```

An annotation such as `@Transactional` is metadata. It does not start a
transaction by itself. Transaction infrastructure finds that metadata and
adds an interceptor to an eligible bean's proxy.

### One transactional call

```java
record SettlementCommand(UUID paymentId) { }
record Receipt(UUID paymentId) { }

@Service
class PaymentService {
    @Transactional
    public Receipt settle(SettlementCommand command) {
        // business and persistence work
        return new Receipt(command.paymentId());
    }
}

@Component
class SettlementCoordinator {
    private final PaymentService payments;

    SettlementCoordinator(PaymentService payments) {
        this.payments = payments;
    }

    Receipt settle(UUID paymentId) {
        return payments.settle(new SettlementCommand(paymentId));
    }
}
```

`SettlementCoordinator` normally receives the exposed `PaymentService` proxy.
The call proceeds as follows:

1. The coordinator calls `settle(...)` on the proxy.

2. The proxy finds the matching transaction interceptor.

3. The interceptor starts or joins a transaction and calls the target method.

4. When the target returns or throws, the interceptor applies the transaction
   completion rules before control returns to the coordinator.

The exact propagation and rollback rules belong to §13. The important point
here is that the call reached the interceptor before it reached the target.

```text
SettlementCoordinator
  -> PaymentService proxy
       -> TransactionInterceptor
            -> PaymentService target
       <- commit or roll back
  <- result or exception
```

Calling `new PaymentService().settle(...)` calls an ordinary Java object. The
container did not create or expose that instance, so no Spring proxy surrounds
the call.

### The small AOP vocabulary

| Term | Meaning in Spring AOP |
|---|---|
| target | the application object containing the method |
| proxy | the object given to callers; it runs interceptors and delegates to the target |
| advice/interceptor | code that runs before, after or around a method call |
| pointcut | a rule selecting which method executions receive advice |
| advisor | a pointcut paired with advice |
| aspect | a component that groups a cross-cutting policy and its selection rule |

Spring AOP supports method-execution join points on Spring-managed beans. Full
AspectJ weaving is a separate bytecode mechanism and is not assumed here.

### Proxy types and method constraints

Spring can expose two common proxy shapes:

| Proxy type | Shape | Main consequence |
|---|---|---|
| JDK dynamic proxy | implements the target's interfaces | callers use an interface implemented by the proxy |
| class-based proxy | generated subclass of the target class | the class and advised methods must be overridable |

Class-based proxies cannot advise `final` methods and cannot subclass a `final`
class. Private methods cannot be advised through either proxy style. Public,
non-final service methods are the least surprising proxy boundary.

Spring Boot commonly enables class-based proxies by default, but application
and feature configuration can change the proxy type. Business design should
not rely on guessing which one was chosen.

### Self-invocation bypasses the proxy

Once a proxy delegates to its target, a call from one target method to another
uses `this`. It does not travel back out through the proxy.

```java
@Service
class SettlementService {
    public void settleBatch(List<UUID> paymentIds) {
        for (UUID paymentId : paymentIds) {
            settleOne(paymentId); // direct call on this
        }
    }

    @Transactional
    public void settleOne(UUID paymentId) {
        // database work
    }
}
```

If `settleBatch` is not transactional, `settleOne` does not acquire a
transaction through its annotation. The same rule affects method security,
caching, retry and `@Async`.

Move the advised operation to a collaborator when it needs an independently
intercepted boundary:

```java
@Service
class SingleSettlementService {
    @Transactional
    public void settle(UUID paymentId) {
        // database work
    }
}

@Service
class BatchSettlementService {
    private final SingleSettlementService single;

    BatchSettlementService(SingleSettlementService single) {
        this.single = single;
    }

    public void settleBatch(List<UUID> paymentIds) {
        paymentIds.forEach(single::settle); // crosses the collaborator proxy
    }
}
```

As written, each item has its own transaction. If the whole batch must be
atomic, put the transaction around the batch instead. Transaction placement is
a business decision, not merely a workaround for self-invocation.

Self-injection and `AopContext.currentProxy()` can force a call back through the
proxy, but they couple business code to the interception mechanism. A separate
collaborator usually makes the intended boundary clearer.

### Other reasons advice appears to be missing

When an annotation seems ineffective, check the call rather than the annotation
first:

- Was the object created and managed by Spring, or with `new`?

- Did the caller receive the exposed proxy, or a raw target reference?

- Did the call come from another object, or from `this`?

- Can the selected proxy type intercept that method?

- Is the feature enabled and is its infrastructure present?

- Did another interceptor change the thread, exception or ordering?

Initialization is another special case. `@PostConstruct` runs before the final
proxy is available for normal use, so it should not depend on an advised
self-call.

Interceptor order also changes behavior. Retry outside a transaction can start
a fresh transaction for each attempt. A transaction outside retry can keep all
attempts inside one transaction that may already be marked rollback-only.
`@Async` moves execution to another thread, so thread-local transaction and
security state do not automatically follow.

### When AOP fits

AOP is a good fit for stable, cross-cutting policy that naturally surrounds a
method call: transactions, authorization, observation and carefully defined
retry or caching rules.

Keep business workflow explicit. Payment state transitions, fee calculation,
compensation and idempotency should be visible in ordinary application code,
not hidden inside an aspect.

Most applications only consume Spring's existing advisors. If an application
defines a custom aspect, keep its pointcut narrow and test both methods that
should match and methods that must not match.

---

# Part II. HTTP and security

Part II follows one HTTP request until it reaches business code:

```text
client
  -> servlet container
  -> servlet filters
       -> Spring Security authenticates and checks the request
  -> DispatcherServlet
       -> select controller
       -> deserialize and validate input
       -> invoke controller
            -> call service proxy
                 -> method authorization
                 -> business operation
       -> serialize the response
```

The order matters. A security filter can reject the request before Spring MVC
selects a controller. MVC exception handling therefore cannot handle every
failure produced during an HTTP request.

## 6. The servlet request path

### From the socket to Spring MVC

In a traditional Spring MVC application, the embedded servlet container owns
the listening socket and a worker-thread pool. It assigns an accepted request
to a thread and invokes the servlet filter chain.

```text
Tomcat or Jetty
  -> Filter 1
  -> Filter 2
  -> Spring Security filter chain
  -> DispatcherServlet
  -> controller
  -> service
```

A filter can inspect or modify the request and response, continue with
`chain.doFilter(...)`, or stop processing and write a response itself. Logging,
correlation, security and compression commonly live in filters because they
need to surround the servlet.

`DispatcherServlet` is the front controller for Spring MVC. Once the filter
chain reaches it, MVC takes over.

### What `DispatcherServlet` delegates

`DispatcherServlet` coordinates other components rather than performing every
step itself:

| Component | Responsibility |
|---|---|
| `HandlerMapping` | find the handler for the request path, method and conditions |
| `HandlerAdapter` | invoke the selected kind of handler |
| argument resolvers | create method arguments such as `@PathVariable`, `Authentication` or `Pageable` |
| `HttpMessageConverter` | read and write formats such as JSON, text and bytes |
| Bean Validation | validate eligible controller arguments |
| `HandlerExceptionResolver` | turn MVC exceptions into responses |

For a REST controller, the normal flow is:

```text
request bytes
  -> message converter creates request DTO
  -> validation checks the DTO
  -> controller calls application service
  -> controller result becomes response DTO
  -> message converter writes response bytes
```

An ordinary `@Controller` commonly returns a view name. `@RestController`
includes response-body behavior, so its return values are written to the HTTP
response.

### Filters, interceptors and advice are different hooks

These extension points sit at different places:

| Hook | Runs around | Can act before controller selection? |
|---|---|---|
| servlet `Filter` | the servlet and everything downstream | yes |
| Spring MVC `HandlerInterceptor` | a mapped MVC handler | no |
| `@ControllerAdvice` | MVC argument, controller and response exception handling | no |
| Spring AOP proxy | a call to an advised Spring bean | unrelated to raw servlet traffic |

Place behavior at the earliest layer that has the information it needs.
Authentication belongs in the security filter chain. HTTP representation and
MVC error mapping belong in MVC. Business ownership checks usually belong on
the service operation and in the data access rule that protects the state.

### The blocking request model

Spring MVC normally keeps one container thread assigned to a request while it
waits for JDBC or blocking network calls. Capacity is therefore constrained by
worker threads, database connections, downstream connections and latency.

If 200 request threads compete for 30 database connections, increasing the
thread count does not create more database throughput. It can instead create a
longer queue and consume more memory.

Virtual threads reduce the cost of blocked threads but do not create additional
database connections or downstream capacity. WebFlux avoids tying up a thread
for non-blocking I/O, but only when the entire relevant path uses non-blocking
drivers and APIs. Mixing blocking JDBC into an event loop removes that benefit.

Timeouts, retries and saturation controls for downstream work are covered in
Part IV.

---

## 7. Spring Security architecture

Spring Security's servlet support is a filter-based layer in front of Spring
MVC. Its job is to establish an identity, apply request-level security rules and
either continue the filter chain or produce a security response.

### Authentication comes before authorization

**Authentication** establishes who the caller is and how that identity was
verified. **Authorization** decides whether that caller may perform an action.

A valid credential is not by itself permission to settle a payment. It creates
an authenticated identity with authorities that later authorization rules can
evaluate.

The main runtime objects are:

| Object | Meaning |
|---|---|
| `Authentication` | principal, authorities, authentication state and mechanism-specific details |
| `SecurityContext` | holder for the current `Authentication` |
| `SecurityContextHolder` | access to the context associated with current execution |
| authority | a granted capability such as `PAYMENT_WRITE` |

### How the security filter chain fits

The servlet container knows about a `DelegatingFilterProxy`. That proxy finds
Spring Security's `FilterChainProxy` bean and delegates to it:

```text
servlet FilterChain
  -> DelegatingFilterProxy
       -> FilterChainProxy
            -> first matching SecurityFilterChain
                 -> exploit protection
                 -> authentication filters
                 -> request authorization
  -> DispatcherServlet
```

Each `SecurityFilterChain` has a request matcher and an ordered set of filters.
`FilterChainProxy` uses the first matching chain. If an API chain matches
`/api/**`, later chains are not added to it; only that chain's filters run.

Inside a chain, authentication must be established before request authorization
can use it. A security filter can stop without calling the next filter, which is
why an unauthenticated request may never reach MVC.

### One bearer-token request

Assume the client sends:

```http
POST /api/payments/7e5b7c0a/settlement HTTP/1.1
Authorization: Bearer eyJ...
Content-Type: application/json
```

The API is a resource server: it accepts an access token issued by an
authorization server and validates that token before serving the protected
resource.

For a JWT, validation includes more than Base64 decoding. The resource server
must verify the signature using a trusted key and validate claims such as
issuer, expiration, not-before and audience. A signed JWT normally provides
integrity, not secrecy, so sensitive data should not be placed in its claims.

Boot can configure standard issuer and audience validation:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://identity.example.com
          audiences: payments-api
```

The token may carry an application-specific claim:

```json
{
  "iss": "https://identity.example.com",
  "sub": "user-1842",
  "aud": ["payments-api"],
  "exp": 1788778800,
  "permissions": ["PAYMENT_WRITE"]
}
```

Claims are trusted only after signature and claim validation succeeds.

### Configuring the API chain

This configuration maps `permissions` directly to authorities and protects the
API with stateless bearer authentication:

```java
@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities =
                new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("permissions");
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    @Order(1)
    SecurityFilterChain api(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        return http
                .securityMatcher("/api/**")
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health/readiness").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/**")
                            .hasAuthority("PAYMENT_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/payments/**")
                            .hasAuthority("PAYMENT_READ")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(converter)))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(
                                new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(
                                new BearerTokenAccessDeniedHandler()))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain fallback(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                .build();
    }
}
```

For the POST request, the flow is:

1. `FilterChainProxy` selects the `/api/**` chain.

2. The bearer filter extracts the token.

3. The JWT decoder verifies the signature and required claims.

4. `JwtAuthenticationConverter` creates an `Authentication` whose authorities
   include `PAYMENT_WRITE`.

5. Request authorization evaluates its matchers in declaration order. The
   first matching POST rule requires that authority.

6. If the rule succeeds, the filter chain continues to `DispatcherServlet`.

The fallback chain prevents requests outside `/api/**` from accidentally
remaining unprotected. Within each authorization block, specific matchers must
appear before broader matchers because the first matching rule applies.

With the default converter, standard `scope` or `scp` claims become authorities
prefixed with `SCOPE_`. The explicit converter is needed here only because the
contract uses a custom `permissions` claim and exact authority names.

### The security context lasts for the request

After successful authentication, the `SecurityContext` holds the resulting
`Authentication`. In the usual servlet model it is available through
`SecurityContextHolder` on the request thread and is cleared when the request
finishes.

Do not store an `Authentication` in a singleton field. Do not assume it follows
work submitted to another executor. Use Spring's context-aware wrappers when
infrastructure truly needs propagation, or pass the minimal business identity
explicitly in an asynchronous command.

### Other authentication mechanisms

Different mechanisms produce the same kind of authenticated result:

| Mechanism | Main path |
|---|---|
| bearer JWT | bearer filter → JWT decoder/validator → converter → `Authentication` |
| username and password | authentication filter → `AuthenticationManager` → `AuthenticationProvider` → `Authentication` |
| existing HTTP session | security-context repository loads the previously stored context |

For username/password authentication, a `UserDetailsService` loads identity
data and a `PasswordEncoder` verifies the submitted password. Store passwords
with an adaptive one-way encoder such as bcrypt, scrypt, PBKDF2 or Argon2.
Never store reversible passwords or fast unsalted hashes.

Session authentication stores server-side context associated with a cookie.
Bearer-token authentication validates a token on each request and normally
does not use a login session. JWT is only a token format; it is not encryption
and does not make revocation or key rotation disappear.

### CSRF and CORS depend on browser behavior

CSRF is a risk when a browser automatically attaches authentication, especially
cookies, to a cross-site request. A server-side session can be stateless from a
business perspective and still need CSRF protection because the browser sends
the cookie automatically.

The API chain above disables CSRF only because it expects a bearer token that
the client code deliberately places in the `Authorization` header and does not
also authenticate through cookies. If cookies authenticate the request, keep
CSRF protection unless the complete threat model justifies another design.

CORS controls whether browser JavaScript from one origin may use a response
from another origin. It is not authentication and does not protect
server-to-server traffic. CORS processing must occur early enough that a valid
preflight request is not rejected as though it were an unauthenticated business
request.

### Who produces 401 and 403

An invalid or missing credential for a protected request leads to
authentication failure. An `AuthenticationEntryPoint` produces the **401**
response.

An authenticated caller without the required authority fails authorization.
An `AccessDeniedHandler` produces the **403** response.

Those failures occur in the security filter chain, before MVC. A
`@ControllerAdvice` cannot handle them because no controller invocation has
started. If the API uses one problem-details schema, configure security handlers
and MVC exception handling to write compatible bodies at their respective
layers.

---

## 8. API boundaries, validation and exceptions

Once the security chain continues, `DispatcherServlet` selects a controller.
The controller is an adapter between HTTP and an application use case.

### Keep the controller focused on HTTP

```java
public record CreatePaymentRequest(
        @NotNull UUID debtorAccountId,
        @NotNull UUID creditorAccountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency) { }

@RestController
@RequestMapping("/api/payments")
class PaymentController {
    private final PaymentApplicationService payments;

    PaymentController(PaymentApplicationService payments) {
        this.payments = payments;
    }

    @PostMapping
    ResponseEntity<PaymentResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {

        CreatePaymentCommand command = new CreatePaymentCommand(
                idempotencyKey,
                authentication.getName(),
                request.debtorAccountId(),
                request.creditorAccountId(),
                request.amount(),
                request.currency());

        PaymentResult result = payments.create(command);

        return ResponseEntity
                .created(URI.create("/api/payments/" + result.id()))
                .body(PaymentResponse.from(result));
    }
}
```

The controller reads HTTP-specific input, converts it to an application command
and turns the result into an HTTP response. It does not own payment state
transitions or database transactions.

### Transport validation and business validation

`@Valid @RequestBody` runs Bean Validation while MVC resolves the controller
argument. It can reject a missing field, malformed value or simple structural
constraint before the controller method runs.

Business rules require domain state and context, for example:

- the debtor account is active and belongs to the caller;

- the balance covers the debit;

- the daily transfer limit has not been exceeded;

- the currency and payment rail are compatible.

Those checks belong in the application or domain operation, often within the
same transaction and concurrency control as the write. Checking current
balance in a controller and updating it later creates a time-of-check/
time-of-use race.

Method validation can apply constraints to service arguments and return values
when it is enabled, but it does not replace domain rules that need current
persistent state.

### Keep persistence entities behind the API contract

Returning JPA entities directly couples JSON to persistence mappings. It can
trigger lazy loading during serialization, expose relationships accidentally
and make the external contract change when the schema model changes.

Use request or command types at input and response projections at output. This
does not require one DTO for every method call; introduce a boundary type where
ownership, contract or change rate differs.

### Give failures a stable response shape

A useful error body has a stable machine-readable code and enough safe context
to correlate the failure:

```json
{
  "type": "https://errors.bank.example/payment-conflict",
  "title": "Payment could not be completed",
  "status": 409,
  "code": "PAYMENT_STATE_CONFLICT",
  "correlationId": "01J...",
  "fieldErrors": []
}
```

Spring's `ProblemDetail` represents RFC problem details and allows additional
properties. Keep `code` stable even if human-facing text changes. Never expose
stack traces, SQL, credentials, token claims, internal hostnames or key
material.

`@RestControllerAdvice` handles failures that enter MVC's exception-resolution
path:

```java
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(PaymentNotFound.class)
    ProblemDetail notFound(PaymentNotFound failure) {
        ProblemDetail problem = ProblemDetail.forStatus(404);
        problem.setTitle("Payment not found");
        problem.setProperty("code", "PAYMENT_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail conflict(OptimisticLockingFailureException failure) {
        ProblemDetail problem = ProblemDetail.forStatus(409);
        problem.setTitle("Payment state changed concurrently");
        problem.setProperty("code", "PAYMENT_STATE_CONFLICT");
        return problem;
    }
}
```

Map expected failures deliberately. For an unexpected exception, log the stack
once at the owning boundary with a correlation or trace identifier and return a
generic 500. Logging it again in every layer creates duplicates without adding
evidence.

Security-filter failures remain owned by the handlers described in §7; they do
not pass through this advice.

### Choose status codes as part of the contract

| Status | Typical meaning here |
|---|---|
| `400 Bad Request` | malformed representation or request constraint failure |
| `401 Unauthorized` | authentication missing or invalid |
| `403 Forbidden` | authenticated identity lacks permission |
| `404 Not Found` | resource absent, or deliberately concealed from another tenant |
| `409 Conflict` | current resource state conflicts with the operation |
| `422 Unprocessable Content` | structurally valid input fails a semantic contract, when the API uses this distinction |
| `429 Too Many Requests` | rate or quota exceeded |
| `503 Service Unavailable` | service is temporarily unable to handle the request |

The status is only one part of the contract. Also define the stable error code,
whether the outcome is retryable and whether repeating the request requires an
idempotency key.

---

## 9. Method authorization and ownership

Request authorization protects an HTTP route. Method authorization protects an
application operation regardless of whether it was reached from MVC, a message
listener, a scheduler or another service bean.

### Apply authorization at the right layers

```text
security filter: may this authority enter POST /api/payments/**?
controller:      translate the authenticated request into a command
service proxy:   may this caller perform this operation on this account?
domain/data:     is the state transition legal and scoped to the tenant?
```

The checks are related but not interchangeable. A route rule is useful and
cheap, but the URL does not always contain enough information for account
ownership or transaction-limit decisions. The service operation has the
business arguments and remains protected when a new entry point is added.

### Enable and apply method security

The security starter does not activate method authorization by itself:

```java
@Configuration
@EnableMethodSecurity
class MethodSecurityConfiguration { }
```

`@EnableMethodSecurity` installs advisors for annotations such as
`@PreAuthorize` and `@PostAuthorize`.

```java
@Service
class AccountApplicationService {

    @PreAuthorize("hasAuthority('PAYMENT_WRITE') " +
                  "and @accountPolicy.canDebit(authentication, #accountId)")
    public PaymentResult debit(UUID accountId, Money amount) {
        return performDebit(accountId, amount);
    }
}
```

`hasAuthority("PAYMENT_WRITE")` checks the exact granted string.
`hasRole("OPS")` conventionally checks for `ROLE_OPS`. Capability-oriented
authorities tend to remain more stable than organization-specific role names.

Move object-level logic into a typed policy bean rather than building a long
expression:

```java
@Component("accountPolicy")
class AccountPolicy {
    private final AccountAccessRepository access;

    AccountPolicy(AccountAccessRepository access) {
        this.access = access;
    }

    public boolean canDebit(
            Authentication authentication,
            UUID accountId) {

        return authentication != null
                && authentication.isAuthenticated()
                && access.mayDebit(authentication.getName(), accountId);
    }
}
```

The expression remains a readable declaration, while the policy can be tested
as ordinary Java. Missing data and dependency failures should deny access unless
the risk model explicitly defines another safe outcome.

### Before, after and filtering annotations

| Annotation | Decision point |
|---|---|
| `@PreAuthorize` | before invocation; usually the clearest choice |
| `@PostAuthorize` | after invocation, with access to `returnObject` |
| `@PreFilter` | removes unsupported elements from eligible input collections |
| `@PostFilter` | removes unsupported elements from eligible result collections |

`@PostAuthorize` is unsuitable when running the method has already exposed
data or produced an irreversible side effect. Filtering is not rejection: an
atomic batch containing one unauthorized item should normally fail, not
silently process the rest. Post-filtering also cannot make an unscoped database
query safe or preserve correct pagination.

Useful expression values include `authentication`, method arguments such as
`#accountId`, bean references such as `@accountPolicy`, and `returnObject` for
post-authorization. The expression itself should be fixed application code;
never evaluate untrusted text as SpEL.

### Method security is proxy-based

Method-security advisors run when a call crosses the Spring proxy. The same
constraints described in §5 apply: self-invocation, objects created with `new`,
non-interceptable methods and calls during initialization can bypass the
advisor.

Authorization on the service method is still not the only safeguard. Tenant or
account ownership should constrain the data-access predicate, and the domain
operation should reject illegal state transitions. Each check protects a
different failure mode.

### Test the access matrix

For a protected money-moving operation, test at least:

| Case | Expected result |
|---|---|
| missing or invalid credential | 401 from the security chain |
| valid identity without route authority | 403 before MVC |
| valid authority but another tenant's account | denial at method/data policy |
| permitted identity and owned account | operation reaches business code |

Also verify that audit records identify the action and decision without storing
credentials or sensitive token contents.

---

# Part III. Persistence and consistency

Part III follows one state-changing operation from Java objects to durable
effects:

```text
call service proxy
  -> transaction begins
  -> service method
  -> repositories load managed entities
  -> business rules change their state
  -> concurrency control protects competing writes
  -> the persistence context flushes SQL
  -> the database commits
  -> other systems learn about the committed result
```

Chapters 10–12 assume that a database transaction surrounds the operation and
focus on JPA, SQL and concurrent writers. Chapter 13 explains that transaction
boundary. Chapter 14 begins where a single database transaction stops.

## 10. JPA and the persistence context

### The layers involved

Four layers participate in a typical Spring Data JPA call:

| Layer | Responsibility |
|---|---|
| Spring Data JPA | creates repository implementations and derives or runs queries |
| Jakarta Persistence (JPA) | defines entities, `EntityManager`, JPQL and persistence-context semantics |
| Hibernate | commonly implements JPA and translates managed state and queries into SQL |
| JDBC and the database | execute SQL and provide the actual transaction and constraints |

The repository abstraction removes repetitive data-access code; it does not
remove SQL or database behavior. Correctness still depends on transaction
boundaries, constraints, indexes and the queries that actually execute.

### A small entity and repository

```java
@Entity
@Table(name = "payment",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_payment_tenant_reference",
           columnNames = {"tenant_id", "reference"}))
class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    protected Payment() { }

    Payment(String tenantId, String reference, BigDecimal amount) {
        this.tenantId = tenantId;
        this.reference = reference;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    void settle() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not pending");
        }
        status = PaymentStatus.SETTLED;
    }

    UUID id() { return id; }
}

enum PaymentStatus { PENDING, SETTLED }
```

Mapping annotations describe how fields correspond to persisted state. Schema
migrations should still own production DDL; entity annotations are not a
substitute for reviewed constraints and indexes.

The protected no-argument constructor exists for the provider. The domain
method protects the state transition instead of exposing a public status
setter.

```java
interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByTenantIdAndId(String tenantId, UUID id);
}
```

Spring Data creates the repository implementation at runtime. Query names are
convenient APIs, but generated SQL must still be inspected when performance or
locking matters.

### The persistence context is a unit of work

Assume a transaction is already active around this service method:

```java
@Transactional
public void settle(String tenantId, UUID paymentId) {
    Payment payment = payments.findByTenantIdAndId(tenantId, paymentId)
            .orElseThrow();

    payment.settle();
}
```

The repository loads `Payment` through the transaction-associated
`EntityManager`. The returned entity is **managed** by its persistence context.
Changing its field does not require another `save` call. At flush, Hibernate's
dirty checking detects the change and produces an `UPDATE`.

```text
transaction-associated EntityManager
  -> persistence context
       -> identity map: row id -> managed Java object
       -> original/current state for dirty checking
       -> pending inserts, updates and deletes
  -> JDBC
  -> database transaction
```

Within one persistence context, the same entity identity normally resolves to
the same Java instance:

```java
Payment first = entityManager.find(Payment.class, id);
Payment second = entityManager.find(Payment.class, id);

assert first == second;
```

This first-level cache is not a general query cache. A JPQL query can still
execute SQL even when its result contains an entity already managed by the
context.

The injected `EntityManager` in a Spring singleton is normally a shared proxy
that delegates to the manager associated with the current transaction. The
underlying persistence context and its entities are not thread-safe and must
not be passed between concurrent threads.

### Entity states

An entity has a state relative to a persistence context:

| State | Meaning |
|---|---|
| transient | newly constructed and not associated with a context |
| managed | tracked by the current context; changes may be flushed |
| detached | has persistent identity but is no longer tracked by this context |
| removed | managed and scheduled for deletion |

```text
new Payment(...) --persist--> managed --detach/close--> detached
database row -----find-------> managed --remove-------> removed
```

`merge(detached)` copies detached state into a managed instance and returns that
managed instance. It does not make the supplied object managed:

```java
Payment managed = entityManager.merge(detached);
assert managed != detached;
```

For an update request, loading the current managed entity and applying an
explicit command is usually safer than merging a graph received from a client.
It avoids copying stale or unauthorized fields and makes the intended mutation
visible.

### Flush and commit are different events

Flush synchronizes pending persistence-context work to SQL. Commit makes the
database transaction durable according to the database contract.

A flush commonly occurs before transaction commit and may also occur before a
query whose result depends on pending changes. SQL can therefore run before the
service method returns. A constraint can fail during flush, and a later
rollback still undoes SQL that was already flushed.

```java
Payment payment = payments.save(command.toPayment());
payments.flush(); // force SQL and surface a database constraint here
```

Tests that claim to verify mappings or constraints should flush. When they also
need to prove a database round trip, clear the context and reload:

```java
entityManager.flush();
entityManager.clear();

Payment reloaded = payments.findById(payment.id()).orElseThrow();
```

### What `save` does

Spring Data JPA uses entity state detection to decide whether `save` should
call `EntityManager.persist(...)` for a new entity or
`EntityManager.merge(...)` for an existing one.

Use `save` when introducing a new entity through the repository. Do not add a
second `save` merely to persist a change to an entity that is already managed
inside the transaction; dirty checking owns that update.

Bulk JPQL or SQL updates are different. They change rows directly and bypass
managed objects, callbacks and ordinary dirty checking. Flush compatible
pending work first and clear or refresh any entities that may now be stale.

### Keep the context bounded

A persistence context keeps references and tracking information for every
managed entity. Processing hundreds of thousands of rows in one context
consumes heap and makes dirty checking expensive.

For batch work, use bounded chunks and call `flush()` and `clear()` between
them. Remember that clearing bounds memory but does not create a new database
transaction. Separate commits require separate transaction boundaries, which
also changes atomicity and restart behavior.

Entity equality also needs deliberate design. Generated identifiers may be
`null` before persistence, and a hash code must not change while an entity is a
key in a `HashMap` or member of a `HashSet`. Avoid including mutable fields or
lazy relationships in `equals`, `hashCode` or `toString`; there is no universal
generated-id equality template that is safe for every model.

---

## 11. Mapping, fetching and query performance

The useful performance question is not “is this relationship lazy?” It is:
**what SQL and how many rows will this use case execute?**

### Map ownership deliberately

An ORM association should reflect an aggregate or ownership relationship, not
every foreign key. Across aggregate boundaries, storing an identifier and using
an explicit query is often safer than navigating a large mutable object graph.

For a bidirectional relationship, one side owns the foreign-key update.
`mappedBy` names the Java field on that owning side:

```java
@Entity
class Payment {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id")
    private PaymentBatch batch;

    void attachTo(PaymentBatch batch) {
        this.batch = batch;
    }
}

@Entity
class PaymentBatch {
    @OneToMany(mappedBy = "batch",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    void add(Payment payment) {
        payments.add(payment);
        payment.attachTo(this);
    }
}
```

The helper maintains both Java sides; the owning `Payment.batch` field controls
the foreign key.

Cascade propagates entity-manager operations from parent to child.
`orphanRemoval = true` deletes a child removed from an owned relationship.
Neither feature should cross a shared-reference or aggregate boundary merely
for convenience.

### Choose a fetch plan per use case

Static fetch settings are defaults, not a complete query plan. Broad eager
mapping over-fetches data. Unplanned lazy navigation can create extra queries
or fail after the persistence context closes.

Useful query-specific tools include:

- JPQL fetch joins for a bounded relationship needed immediately;

- `@EntityGraph` for a named or repository-level fetch plan;

- DTO or interface projections for read-only views;

- batch fetching when several lazy references of the same kind are needed;

- separate queries when one large join would multiply too many rows.

### Recognize N+1

N+1 starts with one query for N parents and then issues another query while
navigating each parent's association:

```java
List<Payment> payments = repository.findByStatus(PENDING); // one query

for (Payment payment : payments) {
    log.info("rail={}", payment.getRail().getName());       // up to N queries
}
```

If the use case needs each rail, fetch it deliberately:

```java
@Query("""
       select p from Payment p
       join fetch p.rail
       where p.status = :status
       """)
List<Payment> findWithRail(PaymentStatus status);
```

For a read-only list, a projection often makes the requested columns clearer
and avoids managed entities:

```java
record PendingPaymentRow(
        UUID id, String reference, String railCode, BigDecimal amount) { }

@Query("""
       select new com.bank.read.PendingPaymentRow(
           p.id, p.reference, r.code, p.amount)
       from Payment p join p.rail r
       where p.status = :status
       """)
List<PendingPaymentRow> findPendingRows(PaymentStatus status);
```

N+1 is an access-pattern problem, not proof that lazy loading itself is wrong.
An eager mapping can still issue secondary selects or retrieve far more data
than the use case needs.

### Avoid row multiplication

Joining a parent to two to-many collections multiplies result rows. Ten
payments with five audit entries each can already produce fifty rows for one
batch before Hibernate reconstructs the objects.

Use multiple bounded queries, batch fetching, projections or a dedicated read
model when a single join explodes. Fewer SQL statements do not automatically
mean less database or network work.

Collection fetch joins also interact badly with pagination because SQL pages
rows while the API usually pages parent entities. A safer pattern is to page
parent identifiers first, then fetch the required graph for those identifiers.

For large ordered result sets, keyset pagination avoids scanning and discarding
an ever-growing offset:

```sql
select id, created_at, status, amount
from payment
where tenant_id = :tenant
  and (created_at, id) < (:last_created_at, :last_id)
order by created_at desc, id desc
fetch first :limit rows only
```

The sort needs a stable unique tie-breaker such as `id`.

### Write paths have query plans too

JDBC batching reduces round trips for compatible inserts and updates. It still
requires a bounded persistence context, and identifier generation can affect
whether inserts can be batched. Measure the driver's actual batches.

Bulk update/delete is efficient for set-based work but bypasses managed entity
state and often bypasses callbacks or ordinary version handling. Define those
semantics explicitly and clear stale context state afterward.

### Keep reads inside an explicit boundary

Open Session in View keeps the persistence context open through web response
rendering. It makes lazy navigation convenient, but can hide N+1 in serializers
and move queries outside the service transaction.

For APIs, a strong default is to disable OSIV and create the required response
DTO inside a deliberate service or query boundary. Disabling OSIV does not fix
a missing fetch plan; it makes the missing plan visible sooner.

Second-level and query caches add a freshness and invalidation contract. They
can help stable reference data, but rapidly changing balances, limits and
authorization state require stronger justification. Cache explicit DTOs or
values rather than leaking managed entities across contexts.

### Diagnose from evidence

For a slow repository operation:

1. Separate connection-pool wait from SQL execution time.

2. Count SQL statements and rows returned.

3. Inspect the generated SQL and bound-value cardinality safely.

4. Examine the database execution plan with representative statistics.

5. Check indexes, sorts, joins and lock waits.

6. Measure the revised query under realistic data volume.

An index recommendation is meaningful only when it follows from the predicate,
ordering and observed plan.

---

## 12. Concurrent writes and locking

A transaction makes its own writes atomic. It does not automatically prevent
two transactions from making conflicting decisions from the same old state.

### The lost-update problem

```text
T1 reads balance 100
T2 reads balance 100
T1 writes 90
T2 writes 80
final balance 80: T1's debit was overwritten
```

The application needs a concurrency strategy that matches the invariant and
expected conflict rate.

### Optimistic locking

Add a version column to an entity whose updates must detect concurrent change:

```java
@Version
private long version;
```

Hibernate includes the expected version in the update:

```sql
update payment
set status = ?, version = version + 1
where id = ? and version = ?
```

If another transaction has already advanced the version, zero rows are updated
and JPA reports an optimistic-lock failure. This works well when conflicts are
uncommon and blocking readers would be wasteful.

A retry must rerun the complete business operation in a fresh transaction:
reload current state, reevaluate the rules and attempt the transition again.
Retrying only `save` repeats a decision made from stale state.

### Pessimistic locking

A pessimistic write lock asks the database to lock selected rows, commonly
using `FOR UPDATE`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Optional<Account> findForUpdate(UUID id);
```

The lock is held until transaction completion. Keep that transaction short,
set a lock timeout and never wait on a remote service while holding the lock.
Exact SQL and lock coverage depend on the database and provider, so test against
the production database engine.

### Atomic conditional updates

When an invariant fits in one SQL predicate, one statement can avoid a
read-modify-write race:

```sql
update account
set available_balance = available_balance - :amount
where id = :id
  and available_balance >= :amount
```

One affected row means the debit succeeded. Zero requires a defined outcome
such as missing account, insufficient balance or concurrent change. The
statement protects the numeric invariant; the surrounding transaction still
needs ledger, audit and idempotency behavior.

### Unique constraints are concurrency controls

An application-level “exists then insert” check races. A database unique
constraint decides correctly when two transactions attempt the same business
key concurrently. Catch and translate the resulting constraint violation into
the domain outcome expected by the API.

### Deadlocks and retries

A deadlock is a wait cycle:

```text
T1 holds account A and waits for B
T2 holds account B and waits for A
```

The database aborts a victim. Reduce deadlocks by acquiring resources in a
consistent order, keeping transactions small and indexing write predicates so
they do not lock more rows than intended.

Retry the complete idempotent unit with bounded backoff and jitter. A retry
cannot safely repeat an external side effect that escaped the database
transaction.

### Choose from the invariant

| Situation | Likely starting point |
|---|---|
| uncommon edits to an ordinary entity | optimistic `@Version` |
| short, hot critical section | pessimistic lock with timeout |
| simple numeric or state predicate | atomic conditional update |
| command deduplication | unique business key or claim row |
| append-only financial truth | immutable ledger entries plus a derived balance |

The annotation follows the invariant; it does not define it.

---

## 13. Spring transactions

Chapter 10 assumed a transaction-associated persistence context. This chapter
explains how Spring opens that transaction, what joins it and how it completes.

### What `@Transactional` controls

`@Transactional` is metadata read by a Spring AOP interceptor. When a call
crosses the proxy, the interceptor asks a transaction manager to begin or join
resource work, calls the target, and then commits or rolls back.

```text
caller
  -> transactional proxy
       -> transaction manager begins or joins
       -> service method
            -> repositories share transaction-bound resources
       -> flush and commit, or roll back
```

For an imperative JPA application, `JpaTransactionManager` normally associates
an `EntityManager` and its database connection with the current thread.
Repository calls on that thread can share them. Work moved to another thread
does not silently join the transaction. Reactive transaction managers instead
use the reactive context.

The database supplies atomicity, isolation and durability. Spring supplies a
consistent way to delimit the work. A rollback undoes enlisted database work;
it does not rewind Java fields, retract an email or cancel an HTTP request.

Proxy rules from §5 still apply. Self-invocation and objects created directly
with `new` do not gain transaction behavior from an annotation.

### Put the boundary around the use case

```java
@Service
class TransferService {
    private final AccountRepository accounts;
    private final LedgerRepository ledger;

    TransferService(AccountRepository accounts, LedgerRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional
    public TransferId transfer(TransferCommand command) {
        List<Account> pair = accounts.findBothForUpdate(
                command.debtor(), command.creditor());

        Account debit = requireAccount(pair, command.debtor());
        Account credit = requireAccount(pair, command.creditor());

        debit.debit(command.amount());
        credit.credit(command.amount());

        LedgerEntry entry = ledger.save(
                LedgerEntry.forTransfer(command));
        return entry.transferId();
    }
}
```

The two account changes and ledger entry form one local invariant. They should
commit together or not at all. Locks are acquired in a stable repository-defined
order, and the method performs no remote network call while holding them.

When the proxied method is called:

1. The transaction interceptor begins a physical database transaction.

2. Repository calls share its persistence context and connection.

3. The service changes managed entities and persists the ledger entry.

4. A normal method return triggers flush; constraints can still fail here.

5. If flush succeeds, the database commits before the proxy returns success.

6. A matching failure causes rollback instead.

### Logical scopes and propagation

Each transactional method creates a logical transaction scope. Propagation
decides how that scope relates to an existing physical transaction.

| Propagation | Behavior |
|---|---|
| `REQUIRED` | join the current transaction or create one; this is the default |
| `REQUIRES_NEW` | suspend the current transaction and start an independent one |
| `NESTED` | create a savepoint in one physical transaction when the manager supports it |
| `SUPPORTS` | join if one exists, otherwise run without a transaction |
| `MANDATORY` | fail unless a transaction already exists |
| `NOT_SUPPORTED` | suspend any current transaction and run without one |
| `NEVER` | fail if a transaction exists |

With `REQUIRED`, inner and outer logical scopes normally share one physical
transaction. If an inner scope marks it rollback-only and the outer method
catches the exception, the final commit still cannot succeed. Spring throws
`UnexpectedRollbackException` rather than reporting a commit that did not
happen.

```java
@Transactional
public void outer() {
    try {
        riskService.recordRisk(); // REQUIRED; marks shared transaction rollback-only
    } catch (RuntimeException ignored) {
        // catching does not clear rollback-only
    }
}
```

`REQUIRES_NEW` has an independent commit outcome but needs another connection
while the outer transaction may still hold one. Under load, nested use can
exhaust the pool. `NESTED` uses a savepoint; it is not an independent commit and
cannot survive an outer rollback.

### Rollback rules

By default, Spring rolls back for `RuntimeException` and `Error`, not ordinary
checked exceptions. Add `rollbackFor` when a checked exception must abort the
unit of work:

```java
@Transactional(rollbackFor = SettlementFileException.class)
public void importSettlement(Path file) throws SettlementFileException {
    // parse and persist settlement data
}
```

A caught exception does not cross the interceptor, so it cannot trigger a
rollback rule there. Translate and rethrow when the transaction must fail, or
mark it rollback-only deliberately. Do not catch an unexpected exception, log
it and return success from a money-moving operation.

### Isolation and concrete invariants

Isolation controls what one database transaction may observe of another.

| Isolation concern | Example |
|---|---|
| dirty read | observe another transaction's uncommitted change |
| non-repeatable read | reread one row after another transaction commits an update |
| phantom | repeat a predicate query and observe newly committed matching rows |
| write skew | concurrent transactions update different rows after reading one shared invariant |

`READ_COMMITTED` prevents dirty reads and is a common default.
`REPEATABLE_READ` and `SERIALIZABLE` provide stronger guarantees, but exact
behavior and failure modes depend on the database.

Isolation level alone is rarely the whole business answer. Version columns,
row locks, unique constraints and conditional updates express the particular
invariants described in §12. An isolation declaration usually takes effect only
when the method starts a new physical transaction; joining one does not
renegotiate it.

### Other transaction attributes

`readOnly = true` is a hint that may affect flush or database behavior. It is
not a security boundary and does not portably guarantee that writes are
impossible.

A transaction timeout bounds work according to transaction-manager support. It
does not replace pool-acquisition, SQL-statement or HTTP-client timeouts.

When several transaction managers exist, select the intended one explicitly:

```java
@Transactional(transactionManager = "ledgerTransactionManager")
public void postLedgerEntry(...) { /* ... */ }
```

Two independent local transaction managers do not create one atomic
distributed transaction.

`TransactionTemplate` is useful when the boundary is conditional or when code
must visibly continue only after the local commit:

```java
UUID paymentId = transactionTemplate.execute(status ->
        payments.save(command.toPayment()).id());

// the local transaction has completed here
return payments.findView(paymentId);
```

Programmatic demarcation makes sequencing explicit but couples the code to the
Spring transaction API. Declarative boundaries remain simpler for ordinary
use-case methods.

### Stop the local transaction at the database

Holding a database transaction open while waiting for a remote service retains
a connection and may retain locks. A network timeout is ambiguous: the peer may
have completed its work even though no response arrived. The peer also cannot
be rolled back by the local database transaction.

Keep local transactions short. When a remote effect must follow a local commit,
model the handoff explicitly. Chapter 14 covers the outbox, idempotency, sagas
and reconciliation used for that handoff.

---

## 14. Cross-system consistency

A single database transaction gives one atomic outcome for resources enlisted
in that transaction. An ordinary message publish, email, cache update or HTTP
request is another system with another failure boundary.

### The dual-write failure

Suppose a service updates a payment and publishes an event as two separate
operations:

```text
database commit succeeds
  -> process dies before publish
  -> committed payment has no event

publish succeeds
  -> database commit fails
  -> event describes state that never committed
```

Changing the order only changes which inconsistency is possible.
`@Transactional` cannot enlist an ordinary HTTP peer or non-transactional Kafka
send in the database transaction.

XA/two-phase commit can coordinate compatible resources, but it adds protocol,
coordinator and operational constraints and is not available for arbitrary
HTTP effects. Service architectures commonly use local atomicity plus durable
handoff and idempotency.

### Transactional outbox

Write the business state and a message record in the same database transaction:

```text
BEGIN
  update payment
  insert ledger entry
  insert outbox event with stable event_id
COMMIT
```

After commit, a publisher polls or streams the outbox and sends events to the
broker:

```text
database transaction commits payment + outbox row
  -> publisher claims row
  -> publisher sends event_id to Kafka
  -> publisher marks row published
```

The publisher can crash after Kafka accepts the event but before the database
records publication. It must send the same event again. The outbox therefore
provides durable handoff with **at-least-once publication**, not end-to-end
exactly-once execution.

Production operation also needs safe claiming across publisher replicas,
retry/backoff, poison-event handling, retention and monitoring of oldest
unpublished age. Preserve ordering per aggregate when consumers depend on it.

### Idempotent consumers

A consumer handles duplicate delivery by recording a stable message id in the
same transaction as its business effect:

```sql
create table processed_message (
    consumer_name varchar(100) not null,
    message_id    uuid not null,
    processed_at  timestamp not null,
    primary key (consumer_name, message_id)
);
```

```java
@Transactional
public void handle(PaymentPosted event) {
    if (!processed.tryInsert("reconciliation", event.eventId())) {
        return; // duplicate delivery is already complete
    }

    reconciliation.apply(event);
}
```

The unique constraint decides correctly under concurrent delivery. An
application-level “exists then insert” check can race.

The deduplication key must match the promise. A transport event id deduplicates
one publication; a business idempotency key deduplicates repeated commands that
may produce different transport messages.

### API idempotency

For a retryable command such as `POST /payments`, scope a high-entropy
idempotency key to the authenticated caller and operation. Atomically store a
request fingerprint and stable outcome.

| Request | Result |
|---|---|
| first use of key | one owner executes and stores the outcome |
| same key and same request | return or replay the stored outcome |
| same key with different request | reject as a conflict |
| concurrent duplicate | one owner executes; the other waits or reads the result |

An in-memory map fails across replicas and restarts. Define retention and the
response while the first attempt remains in progress.

### Sagas and compensation

A saga stores a workflow as a sequence of local transactions:

```text
PAYMENT_ACCEPTED
  -> DEBIT_POSTED
  -> SWITCH_SEND_PENDING
  -> CONFIRMED

definitive rejection
  -> REVERSAL_PENDING
  -> REVERSED

timeout
  -> OUTCOME_UNKNOWN
  -> status enquiry or reconciliation
```

Compensation is a new business action, not a database rollback. A reversal must
be auditable, can fail and may require retry or manual repair. Some effects are
irreversible, so the workflow must prevent or contain them rather than pretend
an undo exists.

An orchestrated saga keeps the state machine and next action in one workflow
owner. Choreography lets services react to events without a central
coordinator. Choreography reduces direct coupling but becomes difficult to
trace when event chains grow; regulated financial flows often benefit from an
explicit persisted state machine.

### Kafka transaction scope

Kafka transactions can atomically write Kafka records and, in a
consume-process-produce flow, coordinate consumed offsets with produced
records. That transaction does not automatically include a relational database
update.

Spring can synchronize Kafka and database transaction managers in specific
shapes, but commit order and crash behavior must be understood. It is not a
substitute for a durable cross-system design when both outcomes are required.

“Exactly once” inside Kafka does not make an external debit execute once. The
business effect still needs an idempotency or deduplication boundary.

### Reconciliation closes unknown outcomes

Timeouts, crashes and partner outages can leave a distributed result unknown.
Correctness therefore includes:

- durable states such as `UNKNOWN` or `PENDING_RECONCILIATION`;

- status enquiry or file-based reconciliation;

- immutable evidence and visible aging queues;

- safe manual repair and approval controls where required;

- metrics for stuck states, duplicates and compensation failures.

If the partner may have debited the account after the local timeout, marking
the payment failed without enquiry or reconciliation is not a complete design.

---

# Part IV. Production engineering

The earlier parts follow one request through Spring and into durable state.
Production adds three complications: work arrives concurrently, dependencies
slow down or fail, and the application changes while traffic is still flowing.

This part follows the operational loop around that request:

```text
traffic
  -> admission and deadline
  -> application resources
  -> database and remote dependencies
  -> user-visible outcome

signals describe the outcome
  -> diagnosis finds the limiting step
  -> tests reproduce the claim
  -> the next release carries the fix
```

Resilience limits the damage, observability supplies evidence, and testing
checks the promised behavior before production has to discover it.

## 15. Resilience

A resilient service does not make every dependency reliable. It limits how
long work may wait, how much failed work may enter the system and which
outcomes may be repeated safely.

### Start with one end-to-end deadline

Assume an API must answer within two seconds. That time is shared by queueing,
local work, database access, remote calls, retries and response writing:

```text
client deadline                         2000 ms
  gateway and network                    200 ms
  local queue, security and database     500 ms
  downstream work and its retries       1100 ms
  response allowance                     200 ms
```

A five-second HTTP read timeout is already wrong for this request, even if it
is a valid library default. Each wait needs a bound that fits inside the one
remaining deadline.

| Bound | What it limits |
|---|---|
| pool-acquisition timeout | waiting for a JDBC or HTTP client connection |
| connect timeout | establishing the network connection and, where applicable, TLS |
| response/read timeout | waiting for the peer after the connection exists |
| query or lock timeout | database execution or lock acquisition |
| overall deadline | all attempts, queues and local work for the operation |

Per-attempt timeouts cannot enforce the overall deadline by themselves. Before
starting a retry, calculate whether enough budget remains for another useful
attempt.

### Retry only when the failure and operation allow it

A retry needs two independent answers:

1. The failure is plausibly temporary.

2. Repeating the complete operation is safe.

| Outcome | Usual decision |
|---|---|
| validation failure or 4xx business rejection | do not retry the same request |
| authentication or authorization failure | do not treat it as transient |
| 429 or 503 with time remaining | retry only under a small, shared policy |
| connection failure during a brief failover | a bounded retry may help |
| timeout after a command was sent | outcome is unknown; enquire or reuse idempotency identity |
| optimistic conflict or deadlock | rerun the whole operation in a fresh transaction |
| programming error | fail; repetition only adds load |

Use a small attempt limit, backoff and jitter. Jitter stops many callers from
waking and retrying together. Keep one layer responsible for retries: if a
gateway, service and client each make three attempts, one request can produce
27 downstream calls.

For a state-changing command, retry safety comes from the business design in
§14: a durable idempotency key, a unique claim, or a conditional state
transition. Reusing the same key matters. Generating a new key for every
attempt turns one logical command into several commands.

### Use each resilience control for its own job

These controls solve different problems:

| Control | Question it answers | When full or open |
|---|---|---|
| rate limiter | how much work may enter during this interval? | reject, delay within a bound, or return quota information |
| bulkhead | how much concurrency may this workload occupy? | reject or degrade without consuming unrelated capacity |
| circuit breaker | is this dependency currently worth calling? | fail fast until limited probes show recovery |
| timeout | how long may this wait continue? | cancel or abandon the attempt and classify its outcome |
| retry | is another attempt both safe and useful? | stop when attempts or deadline are exhausted |

A circuit breaker moves through a small state machine:

```text
CLOSED --enough relevant failures/slow calls--> OPEN
   ^                                             |
   |                                             | wait period
   +------ successful probes <-------------- HALF_OPEN
                         failed probe ----------> OPEN
```

Only dependency-health failures should influence it. A valid insufficient-
funds response says nothing about whether the payment switch is healthy.

A semaphore bulkhead limits concurrent calls without introducing another
queue. A thread-pool bulkhead adds an executor and therefore another bounded
queue, another context-propagation point and another place where time can be
spent. Choose it deliberately rather than treating more threads as isolation.

### Compose one policy and test its observed order

For one remote call, a useful conceptual order is:

```text
overall deadline
  -> rate limit
  -> concurrency bulkhead
  -> circuit breaker
  -> retry
       -> per-attempt timeout
       -> HTTP client
```

The exact order depends on the intended measurements. A breaker may observe
each failed attempt or only the final logical-call outcome. A bulkhead permit
usually covers the entire logical operation so retries cannot escape the
concurrency limit. An overall deadline remains outside the retry loop.

Annotations from Resilience4j or Spring Retry are implemented through advice,
so the proxy rules from §5 apply. Stacking annotations is not proof of their
order. Test the number of client calls, elapsed time, exception delivered to
the caller and interaction with the transaction boundary.

### Treat overload as a finite-resource problem

Every request moves through resources with finite capacity:

```text
server threads or event loop
  -> application executor and queue
  -> JDBC pool and database sessions
  -> row locks, database CPU and I/O
  -> HTTP connection pool and downstream capacity
```

When arrival rate stays above completion rate, waiting work grows until a
queue, timeout or memory limit ends it. Increasing the thread count may only
create more contenders for a smaller connection pool.

Measure active capacity, queue depth, acquisition wait and rejection at each
stage. Then limit admission where the overload can be rejected cheaply. A
bounded queue makes overload visible; an unbounded queue converts it into high
latency and eventually memory pressure.

A fallback is safe only when it preserves the API's meaning. Returning cached
reference data with an explicit freshness rule can be safe. Returning
`PENDING` for an asynchronous payment can be safe. Inventing a zero balance,
approving without fraud evidence, or declaring failure after an ambiguous
remote timeout is not graceful degradation.

### Shut down without losing ownership of work

During deployment, the instance must stop acquiring work before the process is
killed:

```text
instance becomes unready
  -> routing and message delivery begin to drain
  -> no new application work is accepted
  -> in-flight work gets a bounded completion window
  -> listeners, executors, pools and telemetry stop
  -> process exits before the platform's hard deadline
```

Spring Boot can coordinate graceful web-server shutdown, but application-owned
executors, pollers and message listeners still need explicit lifecycle
semantics. The platform grace period must exceed the application's drain
window, including time for readiness changes to reach the router.

Any item that cannot finish before termination must be safe to redeliver or
resume. Test shutdown with real in-flight work; configuration alone cannot
prove when offsets are committed or whether a worker accepts another item
during drain.

---

## 16. Observability and diagnosis

Observability is the evidence needed to explain a user-visible outcome. It is
not the amount of text written to a log file.

### Give each signal a specific job

| Signal | Best question |
|---|---|
| metrics | is a problem widespread, and when did it begin? |
| traces | where did one request or message spend its time? |
| logs | what discrete event or decision occurred? |
| profiles and dumps | what is the process doing with CPU, memory or threads? |

A useful path starts with an alert on a user-visible indicator, narrows the
scope with metrics, uses a trace to allocate time, and opens logs or a database
plan only for the implicated component.

### Log events with bounded, safe context

Use stable fields rather than sentences that require parsing:

```json
{
  "level": "INFO",
  "service": "payment-api",
  "event": "payment_state_changed",
  "paymentId": "7f4...",
  "fromState": "PENDING",
  "toState": "SENT",
  "traceId": "a91...",
  "durationMs": 42
}
```

The event name and field meanings form a contract with dashboards and incident
queries. Log the unexpected exception once at the boundary that handles it;
lower layers can translate it or add context without repeating the same stack
trace.

Never log credentials, tokens, session identifiers, PINs, CVV, private keys or
full financial identifiers. Hashing a low-entropy secret does not make it
anonymous. Validate and bound client-supplied correlation values before
placing them in logs.

### Use metrics for rates, distributions and capacity

Counters describe event totals and are normally viewed as rates. Timers
describe call count and duration. Gauges sample current state such as active
connections or queue depth. Long-task timers describe work that is still in
progress.

Tags must have a small bounded value set. Operation, outcome, rail and region
may be suitable. Payment ids, account ids, exception messages and raw URLs are
not: each distinct value creates another time series.

Percentiles need histogram/distribution configuration and suitable buckets.
Do not average instance-local percentiles and call the result a fleet
percentile.

Start service objectives with a user-visible event. For example:

> Over 28 days, 99.9% of valid payment-status requests return the correct
> response within 400 ms.

Its supporting signals include request outcome and latency, but a financial
system also needs correctness indicators: oldest unpublished outbox event,
payments stuck in an intermediate state, reconciliation mismatches and failed
compensations. HTTP availability can be green while money is stuck.

### Trace boundaries where time or ownership changes

A trace links spans for work performed across HTTP, messaging and database
boundaries. Instrument meaningful operations rather than every private method.
Spring Boot integrates Micrometer Observation with metrics and tracing, and
auto-configured HTTP client builders carry trace propagation for supported
clients.

Custom threads, executors and manually constructed clients can lose context.
Test that propagation explicitly. Low-cardinality observation fields may feed
both metrics and traces; high-cardinality business identifiers, when policy
allows them at all, belong on traces or logs rather than metric tags.

Sampling means a trace will not exist for every request. Metrics remain the
aggregate detection mechanism. A stable business reference remains useful for
domain lookup even when two asynchronous processing attempts have different
trace ids.

### Expose Actuator as an administrative surface

Actuator endpoints can reveal configuration, bean mappings, environment
values, thread dumps and heap contents. Expose only what operators require,
authenticate sensitive endpoints, restrict their network path and audit
runtime changes such as log-level updates. A public health response does not
justify public access to every component detail.

Liveness and readiness answer different questions:

- Liveness: is this process irrecoverably broken, so restarting it may help?

- Readiness: should this instance receive new work now?

A shared database outage should not normally fail liveness and restart every
healthy application instance. An optional dependency should not automatically
make every replica unready. Decide readiness from the contract the instance
can still serve safely.

When probes run on a separate management port, that port may be healthy while
the main server cannot accept traffic. Expose or test probes on the main path
when that distinction matters.

### Diagnose latency by accounting for the time

Suppose p99 rises to 1.8 seconds while CPU remains low. One trace decomposes
the request as follows:

```text
total request                    1800 ms
  security filters                 8 ms
  executor queue                  520 ms
  JDBC pool acquisition           610 ms
  SQL execution                   120 ms
  downstream payment switch       480 ms
  serialization and other          62 ms
```

The low CPU is unsurprising: most work is waiting. Diagnose in this order:

1. Define the affected endpoint, outcome, tenant or region and time window.

2. Compare request rate, latency distribution and error rate with the previous
   healthy period.

3. Inspect saturation and wait time for server threads, executors, connection
   pools and database sessions.

4. Use traces to allocate time to queueing, SQL, locks and remote calls.

5. Inspect statement counts and execution plans only when database evidence
   points there.

6. Correlate the start of the change with deployments, configuration and
   dependency events.

7. Apply a reversible mitigation and verify recovery with the same signals.

Begin with the time that is missing, not a favorite root cause. Average
latency can hide a collapsed tail, and adding connections can move saturation
from the application into the database.

---

## 17. Production testing

A production claim should name the cheapest test that can disprove it. Plain
unit tests are ideal for domain rules; they cannot prove SQL locking, security
filters or HTTP timeout behavior. Full application tests prove wiring; they
are needlessly expensive for every branch.

### Choose scope from the boundary being tested

| Scope | What it proves | What it deliberately omits |
|---|---|---|
| plain unit | domain decisions, mapping and state transitions | Spring wiring, serialization and infrastructure |
| MVC slice | routing, JSON, validation, security integration and error shape | real database and full application startup |
| JPA slice | mappings, repository queries and flush behavior | HTTP path and unrelated beans |
| full context | application wiring and cross-layer behavior | realistic infrastructure unless supplied |
| deployed/black-box | network, process lifecycle and deployment configuration | cheap coverage of every branch |

Most business rules should remain testable without Spring:

```java
@Test
void rejectedPaymentCannotBeSent() {
    Payment payment = Payment.pending(id, amount);
    payment.reject("LIMIT_EXCEEDED");

    assertThatThrownBy(payment::markSent)
            .isInstanceOf(IllegalStateException.class);
}
```

Use the broader test only when the behavior crosses the broader boundary.

### Test one Spring boundary at a time

An MVC slice can prove request mapping, validation, serialization, security and
the public error contract while replacing the application service:

```java
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean PaymentApplicationService payments;

    @Test
    @WithMockUser(authorities = "PAYMENT_WRITE")
    void rejectsZeroAmount() throws Exception {
        mvc.perform(post("/api/payments")
                .with(csrf())
                .header("Idempotency-Key", "test-command-1")
                .contentType(APPLICATION_JSON)
                .content("""
                    {"debtorAccountId":"00000000-0000-0000-0000-000000000001",
                     "creditorAccountId":"00000000-0000-0000-0000-000000000002",
                     "amount":0,
                     "currency":"INR"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
```

Use the bean-override annotation supported by the project's Spring version.
Whether this request needs CSRF must match the real credential model and
security chain.

A JPA slice proves mappings and repository behavior. A full-context test proves
that the application's real configuration starts and its layers connect. A
mock web environment does not open a real server socket; use a random port
when the network boundary itself matters.

### Use the production database engine for database promises

An in-memory database is useful only when its differences are irrelevant to
the test. Dialect, collation, locking, constraint timing and query plans should
be tested against the production engine.

```java
@Testcontainers
@DataJpaTest
@ImportTestcontainers(PostgresContainers.class)
class PaymentRepositoryTest {
    // repository tests run against the container connection
}

interface PostgresContainers {
    @Container
    @ServiceConnection
    PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");
}
```

Spring Boot service connections can derive connection details from supported
containers. Run the same migrations as production and reset data
deterministically. Container reuse is useful only if it does not create order-
dependent tests or leak state between suites.

### Do not let a test transaction hide the result

| Trap | Why the test can pass incorrectly | Correct check |
|---|---|---|
| deferred flush | constraint SQL never executes before rollback | flush at the point being asserted |
| first-level cache | reload returns the already-managed object | flush, clear and query again |
| `REQUIRES_NEW` or another resource | test rollback does not own that commit | verify durable state and clean it explicitly |
| random-port server | server work runs in another thread/transaction | query the committed outcome and clean it explicitly |

Automatic rollback is convenient cleanup, not proof that production commit
semantics were exercised.

### Reproduce the failures the design claims to handle

Security coverage should distinguish missing or invalid credentials (401),
insufficient authority (403), cross-tenant access, and a permitted request.
`@WithMockUser` proves authorization logic but not JWT signature, issuer,
audience or claim conversion; keep a smaller integration test for decoder
configuration.

A programmable HTTP server should exercise the real wire client with delayed
responses, resets, malformed bodies, 429/503 responses and an ambiguous
timeout after request receipt. Assert the headers, idempotency-key reuse,
attempt count and total duration. Mocking the Java client method cannot prove
serialization or timeout configuration.

A concurrency test needs separate connections and a barrier that makes the
operations overlap:

```text
transaction A: read version 3 -> wait -> update -> commit
transaction B: read version 3 -> wait -> update -> commit

assert one defined winner and one optimistic conflict
assert the balance and ledger invariant after both finish
```

Do not share an `EntityManager` between those threads. For a deadlock or retry
test, force the competing lock order and assert a bounded whole-operation
retry rather than hoping a race appears.

Idempotency tests need both time and concurrency:

- same key and same payload, repeated sequentially, returns one outcome;

- concurrent first uses of the key create one business effect;

- the same key with a different payload is rejected;

- a retry after local commit returns the durable result;

- a downstream retry carries the same business identity.

Assert one ledger entry or payment row, not merely two equal HTTP responses.

### Test deployment and recovery behavior

Some claims exist only at process boundaries. Start real in-flight work, send
the termination signal used by the platform, and verify readiness, request
drain, listener stop, offset behavior and redelivery. Fault tests should also
cover database unavailability, a full connection pool and an unavailable
telemetry backend so that observability cannot take down the application.

Organize the suite by feedback time:

- Pull request: unit tests, slices, repository tests, focused contracts and a
  small number of full-context checks.

- Main branch: migrations, security integration, concurrency, idempotency and
  multi-resource failure tests.

- Pre-release or scheduled: load, soak, fault injection, graceful shutdown,
  backup/restore and reconciliation drills.

- Production: safe synthetic requests, canaries and alerts derived from the
  same user-visible objectives.

The layers are not status labels. Each one exists because a cheaper layer
cannot observe that particular failure.

---

## 18. End-to-end banking scenarios

These walkthroughs combine the earlier chapters. Each begins with an observed
failure, follows the evidence, and ends with a prevention that can be tested.

### Scenario 1: two replicas receive the same payment

A mobile client times out and repeats `POST /payments`. Two replicas receive
the same idempotency key at nearly the same time.

The authenticated client identity and operation scope the key. Both requests
attempt to create the same durable claim with the same request fingerprint; a
unique constraint chooses one owner. That owner writes the payment, ledger
entry and outbox record in one transaction. The other request reads the
in-progress or completed outcome. Reusing the key with a different payload is
a conflict.

The test sends the two requests concurrently and verifies one payment, one
ledger effect and a stable response. Metrics report claim conflicts and the
age of claims that never reach a terminal outcome.

### Scenario 2: latency rises after a deployment

The p99 of `GET /accounts/{id}/payments` rises from 250 ms to four seconds,
while CPU remains at 25 percent.

A trace shows most time waiting for a JDBC connection. Pool metrics confirm
that all connections are active, but database execution time is moderate.
Statement counts then reveal that a new serializer traverses a lazy audit
collection under Open Session in View, producing N+1 queries.

Rollback is the immediate mitigation. The fix returns a projection built
inside the query boundary and disables accidental entity traversal. A
multi-row query-count test prevents recurrence. Increasing the pool without
checking database capacity would merely move the queue.

### Scenario 3: the partner times out after receiving a debit

The request body reached the payment switch, but the response timed out. The
local service cannot tell whether the debit happened.

The service records `OUTCOME_UNKNOWN` with the same stable partner reference.
It does not send a new command identity or report a definitive failure. A
status enquiry or reconciliation file later moves the workflow to confirmed,
rejected or manual investigation. A confirmed rejection may permit a new
attempt; a confirmed debit continues the existing workflow.

The client sees pending semantics, operators see aging unknown outcomes, and
tests cover both eventual partner answers. The timeout bound limits resource
use; reconciliation supplies the missing truth.

### Scenario 4: an authorized user reads another tenant's payment

The JWT contains `PAYMENT_READ`, so the route check passes. The repository then
loads a payment by globally unique id without constraining its tenant.

The correction derives tenant identity from validated authentication and uses
`findByTenantIdAndId`. Method authorization can add defense in depth, but an
over-broad query should not load another tenant's data first. The API returns
the deliberately chosen 403 or 404 outcome without revealing ownership.

Tests cover the same authority in two tenants, and audit events record the
safe identifiers needed to investigate repeated cross-tenant attempts.

### Scenario 5: a dependency failure becomes a retry storm

The payment switch returns 503 during a deployment. The gateway, service and
HTTP client each retry three times. Attempt traffic rises far above original
traffic, queues fill and healthy endpoints begin timing out.

One layer becomes the retry owner. It uses a small jittered attempt budget
inside the end-to-end deadline and reuses the same idempotency identity. A
breaker stops calls during sustained failure, a bulkhead caps concurrent
switch work, and admission control rejects excess load before the main worker
and JDBC pools are exhausted.

Dashboards separate original-request rate from attempt rate. A delayed/failing
stub test asserts total calls and duration, while a load test proves unrelated
traffic retains capacity.

### Scenario 6: a pod terminates during message processing

A consumer has received a payment event when the pod receives `SIGTERM`. If it
commits the offset before the database effect, termination can lose the work.
If it commits after the effect, termination can cause redelivery.

The listener stops taking new records during drain. The database change and
deduplication record commit together; the offset follows the chosen processing
contract. Redelivery is therefore safe. The platform grace period allows the
normal case to finish, while an unfinished item remains available to another
consumer.

A lifecycle test terminates the process at each important point and verifies
the final database effect, offset and duplicate handling. Shutdown is correct
only when those observable outcomes agree.

---

## Primary references

Use the version selector on each site for the line selected by the project's
build.

### Spring and Spring Boot

- [Spring Boot reference](https://docs.spring.io/spring-boot/reference/)

- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)

- [Spring Framework core container](https://docs.spring.io/spring-framework/reference/core/beans.html)

- [Container extension points](https://docs.spring.io/spring-framework/reference/core/beans/factory-extension.html)

- [Spring AOP proxying](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html)

### Web and security

- [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)

- [Spring Security servlet architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)

- [Spring Security method authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

### Persistence and transactions

- [Spring transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)

- [Hibernate ORM user guide](https://docs.hibernate.org/orm/current/userguide/html_single/)

### Production and testing

- [Spring Boot Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)

- [Spring Boot observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html)

- [Spring Boot metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)

- [Spring Boot tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)

- [Spring Boot graceful shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html)

- [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/)

- [Spring Boot Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)

- [Micrometer concepts](https://docs.micrometer.io/micrometer/reference/concepts.html)

- [Resilience4j documentation](https://resilience4j.readme.io/docs)

---

## Study map

| Chapters | Companion | Exercise outcome |
|---|---|---|
| §1–§4 Boot and container | [Boot basics](spring-boot-basics.md) and [container internals](spring-container-internals.md) | trace startup and diagnose a conditional-bean failure |
| §5 proxies | [Boot basics §8](spring-boot-basics.md) | reproduce self-invocation and move the advised call across a bean boundary |
| §6 and §8 MVC/API | [Boot basics §5–§6](spring-boot-basics.md) | verify JSON, validation and problem-detail behavior |
| §7 and §9 security | [Security kit](spring-security-basics.md) | distinguish 401, 403 and cross-tenant denial through tests |
| §10–§12 persistence | [JPA performance kit](spring-data-jpa-performance.md) | prove a query budget and a concurrent-write invariant |
| §13–§14 consistency | [Transaction kit](spring-boot-transactions-deep.md) | reproduce rollback-only behavior and design an outbox/idempotent consumer |
| §15 resilience | [Resilience kit](spring-boot-resilience.md) | prove deadline, attempt count, concurrency limit and fallback behavior |
| §16 observability | [Observability kit](spring-boot-observability.md) | explain one slow request from metrics, trace and resource evidence |
| §17 testing | [Production testing kit](spring-boot-testing-deep.md) | verify database, security, concurrency and lifecycle boundaries |
| §18 scenarios | all companions | diagnose a complete failure from symptom to prevention |

The construction state of the companion material is recorded in the
[Spring senior-core checklist](senior-core-checklist.md). Readiness belongs in
each exercise kit's own scorecard.
