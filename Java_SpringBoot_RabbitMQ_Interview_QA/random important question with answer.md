# Random Important Questions with Answers

## 1. How does ConcurrentHashMap work? Do we have hashing and collision handling?

Yes. Since Java 8, `ConcurrentHashMap` (CHM) uses:

- **Hashing:** `spread(h) = (h ^ (h >>> 16)) & 0x7fffffff` mixes the high bits into the low bits. The bucket index is `(n - 1) & hash`, where `n` is the table size (always a power of 2).
- **Table layout:** an array of `Node<K,V>` bins. Each bin is either empty, a linked list of nodes, a red-black `TreeBin` or a `ForwardingNode` (during resize).
- **Collision handling:**
  - Colliding keys are chained in a linked list inside the same bin.
  - When a bin holds 8 or more nodes and the table has at least 64 buckets, the list is converted into a red-black tree (`TREEIFY_THRESHOLD = 8`, `MIN_TREEIFY_CAPACITY = 64`). It converts back to a list at 6 or fewer nodes.
  - If the table is smaller than 64, the map resizes instead of treeifying.
- **Concurrency control (no global lock):**
  - **Reads** are lock-free. Table and node reads use volatile semantics.
  - **Insert into an empty bin** uses CAS (`compareAndSet`) on the table slot.
  - **Update or insert into a non-empty bin** synchronizes only on the head node of that bin. Threads working on different buckets do not block each other.
  - **Resize** is incremental. Threads that hit a `ForwardingNode` help transfer buckets.
  - **Size** is tracked with `CounterCell[]` (similar to `LongAdder`) to avoid a single hot counter.
- **Atomic compound operations:** `computeIfAbsent`, `compute`, `merge` run atomically for a key.
- **Other points:** no `null` keys or values (they would be ambiguous with "absent" under concurrency). Iterators are weakly consistent and never throw `ConcurrentModificationException`.
- **History:** Java 7 used lock striping with `Segment` objects. Java 8 replaced this with per-bin locking.

## 2. What happens if you modify an object field used by `equals()` and `hashCode()` after inserting it into a HashMap?

The map becomes corrupted for that key:

1. The entry was stored in the bucket for the **old** hash code.
2. After mutation, `hashCode()` returns a different value. `get()`, `containsKey()` and `remove()` compute the new hash and search the **wrong bucket**, so they return `null`/`false`.
3. The entry is still present. It shows up in `keySet()`/`entrySet()` iteration and affects `size()`, so it is effectively a leaked entry that you cannot look up or remove.
4. If only `equals()` changes but `hashCode()` does not, lookups may still land in the right bucket but fail the equality check.

**Fix:** make fields used in `equals`/`hashCode` `final` and immutable, or do not use mutable objects as keys (see question 5).

## 3. How does the JVM load, link, initialize and unload classes? How can classloader leaks occur?

**Lifecycle**

1. **Loading:** a `ClassLoader` finds the `.class` bytes and creates a `Class` object. The default delegation model asks the parent first: Bootstrap → Platform → Application → custom loaders.
2. **Linking:**
   - **Verification:** checks bytecode safety.
   - **Preparation:** allocates static fields and sets them to default values (`0`, `null`, `false`). Constant initializers are applied here.
   - **Resolution:** turns symbolic references (names) into direct references. This can be lazy.
3. **Initialization:** `<clinit>` runs once (static initializers and static field assignments). The JVM guarantees thread-safe, one-time initialization with a class-init lock.
4. **Unloading:** a class is unloaded only when its `ClassLoader` is unreachable. This means no instances, no `Class` objects and no references to its classes remain. Metaspace is then reclaimed by GC. Classes from the bootstrap loader are never unloaded.

**Classloader leaks** happen when a long-lived object keeps a reference to a class or loader from a short-lived one (typical in app servers that redeploy webapps):

- Static fields or singletons in a parent/shared library that hold objects from the webapp.
- `ThreadLocal` values set in pooled threads and never removed.
- JDBC drivers registered with `DriverManager` (loaded by a parent loader) and not deregistered.
- Threads, timers or executors started by the app and not shut down.
- Shutdown hooks, MBeans, or listeners registered globally and not removed.
- Logging frameworks or caches holding references to app classes.

