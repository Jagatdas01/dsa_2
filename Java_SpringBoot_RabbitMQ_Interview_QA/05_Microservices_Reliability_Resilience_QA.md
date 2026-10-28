# Microservice Architecture, Reliability and Resilience: Interview Question Set

Target: 6 to 10 years experience, integration architecture role.

## Index

1. Microservice Fundamentals (Q1 to Q6)
2. Communication and Service Discovery (Q7 to Q11)
3. Data Management and Consistency (Q12 to Q17)
4. Resilience Patterns (Q18 to Q27)
5. Failure Handling and Operations (Q28 to Q33)
6. Observability and SLOs (Q34 to Q39)
7. Deployment, Scaling and Disaster Recovery (Q40 to Q46)
8. Governance and Architecture Decisions (Q47 to Q50)
9. High-Frequency Architect & Production Incident Scenarios (Q51 to Q63)

---

## 1. Microservice Fundamentals

### Q1. What are microservices, and what problem do they solve?

**Answer:** Small, independently deployable services, each owning one business capability and its data. They solve scaling and team autonomy problems. They add distributed system complexity: network failures, data consistency, and operational overhead. Adopt them when the monolith slows delivery or needs independent scaling.

### Q2. How do you decide service boundaries?

**Answer:** Use Domain-Driven Design. Find bounded contexts with the business experts, and align services to them. Signs of a wrong boundary: a change needs coordinated releases of several services, services share a database table, or one request calls many services in a chain. Start coarse and split later.

### Q3. What are the 12-factor app principles relevant to microservices?

**Answer:** Key ones: codebase in version control, explicit dependencies, config in the environment, backing services as attached resources, stateless processes, port binding, concurrency through process scaling, disposability (fast startup, graceful shutdown), dev/prod parity, logs as event streams, and admin tasks as one-off processes.

### Q4. What is the difference between synchronous and asynchronous communication?

**Answer:** Synchronous (HTTP, gRPC) couples caller and callee availability: if the callee is down, the call fails. Asynchronous (messaging, events) decouples them in time and absorbs load spikes, but adds eventual consistency and harder debugging. Prefer asynchronous for commands that do not need an immediate answer.

### Q5. What is the "distributed monolith" anti-pattern?

**Answer:** Services that are deployed separately but must be deployed together, share a database, or call each other in long synchronous chains. They have all the costs of microservices and none of the benefits. Fix by clarifying boundaries and removing synchronous coupling.

### Q6. How do you manage shared libraries across microservices?

**Answer:** Keep shared libraries small and stable: API contracts, client SDKs, logging and tracing config, common error model. Do not share domain models across services. Version libraries and upgrade gradually. Avoid putting business rules in a shared library, because it couples all services to one release cycle.

---

## 2. Communication and Service Discovery

### Q7. How does service discovery work?

**Answer:** Services register their address with a registry (Eureka, Consul) or rely on platform DNS (Kubernetes Services). Clients resolve a logical name to available instances. Use client-side load balancing (Spring Cloud LoadBalancer) or server-side (Kubernetes Service, service mesh). Health checks remove failed instances.

### Q8. What is an API gateway, and what should it not do?

**Answer:** It handles cross-cutting concerns: routing, TLS, authentication, rate limiting, and request logging. It should not contain business logic or orchestrate complex workflows. Keep gateway configuration in code, with the same review process as services.

### Q9. What is a service mesh, and what does it provide?

**Answer:** A sidecar proxy (Envoy in Istio or Linkerd) runs next to each service and handles mTLS, retries, timeouts, load balancing, and telemetry without code changes. Use it for consistent policies. Costs: added latency, resource use, and operational complexity. Do not rely on mesh retries and application retries together without a plan, because retries multiply.

### Q10. What is gRPC, and when would you use it instead of REST?

**Answer:** gRPC uses HTTP/2 and Protobuf, giving compact messages, streaming, and strong contracts. Use it for internal, high-volume, low-latency calls between services. Use REST for public APIs, browser clients, and broad tooling support.

### Q11. How do you version APIs between services without breaking consumers?

**Answer:** Follow the Postel principle: be liberal in what you accept, conservative in what you send. Add optional fields, never change meaning. Use consumer-driven contract tests to see which consumers depend on what. Introduce a new version for breaking changes and deprecate the old one with a timeline.

---

## 3. Data Management and Consistency

