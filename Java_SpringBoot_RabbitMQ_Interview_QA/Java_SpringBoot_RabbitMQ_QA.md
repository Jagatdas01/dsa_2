# Java, Spring Boot, Apache Camel and RabbitMQ: Interview Question Set

Mapped to the iPaaS / Integration Architect JD (6 to 10 years experience).

## Index

1. Spring Boot and Java Architecture (Q1 to Q8)
2. RabbitMQ Fundamentals and Spring AMQP (Q9 to Q18)
3. Reliability, Retries, DLQ and Idempotency (Q19 to Q26)
4. Apache Camel and Enterprise Integration Patterns (Q27 to Q36)
5. Camel with RabbitMQ and Kafka (Q37 to Q40)
6. Security (Q41 to Q45)
7. Observability and Performance (Q46 to Q52)
8. Data Integration, API Design and Platform Governance (Q53 to Q60)
9. High-Frequency Camel, Kafka, Redis, DB & Csmart Architect Questions (Q61 to Q75)

---

## 1. Spring Boot and Java Architecture

### Q1. How does Spring Boot auto-configuration work?

**Answer:** Spring Boot scans the classpath and conditional annotations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) decide which beans to register. Auto-configuration classes are listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Developers override defaults by declaring their own beans or by setting properties. Use `--debug` or the `/actuator/conditions` endpoint to see which configurations applied.

### Q2. How would you define service boundaries for a Spring Boot microservice platform?

**Answer:** Split by business capability (bounded context), not by technical layer. Each service owns its data store and exposes a stable API or events. In an iPaaS, keep these separate:
- Connectivity (adapters, protocols)
- Transformation (mapping, canonical model)
- Orchestration (routes, workflows)
- Business logic (stays in Csmart or the owning system)

Shared code should be limited to libraries such as DTOs, client SDKs, and starter modules. Avoid shared databases between services.

### Q3. What standards would you define for configuration management in Spring Boot?

**Answer:**
- Use `application.yml` with profiles (`dev`, `qa`, `prod`).
- Bind config to typed classes with `@ConfigurationProperties` and validate with `@Validated`.
- Keep secrets out of source control. Inject them from Vault, Kubernetes Secrets, or OCI/AWS/Azure secret managers.
- Use environment variables for deployment-specific values.
- Never hardcode endpoints or credentials.

### Q4. How do you standardize exception handling across Spring Boot services?

**Answer:** Use `@RestControllerAdvice` with `@ExceptionHandler` methods. In Spring Boot 3, return `ProblemDetail` (RFC 7807) so every error has the same shape: `type`, `title`, `status`, `detail`, `instance`. Include a correlation ID in every error response. Do not leak stack traces to clients. Map domain exceptions to HTTP status codes in one place.

### Q5. What are the trade-offs between a monolith, modular monolith, and microservices for an iPaaS?

**Answer:**
- Monolith: simple deployment, but scaling and fault isolation are weak.
- Modular monolith: clear module boundaries, one deployable unit. Good starting point.
- Microservices: independent scaling and deployment, but higher operational cost (service discovery, tracing, versioning, network failures).

For an iPaaS with many tenants and connectors, a common approach is a small set of runtime services (gateway, orchestration runtime, connector workers) plus shared libraries for reusable components.

### Q6. How do you handle dependency management and versioning in a multi-module Maven or Gradle project?

**Answer:** Use a parent POM or a platform BOM (for example, `spring-boot-dependencies` and a company BOM). Declare versions only in the BOM. Use `dependencyManagement` to pin transitive versions. Run `mvn dependency:tree` or `gradle dependencies` to find conflicts. Enforce rules with the Maven Enforcer plugin. Upgrade Spring Boot in one controlled step across services.

### Q7. How do you make Spring Boot services cloud-native?

**Answer:**
- Stateless services, so instances can be replaced at any time.
- Externalized config.
- Health endpoints: `/actuator/health/liveness` and `/actuator/health/readiness`.
- Graceful shutdown: `server.shutdown=graceful`.
- Containerize with layered jars or Buildpacks.
- Idempotent startup, with no reliance on local disk.

### Q8. How do you review a complex Spring Boot implementation for risks?

**Answer:** Check these areas:
- Blocking calls inside reactive or async code.
- Thread pool sizes for `@Async`, Tomcat, and Hikari.
- Missing timeouts on `RestTemplate` or `WebClient`.
- Transactions that wrap remote calls (`@Transactional` around HTTP or messaging calls).
- Circular dependencies.
- Unbounded caches or queues.
- Missing tests for error paths.
- Logging of sensitive data.

---

## 2. RabbitMQ Fundamentals and Spring AMQP

### Q9. Explain the RabbitMQ components: producer, exchange, queue, binding, and consumer.