**Diagnosis:** redeploy the app a few times, take a heap dump, and look for multiple instances of the old `WebappClassLoader`. Follow the GC root path.

**Fix:** clean up in `ServletContextListener.contextDestroyed()`: call `ThreadLocal.remove()`, deregister drivers, shut down executors, and remove static references.

## 4. How does IdentityHashMap achieve concurrent access, and how does it differ from Hashtable?

**Correction to the premise:** `IdentityHashMap` does **not** provide concurrent access. It is not thread-safe. For concurrency you must wrap it externally (for example `Collections.synchronizedMap`) or use another structure.

**How it works:**

- It compares keys with `==` (reference identity), not `equals()`.
- It hashes with `System.identityHashCode()`.
- It uses a single flat array with linear probing (keys and values stored next to each other). It allows `null` keys.

**Difference from Hashtable:**

| Aspect | IdentityHashMap | Hashtable |
|---|---|---|
| Key comparison | `==` (identity) | `equals()` |
| Hash | `System.identityHashCode()` | `hashCode()` |
| Thread safety | None | All methods `synchronized` on one lock |
| Nulls | Allowed (keys and values) | Not allowed |
| Status | Specialized utility | Legacy; prefer `ConcurrentHashMap` |

## 5. Why should mutable objects generally not be used as HashMap keys?

- The hash code is computed when the key is inserted. If the key changes later, the map cannot find it (see question 2).
- The same problem affects `HashSet`, and any structure that depends on hashing.
- Even with a stable `hashCode`, external mutation can break the invariant that equal keys stay equal.

**Use instead:** `String`, `Integer`, `UUID`, enums, or records/classes whose fields are all `final` and immutable. If a record holds a `List` or array, copy it defensively (`List.copyOf`), because records only make the references final, not the contents.

## 6. Design a custom collection that supports average O(1) lookup while preserving insertion order.

Use a `HashMap` for O(1) lookup, plus a doubly linked list for insertion order. This is what `LinkedHashMap` does internally.

```java
import java.util.*;

public class OrderedMap<K, V> {
    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev, next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private final Map<K, Node<K, V>> index = new HashMap<>(); // O(1) lookup
    private final Node<K, V> head = new Node<>(null, null);   // sentinel
    private final Node<K, V> tail = new Node<>(null, null);   // sentinel

    public OrderedMap() { head.next = tail; tail.prev = head; }

    public V put(K key, V value) {
        Node<K, V> node = index.get(key);
        if (node != null) {                 // update keeps original position
            V old = node.value;
            node.value = value;
            return old;
        }
        node = new Node<>(key, value);      // new key goes to the end
        index.put(key, node);
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        return null;
    }

    public V get(K key) {
        Node<K, V> n = index.get(key);
        return n == null ? null : n.value;
    }

    public V remove(K key) {
        Node<K, V> n = index.remove(key);   // O(1) unlink
        if (n == null) return null;
        n.prev.next = n.next;
        n.next.prev = n.prev;
        return n.value;
    }

    public List<K> keysInOrder() {
        List<K> keys = new ArrayList<>(index.size());
        for (Node<K, V> n = head.next; n != tail; n = n.next) keys.add(n.key);
        return keys;
    }
}
```

**Complexity:** `get`, `put`, `remove` average O(1). Iteration is O(n). Space is O(n) with extra overhead per entry. Not thread-safe.

## 7. Compare records, ordinary classes and data classes. When would a Java record be unsuitable for a domain model?

| Aspect | Record (Java) | Ordinary class | Data class (Kotlin) |
|---|---|---|---|
| Boilerplate | Auto `equals`, `hashCode`, `toString`, accessors, canonical constructor | Manual | Auto, similar to records |
| Mutability | Components are `final` (immutable) | Anything | `val` (immutable) or `var` (mutable) |
| Inheritance | Cannot extend classes; can implement interfaces | Full | Cannot be `open`/`abstract`; can implement interfaces |
| Equality | By all components | You decide | By all primary-constructor properties |
| Copy | Manual "wither" methods | Manual | `copy()` built in |

