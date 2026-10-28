# Design Patterns: Interview Question Set

Target: 6 to 10 years experience, integration architecture role.

## Index

1. Creational Patterns (Q1 to Q5)
2. Structural Patterns (Q6 to Q11)
3. Behavioral Patterns (Q12 to Q19)
4. Enterprise Integration Patterns (Q20 to Q27)
5. Distributed System and Microservice Patterns (Q28 to Q34)
6. Patterns in Spring and Java Practice (Q35 to Q38)
7. Frequently Asked SOLID, Clean Architecture & Integration Scenarios (Q39 to Q50)

---

## 1. Creational Patterns

### Q1. What is the Singleton pattern, and how do you implement it safely in Java?

**Answer:** A class has one instance with a global access point. Safe options:
- Enum singleton (serialization and reflection safe).
- Initialization-on-demand holder (lazy and thread-safe).

In Spring, prefer container-managed singleton beans over hand-written singletons, because they are easier to test and replace.

```java
public enum IdGenerator {
    INSTANCE;
    public String next() { return UUID.randomUUID().toString(); }
}
```

### Q2. What is the Factory Method pattern, and when would you use it?

**Answer:** A method creates objects, and subclasses or configuration decide which concrete type to create. Use it when the caller should not know the concrete class. Example: a `ConnectorFactory` that returns an `ErpConnector`, `CrmConnector`, or `FileConnector` based on tenant configuration.

### Q3. What is the Abstract Factory pattern?

**Answer:** A factory that creates families of related objects without naming their concrete classes. Example: a factory for a database-specific family (`MySqlDialect`, `MySqlQueryBuilder`, `MySqlTypeMapper`) and an Oracle family. Use it when several related objects must stay consistent.

### Q4. What is the Builder pattern, and when is it useful?

**Answer:** It builds complex objects step by step, with clear optional parameters. Useful for objects with many optional fields, such as an HTTP request config or a message. Lombok's `@Builder` and records with static builders are common Java forms. Validate the object in `build()`.

### Q5. What is the Prototype pattern?

**Answer:** Create new objects by copying an existing instance (`clone()` or copy constructor). Use it when object creation is expensive and copies are mostly similar, such as default message templates. Prefer copy constructors over `Cloneable`, because `clone()` is hard to get right with deep copies.

---

## 2. Structural Patterns

### Q6. What is the Adapter pattern, and where does it appear in integration?

**Answer:** An adapter converts one interface into another that the client expects. In integration, each external system gets an adapter that maps its API or protocol to the platform's internal interface. This is central to keeping Csmart decoupled from external systems. Example: `LegacySoapOrderAdapter implements OrderGateway`.

### Q7. What is the Decorator pattern?

**Answer:** Adds behavior to an object at runtime by wrapping it, with the same interface. Example: a `RetryingMessageSender` wraps a `MessageSender` and adds retry, and a `TracingMessageSender` wraps it again for tracing. Java I/O streams use this pattern (`BufferedInputStream` wraps `FileInputStream`).

### Q8. What is the Facade pattern?

**Answer:** A simple interface over a complex subsystem. Example: an `OrderSyncFacade` hides five services (validation, mapping, persistence, publishing, audit) behind one method. Use it to reduce coupling for callers. Do not let facades become god classes.

### Q9. What is the Proxy pattern, and how does Spring use it?

**Answer:** A proxy controls access to another object. Spring uses proxies for `@Transactional`, `@Async`, `@Cacheable`, `@Retryable`, and security method rules. Remember that calls inside the same class bypass the proxy (self-invocation).

### Q10. What is the Composite pattern?

**Answer:** Treats individual objects and groups of objects the same way through a common interface. Example: a `ValidationRule` composite that contains many rules, where the composite itself is a rule. Useful for pipelines of validators or mappers.

### Q11. What is the Bridge pattern, and when would you use it?

**Answer:** Separates an abstraction from its implementation so both can vary independently. Example: a `NotificationSender` abstraction (email, SMS, alert) and a transport implementation (SMTP, HTTP, RabbitMQ). Use it when two dimensions of variation would otherwise cause a class explosion.

---

## 3. Behavioral Patterns

### Q12. What is the Strategy pattern, and how would you apply it to an iPaaS?

**Answer:** It encapsulates interchangeable algorithms behind one interface. Example: a `RetryStrategy` with exponential backoff, fixed delay, or no retry, selected per integration. Another example: a `RoutingStrategy` for content-based routing per tenant. Inject strategies by name or through configuration.

