# Spring Container Internals — Senior Backend Anti-Fumble Kit

For a backend engineer who uses dependency injection daily but must explain
**what the container stores before objects exist**, **when extension points
run**, and **why the injected object can be a proxy instead of the raw class**.
The goal is not to memorize every method in `AbstractApplicationContext`.
It is to trace one bean from configuration metadata to a usable, lifecycle-
managed instance and diagnose startup failures without calling it “Spring
magic.”

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/inspect · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Attempt every prompt aloud before opening its `<details>` solution. The
answer card near the end is for rehearsal after the exercises, not a
substitute for retrieval practice.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §8 → §9 → §10 → §11**
>
> Full pipeline · metadata sources · definitions/registry · factory/context ·
> refresh · bean lifecycle · post-processors · proxies · `FactoryBean` ·
> configuration modes · early creation/cycles · production diagnosis.
>
> A senior container answer separates **definition time**, **factory time**,
> **instance time**, and **runtime invocation**. Confusing those phases is the
> root of most answers about bean registries, lifecycle and AOP.

> **Version line.** The core `BeanDefinition`, `BeanFactory`, post-processor,
> lifecycle and proxy model applies across Spring Framework 5.3/6/7 and Boot
> 2.7/3/4. Boot adds application preparation, auto-configuration, embedded
> server lifecycle, runners and Boot-specific events around the Framework
> context. Details and defaults can differ: for example Boot 2.6+ normally
> disallows circular references, and Framework 7 adds some configuration
> naming options. Explain the stable model first and label version-specific
> behavior separately.

---

## 0. The one picture — recipes become managed objects · 🎯 CORE PATH

```text
configuration metadata
  ├─ scanned @Component classes
  ├─ @Configuration + @Bean methods
  ├─ @Import / auto-configuration
  ├─ XML or programmatic registration
  ▼
BeanDefinitionRegistry
  └─ bean name → BeanDefinition recipe
       class/factory method · scope · constructor args · properties
       qualifiers · lazy/primary · init/destroy · role/source
  │
  ├─ BeanDefinitionRegistryPostProcessor may add definitions
  └─ BeanFactoryPostProcessor may mutate definitions
  ▼
BeanFactory (commonly DefaultListableBeanFactory)
  ├─ resolves dependencies
  ├─ creates scoped instances
  └─ manages singleton cache and lifecycle
  │
  ▼
instantiate → populate/aware → before-init BPP → init callbacks
                                      → after-init BPP → exposed bean
                                                             │
                                                             └─ may be proxy

ApplicationContext surrounds the factory with environment, resources,
events, messages, lifecycle coordination and automatic processor discovery.
```

- [ ] **0.1** 💭 Bean, bean definition, bean name and bean reference: define
  each without using them as synonyms.
- [ ] **0.2** 💭 Why is a `BeanDefinition` a recipe rather than the object?
- [ ] **0.3** 🔮 At `BeanFactoryPostProcessor` time, should ordinary singleton
  service instances already exist?
- [ ] **0.4** 🔮 Can `getBean("paymentService")` return an object whose runtime
  class is not `PaymentService`?
- [ ] **0.5** 💭 State the four phases in which a strong container answer
  should place an operation.

<details><summary>Solutions 0</summary>

- 0.1 A **bean** is an object managed by the container. A **bean definition**
  is metadata describing how the container should create/configure one. The
  **bean name** identifies a definition/product in that container. A **bean
  reference** is a dependency relationship resolved to an exposed managed
  instance.
- 0.2 It can name a class, constructor arguments, factory bean/method, scope,
  properties, qualifiers, lifecycle methods and other metadata. No service
  object needs to exist yet, and one definition can create a prototype on
  every lookup. A definition is mutable startup metadata, not runtime state.
- 0.3 Normally no. Factory post-processors must operate on metadata before
  ordinary bean creation. Calling `getBean()` there can instantiate a bean
  prematurely and make it miss later post-processing/proxying.
- 0.4 Yes. A `BeanPostProcessor`/auto-proxy creator may expose a JDK proxy or
  class-based proxy; a `FactoryBean` name resolves to its product; a factory
  method's declared/implementation type can also differ. Program to the
  contract and inspect the target/proxy deliberately when debugging.
- 0.5 **Definition time** discovers/registers/mutates recipes; **factory time**
  resolves dependencies and creates objects; **instance initialization time**
  runs processors/callbacks and may wrap the result; **runtime invocation**
  executes through the exposed reference, including proxy advice.

</details>

---

## 1. Configuration metadata and registration ⭐⭐ · 🎯 CORE PATH

- [ ] **1.1** 💭 Name four configuration sources and the common internal form
  into which they are translated.
- [ ] **1.2** 💭 What do component scanning and an `@Bean` method register?
  Do they immediately construct every application singleton?
- [ ] **1.3** 🔮 The main class is under `com.bank.payments`; a component is
  under sibling package `com.bank.shared`. Is it found by the default scan?
- [ ] **1.4** 💭 What role does `ConfigurationClassPostProcessor` play for
  `@Configuration`, `@ComponentScan`, `@Import` and `@Bean`?
- [ ] **1.5** 🛠 Register a bean programmatically before refresh using
  `GenericApplicationContext` or `BeanDefinitionRegistry`.
- [ ] **1.6** 💭 Classpath scanning versus explicit imports/registration:
  give the modularity and startup-debugging trade-off.
- [ ] **1.7** 🐛 A library performs runtime bean-definition registration after
  the context is serving traffic. Why is this unsafe?
- [ ] **1.8** 💭 How does Boot auto-configuration join the same model rather
  than creating a second container?

<details><summary>Solutions 1</summary>

- 1.1 Annotated components, `@Configuration`/`@Bean`, XML and programmatic
  registration are common sources. Readers/scanners/processors turn them into
  `BeanDefinition` entries in a registry; imported/auto-configuration classes
  become definition sources too.
- 1.2 They register metadata. Scanning identifies candidate classes;
  configuration processing registers `@Bean` factory-method definitions.
  Ordinary non-lazy singletons are normally created later during refresh's
  singleton-instantiation phase, though processors and dependencies may be
  created earlier.
- 1.3 No. Default scanning starts at the main class package and descends into
  subpackages; a sibling is outside. Move the main class upward, import the
  configuration, or set deliberate scan packages. Avoid scanning the entire
  classpath as a reflex.
