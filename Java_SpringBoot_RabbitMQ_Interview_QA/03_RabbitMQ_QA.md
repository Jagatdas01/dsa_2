# RabbitMQ: Interview Question Set

Target: 6 to 10 years experience, integration architecture role.

## Index

1. Core Model (Q1 to Q6)
2. Queue Types (Q7 to Q11)
3. Reliability and Delivery Guarantees (Q12 to Q17)
4. Routing, TTL and Dead Lettering (Q18 to Q23)
5. Operations, Scaling and Topology (Q24 to Q30)
6. Spring AMQP and Application Patterns (Q31 to Q37)
7. Design Decisions (Q38 to Q40)
8. Frequently Asked Production & Troubleshooting Scenarios (Q41 to Q52)
9. Durability, Exactly-Once Processing and Acknowledgements (Q53 to Q55)

---

## 1. Core Model

### Q1. Explain the AMQP 0-9-1 model used by RabbitMQ.

**Answer:** Producers publish to exchanges. Exchanges route messages to queues based on bindings and routing keys. Consumers subscribe to queues. Connections are TCP sockets. Channels are lightweight virtual connections multiplexed over one connection. Use channels for concurrency, not extra connections.

### Q2. What are virtual hosts (vhosts), and why use them?

**Answer:** A vhost is a logical namespace with its own exchanges, queues, bindings, and permissions. Use separate vhosts per environment or per tenant tier to isolate names and access. Vhosts do not isolate resources such as CPU or memory, so use limits or separate clusters for noisy tenants.

### Q3. How do users and permissions work?

**Answer:** Each user has permissions on a vhost, expressed as regex patterns for `configure`, `write`, and `read`. Give each service its own user with the minimum patterns it needs (for example, `write` only to `orders.exchange` and `read` only from `orders.*.q`). Disable the default `guest` user for remote access.

### Q4. What is a binding, and how does a routing key work with each exchange type?

**Answer:** A binding links an exchange to a queue with a binding key. For direct exchanges, the routing key must equal the binding key. For fanout, the key is ignored. For topic exchanges, the binding key can use `*` (one word) and `#` (zero or more words). Headers exchanges match on headers using `x-match=all` or `any`.

### Q5. What is the default exchange?

**Answer:** The unnamed default exchange is a direct exchange. Every queue is automatically bound to it with the queue name as the routing key. Use it for simple point-to-point sends, but use named exchanges for real integration flows.

### Q6. What is an alternate exchange?

**Answer:** An alternate exchange receives messages that the main exchange cannot route to any queue. Set it with the `alternate-exchange` argument. Use it to capture unroutable messages for audit or alerting, instead of losing them silently or relying only on publisher returns.

---

## 2. Queue Types

### Q7. What are the queue types in RabbitMQ, and which should you use for critical data?

**Answer:**
- **Quorum queues:** replicated with Raft, data safety first. Use for critical integration flows.
- **Classic queues:** non-replicated (or mirrored in the old model, now deprecated). Use only for transient data where loss is acceptable.
- **Stream queues:** append-only log with replay by offset, non-destructive reads, and high throughput. Use for event replay and fan-out to many consumers.

### Q8. How do quorum queues work?

**Answer:** Each quorum queue has a leader and followers. A message is confirmed only after a majority (quorum) of replicas has written it. If the leader fails, a new leader is elected from the replicas. Require an odd number of nodes (3 or 5) for a meaningful quorum. Quorum queues have higher latency than classic queues because of replication.

### Q9. What limitations do quorum queues have?

**Answer:** They do not support non-durable queues, exclusive queues, or priority queues. They do not allow message TTL per message (queue-level TTL is supported). They use more memory and disk than classic queues. Plan capacity for replicas and set `x-max-in-memory-length` to control memory use.

### Q10. What are stream queues, and how do they differ from queues?

**Answer:** A stream keeps messages in an append-only log. Consumers read from an offset (`first`, `last`, `next`, or a timestamp) and do not remove messages on ack. Multiple consumers can read the same data independently. Use streams when you need replay, time-based reads, or very high fan-out. Use Spring AMQP or the RabbitMQ Stream client with the stream-specific API.

### Q11. What are priority queues, and what are the cautions?

**Answer:** Classic queues support the `x-max-priority` argument. Higher priority messages are delivered first. Caution: priority queues use more CPU and memory, and low-priority messages can starve. Keep the number of priority levels small (3 to 5). Quorum queues do not support priorities, so use separate queues instead.

