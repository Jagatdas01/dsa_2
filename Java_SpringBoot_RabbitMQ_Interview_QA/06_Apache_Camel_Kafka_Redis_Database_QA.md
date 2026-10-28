# Apache Camel, Kafka, Redis, MySQL & Oracle: Interview Question Set

Target: 6 to 10 years experience, iPaaS / Csmart Integration Architect role.

## Index

1. Apache Camel Architecture & Internals (Q1 to Q12)
2. Apache Camel Enterprise Integration Patterns (EIPs) & Error Handling (Q13 to Q22)
3. Apache Kafka vs RabbitMQ & Event Streaming (Q23 to Q30)
4. Redis for Caching, Locking & Rate Limiting (Q31 to Q36)
5. MySQL & Oracle Data Integration & Performance (Q37 to Q44)
6. Csmart, API Governance & Security Architecture (Q45 to Q50)

---

## 1. Apache Camel Architecture & Internals

### Q1. Explain the internal architecture of `CamelContext` and the lifecycle of a Camel `Exchange`.

**Answer:** `CamelContext` is the runtime container holding components, endpoints, routes, type converters, data formats, registries (Spring `ApplicationContext`), and thread pool profiles. When a consumer endpoint receives data, it creates an `Exchange` (with an `exchangeId`, `ExchangePattern` (`InOnly` or `InOut`), `Message` (`In`), headers, and exchange properties) and passes it through a chain of `AsyncProcessor` nodes defined by the route DSL until completion, where `UnitOfWork` synchronization callbacks (`onCompletion`) fire.

### Q2. What is the difference between `InOnly` and `InOut` Message Exchange Patterns (MEP) in Camel?

**Answer:**
- **`InOnly` (Event / One-Way):** fire-and-forget. The consumer does not return a response to the caller (e.g., consuming an event queue).
- **`InOut` (Request-Reply):** the caller expects a response back (e.g., synchronous REST endpoint or `convertSendAndReceive` over AMQP).

### Q3. What is the difference between Exchange Properties and Message Headers in Camel?

**Answer:**
- **Exchange Properties (`exchange.setProperty(k, v)`):** stored on the `Exchange` metadata for the lifetime of the route execution inside Camel; never sent over the wire to external endpoints.
- **Message Headers (`exchange.getMessage().setHeader(k, v)`):** travel with the message across component boundaries (mapped to HTTP headers, AMQP properties, or Kafka headers). Use a `HeaderFilterStrategy` to strip internal headers before calling external systems.

### Q4. How does Apache Camel Type Conversion work?

**Answer:** Camel uses a `TypeConverterRegistry` with built-in and `@Converter`-annotated methods to automatically convert payloads between `byte[]`, `String`, `InputStream`, `Document`, `Map`, and POJOs (`exchange.getMessage().getBody(OrderDto.class)`).

### Q5. What is `StreamCaching` in Apache Camel, and why is it critical?

**Answer:** By default, streaming bodies (`InputStream` from HTTP, File, or SFTP) can only be read **once**. If a route logs the body, evaluates a `choice()` XPath/JSONPath predicate, and then forwards the body to an endpoint, the stream is already exhausted (`Stream closed` or empty payload). Enable `streamCaching()` (enabled by default in Camel 3.17+ / Camel 4) with spooling to disk threshold (`spoolThreshold`) for large payloads.

### Q6. What are Route Templates (`routeTemplate`) and Kamelets, and how do they drive iPaaS reuse?

**Answer:** Route Templates parameterize routes (`templateParameter("tenantId")`, `templateParameter("sourceUri")`, `templateParameter("targetUri")`). Kamelets (Camel Route Snippets) package reusable source, sink, or action connectors with a defined schema. Together they allow onboarding new customers via configuration without writing new route classes.

### Q7. How do you dynamically control routes at runtime (start, stop, suspend, resume)?

**Answer:**
- Use the **ControlBus** EIP (`to("controlbus:route?routeId=erp-sync&action=suspend")`).
- Use `RouteController` (`camelContext.getRouteController().suspendRoute("erp-sync")`).
- Prefer `suspend` over `stop` for temporary maintenance because `suspend` drains in-flight exchanges gracefully while keeping resources warm.

### Q8. How do `RoutePolicy` and `ClusteredRoutePolicy` work?

