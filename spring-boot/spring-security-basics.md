# Spring Security — Senior Backend Anti-Fumble Kit

For a backend engineer who has used secured Spring endpoints but needs
to explain **why** a request becomes authenticated, **where** an
authorization decision happens, and **how** the design fails in
production. The goal is not to memorize every filter or OAuth grant.
It is to trace one request, predict the result, and defend the security
boundary under interview pressure.

**Readable reference:** [Spring Senior Backend Reference](spring-senior-core.md),
chapters 8–9.

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/configure · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Answers sit under each section. Attempt the prompt aloud before
opening its `<details>` block. The compact answer card near the end is
for rehearsal only; learning happens in the exercises.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §6 → §7 → §8 → §9 → §10**
>
> Mental model · filter chain · authentication · request authorization
> · method security/SpEL · JWT · CSRF/CORS · failure boundary · tests ·
> production design.
>
> A senior answer has four layers: **mechanism**, **decision**,
> **failure mode**, and **trade-off**. “I add `@PreAuthorize`” names a
> tool. “A method interceptor evaluates the current `Authentication`
> before crossing the service boundary, and self-invocation bypasses
> that proxy” explains the system.

> **Version line.** The examples use component-based configuration:
> a `SecurityFilterChain` bean, `authorizeHttpRequests`, and
> `@EnableMethodSecurity`. That is the modern Spring Security style
> used by Boot 3/4. A Boot 2.7 estate can use this style too; older
> applications may instead extend the now-removed
> `WebSecurityConfigurerAdapter`, call `authorizeRequests` /
> `antMatchers`, and enable methods with
> `@EnableGlobalMethodSecurity`. Be able to recognize the old form,
> but write the component-based form in new code.

---

## 0. The one picture — two checkpoints, one identity · 🎯 CORE PATH

```text
HTTP request
    │
    ▼
Servlet container filter chain
    │
    ▼
DelegatingFilterProxy
    │ delegates to Spring bean "springSecurityFilterChain"
    ▼
FilterChainProxy
    │ selects the first matching SecurityFilterChain
    ▼
security filters
    ├─ restore/create SecurityContext
    ├─ authenticate credentials/token
    ├─ handle security exceptions
    └─ authorize the HTTP request             CHECKPOINT 1
    │
    ▼
DispatcherServlet → controller → service proxy
                                  │
                                  └─ method authorization            CHECKPOINT 2
                                             │
                                             ▼
                                      business method
```

- [ ] **0.1** 💭 Authentication vs authorization: define each in one
  sentence and say which must happen first.
- [ ] **0.2** 💭 What is the difference between request/route
  authorization and method authorization? Why might a senior design
  use both?
- [ ] **0.3** 🔮 An unauthenticated request is rejected in the security
  filter chain. Does `@RestControllerAdvice` get a chance to format
  that exception? Explain the boundary.
- [ ] **0.4** 💭 Where is the current user's identity represented during
  the request?

<details><summary>Solutions 0</summary>

- 0.1 **Authentication (authN)** proves or establishes *who the caller
  is*. **Authorization (authZ)** decides *what that caller may do*.
  Authorization needs an identity (including anonymous identity), so
  authentication/context establishment precedes the access decision.
- 0.2 Request authorization protects an **HTTP surface** using method,
  path, dispatcher type, and the current `Authentication`. Method
  authorization protects a **Java method invocation**, so it also
  applies when the service is called from a listener, scheduler, or
  another controller. Use route rules for a secure/default-deny HTTP
  perimeter and method rules for high-value business invariants or
  object-level decisions. One is not automatically a substitute for
  the other.
- 0.3 Usually **no**. Security filters run before the
  `DispatcherServlet`; controller advice participates in Spring MVC's
  `HandlerExceptionResolver` chain after MVC has been entered.
  Authentication and access-denied failures at the security boundary
  belong to an `AuthenticationEntryPoint`, `AccessDeniedHandler`, or
  an authentication failure handler (§8).
- 0.4 In an `Authentication` stored inside a `SecurityContext`, normally
  accessed through `SecurityContextHolder`. It holds the principal,
  credentials or credential state, authorities, authentication flag,
  and sometimes request details. Treat it as request security state,
  not as the domain `User` aggregate.

</details>

---

## 1. Filter-chain architecture ⭐⭐ · 🎯 CORE PATH

- [ ] **1.1** 💭 Why does Spring Security live in servlet filters instead
  of only in controllers or MVC interceptors?
- [ ] **1.2** 💭 Give one line each for `DelegatingFilterProxy`,
  `FilterChainProxy`, and `SecurityFilterChain`.
- [ ] **1.3** 🔮 There are three `SecurityFilterChain` beans. How many run
  for one request? What decides?
- [ ] **1.4** 🐛 A custom JWT filter is added after authorization. Tokens
  validate, but protected routes still return 401. Diagnose it.
- [ ] **1.5** 🐛 A chain has `securityMatcher("/api/**")`, but there is no
  fallback chain. What protects `/actuator/health` and `/admin`?
- [ ] **1.6** 🛠 Turn on useful security diagnostics without adding print
  statements to every filter.

<details><summary>Solutions 1</summary>

- 1.1 Filters surround the entire servlet boundary **before MVC route
  selection**. They can reject a request without invoking application
  code, populate request security state, apply exploit protection and
  response headers, and cover non-controller servlet resources too.
  An MVC interceptor begins too late to be the primary security
  perimeter.
- 1.2 `DelegatingFilterProxy` bridges the servlet container to a
  Spring-managed `Filter` bean. That bean is normally
  `FilterChainProxy`, Spring Security's master filter. It selects the
  first matching `SecurityFilterChain`, which is a request matcher plus
  an ordered list of security filters.