- 1.4 It is a `BeanDefinitionRegistryPostProcessor` that parses configuration
  classes, discovers component scans/imports and registers additional bean
  definitions for configuration classes and `@Bean` methods before ordinary
  instances are created. It is a central bridge from annotations to registry
  metadata.
- 1.5 One shape is:

  ```java
  GenericApplicationContext context = new GenericApplicationContext();
  context.registerBean(PaymentClock.class,
      () -> new PaymentClock(Clock.systemUTC()));
  context.refresh();

  PaymentClock clock = context.getBean(PaymentClock.class);
  context.close();
  ```

  For lower-level control, build a `RootBeanDefinition` and call
  `registry.registerBeanDefinition("paymentClock", definition)` before
  refresh.
- 1.6 Scanning reduces wiring but makes discovery dependent on package/filter
  rules. Explicit imports/registration make module boundaries and startup
  ownership clearer but add configuration. Prefer a narrow, explainable
  graph; use the condition/bean reports when discovery is surprising.
- 1.7 Live concurrent mutation can leave factory caches/type matching and
  consumers inconsistent; the Framework does not officially support
  arbitrary new registration concurrently with active access. Register early
  during context preparation/definition post-processing or model runtime
  plugins outside the bean-definition registry.
- 1.8 Boot selects/imports configuration classes based on conditions and
  registers their bean definitions into the same context/factory. Back-off
  conditions decide whether definitions apply; the resulting beans pass
  through the same dependency resolution, post-processing and lifecycle.

</details>

---

## 2. `BeanDefinitionRegistry` and `BeanDefinition` ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **2.1** 💭 What does `BeanDefinitionRegistry` own, and what deliberately
  belongs to `BeanFactory` instead?
- [ ] **2.2** 💭 List useful fields carried by a bean definition.
- [ ] **2.3** 🔮 Two definitions have different names but create the same Java
  class. Are they one bean or two? What about aliases?
- [ ] **2.4** 🐛 Two definitions use the same name. What happens in Boot by
  default, and why is silent overriding risky?
- [ ] **2.5** 💭 `BeanDefinitionRegistryPostProcessor` versus ordinary
  `BeanFactoryPostProcessor`: what additional phase does the former provide?
- [ ] **2.6** 🛠 Describe how a registrar could add one client definition per
  validated external-system configuration entry without constructing clients.
- [ ] **2.7** 🔮 Does marking a definition `lazy` stop its post-processor
  definitions from being created early?
- [ ] **2.8** 💭 Definition `role` (`APPLICATION`, `SUPPORT`, `INFRASTRUCTURE`)
  and source metadata: why are they useful even when they do not change the
  service's business behavior?

<details><summary>Solutions 2</summary>

- 2.1 The registry maps bean names/aliases to definition metadata and supports
  registration/removal/query. `BeanFactory` resolves names/types,
  dependencies and scopes and creates/returns instances. In practice
  `DefaultListableBeanFactory` implements both responsibilities, but the
  interfaces separate metadata registration from object access.
- 2.2 Bean class or factory bean/method, constructor arguments, property
  values, scope, lazy flag, autowire candidate, primary/fallback/qualifiers,
  depends-on, init/destroy methods, role, source and abstract/parent metadata.
  Modern annotation processing can also attach attributes and resolved types.
- 2.3 Different names mean distinct definitions and normally distinct
  singleton instances even if the class matches. Aliases are additional names
  for the same canonical definition/bean. Type injection then sees ambiguity
  unless qualification/primary rules select one.
- 2.4 Boot defaults bean-definition overriding to false, so startup fails
  rather than silently replacing a definition. Overriding can make behavior
  depend on scan/import order and hide configuration mistakes; use explicit
  test override support or deliberate names instead.
- 2.5 A registry post-processor first receives the registry and can add/remove
  definitions; it then also participates as a factory post-processor. A plain
  factory post-processor mutates the definitions already present. Registry
  processors run early enough that newly added definitions can be processed.
- 2.6 Bind/validate external-system properties, iterate a bounded configured
  set, create a definition per logical client with constructor/property
  metadata and qualifiers, then register unique deterministic names. Defer
  sockets/secrets/client construction to normal bean creation; reject duplicate
  names and invalid configuration during startup.
- 2.7 No. Container post-processors are special infrastructure and are
  eagerly discovered/created so they can affect later beans; lazy semantics
  do not postpone the processing phase they are required to implement.
- 2.8 They improve tooling, diagnostics, condition reports and distinction
  between user beans and framework machinery. Source metadata lets an error
  point back to an annotation, method or resource instead of only a generated
  definition name.

</details>

---

## 3. `BeanFactory` versus `ApplicationContext` ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 💭 One sentence each: `BeanFactory`,
  `ListableBeanFactory`, `AutowireCapableBeanFactory`,
  `ApplicationContext`.
- [ ] **3.2** 💭 What does an `ApplicationContext` add beyond basic bean
  creation and lookup?
- [ ] **3.3** 🔮 If a plain `DefaultListableBeanFactory` contains a
  `BeanPostProcessor` definition, is it automatically discovered and
  registered exactly like an `ApplicationContext` refresh?
- [ ] **3.4** 💭 Why do applications normally depend on injected beans rather
  than call `applicationContext.getBean()` throughout business code?
- [ ] **3.5** 🔮 Can a child context see parent beans? Can a parent see child
  beans? Do child post-processors process parent beans?
- [ ] **3.6** 💭 Environment, resource loading, messages, events and lifecycle:
  give one real use of each context facility.
- [ ] **3.7** 🐛 A singleton stores the `ApplicationContext` statically to
  perform arbitrary lookups. What design/test/lifecycle problems follow?
- [ ] **3.8** 🏭 When is `AutowireCapableBeanFactory` useful for externally
  created objects, and why can it still be dangerous?

<details><summary>Solutions 3</summary>

- 3.1 `BeanFactory` creates/resolves named managed objects and dependencies.
  `ListableBeanFactory` can enumerate definitions/beans by type.
  `AutowireCapableBeanFactory` exposes wiring/initialization operations for
  existing/external instances. `ApplicationContext` is a `BeanFactory`
  superset coordinating enterprise application facilities and refresh.
- 3.2 Automatic discovery/invocation of factory/bean post-processors,
  environment/property sources, resource-pattern loading, message source/i18n,
  event publication, lifecycle start/stop and specialized web contexts,
  among other integration facilities.