**Answer:** A producer sends messages to an exchange. The exchange routes messages to queues using bindings and routing keys. Consumers read from queues. The exchange never stores messages. Message routing depends on the exchange type.

### Q10. What are the RabbitMQ exchange types, and when would you use each?

**Answer:**
- **Direct:** routes by exact routing key match. Use for point-to-point commands.
- **Fanout:** copies to all bound queues. Use for broadcast events.
- **Topic:** routes by pattern (`order.*.created`, `#`). Use for flexible pub/sub.
- **Headers:** routes by message headers. Use rarely, when routing needs attributes other than the key.

### Q11. How do you configure a queue, exchange, and binding in Spring AMQP?

**Answer:**

```java
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "orders.exchange";
    public static final String QUEUE = "orders.created.q";
    public static final String ROUTING_KEY = "order.created";

    @Bean
    TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    Queue ordersQueue() {
        return QueueBuilder.durable(QUEUE).quorum().build();
    }

    @Bean
    Binding ordersBinding(Queue ordersQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(ordersQueue).to(ordersExchange).with(ROUTING_KEY);
    }
}
```

### Q12. What is the difference between classic queues and quorum queues?

**Answer:** Quorum queues use the Raft consensus protocol and replicate across nodes. They are the recommended choice for data safety. They support delivery limits (poison message handling) and do not support non-durable, exclusive, or auto-delete-only use cases in the same way. Classic mirrored queues are deprecated. Use quorum queues for critical integration flows. Use classic queues only for transient data where loss is acceptable.

### Q13. What are the message acknowledgment modes in Spring AMQP?

**Answer:**
- `NONE`: the broker considers the message delivered on send. Messages can be lost. Avoid it.
- `AUTO` (default): Spring acknowledges after the listener returns and nacks on exception.
- `MANUAL`: the listener calls `basicAck`, `basicNack`, or `basicReject` directly.

Configure with `spring.rabbitmq.listener.simple.acknowledge-mode=manual`.

### Q14. How do you implement manual acknowledgment in a listener?

**Answer:**

```java
@RabbitListener(queues = "orders.created.q")
public void handle(Order order,
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                   Channel channel) throws IOException {
    try {
        orderService.process(order);
        channel.basicAck(tag, false);
    } catch (RecoverableException ex) {
        channel.basicNack(tag, false, true);   // requeue for retry
    } catch (Exception ex) {
        channel.basicNack(tag, false, false);  // dead-letter, do not requeue
    }
}
```

### Q15. What does prefetch (QoS) do, and how do you tune it?

**Answer:** Prefetch limits the number of unacknowledged messages delivered to a consumer. A low value spreads work evenly across consumers. A high value improves throughput but can cause uneven load and memory pressure. Set it with `spring.rabbitmq.listener.simple.prefetch`. Start with a value that matches processing time (for example, 10 to 50 for fast handlers, 1 to 5 for slow handlers). Measure before changing it.

### Q16. How do you scale consumers in Spring AMQP?

**Answer:** Use `spring.rabbitmq.listener.simple.concurrency` and `max-concurrency` to set the consumer thread range per listener. Add more pods for horizontal scale. Queue depth and consumer count together determine throughput. Use KEDA with the RabbitMQ scaler to scale pods on queue length.

### Q17. How do you publish messages reliably in Spring AMQP?

**Answer:** Enable publisher confirms and returns:

```properties
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
```

Set `rabbitTemplate.setMandatory(true)` so unroutable messages return to the producer. Handle `ConfirmCallback` and `ReturnsCallback`. Persist the outgoing message to an outbox table before publishing if you need guaranteed delivery with a database write (see Q24).

### Q18. What is the difference between RabbitMQ and Kafka, and when would you use each?

**Answer:**

| Aspect | RabbitMQ | Kafka |
|---|---|---|
| Model | Smart broker, queues, per-message ack | Distributed log, consumer offsets |
| Routing | Rich (exchanges, bindings) | Topic and partition based |
| Replay | Not native (unless stream queues are used) | Native, by offset |
| Ordering | Per queue, with single consumer | Per partition |
| Throughput | High, lower than Kafka at very high volume | Very high |
| Typical use | Task queues, request/reply, complex routing | Event streaming, event sourcing, analytics |

Use RabbitMQ for command-style messaging and flexible routing. Use Kafka for event logs that need replay and very high throughput.

---

## 3. Reliability, Retries, DLQ and Idempotency

### Q19. How do you configure retries in Spring AMQP?

**Answer:**

```properties
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=4
spring.rabbitmq.listener.simple.retry.initial-interval=2000
spring.rabbitmq.listener.simple.retry.multiplier=2
spring.rabbitmq.listener.simple.retry.max-interval=20000
```

Retries run in the consumer, which blocks the listener thread. For long backoff, use a delay exchange or a retry queue with TTL instead.