- 1.3 **One** matching security chain is selected — the first match by
  chain order. The filters inside that selected chain run in their
  defined order. This is distinct from authorization rules *inside*
  the chain, where the first matching rule wins (§3).
- 1.4 Authentication must be established **before authorization**.
  Move the custom filter before `AuthorizationFilter` (often relative
  to a known authentication filter), set a valid authenticated object
  in the context, and continue the chain. Better: use Spring's OAuth2
  resource-server support rather than hand-rolling bearer-token
  parsing (§6).
- 1.5 **Nothing in Spring Security protects them** if no
  `SecurityFilterChain` matches. A `securityMatcher` narrows the whole
  chain. Add an ordered fallback chain matching all remaining requests,
  normally with `anyRequest().denyAll()` or an explicit authenticated
  policy.
- 1.6 Set focused log levels, for example:

  ```properties
  logging.level.org.springframework.security=DEBUG
  # TRACE is noisy but can expose the chosen chain and filter sequence.
  ```

  Use briefly and never log raw bearer tokens, passwords, session IDs,
  or sensitive claims.

</details>

### Multiple-chain shape

```java
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    SecurityFilterChain fallback(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().denyAll())
            .build();
    }
}
```

`securityMatcher` chooses whether the **entire chain** applies.
`requestMatchers` chooses an **authorization rule inside** the selected
chain. Mixing those two levels is a common production misconfiguration.

---

## 2. Authentication architecture & passwords ⭐⭐ · 🎯 CORE PATH

- [ ] **2.1** 💭 Define `SecurityContextHolder`, `SecurityContext`,
  `Authentication`, `GrantedAuthority`, `AuthenticationManager`,
  `ProviderManager`, and `AuthenticationProvider` without collapsing
  them into “Spring magic.”
- [ ] **2.2** 🔮 Trace username/password authentication: what is the
  unauthenticated input object, who verifies it, and what represents
  success?
- [ ] **2.3** 💭 `UserDetailsService` vs `AuthenticationProvider` — when
  is loading a user enough, and when do you implement a provider?
- [ ] **2.4** 🐛 Login always fails after migrating stored BCrypt hashes.
  The rows contain `$2a$...`, but Spring reports “no PasswordEncoder
  mapped for id null.” Why?
- [ ] **2.5** 🐛 Code checks a password using
  `encoder.encode(raw).equals(stored)`. Why does this fail even when
  the password is correct?
- [ ] **2.6** 🏭 Why should a slow adaptive password hash be tuned for a
  bounded verification time, and what production resource can it
  exhaust during a login attack?

<details><summary>Solutions 2</summary>

- 2.1 `SecurityContextHolder` is the access strategy for the current
  context (thread-local by default). `SecurityContext` contains the
  current `Authentication`. `Authentication` represents either
  submitted credentials or an authenticated principal plus
  authorities. A `GrantedAuthority` is one permission/role/scope.
  `AuthenticationManager` is the authentication API; `ProviderManager`
  is its common implementation and delegates to compatible
  `AuthenticationProvider`s. A provider knows how to verify one token
  type, such as username/password or JWT.
- 2.2 An authentication filter builds an unauthenticated token such as
  `UsernamePasswordAuthenticationToken` and submits it to the
  `AuthenticationManager`. `ProviderManager` selects a provider; a
  DAO provider commonly loads `UserDetails` and invokes a
  `PasswordEncoder`. On success it returns an authenticated
  `Authentication` with authorities. The filter stores that result in
  the `SecurityContext` and applies the configured persistence policy.
- 2.3 `UserDetailsService` only **loads identity data** by username; it
  does not authenticate a request by itself. It fits standard
  username/password authentication with `DaoAuthenticationProvider`.
  Implement/configure another `AuthenticationProvider` when credential
  verification is different — proprietary tokens, an external legacy
  service, a client certificate mapping, or another authentication
  token type.
- 2.4 `DelegatingPasswordEncoder` expects the storage format
  `{id}encodedPassword`, for example `{bcrypt}$2a$...`. Existing raw
  BCrypt hashes lack `{bcrypt}`, so no delegate is selected. Migrate
  or prefix rows only after verifying their real format; do not “fix”
  it by enabling plaintext encoding.
- 2.5 Adaptive hashes use a random salt, so encoding the same password
  twice normally produces different strings. Call
  `encoder.matches(raw, stored)`; the stored format supplies the salt
  and algorithm parameters needed for verification.
- 2.6 BCrypt/Argon2/PBKDF2/scrypt deliberately consume CPU and/or
  memory to make offline cracking expensive. Tune the work factor on
  production-class hardware, but rate-limit authentication: an
  attacker can otherwise turn verification into CPU/thread-pool
  exhaustion. Hash strength and online abuse protection solve
  different threats.

</details>

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Never store plaintext passwords, reversible encrypted passwords, or a
fast unsalted digest such as raw SHA-256. Never log credentials.

---

## 3. Request/route authorization ⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 🛠 Write a chain with these rules:
  `POST /users` is public, `GET /accounts/{id}` needs
  `account:read`, `/admin/**` needs role `ADMIN`, and everything else
  is denied.
- [ ] **3.2** 🔮 Does the first or most-specific matching authorization
  rule win? Explain why `anyRequest().permitAll()` at the top is a
  catastrophe.
- [ ] **3.3** 💭 `hasRole("ADMIN")` vs
  `hasAuthority("ROLE_ADMIN")` — equivalent by default or not? What
  about `hasAuthority("ADMIN")`?
- [ ] **3.4** 💭 `permitAll()` vs ignoring a path through
  `WebSecurityCustomizer`: why is permit-all usually safer?
