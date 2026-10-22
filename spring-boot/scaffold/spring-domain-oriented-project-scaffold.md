# Domain-Oriented Spring Boot Project Scaffold

This is a small but coherent Spring Boot application showing how to organize a
feature around its business model instead of organizing the whole application
around framework stereotypes.

It is a starting point, not a rule that every project must follow. A simple
admin CRUD application may be clearer with a controller, service and Spring
Data repository. This structure becomes valuable when a feature has business
rules, several entry points, or infrastructure that may change independently.

The central idea is:

> Spring assembles and runs the application. The domain defines what the
> business allows.

The example uses Spring Boot 4.1, Java 21, Spring MVC, validation and JPA. Each
code block represents a separate file.

---

## 1. Decide how much architecture the feature needs

Do not begin by asking whether every class should have `@Service` or
`@Component`. Begin by asking where the real decisions are.

| Feature shape | Sensible starting point |
|---|---|
| Simple CRUD or internal administration | Controller → service → Spring Data repository |
| Mostly queries with little behavior | Query-oriented service with DTO projections |
| Several business rules and state transitions | Domain model plus application use cases |
| Several databases, brokers or external services | Ports and adapters around the use cases |
| Several distinct business areas | Package by feature; consider bounded contexts when the language and ownership differ |

Use the smallest structure that keeps business rules visible. Moving a trivial
field assignment through six interfaces does not make it DDD. Conversely, a
300-line `@Service` full of state-transition rules is a sign that the domain
model is not carrying enough responsibility.

For example, prefer this:

```java
order.cancel();
```

over application code that reaches into an object and performs this:

```java
order.setStatus(OrderStatus.CANCELLED);
```

The first form asks the aggregate to protect its invariant. The second lets
every caller invent its own transition rules.

---

## 2. Dependency direction

The feature is split by responsibility:

```text
HTTP request
    |
    v
web adapter
    |
    v
application use case  <-- transaction boundary
    |
    v
domain model + repository port
    ^
    |
persistence adapter
    |
    v
Spring Data JPA -> database
```

Dependencies point inward:

```text
web -------------> application -------------> domain
configuration ---> application -------------> domain
infrastructure ------------------------------> domain
```

The domain contains no Spring or JPA imports. Infrastructure implements the
repository interface owned by the inner layer. Spring configuration connects
the implementation to the use cases.

This is the useful part of hexagonal architecture. The directory names alone
do not provide the boundary; the direction of Java dependencies does.

---

## 3. Project tree

Package by feature first. Inside the `order` feature, separate the business
model from its entry points and adapters.

```text
src/
├── main/
│   ├── java/com/example/shop/
│   │   ├── ShopApplication.java
│   │   └── order/
│   │       ├── domain/
│   │       │   ├── Order.java
│   │       │   ├── OrderId.java
│   │       │   ├── OrderItem.java
│   │       │   ├── OrderItemId.java
│   │       │   ├── OrderRepository.java
│   │       │   ├── OrderRuleViolationException.java
│   │       │   └── OrderStatus.java
│   │       ├── application/
│   │       │   ├── CancelOrder.java
│   │       │   ├── CreateOrder.java
│   │       │   ├── CreateOrderCommand.java
│   │       │   ├── CreateOrderItemCommand.java
│   │       │   ├── GetOrder.java
│   │       │   ├── InvalidOrderCommandException.java
│   │       │   ├── OrderNotFoundException.java
│   │       │   └── OrderResult.java
│   │       ├── config/
│   │       │   └── OrderConfiguration.java
│   │       ├── infrastructure/persistence/
│   │       │   ├── JpaOrderRepository.java
│   │       │   ├── OrderItemJpaEntity.java
│   │       │   ├── OrderJpaEntity.java
│   │       │   └── SpringDataOrderRepository.java
│   │       └── web/
│   │           ├── ApiExceptionHandler.java
│   │           ├── CreateOrderItemRequest.java
│   │           ├── CreateOrderRequest.java
│   │           ├── OrderController.java
│   │           ├── OrderItemResponse.java
│   │           └── OrderResponse.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/example/shop/order/domain/
        └── OrderTest.java
```

In a real product, repeat this shape for another substantial feature such as
`payment` or `inventory`. Avoid returning to one application-wide
`controller/`, `service/`, `repository/` and `entity/` drawer.