---

## 3. Reliability and Delivery Guarantees

### Q12. What delivery guarantees does RabbitMQ give?

**Answer:** RabbitMQ provides at-most-once (auto-ack, no redelivery), or at-least-once (manual ack with redelivery after failure). It does not provide exactly-once delivery. Consumers must handle duplicates through idempotency.

### Q13. What are publisher confirms, and how do they differ from publisher returns?

**Answer:**
- **Publisher confirm:** the broker acknowledges that it accepted and persisted (for durable queues) the message. A nack means the message may be lost.
- **Publisher return:** the broker returns a message that could not be routed (with the `mandatory` flag set).

Use both for critical publishing. Confirms tell you about broker acceptance, returns tell you about routing failures.

### Q14. What makes a message durable?

**Answer:** Three conditions must hold: the exchange is durable, the queue is durable, and the message is persistent (`deliveryMode=2`). Missing any one causes loss on broker restart. Quorum queues are durable by design. Persistence adds disk I/O, so measure throughput.

### Q15. How does consumer acknowledgment work, and what happens if a consumer dies?

**Answer:** Unacknowledged messages stay assigned to the consumer. When the consumer's channel or connection closes, the broker requeues those messages and marks them as redelivered. Consumers must be idempotent because redelivery can happen after a message was processed but before the ack reached the broker.

### Q16. What is the `redelivered` flag, and how do you use it?

**Answer:** RabbitMQ sets the `redelivered` flag when a message was delivered before. Use it as a signal for extra checks (for example, look up the idempotency key), but do not rely on it alone. With quorum queues, use the `x-delivery-count` header to count attempts and move poison messages to a DLX when the count is high.

### Q17. How do you guarantee ordering in RabbitMQ?

**Answer:** Ordering holds within a single queue with a single consumer (and no requeue reordering). Concurrency breaks ordering. Options:
- Use one queue per entity key (for example, one queue per customer) with a consistent hash exchange.
- Use the consistent hash exchange plugin to spread keys across queues while keeping each key in one queue.
- Accept partial order and design consumers to handle out-of-order events using version numbers.

Requeued messages can arrive out of order, so check versions in the consumer.

---

## 4. Routing, TTL and Dead Lettering

### Q18. What are message TTL and queue TTL?

**Answer:**
- **Message TTL** (`expiration` property, per message): expires a message after a time. Expiry is checked at the head of the queue, so expired messages behind a non-expired one may stay longer.
- **Queue TTL** (`x-message-ttl` argument): applies to all messages in the queue.
- **Queue expiry** (`x-expires`): deletes the queue after it has been unused for a time.

### Q19. What is a dead-letter exchange, and what triggers dead lettering?

**Answer:** A dead-letter exchange (DLX) receives messages when:
- A consumer rejects or nacks with `requeue=false`.
- The message expires by TTL.
- The queue reaches its max length (`x-max-length` with `overflow=reject-publish-dlx`, or `drop-head`).
- The quorum queue delivery limit is exceeded (`x-delivery-limit`).

Configure with `x-dead-letter-exchange` and optionally `x-dead-letter-routing-key`. The original reason is recorded in the `x-death` header.

### Q20. How do you implement retry with delay using RabbitMQ (no in-consumer blocking)?

**Answer:** Three common approaches:
- **Retry queues with TTL:** a failed message goes to a retry queue with `x-message-ttl`, and its DLX points back to the main exchange. Repeat with increasing TTLs (one queue per delay tier).
- **Delayed message exchange plugin** (`rabbitmq_delayed_message_exchange`): publish with an `x-delay` header.
- **Spring Retry in the consumer:** simple, but blocks the listener thread.

Choose retry queues for predictable tiers, and the plugin for flexible per-message delays.

### Q21. What is the difference between `basic.reject` and `basic.nack`?

**Answer:** `basic.reject` rejects one message. `basic.nack` can reject multiple messages with `multiple=true`. Both accept a `requeue` flag. `nack` is preferred because it supports batch operations and is supported by all clients.

### Q22. How do you manage queue length and memory pressure?

**Answer:** Set `x-max-length` or `x-max-length-bytes` with a defined overflow behavior. Monitor queue depth and consumer lag. For quorum queues, `x-max-in-memory-length` keeps only a set number of messages in memory. Prefer fixing consumers over relying on overflow, because overflow means data is parked or dropped.

### Q23. What are the RabbitMQ flow control and resource alarms?