- [ ] **3.5** 🐛 A new controller endpoint shipped without a matching
  rule. What final rule prevents accidental exposure?
- [ ] **3.6** 🏭 Path access says a caller has `account:read`. Is that
  sufficient to allow `GET /accounts/42` in a multi-tenant system?

<details><summary>Solutions 3</summary>

- 3.1 One defensible shape:

  ```java
  @Bean
  SecurityFilterChain web(HttpSecurity http) throws Exception {
      return http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.POST, "/users").permitAll()
              .requestMatchers(HttpMethod.GET, "/accounts/**")
                  .hasAuthority("account:read")
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .anyRequest().denyAll())
          .httpBasic(Customizer.withDefaults())
          .build();
  }
  ```

  Put narrow rules before broad rules and make the unmatched policy
  deliberate. `permitAll()` only answers authorization: with CSRF
  enabled, a state-changing `POST` still needs a valid CSRF token.
- 3.2 **First match wins**, in declaration order. Spring does not
  search for the “most specific” later rule. An early catch-all
  `permitAll` makes every later restriction unreachable.
- 3.3 By default, `hasRole("ADMIN")` looks for the exact authority
  **`ROLE_ADMIN`**, so it is equivalent to
  `hasAuthority("ROLE_ADMIN")`. `hasAuthority("ADMIN")` looks for the
  literal `ADMIN` and is different. Pick a convention; authorities
  such as `account:read` often scale more cleanly than turning every
  permission into a role.
- 3.4 `permitAll` keeps the request inside Spring Security, so security
  headers, firewall behavior, context cleanup, and other filters still
  apply. Ignoring bypasses the security chain entirely. Prefer
  `permitAll` unless bypassing all security processing is an explicit,
  understood requirement.
- 3.5 End the rule set with **`anyRequest().denyAll()`** for strict
  allow-listing, or at minimum `authenticated()` when every
  authenticated caller is intentionally allowed. Deny-by-default
  makes a missing rule fail closed.
- 3.6 No. That is coarse-grained permission, not object ownership or
  tenant isolation. Also verify that account 42 belongs to an allowed
  tenant/customer, ideally at the service/query boundary. Otherwise
  the API has an IDOR/BOLA vulnerability: the caller changes an ID and
  reads someone else's object.

</details>

---

## 4. Method security & SpEL ⭐⭐ · 🎯 CORE PATH

- [ ] **4.1** 🛠 Enable modern method security. Does adding
  `spring-boot-starter-security` enable `@PreAuthorize` by itself?
- [ ] **4.2** 💭 `@PreAuthorize` vs `@PostAuthorize`: when is each
  evaluated, and why is post-authorization risky on a mutating method?
- [ ] **4.3** 🛠 Protect `readAccount(long accountId)` so the caller
  needs `account:read` and a bean named `accountAuth` approves that
  account.
- [ ] **4.4** 💭 Explain these SpEL names: `authentication`,
  `principal`, `#accountId`, `@accountAuth`, `returnObject`, and
  `filterObject`.
- [ ] **4.5** 🐛 A method annotated with `@PreAuthorize` is called from
  another method in the same service and is not checked. Diagnose and
  fix it.
- [ ] **4.6** 💭 `@Secured`, JSR-250 `@RolesAllowed`, and
  `@PreAuthorize`: which is the normal modern choice and why?
- [ ] **4.7** 🏭 An authorization annotation has a 200-character SpEL
  expression with database lookups and five boolean branches. What
  design change would you propose?
- [ ] **4.8** 🔮 Route security allows the endpoint but method security
  denies it. Which result wins?
- [ ] **4.9** 🛠 `updateAccounts` accepts two collection arguments.
  Filter only the `accounts` argument before invocation so the method
  receives accounts owned by the current principal. When is
  `filterTarget` required?
- [ ] **4.10** 🛠 Apply `@PostFilter` to an account-list query. What does
  `filterObject` mean for a collection and for a map, and why is this
  usually the wrong primary tenant filter for a large or paged result?
- [ ] **4.11** 🏭 A bulk money operation receives ten commands, three
  unauthorized. Should `@PreFilter` silently run the other seven, or
  should the whole command fail? State the business decision before
  choosing the annotation.

<details><summary>Solutions 4</summary>

- 4.1 Add `@EnableMethodSecurity` to a configuration class. The
  security starter secures the web application by default, but it does
  **not** activate method authorization. Modern method security enables
  `@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, and `@PostFilter`
  by default.

  ```java
  @Configuration
  @EnableMethodSecurity
  class MethodSecurityConfig {}
  ```

- 4.2 `@PreAuthorize` decides **before** invocation; it prevents the
  method and its side effects. `@PostAuthorize` evaluates the returned
  object **after** invocation but before giving it to the caller. It is
  useful for ownership checks on reads, but on a write the mutation may
  already have occurred before denial. Protect writes before execution.
- 4.3 For example:

  ```java
  @PreAuthorize("hasAuthority('account:read') and " +
                "@accountAuth.canRead(authentication, #accountId)")
  public Account readAccount(long accountId) { ... }
  ```

  Prefer compiling with `-parameters` or use explicit parameter-name
  metadata so expression variables remain discoverable.
- 4.4 `authentication` is the current `Authentication`; `principal` is
  its principal; `#accountId` is the invoked method argument;
  `@accountAuth` references a Spring bean; `returnObject` is available
  to post-authorization; `filterObject` represents each element during
  pre/post filtering. `#` means evaluation-context variable; `@` means
  bean reference.
- 4.5 Method security is Spring AOP. A `this.securedMethod()` call does
  not cross the proxy, so its interceptor never runs. Put the secured
  operation behind another injected bean, secure the externally called
  entry point, or redesign the boundary. Self-injecting the proxy is
  possible but usually makes the design harder to understand.
