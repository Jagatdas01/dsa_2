# Database & SQL Queries: Interview Question Set

Target: 6 to 10 years experience. Queries are written for MySQL 8 / PostgreSQL / Oracle 12c+ (differences noted where relevant).

## Index

1. Sample Schema
2. SQL Fundamentals (Q1 to Q6)
3. GROUP BY and HAVING (Q7 to Q12)
4. Sorting, Pagination and Top-N (Q13 to Q18)
5. Window Functions (Q19 to Q23)
6. Joins, Subqueries and Duplicates (Q24 to Q30)
7. Performance (Q31 to Q34)

---

## 1. Sample Schema

All queries below use these tables.

```sql
CREATE TABLE department (
    dept_id    INT PRIMARY KEY,
    dept_name  VARCHAR(50) NOT NULL
);

CREATE TABLE employee (
    emp_id      INT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    dept_id     INT REFERENCES department(dept_id),
    manager_id  INT REFERENCES employee(emp_id),
    salary      DECIMAL(10,2),
    hire_date   DATE,
    email       VARCHAR(100)
);

CREATE TABLE customer (
    customer_id INT PRIMARY KEY,
    name        VARCHAR(100),
    city        VARCHAR(50)
);

CREATE TABLE orders (
    order_id    INT PRIMARY KEY,
    customer_id INT REFERENCES customer(customer_id),
    order_date  DATE,
    amount      DECIMAL(10,2),
    status      VARCHAR(20)          -- NEW, PAID, SHIPPED, CANCELLED
);
```

---

## 2. SQL Fundamentals

### Q1. What is the logical order of execution of a SQL query?

**Answer:** SQL is written in one order but evaluated in another:

```
FROM / JOIN  ->  WHERE  ->  GROUP BY  ->  HAVING  ->  SELECT  ->  DISTINCT  ->  ORDER BY  ->  LIMIT / OFFSET
```

Consequences:
- You cannot use a `SELECT` alias in `WHERE` (it does not exist yet), but you can use it in `ORDER BY`.
- You cannot use aggregate functions in `WHERE`; use `HAVING`.
- Window functions are computed in the `SELECT` phase, so you cannot filter on them in `WHERE`; wrap them in a subquery/CTE.

### Q2. What is the difference between `WHERE` and `HAVING`?

**Answer:** `WHERE` filters **rows before grouping** and cannot use aggregates. `HAVING` filters **groups after aggregation** and can use aggregates. Put non-aggregate conditions in `WHERE` so fewer rows are grouped (faster).

```sql
SELECT dept_id, AVG(salary) AS avg_salary
FROM employee
WHERE hire_date >= DATE '2020-01-01'     -- row filter
GROUP BY dept_id
HAVING AVG(salary) > 50000;              -- group filter
```

### Q3. What is the difference between `COUNT(*)`, `COUNT(1)`, `COUNT(column)` and `COUNT(DISTINCT column)`?

**Answer:**
- `COUNT(*)` / `COUNT(1)`: count all rows, including rows with `NULL`s. They perform the same in modern databases.
- `COUNT(column)`: counts rows where `column IS NOT NULL`.
- `COUNT(DISTINCT column)`: counts unique non-null values.

```sql
SELECT COUNT(*)                AS total_employees,
       COUNT(manager_id)       AS employees_with_manager,
       COUNT(DISTINCT dept_id) AS departments_used
FROM employee;
```

### Q4. What is the difference between `DELETE`, `TRUNCATE` and `DROP`?

**Answer:**

| | `DELETE` | `TRUNCATE` | `DROP` |
|---|---|---|---|
| Type | DML | DDL | DDL |
| `WHERE` allowed | Yes | No (all rows) | N/A |
| Rollback | Yes | Not in MySQL/Oracle (implicit commit); yes in PostgreSQL | Not in MySQL/Oracle |
| Triggers fire | Yes | No | No |
| Speed | Slow (row-by-row, logged) | Fast (deallocates pages) | Fast |
| Structure kept | Yes | Yes (resets auto-increment) | No, table removed |

### Q5. What is the difference between `UNION` and `UNION ALL`?

