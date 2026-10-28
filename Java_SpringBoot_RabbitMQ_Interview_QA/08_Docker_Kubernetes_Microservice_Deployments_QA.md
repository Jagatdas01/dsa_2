# Docker, Kubernetes & Microservice Deployments: Interview Question Set

Target: 6 to 10 years experience, backend / integration architecture role.

## Index

1. Docker Fundamentals & Image Optimization (Q1 to Q10)
2. Kubernetes Architecture & Core Workloads (Q11 to Q20)
3. Kubernetes Networking, Services & Ingress (Q21 to Q26)
4. Configuration, Secrets & Storage (Q27 to Q29)
5. Scaling, Probes, Resource Management & JVM Tuning (Q30 to Q34)
6. Microservice Deployment Strategies, CI/CD & Production Troubleshooting (Q35 to Q42)

---

## 1. Docker Fundamentals & Image Optimization

### Q1. How do containers differ from Virtual Machines (VMs), and what Linux kernel features make containers work?

**Answer:**
- **Virtual Machines** virtualize hardware; each VM runs a full guest OS and kernel on top of a hypervisor (high memory footprint, slow boot).
- **Containers** virtualize the OS; they share the host Linux kernel and run as isolated user-space processes (lightweight, start in milliseconds).
- **Key Linux kernel primitives:**
  - **Namespaces** (`pid`, `net`, `mnt`, `ipc`, `uts`, `user`): control what a process can *see* (process IDs, network stack, filesystem mount points).
  - **Control Groups (cgroups):** control how much a process can *use* (CPU, memory, disk I/O, network bandwidth).
  - **Union Filesystems (OverlayFS):** stack read-only image layers with a thin writable container layer on top (Copy-on-Write).

### Q2. What is the difference between `CMD` and `ENTRYPOINT` in a Dockerfile?

**Answer:**
- `ENTRYPOINT` defines the fixed executable that runs when the container starts.
- `CMD` provides default arguments to `ENTRYPOINT` (or the default command if `ENTRYPOINT` is not set) and is easily overridden by arguments passed to `docker run` or Kubernetes `args`.
- Always prefer **exec form** (`["java", "-jar", "app.jar"]`) over shell form (`java -jar app.jar`). Shell form wraps the process in `/bin/sh -c`, so `java` is not PID 1 and will not receive `SIGTERM` signals for graceful shutdown.

```dockerfile
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod"]
```

### Q3. What is the difference between `ADD` and `COPY` in a Dockerfile?

**Answer:**
- `COPY` simply copies local files or directories from the build context into the container filesystem.
- `ADD` also supports extracting local `.tar` archives automatically and fetching remote URLs.
- **Best practice:** Always use `COPY` for predictability and security unless you specifically need local tar auto-extraction. For remote files, use `curl`/`wget` in a `RUN` layer so you can verify checksums and clean up in the same layer.

### Q4. How do you optimize a Dockerfile for a Spring Boot microservice (multi-stage builds & layered JARs)?

**Answer:**

#### 1. What is wrong with a "Naive" Spring Boot Dockerfile?
```dockerfile
# BAD PRACTICE — DO NOT USE IN PRODUCTION
FROM eclipse-temurin:21-jdk
COPY target/order-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```
This naive approach has four major production problems:
1. **Huge Image Size (~500–800 MB):** It ships a full **JDK** (compilers, debuggers, header files) and build tools into production when only a **JRE** is needed to run bytecode.
2. **Broken Docker Layer Cache on Every Code Change:** A Spring Boot fat JAR (`app.jar`, ~80–120 MB) bundles third-party Maven libraries (`BOOT-INF/lib/`) and your compiled classes (`BOOT-INF/classes/`) into a single file. Changing a **1-line Java file** changes the hash of `app.jar`, forcing Docker to rebuild, push, and pull the entire 100 MB layer on every deployment even though 99% of the JAR (Maven dependencies) did not change.
3. **Slow Startup:** Launching a nested fat JAR requires unpacking or custom JAR URL handling at startup.
4. **Security Risk:** Running as `root` inside an image that includes compilers and package managers increases the attack surface.

---

#### 2. How the Optimization Works (Step by Step)

**Step A — Multi-Stage Build (Build vs. Runtime separation):**
- **Stage 1 (`builder`):** Uses a full JDK + Maven image to compile source code, run packaging, and unpack the fat JAR. Everything in this stage is discarded after the build.
- **Stage 2 (`runtime`):** Uses a slim **JRE** image (or a custom `jlink` / Distroless image) and copies only the extracted runtime files from Stage 1. Result: image shrinks from **~650 MB to ~180 MB** (or ~90 MB with Alpine/`jlink`).

**Step B — Dependency Caching in the Builder Stage:**
- Copy `pom.xml` and the Maven Wrapper (`.mvn/`, `mvnw`) **before** copying `src/`.
- Run `./mvnw dependency:go-offline`. Docker caches this layer; unless `pom.xml` changes, Maven will not re-download dependencies from Nexus/Maven Central.

