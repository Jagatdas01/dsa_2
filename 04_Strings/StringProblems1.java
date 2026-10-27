/**
 * DSA 450 Problems - Strings (Part 1: Problems 1-20)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class StringProblems1 {
    
    /**
     * Problem 1: Reverse a String
     * Time Complexity: O(n)
     * Space Complexity: O(1) if using swap, O(n) if using new string
     */
    public static String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }
    
    /**
     * Problem 2: Check if string is palindrome
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }
    
    /**
     * Problem 3: Find duplicate characters in a string
     * Time Complexity: O(n)
     * Space Complexity: O(k) where k = alphabet size
     */
    public static List<Character> findDuplicates(String s) {
        Map<Character, Integer> map = new HashMap<>();
        List<Character> result = new ArrayList<>();
        
        for (char c : s.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() > 1) {
                result.add(entry.getKey());
            }
        }
        
        return result;
    }
    
    /**
     * Problem 4: Check if one string is rotation of another
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean isRotation(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        return (s1 + s1).contains(s2);
    }
    
    /**
     * Problem 5: Check if string is valid shuffle of two strings
     * Time Complexity: O(n) where n = s1.length + s2.length
     * Space Complexity: O(n)
     */
    public static boolean isValidShuffle(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        
        boolean[][] dp = new boolean[s1.length() + 1][s2.length() + 1];
        dp[0][0] = true;
        
        for (int i = 1; i <= s1.length(); i++) {
            dp[i][0] = dp[i - 1][0] && s1.charAt(i - 1) == s3.charAt(i - 1);
        }
        
        for (int j = 1; j <= s2.length(); j++) {
            dp[0][j] = dp[0][j - 1] && s2.charAt(j - 1) == s3.charAt(j - 1);
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                dp[i][j] = (dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1)) ||
                           (dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1));
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Problem 6: Count and Say Problem (Look and Say sequence)
     * Time Complexity: O(n * m) where n = number of terms, m = length of result
     * Space Complexity: O(m)
     */
    public static String countAndSay(int n) {
        String result = "1";
        
        for (int i = 1; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            int j = 0;
            
            while (j < result.length()) {
                char current = result.charAt(j);
                int count = 0;
                
                while (j < result.length() && result.charAt(j) == current) {
                    count++;
                    j++;
                }
                
                sb.append(count).append(current);
            }
            
            result = sb.toString();
        }
        
        return result;
    }
    
    /**
     * Problem 7: Find longest palindrome in a string
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static String longestPalindrome(String s) {
        if (s.length() < 2) return s;
        
        String longest = "";
        
        for (int i = 0; i < s.length(); i++) {
            // Odd length palindromes
            String p1 = expandAroundCenter(s, i, i);
            if (p1.length() > longest.length()) longest = p1;
            
            // Even length palindromes
            String p2 = expandAroundCenter(s, i, i + 1);
            if (p2.length() > longest.length()) longest = p2;
        }
        
        return longest;
    }
    
    private static String expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return s.substring(left + 1, right);
    }
    
    /**
     * Problem 8: Find longest recurring subsequence
     * Time Complexity: O(n^2)
     * Space Complexity: O(n^2)
     */
    public static int longestRecurringSubsequence(String s) {
        int n = s.length();
        int[][] dp = new int[n + 1][n + 1];
        
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (s.charAt(i - 1) == s.charAt(j - 1) && i != j) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[n][n];
    }
    
    /**
     * Problem 9: Print all subsequences of a string
     * Time Complexity: O(2^n)
     * Space Complexity: O(n)
     */
    public static List<String> allSubsequences(String s) {
        List<String> result = new ArrayList<>();
        generateSubsequences(s, "", 0, result);
        return result;
    }
    
    private static void generateSubsequences(String s, String current, int index, List<String> result) {
        if (index == s.length()) {
            result.add(current);
            return;
        }
        
        // Include current character
        generateSubsequences(s, current + s.charAt(index), index + 1, result);
        
        // Exclude current character
        generateSubsequences(s, current, index + 1, result);
    }
    
    /**
     * Problem 10: Print all permutations of a string
     * Time Complexity: O(n * n!)
     * Space Complexity: O(n)
     */
    public static List<String> allPermutations(String s) {
        List<String> result = new ArrayList<>();
        permute(s.toCharArray(), 0, result);
        return result;
    }
    
    private static void permute(char[] chars, int index, List<String> result) {
        if (index == chars.length) {
            result.add(new String(chars));
            return;
        }
        
        for (int i = index; i < chars.length; i++) {
            swap(chars, index, i);
            permute(chars, index + 1, result);
            swap(chars, index, i);
        }
    }
    
    private static void swap(char[] chars, int i, int j) {
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
    }
    
    /**
     * Problem 11: Split binary string into equal 0s and 1s substring
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxSubstringsWithEqualZeroOne(String s) {
        int count = 0;
        int zeros = 0, ones = 0;
        
        for (char c : s.toCharArray()) {
            if (c == '0') zeros++;
            else ones++;
            
            if (zeros == ones) {
                count++;
                zeros = 0;
                ones = 0;
            }
        }
        
        return count;
    }
    
    /**
     * Problem 12: Edit Distance
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     * [VERY IMPORTANT]
     */
    public static int editDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                               Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[m][n];
    }
    
    /**
     * Problem 13: Find next greater number with same digits
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     * [VERY VERY IMPORTANT]
     */
    public static String nextGreaterNumberSameDigits(String num) {
        char[] digits = num.toCharArray();
        int i = digits.length - 2;
        
        // Find rightmost digit smaller than its next digit
        while (i >= 0 && digits[i] >= digits[i + 1]) i--;
        
        if (i < 0) return "-1";  // No next permutation exists
        
        // Find rightmost digit greater than digits[i]
        int j = digits.length - 1;
        while (j > i && digits[j] <= digits[i]) j--;
        
        // Swap
        char temp = digits[i];
        digits[i] = digits[j];
        digits[j] = temp;
        
        // Reverse from i+1 to end
        int left = i + 1, right = digits.length - 1;
        while (left < right) {
            temp = digits[left];
            digits[left] = digits[right];
            digits[right] = temp;
            left++;
            right--;
        }
        
        return new String(digits);
    }
    
    /**
     * Problem 14: Balanced Parenthesis Problem
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     * [IMPORTANT]
     */
    public static boolean isBalanced(String s) {
        Stack<Character> stack = new Stack<>();
        
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return false;
                
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == ']' && top != '[') ||
                    (c == '}' && top != '{')) {
                    return false;
                }
            }
        }
        
        return stack.isEmpty();
    }
    
    /**
     * Problem 15: Word Break Problem
     * Time Complexity: O(n * m * k) where n=length, m=wordDict size, k=word length
     * Space Complexity: O(n)
     * [VERY IMPORTANT]
     */
    public static boolean wordBreak(String s, Set<String> wordDict) {
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        
        for (int i = 1; i <= s.length(); i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && wordDict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        
        return dp[s.length()];
    }
    
    /**
     * Problem 16: KMP Algorithm for pattern matching
     * Time Complexity: O(n + m)
     * Space Complexity: O(m)
     */
    public static int kmpSearch(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        int[] lps = computeLPS(pattern);
        
        int i = 0, j = 0;
        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }
            
            if (j == m) {
                return i - j;  // Found at index
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return -1;
    }
    
    private static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        
        return lps;
    }
    
    /**
     * Problem 17: Rabin Karp Algorithm
     * Time Complexity: O(n + m) average
     * Space Complexity: O(1)
     */
    public static int rabinKarp(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        int base = 256;
        int prime = 101;
        
        int patternHash = 0, textHash = 0;
        int basePower = 1;
        
        for (int i = 0; i < m - 1; i++) {
            basePower = (basePower * base) % prime;
        }
        
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % prime;
            textHash = (textHash * base + text.charAt(i)) % prime;
        }
        
        for (int i = 0; i <= n - m; i++) {
            if (patternHash == textHash) {
                if (text.substring(i, i + m).equals(pattern)) {
                    return i;
                }
            }
            
            if (i < n - m) {
                textHash = (base * (textHash - text.charAt(i) * basePower) + 
                           text.charAt(i + m)) % prime;
                if (textHash < 0) textHash += prime;
            }
        }
        
        return -1;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - String Problems (Part 1) ===\n");
        
        System.out.println("1. Reverse: " + reverseString("hello"));
        System.out.println("2. Is palindrome: " + isPalindrome("racecar"));
        System.out.println("3. Duplicates: " + findDuplicates("geeksforgeeks"));
        System.out.println("4. Is rotation: " + isRotation("waterbottle", "erbottlewat"));
        System.out.println("6. Count and say (n=4): " + countAndSay(4));
        System.out.println("7. Longest palindrome: " + longestPalindrome("babad"));
        System.out.println("12. Edit distance: " + editDistance("horse", "ros"));
        System.out.println("14. Balanced: " + isBalanced("{[()]}"));
        System.out.println("16. KMP search: " + kmpSearch("ABABDABACDABABCABAB", "ABABCABAB"));
    }
}
