/**
 * DSA Pattern: String Manipulation
 * Topics: Substring, anagram, palindrome, pattern matching
 */

public class StringPatterns {
    
    /**
     * Problem: Check if String is Palindrome
     * Verify if string reads same forwards and backwards
     * 
     * Input: s = "racecar"
     * Expected Output: true
     * 
     * Input: s = "hello"
     * Expected Output: false
     * 
     * Input: s = "a"
     * Expected Output: true (single char is palindrome)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        
        return true;
    }
    
    /**
     * Problem: Longest Palindromic Substring
     * Find the longest contiguous substring that is palindrome
     * 
     * Input: s = "babad"
     * Expected Output: "bab" or "aba" (both length 3)
     * 
     * Input: s = "cbbd"
     * Expected Output: "bb" (length 2)
     * 
     * Input: s = "racecar"
     * Expected Output: "racecar" (entire string, length 7)
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static String longestPalindrome(String s) {
        if (s.length() < 2) return s;
        
        String longest = "";
        
        for (int i = 0; i < s.length(); i++) {
            // Odd length palindromes
            String p1 = expandAroundCenter(s, i, i);
            // Even length palindromes
            String p2 = expandAroundCenter(s, i, i + 1);
            
            String current = p1.length() > p2.length() ? p1 : p2;
            longest = current.length() > longest.length() ? current : longest;
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
     * Problem: Check if Two Strings are Anagrams
     * Verify if two strings contain same characters with same frequencies
     * 
     * Input: s1 = "listen", s2 = "silent"
     * Expected Output: true (same characters rearranged)
     * 
     * Input: s1 = "hello", s2 = "world"
     * Expected Output: false (different characters)
     * 
     * Input: s1 = "abc", s2 = "def"
     * Expected Output: false
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1) - fixed alphabet size
     */
    public static boolean isAnagram(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        
        int[] charCount = new int[26];
        
        for (char c : s1.toCharArray()) {
            charCount[c - 'a']++;
        }
        
        for (char c : s2.toCharArray()) {
            charCount[c - 'a']--;
            if (charCount[c - 'a'] < 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem: Reverse String
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void reverseString(char[] s) {
        int left = 0, right = s.length - 1;
        
        while (left < right) {
            char temp = s[left];
            s[left] = s[right];
            s[right] = temp;
            left++;
            right--;
        }
    }
    
    /**
     * Problem: First Unique Character in String
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int firstUniqueCharacter(String s) {
        int[] charFreq = new int[26];
        
        for (char c : s.toCharArray()) {
            charFreq[c - 'a']++;
        }
        
        for (int i = 0; i < s.length(); i++) {
            if (charFreq[s.charAt(i) - 'a'] == 1) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Problem: Longest Substring Without Repeating Characters
     * Time Complexity: O(n)
     * Space Complexity: O(min(n, m)) where m is charset
     */
    public static int lengthOfLongestSubstring(String s) {
        int[] charIndex = new int[26];
        int maxLength = 0;
        int start = 0;
        
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            
            if (charIndex[c - 'a'] > 0) {
                start = Math.max(start, charIndex[c - 'a']);
            }
            
            maxLength = Math.max(maxLength, end - start + 1);
            charIndex[c - 'a'] = end + 1;
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Reverse Words in String
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String reverseWords(String s) {
        String[] words = s.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = words.length - 1; i >= 0; i--) {
            result.append(words[i]);
            if (i > 0) {
                result.append(" ");
            }
        }
        
        return result.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Is 'racecar' palindrome? " + isPalindrome("racecar"));
        
        System.out.println("Longest palindrome in 'babad': " + longestPalindrome("babad"));
        
        System.out.println("Is 'listen' anagram of 'silent'? " + isAnagram("listen", "silent"));
        
        System.out.println("First unique char in 'leetcode': " + firstUniqueCharacter("leetcode"));
        
        System.out.println("Longest substring without repeating: " + lengthOfLongestSubstring("abcabcbb"));
        
        System.out.println("Reverse words: " + reverseWords("Hello World Java"));
    }
}
