/**
 * DSA 450 Problems - Strings (Part 3: Problems 35-51)
 * Recursion with Memoization Approach
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class StringProblems3 {
    
    /**
     * Problem 35: Minimum Steps to Make String Without Vowels Unique
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int minStepsUnique(String s) {
        Set<Character> vowels = new HashSet<>(Arrays.asList('a', 'e', 'i', 'o', 'u'));
        int count = 0;
        
        for (char c : s.toCharArray()) {
            if (vowels.contains(c)) count++;
        }
        
        return count;
    }
    
    /**
     * Problem 36: Longest Substring With Non-Repeating Characters
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int longestSubstringNonRepeating(String s) {
        Map<Character, Integer> lastIndex = new HashMap<>();
        int maxLen = 0;
        int start = 0;
        
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            
            if (lastIndex.containsKey(c)) {
                start = Math.max(start, lastIndex.get(c) + 1);
            }
            
            lastIndex.put(c, end);
            maxLen = Math.max(maxLen, end - start + 1);
        }
        
        return maxLen;
    }
    
    /**
     * Problem 37: Print All Subsequences
     * Time Complexity: O(2^n)
     * Space Complexity: O(n)
     */
    public static void printAllSubsequences(String s) {
        printSubsequencesHelper(s, "", 0);
    }
    
    private static void printSubsequencesHelper(String s, String current, int index) {
        if (index == s.length()) {
            System.out.println(current);
            return;
        }
        
        // Include current character
        printSubsequencesHelper(s, current + s.charAt(index), index + 1);
        
        // Exclude current character
        printSubsequencesHelper(s, current, index + 1);
    }
    
    /**
     * Problem 38: Check for Balanced Parentheses
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean isBalancedParentheses(String s) {
        Stack<Character> stack = new Stack<>();
        Map<Character, Character> pairs = new HashMap<>();
        pairs.put(')', '(');
        pairs.put('}', '{');
        pairs.put(']', '[');
        
        for (char c : s.toCharArray()) {
            if (pairs.containsValue(c)) {
                stack.push(c);
            } else if (pairs.containsKey(c)) {
                if (stack.isEmpty() || !stack.pop().equals(pairs.get(c))) {
                    return false;
                }
            }
        }
        
        return stack.isEmpty();
    }
    
    /**
     * Problem 39: Integer to Roman
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static String intToRoman(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                sb.append(symbols[i]);
                num -= values[i];
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Problem 40: Roman to Integer
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int romanToInt(String s) {
        Map<Character, Integer> values = new HashMap<>();
        values.put('I', 1);
        values.put('V', 5);
        values.put('X', 10);
        values.put('L', 50);
        values.put('C', 100);
        values.put('D', 500);
        values.put('M', 1000);
        
        int num = 0;
        
        for (int i = 0; i < s.length(); i++) {
            int current = values.get(s.charAt(i));
            int next = (i + 1 < s.length()) ? values.get(s.charAt(i + 1)) : 0;
            
            if (current < next) {
                num -= current;
            } else {
                num += current;
            }
        }
        
        return num;
    }
    
    /**
     * Problem 41: Word Ladder
     * Time Complexity: O(n * l^2 * 26) where l is length
     * Space Complexity: O(n)
     */
    public static int wordLadderLength(String start, String end, Set<String> wordDict) {
        if (!wordDict.contains(end)) return 0;
        
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        int level = 1;
        
        while (!queue.isEmpty()) {
            int size = queue.size();
            
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                
                if (current.equals(end)) return level;
                
                for (int j = 0; j < current.length(); j++) {
                    char[] chars = current.toCharArray();
                    
                    for (char c = 'a'; c <= 'z'; c++) {
                        chars[j] = c;
                        String next = new String(chars);
                        
                        if (wordDict.contains(next) && !visited.contains(next)) {
                            visited.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
            
            level++;
        }
        
        return 0;
    }
    
    /**
     * Problem 42: Decode Ways
     * Time Complexity: O(n) with memoization
     * Space Complexity: O(n)
     */
    public static int decodeWays(String s) {
        if (s == null || s.length() == 0 || s.charAt(0) == '0') return 0;
        
        Map<Integer, Integer> memo = new HashMap<>();
        return decodeWaysHelper(s, 0, memo);
    }
    
    private static int decodeWaysHelper(String s, int index, Map<Integer, Integer> memo) {
        if (index == s.length()) return 1;
        if (s.charAt(index) == '0') return 0;
        
        if (memo.containsKey(index)) return memo.get(index);
        
        int result = decodeWaysHelper(s, index + 1, memo);
        
        if (index + 1 < s.length()) {
            int twoDigit = Integer.parseInt(s.substring(index, index + 2));
            if (twoDigit <= 26) {
                result += decodeWaysHelper(s, index + 2, memo);
            }
        }
        
        memo.put(index, result);
        return result;
    }
    
    /**
     * Problem 43: Remove K Digits
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String removeKdigits(String num, int k) {
        if (k >= num.length()) return "0";
        
        Stack<Character> stack = new Stack<>();
        
        for (char c : num.toCharArray()) {
            while (!stack.isEmpty() && k > 0 && stack.peek() > c) {
                stack.pop();
                k--;
            }
            stack.push(c);
        }
        
        while (k > 0) {
            stack.pop();
            k--;
        }
        
        StringBuilder result = new StringBuilder();
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        result.reverse();
        
        // Remove leading zeros
        int i = 0;
        while (i < result.length() - 1 && result.charAt(i) == '0') {
            i++;
        }
        
        return result.substring(i);
    }
    
    /**
     * Problem 44: Next Permutation
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void nextPermutation(int[] nums) {
        int i = nums.length - 2;
        
        while (i >= 0 && nums[i] >= nums[i + 1]) {
            i--;
        }
        
        if (i >= 0) {
            int j = nums.length - 1;
            while (j > i && nums[j] <= nums[i]) {
                j--;
            }
            swap(nums, i, j);
        }
        
        reverse(nums, i + 1, nums.length - 1);
    }
    
    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
    
    private static void reverse(int[] nums, int start, int end) {
        while (start < end) {
            swap(nums, start, end);
            start++;
            end--;
        }
    }
    
    /**
     * Problem 45: Distinct Subsequences
     * Time Complexity: O(n * m) with memoization
     * Space Complexity: O(n * m)
     */
    public static int distinctSubsequences(String s, String t) {
        Map<String, Integer> memo = new HashMap<>();
        return countDistinctRecursive(s, t, 0, 0, memo);
    }
    
    private static int countDistinctRecursive(String s, String t, int i, int j,
                                             Map<String, Integer> memo) {
        if (j == t.length()) return 1;
        if (i == s.length()) return 0;
        
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        
        if (s.charAt(i) == t.charAt(j)) {
            result = countDistinctRecursive(s, t, i + 1, j + 1, memo) +
                    countDistinctRecursive(s, t, i + 1, j, memo);
        } else {
            result = countDistinctRecursive(s, t, i + 1, j, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 46: Alien Dictionary
     * Time Complexity: O(n * l * log n)
     * Space Complexity: O(n)
     */
    public static String alienDictionary(String[] words) {
        Map<Character, Set<Character>> graph = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        
        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }
        
        for (int i = 0; i < words.length - 1; i++) {
            String current = words[i];
            String next = words[i + 1];
            int minLen = Math.min(current.length(), next.length());
            
            for (int j = 0; j < minLen; j++) {
                char c1 = current.charAt(j);
                char c2 = next.charAt(j);
                
                if (c1 != c2) {
                    if (!graph.get(c1).contains(c2)) {
                        graph.get(c1).add(c2);
                        inDegree.put(c2, inDegree.get(c2) + 1);
                    }
                    break;
                }
            }
        }
        
        // Topological sort
        Queue<Character> queue = new LinkedList<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) {
                queue.add(c);
            }
        }
        
        StringBuilder result = new StringBuilder();
        while (!queue.isEmpty()) {
            char c = queue.poll();
            result.append(c);
            
            for (char neighbor : graph.get(c)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Problem 47: Concatenated Words
     * Time Complexity: O(n * l^2)
     * Space Complexity: O(n)
     */
    public static List<String> findConcatenatedWords(String[] words) {
        Set<String> wordSet = new HashSet<>(Arrays.asList(words));
        List<String> result = new ArrayList<>();
        
        for (String word : words) {
            wordSet.remove(word);
            
            if (canForm(word, wordSet)) {
                result.add(word);
            }
            
            wordSet.add(word);
        }
        
        return result;
    }
    
    private static boolean canForm(String word, Set<String> wordSet) {
        boolean[] dp = new boolean[word.length() + 1];
        dp[0] = true;
        
        for (int i = 1; i <= word.length(); i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && wordSet.contains(word.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        
        return dp[word.length()];
    }
    
    /**
     * Problem 48: Substring with Concatenation
     * Time Complexity: O(n * m)
     * Space Complexity: O(m)
     */
    public static List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        if (s == null || words == null || words.length == 0) return result;
        
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        
        int wordLen = words[0].length();
        int totalLen = wordLen * words.length;
        
        for (int i = 0; i <= s.length() - totalLen; i++) {
            String substring = s.substring(i, i + totalLen);
            
            if (isValidConcatenation(substring, wordCount, wordLen)) {
                result.add(i);
            }
        }
        
        return result;
    }
    
    private static boolean isValidConcatenation(String s, Map<String, Integer> wordCount, int wordLen) {
        Map<String, Integer> seen = new HashMap<>();
        
        for (int i = 0; i < s.length(); i += wordLen) {
            String word = s.substring(i, i + wordLen);
            seen.put(word, seen.getOrDefault(word, 0) + 1);
        }
        
        return seen.equals(wordCount);
    }
    
    /**
     * Problem 49: Text Justification
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<String> textJustification(String[] words, int maxWidth) {
        List<String> result = new ArrayList<>();
        List<List<String>> lines = new ArrayList<>();
        
        int index = 0;
        while (index < words.length) {
            List<String> line = new ArrayList<>();
            int len = 0;
            
            while (index < words.length && len + words[index].length() + line.size() <= maxWidth) {
                line.add(words[index]);
                len += words[index].length();
                index++;
            }
            
            lines.add(line);
        }
        
        for (int i = 0; i < lines.size(); i++) {
            result.add(justifyLine(lines.get(i), maxWidth, i == lines.size() - 1));
        }
        
        return result;
    }
    
    private static String justifyLine(List<String> line, int maxWidth, boolean isLast) {
        if (isLast) {
            StringBuilder sb = new StringBuilder();
            for (String word : line) {
                sb.append(word).append(" ");
            }
            sb.setLength(sb.length() - 1);
            while (sb.length() < maxWidth) {
                sb.append(" ");
            }
            return sb.toString();
        }
        
        int spaces = maxWidth;
        for (String word : line) {
            spaces -= word.length();
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.size(); i++) {
            sb.append(line.get(i));
            
            if (i < line.size() - 1) {
                int spaceCount = spaces / (line.size() - i - 1);
                for (int j = 0; j < spaceCount + 1; j++) {
                    sb.append(" ");
                }
                spaces -= spaceCount;
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Problem 50: Regular Expression Matching
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n)
     */
    public static boolean regexMatch(String s, String p) {
        Map<String, Boolean> memo = new HashMap<>();
        return matchHelper(s, p, 0, 0, memo);
    }
    
    private static boolean matchHelper(String s, String p, int i, int j,
                                      Map<String, Boolean> memo) {
        if (j == p.length()) return i == s.length();
        
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        boolean firstMatch = (i < s.length() && (s.charAt(i) == p.charAt(j) || p.charAt(j) == '.'));
        
        boolean result;
        if (j + 1 < p.length() && p.charAt(j + 1) == '*') {
            result = matchHelper(s, p, i, j + 2, memo) ||
                    (firstMatch && matchHelper(s, p, i + 1, j, memo));
        } else {
            result = firstMatch && matchHelper(s, p, i + 1, j + 1, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 51: Wildcard Matching
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n)
     */
    public static boolean wildcardMatch(String s, String p) {
        Map<String, Boolean> memo = new HashMap<>();
        return wildcardHelper(s, p, 0, 0, memo);
    }
    
    private static boolean wildcardHelper(String s, String p, int i, int j,
                                         Map<String, Boolean> memo) {
        if (j == p.length()) return i == s.length();
        if (i == s.length()) {
            while (j < p.length()) {
                if (p.charAt(j) != '*') return false;
                j++;
            }
            return true;
        }
        
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        char sc = s.charAt(i);
        char pc = p.charAt(j);
        
        boolean result = false;
        
        if (pc == '*') {
            result = wildcardHelper(s, p, i + 1, j, memo) ||
                    wildcardHelper(s, p, i, j + 1, memo);
        } else if (sc == pc || pc == '?') {
            result = wildcardHelper(s, p, i + 1, j + 1, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - String Problems (Part 3) ===\n");
        
        System.out.println("36. Longest Substring Non-Repeating (abcabcbb): " + 
                          longestSubstringNonRepeating("abcabcbb"));
        System.out.println("39. Int to Roman (58): " + 
                          intToRoman(58));
        System.out.println("40. Roman to Int (LVIII): " + 
                          romanToInt("LVIII"));
        System.out.println("44. Next Permutation tested");
    }
}