**Answer:** A `RoutePolicy` intercepts route lifecycle callbacks (`onStart`, `onExchangeBegin`, `onExchangeDone`) to dynamically throttle, pause, or record metrics. A `ClusteredRoutePolicy` (using Kubernetes Leases, JDBC, Consul, or ZooKeeper) ensures a singleton polling route (such as SFTP or legacy DB polling) runs on only one active leader pod at a time.

### Q9. How do you avoid thread starvation in Apache Camel routes?

**Answer:**
- Never block consumer I/O threads with long synchronous calls without bounded timeouts.
- Configure explicit `ThreadPoolProfile` settings (core size, max size, `maxQueueSize`, and `ThreadPoolRejectedPolicy`).
- Avoid unbounded `seda:` queues (`seda:work?size=1000&blockWhenFull=true`).
- Separate heavy batch processing routes onto dedicated thread pools or separate worker deployments.

### Q10. How do you write custom reusable Camel Components or Processors vs Spring Beans?

**Answer:** Prefer plain Spring `@Component` beans invoked via `.bean(orderEnricher, "enrich")` over `Processor` implementations when possible, because plain beans have no Camel API dependency and are trivial to unit test. Use `Processor` or a custom Camel `Component` when you need direct access to `Exchange` headers, properties, or endpoint lifecycle management.

### Q11. How do you ensure thread safety inside Camel `Processor` and `@Bean` classes?

**Answer:** Camel `Processor` and Spring singleton beans are shared across all concurrent exchanges on a route. Never store per-message state in instance fields of a `Processor` or `@Service`. Store per-message state in `exchange.setProperty(...)` or pass it as method arguments.

### Q12. What is `UnitOfWork` and `onCompletion()` in Apache Camel?

**Answer:** Every `Exchange` has a `UnitOfWork` tracking its processing lifecycle. `onCompletion()` registers synchronization hooks (`onCompleteOnly()`, `onFailureOnly()`) that execute when the exchange finishes—used for audit logging, metrics recording, temporary file cleanup, or releasing correlation contexts.

---

## 2. Apache Camel Enterprise Integration Patterns (EIPs) & Error Handling

### Q13. Compare `DefaultErrorHandler`, `DeadLetterChannel`, and `TransactionErrorHandler` in Camel.

**Answer:**
- `DefaultErrorHandler`: does not dead-letter by default; propagates the exception back to the caller after redeliveries.
- `DeadLetterChannel`: after exhausting redeliveries, moves the failed exchange to a dead-letter endpoint (`spring-rabbitmq:dlx`, `kafka:dlt`, `log:dead`) and marks the exception as handled so the consumer can move on.
- `TransactionErrorHandler`: wraps route execution in a Spring `PlatformTransactionManager` and triggers transaction rollback on unhandled exceptions.

### Q14. What is the difference between `handled(true)` and `continued(true)` in `onException`?

**Answer:**
- `handled(true)`: stops normal route execution, routes the exchange to the `onException` pipeline, clears the exception, and returns a normal completion to the caller/consumer.
- `continued(true)`: ignores the exception at the failing step and **continues** execution at the very next step in the original route.

### Q15. How do you access the exception and failure endpoint inside an `onException` or DLQ route?

**Answer:** Camel stores failure details in Exchange properties:
- `Exchange.EXCEPTION_CAUGHT` (`${exception}`): the thrown `Throwable`.
- `Exchange.FAILURE_ENDPOINT`: the URI of the endpoint that failed (e.g., `https://erp.internal/orders`).
- `Exchange.FAILURE_ROUTE_ID`: the route ID where the failure occurred.
Copy these into message headers before publishing to a RabbitMQ or Kafka DLQ so operators know why and where the message failed.

### Q16. How do you implement the Circuit Breaker EIP inside an Apache Camel route?

**Answer:** Use Camel's native `circuitBreaker()` DSL backed by Resilience4j:

```java
from("direct:callErp")
    .circuitBreaker()
        .resilience4jConfiguration()
            .timeoutEnabled(true).timeoutDuration(3000)
            .failureRateThreshold(50).waitDurationInOpenState(30)
        .end()
        .to("https://erp.internal/api/orders")
    .onFallback()
        .to("direct:queueForLater")
    .end();
```

### Q17. How do `enrich()` and `pollEnrich()` differ?

**Answer:**
- `enrich("https://crm.internal/customers/${header.customerId}", aggregationStrategy)`: uses a **Producer** to make a request-reply call (HTTP, RPC, direct route) and merges the response with the original exchange.
- `pollEnrich("file:/inbox/meta?fileName=${header.metaFile}", timeout, aggregationStrategy)`: uses a **Polling Consumer** to fetch data from a passive source (File, SFTP, JMS/AMQP queue) and merges it.

