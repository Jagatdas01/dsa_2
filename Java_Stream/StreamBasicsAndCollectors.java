import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stream Basics, Lazy Evaluation, Primitive Streams & Collectors
 * Covers: lazy evaluation, short-circuiting, reduce vs collect, primitive streams,
 *         toMap with merge/order, groupingBy/partitioningBy, stream creation.
 */
public class StreamBasicsAndCollectors {

    record Employee(String name, String dept, String gender, double salary, int age) {}

    public static void main(String[] args) {
        List<Employee> emps = List.of(
                new Employee("Asha", "IT", "F", 90000, 30),
                new Employee("Ravi", "IT", "M", 75000, 28),
                new Employee("Meena", "HR", "F", 65000, 32),
                new Employee("Karan", "Finance", "M", 80000, 40),
                new Employee("John", "HR", "M", 55000, 25)
        );

        // 1. Lazy evaluation & short-circuiting
        System.out.println("--- Lazy Evaluation ---");
        Optional<String> firstLong = Stream.of("a", "bb", "ccc", "dddd")
                .filter(s -> {
                    System.out.println("filter: " + s);
                    return s.length() > 1;
                })
                .map(s -> {
                    System.out.println("map: " + s);
                    return s.toUpperCase();
                })
                .findFirst();
        System.out.println("First match: " + firstLong.orElse(""));

        // 2. Matchers
        boolean anyHr      = emps.stream().anyMatch(e -> e.dept().equals("HR"));
        boolean allAdults  = emps.stream().allMatch(e -> e.age() >= 18);
        boolean noneIntern = emps.stream().noneMatch(e -> e.salary() < 10000);
        System.out.println("Matchers -> anyHr=" + anyHr + ", allAdults=" + allAdults + ", noneIntern=" + noneIntern);

        // 3. Reduce & Primitive Streams
        int sum = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);
        int rangeSum = IntStream.rangeClosed(1, 100).sum();
        OptionalDouble avgSalary = emps.stream().mapToDouble(Employee::salary).average();
        DoubleSummaryStatistics stats = emps.stream().mapToDouble(Employee::salary).summaryStatistics();
        System.out.println("Sum=" + sum + ", 1..100=" + rangeSum + ", avgSalary=" + avgSalary.orElse(0));
        System.out.println("Salary stats: " + stats);

        // 4. Collectors.toMap with duplicate key merge + LinkedHashMap order
        Map<String, Double> maxSalaryByDept = emps.stream()
                .collect(Collectors.toMap(
                        Employee::dept,
                        Employee::salary,
                        Math::max,
                        LinkedHashMap::new));
        System.out.println("Max salary by dept (insertion order): " + maxSalaryByDept);

        // 5. groupingBy & partitioningBy with downstream collectors
        Map<String, Long> countByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
        Map<String, Double> avgByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::dept, Collectors.averagingDouble(Employee::salary)));
        Map<String, List<String>> namesByDeptSorted = emps.stream()
                .collect(Collectors.groupingBy(Employee::dept, TreeMap::new,
                        Collectors.mapping(Employee::name, Collectors.toList())));
        Map<String, Map<String, Long>> byDeptThenGender = emps.stream()
                .collect(Collectors.groupingBy(Employee::dept,
                        Collectors.groupingBy(Employee::gender, Collectors.counting())));
        Map<Boolean, List<String>> highEarners = emps.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() > 70000,
                        Collectors.mapping(Employee::name, Collectors.toList())));

        System.out.println("Count by dept: " + countByDept);
        System.out.println("Avg by dept: " + avgByDept);
        System.out.println("Names by dept (TreeMap): " + namesByDeptSorted);
        System.out.println("By dept then gender: " + byDeptThenGender);
        System.out.println("Partition >70k: " + highEarners);

        // 6. Reusing a stream safely via Supplier
        Supplier<Stream<String>> namesSupplier = () -> emps.stream().map(Employee::name);
        System.out.println("Count=" + namesSupplier.get().count() + ", Names=" + namesSupplier.get().toList());

        // 7. Stream creation from various sources
        List<String> splitTokens = Pattern.compile(",").splitAsStream("a,b,c").toList();
        List<Integer> evenArrays = Arrays.stream(new int[]{1, 2, 3, 4}).filter(n -> n % 2 == 0).boxed().toList();
        System.out.println("Split tokens: " + splitTokens + ", Even array items: " + evenArrays);
    }
}