**Answer:** When a node reaches its memory high watermark or free disk threshold, RabbitMQ raises an alarm and blocks publishers on connections that publish. Publishers see blocked connections. Causes: slow consumers, large queues, large messages. Fix by scaling consumers, reducing message size, or setting queue limits. Monitor alarms in the management UI and Prometheus.

---

## 5. Operations, Scaling and Topology

### Q24. How does RabbitMQ clustering work?

**Answer:** Nodes in a cluster share metadata (exchanges, queues, bindings, users, vhosts) but classic queue contents live on one node. Quorum queues replicate data across nodes. Use an odd number of nodes. Clients can connect to any node, but for quorum queues, the leader may be on a different node, which adds latency. Put a load balancer or DNS round robin in front of the cluster.

### Q25. What is the difference between Shovel and Federation?

**Answer:**
- **Shovel:** moves messages from a source queue or exchange to a destination, one way. Simple and explicit. Good for moving DLQ messages back or bridging clusters.
- **Federation:** links exchanges or queues across brokers, so messages flow automatically based on downstream demand. Good for loosely coupled, geographically distributed brokers.

### Q26. How do you run RabbitMQ on Kubernetes?

**Answer:** Use the RabbitMQ Cluster Operator. It manages StatefulSets, configuration, TLS, and upgrades. Set persistent volumes, resource requests and limits, anti-affinity across nodes, and a PodDisruptionBudget. Use an operator-managed admin user and secret rotation. Expose only the AMQP port inside the cluster, and keep the management UI private.

### Q27. What metrics would you monitor for RabbitMQ?

**Answer:**
- Queue depth (ready and unacknowledged messages).
- Consumer count and consumer utilization.
- Publish and deliver rates.
- Memory and disk usage and alarms.
- Connection and channel counts.
- Quorum queue leader changes and replica health.
- DLQ depth and age of oldest message.

Use the Prometheus plugin (`rabbitmq_prometheus`) and Grafana dashboards.

### Q28. How do you upgrade RabbitMQ without downtime?

**Answer:** Upgrade a rolling cluster one node at a time. Check feature flags and compatibility first. Quorum queues keep working while a node is down if the quorum is maintained. Drain connections before stopping a node if clients do not reconnect cleanly. Test the upgrade in a staging cluster with the same client versions.

### Q29. How do you back up and recover RabbitMQ?

**Answer:** Export definitions (exchanges, queues, bindings, users, policies) with `rabbitmqadmin` or the HTTP API, and store them in Git. Messages are not part of definitions. For data recovery, rely on quorum queue replication and backups of the data directory if required. Document the recovery steps and test them.

### Q30. How do you manage RabbitMQ configuration as code?

**Answer:** Define exchanges, queues, bindings, and policies in code (Spring `@Bean` for application-owned resources, or `definitions.json` and Terraform provider for platform-owned resources). Use policies for shared settings (TTL, max length, DLX) so they apply to many queues by name pattern. Review changes through pull requests.

---

## 6. Spring AMQP and Application Patterns

### Q31. How do you configure a message converter for JSON in Spring AMQP?

**Answer:**

```java
@Bean
MessageConverter messageConverter() {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    converter.setClassMapper(classMapper());
    return converter;
}
```

Use explicit type IDs or a `ContentTypeDelegatingMessageConverter`. Avoid sending Java class names in headers, because they couple producers and consumers to package names.

### Q32. What is the security risk with JSON converters in Spring AMQP?

**Answer:** If the converter trusts the `__TypeId__` header, a producer can make the consumer deserialize arbitrary classes. Restrict trusted packages with `setTrustedPackages`, or better, use a fixed type mapping. Keep producer and consumer schemas versioned.

### Q33. How do you implement request/reply over RabbitMQ?

**Answer:** The producer sends a message with `replyTo` and `correlationId`. The consumer publishes the response to the `replyTo` queue with the same correlation ID. Spring AMQP supports this with `RabbitTemplate.convertSendAndReceive`. Use direct reply-to (`amq.rabbitmq.reply-to`) for temporary reply queues. Always set a timeout, because the reply may never arrive.

### Q34. How do you configure concurrency for a `@RabbitListener`?

**Answer:**

```java
@RabbitListener(queues = "orders.created.q", concurrency = "3-10")
public void handle(OrderEvent event) {
    orderService.process(event);
}
```

Concurrency starts consumers in the range. Set prefetch to match the processing time. More consumers help only if the work is independent and the downstream can handle the load.

### Q35. How do you run a message listener safely in Spring Boot?

