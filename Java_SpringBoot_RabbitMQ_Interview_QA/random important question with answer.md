# Random Important Questions with Answers

Simple, interview-friendly answers with examples. The original numbering is kept; question 13 was not supplied.

Java methods shown on their own belong inside a class. Spring examples need the matching application types and imports; they are not complete applications.

## 1. How does ConcurrentHashMap work? Do we have hashing and collision handling?

**Simple answer:** `ConcurrentHashMap` is a thread-safe hash map that achieves high performance by locking only individual buckets (or using lock-free CAS operations) instead of locking the entire map.

### How it works step by step (Java 8+)

1. **Hashing:** It takes `key.hashCode()`, mixes the high bits into the low bits (`spread(h) = (h ^ (h >>> 16)) & 0x7fffffff`) to reduce collisions, and picks a bucket index using `(table.length - 1) & hash`.
2. **Collision handling:**
   - If multiple keys land in the same bucket, they are chained in a **linked list**.
   - If a bucket grows to **8 or more nodes** AND the table capacity is at least **64**, that bucket converts into a **Red-Black Tree** (`TreeBin`) so lookups stay $O(\log n)$ instead of $O(n)$. (If the table is smaller than 64, it just resizes the array first.)
   - If removals shrink a tree bucket down to **6 nodes**, it converts back to a linked list.