- 4.6 Prefer **`@PreAuthorize`** for modern code because it supports
  authorities, arguments, ownership beans, and the common method
  security infrastructure. `@Secured` is a limited legacy option.
  `@RolesAllowed` is useful when a portable JSR-250 role check is all
  you need, but it must be explicitly enabled.
- 4.7 Move policy into a named Java component or custom
  `AuthorizationManager`, give it typed inputs, unit-test it directly,
  and keep the annotation declarative. Complicated strings are hard to
  refactor, observe, and test. Better yet, grant authorities that make
  the common decision simple instead of rebuilding the organization's
  policy engine in every expression.
- 4.8 Access is denied. Passing the HTTP checkpoint only permits the
  request to reach application code. Every later authorization
  checkpoint must also grant access.
- 4.9 `@PreFilter` evaluates each candidate **before** the target method
  runs. `filterObject` is the current element; when more than one
  filterable argument exists, name the intended parameter explicitly:

  ```java
  @PreFilter(
      value = "filterObject.owner == authentication.name",
      filterTarget = "accounts"
  )
  public void updateAccounts(Collection<Account> accounts,
                             Collection<String> auditTags) {
      // accounts contains only elements that passed the expression
  }
  ```

  Current method-security filtering supports arrays, collections,
  maps, and open streams. For a map, `filterObject` is its entry, so
  expressions normally inspect `filterObject.key` or
  `filterObject.value`. Collection filtering removes rejected
  elements; do not pass an immutable collection and assume it will be
  copied. Test the exact argument shape through the Spring proxy.
- 4.10 `@PostFilter` evaluates the returned elements **after** the
  method has loaded and produced them:

  ```java
  @PostFilter("filterObject.owner == authentication.name")
  public Collection<Account> readAccounts() {
      return repository.findAll();
  }
  ```

  For a collection/array/stream, `filterObject` is the current element;
  for a map it is the current entry. This can be useful for a small,
  bounded in-memory result or defense in depth, but it does not stop
  unauthorized rows from being fetched. On large results it wastes
  database, network, memory, and policy work. On a page it can also
  leave misleading page sizes/totals. Put tenant/ownership predicates
  in the repository query and keep post-filtering as an optional final
  guard.
- 4.11 Filtering and denial are different product semantics.
  `@PreFilter` silently removes unauthorized inputs and invokes the
  method with the remainder. That can be correct for "process every
  command you may process," but it is dangerous for an atomic transfer
  or reconciliation batch because the caller may believe all ten
  succeeded. For all-or-nothing work, pre-authorize the whole command
  with a typed policy (or validate ownership in the service) and reject
  before side effects. Use filtering only when partial processing is an
  explicit, observable contract with per-item outcomes.

</details>

### SpEL boundary card

```text
${app.timeout}       property placeholder resolved from Environment
#{2 * 60}            general Spring Expression Language expression

@PreAuthorize("hasAuthority('account:read')")
              └─ security SpEL with a SecurityExpressionRoot

#id                  method/evaluation-context variable
@policy              Spring bean reference
authentication       current Authentication
principal            authentication.principal
returnObject         result in @PostAuthorize
filterObject         current collection element in filter expressions
```

Do not parse or evaluate user-supplied SpEL. Prefer typed Java policy
for complex rules and authorities for stable capabilities.

---

## 5. Sessions, context persistence & async boundaries ⭐

- [ ] **5.1** 💭 Stateful session authentication vs stateless bearer
  authentication: where does the identity come from on request two?
- [ ] **5.2** 💭 `ALWAYS`, `IF_REQUIRED`, `NEVER`, and `STATELESS` —
  distinguish the session-creation policies.
- [ ] **5.3** 🐛 A JWT API declares `STATELESS`, but a controller
  creates an `HttpSession`. Does `STATELESS` prohibit all application
  sessions?
- [ ] **5.4** 🐛 An `@Async` task sees no current user even though the
  controller was authenticated. Why?
- [ ] **5.5** 🏭 Why is blindly switching
  `SecurityContextHolder` to inheritable thread-local dangerous with
  thread pools?

<details><summary>Solutions 5</summary>

- 5.1 With stateful login, successful authentication is normally saved
  and later restored using a session-backed security-context
  repository. With stateless bearer authentication, each request
  supplies a token and is authenticated independently; the server does
  not load the security context from an HTTP session.
- 5.2 `ALWAYS` always creates a session. `IF_REQUIRED` creates one when
  security needs it. `NEVER` does not create one but may use an
  existing session. `STATELESS` neither creates nor uses an HTTP
  session to obtain/save the `SecurityContext`.
- 5.3 No. It controls **Spring Security's context persistence and
  session use**, not every call to `request.getSession()` in the
  application. Avoid application sessions in a stateless API because
  they undermine horizontal simplicity, but do not mistake the policy
  for a servlet-container ban.
- 5.4 `SecurityContextHolder` is thread-bound by default. `@Async`
  executes on another thread, so the context does not magically move.
  Pass the required identity explicitly when possible, or use Spring
  Security's delegating security-context executor/task wrappers with a
  deliberately captured context.
- 5.5 Pool threads are reused across unrelated work. Careless
  inheritance or failure to clear context can leak one user's identity
  into another task. Explicit propagation wrappers capture, install,
  and clear the context around the delegated work.

</details>

---

## 6. JWT/OAuth2 resource server ⭐⭐ · 🎯 CORE PATH

- [ ] **6.1** 💭 Authorization server, client, resource owner, and
  resource server — map the four OAuth2 roles for a backend API.
