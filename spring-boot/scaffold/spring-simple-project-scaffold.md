# Simple Spring Boot Project Scaffold

This is the straightforward Spring Boot structure to use when a feature is
mostly CRUD and does not yet contain enough business complexity to justify a
separate domain model, application layer and persistence adapter.

The feature has four classes:

```text
todo/
├── TodoController.java
├── TodoService.java
├── TodoRepository.java
└── TodoEntity.java
```

This is not an inferior architecture. For a small application, it is often the
clearest one. Move toward the
[domain-oriented scaffold](spring-domain-oriented-project-scaffold.md) when
business rules, workflows or integrations begin to make this service layer
carry too many responsibilities.

The example uses Spring Boot 4.1, Java 21, Spring MVC, Bean Validation, Spring
Data JPA and H2. Each code block represents a separate file.

---

## 1. When this structure fits

Use this structure when:

- the feature mainly creates, reads, updates and deletes records;

- its rules are short and easy to understand;

- HTTP, service and persistence models are nearly the same shape;

- there are few external integrations;

- the application is small or is still proving its value.

Do not split a four-class feature across twelve interfaces merely because a
larger application might need them later. Introduce another boundary when it
solves a problem that actually exists.

Watch for signals that this structure is becoming strained:

- `TodoService` grows into a large collection of unrelated rules;

- several controllers, listeners and scheduled jobs repeat the same logic;

- database entities can no longer represent the business model safely;

- persistence or remote calls make use cases difficult to test;

- one transaction starts coordinating several systems;

- the team cannot explain which class owns an invariant.

Those are reasons to refactor toward application use cases and a richer domain
model. The number of source files alone is not.

---

## 2. Request and dependency flow

```text
HTTP request
    |
    v
TodoController
    |
    v
TodoService              <-- transaction boundary
    |
    v
TodoRepository
    |
    v
TodoEntity <-> database
```

The controller owns HTTP translation and validation. The service owns the use
case and transaction. The repository owns query declarations. The entity owns
persisted state and small state-changing methods.

---

## 3. Project tree

Package by feature even in the simple version. Keeping the four todo classes
together makes the feature easier to find and easier to extract later.

```text
src/
├── main/
│   ├── java/com/example/tasks/
│   │   ├── TasksApplication.java
│   │   └── todo/
│   │       ├── TodoController.java
│   │       ├── TodoEntity.java
│   │       ├── TodoRepository.java
│   │       └── TodoService.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/example/tasks/todo/
        └── TodoServiceTest.java
```

Avoid an application-wide tree such as this once several features exist:

```text
controller/  # controllers for every business area
service/     # services for every business area
repository/  # repositories for every business area
entity/      # entities for every business area
```

That layout groups code by framework role while scattering each feature across
the project.

---

## 4. Build and runtime configuration