**Answer:**
- Use manual or auto ack with retry and DLQ configuration (see Q19).
- Keep listeners short. Move long work to a separate pool with a bounded queue.
- Catch non-retryable exceptions and reject without requeue.
- Log message ID, correlation ID, and queue name.
- Stop listeners on shutdown before the database pool closes.

### Q36. How do you test RabbitMQ in integration tests?

**Answer:** Use Testcontainers with the RabbitMQ image. Create the same exchanges, queues, and bindings as production (run the same configuration class). Publish a message, wait with Awaitility, and assert results. Test DLQ routing and retry behavior with a failing handler.

### Q37. How do you monitor a Spring AMQP consumer in production?

**Answer:** Enable observation (`spring.rabbitmq.listener.simple.observation-enabled=true`) for traces and metrics. Track processing time per listener, failures per exception type, and redeliveries. Alert on DLQ growth and consumer count dropping to zero.

---

## 7. Design Decisions

### Q38. When would you choose RabbitMQ over Kafka in an iPaaS?

**Answer:** Choose RabbitMQ for task queues, command messages, request/reply, complex routing by key or header, per-message acknowledgment, and per-message TTL or delay. Choose Kafka for long-term event logs, replay at scale, and very high throughput streams. Many platforms use both: Kafka for event streams, RabbitMQ for command flows.

### Q39. How do you design exchange and queue naming conventions?

**Answer:** Use the pattern `<domain>.<entity>.<event>` for routing keys (for example, `orders.order.created`). Name exchanges by domain (`orders.exchange`) and queues by consumer (`billing.orders.created.q`). Name DLQs with a `.dlq` suffix and DLX with a `.dlx` suffix. Document the convention in the platform standards.

### Q40. How do you handle schema versioning for messages in RabbitMQ?

**Answer:** Put a version in the routing key or a header (`schemaVersion=2`). Consumers accept older versions for a deprecation period. Add optional fields only, and use a schema registry with Avro or Protobuf for enforcement. Run old and new consumers in parallel during migration, and remove old versions only after no producers send them.

---

## 8. Frequently Asked Production & Troubleshooting Scenarios

### Q41. What is a Single Active Consumer (SAC) in RabbitMQ, and when do you use it?

**Answer:** Setting `x-single-active-consumer=true` on a queue allows multiple consumer instances to subscribe for high availability, while the broker delivers messages to only **one active consumer** at a time. If that consumer disconnects or crashes, RabbitMQ automatically fails over to the next registered consumer. Use SAC when strict FIFO message ordering is required alongside consumer high availability.

### Q42. Why shouldn't you share a RabbitMQ `Channel` across multiple threads?

**Answer:** `Connection` is thread-safe, but `Channel` is **not thread-safe** for concurrent publishing or state changes because AMQP frames from multiple threads can interleave on the wire and corrupt protocol state or publisher confirms. Spring AMQP's `CachingConnectionFactory` handles this by pooling channels per thread/operation automatically.

### Q43. What is the head-of-line blocking problem with per-message TTL in RabbitMQ?

**Answer:** RabbitMQ only expires per-message TTLs when a message reaches the **head of the queue**. If a message with `TTL = 10 minutes` sits at the head of an unconsumed queue ahead of a message with `TTL = 5 seconds`, the 5-second message will not expire or move to the DLX until the 10-minute message is consumed or expires. Fix this by using separate retry queues per TTL tier (`retry.5s.q`, `retry.30s.q`, `retry.5m.q`).

### Q44. What happens during a network partition (split-brain) in a RabbitMQ cluster?

**Answer:**
- **Quorum Queues:** use Raft consensus. The side with a majority of nodes continues operating; the minority side stops serving that queue, preventing split-brain data divergence.
- **Cluster partition handling modes:** `pause_minority` (recommended for 3 or 5 node clusters; minority nodes pause until partition heals), `autoheal`, or `ignore`. Never use `ignore` with durable stateful workloads.

### Q45. What happens when a consumer takes longer than `consumer_timeout` (default 30 minutes) to acknowledge a message?

**Answer:** RabbitMQ closes the channel with `PRECONDITION_FAILED - delivery acknowledgement on channel ... timed out` and requeues the message, which can cause an infinite redelivery loop for long batch jobs. Fix by:
1. Splitting large batch messages into smaller chunks (Splitter EIP).
2. Handing off long-running work to a persistent job store and acknowledging the trigger message once persisted.
3. Adjusting `consumer_timeout` only if genuinely required.