**A record is unsuitable for a domain model when:**

- The entity has **identity** and mutable state over time (for example an `Order` that changes status). Equality by all fields is wrong; equality should be by ID.
- You need **JPA `@Entity`** behaviour: Hibernate needs a no-arg constructor, mutable fields, and proxies for lazy loading. Records are fine for DTOs, projections and `@Embeddable` values in recent Hibernate versions, but not for mutable entities.
- You need **inheritance** or abstract base classes.
- The object is built with many optional fields, where a builder with validation is clearer than a long canonical constructor.
- Fields are large or lazy-loaded, and you need to avoid computing all components in `equals`/`hashCode`.
- Components are mutable collections and you cannot guarantee defensive copies.

## 8. How do sealed classes, pattern matching and exhaustive switch expressions work together?

- **Sealed classes/interfaces** restrict which types may extend them (`sealed ... permits ...`, or inferred from the same file). Every subtype must be `final`, `sealed`, or `non-sealed`.
- **Pattern matching** (`instanceof` patterns, type patterns and record patterns in `switch`) lets you test type and deconstruct in one step. Guards (`when`) add conditions.
- **Exhaustive switch expressions:** because the compiler knows the full set of permitted subtypes, a `switch` over a sealed type needs no `default`. If a case is missing, it is a compile error.

```java
sealed interface Payment permits Card, Upi, Cash {}
record Card(String last4) implements Payment {}
record Upi(String vpa) implements Payment {}
record Cash(double amount) implements Payment {}

static String describe(Payment p) {
    return switch (p) {
        case Card(String last4)                -> "Card ending " + last4;
        case Upi(String vpa) when vpa.isBlank() -> "Invalid UPI";
        case Upi(String vpa)                   -> "UPI " + vpa;
        case Cash(double amt)                  -> "Cash " + amt;
    };
}
```

**Benefit:** when you add a new `Payment` subtype, every `switch` that handles it fails to compile until updated. This is safer than `default` branches that silently hide new cases.

## 9. Explain virtual threads and structured concurrency. What limitations should developers consider?

**Virtual threads** (final in Java 21):

- Lightweight threads managed by the JVM. They are mounted on a small pool of carrier (platform) threads.
- When a virtual thread blocks on I/O, it unmounts and frees the carrier. This allows one thread per request at very high scale, with simple blocking code.

**Structured concurrency** (`StructuredTaskScope`, still a preview API in recent JDKs; check your version):

- Subtasks forked inside a scope must finish before the scope exits. The lifetime of child tasks is bound to the parent.
- Failure in one subtask can cancel the others. Results are collected in a clear, bounded way.
- It removes leaked threads and makes error handling and cancellation easier to reason about.

**Limitations to consider:**

- **Pinning:** in Java 21–23 a virtual thread blocked inside `synchronized` (or a native call) pins its carrier thread. Prefer `ReentrantLock` in blocking code. Java 24+ (JEP 491) removes most `synchronized` pinning.
- **No speedup for CPU-bound work.** Virtual threads help with waiting, not computing.
- **Downstream limits are still the bottleneck.** A database connection pool of 20 still allows only 20 concurrent queries. Thread-per-request does not remove this.
- **Do not pool virtual threads.** Create them per task.
- **ThreadLocal** values are per thread, so millions of threads can use a lot of memory. Consider `ScopedValue` (preview).
- **Observability:** thread dumps and profilers need tooling updates. Frameworks and libraries may still use `synchronized` or thread-local assumptions.

## 10. Explain the Java Memory Model and the happens-before relationship.

The **Java Memory Model (JMM)** defines which values a thread may see when it reads a variable written by another thread. Without synchronization, the compiler, JIT and CPU may reorder operations, and writes may not be visible.

**Happens-before** is the guarantee: if action A happens-before action B, then A's effects are visible to B and A is ordered before B. Main rules:

