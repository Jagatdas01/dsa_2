# Java Core: Interview Question Set

Target: 6 to 10 years experience, integration architecture role.

## Index

1. Language Features and OOP (Q1 to Q6)
2. Collections (Q7 to Q11)
3. Generics, Exceptions and Immutability (Q12 to Q16)
4. Concurrency (Q17 to Q24)
5. JVM, Memory and GC (Q25 to Q30)
6. Modern Java (Java 8 to 21) (Q31 to Q35)
7. Performance and Troubleshooting (Q36 to Q40)
8. Frequently Asked Coding & Tricky Scenarios (Q41 to Q59)
9. Java Streams (Concepts and Coding) (Q60 to Q78)

---

## 1. Language Features and OOP

### Q1. What are the four pillars of OOP, and how does Java implement each?

**Answer:** OOP organizes code around objects and gives us four useful ideas:
- **Encapsulation:** keep data private and control it through methods.
- **Inheritance:** create a class from another class with `extends`, or implement an interface with `implements`.
- **Polymorphism:** use the same method call with different implementations.
- **Abstraction:** show the important operations while hiding how they work.

Prefer putting changeable behavior behind an interface (composition) instead of building deep inheritance trees.

**Example:** A checkout service can accept any payment provider. The card and bank providers each do the payment differently, but checkout only calls `pay`. Each provider can hide its own implementation details.
```java
interface PaymentProvider {
    void pay(BigDecimal amount);
}

class CheckoutService {
    private final PaymentProvider provider;
    CheckoutService(PaymentProvider provider) { this.provider = provider; }
    void checkout(BigDecimal total) { provider.pay(total); }
}

class CardProvider implements PaymentProvider {
    public void pay(BigDecimal amount) {
        System.out.println("Charge card: " + amount);
    }
}

class BankProvider implements PaymentProvider {
    public void pay(BigDecimal amount) {
        System.out.println("Debit bank account: " + amount);
    }
}

CheckoutService checkout = new CheckoutService(new CardProvider());
checkout.checkout(new BigDecimal("25.00"));
```

### Q2. What is the difference between an abstract class and an interface?

**Answer:** Use an **interface** to say what a class can do. A class can implement several interfaces. Use an **abstract class** when related classes should share fields or ready-made code. A class can extend only one class. Interfaces can also have default methods.

**Example:** Both cloud and local storage can implement the same `Storage` contract. A shared abstract class is useful only if both need the same state or setup.
```java
interface Storage {
    void save(String name, byte[] data);
}

interface Retryable {
    void retry();
}

abstract class BaseStorage {
    protected final String region;

    BaseStorage(String region) {
        this.region = region;
    }
}

class S3Storage extends BaseStorage implements Storage, Retryable {
    S3Storage(String region) { super(region); }
    public void save(String name, byte[] data) { uploadToS3(name, data); }
    public void retry() { retryLastUpload(); }

    private void uploadToS3(String name, byte[] data) { }
    private void retryLastUpload() { }
}
```

### Q3. Explain `final`, `finally`, and `finalize`.

**Answer:**
- `final` means a variable cannot be assigned again, a method cannot be overridden, or a class cannot be extended.
- `finally` is a block that runs after `try`, including when an exception occurs. For files and similar resources, use try-with-resources instead.
- `finalize()` was an unreliable cleanup method and is deprecated for removal. Do not use it.

**Example:** The retry limit cannot be reassigned. The lock is always released when the method exits, including when its work throws an exception. Use try-with-resources for files instead of `finalize()`.
```java
private final int maxRetries = 3;
private final Lock lock = new ReentrantLock();

void update() {
    lock.lock();
    try {
        updateState();
    } finally {
        lock.unlock();
    }
}

String readFirstLine(Path path) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(path)) {
        return reader.readLine();
    }
}
```

### Q4. What is the difference between `==` and `equals()`?

**Answer:** For primitive values, `==` compares the values. For objects, it checks whether both references point to the same object. `equals()` checks whether two objects should count as equal according to their class. Use `equals()` to compare text or other object contents.

**Example:** These two strings hold the same text but are separate objects, so `==` is false and `equals()` is true.
```java
String first = new String("java");
String second = new String("java");

System.out.println(first == second);      // false: different objects
System.out.println(first.equals(second)); // true: same text
```

### Q5. What is the `hashCode()` and `equals()` contract?

**Answer:** If two objects are equal according to `equals()`, they must return the same `hashCode()`. Two objects with the same hash are not necessarily equal. Collections such as `HashMap` use both methods to find keys, so implement them together and avoid changing key fields after insertion.

**Example:** Two `UserId` instances with the same ID should work interchangeably as map keys.
```java
record UserId(String value) {}

Map<UserId, String> names = new HashMap<>();
names.put(new UserId("u-42"), "Mina");

System.out.println(names.get(new UserId("u-42"))); // Mina
```

### Q6. What are the access modifiers, and what is the default?

**Answer:** Access modifiers control where code can be used. `private` is available only inside its class. With no modifier, code is available in the same package. `protected` is available in the same package and to subclasses. `public` is available anywhere. Keep data private and expose safe operations.

**Example:** Callers cannot set an account's balance to an arbitrary amount; they must use `deposit`, which checks that the amount is positive.
```java
class Account {
    private BigDecimal balance = BigDecimal.ZERO;

    public void deposit(BigDecimal amount) {
        if (amount.signum() <= 0) throw new IllegalArgumentException();
        balance = balance.add(amount);
    }
}
```

---

## 2. Collections

### Q7. How does `HashMap` work internally?

**Answer:** A `HashMap` uses an array of buckets. It uses a key's `hashCode()` to choose a bucket, then `equals()` to find the exact key there. Different keys can land in the same bucket; that is called a collision. Large collision groups can become trees in modern Java. The map grows as it fills up.

**Example:** Store orders by ID. Later, look one up using the same ID; the map uses the string's hash and equality to locate the order.
```java
Map<String, Order> orders = new HashMap<>();
orders.put("order-7", order);

Order found = orders.get("order-7");
System.out.println(found);
```

### Q8. What is the difference between `HashMap`, `LinkedHashMap`, and `TreeMap`?

**Answer:** `HashMap` is usually the general-purpose choice when order does not matter. `LinkedHashMap` remembers insertion order. `TreeMap` keeps keys sorted. The ordered maps may do a little more work to maintain their order.

**Example:** The same entries appear differently depending on the map: insertion order for `LinkedHashMap`, numeric key order for `TreeMap`.
```java
Map<Integer, String> recent = new LinkedHashMap<>();
recent.put(20, "second");
recent.put(10, "first");

Map<Integer, String> sorted = new TreeMap<>(recent);
System.out.println(recent.keySet()); // [20, 10]
System.out.println(sorted.keySet()); // [10, 20]
```

### Q9. What is the difference between `ArrayList` and `LinkedList`?

**Answer:** `ArrayList` stores items in a resizable array, so reading by index is fast. `LinkedList` connects items as nodes; finding an item by index means walking through the list. Although a linked list can insert quickly once you already have the node, most everyday code benefits from `ArrayList`.

**Example:** A screen that displays a page of products often needs to read items by index, so an `ArrayList` is a straightforward fit.
```java
List<String> products = new ArrayList<>();
products.add("Keyboard");
products.add("Monitor");

String secondProduct = products.get(1); // Fast indexed lookup
```

### Q10. How do `ConcurrentHashMap` and `Collections.synchronizedMap` differ?

**Answer:** A synchronized map allows only one thread at a time to use the map's operations. `ConcurrentHashMap` is designed for many threads to access it safely with less blocking. It does not accept `null` keys or values. Use methods like `computeIfAbsent` when a check and update must happen together.

**Example:** Several request threads can ask for the same user. `computeIfAbsent` checks the cache and loads the user if it is missing.
```java
ConcurrentMap<String, User> cache = new ConcurrentHashMap<>();

User user = cache.computeIfAbsent(userId, id -> userService.load(id));
```

### Q11. How do you avoid `ConcurrentModificationException`?

**Answer:** A regular collection may throw `ConcurrentModificationException` if you change its structure while a for-each loop is walking through it. Use `removeIf`, or use an iterator's `remove` method. This exception does not mean the collection is thread-safe.

**Example:** Remove inactive users with the collection's own filtering method instead of deleting items inside a for-each loop.
```java
List<User> users = new ArrayList<>(loadUsers());
users.removeIf(User::isInactive);

users.forEach(System.out::println);
```

---

## 3. Generics, Exceptions and Immutability

### Q12. What are generics, and what does type erasure mean?

**Answer:** Generics tell the compiler what kind of values a collection should hold. The compiler catches the wrong type before the program runs. Java removes most generic type details when compiling; this is called type erasure. As a result, you cannot check at runtime whether an object is specifically a `List<String>`.

**Example:** The compiler accepts strings in this list and reports an error if you try to add a number.
```java
List<String> names = new ArrayList<>();
names.add("Asha");
// names.add(42); // Compile-time error: 42 is not a String
String first = names.get(0);
```

### Q13. What is PECS (Producer Extends, Consumer Super)?

**Answer:** PECS means **Producer Extends, Consumer Super**. If a collection supplies values for you to read, use `? extends T`. If you will add `T` values to a collection, use `? super T`.

**Example:** This method reads integers from the source and adds them to a destination that can hold integers, such as `List<Integer>` or `List<Number>`.
```java
static <T> void copy(List<? super T> destination,
                     List<? extends T> source) {
    for (T item : source) {
        destination.add(item);
    }
}
```

### Q14. What is the difference between checked and unchecked exceptions?