### Q13. What is the Observer pattern, and how does Spring implement it?

**Answer:** Subjects notify subscribers when state changes. Spring implements it through `ApplicationEventPublisher` and `@EventListener`. Use `@TransactionalEventListener` to run listeners only after the transaction commits, which avoids publishing events for rolled-back changes.

### Q14. What is the Template Method pattern?

**Answer:** A base class defines the algorithm steps, and subclasses override specific steps. Spring uses it in `JdbcTemplate`, `RestTemplate`, and `AbstractRoutingDataSource`. In integration, a `BaseConnector` can define `connect`, `fetch`, `map`, `publish`, and subclasses implement the details.

### Q15. What is the Command pattern?

**Answer:** Encapsulates a request as an object, so it can be queued, logged, retried, or undone. Examples: a `CreateOrderCommand` sent through RabbitMQ, or a Spring Batch step. The command object is also what makes replay possible.

### Q16. What is the Chain of Responsibility pattern?

**Answer:** A series of handlers, where each handler either processes the request or passes it on. Examples: servlet filters, Spring Security filter chain, Spring MVC interceptors, and validation pipelines. Use it for pre-processing, enrichment, or authorization steps that must run in order.

### Q17. What is the State pattern?

**Answer:** An object changes behavior based on its internal state, with state-specific classes. Example: an integration message with states `RECEIVED`, `VALIDATED`, `SENT`, `ACKNOWLEDGED`, `FAILED`. Use it when transitions have rules, and reject invalid transitions explicitly. Store the state in the database with optimistic locking.

### Q18. What is the Iterator pattern, and why does it matter for large data?

**Answer:** It provides sequential access to elements without exposing the structure. For large data sets (files, query results), use streaming iterators instead of loading everything into memory. Example: a `CsvRecordIterator` that reads one line at a time, or JDBC result set streaming.

### Q19. What is the Mediator pattern?

**Answer:** Components communicate through a central mediator instead of directly with each other, reducing coupling. In integration, an orchestrator route or workflow engine acts as a mediator between systems. Watch out for it becoming a bottleneck or a place where all business logic accumulates.

---

## 4. Enterprise Integration Patterns

### Q20. What are the main Enterprise Integration Patterns for routing?

**Answer:**
- **Content-based router:** route by message content.
- **Message filter:** drop messages that do not match.
- **Recipient list:** send to a dynamic list of destinations.
- **Splitter and aggregator:** break a message into parts and combine responses.
- **Scatter-gather:** send to many recipients and combine replies.
- **Routing slip:** message carries its list of steps.

### Q21. What are the EIPs for message transformation?

**Answer:**
- **Message translator:** convert format (XML to JSON).
- **Content enricher:** add data by calling another system.
- **Content filter:** remove fields not needed downstream.
- **Canonical data model:** a shared model that reduces the number of mappings.
- **Normalizer:** convert multiple formats to one internal format.

### Q22. What is the Dead Letter Channel pattern?

**Answer:** Messages that cannot be processed are sent to a dead letter channel for later inspection and replay, instead of being lost or retried forever. See the RabbitMQ and Camel question sets for implementation details.

### Q23. What is the Idempotent Receiver pattern?

**Answer:** A consumer processes each logical message once, even if it arrives more than once. Implement with a stored message ID or business key checked in the same transaction as the business change. Camel offers `idempotentConsumer`.

### Q24. What is the Claim Check pattern?

**Answer:** Store a large payload in external storage (for example, S3 or a file store) and send only a reference (claim check) through the message broker. The consumer fetches the payload when needed. Use it to keep broker messages small and avoid broker memory pressure.

### Q25. What is the Request-Reply pattern?

**Answer:** The sender includes a reply channel and correlation ID. The receiver sends its response to the reply channel. Use it when a caller needs a result from an asynchronous system. Always add a timeout and handle late replies.

### Q26. What is the Publish-Subscribe pattern, and how does it differ from point-to-point?

**Answer:** Publish-subscribe sends one message to many subscribers (fanout or topic exchange in RabbitMQ). Point-to-point sends a message to one consumer among many competing consumers. Use pub/sub for events and point-to-point for commands.

### Q27. What is the Process Manager (orchestration) pattern?

**Answer:** A central component tracks the state of a multi-step business process, decides the next step, and handles failures. It is the orchestration form of the saga pattern. Use it for long-running workflows, such as "create customer in CRM, then provision in ERP, then notify billing".