- 3.3 No. A raw factory requires programmatic registration/invocation of much
  infrastructure. `ApplicationContext.refresh()` automatically discovers and
  orders post-processor beans. This is a central behavioral difference, not
  just a richer lookup interface.
- 3.4 Constructor injection makes dependencies explicit, immutable/testable
  and container-agnostic. Service-locator calls hide the graph, move missing
  dependencies to runtime branches and couple domain code to Spring.
- 3.5 Child lookup can fall back to parent; parent cannot see child. Post-
  processors are scoped per container and do not process beans in the other
  context. Name shadowing and duplicate events are common hierarchy traps.
- 3.6 Environment selects properties/profiles; resource loading reads a
  classpath/file URL uniformly; messages resolve localized text; events
  decouple in-process notifications; lifecycle coordinates servers,
  listeners and background components during start/stop.
- 3.7 Hidden global dependencies, cross-test contamination, stale context
  after reload/close, arbitrary lookup timing, bypassed module boundaries and
  hard-to-reason cycles. Use explicit injection or a narrowly designed
  adapter at a true framework boundary.
- 3.8 Frameworks can take a plugin/entity/listener created externally and ask
  Spring to inject or initialize it. But construction ownership, destruction,
  scope and double initialization can be unclear; prefer container-created
  objects or documented integration hooks.

</details>

---

## 4. Context refresh — the startup spine ⭐⭐⭐ · 🎯 CORE PATH

```text
prepare context/environment
        ↓
obtain/prepare BeanFactory and load initial definitions
        ↓
invoke BeanDefinitionRegistryPostProcessors
        ↓
invoke remaining BeanFactoryPostProcessors
        ↓
discover + register BeanPostProcessors
        ↓
initialize message source / event multicaster / listeners / context services
        ↓
pre-instantiate remaining non-lazy singletons
        ↓
start lifecycle components + publish ContextRefreshedEvent
        ↓
Boot continues: started event → runners → ready event
```

- [ ] **4.1** 💭 Why must definition post-processing precede bean post-
  processor registration and ordinary singleton creation?
- [ ] **4.2** 🔮 At which broad phase are placeholder values applied to bean
  metadata?
- [ ] **4.3** 🔮 When does an AOP auto-proxy creator need to be registered
  relative to service creation?
- [ ] **4.4** 💭 `ContextRefreshedEvent` versus Boot
  `ApplicationReadyEvent`: why are they not synonyms?
- [ ] **4.5** 🐛 An expensive network call runs in a singleton constructor and
  blocks startup for two minutes. Which refresh phase is stalled and what is
  the design fix?
- [ ] **4.6** 💭 What does lazy initialization change about startup failure
  discovery and first-request latency?
- [ ] **4.7** 🔮 If one eager singleton fails during initialization, does the
  context become ready with all other beans?
- [ ] **4.8** 🛠 Use startup logs/Actuator condition report and a debugger or
  `ApplicationStartup` recording to place a slow startup step in the pipeline.
- [ ] **4.9** 💭 Where does embedded web-server creation fit conceptually in a
  Boot web context without changing the definition/instance model?

<details><summary>Solutions 4</summary>

- 4.1 Registry/factory processors may add or mutate the recipes, including
  processor definitions. Bean post-processors must be known before application
  instances exist so every eligible bean receives the complete chain. Only
  then can eager singleton creation safely build the final graph.
- 4.2 During bean-factory post-processing; for example a placeholder configurer
  resolves values in definition metadata before ordinary bean instantiation.
  Runtime environment access and `@Value` injection have further resolution
  machinery, but the critical phase is before consuming the metadata.
- 4.3 Before the service is instantiated. Auto-proxy creators are bean post-
  processors; a service created too early can miss proxy wrapping and log “not
  eligible for processing by all BeanPostProcessors.”
- 4.4 `ContextRefreshedEvent` means the Framework context finished refresh:
  processors active, non-lazy singletons instantiated, lifecycle refreshed.
  Boot's ready event happens later, after its started event and runners, and
  means the application is ready to service requests under Boot's lifecycle.
- 4.5 The remaining-singleton initialization phase is blocked while creating
  that bean/dependency graph. Constructors should establish valid local state,
  not perform unbounded remote calls. Configure bounded clients and move
  optional warming to a managed readiness/startup workflow; fail fast when a
  required dependency truly makes startup invalid.
- 4.6 Lazy beans shift construction and configuration errors to first access,
  reduce/reshape startup cost and can cause a latency spike or runtime outage
  on the first request. Use selectively and warm critical paths; eager
  singleton creation deliberately discovers graph problems at startup.
- 4.7 No. Refresh is cancelled and the context does not reach a healthy ready
  state; already created singletons are cleaned up as the failed context
  closes. Fix the root initialization failure rather than ignoring readiness.
- 4.8 First decide whether time is definition processing, processor discovery,
  singleton graph creation, web server, runner or external initialization.
  Condition evaluation explains why auto-config definitions exist/back off;
  startup steps and thread dumps expose the slow phase. Avoid logging every
  bean at INFO in production.
- 4.9 A web-specific context performs server setup in its refresh hooks and
  manages the server as lifecycle infrastructure. Controllers/services still
  come from definitions, pass through post-processors and live in the same
  bean factory; Boot is orchestration around the same core pipeline.

</details>

---

## 5. One bean's creation and destruction lifecycle ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **5.1** 💭 Put these in broad order: instantiate, dependency population,
  aware callbacks, the before-initialization processor chain (including the
  processor that invokes `@PostConstruct`), `afterPropertiesSet`, custom init,
  and the after-initialization processor chain.
- [ ] **5.2** 🔮 Is constructor injection complete before the constructor runs?
  What about field/setter injection?
- [ ] **5.3** 💭 Why should heavy business work not live in `@PostConstruct`?
- [ ] **5.4** 🔮 Which reference is stored in the singleton cache—the raw
  target or the value returned by the final after-initialization processor?
- [ ] **5.5** 💭 `InitializingBean`, `@PostConstruct` and `@Bean(initMethod=...)`:
  compare coupling and ordering awareness.
- [ ] **5.6** 💭 Prototype destruction: does the container automatically call
  configured destruction callbacks after handing a prototype to the caller?
