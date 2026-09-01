# Spring Boot Basics — Anti-Fumble Exercises

For an engineer who has **shipped Java** and touched Spring Boot at
the edges (a controller here, an `@Autowired` there). Goal: **name,
explain, and predict** Spring's core mechanics — the container, the
auto-config magic, the proxy tricks, the data layer — under
interview fire, not first contact.

**Legend** — exercise styles:
🔮 predict-the-output · 🛠 build · 🐛 fix-the-bug · 💭 explain-the-difference

Solutions are hidden under each section. Try first, then expand.
Scaffold a throwaway app at <https://start.spring.io> (Web + JPA +
H2 + Validation + Actuator) and run snippets in it, or use a single
`@SpringBootTest` as a scratchpad.

> ### 🎯 Minimum viable path (Pareto — the 20% that gates 80%)
>
> Spring is enormous; interviews and daily work lean on a small
> core. Do the `🎯 CORE PATH` sections in this order:
>
> > **§1 → §4 → §3 → §5 → §7 → §8**
> >
> > DI · auto-config · config · web · data · transactions/proxies.
>
> - **Prioritize 💭 and 🐛** in §1, §4, §8 — the container, the
>   "magic," and the proxy traps are what separate "I used Spring"
>   from "I understand Spring."
> - **Checkbox key:** `[ ]` do · `[~]` skim (low payoff right now) ·
>   `[x]` done · **keep** = do it anyway, even in a `[~]` section.
> - Everything past §8 is breadth. The **Extensions** list at the
>   bottom is the deliberate "later" pile — pull from it when a JD
>   or interview demands a specific area.

> **📖 Version caveat — Boot 2 vs Boot 3.** Spring Boot 3 (Nov 2022,
> Spring Framework 6) moved the baseline to **Java 17** and renamed
> the `javax.*` EE packages to **`jakarta.*`** (`jakarta.persistence`,
> `jakarta.validation`, `jakarta.servlet`). It also brought
> **`ProblemDetail`** (RFC 7807) and removed
> `WebSecurityConfigurerAdapter`. Where a fact flips across that
> line it's flagged **`Boot 3`** / **`Boot 2`**. Refs point at the
> official reference docs by area (*Spring Framework — Core*, *Spring
> Boot — Features*, *Spring Data JPA*) — stable section names, not
> page numbers.
>
> **📅 Where the version line sits (July 2026).** **Spring Boot 4.1**
> on **Spring Framework 7** is the current release for new work; Boot
> 4.0 / Framework 7.0 landed **Nov 2025**. The whole **3.x line is
> past open-source EOL** — Boot 3.5 took its last free patch (3.5.16)
> in **June 2026**, and Framework 6.2 and Spring Security 6.x lost
> community patches on the same date. Commercial support continues;
> Maven Central keeps serving the artifacts.
>
> The `Boot 3` flags throughout this doc still earn their place: the
> **`javax.*` → `jakarta.*`** rename is the migration line most
> production estates are still crossing, and plenty of shipped systems
> sit on **Boot 2.7**. Two answers worth having ready: *"what's
> current?"* → Boot 4.1 / Framework 7. *"what have you run?"* → name
> the version honestly and say what the upgrade would cost. Claiming
> the newest number you've never run is the trap.

---

## 0. Project anatomy & startup · `[~]` skim (you've seen this)

- [ ] **0.1** 💭 What does `@SpringBootApplication` expand to? Name
  the three composed annotations and what each does.
- [ ] **0.2** 💭 What is a *starter* (e.g. `spring-boot-starter-web`)?
  What does depending on it actually pull in, and what does it **not**
  do by itself?
- [ ] **0.3** 💭 `SpringApplication.run(App.class, args)` — what are the
  big things that happen between calling it and the app being ready?
- [ ] **0.4** 💭 Where does the web server come from? (There's no
  external Tomcat install.) How do you switch Tomcat → Jetty/Undertow?
- [ ] **0.5** 🛠 Sketch the conventional package layout for a small
  service (`web`/`service`/`repository`/`domain`/`config`). Why does
  the main class's package matter for component scanning?

<details><summary>Solutions 0</summary>