- **Program order:** within one thread, each action happens-before the next in program order.
- **Monitor lock:** an unlock of a monitor happens-before every later lock of the same monitor.
- **Volatile:** a write to a volatile variable happens-before every later read of that variable.
- **Thread start:** `Thread.start()` happens-before any action in the started thread.
- **Thread join:** all actions in a thread happen-before another thread returns from `join()` on it.
- **Final fields:** a properly constructed object's `final` fields are visible to all threads after construction (provided `this` does not escape).
- **Transitivity:** if A happens-before B and B happens-before C, then A happens-before C.

```java
int data = 0;
volatile boolean ready = false;

// Thread 1
data = 42;
ready = true;       // volatile write

// Thread 2
if (ready) {        // volatile read sees true...
    print(data);    // ...so this sees 42
}
```

## 11. Why is `volatile int count; count++` not thread-safe?

`count++` is not one operation. It is three steps: **read** `count`, **add** 1, **write** back. `volatile` guarantees visibility and ordering of each read and write, but it does not make the three steps atomic.

Two threads can both read `5`, both write `6`, and one increment is lost.

**Fixes:**

- `AtomicInteger.incrementAndGet()` (lock-free CAS).
- `synchronized` method or block around the increment.
- `LongAdder` for high-contention counters.

## 12. Two threads update the same bank account simultaneously. How would you design a thread-safe solution?

**Single JVM:**

```java
class Account {
    private final long id;
    private long balanceCents;           // guarded by lock
    private final ReentrantLock lock = new ReentrantLock();

    Account(long id, long balanceCents) { this.id = id; this.balanceCents = balanceCents; }

    void withdraw(long amount) {
        lock.lock();
        try {
            if (amount <= 0 || balanceCents < amount)
                throw new IllegalStateException("Insufficient funds");
            balanceCents -= amount;
        } finally {
            lock.unlock();
        }
    }
}
```

**Transfers between two accounts (deadlock risk):** always lock in a consistent order, for example by account ID. This prevents A→B and B→A from deadlocking. Alternatively use `tryLock` with a timeout and retry.

**Multiple instances or a database:**

- Use a transaction with `SELECT ... FOR UPDATE` on the account rows, or
- Use optimistic locking with a `@Version` column and retry on `OptimisticLockException`.
- Keep the balance check and update in the same transaction, and add a `CHECK (balance >= 0)` constraint as a safety net.
- Use idempotency keys so retries do not transfer money twice (see question 16).
- Record each movement as a ledger entry; do not only update a balance.

## 14. How does Spring Boot auto-configuration work internally?

1. `@SpringBootApplication` includes `@EnableAutoConfiguration`.
2. That imports `AutoConfigurationImportSelector`, which reads the list of auto-configuration classes from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (older versions used `spring.factories`).
3. Each candidate is filtered by `@Conditional` annotations, evaluated when configuration is parsed:
   - `@ConditionalOnClass` (library on classpath)
   - `@ConditionalOnMissingBean` (user has not defined the bean)
   - `@ConditionalOnProperty` (property enabled)
   - `@ConditionalOnWebApplication`, and others
4. Ordering is controlled with `@AutoConfigureOrder`, `@AutoConfigureBefore` and `@AutoConfigureAfter`.
5. Properties are bound to beans with `@ConfigurationProperties` (for example `spring.datasource.*`).
6. Because of `@ConditionalOnMissingBean`, your own beans take precedence over defaults.

**Debugging:** run with `--debug` for the condition evaluation report, or expose the `conditions` endpoint through Actuator.

## 15. Why might `@Transactional` fail when one method calls another method within the same class?

Spring applies `@Transactional` through a **proxy** wrapped around the bean. External callers go through the proxy, which starts or joins the transaction. A call such as `this.save()` inside the same class skips the proxy, so no transaction interceptor runs.

Other causes of silent failure:

- The method is `private`, `final` or (with CGLIB) not overridable.
- The bean is not a Spring-managed bean, or it was created with `new`.
- Checked exceptions do not roll back by default. Use `rollbackFor = Exception.class` if needed.
- The method is invoked inside a `@Transactional(propagation = NOT_SUPPORTED)` or similar context.