- [ ] **5.7** 🐛 A bean starts an unmanaged thread in `@PostConstruct` and
  never stops it. What lifecycle failures follow?
- [ ] **5.8** 💭 `Lifecycle`/`SmartLifecycle` versus initialization callbacks:
  when should each be used?
- [ ] **5.9** 🔮 On shutdown, why does dependency-aware destruction generally
  reverse creation/dependency order?

<details><summary>Solutions 5</summary>

- 5.1 Broadly: choose constructor/factory and **instantiate**; populate
  properties/dependencies; invoke aware callbacks (some context-aware ones
  arrive through processors); run the `postProcessBeforeInitialization`
  chain, where the relevant lifecycle processor invokes `@PostConstruct`;
  then call `InitializingBean.afterPropertiesSet()` and a custom init method;
  finally run `postProcessAfterInitialization`, where proxy wrapping commonly
  occurs.
  Specialized processors can hook earlier phases, so present this as the
  normal conceptual order, not every internal callback.
- 5.2 Constructor arguments must be resolved before the constructor is
  invoked, so constructor dependencies are available inside it. Fields and
  setters are populated only after instantiation; using them in the
  constructor observes defaults/null.
- 5.3 It blocks bean/context readiness, is hard to time out/retry/test, can
  force early dependency use and has unclear shutdown symmetry. Keep it to
  bounded local validation/initialization; use runners or managed lifecycle
  components for coordinated work, and durable jobs for important async work.
- 5.4 Normally the **exposed object** returned after post-processing, which may
  be a proxy. The raw target can be retained behind that proxy and special
  early-reference machinery exists for cycles, but callers should receive the
  final managed reference.
- 5.5 `@PostConstruct` is standard annotation style and low Spring coupling;
  `InitializingBean` couples the class to Spring; a configured init method
  keeps lifecycle declaration outside the class. If multiple mechanisms are
  combined, know and test the order, but usually choose one clear mechanism.
- 5.6 No. The container creates/configures a prototype but does not track its
  full lifetime after delivery; the caller must release resources or use a
  custom scope/ownership abstraction. Singleton destruction is container-
  managed on context close.
- 5.7 Thread leak on context restart/test, work continuing after dependencies
  close, no drain/interrupt contract, blocked JVM exit and lost context/MDC.
  Use a managed `TaskExecutor`, scheduler or `SmartLifecycle` with explicit
  stop and bounded shutdown.
- 5.8 Initialization establishes one valid instance. Lifecycle components
  represent running activity that can start/stop with context phases—servers,
  consumers, schedulers. `SmartLifecycle` adds auto-start and phase ordering;
  do not use `@PostConstruct` as a substitute for a controllable service.
- 5.9 Dependents must stop before the resources they use. If A depends on B,
  destroying B first can make A's cleanup fail. Lifecycle phase/dependency
  metadata helps coordinate, but external durable correctness must also
  survive forced termination.

</details>

---

## 6. Factory versus bean post-processors ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **6.1** 💭 Give the decisive one-line distinction between
  `BeanFactoryPostProcessor` and `BeanPostProcessor`.
- [ ] **6.2** 🔮 Which would you use to change a definition's scope before
  creation? Which to wrap annotated instances with a proxy?
- [ ] **6.3** 🐛 A factory post-processor calls `getBean(PaymentService.class)`
  to inspect an annotation. What can break?
- [ ] **6.4** 💭 Why should post-processor-producing `@Bean` methods often be
  `static` and have minimal dependencies?
- [ ] **6.5** 🔮 A programmatically registered `BeanPostProcessor` implements
  `Ordered`. Does the factory necessarily honor that interface?
- [ ] **6.6** 💭 `PriorityOrdered`, `Ordered`, and unordered processors: why is
  relying on incidental registration order fragile?
- [ ] **6.7** 🐛 Startup logs say a bean “is not eligible for getting processed
  by all BeanPostProcessors.” Give the likely cause and consequence.
- [ ] **6.8** 💭 How do `AutowiredAnnotationBeanPostProcessor`, lifecycle
  annotation processing and AOP auto-proxy creators demonstrate different
  jobs within the same extension family?
- [ ] **6.9** 🛠 Sketch one safe custom `BeanPostProcessor` use and the tests
  needed to avoid double wrapping.

<details><summary>Solutions 6</summary>

- 6.1 A `BeanFactoryPostProcessor` changes **configuration metadata before
  ordinary instances**; a `BeanPostProcessor` works on **new bean instances
  around initialization** and can return a replacement/proxy.
- 6.2 Scope metadata → factory post-processor. Instance proxy → bean post-
  processor/auto-proxy creator, usually returned after initialization.
- 6.3 It prematurely creates the service before the complete post-processor
  chain is registered, so injection, lifecycle callbacks or AOP proxying can
  be missed. Inspect `BeanDefinition`/class metadata without resolving the
  instance.
- 6.4 These processors must be detected extremely early. A non-static method
  requires instantiating its configuration class, which can force dependencies
  too soon and make configuration annotation processing incomplete. A static,
  low-dependency declaration avoids that lifecycle conflict.
- 6.5 No. For post-processors added programmatically to a bean factory,
  registration order controls execution and `Ordered` semantics are ignored.
  Context autodetection sorts eligible processor beans.
- 6.6 Framework infrastructure often needs strict phase precedence. Use the
  documented ordering contracts only where required, minimize custom ordering
  dependencies and test interactions. Incidental classpath/scan order can
  change after refactoring or upgrades.
- 6.7 A post-processor or one of its dependencies caused this bean to be
  instantiated during the special early phase. It may miss auto-proxying or
  other processors. Make processor declarations static/minimal, remove eager
  lookups and inspect the dependency chain that forced creation.
- 6.8 One injects annotated fields/methods, one invokes lifecycle annotations,
  and one decides whether to expose a proxy with advisors. The same extension
  interface hosts many ordered transformations; “BPP equals AOP” is too narrow.
- 6.9 Example: validate a marker annotation after initialization or wrap one
  interface with a metrics proxy. Exclude infrastructure/already-wrapped
  objects, preserve type/contracts and test eligible/ineligible beans,
  singleton identity, ordering with AOP, exceptions and context startup. Use
  existing Spring facilities before inventing a general-purpose proxy layer.

</details>

---

## 7. AOP proxy creation and the exposed bean ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **7.1** 💭 Why can the object injected into a controller differ from the
  object constructed by the service constructor?