**Answer:** A checked exception must be caught or listed in the method signature; Java checks this when compiling. An unchecked exception does not have that requirement and often signals a programming or invalid-input problem. When passing an error up, include useful context and keep the original cause.

**Example:** If reading a partner response fails, wrap the file/network error with the message ID so logs can identify which integration request failed.
```java
try {
    return partnerClient.read(messageId);
} catch (IOException cause) {
    throw new IntegrationException(
        "Could not read partner response for " + messageId, cause);
}
```

### Q15. How does try-with-resources work?

**Answer:** Try-with-resources automatically closes resources such as files and database connections when the block ends, including when an exception occurs. The resource must implement `AutoCloseable`. When there are several resources, Java closes them in reverse order.

**Example:** The reader is closed even if `readLine()` fails, so the program does not leave the file open.
```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
    String firstLine = reader.readLine();
    return firstLine;
}
```

### Q16. How do you create an immutable class in Java?

**Answer:** An immutable object cannot be changed after it is created. Make the class final, keep fields private and final, provide no setters, and make copies of mutable data passed in. This prevents other code from changing the object's state through a shared list or array.

**Example:** Copy the incoming list so that changing the caller's original list does not change the order.
```java
final class Order {
    private final List<String> items;

    Order(List<String> items) {
        this.items = List.copyOf(items);
    }

    List<String> items() {
        return items;
    }
}
```

---

## 4. Concurrency

### Q17. What is the difference between a process and a thread?

**Answer:** A process is a running program with its own memory. A process can contain multiple threads. Threads in one process share objects in the heap, but each thread has its own call stack and local variables. Because threads share data, changes to shared mutable objects may need synchronization.

**Example:** Two requests can run on separate threads and use the same shared cache. Each request has its own local `userId`, but both threads access the shared cache.
```java
class UserService {
    private final Map<String, User> cache = new ConcurrentHashMap<>();

    User find(String userId) {
        return cache.computeIfAbsent(userId, this::loadFromDatabase);
    }
}
```

### Q18. What do `synchronized`, `volatile`, and `Lock` do?

**Answer:**
- `synchronized` lets only one thread at a time enter a protected section, and it makes changes visible to the next thread using the same lock.
- `volatile` makes the latest value of one variable visible between threads. It does not make multi-step operations such as `count++` safe.
- `Lock` provides extra options such as timed attempts to acquire a lock. Release it in `finally`.

**Example:** A `volatile` flag works for a simple stop signal. For a shared counter, use `AtomicInteger` because incrementing involves reading and changing the value.
```java
private volatile boolean running = true;
private final AtomicInteger count = new AtomicInteger();

void processItem() {
    if (running) {
        count.incrementAndGet();
    }
}
```

### Q19. What is the Java Memory Model's happens-before relationship?

**Answer:** Happens-before is a Java rule that guarantees one thread can see another thread's earlier changes. For example, a write to a `volatile` variable is visible to a thread that later reads that variable. Similar guarantees exist when using the same lock or when joining a thread.

**Example:** The worker writes the message before setting `ready`. If another thread sees `ready` as true, it will also see the earlier message write.
```java
private String message;
private volatile boolean ready;

void publish() {
    message = "Finished";
    ready = true;
}

String readWhenReady() {
    return ready ? message : "Not ready";
}
```

### Q20. What is the difference between `ExecutorService` types?

**Answer:** An executor runs submitted tasks on background threads. A fixed pool keeps a set number of threads but uses an unbounded queue by default. A cached pool can create many threads during a burst. For production workloads, a `ThreadPoolExecutor` lets you set the thread limits, queue size, and what happens when it is full.

**Example:** An image service can use four regular workers, grow to eight during a burst, and queue at most 100 jobs. New jobs are rejected once all limits are reached.
```java
ExecutorService images = new ThreadPoolExecutor(
    4, 8, 30, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.AbortPolicy()
);
```

### Q21. How does `ThreadPoolExecutor` decide to create a thread?

**Answer:** When a task arrives, the executor first starts workers until it reaches the core thread count. After that, it queues tasks. If the queue fills up, it adds workers up to the maximum. If the queue and all workers are full, the rejection policy decides what happens.

**Example:** With two core workers and a queue of ten, the first two tasks start immediately and the next ten wait. If the queue fills, the executor can start two more workers. Another task is rejected or handled by the configured policy.
```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    2, 4, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(10)
);
```

### Q22. What is `CompletableFuture`, and how do you handle errors in it?

**Answer:** `CompletableFuture` represents work that may finish later. You can attach steps that transform its result, start another async step, or combine several results. Handle failures explicitly and set a timeout when waiting on a remote service. For blocking work, use a dedicated executor instead of the shared default pool.

**Example:** Fetch a user asynchronously, convert the result to an email address, and use a clear fallback if the lookup fails.
```java
CompletableFuture<String> email = fetchUser(userId)
    .thenApply(User::email)
    .exceptionally(error -> "unavailable@example.com")
    .orTimeout(2, TimeUnit.SECONDS);
```

### Q23. What is a deadlock, and how do you prevent one?

**Answer:** A deadlock happens when two or more threads each hold a lock that another thread needs. None of them can continue. Avoid it by acquiring locks in the same order everywhere, keeping locked sections short, and not calling slow external services while holding a lock. Thread dumps can help find deadlocks.

**Example:** One transfer must not lock account 1 then 2 while another locks account 2 then 1. Sort the accounts first so every transfer uses the same order.
```java
Account first = accountA.id() < accountB.id() ? accountA : accountB;
Account second = first == accountA ? accountB : accountA;

synchronized (first) {
    synchronized (second) {
        transferFunds(accountA, accountB, amount);
    }
}
```

### Q24. What is `ThreadLocal`, and what are its risks in thread pools?

**Answer:** `ThreadLocal` gives each thread its own copy of a value, often used for request or logging context. A thread pool reuses threads, so an old value can accidentally remain for the next task. Clear it in a `finally` block. It also does not automatically carry context to a different thread.

**Example:** Set a request ID while processing a request and always remove it afterward, even if processing throws an exception.
```java
private static final ThreadLocal<String> requestId = new ThreadLocal<>();

void handle(String id) {
    requestId.set(id);
    try {
        processRequest();
    } finally {
        requestId.remove();
    }
}
```

---

## 5. JVM, Memory and GC

### Q25. Describe the main JVM memory areas.

**Answer:**
- **Heap:** where objects and arrays are stored; all threads can use them.
- **Stack:** each thread has its own stack for method calls and local variables.
- **Metaspace:** stores information about loaded classes.
- **Code cache:** stores machine code produced by the JIT compiler.

**Example:** When a method creates an order, the order object is on the heap. The method's local variable refers to it from the thread's stack.
```java
void createOrder() {
    Order order = new Order(); // object: heap; local reference: this thread's stack
    save(order);
}
```

### Q26. What causes `OutOfMemoryError`, and how do you diagnose it?

**Answer:** An `OutOfMemoryError` means the JVM could not get more memory it needed. The heap may be too small, or the program may keep objects it no longer needs. Unbounded caches and reading huge responses all at once are common causes. Capture a heap dump and inspect which objects are holding the most memory.

**Example:** This cache never removes entries, so each processed request can stay in memory. A heap dump can show the map retaining those requests.
```java
class RequestCache {
    private static final Map<String, Request> allRequests = new HashMap<>();

    void remember(Request request) {
        allRequests.put(request.id(), request); // grows unless entries are removed
    }
}
```

### Q27. What are the main garbage collectors, and when would you choose each?

**Answer:** A garbage collector frees memory used by objects that the program no longer needs. G1 is a good general starting point. ZGC and Shenandoah aim for short pauses and can help large, latency-sensitive services. Parallel GC focuses more on throughput and can suit batch jobs. Measure first; changing the collector without data may not help.

**Example:** If a web service has long pauses that hurt request latency, inspect its GC logs. If the pauses are caused by garbage collection, test another collector under representative load and compare pause times.
```text
-Xlog:gc*
```

### Q28. What is the difference between `-Xms`, `-Xmx`, and container memory limits?

**Answer:** `-Xms` is the heap size at startup; `-Xmx` is the largest heap the JVM may use. A container also needs memory for class metadata, thread stacks, and other native data. If the heap uses the whole container limit, the operating system may kill the process even if Java's heap is not full.

**Example:** In a container with a 1 GiB limit, do not assign all 1 GiB to `-Xmx`. Leave some memory for the JVM and monitor the container's total usage.
```text
java -XX:MaxRAMPercentage=70 -jar service.jar
```

### Q29. What is class loading, and what are the common problems?

**Answer:** Before Java can use a class, a class loader finds its bytecode and loads it. Some classes come from Java itself, and others come from your application and libraries. If a required library is missing at runtime, or two incompatible versions are present, the application can fail to start or use the class.

**Example:** The code compiles because the client library is available during development, but deployment leaves that library out. The service then fails when it tries to create the client.
```text
java.lang.NoClassDefFoundError: com/example/Client
```

### Q30. What is JIT compilation?

**Answer:** Java code is compiled to bytecode. The JVM can interpret that bytecode, and it can compile frequently used ("hot") parts into native machine code while the program runs. This lets the JVM optimize the code based on how it is actually used.

**Example:** A request parser used on every API call may become hot and get compiled to native code. A rarely used admin operation may not need that optimization.
```java
for (int request = 0; request < 1_000_000; request++) {
    parseRequest(payload); // frequently called method may become JIT-compiled
}
```

---

## 6. Modern Java (Java 8 to 21)