**Fixes:**

- Move the transactional method to another Spring bean and inject it.
- Use `TransactionTemplate` for programmatic control.
- Use AspectJ weaving (`mode = AdviceMode.ASPECTJ`), which also intercepts self-invocation.

## 16. How would you design an idempotent payment API that prevents duplicate transactions during retries?

**Design:**

1. The client sends an `Idempotency-Key` header (for example a UUID per payment intent).
2. The server stores a row in an `idempotency_keys` table with a **unique constraint** on the key, plus the request hash, status (`IN_PROGRESS`, `COMPLETED`, `FAILED`) and the stored response.
3. **First request:** insert the key as `IN_PROGRESS`. If the insert fails because the key exists, another request is already running; return `409` or wait.
4. Process the payment. Pass the same idempotency key to the payment gateway, which also deduplicates.
5. Save the result and mark `COMPLETED`, within the same transaction as the payment record where possible.
6. **Retry with the same key:** return the stored response. Do not charge again.
7. **Same key, different payload:** return `422`. The request hash detects this.
8. Use a unique constraint on business identifiers too (for example `orderId`) as a second safety net.
9. Expire keys after a window (for example 24 hours).
10. For external calls that cannot be made atomic with the DB, use the outbox pattern and a reconciliation job that checks the gateway for `IN_PROGRESS` records.

## 17. An API's average response time is 150 ms, but P99 latency exceeds 3 seconds. How would you investigate?

Averages hide the tail. P99 means 1% of requests are slow, which often affects real users or specific data.

1. **Measure:** check per-endpoint histograms (Micrometer/Prometheus percentiles), not averages. Check whether the slow 1% is spread across endpoints or concentrated.
2. **Trace:** use distributed tracing (OpenTelemetry, Jaeger, Zipkin) on slow requests. Find which span takes the time.
3. **Check common tail causes:**
   - **Thread or queue saturation:** pool queues, Tomcat/Netty threads, event loop blocking.
   - **Connection pool waits:** HikariCP `pending` connections (see question 19).
   - **Slow or locked queries:** lock waits, missing indexes, full scans for some tenants or inputs.
   - **Downstream timeouts and retries:** a dependency with its own P99 spikes, multiplied by retries.
   - **GC pauses or JIT warm-up:** correlate with GC logs.
   - **CPU throttling in containers:** check cgroup throttling metrics.
   - **Large payloads or specific input data**, such as big pages or heavy users.
4. **Correlate timestamps** of slow requests with GC, deployments, traffic spikes and DB metrics.

**Fixes:** set timeouts on every outbound call, use bulkheads and circuit breakers, add hedged requests where safe, paginate large responses, cache, and move slow work to async processing.

## 18. How would you diagnose and fix the N+1 query problem in Hibernate?

**N+1:** one query loads N parents, then each lazy association triggers one more query, so you get N+1 queries.

**Diagnose:**

- Turn on SQL logging (`spring.jpa.show-sql` or `logging.level.org.hibernate.SQL=DEBUG`), and watch for repeated similar queries.
- Enable statistics (`hibernate.generate_statistics=true`) and check the prepared statement count.
- Use a tool such as datasource-proxy, p6spy, or Hypersistence Optimizer in tests to fail on N+1.

**Fix:**

```java
// Problem: loops over orders, each order.getItems() fires a query
List<Order> orders = orderRepo.findAll();

// Fix 1: fetch join (one query)
@Query("select o from Order o join fetch o.items where o.status = :s")
List<Order> findWithItems(@Param("s") Status s);

// Fix 2: entity graph
@EntityGraph(attributePaths = "items")
List<Order> findByStatus(Status s);

// Fix 3: batch fetching (fewer queries for large lists)
// spring.jpa.properties.hibernate.default_batch_fetch_size=50

// Fix 4: DTO projection when you only need some columns
```

**Notes:**

- Fetching two `List` (bag) collections in one query throws `MultipleBagFetchException`. Use `Set`, separate queries, or batch fetching.
- Keep associations `LAZY`. Avoid `EAGER` on collections.
- Fetch joins with pagination load everything into memory and warn. Use batch fetching or a two-step query (IDs first, then fetch).