### Q46. How do you handle messages with very large payloads (e.g., 50 MB CSV/XML files) in RabbitMQ?

**Answer:** Do not send 50 MB messages through RabbitMQ—large messages spike broker memory, trigger flow-control alarms, and block channels. Implement the **Claim Check pattern**: upload the payload to object storage (S3 / OCI Object Storage / shared store), publish a lightweight JSON event containing the object URI, checksum, and metadata, and let the consumer stream the file directly from storage.

### Q47. How does batch consumption work in Spring AMQP, and when should you use it?

**Answer:** Set `spring.rabbitmq.listener.simple.consumer-batch-enabled=true` and `batch-size=100` (with `batchListener=true` on the container factory) so `@RabbitListener` receives `List<Message>` or `List<OrderEvent>`. Use it when writing to a database (MySQL/Oracle JDBC batch insert) to turn 100 individual DB round-trips into 1 batched transaction.

### Q48. What is the difference between AMQP Transactions (`channel.txSelect`) and Publisher Confirms?

**Answer:** AMQP transactions are synchronous and reduce throughput by up to 100x because the broker flushes to disk and blocks on every commit. Asynchronous **Publisher Confirms** (`confirm-type=correlated`) achieve the same durability guarantee with much higher throughput.

### Q49. How do you prevent an infinite retry loop when a consumer throws an exception?

**Answer:**By default in `AUTO` ack mode, throwing any exception requeues the message infinitely at high speed. Prevent this by:
1. Setting `default-requeue-rejected=false` combined with a retry template or `RejectAndDontRequeueRecoverer`.
2. Using Quorum Queues with `x-delivery-limit=5` so the broker automatically dead-letters the message after 5 failed deliveries.
3. Distinguishing fatal exceptions (`MessageConversionException`, `ValidationException`) from transient exceptions (`CannotAcquireLockException`, `SocketTimeoutException`).

### Q50. How do you achieve tenant fairness so one tenant dumping 1 million messages doesn't starve other tenants?

**Answer:**
- **Sharded / Per-tenant-tier queues:** isolate enterprise, standard, and bulk/batch tenants onto separate queues and listener pools.
- **Rate limiting at ingress:** enforce per-tenant publish quotas at the API gateway or ingestion service.
- **Separate vhosts or dedicated workers** for high-volume bulk sync workloads.

### Q51. How do you trace a message from HTTP ingress -> RabbitMQ -> Apache Camel -> Oracle DB?

**Answer:** Propagate W3C Trace Context (`traceparent`, `tracestate`) and business `X-Correlation-Id` across every hop:
- Spring Boot HTTP filter extracts or creates `traceparent` + `correlationId`.
- `RabbitTemplate` (`observation-enabled=true`) injects trace headers into AMQP message properties.
- `@RabbitListener` / Camel route extracts the headers into MDC (`MDC.put("correlationId", ...)`).
- Audit/DB tables persist the `correlation_id` column alongside the business record.

### Q52. What is the difference between `SimpleMessageListenerContainer` (SMLC) and `DirectMessageListenerContainer` (DMLC) in Spring AMQP?

**Answer:**
- **SMLC (default):** uses an internal queue and dedicated consumer threads per listener. Supports dynamic concurrency scaling (`min-max`).
- **DMLC:** invokes the listener directly on the RabbitMQ client's I/O thread pool without an extra thread hop, reducing context switching and memory overhead when managing many queues. Avoid blocking the thread for long periods in DMLC unless paired with a bounded worker pool or virtual threads.

## 9. Durability, Exactly-Once Processing and Acknowledgements

### Q53. What is the difference between a durable queue and a persistent message?

**Answer:** They are two different settings, and you need **both** to survive a broker restart.

| | Durable queue | Persistent message |
|---|---|---|
| Applies to | The queue (or exchange) **definition** | Each **message** |
| Set by | `durable=true` when declaring the queue | `deliveryMode=2` (`MessageDeliveryMode.PERSISTENT`) by the publisher |
| Survives restart | The queue **metadata** is recreated after restart | The message **body** is written to disk |
| Alone it means | Queue exists after restart but is **empty** (transient messages lost) | Message written to disk, but if the queue is non-durable the queue and its messages are gone |

Other notes:
- Exchanges must also be durable, otherwise bindings disappear after restart.
- Quorum queues and streams are always durable and always store messages on disk (replicated via Raft).
- Persistence alone does not guarantee delivery; the publisher must also use **publisher confirms** to know the broker wrote the message.
- Spring AMQP: `new Queue(name)` is durable by default, and `RabbitTemplate` sends persistent messages by default.