- [ ] **7.2** 💭 JDK dynamic proxy versus class-based proxy: what type surface
  and subclass limitations matter?
- [ ] **7.3** 🔮 `paymentService.getClass() == PaymentService.class`—is this a
  safe assertion in a Spring application?
- [ ] **7.4** 🐛 Code casts an injected service interface to its concrete class
  and fails only when transactions are enabled. Diagnose.
- [ ] **7.5** 💭 Why does same-bean self-invocation bypass `@Transactional`,
  `@Async`, caching, method security and retry advice?
- [ ] **7.6** 🔮 Is every bean processed by an auto-proxy creator necessarily
  wrapped?
- [ ] **7.7** 💭 Early references and circular dependencies: why is exposing a
  raw target before final proxying dangerous?
- [ ] **7.8** 🐛 A final method is annotated with proxy-based advice. What
  differs between JDK interface and class-based proxy scenarios?
- [ ] **7.9** 🛠 How do you diagnose whether a bean is proxied and which
  advisors apply without embedding proxy checks in business logic?

<details><summary>Solutions 7</summary>

- 7.1 The constructor creates a target. An auto-proxy creator, itself a bean
  post-processor, evaluates advisors and can return a proxy from after-
  initialization processing. The singleton exposed to dependents is that
  returned reference; method calls flow through advice before reaching target.
- 7.2 A JDK proxy implements selected interfaces, so callers see the interface
  contract. A class-based proxy subclasses the target and cannot override
  private/final methods or subclass a final class. Type assumptions and method
  visibility determine what can be advised.
- 7.3 No. The runtime class may be a generated proxy/subclass. Use
  `instanceof`/contract-based code and Spring AOP diagnostic utilities in
  tests/tools when target-class inspection is truly needed.
- 7.4 Enabling transactions made Spring expose a JDK interface proxy that is
  not an instance of the implementation class. Inject/use the interface or
  deliberately select class-based proxying with its limitations; do not make
  business code depend on generated representation.
- 7.5 The proxy is outside the target. A `this.method()` call stays inside the
  target and never re-enters the exposed proxy, so interceptors cannot run.
  Put advice on an external use-case boundary or move the advised operation to
  another injected bean.
- 7.6 No. It may find no matching advisor and return the original object.
  Processing is an opportunity to transform, not proof of transformation.
- 7.7 Another bean can retain the raw object while later callers receive a
  proxy, creating inconsistent identity/advice and bypassing transactions or
  security on one path. Constructor cycles are better redesigned; enabling
  circular references is not a safe AOP strategy.
- 7.8 A JDK proxy intercepts calls through an advised interface method and
  delegates to the target even if the implementation method is final;
  class-based proxying needs to override the method, so final blocks advice.
  Private methods are not interface contract methods and are not overridable.
- 7.9 Use startup/debug AOP logs, `AopUtils.isAopProxy`,
  `AopUtils.isJdkDynamicProxy`/`isCglibProxy`, and `Advised` inspection in a
  focused diagnostic/test. Verify behavior with an external call. Keep such
  framework introspection out of domain logic.

</details>

---

## 8. `FactoryBean<T>` — factory name versus product ⭐⭐ · 🎯 CORE PATH

- [ ] **8.1** 💭 `FactoryBean<T>` versus `BeanFactory` versus an ordinary
  factory object: distinguish all three.
- [ ] **8.2** 🔮 For a `FactoryBean` named `paymentClient`, what do
  `getBean("paymentClient")` and `getBean("&paymentClient")` return?
- [ ] **8.3** 💭 What do `getObject()`, `getObjectType()` and `isSingleton()`
  tell the container?
- [ ] **8.4** 🔮 If the `FactoryBean` itself is a singleton but
  `isSingleton()` returns false, is every product lookup the same object?
- [ ] **8.5** 🐛 Type lookup behaves strangely because `getObjectType()`
  returns null before initialization. Why does early type prediction matter?
- [ ] **8.6** 💭 When is a `FactoryBean` justified instead of a simple
  `@Bean` factory method?
- [ ] **8.7** 🐛 A developer injects `FactoryBean<Client>` but expected the
  client product. How should each be requested?
- [ ] **8.8** 💭 Does `FactoryBean` mean the produced object skips ordinary
  post-processing and lifecycle?

<details><summary>Solutions 8</summary>

- 8.1 `BeanFactory` is the container API. `FactoryBean<T>` is a Spring-managed
  SPI whose bean name normally resolves to a product it creates. An ordinary
  factory is just application code unless a `@Bean` method/definition calls
  it; capitalization matters.
- 8.2 The plain name returns the **product** from `getObject()`; the `&` prefix
  dereferences the factory and returns the `FactoryBean` instance itself.
- 8.3 `getObject()` creates/returns the product; `getObjectType()` lets the
  container predict its type for autowiring/introspection; `isSingleton()`
  declares whether the product is shared/cached for the factory.
- 8.4 No. Factory scope and product singleton semantics are distinct. A shared
  factory can produce a new/non-singleton product on each lookup.
- 8.5 The container performs by-type matching before all beans are fully
  instantiated. Unknown product type can make discovery/autowiring/tooling
  less reliable or require initialization. Return the most accurate type
  possible without expensive product creation.
- 8.6 Use it for reusable container-integrated complex creation where product
  type/identity semantics and factory dereference are valuable—many framework
  proxy/client factories do this. For ordinary application configuration, a
  readable `@Bean` method is usually simpler.
- 8.7 Inject/request `Client` or the plain bean name for the product. Request
  the factory by `&paymentClient` programmatically or an explicitly qualified
  factory type when administration of the factory itself is intended.
- 8.8 No. The product obtained from a `FactoryBean` participates in the
  container's product post-processing path. The factory is also a managed
  bean. Exact product creation/destruction ownership should be checked for the
  implementation; the SPI is not a bypass around the container.

</details>

---

## 9. `@Configuration(proxyBeanMethods = ...)` and lite mode ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **9.1** 🔮 With default `proxyBeanMethods=true`, what happens when one
  `@Bean` method directly calls another `@Bean` method in the same class?
- [ ] **9.2** 🔮 What changes with `proxyBeanMethods=false`?
- [ ] **9.3** 💭 Why does full mode require a generated subclass and impose
  non-final class/method constraints?