**Answer:** `UNION` removes duplicates (requires a sort/hash, slower). `UNION ALL` keeps all rows and is faster. Use `UNION ALL` unless you really need deduplication. Both require the same number of columns with compatible types.

```sql
SELECT city FROM customer WHERE city LIKE 'A%'
UNION ALL
SELECT city FROM customer WHERE city LIKE 'B%';
```

### Q6. How do `NULL` values behave in comparisons, `NOT IN`, and aggregates?

**Answer:**
- `NULL = NULL` is `UNKNOWN`, not true. Use `IS NULL` / `IS NOT NULL`.
- Aggregates (`SUM`, `AVG`, `MAX`, `COUNT(col)`) ignore `NULL`s. `AVG(salary)` divides only by non-null rows.
- **Trap:** `NOT IN (subquery)` returns **no rows** if the subquery contains a `NULL`. Use `NOT EXISTS` instead.
- Use `COALESCE(col, default)` to replace nulls.

```sql
-- Returns nothing if any manager_id is NULL!
SELECT * FROM employee WHERE emp_id NOT IN (SELECT manager_id FROM employee);

-- Correct: employees who are not managers
SELECT * FROM employee e
WHERE NOT EXISTS (SELECT 1 FROM employee x WHERE x.manager_id = e.emp_id);
```

---

## 3. GROUP BY and HAVING

### Q7. Count employees and total/average/max salary per department.

```sql
SELECT d.dept_name,
       COUNT(e.emp_id)       AS emp_count,
       SUM(e.salary)         AS total_salary,
       ROUND(AVG(e.salary),2) AS avg_salary,
       MAX(e.salary)         AS max_salary
FROM department d
LEFT JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_name
ORDER BY emp_count DESC;
```

**Note:** `LEFT JOIN` + `COUNT(e.emp_id)` shows departments with 0 employees. `COUNT(*)` would wrongly return 1 for them.

### Q8. Why does `SELECT name, dept_id, MAX(salary) FROM employee GROUP BY dept_id` fail (or give wrong results)?

**Answer:** Every non-aggregated column in `SELECT` must be in `GROUP BY`. `name` is not, so the database does not know which name to return. Oracle/PostgreSQL and MySQL with `ONLY_FULL_GROUP_BY` (default since 5.7) raise an error; old MySQL returned an arbitrary name, **not** the name of the highest-paid employee. To get the employee row with the max salary, use a window function (Q20) or a join to the grouped result.

```sql
SELECT e.name, e.dept_id, e.salary
FROM employee e
JOIN (SELECT dept_id, MAX(salary) AS max_sal
      FROM employee GROUP BY dept_id) m
  ON e.dept_id = m.dept_id AND e.salary = m.max_sal;
```

### Q9. Find departments having more than 5 employees.

```sql
SELECT dept_id, COUNT(*) AS emp_count
FROM employee
GROUP BY dept_id
HAVING COUNT(*) > 5;
```

### Q10. Find total sales per month (group by year and month).

```sql
-- MySQL
SELECT DATE_FORMAT(order_date, '%Y-%m') AS month, SUM(amount) AS total
FROM orders
WHERE status <> 'CANCELLED'
GROUP BY DATE_FORMAT(order_date, '%Y-%m')
ORDER BY month;

-- PostgreSQL / Oracle
SELECT TO_CHAR(order_date, 'YYYY-MM') AS month, SUM(amount) AS total
FROM orders
WHERE status <> 'CANCELLED'
GROUP BY TO_CHAR(order_date, 'YYYY-MM')
ORDER BY month;
```

### Q11. How do you pivot rows into columns (conditional aggregation)?

**Answer:** Use `SUM`/`COUNT` with `CASE` inside `GROUP BY`. Works in all databases.

```sql
SELECT customer_id,
       COUNT(*)                                              AS total_orders,
       SUM(CASE WHEN status = 'PAID'      THEN 1 ELSE 0 END) AS paid,
       SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled,
       SUM(CASE WHEN status = 'PAID' THEN amount ELSE 0 END) AS paid_amount
FROM orders
GROUP BY customer_id;
```

### Q12. What do `ROLLUP` and `GROUPING SETS` do?