### Q12. What does "database per service" mean, and why does it matter?

**Answer:** Each service owns its schema, and other services access its data only through its API or events. This lets services change their schema independently. Shared databases create hidden coupling and make independent deployment impossible.

### Q13. What is the CAP theorem, and how does it apply in practice?

**Answer:** During a network partition, a distributed system must choose between consistency (all nodes see the same data) and availability (every request gets a response). Partitions are unavoidable in distributed systems, so the real choice is CP or AP during partitions. PACELC extends this: even without partitions, you trade latency against consistency.

### Q14. What is eventual consistency, and how do you design for it?

**Answer:** After an update, replicas and derived views converge to the same value over time, not immediately. Design for it:
- Show status to users ("processing") instead of assuming completion.
- Make operations idempotent.
- Use version numbers to detect stale events.
- Provide reconciliation jobs for drift.

### Q15. How do you handle transactions that span multiple services?

**Answer:** Avoid two-phase commit (2PC) because it blocks and couples services. Use sagas with compensating actions (see the design patterns question set), plus the transactional outbox for reliable event publishing. Accept that intermediate states exist and expose them clearly.

### Q16. How do you query data that spans multiple services?

**Answer:** Options: API composition (a gateway or BFF calls services and merges results), CQRS with a read model updated by events, or a data replication pattern. Avoid joins across service databases. Choose based on latency, freshness, and volume.

### Q17. What are schema migration risks in a microservice platform, and how do you manage them?

**Answer:** Rolling deployments run old and new versions at once. Use expand and contract: add the new column or table first, make code write both, migrate data, switch reads, then remove the old column in a later release. Never rename or drop columns in one release.

---

## 4. Resilience Patterns

### Q18. What is resilience, and what are its main building blocks?

**Answer:** The ability to keep working or recover quickly when parts fail. Building blocks:
- Timeouts (bound waiting).
- Retries with backoff and jitter (recover from transient faults).
- Circuit breakers (stop calling a failing dependency).
- Bulkheads (isolate resources).
- Rate limiters (protect callee and caller).
- Fallbacks (degraded but useful response).
- Load shedding (reject excess work early).

### Q19. What is a timeout budget, and why do you need one?

**Answer:** A user request has an overall deadline. Each downstream call gets a share of the remaining time, and the total must fit within the deadline. Without budgets, each service waits its full default timeout and the total latency explodes. Propagate the deadline in a header (for example, gRPC deadline or a custom `X-Deadline` header) and check it before each call.

### Q20. How do you implement retries correctly?

**Answer:**
- Retry only transient errors (timeouts, 503, 429, connection reset).
- Retry only idempotent operations, or use idempotency keys.
- Use exponential backoff with jitter (randomized delay) to avoid retry storms.
- Limit total attempts.
- Use a retry budget (for example, retries cannot exceed 10 percent of traffic).
- Do not retry at every layer. Choose one layer for application retries.

### Q21. What is a circuit breaker, and what are its states?

**Answer:**
- **Closed:** calls pass through. Failures are counted in a sliding window.
- **Open:** calls fail fast (or fall back) for a wait duration after the failure rate passes a threshold.
- **Half-open:** a limited number of trial calls test recovery. Success closes the circuit; failure reopens it.

Track slow calls too, not only errors, because slow dependencies cause cascading failures.

### Q22. How do you configure Resilience4j in Spring Boot?

**Answer:**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      erpClient:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 50
        minimumNumberOfCalls: 20
        failureRateThreshold: 50
        slowCallDurationThreshold: 2s
        slowCallRateThreshold: 80
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 5
  retry:
    instances:
      erpClient:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.io.IOException
  timelimiter:
    instances:
      erpClient:
        timeoutDuration: 3s
```

Explain each value in the interview: the window size and minimum calls prevent the breaker from opening on too little data.

### Q23. How do you use the Resilience4j annotations together?

**Answer:**

```java
@CircuitBreaker(name = "erpClient", fallbackMethod = "orderFallback")
@Retry(name = "erpClient")
@TimeLimiter(name = "erpClient")
public CompletableFuture<OrderResponse> createOrder(OrderRequest request) {
    return CompletableFuture.supplyAsync(() -> erpClient.post(request), executor);
}