3. **Concurrency control (why it's fast):**
   - **Reads (`get`):** Completely lock-free. It relies on `volatile` reads of the table array and node fields.
   - **Write to an empty bucket:** Uses a lock-free **CAS (Compare-And-Swap)** operation to place the first node into the empty array slot.
   - **Write to a non-empty bucket:** Uses `synchronized` **only on the first node (head)** of that single bucket. Threads writing to other buckets are not blocked at all.

### Example 1: Visual bucket & lock behavior

```text
Table Array (size 16):
Index 0: [ null ]                 <-- Thread A inserts here using CAS (no lock!)
Index 1: [ Node(K1) -> Node(K2) ] <-- Thread B locks ONLY Node(K1) to append Node(K3)
Index 2: [ TreeBin(8+ nodes) ]    <-- Thread C reads from here with 0 locks
```

### Example 2: Wrong vs. Right way to update a counter in ConcurrentHashMap

```java
ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<>();

// WRONG (Race condition! get + put are two separate steps):
Integer current = wordCounts.get("java");
wordCounts.put("java", current == null ? 1 : current + 1);

// RIGHT (Atomic single-step operation inside the bucket lock):
wordCounts.merge("java", 1, Integer::sum);
// Or:
wordCounts.compute("java", (k, v) -> (v == null) ? 1 : v + 1);
```

**Remember:**
- `null` keys and `null` values are **not allowed** (because in concurrent code you couldn't tell if `get(k) == null` means "key not found" or "key is mapped to null").
- Iterators are **weakly consistent**: they reflect the state of the map at or since iterator creation and do not throw `ConcurrentModificationException`.

## 2. What happens if you modify an object field used by `equals()` and `hashCode()` after inserting it into a HashMap?

**Simple answer:** The entry becomes "lost" inside the map. It still sits in the old bucket and counts toward `size()`, but `get()`, `containsKey()`, and `remove()` look in the new bucket and return `null` / `false`.

### Code Example & Step-by-Step Trace

```java
import java.util.*;

class AccountKey {
    int id; // Mutable field used in equals and hashCode!

    AccountKey(int id) { this.id = id; }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public boolean equals(Object o) {
        return (o instanceof AccountKey other) && this.id == other.id;
    }
}

public class MutableKeyDemo {
    public static void main(String[] args) {
        Map<AccountKey, String> map = new HashMap<>();
        AccountKey key = new AccountKey(10);

        // Step 1: Insert with id = 10 (stored in bucket for hash(10))
        map.put(key, "Alice");
        System.out.println(map.get(key)); // Prints: "Alice"

        // Step 2: Mutate the key while it is inside the map!
        key.id = 20;

        // Step 3: Lookup with mutated key (searches bucket for hash(20))
        System.out.println(map.get(key));               // Prints: null
        System.out.println(map.get(new AccountKey(10))); // Prints: null (bucket matches, but equals() checks 20 == 10 -> false!)
        System.out.println(map.size());                 // Prints: 1 (entry is leaked inside the map!)
    }
}
```

### Why even `new AccountKey(10)` fails:
1. `new AccountKey(10)` hashes to `10`, so `HashMap` goes to the **original bucket** where the entry is still stored.
2. However, inside that bucket, `HashMap` compares the search key (`id = 10`) with the stored key object (whose `id` is now `20`).
3. `equals()` returns `false`, so you cannot retrieve it with either `10` or `20`!

**Fix:** Make fields used in `equals()` and `hashCode()` `final`. If a key's state must change, `remove()` it from the map first, mutate it, and `put()` it back.

## 3. How does the JVM load, link, initialize and unload classes? How can classloader leaks occur?

**Simple answer:** The JVM loads `.class` bytes on demand, verifies and prepares static memory during linking, runs `static` initializers on first active use, and only unloads classes when their entire `ClassLoader` becomes unreachable.

| Step | What happens | Concrete Example |
|---|---|---|
| **1. Loading** | `ClassLoader` reads `.class` bytecode and creates a `Class<?>` object in memory. Uses parent delegation (`Bootstrap -> Platform -> App`). | Calling `Class.forName("com.app.Order")` finds `Order.class`. |
| **2. Linking: Verify** | Checks that bytecode is valid and doesn't violate JVM safety rules. | Rejects corrupted `.class` files with `VerifyError`. |
| **3. Linking: Prepare** | Allocates memory for `static` fields and sets **default zero/null values** (plus `static final` compile-time constants). | `static int count = 50;` is set to `0` during Prepare, not `50` yet! (`static final int MAX = 10;` is set to `10` here.) |
| **4. Linking: Resolve** | Replaces symbolic names (like `"java/lang/String"`) with direct memory references. | Links method call references to actual method addresses. |
| **5. Initialization** | Executes `<clinit>` (static variable assignments and `static { ... }` blocks). Guaranteed thread-safe by the JVM. | `count` now becomes `50`, and `static { System.out.println("Init"); }` runs. |
| **6. Unloading** | Happens during GC **only** when the `ClassLoader` that loaded the class is garbage-collected. | Undeploying a plugin or WAR file whose custom `ClassLoader` has zero remaining references. |

### Code Example: Seeing Prepare vs. Initialize

```java
class Config {
    static final int COMPILE_CONST = 100; // Prepared at link time (reading this does NOT initialize Config!)
    static int runtimeValue = 200;        // Set to 0 at Prepare, set to 200 at Initialize

    static {
        System.out.println("Config class initialized!");
    }
}

// In main():
System.out.println(Config.COMPILE_CONST); // Prints 100 (NO "Config class initialized!" printed)
System.out.println(Config.runtimeValue);  // Prints "Config class initialized!" then 200
```

### Example of a ClassLoader Leak (Memory Leak on Redeploy)

Every object has a reference to its `Class`, and every `Class` has a reference to the `ClassLoader` that loaded it (`Object -> Class -> ClassLoader -> all classes loaded by it`).

```java
// Classic leak inside a web application / plugin:
public class RequestContext {
    // ThreadLocal holds an application-specific UserSession object
    private static final ThreadLocal<UserSession> SESSION = new ThreadLocal<>();

    public static void set(UserSession s) { SESSION.set(s); }
    // If SESSION.remove() is forgotten in a finally block, the server's shared thread pool
    // keeps holding UserSession -> UserSession.class -> WebAppClassLoader forever!
}
```

**How to prevent classloader leaks:**
- Always call `threadLocal.remove()` in a `finally` block.
- Shut down any custom `ExecutorService` or background `Thread` when the app stops.
- Deregister JDBC drivers or global listeners registered by the application.

## 4. How does IdentityHashMap achieve concurrent access, and how does it differ from Hashtable?

**Simple answer:** Trick question! **`IdentityHashMap` is NOT thread-safe and does NOT support concurrent access** on its own. Its special feature is that it compares keys by **memory address (`==`)** instead of `.equals()`.

| Feature | `IdentityHashMap` | `Hashtable` |
|---|---|---|
| **Key comparison** | Reference equality (`k1 == k2`) | Object equality (`k1.equals(k2)`) |
| **Hash function** | `System.identityHashCode(k)` | `k.hashCode()` |
| **Thread safety** | **Not thread-safe** (must wrap with `Collections.synchronizedMap` for concurrent access) | **Thread-safe** (locks the entire table on every `get`/`put` using `synchronized`) |
| **Null keys/values** | Allows `null` keys and `null` values | Throws `NullPointerException` on `null` key or value |
| **Internal layout** | Single flat `Object[]` table using linear probing (`key, value` side by side) | Array of linked-list buckets |

### Code Example: `IdentityHashMap` vs. `Hashtable` / `HashMap`

```java
Map<String, String> identityMap = new IdentityHashMap<>();
Map<String, String> hashTable = new Hashtable<>();

String s1 = new String("admin"); // New object at memory address A
String s2 = new String("admin"); // New object at memory address B

// s1.equals(s2) is true, but (s1 == s2) is false!
identityMap.put(s1, "First");
identityMap.put(s2, "Second");

hashTable.put(s1, "First");
hashTable.put(s2, "Second"); // Overwrites "First" because s1.equals(s2) is true

System.out.println(identityMap.size()); // Prints: 2 (two distinct object references)
System.out.println(hashTable.size());   // Prints: 1 (same logical String value)
```

**When would you ever use `IdentityHashMap`?**
- Graph traversal / serialization frameworks (like Jackson or Hibernate) that need to track "have I already visited this exact object instance in memory?" to detect cycles, even if two objects override `.equals()` to return `true`.

## 5. Why should mutable objects generally not be used as HashMap keys?

**Simple answer:** Because `HashMap` chooses a bucket when the key is inserted and assumes the key's `hashCode()` and `equals()` result will never change afterward.

If a key mutates after insertion:
1. **Lookup failure:** `map.get(key)` hashes to a new bucket and returns `null`.
2. **Duplicate keys:** You can insert the "same" logical key again, violating map uniqueness.
3. **Memory leak:** `map.remove(key)` cannot find the entry in the new bucket, leaving it stuck in the old bucket.

### Example 1: Using a Mutable `List` as a Key (Bad!)

```java
Map<List<String>, String> roleCache = new HashMap<>();
List<String> roles = new ArrayList<>(List.of("READ"));

roleCache.put(roles, "BasicUser");

// Later in code, someone modifies the list:
roles.add("WRITE"); // Changes roles.hashCode()!

System.out.println(roleCache.get(roles)); // null!
```

### Example 2: Even a Java `record` can be unsafe if it holds a mutable field!

```java
// DANGEROUS KEY: record fields are final, but the List inside can still be modified!
record BadKey(List<String> tags) {}

// SAFE KEY: defensively copy into an unmodifiable list in the compact constructor
record SafeKey(List<String> tags) {
    SafeKey {
        tags = List.copyOf(tags); // Guaranteed deeply immutable (since String is also immutable)
    }
}
```

**Good keys:** `String`, `Integer`, `Long`, `UUID`, `Enum`, `LocalDate`, or custom classes/records where all fields are deeply immutable.

## 6. Design a custom collection that supports average O(1) lookup while preserving insertion order.

**Simple answer:** Combine a **`HashMap<K, Node>`** (for $O(1)$ lookup by key) with a **Doubly Linked List of `Node`s** (to preserve insertion order and support $O(1)$ unlinking on removal). This is how Java's `LinkedHashMap` works.

### Visual Diagram

```text
HashMap index:
  "A" ──────────┐
  "B" ──────────┼─────────────┐
  "C" ──────────┼─────────────┼─────────────┐
                ▼             ▼             ▼
Linked List:  [HEAD] <--> [Node A: 10] <--> [Node B: 20] <--> [Node C: 30] <--> [TAIL]
```
- **Lookup `get("B")`:** Use the `HashMap` to jump straight to `Node B` in $O(1)$.
- **Delete `remove("B")`:** Look up `Node B` in $O(1)$ via the `HashMap`, then wire `Node A.next = Node C` and `Node C.prev = Node A` in $O(1)$ without scanning the list!

### Complete Java Implementation

```java
import java.util.*;

public class OrderedMap<K, V> {
    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final Map<K, Node<K, V>> index = new HashMap<>();
    private final Node<K, V> head = new Node<>(null, null); // Dummy head
    private final Node<K, V> tail = new Node<>(null, null); // Dummy tail

    public OrderedMap() {
        head.next = tail;
        tail.prev = head;
    }

    public V put(K key, V value) {
        Node<K, V> existing = index.get(key);
        if (existing != null) {
            // Updating an existing key keeps its original insertion position
            V old = existing.value;
            existing.value = value;
            return old;
        }

        Node<K, V> node = new Node<>(key, value);
        index.put(key, node);

        // Append right before dummy tail in O(1)
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
        return null;
    }

    public V get(K key) {
        Node<K, V> node = index.get(key);
        return node == null ? null : node.value;
    }

    public V remove(K key) {
        Node<K, V> node = index.remove(key);
        if (node == null) {
            return null;
        }
        // Unlink from doubly linked list in O(1)
        node.prev.next = node.next;
        node.next.prev = node.prev;
        return node.value;
    }

    public List<K> keysInOrder() {
        List<K> keys = new ArrayList<>(index.size());
        for (Node<K, V> curr = head.next; curr != tail; curr = curr.next) {
            keys.add(curr.key);
        }
        return keys;
    }
}
```

### Usage Trace

```java
OrderedMap<String, Integer> map = new OrderedMap<>();
map.put("B", 2);
map.put("A", 1);
map.put("C", 3);
map.put("B", 20); // Updates value of "B", keeps order: [B, A, C]
map.remove("A");  // Unlinks "A" in O(1), order is now: [B, C]
```

**Complexity:** `get`, `put`, and `remove` are all **$O(1)$ average time**. Iteration is **$O(n)$**. Space is **$O(n)$**.

## 7. Compare records, ordinary classes and data classes. When would a Java record be unsuitable for a domain model?

**Simple answer:** Java `record`s are immutable value carriers where identity is defined by **all field values**. Domain entities usually have a **unique ID** and **mutable state** that changes over time, making `record`s a bad fit for entities.

| Feature | Java `record` | Ordinary Java `class` | Kotlin `data class` |
|---|---|---|---|
| **Mutability** | Shallowly **immutable** (all fields are `private final`) | Mutable or immutable (you choose) | Either (`val` for read-only, `var` for mutable) |
| **Auto-generated** | Constructor, getters (`name()`), `equals()`, `hashCode()`, `toString()` | Nothing (unless using Lombok) | Constructor, getters/setters, `equals()`, `hashCode()`, `toString()`, `copy()`, `componentN()` |
| **Inheritance** | Cannot `extends` any class (implicitly extends `java.lang.Record`); can `implements` interfaces | Full class & interface inheritance | Cannot be `open` / subclassed; can extend classes and implement interfaces |
| **Equality rule** | Compares **every component field** | You define it (e.g., compare only by `id`) | Compares properties in the primary constructor |

### Code Example: Why `record` is great for Value Objects / DTOs, but bad for Entities

```java
// GOOD USE OF RECORD: Value Object (Money has no ID; two $10 USD objects are equal)
public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
    }
}

// BAD USE OF RECORD: Mutable Domain Entity / JPA @Entity
// 1. If Order status changes from PENDING to PAID, a record forces you to create a new object.
// 2. Two Order records with the same orderId=100 but different statuses would NOT be .equals()!
// 3. JPA/Hibernate @Entity requires a no-arg constructor, non-final fields, and proxy subclassing.
@Entity
public class OrderEntity {
    @Id
    private Long id;
    private String status; // Changes over lifecycle: CREATED -> PAID -> SHIPPED

    protected OrderEntity() {} // Required by JPA

    public void markPaid() { this.status = "PAID"; }

    @Override
    public boolean equals(Object o) {
        // Entity equality depends ONLY on the ID, not mutable fields!
        return (o instanceof OrderEntity other) && Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
```

## 8. How do sealed classes, pattern matching and exhaustive switch expressions work together?

**Simple answer:**
- **`sealed`** restricts which classes are allowed to implement/extend a type.
- **Pattern matching** checks the subtype and extracts its fields in one step.
- **Exhaustive `switch`** uses that closed list of subtypes so the compiler **forces you to handle every subtype** without needing a `default` branch.

### Complete Example (Java 21+)

Suppose you are building a payment result handler. Only three outcomes are allowed:

```java
sealed interface PaymentResult permits Approved, Declined, FraudHold {}

record Approved(String authCode, long amountCents) implements PaymentResult {}
record Declined(String reason) implements PaymentResult {}
record FraudHold(int riskScore) implements PaymentResult {}

public class PaymentProcessor {
    // Look: NO 'default' branch is needed!
    static String toUserMessage(PaymentResult result) {
        return switch (result) {
            // Record deconstruction pattern + guard ('when')
            case Approved(String code, long cents) when cents > 100_000 ->
                "High-value payment approved (Auth: " + code + ")";
            case Approved(String code, long cents) ->
                "Approved for " + cents + " cents (Auth: " + code + ")";
            case Declined(String reason) ->
                "Payment declined: " + reason;
            case FraudHold(int score) ->
                "Held for review (Risk score: " + score + ")";
        };
    }
}
```

### Why this is much safer than old `if-else` or `default` switches:
Suppose a teammate later adds a 4th subtype:
```java
record Timeout(int retryAfterSeconds) implements PaymentResult {}
```
Every `switch (result)` across the entire codebase **immediately fails to compile** with *"the switch expression does not cover all possible input values"*. You never accidentally forget to handle a new case in production!

## 9. Explain virtual threads and structured concurrency. What limitations should developers consider?

**Simple answer:**
- **Virtual Threads (Java 21):** Lightweight threads managed by the JVM (millions can exist in memory) that run on top of a small pool of OS/platform threads ("carrier threads"). When a virtual thread waits on network/DB I/O, it unmounts from the OS thread so the OS thread can run other virtual threads.
- **Structured Concurrency (Preview API):** Treats multiple concurrent subtasks spawned for one request as a **single unit of work**—if one subtask fails, the others are automatically cancelled and no background threads leak.

### Example 1: Platform Thread vs. Virtual Thread under Blocking I/O

```text
Traditional Platform Thread (1 request = 1 OS thread, ~1MB stack):
[OS Thread 1] ── calls DB (50ms) ── [BLOCKED / IDLE FOR 50ms] ── returns

Virtual Thread (millions possible, ~few KB heap stack):
[Virtual Thread A] ── calls DB (50ms) ──► unmounts!
[OS Carrier Thread] immediately picks up [Virtual Thread B] while A waits for DB!
```

### Example 2: Structured Concurrency (`ShutdownOnFailure`)

Fetching a user profile and their open orders in parallel for a single API response:

```java
// Using StructuredTaskScope (Preview API in Java 21+)
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User> userTask = scope.fork(() -> userClient.fetchUser(userId));
    Subtask<List<Order>> ordersTask = scope.fork(() -> orderClient.fetchOrders(userId));

    scope.join()            // Wait for both virtual threads
         .throwIfFailed();  // If userTask fails, ordersTask is automatically cancelled!

    return new UserDashboard(userTask.get(), ordersTask.get());
}
// Guarantee: When this try-block exits, ZERO child threads are left running in the background.
```

### Limitations to Watch Out For
1. **No benefit for CPU-bound work:** Video encoding, cryptography, or tight loops do not block on I/O, so virtual threads won't make them faster.
2. **Thread Pinning (Java 21–23):** If a virtual thread performs blocking I/O *inside a `synchronized` block* or native `JNI` call, it cannot unmount from its OS carrier thread ("pinned"). *Fix:* Replace `synchronized` around I/O with `ReentrantLock` (or upgrade to Java 24+, where JEP 491 fixed `synchronized` pinning).
3. **Downstream bottlenecks remain:** Starting 10,000 virtual threads that all query a database with a 20-connection HikariCP pool means 9,980 virtual threads will still wait for a DB connection. Use `Semaphore` or bulkheads to rate-limit concurrent calls.
4. **Never pool virtual threads:** Don't put them in a fixed thread pool (`Executors.newFixedThreadPool`). Use `Executors.newVirtualThreadPerTaskExecutor()`.
5. **Avoid heavy `ThreadLocal` caching:** Storing large objects in `ThreadLocal` across 500,000 virtual threads will blow up heap memory.

## 10. Explain the Java Memory Model and the happens-before relationship.

**Simple answer:** CPUs and the JIT compiler reorder instructions and cache variables in CPU registers for speed. The **Java Memory Model (JMM)** defines the exact rules for when a write by Thread A is **guaranteed to be visible** to Thread B. That guarantee is called **happens-before**.

### Example: The Bug Without `happens-before`

```java
class TaskRunner {
    int answer = 0;
    boolean ready = false; // NOT volatile!

    void writerThread() {
        answer = 42;   // (1)
        ready = true;  // (2) Compiler/CPU may reorder (2) before (1), or keep 'ready' in CPU cache!
    }

    void readerThread() {
        while (!ready) {
            // Might loop forever because it never sees ready = true!
        }
        // Even if it exits the loop, it might print 0 instead of 42!
        System.out.println(answer);
    }
}
```

### Fixing it with `volatile` (Creating a `happens-before` edge)

By making `volatile boolean ready = false;`, the JMM gives us two rules:
1. **Program Order Rule:** `answer = 42` *happens-before* `ready = true` (in `writerThread`).
2. **Volatile Variable Rule:** A write to `ready` *happens-before* every subsequent read of `ready` (in `readerThread`).
3. **Transitivity:** Therefore, `answer = 42` *happens-before* `System.out.println(answer)`—it is **100% guaranteed to print `42`**.

```text
Writer Thread:   [ answer = 42 ] ──(program order)──► [ volatile write: ready = true ]
                                                                   │
                                                         (volatile happens-before)
                                                                   ▼
Reader Thread:                                        [ volatile read: ready == true ] ──► [ reads answer (42) ]
```

### Other Key `happens-before` Rules
- **Monitor Lock Rule:** Unlocking a `synchronized` block or `Lock` *happens-before* any later locking of that same monitor.
- **Thread Start Rule:** Everything before `thread.start()` *happens-before* any code inside that new thread.
- **Thread Join Rule:** Everything inside a thread *happens-before* another thread returns from `thread.join()`.

## 11. Why is `volatile int count; count++` not thread-safe?

**Simple answer:** `volatile` guarantees **visibility** (threads read the latest value), but it does **not** guarantee **atomicity** for read-modify-write operations. `count++` is actually three separate CPU steps: Read, Add 1, Write.

### Step-by-Step Interleaving Trace (Lost Update)

Suppose `count` starts at `5` and two threads run `count++` at the same time:

| Time | Thread A | Thread B | `count` in Memory |
|---|---|---|---|
| `t1` | Reads `count` $\rightarrow$ sees `5` | | `5` |
| `t2` | | Reads `count` $\rightarrow$ also sees `5`! | `5` |
| `t3` | Computes `5 + 1 = 6` | Computes `5 + 1 = 6` | `5` |
| `t4` | Writes `6` to memory | | `6` |
| `t5` | | Writes `6` to memory (overwrites A's increment!) | `6` (Expected `7`!) |

### Code Fix: `AtomicInteger` or `LongAdder`

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

class SafeCounters {
    // Fix 1: AtomicInteger uses a hardware CAS (Compare-And-Swap) loop
    private final AtomicInteger atomicCount = new AtomicInteger(0);

    public int incrementAndGet() {
        return atomicCount.incrementAndGet(); // Atomic!
    }

    // Fix 2: LongAdder (Best when many threads increment heavily and you read the sum less often)
    private final LongAdder highThroughputCount = new LongAdder();

    public void recordRequest() {
        highThroughputCount.increment();
    }
}
```

## 12. Two threads update the same bank account simultaneously. How would you design a thread-safe solution?

**Simple answer:** The balance check (`balance >= amount`) and balance deduction (`balance -= amount`) must execute as a single atomic unit, both in memory (for a single JVM) and in the database (for multiple servers).

### Case 1: Single JVM (In-Memory) — Avoiding Deadlocks During Transfers

If Thread 1 transfers from **Account A $\rightarrow$ Account B** while Thread 2 transfers from **Account B $\rightarrow$ Account A**, locking `from` then `to` will **deadlock**. Always lock accounts in a **consistent order by ID**:

```java
class BankAccount {
    final long id;
    private long balanceCents; // Always store money in cents (long) or BigDecimal, never double!
    final Object lock = new Object();

    BankAccount(long id, long balanceCents) {
        this.id = id;
        this.balanceCents = balanceCents;
    }

    static void transfer(BankAccount from, BankAccount to, long amountCents) {
        if (from.id == to.id || amountCents <= 0) {
            throw new IllegalArgumentException("Invalid transfer");
        }
        // Always lock the smaller ID first to make deadlock impossible!
        BankAccount firstLock = from.id < to.id ? from : to;
        BankAccount secondLock = from.id < to.id ? to : from;

        synchronized (firstLock.lock) {
            synchronized (secondLock.lock) {
                if (from.balanceCents < amountCents) {
                    throw new IllegalStateException("Insufficient funds");
                }
                from.balanceCents -= amountCents;
                to.balanceCents += amountCents;
            }
        }
    }
}
```

### Case 2: Production Multi-Instance Service (Database Level)

In microservices or multiple app servers, JVM `synchronized` locks don't work across servers. You have three standard SQL/JPA patterns:

1. **Single Atomic SQL Update (Fastest for single account debit):**
   ```sql
   UPDATE accounts
   SET balance_cents = balance_cents - :amount
   WHERE id = :accountId AND balance_cents >= :amount;
   -- Check affected rows == 1. If 0, throw InsufficientFundsException!
   ```
2. **Pessimistic Locking (`SELECT ... FOR UPDATE`):**
   Lock the rows in ascending `id` order inside a `@Transactional` method so the second transaction waits until the first commits.
3. **Optimistic Locking (`@Version`):**
   Add `@Version private long version;` to the JPA entity. If two threads read `version = 3` and both try to update, the second executes `UPDATE ... WHERE id = 1 AND version = 3`, affects 0 rows, and throws `OptimisticLockLockingFailureException`.

## 14. How does Spring Boot auto-configuration work internally?

**Simple answer:** At startup, Spring Boot reads a list of pre-built configuration classes from its jars, checks `@Conditional...` rules (like "is `DataSource` on the classpath?" and "did the developer already define their own bean?"), and automatically registers only the beans whose conditions match.

### Step-by-Step Flow with `DataSourceAutoConfiguration` as an Example

1. `@SpringBootApplication` includes `@EnableAutoConfiguration`.
2. `AutoConfigurationImportSelector` reads candidate class names from:
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
3. One candidate is `DataSourceAutoConfiguration`. Spring evaluates its conditions:

```java
// Simplified view of what Spring Boot does internally:
@AutoConfiguration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class }) // 1. Is JDBC jar on classpath?
@EnableConfigurationProperties(DataSourceProperties.class)            // 2. Bind spring.datasource.* from application.yml
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class)                       // 3. Did the user already define a custom DataSource bean?
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")// 4. Is spring.datasource.url configured?
    public HikariDataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                         .type(HikariDataSource.class)
                         .build();
    }
}
```

### What happens if you define your own `@Bean DataSource`?
Because of `@ConditionalOnMissingBean(DataSource.class)`, Spring Boot sees your bean first and **backs off** (skips creating its default `HikariDataSource`).

**Pro Tip:** Run your app with `--debug` (or set `debug: true` in `application.yml`) to print the **Conditions Evaluation Report**, which shows every auto-configuration that matched (`Positive matches`) and why others were skipped (`Negative matches`).

## 15. Why might `@Transactional` fail when one method calls another method within the same class?

**Simple answer:** Spring implements `@Transactional` by wrapping your service in a **Proxy object**. External calls go through the Proxy (which starts and commits the transaction), but calling `this.otherMethod()` inside the same class calls the target object directly and **bypasses the Proxy**.

### Visual Diagram of the Proxy Bypass

```text
External Caller ──► [ OrderService$$SpringProxy ] (Starts TX if @Transactional is on entry method)
                             │
                             ▼
                    [ Real OrderService ]
                      placeOrder() {
                          this.saveAuditLog(); ──┐ (Direct 'this' call! Does NOT go back through Proxy!)
                      }                          │
                      @Transactional(REQUIRES_NEW)
                      saveAuditLog() ◄───────────┘ (REQUIRES_NEW is completely ignored!)