- [ ] **9.4** 💭 Show the recommended parameter-injection form that works in
  both modes without direct inter-bean method calls.
- [ ] **9.5** 🔮 A class annotated only `@Component` contains two `@Bean`
  methods and one directly calls the other. Is full inter-bean interception
  guaranteed?
- [ ] **9.6** 💭 What startup/AOT benefits can lite configuration provide, and
  what correctness condition must hold?
- [ ] **9.7** 🐛 With lite mode, a direct method call constructs a second
  `DataSource` outside the container's singleton lookup. What resources and
  lifecycle bugs follow?
- [ ] **9.8** 💭 Does `proxyBeanMethods=false` make the container create a new
  object every time another bean injects the registered singleton?
- [ ] **9.9** 🐛 A non-static post-processor `@Bean` method forces early
  configuration creation. Why is changing only `proxyBeanMethods` not the
  main fix?

<details><summary>Solutions 9</summary>

- 9.1 Spring enhances the configuration class with a CGLIB subclass.
  Interception redirects the call through the container so a singleton
  definition returns its managed singleton instead of executing ordinary
  Java construction repeatedly.
- 9.2 The class is not enhanced for inter-bean calls. A direct Java call
  executes the method body and creates whatever it creates; it does not become
  a container lookup. Each `@Bean` method is still a definition when invoked
  by the container for its own bean.
- 9.3 Interception needs to override `@Bean` methods in a subclass. Final
  classes/methods and inaccessible methods prevent that mechanism. Full mode
  trades enhancement/startup work for managed semantics on direct calls.
- 9.4 Express the dependency as a method parameter:

  ```java
  @Configuration(proxyBeanMethods = false)
  class PaymentsConfig {
      @Bean PaymentClient paymentClient(HttpClient httpClient) {
          return new PaymentClient(httpClient);
      }

      @Bean HttpClient httpClient() {
          return HttpClient.newHttpClient();
      }
  }
  ```

  The container resolves `HttpClient`; the method does not call another
  factory method as ordinary Java.
- 9.5 No. `@Bean` methods on non-`@Configuration` components use lite
  semantics; direct calls are not intercepted for singleton enforcement.
  Prefer dedicated configuration and parameter injection.
- 9.6 It avoids runtime subclass enhancement, can reduce startup work and
  makes factory methods simpler for AOT analysis. It is correct when methods
  are self-contained and dependencies arrive as parameters rather than direct
  calls that expect container interception.
- 9.7 A second pool may open connections, miss configuration post-processing,
  metrics and destruction, leak resources and differ from what repositories
  inject. The direct object is not the registered managed singleton. Replace
  the call with parameter injection.
- 9.8 No. The registered definition's scope still controls container lookups;
  a singleton remains shared. Only user-code direct method calls lose the
  special interception semantics.
- 9.9 Processor definitions must be discovered before normal configuration
  instances. Declare the processor method `static` and minimize dependencies.
  Proxy mode addresses inter-bean calls, not the fundamental early-phase
  lifecycle conflict.

</details>

---

## 10. Ordering, early creation and circular dependencies ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **10.1** 💭 Why is an A↔B constructor cycle impossible to satisfy with
  fully initialized immutable objects?
- [ ] **10.2** 🔮 Can setter/field cycles sometimes be resolved by raw Spring
  Framework machinery? Why does that not make them good design?
- [ ] **10.3** 💭 What did Boot 2.6+ change about circular-reference defaults?
- [ ] **10.4** 🐛 Adding `@Lazy` to one side makes startup pass. What problem
  may have been deferred rather than solved?
- [ ] **10.5** 💭 `ObjectProvider<T>`: when is deliberate lazy/optional lookup
  appropriate, and when is it service locator in disguise?
- [ ] **10.6** 🔮 Does `@DependsOn("b")` inject B into A or merely control
  initialization/destruction ordering?
- [ ] **10.7** 💭 How do `@Order`, `Ordered`, `PriorityOrdered`, `@Primary` and
  `@Priority` solve different problems?
- [ ] **10.8** 🐛 A `BeanPostProcessor` autowires a large application service
  graph. Explain the early-instantiation cascade.
- [ ] **10.9** 🏭 How would you break a real service cycle without hiding it
  behind field injection?
- [ ] **10.10** 💭 Why can global lazy initialization make a production
  rollout look healthy until the first rare endpoint is called?

<details><summary>Solutions 10</summary>

- 10.1 A needs a constructed B before A's constructor; B simultaneously needs
  a constructed A. No fully initialized first object exists to pass. The
  cycle usually signals coupled responsibilities or a missing orchestration/
  event boundary.
- 10.2 When circular references are allowed, early singleton references and
  later property injection can resolve some setter cycles. One bean observes
  another before full initialization, proxy identity can be inconsistent, and
  initialization order becomes fragile. Constructor injection exposes the
  design problem honestly.
- 10.3 Spring Boot 2.6 changed the default to disallow circular references;
  current Boot also defaults `spring.main.allow-circular-references=false`.
  Enabling it is a migration escape hatch for resolvable cycles, not the
  preferred repair.
- 10.4 It inserts a deferred proxy/provider so the cycle is not resolved until
  later. Runtime invocation can still recurse, deadlock or reveal an invalid
  responsibility graph. Use it only when laziness is intentional and test the
  first real invocation.
- 10.5 Appropriate for optional plugins, scope-aware retrieval or deliberate
  deferred/streamed candidates at infrastructure boundaries. If every service
  asks a provider for arbitrary dependencies to avoid constructor design, it
  recreates hidden service location.
- 10.6 It controls creation ordering (and corresponding singleton destruction
  ordering) when no direct dependency expresses it. It does not pass B to A;
  constructor/setter parameters still define injection.
- 10.7 `Primary`/qualifiers resolve **which candidate** is injected. Ordering
  annotations/interfaces arrange **multiple selected elements** or extension
  callbacks. `PriorityOrdered` is an earlier ordering tier for framework
  infrastructure. Exact support varies by injection/processor API; do not use
  one as a substitute for another.
- 10.8 Post-processors are instantiated before application beans. Resolving
  its service dependency constructs that graph before all processors are
  registered, making those beans ineligible for auto-proxying and other
  transformations. Keep processors infrastructure-only and lazy-access any
  truly unavoidable collaborator.