### Q18. How do you implement the Claim Check EIP using Camel's `claimCheck` DSL?

**Answer:**
- **In-memory / route scope:** `.claimCheck(ClaimCheckOperation.Set, "originalOrder")` stashes the body in the exchange repository before calling a lightweight service, and `.claimCheck(ClaimCheckOperation.GetAndRemove, "originalOrder")` restores it afterward.
- **Distributed / cross-broker scope:** upload large payloads to S3/Object Storage via `aws2-s3:` or `oci-object-storage:`, replace the body with the object key, and send the lightweight reference over RabbitMQ/Kafka.

### Q19. How do you implement the Saga EIP in Apache Camel?

**Answer:** Camel provides the `.saga()` DSL (`InMemorySagaService` or `LRASagaService` for distributed Long-Running Actions):

```java
from("direct:orderSaga")
    .saga()
    .compensation("direct:cancelOrder")
    .completion("direct:completeOrder")
    .to("direct:reserveInventory")
    .to("direct:chargePayment");
```

### Q20. How do you validate JSON, XML, and CSV schemas in Camel routes?

**Answer:**
- **JSON:** `.to("json-validator:classpath:schemas/order-v1.json")`
- **XML:** `.to("validator:classpath:schemas/order.xsd")`
- **Java Bean Validation:** `.to("bean-validator:orderRules")` after unmarshalling to a DTO annotated with `@NotNull`, `@NotBlank`, `@Valid`.
Catch `ValidationException` in `onException(ValidationException.class).handled(true)` and route immediately to an invalid-message channel without retrying.

### Q21. How do you transform complex XML/JSON payloads in Camel?

**Answer:**
- **JSON <-> POJO <-> JSON:** Jackson + **MapStruct** (type-safe, compiled Java code, fastest and easiest to test).
- **JSON <-> JSON (dynamic/configurable):** **Jolt** (`camel-jolt`) or **Jsonata** (`camel-jsonata`) where tenant mapping specs are stored in DB/config.
- **XML <-> XML / JSON:** **XSLT** (`camel-xslt-saxon`) or Freemarker/Velocity templates (`camel-freemarker`).

### Q22. How do you prevent memory leaks when using Camel's `DynamicRouter` or `toD()` (`toD("https://partner.com/orders/${header.orderId}")`)?

**Answer:** Each unique resolved URI in `toD()` creates a new `Endpoint` and `Producer` in the `CamelContext` cache. If `${header.orderId}` has millions of unique values, endpoint cache churn or unbounded caching degrades performance. Fix by:
1. Using `Exchange.HTTP_PATH` header with a static `.to("https://partner.com/orders")` endpoint, or
2. Enabling `allowOptimisedComponents=true` (default in modern Camel HTTP components, which automatically moves dynamic path/query parameters into headers), and bounding `cacheSize`.

---

## 3. Apache Kafka vs RabbitMQ & Event Streaming

### Q23. When designing an iPaaS, what criteria decide whether a flow uses Kafka or RabbitMQ?

**Answer:**
- **Choose Kafka when:** multiple independent downstream domains need the same event stream, historical event replay by offset/timestamp is required, throughput is 50k+ msgs/sec, or you are ingesting CDC streams (Debezium).
- **Choose RabbitMQ when:** you need complex routing (`topic`/`headers` exchanges), per-message TTL/delayed retries, individual message ack/nack out of order, priority queues, or low-latency command/task distribution.

### Q24. How does Kafka guarantee message ordering, and how can a producer break it?

**Answer:** Kafka guarantees ordering **only within a single partition**. All events for the same entity (e.g., `orderId` or `customerId`) must be published with that ID as the **partition key**. A producer can break ordering if `retries > 0` and `max.in.flight.requests.per.connection > 1` without idempotence. Fix by setting `enable.idempotence=true` (default in Kafka 3.x+), which guarantees ordering with up to 5 in-flight requests per connection.

### Q25. What do `acks=0`, `acks=1`, and `acks=all` (`-1`) mean in Kafka along with `min.insync.replicas`?

**Answer:**
- `acks=0`: producer does not wait for broker ack; data loss on any crash.
- `acks=1`: leader writes to its local log and acks before followers replicate; data loss if leader crashes before replication.
- `acks=all` + topic `min.insync.replicas=2` (with `replication.factor=3`): leader waits for at least 2 in-sync replicas (leader + 1 follower) to write the record before acknowledging. This is the standard production setting for zero data loss.