**Step C — Spring Boot Layered JAR Extraction (`layertools` / `tools`):**
Spring Boot 2.3+ (and 3.x) structures the fat JAR using a `layers.idx` index file that splits the JAR into **4 logical layers**, ordered from **least likely to change** to **most likely to change**:

| Order | Layer Directory | What it Contains | Typical Size | How Often It Changes |
|---|---|---|---|---|
| 1 (Bottom) | `dependencies/` | Third-party release JARs (`spring-core`, `jackson`, `hikari`) | ~80–100 MB | Rarely (only when `pom.xml` versions change) |
| 2 | `spring-boot-loader/` | Spring Boot `JarLauncher` classes | ~250 KB | Only on Spring Boot upgrade |
| 3 | `snapshot-dependencies/` | Internal `*-SNAPSHOT.jar` libraries | ~0–5 MB | Occasionally |
| 4 (Top) | `application/` | Your microservice's `.class` files, `application.yml`, static resources | **~100–500 KB** | **Every commit!** |

When you copy each extracted directory in a separate `COPY --from=builder` instruction, a code change only invalidates Layer 4 (`application/`). Kubernetes nodes only pull **~200 KB** instead of **100 MB** during rolling deployments!

---

#### 3. Complete Production Example

**`pom.xml` (Enable Layered JAR in Spring Boot Maven Plugin — enabled by default in Spring Boot 2.4+ / 3.x):**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <layers>
                    <enabled>true</enabled>
                </layers>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**`.dockerignore` (Place in project root to keep build context small and cache stable):**
```dockerignore
.git
.gitignore
target/
build/
*.iml
.idea/
.vscode/
README.md
docker-compose*.yml
.env
```

**`Dockerfile` (Production-Ready Multi-Stage + Layered JAR + Non-Root + JVM Container Tuning):**
```dockerfile
# =========================================================================
# STAGE 1: Build the application and extract Spring Boot layers
# =========================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# 1. Copy only Maven wrapper & pom.xml first (cached unless pom.xml changes)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 2. Copy application source and build the fat JAR
COPY src ./src
RUN ./mvnw package -DskipTests -B

# 3. Extract the fat JAR into the 4 Spring Boot layers
#    (Note: Spring Boot 3.3+ also supports -Djarmode=tools extract --layers --launcher)
RUN java -Djarmode=layertools -jar target/*.jar extract --destination /workspace/extracted

# =========================================================================
# STAGE 2: Minimal, hardened JRE runtime image
# =========================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# 1. Create a non-root system user and group for security
RUN groupadd --system --gid 10001 appgroup && \
    useradd  --system --uid 10001 --gid appgroup --shell /sbin/nologin appuser

# 2. Copy extracted layers from least-frequently to most-frequently changing
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/application/ ./

# 3. Switch to non-root user
USER 10001:10001

# 4. Container-aware JVM flags (70% heap ceiling, exit on OOM for K8s restart)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=70.0 \
    -XX:InitialRAMPercentage=70.0 \
    -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# 5. Use JarLauncher (exec form so Java is PID 1 and receives SIGTERM)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```
*(Note: In Spring Boot **3.2+**, the launcher package moved from `org.springframework.boot.loader.JarLauncher` to `org.springframework.boot.loader.launch.JarLauncher`.)*

**Inspecting the layers locally before building the image:**
```bash
# List the layers inside your built JAR
java -Djarmode=layertools -jar target/order-service-1.0.0.jar list
# Output:
# dependencies
# spring-boot-loader
# snapshot-dependencies
# application
```

### Q5. Why should containers never run as `root`, and how do you harden container security?

**Answer:** Because containers share the host kernel, a process running as `root` (UID 0) inside a container that escapes via a kernel or runtime vulnerability has `root` privileges on the host node.

**Hardening checklist:**
- Create a dedicated non-root user (`USER 10001`) in the Dockerfile and enforce `runAsNonRoot: true` in Kubernetes `securityContext`.
- Use minimal or **Distroless** base images (no shell, no package manager, smaller attack surface).
- Mount the root filesystem as read-only (`readOnlyRootFilesystem: true`) and mount `emptyDir` on `/tmp` if needed.
- Drop all Linux capabilities (`drop: ["ALL"]`).
- Scan images in CI with Trivy/Grype and sign images with Cosign.

### Q6. How does Docker image layer caching work, and what invalidates the cache?

**Answer:** Each `RUN`, `COPY`, and `ADD` instruction creates an immutable layer. Docker reuses cached layers until an instruction changes:
- For `COPY`/`ADD`, Docker hashes the file contents; if any copied file changes, that layer and **all subsequent layers** are rebuilt.
- Putting `COPY . .` before dependency download breaks caching on every code edit. Always copy `pom.xml` / `build.gradle` first, download dependencies, and then copy `src/`.

### Q7. Explain Docker networking modes (`bridge`, `host`, `none`, `overlay`).

**Answer:**
- **`bridge` (default):** containers attach to a virtual bridge (`docker0` or user-defined bridge) with private IPs and NAT to the host. User-defined bridges provide automatic DNS resolution by container name.
- **`host`:** container shares the host's network namespace directly (no NAT, binds directly to host ports; lower isolation).
- **`none`:** loopback interface only; no external network access.
- **`overlay`:** multi-host virtual network (VXLAN) allowing containers across different nodes to communicate.