---

## 4. Build and runtime configuration

The version is pinned so the example is reproducible. For a new project,
generate the shell at <https://start.spring.io> and use its current stable
Spring Boot version.

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
    <artifactId>domain-oriented-shop</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>domain-oriented-shop</name>

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
    url: jdbc:h2:mem:shop;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    username: sa
    password:

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
```

H2 and `ddl-auto: create-drop` make this note easy to run. A production
application should use its production database family in integration tests and
manage schema changes with Flyway or Liquibase.

---

## 5. Application entry point

### `ShopApplication.java`

```java
package com.example.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
```

Keeping the main class in `com.example.shop` lets component and entity scanning
find every feature package below it.

---

## 6. Domain: business state and rules

The domain objects are ordinary Java. They can be constructed and tested
without a Spring context or a database.

### `OrderId.java`

```java
package com.example.shop.order.domain;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "Order id is required");
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
}
```

### `OrderItemId.java`

```java
package com.example.shop.order.domain;

import java.util.Objects;
import java.util.UUID;

public record OrderItemId(UUID value) {

    public OrderItemId {
        Objects.requireNonNull(value, "Order item id is required");
    }

    public static OrderItemId newId() {
        return new OrderItemId(UUID.randomUUID());
    }

    public static OrderItemId of(UUID value) {
        return new OrderItemId(value);
    }
}
```

### `OrderStatus.java`

```java
package com.example.shop.order.domain;

public enum OrderStatus {
    DRAFT,
    CONFIRMED,
    SHIPPED,
    CANCELLED
}
```

### `OrderRuleViolationException.java`

```java
package com.example.shop.order.domain;

public class OrderRuleViolationException extends RuntimeException {