- [ ] **6.2** 💭 Is a JWT encrypted? What must a resource server
  validate before trusting its claims?
- [ ] **6.3** 🛠 Configure a stateless resource server using an issuer
  URI and require scope `accounts.read`.
- [ ] **6.4** 🔮 A token contains scope `accounts.read`. Which authority
  does Spring expose by default?
- [ ] **6.5** 🐛 The API Base64-decodes a JWT and trusts `sub` without
  verifying its signature. Explain the vulnerability.
- [ ] **6.6** 🏭 JWT vs opaque token: compare latency, revocation,
  central control, and failure dependencies.
- [ ] **6.7** 🏭 A user is disabled, but their signed access token works
  for another ten minutes. Is that necessarily a validation bug? Give
  mitigation options.
- [ ] **6.8** 💭 Authentication at the API gateway vs at every service:
  can an internal service simply trust any forwarded headers?

<details><summary>Solutions 6</summary>

- 6.1 The **resource owner** is the user/entity granting access; the
  **client** requests/uses a token; the **authorization server**
  authenticates/grants and issues tokens; the **resource server** is
  this API, which validates the access token and protects resources.
  One product can play multiple roles, but the responsibilities remain
  distinct.
- 6.2 A normal signed JWT is **encoded, not encrypted**; anyone holding
  it can often read its claims. Validate the signature and trusted
  algorithm/key, issuer (`iss`), expiry (`exp`), not-before (`nbf`),
  intended audience (`aud`) when applicable, and application-specific
  claims. Never accept `alg=none`, an untrusted key URL, or a key chosen
  from attacker-controlled input.
- 6.3 Minimal shape:

  ```yaml
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: https://idp.example.com/issuer
            audiences: https://accounts-api.example.com
  ```

  ```java
  @Bean
  SecurityFilterChain api(HttpSecurity http) throws Exception {
      return http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.GET, "/accounts/**")
                  .hasAuthority("SCOPE_accounts.read")
              .anyRequest().authenticated())
          .oauth2ResourceServer(oauth ->
              oauth.jwt(Customizer.withDefaults()))
          .sessionManagement(session -> session
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .csrf(AbstractHttpConfigurer::disable)
          .build();
  }
  ```

- 6.4 **`SCOPE_accounts.read`**. Spring's default JWT converter maps
  `scope`/`scp` values to authorities prefixed with `SCOPE_`.
- 6.5 Base64 decoding proves nothing. An attacker can create a token
  with any subject or role and hand it to the API. Cryptographic
  signature verification and claim validation are what establish
  trust; use the resource-server `JwtDecoder` pipeline rather than a
  handwritten parser/filter.
- 6.6 A self-contained JWT is validated locally: low per-request
  network latency and continued validation during a temporary
  authorization-server outage, but revocation is harder and claims are
  stale until expiry. An opaque token commonly needs introspection:
  central, near-current control and easier revocation, but adds a
  network/cache dependency and latency. Short-lived JWTs plus refresh
  control are a common compromise.
- 6.7 Not necessarily. Local JWT validation verifies the token, not the
  user's live database status. Options: short access-token TTL,
  revoke/rotate refresh tokens, a deny-list for exceptional cases,
  version/security-stamp claims, introspection for high-risk actions,
  or event-driven invalidation. Each adds state, latency, or operational
  complexity.
- 6.8 A gateway is useful for coarse edge authentication, rate limits,
  and token relay, but services should validate a verifiable credential
  or use a strongly authenticated trusted channel. Arbitrary forwarded
  identity headers are forgeable if a caller can bypass the gateway.
  Apply defense in depth according to the network and threat model.

</details>

---

## 7. CSRF, CORS & browser boundaries ⭐⭐ · 🎯 CORE PATH

- [ ] **7.1** 💭 What condition makes CSRF possible? Why is “the API is
  stateless” not, by itself, proof that CSRF can be disabled?
- [ ] **7.2** 🔮 A browser uses a session cookie for authentication and
  sends `POST /transfer`. Should CSRF normally be enabled?
- [ ] **7.3** 🔮 A non-browser client sends a bearer token explicitly in
  the `Authorization` header, and the server uses no authentication
  cookies. Is CSRF protection normally useful for that endpoint?
- [ ] **7.4** 💭 CORS vs CSRF: define the threat/control each addresses.
- [ ] **7.5** 🐛 Browser preflight receives 401 before reaching the
  controller. Why, and what ordering/configuration fixes it?
- [ ] **7.6** 🐛 Configuration uses allowed origin `*` together with
  credentialed cross-origin requests. What is wrong with that policy?
- [ ] **7.7** 🏭 Does a successful CORS policy authorize a caller to read
  an account? Does blocking CORS stop curl or another server?

<details><summary>Solutions 7</summary>

- 7.1 CSRF is possible when a user agent **automatically attaches
  credentials** to a cross-site request, commonly a session cookie.
  Server-side statelessness does not help if the browser still sends an
  authentication cookie or automatically reused credential. Decide
  from the credential transport, not the word “REST.”
- 7.2 Yes. A malicious site can cause the victim's browser to submit a
  state-changing request with the victim's cookie. Keep Spring
  Security's CSRF protection and expose the token to the legitimate UI
  using an appropriate repository/integration.
- 7.3 Usually no, because another origin cannot make the browser
  automatically attach a bearer value that only the legitimate client
  adds to the header. Disabling CSRF for that chain can be appropriate.
  XSS/token theft remains a separate threat.
- 7.4 **CSRF** prevents unwanted state-changing requests made with
  automatically attached victim credentials. **CORS** is a browser
  policy controlling which origins may read/use cross-origin responses
  through browser scripts. CORS is not authentication, authorization,
  or a defense against non-browser clients.
