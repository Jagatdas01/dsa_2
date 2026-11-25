import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stream API Additions from Java 9 to Java 21
 * Covers: takeWhile, dropWhile, Stream.ofNullable, 3-arg Stream.iterate, Optional.stream,
 *         Collectors.filtering, Java 10 toUnmodifiableList, Java 11 Predicate.not & String.lines,
 *         Java 12 Collectors.teeing, Java 16 Stream.toList & mapMulti, Java 21 reversed().stream().
 */
public class StreamJava9To21Features {

    record Employee(String name, String dept, double salary) {}

    public static void main(String[] args) {
        List<Employee> emps = List.of(
                new Employee("Asha", "IT", 90000),
                new Employee("Ravi", "IT", 75000),
                new Employee("Meena", "HR", 65000),
                new Employee("Karan", "Finance", 80000),
                new Employee("John", "HR", 55000)
        );

        // 1. Java 9: takeWhile vs dropWhile
        List<Integer> nums = List.of(1, 2, 3, 7, 1, 2);
        List<Integer> taken   = nums.stream().takeWhile(n -> n < 5).toList(); // [1, 2, 3]
        List<Integer> dropped = nums.stream().dropWhile(n -> n < 5).toList(); // [7, 1, 2]
        System.out.println("takeWhile: " + taken + ", dropWhile: " + dropped);

        // 2. Java 9: Stream.ofNullable & bounded Stream.iterate & Optional.stream
        Map<String, String> env = Map.of("DB_HOST", "localhost", "DB_PORT", "5432");
        List<String> values = Stream.of("DB_HOST", "MISSING_KEY", "DB_PORT")
                .flatMap(key -> Stream.ofNullable(env.get(key)))
                .toList();
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n <= 64, n -> n * 2).toList();
        List<String> present = List.of(Optional.of("A"), Optional.<String>empty(), Optional.of("B"))
                .stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("ofNullable: " + values + ", powersOfTwo: " + powersOfTwo + ", Optional.stream: " + present);

        // 3. Java 9: Collectors.filtering inside groupingBy (preserves HR with [])
        Map<String, List<String>> highEarnersByDept = emps.stream()
                .collect(Collectors.groupingBy(
                        Employee::dept,
                        Collectors.filtering(e -> e.salary() > 70000,
                                Collectors.mapping(Employee::name, Collectors.toList()))));
        System.out.println("High earners by dept (keeps HR=[]): " + highEarnersByDept);

        // 4. Java 10 & 11: String.lines(), Predicate.not(), toUnmodifiableList()
        String multiline = "order-101\n   \norder-102\n";
        List<String> orderIds = multiline.lines()
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toUnmodifiableList());
        System.out.println("Order IDs: " + orderIds);

        // 5. Java 12: Collectors.teeing (two aggregations in a single pass)
        String minMaxSummary = emps.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingDouble(Employee::salary)),
                Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                (minOpt, maxOpt) -> minOpt.orElseThrow().name() + " to " + maxOpt.orElseThrow().name()
        ));
        Map<String, Number> stats = emps.stream().collect(Collectors.teeing(
                Collectors.counting(),
                Collectors.averagingDouble(Employee::salary),
                (count, avg) -> Map.of("count", count, "avgSalary", avg)
        ));
        System.out.println("Min-Max: " + minMaxSummary + ", Stats: " + stats);

        // 6. Java 16: Stream.mapMulti (zero intermediate Stream allocations)
        List<Object> mixed = List.of("Java", 42, "Spring Boot", -5, 100);
        List<String> validItems = mixed.stream()
                .<String>mapMulti((element, downstream) -> {
                    if (element instanceof String s && !s.isBlank()) {
                        downstream.accept(s.toUpperCase());
                    } else if (element instanceof Integer n && n > 0) {
                        downstream.accept("NUM:" + n);
                    }
                })
                .toList();
        System.out.println("mapMulti: " + validItems);

        // 7. Java 21: SequencedCollection.reversed().stream()
        List<String> steps = List.of("Init", "Validate", "Charge", "Ship");
        List<String> rollbackOrder = steps.reversed().stream()
                .map(step -> "Undo-" + step)
                .toList();
        System.out.println("Rollback order: " + rollbackOrder);
    }
}