### Q31. What are the key features added in Java 8?

**Answer:** Java 8 added lambdas and the Stream API for working with collections, `Optional` for values that might be absent, `CompletableFuture` for async work, and the modern `java.time` date/time API. It also added default methods to interfaces.

**Example:** Filter a list of names and collect the matches into a new list. This avoids writing a separate loop for this simple operation.
```java
List<String> names = Arrays.asList("Asha", "Ben", "Amir");
List<String> matching = names.stream()
    .filter(name -> name.startsWith("A"))
    .collect(Collectors.toList());
```

### Q32. What is the difference between `map` and `flatMap` in streams?

**Answer:** Think of each input item as a box:
- `map` changes each item into one new item. If the new item is a list, you end up with a list inside each result.
- `flatMap` changes each item into a stream of items, then opens and combines those streams into one flat stream.

Use `map` for one-to-one changes, such as converting each name to uppercase. Use `flatMap` when each item contains several values you want to process together, such as getting all words from several sentences.

**Example:** Here, `map` splits each sentence but keeps each sentence's words together in its own array. `flatMap` also splits the sentences, then combines all the words into one list.
```java
List<String> sentences = Arrays.asList("Java is useful", "Streams are powerful");

// map returns one String[] for each sentence: a list of arrays
List<String[]> wordsBySentence = sentences.stream()
    .map(sentence -> sentence.split(" "))
    .collect(Collectors.toList());

// flatMap combines all the words into one list of strings
List<String> allWords = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
    .collect(Collectors.toList());

System.out.println(Arrays.deepToString(wordsBySentence.toArray()));
// [[Java, is, useful], [Streams, are, powerful]]

System.out.println(allWords);
// [Java, is, useful, Streams, are, powerful]
```

### Q33. What are records and sealed classes?

**Answer:** A record is a short way to create a data class. Java provides its constructor, accessors, `equals`, `hashCode`, and `toString`. A sealed class or interface restricts which types can extend or implement it. Use these when the set of possible message types is known.

**Example 1 — record as a data object:** A `Payment` record stores an ID and amount. Java automatically creates its constructor, accessors, and value-based `equals`/`hashCode`.
```java
record Payment(String id, BigDecimal amount) { }

Payment payment = new Payment("p-101", new BigDecimal("25.00"));
System.out.println(payment.id());     // p-101
System.out.println(payment.amount()); // 25.00

Payment samePayment = new Payment("p-101", new BigDecimal("25.00"));
System.out.println(payment.equals(samePayment)); // true
```

**Example 2 — sealed types for known outcomes:** A payment result can only be a `Success` or a `Failure`. A switch can handle each permitted type.
```java
sealed interface PaymentResult permits Success, Failure { }
record Success(String confirmationId) implements PaymentResult { }
record Failure(String reason) implements PaymentResult { }

String describe(PaymentResult result) {
    return switch (result) {
        case Success success -> "Confirmed: " + success.confirmationId();
        case Failure failure -> "Failed: " + failure.reason();
    };
}

System.out.println(describe(new Success("c-900"))); // Confirmed: c-900
System.out.println(describe(new Failure("Card declined"))); // Failed: Card declined
```

### Q34. What are virtual threads (Java 21), and when should you use them?

**Answer:** Use virtual threads when an application has many concurrent tasks that spend most of their time waiting for I/O, such as HTTP requests, database calls, or file access. They let you keep simple blocking code without needing a large pool of platform threads. They do not make CPU-heavy work faster, so use a bounded platform-thread pool for tasks such as image processing. Create a virtual thread per task rather than pooling virtual threads.

**Example:** Each incoming request can run on its own virtual thread and wait for a partner API response. While one request waits, the JVM can use the carrier thread for other work.
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<Response> response = executor.submit(
        () -> partnerClient.fetch(requestId));
    return response.get();
}
```

**Example 2 — many requests waiting on I/O:** Suppose an application needs to call a slow partner service for many independent requests. A virtual-thread-per-task executor lets each request use simple blocking code without creating a large platform-thread pool. It does not make the partner service itself faster, and CPU-heavy work still needs sensible limits.
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Response>> results = requestIds.stream()
        .map(id -> executor.submit(() -> partnerClient.fetch(id)))
        .collect(Collectors.toList());

    for (Future<Response> result : results) {
        process(result.get());
    }
}
```

For CPU-heavy tasks, such as image compression, use a bounded platform-thread pool sized for available CPU cores instead of creating unlimited concurrent work.

### Q35. What is pattern matching, and how is it used?

**Answer:** Pattern matching checks an object's type and gives you a variable of that type in the same step. This removes a separate cast. Newer Java versions can also match types in a `switch`, which is useful when handling a known set of message types.

**Example:** The code checks whether the input is a string and uses it only when the check succeeds.
```java
Object value = "hello";
if (value instanceof String text) {
    System.out.println("Text length: " + text.length());
}
```

---

## 7. Performance and Troubleshooting

### Q36. Which tools would you use to troubleshoot a production Java service?

**Answer:** Start with service metrics and logs to identify what is slow or failing. Use `jcmd` or a thread dump to investigate stuck threads, a heap dump to investigate memory use, and Java Flight Recorder (JFR) to profile CPU, allocations, and runtime activity. Choose the tool based on the symptom instead of collecting everything without a question.

**Example:** If API latency suddenly rises, check the latency and thread-pool metrics first. If worker threads are stuck, capture a thread dump; if CPU is high, record JFR and inspect which methods use the CPU.
```text
jcmd <pid> Thread.print
jcmd <pid> JFR.start name=latency-check duration=60s filename=latency.jfr
```

### Q37. How do you find a memory leak?

**Answer:** A memory leak happens when the program keeps references to objects it no longer needs. Take heap dumps at different times under similar load and compare them with a tool such as Eclipse MAT. Look for collections or object counts that keep growing, then inspect what is still holding those objects.

**Example:** Suppose the number of `Session` objects grows after every batch. In MAT, follow the retaining path to see whether a cache, static list, or listener is keeping them alive.
```text
Heap dump 1:  1,000 Session objects
Heap dump 2: 10,000 Session objects
Next step: inspect which long-lived object retains those sessions
```

### Q38. How do you avoid unnecessary object allocation in hot paths?

**Answer:** First measure allocations with a profiler; do not optimize based on guesswork. If a hot section creates many temporary objects, consider reusing safe objects, choosing a suitable collection size, or building strings with one `StringBuilder`. Keep the code easy to understand.

**Example:** Repeated string concatenation in a loop can create many temporary strings. A single builder creates the final message more directly.
```java
StringBuilder csv = new StringBuilder();
for (String field : fields) {
    if (csv.length() > 0) csv.append(',');
    csv.append(field);
}
String result = csv.toString();
```

### Q39. How do you handle `String` performance and immutability?

**Answer:** A `String` cannot be changed after it is created, so it is safe to share. Repeatedly joining strings in a loop can create many temporary objects. Use `StringBuilder` for repeated appends, or `String.join` for a collection of strings. Avoid calling `intern()` on large amounts of changing input.

**Example:** Build a comma-separated list using a builder. The same builder is reused for every field, and `toString()` creates the final text when the loop is done.
```java
StringBuilder line = new StringBuilder();
for (String field : fields) {
    if (line.length() > 0) line.append(',');
    line.append(field);
}
String csvRow = line.toString();
```

### Q40. How do you serialize objects safely?

**Answer:** Avoid Java's built-in object serialization for data received from outside your application because deserializing untrusted objects can be dangerous. Use a format with a defined schema, such as JSON or Protobuf, and validate incoming data before using it. If legacy code must use Java serialization, restrict which classes are allowed.

**Example:** A service can convert a payment event to JSON for transport. When it receives the JSON, validate required fields and business rules before processing the payment.
```java
String json = objectMapper.writeValueAsString(paymentEvent);

PaymentEvent event = objectMapper.readValue(json, PaymentEvent.class);
validate(event);
paymentService.process(event);
```

---

## 8. Frequently Asked Coding & Tricky Scenarios

### Q41. What happens if you use a mutable object as a `HashMap` key and modify it after insertion?

**Answer:** A hash map places a key in a bucket based on its hash code. If you change a field that contributes to the hash after insertion, the map may look in a different bucket when you search for that key. The entry can still occupy memory but be difficult to find. Use keys that do not change.

**Example:** This key uses `id` to calculate its hash. After changing the ID, the map searches using a new hash, while the entry was placed using the old one.
```java
MutableKey key = new MutableKey(1);
Map<MutableKey, String> map = new HashMap<>();
map.put(key, "saved");

key.setId(2);                 // Changes the hash used by the key
System.out.println(map.get(key)); // May return null
```

### Q42. Why is `BigDecimal` preferred over `double` for financial/monetary calculations, and what is the `equals()` gotcha?

**Answer:** A `double` stores numbers in binary, so some decimal amounts (such as `0.1`) cannot be represented exactly. This can create small rounding errors in money calculations. `BigDecimal` supports decimal arithmetic and lets you define how rounding should work.
- **Gotcha 1:** Never use `new BigDecimal(0.1)`—use `BigDecimal.valueOf(0.1)` or `new BigDecimal("0.1")`.
- **Gotcha 2:** `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` is `false` because `equals()` checks both value and scale. Use `compareTo()` for value comparison (`a.compareTo(b) == 0`).

**Example:** Create amounts from strings and use `compareTo` when values should be equal regardless of trailing zeros.
```java
BigDecimal price = new BigDecimal("19.99");
BigDecimal quantity = new BigDecimal("2");
BigDecimal total = price.multiply(quantity);

System.out.println(total); // 39.98
System.out.println(new BigDecimal("1.0")
    .compareTo(new BigDecimal("1.00")) == 0); // true
```