- 7.5 A preflight `OPTIONS` request normally has no login cookie and
  must be handled before authentication rejection. Integrate CORS with
  Spring Security using a `CorsConfigurationSource` / MVC CORS config
  and `http.cors(...)`, and permit the required preflight behavior.
- 7.6 Credentialed CORS must name trusted origins; wildcard origin with
  credentials is invalid/unsafe. Keep the allow-list narrow, including
  scheme and port, and allow only required methods and headers.
- 7.7 No and no. CORS only controls browser cross-origin access.
  Spring Security authorization still decides access, and curl/server
  clients do not enforce browser CORS policy.

</details>

---

## 8. 401/403 and the exception boundary ⭐⭐ · 🎯 CORE PATH

- [ ] **8.1** 💭 State the interview-safe distinction between 401 and
  403.
- [ ] **8.2** 💭 What do `AuthenticationEntryPoint`,
  `AccessDeniedHandler`, and `ExceptionTranslationFilter` each do?
- [ ] **8.3** 🐛 A team adds an `@ExceptionHandler(AccessDeniedException.class)`
  and expects all forbidden requests to use it. Some still return the
  default security JSON/HTML. Why?
- [ ] **8.4** 🛠 Sketch consistent JSON handling for unauthenticated and
  forbidden API requests.
- [ ] **8.5** 🏭 What security details should an API error body and log
  contain — and what must they not reveal?

<details><summary>Solutions 8</summary>

- 8.1 **401** means authentication is absent or invalid — “prove who
  you are”; it may include `WWW-Authenticate`. **403** means the
  request has an established identity but lacks permission — “I know
  you; this action is refused.” Do not use 403 as a generic login
  failure.
- 8.2 `ExceptionTranslationFilter` converts security exceptions into
  an HTTP response workflow. It invokes an
  `AuthenticationEntryPoint` when authentication must begin/fails at
  the authorization boundary, and an `AccessDeniedHandler` for an
  authenticated caller denied access. Authentication filters may also
  have their own failure handlers.
- 8.3 Some failures occur **before MVC**, in the servlet security
  chain, so MVC controller advice cannot see them. Other access-denied
  exceptions may arise inside a proxied controller/service after MVC
  is entered. Define the API's security-boundary handlers as well as
  its controller/domain exception mapping.
- 8.4 For example:

  ```java
  @Bean
  SecurityFilterChain api(HttpSecurity http, ObjectMapper json)
          throws Exception {
      return http
          .exceptionHandling(errors -> errors
              .authenticationEntryPoint((request, response, ex) -> {
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                  json.writeValue(response.getOutputStream(),
                      Map.of("code", "UNAUTHENTICATED",
                             "message", "Authentication is required"));
              })
              .accessDeniedHandler((request, response, ex) -> {
                  response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                  json.writeValue(response.getOutputStream(),
                      Map.of("code", "FORBIDDEN",
                             "message", "Access is denied"));
              }))
          // authentication and authorization configuration...
          .build();
  }
  ```

  In production, centralize the writer/body type to keep headers,
  correlation IDs, content type, and schema consistent.
- 8.5 Give clients a stable code, safe message, HTTP status, instance/
  correlation ID, and perhaps a documentation type. Log the decision,
  route, principal identifier when known, policy/rule, and trace ID at
  an appropriate level. Never return stack traces, parser details,
  password hints, secrets, raw tokens, or sensitive claims. Avoid
  logging differences that enable user enumeration.

</details>

---

## 9. Security testing ⭐⭐ · 🎯 CORE PATH

- [ ] **9.1** 💭 What must be tested for each protected endpoint beyond
  the happy path?
- [ ] **9.2** 🔮 `@WithMockUser(roles = "ADMIN")` grants which exact
  authority? What does `authorities = "ADMIN"` grant?
- [ ] **9.3** 🐛 A MockMvc POST with `@WithMockUser` returns 403 even
  though the role is correct. Session-cookie security is configured.
  What is probably missing?
- [ ] **9.4** 🛠 Test a JWT-protected endpoint without minting a real
  signed JWT.
- [ ] **9.5** 🐛 A unit test constructs `new AccountService()` and
  expects `@PreAuthorize` to reject the call. Why does it pass?
- [ ] **9.6** 🏭 Why should an authorization test include cross-tenant
  object IDs, not just roles?

<details><summary>Solutions 9</summary>

- 9.1 Test unauthenticated → 401, authenticated without authority →
  403, correct authority → intended result, wrong HTTP method/path,
  matcher ordering/fallback behavior, CSRF when applicable, and
  object/tenant ownership. Also test the response schema so security
  failures do not leak internals.
- 9.2 `roles = "ADMIN"` grants **`ROLE_ADMIN`**. `authorities =
  "ADMIN"` grants literal **`ADMIN`**. This catches many false test
  expectations.
- 9.3 A valid **CSRF token**. With Spring Security MockMvc support:

  ```java
  mvc.perform(post("/transfer")
          .with(user("ada").roles("USER"))
          .with(csrf()))
     .andExpect(status().isOk());
  ```

- 9.4 Use the resource-server test request post-processor:

  ```java
  mvc.perform(get("/accounts/42")
          .with(jwt().authorities(
              new SimpleGrantedAuthority("SCOPE_accounts.read"))))
     .andExpect(status().isOk());
  ```

  Separately integration-test real decoder configuration/signatures
  where that boundary matters.
- 9.5 A plain object is not a Spring bean and has no method-security
  proxy. Test the Spring-managed bean with method security enabled, or
  unit-test the extracted authorization policy directly. An annotation
  without its infrastructure is metadata, not enforcement.