- 10.9 Extract the shared rule into a third service, move use-case coordination
  above both, publish an event for a genuinely asynchronous reaction, or
  invert one dependency behind a narrow port. Preserve transaction and error
  semantics explicitly; do not merely swap constructor injection for fields.
- 10.10 Lazy definitions are not instantiated during refresh, so missing
  config, incompatible classes or constructor failures appear on first use.
  Readiness can turn green while cold paths remain broken. Eagerly validate/
  warm critical and rare-but-required integrations before taking traffic.

</details>

---

## 11. Senior production scenarios ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **11.1** 🏭 After adding a custom post-processor, transactions silently
  stop applying to several beans. Give the investigation path.
- [ ] **11.2** 🏭 Startup time jumps from 15 s to 90 s. How do you split
  definition processing, bean creation and external initialization?
- [ ] **11.3** 🏭 A bean exists in the condition report but injection says no
  candidate. What metadata/type/context issues do you inspect?
- [ ] **11.4** 🏭 An application has two contexts and receives every custom
  event twice. Explain likely hierarchy/listener behavior.
- [ ] **11.5** 🏭 A library starter unexpectedly overrides an application
  client. What should a well-behaved auto-configuration do?
- [ ] **11.6** 🏭 A service is injected as a proxy and equality/hash-based map
  lookup fails. What contract mistake may exist?
- [ ] **11.7** 🏭 First production request is slow and fails while startup and
  readiness were green. Connect this to lazy initialization.
- [ ] **11.8** 🏭 A `FactoryBean` product cannot be autowired by type until the
  factory initializes an external connection. Redesign the type prediction.
- [ ] **11.9** 🏭 A graceful shutdown closes a datasource before a consumer
  finishes. Which lifecycle/dependency controls are relevant?
- [ ] **11.10** 🏭 Give a startup-failure triage order for
  `NoSuchBeanDefinitionException`, `NoUniqueBeanDefinitionException`,
  `BeanCurrentlyInCreationException` and `BeanCreationException`.

<details><summary>Solutions 11</summary>

- 11.1 Look for “not eligible for all BeanPostProcessors,” non-static
  processor methods, eager `getBean()` calls and large processor dependencies.
  Confirm runtime proxy type/advisors and an external invocation. Ensure auto-
  proxy creator registered before targets; remove premature creation rather
  than manually wrapping transaction targets.
- 11.2 Measure refresh steps. Count/inspect scan and configuration processing,
  then identify which singleton graph/constructor/init callback is slow; use
  thread dumps/spans around approved startup operations. Separate embedded
  server and runners. Bound external calls and remove accidental eager graphs
  before reaching for global laziness.
- 11.3 Definition may be non-autowire-candidate, profile/condition differs in
  the failing context, qualifier/generic type mismatches, product type is
  unknown, bean lives only in a child, or a proxy exposes only interfaces.
  Inspect canonical name, source, resolvable type, qualifiers and context
  hierarchy—not just “bean count.”
- 11.4 Parent and child can each contain/register listeners, and event
  propagation/hierarchy can make one logical action visible more than once.
  Post-processors/listeners are per context. Scope the listener to the intended
  context or filter source/context identity; avoid duplicating configuration
  into both.
- 11.5 Use conditional back-off such as missing-bean/type/property conditions,
  specific names and infrastructure roles so an application-owned bean wins.
  Boot's default no-overriding behavior should expose collisions; starters
  should configure defaults, not silently replace user intent.
- 11.6 Equality/hash code may depend on concrete class/identity rather than a
  stable domain key and contract, or the target compares `getClass()` exactly.
  Proxies reveal that flaw. Avoid managed service objects as domain map keys;
  keep entity/value equality proxy-aware where ORM applies.
- 11.7 The required bean/integration was lazy, so refresh did not create or
  validate it. First lookup paid construction/network cost and revealed the
  error. Warm/validate required paths before readiness or make initialization
  eager and bounded.
- 11.8 Make `getObjectType()` return the predictable product interface/class
  without creating the connection/product. Split metadata/type description
  from expensive initialization and create/connect lazily inside a properly
  bounded client if semantics permit.
- 11.9 The consumer should depend on the datasource and stop intake/drain in a
  lifecycle phase before the pool is destroyed. Use `SmartLifecycle` phase,
  dependency graph/`@DependsOn` only where needed, managed executors and an
  aligned shutdown timeout. Durable replay still covers forced termination.
- 11.10 Missing: scan/import/condition/profile/name/context. Non-unique:
  candidates, qualifiers, primary, generics. Currently-in-creation: draw the
  constructor/early-lookup cycle. Creation exception: read the deepest cause
  and identify constructor/population/init/post-processor phase. Do not stop at
  the outer `UnsatisfiedDependencyException`; trace the dependency path.

</details>

---

## 12. Rapid-fire trap wall 🔮 · all keep

- [ ] **12.1** Is a `BeanDefinition` the singleton object itself?
- [ ] **12.2** Does component scanning instantiate every candidate immediately?
- [ ] **12.3** Does a `BeanFactoryPostProcessor` normally mutate bean instances?
- [ ] **12.4** Does a `BeanPostProcessor` normally add new definitions?
- [ ] **12.5** Does raw `BeanFactory` automatically discover processor beans
  exactly like `ApplicationContext`?
- [ ] **12.6** Can a bean post-processor return a different object?
- [ ] **12.7** Is the exposed singleton always the constructor-created class?
- [ ] **12.8** Does same-bean invocation pass through an external AOP proxy?
- [ ] **12.9** Does `getBean("factory")` return the `FactoryBean` itself?
- [ ] **12.10** Does `&factory` return the product?
- [ ] **12.11** Does `proxyBeanMethods=false` change a registered singleton's
  scope to prototype?
- [ ] **12.12** Are direct `@Bean` method calls intercepted in lite mode?
- [ ] **12.13** Can a final method be overridden by a class-based proxy?
- [ ] **12.14** Are constructor cycles normally resolvable through early
  references?
- [ ] **12.15** Does `@Lazy` necessarily remove the underlying logical cycle?
- [ ] **12.16** Does `@DependsOn` inject the named bean?
- [ ] **12.17** Are prototype destruction callbacks fully managed after lookup?
- [ ] **12.18** Is `ContextRefreshedEvent` the same point as Boot's
  `ApplicationReadyEvent`?
- [ ] **12.19** Should a post-processor call `getBean()` freely during factory
  post-processing?