### Q43. What is the difference between `Comparable` and `Comparator`?

**Answer:** `Comparable` defines one default ordering inside a class, such as employees sorted by ID. A `Comparator` lets the caller choose another ordering without changing the class, such as sorting by last name or salary.

**Example:** `Employee` uses its ID as the natural order by implementing `Comparable`. A report can sort the same employees by last name using a `Comparator`, without changing the `Employee` class.
```java
record Employee(int id, String firstName, String lastName)
        implements Comparable<Employee> {
    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);
    }
}

List<Employee> employees = new ArrayList<>(List.of(
    new Employee(3, "Mina", "Shah"),
    new Employee(1, "Arun", "Patel"),
    new Employee(2, "Lina", "Chen")
));

// Comparable: uses Employee's built-in order (ID)
employees.sort(Comparator.naturalOrder());
System.out.println(employees);
// [Employee[id=1, firstName=Arun, lastName=Patel],
//  Employee[id=2, firstName=Lina, lastName=Chen],
//  Employee[id=3, firstName=Mina, lastName=Shah]]

// Comparator: choose another order without changing Employee
employees.sort(Comparator.comparing(Employee::lastName)
    .thenComparing(Employee::firstName));
System.out.println(employees);
// [Employee[id=2, firstName=Lina, lastName=Chen],
//  Employee[id=1, firstName=Arun, lastName=Patel],
//  Employee[id=3, firstName=Mina, lastName=Shah]]
```

### Q44. What is the difference between `fail-fast` and `fail-safe` iterators?

**Answer:**
- A **fail-fast** iterator from a regular collection may throw `ConcurrentModificationException` if the collection is structurally changed while you iterate.
- Concurrent collections provide iterators designed for concurrent updates. Some, such as `CopyOnWriteArrayList`, iterate over a snapshot; others, such as `ConcurrentHashMap`, provide weakly consistent iteration. Changes during iteration may not all appear.

**Example:** The listener added after the iterator is created may not receive the current notification, but it will be in the list for later notifications.
```java
CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
Iterator<Listener> current = listeners.iterator();

listeners.add(newListener); // Does not break the existing snapshot iterator
while (current.hasNext()) {
    current.next().onEvent();
}
```

### Q45. How do `AtomicInteger`, `LongAdder`, and `synchronized` compare for counters under high contention?

**Answer:** All three can protect a counter from lost updates. `synchronized` is simple but makes other threads wait for the lock. `AtomicInteger` is a good general-purpose counter. `LongAdder` can handle many simultaneous updates efficiently, but reading its total requires calling `sum()`. It is often a good fit for metrics, not for values that need an exact snapshot after every update.

**Example:** Many request threads can increment one metric; the monitoring code periodically reads the total.
```java
private final LongAdder requests = new LongAdder();

void handleRequest() {
    requests.increment();
    // Process the request
}

long requestCount() {
    return requests.sum();
}
```

### Q46. What are `CountDownLatch`, `CyclicBarrier`, `Semaphore`, and `Phaser`?

**Answer:**
- `CountDownLatch` lets one or more threads wait until a fixed number of tasks finish. It cannot be reset.
- `CyclicBarrier` makes a fixed group of threads wait for each other and can be reused.
- `Semaphore` limits how many operations can happen at once.
- `Phaser` supports reusable stages when the number of participating tasks can change.

**Example 1 — `CountDownLatch`:** Wait until three startup checks finish before accepting requests. The latch is one-shot; after it reaches zero, it stays open.
```java
CountDownLatch checksDone = new CountDownLatch(3);

executor.submit(() -> { checkDatabase(); checksDone.countDown(); });
executor.submit(() -> { checkQueue(); checksDone.countDown(); });
executor.submit(() -> { checkConfig(); checksDone.countDown(); });

checksDone.await();
startAcceptingRequests();
```

**Example 2 — `CyclicBarrier`:** Four workers each process part of a report. They all wait at the barrier before starting the next reporting stage. The barrier can be reused for another stage.
```java
CyclicBarrier reportBarrier = new CyclicBarrier(4);

void worker() throws Exception {
    processMyReportSection();
    reportBarrier.await(); // Wait for the other three workers
    addSectionTotals();
}
```

**Example 3 — `Semaphore`:** Allow at most ten partner API calls at once. Always release the permit, including when the call fails.
```java
Semaphore permits = new Semaphore(10);

permits.acquire();
try {
    partnerClient.call();
} finally {
    permits.release();
}
```

**Example 4 — `Phaser`:** Use a phaser when a multi-stage job has a changing number of workers. Workers register when they join. The main thread and workers wait at the end of each phase before moving on.
```java
Phaser phaser = new Phaser(1); // Register the main thread

for (Task task : tasks) {
    phaser.register();
    executor.submit(() -> {
        try {
            processPhaseOne(task);
            phaser.arriveAndAwaitAdvance();
            processPhaseTwo(task);
        } finally {
            phaser.arriveAndDeregister();
        }
    });
}

phaser.arriveAndAwaitAdvance(); // Main thread waits for phase one
preparePhaseTwo();
phaser.arriveAndAwaitAdvance(); // Wait for workers to finish phase two
finishReport();
```

### Q47. What is the difference between `ForkJoinPool` and `ThreadPoolExecutor`?

**Answer:** `ThreadPoolExecutor` is a general-purpose pool that runs submitted tasks from a queue. `ForkJoinPool` is designed for work that can be split into smaller tasks; idle workers can take ("steal") work from busy workers. Parallel streams and some async calls use a shared common fork-join pool by default, so avoid putting long blocking tasks there.

**Example 1 — `ForkJoinPool` for split-and-combine work:** A large CPU-heavy calculation can split the input into smaller ranges. Each task sums its range, then combines the partial sums. Idle workers can help with tasks from busy workers.
```java
class SumTask extends RecursiveTask<Long> {
    private final long[] numbers;
    private final int start;
    private final int end;

    SumTask(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= 1_000) {
            long total = 0;
            for (int i = start; i < end; i++) {
                total += numbers[i];
            }
            return total;
        }

        int middle = (start + end) / 2;
        SumTask left = new SumTask(numbers, start, middle);
        SumTask right = new SumTask(numbers, middle, end);
        left.fork(); // Run one half separately
        long rightTotal = right.compute(); // Work on the other half
        return left.join() + rightTotal; // Combine both results
    }
}

ForkJoinPool forkJoinPool = new ForkJoinPool();
long total = forkJoinPool.invoke(new SumTask(numbers, 0, numbers.length));
forkJoinPool.shutdown();
```

**Example 2 — `ThreadPoolExecutor` for independent tasks:** For ordinary jobs, such as sending separate notifications, submit each task to a regular executor. It runs tasks from a shared queue and does not require each task to split itself into smaller tasks.
```java
ExecutorService notificationPool = Executors.newFixedThreadPool(4);
try {
    for (String email : emailAddresses) {
        notificationPool.submit(() -> sendNotification(email));
    }
} finally {
    notificationPool.shutdown();
}
```

### Q48. Why should you never perform blocking I/O inside `stream().parallel()`?

**Answer:** A parallel stream normally uses a shared pool intended for CPU work. A blocking network call can occupy one of its limited workers while it waits. If many calls block, other work using the same pool can be delayed. Use a dedicated executor or virtual threads for blocking I/O.

**Example:** Instead of making blocking HTTP calls inside a parallel stream, submit them to an executor dedicated to partner requests.
```java
ExecutorService ioPool = Executors.newFixedThreadPool(20);
List<Future<Response>> responses = urls.stream()
    .map(url -> ioPool.submit(() -> httpClient.get(url)))
    .collect(Collectors.toList());
```

### Q49. What are Strong, Soft, Weak, and Phantom references in Java?

**Answer:**
- A **strong** reference is an ordinary reference; the object stays reachable while the reference is reachable.
- A **soft** reference may be cleared when the JVM needs memory, though it is usually better to use a real cache with a clear size policy.
- A **weak** reference does not keep an otherwise unused object alive; `WeakHashMap` uses weak keys.
- A **phantom** reference is used with a reference queue for cleanup coordination after an object is no longer normally reachable. Most application code should use `Cleaner` rather than build this mechanism directly.

**Example:** A plugin system can keep metadata keyed by a `Class` in a `WeakHashMap`. The metadata value must not hold a strong reference back to the class, or the key will remain reachable.
```java
Map<Class<?>, Metadata> metadataByType = new WeakHashMap<>();
metadataByType.put(pluginClass, metadata);
```

### Q50. What is false sharing in concurrent Java programs, and how is it prevented?

**Answer:** False sharing is a performance problem, not a data-correctness problem. It can happen when different threads frequently update separate values that happen to sit close together in memory. The CPU may move the same cache line between cores even though the threads update different counters. Confirm it with profiling before trying to change memory layout.

**Example:** Two workers update separate counters many times per second. If profiling shows cache contention, a purpose-built counter such as `LongAdder` may help; do not add padding without measuring.
```java
LongAdder accepted = new LongAdder();
LongAdder rejected = new LongAdder();
```

### Q51. How does `CompletableFuture.allOf` work when one task fails, and how do you fail fast?

**Answer:** `allOf` creates a future that completes after every supplied future has completed. If any one fails, the combined future completes exceptionally, but the other tasks are not automatically stopped as soon as the first failure occurs. If you need fail-fast behavior, coordinate cancellation yourself and make sure the underlying tasks support cancellation.

