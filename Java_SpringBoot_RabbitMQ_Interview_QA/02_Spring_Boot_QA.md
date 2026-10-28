# Spring Boot: Interview Question Set

Target: 6 to 10 years experience, integration architecture role.

## Index

1. Core Spring and IoC (Q1 to Q7)
2. Spring Boot Configuration and Startup (Q8 to Q13)
3. Transactions and Data Access (Q14 to Q20)
4. Web, REST and Security (Q21 to Q27)
5. Async, Scheduling and Threading (Q28 to Q31)
6. Observability, Operations and Testing (Q32 to Q38)
7. Architecture and Governance (Q39 to Q42)
8. Frequently Asked Tricky & Deep-Dive Questions (Q43 to Q56)
9. Transactions Deep-Dive (Q57 to Q64)

---

## 1. Core Spring and IoC

### Q1. What is Inversion of Control and Dependency Injection?

**Answer:** Inversion of Control (IoC) means Spring creates and connects application objects instead of your code creating everything with `new`. Dependency Injection (DI) is how Spring gives an object the other objects it needs. Constructor injection makes those needs clear and easy to test.

**Example:** Spring creates `OrderService` and supplies an `OrderRepository` when it constructs the service.
```java
@Service
class OrderService {
    private final OrderRepository orders;

    OrderService(OrderRepository orders) {
        this.orders = orders;
    }
}
```

### Q2. What are bean scopes?

**Answer:** A bean's scope controls how long Spring reuses it. `singleton` (the default) means one bean per application context. `prototype` creates a new instance when requested. Web apps also have request, session, and application scopes. A singleton is shared across requests, so do not store per-request data in its fields.

**Example:** This singleton service can safely serve many requests because it keeps no request-specific state in a field.
```java
@Service
class GreetingService {
    String greeting(String name) {
        return "Hello, " + name;
    }
}
```

### Q3. What is the bean lifecycle?

**Answer:** Spring creates a bean, gives it its dependencies, runs its initialization callbacks, and then makes it available. When the application shuts down, Spring runs its cleanup callback. Spring can also wrap a bean in a proxy for features such as `@Transactional`.

**Example:** Spring calls `connect()` after constructing this bean, then calls `close()` during orderly shutdown.
```java
@Component
class PartnerConnection {
    @PostConstruct
    void connect() { /* initialize the connection */ }

    @PreDestroy
    void close() { /* release resources */ }
}
```

### Q4. What is the difference between `@Component`, `@Service`, `@Repository`, and `@Controller`?

**Answer:** These annotations tell Spring to find and create classes as beans. They also describe the class's role: `@Service` for business logic, `@Repository` for database access, and `@Controller` or `@RestController` for web requests. `@Repository` also translates common database exceptions into Spring's data-access exceptions.

**Example:** A request enters a controller, which calls a service, which reads data through a repository.
```java
@RestController
class OrderController {
    private final OrderService service;

    OrderController(OrderService service) { this.service = service; }
}

@Service
class OrderService { }

@Repository
interface OrderRepository extends JpaRepository<Order, Long> { }
```

### Q5. What is the difference between `@Configuration` and `@Component` for beans?

**Answer:** Use `@Configuration` for a class that declares beans with `@Bean`. Spring enhances it so that calls between its bean methods return the managed bean, usually the same singleton. A plain `@Component` with `@Bean` methods does not get that full behavior for direct method calls.

**Example:** `orderService()` receives the managed `paymentClient` bean rather than making an unmanaged second instance.
```java
@Configuration
class AppConfig {
    @Bean
    PaymentClient paymentClient() {
        return new PaymentClient();
    }

    @Bean
    OrderService orderService() {
        return new OrderService(paymentClient());
    }
}
```

### Q6. What is the difference between JDK dynamic proxies and CGLIB proxies?

**Answer:** A proxy is a wrapper Spring uses to add behavior such as transactions. A JDK proxy wraps an interface; a CGLIB proxy creates a subclass of the class. CGLIB cannot override final classes or methods. Make sure calls go through the Spring-managed proxy for features such as `@Transactional`.

**Example:** When another bean calls `paymentService.pay()`, Spring's proxy can start and finish a transaction around the method.
```java
public interface PaymentService {
    void pay();
}

@Service
class PaymentServiceImpl implements PaymentService {
    @Transactional
    public void pay() {
        // database work runs in a transaction
    }
}
```

### Q7. What is AOP in Spring, and what are the common uses?

**Answer:** AOP lets Spring apply common behavior—such as logging, timing, or transactions—around methods without putting that code in every method. Spring AOP uses proxies, so the call must pass through the Spring-managed bean. Calling another method on `this` does not pass through that proxy.

**Example:** A service can keep its business method focused on processing while a timing aspect records how long it takes.
```java
@Around("@annotation(MeasureTime)")
Object measure(ProceedingJoinPoint call) throws Throwable {
    long start = System.nanoTime();
    try {
        return call.proceed();
    } finally {
        recordDuration(System.nanoTime() - start);
    }
}
```

---

## 2. Spring Boot Configuration and Startup

### Q8. How does Spring Boot's startup work?

**Answer:** `SpringApplication.run()` starts the app. Spring loads configuration, creates the application context, finds and configures beans, starts the embedded web server (if used), and then runs startup callbacks such as `ApplicationRunner`.

**Example:** An `ApplicationRunner` runs once after the application starts, which can be useful for checking startup data or printing a message.
```java
@Component
class StartupMessage implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Application is ready");
    }
}
```

### Q9. What is the order of property precedence in Spring Boot?