### Q20. What is a dead-letter exchange (DLX), and how do you set one up?

**Answer:** A DLX receives messages that are rejected without requeue, expire by TTL, or exceed the queue length limit. Set it on the queue with arguments:

```java
@Bean
Queue ordersQueue() {
    return QueueBuilder.durable("orders.created.q")
            .quorum()
            .deliveryLimit(5)
            .deadLetterExchange("orders.dlx")
            .deadLetterRoutingKey("orders.created.dead")
            .build();
}
```

Monitor the DLQ, alert on its depth, and have a documented replay procedure.

### Q21. How do you handle poison messages?

**Answer:** A poison message fails every time it is processed. Options:
- Quorum queues with `x-delivery-limit` move the message to the DLX after N redeliveries.
- Classify exceptions. Non-retryable errors (validation, schema mismatch) should be rejected without requeue at once.
- Use `ConditionalRejectingErrorHandler` or a custom `ErrorHandler` in Spring AMQP.
- Store the failed payload with the error reason for audit.

### Q22. How do you design a replay mechanism for DLQ messages?

**Answer:** Use a controlled replay service or Shovel plugin. Steps:
1. Inspect the message and error headers.
2. Fix the root cause (data, configuration, or code).
3. Move messages back to the original exchange, in batches, with a rate limit.
4. Ensure consumers are idempotent so replay does not create duplicates.
5. Log every replay with an operator ID and a correlation ID.

### Q23. What does idempotency mean in messaging, and how do you implement it?

**Answer:** Processing the same message more than once must give the same result. Brokers offer at-least-once delivery, so duplicates happen. Implementation options:
- Unique business key with a database constraint (`UNIQUE (order_id)`).
- Idempotency table keyed by `messageId` or a business key, written in the same transaction as the business change.
- Redis `SET key NX EX ttl` for short-lived deduplication.
- Camel `idempotentConsumer` EIP with a persistent repository.

### Q24. What is the transactional outbox pattern, and why is it needed?

**Answer:** A service writes the business change and an outbox row in one local database transaction. A separate relay reads the outbox and publishes to RabbitMQ, then marks the row as sent. This avoids the dual-write problem, where the database commits but the publish fails, or the reverse. The relay can be polling or CDC-based (for example, Debezium). Consumers must still be idempotent.

### Q25. What are the common compensation patterns for long-running workflows?

**Answer:** Use the saga pattern. Each step has a compensating action (for example, "release inventory" for "reserve inventory"). Two styles:
- **Choreography:** services react to events and publish the next event. Simple, but harder to track.
- **Orchestration:** a central orchestrator (for example, a Camel route or a workflow engine) calls steps and triggers compensations on failure.

For an iPaaS, orchestration is usually easier to observe and govern.

### Q26. How do you prevent cascading failures across integrated systems?