private CompletableFuture<OrderResponse> orderFallback(OrderRequest request, Throwable ex) {
    return CompletableFuture.completedFuture(OrderResponse.queuedForLater(request.id()));
}
```

Aspect order matters. Resilience4j applies Retry around CircuitBreaker by default, so the breaker sees each attempt. Check the order in the library documentation before relying on it.

### Q24. What is the bulkhead pattern?

**Answer:** Isolate resources per dependency so one slow dependency cannot exhaust shared resources. Types:
- **Thread pool bulkhead:** separate executor per dependency.
- **Semaphore bulkhead:** limit concurrent calls on the calling thread.
- **Connection pool per dependency:** separate HTTP client pools.

Example: a slow CRM should not consume all threads needed for ERP calls.

### Q25. What is a rate limiter, and how does it differ from throttling?

**Answer:** Rate limiting rejects calls above a set rate (for example, 100 per second) and returns 429. Throttling slows processing to match a rate, often by queueing. Use rate limiting at the gateway for clients, and client-side rate limiting to respect a downstream provider's quota.

### Q26. What is a fallback, and what should it do?

**Answer:** A fallback provides an alternative when a call fails: cached data, default value, queued request, or a clear error. Choose based on the business need. Never return stale data for a financial balance without marking it as stale. Make fallbacks observable, with metrics and logs, so degraded mode is visible.

### Q27. How do you prevent cascading failures across a microservice chain?

**Answer:** Combine these measures:
- Short timeouts with budgets.
- Circuit breakers on every outbound call.
- Bulkheads per dependency.
- Load shedding when the queue or thread pool is full.
- Back-pressure through bounded queues.
- Asynchronous buffering for non-urgent work.
- Health checks that do not depend on downstream systems in liveness.

---

## 5. Failure Handling and Operations

### Q28. What are the common types of failures in distributed systems?

**Answer:** Network partition, latency spikes, partial failure (some instances fail), slow dependencies, data corruption, clock skew, message loss or duplication, and resource exhaustion (threads, connections, disk). Design tests for each one.

### Q29. How do you handle message duplication in a microservice platform?

**Answer:** Assume at-least-once delivery. Use idempotency keys from the caller, store processed keys with the business change, and reject or return the original result for duplicates. Make operations naturally idempotent where possible (set a value instead of increment, or use upsert).

### Q30. What is a dead letter queue strategy for microservices?

**Answer:** Every consumer has a DLQ. Messages that fail after retries are parked with the error and metadata. Operators can inspect, fix, and replay them. Alert on DLQ depth and age. Define an owner and a runbook for each DLQ.

### Q31. How do you implement health checks for microservices?

**Answer:** Liveness: is the process alive and not deadlocked (restart if failing). Readiness: can it accept traffic (remove from load balancer if failing). Startup: allow slow startup before liveness takes effect. Keep dependency checks in readiness, not liveness, to avoid restart storms during downstream outages.

### Q32. What is a graceful shutdown, and why does it matter?

**Answer:** On termination, the service stops accepting new work, completes in-flight requests and messages, and then exits. Use a preStop hook or readiness change so traffic stops before shutdown. Set `terminationGracePeriodSeconds` in Kubernetes high enough for in-flight work. Without this, deployments cause errors and duplicate processing.

### Q33. How do you run chaos engineering experiments?

**Answer:** Define a steady state (for example, order success rate above 99 percent). Form a hypothesis ("if the CRM is slow, orders still complete via fallback"). Inject faults (latency, errors, pod kills, network partitions) using Chaos Mesh, Litmus, or Gremlin, starting in staging. Measure, learn, and fix. Run game days with the operations team.

---

## 6. Observability and SLOs

### Q34. What are the three pillars of observability?

**Answer:**
- **Logs:** discrete events, structured (JSON), with correlation IDs.
- **Metrics:** aggregated numbers over time (rates, latency percentiles, errors).
- **Traces:** the path of a request across services with timing per span.

Add profiling and events to the mix for deeper analysis.

### Q35. What are the RED and USE methods?

**Answer:**
- **RED** (for services): Rate (requests per second), Errors (failed requests), Duration (latency distribution).
- **USE** (for resources): Utilization, Saturation, Errors (for CPU, memory, disk, network, connection pools).

Use RED for each endpoint or message consumer, and USE for infrastructure.

### Q36. What is distributed tracing, and how does context propagate?

**Answer:** Each request gets a trace ID. Each operation is a span with a parent span ID. Context is propagated in headers (W3C `traceparent`) over HTTP and in message headers for RabbitMQ and Kafka. Sampling controls cost: use head-based sampling for steady traffic and tail-based sampling to keep errors and slow traces.

### Q37. What is the difference between SLI, SLO, and SLA?

**Answer:**
- **SLI** (indicator): a measured value, for example, percentage of requests under 300 ms.
- **SLO** (objective): the target for the SLI, for example, 99.9 percent over 30 days.
- **SLA** (agreement): a contract with consequences if the target is missed.

Set SLOs internally stricter than SLAs.

### Q38. What is an error budget, and how is it used?

**Answer:** The allowed amount of unreliability: 100 percent minus the SLO. For a 99.9 percent SLO over 30 days, the error budget is about 43 minutes. When the budget is consumed, the team slows feature releases and focuses on reliability. This turns reliability into a shared decision rather than a debate.

### Q39. What alerts should be based on SLOs?

**Answer:** Use burn rate alerts. Fast burn (for example, consuming 2 percent of the monthly budget in 1 hour) pages on-call. Slow burn (consuming it over days) creates a ticket. Avoid alerts on raw CPU or single errors that do not affect users.

---

## 7. Deployment, Scaling and Disaster Recovery

### Q40. Compare blue-green, canary, and rolling deployments.

**Answer:**
- **Blue-green:** two full environments, switch traffic all at once. Fast rollback, double cost during switch.
- **Canary:** send a small percentage of traffic to the new version, increase gradually while watching metrics. Limits blast radius.
- **Rolling:** replace instances gradually. Simple, but old and new versions run together, so compatibility matters.

### Q41. How do you scale microservices in Kubernetes?

**Answer:** Horizontal Pod Autoscaler on CPU, memory, or custom metrics (request rate, queue depth via KEDA). Set resource requests accurately, because the scheduler and HPA use them. Use PodDisruptionBudgets to keep capacity during node maintenance. Set minimum replicas for availability.

### Q42. How do you isolate noisy workloads in Kubernetes?

**Answer:** Use separate namespaces with ResourceQuotas, LimitRanges, node pools or taints for batch workloads, and PriorityClasses so critical services get scheduled first. Use network policies to limit traffic between namespaces.

### Q43. What are RTO and RPO?

**Answer:**
- **RTO** (Recovery Time Objective): maximum acceptable time to restore service after a disaster.
- **RPO** (Recovery Point Objective): maximum acceptable data loss, measured in time.

Example: RTO 1 hour, RPO 5 minutes. These drive backup frequency, replication mode, and the DR design.

### Q44. What disaster recovery strategies exist?

**Answer:**
- **Backup and restore:** cheapest, longest RTO.
- **Pilot light:** minimal core running in the DR region, scaled up when needed.
- **Warm standby:** scaled-down full copy running.
- **Active-active:** full traffic in multiple regions. Lowest RTO, highest complexity and data consistency challenges.

Test failover regularly. An untested DR plan is not a plan.

### Q45. How do you design multi-region messaging?

**Answer:** Use federation or shovel between brokers in different regions. Keep the primary region for writes, and replicate events asynchronously. Define how duplicates and conflicts are resolved after failover. Make consumers idempotent. Document the failover runbook, including how the DLQ and in-flight messages are handled.

### Q46. How do you handle a regional outage for an integration platform?

**Answer:** Detect with health checks and synthetic monitoring. Fail over DNS or the global load balancer to the healthy region. Confirm the RPO by checking replication lag at the time of failure. Replay events that were not replicated if the source is available. Notify tenants and record the incident for a post-incident review.

---

## 8. Governance and Architecture Decisions

### Q47. How do you govern API and event contracts across teams?

**Answer:** Maintain a contract registry (OpenAPI for REST, AsyncAPI for events, schema registry for Avro and Protobuf). Require contract tests in CI for both providers and consumers. Review breaking changes in architecture review. Assign an owner for each contract.

### Q48. How do you run an architecture review for a new microservice or integration?

**Answer:** Use a checklist:
- Business capability and boundary.
- Data ownership and consistency model.
- Failure modes and resilience settings (timeouts, retries, circuit breakers).
- Security (authentication, authorization, secrets, data classification).
- Observability (logs, metrics, traces, alerts, SLOs).
- Scaling and capacity estimates.
- Deployment and rollback plan.
- Operational runbooks and ownership.

Record the outcome in an ADR.

### Q49. How do you reduce technical debt in a microservice platform?

**Answer:** Track debt in a visible backlog with impact. Allocate a fixed percentage of each sprint (for example, 15 to 20 percent) for debt work. Prioritize debt that affects reliability, security, or delivery speed. Remove unused services and dead code. Upgrade dependencies on a regular schedule, not only when forced.

### Q50. How would you explain the trade-off between microservices and a modular monolith to a stakeholder?


---

## 9. High-Frequency Architect & Production Incident Scenarios

### Q51. What is a Retry Storm (Thundering Herd), and how do you prevent it?

**Answer:** When a downstream service recovers from a brief outage, thousands of waiting clients retry at the exact same millisecond, immediately crashing the service again. Prevent it with:
1. **Exponential backoff + full jitter:** `sleep = random(0, min(cap, base * 2^attempt))`.
2. **Circuit Breakers:** fail fast while the dependency is unhealthy and allow only a few probe calls in `HALF_OPEN`.
3. **Retry Budgets:** cap retries at e.g. 10% of total requests across the client pool.
4. **Single-layer retries:** never stack gateway retries + service mesh retries + Resilience4j retries ($3 \times 3 \times 3 = 27$ calls per failure).

### Q52. What is the correct execution order when combining Resilience4j decorators?

**Answer:** From outermost to innermost around the remote call:
`Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function ) ) ) ) )`
- **Bulkhead / TimeLimiter** sit closest to the call so each individual attempt is bounded in concurrency and time.
- **CircuitBreaker** sits inside **Retry** so every timed-out or failed attempt is recorded by the breaker, and if the breaker opens, `CallNotPermittedException` stops further retries immediately.

### Q53. How do you design an Idempotent POST / Payment / Order API end-to-end?

**Answer:**
1. Client sends an `Idempotency-Key` header (UUID) with the request.
2. Service computes a hash of the request payload and inserts `(idempotency_key, tenant_id, request_hash, status='IN_PROGRESS')` into an idempotency store (Oracle/MySQL with unique constraint or Redis `SET NX`).
3. If the key already exists:
   - If payload hash differs -> return `422 Unprocessable Entity` (key reuse with different body).
   - If `status == 'IN_PROGRESS'` -> return `409 Conflict` (concurrent duplicate in flight).
   - If `status == 'COMPLETED'` -> return the stored response status and body without re-executing.
4. Execute business logic and update the idempotency record to `COMPLETED` with the serialized response in the same local DB transaction.

### Q54. How do you secure incoming Webhooks from external partners?

**Answer:**
- Verify **HMAC-SHA256 signature** over `timestamp + "." + raw_payload` using a shared secret per partner.
- Validate the timestamp header (e.g., reject if older than 5 minutes) to prevent **replay attacks**.
- Deduplicate using the webhook event ID in Redis/DB.
- Enforce mTLS or IP allow-listing where supported.
- Acknowledge quickly (`202 Accepted`) after persisting to a durable queue (RabbitMQ/Kafka) so slow downstream processing never causes the partner to time out.

### Q55. How do you protect external partners or legacy ERP systems that have strict rate limits (e.g., 20 req/sec) when your platform receives 2,000 req/sec spikes?

**Answer:** Use **Queue-Based Load Leveling**:
1. Ingest the 2,000 req/sec spike into a durable RabbitMQ queue (or Kafka topic) and return `202 Accepted` (or process asynchronously).
2. Configure consumer concurrency, `prefetchCount`, and Camel `.throttle(20).timePeriodMillis(1000)` / Resilience4j `RateLimiter` on the egress worker.
3. If the partner returns `429 Too Many Requests`, inspect the `Retry-After` header, pause consumption on that route via Camel's `ControlBus`, or route to a delayed retry queue.

### Q56. What is the difference between Liveness, Readiness, and Startup probes in Kubernetes, and how can a misconfigured Liveness probe cause a total outage?

**Answer:**
- **Startup probe:** waits for slow JVM/Spring startup to finish before enabling liveness checks.
- **Liveness probe:** checks if the JVM itself is wedged/deadlocked; failure **restarts the Pod**.
- **Readiness probe:** checks if the Pod is ready to serve traffic; failure removes the Pod from the Service endpoints without restarting it.
- **Fatal mistake:** Putting a database or RabbitMQ health check inside the **Liveness** probe. If the DB blips for 10 seconds, Kubernetes restarts every Pod simultaneously, causing a cold-start stampede that takes down the entire cluster.

### Q57. How do you handle distributed locking safely, and what is the token/fencing problem?

**Answer:** If Pod A acquires a Redis lock with a 10-second TTL, pauses for a 12-second GC pause, the lock expires and Pod B acquires it. When Pod A wakes up, it thinks it still holds the lock and corrupts data.
- **Mitigation:**
  1. Never delete a lock without checking ownership (use a Lua script that deletes only if `lockValue == myUUID`, which Redisson does automatically).
  2. Use a **fencing token** (monotonically increasing sequence number) validated by the target storage or optimistic locking (`@Version` column in the database) as the final guard.

### Q58. How do you design zero-downtime secrets rotation (DB passwords, RabbitMQ credentials, OAuth client secrets)?

**Answer:** Use a dual-credential (overlapping validity) rotation model:
1. Create credential B in the target system while credential A remains active.
2. Update Vault / K8s Secret with credential B.
3. Refresh the connection pool (e.g., HikariCP soft eviction of old connections via `HikariConfigMXBean` or rolling pod restart).
4. Verify zero active connections using credential A, then revoke credential A.

### Q59. What is Tail Latency Amplification in microservice fan-out, and how do you reduce it?

**Answer:** If a single request calls 10 downstream services in parallel and each service has a 99th percentile ($p99$) latency of 1 second, the probability that all 10 finish under 1 second is $0.99^{10} \approx 90.4\%$, meaning ~10% of user requests see high latency. Reduce it by:
- Avoiding deep synchronous fan-out (pre-aggregate read models via CQRS/events).
- Caching reference data in Redis/local Caffeine cache.
- Setting strict per-branch timeouts with partial degradation fallbacks.
- Hedged requests for idempotent reads where appropriate.

### Q60. What is Backpressure, and how does it work across HTTP, RabbitMQ, and Kafka?

**Answer:** Backpressure prevents fast producers from overwhelming slow consumers:
- **RabbitMQ:** consumer sets `basic.qos` (`prefetchCount`); broker stops pushing once unacked messages reach `prefetchCount`. If the queue fills memory/disk thresholds, broker flow control blocks the publisher TCP socket.
- **Kafka:** pull-based model; consumers pull at their own pace (`max.poll.records`).
- **HTTP / Reactive / Thread Pools:** bounded queues + `CallerRunsPolicy` or returning `429 Too Many Requests` / `503 Service Unavailable` with `Retry-After`.

### Q61. How do you conduct a blameless Post-Incident Review (Postmortem) after an integration outage?

**Answer:** Document:
1. **Timeline:** exact UTC timestamps of fault onset, alert firing, detection (MTTD), mitigation (MTTR), and full recovery.
2. **Root Cause & Contributing Factors:** use the "5 Whys" focusing on system/process gaps (e.g., missing DLQ alert, unbounded retry), never blaming individuals.
3. **Customer Impact:** messages delayed, SLA/error budget consumed.
4. **Action Items:** concrete preventative engineering tasks (with owners and due dates) categorized into Detection, Mitigation, and Prevention.

### Q62. How do you enforce architectural boundaries between Csmart (core product) and the iPaaS layer?

**Answer:**
- **Csmart owns:** domain state, business rules, domain validation, and canonical business events/APIs.
- **iPaaS owns:** protocol mediation (REST/SOAP/SFTP/AMQP/Kafka), customer-specific field mapping, content-based routing, enrichment orchestration, partner authentication, and delivery retries/DLQs.
- **Rule of thumb:** If a rule applies regardless of which external partner is connected, it belongs in Csmart. If a rule exists because Partner X sends XML dates in `MM/dd/yyyy` or requires a 3-step token handshake, it belongs in the iPaaS.

**Answer:** Microservices give independent scaling and team autonomy, but cost more to operate and debug. A modular monolith gives simpler operations and strong consistency, but scales as one unit. Recommend based on team size, load profile, change frequency, and operational maturity. Often, start with a modular monolith and split services where the boundaries are proven.

### Q63. How do two microservices communicate with each other?

**Answer:** There are two main styles: **synchronous** (request/response, the caller waits) and **asynchronous** (message/event based, the caller does not wait).

**1. Synchronous communication**

| Option | How it works | When to use |
|---|---|---|
| **REST over HTTP** (JSON) | Service A calls `GET/POST` on Service B using `RestClient`, `WebClient`, `RestTemplate`, or OpenFeign. | Simple queries, CRUD, public/partner APIs. Most common choice. |
| **gRPC** (HTTP/2 + Protobuf) | Strongly typed contract (`.proto`), binary payload, streaming support. | Low-latency internal calls, high throughput, polyglot services. |
| **GraphQL** | Client asks for exactly the fields it needs. | Aggregating data for UIs / BFF layers. |

Pros: simple, immediate response, easy to debug. Cons: **temporal coupling** (if B is down, A fails), latency adds up in call chains, risk of cascading failures.

**2. Asynchronous communication**

| Option | How it works | When to use |
|---|---|---|
| **Message broker queues** (RabbitMQ, ActiveMQ, SQS) | A publishes a command/message to a queue; B consumes it later and acks. | Work distribution, commands, retries, DLQ handling. |
| **Event streaming** (Kafka, RabbitMQ Streams) | A publishes events (`OrderCreated`) to a topic; any number of services subscribe and can replay. | Event-driven architecture, audit, analytics, multiple consumers. |
| **Async request/reply** | A sends a request with `replyTo` + `correlationId`; B replies on a reply queue. | Long-running work where the caller still needs a result. |

Pros: loose coupling, B can be down temporarily, absorbs load spikes, easy fan-out. Cons: eventual consistency, harder tracing, duplicates and ordering must be handled (idempotent consumers).

**3. How a service finds the other one (service discovery)**
- **Kubernetes DNS / Service:** call `http://order-service:8080` and K8s load-balances across pods (most common today).
- **Registry-based:** Eureka / Consul with client-side load balancing (Spring Cloud LoadBalancer).
- **API Gateway** for external clients; **service mesh** (Istio/Linkerd) for mTLS, retries, and traffic policies between services.

