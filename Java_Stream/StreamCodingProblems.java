import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Frequently Asked Java Stream Coding Interview Problems
 * Covers: 2nd highest salary, top earner per dept, multi-field sort, char/word frequency,
 *         first non-repeated char, duplicates, flatMap, merge lists, palindrome & anagrams.
 */
public class StreamCodingProblems {

    record Employee(String name, String dept, double salary) {}

    public static void main(String[] args) {
        List<Employee> emps = List.of(
                new Employee("Asha", "IT", 90000),
                new Employee("Ravi", "IT", 75000),
                new Employee("Meena", "HR", 65000),
                new Employee("Karan", "Finance", 80000),
                new Employee("John", "HR", 55000)
        );

        // 1. Second highest distinct salary
        Optional<Double> secondHighest = emps.stream()
                .map(Employee::salary)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst();
        System.out.println("2nd highest salary: " + secondHighest.orElse(0.0)); // 80000.0

        // 2. Highest paid employee in each department
        Map<String, Employee> topByDept = emps.stream()
                .collect(Collectors.toMap(
                        Employee::dept,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingDouble(Employee::salary))));
        System.out.println("Top earner per dept: " + topByDept);

        // 3. Sort by dept ASC, salary DESC, name ASC & Top 3 earners
        List<Employee> sorted = emps.stream()
                .sorted(Comparator.comparing(Employee::dept)
                        .thenComparing(Employee::salary, Comparator.reverseOrder())
                        .thenComparing(Employee::name))
                .toList();
        List<String> top3Names = emps.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3)
                .map(Employee::name)
                .toList();
        System.out.println("Sorted employees: " + sorted);
        System.out.println("Top 3 earners: " + top3Names); // [Asha, Karan, Ravi]

        // 4. Character frequency & first non-repeated / repeated character
        String input = "programming";
        Map<Character, Long> freq = input.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        Character firstNonRepeated = freq.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        Character firstRepeated = freq.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        System.out.println("Char freq: " + freq);
        System.out.println("First non-repeated: " + firstNonRepeated + ", First repeated: " + firstRepeated);

        // 5. Word frequency
        Map<String, Long> wordCount = Arrays.stream("java is fun and java is fast".split("\\s+"))
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));
        System.out.println("Word count: " + wordCount);

        // 6. Find duplicate elements in a list
        List<Integer> nums = List.of(1, 2, 3, 2, 4, 5, 3, 3);
        Set<Integer> seen = new HashSet<>();
        Set<Integer> dups = nums.stream().filter(n -> !seen.add(n)).collect(Collectors.toSet());
        List<Integer> dupsParallelSafe = nums.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        System.out.println("Duplicates (Set): " + dups + ", (groupingBy): " + dupsParallelSafe);

        // 7. Common one-liners: max, sum of odds, longest word, joining, flatMap
        List<Integer> sample = List.of(5, 3, 8, 1, 9, 2, 8);
        int max = sample.stream().max(Integer::compare).orElseThrow();
        int sumOfOdds = sample.stream().filter(n -> n % 2 != 0).mapToInt(Integer::intValue).sum();
        List<String> words = List.of("apple", "banana", "kiwi", "cherry");
        String longest = words.stream().max(Comparator.comparingInt(String::length)).orElse("");
        String joined = words.stream().map(String::toUpperCase).collect(Collectors.joining(", ", "[", "]"));
        List<Integer> flat = List.of(List.of(1, 2), List.of(3, 4)).stream().flatMap(List::stream).toList();
        System.out.println("Max=" + max + ", SumOfOdds=" + sumOfOdds + ", Longest=" + longest);
        System.out.println("Joined=" + joined + ", Flat=" + flat);

        // 8. Merge two lists without duplicates, sorted
        List<Integer> merged = Stream.concat(List.of(1, 3, 5).stream(), List.of(3, 4, 5).stream())
                .distinct()
                .sorted()
                .toList();
        System.out.println("Merged sorted unique: " + merged); // [1, 3, 4, 5]

        // 9. Palindrome, Anagram & Group Anagrams
        String w = "madam";
        boolean palindrome = IntStream.range(0, w.length() / 2)
                .allMatch(i -> w.charAt(i) == w.charAt(w.length() - 1 - i));
        boolean anagram = Arrays.equals(
                "listen".chars().sorted().toArray(),
                "silent".chars().sorted().toArray());
        Map<String, List<String>> anagramGroups = Stream.of("eat", "tea", "tan", "ate", "nat")
                .collect(Collectors.groupingBy(s -> s.chars().sorted()
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString()));
        System.out.println("Palindrome('madam')=" + palindrome + ", Anagram('listen','silent')=" + anagram);
        System.out.println("Anagram groups: " + anagramGroups);
    }
}