```java
@Bean
Queue ordersQueue() {
    return QueueBuilder.durable("orders.q")
            .quorum()                                  // replicated + always on disk
            .build();
}

rabbitTemplate.convertAndSend("orders.x", "order.created", order, m -> {
    m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
    return m;
});
```

### Q54. How do you make sure a message is processed (read) only once?

**Answer:** RabbitMQ gives **at-most-once** (auto-ack) or **at-least-once** (manual ack). It does not give exactly-once delivery: a consumer can process a message and crash before the ack reaches the broker, so the message is redelivered. You achieve **effectively-once processing** with:

1. **Manual ack after successful processing** (`AcknowledgeMode.MANUAL` or `AUTO` in Spring, which acks only when the listener returns normally).
2. **Unique message ID** set by the producer (`messageId` property or business key such as `orderId`).
3. **Idempotent consumer / deduplication store:**
   - Insert the `messageId` into a `processed_messages` table with a **unique constraint in the same DB transaction** as the business change. A duplicate causes a constraint violation; ack and skip.
   - Or use Redis `SET key NX EX <ttl>` for fast dedup (less strong than the DB approach).
4. **Naturally idempotent operations:** upserts, `UPDATE ... WHERE status='PENDING'`, version checks.
5. **Single active consumer** (`x-single-active-consumer=true`) if strict ordering is needed — it does not prevent redelivery, so idempotency is still required.
6. **Producer side:** publisher confirms + retry may create duplicates too, so dedup on the consumer is mandatory. (RabbitMQ Streams support producer-side deduplication via `publishingId`.)

```java
@RabbitListener(queues = "orders.q")
@Transactional
public void onOrder(OrderEvent event, @Header(AmqpHeaders.MESSAGE_ID) String messageId) {
    try {
        processedRepo.save(new ProcessedMessage(messageId));   // UNIQUE(message_id)
    } catch (DataIntegrityViolationException dup) {
        log.info("Duplicate message {} ignored", messageId);
        return;                                                 // ack, do nothing
    }
    orderService.apply(event);                                  // same DB transaction
}                                                               // ack after commit
```

### Q55. What happens if a consumer never sends an ack, and how do you handle it?

**Answer:** In manual ack mode, a delivered message stays **Unacked** and is assigned to that consumer. RabbitMQ does **not** time it out quickly:
- **Consumer connection/channel closes or crashes:** all its unacked messages are **requeued** (`redelivered=true`) and delivered to another consumer.
- **Consumer alive but never acks (bug, hung thread, forgot `basicAck`):** messages stay unacked. Once the count reaches `prefetch`, the broker stops sending more to that consumer, so the consumer looks stuck while the queue grows.
- **`consumer_timeout`** (default 30 minutes in RabbitMQ 3.8.15+): if a delivery is not acked in that time, the broker closes the channel with `PRECONDITION_FAILED` and requeues the messages.

How to handle it:
1. **Always ack or nack in `finally`-style logic**; in Spring use `AUTO` ack mode so the container acks on success and nacks on exception.
2. **Set a sensible `prefetch`** (e.g. 10–250) so one stuck consumer cannot hoard thousands of messages.
3. **Tune `consumer_timeout`** to slightly above your maximum processing time; add processing timeouts in code (HTTP/DB timeouts).
4. **Reject poison messages** with `basicNack(tag, false, false)` / `AmqpRejectAndDontRequeueException` and route them to a **Dead Letter Exchange**. Use quorum queue `x-delivery-limit` to stop infinite redelivery loops.
5. **Make consumers idempotent** because requeued messages will be processed again (Q54).
6. **Monitor** `messages_unacknowledged` per queue/consumer and alert when it stays at the prefetch value with no ack rate.
7. **Long-running jobs:** persist the job and ack immediately, then process asynchronously (see Q45).

```java
@RabbitListener(queues = "orders.q", ackMode = "MANUAL")
public void handle(OrderEvent event, Channel channel,
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    try {
        orderService.apply(event);
        channel.basicAck(tag, false);
    } catch (TransientException e) {
        channel.basicNack(tag, false, true);    // requeue for retry
    } catch (Exception e) {
        channel.basicNack(tag, false, false);   // to DLX, no requeue
    }
}
```

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: auto
        prefetch: 50
        default-requeue-rejected: false
```