**Answer:** They produce subtotals and grand totals in one query.

```sql
-- PostgreSQL / Oracle: subtotal per dept + grand total
SELECT dept_id, EXTRACT(YEAR FROM hire_date) AS yr, SUM(salary)
FROM employee
GROUP BY ROLLUP (dept_id, EXTRACT(YEAR FROM hire_date));

-- MySQL syntax
SELECT dept_id, YEAR(hire_date) AS yr, SUM(salary)
FROM employee
GROUP BY dept_id, YEAR(hire_date) WITH ROLLUP;
```

Rows where `dept_id`/`yr` is `NULL` are the subtotal/grand-total rows (use `GROUPING()` to distinguish them from real nulls).

---

## 4. Sorting, Pagination and Top-N

### Q13. How do you sort by multiple columns and in different directions?

```sql
SELECT name, dept_id, salary
FROM employee
ORDER BY dept_id ASC, salary DESC, name ASC;
```

Rows are sorted by `dept_id`; ties are sorted by `salary` descending; remaining ties by `name`. Without `ORDER BY`, row order is **never guaranteed**.

### Q14. How are `NULL`s sorted, and how do you control it?

**Answer:** MySQL treats `NULL` as smallest (first in `ASC`). PostgreSQL and Oracle treat it as largest (last in `ASC`).

```sql
-- PostgreSQL / Oracle
SELECT name, salary FROM employee ORDER BY salary DESC NULLS LAST;

-- MySQL (no NULLS LAST): sort by "is null" flag first
SELECT name, salary FROM employee ORDER BY salary IS NULL, salary DESC;
```

### Q15. How do you sort with a custom order (e.g. status NEW, PAID, SHIPPED, CANCELLED)?

```sql
SELECT * FROM orders
ORDER BY CASE status
           WHEN 'NEW'       THEN 1
           WHEN 'PAID'      THEN 2
           WHEN 'SHIPPED'   THEN 3
           WHEN 'CANCELLED' THEN 4
           ELSE 5
         END,
         order_date DESC;

-- MySQL shortcut
SELECT * FROM orders ORDER BY FIELD(status, 'NEW','PAID','SHIPPED','CANCELLED');
```

### Q16. Find the second highest salary.

```sql
-- 1. Subquery (works everywhere, returns NULL if none)
SELECT MAX(salary) AS second_highest
FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- 2. LIMIT/OFFSET (MySQL / PostgreSQL)
SELECT DISTINCT salary FROM employee
ORDER BY salary DESC
LIMIT 1 OFFSET 1;

-- 3. Oracle 12c+ / PostgreSQL / SQL standard
SELECT DISTINCT salary FROM employee
ORDER BY salary DESC
OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY;
```

`DISTINCT` matters: without it, two employees sharing the top salary would make the "second highest" equal to the highest.

### Q17. Find the Nth highest salary (generic).

```sql
SELECT salary
FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
    FROM employee
) t
WHERE rnk = :n
FETCH FIRST 1 ROWS ONLY;      -- MySQL: LIMIT 1
```

### Q18. How do you implement pagination, and why is `OFFSET` slow for deep pages?

**Answer:** `LIMIT 20 OFFSET 100000` makes the database read and discard 100,000 rows, so deep pages get slower. Use **keyset (seek) pagination**: remember the last seen sort key and continue from it. It uses the index and is constant time. Always include a unique tie-breaker column in the sort.

```sql
-- Offset pagination (simple, slow for deep pages)
SELECT order_id, order_date, amount FROM orders
ORDER BY order_date DESC, order_id DESC
LIMIT 20 OFFSET 40;

-- Keyset pagination (fast); pass last row's values from the previous page
SELECT order_id, order_date, amount FROM orders
WHERE (order_date, order_id) < (:last_date, :last_id)
ORDER BY order_date DESC, order_id DESC
LIMIT 20;
```

---

## 5. Window Functions

### Q19. What is the difference between `ROW_NUMBER`, `RANK` and `DENSE_RANK`?

**Answer:** For salaries `100, 90, 90, 80`:

| salary | ROW_NUMBER | RANK | DENSE_RANK |
|---|---|---|---|
| 100 | 1 | 1 | 1 |
| 90 | 2 | 2 | 2 |
| 90 | 3 | 2 | 2 |
| 80 | 4 | **4** | **3** |

- `ROW_NUMBER`: unique sequence, ties broken arbitrarily (add a tie-breaker).
- `RANK`: ties share a rank, then gaps.
- `DENSE_RANK`: ties share a rank, no gaps. Use it for "Nth highest" questions.

**GROUP BY vs window function:** `GROUP BY` collapses rows into one per group; a window function (`OVER (PARTITION BY ...)`) keeps every row and adds the computed value.

### Q20. Find the highest-paid employee(s) in each department.

```sql
SELECT dept_id, name, salary
FROM (
    SELECT e.*, RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk
    FROM employee e
) t
WHERE rnk = 1;
```

### Q21. Find the top 3 salaries in each department.

```sql
WITH ranked AS (
    SELECT dept_id, name, salary,
           DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk
    FROM employee
)
SELECT dept_id, name, salary, rnk
FROM ranked
WHERE rnk <= 3
ORDER BY dept_id, rnk;
```

### Q22. Calculate a running total and each row's percentage of the total.

```sql
SELECT order_id, order_date, amount,
       SUM(amount) OVER (ORDER BY order_date, order_id)                 AS running_total,
       SUM(amount) OVER (PARTITION BY customer_id ORDER BY order_date)   AS customer_running_total,
       ROUND(100 * amount / SUM(amount) OVER (), 2)                      AS pct_of_total
FROM orders;
```

### Q23. Compare each order with the previous one (`LAG` / `LEAD`).

```sql
SELECT customer_id, order_date, amount,
       LAG(amount)  OVER (PARTITION BY customer_id ORDER BY order_date) AS prev_amount,
       amount - LAG(amount) OVER (PARTITION BY customer_id ORDER BY order_date) AS diff,
       LEAD(order_date) OVER (PARTITION BY customer_id ORDER BY order_date) AS next_order_date
FROM orders;
```

---

## 6. Joins, Subqueries and Duplicates

### Q24. Explain the types of joins.

**Answer:**
- `INNER JOIN`: only matching rows in both tables.
- `LEFT JOIN`: all rows from the left table + matches (or `NULL`s) from the right.
- `RIGHT JOIN`: opposite of left.
- `FULL OUTER JOIN`: all rows from both (not supported in MySQL; emulate with `LEFT JOIN UNION RIGHT JOIN`).
- `CROSS JOIN`: Cartesian product.
- `SELF JOIN`: a table joined with itself (e.g. employee and manager).

**Trap:** a condition on the right table in `WHERE` turns a `LEFT JOIN` into an `INNER JOIN`. Put it in the `ON` clause instead.

```sql
-- All customers, with only their PAID orders (customers with none still shown)
SELECT c.name, o.order_id
FROM customer c
LEFT JOIN orders o ON o.customer_id = c.customer_id AND o.status = 'PAID';
```

### Q25. Find employees who earn more than their manager.

```sql
SELECT e.name AS employee, e.salary, m.name AS manager, m.salary AS manager_salary
FROM employee e
JOIN employee m ON e.manager_id = m.emp_id
WHERE e.salary > m.salary;
```

### Q26. Find customers who have never placed an order.

```sql
-- NOT EXISTS (preferred, NULL-safe)
SELECT c.* FROM customer c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id);

-- LEFT JOIN / IS NULL (anti-join)
SELECT c.* FROM customer c
LEFT JOIN orders o ON o.customer_id = c.customer_id
WHERE o.order_id IS NULL;
```

### Q27. Find employees whose salary is above their department's average.

```sql
-- Correlated subquery
SELECT e.name, e.dept_id, e.salary
FROM employee e
WHERE e.salary > (SELECT AVG(x.salary) FROM employee x WHERE x.dept_id = e.dept_id);

-- Window function (single scan)
SELECT name, dept_id, salary
FROM (
    SELECT e.*, AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg
    FROM employee e
) t
WHERE salary > dept_avg;
```

### Q28. Find duplicate records (e.g. duplicate emails).