- 9.6 Roles prove a coarse capability, not resource ownership. Testing
  “user can read own account but not another tenant's account” catches
  IDOR/BOLA bugs that role-only tests miss.

</details>

---

## 10. Senior production scenarios ⭐⭐ · 🎯 CORE PATH

- [ ] **10.1** 🏭 A public signup endpoint accidentally shadows the
  `/admin/**` rule. State the prevention, detection, and test strategy.
- [ ] **10.2** 🏭 JWT validation adds 300 ms to every request because the
  service calls the identity provider each time. What architectural
  mismatch do you suspect?
- [ ] **10.3** 🏭 Authorization makes a repository call for every item in
  a returned page. What security/performance smell is this, and how
  would you redesign it?
- [ ] **10.4** 🏭 One service trusts `X-User-Id` because the gateway sets
  it. During an incident, an internal caller forges the header. Give
  layered fixes.
- [ ] **10.5** 🏭 A service logs complete JWTs to diagnose 401s. Why is
  this an incident risk even if the tokens expire in ten minutes?
- [ ] **10.6** 🏭 Where should tenant isolation live: route rules,
  method security, repository queries, or the database?
- [ ] **10.7** 🏭 A request is authenticated and authorized, yet should
  still be throttled. Is rate limiting authentication or
  authorization?
- [ ] **10.8** 🏭 What would you monitor for an authentication system in
  production?

<details><summary>Solutions 10</summary>

- 10.1 Prevention: narrow first-match rules and end with deny-all;
  avoid broad public wildcards. Detection: startup/config review and
  security DEBUG logs in a safe environment. Tests: one matrix covering
  anonymous/user/admin across public and admin paths, plus an unmatched
  route. Treat matcher changes as security-sensitive code review.
- 10.2 A self-contained JWT should normally be signature/claim-validated
  locally using cached JWKs. Per-request introspection is opaque-token
  behavior or a custom online check. Decide intentionally: local JWT
  validation for latency/availability, introspection for central live
  control, or a bounded cache depending on revocation requirements.
- 10.3 It is authorization's version of **N+1** and often indicates
  policy being evaluated too late, item by item. Push tenant/ownership
  constraints into one repository query, model useful authorities,
  batch policy input, or return a projection already restricted to
  visible rows. Never fetch forbidden rows merely to discard them in
  Java when the datastore can enforce the boundary.
- 10.4 Prevent direct untrusted access to the service; authenticate
  service-to-service traffic (mTLS/workload identity or signed tokens);
  strip inbound identity headers at the trusted edge; pass a signed,
  audience-bound credential; and have the service validate it. Network
  location alone is weak identity.
- 10.5 A bearer token is a credential: anyone holding it may replay it
  during its lifetime, and logs are copied into aggregators, backups,
  support tools, and developer screens. Claims may also contain PII.
  Log safe token metadata such as issuer, key id, validation category,
  and a non-reversible correlation fingerprint — never the credential.
- 10.6 Use layers. Routes enforce coarse HTTP capabilities; the service
  enforces business ownership independent of transport; repository
  predicates prevent cross-tenant reads/writes; database controls such
  as separate schemas or row-level security can add defense for strong
  isolation needs. The exact depth follows the threat model, but a
  caller-supplied tenant ID must never be trusted by itself.
- 10.7 Rate limiting is an abuse/resilience control. It can use identity
  and policy, but it is neither proof of identity nor the core access
  decision. Place coarse limits at the edge and protect expensive
  service operations with appropriate local controls.
- 10.8 Monitor authentication success/failure by safe reason category,
  401/403 rates by route/client, latency and saturation of password or
  introspection work, token validation failures (issuer/audience/
  expiry/signature), JWK refresh failures, session counts when stateful,
  lockouts/rate limits, and unusual geographic/client patterns. Alert
  on changes, not merely totals; never label metrics with raw users or
  tokens because of cardinality and privacy.

</details>

---

## 11. Rapid-fire trap wall 🔮 · all keep

- [ ] **11.1** Adding the security starter makes every route public or
  authenticated by default?
- [ ] **11.2** Does `permitAll` skip Spring Security's filter chain?
- [ ] **11.3** Does `hasRole("ADMIN")` search for `ADMIN` or
  `ROLE_ADMIN` by default?
- [ ] **11.4** If two request authorization matchers match, does the
  most specific win?
- [ ] **11.5** If two `SecurityFilterChain`s match, do both run?
- [ ] **11.6** Can a signed JWT safely contain a password because it
  cannot be modified?
- [ ] **11.7** Is Base64-decoding a JWT authentication?
- [ ] **11.8** Does `STATELESS` automatically disable CSRF?
- [ ] **11.9** Does CORS stop Postman/curl from calling the API?
- [ ] **11.10** Does `@PreAuthorize` work on `new Service()`?
- [ ] **11.11** Does method security activate with the security starter?
- [ ] **11.12** Will `@ControllerAdvice` format a bearer-token failure
  raised before `DispatcherServlet`?
- [ ] **11.13** Is 401 “known user lacks permission”?
- [ ] **11.14** Does `@WithMockUser(roles="ADMIN")` grant `ADMIN`?
- [ ] **11.15** Is `@PostAuthorize` a safe primary guard for a method
  that transfers money?
- [ ] **11.16** Can a service trust an unsigned identity header merely
  because the gateway normally sets it?
- [ ] **11.17** Does `@PostFilter` prevent unauthorized rows from being
  read from the database?
- [ ] **11.18** Does `@PreFilter` reject the entire invocation when one
  element fails its expression?

<details><summary>Solutions 11</summary>

- 11.1 **Authenticated by default** with generated development
  credentials when Boot's default security auto-configuration applies;
  declaring your own chain defines the real policy.
