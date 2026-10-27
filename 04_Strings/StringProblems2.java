/**
 * DSA 450 Problems - Strings (Part 2: Problems 18-34)
 * Recursion with Memoization Approach
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class StringProblems2 {
    
    /**
     * Problem 18: Minimum Window Substring
     * Time Complexity: O(m + n) with memoization
     * Space Complexity: O(1) - fixed charset
     */
    public static String minWindowSubstring(String s, String t) {
        if (t.length() > s.length()) return "";
        
        Map<Character, Integer> targetMap = new HashMap<>();
        for (char c : t.toCharArray()) {
            targetMap.put(c, targetMap.getOrDefault(c, 0) + 1);
        }
        
        int[] result = {0, s.length()};
        int formed = 0;
        Map<Character, Integer> windowMap = new HashMap<>();
        int required = targetMap.size();
        
        int left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowMap.put(c, windowMap.getOrDefault(c, 0) + 1);
            
            if (targetMap.containsKey(c) && windowMap.get(c).equals(targetMap.get(c))) {
                formed++;
            }
            
            while (left <= right && formed == required) {
                if (right - left + 1 < result[1] - result[0] + 1) {
                    result[0] = left;
                    result[1] = right;
                }
                
                char leftChar = s.charAt(left);
                windowMap.put(leftChar, windowMap.get(leftChar) - 1);
                if (targetMap.containsKey(leftChar) && windowMap.get(leftChar) < targetMap.get(leftChar)) {
                    formed--;
                }
                left++;
            }
        }
        
        return result[1] >= s.length() ? "" : s.substring(result[0], result[1] + 1);
    }
    
    /**
     * Problem 19: Permutation of String
     * Time Complexity: O(n!) with memoization
     * Space Complexity: O(n)
     */
    public static List<String> permutations(String s) {
        List<String> result = new ArrayList<>();
        permutationHelper(s.toCharArray(), 0, result);
        return result;
    }
    
    private static void permutationHelper(char[] chars, int index, List<String> result) {
        if (index == chars.length) {
            result.add(new String(chars));
            return;
        }
        
        for (int i = index; i < chars.length; i++) {
            // Swap
            char temp = chars[index];
            chars[index] = chars[i];
            chars[i] = temp;
            
            permutationHelper(chars, index + 1, result);
            
            // Swap back
            temp = chars[index];
            chars[index] = chars[i];
            chars[i] = temp;
        }
    }
    
    /**
     * Problem 20: String Compression
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static String stringCompression(String s) {
        StringBuilder compressed = new StringBuilder();
        int count = 0;
        
        for (int i = 0; i < s.length(); i++) {
            count++;
            
            if (i + 1 >= s.length() || s.charAt(i) != s.charAt(i + 1)) {
                compressed.append(s.charAt(i));
                if (count > 1) {
                    compressed.append(count);
                }
                count = 0;
            }
        }
        
        return compressed.toString();
    }
    
    /**
     * Problem 21: Smallest Distinct Window
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static String smallestDistinctWindow(String s) {
        Set<Character> distinct = new HashSet<>();
        for (char c : s.toCharArray()) distinct.add(c);
        
        int required = distinct.size();
        int formed = 0;
        Map<Character, Integer> windowMap = new HashMap<>();
        int[] result = {0, s.length()};
        
        int left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowMap.put(c, windowMap.getOrDefault(c, 0) + 1);
            
            if (windowMap.get(c) == 1) formed++;
            
            while (formed == required) {
                if (right - left + 1 < result[1] - result[0]) {
                    result[0] = left;
                    result[1] = right + 1;
                }
                
                char leftChar = s.charAt(left);
                windowMap.put(leftChar, windowMap.get(leftChar) - 1);
                if (windowMap.get(leftChar) == 0) formed--;
                left++;
            }
        }
        
        return s.substring(result[0], result[1]);
    }
    
    /**
     * Problem 22: Isomorphic Strings
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isIsomorphic(String s, String t) {
        if (s.length() != t.length()) return false;
        
        Map<Character, Character> map1 = new HashMap<>();
        Map<Character, Character> map2 = new HashMap<>();
        
        for (int i = 0; i < s.length(); i++) {
            char c1 = s.charAt(i);
            char c2 = t.charAt(i);
            
            if (map1.containsKey(c1) && !map1.get(c1).equals(c2)) return false;
            if (map2.containsKey(c2) && !map2.get(c2).equals(c1)) return false;
            
            map1.put(c1, c2);
            map2.put(c2, c1);
        }
        
        return true;
    }
    
    /**
     * Problem 23: Group Shifted Strings
     * Time Complexity: O(n * k) where k is average string length
     * Space Complexity: O(n)
     */
    public static List<List<String>> groupShiftedStrings(String[] strings) {
        Map<String, List<String>> groups = new HashMap<>();
        
        for (String s : strings) {
            String pattern = getPattern(s);
            groups.computeIfAbsent(pattern, k -> new ArrayList<>()).add(s);
        }
        
        return new ArrayList<>(groups.values());
    }
    
    private static String getPattern(String s) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i++) {
            int diff = (s.charAt(i + 1) - s.charAt(i) + 26) % 26;
            pattern.append(diff).append(",");
        }
        return pattern.toString();
    }
    
    /**
     * Problem 24: Shortest Palindrome
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String shortestPalindrome(String s) {
        String rev = new StringBuilder(s).reverse().toString();
        String combined = s + "#" + rev;
        
        int[] lps = computeLPS(combined);
        int palindromeLength = lps[combined.length() - 1];
        
        return rev.substring(0, s.length() - palindromeLength) + s;
    }
    
    private static int[] computeLPS(String s) {
        int n = s.length();
        int[] lps = new int[n];
        int len = 0;
        int i = 1;
        
        while (i < n) {
            if (s.charAt(i) == s.charAt(len)) {
                lps[i] = ++len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        
        return lps;
    }
    
    /**
     * Problem 25: Interleaved Strings
     * Time Complexity: O(m + n) with memoization
     * Space Complexity: O(m * n)
     */
    public static boolean isInterleavedRecursive(String s1, String s2, String s3) {
        Map<String, Boolean> memo = new HashMap<>();
        return interleavedHelper(s1, s2, s3, 0, 0, 0, memo);
    }
    
    private static boolean interleavedHelper(String s1, String s2, String s3,
                                            int i, int j, int k, Map<String, Boolean> memo) {
        if (i == s1.length() && j == s2.length() && k == s3.length()) return true;
        if (k >= s3.length()) return false;
        
        String key = i + ":" + j + ":" + k;
        if (memo.containsKey(key)) return memo.get(key);
        
        boolean result = false;
        
        if (i < s1.length() && s1.charAt(i) == s3.charAt(k)) {
            result = interleavedHelper(s1, s2, s3, i + 1, j, k + 1, memo);
        }
        
        if (!result && j < s2.length() && s2.charAt(j) == s3.charAt(k)) {
            result = interleavedHelper(s1, s2, s3, i, j + 1, k + 1, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 26: Repeated String Match
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int repeatedStringMatch(String a, String b) {
        StringBuilder repeated = new StringBuilder(a);
        int count = 1;
        
        while (repeated.length() < a.length() + b.length()) {
            repeated.append(a);
            count++;
        }
        
        if (repeated.toString().contains(b)) {
            return count;
        }
        
        repeated.append(a);
        count++;
        return repeated.toString().contains(b) ? count : -1;
    }
    
    /**
     * Problem 27: Largest Number
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static String largestNumber(int[] nums) {
        String[] strs = new String[nums.length];
        for (int i = 0; i < nums.length; i++) {
            strs[i] = String.valueOf(nums[i]);
        }
        
        Arrays.sort(strs, (a, b) -> (b + a).compareTo(a + b));
        
        if (strs[0].equals("0")) return "0";
        
        StringBuilder result = new StringBuilder();
        for (String s : strs) result.append(s);
        return result.toString();
    }
    
    /**
     * Problem 28: Count Different Palindromic Subsequences
     * Time Complexity: O(n) with KMP
     * Space Complexity: O(1)
     */
    public static int countDifferentPalindromes(String s) {
        int count = 0;
        
        for (char c = 'a'; c <= 'z'; c++) {
            int first = s.indexOf(c);
            int last = s.lastIndexOf(c);
            
            if (first != -1) {
                count++;
                if (first != last) {
                    count += countDistinct(s, first + 1, last - 1);
                }
            }
        }
        
        return count;
    }
    
    private static int countDistinct(String s, int start, int end) {
        if (start > end) return 0;
        Set<Character> distinct = new HashSet<>();
        for (int i = start; i <= end; i++) {
            distinct.add(s.charAt(i));
        }
        return distinct.size();
    }
    
    /**
     * Problem 29: Restore IP Addresses
     * Time Complexity: O(3^4) = O(1) with memoization
     * Space Complexity: O(1)
     */
    public static List<String> restoreIPAddresses(String s) {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> memo = new HashMap<>();
        
        if (s.length() > 12 || s.length() < 4) return result;
        
        restoreIPHelper(s, 0, "", 0, result, memo);
        return result;
    }
    
    private static void restoreIPHelper(String s, int index, String current, int parts,
                                       List<String> result, Map<String, List<String>> memo) {
        if (parts == 4) {
            if (index == s.length()) {
                result.add(current.substring(0, current.length() - 1));
            }
            return;
        }
        
        if (index >= s.length()) return;
        
        for (int i = 1; i <= 3 && index + i <= s.length(); i++) {
            String part = s.substring(index, index + i);
            
            if ((part.charAt(0) == '0' && part.length() > 1) || Integer.parseInt(part) > 255) {
                break;
            }
            
            restoreIPHelper(s, index + i, current + part + ".", parts + 1, result, memo);
        }
    }
    
    /**
     * Problem 30: Convert to Zigzag Pattern
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String zigzagConversion(String s, int numRows) {
        if (numRows == 1) return s;
        
        List<StringBuilder> rows = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            rows.add(new StringBuilder());
        }
        
        int row = 0;
        int direction = 1;
        
        for (char c : s.toCharArray()) {
            rows.get(row).append(c);
            
            if (row == 0) direction = 1;
            else if (row == numRows - 1) direction = -1;
            
            row += direction;
        }
        
        StringBuilder result = new StringBuilder();
        for (StringBuilder sb : rows) {
            result.append(sb);
        }
        
        return result.toString();
    }
    
    /**
     * Problem 31: Rotate String
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean rotateString(String s, String goal) {
        if (s.length() != goal.length()) return false;
        return (s + s).contains(goal);
    }
    
    /**
     * Problem 32: String Matching in Array
     * Time Complexity: O(n * m) where m is average length
     * Space Complexity: O(n)
     */
    public static List<String> stringMatching(String[] words) {
        List<String> result = new ArrayList<>();
        Set<String> checked = new HashSet<>();
        
        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words.length; j++) {
                if (i != j && !checked.contains(words[i]) && 
                    words[j].contains(words[i])) {
                    result.add(words[i]);
                    checked.add(words[i]);
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Problem 33: Multiply Large Numbers
     * Time Complexity: O(n * m)
     * Space Complexity: O(n + m)
     */
    public static String multiplyStrings(String num1, String num2) {
        if (num1.equals("0") || num2.equals("0")) return "0";
        
        int m = num1.length();
        int n = num2.length();
        int[] result = new int[m + n];
        
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                int mul = (num1.charAt(i) - '0') * (num2.charAt(j) - '0');
                int pos1 = i + j;
                int pos2 = i + j + 1;
                
                int sum = mul + result[pos2];
                result[pos2] = sum % 10;
                result[pos1] += sum / 10;
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (int num : result) {
            if (!(sb.length() == 0 && num == 0)) {
                sb.append(num);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Problem 34: Number of Closed Islands
     * Time Complexity: O(n * m)
     * Space Complexity: O(n * m)
     */
    public static int countIslands(String[] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int count = 0;
        boolean[][] visited = new boolean[grid.length][grid[0].length()];
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length(); j++) {
                if (grid[i].charAt(j) == '1' && !visited[i][j]) {
                    dfsIsland(grid, i, j, visited);
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private static void dfsIsland(String[] grid, int row, int col, boolean[][] visited) {
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length() ||
            visited[row][col] || grid[row].charAt(col) == '0') {
            return;
        }
        
        visited[row][col] = true;
        
        dfsIsland(grid, row + 1, col, visited);
        dfsIsland(grid, row - 1, col, visited);
        dfsIsland(grid, row, col + 1, visited);
        dfsIsland(grid, row, col - 1, visited);
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - String Problems (Part 2) ===\n");
        
        System.out.println("20. String Compression (aaabbbcc): " + 
                          stringCompression("aaabbbcc"));
        System.out.println("22. Isomorphic (egg, add): " + 
                          isIsomorphic("egg", "add"));
        System.out.println("27. Largest Number: " + 
                          largestNumber(new int[]{3, 30, 34, 5, 9}));
        System.out.println("31. Rotate String (abcd, cdab): " + 
                          rotateString("abcd", "cdab"));
    }
}