### Q26. How do manual offset commits work in Camel Kafka (`camel-kafka`) and Spring Kafka?

**Answer:** Disable `autoCommitEnable=false` (`enable.auto.commit=false`) and enable manual commits (`allowManualCommit=true` in Camel or `AckMode.MANUAL_IMMEDIATE` in Spring Kafka). Commit the offset only after the downstream target or database transaction succeeds, ensuring at-least-once processing.

### Q27. What is Kafka Log Compaction (`cleanup.policy=compact`), and when is it used in integration?

**Answer:** Log compaction retains at least the **latest value for every message key** rather than deleting all segments purely by age. Use it for reference/master data topics (e.g., `customer-master`, `product-catalog`, `currency-rates`) so new consumers can bootstrap the latest state of every entity by reading the topic from offset 0.

### Q28. What is Change Data Capture (CDC) with Debezium, and how is it used with MySQL and Oracle?

**Answer:** Debezium reads database transaction logs directly (MySQL binlog or Oracle LogMiner/XStream) and streams row-level `INSERT`, `UPDATE`, and `DELETE` events into Kafka topics in real time without polling overhead on the source database. It is used for zero-impact data synchronization, cache invalidation, and transactional outbox relaying.

### Q29. What happens if the number of consumers in a Kafka Consumer Group exceeds the number of partitions?

**Answer:** A partition can be assigned to only one consumer within the same consumer group at a time. Any extra consumers beyond the partition count sit completely **idle** as warm standbys. Therefore, partition count sets the upper bound on consumer parallelism.

### Q30. How do you handle large messages in Kafka?

**Answer:** Kafka's default `message.max.bytes` is 1 MB. Increasing it to 10–50 MB degrades broker page cache and causes consumer group rebalances. Instead:
1. Enable `compression.type=lz4` or `zstd` for text-heavy JSON/XML payloads.
2. Use the **Claim Check pattern** (store payload in Object Storage, send URI in Kafka) for payloads over 1 MB.

---

## 4. Redis for Caching, Locking & Rate Limiting

### Q31. What are the main caching patterns (Cache-Aside, Read-Through, Write-Through, Write-Behind)?

**Answer:**
- **Cache-Aside (Lazy Loading):** application checks Redis first; on miss, reads DB, populates Redis with TTL, and returns. Most common in Spring Boot (`@Cacheable`).
- **Write-Through:** writes update both cache and DB synchronously.
- **Write-Behind (Write-Back):** writes go to cache first and are flushed asynchronously in batches to DB.
- **Cache Invalidation:** evict or update Redis entries on DB updates or via CDC/events to prevent stale data.

### Q32. What are Cache Stampede, Cache Penetration, and Cache Avalanche, and how do you prevent them?

**Answer:**
- **Cache Stampede (Dogpiling):** a hot key expires and 1,000 concurrent threads miss the cache and hit Oracle/MySQL simultaneously. Prevent with mutex lock (`@Cacheable(sync=true)`), single-flight refresh, or early probabilistic expiration.
- **Cache Penetration:** requests for non-existent keys (`customerId = -999`) always miss Redis and hit the DB. Prevent by caching a short-lived null marker (`TTL = 60s`) or using a **Bloom Filter**.
- **Cache Avalanche:** thousands of keys set with the exact same TTL expire at the exact same second. Prevent by adding random jitter to TTLs (`baseTtl + random(0, 300s)`).

### Q33. Why are Lua scripts used in Redis for rate limiting and idempotency?

**Answer:** Redis executes a Lua script **atomically** on its single command execution thread. A check-and-decrement token bucket or check-and-delete lock release written in Lua cannot suffer race conditions between `GET` and `SET` across multiple microservice pods.

### Q34. What Redis eviction policies exist, and which should you use?

**Answer:**
- `volatile-lru` / `volatile-lfu`: evicts keys that have an expire (`TTL`) set.
- `allkeys-lru` / `allkeys-lfu`: evicts least recently/frequently used keys across all keys (good for pure caches).
- `noeviction`: returns an error when memory is full (required when Redis holds distributed locks or idempotency keys that must not be silently dropped before their TTL).

### Q35. Compare Redis Standalone, Redis Sentinel, and Redis Cluster.