- [ ] **12.20** Can a child context's post-processor transform a parent bean?

<details><summary>Solutions 12</summary>

- 12.1 **No. It is creation/configuration metadata.**
- 12.2 **No. It registers definitions; eager creation comes later.**
- 12.3 **No. It changes metadata before ordinary instances.**
- 12.4 **Normally no. Use a registry post-processor for definitions.**
- 12.5 **No. Context refresh supplies automatic discovery/registration.**
- 12.6 **Yes. It can return a proxy/replacement.**
- 12.7 **No. It may be a JDK/class proxy or `FactoryBean` product.**
- 12.8 **No. `this` calls bypass it.**
- 12.9 **No. It normally returns the product.**
- 12.10 **No. It dereferences and returns the factory.**
- 12.11 **No. Container scope remains singleton unless configured otherwise.**
- 12.12 **No. They execute as ordinary Java calls.**
- 12.13 **No. It cannot override final methods.**
- 12.14 **No. Constructor cycles have no first fully constructed object.**
- 12.15 **No. It may only defer resolution/failure.**
- 12.16 **No. It controls ordering.**
- 12.17 **No. The caller owns cleanup after the prototype is handed out.**
- 12.18 **No. Boot ready occurs later, after runners.**
- 12.19 **No. It risks premature creation and missed processing.**
- 12.20 **No. Post-processors are scoped to their own container.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Container pipeline? | Metadata becomes `BeanDefinition`s in a registry; registry/factory processors add or mutate recipes; the factory resolves and creates objects; bean processors initialize/wrap them; the context adds events, resources, environment and lifecycle. |
| `BeanDefinition`? | A mutable creation recipe—class/factory, arguments, properties, scope, qualifiers, lifecycle and source—not the bean instance. |
| Registry vs factory? | `BeanDefinitionRegistry` stores named recipes; `BeanFactory` resolves dependencies/scopes and creates or returns managed instances. `DefaultListableBeanFactory` commonly implements both. |
| Factory vs context? | `BeanFactory` is the core DI/object factory. `ApplicationContext` is its superset that automatically discovers processors and adds environment, resources, messages, events, lifecycle and web specializations. |
| BFPP vs BPP? | Factory post-processors change metadata before ordinary creation; bean post-processors transform instances around initialization and can return proxies. |
| Refresh order? | Load definitions → registry/factory post-processing → register bean processors → initialize context services → create remaining eager singletons → lifecycle start/context-refreshed; Boot then runs runners and publishes ready. |
| Why proxy? | An auto-proxy creator returns an advised object after target initialization, so callers receive an interface/class proxy and runtime calls can apply transactions, security, retry or other advice. |
| `FactoryBean`? | A managed factory SPI whose plain bean name returns its product; `&name` returns the factory. Factory scope and product singleton semantics are distinct. |
| `proxyBeanMethods`? | Full mode enhances configuration so direct inter-`@Bean` calls route through the container; false/lite mode treats them as ordinary Java. Parameter injection works safely in both. |
| Cycle diagnosis? | Draw the dependency/early-lookup graph, keep constructor injection, remove the responsibility cycle or introduce a deliberate orchestration/port/event boundary; do not hide it with fields or global circular-reference enablement. |

---

## Primary references

- [Spring IoC container introduction](https://docs.spring.io/spring-framework/reference/core/beans/introduction.html)
- [Container overview and metadata sources](https://docs.spring.io/spring-framework/reference/core/beans/basics.html)
- [Bean definition overview](https://docs.spring.io/spring-framework/reference/core/beans/definition.html)
- [Container extension points](https://docs.spring.io/spring-framework/reference/core/beans/factory-extension.html)
- [`ApplicationContext` capabilities and events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html)
- [Spring Boot application lifecycle and events](https://docs.spring.io/spring-boot/reference/features/spring-application.html)
- [Dependency resolution and circular dependencies](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Bean lifecycle callbacks](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html)
- [`@Configuration` and `proxyBeanMethods`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html)
- [`AbstractApplicationContext.refresh()` lifecycle](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/support/AbstractApplicationContext.html)
- [`BeanDefinitionRegistry`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/support/BeanDefinitionRegistry.html)

---

## Extensions — after the senior core

- `MergedBeanDefinitionPostProcessor`, parent/child definitions and merged
  metadata caches.
- `InstantiationAwareBeanPostProcessor`, constructor selection and early
  before-instantiation short-circuiting.
- Three-level singleton caches and `getEarlyBeanReference` internals.
- Custom scopes and scoped proxies.
- Context hierarchies in Spring MVC/management contexts in depth.
- Boot auto-configuration import selection, condition phases and failure
  analyzers.
- AOT-generated bean registrations and native-image constraints.
- Functional bean registration and module systems such as Spring Modulith.

---

## How to drill this kit

1. Draw §0 from memory and place every unfamiliar Spring interface at
   definition, factory, instance or invocation time.
2. Build one tiny `AnnotationConfigApplicationContext` with a registry
   processor, factory processor and bean processor; log only phase + bean.
3. Create one transactional/proxied service and prove external call versus
   self-invocation behavior.
4. Toggle `proxyBeanMethods`, directly call one `@Bean` method, then replace
   the call with parameter injection and predict identities before running.
5. Implement a small `FactoryBean`, inspect `name` versus `&name`, product
   type and singleton behavior.
6. Re-run §§4, 6, 7, 9, 11 and 12 blind after a week.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 full pipeline | ⬜ | ⬜ | ⬜ |
| §1 metadata sources | ⬜ | ⬜ | ⬜ |
| §2 definitions/registry | ⬜ | ⬜ | ⬜ |
| §3 factory/context | ⬜ | ⬜ | ⬜ |
| §4 refresh sequence | ⬜ | ⬜ | ⬜ |
| §5 bean lifecycle | ⬜ | ⬜ | ⬜ |
| §6 post-processors | ⬜ | ⬜ | ⬜ |
| §7 proxies/exposed bean | ⬜ | ⬜ | ⬜ |
| §8 `FactoryBean` | ⬜ | ⬜ | ⬜ |
| §9 configuration modes | ⬜ | ⬜ | ⬜ |
| §10 ordering/cycles | ⬜ | ⬜ | ⬜ |
| §11 production scenarios | ⬜ | ⬜ | ⬜ |
| §12 trap wall | ⬜ | ⬜ | ⬜ |