```

### Code Example: Problem and Fix

```java
@Service
public class OrderService {

    // PROBLEM: placeOrder() has NO @Transactional.
    // Calling this.createOrderRecord() bypasses the proxy, so NO transaction starts at all!
    public void placeOrder(OrderRequest req) {
        createOrderRecord(req); // <-- Runs WITHOUT a transaction!
    }

    @Transactional
    public void createOrderRecord(OrderRequest req) {
        // DB writes here are not wrapped in a transaction when called via this.createOrderRecord()
    }
}
```

### 3 Clean Ways to Fix It

1. **Extract to a separate Bean (Best practice):**
   Put `saveAuditLog()` or `createOrderRecord()` in a separate `@Service` (e.g., `OrderPersistenceService`) and inject it. Calls to another injected bean always go through its proxy.
2. **Put `@Transactional` on the outer public entry method** (if both methods should share the same transaction).
3. **Use `TransactionTemplate` programmatically** when you want explicit transaction boundaries inside one method without proxy magic:
   ```java
   transactionTemplate.executeWithoutResult(status -> {
       orderRepository.save(order);
   });
   ```

## 16. How would you design an idempotent payment API that prevents duplicate transactions during retries?

**Simple answer:** Require the client to send a unique `Idempotency-Key` header (UUID) for each payment intent, lock/insert that key in the database **before** calling the payment gateway, and return the saved response if the client retries with the same key.

### Concrete End-to-End Scenario

Suppose a mobile app calls `POST /api/payments` for $50 with `Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d`, and the user's Wi-Fi drops right before the HTTP response arrives. The app retries the exact same request 2 seconds later.

### Database Schema (`idempotency_records`)

```sql
CREATE TABLE idempotency_records (
    user_id         BIGINT       NOT NULL,
    idempotency_key VARCHAR(64)  NOT NULL,
    request_hash    VARCHAR(64)  NOT NULL, -- SHA-256 of (amount, currency, orderId)
    status          VARCHAR(20)  NOT NULL, -- IN_PROGRESS, COMPLETED, FAILED
    response_code   INT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, idempotency_key) -- Composite unique constraint!
);
```

### Step-by-Step Request Handling Flow

1. **Attempt 1 arrives:**
   - Insert `(user_id=42, key='9b1d...', hash='abc', status='IN_PROGRESS')`.
   - Call Stripe/Adyen and pass `'9b1d...'` as the gateway's own idempotency key too (so even if our server crashes and retries the gateway call, Stripe won't double-charge).
   - Gateway succeeds $\rightarrow$ update row to `status='COMPLETED'`, `response_code=201`, `response_body='{"paymentId":"pay_123","status":"SUCCESS"}'`.
2. **Attempt 2 arrives (Network Retry with same key):**
   - The `INSERT` hits the unique constraint `(user_id, idempotency_key)`.
   - We read the existing row:
     - **Case A (`request_hash` is different):** Client reused the same key for a $900 payment instead of $50! Return **`422 Unprocessable Entity`**.
     - **Case B (`status == 'IN_PROGRESS'`):** Attempt 1 is still running right now. Return **`409 Conflict`** (or `202 Accepted`) telling the client to retry shortly.
     - **Case C (`status == 'COMPLETED'`):** Return the cached `201` and `{"paymentId":"pay_123","status":"SUCCESS"}` immediately without touching the payment gateway.

## 17. An API's average response time is 150 ms, but P99 latency exceeds 3 seconds. How would you investigate?

**Simple answer:** Average latency hides outliers. If 99 requests take `100 ms` and 1 request takes `5,100 ms`, the average is `150 ms`, but **P99 is `5.1 seconds`**. To fix P99, you must inspect the slowest 1% of traces and correlate them with DB locks, connection pool waits, GC pauses, or "whale" users with huge data.

### Step-by-Step Investigation Checklist with Examples

1. **Look at Distributed Traces (OpenTelemetry / Jaeger / Datadog) for the >3s requests:**
   Compare a normal `120 ms` trace against a `3,200 ms` trace to see which span grew:
   ```text
   Normal Request (120 ms):
   [HTTP Controller: 120ms]
     └── [Auth: 5ms] + [Hikari Acquire: 1ms] + [SQL Query: 15ms] + [Shipping API: 95ms]

   Slow P99 Request (3,250 ms):
   [HTTP Controller: 3,250ms]
     └── [Auth: 5ms] + [Hikari Acquire: 2,900ms!] + [SQL Query: 250ms] + [Shipping API: 95ms]
   ```
   *In this example, the code wasn't slow—the thread waited 2.9 seconds just to borrow a DB connection!*

2. **Check the 5 Most Common P99 Culprits:**
   - **Data skew ("Whale" accounts):** Normal users have 10 orders; enterprise tenant #42 has 250,000 orders and triggers an unpaginated query or N+1 problem.
   - **Downstream retries:** An external API has a `1,500 ms` timeout. On the 1% of calls where it times out and retries once, total latency jumps to `1,500 ms + 200 ms = 1,700 ms+`.
   - **DB Lock Contention / Pool Exhaustion:** A background batch job locks rows or holds DB connections for 3 seconds every 5 minutes.
   - **JVM Stop-the-World GC Pauses or Container CPU Throttling:** Check `jvm_gc_pause_seconds_max` and Kubernetes `container_cpu_cfs_throttled_seconds_total`.
   - **Cold cache / synchronized lock:** 50 requests miss Redis at the same millisecond and queue up behind a lock (`cache stampede`).

## 18. How would you diagnose and fix the N+1 query problem in Hibernate?

**Simple answer:** The N+1 problem happens when 1 query fetches $N$ parent rows, and then looping over those parents triggers $N$ additional SQL queries to lazy-load each parent's child collection ($1 + N$ queries total).

### Code Example: How N+1 Happens

```java
@Entity
class Department {
    @Id Long id;
    String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    List<Employee> employees;
}