**Answer:**
- **Standalone:** single node, no HA.
- **Sentinel:** primary + replicas with automatic failover; entire dataset fits in one node's RAM.
- **Redis Cluster:** shards data across 16,384 hash slots over multiple primary/replica nodes for horizontal memory and write scaling. Multi-key operations require hash tags (`{tenant1}:keyA`, `{tenant1}:keyB`) so keys land on the same slot.

### Q36. How do you size and tune Lettuce / Jedis connection pools in Spring Boot?

**Answer:** Spring Boot uses **Lettuce** by default, which relies on non-blocking Netty connections and shares a single multiplexed connection across threads for standard commands (pooling is only needed for blocking/transactional operations like `BLPOP` or `MULTI/EXEC`). Set fast command timeouts (`spring.data.redis.timeout=200ms`) and enable cluster topology refresh (`enable-periodic-refresh=true`, `enable-adaptive-refresh-trigger`) when running against Redis Cluster.

---

## 5. MySQL & Oracle Data Integration & Performance

### Q37. How do you diagnose and fix a slow SQL query in Oracle and MySQL?

**Answer:**
1. Run `EXPLAIN PLAN FOR ...` (`DBMS_XPLAN.DISPLAY` in Oracle) or `EXPLAIN ANALYZE` in MySQL 8+.
2. Check for full table scans (`TABLE ACCESS FULL` / `type: ALL`), high rows examined vs returned, temporary disk sorts (`Using filesort`, `Using temporary`), or implicit type conversions disabling indexes (e.g., comparing a `VARCHAR` column to a number).
3. Add composite covering indexes matching the `WHERE` equality columns first, then range/sort columns (leftmost prefix rule).

### Q38. What causes database deadlocks in high-concurrency integration consumers, and how do you eliminate them?

**Answer:** Two concurrent consumer transactions lock rows in opposite orders (Tx 1 locks Order A then Order B; Tx 2 locks Order B then Order A), or gap locks collide during concurrent `INSERT ... ON DUPLICATE KEY UPDATE`.
- **Fixes:**
  1. Sort batch items by primary/business key before executing updates/upserts so every transaction acquires row locks in the **exact same ascending order**.
  2. Keep transactions short and index foreign keys.
  3. Catch `CannotAcquireLockException` / `DeadlockLoserDataAccessException` and retry with jitter.

### Q39. Why is `OFFSET / LIMIT` pagination slow on millions of rows, and what is Keyset (Seek) Pagination?

**Answer:** `OFFSET 1000000 LIMIT 100` forces the database to scan and discard 1,000,000 rows before returning 100. **Keyset (Seek) Pagination** uses an indexed watermark:
```sql
SELECT id, updated_at, payload
FROM integration_events
WHERE (updated_at > :lastUpdatedAt)
   OR (updated_at = :lastUpdatedAt AND id > :lastId)
ORDER BY updated_at ASC, id ASC
FETCH FIRST 500 ROWS ONLY
```
This uses an index range scan with constant $O(\log N)$ latency whether on page 1 or page 100,000.

### Q40. How do you design high-volume audit and outbox tables in Oracle and MySQL so they don't degrade performance over time?

**Answer:**
- **Range Partitioning** by day/week/month on `created_at` so old data can be archived and dropped instantly via `ALTER TABLE DROP PARTITION` instead of slow row-by-row `DELETE` statements (which bloat undo/redo logs and fragment tablespaces).
- Keep minimal indexes on write-heavy outbox/audit tables.

### Q41. What is the difference between Read Replicas and Read-After-Write consistency issues?

**Answer:** When a service writes to the primary DB and immediately emits an event, a consumer reading from an asynchronous Read Replica milliseconds later may not see the row yet due to replication lag.
- **Fix:** Either include all required fields inside the event payload (Event-Carried State Transfer) or route critical immediate reads to the Primary datasource (`@Transactional(readOnly = false)` with `AbstractRoutingDataSource`).

### Q42. How do you handle character encoding and timezone consistency between Java, MySQL, and Oracle?

**Answer:**
- Store all timestamps in **UTC** (`Instant` or `OffsetDateTime` in Java; `TIMESTAMP WITH TIME ZONE` in Oracle; `DATETIME(6)` / `TIMESTAMP(6)` with `connectionTimeZone=UTC` in MySQL).
- Use `utf8mb4` in MySQL (never legacy 3-byte `utf8`, which fails on 4-byte Unicode characters) and `AL32UTF8` in Oracle.

### Q43. How do you design a zero-data-loss database cutover or synchronization validation?