For a new project, generate the shell at <https://start.spring.io> and select
Spring Web MVC, Validation, Spring Data JPA and H2 Database. The pinned version
below makes this example reproducible; use the current stable Boot release for
a newly generated application.

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>simple-tasks</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>simple-tasks</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:tasks;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    username: sa
    password:

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: create-drop
```

H2 and `create-drop` are development conveniences. Use the production database
family in integration tests and manage production schema changes with a
migration tool.

---

## 5. Application entry point

### `TasksApplication.java`

```java
package com.example.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TasksApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasksApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
```

The main class sits above the `todo` package so Boot discovers its controller,
service, repository and entity.

---

## 6. Persistence model

### `TodoEntity.java`

```java
package com.example.tasks.todo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "todos")
public class TodoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected TodoEntity() {
    }

    public TodoEntity(UUID id, String title, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Todo id is required");
        this.title = requireTitle(title);
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required");
        this.completed = false;
    }

    public void update(String title, boolean completed) {
        this.title = requireTitle(title);
        this.completed = completed;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private String requireTitle(String candidate) {
        Objects.requireNonNull(candidate, "Todo title is required");
        String normalized = candidate.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Todo title must not be blank");
        }
        return normalized;
    }
}
```

The entity contains only a small invariant: a todo must have a non-blank
title. `@Version` prevents two stale updates from silently overwriting one
another.

For a CRUD feature, using the JPA entity as the service's model is reasonable.
Do not create a second object with identical fields merely to claim that the
domain is independent.

---

## 7. Repository

### `TodoRepository.java`

```java
package com.example.tasks.todo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {

    List<TodoEntity> findAllByOrderByCreatedAtDesc();
}
```

Spring Data supplies the normal CRUD implementation. Add a repository method
only when the feature needs a specific query; do not wrap every generated
method in another interface without a reason.

---

## 8. Service and transaction boundary

### `TodoService.java`

```java
package com.example.tasks.todo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoRepository repository;
    private final Clock clock;

    public TodoService(TodoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<TodoEntity> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public TodoEntity findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public TodoEntity create(String title) {
        TodoEntity todo = new TodoEntity(
                UUID.randomUUID(),
                title,
                Instant.now(clock));
        return repository.save(todo);
    }

    @Transactional
    public TodoEntity update(UUID id, String title, boolean completed) {
        TodoEntity todo = repository.findById(id)
                .orElseThrow(() -> notFound(id));

        todo.update(title, completed);
        return todo;
    }

    @Transactional
    public void delete(UUID id) {
        TodoEntity todo = repository.findById(id)
                .orElseThrow(() -> notFound(id));
        repository.delete(todo);
    }

    private ResponseStatusException notFound(UUID id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Todo not found: " + id);
    }
}
```

The write methods form transaction boundaries. `update()` does not need to
call `save()` after loading the entity: the entity is managed inside the
transaction, and JPA dirty checking writes its changed fields at flush.

Injecting the `Clock` bean keeps time deterministic in tests and makes the
runtime time zone explicit. It is a small dependency with a real payoff; it is
not an architectural layer.

Using `ResponseStatusException` in the service is another deliberate
simplification. Once the same service is called from messaging or batch code,
replace it with an application exception and map that exception at each entry
point.

---

## 9. Controller and HTTP models

The request and response records are nested because they belong only to this
controller. Move them into separate files when they become large or are shared
as a genuine API contract.

### `TodoController.java`

```java
package com.example.tasks.todo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TodoResponse> findAll() {
        return service.findAll().stream()
                .map(TodoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TodoResponse findById(@PathVariable UUID id) {
        return TodoResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(
            @Valid @RequestBody CreateTodoRequest request) {
        TodoEntity created = service.create(request.title());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(TodoResponse.from(created));
    }

    @PutMapping("/{id}")
    public TodoResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTodoRequest request) {
        return TodoResponse.from(
                service.update(id, request.title(), request.completed()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateTodoRequest(
            @NotBlank @Size(max = 200) String title) {
    }

    public record UpdateTodoRequest(
            @NotBlank @Size(max = 200) String title,
            boolean completed) {
    }

    public record TodoResponse(
            UUID id,
            String title,
            boolean completed,
            Instant createdAt) {

        static TodoResponse from(TodoEntity todo) {
            return new TodoResponse(
                    todo.getId(),
                    todo.getTitle(),
                    todo.isCompleted(),
                    todo.getCreatedAt());
        }
    }
}
```

The controller never returns `TodoEntity` directly. Even in a small project,
an HTTP response should not accidentally expose a new persistence field simply
because somebody added it to the entity.

---

## 10. A fast service test

The same constructor receives the production UTC clock from Spring and a fixed
clock from the unit test.

### `TodoServiceTest.java`

```java
package com.example.tasks.todo;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TodoServiceTest {

    @Test
    void createsAnIncompleteTodoAtTheCurrentTime() {
        TodoRepository repository = mock(TodoRepository.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-21T08:00:00Z"),
                ZoneOffset.UTC);
        TodoService service = new TodoService(repository, clock);

        when(repository.save(any(TodoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoEntity created = service.create("  Learn Spring boundaries  ");

        assertThat(created.getTitle()).isEqualTo("Learn Spring boundaries");
        assertThat(created.isCompleted()).isFalse();
        assertThat(created.getCreatedAt())
                .isEqualTo(Instant.parse("2026-09-21T08:00:00Z"));
        verify(repository).save(created);
    }

    @Test
    void updatesTheManagedTodo() {
        TodoRepository repository = mock(TodoRepository.class);
        TodoService service = new TodoService(repository, Clock.systemUTC());
        UUID id = UUID.randomUUID();
        TodoEntity existing = new TodoEntity(id, "Old title", Instant.EPOCH);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        TodoEntity updated = service.update(id, "New title", true);

        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.isCompleted()).isTrue();
    }
}
```

This unit test proves the service's coordination logic. It does not prove JPA
mapping, transaction behavior, validation or HTTP serialization. Add focused
integration tests when those boundaries matter.

---

## 11. Run the example

Start it:

```bash
mvn spring-boot:run
```

Create a todo:

```bash
curl -i http://localhost:8080/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Spring boundaries"}'
```

List todos:

```bash
curl -i http://localhost:8080/todos
```

Update one using the returned ID:

```bash
curl -i -X PUT http://localhost:8080/todos/{id} \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Spring boundaries","completed":true}'
```

Delete it:

```bash
curl -i -X DELETE http://localhost:8080/todos/{id}
```

---

## 12. What not to add yet

This project does not currently need:

- a repository port plus a JPA repository adapter;

- a separate domain object mirroring `TodoEntity` field for field;

- one class per create, read, update and delete operation;

- domain events, factories, specifications or aggregates beyond the entity;

- mapper interfaces whose only work is copying four scalar values;

- an interface in front of `TodoService` with only one implementation.

Add those structures when they isolate real volatility or protect real rules.
Architecture earns its keep by making change safer, not by maximizing the
number of layers.

---

## 13. Refactoring path if the feature grows

Do not rewrite everything at once. Extract boundaries around the pressure:

1. Move a growing business rule from `TodoService` into behavior on an object.

2. Split a long service into named application use cases.

3. Replace `ResponseStatusException` with application exceptions when non-HTTP
   callers appear.

4. Introduce a repository port when persistence details begin influencing the
   use cases or when another adapter is genuinely required.

5. Separate JPA and domain models when their shapes or lifecycles diverge.

6. Move integrations behind ports and add durable coordination patterns only
   when the workflow crosses resource boundaries.

At that point, the
[domain-oriented scaffold](spring-domain-oriented-project-scaffold.md) shows
the next useful shape.

---

## 14. Primary references

- [Spring Boot web applications](https://docs.spring.io/spring-boot/reference/web/index.html)

- [Spring Boot SQL databases and Spring Data JPA](https://docs.spring.io/spring-boot/reference/data/sql.html)

- [Spring Boot validation](https://docs.spring.io/spring-boot/reference/io/validation.html)

- [Spring Framework transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

- [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/)