### Q8. What is the difference between Docker Volumes and Bind Mounts?

**Answer:**
- **Volumes:** managed by Docker (`/var/lib/docker/volumes/`); isolated from host directory layout, portable, safe to share across containers, and supported by volume drivers (EBS, NFS).
- **Bind mounts:** map a specific host file/directory path directly into the container; useful for local development (live reload) but tightly coupled to the host filesystem.

### Q9. How do signals (`SIGTERM` vs `SIGKILL`) work when stopping a container?

**Answer:** When `docker stop` or Kubernetes pod termination runs:
1. The runtime sends `SIGTERM` to **PID 1** inside the container.
2. It waits for the grace period (default 10s in Docker, 30s in Kubernetes `terminationGracePeriodSeconds`).
3. If the process is still running, the kernel sends `SIGKILL`, terminating it immediately.

If your Dockerfile uses shell form (`ENTRYPOINT java -jar app.jar`), `/bin/sh` is PID 1 and does not forward `SIGTERM` to the JVM, causing abrupt kills after 30s. Use exec form (`["java", ...]`) or an init process (`tini` / `--init`).

### Q10. How do you debug a distroless or minimal container that has no shell or debugging tools?

**Answer:**
- In Kubernetes 1.25+, use **ephemeral debug containers**:
  ```bash
  kubectl debug -it <pod-name> --image=busybox:latest --target=<container-name>
  ```
  This attaches a temporary container sharing the target container's process, network, and filesystem (`/proc/1/root`) namespaces without modifying the production image.
- Locally with Docker: attach another container to the same network/PID namespace (`docker run --rm -it --network container:<id> --pid container:<id> nicolaka/netshoot`).

---

## 2. Kubernetes Architecture & Core Workloads

### Q11. Explain the Kubernetes architecture (Control Plane vs Worker Nodes).

**Answer:**
- **Control Plane:**
  - **`kube-apiserver`:** front door for all REST commands (`kubectl`, controllers, kubelets); validates and persists state in `etcd`. Only component that talks directly to `etcd`.
  - **`etcd`:** distributed, strongly consistent (Raft) key-value store holding all cluster state.
  - **`kube-scheduler`:** watches for unscheduled Pods and assigns them to suitable Nodes based on resource requests, affinities, and taints.
  - **`kube-controller-manager`:** runs control loops (Deployment, ReplicaSet, Node, Endpoint controllers) that reconcile actual state toward desired state.
- **Worker Node:**
  - **`kubelet`:** node agent that watches Pod specs assigned to its node and instructs the container runtime (containerd/CRI-O) to start/stop containers.
  - **`kube-proxy`:** maintains network rules (iptables/IPVS/eBPF) on nodes to route traffic for Kubernetes `Service` virtual IPs to backend Pods.
  - **Container Runtime:** pulls images and runs containers.

### Q12. Why is a Pod the smallest deployable unit in Kubernetes instead of a single container?

**Answer:** A Pod represents a logical host for one or more tightly coupled containers that must be co-located, scheduled together on the same node, and share:
- The same network namespace (one IP address and port space; communicate over `localhost`).
- Shared storage volumes (`emptyDir`, `projected` volumes).

This enables helper patterns without polluting the main application image: **sidecars** (Envoy service mesh proxy, Fluent Bit log shipper), **ambassadors**, and **adapters**.

### Q13. What is the difference between a `Deployment`, a `StatefulSet`, and a `DaemonSet`?

**Answer:**

| Feature | `Deployment` | `StatefulSet` | `DaemonSet` |
|---|---|---|---|
| Use case | Stateless microservices (REST APIs, workers) | Stateful systems (Kafka, RabbitMQ, MySQL, Redis) | Node-level agents (log collectors, Prometheus node-exporter, CNI) |
| Pod identity | Random hash (`order-api-7f9d8-x2k`) | Sticky ordinal (`rabbitmq-0`, `rabbitmq-1`) | One Pod per matching node |
| Network DNS | Load-balanced Service | Stable per-pod DNS (`pod-0.svc.ns.svc.cluster.local`) via Headless Service | Usually node-local or hostPort |
| Storage | Shared PVC or stateless | Dedicated PVC per replica via `volumeClaimTemplates` | Host path / node metrics |
| Scaling/Updates | Parallel rolling updates | Ordered creation/termination (`0..N-1`) | Automatically added to new nodes |

### Q14. What are `Init Containers` vs `Sidecar Containers`, and when do you use each?

**Answer:**
- **Init Containers:** run to completion **sequentially before** any app container starts. If an init container fails, Kubernetes restarts it until it succeeds. Use for: waiting for dependencies, schema migrations, fetching certificates, or setting permissions on mounted volumes.
- **Sidecar Containers:** run **concurrently alongside** the main application container for the entire lifecycle of the Pod (in K8s 1.28+, native sidecars are init containers with `restartPolicy: Always`). Use for: mTLS proxies (Istio Envoy), log forwarding, metrics exporters, or Vault agent secret refresh.