**Answer:**
1. Backfill historical rows in batches using keyset pagination while capturing ongoing changes via CDC (Debezium).
2. Run a shadow reconciliation validator comparing row counts and rolling SHA-256 hashes per time bucket between MySQL and Oracle.
3. Apply captured CDC deltas until lag is near zero, switch traffic via feature flag, and keep reverse CDC active for instant rollback if needed.

### Q44. How do you prevent connection pool starvation when calling external APIs during database operations?

**Answer:** Never hold a DB connection (`@Transactional`) across an HTTP, SOAP, or SFTP call. Split into three steps:
1. Transaction 1 (milliseconds): read data and mark status `PROCESSING` -> commit & release DB connection.
2. Non-transactional: execute remote HTTP/Camel call (with timeout & retries).
3. Transaction 2 (milliseconds): update status `COMPLETED` or `FAILED` -> commit.

---

## 6. Csmart, API Governance & Security Architecture

### Q45. How do you define the boundary between Csmart, the iPaaS layer, and External Systems?

**Answer:**
- **Csmart (System of Record / Core Product):** owns customer lifecycle, billing/catalog domain rules, core state transitions, and exposes clean product APIs and domain events.
- **iPaaS Layer:** owns connectivity adapters, data transformation (`Canonical <-> Partner`), content-based routing, protocol bridging (REST <-> AMQP/Kafka/SFTP/SOAP), partner rate limiting, retries, DLQs, and integration observability.
- **Why this matters:** Csmart stays 100% customer-agnostic and upgradable, while customer-specific ERP/CRM quirks are isolated in configurable iPaaS routes and connectors.

### Q46. How do you design multi-tenancy in an iPaaS platform?

**Answer:**
- **Tenant Context Propagation:** extract `X-Tenant-Id` from the verified JWT/mTLS certificate at the gateway, store it in a `ScopedValue`/MDC and message headers, and enforce tenant isolation on every DB query, Redis key (`tenant:{id}:...`), and queue.
- **Tiered Isolation (Silo vs Pool):** shared runtime pool with per-tenant rate limits for standard tenants; dedicated queues, worker pods, and vhosts for high-volume enterprise tenants.

### Q47. How do you design an Enterprise API Gateway + Backend Service security architecture?

**Answer:**
- **Edge (API Gateway):** TLS 1.3 / mTLS termination, WAF, DDoS/rate limiting, OAuth2/OIDC token signature & expiry validation, and stripping spoofed internal headers.
- **Internal (Zero Trust):** forward the verified JWT (or exchange it via OAuth2 Token Exchange RFC 8693) so downstream Spring Boot services verify method-level scopes/roles (`@PreAuthorize`) and tenant ownership (preventing Broken Object Level Authorization / BOLA).

### Q48. What are the OWASP API Security Top 10 risks most relevant to integration platforms?

**Answer:**
1. **BOLA (Broken Object Level Authorization):** Tenant A passes Tenant B's `orderId` in `/api/orders/{id}` and accesses Tenant B's data. Always filter queries by `WHERE id = :id AND tenant_id = :jwtTenantId`.
2. **SSRF (Server-Side Request Forgery):** Tenant configures a webhook URL pointing to `http://169.254.169.254` (cloud metadata) or internal `10.x.x.x` services. Block private IP ranges and resolve DNS safely in outbound webhook connectors.
3. **Unrestricted Resource Consumption:** Missing payload size limits, pagination bounds, or rate limits.

### Q49. How do you govern OpenAPI and AsyncAPI specifications across engineering teams?

**Answer:** Follow an **API-First / Contract-First** workflow:
1. Author OpenAPI 3.1 (REST) and AsyncAPI (RabbitMQ/Kafka events) specs in Git.
2. Lint automatically in CI with **Spectral** (enforcing naming conventions, pagination, `ProblemDetail` error responses, and security schemes).
3. Run `openapi-diff` in PR checks to block unintended breaking changes.
4. Generate server interfaces and client DTOs during the Maven/Gradle build.

### Q50. What is an Architecture Decision Record (ADR), and what should it contain?

**Answer:** An ADR is a concise markdown document stored in the repo (`docs/adr/0012-use-quorum-queues-for-order-events.md`) capturing:
- **Title & Status** (Proposed, Accepted, Superseded)
- **Context & Problem Statement** (forces, constraints, SLAs)
- **Options Considered** (with pros/cons of each)
- **Decision** (what was chosen and why)
- **Consequences** (trade-offs accepted, operational impact, follow-up tasks)
ADRs prevent teams from relitigating past decisions and onboard new engineers quickly.