**Answer:** Spring can get a setting from several places. In a simplified order, command-line arguments override environment variables, which override profile-specific configuration, which overrides the default `application.yml` and code defaults. This lets deployments change settings without rebuilding the app.

**Example:** The file sets the default port to 8080, but an environment variable can choose a different port for a deployment.
```yaml
# application.yml
server:
  port: 8080
```
```text
SERVER_PORT=9090
```

### Q10. How do you use `@ConfigurationProperties` correctly?

**Answer:**

```java
@ConfigurationProperties(prefix = "integration.erp")
@Validated
public record ErpProperties(
        @NotBlank String baseUrl,
        @Min(1) @Max(60) int timeoutSeconds,
        @NotNull Duration retryBackoff) {}
```

Enable with `@ConfigurationPropertiesScan` or `@EnableConfigurationProperties`. Validation fails the startup if values are wrong, which is better than failing at runtime.

**Example:** The YAML keys map to the record fields. If the URL is blank or the timeout is outside 1–60 seconds, startup validation reports the configuration error.
```yaml
integration:
  erp:
    base-url: https://erp.example.com
    timeout-seconds: 10
    retry-backoff: 2s
```

### Q11. What are Spring profiles, and how do you use them safely?

**Answer:** Profiles let you use environment-specific settings or beans, such as local development versus production. Activate a profile with `SPRING_PROFILES_ACTIVE`. Keep the business behavior the same across profiles so production code is still tested.

**Example:** The app uses a local database URL in the default file and a production URL in `application-prod.yml`. Activate the production profile in deployment.
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://db.internal/orders
```
```text
SPRING_PROFILES_ACTIVE=prod
```

### Q12. How do you customize auto-configuration?

**Answer:** Spring Boot usually supplies a default bean when you have not defined one. To change that behavior, define your own bean of the needed type. When creating a reusable starter, use conditional auto-configuration so applications can override the default.

**Example:** If auto-configuration provides a default clock, this bean supplies a UTC clock instead.
```java
@Configuration
class TimeConfig {
    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
```

### Q13. How do you implement graceful shutdown?

**Answer:** Graceful shutdown gives work already in progress a chance to finish. Spring stops accepting new web requests and waits up to the configured timeout before closing. Message consumers should stop taking new messages and finish or release the current message safely.

**Example:** These settings enable graceful web-server shutdown and allow up to 30 seconds for in-progress work.
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

---

## 3. Transactions and Data Access

### Q14. How does `@Transactional` work, and what are its limits?

**Answer:** Spring creates a proxy that starts a transaction before the method and commits or rolls back after it. Limits:
- Self-invocation bypasses the proxy.
- Only public methods work with the default proxy setup for most cases.
- By default, rollback happens for unchecked exceptions only. Use `rollbackFor = Exception.class` for checked exceptions.
- Do not call remote services or wait on messages inside a transaction. Keep transactions short.

**Example:** If `saveOrder` throws a runtime exception, Spring rolls back the database changes made by this method.
```java
@Service
class OrderService {
    @Transactional
    public void saveOrder(Order order) {
        repository.save(order);
        // A runtime exception here rolls back the transaction
    }
}
```

### Q15. What are transaction propagation levels?

**Answer:**
- `REQUIRED` (default): join the current transaction or start one.
- `REQUIRES_NEW`: always start a new one, suspending the current one.
- `SUPPORTS`: use one if it exists.
- `MANDATORY`: fail if none exists.
- `NEVER`: fail if one exists.
- `NOT_SUPPORTED`: run without a transaction.
- `NESTED`: savepoint within the current transaction (JDBC only).

**Example:** `REQUIRED` lets a service method join the caller's transaction. `REQUIRES_NEW` can save an audit record in its own transaction even if the outer operation later rolls back.
```java
@Service
class OrderService {
    private final OrderRepository orderRepository;
    private final AuditService auditService;

    OrderService(OrderRepository orderRepository, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void placeOrder(Order order) {
        orderRepository.save(order);
        auditService.recordOrderCreated(order); // Calls a separate Spring bean
    }
}

@Service
class AuditService {
    private final AuditRepository auditRepository;

    AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOrderCreated(Order order) {
        auditRepository.save(new AuditEntry(order.getId()));
    }
}
```

### Q16. What are isolation levels, and what problems do they prevent?

**Answer:** Isolation controls how much one database transaction can see another transaction's changes. `READ_UNCOMMITTED` allows the most visibility; `SERIALIZABLE` is the strictest and can reduce concurrency. `READ_COMMITTED` prevents reading uncommitted changes and is a common default. Exact behavior can vary by database.

**Example:** If one transaction changes an account balance but has not committed, `READ_COMMITTED` prevents another transaction from reading that uncommitted value.
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public BigDecimal currentBalance(long accountId) {
    return accountRepository.getBalance(accountId);
}
```

### Q17. How do you handle concurrent updates in JPA?

**Answer:**
- **Optimistic locking:** add `@Version` to the entity. A stale update throws `OptimisticLockException` (wrapped as `ObjectOptimisticLockingFailureException`). Retry or reject.
- **Pessimistic locking:** `@Lock(LockModeType.PESSIMISTIC_WRITE)` issues `SELECT ... FOR UPDATE`. Use for short critical sections only.

Optimistic locking suits most integration flows because conflicts are rare.

**Example:** The `@Version` value changes on each successful update. If two requests edit the same row, the second request with an old version gets a conflict rather than silently overwriting the first update.
```java
@Entity
class Inventory {
    @Id
    private Long id;

    private int quantity;

    @Version
    private long version;
}
```

### Q18. What is the N+1 select problem, and how do you fix it?

**Answer:** N+1 happens when one query loads a list of parent records, then the app sends another query for each parent's children. For 100 parents, that can mean 101 queries. Fetch the needed data together with a join, entity graph, batch fetching, or a DTO query.

**Example:** This fetch-join query loads orders and their customer data in one query instead of loading each customer separately.
```java
@Query("select o from Order o join fetch o.customer")
List<Order> findAllWithCustomer();
```

### Q19. What is the difference between `FetchType.LAZY` and `EAGER`?

**Answer:** `LAZY` waits to load related data until the code asks for it. `EAGER` loads it as part of loading the entity, which can fetch more data than needed. Prefer lazy loading and explicitly fetch what a particular operation needs.

**Example:** Products are not loaded when the order is first read. Accessing `order.getProducts()` inside an open persistence context loads them.
```java
@Entity
class Order {
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Product> products;
}
```

### Q20. How do you handle bulk inserts and updates efficiently?

**Answer:** Avoid sending one database insert for every row when loading lots of data. Batch multiple writes together, and periodically flush and clear JPA's persistence context so it does not keep every entity in memory. For very large imports, consider JDBC or a database-native bulk loader.

**Example:** Hibernate sends inserts in groups of 50; clearing every 50 records limits memory use during the import.
```yaml
spring:
  jpa:
    properties:
      hibernate.jdbc.batch_size: 50
```
```java
for (int i = 0; i < rows.size(); i++) {
    entityManager.persist(rows.get(i));
    if ((i + 1) % 50 == 0) {
        entityManager.flush();
        entityManager.clear();
    }
}
```

---

## 4. Web, REST and Security

### Q21. What is the difference between `RestTemplate`, `WebClient`, and `RestClient`?

**Answer:**
- `RestTemplate`: blocking, in maintenance mode. Use for legacy code only.
- `WebClient`: reactive, non-blocking, but usable in blocking style with `.block()`. Good for high-concurrency I/O.
- `RestClient` (Spring Framework 6.1+, Boot 3.2+): modern blocking client with a fluent API, a good replacement for `RestTemplate`.

Set connect and read timeouts on all clients.

**Example:** Use `RestClient` for a straightforward blocking call. Use `WebClient` when the app already uses reactive flows and needs non-blocking I/O.
```java
RestClient client = RestClient.create("https://inventory.example.com");

Inventory inventory = client.get()
    .uri("/items/{id}", itemId)
    .retrieve()
    .body(Inventory.class);
```

### Q22. How do you handle errors from downstream REST calls?

**Answer:** Check the response status and turn failures into clear application errors. Retry temporary failures such as timeouts, HTTP 5xx, or HTTP 429 when appropriate. Do not keep retrying permanent errors such as most other 4xx responses. Log which downstream service failed and its status, but do not log secrets or full sensitive responses.

**Example:** Convert a downstream 404 into a domain exception that the application can handle.
```java
Inventory inventory = client.get()
    .uri("/items/{id}", itemId)
    .retrieve()
    .onStatus(status -> status.value() == 404,
        (request, response) -> {
            throw new ItemNotFoundException(itemId);
        })
    .body(Inventory.class);
```

### Q23. How does Spring MVC process a request?

**Answer:** Spring MVC receives a request in the `DispatcherServlet`, finds the matching controller method, converts request data into method arguments, and calls the controller. It then converts the returned value into an HTTP response. Shared exception handlers can turn errors into consistent responses.

**Example:** A `GET /orders/42` request is routed to `getOrder`, where Spring converts `42` to a `Long` and serializes the returned order as JSON.
```java
@RestController
class OrderController {
    @GetMapping("/orders/{id}")
    Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
```

### Q24. How do you validate requests?

**Answer:** Put basic input rules on request DTO fields using Bean Validation, then add `@Valid` to the controller parameter. Spring rejects invalid requests before business processing. Put business rules—such as whether an account can afford a payment—in the service layer.

**Example:** Spring returns a validation error if the name is blank or the email is not a valid email address.
```java
record CreateCustomerRequest(
    @NotBlank String name,
    @Email String email
) { }

@PostMapping("/customers")
Customer create(@Valid @RequestBody CreateCustomerRequest request) {
    return customerService.create(request);
}
```

### Q25. How does Spring Security's filter chain work?

**Answer:** Spring Security checks a request before it reaches your controller. A chain of filters can check who the caller is (authentication), what they may do (authorization), and apply protections such as security headers and CSRF rules. Different paths can use different security rules.

**Example:** Public health checks are permitted, while all other endpoints require an authenticated caller.
```java
@Bean
SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated())
        .build();
}
```

### Q26. How do you secure service-to-service calls in Spring Boot?

**Answer:** For service-to-service calls, the calling service can get an OAuth2 access token using its own client credentials and send that token to the other service. The receiving service must validate the token and required permissions. Use TLS, and avoid forwarding a user's identity unless the downstream service needs it.

**Example:** A request to the inventory API includes the access token in the standard bearer-token header.
```http
GET /inventory/42 HTTP/1.1
Host: inventory.internal
Authorization: Bearer <access-token>
```

### Q27. How do you prevent common web vulnerabilities in a Spring Boot API?

**Answer:**
- Injection: use parameterized queries and JPA or JdbcTemplate parameters.
- Mass assignment: map request DTOs to entities explicitly.
- Sensitive data: never log tokens, passwords, or full payloads.
- Dependency risks: scan with OWASP Dependency-Check or Snyk.
- Headers and CORS: keep restrictive defaults.
- Rate limiting: protect login and expensive endpoints.

**Example:** Bind user input to a query parameter instead of joining raw text into SQL. Only fields present in the request DTO can be changed by this endpoint.
```java
@Query("select u from User u where u.email = :email")
Optional<User> findByEmail(@Param("email") String email);

record UpdateProfileRequest(String displayName) { }
```

---

## 5. Async, Scheduling and Threading

### Q28. How does `@Async` work, and what are its pitfalls?

**Answer:** `@Async` asks Spring to run a method on a background executor instead of making the caller wait for it. Enable async support, and call the method through a Spring bean (not from another method in the same class). Configure a bounded executor so a burst cannot create unlimited queued work. Return a `CompletableFuture` when the caller needs the result or error.

**Example:** The caller receives a future immediately and can handle the result when the email task finishes.
```java
@EnableAsync
@Configuration
class AsyncConfig { }

@Service
class EmailService {
    @Async
    public CompletableFuture<String> sendReceipt(String email) {
        sendEmail(email);
        return CompletableFuture.completedFuture("sent");
    }
}
```

### Q29. How do you configure virtual threads in Spring Boot?

**Answer:** With Java 21 and Spring Boot 3.2 or later, set `spring.threads.virtual.enabled=true` to use virtual threads where Spring Boot supports them. Virtual threads make waiting on I/O cheaper, but they do not increase database connections or a partner API's capacity. Keep limits around those resources.

**Example:** A Spring Boot app can enable virtual threads in configuration; a JDBC connection pool still limits how many database operations can run at once.
```yaml
spring:
  threads:
    virtual:
      enabled: true
  datasource:
    hikari:
      maximum-pool-size: 20
```

### Q30. How do you schedule jobs safely in a multi-instance deployment?

**Answer:** `@Scheduled` runs on every instance by default. Options:
- Use ShedLock to take a distributed lock in a database or Redis.
- Run the job as a Kubernetes `CronJob`.
- Use a dedicated scheduler instance with a leader election.

Make the job idempotent and record its last successful run.

**Example:** If two application instances run this scheduled method at the same time, a distributed lock lets only one instance perform the job for that lock period.
```java
@Scheduled(cron = "0 0 * * * *")
@SchedulerLock(name = "hourlyOrderSync",
               lockAtMostFor = "55m",
               lockAtLeastFor = "1m")
void syncOrders() {
    syncOrdersIdempotently();
}
```

### Q31. How do you implement a reliable batch job in Spring?

**Answer:** Use Spring Batch when a job processes many records or must restart safely after a failure. It reads a chunk, processes it, and writes it in a transaction. Its job repository tracks progress. Configure retries and skips for known failure cases, and make writes safe to repeat.

**Example:** A chunk size of 100 means Spring Batch reads and writes 100 records per transaction. If a chunk fails, that chunk can be retried instead of starting the whole file again.
```java
new StepBuilder("importCustomers", jobRepository)
    .<CustomerRow, Customer>chunk(100, transactionManager)
    .reader(customerReader)
    .processor(customerProcessor)
    .writer(customerWriter)
    .build();
```

---

## 6. Observability, Operations and Testing

### Q32. What do the Spring Boot Actuator endpoints give you, and how do you secure them?

**Answer:** Actuator provides endpoints that help you check and monitor a running app, such as health, metrics, and thread dumps. Expose only the endpoints operators need and protect sensitive ones. In particular, environment and heap-dump endpoints can reveal secrets or private data.

**Example:** Expose health and metrics, but do not expose every management endpoint to the public web.
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

### Q33. How do you configure liveness and readiness probes?

**Answer:** Liveness answers, “Should the platform restart this process?” Readiness answers, “Should this instance receive traffic right now?” A dependency outage may make an instance unready, but usually should not make it fail liveness, or the platform may repeatedly restart healthy app processes.

**Example:** Enable Spring Boot's probe endpoints. Kubernetes can use `/actuator/health/liveness` and `/actuator/health/readiness` for separate checks.
```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
```

### Q34. How do you add custom metrics?

**Answer:** Inject `MeterRegistry` and create counters, timers, or gauges:

```java
Timer.builder("integration.erp.call")
     .tag("operation", "createOrder")
     .register(meterRegistry)
     .record(() -> erpClient.createOrder(order));
```

Keep tag cardinality low. Never tag with user IDs or order IDs.

**Example:** Increment a counter after successfully sending an order. A fixed operation tag is safe; a unique order ID tag would create too many time series.
```java
Counter sentOrders = Counter.builder("integration.orders.sent")
    .tag("system", "erp")
    .register(meterRegistry);

sentOrders.increment();
```

### Q35. How do you test Spring Boot applications at each level?

**Answer:**
- **Unit test:** test one class with JUnit and Mockito, without starting Spring.
- **Slice test:** start only one part of Spring, such as MVC or JPA.
- **Integration test:** start the application and real dependencies, often with Testcontainers.
- **Contract test:** check that two services agree on their API or message format.

Use the real database engine in integration tests when possible; H2 may behave differently from the production database.

**Example:** A slice test starts the MVC layer, sends a request, and checks the HTTP response without starting the whole application.
```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void returnsBadRequestForInvalidOrder() throws Exception {
        mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}
```

### Q36. What changed for mocking beans in Spring Boot 3.4?

**Answer:** In Spring Boot 3.4, `@MockBean` and `@SpyBean` are deprecated. For new tests, use Spring Framework's `@MockitoBean` and `@MockitoSpyBean`.

**Example:** Replace a real payment gateway with a Mockito mock inside a Spring test.
```java
@SpringBootTest
class CheckoutServiceTest {
    @MockitoBean
    PaymentGateway paymentGateway;

    @Autowired
    CheckoutService checkoutService;
}
```

### Q37. How do you test messaging code in Spring?

**Answer:** For messaging integration tests, run a real broker with Testcontainers. Publish through the same client the application uses, wait for the listener's side effect, and test failures too—such as retry, dead-letter routing, and duplicate messages.

**Example:** Publish a message and use Awaitility to wait for the listener to save the expected result; asynchronous processing may take a short time.
```java
rabbitTemplate.convertAndSend("orders", new OrderCreated("o-42"));

await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
    assertThat(orderRepository.existsByExternalId("o-42")).isTrue()
);
```

### Q38. How do you manage configuration drift between environments?

**Answer:** Build one JAR or container image and promote that exact artifact through test and production. Change only environment-specific configuration and secrets. Validate required settings at startup so a bad configuration fails early. Review config changes just like code.

**Example:** The same image reads a different database URL in staging and production from environment-specific variables.
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
```

---

## 7. Architecture and Governance

### Q39. What standards would you define for a Spring Boot platform?

**Answer:**
- Java version and Spring Boot BOM version, managed centrally.
- Project template (structure, logging, error handling, actuator, health probes).
- Shared starter library for common concerns (tracing, error model, security, retry defaults).
- Coding rules (Checkstyle, SpotBugs, ArchUnit for layering rules).
- CI/CD pipeline with test, scan, and build steps.
- Documentation (README, ADRs, OpenAPI).

**Example:** A platform template can give every service the same health endpoint, log format, security defaults, and CI checks. Teams can then focus on business behavior instead of rebuilding those basics.
```text
service-template/
  src/main/java/...
  actuator + health probes
  structured logging
  CI: test -> dependency scan -> build image
  README + OpenAPI
```

### Q40. How would you enforce layering rules in a Spring Boot project?

**Answer:** Architecture tests can check that code follows the intended layers. For example, controllers should call services rather than repositories directly, and domain classes should not depend on web code. Run these checks in CI so new changes cannot quietly break the design.

**Example:** An ArchUnit rule fails the build if a class in the controller package depends directly on a repository.
```java
noClasses()
    .that().resideInAPackage("..controller..")
    .should().dependOnClassesThat()
    .resideInAPackage("..repository..");
```

### Q41. How do you handle schema migration with Spring Boot?

**Answer:** Use a migration tool such as Flyway or Liquibase to track database changes. For a rolling deployment, make backward-compatible changes in stages: add the new column, deploy code that can work with old and new versions, backfill data, switch reads, and only later remove the old column.

**Example:** First add a nullable column with a migration. A later app version can populate it while old app instances still run.
```sql
-- V12__add_customer_region.sql
ALTER TABLE customer ADD COLUMN region VARCHAR(50);
```

### Q42. When would you choose Spring Boot over other frameworks for an iPaaS?

**Answer:** Choose Spring Boot when the team knows Java/Spring and benefits from its broad support for web, security, databases, messaging, and monitoring. Consider Quarkus or Micronaut when fast startup and low memory are especially important. A dedicated iPaaS may fit better when customers need low-code integration design. Compare real deployment needs rather than choosing only by framework popularity.

**Example:** A long-running integration service with many Spring and Camel connectors may benefit from Spring Boot's mature ecosystem. A short-lived connector that must start quickly in a constrained container may be worth benchmarking with Quarkus or Micronaut.

---

## 8. Frequently Asked Tricky & Deep-Dive Questions

### Q43. How do you inject a `prototype`-scoped bean into a `singleton` bean correctly?

**Answer:** Direct constructor injection only resolves the prototype once when the singleton is created. To get a new prototype instance per call, use:
- `ObjectProvider<MyPrototype>` (`provider.getObject()`).
- `@Lookup` method injection.
- Scoped proxy: `@Scope(value = ConfigurableBeanFactory.ConfigurablePrototype, proxyMode = ScopedProxyMode.TARGET_CLASS)`.

**Example:** The singleton service asks the provider for a fresh job object every time `startJob()` is called.
```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ImportJob { }

@Service
class ImportService {
    private final ObjectProvider<ImportJob> jobs;