---

## 5. Distributed System and Microservice Patterns

### Q28. What is the API Gateway pattern?

**Answer:** A single entry point for clients that handles routing, authentication, rate limiting, TLS termination, and request aggregation. It keeps clients from knowing internal service addresses. Avoid putting business logic in the gateway.

### Q29. What is the Backend for Frontend (BFF) pattern?

**Answer:** A separate backend per client type (web, mobile, partner), each shaped for its needs. It avoids one generic API that fits no one well. Use it when client needs diverge significantly.

### Q30. What is the Saga pattern?

**Answer:** A sequence of local transactions, each with a compensating action for rollback. Choreography uses events between services. Orchestration uses a central coordinator (process manager). Sagas replace distributed transactions (2PC), which do not scale well across services.

### Q31. What is the Transactional Outbox pattern?

**Answer:** Write the business data and an outbox record in one local transaction. A relay publishes outbox records to the broker. This solves the dual-write problem between the database and the message broker.

### Q32. What is CQRS, and when is it worth the complexity?

**Answer:** Command Query Responsibility Segregation separates the write model (commands) from the read model (queries). Use it when read and write loads differ greatly, when reads need denormalized views, or when different stores suit reads and writes. Avoid it for simple CRUD, because it adds eventual consistency and synchronization work.

### Q33. What is Event Sourcing?

**Answer:** Store every state change as an immutable event, and rebuild state by replaying events. Benefits: full audit trail and replay. Costs: schema evolution of events, snapshotting for performance, and complexity for queries. Pair it with CQRS when needed.

### Q34. What is the Strangler Fig pattern?

**Answer:** Migrate a legacy system gradually by routing some functionality to new services while the old system handles the rest, until the old system can be retired. In integration, put a routing layer or gateway in front of the legacy system and move routes one at a time.

---

## 6. Patterns in Spring and Java Practice

### Q35. Which design patterns are used inside Spring Framework?

**Answer:**
- Singleton: default bean scope.
- Factory: `BeanFactory`, `FactoryBean`.
- Proxy: AOP and transactions.
- Template Method: `JdbcTemplate`, `RestTemplate`.
- Observer: application events.
- Adapter: `HandlerAdapter` in Spring MVC.
- Chain of Responsibility: `HandlerInterceptor`, Spring Security filters.
- Strategy: `PlatformTransactionManager` implementations, `Resource` loaders.

### Q36. Where would you apply the Decorator and Proxy patterns in an integration service?

**Answer:** Wrap outbound HTTP clients with decorators for retry, timeout, circuit breaker, and tracing, in that order. Use Resilience4j decorators or Spring's `@Retryable` on a proxied method. Keep the business client unaware of these cross-cutting concerns.

### Q37. What are the common anti-patterns to avoid?

**Answer:**
- God class or God service that holds all business logic.
- Anemic domain model, where entities are only data bags and services do everything.
- Singleton with mutable global state.
- Service locator, instead of constructor injection.
- Overuse of inheritance for reuse.
- Shared database between microservices.
- Chatty APIs (many small calls where one would do).

### Q38. How do you decide whether a pattern is worth using?

**Answer:** Ask: what problem does it solve, what is the current pain (duplication, coupling, change cost, failure), and what complexity does it add? Start with the simplest design that works. Introduce a pattern when the problem appears in practice, and document the decision in an ADR.

---

## 7. Frequently Asked SOLID, Clean Architecture & Integration Scenarios

### Q39. Explain the SOLID principles with real integration platform examples.

**Answer:**
- **S (Single Responsibility):** An `OrderMapper` only transforms payloads; it does not call HTTP endpoints or write to the database.
- **O (Open/Closed):** Adding a new tenant connector (`SapErpConnector implements ErpConnector`) requires registering a new bean, not modifying existing `if/else` blocks in the router.
- **L (Liskov Substitution):** Any `MessagePublisher` implementation (`RabbitMqPublisher`, `KafkaPublisher`) honors the contract (throws `PublishException` on failure, never silently drops).
- **I (Interface Segregation):** Split fat interfaces into `ReadableConnector`, `WritableConnector`, and `HealthCheckableConnector` so read-only connectors aren't forced to implement `write()`.
- **D (Dependency Inversion):** Core orchestration depends on abstract ports (`CustomerRepository`, `BillingGateway`), while infrastructure adapters (`OracleCustomerRepository`, `RestBillingAdapter`) depend on those ports.