**Example:** Start three profile lookups. `allOf` reports failure after all have finished; cancelling the others requires retaining their futures and applying your own policy.
```java
CompletableFuture<User> user = loadUser(id);
CompletableFuture<List<Order>> orders = loadOrders(id);
CompletableFuture<Settings> settings = loadSettings(id);

CompletableFuture<Void> all =
    CompletableFuture.allOf(user, orders, settings);
```

### Q52. What is Structured Concurrency (Java 21+), and what problem does it solve?

**Answer:** Structured concurrency groups related concurrent tasks under one parent operation. The parent waits for its child tasks and handles their failures together. With a shutdown-on-failure policy, a failure can trigger cancellation of sibling tasks. This feature has been a preview API in Java 21, so check the Java version and preview settings before using it.

**Example:** A request needs a customer and their orders. Run both lookups as child tasks; if one fails, stop waiting for work that is no longer useful.
```java
// Java 21 preview API; compile and run with --enable-preview.
try (var scope =
         new StructuredTaskScope.ShutdownOnFailure()) {
    var customer = scope.fork(() -> loadCustomer(id));
    var orders = scope.fork(() -> loadOrders(id));
    scope.join().throwIfFailed();
    return new CustomerView(customer.get(), orders.get());
}
```

### Q53. What are Scoped Values (Java 21+) vs `ThreadLocal`?

**Answer:** A `ThreadLocal` stores a value on a thread and can be changed; this can be awkward when work moves between threads or a pooled thread is reused. A `ScopedValue` is designed to provide a value for a limited block of work and its child tasks. The binding ends with the scope. The API is preview/incubator depending on the Java release, so check the target JDK.

**Example:** Bind the request ID once, then helper methods called inside the scope can read it without passing it through every method parameter.
```java
// ScopedValue API availability depends on the Java release/preview status.
static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

ScopedValue.where(REQUEST_ID, "req-42").run(() -> processRequest());
```

### Q54. Write a concise Stream pipeline to group orders by customer and find the total spend per customer.

**Answer:** Group orders by customer ID, then add the amounts for each customer's group. `BigDecimal` is used for amounts to avoid floating-point rounding errors.

```java
Map<String, BigDecimal> spendByCustomer = orders.stream()
    .collect(Collectors.groupingBy(
        Order::customerId,
        Collectors.reducing(BigDecimal.ZERO, Order::amount, BigDecimal::add)
    ));
```

**Example:** The following orders result in a total of `15.50` for customer `"c-1"` and `7.00` for customer `"c-2"`.
```java
record Order(String customerId, BigDecimal amount) {}

List<Order> orders = List.of(
    new Order("c-1", new BigDecimal("10.00")),
    new Order("c-1", new BigDecimal("5.50")),
    new Order("c-2", new BigDecimal("7.00"))
);
```

### Q55. What is a thread dump, and how do you interpret thread states (`RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`)?

**Answer:**
- `RUNNABLE`: the thread is running or ready to run. A thread doing some native I/O can also appear runnable.
- `BLOCKED`: the thread is waiting to enter a `synchronized` block whose lock another thread holds.
- `WAITING`: the thread is waiting for another action, with no timeout (for example, `join()`).
- `TIMED_WAITING`: the thread is waiting with a timeout (for example, `sleep()`).

Look for many threads in the same state and check their stack traces. A large number blocked on one lock can point to lock contention.

**Example:** If many web request threads are blocked on the same monitor, find the thread holding that lock and inspect what it is doing. Increasing the thread pool may add more waiting threads without fixing the bottleneck.
```text
"http-worker-1" BLOCKED waiting for <0x123> held by "cache-writer"
"http-worker-2" BLOCKED waiting for <0x123> held by "cache-writer"
"cache-writer"  RUNNABLE in Cache.refresh()
```

### Q56. What is the difference between `Thread` and `Runnable`?

**Answer:** A `Thread` represents a path of execution. A `Runnable` represents the work to be done. Implementing `Runnable` keeps the task separate from the thread that runs it, so the same task can be run by a thread or submitted to an executor. Extending `Thread` combines the task and its execution mechanism, and also prevents the class from extending another class. In most cases, use `Runnable` with an executor.

Call `start()` to run work on a new thread. Calling `run()` directly is just a normal method call and runs on the current thread.

**Example:** The first class puts the work inside a `Thread` subclass. The second describes the work as a `Runnable`, which can be passed to a `Thread` or executor. The `Runnable` approach is usually more flexible.
```java
class PrintThread extends Thread {
    @Override
    public void run() {
        System.out.println("Work inside a Thread subclass");
    }
}

class PrintTask implements Runnable {
    private final String message;

    PrintTask(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(message + " on " +
            Thread.currentThread().getName());
    }
}

// Start the Thread subclass
new PrintThread().start();

// Run the Runnable task on a new thread
Thread thread = new Thread(new PrintTask("Hello"));
thread.start();

// Or let an executor manage the worker thread
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(new PrintTask("Hello from the executor"));
executor.shutdown();
```

### Q57. What is the difference between `ConcurrentHashMap` and `HashMap`?

**Answer:**

| Aspect | `HashMap` | `ConcurrentHashMap` |
|---|---|---|
| Thread safety | Not thread-safe. Concurrent writes can lose updates or corrupt internal state. | Thread-safe for all single operations. |
| Locking | None. | Java 8+: lock-free CAS for empty bins, `synchronized` on the first node of a bin only for writes. Reads are lock-free. (Java 7 used segment locks.) |
| `null` keys/values | One `null` key and many `null` values allowed. | Not allowed. `get()` returning `null` must clearly mean "absent". |
| Iterator | Fail-fast: throws `ConcurrentModificationException` if modified during iteration. | Weakly consistent: never throws CME, may or may not show updates made during iteration. |
| Atomic compound ops | None. | `putIfAbsent`, `computeIfAbsent`, `compute`, `merge`, `replace(k, old, new)`. |
| `size()` | Exact. | An estimate under concurrent updates (use `mappingCount()` for large maps). |
| Performance | Fastest in a single thread. | Slightly slower single-threaded, far better than `synchronizedMap`/`Hashtable` under contention. |

Use `HashMap` for local variables or data confined to one thread. Use `ConcurrentHashMap` for shared caches, registries, and counters accessed by multiple threads.

**Example:**
```java
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put(null, 1);                       // allowed

ConcurrentMap<String, Integer> chm = new ConcurrentHashMap<>();
// chm.put(null, 1);                        // NullPointerException

// Atomic per-key counter without external locking
chm.merge("orders", 1, Integer::sum);

// Safe iteration while other threads update the map
for (String key : chm.keySet()) {
    chm.remove(key);                        // no ConcurrentModificationException
}
```

### Q58. What is the difference between `ConcurrentHashMap` and `volatile`? Can a `volatile` HashMap replace a `ConcurrentHashMap`?

**Answer:** They solve different problems.
- `volatile` applies to **a single variable (the reference)**. It guarantees visibility and ordering: when a thread writes the variable, other threads see the new value. It gives **no atomicity** for compound actions and says nothing about the internal state of the object it points to.
- `ConcurrentHashMap` is a **thread-safe data structure**. It makes every operation on its contents safe (including internal resizing) and offers atomic compound operations such as `computeIfAbsent` and `merge`.

So `volatile Map<K,V> map = new HashMap<>()` only makes the *reference* visible. Two threads calling `map.put(...)` at the same time can still lose entries or corrupt the table.

When `volatile` with a map **is** correct: the **copy-on-write / immutable snapshot** pattern. Build a new map, then publish it by replacing the reference. Readers never see a half-built map.

**Example:**
```java
// WRONG: volatile does not make HashMap operations thread-safe
private volatile Map<String, Integer> counts = new HashMap<>();
void wrong(String k) { counts.put(k, counts.getOrDefault(k, 0) + 1); } // race

// RIGHT: concurrent structure with atomic update
private final ConcurrentHashMap<String, Integer> safeCounts = new ConcurrentHashMap<>();
void right(String k) { safeCounts.merge(k, 1, Integer::sum); }

// RIGHT: volatile reference to an immutable snapshot (read-mostly config)
private volatile Map<String, String> config = Map.of();
void reload(Map<String, String> fresh) {
    config = Map.copyOf(fresh);             // atomic publish of a fully built map
}
String get(String key) { return config.get(key); }
```

Rule of thumb: `volatile` for flags and publishing immutable references; `ConcurrentHashMap` for shared mutable key-value data.

### Q59. Is using `ConcurrentHashMap` enough to make code thread-safe?

**Answer:** No. `ConcurrentHashMap` guarantees that **each individual method call** is thread-safe. It does not make a **sequence of calls** atomic, and it does not protect the objects stored inside it.

Common pitfalls:
1. **Check-then-act:** `if (!map.containsKey(k)) map.put(k, v);` — two threads can both pass the check. Use `putIfAbsent` / `computeIfAbsent`.
2. **Read-modify-write:** `map.put(k, map.get(k) + 1);` — lost updates. Use `merge`, `compute`, or store a `LongAdder`/`AtomicLong` as the value.
3. **Mutable values:** `map.get(k).add(item)` where the value is an `ArrayList` — the list itself is not thread-safe. Use a concurrent value type or do the mutation inside `compute`.
4. **Invariants across multiple keys or multiple maps:** e.g. transferring a balance from key A to key B, or keeping two maps consistent. The map cannot make two operations atomic together; use a lock, a single immutable state object, or a database transaction.
5. **Aggregate methods are not snapshots:** `size()`, `isEmpty()`, iteration, and bulk ops reflect a moment that may already be stale.
6. **Long work inside `compute`/`computeIfAbsent`:** the bin is locked while the function runs. Keep it short, never call remote services, and never modify the same map inside the function (can deadlock or throw `IllegalStateException`).
7. **Multiple JVM instances:** in a Spring Boot service with several pods, an in-memory map is per-instance. Cross-instance consistency needs Redis, DB locks/constraints, or optimistic locking.