### Q15. Explain the complete lifecycle of a Pod from `kubectl apply` to termination.

**Answer:**
1. `kubectl` sends the Pod/Deployment spec to `kube-apiserver`, which authenticates, authorizes, runs admission webhooks, and writes to `etcd` (`Pending`).
2. `kube-scheduler` picks a Node and binds the Pod.
3. The node's `kubelet` sees the assigned Pod, creates the network sandbox (via CNI), runs `initContainers` sequentially, then starts main containers (`Running`).
4. `kubelet` executes `startupProbe`, then `readinessProbe` (adds Pod IP to `EndpointSlice` when ready) and `livenessProbe`.
5. **On deletion:**
   - Pod state changes to `Terminating`.
   - In parallel: Endpoint controller removes the Pod IP from `Service`/`EndpointSlice` (stops new traffic), and `kubelet` runs the `preStop` hook inside the container, then sends `SIGTERM`.
   - After `terminationGracePeriodSeconds` (default 30s), if still alive, `kubelet` sends `SIGKILL` and cleans up.

### Q16. How do you ensure zero-downtime Pod termination in Kubernetes for a Spring Boot app?

**Answer:** When a Pod is deleted, Endpoint removal and `SIGTERM` happen **concurrently**, not sequentially. Ingress controllers or `kube-proxy` on other nodes may still route requests to the Pod for a few seconds after it receives `SIGTERM`.

**Solution:**
1. Enable graceful shutdown in Spring Boot so it stops accepting new requests and waits for in-flight requests/RabbitMQ listeners to finish:
   ```yaml
   server:
     shutdown: graceful
   spring:
     lifecycle:
       timeout-per-shutdown-phase: 20s
   ```
2. Add a small `preStop` sleep hook (5–10 seconds) so Kubernetes removes the Pod from `EndpointSlices` and load balancers *before* Spring Boot receives `SIGTERM`:
   ```yaml
   lifecycle:
     preStop:
       exec:
         command: ["sh", "-c", "sleep 5"]
   terminationGracePeriodSeconds: 35
   ```

### Q17. What are `Jobs` and `CronJobs` in Kubernetes, and how do you handle concurrency and retries?