### Q40. What is Hexagonal Architecture (Ports and Adapters), and why is it ideal for iPaaS / Csmart integration?

**Answer:** Hexagonal Architecture places domain/orchestration contracts at the center and isolates all external tech (REST controllers, RabbitMQ listeners, Camel routes, Oracle/MySQL repositories, external SOAP/REST APIs) into **inbound (driving)** and **outbound (driven) adapters**. If an external system changes from SOAP XML to REST JSON, only the outbound adapter changes—Csmart and core domain models stay untouched.

### Q41. What is an Anti-Corruption Layer (ACL) in Domain-Driven Design?

**Answer:** An ACL is a translation layer (combining Facade, Adapter, and Translator patterns) placed between a clean internal system (like Csmart) and a legacy or third-party system with a messy data model. It prevents external naming conventions, status codes, and schema quirks from leaking into the core domain.

### Q42. How do you implement a pluggable Strategy Registry in Spring Boot without `if/else` or `switch` statements?

**Answer:** Inject a `List<ConnectorStrategy>` or `Map<String, ConnectorStrategy>` and index by a key method:

```java
@Service
public class ConnectorRouter {
    private final Map<ConnectorType, ConnectorStrategy> strategies;

    public ConnectorRouter(List<ConnectorStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toUnmodifiableMap(ConnectorStrategy::getType, Function.identity()));
    }

    public ConnectorStrategy resolve(ConnectorType type) {
        ConnectorStrategy strategy = strategies.get(type);
        if (strategy == null) throw new IllegalArgumentException("Unsupported type: " + type);
        return strategy;
    }
}
```

### Q43. What is the Resequencer Enterprise Integration Pattern, and when is it needed?

**Answer:** When messages are processed concurrently across multiple threads or queues, they can arrive out of order (e.g., `OrderUpdated` arrives before `OrderCreated`, or sequence #3 before #2). A Resequencer buffers out-of-order messages by correlation key and sequence number over a time/size window and releases them in strict sequence.

### Q44. What is the Composed Message Processor pattern?

**Answer:** It combines a **Splitter**, a **Router/Processor** for each individual item, and an **Aggregator** to collect the processed items back into a single combined response or batch outcome. In Camel, `.split(body(), new MyAggregationStrategy())...end()` implements this directly without needing a separate aggregator step.

### Q45. What is the Competing Consumers pattern vs Message Dispatcher?

**Answer:**
- **Competing Consumers:** multiple consumer threads/pods pull from the same queue concurrently to scale throughput.
- **Message Dispatcher:** a single coordinator reads from a channel and dispatches work to local performers based on rules or affinity.

### Q46. What is the Detour and Wire Tap pattern combination for production debugging?

**Answer:**
- **Wire Tap:** asynchronously sends a copy of the message to a secondary channel (audit log, debug queue) without slowing down the main flow.
- **Detour:** conditionally routes messages through extra validation, logging, or scrubbing steps when a feature flag or tenant debug flag is enabled via Control Bus.

### Q47. What is the Scatter-Gather pattern, and how do you handle partial failures?

**Answer:** Broadcast a request to multiple providers concurrently (e.g., querying three shipping carriers for rates) and aggregate the responses within a timeout. Handle partial failures by setting `completionTimeout` (e.g., 1500 ms) and `ignoreInvalidEndpoints` so the aggregator returns the best available quotes even if one carrier times out.

### Q48. What is the Inbox Pattern (vs Transactional Outbox)?

**Answer:** While the **Outbox** guarantees reliable at-least-once publishing from the producer, the **Inbox** pattern on the consumer side persists incoming messages (along with `message_id` in a unique-constrained `inbox` table) in a local transaction before or during processing. This guarantees idempotency, auditability, and safe local retries without losing messages.

### Q49. What is the Double-Checked Locking pattern in Java, and why is `volatile` mandatory?

**Answer:** Without `volatile`, the JVM instruction compiler can reorder object allocation and constructor execution, allowing another thread to see a non-null reference to a partially constructed object. Marking the field `private static volatile MyService instance;` establishes a happens-before barrier and prevents instruction reordering.

### Q50. What is the Sidecar / Ambassador pattern in cloud-native integration?

**Answer:** A helper container deployed in the same Kubernetes Pod as the main application container. It offloads infrastructure concerns such as mTLS termination, Vault secret rotation, log shipping (FluentBit), OpenTelemetry collection, or legacy protocol proxying, keeping the Java service focused on integration logic.