**Example:**
```java
ConcurrentHashMap<String, List<String>> bad = new ConcurrentHashMap<>();
bad.computeIfAbsent("k", x -> new ArrayList<>()).add("v");    // ArrayList not thread-safe

ConcurrentHashMap<String, Queue<String>> good = new ConcurrentHashMap<>();
good.computeIfAbsent("k", x -> new ConcurrentLinkedQueue<>()).add("v");

ConcurrentHashMap<String, LongAdder> hits = new ConcurrentHashMap<>();
hits.computeIfAbsent("/orders", x -> new LongAdder()).increment();

// Multi-key invariant still needs explicit synchronization
private final Object lock = new Object();
void transfer(Map<String, Long> balances, String from, String to, long amt) {
    synchronized (lock) {
        balances.merge(from, -amt, Long::sum);
        balances.merge(to, amt, Long::sum);
    }
}
```

## 9. Java Streams (Concepts and Coding)

Sample data used in the coding questions:
```java
record Employee(String name, String dept, double salary, int age, String gender) {}

List<Employee> emps = List.of(
    new Employee("Asha",  "IT",      90000, 32, "F"),
    new Employee("Ravi",  "IT",      75000, 28, "M"),
    new Employee("John",  "HR",      50000, 45, "M"),
    new Employee("Meera", "HR",      65000, 38, "F"),
    new Employee("Karan", "Finance", 80000, 41, "M"));
```

### Q60. What is a Stream, and how is it different from a Collection?

**Answer:**

| Collection | Stream |
|---|---|
| Stores data in memory. | Does not store data; it is a pipeline over a source (collection, array, file, generator). |
| Can be iterated many times. | Can be consumed **only once**; reuse throws `IllegalStateException`. |
| External iteration (`for` loop). | Internal iteration (the library loops for you). |
| Eagerly built. | **Lazy**: nothing runs until a terminal operation is called. |
| Can be modified (add/remove). | Does not modify the source; produces a new result. |

A stream pipeline = **source** -> zero or more **intermediate operations** -> one **terminal operation**.

### Q61. What are intermediate and terminal operations? What does "lazy evaluation" mean?

**Answer:**
- **Intermediate** (return a `Stream`, lazy): `filter`, `map`, `flatMap`, `distinct`, `sorted`, `peek`, `limit`, `skip`, `takeWhile`, `dropWhile`, `mapToInt`, `boxed`.
- **Terminal** (produce a result or side effect, trigger execution): `forEach`, `collect`, `toList`, `reduce`, `count`, `min`, `max`, `anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny`, `toArray`.
- **Stateless** intermediate ops (`filter`, `map`) handle each element independently; **stateful** ones (`sorted`, `distinct`) need to see other elements, which costs memory and hurts parallel performance.

**Lazy evaluation:** intermediate operations only build the pipeline. Elements are pulled one at a time through all stages (vertical processing) when the terminal operation runs. **Short-circuiting** ops (`limit`, `findFirst`, `anyMatch`) stop as soon as the answer is known.

```java
Stream.of("a", "bb", "ccc", "dddd")
      .filter(s -> { System.out.println("filter " + s); return s.length() > 1; })
      .map(s -> { System.out.println("map " + s); return s.toUpperCase(); })
      .findFirst();
// Prints: filter a, filter bb, map bb   -> "ccc" and "dddd" are never touched

Stream<String> s = Stream.of("x").filter(v -> true);   // nothing printed, nothing executed
```

### Q62. What is the difference between `findFirst()` and `findAny()`, and `anyMatch`/`allMatch`/`noneMatch`?

**Answer:** `findFirst()` returns the first element in encounter order. `findAny()` may return any element; in parallel streams it is faster because it doesn't need to respect order. Both return `Optional`.

`anyMatch` = at least one matches, `allMatch` = all match, `noneMatch` = none match. All short-circuit. On an **empty stream**, `allMatch` and `noneMatch` return `true`, `anyMatch` returns `false`.

```java
boolean anyHr      = emps.stream().anyMatch(e -> e.dept().equals("HR"));     // true
boolean allAdults  = emps.stream().allMatch(e -> e.age() >= 18);              // true
boolean noneIntern = emps.stream().noneMatch(e -> e.salary() < 10000);        // true
```

### Q63. What is the difference between `map()` + `reduce()` and `collect()`? When do you use `reduce`?

**Answer:**
- `reduce` combines elements into **one immutable value** (sum, max, concatenated result). The accumulator must be associative and the identity must be neutral (`0` for sum, `1` for product).
- `collect` is a **mutable reduction**: it accumulates into a container (`List`, `Map`, `StringBuilder`). It is efficient and parallel-safe.
- Prefer specialized methods when they exist: `mapToInt(...).sum()`, `max(...)`, `Collectors.joining()`.

```java
int sum = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);                          // 10
Optional<Integer> max = Stream.of(4, 9, 2).reduce(Integer::max);                   // Optional[9]
double totalSalary = emps.stream().mapToDouble(Employee::salary).sum();           // preferred
```

### Q64. What are primitive streams (`IntStream`, `LongStream`, `DoubleStream`) and why use them?

**Answer:** They avoid boxing/unboxing overhead of `Stream<Integer>` and provide numeric methods: `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()`, `range()`, `rangeClosed()`. Convert with `mapToInt` / `mapToObj` / `boxed()`.

```java
int sum = IntStream.rangeClosed(1, 100).sum();                                    // 5050
OptionalDouble avg = emps.stream().mapToDouble(Employee::salary).average();
DoubleSummaryStatistics stats = emps.stream()
        .mapToDouble(Employee::salary).summaryStatistics();                        // count, min, max, avg, sum
List<Integer> boxed = IntStream.range(0, 5).boxed().toList();                     // [0, 1, 2, 3, 4]
```

### Q65. What is the difference between `Collectors.toList()`, `Stream.toList()`, and `Collectors.toUnmodifiableList()`?

**Answer:**
- `collect(Collectors.toList())`: returns a mutable `ArrayList` in practice (not guaranteed by spec); allows `null`.
- `stream.toList()` (Java 16+): returns an **unmodifiable** list; allows `null` elements.
- `collect(Collectors.toUnmodifiableList())` (Java 10+): unmodifiable; throws `NullPointerException` on `null`.

### Q66. What happens with `Collectors.toMap()` when there are duplicate keys? How do you keep insertion order?

**Answer:** `toMap(keyMapper, valueMapper)` throws `IllegalStateException: Duplicate key` if two elements map to the same key. It also throws `NullPointerException` if a value is `null`. Pass a **merge function** to resolve duplicates, and a **map supplier** to choose the map type.

```java
// Duplicate departments -> keep the higher salary; LinkedHashMap keeps encounter order
Map<String, Double> maxSalaryByDept = emps.stream()
        .collect(Collectors.toMap(
                Employee::dept,
                Employee::salary,
                Math::max,
                LinkedHashMap::new));                                              // {IT=90000.0, HR=65000.0, Finance=80000.0}
```

### Q67. Explain `groupingBy` vs `partitioningBy` with downstream collectors.

**Answer:**
- `groupingBy(classifier)` creates a `Map<K, List<T>>` with any number of keys.
- `partitioningBy(predicate)` always creates a `Map<Boolean, List<T>>` with exactly two keys, `true` and `false` (even if one is empty).
- Both accept a **downstream collector**: `counting()`, `summingDouble()`, `averagingDouble()`, `mapping()`, `maxBy()`, `toSet()`, or another `groupingBy` (multi-level grouping).

```java
Map<String, Long> countByDept = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));

Map<String, Double> avgSalaryByDept = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept, Collectors.averagingDouble(Employee::salary)));

Map<String, List<String>> namesByDept = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                 Collectors.mapping(Employee::name, Collectors.toList())));      // sorted keys

Map<String, Map<String, Long>> byDeptThenGender = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept,
                 Collectors.groupingBy(Employee::gender, Collectors.counting())));

Map<Boolean, List<Employee>> highEarners = emps.stream()
        .collect(Collectors.partitioningBy(e -> e.salary() > 70000));
```

### Q68. What is `peek()` for, and why shouldn't it be used for business logic?

**Answer:** `peek` is an intermediate operation meant for **debugging** (logging elements as they pass). Because streams are lazy, `peek` does not run without a terminal operation. Since Java 9, some terminals (e.g. `count()` on a sized stream) can skip the pipeline entirely, so `peek` side effects may never run. Use `forEach` or `map` for real logic.

```java
long c = List.of(1, 2, 3).stream().peek(System.out::println).count();   // may print nothing (Java 9+)
```

### Q69. What are the common Stream pitfalls?