    ImportService(ObjectProvider<ImportJob> jobs) {
        this.jobs = jobs;
    }

    ImportJob startJob() {
        return jobs.getObject(); // A new prototype bean each call
    }
}
```

### Q44. How does Spring handle circular dependencies, and why are they discouraged?

**Answer:** Spring Boot 2.6+ disables circular references by default (`spring.main.allow-circular-references=false`). Constructor injection fails immediately at startup with `BeanCurrentlyInCreationException`. Fix circular dependencies by extracting shared logic into a third service, using domain events (`ApplicationEventPublisher`), or redesigning module boundaries (rather than hiding the issue with `@Lazy`).

**Example:** If `OrderService` needs `PaymentService` and `PaymentService` also needs `OrderService`, Spring cannot construct either first. Move the shared behavior into a third service so dependencies point one way.
```text
Before: OrderService <-> PaymentService
After:  OrderService -> PaymentProcessor
        PaymentService -> PaymentProcessor
```

### Q45. What happens when a `@Transactional` method calls another `@Transactional(propagation = REQUIRES_NEW)` method in the same class?

**Answer:** Nothing—both run in the original transaction because self-invocation (`this.otherMethod()`) bypasses the Spring AOP proxy. To make `REQUIRES_NEW` take effect, move the second method into a separate bean and inject it.

**Example:** This call to `writeAudit()` is an internal call, so the `REQUIRES_NEW` annotation is not applied. Put `writeAudit()` on a separate Spring bean and call that bean instead.
```java
@Service
class OrderService {
    @Transactional
    void placeOrder(Order order) {
        saveOrder(order);
        writeAudit(order); // Self-invocation: does not start a new transaction
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void writeAudit(Order order) { }
}
```

### Q46. Why is `spring.jpa.open-in-view` (`OSIV`) enabled by default, and why should you disable it in production?

**Answer:** Open Session in View keeps the Hibernate persistence context open through the HTTP request, including response serialization. That can let JSON serialization trigger hidden lazy-loading database queries. These extra queries can make responses slow and put pressure on the database connection pool. Set `spring.jpa.open-in-view=false` and fetch the data needed for the response explicitly with `JOIN FETCH`, `@EntityGraph`, or a DTO projection.

**Example:** Disable Open Session in View and load the customer needed by the response inside the repository query, rather than letting JSON serialization trigger a hidden database query.
```yaml
spring:
  jpa:
    open-in-view: false
```
```java
@Query("select o from Order o join fetch o.customer where o.id = :id")
Optional<Order> findWithCustomerById(Long id);
```

### Q47. What is the difference between `@Qualifier` and `@Primary` when multiple beans implement the same interface?

**Answer:** `@Primary` marks the default bean to use when no qualifier is specified. `@Qualifier("beanName")` explicitly selects a specific bean at the injection point and takes precedence over `@Primary`.

**Example:** `CardGateway` is selected by default, while a specific service can request `BankGateway` by name.
```java
@Component
@Primary
class CardGateway implements PaymentGateway { }

@Component("bankGateway")
class BankGateway implements PaymentGateway { }

@Service
class BankPaymentService {
    BankPaymentService(@Qualifier("bankGateway") PaymentGateway gateway) {
        // This injection gets BankGateway, not the primary CardGateway.
    }
}
```

### Q48. What is the difference between `Filter`, `HandlerInterceptor`, and `@Aspect` (AOP) in Spring Boot?

**Answer:**
- **Servlet `Filter` (`OncePerRequestFilter`):** runs at the servlet container level before `DispatcherServlet`. Best for security, correlation IDs, request/response logging, and CORS.
- **`HandlerInterceptor`:** runs inside Spring MVC before/after controller execution with access to the target `HandlerMethod`.
- **`@Aspect`:** wraps any Spring bean method (service, repository, or controller) for cross-cutting concerns like retries, metrics, or custom auditing.

**Example:** Use a filter to attach a correlation ID to every HTTP request, an interceptor to check controller-specific metadata, and an aspect to time service methods.
```text
HTTP request -> Filter -> DispatcherServlet
             -> HandlerInterceptor -> Controller
             -> Service method wrapped by @Aspect
```

### Q49. How do you manage multiple datasources (e.g., MySQL and Oracle) in a single Spring Boot service?

**Answer:**
1. Define two `@ConfigurationProperties` for the datasources.
2. Create two `DataSource`, `LocalContainerEntityManagerFactoryBean`, and `PlatformTransactionManager` beans.
3. Mark one as `@Primary`.
4. Use `@EnableJpaRepositories(basePackages = ..., entityManagerFactoryRef = ..., transactionManagerRef = ...)` to bind each repository package to its respective database.
5. Specify the transaction manager in `@Transactional("oracleTransactionManager")` when targeting the secondary DB.

**Example:** Give each database its own repository package and transaction manager. A service can name the Oracle transaction manager when it writes to Oracle.
```java
@Transactional("oracleTransactionManager")
public void saveLegacyRecord(LegacyRecord record) {
    oracleRepository.save(record);
}
```

### Q50. How do you use Redis in Spring Boot for caching and distributed coordination?

**Answer:**
- **Caching:** `@EnableCaching` with `@Cacheable`, `@CachePut`, `@CacheEvict` backed by `RedisCacheManager`. Always configure a TTL and JSON serialization (avoid Java serialization).
- **Preventing Cache Stampede:** use `@Cacheable(sync = true)` or Redis distributed locks.
- **Distributed Locks / Idempotency:** use `StringRedisTemplate.opsForValue().setIfAbsent(key, val, ttl)` or Redisson.
- **Rate Limiting:** Redis + Lua scripts (e.g., Spring Cloud Gateway `RequestRateLimiter` or Bucket4j-Redis).

**Example:** Cache a product lookup so repeated calls for the same ID can return the cached value. Set an expiration so old data does not live forever.
```java
@Cacheable(cacheNames = "products", key = "#id")
public Product findProduct(String id) {
    return productRepository.findById(id).orElseThrow();
}
```

### Q51. How do you resolve Jackson serialization issues like `LazyInitializationException` and infinite recursion?

**Answer:** Do not serialize JPA entities directly in REST responses or message payloads. Map entities to DTOs/records using MapStruct or manual mappers. If entities must be serialized, use `@JsonIgnore`, `@JsonManagedReference`/`@JsonBackReference`, or the `jackson-datatype-hibernate6` module.

**Example:** Return a response DTO containing only the fields the API needs, rather than serializing the entity and its lazy relationships.
```java
record OrderResponse(Long id, String customerName) { }

OrderResponse toResponse(Order order) {
    return new OrderResponse(order.getId(), order.getCustomer().getName());
}
```

### Q52. What is `@TransactionalEventListener`, and what happens if the listener throws an exception?

**Answer:** `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` runs the listener only after the producer's database transaction commits, preventing premature messages on rollback.
- **Gotcha:** By `AFTER_COMMIT`, the original transaction is already committed. If the listener needs to write to the DB, it requires `@Transactional(propagation = Propagation.REQUIRES_NEW)`. And if the listener fails to publish to RabbitMQ, the DB write is already committed—which is why critical flows use the **Transactional Outbox** pattern instead of in-memory events.

**Example:** Publish an event while saving an order. This listener runs only after the order transaction commits; for guaranteed message delivery, store an outbox row in the same transaction and publish it with a separate relay.
```java
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    eventPublisher.publishEvent(new OrderCreated(order.getId()));
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void afterOrderCommit(OrderCreated event) {
    // Convenient notification, but not durable if the process stops here.
    rabbitTemplate.convertAndSend("orders", event);
}
```

### Q53. How do you build a custom Spring Boot Starter for reusable iPaaS platform capabilities?

**Answer:**
1. Create an `autoconfigure` module containing `@AutoConfiguration` classes, `@ConfigurationProperties`, and conditional beans (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`).
2. Register the auto-configuration class in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
3. Create a starter module that pulls in the `autoconfigure` module and required transitive dependencies.
Teams include the starter dependency and automatically get standardized tracing, exception handling (`ProblemDetail`), security filters, and Camel/RabbitMQ defaults.

**Example:** A starter can create a default client only when the application has the client library and has not supplied its own bean.
```java
@AutoConfiguration
@ConditionalOnClass(ErpClient.class)
class ErpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    ErpClient erpClient(ErpProperties properties) {
        return new ErpClient(properties.baseUrl());
    }
}
```

### Q54. How do you tune embedded Tomcat and HikariCP for high-throughput Spring Boot services?

**Answer:**
- **Tomcat:** tune `server.tomcat.threads.max`, `accept-count`, `max-connections`, and `connection-timeout`, or enable virtual threads (`spring.threads.virtual.enabled=true`).
- **HikariCP:** keep `maximum-pool-size` tight (often 10–30 per pod, ensuring `pods * maxPoolSize < DB max_connections`), set `max-lifetime` 30–60s shorter than DB/network idle timeouts, and enable `leak-detection-threshold` in staging to catch unclosed connections.

**Example:** With 5 pods and a pool size of 10, the service can use up to 50 DB connections. Ensure this fits the database's connection budget, including other applications.
```yaml
server:
  tomcat:
    threads:
      max: 200
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      max-lifetime: 1740000
```

### Q55. What is Spring Cloud Config / Kubernetes ConfigMaps reload strategy without restarting pods?

**Answer:** Bind properties via `@ConfigurationProperties` (which supports dynamic rebinding cleanly) or `@RefreshScope` beans. Trigger reload via Spring Cloud Bus, Actuator `/actuator/refresh`, or Spring Cloud Kubernetes configuration watcher when a ConfigMap/Secret changes. Keep in mind that connection pools and broker listeners should be drained carefully on credential rotation.

**Example:** A refresh-scoped bean can receive updated settings after a configured refresh event. Do not expose the refresh endpoint publicly; secure it and test which settings can safely change at runtime.
```java
@RefreshScope
@Component
class PartnerSettings {
    @Value("${partner.base-url}")
    private String baseUrl;

    String baseUrl() { return baseUrl; }
}
```

### Q56. What is the difference between Spring MVC and Spring WebFlux, and when should you choose each?

**Answer:** Spring MVC is imperative and thread-per-request (now scalable with Java 21 virtual threads). Spring WebFlux is non-blocking, event-loop based (Project Reactor/Netty). With Java 21 virtual threads in Spring Boot 3.2+, Spring MVC + `RestClient` + JDBC/AMQP gives high I/O concurrency with much simpler debugging and stack traces than reactive code. Use WebFlux primarily for reactive streaming, SSE/WebSockets, or Spring Cloud Gateway.

**Example:** MVC returns a regular object and works well with blocking JDBC. WebFlux returns a `Mono` and is useful when the whole request path uses non-blocking clients.
```java
// Spring MVC: blocking service call
@GetMapping("/orders/{id}")
Order getOrder(@PathVariable Long id) {
    return orderService.find(id);
}

// Spring WebFlux: reactive service call
@GetMapping("/reactive/orders/{id}")
Mono<Order> getOrderReactive(@PathVariable Long id) {
    return reactiveOrderService.find(id);
}
```

## 9. Transactions Deep-Dive

### Q57. Does `@Transactional` roll back on checked exceptions?

**Answer:** No. By default Spring rolls back only on **unchecked exceptions** (`RuntimeException`) and `Error`. A checked exception (e.g. `IOException`, custom `BusinessException extends Exception`) **commits** the transaction. Use `rollbackFor` / `noRollbackFor` to change this.

Also, if you **catch and swallow** an exception inside the method, Spring never sees it and commits. Either rethrow or call `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`.

**Example:**
```java
@Transactional(rollbackFor = Exception.class)          // roll back on checked too
public void importFile(Path file) throws IOException {
    repository.save(parse(file));                       // IOException now rolls back
}

@Transactional(noRollbackFor = DuplicateOrderException.class)
public void placeOrder(Order o) { /* ... */ }
```

### Q58. In which cases does `@Transactional` silently not work?

**Answer:**
1. **Self-invocation:** calling `this.method()` bypasses the proxy (see Q45).
2. **Non-public methods:** with proxy-based AOP, `private` methods are never intercepted (Spring 6 supports `protected`/package-private for CGLIB proxies, but `private` and `final` never work).
3. **Object not a Spring bean:** created with `new`, so there is no proxy.
4. **Exception swallowed** or a **checked exception** thrown (see Q57).
5. **Wrong transaction manager** with multiple datasources (see Q49).
6. **New thread / `@Async` / `CompletableFuture`:** the transaction is bound to a `ThreadLocal`, so work on another thread runs outside it.
7. **Storage engine not transactional:** e.g. MySQL MyISAM tables.
8. **`@Transactional` on an interface method with class-based proxies** in older versions — prefer annotating the concrete class.

### Q59. What does `@Transactional(readOnly = true)` do?

**Answer:** It is an optimization hint, not a security guard:
- Hibernate sets `FlushMode.MANUAL`, so no dirty checking or flush happens (saves CPU and memory for large reads).
- The JDBC connection is marked `setReadOnly(true)`; some drivers/databases optimize or route it to a read replica (e.g. with `AbstractRoutingDataSource` / `LazyConnectionDataSourceProxy`).
- Some databases (PostgreSQL, Oracle) reject writes in a read-only transaction; MySQL InnoDB does as well. Others may not enforce it.

Common pattern: `@Transactional(readOnly = true)` at the class level and `@Transactional` on write methods.

```java
@Service
@Transactional(readOnly = true)
public class OrderQueryService {
    public List<Order> findAll() { return repo.findAll(); }

    @Transactional                     // overrides readOnly for writes
    public Order save(Order o) { return repo.save(o); }
}
```

### Q60. What is `UnexpectedRollbackException` and why does it occur?

**Answer:** It occurs when an inner method with `REQUIRED` propagation throws a runtime exception, which marks the **shared** transaction as **rollback-only**. If the outer method catches that exception and tries to commit, Spring cannot commit and throws `UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only`.

Fixes:
- Let the exception propagate (rollback everything), or
- Run the inner call in `REQUIRES_NEW` (independent transaction) or `NESTED` (savepoint) if partial failure is acceptable, or
- Do not throw from the inner method; return a result instead.

```java
@Transactional
public void outer() {
    try {
        innerService.inner();          // REQUIRED, throws RuntimeException -> rollback-only
    } catch (RuntimeException e) {
        log.warn("ignored");           // commit at end -> UnexpectedRollbackException
    }
}
```

### Q61. What is the difference between declarative and programmatic transactions? When is `TransactionTemplate` better?

**Answer:** Declarative (`@Transactional`) is simple and covers most cases. Programmatic (`TransactionTemplate` or `PlatformTransactionManager`) gives fine-grained control:
- Transaction boundary is **smaller than a method** (e.g. call a remote API first, then open a short DB transaction).
- Inside loops: commit each batch separately.
- Avoids proxy issues (self-invocation, private methods).

```java
@Service
public class PaymentService {
    private final TransactionTemplate tx;

    PaymentService(PlatformTransactionManager tm) {
        this.tx = new TransactionTemplate(tm);
        this.tx.setTimeout(5);                       // seconds
    }

    public void pay(PaymentRequest req) {
        GatewayResult result = gateway.charge(req);  // remote call OUTSIDE the transaction
        tx.executeWithoutResult(status -> {
            paymentRepo.save(Payment.from(result));
            if (!result.ok()) status.setRollbackOnly();
        });
    }
}
```

### Q62. Optimistic vs pessimistic locking in Spring Data JPA — when do you use each?

**Answer:**
- **Optimistic (`@Version`):** no DB lock; on update Hibernate checks `WHERE id=? AND version=?`. If another transaction changed the row, it throws `ObjectOptimisticLockingFailureException`; you retry. Best for low-contention, read-heavy data and long user think-time.
- **Pessimistic (`@Lock(PESSIMISTIC_WRITE)`):** issues `SELECT ... FOR UPDATE`; other writers block until commit. Best for high-contention hot rows (inventory, balance) where retries would be frequent. Always set a lock timeout to avoid long waits/deadlocks.

```java
@Entity
class Account {
    @Id Long id;
    BigDecimal balance;
    @Version Long version;                  // optimistic locking
}

interface AccountRepo extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findForUpdate(@Param("id") Long id);
}
```

### Q63. How do you handle a transaction that spans multiple microservices?

**Answer:** `@Transactional` works only within one service and one resource (or JTA/XA for multiple resources in one JVM). Across microservices avoid 2PC/XA (blocking, poor availability) and use:
- **Saga pattern:** a sequence of local transactions; each step publishes an event, and failures trigger **compensating transactions** (e.g. `cancelPayment`, `releaseStock`). Orchestration (central coordinator) or choreography (events).
- **Transactional Outbox:** write the business row and an outbox event in the same local DB transaction; a relay (poller or CDC/Debezium) publishes to RabbitMQ/Kafka. Guarantees the DB change and event are not lost or out of sync.
- **Idempotent consumers** so retried/redelivered events do not apply twice.

### Q64. Where should `@Transactional` be placed — controller, service, or repository? Can it be used on a test?

**Answer:** Put it on the **service layer**, where a business use case is defined. Controllers should not own transactions (long transactions, mixing HTTP concerns). Spring Data repository methods are already transactional per call (`readOnly` for reads), but a use case calling several repositories needs one service-level transaction so all writes commit or roll back together.

In tests, `@Transactional` on a `@SpringBootTest`/`@DataJpaTest` method **rolls back automatically** after each test, keeping the DB clean. Use `@Commit` / `@Rollback(false)` to keep the data. Note: code running on other threads (e.g. RabbitMQ listeners) will not see the uncommitted test data.
