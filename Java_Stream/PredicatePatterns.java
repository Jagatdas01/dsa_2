import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Predicate<T> Patterns & Composition (Java 8 - 21)
 * Covers: test, and, or, negate, Predicate.not, Predicate.isEqual,
 *         dynamic filter chaining, BiPredicate, IntPredicate, removeIf, Optional.filter.
 */
public class PredicatePatterns {

    record Employee(String name, String dept, double salary, int age) {}

    public static void main(String[] args) {
        List<Employee> emps = List.of(
                new Employee("Asha", "IT", 90000, 30),
                new Employee("Ravi", "IT", 75000, 28),
                new Employee("Meena", "HR", 65000, 32),
                new Employee("Karan", "Finance", 82000, 40),
                new Employee("John", "HR", 78000, 36)
        );

        // 1. Atomic predicates
        Predicate<Employee> isItDept     = e -> "IT".equals(e.dept());
        Predicate<Employee> isHrDept     = e -> "HR".equals(e.dept());
        Predicate<Employee> isHighEarner = e -> e.salary() >= 75000;
        Predicate<Employee> isSeniorAge  = e -> e.age() >= 35;

        // 2. Composing rules: (IT or HR) AND High Earner AND NOT Senior Age
        Predicate<Employee> targetRule = isItDept.or(isHrDept)
                .and(isHighEarner)
                .and(isSeniorAge.negate());

        List<String> matched = emps.stream()
                .filter(targetRule)
                .map(Employee::name)
                .toList();
        System.out.println("Target rule matched: " + matched); // [Asha, Ravi]

        // 3. Dynamic filter chain (e.g., optional search criteria)
        List<Predicate<Employee>> dynamicFilters = List.of(isItDept, isHighEarner);
        Predicate<Employee> combinedFilter = dynamicFilters.stream()
                .reduce(x -> true, Predicate::and);

        System.out.println("Dynamic filter matched: " + emps.stream()
                .filter(combinedFilter)
                .map(Employee::name)
                .toList()); // [Asha, Ravi]

        // 4. Predicate.not (Java 11), Predicate.isEqual, and Collection.removeIf
        List<String> tags = new ArrayList<>(Arrays.asList("java", "", "  ", "spring", "deprecated"));
        tags.removeIf(String::isBlank);
        tags.removeIf(Predicate.isEqual("deprecated"));
        System.out.println("Cleaned tags: " + tags); // [java, spring]

        List<String> nonBlankLines = List.of("a", " ", "b", "").stream()
                .filter(Predicate.not(String::isBlank))
                .toList();
        System.out.println("Non-blank lines: " + nonBlankLines); // [a, b]

        // 5. BiPredicate & IntPredicate (avoids Integer auto-boxing)
        BiPredicate<Employee, Double> earnsMoreThan = (emp, threshold) -> emp.salary() > threshold;
        System.out.println("Asha earns > 80k: " + earnsMoreThan.test(emps.get(0), 80000.0)); // true

        IntPredicate isEven = n -> n % 2 == 0;
        int[] evens = IntStream.rangeClosed(1, 10).filter(isEven).toArray();
        System.out.println("Evens 1..10: " + Arrays.toString(evens)); // [2, 4, 6, 8, 10]

        // 6. Optional.filter
        Optional<Employee> seniorHighEarner = Optional.of(emps.get(3))
                .filter(isHighEarner.and(isSeniorAge));
        System.out.println("Optional filter result: " + seniorHighEarner.map(Employee::name).orElse("None")); // Karan
    }
}