- 0.1 *(ref: Boot — Using Spring Boot)* `@SpringBootApplication` =
  **`@SpringBootConfiguration`** (a `@Configuration` specialization —
  marks the class as a bean-definition source) +
  **`@EnableAutoConfiguration`** (turns on Boot's auto-config) +
  **`@ComponentScan`** (scans this package and below for
  `@Component`s). MCQ staple; know it verbatim.
- 0.2 *(ref: Boot — Starters)* A starter is a **curated,
  version-aligned dependency bundle** — one coordinate that transitively
  pulls a coherent set (e.g. `-web` brings Spring MVC, Jackson,
  validation, embedded Tomcat). It supplies *jars*; the **behavior**
  comes from auto-configuration reacting to those jars being on the
  classpath (§4). Starter on classpath ≠ feature configured until an
  auto-config's conditions match.
- 0.3 *(ref: Boot — SpringApplication)* Roughly: create the
  `ApplicationContext`, run `ApplicationContextInitializer`s and
  listeners, load bean definitions (your `@Component`s +
  auto-configs), **refresh** the context (instantiate & wire
  singletons, run `BeanPostProcessor`s), start the embedded server,
  fire `ApplicationRunner`/`CommandLineRunner`s, publish
  `ApplicationReadyEvent`.
- 0.4 *(ref: Boot — Embedded Web Servers)* The server is **embedded**:
  `spring-boot-starter-web` bundles Tomcat and auto-config starts it.
  Swap by excluding `spring-boot-starter-tomcat` and adding
  `-starter-jetty` or `-starter-undertow`. The `main` method + a fat
  jar means "run the app," not "deploy a war."
- 0.5 *(ref: Boot — Structuring Your Code)* `@ComponentScan` (via
  `@SpringBootApplication`) scans the main class's package **and all
  sub-packages**. Put the main class at the **root** package so
  everything is discovered; a bean in a sibling/parent package is
  invisible unless you widen the scan.

  Why the layers exist — **each one knows exactly one thing**:

  | Layer | Knows | Must not know |
  |---|---|---|
  | Controller | HTTP — paths, verbs, status codes, JSON | SQL, business rules |
  | Service | business rules — "can this order be cancelled?" | HTTP, JSON, SQL dialect |
  | Repository | persistence — how a row becomes an object | HTTP, business rules |

  The payoff is testability: a service with no HTTP in it is testable
  with plain `new` and no container, and each layer gets its own test
  slice (§9). The failure mode is the controller method that opens a
  transaction, runs the business rule, and builds the JSON — it can
  only be tested by booting the whole app.

  Two honest caveats. The split is **not free**: a genuine CRUD
  passthrough gets three files and two interfaces to move a field from
  DB to JSON, which is why small services often collapse
  service-into-controller deliberately. And the layer count is not the
  point — **the direction of dependencies is**. Controller → service →
  repository, never backwards; a repository that imports a controller's
  DTO has broken the design regardless of how many packages exist.
</details>

---

## 1. IoC & Dependency Injection ⭐⭐ · 🎯 CORE PATH (the heart)

- [ ] **1.1** 💭 "Inversion of Control" and "Dependency Injection" —
  define each and how they relate. What is the *container* actually
  inverting?
- [ ] **1.2** 💭 `@Component` vs `@Service` vs `@Repository` vs
  `@Controller`. Are they functionally different, or just semantic?
  Which one does something **extra**?
- [ ] **1.3** 🛠 Wire a `GreetingService` into a `GreetingController`
  three ways: constructor, field (`@Autowired`), setter. Then say which
  one you'd ship and why.
- [ ] **1.4** 🐛 This field-injected bean is a pain to unit-test and
  hides a design smell. Rewrite it the preferred way:
  ```java
  @Service
  class OrderService {
      @Autowired private PaymentClient payment;
      @Autowired private InventoryRepo inventory;
      // ... 6 more @Autowired fields ...
  }
  ```
- [ ] **1.5** 💭 Since Spring 4.3, when can you **omit** `@Autowired`
  on a constructor? What breaks that shortcut?
- [ ] **1.6** 🐛 Two beans implement the same interface and startup
  fails with `NoUniqueBeanDefinitionException`. Give **two** ways to
  fix it:
  ```java
  interface Notifier {}
  @Component class EmailNotifier implements Notifier {}
  @Component class SmsNotifier   implements Notifier {}

  @Service class AlertService {
      AlertService(Notifier n) { /* which one?! */ }
  }
  ```
- [ ] **1.7** 💭 `@Qualifier` vs `@Primary` — when do you reach for
  each? Can they coexist, and who wins?
- [ ] **1.8** 🔮 Circular dependency. What happens at startup for each,
  and why?
  ```java
  @Service class A { A(B b) {} }   // constructor
  @Service class B { B(A a) {} }

  // vs. both using @Autowired fields instead
  ```
- [ ] **1.9** 💭 `@Component` (scanned) vs `@Bean` (declared in a
  `@Configuration` method) — when must you use `@Bean`? (Think:
  third-party classes.)
- [ ] **1.10** 💭 What is `ObjectProvider<T>` / `List<T>` injection for?
  How do you inject **all** implementations of an interface?

<details><summary>Solutions 1</summary>

- 1.1 *(ref: Core — The IoC Container)* **IoC**: the framework, not
  your code, controls object creation and lifecycle — you don't `new`
  your collaborators, the container does and hands them to you. **DI**
  is the *mechanism* of IoC: dependencies are *injected* (constructor/
  setter/field) rather than looked up or constructed. The container
  inverts the **"who creates and wires whom"** relationship.

  The picture: in plain Java, something has to `new` up all these
  objects in the right order and hand them to each other. That place —
  usually `App.java`'s `main` — is your **composition root**, wired by
  hand. **Spring Boot is nothing but an automated composition root plus
  an HTTP front door**: it reads your constructors, works out the wiring
  order itself, and exposes some of those objects over HTTP. That's the
  whole trick.
- 1.2 *(ref: Core — Classpath Scanning; Data Access — Exception
  Translation)* All four are `@Component` specializations — same
  scanning, different **intent labels**. The one that does *extra*:
  **`@Repository`** enables **persistence exception translation**
  (vendor `SQLException`s → Spring's `DataAccessException`
  hierarchy). `@Service`/`@Controller` are semantic markers (though
  `@Controller`/`@RestController` are wired into MVC handler mapping).
- 1.3 *(ref: Core — Dependency Injection)*
  ```java
  @Service
  class GreetingController {
      private final GreetingService svc;         // constructor (ship this)
      GreetingController(GreetingService svc) { this.svc = svc; }
  }
  ```
  **Constructor injection** wins: fields can be `final` (immutable),
  dependencies are explicit and required, the object is never in a
  half-built state, and it's trivial to instantiate in a plain unit
  test with `new`. Field injection needs reflection/Spring to
  populate; setter injection is for genuinely optional deps.
- 1.4 *(ref: Core — Constructor-based DI)* Constructor-inject and make
  them `final`:
  ```java
  @Service
  class OrderService {
      private final PaymentClient payment;
      private final InventoryRepo inventory;
      OrderService(PaymentClient payment, InventoryRepo inventory) {
          this.payment = payment;
          this.inventory = inventory;
      }
  }
  ```
  The smell field injection *hid*: **too many dependencies**. A
  constructor with 8 params is visibly screaming "this class does too
  much" — split it. Field injection mutes that signal.
- 1.5 *(ref: Core — Constructor-based DI)* If a bean has **exactly one
  constructor**, Spring uses it automatically — no `@Autowired`
  needed. Add a second constructor and Spring no longer knows which to
  use → you must annotate the intended one.
- 1.6 *(ref: Core — Autowiring; Fine-tuning with @Primary/@Qualifier)*
  ```java
  // (a) mark one as the default
  @Component @Primary class EmailNotifier implements Notifier {}

  // (b) qualify at the injection point
  @Service class AlertService {
      AlertService(@Qualifier("smsNotifier") Notifier n) {}
  }
  ```
  Bean names default to the class name with a lowercased first letter
  (`smsNotifier`).
- 1.7 *(ref: Core — @Primary and @Qualifier)* **`@Primary`** = the
  *default* pick when a type is ambiguous — one place, affects all
  unqualified injection points. **`@Qualifier`** = an *explicit* pick
  at a specific injection point. They coexist: `@Qualifier` at the
  site **overrides** `@Primary`. Use `@Primary` for the "usual" impl,
  `@Qualifier` for the exceptions.
- 1.8 *(ref: Core — Circular Dependencies)* **Constructor↔constructor:
  startup fails** — Spring can't build A without B and vice versa
  (`BeanCurrentlyInCreationException`). **Field/setter injection can
  resolve it**: Spring creates the raw instances first, then populates
  fields, so the cycle is broken. But since **Boot 2.6** circular refs
  are **prohibited by default** (`spring.main.allow-circular-references`
  must be `true` to even allow the field version). The real fix is to
  break the cycle (extract a third bean / rethink the design), not to
  re-enable it.
- 1.9 *(ref: Core — Java-based Configuration)* Use `@Bean` when **you
  don't own the class** and can't annotate it (e.g. a
  `RestClient`, an `ObjectMapper`, a third-party `DataSource`), or when
  construction needs logic/config. `@Component` is for **your own**
  classes you can annotate and let scanning find.
- 1.10 *(ref: Core — Autowiring Multiple Beans)* Inject
  `List<Notifier>` (or `Map<String,Notifier>` keyed by bean name) to
  get **all** implementations — the plugin/strategy pattern.
  `ObjectProvider<T>` gives lazy/optional/streamed access
  (`getIfAvailable`, `orderedStream`) without failing when zero or
  many candidates exist.
</details>

---

## 2. Bean lifecycle & scopes ⭐ · mostly `[~]` (one keep)

- [~] **2.1** 💭 What's the **default** bean scope? What does it mean
  for state and thread-safety?
- [~] **2.2** 💭 `singleton` vs `prototype` — when the container gives
  you a fresh instance vs the same one. Name a request/session-scoped
  use in web apps.
- [ ] **2.3** 🐛 The classic scope trap: a **singleton** injects a
  **prototype** — how many prototype instances actually exist over the
  app's life, and why is that a surprise? Give the fix. · **keep**
  ```java
  @Component @Scope("prototype") class Task {}

  @Service class Runner {
      private final Task task;   // injected once
      Runner(Task task) { this.task = task; }
  }
  ```
- [~] **2.4** 💭 Lifecycle callbacks: `@PostConstruct` /
  `@PreDestroy`, `InitializingBean`/`DisposableBean`, `@Bean(initMethod
  =, destroyMethod =)`. Which runs when? **`Boot 3`** note: which
  package is `@PostConstruct` in now?
- [~] **2.5** 💭 What does `@Lazy` do to a bean, and when is it worth
  it (vs the default eager singleton init)?

<details><summary>Solutions 2</summary>

- 2.1 *(ref: Core — Bean Scopes)* **`singleton`** — one shared
  instance per container. So Spring beans are usually **stateless**;
  mutable instance state on a singleton is shared across all threads
  and is a bug magnet. Keep per-request state in method locals/params.
- 2.2 *(ref: Core — Bean Scopes)* `singleton`: same instance every
  injection/`getBean`. `prototype`: a **new** instance every time it's
  requested. Web scopes: `request` (one per HTTP request),
  `session` (one per user session), `application`.
- 2.3 *(ref: Core — Scoped Beans as Dependencies)* **Exactly one**
  `Task` is created — the prototype is resolved **once**, when `Runner`
  (a singleton) is built, then reused forever. Prototype scope only
  fires on *fresh resolution*, and a singleton's constructor runs
  once. Fixes: inject an `ObjectProvider<Task>` and call
  `getObject()` per use; use `@Lookup` method injection; or a scoped
  proxy (`@Scope(value="prototype", proxyMode=TARGET_CLASS)`).
- 2.4 *(ref: Core — Lifecycle Callbacks)* Order per bean:
  constructor → dependencies injected → `@PostConstruct` →
  `InitializingBean.afterPropertiesSet` → custom `initMethod`; on
  shutdown the reverse-ish: `@PreDestroy` →
  `DisposableBean.destroy` → custom `destroyMethod`. **`Boot 3`**:
  `@PostConstruct`/`@PreDestroy` live in **`jakarta.annotation`**
  (were `javax.annotation` in **`Boot 2`**).
- 2.5 *(ref: Core — Lazy-initialized Beans)* `@Lazy` defers a bean's
  creation until first use instead of at context refresh. Worth it for
  expensive beans rarely used, or to break an init ordering knot — but
  it **hides startup failures** until runtime, so eager (the default)
  is usually safer.
</details>

---

## 3. Configuration & profiles · 🎯 CORE PATH

- [ ] **3.1** 💭 `application.properties` vs `application.yml` — same
  power? What's the precedence order across env vars, command-line
  args, and the files? (Who wins?)
- [ ] **3.2** 💭 `@Value("${...}")` vs `@ConfigurationProperties` — the
  real trade-offs. Which gives type-safety, relaxed binding, and
  validation?
- [ ] **3.3** 🛠 Bind a typed config block to a POJO:
  ```yaml
  app:
    retry:
      max-attempts: 3
      backoff: 500ms
  ```
  Write the `@ConfigurationProperties("app.retry")` class and show how
  it's enabled.
- [ ] **3.4** 💭 Profiles: what does `@Profile("prod")` do, how do you
  activate one, and how does `application-prod.yml` fit in?
- [ ] **3.5** 🐛 A `@Value("${feature.timeout}")` throws at startup
  because the property is missing. Give it a default two ways.
- [ ] **3.6** 💭 "Relaxed binding" — why does `MAX_ATTEMPTS`
  (env var) map to `maxAttempts` / `max-attempts`? Why does that make
  env-var config in containers painless?

<details><summary>Solutions 3</summary>

- 3.1 *(ref: Boot — Externalized Configuration)* Same power, different
  syntax (YAML nests, no repeated prefixes; properties is flat).
  Precedence (later **overrides** earlier), roughly: default props →
  `application.yml` → profile-specific files → OS **env vars** →
  **command-line args** (`--server.port=9000`) near the top → plus
  `SPRING_APPLICATION_JSON` and test props. Rule of thumb:
  **command-line and env win over files** — exactly what you want for
  12-factor deploys.
- 3.2 *(ref: Boot — Type-safe Configuration Properties)* `@Value`:
  quick, SpEL-capable, **one property at a time**, no relaxed binding,
  no validation. `@ConfigurationProperties`: binds a **whole tree** to
  a typed POJO, with **relaxed binding**, JSR-303 **validation**
  (`@Validated`), and IDE metadata. Prefer `@ConfigurationProperties`
  for anything beyond a lone value.
- 3.3 *(ref: Boot — @ConfigurationProperties)*
  ```java
  @ConfigurationProperties("app.retry")
  @Validated
  public class RetryProps {
      @Min(1) private int maxAttempts = 1;
      private Duration backoff = Duration.ofMillis(200);
      // getters/setters (or a Java record + constructor binding)
  }
  ```
  Enable via `@EnableConfigurationProperties(RetryProps.class)` on a
  config class, or `@ConfigurationPropertiesScan` on the app class.
  Note `500ms` binds straight to a `Duration` — Boot's converters
  handle it.
- 3.4 *(ref: Boot — Profiles)* `@Profile("prod")` makes a bean/config
  **only active** when the `prod` profile is. Activate with
  `spring.profiles.active=prod` (property, env var
  `SPRING_PROFILES_ACTIVE`, or `--spring.profiles.active`).
  `application-prod.yml` is loaded **and merged over** the base
  `application.yml` when `prod` is active — the standard per-env
  override mechanism.
- 3.5 *(ref: Boot — Externalized Configuration)*
  ```java
  @Value("${feature.timeout:5000}")   // default after the colon
  private long timeout;
  ```
  Or make it a `@ConfigurationProperties` field with a default value
  assigned in the POJO. (The `:` default syntax is the quick one.)
- 3.6 *(ref: Boot — Relaxed Binding)* Boot binds property names
  **loosely**: `max-attempts` (kebab), `maxAttempts` (camel),
  `max_attempts`, and `MAX_ATTEMPTS` (upper env-var form) all map to
  the same target. So in Docker/Kubernetes you set
  `APP_RETRY_MAX_ATTEMPTS` as an env var and it binds to
  `app.retry.max-attempts` — no code change, no property file baked
  into the image.
</details>

---

## 4. Auto-configuration & starters ⭐⭐ · 🎯 CORE PATH (the Boot signature)

- [ ] **4.1** 💭 In one breath: **how does auto-configuration actually
  work?** What triggers it, and what decides whether a given
  auto-config applies?
- [ ] **4.2** 💭 The `@ConditionalOn*` family — name four and what each
  gates on (`@ConditionalOnClass`, `@ConditionalOnMissingBean`,
  `@ConditionalOnProperty`, `@ConditionalOnBean`).
- [ ] **4.3** 💭 **Why can you override any auto-configured bean just
  by declaring your own?** What ordering guarantee makes
  `@ConditionalOnMissingBean` the mechanism of "your bean wins"?
- [ ] **4.4** 💭 Where does Boot find the list of auto-config classes?
  (**`Boot 3`**/2.7+ answer vs the older `spring.factories` answer.)
- [ ] **4.5** 🛠 You add `spring-boot-starter-data-jpa` + the H2 jar
  and — with **zero** `@Bean`s — you get a `DataSource`, an
  `EntityManagerFactory`, and a `JdbcTemplate`. Explain the chain of
  conditions that produced each.
- [ ] **4.6** 🛠 Debug it: how do you make Boot **tell you** which
  auto-configs matched, which didn't, and why? (Two ways.)
- [ ] **4.7** 🐛 An auto-config is stealing the show — you want your own
  `ObjectMapper` instead of Boot's. What's the clean way to opt out of
  just that one, and the blunt way to exclude a whole auto-config
  class?
- [ ] **4.8** 💭 Write your own tiny starter/auto-config: what makes a
  class an auto-config, and how do you register it so *other* modules
  pick it up? (Conceptual — the shape, not a full build.)

<details><summary>Solutions 4</summary>

- 4.1 *(ref: Boot — Auto-configuration)* `@EnableAutoConfiguration`
  (via `@SpringBootApplication`) loads a big list of
  **auto-configuration classes**; each is guarded by
  **`@Conditional`** annotations that inspect the classpath, existing
  beans, and properties. A class's `@Bean`s are only created **if its
  conditions match**. So "having the jar on the classpath" +
  "conditions satisfied" ⇒ sensible defaults appear, no XML, no
  boilerplate.
- 4.2 *(ref: Boot — Condition Annotations)*
  `@ConditionalOnClass` — a class is present on the classpath (the jar
  is there). `@ConditionalOnMissingBean` — the user hasn't already
  defined a bean of this type. `@ConditionalOnProperty` — a property
  has a given value. `@ConditionalOnBean` — some other bean exists.
  (Also `@ConditionalOnWebApplication`, `@ConditionalOnMissingClass`.)
- 4.3 *(ref: Boot — Auto-configuration ordering)* **User configuration
  is processed, and auto-configuration runs *after* / at lower
  priority.** Auto-config beans are wrapped in
  `@ConditionalOnMissingBean`, so if you've already declared, say, a
  `DataSource`, the auto-config's condition fails and **yours stands**.
  That "user beans win by default" property is the whole ergonomics of
  Boot.
- 4.4 *(ref: Boot — Creating Your Own Auto-configuration)* **Boot
  2.7+ / `Boot 3`:** the class names live in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
  (one FQN per line). **Older Boot:** they were listed under the
  `EnableAutoConfiguration` key in `META-INF/spring.factories`. Same
  idea, new file.
- 4.5 *(ref: Boot — Auto-configuration; Spring Data JPA)* Chain:
  `DataSourceAutoConfiguration` sees H2 on the classpath
  (`@ConditionalOnClass`) and **no user `DataSource`**
  (`@ConditionalOnMissingBean`) → builds an embedded `DataSource`.
  `HibernateJpaAutoConfiguration` sees JPA/Hibernate classes → builds
  an `EntityManagerFactory` and a `JpaTransactionManager`.
  `JdbcTemplateAutoConfiguration` sees a `DataSource` bean now exists
  (`@ConditionalOnBean(DataSource.class)`) → builds a `JdbcTemplate`.
  Each link is a condition on the previous link's output.
- 4.6 *(ref: Boot — Auto-configuration report)* (a) Run with
  **`--debug`** (or `debug=true`) → prints the **Condition Evaluation
  Report**: `Positive matches`, `Negative matches`, `Exclusions`. (b)
  With Actuator, hit **`/actuator/conditions`** for the same data as
  JSON. This is how you answer "why didn't my bean get created?"
- 4.7 *(ref: Boot — Overriding auto-config)* Clean: just **declare your
  own `@Bean ObjectMapper`** — the auto-config's
  `@ConditionalOnMissingBean` backs off (4.3). Blunt: exclude the
  whole auto-config —
  `@SpringBootApplication(exclude = JacksonAutoConfiguration.class)`
  or `spring.autoconfigure.exclude=`. Prefer the clean override; only
  exclude when you want the feature **gone**, not replaced.
- 4.8 *(ref: Boot — Creating Your Own Starter)* An auto-config is just
  an **`@AutoConfiguration`** class (a specialized `@Configuration`)
  with `@Bean` methods guarded by `@ConditionalOn*`. Register it by
  listing its FQN in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
  A "starter" is then a thin dependency module that pulls that
  auto-config module + the libraries it configures. That's the entire
  trick behind every `spring-boot-starter-*`.
</details>

---

## 5. Web layer (Spring MVC) ⭐ · 🎯 CORE PATH

- [ ] **5.1** 💭 `@Controller` vs `@RestController`. What exactly does
  `@RestController` add, and what changes about the return value?
- [ ] **5.2** 🛠 Build a small REST resource: `GET /users/{id}`,
  `GET /users?active=true`, `POST /users` (JSON body). Use
  `@PathVariable`, `@RequestParam`, `@RequestBody`.
- [ ] **5.3** 💭 `@RequestBody` — what turns the JSON into your object,
  and what turns your returned object back into JSON? (Name the
  machinery.)
- [ ] **5.4** 🔮 What HTTP status does each return by default, and how
  do you take control?
  ```java
  @PostMapping("/users") User create(@RequestBody User u) { ... } // status?
  // vs returning ResponseEntity<User>
  ```
- [ ] **5.5** 💭 Trace one request through the **DispatcherServlet**:
  from bytes on the socket to your controller method and back to JSON.
  Name the front controller, the handler mapping, the adapter, the
  converter.
- [ ] **5.6** 💭 `@PathVariable` vs `@RequestParam` vs `@RequestBody`
  vs `@RequestHeader` — one line each, when to use.
- [ ] **5.7** 🐛 Fix — this "GET with a body to filter" fights REST and
  breaks caching/proxies. What's the idiomatic shape?
  ```java
  @GetMapping("/search") List<Item> search(@RequestBody Query q) { ... }
  ```
- [ ] **5.8** 💭 **`Boot 3`** REST note: `RestClient` /
  `WebClient` vs the (now deprecated) `RestTemplate` for **calling**
  other services — which would you pick today and why?

<details><summary>Solutions 5</summary>

- 5.1 *(ref: Web — Annotated Controllers)* `@RestController` =
  `@Controller` + `@ResponseBody`. On a plain `@Controller`, a
  returned `String` is a **view name** (template to render);
  `@ResponseBody`/`@RestController` means the return value is
  **serialized straight into the response body** (JSON) — no view
  resolution. REST APIs use `@RestController`.
- 5.2 *(ref: Web — Handler Methods)*
  ```java
  @RestController
  @RequestMapping("/users")
  class UserController {
      @GetMapping("/{id}")
      User byId(@PathVariable Long id) { ... }

      @GetMapping
      List<User> list(@RequestParam(defaultValue = "false") boolean active) { ... }

      @PostMapping
      @ResponseStatus(HttpStatus.CREATED)
      User create(@RequestBody @Valid User u) { ... }
  }
  ```
- 5.3 *(ref: Web — HttpMessageConverter)* **`HttpMessageConverter`s**
  do both directions — for JSON that's `MappingJackson2HttpMessage
  Converter` wrapping **Jackson**. Content negotiation (the `Accept`
  and `Content-Type` headers) picks the converter. `@RequestBody`
  deserializes the request; the return value (`@ResponseBody`)
  serializes the response.
- 5.4 *(ref: Web — Responses)* A bare returned object → **200 OK**
  (unless you add `@ResponseStatus(CREATED)`). **`ResponseEntity<User>`**
  gives you **full control** — status, headers, body:
  `return ResponseEntity.created(location).body(user);` → 201 with a
  `Location` header. Use `ResponseEntity` when status/headers vary at
  runtime; `@ResponseStatus` when it's fixed.
- 5.5 *(ref: Web — DispatcherServlet)* Request → **`DispatcherServlet`**
  (the front controller) → **`HandlerMapping`** resolves URL+verb to a
  controller method → **`HandlerAdapter`** invokes it, resolving args
  (`@PathVariable`, `@RequestBody` via converters) → your method runs →
  return value goes through an **`HttpMessageConverter`** → response
  written. Exceptions detour through `HandlerExceptionResolver` (§6).
- 5.6 *(ref: Web — Method Arguments)* `@PathVariable` — a value **in
  the URL path** (`/users/42`). `@RequestParam` — a **query/form
  param** (`?active=true`). `@RequestBody` — the **whole request
  body** deserialized. `@RequestHeader` — a specific header.
- 5.7 *(ref: Web — GET semantics)* GET should be **safe, cacheable, and
  bodyless** — bodies on GET are widely ignored by caches/proxies. Put
  the filter in the **query string**:
  ```java
  @GetMapping("/search")
  List<Item> search(@RequestParam String q,
                    @RequestParam(required = false) String category) { ... }
  ```
  (For a genuinely complex query object, bind many params with a POJO
  via `@ModelAttribute`, or accept it's a POST `/search`.)
- 5.8 *(ref: Web — REST Clients)* **`RestTemplate` is in maintenance
  mode.** For new code use **`RestClient`** (`Boot 3.2+`, synchronous,
  fluent) or **`WebClient`** (reactive, also usable blocking). Pick
  `RestClient` for a straightforward blocking MVC service; `WebClient`
  when you're already reactive or need streaming/concurrency.
</details>

---

## 6. Validation & exception handling · CORE-adjacent

- [ ] **6.1** 🛠 Add Bean Validation to a request DTO (`@NotBlank`,
  `@Email`, `@Min`) and trigger it with `@Valid` on the controller
  param. What exception fires when it fails?
- [ ] **6.2** 💭 **`Boot 3`** namespace check: which package are the
  validation annotations in now (`@NotNull` etc.)?
- [ ] **6.3** 🛠 Centralize error handling with `@RestControllerAdvice`
  + `@ExceptionHandler` — map a `NotFoundException` → 404 and a
  validation failure → 400 with field details.
- [ ] **6.4** 💭 `@ControllerAdvice` vs `@RestControllerAdvice` — what
  does the `Rest` prefix add? Then: advice vs a per-controller
  `@ExceptionHandler` vs throwing `ResponseStatusException` inline —
  when each?
- [ ] **6.5** 💭 **`Boot 3`** `ProblemDetail` (RFC 7807): what problem
  does the standard error body solve, and how do you turn it on?

<details><summary>Solutions 6</summary>

- 6.1 *(ref: Web — Validation)*
  ```java
  record CreateUser(@NotBlank String name, @Email String email,
                    @Min(18) int age) {}

  @PostMapping("/users")
  User create(@RequestBody @Valid CreateUser body) { ... }
  ```
  On failure Spring throws **`MethodArgumentNotValidException`**
  (for `@RequestBody`) → default 400. (For `@Validated` on
  `@RequestParam`/`@PathVariable` it's `ConstraintViolationException`.)
- 6.2 *(ref: Boot 3 migration)* **`jakarta.validation.constraints.*`**
  in **`Boot 3`** (was `javax.validation.constraints.*` in **`Boot
  2`**). Same annotations, new package — a top migration gotcha.
- 6.3 *(ref: Web — Exception Handling)*
  ```java
  @RestControllerAdvice
  class ApiErrors {
      @ExceptionHandler(NotFoundException.class)
      @ResponseStatus(HttpStatus.NOT_FOUND)
      ErrorBody notFound(NotFoundException e) { return new ErrorBody(e.getMessage()); }

      @ExceptionHandler(MethodArgumentNotValidException.class)
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      Map<String,String> invalid(MethodArgumentNotValidException e) {
          return e.getBindingResult().getFieldErrors().stream()
              .collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
      }
  }
  ```
- 6.4 *(ref: Web — Exception Handling)* **`@RestControllerAdvice` =
  `@ControllerAdvice` + `@ResponseBody`** — the same pairing as
  `@RestController` and `@Controller`
  ([§5.1](#5-web-layer-spring-mvc----core-path)),
  for the same reason. On plain `@ControllerAdvice`, a handler
  returning `String` gives back a **view name**; on
  `@RestControllerAdvice` the returned object is **serialized into
  the error body** as JSON. A JSON API wants the `Rest` variant —
  a handler returning `new ErrorBody(...)` without it resolves as a
  view and blows up looking for a template.

  Which of the three:
  - **Advice** — cross-cutting, applies to all controllers; the
    default home for API error mapping.
  - **Per-controller `@ExceptionHandler`** — handling specific to
    one controller. A handler here wins over the advice for the
    same exception type.
  - **`ResponseStatusException`** — a quick inline throw (`throw new
    ResponseStatusException(NOT_FOUND, "no user")`) when a custom
    exception type isn't worth it.

  Advice for the policy, inline for one-offs.
- 6.5 *(ref: Web — Error Responses; `Boot 3`)* `ProblemDetail` is a
  **standardized JSON error shape** (`type`, `title`, `status`,
  `detail`, `instance`) so clients parse errors uniformly instead of
  every API inventing its own body. Turn it on with
  `spring.mvc.problemdetails.enabled=true`, or return/throw
  `ProblemDetail` / `ErrorResponseException` from handlers.
</details>

---

## 7. Data layer — Spring Data JPA & JDBC ⭐ · 🎯 CORE PATH

- [ ] **7.1** 💭 What does extending `JpaRepository<User, Long>` **give
  you for free**? Who implements it — did you write a class?
- [ ] **7.2** 🛠 Write **derived query methods**
  (`findByEmail`, `findByActiveTrueOrderByCreatedAtDesc`) and one
  `@Query` (JPQL) with a named parameter. When do you drop to `@Query`
  or native SQL?
- [ ] **7.3** 💭 `@Entity` essentials: `@Id`, `@GeneratedValue`
  strategies (`IDENTITY` vs `SEQUENCE` vs `AUTO`) — which do you pick
  for Postgres/Oracle and why does it matter for batching?
- [ ] **7.4** 🐛 The **N+1 select** problem — spot it, then fix it three
  ways:
  ```java
  List<Order> orders = repo.findAll();
  for (Order o : orders)
      total += o.getItems().size();   // lazy collection per order
  ```
- [ ] **7.5** 🐛 `LazyInitializationException` — why does touching a
  lazy association in the controller (after the service returned) blow
  up? Name the real fix vs the lazy hack (`open-session-in-view`).
- [ ] **7.6** 💭 `JpaRepository` / JPA vs raw **`JdbcTemplate`** vs
  **`JdbcClient`** (`Boot 3.2+`) — when would you deliberately skip the
  ORM and write SQL?
- [ ] **7.7** 💭 `save()` for an entity with a set id — `persist` or
  `merge`? How does Spring Data decide whether the entity is new, and
  what surprises people about `saveAll` batching?

<details><summary>Solutions 7</summary>

- 7.1 *(ref: Spring Data JPA — Core Concepts)* You get CRUD +
  pagination + sorting (`save`, `findById`, `findAll(Pageable)`,
  `delete`, `count`…) **without writing an implementation** — Spring
  Data generates a proxy backing the interface at runtime. You declare
  the interface; the container supplies the bean.
- 7.2 *(ref: Spring Data JPA — Query Methods)*
  ```java
  interface UserRepo extends JpaRepository<User, Long> {
      Optional<User> findByEmail(String email);
      List<User> findByActiveTrueOrderByCreatedAtDesc();

      @Query("select u from User u where u.email = :email")
      Optional<User> lookup(@Param("email") String email);
  }
  ```
  Drop to `@Query` when the derived-name method gets unreadable, needs
  a join/projection, or you want native SQL (`@Query(value=...,
  nativeQuery=true)`) for DB-specific features.
- 7.3 *(ref: JPA — Identifier Generation)* `IDENTITY` uses the DB
  auto-increment column — simple, but **disables JDBC batch inserts**
  (Hibernate needs the id back immediately per row). `SEQUENCE`
  (Postgres/Oracle) lets Hibernate pre-allocate ids and **batch**
  inserts — preferred for write-heavy loads. `AUTO` lets the provider
  choose. Pick `SEQUENCE` on databases that have real sequences.
- 7.4 *(ref: JPA — Fetching)* N+1: one query for the orders, then **one
  more per order** for its items. Fixes: (a) **`JOIN FETCH`** —
  `@Query("select o from Order o join fetch o.items")`; (b)
  **`@EntityGraph`** on the repo method to eager-load `items`; (c)
  **batch fetching** — `@BatchSize`/`hibernate.default_batch_fetch_size`
  turns N selects into ⌈N/size⌉. Default associations to `LAZY` and
  fetch **explicitly** where needed.
- 7.5 *(ref: JPA — Session/EntityManager lifecycle)* A `LAZY`
  association is a proxy that loads **on access, using the Hibernate
  session**. Once the `@Transactional` service method returns, the
  session is closed; touching the proxy in the controller has no
  session → `LazyInitializationException`. Real fix: **fetch what the
  caller needs inside the transaction** (JOIN FETCH / entity graph /
  a DTO projection). The hack: `spring.jpa.open-in-view=true` (on by
  default!) keeps the session open through the view — convenient,
  but it leaks DB work into the web layer and hides N+1s. Many teams
  disable it deliberately.
- 7.6 *(ref: Data Access — JDBC)* Skip the ORM when you have **complex
  reporting SQL, bulk operations, or want exact control** over the
  query and no entity-graph/dirty-checking overhead. `JdbcTemplate`
  (classic) and **`JdbcClient`** (`Boot 3.2+`, fluent) map rows to
  objects with a `RowMapper` and nothing hidden. JPA for the
  aggregate/write model; SQL for the read/reporting model.
- 7.7 *(ref: Spring Data — Saving)* `save()` asks Spring Data's
  `EntityInformation` whether the entity is **new**. By default it
  first checks a non-primitive `@Version` property (`null` = new); if
  there is none, it checks the id (`null` = new). New →
  `EntityManager.persist`; not new → `EntityManager.merge`.
  A non-null assigned id therefore does **not** universally mean
  "UPDATE": assigned-id entities often implement `Persistable` and
  supply `isNew()`, and `merge` is an operation on entity state, not a
  promise about the exact SQL. Surprise: `saveAll` doesn't guarantee a
  single batched INSERT unless batching is configured
  (`hibernate.jdbc.batch_size`) **and** the id strategy allows it
  (7.3 — `IDENTITY` defeats batching).
</details>

---

## 8. Transactions & AOP proxies ⭐⭐ · 🎯 CORE PATH (deepest traps)

- [ ] **8.1** 💭 What does `@Transactional` actually *do* — mechanically?
  What object sits between the caller and your bean, and where does the
  transaction begin/commit/rollback happen?
- [ ] **8.2** 🐛🔮 **The self-invocation trap** (the #1 Spring gotcha).
  Does `doWork` run in a transaction? Why or why not — and fix it.
  ```java
  @Service
  class Billing {
      public void process() {
          doWork();                 // internal call
      }
      @Transactional
      public void doWork() { /* db writes */ }
  }
  ```
- [ ] **8.3** 🐛 A `@Transactional` method **doesn't roll back** on a
  checked exception. Why is that the *default*, and how do you make it
  roll back?
  ```java
  @Transactional
  void transfer() throws InsufficientFundsException { ... }  // no rollback!
  ```
- [ ] **8.4** 💭 Propagation: `REQUIRED` (default) vs `REQUIRES_NEW` vs
  `NESTED` vs `SUPPORTS` vs `MANDATORY`. Give the one-liner and a real
  use for `REQUIRES_NEW` (hint: an audit log that must survive a
  rollback).
- [ ] **8.5** 💭 Isolation levels through Spring — `READ_COMMITTED` vs
  `REPEATABLE_READ` vs `SERIALIZABLE`; which anomalies each prevents
  (dirty / non-repeatable / phantom reads). Does Spring set it or the
  DB?
- [ ] **8.6** 💭 Which method visibilities can `@Transactional`
  advise through a JDK interface proxy versus a class-based proxy?
  Why can neither a `private` method nor a `final` method be advised
  by a class-based proxy? Include the Spring 6+ visibility change.
- [ ] **8.7** 💭 Same proxy mechanism powers `@Async`, `@Cacheable`,
  `@Transactional`, `@Retryable`. State the **one rule** that explains
  why self-invocation silently disables **all** of them.
- [ ] **8.8** 🐛 An `@Async` method declares an ordinary `T` return
  type. Why is that not a valid asynchronous result contract, which
  return types are supported, and when does the caller actually block?

<details><summary>Solutions 8</summary>

- 8.1 *(ref: Data Access — Declarative Transaction Management)* Spring
  wraps the bean in an **AOP proxy**. Callers hold the proxy, not your
  object. On an annotated method the proxy **starts a transaction
  before** delegating to your code and **commits after** it returns
  (or **rolls back** on a matching exception), via a
  `PlatformTransactionManager`. The annotation is *advice*; the proxy
  is *how* the advice runs around your method.
- 8.2 *(ref: Data Access — Proxy limitations)* **`doWork` runs with NO
  transaction.** `process()` calls `doWork()` as a plain `this.`
  call — it **never goes through the proxy**, so the transactional
  advice is bypassed. Fixes: (a) move `doWork` to a **separate bean**
  and inject it (calls now cross the proxy); (b) self-inject the proxy
  and call `self.doWork()`; (c) put `@Transactional` on the
  **entry point** (`process`) instead. This is *the* Spring interview
  trap — know it cold.
- 8.3 *(ref: Data Access — Rolling Back)* Default rollback fires on
  **`RuntimeException` and `Error`** only — **checked exceptions do
  NOT roll back** (a Spring convention inherited from EJB). Force it:
  `@Transactional(rollbackFor = InsufficientFundsException.class)` (or
  `rollbackFor = Exception.class`). Conversely `noRollbackFor` opts a
  runtime exception out.
- 8.4 *(ref: Data Access — Propagation)* `REQUIRED` — join the current
  tx or start one (default). `REQUIRES_NEW` — **suspend** the current
  tx and run in a brand-new independent one (commits/rolls back on its
  own). `NESTED` — a savepoint within the current tx (partial
  rollback). `SUPPORTS` — join if one exists, else run non-tx.
  `MANDATORY` — must already be in a tx or throw. Real
  `REQUIRES_NEW` use: **writing an audit/log row that must persist
  even if the business tx rolls back** — the new tx commits
  independently.
- 8.5 *(ref: Data Access — Isolation)* `READ_COMMITTED` stops **dirty
  reads** (Postgres/Oracle default). `REPEATABLE_READ` also stops
  **non-repeatable reads** (MySQL/InnoDB default). `SERIALIZABLE`
  also stops **phantom reads** — full isolation, most locking/least
  concurrency. Spring **requests** the level
  (`@Transactional(isolation = REPEATABLE_READ)`); the **database**
  enforces it, and not every DB supports every level.
- 8.6 *(ref: Data Access — Proxy modes; Core — AOP proxies)* Proxy
  advice can only intercept calls it **wraps**. **JDK dynamic
  proxies** expose an interface, so transactional methods must be
  **public interface methods**. A **class-based proxy** subclasses the
  target and overrides an interceptable method. Since **Spring
  Framework 6**, `public`, `protected`, and package-visible methods
  can be transactional through class-based proxies by default.
  `private` methods cannot be overridden, and `final` methods cannot
  be overridden; a `final` class cannot be subclassed. Public methods
  remain the least surprising choice when code may switch proxy type.
- 8.7 *(ref: Core — Understanding AOP Proxies)* **The rule: advice only
  applies to calls that go *through the proxy* — i.e. calls from
  *outside* the bean.** Any `this.method()` internal call skips the
  proxy, so `@Transactional`, `@Async`, `@Cacheable`, `@Retryable` all
  silently do nothing on self-invocation. One rule explains every one
  of these bugs.
- 8.8 *(ref: Integration — @Async)* The proxy submits the invocation
  to an executor and must give the caller an **asynchronous handle**.
  Supported return shapes are `void` or `Future` types, normally
  **`CompletableFuture<T>`**; an ordinary `T` cannot represent a value
  that will arrive later. Calling the method returns the future
  promptly. Composition (`thenApply`, `thenAccept`) stays
  non-blocking; `get()`/`join()` blocks only when the caller explicitly
  waits. Use `void` only for deliberate fire-and-forget work because
  failures cannot be returned to the caller. `@Async` also needs
  `@EnableAsync` and, as in §8.7, self-invocation bypasses its proxy.
</details>

---

## 9. Testing · mostly `[~]` (two keeps)

- [ ] **9.1** 💭 `@SpringBootTest` (full context) vs a **slice**
  (`@WebMvcTest`, `@DataJpaTest`) — the trade-off in speed and scope.
  When each? · **keep**
- [ ] **9.2** 🛠 `@WebMvcTest` the controller layer with **MockMvc**,
  mocking the service. Assert status + JSON. · **keep**
  ```java
  @WebMvcTest(UserController.class)
  class UserControllerTest {
      @Autowired MockMvc mvc;
      @MockitoBean UserService svc;   // Boot 3.4+ (was @MockBean)
      // ...
  }
  ```
- [~] **9.3** 💭 `@MockBean` / **`@MockitoBean`** (`Boot 3.4+`) — what
  does it replace in the *context*, and how is that different from a
  plain Mockito `@Mock`?
- [~] **9.4** 💭 `@DataJpaTest` — what does it auto-configure, why does
  it **roll back** each test, and what DB does it use by default?
- [~] **9.5** 💭 **Testcontainers** — the one-liner on *why* (real
  Postgres/Kafka in a container beats H2/embedded), and
  `@ServiceConnection` (`Boot 3.1+`) wiring.

<details><summary>Solutions 9</summary>

- 9.1 *(ref: Boot — Testing)* `@SpringBootTest` loads the **whole
  application context** — closest to production, slowest, for
  integration tests. **Slices** load only one layer's beans
  (`@WebMvcTest` = MVC + your controller, no service/repo;
  `@DataJpaTest` = JPA + repositories + a DB) — much faster, focused.
  Rule: slice for unit-ish layer tests, `@SpringBootTest` for genuine
  end-to-end wiring.
- 9.2 *(ref: Boot — Testing the Web Layer)*
  ```java
  @Test
  void returnsUser() throws Exception {
      given(svc.byId(1L)).willReturn(new User(1L, "Ada"));
      mvc.perform(get("/users/1"))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.name").value("Ada"));
  }
  ```
  `@WebMvcTest` stands up MVC infra without a real server; MockMvc
  drives requests through the `DispatcherServlet` in-process.
- 9.3 *(ref: Boot — Mocking Beans)* `@MockBean`/`@MockitoBean` puts a
  **Mockito mock into the Spring context**, replacing the real bean
  everywhere it's injected — so the *wired* graph uses your mock. A
  plain `@Mock` is just an object; it isn't in the context and won't
  be injected into Spring-managed beans. (**`Boot 3.4`** renamed
  `@MockBean` → `@MockitoBean`; old name deprecated.)
- 9.4 *(ref: Boot — @DataJpaTest)* Configures JPA, repositories, and a
  transaction manager (not the full app). Each test runs in a
  **transaction that rolls back** at the end → tests don't pollute
  each other. By default it swaps in an **embedded DB (H2)** unless you
  tell it to use the real one (`@AutoConfigureTestDatabase(replace =
  NONE)` + Testcontainers).
- 9.5 *(ref: Boot — Testcontainers)* Testcontainers spins up the
  **real** dependency (Postgres, Kafka, Redis) in Docker for the test,
  so you catch dialect/behavior differences H2 masks. `Boot 3.1+`
  **`@ServiceConnection`** on a container bean auto-wires the app's
  `DataSource`/connection props to it — no manual `spring.datasource.*`
  overrides.
</details>

---

## 10. Production concerns (Actuator, logging, shutdown) · `[~]` awareness

- [~] **10.1** 💭 What does `spring-boot-starter-actuator` expose?
  Name `/health`, `/info`, `/metrics`, `/conditions`, `/env` — and the
  security caveat about exposing them.
- [~] **10.2** 💭 Health checks: liveness vs readiness probes — what
  each tells Kubernetes, and how Actuator groups map to them.
- [~] **10.3** 💭 Micrometer: the "SLF4J for metrics" one-liner — how a
  `MeterRegistry` fans out to Prometheus/etc. without app code changes.
- [~] **10.4** 💭 **Graceful shutdown** (`server.shutdown=graceful`) —
  what it changes about in-flight requests on SIGTERM, and why it
  matters for rolling deploys.
- [~] **10.5** 💭 Logging: default Logback, `logging.level.*` per
  package, and why structured/JSON logs matter in aggregation.

<details><summary>Solutions 10</summary>

- 10.1 *(ref: Boot — Actuator)* Actuator adds **operational
  endpoints**: `/actuator/health` (up/down + component details),
  `/info`, `/metrics`, `/conditions` (the auto-config report, §4.6),
  `/env`, `/loggers`, `/threaddump`, `/httpexchanges`. Most are
  **disabled over HTTP by default** — you opt in via
  `management.endpoints.web.exposure.include=` and should secure them
  (they leak internals).
- 10.2 *(ref: Boot — Kubernetes Probes)* **Liveness** = "is the app
  broken beyond recovery?" (fail → restart the pod). **Readiness** =
  "can it serve traffic right now?" (fail → pull from the load
  balancer, don't restart). Boot exposes
  `/actuator/health/liveness` and `/readiness` groups mapped to app
  lifecycle state.
- 10.3 *(ref: Boot — Metrics)* Micrometer is a **vendor-neutral facade
  for metrics** — your code records against a `MeterRegistry`, and a
  registry implementation ships them to Prometheus, Datadog, CloudWatch,
  etc. Swap the backend by swapping the dependency; instrumentation
  code is unchanged. (The "SLF4J of metrics.")
- 10.4 *(ref: Boot — Graceful Shutdown)* On SIGTERM the server **stops
  accepting new requests but lets in-flight ones finish** (up to a
  timeout) before the context closes. Without it, active requests are
  cut mid-flight on every rolling deploy → intermittent client errors.
- 10.5 *(ref: Boot — Logging)* Boot preconfigures **Logback** via
  SLF4J; set levels per package
  (`logging.level.org.hibernate.SQL=DEBUG`). JSON/structured logging
  makes logs machine-parseable for aggregation (ELK/Loki) — you filter
  by fields, not regex over free text.
</details>

---

## 11. Rapid-fire gotchas 🔮 · all **keep**

Predict/answer each, then verify:

- [ ] **11.1** A `@Component` you wrote isn't being picked up. Give the
  **three** most common reasons.
- [ ] **11.2** `@Value("${missing.prop}")` with no default — startup or
  runtime failure? What if you make the field `@ConfigurationProperties`
  instead?
- [ ] **11.3**
  ```java
  @RestController class C {
      @GetMapping("/x") String x() { return "home"; }  // returns to browser?
  }
  ```
  What does the client receive — the string `"home"` or a rendered view
  named `home`? Why?
- [ ] **11.4** Two `@Configuration` classes both declare a `@Bean
  clock()`. What happens at startup?
- [ ] **11.5** You annotate a **`private`** method with
  `@Transactional`. Compile error, runtime error, or **silently
  ignored**?
- [ ] **11.6** A singleton `@Service` has a mutable `int counter` field
  incremented per request under load. What's the bug class?
- [ ] **11.7** `@SpringBootApplication` sits in `com.acme.app`; a
  `@Component` sits in `com.acme.util`. Scanned or not?
- [ ] **11.8** `application.yml` sets `server.port: 8080`; you also pass
  `--server.port=9090` on the command line. Which wins?

<details><summary>Solutions 11</summary>

- 11.1 *(ref: Core/Boot — Component Scanning)* (1) It's **outside the
  scanned package tree** (not under the main class's package, §0.5).
  (2) It **lacks a stereotype** annotation (`@Component`/`@Service`/…).
  (3) A **`@Conditional`** / profile gate excludes it, or a
  same-type bean shadowed it. (Also: you're injecting the concrete
  class where an interface with two impls is ambiguous.)
- 11.2 *(ref: Boot — Externalized Config)* `@Value("${missing.prop}")`
  fails **at startup** (`IllegalArgumentException` resolving the
  placeholder) unless you give a default `${missing.prop:fallback}`.
  With `@ConfigurationProperties` a missing key just leaves the
  field at its **default value** — no failure (unless `@Validated`
  requires it).
- 11.3 *(ref: Web — @RestController)* The client gets the literal
  **`home`** as the response body. `@RestController` implies
  `@ResponseBody`, so the return value is serialized directly — it's
  **not** treated as a view name (that'd be a plain `@Controller`).
- 11.4 *(ref: Core — Bean Overriding)* By default **`Boot 2.1+`
  forbids bean overriding** → startup fails with a
  `BeanDefinitionOverrideException`. You'd have to set
  `spring.main.allow-bean-definition-overriding=true` (a smell) or
  rename/qualify one.
- 11.5 *(ref: Data Access — Proxy limitations)* **Silently ignored** —
  it compiles and runs, but a proxy cannot intercept or override a
  `private` method, so there's **no transaction**. Protected and
  package-visible methods are a different case for Spring 6+
  class-based proxies (§8.6). The scariest kind of bug: no error,
  wrong behavior.
- 11.6 *(ref: Core — Bean Scopes)* A **data race / shared mutable
  state** bug — one singleton instance is shared across all request
  threads, so `counter++` (not atomic) loses updates. Use
  `AtomicInteger`, a `Micrometer` counter, or don't keep request state
  on a singleton (§2.1).
- 11.7 *(ref: Boot — Component Scan)* **Not scanned.** Default scanning
  covers the main class's package **and all sub-packages**, but
  `com.acme.util` is a **sibling** of `com.acme.app`, not under it, so
  it is **NOT scanned** unless you widen `@ComponentScan(basePackages
  = "com.acme")` or move the main class up to `com.acme`. (Trap:
  "sub-package" ≠ "sibling package.")
- 11.8 *(ref: Boot — Config precedence)* **`--server.port=9090` wins.**
  Command-line args sit near the top of the precedence order, above
  `application.yml` (§3.1). App boots on 9090.
</details>

---

## Extensions — the deliberate "later" pile

This doc is the Pareto core. Each bullet below is a **candidate for
its own drill file** (mirroring how `go-basics.md` spun off
`go-concurrency.md`) — pull one in when a JD, an interview, or a
real task demands it. Rough priority top-to-bottom for a backend/
payments track.

Build status and the acceptance scope for the senior core live in
[senior-core-checklist.md](senior-core-checklist.md).

**Readable reference:**
[Spring Senior Backend Reference](spring-senior-core.md).

**Senior companions:**

- [spring-security-basics.md](spring-security-basics.md) — filter-chain
  architecture, authentication, request/method authorization, SpEL,
  JWT, CSRF/CORS, security failure boundaries and tests.
- [spring-data-jpa-performance.md](spring-data-jpa-performance.md) —
  persistence-context mechanics, mappings, fetch plans, projections,
  pagination, batching, bulk DML, locking, caches, OSIV and diagnostics.
- [spring-boot-transactions-deep.md](spring-boot-transactions-deep.md) —
  logical/physical scopes, rollback-only behavior, propagation/resource
  costs, deadlocks, programmatic boundaries, remote calls, DB/Kafka
  coordination, outbox and saga.
- [spring-container-internals.md](spring-container-internals.md) —
  `BeanDefinitionRegistry`,
  `BeanFactory` vs `ApplicationContext`, factory vs bean post-processors,
  context refresh, proxy creation, `FactoryBean`, and
  `@Configuration(proxyBeanMethods = ...)`.
- **`spring-boot-messaging.md`** — `@KafkaListener`, producer/consumer
  config, acks & offset commit semantics, `@RabbitListener`, retries &
  DLQ, `@TransactionalEventListener`, the transactional outbox
  pattern.
- [spring-boot-resilience.md](spring-boot-resilience.md) — Resilience4j
  (circuit breaker,
  retry, bulkhead, rate limiter), timeouts, `@Retryable`, graceful
  degradation, idempotency keys.
- **`spring-boot-observability.md`** — Micrometer metrics deep,
  distributed tracing (Micrometer Tracing / OpenTelemetry), custom
  Actuator endpoints, structured logging, correlation IDs.
- **`spring-webflux-reactive.md`** — `Mono`/`Flux`, the reactive
  stack vs MVC, `WebClient` streaming, backpressure, R2DBC, when
  reactive actually pays (and when it doesn't).
- **`spring-boot-caching-scheduling.md`** — `@Cacheable`/`@CacheEvict`
  + a real cache (Redis/Caffeine), `@Scheduled`, `@Async` executors &
  thread-pool tuning.
- **`spring-boot-testing-deep.md`** — Testcontainers patterns,
  `@ServiceConnection`, contract testing, `@Sql` fixtures, slice-test
  matrix, WireMock for external calls, MockMvc vs `WebTestClient`.
- **`spring-boot-api-design.md`** — versioning strategies, OpenAPI/
  springdoc, HATEOAS, pagination/filtering conventions, idempotent
  POST, ETags/conditional requests.
- **`spring-boot-native-startup.md`** — GraalVM native image, AOT
  processing, startup-time/memory wins & the reflection caveats,
  when it's worth it for serverless.
- **Spring beyond Boot** — Spring Batch (chunk processing), Spring
  Integration / Cloud Stream, Spring Cloud (config server, gateway,
  service discovery) — only if a JD names them.

---

## How to work through this

1. Keep **one scratch app** (start.spring.io) and one
   `ScratchTest` `@SpringBootTest` — most snippets here run as a
   test method faster than booting a server.
2. **Predict before running** every 🔮. For 💭 items, **say the
   answer aloud** in one or two sentences — articulation is the
   actual interview skill, same law as the Go drills.
3. Do the `🎯 CORE PATH` first: **§1 → §4 → §3 → §5 → §7 → §8**.
   Re-run **§1, §4, §8** after a week — DI, the auto-config magic,
   and the proxy traps are what get probed hardest and fade fastest.
4. When a section's boxes are checked and you can explain each
   `💭` cold, mark it done. Only pull an **Extensions** file when
   something real (a JD, a round, a task) demands it — don't build
   breadth for its own sake.

**The three things an interviewer is really testing:** (1) do you
understand the **container** (DI, beans, scopes — §1, §2); (2) do
you understand the **"magic"** (auto-config & starters — §4); (3)
do you understand that **it's all proxies** (§8) — self-invocation,
rollback rules, why `private`/`final`/internal calls break the
annotations. Own those three and the rest is vocabulary.
</content>
</invoke>