**Answer:**
- Set timeouts on every outbound call.
- Use circuit breakers (Resilience4j or Camel's `circuitBreaker()`).
- Use bulkheads (separate thread pools or connection pools per downstream system).
- Apply rate limiting and back-pressure (prefetch, `throttle()`).
- Isolate queues per downstream system, so one slow target does not block others.
- Use DLQs so failures are parked, not retried endlessly.

---

## 4. Apache Camel and Enterprise Integration Patterns

### Q27. What is Apache Camel, and what are its core concepts?

**Answer:** Apache Camel is an integration framework that implements Enterprise Integration Patterns. Core concepts:
- **Component:** adapter for a system (for example, `rabbitmq:`, `kafka:`, `http:`, `sql:`, `file:`).
- **Endpoint:** a component URI with options.
- **Route:** a flow defined with `from(...).to(...)`.
- **Exchange:** the message container during processing (body, headers, properties, exception).
- **Processor:** a Java class that transforms or handles the exchange.
- **Message:** body and headers inside an exchange.

### Q28. Show a simple Camel route in Java DSL.

**Answer:**

```java
@Component
public class OrderRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("rabbitmq:orders.exchange?queue=orders.created.q&routingKey=order.created&exchangeType=topic&autoAck=false")
            .routeId("order-created-route")
            .unmarshal().json(OrderDto.class)
            .process(orderValidator)
            .marshal().json()
            .to("https://erp.internal/api/orders");
    }
}
```

### Q29. How do you implement error handling in Camel?

**Answer:** Use `errorHandler` or `onException` with redelivery policy and a dead-letter channel:

```java
errorHandler(deadLetterChannel("rabbitmq:orders.dlx?routingKey=orders.created.dead")
        .maximumRedeliveries(3)
        .redeliveryDelay(1000)
        .useExponentialBackOff()
        .backOffMultiplier(2)
        .logExhausted(true));

onException(ValidationException.class)
        .handled(true)
        .to("direct:invalid-payload");
```

Use `onException` to handle specific exception types differently from the default behavior. Always log the exchange ID and correlation ID.

### Q30. What is the difference between `errorHandler` and `onException`?

**Answer:** `errorHandler` sets the default behavior for the whole route builder (or a route). `onException` defines behavior for a specific exception type. Several `onException` blocks can exist, and Camel uses the most specific match. Use `handled(true)` to stop propagation, or `continued(true)` to continue the route.

### Q31. How do you implement a content-based router in Camel?

**Answer:**

```java
from("direct:orders-in")
    .choice()
        .when(simple("${body.region} == 'EU'")).to("direct:eu-erp")
        .when(simple("${body.region} == 'US'")).to("direct:us-erp")
        .otherwise().to("direct:default-erp")
    .end();
```

Keep routing rules in configuration or in a routing table when many tenants exist. This supports onboarding new clients without code changes.

### Q32. How do you implement message transformation and enrichment in Camel?

**Answer:**
- **Transformation:** `marshal()` and `unmarshal()` for formats (JSON, XML, CSV), `transform()` for expressions, `process()` for custom code, XSLT for XML, Jackson or MapStruct for object mapping.
- **Enrichment:** `enrich()` (calls a resource and merges the result using an `AggregationStrategy`) or `pollEnrich()` (reads from a consumer endpoint).

Keep canonical mapping in dedicated processors or mapper classes, not inline in routes.

### Q33. How do you implement a splitter, aggregator, and wire tap?

**Answer:**
- **Splitter:** `split(body()).streaming()` divides a batch into items. Use `parallelProcessing()` with care.
- **Aggregator:** `aggregate(header("batchId"), strategy).completionSize(100).completionTimeout(5000)` combines messages.
- **Wire tap:** `wireTap("direct:audit")` sends a copy to another route without waiting for it. Use it for audit and monitoring.

### Q34. How do you manage threads and back-pressure in Camel?

**Answer:** Use `.threads(poolSize, maxPoolSize)` for a dedicated thread pool on a step. Use `.throttle(rate).timePeriodMillis(1000)` for rate limiting. Set `.stopOnException()` or `.onException()` to avoid silent drops. Monitor the inflight exchanges and thread pool queue with Camel metrics. Avoid unbounded `seda:` queues.

### Q35. What is the difference between `direct:`, `seda:`, and `vm:` in Camel?

**Answer:**
- `direct:` is synchronous and runs in the caller thread.
- `seda:` is asynchronous, with an in-memory queue and a consumer thread pool. Use it for decoupling within a JVM.
- `vm:` is like `seda:` but shared across Camel contexts in the same JVM.

Use `direct:` for sub-routes that must return a result. Use `seda:` for async steps with bounded queue sizes.

### Q36. How do you review a complex Camel route for performance and reliability problems?

**Answer:** Check these points:
- Blocking calls inside `process()` without timeouts.
- Unbounded `aggregate()` or `seda:` queues.
- Large bodies loaded into memory (use streaming for splitting and files).
- Missing `idempotentConsumer` for at-least-once sources.
- Error handling that drops messages silently.
- Missing `routeId` and logging, which make tracing difficult.
- Shared mutable state in processors (not thread-safe).
- Connection pools that are too small or not configured.

---

## 5. Camel with RabbitMQ and Kafka

### Q37. How do you consume from RabbitMQ in Camel, and what options matter most?

**Answer:** Use the `rabbitmq:` component:

```
rabbitmq:orders.exchange?queue=orders.created.q&routingKey=order.created&exchangeType=topic&autoAck=false&prefetchEnabled=true&prefetchCount=20
```

Key options:
- `autoAck=false` for at-least-once processing.
- `prefetchCount` to control in-flight messages.
- `exchangeType` and `routingKey` to match the broker setup.
- `durable` and `autoDelete` for queue lifecycle.

Verify option names against the Camel version in use.

### Q38. How do you publish from Camel to RabbitMQ with publisher confirms?

**Answer:** Configure the component to wait for confirms and use the correct routing key. Send with a stable message ID header so consumers can deduplicate:

```java
from("direct:order-events")
    .setHeader("messageId", simple("${exchangeId}"))
    .setHeader("correlationId", header("X-Correlation-Id"))
    .to("rabbitmq:orders.exchange?routingKey=order.created&exchangeType=topic");
```

Set the broker-side confirm configuration in the component or the Spring AMQP template, depending on how the Camel context is built.

### Q39. How do you integrate Camel with Kafka and RabbitMQ in one platform?

**Answer:** Use Camel as the connectivity layer. Use Kafka for event streams and replay. Use RabbitMQ for command queues and routing-heavy flows. A bridge route reads from one and writes to the other:

```java
from("kafka:customer-events?brokers={{kafka.brokers}}&groupId=bridge")
    .to("rabbitmq:crm.exchange?routingKey=customer.updated&exchangeType=topic");
```

Define the event schema and idempotency rules in the platform standards. Do not let bridges hide message loss.

### Q40. How do you handle schema evolution for messages between services?

**Answer:**
- Use a schema registry with Avro or Protobuf for compatibility checks (backward, forward, full).
- For JSON, add optional fields only, never rename or remove fields in place.
- Include a `schemaVersion` header or a version field in the payload.
- Use consumer-driven contract tests (for example, Spring Cloud Contract or Pact).
- Keep old and new versions running in parallel during migration.

---

## 6. Security

### Q41. How do you secure RabbitMQ connections in Spring Boot?

**Answer:**
- Use TLS: `spring.rabbitmq.ssl.enabled=true`, with trust store and key store properties.
- Use separate users per service with least-privilege permissions (configure, write, read).
- Use vhosts to isolate tenants or environments.
- Disable the default `guest` user for remote access.
- Store passwords in a secret manager, not in `application.yml`.

### Q42. How do you implement mTLS between services?

**Answer:** Each service has a client certificate signed by an internal CA. The server validates the client certificate. Use `server.ssl.client-auth=need` for Spring Boot servers. For outbound calls, configure a key store and trust store on the HTTP client. In Kubernetes, use a service mesh (Istio or Linkerd) to handle mTLS automatically and rotate certificates.

### Q43. How do you secure REST APIs with OAuth2 and OIDC in Spring Boot?

**Answer:** Use Spring Security as a resource server:

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/api/**").hasAuthority("SCOPE_orders.write")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
    return http.build();
}
```

Validate issuer, audience, and expiry. Use short-lived access tokens. Use client credentials flow for service-to-service calls. Set `spring.security.oauth2.resourceserver.jwt.issuer-uri`.

### Q44. How do you manage secrets in a Kubernetes-based integration platform?

**Answer:**
- Store secrets in Vault, OCI Vault, AWS Secrets Manager, or Azure Key Vault.
- Sync to pods with the Vault Agent, External Secrets Operator, or CSI driver.
- Mount as files or environment variables, and rotate them without a redeploy where possible.
- Enable encryption at rest for etcd.
- Audit secret access.

Never log secrets. Mask them in config dumps and actuator endpoints (`/actuator/env` must be restricted).

### Q45. How do you ensure comprehensive auditability of integration transactions?

**Answer:** Record each transaction with: correlation ID, message ID, source and target systems, tenant ID, timestamp, status, and a hash or reference to the payload (not the full sensitive payload unless required). Write audit events to an append-only store. Retain according to policy. Make audit logs queryable by correlation ID across services.

---

## 7. Observability and Performance

### Q46. How do you implement correlation IDs and distributed tracing in Spring Boot?

**Answer:** Use Micrometer Tracing with OpenTelemetry or Brave. It propagates trace context (W3C `traceparent`) over HTTP and AMQP headers. Put the trace ID in logs using MDC. Enable RabbitMQ observation:

```properties
spring.rabbitmq.template.observation-enabled=true
spring.rabbitmq.listener.simple.observation-enabled=true
```

Accept an incoming correlation ID header if present and generate one if absent.

### Q47. What metrics would you expose for an integration service?

**Answer:**
- Throughput (messages per second, in and out).
- Latency (p50, p95, p99) per route or endpoint.
- Error rate and retry count.
- Queue depth and consumer count (from the RabbitMQ management API or Prometheus plugin).
- DLQ depth and age of oldest message.
- Thread pool usage and inflight exchanges (Camel metrics).
- Connection pool usage (Hikari, HTTP clients).
- JVM heap, GC pauses, and CPU.

### Q48. What alerts would you define for an integration platform?

**Answer:**
- DLQ depth above threshold for more than N minutes.
- Queue depth growing for more than N minutes with no consumers.
- Error rate above SLO for a tenant or route.
- Latency p99 above SLO.
- Circuit breaker open for a downstream system.
- Health check failures or pod restart loops.
- Certificate expiry within 30 days.

### Q49. How do you tune Spring AMQP connection and channel usage for performance?

**Answer:** Use channel caching (default) with a sensible channel cache size. Use `spring.rabbitmq.cache.connection.mode=channel` for most cases, and `connection` mode only when required. Batch publishing with `invoke` and confirms. Avoid creating a connection per message. Use a dedicated connection factory for publishing and consuming if load is high.

### Q50. How do you tune Hikari and JDBC for an integration service that writes to Oracle or MySQL?

**Answer:**
- Set `maximumPoolSize` based on DB capacity, not on thread count. A common starting point is `(core_count * 2) + effective_spindles`, adjusted by load tests.
- Set `connectionTimeout` and `maxLifetime` below the DB or firewall idle timeout.
- Batch inserts with `rewriteBatchedStatements=true` (MySQL) or JDBC batch settings (Oracle).
- Use prepared statements and avoid N+1 queries.
- Monitor pool wait time.

### Q51. How do you plan capacity and performance tests for the iPaaS?

**Answer:**
- Define target throughput, message size, and latency SLOs per integration type.
- Build realistic load profiles (peak, burst, and steady state).
- Test each component separately (route, broker, database, downstream mock) and then end to end.
- Include failure tests: downstream slow, downstream down, broker restart.
- Track results over time and use them for scaling decisions.
- Tools: Gatling, JMeter, k6, and RabbitMQ PerfTest.

### Q52. How do you isolate workloads for high-volume and low-volume tenants?

**Answer:** Use separate queues, consumer groups, and thread pools per tenant tier. Use separate vhosts or clusters for very large tenants. Apply rate limits per tenant. Use separate Kubernetes deployments or node pools for batch workloads, so batch jobs do not starve real-time traffic.

---

## 8. Data Integration, API Design and Platform Governance

### Q53. How do you design a synchronous REST API vs an asynchronous API?

**Answer:**
- **Synchronous:** the caller waits for the result. Use for queries and short operations. Set timeouts and idempotency keys for POST.
- **Asynchronous:** the server accepts the request (HTTP 202 Accepted) and returns a status URL. Use for long-running work. Use callbacks or webhooks for completion notices.

Choose based on latency tolerance, client capability, and failure handling needs.

### Q54. What API versioning and backward compatibility strategy would you define?

**Answer:**
- Version in the URI (`/v1/orders`) for major, breaking changes, or use media type versioning.
- Backward-compatible changes (add optional fields, add endpoints) do not need a new version.
- Breaking changes need a new version and a deprecation timeline.
- Publish OpenAPI specs and check them in CI with a linter (Spectral) and a breaking-change detector (openapi-diff).

### Q55. How do you define API throttling and rate limiting?

**Answer:** Enforce limits at the API gateway (per client ID or API key) using token bucket or sliding window algorithms. Return HTTP 429 with a `Retry-After` header. Use Bucket4j or Spring Cloud Gateway's `RequestRateLimiter` for service-level limits. Apply separate limits for read and write operations. Publish limits in API documentation.

### Q56. How do you design a canonical data model for multiple systems?

**Answer:** Define a canonical model only for common concepts (customer, order, product). Keep it small and versioned. Map each system to the canonical model in an adapter layer, not in the business logic. Avoid a single model that tries to represent everything. Document field meanings, units, and code lists.

### Q57. How do you synchronize data between MySQL and Oracle?

**Answer:** Options depend on the latency and consistency needs:
- **Batch:** scheduled extract, transform, load with watermark columns (`updated_at`).
- **CDC:** Debezium reads the MySQL binlog or Oracle redo logs (LogMiner or XStream), publishes change events to Kafka or RabbitMQ, and a consumer applies them to the target.
- **Real-time sync via application events:** use the transactional outbox pattern.

Handle type differences (for example, `NUMBER` vs `BIGINT`, `DATE` vs `DATETIME`, and time zones). Use a unique business key and upsert (`MERGE` in Oracle, `INSERT ... ON DUPLICATE KEY UPDATE` in MySQL). Reconcile counts and checksums regularly.

### Q58. How do you handle conflicts in bidirectional data synchronization?

**Answer:** Choose one of these:
- Single writer per record (ownership rules).
- Last-write-wins with a version or timestamp (accept data loss risk).
- Optimistic locking with `@Version` in JPA, rejecting stale updates.
- Conflict queue for manual resolution.

Document the rule per entity. Avoid bidirectional sync where a simpler one-way flow works.

### Q59. How do you govern integration development across teams?

**Answer:**
- Maintain a reusable component catalog (connectors, routes, templates).
- Use architecture review for new integrations, with a checklist (security, error handling, observability, SLOs).
- Require ADRs (Architecture Decision Records) for significant choices.
- Enforce standards with linters, static analysis (SpotBugs, Checkstyle), and CI templates.
- Hold shared code in versioned libraries with release notes.

### Q60. How do you onboard a new client quickly using reusable integration capabilities?

**Answer:** Start from a template route or a connector from the catalog. Configure the client via properties and a tenant record (endpoints, credentials reference, mapping version, routing rules). Avoid code changes for standard cases. Only build custom mapping when no reusable option fits, and promote it to the catalog if other clients need it. Test with a contract suite and a sandbox before production.

---

## 9. High-Frequency Camel, Kafka, Redis, DB & Csmart Architect Questions

### Q61. What are Apache Camel Route Templates (`routeTemplate`), and how do they enable multi-tenant iPaaS onboarding?

**Answer:** Route Templates define a parameterized route skeleton (`templateParameter("tenantId")`, `templateParameter("sourceQueue")`, `templateParameter("targetUrl")`). At startup or dynamically at runtime via `TemplatedRouteBuilder`, the platform instantiates isolated routes per customer from database/YAML metadata without writing or deploying new Java classes.

### Q62. In Apache Camel 4 (with Spring Boot 3 / Java 17-21), what changed regarding the RabbitMQ component and headers?

**Answer:**
- The legacy `camel-rabbitmq` component was replaced by **`camel-spring-rabbitmq`**, which integrates directly with Spring AMQP's `RabbitTemplate` and `MessageListenerContainer`.
- In the `Exchange`, `getOut()` is deprecated; always mutate or replace the message via `exchange.getMessage()` (or `exchange.getIn()`).
- Exchange properties (`exchange.getProperty()`) persist for the duration of the route inside Camel, whereas Message headers (`message.getHeader()`) travel across wire protocols (HTTP, AMQP, Kafka). Use `HeaderFilterStrategy` to block internal headers from leaking to external partners.

### Q63. How do you handle large CSV/XML batch files (e.g., 5 GB) in Apache Camel without causing `OutOfMemoryError`?

**Answer:** Use streaming and tokenizing instead of reading the entire file into memory:

```java
from("file:/inbox?move=.done&moveFailed=.error&readLock=changed")
    .routeId("large-csv-batch-route")
    .split(body().tokenize("\n")).streaming().parallelProcessing()
        .unmarshal().csv()
        .to("direct:processRecord")
    .end();
```

For XML, use `.split().tokenizeXML("order").streaming()` (StAX-based streaming) so memory footprint stays constant regardless of file size.

### Q64. How does Apache Camel's `AggregationStrategy` work when the JVM crashes mid-batch?

**Answer:** By default, Camel aggregates in memory, so in-flight batches are lost on pod crash. For critical workflows, configure a persistent `AggregationRepository` backed by **JdbcAggregationRepository** (Oracle/MySQL) or **Redis**, and configure `completionSize`, `completionTimeout`, and recovery scanning so incomplete or failed aggregates are recovered by surviving pods.

### Q65. How do you test Apache Camel routes in Spring Boot without connecting to real external systems?

**Answer:** Use `@CamelSpringBootTest`, `@MockEndpointsAndSkip("https:*|spring-rabbitmq:*")`, and `AdviceWith`:

```java
AdviceWith.adviceWith(camelContext, "order-created-route", a -> {
    a.replaceFromWith("direct:start");
    a.weaveByToUri("https://erp.internal/*").replace().to("mock:erp");
});
```

Then send test payloads with `ProducerTemplate` and verify expectations on `MockEndpoint` (`mockErp.expectedMessageCount(1)`).

### Q66. How do you prevent duplicate processing across a cluster of Camel pods consuming from a shared folder, SFTP server, or DB table?

**Answer:**
- **SFTP/File:** use a distributed `idempotentRepository` and `readLock=idempotent-changed` backed by Redis or JDBC so two pods never pick up the same file.
- **Database Polling:** use `SELECT ... FOR UPDATE SKIP LOCKED` (supported in both Oracle and MySQL 8+) or a Claim-Check status column update so competing pollers never lock the same rows.
- **Singleton Route:** use Camel **ClusteredRoutePolicy** (backed by Kubernetes lease, Consul, or JDBC lock) so only the leader pod runs the polling route.

### Q67. What are the key differences between MySQL and Oracle that affect enterprise integration and Spring Data/JDBC code?

**Answer:**
- **Empty String vs NULL:** Oracle treats `''` (empty string) as `NULL` in `VARCHAR2`; MySQL treats `''` and `NULL` as distinct.
- **Sequences & Auto-Increment:** MySQL uses `AUTO_INCREMENT` (`GenerationType.IDENTITY`, which disables Hibernate batch inserts!). Oracle uses `SEQUENCE` (`GenerationType.SEQUENCE` with `allocationSize=50`), which allows high-speed Hibernate JDBC batching.
- **Pagination:** MySQL uses `LIMIT ? OFFSET ?`; Oracle 12c+ uses `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY` (keyset/seek pagination `WHERE id > :lastId ORDER BY id FETCH FIRST 100 ROWS ONLY` is much faster in both for deep pages).
- **Upsert:** MySQL uses `INSERT ... ON DUPLICATE KEY UPDATE`; Oracle uses `MERGE INTO`.

### Q68. Why does `GenerationType.IDENTITY` disable Hibernate JDBC batching, and how do you fix it?

**Answer:** With `IDENTITY`, Hibernate must execute the `INSERT` statement immediately on `save()` to fetch the generated primary key, breaking transactional write-behind batching. Fix by using `GenerationType.SEQUENCE` (Oracle) or UUIDv7 / TSID (time-sorted unique IDs generated in the application) or `JdbcTemplate.batchUpdate()` for high-volume MySQL ingestion.

### Q69. In Kafka, what happens during a consumer group rebalance, and how do you minimize its impact?

**Answer:** When a consumer joins, leaves, or exceeds `max.poll.interval.ms`, partitions are reassigned. Eager rebalancing stops all consumers ("stop-the-world"). Minimize impact by:
1. Using `CooperativeStickyAssignor` (incremental cooperative rebalancing).
2. Using static group membership (`group.instance.id`) so brief pod restarts don't trigger immediate rebalances.
3. Tuning `max.poll.records` and `max.poll.interval.ms` so slow downstream calls never cause the consumer to be kicked out of the group.

### Q70. How do you handle poison messages and retries in Kafka vs RabbitMQ?

**Answer:** Unlike RabbitMQ, Kafka cannot individually ack or requeue a single message out of order because offset commits are sequential per partition. In Spring for Apache Kafka, use `@RetryableTopic` (non-blocking retries): failed messages are forwarded to `orders-retry-1000`, `orders-retry-2000`, and finally `orders-dlt` (Dead Letter Topic), allowing the main partition consumer to keep moving forward without blocking.

### Q71. How do you use Redis safely in an integration platform, and what happens if Redis goes down?

**Answer:** Common iPaaS uses for Redis:
1. OAuth2 access token caching (caching partner tokens until 60s before `exp`).
2. Short-term idempotency / deduplication (`SET msgId 1 NX EX 86400`).
3. Distributed rate limiting (Token Bucket via Lua scripts).
4. Reference data lookup cache (country codes, currency rates, tenant routing tables).
- **Failure design:** Wrap Redis calls with a fast timeout (100–200ms) and a fallback: if Redis is down, token cache falls back to fetching a fresh token, reference cache falls back to DB, and idempotency falls back to the DB unique constraint.

### Q72. How do you handle outbound OAuth2 token management when calling 50 different external partner APIs?

**Answer:** Never fetch a new OAuth2 token on every outbound API call. Use Spring Security's `OAuth2AuthorizedClientManager` (or a custom token provider cached in Redis by `tenantId + partnerId`) that:
- Caches the token until `expires_in - safetyMargin` (e.g., 60 seconds prior to expiry).
- Uses a lock (`synchronized` / Redis lock) on token refresh so 100 concurrent threads don't trigger 100 simultaneous `/oauth/token` calls.
- Automatically evicts the cached token and retries once if the partner returns `401 Unauthorized`.

### Q73. What is the difference between Orchestration and Choreography, and when do you use each in Csmart + iPaaS?

**Answer:**
- **Choreography (Event-Driven):** Csmart emits `CustomerActivatedEvent`; CRM, Billing, and Notification integrations independently subscribe and react. Best for decoupled, extensible domain events where Csmart doesn't care about downstream consumers.
- **Orchestration (Command-Driven):** An iPaaS Camel route or workflow coordinates an ordered sequence (`Step 1: Validate Credit -> Step 2: Provision in Csmart -> Step 3: Update Partner ERP`, with compensating rollback on failure). Best when strict step ordering, conditional branching, and centralized SLA tracking are required.

### Q74. How do you design a Canonical Data Model (CDM) without creating a brittle enterprise bottleneck?

**Answer:**
- Scope canonical models **per domain** (e.g., `Order`, `Customer`, `Invoice`), not one giant enterprise-wide schema.
- Version canonical schemas (`v1`, `v2`) and keep fields optional where partner support varies.
- Include an `extensions` map (`Map<String, Object>`) only for pass-through metadata, never for core routing fields.
- Provide a shared library of validated JSON Schemas / Avro schemas and MapStruct mappers so every connector maps `Partner Model <-> Canonical Model <-> Csmart Model`.

### Q75. Walk through how you would architect a real-time + batch hybrid integration between Csmart and an external ERP.

**Answer:**
1. **Real-time path (Low latency):** Csmart writes state + Outbox -> publishes event to RabbitMQ/Kafka -> Camel route transforms to ERP format, enriches from Redis cache, applies Resilience4j CircuitBreaker + RateLimiter -> calls ERP REST API -> writes audit trail. Failed messages go to retry tiers then DLQ with alerts.
2. **Batch path (High volume / Reconciliation):** Nightly delta extract or SFTP file drop -> Claim Check to Object Storage -> Camel streaming splitter processes chunks -> JDBC batch upsert (`MERGE` / `ON DUPLICATE KEY UPDATE`) -> generates reconciliation summary report comparing Csmart vs ERP record counts and checksums.