## 19. HikariCP reports connection timeouts after 30 seconds. How would you identify the root cause?

`connectionTimeout` (default 30 s) is how long a thread waits to borrow a connection. A timeout means **no connection became free in 30 seconds**. The pool is not the cause by itself; something is holding connections.

**Investigate:**

1. **Hikari metrics:** `active`, `idle`, `pending` and `total` connections. High `pending` with `active == max` means exhaustion.
2. **Thread dumps:** look for many threads waiting in `HikariPool.getConnection`, and what the active threads are doing.
3. **Leak detection:** set `leakDetectionThreshold` (for example 20 s). Hikari logs the stack trace where the leaked connection was borrowed.
4. **Database side:** check active sessions and locks (`pg_stat_activity`, `SHOW PROCESSLIST`, lock wait views). Long-running or idle-in-transaction sessions are common culprits.
5. **Code side:** look for external HTTP calls or slow work **inside** `@Transactional` methods, which hold a connection the whole time.

**Common root causes:**

- Connection leaks (connection not closed or returned).
- Long transactions that include remote calls.
- Slow queries or lock contention.
- Pool too small for the load, or too large (which overloads the DB).
- Database unreachable or network problems.

**Fixes:** fix the leak or transaction scope first. Then tune `maximumPoolSize` (a common starting point is about `(core_count * 2) + effective_spindles`). Set `maxLifetime` below the DB or proxy idle timeout, and add query timeouts.

## 20. When should a monolithic application be split into microservices, and when is a modular monolith preferable?

**Split into microservices when:**

- Parts of the system need **independent scaling** (for example search versus checkout).
- Teams need **independent deployment** and autonomy, with clear bounded contexts.
- Components have very different reliability, security or technology needs.
- The domain boundaries are well understood and stable.
- You have the operational maturity: CI/CD, observability, service discovery, tracing, and on-call.

**Prefer a modular monolith when:**

- The team is small, or the domain boundaries are still changing.
- You need strong transactional consistency across modules.
- Latency between internal calls matters, and network hops are unnecessary.
- Operational maturity is low.
- You want to avoid a **distributed monolith**: services that must be deployed together and share a database. This is the worst of both worlds.

**Middle path:** build a modular monolith with enforced module boundaries (separate packages, no cross-module table access, events between modules). Extract a module into a service later when the need is proven.

## 21. Payment succeeds, but inventory reservation fails. How would you implement the Saga pattern?

Because payment already succeeded, the saga needs a **compensating action** (refund) instead of a rollback.

**Approach: orchestrated saga**

1. An `OrderSaga` orchestrator stores its state in a `saga_instance` table: `orderId`, `currentStep`, `status`.
2. Steps:
   - `PAYMENT_CHARGED` (success) → send `ReserveInventory` command.
   - `INVENTORY_RESERVED` → mark order confirmed.
   - `INVENTORY_FAILED` → trigger compensation: `RefundPayment` command.
3. Each step is a **local transaction** plus an **outbox** record, so the event is published reliably with the state change.
4. The refund is an idempotent command keyed by `orderId` or payment ID. Retry it until it succeeds.
5. Set timeouts on each step. If an event never arrives, the orchestrator moves to a compensating or manual-review state.
6. Record the compensation outcome (`REFUNDED`, `REFUND_PENDING`) so support can see it.

**Alternative:** reorder the steps. Reserve inventory first (a reversible step), then charge payment (the "pivot" transaction). Then only inventory needs compensation if payment fails. Preferring reversible steps first reduces refunds.

**Choreography** (services react to events, no central orchestrator) also works, but it is harder to track and debug for longer sagas.

**Note:** compensations are business actions, not rollbacks. A refund is not the same as the original charge never happening (fees, statements, timing). Design for that.

## 22. A Kafka consumer updates the database successfully but crashes before committing its offset. How would you prevent duplicate business operations?

After a crash, Kafka redelivers the message because the offset was not committed. The fix is to make processing **idempotent**.