    public OrderRuleViolationException(String message) {
        super(message);
    }
}
```

### `OrderItem.java`

```java
package com.example.shop.order.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderItem(
        OrderItemId id,
        String productId,
        int quantity,
        BigDecimal unitPrice) {

    public OrderItem {
        Objects.requireNonNull(id, "Order item id is required");
        Objects.requireNonNull(productId, "Product id is required");
        Objects.requireNonNull(unitPrice, "Unit price is required");

        if (productId.isBlank()) {
            throw new OrderRuleViolationException("Product id must not be blank");
        }
        if (quantity <= 0) {
            throw new OrderRuleViolationException("Quantity must be positive");
        }
        if (unitPrice.signum() <= 0) {
            throw new OrderRuleViolationException("Unit price must be positive");
        }
    }

    public static OrderItem create(
            String productId,
            int quantity,
            BigDecimal unitPrice) {
        return new OrderItem(
                OrderItemId.newId(),
                productId,
                quantity,
                unitPrice);
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

### `Order.java`

```java
package com.example.shop.order.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Order {

    private final OrderId id;
    private final Currency currency;
    private final List<OrderItem> items;
    private OrderStatus status;

    private Order(
            OrderId id,
            Currency currency,
            OrderStatus status,
            List<OrderItem> items) {
        this.id = Objects.requireNonNull(id, "Order id is required");
        this.currency = Objects.requireNonNull(currency, "Currency is required");
        this.status = Objects.requireNonNull(status, "Order status is required");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items are required"));

        Set<OrderItemId> uniqueIds = new HashSet<>();
        if (!this.items.stream().allMatch(item -> uniqueIds.add(item.id()))) {
            throw new IllegalArgumentException("Order item ids must be unique");
        }
        if (status != OrderStatus.DRAFT && this.items.isEmpty()) {
            throw new IllegalArgumentException("A non-draft order must contain an item");
        }
    }

    public static Order draft(OrderId id, Currency currency) {
        return new Order(id, currency, OrderStatus.DRAFT, List.of());
    }

    public static Order restore(
            OrderId id,
            Currency currency,
            OrderStatus status,
            List<OrderItem> items) {
        return new Order(id, currency, status, items);
    }

    public void addItem(String productId, int quantity, BigDecimal unitPrice) {
        requireStatus(OrderStatus.DRAFT, "Only a draft order can be changed");
        items.add(OrderItem.create(productId, quantity, unitPrice));
    }

    public void confirm() {
        requireStatus(OrderStatus.DRAFT, "Only a draft order can be confirmed");
        if (items.isEmpty()) {
            throw new OrderRuleViolationException("An empty order cannot be confirmed");
        }
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return; // repeated cancellation is deliberately idempotent
        }
        requireStatus(OrderStatus.CONFIRMED, "Only a confirmed order can be cancelled");
        status = OrderStatus.CANCELLED;
    }

    public void ship() {
        requireStatus(OrderStatus.CONFIRMED, "Only a confirmed order can be shipped");
        status = OrderStatus.SHIPPED;
    }

    public BigDecimal total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderId id() {
        return id;
    }

    public Currency currency() {
        return currency;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    private void requireStatus(OrderStatus required, String message) {
        if (status != required) {
            throw new OrderRuleViolationException(message + "; current status is " + status);
        }
    }
}
```

`restore(...)` is deliberately different from `draft(...)`. Reconstructing a
persisted order must restore known state; it must not replay `confirm()`,
`cancel()` or `ship()`. Replaying those methods can fail when rules change and
can trigger behavior that belongs only to a new command.

### `OrderRepository.java`

```java
package com.example.shop.order.domain;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

This interface is the persistence port required by the use cases. It says what
the feature needs without choosing JPA, JDBC, an external service or an
in-memory fake.

---

## 7. Application: execute one use case

The application layer coordinates a command, a domain aggregate and a port.
It does not contain HTTP or JPA mapping.

### `CreateOrderItemCommand.java`

```java
package com.example.shop.order.application;

import java.math.BigDecimal;

public record CreateOrderItemCommand(
        String productId,
        int quantity,
        BigDecimal unitPrice) {
}
```

### `CreateOrderCommand.java`

```java
package com.example.shop.order.application;

import java.util.List;
import java.util.Objects;

public record CreateOrderCommand(
        String currency,
        List<CreateOrderItemCommand> items) {

    public CreateOrderCommand {
        Objects.requireNonNull(currency, "Currency is required");
        items = List.copyOf(Objects.requireNonNull(items, "Items are required"));
    }
}
```

### `OrderResult.java`

```java
package com.example.shop.order.application;

import com.example.shop.order.domain.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID orderId,
        String status,
        String currency,
        BigDecimal total,
        List<Item> items) {

    public record Item(
            UUID itemId,
            String productId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }

    public static OrderResult from(Order order) {
        List<Item> itemResults = order.items().stream()
                .map(item -> new Item(
                        item.id().value(),
                        item.productId(),
                        item.quantity(),
                        item.unitPrice(),
                        item.subtotal()))
                .toList();

        return new OrderResult(
                order.id().value(),
                order.status().name(),
                order.currency().getCurrencyCode(),
                order.total(),
                itemResults);
    }
}
```

### `InvalidOrderCommandException.java`

```java
package com.example.shop.order.application;

public class InvalidOrderCommandException extends RuntimeException {

    public InvalidOrderCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### `OrderNotFoundException.java`

```java
package com.example.shop.order.application;

import com.example.shop.order.domain.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id.value());
    }
}
```

### `CreateOrder.java`

```java
package com.example.shop.order.application;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

public class CreateOrder {

    private final OrderRepository orders;

    public CreateOrder(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public OrderResult execute(CreateOrderCommand command) {
        Currency currency;
        try {
            currency = Currency.getInstance(command.currency());
        } catch (IllegalArgumentException failure) {
            throw new InvalidOrderCommandException(
                    "Unsupported currency: " + command.currency(),
                    failure);
        }

        Order order = Order.draft(OrderId.newId(), currency);
        command.items().forEach(item -> order.addItem(
                item.productId(),
                item.quantity(),
                item.unitPrice()));
        order.confirm();

        orders.save(order);
        return OrderResult.from(order);
    }
}
```

### `CancelOrder.java`

```java
package com.example.shop.order.application;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

public class CancelOrder {

    private final OrderRepository orders;

    public CancelOrder(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public void execute(OrderId id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.cancel();
        orders.save(order);
    }
}
```

### `GetOrder.java`

```java
package com.example.shop.order.application;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

public class GetOrder {

    private final OrderRepository orders;

    public GetOrder(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public OrderResult execute(OrderId id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResult.from(order);
    }
}
```

The transaction surrounds the whole use case—not just `save()`. That keeps the
load, decision and update inside one unit of work. `@Transactional` is the one
deliberate Spring dependency in this application layer.

That is usually a sensible compromise. If application code must also be
framework-independent, define use-case interfaces and add transactional
decorators in configuration. Do not add that indirection merely to make a
small codebase look architecturally pure.

---

## 8. Configuration: let Spring assemble the use cases

The use cases have no stereotype annotations. Configuration makes them Spring
beans and supplies the repository port.

### `OrderConfiguration.java`

```java
package com.example.shop.order.config;

import com.example.shop.order.application.CancelOrder;
import com.example.shop.order.application.CreateOrder;
import com.example.shop.order.application.GetOrder;
import com.example.shop.order.domain.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrderConfiguration {

    @Bean
    CreateOrder createOrder(OrderRepository orders) {
        return new CreateOrder(orders);
    }

    @Bean
    CancelOrder cancelOrder(OrderRepository orders) {
        return new CancelOrder(orders);
    }

    @Bean
    GetOrder getOrder(OrderRepository orders) {
        return new GetOrder(orders);
    }
}
```

Annotating the use cases with `@Service` would also be reasonable. Explicit
`@Bean` methods make the assembly boundary visible and leave component scanning
for adapters such as controllers and repository implementations.

---

## 9. Persistence adapter: translate between domain and JPA

This example deliberately keeps JPA entities out of the domain. That costs
mapping code, but it prevents persistence requirements from controlling the
business model.

Do not create a separate persistence model automatically. If the feature is
simple and its domain shape matches the tables, putting JPA annotations on the
domain entity can be the more honest choice.

### `OrderJpaEntity.java`

```java
package com.example.shop.order.infrastructure.persistence;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderItem;
import com.example.shop.order.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
public class OrderJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @jakarta.persistence.Transient
    private boolean newEntity = true;

    protected OrderJpaEntity() {
    }

    public OrderJpaEntity(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        newEntity = false;
    }

    public void synchronizeWith(Order source) {
        status = source.status();
        currency = source.currency().getCurrencyCode();

        Set<UUID> sourceIds = new HashSet<>();
        source.items().forEach(item -> sourceIds.add(item.id().value()));
        items.removeIf(entity -> !sourceIds.contains(entity.getId()));

        Map<UUID, OrderItemJpaEntity> existing = new HashMap<>();
        items.forEach(entity -> existing.put(entity.getId(), entity));

        for (OrderItem item : source.items()) {
            OrderItemJpaEntity entity = existing.get(item.id().value());
            if (entity == null) {
                items.add(OrderItemJpaEntity.from(item, this));
            } else {
                entity.synchronizeWith(item);
            }
        }
    }

    public Order toDomain() {
        List<OrderItem> domainItems = items.stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList();

        return Order.restore(
                OrderId.of(id),
                Currency.getInstance(currency),
                status,
                domainItems);
    }
}
```

Implementing `Persistable` tells Spring Data that a domain-assigned UUID still
belongs to a new entity. Without that signal, a non-null assigned ID is often
treated as an existing entity and routed through `merge()`.

### `OrderItemJpaEntity.java`

```java
package com.example.shop.order.infrastructure.persistence;

import com.example.shop.order.domain.OrderItem;
import com.example.shop.order.domain.OrderItemId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items")
public class OrderItemJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    protected OrderItemJpaEntity() {
    }

    private OrderItemJpaEntity(
            UUID id,
            String productId,
            int quantity,
            BigDecimal unitPrice,
            OrderJpaEntity order) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.order = order;
    }

    static OrderItemJpaEntity from(OrderItem source, OrderJpaEntity order) {
        return new OrderItemJpaEntity(
                source.id().value(),
                source.productId(),
                source.quantity(),
                source.unitPrice(),
                order);
    }

    UUID getId() {
        return id;
    }

    void synchronizeWith(OrderItem source) {
        productId = source.productId();
        quantity = source.quantity();
        unitPrice = source.unitPrice();
    }

    OrderItem toDomain() {
        return new OrderItem(
                OrderItemId.of(id),
                productId,
                quantity,
                unitPrice);
    }
}
```

### `SpringDataOrderRepository.java`

```java
package com.example.shop.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderRepository
        extends JpaRepository<OrderJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    @Query("select o from OrderJpaEntity o where o.id = :id")
    Optional<OrderJpaEntity> findWithItemsById(@Param("id") UUID id);
}
```

The adapter needs the complete aggregate, so the query states the required
fetch plan. It does not rely on Open Session in View or on accidental lazy
loading from the controller.

### `JpaOrderRepository.java`

```java
package com.example.shop.order.infrastructure.persistence;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public JpaOrderRepository(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findWithItemsById(id.value())
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = repository
                .findWithItemsById(order.id().value())
                .orElseGet(() -> new OrderJpaEntity(order.id().value()));

        entity.synchronizeWith(order);
        repository.save(entity);
    }
}
```

The stable domain item IDs let the adapter update the existing child rows
instead of deleting and recreating every item whenever the order status
changes. `@Version` on the order entity prevents two writers from silently
overwriting one another.

---

## 10. Web adapter: translate HTTP into a command

Transport validation belongs on request models. Business rules still belong
in the domain because the same use case may later be called by a message
listener, scheduler or another application service.

### `CreateOrderItemRequest.java`

```java
package com.example.shop.order.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotBlank String productId,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2)
        BigDecimal unitPrice) {
}
```

### `CreateOrderRequest.java`

```java
package com.example.shop.order.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotEmpty List<@Valid CreateOrderItemRequest> items) {
}
```

### `OrderItemResponse.java`

```java
package com.example.shop.order.web;

import com.example.shop.order.application.OrderResult;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID itemId,
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {

    static OrderItemResponse from(OrderResult.Item item) {
        return new OrderItemResponse(
                item.itemId(),
                item.productId(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal());
    }
}
```

### `OrderResponse.java`

```java
package com.example.shop.order.web;

import com.example.shop.order.application.OrderResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String status,
        String currency,
        BigDecimal total,
        List<OrderItemResponse> items) {

    static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.status(),
                result.currency(),
                result.total(),
                result.items().stream()
                        .map(OrderItemResponse::from)
                        .toList());
    }
}
```

### `OrderController.java`

```java
package com.example.shop.order.web;

import com.example.shop.order.application.CancelOrder;
import com.example.shop.order.application.CreateOrder;
import com.example.shop.order.application.CreateOrderCommand;
import com.example.shop.order.application.CreateOrderItemCommand;
import com.example.shop.order.application.GetOrder;
import com.example.shop.order.application.OrderResult;
import com.example.shop.order.domain.OrderId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrder createOrder;
    private final GetOrder getOrder;
    private final CancelOrder cancelOrder;

    public OrderController(
            CreateOrder createOrder,
            GetOrder getOrder,
            CancelOrder cancelOrder) {
        this.createOrder = createOrder;
        this.getOrder = getOrder;
        this.cancelOrder = cancelOrder;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.currency(),
                request.items().stream()
                        .map(item -> new CreateOrderItemCommand(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice()))
                        .toList());

        OrderResult result = createOrder.execute(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.orderId())
                .toUri();

        return ResponseEntity.created(location)
                .body(OrderResponse.from(result));
    }

    @GetMapping("/{id}")
    public OrderResponse find(@PathVariable UUID id) {
        return OrderResponse.from(getOrder.execute(OrderId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelOrder.execute(OrderId.of(id));
        return ResponseEntity.noContent().build();
    }
}
```

### `ApiExceptionHandler.java`

```java
package com.example.shop.order.web;

import com.example.shop.order.application.InvalidOrderCommandException;
import com.example.shop.order.application.OrderNotFoundException;
import com.example.shop.order.domain.OrderRuleViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException failure) {
        return problem(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", failure.getMessage());
    }

    @ExceptionHandler(InvalidOrderCommandException.class)
    ProblemDetail invalidCommand(InvalidOrderCommandException failure) {
        return problem(HttpStatus.BAD_REQUEST, "ORDER_COMMAND_INVALID", failure.getMessage());
    }

    @ExceptionHandler(OrderRuleViolationException.class)
    ProblemDetail ruleViolation(OrderRuleViolationException failure) {
        return problem(HttpStatus.CONFLICT, "ORDER_RULE_VIOLATION", failure.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrentChange(OptimisticLockingFailureException failure) {
        return problem(
                HttpStatus.CONFLICT,
                "ORDER_CHANGED_CONCURRENTLY",
                "The order changed while this request was being processed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidRequest(MethodArgumentNotValidException failure) {
        Map<String, String> errors = new LinkedHashMap<>();
        failure.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "REQUEST_INVALID",
                "The request contains invalid fields");
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        return problem;
    }
}
```

The error response exposes stable machine codes and safe messages. It does not
return stack traces, entity internals or database exception text.

---

## 11. Prove the domain without Spring

### `OrderTest.java`

```java
package com.example.shop.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void confirmsAnOrderWithAtLeastOneItem() {
        Order order = Order.draft(
                OrderId.newId(),
                Currency.getInstance("INR"));

        order.addItem("BOOK-1", 2, new BigDecimal("499.00"));
        order.confirm();

        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.total()).isEqualByComparingTo("998.00");
    }

    @Test
    void cannotCancelAnOrderAfterItHasShipped() {
        Order order = Order.draft(
                OrderId.newId(),
                Currency.getInstance("INR"));
        order.addItem("BOOK-1", 1, new BigDecimal("499.00"));
        order.confirm();
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderRuleViolationException.class)
                .hasMessageContaining("Only a confirmed order can be cancelled");
    }
}
```

These tests run in milliseconds because the rules do not need Spring, HTTP or
a database. Separate integration tests should prove the JPA mapping,
optimistic locking, transaction boundary and HTTP contract against their real
boundaries.

---

## 12. Run the example

Start it:

```bash
mvn spring-boot:run
```

Create an order:

```bash
curl -i http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "currency": "INR",
    "items": [
      {"productId": "BOOK-1", "quantity": 2, "unitPrice": 499.00}
    ]
  }'
```

Use the returned `orderId`:

```bash
curl -i http://localhost:8080/orders/{orderId}

curl -i -X POST http://localhost:8080/orders/{orderId}/cancel
```

---

## 13. What belongs where

When adding code, ask which statement best describes it.

| Concern | Home |
|---|---|
| “A shipped order cannot be cancelled” | Domain |
| “Load an order, cancel it and persist the result” | Application use case |
| “`POST /orders/{id}/cancel` maps a UUID path variable” | Web adapter |
| “Orders are stored with JPA using these tables and fetch plans” | Persistence adapter |
| “Create these use cases using this repository implementation” | Spring configuration |
| “The operation must commit atomically” | Application transaction boundary |

A useful test is to imagine replacing HTTP with Kafka or replacing JPA with
JDBC. Those changes should affect adapters and configuration, not rewrite
`Order.cancel()`.

---

## 14. Deliberate choices and trade-offs

This scaffold makes several choices that should remain conscious:

- **Package by feature.** `order` stays together instead of being scattered
  across application-wide technical folders.

- **A rich aggregate.** `Order` owns its state transitions; use cases do not
  set its status directly.

- **A repository port.** The application depends on the persistence operation
  it needs, not on `JpaRepository`.

- **Separate JPA entities.** This protects the domain from persistence
  constraints but adds mapping code. Do it only when the separation pays for
  itself.

- **Transactions in application services.** This keeps the complete use case
  atomic. It introduces one Spring annotation into the application layer as a
  pragmatic trade-off.

- **No Open Session in View.** The repository loads the aggregate it promises;
  the controller does not trigger surprise SQL.

- **Optimistic concurrency.** The JPA aggregate root has `@Version`, so stale
  writers fail instead of silently losing an update.

- **Separate transport models.** Validation and JSON evolution do not force
  annotations or fields into the domain.

Do not copy the shape blindly. Keep it when it makes the rules and dependency
direction clearer; collapse layers when they merely forward the same fields.

---

## 15. How this can evolve

Add a concept when a real requirement appears:

- introduce a domain event when another capability must react to an accepted
  domain fact;

- persist that event through an outbox when delivery must survive a crash;

- add an external-service port when pricing, inventory or payment moves out of
  process;

- split reads into projections when loading the full aggregate is wasteful;

- split the feature into bounded contexts only when language, rules, data
  ownership or team ownership genuinely diverge.

DDD is not the starting folder tree. It is the continued effort to keep the
important business language and invariants explicit as the software grows.

---

## 16. Primary references

- [Spring Boot web applications](https://docs.spring.io/spring-boot/reference/web/index.html)

- [Spring Boot SQL databases and Spring Data JPA](https://docs.spring.io/spring-boot/reference/data/sql.html)

- [Spring Boot validation](https://docs.spring.io/spring-boot/reference/io/validation.html)

- [Spring Framework transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

- [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/)