**4. What must be in place for reliable communication**
- **Timeouts** on every remote call, plus **retries with backoff + jitter** only for idempotent operations.
- **Circuit breaker, bulkhead, fallback** (Resilience4j) to prevent cascading failures.
- **Security:** OAuth2 client-credentials / JWT propagation, or mTLS via the service mesh.
- **Observability:** propagate `traceparent` and `X-Correlation-Id` headers across HTTP and messages.
- **Contracts:** OpenAPI for REST, AsyncAPI/schema registry for events, with backward-compatible versioning.
- **Consistency across services:** Saga pattern + Transactional Outbox instead of distributed (XA) transactions.

**How to choose:**
- Need an immediate answer to continue (e.g., check stock before confirming the order)? Use **sync REST/gRPC** with timeouts and a circuit breaker.
- Notifying others that something happened, or the work can finish later (e.g., send email, update inventory, sync to ERP)? Use **async messaging/events**.
- Real systems usually combine both: a sync API at the edge, then async events between internal services.

**Example (Spring Boot):**
```java
// Synchronous: OpenFeign client with Resilience4j circuit breaker
@FeignClient(name = "inventory-service", url = "${inventory.url}")
interface InventoryClient {
    @GetMapping("/inventory/{sku}")
    StockResponse getStock(@PathVariable String sku);
}

@Service
class OrderService {
    private final InventoryClient inventory;
    private final RabbitTemplate rabbit;

    OrderService(InventoryClient inventory, RabbitTemplate rabbit) {
        this.inventory = inventory;
        this.rabbit = rabbit;
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "stockUnknown")
    public Order place(OrderRequest req) {
        StockResponse stock = inventory.getStock(req.sku());          // sync call
        if (!stock.available()) throw new OutOfStockException(req.sku());
        Order order = save(req);

        // Asynchronous: publish an event; payment/notification services consume it
        rabbit.convertAndSend("orders.x", "order.created",
                new OrderCreatedEvent(order.id(), order.sku(), order.amount()));
        return order;
    }

    Order stockUnknown(OrderRequest req, Throwable t) {
        throw new ServiceUnavailableException("Inventory unavailable, retry later");
    }
}

// Consumer side in another microservice
@RabbitListener(queues = "payment.order-created.q")
void onOrderCreated(OrderCreatedEvent event) {
    paymentService.charge(event);                                     // idempotent handler
}
```