```sql
SELECT email, COUNT(*) AS cnt
FROM employee
GROUP BY email
HAVING COUNT(*) > 1;
```

### Q29. Delete duplicate rows, keeping only the one with the smallest id.

```sql
-- PostgreSQL / Oracle / MySQL 8
DELETE FROM employee
WHERE emp_id IN (
    SELECT emp_id FROM (
        SELECT emp_id,
               ROW_NUMBER() OVER (PARTITION BY email ORDER BY emp_id) AS rn
        FROM employee
    ) t
    WHERE rn > 1
);

-- MySQL self-join alternative
DELETE e1 FROM employee e1
JOIN employee e2 ON e1.email = e2.email AND e1.emp_id > e2.emp_id;
```

The extra derived table in the first query is needed in MySQL, which does not allow selecting from the table being deleted directly. Afterwards add a `UNIQUE` constraint to prevent new duplicates.

### Q30. Find customers who placed orders on 3 or more consecutive days (gaps-and-islands).

**Answer:** Subtracting `ROW_NUMBER` (in days) from the date gives the same value for every row in a consecutive run; group by that value.

```sql
-- PostgreSQL (in MySQL use DATE_SUB(order_date, INTERVAL rn DAY))
WITH days AS (
    SELECT DISTINCT customer_id, order_date FROM orders
),
grp AS (
    SELECT customer_id, order_date,
           order_date - CAST(ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY order_date) AS INT) AS grp_key
    FROM days
)
SELECT customer_id, MIN(order_date) AS start_date, MAX(order_date) AS end_date, COUNT(*) AS days
FROM grp
GROUP BY customer_id, grp_key
HAVING COUNT(*) >= 3;
```

---

## 7. Performance

### Q31. How can indexes help `ORDER BY` and `GROUP BY`?

**Answer:** If an index matches the `WHERE` equality columns followed by the `ORDER BY`/`GROUP BY` columns, the database reads rows already sorted and avoids a filesort / sort step. Column order in a composite index matters (leftmost prefix rule).

```sql
CREATE INDEX idx_orders_cust_date ON orders (customer_id, order_date);

-- Uses the index for both filtering and sorting
SELECT * FROM orders WHERE customer_id = 42 ORDER BY order_date DESC LIMIT 10;

-- Cannot use it for sorting efficiently (customer_id not fixed)
SELECT * FROM orders ORDER BY order_date;
```

### Q32. Why might a query not use an index?

**Answer:**
- Function on the indexed column: `WHERE YEAR(order_date) = 2025` — rewrite as a range `order_date >= '2025-01-01' AND order_date < '2026-01-01'`.
- Leading wildcard: `LIKE '%abc'`.
- Implicit type conversion: comparing a `VARCHAR` column to a number.
- Not using the leftmost column of a composite index.
- `OR` across different columns (consider `UNION ALL`).
- Low selectivity: the optimizer prefers a full scan when most rows match.
- Stale statistics: run `ANALYZE TABLE` (MySQL) / `ANALYZE` (PostgreSQL) / `DBMS_STATS` (Oracle).

Use `EXPLAIN` (MySQL/PostgreSQL) or `EXPLAIN PLAN` (Oracle) to check.

### Q33. What is a covering index?

**Answer:** An index that contains all columns a query needs, so the database answers from the index alone without reading the table rows ("Using index" in MySQL `EXPLAIN`, "Index Only Scan" in PostgreSQL).

```sql
CREATE INDEX idx_emp_dept_salary ON employee (dept_id, salary);

SELECT dept_id, MAX(salary) FROM employee GROUP BY dept_id;   -- index-only
```

### Q34. `EXISTS` vs `IN` vs `JOIN` — which is faster?

**Answer:** Modern optimizers often rewrite them into the same plan (semi-join). Practical guidance:
- `EXISTS` stops at the first match and is `NULL`-safe; good for "has at least one" checks on large subqueries.
- `IN` is fine for small static lists or small subqueries; avoid `NOT IN` with nullable columns (Q6).
- `JOIN` can create duplicate rows when the right side has many matches; you may need `DISTINCT`, which costs extra. Use `EXISTS` when you only need to filter.