- 11.2 **No.** It permits authorization while keeping the chain.
- 11.3 **`ROLE_ADMIN`**.
- 11.4 **No. First declared match wins.**
- 11.5 **No. First ordered matching chain wins.**
- 11.6 **No. Signed is not encrypted; holders can normally read it.**
- 11.7 **No. Signature and claim validation establish trust.**
- 11.8 **No. Credential transport determines CSRF risk.**
- 11.9 **No. Browsers enforce CORS.**
- 11.10 **No. It needs a Spring-managed method-security proxy.**
- 11.11 **No. Add `@EnableMethodSecurity`.**
- 11.12 **Usually no. Use security exception handlers.**
- 11.13 **No. That is 403; 401 means missing/invalid authentication.**
- 11.14 **No. It grants `ROLE_ADMIN`.**
- 11.15 **No. Denial happens after the method; guard mutations before
  invocation.**
- 11.16 **No. Authenticate the channel/credential and prevent gateway
  bypass or forgery.**
- 11.17 **No. It filters the result after the method returns; constrain
  the query at the data boundary.**
- 11.18 **No. It removes failing elements and invokes the method with
  the remainder. Use pre-authorization for all-or-nothing semantics.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| What is Spring Security? | A filter- and interceptor-based framework that establishes an `Authentication`, stores it in a `SecurityContext`, and applies authorization and exploit protections around web requests and method calls. |
| Request flow? | Servlet chain → `DelegatingFilterProxy` → `FilterChainProxy` → first matching `SecurityFilterChain` → authentication → exception translation → request authorization → `DispatcherServlet` → optional method-security proxy. |
| AuthN vs authZ? | Authentication establishes identity; authorization decides whether that identity may perform this operation on this resource. |
| 401 vs 403? | 401 is missing/invalid authentication; 403 is an authenticated identity lacking permission. |
| Route vs method security? | Route rules protect HTTP paths and methods; method rules protect service invocations and can use arguments/returned objects. Use both where the business boundary deserves defense in depth. |
| `hasRole` vs `hasAuthority`? | Authorities compare exact strings; roles add `ROLE_` by default, so `hasRole("ADMIN")` checks `ROLE_ADMIN`. |
| Method-security trap? | It is AOP: self-invocation and unmanaged objects bypass the proxy. Enable it explicitly with `@EnableMethodSecurity`. |
| `@PreFilter` / `@PostFilter`? | They remove failing input/result elements using `filterObject`; they do not reject the whole batch or make an over-broad query efficient. Prefer typed pre-authorization for atomic commands and query-time tenant filtering for large reads. |
| JWT trust? | Decode is not trust. Verify signature/algorithm/key and validate issuer, expiry, not-before, audience, and required claims. Signed usually does not mean encrypted. |
| CSRF decision? | Keep it when browsers automatically attach authentication credentials, especially cookies. A truly header-bearer API commonly disables it for that chain. |
| CORS? | A browser cross-origin response policy, not authentication or authorization; process preflight before authentication rejection. |
| Password storage? | Use a slow adaptive one-way `PasswordEncoder`, call `matches`, support migration with `DelegatingPasswordEncoder`, rate-limit verification, and never log credentials. |
| Default policy? | Narrow allow rules first, explicit authority/object checks, and a deny-all fallback. Test anonymous, insufficient, sufficient, and cross-tenant cases. |

---

## Primary references

- [Servlet security architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [Authentication architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)
- [Request authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Method security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [JWT resource server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [CORS integration](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [Password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [Spring Security testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html)

---

## Extensions — only after the senior core

- OAuth2 client/login flows, authorization code + PKCE, OIDC ID tokens
  versus OAuth2 access tokens.
- Opaque-token introspection, DPoP/mTLS-bound tokens, token exchange,
  and workload identity.
- A dedicated authorization server and consent/grant modeling.
- SAML2, LDAP/Active Directory, passkeys/WebAuthn and multi-factor
  authentication.
- Custom `AuthorizationManager`, policy agents and fine-grained
  authorization models (RBAC, ABAC, relationship-based access).
- Reactive WebFlux security and reactive-context propagation.
- Security headers, CSP, session fixation/concurrency, remember-me, and
  browser login/logout flows in depth.

---

## How to drill this kit

1. Draw the §0 request path from memory. If the filter chain and method
   proxy appear as one box, repeat §§1 and 4.
2. Say every 💭 answer in at most 90 seconds: mechanism first, then
   the failure mode or trade-off.
3. Build one scratch MVC application twice: session login + CSRF, then
   stateless JWT resource server. Do not build a custom JWT filter.
4. For every protected operation, test the four-cell matrix:
   unauthenticated, wrong authority, correct authority/wrong object,
   and correct authority/correct object.
5. Re-run the trap wall blind after a week. Reading an answer is not a
   rep; predicting it before reveal is.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 mental model | ⬜ | ⬜ | ⬜ |
| §1 filter chain | ⬜ | ⬜ | ⬜ |
| §2 authentication/passwords | ⬜ | ⬜ | ⬜ |
| §3 route authorization | ⬜ | ⬜ | ⬜ |
| §4 method security/SpEL | ⬜ | ⬜ | ⬜ |
| §5 sessions/context | ⬜ | ⬜ | ⬜ |
| §6 JWT resource server | ⬜ | ⬜ | ⬜ |
| §7 CSRF/CORS | ⬜ | ⬜ | ⬜ |
| §8 exception boundary | ⬜ | ⬜ | ⬜ |
| §9 testing | ⬜ | ⬜ | ⬜ |
| §10 production scenarios | ⬜ | ⬜ | ⬜ |
| §11 trap wall | ⬜ | ⬜ | ⬜ |
