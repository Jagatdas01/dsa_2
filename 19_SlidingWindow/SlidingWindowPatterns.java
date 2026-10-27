/**
 * DSA Pattern: Sliding Window
 * Topics: Fixed window, variable window, substring problems
 */

import java.util.*;

public class SlidingWindowPatterns {
    
    /**
     * Problem: Maximum Sum of Subarray of Fixed Size k
     * Find maximum sum among all contiguous subarrays of size k
     * 
     * Input: nums = [1, 4, 2, 10, 2, 3, 1, 0, 20], k = 4
     * Expected Output: 24 (subarray [10, 2, 3, 1] has sum 16, but [2,3,1,0,20] wait wrong size)
     * Correctly: windows of 4: [1,4,2,10]=17, [4,2,10,2]=18, [2,10,2,3]=17, [10,2,3,1]=16, [2,3,1,0]=6, [3,1,0,20]=24
     * 
     * Input: nums = [1, 2, 3, 4], k = 2
     * Expected Output: 7 (subarray [3, 4])
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxSumSubarray(int[] nums, int k) {
        if (nums.length < k) return 0;
        
        int windowSum = 0;
        for (int i = 0; i < k; i++) {
            windowSum += nums[i];
        }
        
        int maxSum = windowSum;
        
        for (int i = k; i < nums.length; i++) {
            windowSum = windowSum - nums[i - k] + nums[i];
            maxSum = Math.max(maxSum, windowSum);
        }
        
        return maxSum;
    }
    
    /**
     * Problem: Longest Substring Without Repeating Characters
     * Find length of longest substring with all unique characters
     * 
     * Input: s = "abcabcbb"
     * Expected Output: 3 (substring "abc")
     * 
     * Input: s = "bbbbb"
     * Expected Output: 1 (substring "b")
     * 
     * Input: s = "pwwkew"
     * Expected Output: 3 (substring "wke")
     * 
     * Time Complexity: O(n) - two pointers
     * Space Complexity: O(min(n, m)) - character map size
     */
    public static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLength = 0;
        int start = 0;
        
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= start) {
                start = lastSeen.get(c) + 1;
            }
            
            maxLength = Math.max(maxLength, end - start + 1);
            lastSeen.put(c, end);
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Minimum Window Substring
     * Find smallest substring containing all characters of target string
     * 
     * Input: s = "ADOBECODEBANC", t = "ABC"
     * Expected Output: "BANC" (contains all of A, B, C)
     * 
     * Input: s = "a", t = "a"
     * Expected Output: "a"
     * 
     * Input: s = "a", t = "aa"
     * Expected Output: "" (impossible - not enough characters)
     * 
     * Time Complexity: O(n + m) - two pointer + hash map
     * Space Complexity: O(m) where m is charset size
     */
    public static String minWindowSubstring(String s, String t) {
        if (t.isEmpty() || s.isEmpty() || s.length() < t.length()) {
            return "";
        }
        
        Map<Character, Integer> dictT = new HashMap<>();
        for (char c : t.toCharArray()) {
            dictT.put(c, dictT.getOrDefault(c, 0) + 1);
        }
        
        int required = dictT.size();
        int formed = 0;
        Map<Character, Integer> windowCounts = new HashMap<>();
        
        int[] ans = {Integer.MAX_VALUE, 0, 0};
        int left = 0;
        
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowCounts.put(c, windowCounts.getOrDefault(c, 0) + 1);
            
            if (dictT.containsKey(c) && windowCounts.get(c).equals(dictT.get(c))) {
                formed++;
            }
            
            while (left <= right && formed == required) {
                c = s.charAt(left);
                
                if (right - left + 1 < ans[0]) {
                    ans[0] = right - left + 1;
                    ans[1] = left;
                    ans[2] = right;
                }
                
                windowCounts.put(c, windowCounts.get(c) - 1);
                if (dictT.containsKey(c) && windowCounts.get(c) < dictT.get(c)) {
                    formed--;
                }
                
                left++;
            }
        }
        
        return ans[0] == Integer.MAX_VALUE ? "" : s.substring(ans[1], ans[2] + 1);
    }
    
    /**
     * Problem: Fruits into Baskets (at most 2 types)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int totalFruit(int[] fruits) {
        Map<Integer, Integer> fruitCount = new HashMap<>();
        int maxLength = 0;
        int left = 0;
        
        for (int right = 0; right < fruits.length; right++) {
            fruitCount.put(fruits[right], fruitCount.getOrDefault(fruits[right], 0) + 1);
            
            while (fruitCount.size() > 2) {
                fruitCount.put(fruits[left], fruitCount.get(fruits[left]) - 1);
                if (fruitCount.get(fruits[left]) == 0) {
                    fruitCount.remove(fruits[left]);
                }
                left++;
            }
            
            maxLength = Math.max(maxLength, right - left + 1);
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Permutation in String
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean permutationInString(String s1, String s2) {
        if (s1.length() > s2.length()) return false;
        
        int[] s1Count = new int[26];
        int[] s2Count = new int[26];
        
        for (char c : s1.toCharArray()) {
            s1Count[c - 'a']++;
        }
        
        for (int i = 0; i < s2.length(); i++) {
            s2Count[s2.charAt(i) - 'a']++;
            
            if (i >= s1.length()) {
                s2Count[s2.charAt(i - s1.length()) - 'a']--;
            }
            
            if (Arrays.equals(s1Count, s2Count)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Problem: Longest Repeating Character Replacement
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int characterReplacement(String s, int k) {
        int[] charCount = new int[26];
        int maxLength = 0;
        int maxFreq = 0;
        int left = 0;
        
        for (int right = 0; right < s.length(); right++) {
            charCount[s.charAt(right) - 'A']++;
            maxFreq = Math.max(maxFreq, charCount[s.charAt(right) - 'A']);
            
            int windowLength = right - left + 1;
            int numToReplace = windowLength - maxFreq;
            
            if (numToReplace > k) {
                charCount[s.charAt(left) - 'A']--;
                left++;
            } else {
                maxLength = Math.max(maxLength, right - left + 1);
            }
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Max Consecutive Ones III
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int longestOnes(int[] nums, int k) {
        int maxLength = 0;
        int zeroCount = 0;
        int left = 0;
        
        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) {
                zeroCount++;
            }
            
            while (zeroCount > k) {
                if (nums[left] == 0) {
                    zeroCount--;
                }
                left++;
            }
            
            maxLength = Math.max(maxLength, right - left + 1);
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Contains Duplicate II
     * Time Complexity: O(n)
     * Space Complexity: O(min(n, k))
     */
    public static boolean containsNearbyDuplicate(int[] nums, int k) {
        Set<Integer> window = new HashSet<>();
        
        for (int i = 0; i < nums.length; i++) {
            if (i > k) {
                window.remove(nums[i - k - 1]);
            }
            
            if (window.contains(nums[i])) {
                return true;
            }
            
            window.add(nums[i]);
        }
        
        return false;
    }
    
    public static void main(String[] args) {
        int[] nums = {1, 4, 2, 10, 2, 3, 1, 0, 20};
        System.out.println("Max sum subarray (k=4): " + maxSumSubarray(nums, 4));
        
        System.out.println("Longest substring without repeating: " + lengthOfLongestSubstring("abcabcbb"));
        
        System.out.println("Min window substring: " + minWindowSubstring("ADOBECODEBANC", "ABC"));
        
        int[] fruits = {1, 2, 1};
        System.out.println("Fruits into baskets: " + totalFruit(fruits));
        
        System.out.println("Permutation in string: " + permutationInString("ab", "eidbaooo"));
        
        System.out.println("Character replacement: " + characterReplacement("ABAB", 2));
        
        int[] nums2 = {1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0};
        System.out.println("Max ones with k=2 flips: " + longestOnes(nums2, 2));
        
        int[] nums3 = {99, 99};
        System.out.println("Contains nearby duplicate (k=2): " + containsNearbyDuplicate(nums3, 2));
    }
}