// In a Service method:
List<Department> depts = departmentRepo.findAll(); // Query #1: SELECT * FROM department (returns 50 rows)
for (Department d : depts) {
    // Queries #2 to #51: Runs SELECT * FROM employee WHERE department_id = ? FIFTY TIMES!
    System.out.println(d.getName() + ": " + d.getEmployees().size());
}
```

### How to Diagnose It
- Enable SQL logging in local/test (`logging.level.org.hibernate.SQL=DEBUG`) and see dozens of identical `SELECT ... WHERE department_id = ?` lines.
- Use **Datasource-Proxy** or **QuickPerf** in integration tests to assert `SQLQueryCount <= 2` so N+1 bugs fail the build automatically.

### 3 Ways to Fix It

```java
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Fix 1: JOIN FETCH (Loads departments + employees in 1 single SQL JOIN query)
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();

    // Fix 2: @EntityGraph (Cleaner annotation-based LEFT JOIN FETCH)
    @EntityGraph(attributePaths = "employees")
    List<Department> findByActiveTrue();

    // Fix 3: DTO Projection (Best when you only need a count or summary—avoids loading Employee entities at all!)
    @Query("SELECT new com.app.DeptSummaryDto(d.name, COUNT(e)) FROM Department d LEFT JOIN d.employees e GROUP BY d.id, d.name")
    List<DeptSummaryDto> findDepartmentSummaries();
}
```

- **Fix 4 (`@BatchSize(size = 50)` or `hibernate.default_batch_fetch_size: 50`):** Instead of 50 separate queries, Hibernate loads children using `WHERE department_id IN (?, ?, ..., ?)` (reduces 51 queries to just 2 queries, and works safely with `Pageable`!).

**Important pitfall:** Never combine `JOIN FETCH` on a `@OneToMany` collection directly with `Pageable` (`findAll(Pageable)`). Hibernate will load the **entire table into memory** and paginate in Java (`HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`). Instead, use `@BatchSize` or fetch a page of parent IDs first, then `JOIN FETCH` for those IDs.

## 19. HikariCP reports connection timeouts after 30 seconds. How would you identify the root cause?

**Simple answer:** `HikariPool-1 - Connection is not available, request timed out after 30000ms` means all connections in the pool were busy for a full 30 seconds (or the DB couldn't be reached). Typically, application code is either **holding `@Transactional` open during slow external HTTP calls**, **running locked/slow SQL**, or **leaking connections**.

### Classic Bug Example: Holding a DB Connection During an External HTTP Call

Suppose `maximumPoolSize = 10` and `stripeClient.charge()` takes 2 seconds:

```java
// BAD: @Transactional borrows a DB connection at method entry and holds it during the 2-second HTTP call!
// With 10 connections, the server can only handle 5 requests/sec before threads queue up and time out!
@Transactional
public void checkout(OrderRequest req) {
    Order order = orderRepo.save(new Order(req)); // Borrows DB connection here
    stripeClient.charge(req.cardToken());         // Holds DB connection idle for 2,000 ms!
    order.markPaid();
}
```

### Fix: Keep Remote I/O Outside the Database Transaction

```java
public void checkout(OrderRequest req) {
    Order order = orderTxService.createPendingOrder(req); // TX 1: takes 3 ms, releases connection immediately!
    PaymentResult res = stripeClient.charge(req.cardToken()); // Runs for 2,000 ms with ZERO DB connections held
    orderTxService.completeOrder(order.getId(), res);     // TX 2: takes 3 ms
}
```

### Step-by-Step Root Cause Checklist

1. **Check Hikari Metrics (Prometheus / Actuator):**
   - `hikaricp_connections_active == hikaricp_connections_max` and `hikaricp_connections_pending > 0` $\rightarrow$ Pool exhaustion (connections are held too long or leaked).
2. **Turn on Leak Detection (`spring.datasource.hikari.leak-detection-threshold: 10000`):**
   - If any thread holds a connection for $>10\text{s}$, Hikari logs the **exact stack trace** where the connection was borrowed.
3. **Check the Database (`pg_stat_activity` in Postgres):**
   - Are connections sitting in `idle in transaction`? $\rightarrow$ Java code is doing slow non-DB work (like HTTP/file I/O) inside `@Transactional`.
   - Are connections `active` on a specific query waiting on a lock? $\rightarrow$ Missing index or row lock contention.
4. **Fix Manual Connection Leaks:**
   - If any code calls `dataSource.getConnection()` directly, make sure it is inside a `try (Connection conn = dataSource.getConnection())` block.

## 20. When should a monolithic application be split into microservices, and when is a modular monolith preferable?

**Simple answer:** Start with a **Modular Monolith** (one deployable app split into strict domain packages/modules) and only extract a module into a separate **Microservice** when you have a concrete scaling, deployment, or team-ownership bottleneck.

| Criteria | Modular Monolith | Microservices |
|---|---|---|
| **Deployment** | Single deployment artifact (1 JAR/container) | Each service deployed independently |
| **Communication** | Fast, in-memory Java method calls / local events | Network calls (REST, gRPC, Kafka) with serialization & latency |
| **Transactions** | Simple ACID `@Transactional` across modules | Eventual consistency, Sagas, and Outbox pattern required |
| **Best when** | Startup / medium team, evolving domain boundaries, need fast delivery | 50+ engineers across independent teams, uneven scaling needs, strict blast-radius isolation |

### Concrete Example: E-Commerce App

- **Why a Modular Monolith wins early:** `Catalog`, `Cart`, `Order`, and `Billing` live in one Spring Boot app (`com.shop.catalog`, `com.shop.order`, etc., enforced via **Spring Modulith** or **ArchUnit** so `order` cannot directly query `billing`'s tables). You deploy one container and use normal DB transactions.
- **When to extract a Microservice:** On Black Friday, **Product Search (`Catalog`)** gets 50,000 reads/sec and needs 20 CPU-heavy instances, while **Checkout (`Order`)** gets 200 writes/sec. Or the Fraud Detection team needs Python/ML libraries and deploys 10 times a day. You extract *only* `Catalog` or `Fraud` into a separate service, rather than splitting the whole app into 25 tiny services ("distributed monolith").

## 21. Payment succeeds, but inventory reservation fails. How would you implement the Saga pattern?

**Simple answer:** Because Payment and Inventory live in separate services/databases, you cannot use a single DB rollback. Instead, a **Saga** executes a sequence of local transactions and runs a **compensating transaction (`refundPayment`)** if a later step (`reserveInventory`) fails.

### Visual Flow: Orchestrated Saga (Happy Path vs. Compensation Path)

```text
Happy Path:
[Order Saga] ──1. chargeCard()──► [Payment Service] (Success: $100 charged)
     │
     └──2. reserveStock()────────► [Inventory Service] (Success: 1 iPhone reserved)
     │
     └──3. markOrderConfirmed()