**Answer:**
1. **Reusing a stream** -> `IllegalStateException: stream has already been operated upon or closed`. Use a `Supplier<Stream<T>>` if needed.
2. **Modifying the source inside the pipeline** (interference) -> `ConcurrentModificationException` or undefined results.
3. **Stateful lambdas / shared mutable state** in `forEach` or `map` (e.g. adding to an external `ArrayList`), especially with parallel streams. Use `collect`.
4. **Checked exceptions** cannot be thrown from `Function`/`Predicate` lambdas; wrap them or extract a helper method.
5. **Infinite streams** (`Stream.iterate`, `Stream.generate`) without `limit`/`takeWhile` never end.
6. **Calling `Optional.get()`** on `findFirst`/`max` without checking; prefer `orElse`, `orElseThrow`, `ifPresent`.
7. **`sorted()` / `distinct()` on huge streams** hold all elements in memory.
8. **Overusing streams**: a simple loop is sometimes clearer and faster (e.g. tight numeric loops, early `break` with complex conditions).
9. **Parallel streams** for small data, I/O, or ordered operations are often slower (see Q48).

```java
Supplier<Stream<String>> names = () -> emps.stream().map(Employee::name);
long n = names.get().count();
List<String> list = names.get().toList();           // fresh stream each time
```

### Q70. How do you create streams from different sources?

```java
Stream<String> s1 = List.of("a", "b").stream();
Stream<String> s2 = Stream.of("a", "b", "c");
IntStream     s3 = Arrays.stream(new int[]{1, 2, 3});
IntStream     s4 = "hello".chars();
Stream<String> s5 = Stream.iterate("x", v -> v + "x").limit(3);            // x, xx, xxx
Stream<Integer> s6 = Stream.iterate(1, i -> i <= 100, i -> i * 2);          // Java 9: 1..64
Stream<Double> s7 = Stream.generate(Math::random).limit(5);
Stream<String> s8 = Pattern.compile(",").splitAsStream("a,b,c");
try (Stream<String> lines = Files.lines(Path.of("data.txt"))) {           // close I/O streams
    lines.filter(l -> !l.isBlank()).forEach(System.out::println);
}
Stream<Map.Entry<String, Integer>> s9 = Map.of("a", 1).entrySet().stream();
```

### Q71. Coding: Find the second highest salary / the Nth highest distinct value.

```java
Optional<Double> secondHighest = emps.stream()
        .map(Employee::salary)
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst();                                                      // 80000.0

// Employee with the highest salary in each department
Map<String, Optional<Employee>> topByDept = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept,
                 Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));

// Without Optional in the map values
Map<String, Employee> topByDept2 = emps.stream()
        .collect(Collectors.toMap(Employee::dept, Function.identity(),
                 BinaryOperator.maxBy(Comparator.comparingDouble(Employee::salary))));
```

### Q72. Coding: Sort employees by department, then salary descending, then name.

```java
List<Employee> sorted = emps.stream()
        .sorted(Comparator.comparing(Employee::dept)
                .thenComparing(Employee::salary, Comparator.reverseOrder())
                .thenComparing(Employee::name))
        .toList();

// Top 3 earners
List<String> top3 = emps.stream()
        .sorted(Comparator.comparingDouble(Employee::salary).reversed())
        .limit(3)
        .map(Employee::name)
        .toList();                                                         // [Asha, Karan, Ravi]
```

### Q73. Coding: Count the frequency of each character / word, and find the first non-repeated character.

```java
String input = "programming";

Map<Character, Long> freq = input.chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
// {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}

Character firstNonRepeated = freq.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);                                                     // 'p'

Character firstRepeated = freq.entrySet().stream()
        .filter(e -> e.getValue() > 1)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);                                                     // 'r'

Map<String, Long> wordCount = Arrays.stream("java is fun and java is fast".split("\\s+"))
        .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));
```

### Q74. Coding: Find duplicate elements in a list.

```java
List<Integer> nums = List.of(1, 2, 3, 2, 4, 5, 3, 3);

// Using a Set (add() returns false for duplicates)
Set<Integer> seen = new HashSet<>();
Set<Integer> dups = nums.stream()
        .filter(n -> !seen.add(n))
        .collect(Collectors.toSet());                                      // [2, 3]  (sequential streams only)

// Pure functional approach (safe for parallel)
List<Integer> dups2 = nums.stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .filter(e -> e.getValue() > 1)
        .map(Map.Entry::getKey)
        .toList();
```

### Q75. Coding: Common stream one-liners on a list of numbers / strings.

```java
List<Integer> nums = List.of(5, 3, 8, 1, 9, 2, 8);

List<Integer> evens       = nums.stream().filter(n -> n % 2 == 0).toList();              // [8, 2, 8]
List<Integer> squares     = nums.stream().map(n -> n * n).toList();
int max                   = nums.stream().max(Integer::compare).orElseThrow();            // 9
int sumOfOdds             = nums.stream().filter(n -> n % 2 != 0).mapToInt(Integer::intValue).sum(); // 18
List<Integer> sortedDesc  = nums.stream().sorted(Comparator.reverseOrder()).toList();
List<Integer> unique      = nums.stream().distinct().toList();
List<String> startWith1   = nums.stream().map(String::valueOf).filter(s -> s.startsWith("1")).toList();

List<String> words = List.of("apple", "banana", "kiwi", "cherry");
String longest            = words.stream().max(Comparator.comparingInt(String::length)).orElse("");   // banana
String joined             = words.stream().map(String::toUpperCase).collect(Collectors.joining(", ", "[", "]"));
Map<Integer, List<String>> byLength = words.stream().collect(Collectors.groupingBy(String::length));
String reversed           = new StringBuilder("stream").reverse().toString();
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
List<Integer> flat        = nested.stream().flatMap(List::stream).toList();                // [1, 2, 3, 4]
```

### Q76. Coding: Convert a list to a map, merge two lists, and check for palindromes / anagrams.

```java
// List -> Map<name, salary>
Map<String, Double> salaryByName = emps.stream()
        .collect(Collectors.toMap(Employee::name, Employee::salary));

// Merge two lists without duplicates, sorted
List<Integer> merged = Stream.concat(List.of(1, 3, 5).stream(), List.of(3, 4, 5).stream())
        .distinct().sorted().toList();                                     // [1, 3, 4, 5]

// Palindrome check
String w = "madam";
boolean palindrome = IntStream.range(0, w.length() / 2)
        .allMatch(i -> w.charAt(i) == w.charAt(w.length() - 1 - i));      // true

// Anagram check
boolean anagram = Arrays.equals(
        "listen".chars().sorted().toArray(),
        "silent".chars().sorted().toArray());                              // true

// Group anagrams
Map<String, List<String>> groups = Stream.of("eat", "tea", "tan", "ate", "nat")
        .collect(Collectors.groupingBy(s -> s.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()));                                             // {aet=[eat, tea, ate], ant=[tan, nat]}
```

### Q77. What are the Stream API additions in Java 9 to 21?

**Answer:** After `Stream` was introduced in Java 8, later releases added methods to fix boilerplate, eliminate unnecessary stream allocations, support short-circuiting on ordered data, and bridge streams with new collection features.

| Release | New Methods / Features | Problem It Solves |
|---|---|---|
| **Java 9** | `takeWhile`, `dropWhile`, `Stream.ofNullable`, `Stream.iterate(seed, hasNext, next)`, `Optional.stream()`, `Collectors.filtering`, `Collectors.flatMapping` | Conditional slicing, null-safe 0/1-element streams, bounded loops without `limit()`, and keeping empty groups in `groupingBy` |
| **Java 10** | `Collectors.toUnmodifiableList()`, `toUnmodifiableSet()`, `toUnmodifiableMap()` | Collects directly into truly immutable collections (`null`-hostile) |
| **Java 11** | `Predicate.not(...)`, `String.lines()` | Clean negation of method references (`filter(Predicate.not(String::isBlank))`) and lazy line streaming |
| **Java 12** | `Collectors.teeing(c1, c2, merger)` | Runs two collectors in a **single pass** and merges their results (e.g., min + max, or count + average) |
| **Java 16** | `Stream.toList()`, `Stream.mapMulti()` (`mapMultiToInt/Long/Double`) | Concise unmodifiable list terminal op; zero-allocation 1-to-many mapping alternative to `flatMap` |
| **Java 21** | `SequencedCollection.reversed().stream()` | Stream any `List`, `Deque`, `LinkedHashSet` in reverse order without copying or indexing |
| *(Java 22–24)* | `Stream.gather(Gatherer)` (`Gatherers.windowFixed`, `windowSliding`, `fold`, `scan`) | Custom intermediate operations (sliding/fixed windows, stateful folding) |

---

#### 1. Java 9 Additions

- **`takeWhile(Predicate)` & `dropWhile(Predicate)`:**
  - `filter()` checks **every** element in the stream.
  - `takeWhile()` takes elements from the start **only while the predicate is true**, and **stops immediately (short-circuits)** on the first `false`.
  - `dropWhile()` discards elements from the start while the predicate is true, and once it hits the first `false`, emits **all remaining elements** without testing them again.
- **`Stream.ofNullable(T)`:** returns a 1-element stream if non-null, or `Stream.empty()` if `null`. Eliminates `val == null ? Stream.empty() : Stream.of(val)` inside `flatMap`.
- **3-arg `Stream.iterate(seed, hasNextPredicate, unaryOperator)`:** works just like a classic `for (int i = 1; i <= 100; i *= 2)` loop without needing `.limit()`.
- **`Optional.stream()`:** turns an `Optional<T>` into a 0-or-1 element `Stream<T>`, making `streamOfOptionals.flatMap(Optional::stream)` clean.
- **`Collectors.filtering` & `Collectors.flatMapping`:** used as downstream collectors inside `groupingBy`. Unlike `.filter(...).collect(groupingBy(...))` (which completely drops departments where no employee matched the filter), `groupingBy(dept, filtering(...))` preserves every department key with an empty `[]` list.