**Option A: processed-events table in the same DB transaction (most common)**

```java
@Transactional
public void handle(OrderEvent event) {
    // unique constraint on event_id
    processedEventRepo.save(new ProcessedEvent(event.getId()));  // fails if already processed
    inventoryService.apply(event);                              // business update
}
```

- Insert the event ID and the business update in one transaction. Either both commit or neither does.
- On redelivery, the unique constraint violation (`DataIntegrityViolationException`) means "already done"; skip it and commit the offset.
- Catch the duplicate key exception explicitly, and treat it as a success.

**Option B: store the offset in the database** with the business data, and use `consumer.seek` on startup. Requires manual partition assignment and is more complex.

**Option C: make the operation naturally idempotent.** Use an upsert, or a conditional update such as `UPDATE ... SET qty = qty - ? WHERE id = ? AND last_event_version < ?`.

**Also:**

- Set `enable.auto.commit=false` and commit offsets **after** successful processing.
- Use a stable event ID (producer-assigned UUID), not the Kafka offset.
- Keep a dead-letter topic for poison messages.

## 23. Explain at-most-once, at-least-once and exactly-once processing. What limitations exist across databases and external systems?

- **At-most-once:** commit the offset (or acknowledge) **before** processing. If the consumer crashes, the message is lost, but it is never processed twice.
- **At-least-once:** process first, then commit. If the consumer crashes after processing and before committing, the message is redelivered. Duplicates are possible. This is the default for most systems.
- **Exactly-once:** the business effect happens once. In practice this is **effectively-once** = at-least-once delivery + idempotent processing.

**Kafka exactly-once (EOS):** idempotent producers, transactions (`transactional.id`), and `isolation.level=read_committed` for consumers. It works for consume → transform → produce **within Kafka**.

**Limitations:**

- Kafka's guarantee stops at the Kafka boundary. Writing to a database or calling an HTTP API or email service is outside it.
- Cross-system atomicity needs XA/two-phase commit. That is blocking, slow, and often unsupported by modern systems or cloud services.
- External systems (payment gateways, SMS, email) may not support idempotency keys. You need your own deduplication and reconciliation.
- Sinks must be idempotent. Using the outbox pattern, idempotency keys, dedupe tables, and reconciliation jobs gives "exactly-once effect" without distributed transactions.

## 24. Find the longest subarray with sum K when negative numbers are allowed. Explain the optimal algorithm.

**Why not sliding window?** With negative numbers, shrinking the window does not reliably reduce the sum, so the two-pointer invariant breaks.

**Optimal approach: prefix sums + hash map.** Time O(n), space O(n).

- Let `prefix[i]` be the sum of elements `0..i-1`. The sum of subarray `(j, i]` is `prefix[i] - prefix[j]`.
- We need `prefix[i] - prefix[j] = K`, so `prefix[j] = prefix[i] - K`.
- Store the **first** index where each prefix sum appears. We want the longest subarray, so the earliest index gives the maximum length. Do not overwrite existing entries.
- Initialize the map with `{0 → -1}` so subarrays starting at index 0 are counted.

```java
import java.util.*;

static int longestSubarrayWithSumK(int[] nums, long k) {
    Map<Long, Integer> firstIndex = new HashMap<>();
    firstIndex.put(0L, -1);       // empty prefix before index 0
    long prefix = 0;
    int best = 0;

    for (int i = 0; i < nums.length; i++) {
        prefix += nums[i];        // use long to avoid overflow
        Integer j = firstIndex.get(prefix - k);
        if (j != null) {
            best = Math.max(best, i - j);
        }
        firstIndex.putIfAbsent(prefix, i);   // keep earliest index only
    }
    return best;                  // 0 if no such subarray exists
}
```

**Example:** `nums = [1, -1, 5, -2, 3]`, `k = 3`. The answer is `4` (subarray `[1, -1, 5, -2]`, sum 3).

**Notes:**

- Use `long` for prefix sums to avoid integer overflow.
- For non-negative numbers, a sliding window gives O(n) with O(1) space.