Failure Path (Inventory Out of Stock):
[Order Saga] ──1. chargeCard()──► [Payment Service] (Success: $100 charged)
     │
     └──2. reserveStock()────────► [Inventory Service] (FAILS: Out of stock!)
     │
     └──3. refundPayment()──────► [Payment Service] (Compensating Action: $100 refunded!)
     │
     └──4. markOrderCancelled("Out of stock, payment refunded")
```

### Code Example: Saga Orchestrator State Machine

```java
@Transactional
public void onInventoryReservationFailed(InventoryFailedEvent event) {
    OrderSagaState saga = sagaRepo.findByOrderId(event.orderId());
    if (saga.getStatus() == SagaStatus.COMPENSATING || saga.getStatus() == SagaStatus.CANCELLED) {
        return; // Idempotent guard: ignore duplicate failure events!
    }

    saga.setStatus(SagaStatus.COMPENSATING);
    // Write to transactional outbox so the Refund command is guaranteed to be sent to Kafka
    outboxRepo.save(new OutboxMessage(
        "payment-commands",
        event.orderId(),
        new RefundPaymentCommand(event.orderId(), saga.getPaymentId(), saga.getAmountCents())
    ));
}
```

### Key Production Rules for Sagas
1. **Compensations must be idempotent and retried until they succeed:** If `PaymentService` is temporarily down when `RefundPaymentCommand` arrives, Kafka retries it; the `paymentId` ensures the customer is refunded once, not twice.
2. **Better design when possible (Pivot Transaction):** Reserve inventory *first* (or place a temporary 10-minute hold on the stock) and charge the credit card *second*. Releasing an inventory hold is free and instant, whereas refunding a credit card costs payment-gateway fees and annoys customers!

## 22. A Kafka consumer updates the database successfully but crashes before committing its offset. How would you prevent duplicate business operations?

**Simple answer:** Kafka is **at-least-once** by default: when the consumer restarts, it will read the same message again. To prevent applying the business operation twice, insert the message's unique `event_id` into a **`processed_events` table inside the exact same `@Transactional` database transaction** as the business update.

### Failure Timeline Without Deduplication

1. Kafka delivers `OrderPaidEvent(eventId = "evt-99", orderId = 500)`.
2. Consumer subtracts 1 item from `inventory` table and commits the DB transaction.
3. **Consumer pod crashes (OOM / redeploy) 5ms before `ack.acknowledge()` commits the Kafka offset.**
4. New consumer pod starts up, reads `"evt-99"` again, and subtracts a **2nd** item from `inventory`!

### Code Example: Atomic DB Transaction + `processed_events` Table

```sql
CREATE TABLE processed_events (
    event_id     VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

```java
@Component
public class InventoryKafkaConsumer {

    @KafkaListener(topics = "order-paid-events", groupId = "inventory-service")
    public void onMessage(OrderPaidEvent event, Acknowledgment ack) {
        inventoryService.processOrderPaidIdempotently(event);
        ack.acknowledge(); // Commit Kafka offset ONLY after DB transaction commits!
    }
}

@Service
public class InventoryService {

    @Transactional
    public void processOrderPaidIdempotently(OrderPaidEvent event) {
        // PostgreSQL: INSERT INTO processed_events(event_id) VALUES (:id) ON CONFLICT DO NOTHING
        int insertedRows = processedEventRepo.insertIfAbsent(event.eventId());
        if (insertedRows == 0) {
            // Event "evt-99" was already processed before the crash! Skip safely.
            return;
        }

        // Runs in the SAME database transaction as insertIfAbsent:
        // Either BOTH the event_id record and stock deduction commit, or NEITHER commits!
        inventoryRepo.decrementStock(event.productId(), event.quantity());
    }
}
```

## 23. Explain at-most-once, at-least-once and exactly-once processing. What limitations exist across databases and external systems?

**Simple answer:** These three terms describe what happens when a network timeout or crash occurs during message processing.

| Mode | How the Consumer Works | Real-World Example | Risk |
|---|---|---|---|
| **At-most-once** | Commit offset **before** processing the message (`ack` $\rightarrow$ `process`). | IoT temperature sensor telemetry or non-critical clickstream analytics. | **Data loss:** If the app crashes during `process()`, the message is never retried. |
| **At-least-once** | Process the message **first**, then commit the offset (`process` $\rightarrow$ `ack`). | Standard Kafka/RabbitMQ microservices. | **Duplicates:** If the app crashes after `process()` but before `ack`, the message is redelivered. |
| **Exactly-once (Effectively-once)** | **At-least-once delivery + Idempotent receiver** (or transactional stream processing). | Banking ledger transfers, order billing, inventory deduction. | Higher complexity; requires deduplication across boundaries. |

### Concrete Example of the External System Limitation

Kafka supports native **Exactly-Once Semantics (EOS)** when reading from one Kafka topic, transforming data, and writing to another Kafka topic (using transactional producers + `isolation.level=read_committed`).

**Why Kafka EOS does NOT automatically cover Postgres or Stripe/Twilio:**
```text
[Kafka Topic] ──► [Your Consumer] ──1. Sends SMS via Twilio API──► (Customer gets SMS)
                        │
                        └──2. Writes to Postgres DB ──► (CRASH before Kafka offset commit!)
```
Kafka cannot "roll back" an SMS that Twilio already sent to a phone, nor does Kafka share a two-phase commit transaction with your Postgres database.

**How to achieve "Effectively-Once" with external systems:**
- **For Databases:** Commit the `event_id` and business state in one local DB transaction (Question 22).
- **For External REST APIs (Stripe/Twilio):** Pass a deterministic `Idempotency-Key` (e.g., `eventId`) in the HTTP request so the external provider ignores duplicate retries.
- **For Publishing Events After a DB Write:** Write the event to a DB `outbox` table in the same DB transaction, and have a relay (or Debezium CDC) publish it to Kafka.

## 24. Find the longest subarray with sum K when negative numbers are allowed. Explain the optimal algorithm.

**Simple answer:** Use **Prefix Sums + a `HashMap`** storing the **earliest index** at which each prefix sum occurred. If the running sum at index `i` is `sum`, and we previously saw `sum - k` at index `j`, then the subarray from `j + 1` to `i` sums to `k` and has length `i - j`.

### Why Sliding Window (Two Pointers) Fails with Negative Numbers
In a sliding window (`[4, -2, 1]`, `k = 3`), moving the left pointer forward or right pointer forward is supposed to predictably decrease or increase the sum. When negative numbers exist, adding a number can *decrease* the sum and removing a number can *increase* the sum, so two pointers miss valid subarrays.

### Step-by-Step Dry Run (`nums = [1, -1, 5, -2, 3]`, `k = 3`)

Initialize `firstIndex = { 0 : -1 }`, `sum = 0`, `longest = 0`.

| Index `i` | `nums[i]` | Running `sum` | Needed `sum - k` | Is `sum - k` in Map? | Subarray & Length (`i - earlier`) | ` longest` | Map `firstIndex` after `putIfAbsent` |
|---|---|---|---|---|---|---|---|
| `0` | `1` | `1` | `1 - 3 = -2` | No | — | `0` | `{0:-1, 1:0}` |
| `1` | `-1` | `0` | `0 - 3 = -3` | No | — | `0` | `{0:-1, 1:0}` *(keep `0:-1`, don't overwrite with `1`!)* |
| `2` | `5` | `5` | `5 - 3 = 2` | No | — | `0` | `{0:-1, 1:0, 5:2}` |
| `3` | `-2` | `3` | `3 - 3 = 0` | **Yes (`-1`)** | `nums[0..3] = [1,-1,5,-2]`, len `3 - (-1) = 4` | **`4`** | `{0:-1, 1:0, 5:2, 3:3}` |
| `4` | `3` | `6` | `6 - 3 = 3` | **Yes (`3`)** | `nums[4..4] = [3]`, len `4 - 3 = 1` | **`4`** | `{0:-1, 1:0, 5:2, 3:3, 6:4}` |

Notice at `i = 1`, `sum` became `0` again. Because we used `putIfAbsent(0, 1)`, we kept `0 -> -1`. That allowed `i = 3` to find a subarray of length `4` instead of length `2`!

### Java Implementation

```java
import java.util.*;

public class LongestSubarraySumK {
    public static int longestSubarrayWithSumK(int[] nums, long k) {
        // Maps: prefixSum -> earliest index where this prefixSum occurred
        Map<Long, Integer> firstIndex = new HashMap<>();
        firstIndex.put(0L, -1); // Base case: a prefix sum of 0 exists before index 0

        long sum = 0;
        int longest = 0;

        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];

            Integer earlierIndex = firstIndex.get(sum - k);
            if (earlierIndex != null) {
                longest = Math.max(longest, i - earlierIndex);
            }

            // Only store the FIRST time we see this sum so (i - earlierIndex) is maximized
            firstIndex.putIfAbsent(sum, i);
        }
        return longest;
    }
}
```

**Complexity:** **$O(n)$ Time** (single pass through the array) and **$O(n)$ Space** (at most $n+1$ distinct prefix sums in the `HashMap`).

---

# Additional Interview Questions by Round

## Round 1: Phone Screen (Pass)

### 25. Reverse a linked list, iteratively and recursively.

**Simple answer:** Walk through the list and flip each node's `next` arrow so it points to the `previous` node instead of the `next` node.

### Visual Pointer Walk (Iterative on `1 -> 2 -> 3 -> null`)

```text
Initial:   prev = null,   curr = [1] -> [2] -> [3] -> null
Step 1:    null <- [1]    prev = [1],   curr = [2] -> [3] -> null
Step 2:    null <- [1] <- [2]           prev = [2],   curr = [3] -> null
Step 3:    null <- [1] <- [2] <- [3]    prev = [3],   curr = null (Done! Return prev)
```

### Java Code (Both Iterative and Recursive)

The following `ListNode` class is shared by Questions 25–27:

```java
static final class ListNode {
    final int value;
    ListNode next;

    ListNode(int value) {
        this.value = value;
    }
}

// 1. Iterative: O(n) Time, O(1) Space (Preferred in production)
static ListNode reverseIterative(ListNode head) {
    ListNode previous = null;
    ListNode current = head;

    while (current != null) {
        ListNode nextTemp = current.next; // 1. Save next node before breaking link
        current.next = previous;          // 2. Reverse the pointer backward
        previous = current;               // 3. Advance previous
        current = nextTemp;               // 4. Advance current
    }
    return previous; // New head of the reversed list
}

// 2. Recursive: O(n) Time, O(n) Call Stack Space
static ListNode reverseRecursive(ListNode head) {
    if (head == null || head.next == null) {
        return head; // Base case: empty list or last node becomes the new head
    }
    // Reverse everything after head: 1 -> (2 <- 3), where newHead is 3
    ListNode newHead = reverseRecursive(head.next);

    head.next.next = head; // Make node 2 point back to node 1
    head.next = null;      // Cut node 1's old forward link so there's no cycle!
    return newHead;
}
```

### 26. Find the middle node of a linked list.

**Simple answer:** Use the **Tortoise and Hare (Slow & Fast Pointer)** technique: `slow` moves 1 step at a time while `fast` moves 2 steps at a time. When `fast` reaches the end, `slow` is at the exact middle.

### Step-by-Step Pointer Trace

**Case A: Odd length (`1 -> 2 -> 3 -> 4 -> 5`)**
- Start: `slow = 1`, `fast = 1`
- After 1st hop: `slow = 2`, `fast = 3`
- After 2nd hop: `slow = 3`, `fast = 5` (`fast.next == null` $\rightarrow$ loop stops, returns **`3`**)

**Case B: Even length (`1 -> 2 -> 3 -> 4`)**
- Start: `slow = 1`, `fast = 1`
- After 1st hop: `slow = 2`, `fast = 3`
- After 2nd hop: `slow = 3`, `fast = null` (`fast == null` $\rightarrow$ loop stops, returns **`3`**, the second middle node)

```java
// Returns the middle node (for even length, returns the 2nd middle node)
static ListNode middleNode(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;      // 1 step
        fast = fast.next.next; // 2 steps
    }
    return slow;
}
```
*(Tip: If an interviewer asks for the **1st middle node** on even-length lists—so `1 -> 2 -> 3 -> 4` returns `2`—just start with `ListNode fast = head.next;` after checking `head != null`.)*

**Complexity:** **$O(n)$ Time** and **$O(1)$ Space**.

### 27. Reverse a linked list in groups of k.

**Simple answer:** Count `k` nodes ahead. If a full group of `k` nodes exists, reverse those `k` nodes in place and stitch the reversed group back to the previous group's tail and the next group's head. If fewer than `k` nodes remain at the end, leave them as-is.

### Visual Walkthrough (`1 -> 2 -> 3 -> 4 -> 5`, `k = 2`)

```text
Initial:      [Dummy: 0] -> [1 -> 2] -> [3 -> 4] -> [5]
                            (Group 1)   (Group 2)   (< k nodes: leave alone)

After Grp 1:  [Dummy: 0] -> [2 -> 1] -> [3 -> 4] -> [5]
                             ▲    ▲
                      new head    oldHead (becomes groupPrevious for Group 2!)

After Grp 2:  [Dummy: 0] -> [2 -> 1] -> [4 -> 3] -> [5]
```

### Java Implementation ($O(n)$ Time, $O(1)$ Space)

```java
static ListNode reverseKGroup(ListNode head, int k) {
    if (k <= 0) {
        throw new IllegalArgumentException("k must be positive");
    }
    if (head == null || k == 1) {
        return head;
    }

    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode groupPrevious = dummy;

    while (true) {
        // 1. Check if at least k nodes remain
        ListNode kth = groupPrevious;
        for (int i = 0; i < k && kth != null; i++) {
            kth = kth.next;
        }
        if (kth == null) {
            break; // Fewer than k nodes left; leave the remainder untouched
        }

        // 2. Reverse the k nodes between groupPrevious.next and kth
        ListNode groupNext = kth.next;
        ListNode oldGroupHead = groupPrevious.next;
        ListNode previous = groupNext; // Notice: the new tail will automatically point to groupNext!
        ListNode current = oldGroupHead;

        while (current != groupNext) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        // 3. Stitch the reversed group back into the main list
        groupPrevious.next = kth;     // kth is now the head of this reversed group
        groupPrevious = oldGroupHead; // oldGroupHead is now the tail of this reversed group
    }
    return dummy.next;
}
```

### 28. Explain the CAP theorem and ACID properties.

**Simple answer:**
- **CAP Theorem** applies to **distributed data stores** when a network cable/switch fails between nodes (**Partition**): you must choose between returning a guaranteed up-to-date value (**Consistency**) or letting every working node answer requests even if its data might be slightly stale (**Availability**).
- **ACID** applies to **database transactions**, ensuring that a multi-step read/write leaves the database valid even under crashes or concurrency.

### Real-World CAP Example: ATM Withdrawal During a Network Split

Suppose a bank has two database nodes (`Node US-East` and `Node US-West`) holding your `$100` balance, and the network link between them breaks (**Network Partition `P`**):
- **If the bank chooses `CP` (Consistency + Partition Tolerance):** Until the two nodes can talk to reach quorum, `Node US-West` **rejects** your ATM withdrawal with *"Service Temporarily Unavailable"*. You can't withdraw money right now, but you can never overdraft `$100` twice.
- **If the bank chooses `AP` (Availability + Partition Tolerance):** Both nodes stay up and let you withdraw `$100` on each coast (`$200` total withdrawn!), and the bank has to reconcile the negative balance later. (Conversely, for a **product catalog** or **social media feed**, `AP` is great—showing a post 2 seconds late is much better than taking the site down!)

### Concrete ACID Breakdown (Bank Transfer: Move $50 from Alice to Bob)

| Property | What it means | Concrete Example in the $50 Transfer |
|---|---|---|
| **Atomicity** | All statements in the transaction succeed, or **all are rolled back**. | If `UPDATE accounts SET balance = balance - 50 WHERE user = 'Alice'` succeeds, but crediting Bob fails due to a constraint error, Alice's `$50` is automatically restored. |
| **Consistency** | A transaction moves the DB from one valid state to another, obeying all constraints (`CHECK`, `FOREIGN KEY`, `UNIQUE`). | A `CHECK (balance >= 0)` constraint rejects the entire transaction if Alice only had `$30`. |
| **Isolation** | Concurrent transactions do not corrupt each other's in-flight state. | If Alice transfers `$50` on her phone and `$50` on her laptop at the exact same millisecond, row locks or `SERIALIZABLE` isolation stop both from reading the same initial `$50` balance. |
| **Durability** | Once `COMMIT` returns success, the change survives a power outage. | Postgres writes the committed change to the disk **WAL (Write-Ahead Log)** before returning success. |

### 29. Walk through your project. How do transactions work?

**Simple answer:** When answering this in an interview, use a crisp **60-second story**: state the business problem, walk through one real write API from Controller to DB, show exactly where `@Transactional` sits, and explain one concurrency/rollback edge case you handled.

### Concrete Template Example You Can Adapt (Order Checkout Flow)

```java
@Service
public class CheckoutService {

    private final OrderRepository orderRepo;
    private final InventoryRepository inventoryRepo;
    private final OutboxRepository outboxRepo;

    // Single transaction boundary around all DB writes for this use case!
    @Transactional
    public OrderResponse placeOrder(Long userId, CreateOrderRequest req) {
        // 1. Lock & decrement inventory row (SELECT ... FOR UPDATE or atomic UPDATE)
        int updated = inventoryRepo.decrementIfAvailable(req.productId(), req.quantity());
        if (updated == 0) {
            throw new OutOfStockException("Product " + req.productId() + " is out of stock");
        }

        // 2. Persist the order
        Order order = orderRepo.save(new Order(userId, req.productId(), req.quantity(), "CONFIRMED"));

        // 3. Save an Outbox event in the SAME transaction (so Kafka publishing is reliable)
        outboxRepo.save(OutboxEvent.forOrderCreated(order));

        return OrderResponse.from(order);
        // 4. When method returns normally, Spring's TransactionInterceptor flushes JPA & commits!
    }
}
```

### How to Explain What Happens Under the Hood
1. **Proxy Entry:** Controller calls `checkoutService.placeOrder()`. Spring's CGLIB proxy intercepts the call, borrows a JDBC connection from HikariCP, and sets `connection.setAutoCommit(false)`.
2. **Shared Connection:** `inventoryRepo`, `orderRepo`, and `outboxRepo` all reuse the **exact same JDBC connection** bound to the current thread (`ThreadLocal` via `TransactionSynchronizationManager`).
3. **Rollback on Failure:** If `outboxRepo.save()` throws a `RuntimeException`, the proxy catches it, calls `connection.rollback()`, and the inventory deduction and order row are completely undone.
4. **Why we don't call Kafka/Stripe directly inside `@Transactional`:** If we sent a Kafka message on line 2 and the DB commit failed on line 4, downstream services would receive a "ghost" order event that doesn't exist in the DB! Writing to the `outbox` table solves this.

---

## Round 2: Spring Boot Development (Pass)

### 30. Build two new APIs in the provided Spring Boot project, end to end.

**Simple answer:** In a live coding round with an existing Spring Boot repo, interviewers evaluate whether you follow existing conventions, separate DTOs from Entities, enforce ownership/validation, and write clean Controller $\rightarrow$ Service $\rightarrow$ Repository code.

### Complete End-to-End Code Example (`POST /api/tasks` & `GET /api/tasks`)

```java
// 1. Request & Response DTOs (Java records with Bean Validation)
public record CreateTaskRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be <= 200 chars")
    String title
) {}

public record TaskResponse(Long id, String title, String status, Instant createdAt) {
    static TaskResponse from(TaskEntity e) {
        return new TaskResponse(e.getId(), e.getTitle(), e.getStatus(), e.getCreatedAt());
    }
}

// 2. Repository Layer (Scopes queries to the authenticated ownerId!)
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Page<TaskEntity> findByOwnerId(Long ownerId, Pageable pageable);
}

// 3. Service Layer (Owns transactions and business logic)
@Service
public class TaskService {
    private final TaskRepository taskRepo;

    public TaskService(TaskRepository taskRepo) { this.taskRepo = taskRepo; }

    @Transactional
    public TaskResponse createTask(Long ownerId, CreateTaskRequest req) {
        TaskEntity entity = new TaskEntity(ownerId, req.title().trim(), "OPEN", Instant.now());
        return TaskResponse.from(taskRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasks(Long ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(
            page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        return taskRepo.findByOwnerId(ownerId, pageable).map(TaskResponse::from);
    }
}

// 4. Controller Layer (HTTP status codes, validation, principal extraction)
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateTaskRequest req) {
        TaskResponse created = taskService.createTask(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<TaskResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return taskService.listTasks(user.getId(), page, size);
    }
}
```

### 31. How do you paginate the results?

**Simple answer:** Never load the full table into memory (`findAll()`) and slice it in Java. Push pagination down to the database using either **Offset Pagination (`LIMIT / OFFSET`)** for normal pages or **Keyset / Cursor Pagination (`WHERE (created_at, id) < (?, ?)`)** for deep pages.

### Method 1: Offset Pagination (`Pageable` / `Page<T>`)

- **Request:** `GET /api/tasks?page=2&size=20`
- **Generated SQL:**
  ```sql
  SELECT id, title, status, created_at
  FROM tasks
  WHERE owner_id = 42
  ORDER BY created_at DESC, id DESC
  LIMIT 20 OFFSET 40;
  ```
- **Why include `id DESC` in `ORDER BY`?** If 5 tasks were created at the exact same millisecond (`created_at`), sorting only by `created_at DESC` is non-deterministic—the database can return the same row on both Page 0 and Page 1! Adding a unique tie-breaker (`id DESC`) guarantees stable ordering.
- **`Page<T>` vs. `Slice<T>`:** `Page<T>` runs an extra `SELECT COUNT(*)` query to calculate `totalElements` and `totalPages`. If the UI only has a *"Load More"* button (infinite scroll), return `Slice<T>` to skip the expensive `COUNT(*)` query!

### Method 2: Keyset / Cursor Pagination (For Millions of Rows)

With `OFFSET 100000`, the database still has to scan and discard 100,000 rows before returning 20. **Cursor pagination** uses the last item of the previous page (`lastCreatedAt = '2026-10-29T10:00:00Z'`, `lastId = 850`) to jump straight there via an index on `(owner_id, created_at DESC, id DESC)`:

```sql
SELECT id, title, status, created_at
FROM tasks
WHERE owner_id = 42
  AND (created_at, id) < ('2026-10-29T10:00:00Z', 850)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

### 32. How do you split the data layer and the service layer?

**Simple answer:**
- **Controller Layer:** Speaks **HTTP** (headers, status codes, JSON validation, auth principal).
- **Service Layer:** Speaks **Business Use Cases & Transactions** (checks rules, coordinates multiple repositories, maps Entities $\leftrightarrow$ DTOs).
- **Repository / Data Layer:** Speaks **Persistence / SQL** (saves entities and executes queries).

### Side-by-Side Example: Bad (Mixed Layers) vs. Good (Clean Separation)

```java
// BAD: Controller talks directly to Repository and returns JPA Entity in HTTP response!
@PostMapping("/orders/{id}/cancel")
public OrderEntity cancelOrderBad(@PathVariable Long id) {
    OrderEntity order = orderRepository.findById(id).orElseThrow();
    order.setStatus("CANCELLED"); // Leaks business rules into Controller; no @Transactional!
    return orderRepository.save(order); // Exposes internal DB schema & lazy-loading traps in JSON!
}

// GOOD: Controller -> Service -> Repository
@PostMapping("/orders/{id}/cancel")
public OrderDto cancelOrderGood(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
    return orderService.cancelOrder(user.getId(), id);
}

// Inside OrderService:
@Transactional
public OrderDto cancelOrder(Long userId, Long orderId) {
    OrderEntity order = orderRepository.findByIdAndOwnerId(orderId, userId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    order.cancel(); // Domain logic inside the entity: throws IllegalStateException if already SHIPPED!
    inventoryRepository.restoreStock(order.getProductId(), order.getQuantity());
    return OrderDto.from(order);
}
```

### 33. How do you handle exceptions globally?

**Simple answer:** Use a `@RestControllerAdvice` class with `@ExceptionHandler` methods so every controller returns a consistent, structured JSON error (such as RFC 7807 `ProblemDetail` in Spring Boot 3) without littering controllers with `try-catch` blocks.

### Complete `@RestControllerAdvice` Example

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Custom 404 Not Found
    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleNotFound(TaskNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }

    // 2. 400 Bad Request for @Valid field validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request validation failed"
        );
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    // 3. 409 Conflict for Optimistic Locking collisions
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleConflict(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, "The resource was modified by another request. Please retry."
        );
    }

    // 4. 500 Fallback — Log the full stack trace internally, but NEVER leak SQL/stack traces to the client!
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."
        );
    }
}
```

### Example JSON Response Produced (`400 Bad Request`)

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/tasks",
  "errors": {
    "title": "Title is required"
  }
}
```

### 34. How do you secure and rate limit these APIs?

**Simple answer:**
- **Security:** Authenticate the caller with **Spring Security** (e.g., stateless JWT verification), authorize both by **role (`@PreAuthorize`)** and **data ownership (`WHERE owner_id = :userId`)**, and validate all inputs.
- **Rate Limiting:** Use the **Token Bucket** algorithm (e.g., **Bucket4j + Redis** or an API Gateway) keyed by `userId` (or client IP for login endpoints) to return `429 Too Many Requests` when a client floods the API.

### Example 1: Preventing BOLA / IDOR (Broken Object Level Authorization)

Validating a JWT only proves *who* the caller is (`userId = 99`). It does **not** stop `userId = 99` from calling `DELETE /api/tasks/500` where task `500` belongs to `userId = 42`!

```java
// INSECURE (IDOR vulnerability — any logged-in user can delete anyone's task!):
taskRepository.deleteById(taskId);

// SECURE (Enforces ownership right in the SQL query):
int deleted = taskRepository.deleteByIdAndOwnerId(taskId, currentUser.getId());
if (deleted == 0) {
    throw new TaskNotFoundException(taskId); // Returns 404 so we don't even leak whether ID 500 exists
}
```

### Example 2: Token Bucket Rate Limiting Filter (Conceptual Flow)

Suppose each user gets a bucket of **20 tokens**, refilling at **10 tokens/second** in Redis:

```java
public boolean allowRequest(String userId, HttpServletResponse response) throws IOException {
    ConsumptionProbe probe = redisRateLimiter.tryConsumeAndReturnRemaining(userId, 1);
    if (probe.isConsumed()) {
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        return true;
    }
    long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
    response.setHeader("Retry-After", String.valueOf(waitSeconds));
    return false;
}
```

**Why Redis instead of a local `ConcurrentHashMap` in Java?**
If you run **5 Spring Boot pods** behind a load balancer and store counters in memory, a user with a 20 req/sec limit can actually send `5 × 20 = 100 req/sec`! Centralizing the bucket state in Redis enforces the true global limit across all pods.

### 35. How is your code structured, and why?

**Simple answer:** Organize the top level **by domain feature (`task`, `order`, `payment`)** rather than dumping 200 unrelated classes into one giant `controllers/` and `services/` folder, and keep strict layer responsibilities inside each feature.

### Concrete Package Layout Example

```text
com.acme.workhub
├── common
│   ├── error          (GlobalExceptionHandler, custom base exceptions)
│   └── security       (SecurityConfig, JwtAuthenticationFilter, UserPrincipal)
├── task               <-- Everything related to Tasks lives together!
│   ├── TaskController.java
│   ├── TaskService.java
│   ├── TaskRepository.java
│   ├── TaskEntity.java
│   └── dto
│       ├── CreateTaskRequest.java
│       └── TaskResponse.java
└── notification
    ├── NotificationService.java
    └── ...
```

### Why Interviewers Like Package-by-Feature:
1. **High cohesion & visibility control:** `TaskRepository` and `TaskEntity` can be **package-private** (no `public` keyword!), so `NotificationService` is prevented by the Java compiler from bypassing `TaskService` and querying `TaskRepository` directly!
2. **Easy navigation & refactoring:** When you modify the `task` feature, every file you touch is in one folder.
3. **Microservice-ready:** If `task` ever needs to be extracted into its own microservice, its boundaries are already cleanly isolated.

---

## Round 3: DSA and Debugging (Pass)

### 36. Reverse the words in a string in place, in Java, with no extra space.

**Simple answer:**
1. First clarify a key Java fact: **`java.lang.String` is immutable**, so you cannot modify a `String` in $O(1)$ space. To do it in place with **$O(1)$ extra space**, the input must be passed as a mutable **`char[]`**.
2. **Algorithm (2-Step Reversal):**
   - **Step 1:** Reverse the entire `char[]` from index `0` to `n - 1`.
   - **Step 2:** Scan through the array and reverse each individual word back to normal spelling.

### Visual Step-by-Step Trace

```text
Input char[]:              ['J','a','v','a',' ','i','s',' ','f','u','n']  ("Java is fun")

Step 1 (Reverse whole):    ['n','u','f',' ','s','i',' ','a','v','a','J']  ("nuf si avaJ")
                            \_________/     \_____/     \_____________/
                             Word 1 (0..2)  Word 2 (4..5) Word 3 (7..10)

Step 2 (Reverse each word):['f','u','n',' ','i','s',' ','J','a','v','a']  ("fun is Java")
```

### Java Code ($O(n)$ Time, $O(1)$ Extra Space)

```java
public class ReverseWordsInPlace {

    public static void reverseWords(char[] text) {
        if (text == null || text.length <= 1) {
            return;
        }
        // Step 1: Reverse the entire character array
        reverseRange(text, 0, text.length - 1);

        // Step 2: Find each word [start..end-1] and reverse it individually
        int start = 0;
        while (start < text.length) {
            if (text[start] == ' ') {
                start++;
                continue;
            }
            int end = start;
            while (end < text.length && text[end] != ' ') {
                end++;
            }
            reverseRange(text, start, end - 1);
            start = end;
        }
    }

    private static void reverseRange(char[] text, int left, int right) {
        while (left < right) {
            char temp = text[left];
            text[left++] = text[right];
            text[right--] = temp;
        }
    }
}
```

### Bonus Follow-up: "What if there are leading, trailing, or multiple spaces between words?"
If `"  hello   world  "` must become `"world hello"`, you add a 3rd in-place step using a `read` and `write` pointer to compact extra spaces to the left (and return the new logical length `write`).

### 37. What is the time complexity of these loops?

**Simple answer:** Don't guess based on how many `for`/`while` keywords are nested. Count the **total number of times the innermost statement executes** as a function of $n$.

### 6 Classic Interview Loop Snippets & Their Exact Math

#### Snippet 1: Logarithmic Halving / Doubling — $O(\log n)$
```java
for (int i = 1; i < n; i = i * 2) {
    // i takes values: 1, 2, 4, 8, 16, ..., 2^k < n
    // Stops when 2^k >= n  ==>  k = log2(n) iterations!
}
```

#### Snippet 2: Dependent Triangular Loop — $O(n^2)$
```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < i; j++) {
        // Runs 0 + 1 + 2 + ... + (n - 1) = n(n - 1) / 2 times = O(n^2)
    }
}
```

#### Snippet 3: Doubling Outer Loop + Linear Inner Loop (Geometric Series) — $O(n)$ (Trick!)
```java
for (int i = 1; i < n; i = i * 2) {
    for (int j = 0; j < i; j++) {
        // Inner loop runs: 1 + 2 + 4 + 8 + ... + n/2 times.
        // Sum of geometric series = 2n - 1  ==>  O(n), NOT O(n log n)!
    }
}
```

#### Snippet 4: Harmonic Series Step (`j += i`) — $O(n \log n)$
```java
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= n; j += i) {
        // For i=1: runs n/1 times
        // For i=2: runs n/2 times
        // For i=3: runs n/3 times ...
        // Total = n * (1 + 1/2 + 1/3 + ... + 1/n) = n * H_n = O(n log n)!
    }
}
```

#### Snippet 5: Two-Pointer / Monotonic Inner Pointer — $O(n)$ (Trick!)
```java
int j = 0; // Initialized OUTSIDE the outer loop!
for (int i = 0; i < n; i++) {
    while (j < n && arr[j] < arr[i]) {
        j++; // j only moves forward and NEVER resets to 0!
    }
}
// j can increment at most n times across the ENTIRE outer loop ==> O(n) total!
```

#### Snippet 6: Square Root Bound — $O(\sqrt{n})$
```java
for (int i = 1; i * i <= n; i++) {
    // Stops when i > sqrt(n)  ==>  O(sqrt(n))
}
```

### 38. How do you debug a Java client?

**Simple answer:** Isolate whether the bug is **before the network call** (bad input/serialization), **over the network** (DNS, TLS, timeout, HTTP 4xx/5xx), or **after the call** (deserialization, thread hang, race condition), using logs, correlation IDs, IDE breakpoints, and thread dumps.

### Concrete Debugging Scenarios & Exact Tools to Use

#### Scenario A: REST Client Throws `HttpClientErrorException$BadRequest (400)` or `JsonMappingException`
1. Enable wire/HTTP logging on the client (e.g., `logging.level.org.apache.hc.client5.http.wire=DEBUG` or Spring `ClientHttpRequestInterceptor`) in local/staging, redacting `Authorization` headers.
2. Put a **conditional breakpoint** in the IDE right before `restClient.post()` (e.g., condition: `orderId == 1042L`) to inspect the serialized JSON payload and headers.
3. Pass and log an `X-Correlation-Id` header so you can grep the exact matching stack trace in the server logs.

#### Scenario B: Java Client Hangs / Freezes Without Throwing an Exception
1. Find the JVM Process ID:
   ```bash
   jps -l
   ```
2. Capture 3 **thread dumps** spaced 5 seconds apart:
   ```bash
   jstack <pid> > dump1.txt
   ```
3. Inspect what the stuck client threads are waiting on:
   - Stuck in `SocketInputStream.socketRead0` $\rightarrow$ The client is missing a **read/response timeout**! Configure `connectTimeout` and `readTimeout` on the HTTP client.
   - Stuck in `Unsafe.park` inside `HikariPool.getConnection` or `HttpClient` pool $\rightarrow$ Connection pool exhaustion (forgot to close a response body `InputStream`).

#### Scenario C: High CPU or Memory Leak in the Client
- Run **Java Flight Recorder (JFR)** (`jcmd <pid> JFR.start duration=60s filename=profile.jfr`) or take a heap dump (`jmap -dump:live,format=b,file=heap.hprof <pid>`) to see which objects or retry loops are consuming memory/CPU.

### 39. How do you design unit and integration tests for these APIs?

**Simple answer:** Use the **Testing Pyramid**:
1. **Fast Unit Tests (`JUnit 5 + Mockito`):** Test business rules and edge cases in `TaskService` in milliseconds without starting Spring.
2. **Web Slice Tests (`@WebMvcTest + MockMvc`):** Test HTTP routing, `@Valid` status codes (`400`, `401`, `404`), and JSON serialization in `TaskController`.
3. **Database / Full Integration Tests (`@DataJpaTest` / `@SpringBootTest + Testcontainers`):** Test real SQL queries, pagination, constraints, and `@Transactional` rollback against a real PostgreSQL container.

### Example 1: Service Unit Test (`JUnit 5 + Mockito`)

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepo;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_trimsTitleAndAssignsAuthenticatedOwner() {
        CreateTaskRequest req = new CreateTaskRequest("  Prepare Round 2  ");
        when(taskRepo.save(any(TaskEntity.class))).thenAnswer(inv -> {
            TaskEntity saved = inv.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        TaskResponse res = taskService.createTask(42L, req);

        assertThat(res.id()).isEqualTo(101L);
        assertThat(res.title()).isEqualTo("Prepare Round 2");
        verify(taskRepo).save(argThat(task ->
            task.getOwnerId().equals(42L) && task.getTitle().equals("Prepare Round 2")
        ));
    }
}
```

### Example 2: Controller Slice Test (`@WebMvcTest + MockMvc`)

```java
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    @WithMockUser(username = "alice")
    void createTask_whenTitleIsBlank_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "   "}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").value("Title is required"));

        verifyNoInteractions(taskService); // Service should never be called on invalid input!
    }
}
```

### Example 3: What Only an Integration Test Can Catch (Important Interview Point!)

A unit test with `Mockito` **cannot** catch:
- Broken SQL/JPQL syntax in `@Query`.
- `@Transactional` self-invocation bugs (because Mockito mocks don't use Spring's transaction proxy).
- PostgreSQL-specific `UNIQUE` or `CHECK` constraint violations.
- Pagination tie-breaker bugs.

Always write a focused `@DataJpaTest` or `@SpringBootTest` with **Testcontainers** (`PostgreSQLContainer`) to verify those real database behaviors!