```java
// 1. takeWhile vs dropWhile (Notice how trailing '1, 2' are handled!)
List<Integer> nums = List.of(1, 2, 3, 7, 1, 2);
List<Integer> taken   = nums.stream().takeWhile(n -> n < 5).toList(); // [1, 2, 3] (stops at 7)
List<Integer> dropped = nums.stream().dropWhile(n -> n < 5).toList(); // [7, 1, 2] (keeps everything from 7 onward)

// 2. Stream.ofNullable — safely lookup keys in a Map without nulls
Map<String, String> env = Map.of("DB_HOST", "localhost", "DB_PORT", "5432");
List<String> values = Stream.of("DB_HOST", "MISSING_KEY", "DB_PORT")
        .flatMap(key -> Stream.ofNullable(env.get(key)))
        .toList();                                                    // [localhost, 5432]

// 3. Bounded Stream.iterate (for-loop equivalent)
List<Integer> powersOfTwo = Stream.iterate(1, n -> n <= 64, n -> n * 2)
        .toList();                                                    // [1, 2, 4, 8, 16, 32, 64]

// 4. Optional.stream() — unwrap present Optionals in one step
List<Optional<String>> raw = List.of(Optional.of("A"), Optional.empty(), Optional.of("B"));
List<String> present = raw.stream().flatMap(Optional::stream).toList(); // [A, B]

// 5. Collectors.filtering inside groupingBy (preserves HR key with empty list!)
Map<String, List<Employee>> highEarnersByDept = emps.stream()
        .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.filtering(e -> e.salary() > 70000, Collectors.toList())));
// {IT=[Asha, Ravi], HR=[], Finance=[Karan]}
```

---

#### 2. Java 10 & Java 11 Additions

- **Java 10 `Collectors.toUnmodifiableList()` / `toUnmodifiableSet()` / `toUnmodifiableMap()`:** produces truly immutable collections (`UnsupportedOperationException` on mutation) and rejects `null` elements with `NullPointerException`.
- **Java 11 `Predicate.not(Predicate)` & `String.lines()`:** lets you negate method references cleanly (`Predicate.not(String::isBlank)` instead of `s -> !s.isBlank()`).

```java
String multiline = "order-101\n   \norder-102\n";
List<String> orderIds = multiline.lines()                             // Java 11 lazy line stream
        .filter(Predicate.not(String::isBlank))                       // Java 11 negated method reference
        .collect(Collectors.toUnmodifiableList());                    // Java 10 immutable list -> [order-101, order-102]
```

---

#### 3. Java 12 Addition: `Collectors.teeing()`

When you need **two different aggregations** over the same stream (e.g., min and max, or filtered list and total count), previously you had to either stream the collection twice or write a custom collector. `Collectors.teeing(collector1, collector2, mergerBiFunction)` feeds each element into both downstream collectors in a **single pass** and combines the two results at the end.

```java
record SalaryReport(Employee lowest, Employee highest, double average) {}

// Find lowest earner AND highest earner in a single stream pass
String minMaxSummary = emps.stream().collect(Collectors.teeing(
        Collectors.minBy(Comparator.comparingDouble(Employee::salary)),
        Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
        (minOpt, maxOpt) -> minOpt.orElseThrow().name() + " to " + maxOpt.orElseThrow().name()
));                                                                   // "John to Asha"

// Compute both count and average salary in one pass
Map<String, Number> stats = emps.stream().collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.averagingDouble(Employee::salary),
        (count, avg) -> Map.of("count", count, "avgSalary", avg)
));                                                                   // {count=5, avgSalary=72000.0}
```

---

#### 4. Java 16 Additions: `Stream.toList()` and `Stream.mapMulti()`

- **`stream.toList()`:**
  - Short terminal operation returning an **unmodifiable** `List`.
  - Unlike `Collectors.toUnmodifiableList()`, `stream.toList()` **allows `null` elements** and is faster because it sizes the backing array directly when the stream size is known.
- **`stream.mapMulti(BiConsumer<T, Consumer<R>>)`:**
  - An imperative, high-performance alternative to `flatMap`.
  - With `flatMap`, you must create a new `Stream` object for **every single input element**, which causes heavy allocation overhead when mapping 0 or 1–2 items.
  - With `mapMulti`, you receive a `downstream.accept(...)` callback and push 0, 1, or many elements onto the pipeline **without allocating intermediate `Stream` objects** (and you can easily use `instanceof` pattern matching).

```java
// mapMulti: filter + cast + transform in one step with ZERO inner Stream allocations
List<Object> mixed = List.of("Java", 42, "Spring Boot", -5, 100);

List<String> validItems = mixed.stream()
        .<String>mapMulti((element, downstream) -> {
            if (element instanceof String s && !s.isBlank()) {
                downstream.accept(s.toUpperCase());
            } else if (element instanceof Integer n && n > 0) {
                downstream.accept("NUM:" + n);
            }
            // negative numbers and other types simply call downstream.accept() 0 times!
        })
        .toList();                                                    // [JAVA, NUM:42, SPRING BOOT, NUM:100]
```

---

#### 5. Java 21 Addition: `SequencedCollection` + `.reversed().stream()`

Before Java 21, streaming a `List` in reverse order required `IntStream.range(...)` index math or copying and mutating a list with `Collections.reverse()`. Java 21 introduced `SequencedCollection` (`List`, `Deque`, `LinkedHashSet`), which provides a lightweight, zero-copy `.reversed()` view that you can stream directly.

```java
List<String> steps = List.of("Init", "Validate", "Charge", "Ship");

List<String> rollbackOrder = steps.reversed()                         // zero-copy reverse view (Java 21)
        .stream()
        .map(step -> "Undo-" + step)
        .toList();                                                    // [Undo-Ship, Undo-Charge, Undo-Validate, Undo-Init]
```

### Q78. What is `Predicate` in Java, and how do you compose and use it?

**Answer:** `Predicate<T>` (in `java.util.function`) is a built-in `@FunctionalInterface` introduced in Java 8 that takes **one input of type `T`** and returns a **`boolean`**. It represents a condition or filter rule that you can store in a variable, pass as a method argument, combine dynamically, and reuse across Streams, `Optional`, and Collections.

#### 1. Core Methods of `Predicate<T>`

| Method | Type | Description |
|---|---|---|
| `boolean test(T t)` | Abstract (SAM) | Evaluates the condition on input `t` |
| `and(Predicate<? super T> other)` | Default | Logical short-circuit `&&` (`p1.and(p2)`) |
| `or(Predicate<? super T> other)` | Default | Logical short-circuit `\|\|` (`p1.or(p2)`) |
| `negate()` | Default | Logical `!` (`p1.negate()`) |
| `Predicate.not(Predicate<T>)` | Static (Java 11) | Negates a method reference cleanly (`Predicate.not(String::isBlank)`) |
| `Predicate.isEqual(Object target)` | Static | Null-safe equality check (`Objects.equals(t, target)`) |

#### 2. Specialized Variants (Avoid Boxing Overhead)
- **Two arguments:** `BiPredicate<T, U>` -> `boolean test(T t, U u)`
- **Primitives (no auto-boxing):** `IntPredicate`, `LongPredicate`, `DoublePredicate`

#### 3. Where `Predicate` is Used in the JDK
- `Stream.filter(Predicate)`, `anyMatch`, `allMatch`, `noneMatch`, `takeWhile`, `dropWhile`
- `Collectors.partitioningBy(Predicate)` and `Collectors.filtering(Predicate, Collector)`
- `Collection.removeIf(Predicate)` (removes matching elements in-place)
- `Optional.filter(Predicate)`

#### 4. Example: Composing Dynamic Business Rules with `Predicate`

```java
// 1. Define reusable atomic predicates
Predicate<Employee> isItDept      = e -> "IT".equals(e.dept());
Predicate<Employee> isHrDept      = e -> "HR".equals(e.dept());
Predicate<Employee> isHighEarner  = e -> e.salary() >= 75000;
Predicate<Employee> isSeniorAge   = e -> e.age() >= 35;

// 2. Compose complex rules using and(), or(), negate()
// Rule: (IT or HR) AND High Earner AND NOT Senior Age
Predicate<Employee> targetRule = isItDept.or(isHrDept)
        .and(isHighEarner)
        .and(isSeniorAge.negate());

List<String> matched = emps.stream()
        .filter(targetRule)
        .map(Employee::name)
        .toList();                                                    // [Asha, Ravi]

// 3. Dynamic filter chain (e.g., building search filters from optional request parameters)
List<Predicate<Employee>> dynamicFilters = List.of(isItDept, isHighEarner);
Predicate<Employee> combinedFilter = dynamicFilters.stream()
        .reduce(x -> true, Predicate::and);                           // chains all with AND

// 4. Using Predicate.not (Java 11), Predicate.isEqual, and Collection.removeIf
List<String> tags = new ArrayList<>(Arrays.asList("java", "", "  ", "spring", "deprecated"));
tags.removeIf(String::isBlank);                                       // removes "" and "  " in-place
tags.removeIf(Predicate.isEqual("deprecated"));                       // removes "deprecated"

// 5. BiPredicate (2 inputs) & IntPredicate (primitive, no Integer boxing)
BiPredicate<Employee, Double> earnsMoreThan = (emp, threshold) -> emp.salary() > threshold;
boolean ashaRich = earnsMoreThan.test(emps.get(0), 80000.0);          // true

IntPredicate isEven = n -> n % 2 == 0;
int[] evens = IntStream.rangeClosed(1, 10).filter(isEven).toArray();  // [2, 4, 6, 8, 10]
```