**Answer:**
- **`Job`:** runs one or more Pods until a specified number of completions succeed (`completions`, `parallelism`). `backoffLimit` controls retries before marking the Job failed; `activeDeadlineSeconds` bounds total runtime.
- **`CronJob`:** schedules `Jobs` on a cron expression.
  - `concurrencyPolicy`: `Allow` (default), `Forbid` (skip next run if current hasn't finished — safest for batch/reconciliation jobs), or `Replace` (cancel running job and start a new one).
  - Always make job logic **idempotent** because node preemption can re-run a Pod.

### Q18. How do `Labels`, `Selectors`, and `Annotations` differ?

**Answer:**
- **Labels:** key-value pairs attached to objects (`app: order-service`, `tier: backend`) used to **identify, group, and select** subsets of objects via **Label Selectors** (used by Services, Deployments, NetworkPolicies). Indexed and constrained in length.
- **Annotations:** key-value metadata **not** used for selection; stores non-identifying operational data (Prometheus scrape paths, Ingress controller settings, Git commit SHA, Helm hooks).

### Q19. Explain `Node Affinity`, `Pod Anti-Affinity`, and `Topology Spread Constraints`.

**Answer:**
- **Node Affinity:** attracts Pods to specific nodes based on node labels (e.g., `node.kubernetes.io/instance-type`, GPU nodes). Can be hard (`requiredDuringSchedulingIgnoredDuringExecution`) or soft (`preferred...`).
- **Pod Anti-Affinity:** prevents replicas of the same service from landing on the same node or availability zone so a single node/AZ failure doesn't take down all instances.
- **Topology Spread Constraints:** controls how Pods are evenly distributed across failure domains (`topology.kubernetes.io/zone` or `kubernetes.io/hostname`) with a `maxSkew` (e.g., `maxSkew: 1` ensures zones have nearly identical replica counts).

```yaml
topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: topology.kubernetes.io/zone
    whenUnsatisfied: DoNotSchedule
    labelSelector:
      matchLabels:
        app: order-service
```

### Q20. What are `Taints` and `Tolerations`, and how do they differ from Node Affinity?

**Answer:**
- **Node Affinity** is a property of *Pods* that attracts them to a set of nodes.
- **Taints** are applied to *Nodes* to **repel** Pods unless the Pod has a matching **Toleration**.
- Taint effects:
  - `NoSchedule`: new Pods without a toleration will not be scheduled on the node.
  - `PreferNoSchedule`: soft version of `NoSchedule`.
  - `NoExecute`: also evicts already-running Pods that do not tolerate the taint.
- Common use: dedicate nodes to Kafka/RabbitMQ or high-CPU workloads by tainting the nodes **and** using Node Affinity so *only* those Pods land there.

---

## 3. Kubernetes Networking, Services & Ingress

### Q21. Explain the Kubernetes networking model and CNI.

**Answer:** Kubernetes enforces three fundamental rules via a **CNI (Container Network Interface)** plugin (Calico, Cilium, Flannel, AWS VPC CNI):
1. Every Pod gets its own unique IP address.
2. Pods on any node can communicate with all Pods on any other node **without NAT**.
3. Agents on a node (`kubelet`) can communicate with all Pods on that node.

### Q22. Compare Kubernetes Service types: `ClusterIP`, `NodePort`, `LoadBalancer`, and `Headless Service`.

**Answer:**
- **`ClusterIP` (default):** allocates a stable internal virtual IP reachable only inside the cluster; load-balances traffic across matching Pods.
- **`NodePort`:** exposes the Service on a static port (`30000–32767`) on every node's IP, forwarding to the `ClusterIP`.
- **`LoadBalancer`:** provisions an external cloud load balancer (AWS NLB/ALB, GCP LB) that routes external traffic to the nodes/Pods.
- **`Headless Service` (`clusterIP: None`):** does not allocate a virtual IP or load-balance. DNS queries return the **individual Pod A-records** directly. Used by `StatefulSets` (RabbitMQ/Kafka cluster discovery) and client-side load balancers.

### Q23. How does DNS service discovery work inside Kubernetes (`CoreDNS`)?

**Answer:** `CoreDNS` watches the API server and creates DNS records for Services and Pods:
- Full Service FQDN: `<service-name>.<namespace>.svc.cluster.local`
- Within the same namespace, a microservice can simply call `http://payment-service:8080`.
- Across namespaces: `http://payment-service.payments-prod.svc.cluster.local:8080`.
- Pitfall (`ndots:5` default in `/etc/resolv.conf`): any external hostname with fewer than 5 dots (like `api.stripe.com`) triggers up to 4 internal search-domain DNS lookups before resolving externally. Set `dnsConfig.options: [{name: ndots, value: "2"}]` if external API call latency/DNS load is high.

### Q24. What is the difference between a Kubernetes `Service` (`LoadBalancer`), `Ingress`, and the `Gateway API`?

**Answer:**
- **`Service (type: LoadBalancer)`:** operates at L4 (TCP/UDP); typically creates one expensive cloud load balancer per Service.
- **`Ingress`:** operates at L7 (HTTP/HTTPS); a single external load balancer fronts an Ingress Controller (NGINX, Traefik, AWS ALB) that routes traffic to many internal `ClusterIP` Services based on hostnames (`api.company.com`) and URL paths (`/orders`, `/payments`), handling TLS termination.
- **`Gateway API`:** the modern successor to Ingress; provides role-oriented resources (`GatewayClass`, `Gateway`, `HTTPRoute`) with built-in support for header routing, traffic splitting (canary weights), cross-namespace routing, and gRPC without vendor-specific annotations.

### Q25. What are `NetworkPolicies`, and how do you implement zero-trust network segmentation between microservices?

**Answer:** By default in Kubernetes, **all Pods can talk to all other Pods** in any namespace. A `NetworkPolicy` is an L3/L4 firewall enforced by the CNI (Calico/Cilium) using Pod label selectors and namespace selectors.

**Zero-trust best practice:**
1. Apply a **default-deny all** ingress and egress policy in every namespace.
2. Explicitly whitelist only required communication paths (e.g., allow `order-service` to talk to `payment-service` on port `8080` and to `CoreDNS` on port `53`).

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-order-to-payment
  namespace: prod
spec:
  podSelector:
    matchLabels:
      app: payment-service
  policyTypes: [Ingress]
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: order-service
      ports:
        - protocol: TCP
          port: 8080
```

### Q26. How does a Service Mesh (Istio / Linkerd) work, and when do you actually need one?

**Answer:** A service mesh injects a lightweight L7 proxy (Envoy or `linkerd2-proxy`) as a sidecar into every Pod (or per-node in ambient mode). All inbound and outbound traffic flows through the proxy (**data plane**), configured by the **control plane**.

**Capabilities without changing application code:**
- Automatic **mTLS** encryption and workload identity (SPIFFE) between services.
- Fine-grained L7 authorization policies (e.g., allow `GET /orders` from `billing-service`, deny `DELETE`).
- Traffic shifting (canary 5%/95%), traffic mirroring, timeouts, retries, and circuit breaking.
- Uniform golden-signal metrics (RED) and distributed trace spans.

**When you need it:** large polyglot platforms, strict zero-trust mTLS compliance, or complex traffic shaping. For a small set of Spring Boot services, Kubernetes `NetworkPolicies` + Spring Cloud / Resilience4j + Micrometer is much simpler to operate.

---

## 4. Configuration, Secrets & Storage

### Q27. What is the difference between `ConfigMaps` and `Secrets`, and how are they injected into Pods?

**Answer:**
- **`ConfigMap`:** stores non-sensitive configuration (application.yml, URLs, feature flags).
- **`Secret`:** stores sensitive data (passwords, TLS certs, OAuth keys). Note: Secrets are only **base64-encoded** in manifests, not encrypted by default!
- **Ways to consume:**
  1. Environment variables (`env` / `envFrom`).
  2. Mounted files in a volume.
- **Note on updates:** Mounted volume files are updated automatically by `kubelet` (within ~1 minute) when a ConfigMap/Secret changes (unless `subPath` is used!), whereas environment variables are **never updated** without restarting the Pod.

### Q28. How do you manage Secrets securely in production Kubernetes (GitOps-safe)?

**Answer:** Never commit plain Kubernetes `Secret` YAML files to Git.
1. **Enable encryption at rest** for `etcd` using KMS provider (AWS KMS, Azure Key Vault, GCP KMS).
2. **External Secrets Operator (ESO)** or **Secrets Store CSI Driver:** store secrets in AWS Secrets Manager / HashiCorp Vault / Azure Key Vault; ESO syncs them into Kubernetes Secrets automatically.
3. **Sealed Secrets / SOPS:** if storing encrypted secrets in Git for GitOps (ArgoCD/Flux), encrypt with a cluster-held private key.
4. Restrict RBAC `get/list/watch` on `secrets` so developers and most Pods cannot read other services' secrets.

### Q29. Explain Persistent Volumes (`PV`), Persistent Volume Claims (`PVC`), and `StorageClasses`.

**Answer:**
- **`PersistentVolume (PV)`:** a piece of actual storage in the cluster (AWS EBS, NFS, Ceph).
- **`PersistentVolumeClaim (PVC)`:** a Pod's request for storage (size and access mode: `ReadWriteOnce` single node, `ReadOnlyMany`, or `ReadWriteMany` across nodes).
- **`StorageClass`:** enables **dynamic provisioning** via CSI drivers so administrators don't pre-create PVs; when a PVC requests `storageClassName: gp3`, the CSI provisioner creates the cloud disk and PV automatically.
- `reclaimPolicy`: `Retain` (keep disk when PVC is deleted — safest for production DBs/queues) vs `Delete` (delete underlying disk).

---

## 5. Scaling, Probes, Resource Management & JVM Tuning

### Q30. Explain Resource `requests` vs `limits` and QoS classes (`Guaranteed`, `Burstable`, `BestEffort`).

**Answer:**
- **`requests`:** what the container is guaranteed to get; used by `kube-scheduler` to place the Pod on a node.
- **`limits`:** hard ceiling the container cannot exceed:
  - **CPU is compressible:** exceeding CPU limit causes **CFS throttling** (latency spikes, not crash).
  - **Memory is incompressible:** exceeding memory limit causes the Linux kernel OOM killer to kill the container (**`OOMKilled`**, exit code `137`).
- **QoS Classes** (determine eviction order when a node runs out of memory):
  1. **`Guaranteed`:** `requests == limits` for both CPU and memory on every container. Evicted last.
  2. **`Burstable`:** at least one container has a request or limit set, and `requests < limits`.
  3. **`BestEffort`:** no requests or limits set at all. Evicted first.

### Q31. How should you size JVM memory inside a Kubernetes container to avoid `OOMKilled` (Exit Code 137)?

**Answer:** A JVM uses much more than just the **Heap**: Metaspace, thread stacks (`~1 MB` per platform thread), direct byte buffers (Netty/NIO), GC structures, and native C libraries all consume container memory. If you set `-Xmx2g` inside a container with a `2Gi` memory limit, the Pod will inevitably be **`OOMKilled`**.

**Best practices (Java 17/21):**
1. The JVM is container-aware (`-XX:+UseContainerSupport` is on by default).
2. Set **`-XX:MaxRAMPercentage=70.0`** (or explicit `-Xms` == `-Xmx` at ~70–75% of container memory limit) and `-XX:MaxMetaspaceSize=256m`, leaving 25–30% headroom for non-heap memory.
3. For steady-state production services, set memory `requests == limits` (Guaranteed memory) so the node never overcommits memory.
4. Many teams avoid strict **CPU limits** (or set a higher limit) for Java services because JVM startup and GC threads need short bursts of CPU; aggressive CPU limits cause high tail latency due to throttling.

```yaml
resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "2000m"
    memory: "1Gi"
env:
  - name: JAVA_TOOL_OPTIONS
    value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError"
```

### Q32. Compare `startupProbe`, `readinessProbe`, and `livenessProbe` for a Spring Boot application.

**Answer:**
- **`startupProbe`:** checks if the application has finished starting. Disables liveness and readiness checks until it succeeds. Crucial for Spring Boot apps that take 20–60s to start so `livenessProbe` doesn't kill them during startup.
- **`readinessProbe`:** checks if the Pod is ready to accept traffic (`/actuator/health/readiness`). If it fails, the Pod is **removed from Service endpoints** (not restarted).
- **`livenessProbe`:** checks if the process is deadlocked/broken (`/actuator/health/liveness`). If it fails, `kubelet` **restarts the container**.

**Critical rule:** Never check external dependencies (DB, RabbitMQ, downstream REST APIs) inside a **`livenessProbe`**. If the database has a brief hiccup, checking it in liveness will restart all your Pods simultaneously, turning a minor hiccup into a full outage.

```yaml
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  failureThreshold: 30
  periodSeconds: 2
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  periodSeconds: 5
  failureThreshold: 3
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  periodSeconds: 10
  failureThreshold: 3
```

### Q33. How do `HPA` (Horizontal Pod Autoscaler), `VPA` (Vertical Pod Autoscaler), and `KEDA` work?

**Answer:**
- **`HPA`:** scales the number of Pod replicas (`minReplicas` to `maxReplicas`) based on CPU, memory, or custom metrics:
  $$\text{desiredReplicas} = \lceil \text{currentReplicas} \times (\text{currentMetric} / \text{targetMetric}) \rceil$$
- **`VPA`:** adjusts CPU/memory `requests` and `limits` based on historical usage (restarts Pods to apply). Do not run VPA and HPA on CPU/memory of the same Deployment at the same time.
- **`KEDA` (Kubernetes Event-Driven Autoscaling):** extends HPA to scale microservices based on **event sources** such as RabbitMQ queue depth, Kafka consumer lag, Redis lists, or Prometheus queries — including scaling down to `0` when queues are empty.
- **`Cluster Autoscaler` / `Karpenter`:** adds/removes underlying worker Nodes when Pods are `Pending` due to insufficient cluster capacity.

### Q34. What is a `PodDisruptionBudget` (PDB), and why is it essential for high availability?

**Answer:** A `PodDisruptionBudget` protects a service against **voluntary disruptions** (node drains, cluster upgrades, autoscaler scale-down) by ensuring a minimum number (`minAvailable`) or maximum unavailable (`maxUnavailable`) of replicas remain running at all times. During `kubectl drain`, the eviction API blocks draining a node if doing so would violate the PDB.

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: order-service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: order-service
```

---

## 6. Microservice Deployment Strategies, CI/CD & Production Troubleshooting

### Q35. Compare Rolling Update, Recreate, Blue-Green, Canary, and Shadow deployments.

**Answer:**

| Strategy | How it works | Pros | Cons / Risks |
|---|---|---|---|
| **Rolling Update** (K8s default) | Gradually replaces old Pods with new Pods (`maxSurge`, `maxUnavailable`). | Zero downtime, no extra infra cost. | v1 and v2 run simultaneously; rollback requires another rollout. |
| **Recreate** | Terminates all v1 Pods before starting v2 Pods. | Simple, never mixes versions. | Causes downtime. |
| **Blue-Green** | Spins up a full v2 environment (Green) alongside v1 (Blue); switches router/Service selector at once. | Instant traffic switch and instant rollback. | Requires 2x cluster resources during deployment; hard with incompatible DB changes. |
| **Canary** | Routes a small percentage (e.g., 5% -> 25% -> 100%) of live traffic to v2 while monitoring error rate/latency (Argo Rollouts / Flagger). | Lowest blast radius; automated rollback on SLO breach. | Requires Ingress/Gateway/Mesh traffic splitting and strong metrics. |
| **Shadow (Dark Launch)** | Mirrors a copy of live production requests to v2 asynchronously; discards v2 responses. | Tests v2 with real production load with zero user impact. | v2 must not trigger real side effects (payments/emails). |

### Q36. How do you configure a safe Rolling Update in a Kubernetes `Deployment`?

**Answer:**
- Set `replicas >= 3` spread across zones.
- Configure `maxUnavailable: 0` (never drop below desired capacity during rollout) and `maxSurge: 1` (or `25%`) so a new Pod must pass its `readinessProbe` before an old Pod is terminated.
- Pair with proper `readinessProbe` and `preStop` + graceful shutdown (Q16).

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

### Q37. How do you perform zero-downtime database schema migrations during microservice deployments?

**Answer:** During a Rolling, Blue-Green, or Canary deployment, **both v1 and v2 of the application run against the same database at the same time**. Any breaking schema change (renaming/dropping a column, adding a `NOT NULL` column without a default) will break v1 immediately and make rollback impossible.

Use the **Expand and Contract (Parallel Change)** pattern across multiple releases via Flyway/Liquibase:
1. **Expand (Release 1):** add the new nullable column/table; deploy v2 code that **dual-writes** to both old and new columns and reads from the old (or falls back).
2. **Backfill:** migrate existing historical rows in small batches.
3. **Switch reads (Release 2):** update code to read/write only the new column; stop writing to the old column.
4. **Contract (Release 3):** once v1 is completely gone and rollback is no longer needed, drop the old column.

### Q38. What is `Helm` vs `Kustomize`, and when do you use each?

**Answer:**
- **`Helm`:** the Kubernetes package manager; uses Go-templated charts (`Chart.yaml`, `values.yaml`, `templates/`) with release versioning (`helm upgrade --install`, `helm rollback`). Best for packaging reusable microservices or installing third-party infrastructure (RabbitMQ, Prometheus, Ingress-NGINX).
- **`Kustomize`:** built into `kubectl` (`kubectl apply -k`); uses template-free YAML overlays (`base/` + `overlays/dev`, `overlays/prod`) to patch environment differences. Best when you want pure, readable YAML without complex template logic.
- Many teams combine both: a single standardized **base Helm chart** for all internal Spring Boot microservices, with per-environment `values-dev.yaml` / `values-prod.yaml`.

### Q39. What is GitOps (ArgoCD / Flux), and why is pull-based deployment preferred over push-based CI?

**Answer:**
- **Push-based CI/CD:** Jenkins/GitHub Actions runs `kubectl apply` or `helm upgrade` directly against the cluster. Drawbacks: CI holds cluster-admin credentials, manual `kubectl` changes cause configuration drift, and there is no automatic self-healing.
- **GitOps (Pull-based):** Git is the **single source of truth** for desired cluster state. An agent running *inside* the cluster (ArgoCD or Flux) continuously compares the Git repo with live cluster state:
  - No cluster credentials stored in external CI pipelines (CI only builds/scans images and opens a PR updating the image tag in the GitOps repo).
  - Automatic drift detection and self-healing if someone edits the cluster manually.
  - Full audit trail and one-click rollback via `git revert`.

### Q40. Design an end-to-end production CI/CD pipeline for a containerized Spring Boot microservice.

**Answer:**
1. **Commit / PR stage (CI):**
   - Run unit tests, integration tests (**Testcontainers** for MySQL/RabbitMQ), and static analysis (SpotBugs/SonarQube).
   - Check dependency vulnerabilities (OWASP Dependency-Check / Snyk).
2. **Build & Package stage:**
   - Build layered OCI image (multi-stage Dockerfile or Paketo Buildpacks).
   - Tag image with immutable **Git commit SHA** (never deploy `:latest` in production!).
3. **Container Security & Signing:**
   - Scan image with **Trivy** (fail pipeline on CRITICAL/HIGH CVEs).
   - Generate SBOM (Syft) and sign image digest with **Cosign**.
   - Push to private container registry.
4. **Deploy to Staging & Contract Testing:**
   - Update staging GitOps manifest with the new image digest; ArgoCD syncs.
   - Run consumer-driven contract tests (Pact / Spring Cloud Contract) and smoke tests.
5. **Production Rollout (Progressive Delivery):**
   - Promote digest to production branch via approved PR.
   - **Argo Rollouts** performs a Canary release (5% -> 25% -> 50% -> 100%), querying Prometheus metrics (HTTP 5xx rate, p99 latency); automatically aborts and rolls back if thresholds are breached.

### Q41. How do you troubleshoot a Pod stuck in `Pending`, `ImagePullBackOff`, or `CrashLoopBackOff`?

**Answer:**
- **`Pending` (not scheduled yet):**
  - Run `kubectl describe pod <pod>` and check `Events`.
  - Common causes: insufficient node CPU/memory (`0/5 nodes available: Insufficient memory`), unsatisfied `nodeSelector`/affinity, untolerated node taints, or an unbound `PVC` waiting for storage provisioning.
- **`ImagePullBackOff` / `ErrImagePull`:**
  - Wrong image name/tag, private registry authentication missing (`imagePullSecrets`), registry rate limit (Docker Hub), or worker node has no network route to the registry.
- **`CrashLoopBackOff` (container starts and exits repeatedly):**
  - Check exit code and termination reason in `kubectl describe pod <pod>` (`Last State: Terminated, Exit Code: ...`).
  - Read logs of the crashed instance: `kubectl logs <pod> --previous`.
  - Common causes:
    - Exit code `1`: unhandled Java exception on startup (missing env var/Secret, DB connection refused, Flyway migration error).
    - Exit code `137`: `OOMKilled` (memory limit too low or `-Xmx` too high) or aggressive `livenessProbe` killing the app before it finishes starting (add a `startupProbe`).
    - Exit code `126`/`127`: wrong `ENTRYPOINT` binary path or file permissions.
- **`CreateContainerConfigError`:**
  - Referenced `ConfigMap` or `Secret` (or a specific key in it) does not exist in the namespace.

### Q42. A microservice works fine on your laptop and inside the Pod (`localhost`), but returns `502`/`503` or times out through the Service or Ingress. How do you debug it?

**Answer:** Walk the network path layer by layer:
1. **Pod listener:** verify the app binds to `0.0.0.0:8080` (not `127.0.0.1:8080`) and `readinessProbe` is passing (`READY 1/1`).
2. **Service -> Pod selector & port:**
   - Run `kubectl get endpoints <svc>` (or `kubectl get endpointslices`).
   - If `ENDPOINTS` is `<none>`, the Service's `selector` labels do not match the Pod's labels, or the Pod is not `Ready`.
   - Verify Service `targetPort` matches the container's actual listening port.
3. **NetworkPolicy:** check if a `NetworkPolicy` in the namespace is blocking ingress from the Ingress controller namespace or caller Pod (`kubectl get netpol`).
4. **Ingress / Gateway:** verify host, path, rewrite annotations, TLS secret, and that the Ingress points to the right Service name and port; inspect Ingress controller logs.
5. **mTLS / Service Mesh:** if Istio/Linkerd is enabled, check for `PeerAuthentication` (`STRICT` mTLS vs plaintext) or `AuthorizationPolicy` rejections.
