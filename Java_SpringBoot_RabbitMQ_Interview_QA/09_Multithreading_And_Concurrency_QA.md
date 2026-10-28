
# Multithreading and Concurrency: Guide & Interview Question Set

This guide explains Java threads and concurrency with beginner-friendly examples, reference source code, and deep-dive interview questions and answers (Q1 to Q35). Code snippets focus on the Java standard library; imports and application-specific classes may need to be added.

## Contents

1. [Processes, threads, and concurrency](#1-processes-threads-and-concurrency)
2. [Creating and starting work](#2-creating-and-starting-work)
3. [Thread lifecycle and interruption](#3-thread-lifecycle-and-interruption)
4. [Race conditions and synchronization](#4-race-conditions-and-synchronization)
5. [Visibility and `volatile`](#5-visibility-and-volatile)
6. [Atomic variables and locks](#6-atomic-variables-and-locks)
7. [Waiting and coordinating threads](#7-waiting-and-coordinating-threads)
8. [Executors and thread pools](#8-executors-and-thread-pools)
9. [Results and errors from asynchronous work](#9-results-and-errors-from-asynchronous-work)
10. [Concurrent collections](#10-concurrent-collections)
11. [Producer-consumer pattern](#11-producer-consumer-pattern)
12. [Deadlock and other concurrency problems](#12-deadlock-and-other-concurrency-problems)
13. [Thread safety and safe sharing](#13-thread-safety-and-safe-sharing)
14. [Virtual threads](#14-virtual-threads)
15. [Choosing a concurrency tool](#15-choosing-a-concurrency-tool)
16. [Practical rules](#16-practical-rules)
17. [Interview quick review](#17-interview-quick-review)
18. [Source code from `DSA-main.zip`](#18-source-code-from-dsa-mainzip)
19. [Multithreading & Concurrency Interview Questions & Answers (Q1 to Q35)](#19-multithreading--concurrency-interview-questions--answers)

## 1. Processes, threads, and concurrency

A **process** is a running program with its own memory. A **thread** is one path of execution inside a process.

Threads in the same process share objects on the heap. Each thread has its own call stack and local variables. Sharing makes communication convenient, but it also means two threads can change the same object at the same time.

**Concurrency** means several tasks are in progress during the same period. They may take turns on one CPU core. **Parallelism** means tasks are actually running at the same time, for example on separate CPU cores.

Example: a web server can handle two requests concurrently. If the machine has enough CPU cores, some parts of those requests may also run in parallel.

## 2. Creating and starting work

### `Thread` and `Runnable`

A `Runnable` describes work. A `Thread` runs work on a separate thread. Keeping task logic in a `Runnable` makes it reusable and lets a thread or executor decide how to run it.

```java
class GreetingTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running on " + Thread.currentThread().getName());
    }
}

Thread worker = new Thread(new GreetingTask());
worker.start();
```

Call `start()` to ask Java to create a separate execution path and call `run()` on it. Calling `run()` directly is just an ordinary method call on the current thread.

```java
Thread worker = new Thread(() ->
    System.out.println(Thread.currentThread().getName()));

worker.run();   // Runs on the current thread
worker.start(); // Runs on a new thread
```

You can also subclass `Thread`, but this combines the task and the thread mechanism and uses up the class's one superclass. Prefer `Runnable` or `Callable` for task definitions.

### `Runnable` versus `Callable`

`Runnable` does not return a result and cannot declare a checked exception from `run()`. `Callable<T>` returns a result and can throw an exception.

```java
Callable<Integer> calculateTotal = () -> 20 + 22;
```

In application code, tasks are usually submitted to an executor rather than starting a new thread manually for every operation.

## 3. Thread lifecycle and interruption

The main `Thread.State` values are:

- `NEW`: created, but `start()` has not been called.
- `RUNNABLE`: running or ready to run. Java does not distinguish these as separate states.
- `BLOCKED`: waiting to enter a `synchronized` section because another thread owns its monitor.
- `WAITING`: waiting indefinitely for another thread or action, such as an untimed `join()`.
- `TIMED_WAITING`: waiting for a limited time, such as during `sleep()` or a timed wait.
- `TERMINATED`: the `run()` method has finished.

### Waiting for a thread

`join()` makes the current thread wait until another thread finishes.

```java
Thread worker = new Thread(() -> doWork());
worker.start();

worker.join(); // Current thread waits for worker to finish
System.out.println("Work is complete");
```

`sleep()` pauses the current thread for a duration. It does **not** release a monitor lock that the thread already holds.

### Interrupting a thread

Interruption is a cooperative request to stop or cancel work. It does not forcibly kill a thread. Blocking methods such as `sleep()` and `join()` usually respond by throwing `InterruptedException`.

```java
class PollingTask implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            pollForWork();
        }
        cleanUp();
    }

    private void pollForWork() {
        // Check for work
    }

    private void cleanUp() {
        // Release resources
    }
}
```

When a method cannot rethrow `InterruptedException`, restore the interrupt flag after handling it:

```java
try {
    Thread.sleep(1_000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

Do not swallow an interrupt and continue as if nothing happened. Higher-level code may rely on the signal to cancel work or shut down.

## 4. Race conditions and synchronization

A **race condition** occurs when the result depends on the timing or interleaving of operations between threads.

For example, `count++` looks like one operation but involves reading the value, adding one, and writing it back. Two threads can both read `0`, both calculate `1`, then both write `1`. The result is `1`, not `2`.

```java
class Counter {
    private int count;

    void increment() {
        count++; // Not thread-safe when shared by multiple threads
    }

    int value() {
        return count;
    }
}
```

### Use `synchronized` for a critical section

The `synchronized` keyword lets only one thread at a time execute code guarded by the same monitor. It also provides visibility: changes made before releasing the monitor become visible to a thread that later acquires that monitor.

```java
class SafeCounter {
    private int count;

    synchronized void increment() {
        count++;
    }

    synchronized int value() {
        return count;
    }
}
```

For object-level locking, synchronize on a private lock rather than a publicly accessible object:

```java
class Account {
    private final Object lock = new Object();
    private int balance;

    void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        synchronized (lock) {
            balance += amount;
        }
    }

    int balance() {
        synchronized (lock) {
            return balance;
        }
    }
}
```

Keep synchronized sections short. Do not hold a lock while making a slow network call, waiting on a user, or calling code whose behavior you do not control.

## 5. Visibility and `volatile`

The Java Memory Model defines when one thread's changes are guaranteed to be visible to another thread.

`volatile` makes reads and writes of one variable visible across threads. It is useful for simple flags and does not provide mutual exclusion. It does **not** make compound operations such as `count++` atomic.

```java
class Worker implements Runnable {
    private volatile boolean running = true;

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            doOneUnitOfWork();
        }
    }

    private void doOneUnitOfWork() {
        // Perform one small step
    }
}
```

The volatile write of `false` becomes visible to the worker's later volatile read. Use `AtomicInteger`, `synchronized`, or a lock for a shared counter.

## 6. Atomic variables and locks

### Atomic classes

Classes such as `AtomicInteger` provide thread-safe operations, including atomic increments and compare-and-set.

```java
AtomicInteger completedJobs = new AtomicInteger();

void jobFinished() {
    int total = completedJobs.incrementAndGet();
    System.out.println("Completed jobs: " + total);
}
```

`LongAdder` is often useful for high-contention metrics counters. It spreads updates internally and provides the current total with `sum()`. It is not intended for every situation where an immediately exact counter value is required.

### `Lock` and `ReentrantLock`

Use a `Lock` when you need features such as a timed lock attempt or a separate condition. Always release it in `finally`.

```java
class LockedCounter {
    private final Lock lock = new ReentrantLock();
    private int count;

    void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}
```

A timed attempt can avoid waiting forever for a lock:

```java
if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
    try {
        updateSharedState();
    } finally {
        lock.unlock();
    }
} else {
    handleBusyResource();
}
```

Use the simplest tool that meets the requirement. For a basic counter, an atomic class is usually simpler than a custom lock.

## 7. Waiting and coordinating threads

Avoid using `Thread.sleep()` to coordinate tasks. It only pauses for a duration; it does not mean another task has completed. Use a coordination utility that expresses what the program is waiting for.

### `CountDownLatch`

A latch lets one or more threads wait until a fixed number of events have occurred. It is one-shot: once it reaches zero, it cannot be reset.

```java
CountDownLatch ready = new CountDownLatch(2);

Thread databaseCheck = new Thread(() -> {
    connectToDatabase();
    ready.countDown();
});
Thread brokerCheck = new Thread(() -> {
    connectToBroker();
    ready.countDown();
});

databaseCheck.start();
brokerCheck.start();

ready.await(); // Continue only when both checks have finished
startApplicationTraffic();
```

### `CyclicBarrier`

A barrier makes a fixed number of threads wait until all have reached the same point. It can be reused for multiple phases.

```java
CyclicBarrier barrier = new CyclicBarrier(3);

Runnable worker = () -> {
    calculatePartition();
    try {
        barrier.await(); // Wait for the other two workers
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } catch (BrokenBarrierException e) {
        throw new IllegalStateException(e);
    }
    combineResults();
};
```

### `Semaphore`

A semaphore limits how many threads can access a resource at once. It is useful for protecting a service with limited capacity, such as an external API.

```java
Semaphore permits = new Semaphore(5);

permits.acquire();
try {
    callPartnerApi();
} finally {
    permits.release();
}
```

### `Phaser`

A phaser supports reusable phases and a changing number of participants. Use it for staged work when workers may join or leave between phases. For a fixed group, `CountDownLatch` or `CyclicBarrier` is often simpler.

## 8. Executors and thread pools

An `ExecutorService` separates task submission from thread management. Instead of creating one thread for every task, submit tasks to an executor.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
try {
    for (String file : files) {
        executor.submit(() -> processFile(file));
    }
} finally {
    executor.shutdown(); // Stop accepting new work
}
```

Call `shutdown()` to stop accepting new tasks while allowing submitted tasks to finish. `shutdownNow()` requests interruption of active work and returns tasks that never started; tasks still need to cooperate with interruption.

### Common executor choices

- `newFixedThreadPool(n)`: at most `n` worker threads, but its default queue is unbounded. A sustained overload can use too much memory.
- `newCachedThreadPool()`: creates threads as needed and can grow very large during a burst.
- `newSingleThreadExecutor()`: runs tasks one at a time, in order, with one worker thread.
- `ThreadPoolExecutor`: lets you explicitly set core and maximum pool sizes, queue capacity, thread factory, and rejection policy.
- `newVirtualThreadPerTaskExecutor()` (Java 21): starts a virtual thread per submitted task; it is not a fixed-size pool.

For a production service with bounded capacity, configure the queue and rejection policy deliberately:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4,                              // Core threads
    8,                              // Maximum threads
    30, TimeUnit.SECONDS,           // Idle time for extra threads
    new ArrayBlockingQueue<>(100),  // Bounded waiting queue
    new ThreadPoolExecutor.AbortPolicy()
);
```

The executor generally starts core threads first, then queues tasks. When the queue is full, it may grow toward the maximum size. If both the queue and threads are full, it applies the rejection policy.

`AbortPolicy` throws `RejectedExecutionException`. Other policies include `CallerRunsPolicy`, which runs the task on the submitting thread and slows down submissions. Choose a policy based on whether the application can safely reject, delay, or drop work; do not silently discard important work.

## 9. Results and errors from asynchronous work

### `Future`

A `Future<T>` represents a result that may become available later. Calling `get()` waits until the task finishes. A timed `get()` limits how long the caller waits.

```java
Future<Integer> result = executor.submit(() -> calculate());
Integer value = result.get(2, TimeUnit.SECONDS);
```

### `CompletableFuture`

`CompletableFuture` lets you compose asynchronous steps and handle success or failure.

```java
CompletableFuture<String> email = CompletableFuture
    .supplyAsync(() -> loadCustomer(customerId), executor)
    .thenApply(Customer::email)
    .exceptionally(error -> "unknown@example.com");
```

Pass an executor when the work blocks or needs an isolated pool. Without one, many async methods use the shared common `ForkJoinPool`.

Use `thenCompose` when a step returns another `CompletableFuture` and you want one flattened result:

```java
CompletableFuture<Order> order =
    loadCustomer(customerId)
        .thenCompose(customer -> loadLatestOrder(customer.id(), executor));
```

Use `thenCombine` to combine independent results:

```java
CompletableFuture<Customer> customer =
    loadCustomer(customerId, executor);
CompletableFuture<List<Order>> orders =
    loadOrders(customerId, executor);

CompletableFuture<CustomerSummary> summary =
    customer.thenCombine(orders, CustomerSummary::new);
```

Handle failures intentionally. A fallback value is only appropriate when the application has a meaningful fallback; otherwise propagate the error with context.

## 10. Concurrent collections

Regular collections such as `ArrayList` and `HashMap` are not safe for concurrent modification without external coordination.

- `ConcurrentHashMap`: supports concurrent map operations. Use atomic methods such as `computeIfAbsent` for check-and-update logic.
- `CopyOnWriteArrayList`: iterators see a snapshot. Useful when reads are frequent and updates are rare; writes copy the underlying array.
- `BlockingQueue`: supports thread-safe producer-consumer handoff and optional waiting when empty or full.
- `Collections.synchronizedMap(...)`: synchronizes individual operations, but iteration still needs synchronization on the returned map.

Example of atomic cache population:

```java
ConcurrentMap<String, User> cache = new ConcurrentHashMap<>();
User user = cache.computeIfAbsent(userId, id -> loadUser(id));
```

Do not assume a sequence of individually thread-safe calls is itself atomic. For example, a separate `containsKey()` followed by `put()` can still race.

## 11. Producer-consumer pattern

A `BlockingQueue` is a simple way for producer threads to hand work to consumer threads. A bounded queue limits memory use and applies backpressure when producers are faster than consumers.

```java
BlockingQueue<String> jobs = new ArrayBlockingQueue<>(100);

// Producer
jobs.put("job-1"); // Waits if the queue is full

// Consumer
String jobId = jobs.take(); // Waits if the queue is empty
process(jobId);
```

In a real service, define how shutdown is signaled, how failed work is retried, and whether a full queue should block, reject, or persist the work elsewhere.

## 12. Deadlock and other concurrency problems

### Deadlock

A deadlock occurs when threads wait for locks held by each other. A common prevention method is to acquire multiple locks in one consistent global order.

```text
Thread A owns Lock 1 and waits for Lock 2
Thread B owns Lock 2 and waits for Lock 1
Neither thread can continue
```

Other ways to reduce risk:

- Keep critical sections small.
- Avoid calling unknown or slow code while holding a lock.
- Use timed `tryLock()` where appropriate.
- Capture thread dumps to investigate a suspected deadlock.

### Livelock and starvation

- **Livelock:** threads keep reacting to each other but make no progress.
- **Starvation:** a thread waits too long because other work repeatedly gets resources first.
- **Contention:** many threads compete for the same lock or resource, reducing throughput.

Adding more threads does not necessarily improve performance. If all threads wait for the same database connection or lock, more threads may make contention worse.

## 13. Thread safety and safe sharing

An object is thread-safe when concurrent use follows its intended rules and does not corrupt its state.

Useful strategies:

1. **Immutability:** create the object once and do not change its state.
2. **Thread confinement:** keep an object used by only one thread.
3. **Synchronization:** guard shared mutable state with a common lock.
4. **Atomic classes:** use built-in atomic operations for simple shared values.
5. **Concurrent data structures:** use collections designed for concurrent access.

Do not publish a partially initialized object to other threads. Prefer immutable fields and constructor initialization.

`ThreadLocal` gives each thread its own value, but pooled threads are reused. Remove values when the task ends:

```java
try {
    requestContext.set(context);
    handleRequest();
} finally {
    requestContext.remove();
}
```

## 14. Virtual threads

Virtual threads, available as a standard feature in Java 21, are lightweight threads managed by the JVM. They are useful when an application handles many concurrent tasks that spend most of their time waiting for I/O, such as HTTP or database calls.

They do not make CPU-intensive work faster and do not increase a database's connection limit or a partner service's capacity. Keep appropriate limits on those resources. Create a virtual thread per task rather than pooling virtual threads.

```java
try (ExecutorService executor =
         Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> result =
        executor.submit(() -> blockingPartnerCall());
    System.out.println(result.get());
}
```

Use a bounded platform-thread pool for CPU-heavy work. Measure the application under realistic load before changing its concurrency model.

## 15. Choosing a concurrency tool

| Need | Typical choice |
|---|---|
| Run one background task | `Runnable` or `Callable` |
| Manage a group of tasks | `ExecutorService` |
| Get a result from a task | `Future` or `CompletableFuture` |
| Protect a simple shared counter | `AtomicInteger` or `LongAdder` |
| Protect a group of related state changes | `synchronized` or `Lock` |
| Share a map among many threads | `ConcurrentHashMap` |
| Limit access to a scarce resource | `Semaphore` |
| Wait for a fixed number of tasks | `CountDownLatch` |
| Coordinate a fixed group in repeated phases | `CyclicBarrier` |
| Pass work between producers and consumers | `BlockingQueue` |
| Handle many I/O-bound tasks | Virtual threads or an appropriately sized I/O executor |
| Split CPU-bound tasks recursively | `ForkJoinPool` |

## 16. Practical rules

- Prefer tasks (`Runnable`/`Callable`) and executors over manually creating many threads.
- Use `start()` to start a thread; do not call `run()` expecting a new thread.
- Share as little mutable state as possible.
- Protect every access to shared mutable state consistently.
- Remember that `volatile` gives visibility, not atomicity for compound operations.
- Use interruption as a cooperative cancellation signal.
- Release locks and permits in `finally` blocks.
- Bound queues and external-resource concurrency where overload is possible.
- Do not use `sleep()` as a synchronization mechanism.
- Do not block the shared common fork-join pool with long-running I/O.
- Test races and shutdown behavior, not just the successful path.
- Profile before tuning thread counts.

## 17. Interview quick review

**Why is `Runnable` usually preferred to extending `Thread`?**  
It separates the task from the thread that runs it, and the class remains free to extend another class.

**What is the difference between `start()` and `run()`?**  
`start()` starts a new thread and then invokes `run()` on it. Calling `run()` directly executes the method on the current thread.

**Does `volatile` make `count++` thread-safe?**  
No. Increment is a read-modify-write operation. Use an atomic class or synchronization.

**What does `synchronized` provide?**  
Mutual exclusion for code guarded by the same monitor and visibility between lock release and a later acquisition of that monitor.

**What is a deadlock?**  
A situation where threads wait indefinitely for locks held by each other.

**When should virtual threads be used?**  
For many concurrent I/O-bound tasks. They do not speed up CPU-bound calculations or increase downstream capacity.

**Why use a bounded executor queue?**  
It limits memory growth and makes overload behavior explicit instead of allowing an unbounded backlog.

## 18. Source code from `DSA-main.zip`

The code below is copied from the Java files in the ZIP archive provided by the user. Each file is shown in full; package declarations and original comments are retained.

### 18.1 `CallableAndFuture.java`

```java
package MultiThreading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallableAndFuture {
    public static void main(String[] args) {

        ExecutorService service = Executors.newSingleThreadExecutor();

        Callable<Long> callable = new FactorailTask(5);
        Future<Long> future = service.submit(callable);

        try {
            long res = future.get();
            System.out.println("fact is " + res);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
        } finally {
            service.shutdown();
        }
    }

}

class FactorailTask implements Callable {
    private int num;

    public FactorailTask(int n) {
        this.num = n;
    }

    public Long call() {
        return calculate(num);
    }

    public long calculate(int n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            return n * calculate(n - 1);
        }
    }
}
```

### 18.2 `CountDownLatchExample.java`

```java
package MultiThreading;

import java.util.concurrent.CountDownLatch;

/**
 * In Java, CountDownLatch is a concurrency utility that allows
 *  one or more threads to wait until a set of operations being performed 
 * in other threads completes. It is part of the java.util.concurrent package. 
 */
public class CountDownLatchExample {
    public static void main(String[] args) {
        int MAX_COUNT = 3;
        CountDownLatch latch = new CountDownLatch(MAX_COUNT);

        for(int i=0 ;i< 10;i++){
            Thread t1 = new Thread(new CountDownLatchExampleNew(i, latch));
            t1.start();
        }

        try{
            System.out.println("waiting for all the thread to finish");
            latch.await();
            System.out.println("all thread execution done!");
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    
}

class CountDownLatchExampleNew implements Runnable{

    private int task;
    private CountDownLatch latch;

    public CountDownLatchExampleNew(int task , CountDownLatch latch){
        this.task = task;
        this.latch = latch;
    }

    public void run(){
        try{
            System.out.println("processing for task " +task);
            Thread.sleep(5000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        latch.countDown();
    }
}
```

### 18.3 `CyclicBarrierExample.java`

```java
package MultiThreading;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * In Java, CyclicBarrier is a synchronization aid that allows a set of threads 
 * to all wait for each other to reach a common barrier point. It is called cyclic
 *  because it can be re-used after the waiting threads are released. CyclicBarrier
 *  is part of the java.util.concurrent package.
 */
public class CyclicBarrierExample {
    public static void main(String[] args) {
        
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3,() ->System.out.println("all thread reached the barries point "));

        for(int i=0;i<5;i++){
            Thread thread = new Thread(new CyclicBarrierExampleNew(i, cyclicBarrier));
            thread.start();
        }
    }
    
}
class CyclicBarrierExampleNew implements Runnable{
    private int task;
    private CyclicBarrier barries;

    public CyclicBarrierExampleNew(int task , CyclicBarrier barrier){
        this.task = task;
        this.barries = barrier;
    }

    public void run(){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Task " +task + " thread name "+ Thread.currentThread().getName());
        try{
            barries.await();
        }catch(InterruptedException | BrokenBarrierException e){
           e.printStackTrace();
        }
        System.out.println("Continue Task " +task + " thread name "+ Thread.currentThread().getName());
    }
}
```

### 18.4 `DeadLockExampleWithFix.java`

```java
package MultiThreading;

import javax.management.ObjectName;

public class DeadLockExampleWithFix {
    private static final Object res1 = new Object();
    private static final Object res2 = new Object();
    public static void main(String[] args) {
        Thread t1 = new Thread(new Task(res1, res2));
        Thread t2 = new Thread(new Task(res2, res1));

        t1.start();
        t2.start();
    }
    
    static class Task implements Runnable{
        private final Object res1;
        private final Object res2;

        // public Task(Object res1 , Object res2){
        //     this.res1 = res1;
        //     this.res2 = res2;
        // }

        //fix
        public Task(Object res1 , Object res2){
            if(System.identityHashCode(res1) < System.identityHashCode(res2)){
                this.res1 = res1;
                this.res2 = res2;
            }
            else{
                this.res1 = res2;
                this.res2 = res1;
            }
        }

        public void run(){
            synchronized(res1){
                System.out.println(Thread.currentThread().getName());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                synchronized(res2){
                    System.out.println(Thread.currentThread().getName());
                }
            }
        }
    }
}
```

### 18.5 `NewThread.java`

```java
package MultiThreading;

public class NewThread extends Thread{
    @Override
    public void run() {
        System.out.println("Thread name -> " + this.getName());
    }    
}
```

### 18.6 `ProducerConsumer.java`

```java
package MultiThreading;

import java.util.LinkedList;

public class ProducerConsumer {
    public static void main(String[] args) {
        Buffer buffer = new Buffer(5);
        Thread pThread = new Thread(new Producer(buffer));
        Thread cThread = new Thread(new Consumer(buffer));

        pThread.start();;
        cThread.start();
    }
    
}

class Buffer {
    private int capacity;
    private LinkedList<Integer> items;

    public Buffer(int capacity){
        this.capacity = capacity;
        items = new LinkedList<>();
    }

    public synchronized void produce(int item) throws InterruptedException {
        while (items.size() == capacity) {
            System.out.println("buffer is full , waiting to be consumed !");
            wait();
        }

        items.add(item);
        System.out.println("produced item ->"+item);
        notify();
    }

    public synchronized void consume() throws InterruptedException{
        while (items.isEmpty()) {
            System.out.println("buffer is empty , waiting to be added something !");
            wait();
        }

        int item = items.remove();
        System.out.println("consumed item ->" + item);
        notify();
    }
}

class Producer implements Runnable{

    private Buffer buffer;
    public Producer(Buffer buffer){
        this.buffer = buffer;
    }
        
    public void run(){
        try {
            for(int i=0;i<10;i++){
                buffer.produce(i);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

class Consumer implements Runnable{

    private Buffer buffer;
    public Consumer(Buffer buffer){
        this.buffer = buffer;
    }

    public void run(){
        try {
            for(int i=0;i<10;i++){
                buffer.consume();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```

### 18.7 `ReentrantLockExample.java`

```java
package MultiThreading;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        Thread t1 = new Thread(new ReentrantLockExampleNew(1));
        Thread t2 = new Thread(new ReentrantLockExampleNew(2));
        Thread t3 = new Thread(new ReentrantLockExampleNew(3));
        
        t1.start();
        t2.start();
        t3.start();
    }
    
    static class ReentrantLockExampleNew implements Runnable{
        private int task;
        public ReentrantLockExampleNew(int task){
            this.task = task;
        }
        public void run(){
            try {
                lock.lock();
                System.out.println("aquring lock for  "+task +" thread name "+Thread.currentThread().getName());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }finally{
                lock.unlock();
                System.out.println("released lock for  "+task +" thread name "+Thread.currentThread().getName());
            }
        }
    }
}
```

### 18.8 `SemaphoneExample.java`

```java
package MultiThreading;

import java.util.concurrent.Semaphore;

/**
 * In Java, a Semaphore is a thread synchronization construct that can be used to
 *  limit the number of threads that have access to a certain resource.
 *  It is part of the java.util.concurrent package.  A Semaphore is initialized with a
 *  number of "permits". Threads can then "acquire" these permits, and if no permits are available,
 *  the acquiring thread will be blocked until a permit becomes available. When a thread is done
 *  with its permit, it can "release" it, making it available for another thread to acquire.
 */
public class SemaphoneExample {

    //here we are proving 3 , as its provides max 3 permits to get the lock
    private static final Semaphore semaphore = new Semaphore(3);

    public static void main(String[] args) {
        Thread t1 = new Thread(new SemaphoneExampleNew(1));
        Thread t2 = new Thread(new SemaphoneExampleNew(2));
        Thread t3 = new Thread(new SemaphoneExampleNew(3));

        t1.start();
        t2.start();
        t3.start();
    }

    static class SemaphoneExampleNew implements Runnable {
        private int task;

        public SemaphoneExampleNew(int task) {
            this.task = task;
        }

        public void run() {
            try {

                semaphore.acquire();
                System.out.println("getting permit for " + task + " for thread " + Thread.currentThread().getName());
                Thread.sleep(1000);
                System.out.println("got permit for " + task + " for thread " + Thread.currentThread().getName());
                semaphore.release();
                System.out.println("released permit for " + task + " for thread " + Thread.currentThread().getName());

            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}
```

### 18.9 `SimpleThread.java`

```java
package MultiThreading;

public class SimpleThread {
    public static void main(String[] args) {
        Mythread tMythread = new Mythread();
        Thread thread = new Thread(tMythread);
        thread.start();
    }
    
}

class Mythread implements Runnable{
    public void run(){
        for(int i=0;i<10;i++){
            System.out.println("i ->"+i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}
```

### 18.10 `SyncThread.java`

```java
package MultiThreading;

public class SyncThread {

    public static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        SyncThreadNew t1 = new SyncThreadNew(counter);
        SyncThreadNew t2 = new SyncThreadNew(counter);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("final value ->" + counter.getCounter());
    }

}

class Counter {
    int count;

    public synchronized void increment() {
        count++;
    }

    public int getCounter() {
        return count;
    }

}

class SyncThreadNew extends Thread {

    private Counter counter;

    public SyncThreadNew(Counter counter) {
        this.counter = counter;
    }

    public void run() {

        for (int i = 0; i < 11; i++) {
            counter.increment();
        }

    }
}
```

### 18.11 `ThreadCommunicaton.java`

```java
package MultiThreading;

public class ThreadCommunicaton {
    private static final Object lock = new Object();
    private static int counter = 1;
    private static final int MAX_COUNT = 10;
    public static void main(String[] args) {

        Thread t1 = new Thread(new EvenPrint());
        Thread t2 = new Thread(new OddPrint());

        t1.start();
        t2.start();
    }

    static class OddPrint implements Runnable{
        public void run(){
            while (counter <= MAX_COUNT) {
                synchronized(lock){
                    if(counter % 2 != 0){
                        System.out.println("odd counter : "+counter);
                        counter ++;
                        lock.notify();
                    }
                    else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
    }

    static class EvenPrint implements Runnable{
        public void run(){
            while (counter <= MAX_COUNT) {
                synchronized(lock){
                    if(counter % 2 ==0){
                        System.out.println("even counter : "+counter);
                        counter++;
                        lock.notify();
                    }else{
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
    }
    
}
```

### 18.12 `ThreadExample.java`

```java
package MultiThreading;

public class ThreadExample {
    public static void main(String[] args) {
        System.out.println("er");
        new Thread(() -> System.out.println("We are in ->" +Thread.currentThread().getName())).start();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Thread name " + Thread.currentThread().getName());
            }
            
        });

        thread.setName("jagat");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

        NewThread thread2 = new NewThread();
        thread2.start();
    }
    
}
```

### 18.13 `ThreadGroupExample.java`

```java
package MultiThreading;

public class ThreadGroupExample {
    public static void main(String[] args) {
        // Create a new thread group
        ThreadGroup group = new ThreadGroup("My Thread Group");

        // Create new threads in the group
        Thread thread1 = new Thread(group, () -> {
            System.out.println("Thread 1 is running");
        });

        Thread thread2 = new Thread(group, () -> {
            System.out.println("Thread 2 is running");
        });

        // Start the threads
        thread1.start();
        thread2.start();

        // Display the number of active threads in the group
        System.out.println("Active threads in group: " + group.activeCount());
    }
}
```

### 18.14 `ThreadInterupption.java`

```java
package MultiThreading;

public class ThreadInterupption {
    public static void main(String[] args) throws InterruptedException {
        Thread t1Thread = new Thread(new MyThreadInterruputed());
        t1Thread.start();

        Thread.sleep(2000);
        t1Thread.interrupt();
    }

    
}

class MyThreadInterruputed implements Runnable{
    public void run(){
        try {
            for(int i=0;i<10;i++){
                Thread.sleep(1000);

                if(Thread.interrupted()){
                    System.out.println("thread interruppte ");
                    return;
                }
            }
        } catch (InterruptedException  e) {
            System.out.println("Thread interrupted while sleeping");
        }
    }
}
```

### 18.15 `ThreadPoolImplementation.java`

```java
package MultiThreading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ThreadPoolImplementation {
    public static void main(String[] args) {
        CustomThreadPool customThreadPool = new CustomThreadPool(4);
        for(int i=0;i<10;i++){
            final int taskId  = i;
            customThreadPool.submitTask(() -> System.out.println("Task : "+taskId));
        }

        customThreadPool.shutDown();
    }

}

class CustomThreadPool {
    private int poolsize;
    private List<WorkerThread> WorkerThreadList;
    private BlockingQueue<Runnable> blockingQueue;

    public CustomThreadPool(int poolsize) {
        this.poolsize = poolsize;
        WorkerThreadList = new ArrayList<>();
        blockingQueue = new LinkedBlockingDeque<>();

        for (int i = 0; i < poolsize; i++) {
            WorkerThread workerThread = new WorkerThread();
            WorkerThreadList.add(workerThread);
            workerThread.start();
        }
    }

    public void submitTask(Runnable task) {
        try {
            blockingQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutDown() {
        for (WorkerThread workerThread : WorkerThreadList) {
            workerThread.interrupt();
        }
    }

    class WorkerThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Runnable task = blockingQueue.take();
                    task.run();
                    System.out.println(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
```

### 18.16 `ThreadPriority.java`

```java
package MultiThreading;

public class ThreadPriority {
    public static void main(String[] args) {
        Thread t1 = new Thread(new ThreadPriorityExample());
        Thread t2 = new Thread(new ThreadPriorityExample());
        Thread t3 = new Thread(new ThreadPriorityExample());

        t1.setPriority(Thread.NORM_PRIORITY);
        t2.setPriority(Thread.MIN_PRIORITY);
        t3.setPriority(Thread.MAX_PRIORITY);

        t1.start();
        t2.start();
        t3.start();
    }
    
}

class ThreadPriorityExample implements Runnable{
    public void run(){
        for(int i=0;i<5;i++){
            try {
                System.out.println(Thread.currentThread().getName() + " i : " +i);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}
```

---

## 19. Multithreading & Concurrency Interview Questions & Answers

### Q1. What is the difference between a Process and a Thread, and between Concurrency and Parallelism?

**Answer:**
- **Process vs Thread:** A process is an independent OS execution unit with its own isolated virtual address space, file descriptors, and heap. A thread is a lightweight execution path within a process. Threads in the same JVM share the heap and metaspace, while each thread has its own call stack, program counter, and thread-local storage. Context switching between threads is much cheaper than between processes.
- **Concurrency vs Parallelism:**
  - **Concurrency** is about *dealing* with multiple tasks at once (overlapping lifetimes, interleaved execution via time-slicing even on a single CPU core).
  - **Parallelism** is about *doing* multiple tasks at the exact same instant across multiple physical CPU cores.

### Q2. What are all 6 states of a Java `Thread` (`Thread.State`), and how does a thread transition between them?

**Answer:**
1. **`NEW`:** `new Thread(...)` created, `start()` not yet called.
2. **`RUNNABLE`:** `start()` called; the thread is either executing on a CPU core or ready in the OS run queue waiting for CPU time (also includes blocking on traditional socket I/O).
3. **`BLOCKED`:** waiting to acquire an intrinsic monitor lock (`synchronized` block/method) held by another thread.
4. **`WAITING`:** waiting indefinitely for another thread's signal (`Object.wait()`, `Thread.join()`, `LockSupport.park()`).
5. **`TIMED_WAITING`:** waiting with a timeout (`Thread.sleep(ms)`, `wait(ms)`, `join(ms)`, `LockSupport.parkNanos()`).
6. **`TERMINATED`:** `run()` completed normally or threw an uncaught exception. Once terminated, calling `start()` again throws `IllegalThreadStateException`.

### Q3. Why are `wait()`, `notify()`, and `notifyAll()` defined in `Object` instead of `Thread`, and why must `wait()` be called inside a `while` loop?

**Answer:**
- **Why on `Object`:** In Java, every object has an intrinsic monitor (lock) in its object header, and threads wait on a specific **monitor** (condition queue), not on a thread. Because any object can act as a lock in `synchronized (obj)`, the wait/notify mechanism belongs on `Object`.
- **Why inside a `while` loop (never an `if`):**
  1. **Spurious wakeups:** the OS/JVM is permitted to wake a waiting thread even if no thread called `notify()`.
  2. **Stolen condition:** between the time `notifyAll()` wakes Thread A and the moment Thread A re-acquires the monitor lock, another Thread B may have acquired the lock and consumed the condition (e.g., emptied the queue).

```java
synchronized (lock) {
    while (queue.isEmpty()) {     // ALWAYS re-check condition in a while loop
        lock.wait();
    }
    return queue.remove();
}
```

### Q4. What is the difference between `Thread.sleep()` and `Object.wait()`, and between `Thread.yield()` and `Thread.join()`?

**Answer:**

| Method | Releases Monitor Lock? | Where Called | Wake-up Condition |
|---|---|---|---|
| `Thread.sleep(ms)` | **No** (keeps all locks it holds!) | Anywhere | Time expires or `interrupt()` |
| `obj.wait()` | **Yes** (atomically releases `obj`'s lock and suspends) | Must hold `synchronized(obj)` (`IllegalMonitorStateException` otherwise) | `notify()`/`notifyAll()` + re-acquiring lock, or `interrupt()` |
| `Thread.yield()` | No | Anywhere | Hint to scheduler that thread is willing to yield CPU; OS may ignore |
| `t.join()` | No | Anywhere | Waits until thread `t` finishes (`TERMINATED`) |

### Q5. What is a Daemon thread vs a User thread? What happens to `finally` blocks in Daemon threads?

**Answer:**
- **User thread (default):** keeps the JVM alive. The JVM waits for all user threads to finish before exiting.
- **Daemon thread (`setDaemon(true)` before `start()`):** background support thread (e.g., GC threads, `ForkJoinPool.commonPool()` workers, timer cleanups). When the last user thread finishes, the JVM **exits immediately** — daemon threads are abruptly halted and their `finally` blocks or stacks are **not guaranteed to execute**. Never use daemon threads for unbuffered file/DB writes without a shutdown hook or explicit pool shutdown.

### Q6. Explain the Java Memory Model (JMM) and the `happens-before` guarantee.

**Answer:** Modern CPUs use per-core registers, L1/L2 caches, and store buffers, and both the JIT compiler and CPU reorder instructions for performance. Without synchronization, a write by Thread A may stay in Core 1's store buffer and remain invisible to Thread B on Core 2, or appear out of order.

The **Java Memory Model (JMM)** defines when writes by one thread are guaranteed to be visible to reads by another thread via **`happens-before` edges**:
1. **Program order rule:** each action in a single thread happens-before every subsequent action in that thread.
2. **Monitor lock rule:** unlocking a monitor (`synchronized` / `Lock.unlock()`) happens-before every subsequent lock of that same monitor.
3. **Volatile variable rule:** a write to a `volatile` field happens-before every subsequent read of that same `volatile` field.
4. **Thread start rule:** `thread.start()` happens-before any action in the started thread.
5. **Thread termination rule:** all actions in a thread happen-before another thread returns from `thread.join()`.
6. **Transitivity:** if $A \to B$ and $B \to C$, then $A \to C$ (this allows "piggybacking" non-volatile writes before a volatile write).

### Q7. How does `volatile` work under the hood, and when is it NOT enough?

**Answer:**
- **Visibility:** reading a `volatile` variable invalidates local CPU cache lines so the thread reads the latest value from shared cache/main memory; writing flushes store buffers immediately.
- **Ordering (Memory Barriers / Fences):** the JVM inserts LoadLoad/LoadStore/StoreStore/StoreLoad barriers preventing instruction reordering across the volatile read/write (e.g., preventing a partially constructed object from being published).
- **When it is NOT enough:** `volatile` does **not** make compound operations atomic (`count++`, `if (!initialized) initialize()`). If the new value depends on the previous value or multiple variables must change together as an invariant, use `Atomic*`, `LongAdder`, `synchronized`, or `Lock`.

### Q8. How do you implement a thread-safe lazy Singleton (Double-Checked Locking vs Enum vs Holder pattern)?

**Answer:** Without `volatile` on `instance`, Double-Checked Locking is broken because the JVM can reorder object allocation, constructor execution, and reference assignment (`instance = new Singleton()`), letting another thread observe a non-null reference to an **incompletely initialized** object.

```java
// 1. Double-Checked Locking (volatile is mandatory!)
public final class ConfigManager {
    private static volatile ConfigManager instance;
    private ConfigManager() {}

    public static ConfigManager getInstance() {
        ConfigManager result = instance;          // single volatile read on fast path
        if (result == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = result = new ConfigManager();
                }
            }
        }
        return result;
    }
}

// 2. Initialization-on-demand Holder idiom (lazy, lock-free, uses classloader guarantee)
public final class ServiceRegistry {
    private ServiceRegistry() {}
    private static class Holder {
        static final ServiceRegistry INSTANCE = new ServiceRegistry();
    }
    public static ServiceRegistry getInstance() { return Holder.INSTANCE; }
}
```

### Q9. What is the difference between Object-level locking (`synchronized(this)`) and Class-level locking (`synchronized(MyClass.class)`)?

**Answer:**
- **Object-level lock (`synchronized` instance method or `synchronized(this)`):** locks the monitor of that specific object instance. Two threads calling the method on **two different instances** run concurrently without blocking each other.
- **Class-level lock (`static synchronized` method or `synchronized(MyClass.class)`):** locks the `Class<MyClass>` object monitor. Only one thread across the entire JVM (per classloader) can execute static synchronized blocks of that class at a time.
- **Best practice:** lock on a `private final Object lock = new Object();` field rather than `this` or `MyClass.class` so external code cannot accidentally acquire the same monitor and cause deadlocks.

### Q10. How does JVM Intrinsic Lock (`synchronized`) optimization work (Biased, Lightweight, Heavyweight locks)?

**Answer:** Acquiring an OS mutex requires an expensive user-to-kernel mode switch. HotSpot optimizes `synchronized` using the object header's **Mark Word**:
1. **Lock Elision & Coarsening (JIT):** Escape analysis removes locks on objects that never escape a single thread; adjacent lock blocks on the same monitor are merged.
2. **Lightweight (Thin) Locking:** when a second thread accesses the lock without active contention, the JVM uses a fast user-space CAS operation to swap the Mark Word with a Lock Record on the thread's stack (spinning briefly via **Adaptive Spinning** if needed).
3. **Heavyweight (Inflated) Locking:** under real contention, the lock inflates into an OS-backed `ObjectMonitor`, parking waiting threads via the kernel scheduler (`BLOCKED` state). *(Note: Biased locking was deprecated and removed in Java 15+ due to high revocation cost on modern multi-core apps.)*

### Q11. Compare `synchronized` vs `ReentrantLock`. When should you choose `ReentrantLock`?

**Answer:**

| Feature | `synchronized` | `ReentrantLock` |
|---|---|---|
| Lock release | Automatic on block exit / exception | Manual (`try { ... } finally { lock.unlock(); }`) |
| Timed acquisition | No (waits forever) | Yes (`tryLock(100, TimeUnit.MILLISECONDS)`) |
| Non-blocking poll | No | Yes (`tryLock()`) |
| Interruptible wait | No (cannot interrupt a `BLOCKED` thread) | Yes (`lockInterruptibly()`) |
| Fairness policy | Always non-fair (barging allowed) | Configurable (`new ReentrantLock(true)` FIFO queue) |
| Multiple wait sets | Only one (`obj.wait()`) | Multiple `Condition` objects (`notEmpty`, `notFull`) |

Use `synchronized` by default for simplicity and readable thread dumps; use `ReentrantLock` when you need `tryLock`, timeouts, interruptible waiting, fairness, or multiple `Condition` queues.

### Q12. What is a `ReadWriteLock` vs `StampedLock`, and what is the Optimistic Read pattern?

**Answer:**
- **`ReentrantReadWriteLock`:** allows multiple concurrent readers *or* a single exclusive writer. Can suffer from **writer starvation** under heavy read load and has high CAS overhead on reader count updates across many cores.
- **`StampedLock` (Java 8+):** adds **lock-free Optimistic Reading**. Readers do not acquire a lock or modify shared state at all; they read a version `stamp`, read the fields into local variables, and call `validate(stamp)` to check if a writer intervened. If a writer ran, the reader falls back to a regular read lock.
- **Gotcha:** `StampedLock` is **not reentrant** (acquiring it twice on the same thread deadlocks!) and does not support `Condition` variables.

```java
class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double dx, double dy) {
        long stamp = sl.writeLock();
        try { x += dx; y += dy; }
        finally { sl.unlockWrite(stamp); }
    }

    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();      // no CAS, just reads stamp
        double curX = x, curY = y;
        if (!sl.validate(stamp)) {                // did a write occur meanwhile?
            stamp = sl.readLock();
            try { curX = x; curY = y; }
            finally { sl.unlockRead(stamp); }
        }
        return Math.hypot(curX, curY);
    }
}
```

### Q13. How does Compare-And-Swap (CAS) work, what is the ABA problem, and how does `AtomicStampedReference` solve it?

**Answer:**
- **CAS:** a single hardware CPU instruction (`CMPXCHG` on x86) that takes `(memoryLocation, expectedOldValue, newValue)` and atomically updates the value only if it still equals `expectedOldValue`, returning whether it succeeded. `AtomicInteger` loops with CAS (or `LOCK XADD`) without suspending threads.
- **ABA Problem:** Thread 1 reads value `A`, gets preempted. Thread 2 changes `A -> B -> A` (e.g., pops node `A` from a lock-free stack, frees/modifies it, and pushes `A` back). Thread 1 resumes, sees `A`, and its CAS succeeds even though the underlying data structure changed in a dangerous way.
- **Solution:** `AtomicStampedReference<V>` pairs the reference with an integer **version stamp** (`(ref, stamp)`), incremented on every update (`(A,1) -> (B,2) -> (A,3)`), so Thread 1's CAS expecting `(A,1)` fails.

### Q14. What is `AbstractQueuedSynchronizer` (AQS), and which JDK classes are built on it?

**Answer:** `AbstractQueuedSynchronizer` is the foundational framework behind `ReentrantLock`, `Semaphore`, `CountDownLatch`, `ReentrantReadWriteLock`, and `FutureTask`. It combines:
1. A single `volatile int state` (or `long` in `AbstractQueuedLongSynchronizer`) representing lock count, available permits, or latch count, updated via CAS.
2. A **CLH (Craig, Landin, and Hagersten) doubly-linked FIFO wait queue** of blocked threads parked via `LockSupport.park()`.
3. Template methods (`tryAcquire`, `tryRelease` for exclusive mode; `tryAcquireShared`, `tryReleaseShared` for shared mode) implemented by subclasses.

### Q15. Explain the internal workings of `ThreadPoolExecutor` when a task is submitted.

**Answer:** A `ThreadPoolExecutor` is governed by `corePoolSize`, `maximumPoolSize`, `keepAliveTime`, `workQueue`, `threadFactory`, and `RejectedExecutionHandler`.

**Exact order when `execute(task)` is called:**
1. If running threads `< corePoolSize`, create a **new core thread** immediately (even if existing core threads are idle).
2. Otherwise, try to **enqueue** the task into `workQueue` (`workQueue.offer(task)`).
3. If the queue is **full** and running threads `< maximumPoolSize`, create a **new non-core thread**.
4. If the queue is **full** and running threads `== maximumPoolSize`, invoke the **`RejectedExecutionHandler`**.

**Common trap:** If you use an **unbounded** `LinkedBlockingQueue` (default in `Executors.newFixedThreadPool`), step 2 never fails, so `maximumPoolSize` is completely ignored and the queue can grow until `OutOfMemoryError`.

### Q16. What are the 4 standard `RejectedExecutionHandler` policies in `ThreadPoolExecutor`?

**Answer:**
1. **`AbortPolicy` (default):** throws `RejectedExecutionException` immediately so the caller knows the pool is saturated.
2. **`CallerRunsPolicy`:** executes the task directly on the **calling thread** that submitted it. This provides natural **backpressure** by slowing down the producer thread, though be careful not to block critical event-loop or I/O selector threads.
3. **`DiscardPolicy`:** silently drops the new task without throwing an exception.
4. **`DiscardOldestPolicy`:** drops the oldest task at the head of the queue and retries submitting the new task.

### Q17. Why do production guidelines (e.g., Alibaba / Spring best practices) discourage `Executors.newFixedThreadPool`, `newCachedThreadPool`, and `newScheduledThreadPool`?

**Answer:**
- `newFixedThreadPool` and `newSingleThreadExecutor` use an unbounded `LinkedBlockingQueue(Integer.MAX_VALUE)` -> **OOM under traffic spikes**.
- `newCachedThreadPool` and `newScheduledThreadPool` allow `maximumPoolSize = Integer.MAX_VALUE` -> **creates thousands of OS threads under spike, exhausting native memory (`OutOfMemoryError: unable to create native thread`) and thrashing CPU**.
- Instead, construct `ThreadPoolExecutor` (or Spring's `ThreadPoolTaskExecutor`) explicitly with a **bounded queue**, sensible `core`/`max` sizes, custom named threads (`ThreadFactory`), and an explicit rejection policy.

### Q18. How do you size a thread pool for CPU-bound vs I/O-bound workloads?

**Answer:**
- **CPU-bound tasks** (compression, hashing, complex math):
  $$N_{\text{threads}} = N_{\text{cores}} \text{ (or } N_{\text{cores}} + 1 \text{)}$$
  More threads only increase context-switching overhead and cache misses.
- **Blocking I/O-bound tasks on platform threads** (JDBC, REST calls):
  $$N_{\text{threads}} = N_{\text{cores}} \times U_{\text{cpu}} \times \left(1 + \frac{\text{Wait Time } (W)}{\text{Compute Time } (C)}\right)$$
  For example, on 8 cores if a task spends 90 ms waiting on DB/HTTP and 10 ms on CPU ($W/C = 9$), $8 \times (1 + 9) = 80$ threads. Always cap this against downstream limits (e.g., HikariCP DB connection pool size — having 200 threads waiting on 20 DB connections only shifts queueing to HikariCP).
- **Java 21+ with Virtual Threads:** use `Executors.newVirtualThreadPerTaskExecutor()` for I/O-bound tasks and limit concurrency to downstream systems using a `Semaphore` or connection pool instead of sizing a thread pool.

### Q19. What is the difference between `ExecutorService.submit()` and `execute()`, and how are uncaught exceptions handled in each?

**Answer:**
- `execute(Runnable)` returns `void`. If the task throws an uncaught runtime exception, the worker thread terminates, the `UncaughtExceptionHandler` is invoked (printing stack trace to `stderr` by default), and the pool spawns a replacement thread.
- `submit(Runnable/Callable)` returns a `Future<?>`. Any exception thrown inside the task is **caught and stored inside the `Future` object**. It is **completely silent** unless you call `future.get()` / `exceptionally()` (or override `ThreadPoolExecutor.afterExecute`).

### Q20. How do you shut down an `ExecutorService` gracefully?

**Answer:**
- `shutdown()`: stops accepting new tasks, lets previously submitted tasks (running and queued) finish.
- `shutdownNow()`: stops accepting new tasks, returns queued tasks that never started, and sends `interrupt()` to currently running worker threads.

```java
void shutdownGracefully(ExecutorService pool) {
    pool.shutdown();
    try {
        if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            List<Runnable> dropped = pool.shutdownNow();
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("Pool did not terminate");
            }
        }
    } catch (InterruptedException e) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();       // preserve interrupt status!
    }
}
```

### Q21. Deep-dive into `CompletableFuture`: `thenApply` vs `thenCompose` vs `thenCombine`, and `*Async` thread pool traps.

**Answer:**
- **`thenApply(fn)`:** synchronous map (`T -> U`), like `Stream.map`.
- **`thenCompose(fn)`:** flatMap for futures (`T -> CompletableFuture<U>`); chains two dependent async calls without nesting `CompletableFuture<CompletableFuture<U>>`.
- **`thenCombine(otherFuture, biFn)`:** runs two independent futures in parallel and combines their results when both finish.
- **Exception handling:** `exceptionally(ex -> fallback)`, `handle((res, ex) -> ...)`, `whenComplete((res, ex) -> log)`.
- **Timeouts (Java 9+):** `orTimeout(2, TimeUnit.SECONDS)`, `completeOnTimeout(defaultVal, 2, TimeUnit.SECONDS)`.
- **Thread pool trap:**
  - Non-`Async` methods (`thenApply`) run on whichever thread completes the previous stage *or* on the caller thread if already complete.
  - `*Async` methods without an executor argument (`supplyAsync(fn)`, `thenApplyAsync(fn)`) run on **`ForkJoinPool.commonPool()`**, which has only `N_cores - 1` threads! Always pass a dedicated `Executor` for blocking I/O work.

```java
ExecutorService ioPool = Executors.newVirtualThreadPerTaskExecutor();

CompletableFuture<User> userF = CompletableFuture.supplyAsync(() -> fetchUser(id), ioPool);
CompletableFuture<Credit> creditF = CompletableFuture.supplyAsync(() -> fetchCredit(id), ioPool);

CompletableFuture<OrderResult> pipeline = userF
        .thenCombineAsync(creditF, (user, credit) -> validate(user, credit), ioPool)
        .thenComposeAsync(valid -> placeOrderAsync(valid), ioPool)
        .orTimeout(3, TimeUnit.SECONDS)
        .exceptionally(ex -> OrderResult.failed(ex.getMessage()));
```

### Q22. How does `ThreadLocal` work internally, why does it cause memory leaks in thread pools, and how does `ScopedValue` (Java 21+) improve on it?

**Answer:**
- **Internals:** Every `Thread` instance has a field `ThreadLocal.ThreadLocalMap threadLocals`. The map's keys are **`WeakReference<ThreadLocal<?>>`** and the values are **strong references** to the stored objects.
- **Why memory leaks happen:** When a `ThreadLocal` instance is garbage-collected, the key in `ThreadLocalMap` becomes `null`, but the **value remains strongly referenced** by the `Entry` inside the `Thread`! Because worker threads in a thread pool (like Tomcat's request pool) live for the lifetime of the application, those values are never freed unless `threadLocal.remove()` is called, or can leak data across requests (tenant/user mix-up!).
- **Golden rule:** Always call `threadLocal.remove()` in a `finally` block.
- **`InheritableThreadLocal`:** copies values to child threads at `new Thread()` creation time, which **does not work** when submitting tasks to an existing thread pool (use Micrometer Context Propagation or `TransmittableThreadLocal`).
- **`ScopedValue` (Java 21+):** immutable, bounded to a lexical scope (`ScopedValue.where(USER, user).run(() -> ...)`), automatically cleaned up when the scope exits, and efficiently shared with child virtual threads in `StructuredTaskScope` without copying.

### Q23. Compare `BlockingQueue` implementations (`ArrayBlockingQueue`, `LinkedBlockingQueue`, `SynchronousQueue`, `PriorityBlockingQueue`, `DelayQueue`).

**Answer:**

| Implementation | Backing Structure | Bounded? | Locking | Typical Use Case |
|---|---|---|---|---|
| `ArrayBlockingQueue` | Circular array | **Always bounded** (fixed capacity) | Single `ReentrantLock` for both put & take | Bounded producer-consumer with predictable memory |
| `LinkedBlockingQueue` | Linked nodes | Optional (defaults to `Integer.MAX_VALUE` — **always pass capacity!**) | **Two locks** (`putLock` and `takeLock`) -> higher throughput under contention | `ThreadPoolExecutor` work queue |
| `SynchronousQueue` | None (0 capacity) | 0 elements | Lock-free CAS transfer stack/queue | Direct handoff (`newCachedThreadPool`); `put` blocks until a thread calls `take` |
| `PriorityBlockingQueue` | Binary min-heap | Unbounded (grows dynamically) | Single `ReentrantLock` | Priority task scheduling |
| `DelayQueue` | `PriorityQueue` of `Delayed` items | Unbounded | Single `ReentrantLock` + `Condition` | Scheduled tasks, TTL cache eviction, retry backoff |

### Q24. What are the 4 Coffman conditions for Deadlock, and how do you prevent, detect, and recover from a Deadlock?

**Answer:**
**Four necessary conditions (all 4 must hold simultaneously):**
1. **Mutual Exclusion:** resources cannot be shared concurrently.
2. **Hold and Wait:** a thread holding one lock waits to acquire another lock.
3. **No Preemption:** a lock can only be released voluntarily by the thread holding it.
4. **Circular Wait:** Thread A holds Lock 1 and waits for Lock 2; Thread B holds Lock 2 and waits for Lock 1.

**How to prevent (break at least one condition):**
- **Break Circular Wait (most common):** enforce a **strict global lock acquisition order** (e.g., always lock accounts in ascending order of `accountId`, or `System.identityHashCode`).
- **Break Hold and Wait / No Preemption:** use `ReentrantLock.tryLock(timeout)` and release held locks if the second lock cannot be acquired.
- Avoid calling external/alien methods while holding a lock (**open calls**).

**How to detect:**
- Take a thread dump (`jcmd <pid> Thread.print` or `jstack <pid>`) — JVM automatically prints `Found one Java-level deadlock:` at the bottom.
- Programmatically via `ManagementFactory.getThreadMXBean().findDeadlockedThreads()`.

```java
// Deadlock-free transfer between arbitrary accounts using canonical lock ordering
void transfer(Account from, Account to, BigDecimal amount) {
    Account first  = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

### Q25. How do Virtual Threads (Java 21) work under the hood (Continuations, Carrier Threads, and Pinning)?

**Answer:**
- **Platform threads** are 1:1 wrappers around OS kernel threads (~1 MB stack, expensive context switches).
- **Virtual threads** ($M:N$ scheduling) are lightweight JVM-managed threads (stacks start at a few hundred bytes on the heap and grow/shrink dynamically).
- **How they run:** The JVM mounts a virtual thread onto a **carrier thread** (an OS thread from a work-stealing `ForkJoinPool` sized to CPU cores). When the virtual thread performs a blocking operation (socket I/O, `BlockingQueue.take()`, `Lock.lock()`, `Thread.sleep()`), the JVM **unmounts** the virtual thread — saving its stack frames as a **Continuation** on the heap — freeing the carrier thread to run another virtual thread. When I/O completes, the continuation is scheduled back onto a carrier thread.
- **What is Thread Pinning?** A virtual thread is **pinned** to its carrier thread (cannot unmount during blocking) in two situations:
  1. Inside a `synchronized` block or method (in Java 21–23; fixed in Java 24 JEP 491!).
  2. When calling a `native` method or Foreign Function (JNI/FFM).
  - **Fix in Java 21:** replace `synchronized` around blocking I/O (like JDBC drivers or caches) with `ReentrantLock`, and detect pinning with `-Djdk.tracePinnedThreads=short` or JFR (`jdk.VirtualThreadPinned` event).

---

### Coding & Classic Concurrency Problems (Q26 to Q35)

### Q26. Coding: Print odd and even numbers alternatively from 1 to 20 using two threads (`wait`/`notify`).

```java
public class OddEvenPrinter {
    private int number = 1;
    private final int max = 20;
    private final Object lock = new Object();

    public void printOdd() {
        synchronized (lock) {
            while (number <= max) {
                while (number % 2 == 0) {
                    try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                if (number <= max) {
                    System.out.println(Thread.currentThread().getName() + " -> " + number++);
                    lock.notifyAll();
                }
            }
        }
    }

    public void printEven() {
        synchronized (lock) {
            while (number <= max) {
                while (number % 2 != 0) {
                    try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                if (number <= max) {
                    System.out.println(Thread.currentThread().getName() + " -> " + number++);
                    lock.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        OddEvenPrinter printer = new OddEvenPrinter();
        new Thread(printer::printOdd,  "Odd-Thread").start();
        new Thread(printer::printEven, "Even-Thread").start();
    }
}
```

### Q27. Coding: Print `A B C A B C ...` in strict sequence across 3 threads (`T1`, `T2`, `T3`).

```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreeThreadSequence {
    private int turn = 0; // 0 -> A, 1 -> B, 2 -> C
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void print(int myTurn, char letter, int times) {
        for (int i = 0; i < times; i++) {
            lock.lock();
            try {
                while (turn != myTurn) {
                    condition.await();
                }
                System.out.print(letter + " ");
                turn = (turn + 1) % 3;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        ThreeThreadSequence seq = new ThreeThreadSequence();
        new Thread(() -> seq.print(0, 'A', 5)).start();
        new Thread(() -> seq.print(1, 'B', 5)).start();
        new Thread(() -> seq.print(2, 'C', 5)).start();
    }
}
```

### Q28. Coding: Implement a custom bounded `BlockingQueue` from scratch using `ReentrantLock` and two `Condition` variables.

**Answer:** Using two separate conditions (`notFull` and `notEmpty`) avoids waking up producers when another producer is waiting, which is much more efficient than a single monitor with `notifyAll()`.

```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueue<T> {
    private final Object[] items;
    private int head, tail, count;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull  = lock.newCondition();

    public BoundedBlockingQueue(int capacity) {
        this.items = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                notFull.await();                  // wait until space is available
            }
            items[tail] = item;
            tail = (tail + 1) % items.length;
            count++;
            notEmpty.signal();                    // wake one waiting consumer
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();                 // wait until an item is available
            }
            T item = (T) items[head];
            items[head] = null;                   // avoid memory leak
            head = (head + 1) % items.length;
            count--;
            notFull.signal();                     // wake one waiting producer
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

### Q29. Coding: Write a program that intentionally creates a Deadlock, then show how to fix it with `tryLock`.

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockAndTryLockFix {
    private static final Lock lockA = new ReentrantLock();
    private static final Lock lockB = new ReentrantLock();

    // Safe acquisition of two locks without deadlock
    public static void executeSafely(Lock first, Lock second, String taskName) {
        while (true) {
            try {
                if (first.tryLock(50, TimeUnit.MILLISECONDS)) {
                    try {
                        if (second.tryLock(50, TimeUnit.MILLISECONDS)) {
                            try {
                                System.out.println(taskName + " acquired both locks!");
                                return;
                            } finally {
                                second.unlock();
                            }
                        }
                    } finally {
                        first.unlock();
                    }
                }
                // Back off briefly with jitter to avoid livelock
                Thread.sleep((long) (Math.random() * 20));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public static void main(String[] args) {
        new Thread(() -> executeSafely(lockA, lockB, "Worker-1")).start();
        new Thread(() -> executeSafely(lockB, lockA, "Worker-2")).start();
    }
}
```

### Q30. Coding: Implement a thread-safe Token Bucket / Sliding Window Rate Limiter.

```java
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter {
    private final long capacity;
    private final long refillTokensPerSecond;
    private final AtomicLong availableTokens;
    private volatile long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillNanos = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (availableTokens.get() > 0) {
            availableTokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillNanos;
        long newTokens = (elapsedNanos * refillTokensPerSecond) / 1_000_000_000L;
        if (newTokens > 0) {
            long updated = Math.min(capacity, availableTokens.get() + newTokens);
            availableTokens.set(updated);
            lastRefillNanos = now;
        }
    }
}
```

### Q31. Coding: Implement a thread-safe In-Memory Cache that prevents Cache Stampede (computing each missing key only once).

**Answer:** Calling `ConcurrentHashMap.computeIfAbsent` directly with a slow network call blocks the hash bin. Instead, store a `CompletableFuture<V>` in the map so only the first thread triggers the computation while concurrent callers for the same key await the same future outside the bin lock; remove the future if computation fails.

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MemoizerCache<K, V> {
    private final ConcurrentHashMap<K, CompletableFuture<V>> cache = new ConcurrentHashMap<>();

    public V get(K key, Function<K, V> loader) {
        CompletableFuture<V> future = cache.get(key);
        if (future == null) {
            CompletableFuture<V> created = new CompletableFuture<>();
            future = cache.putIfAbsent(key, created);
            if (future == null) {
                future = created;
                try {
                    created.complete(loader.apply(key));
                } catch (Throwable ex) {
                    cache.remove(key, created);           // evict failed computation so retries work
                    created.completeExceptionally(ex);
                }
            }
        }
        return future.join();
    }
}
```

### Q32. How do you solve the Dining Philosophers problem without Deadlock or Starvation?

**Answer:** Five philosophers sit around a table with 5 forks between them; each needs both left and right forks to eat. If every philosopher picks up their left fork first, all 5 wait forever for the right fork (**circular wait deadlock**).

**Three standard solutions:**
1. **Resource Hierarchy (Lock Ordering):** number forks `0..4`. Every philosopher always picks up the **lower-numbered fork first**, then the higher-numbered fork. Philosopher 4 reaches for fork 0 before fork 4, breaking the cycle.
2. **Arbitrator / Semaphore (`Semaphore(4)`):** allow at most $N - 1 = 4$ philosophers to attempt picking up forks simultaneously; by Pigeonhole Principle, at least one philosopher is guaranteed to get both forks.
3. **Chandy/Misra solution** or `ReentrantLock(true)` (fair locks) with `tryLock` + random backoff.

### Q33. What is the difference between `Exchanger` and `SynchronousQueue`?

**Answer:**
- **`Exchanger<V>`:** a two-thread synchronization point where two threads meet and **swap** objects with each other (`V other = exchanger.exchange(myBuffer)`). Ideal when a reader thread fills a buffer and swaps it with an empty buffer from a processor thread (zero allocation after warmup).
- **`SynchronousQueue<E>`:** a one-way handoff from any producer thread to any waiting consumer thread.

### Q34. How do you test concurrent Java code reliably in unit and stress tests?

**Answer:** Simple `Thread.sleep()` tests miss subtle race conditions. Use:
1. **`CountDownLatch` start-gun pattern:** create $N$ worker threads that all call `readyLatch.countDown()` and then `startGun.await()`; the main test thread calls `startGun.countDown()` so all $N$ threads hammer the target method at the exact same instant.
2. **OpenJDK `jcstress` (Java Concurrency Stress):** the official tool used by JDK engineers to test JMM visibility, atomicity, and instruction reordering across millions of iterations.
3. **Awaitility:** for testing asynchronous state transitions (`await().atMost(2, SECONDS).untilAsserted(...)`) instead of brittle `Thread.sleep()`.

```java
// Start-gun test pattern for maximum thread contention
CountDownLatch ready = new CountDownLatch(threads);
CountDownLatch start = new CountDownLatch(1);
CountDownLatch done  = new CountDownLatch(threads);

for (int i = 0; i < threads; i++) {
    executor.submit(() -> {
        ready.countDown();
        try {
            start.await();                        // all threads wait at the starting line
            serviceUnderTest.increment();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            done.countDown();
        }
    });
}
ready.await();
start.countDown();                                // fire the starting gun!
done.await();
```

### Q35. Production Scenario: Your Spring Boot service CPU spikes to 100% or all HTTP worker threads hang. How do you diagnose it step by step?

**Answer:**
1. **Capture 3 thread dumps spaced 5 seconds apart** (`jcmd <pid> Thread.print > dump1.txt` or `kill -3 <pid>`) so you can distinguish threads making progress from threads stuck at the exact same line.
2. **If CPU is 100%:**
   - Find the thread consuming CPU: `top -H -p <pid>` gives the OS native thread ID (`PID`).
   - Convert the decimal `PID` to lowercase hex (`printf "%x\n" <tid>`) and search for `nid=0x<hex>` in the thread dump.
   - Common culprits: infinite loop, regex catastrophic backtracking (`ReDoS`), non-thread-safe `HashMap` corruption, or constant GC thrashing (`GC task thread` — verify with `jstat -gcutil <pid> 1000`).
3. **If CPU is low but requests hang (thread starvation / deadlock):**
   - Count thread states in the dump:
     - Hundreds of threads `BLOCKED (on object monitor)` -> lock contention or deadlock; inspect which thread holds `- locked <0x0000...>` that everyone else is `- waiting to lock <0x0000...>`.
     - Hundreds of threads in `WAITING` / `TIMED_WAITING` inside `HikariPool.getConnection` -> DB connection pool exhausted (long transactions or slow SQL query).
     - Hundreds of threads in `SocketInputStream.socketRead0` -> downstream HTTP/RPC call without a read/connect timeout.

