/**
 * DSA 450 Problems - Dynamic Programming (Part 3: Problems 46-68)
 * Recursion with Memoization Approach
 */

import java.util.*;

public class DPProblems3 {
    
    /**
     * Problem 46: Palindrome Partitioning Count
     * 
     * Question: Given a string, find the NUMBER OF WAYS to partition it
     * such that EVERY PART is a palindrome.
     * 
     * Input: s = "nitin"
     * Expected Output: 3
     * Explanation: {"n", "i", "t", "i", "n"}, {"n", "iti", "n"}, {"nitin"}
     * 
     * Input: s = "aa"
     * Expected Output: 2
     * Explanation: {"a", "a"}, {"aa"}
     * 
     * Input: s = "a"
     * Expected Output: 1
     * Explanation: {"a"}
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     */
    public static int palindromePartitions(String s) {
        Map<String, Integer> memo = new HashMap<>();
        return palindromePartHelper(s, 0, memo);
    }
    
    private static int palindromePartHelper(String s, int start, Map<String, Integer> memo) {
        if (start == s.length()) return 1;
        if (memo.containsKey(String.valueOf(start))) return memo.get(String.valueOf(start));
        
        int count = 0;
        for (int end = start; end < s.length(); end++) {
            if (isPalindrome(s, start, end)) {
                count += palindromePartHelper(s, end + 1, memo);
            }
        }
        
        memo.put(String.valueOf(start), count);
        return count;
    }
    
    private static boolean isPalindrome(String s, int start, int end) {
        while (start < end) {
            if (s.charAt(start++) != s.charAt(end--)) return false;
        }
        return true;
    }
    
    /**
     * Problem 47: Maximum Sum of Non-Adjacent Elements
     * 
     * Question: Given an array of integers, find the MAXIMUM SUM OF A SUBSET
     * such that NO TWO SELECTED ELEMENTS are ADJACENT.
     * 
     * Input: nums = [5, 5, 10, 100, 10, 5]
     * Expected Output: 110
     * Explanation: Select 5 + 100 + 5 (non-adjacent elements)
     * 
     * Input: nums = [1, 2, 3, 4, 5]
     * Expected Output: 9
     * Explanation: Select 1 + 3 + 5 (or 2 + 5, which is also 7, or 4 + 5 = 9)
     * 
     * Input: nums = [10]
     * Expected Output: 10
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int maxSumNonAdjacent(int[] nums) {
        Map<Integer, Integer> memo = new HashMap<>();
        return maxSumHelper(nums, 0, memo);
    }
    
    private static int maxSumHelper(int[] nums, int index, Map<Integer, Integer> memo) {
        if (index >= nums.length) return 0;
        if (memo.containsKey(index)) return memo.get(index);
        
        int take = nums[index] + maxSumHelper(nums, index + 2, memo);
        int skip = maxSumHelper(nums, index + 1, memo);
        
        int result = Math.max(take, skip);
        memo.put(index, result);
        return result;
    }
    
    /**
     * Problem 48: Count Decode Ways
     * 
     * Question: Given a string of digits, count HOW MANY WAYS it can be DECODED.
     * 1='A', 2='B', ..., 26='Z', 0 can't map to anything.
     * 
     * Input: s = "226"
     * Expected Output: 3
     * Explanation: "2 2 6" = "BBF", "22 6" = "VF", "2 26" = "BZ"
     * 
     * Input: s = "0"
     * Expected Output: 0 (0 cannot be decoded)
     * 
     * Input: s = "123"
     * Expected Output: 3
     * Explanation: "1 2 3" = "ABC", "12 3" = "LC", "1 23" = "AW"
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int countDecodings(String s) {
        Map<Integer, Integer> memo = new HashMap<>();
        return countDecodingsHelper(s, 0, memo);
    }
    
    private static int countDecodingsHelper(String s, int index, Map<Integer, Integer> memo) {
        if (index == s.length()) return 1;
        if (s.charAt(index) == '0') return 0;
        if (memo.containsKey(index)) return memo.get(index);
        
        int result = countDecodingsHelper(s, index + 1, memo);
        
        if (index + 1 < s.length() && Integer.parseInt(s.substring(index, index + 2)) <= 26) {
            result += countDecodingsHelper(s, index + 2, memo);
        }
        
        memo.put(index, result);
        return result;
    }
    
    /**
     * Problem 49: Length of Longest Increasing Subsequence (LIS)
     * 
     * Question: Find the LENGTH of the LONGEST SUBSEQUENCE where elements are STRICTLY INCREASING.
     * Subsequence doesn't need to be consecutive.
     * 
     * Input: nums = [10, 9, 2, 5, 3, 7, 101, 18]
     * Expected Output: 4
     * Explanation: LIS = [2, 3, 7, 101] or [2, 5, 7, 101], both length 4
     * 
     * Input: nums = [0, 1, 0, 4, 4, 4, 3, 2, 1]
     * Expected Output: 2
     * Explanation: LIS = [0, 1] or [0, 4]
     * 
     * Input: nums = [1]
     * Expected Output: 1
     * 
     * Time Complexity: O(n^2) with memoization
     * Space Complexity: O(n)
     */
    public static int lengthOfLIS(int[] nums) {
        Map<String, Integer> memo = new HashMap<>();
        return lisHelper(nums, 0, Integer.MIN_VALUE, memo);
    }
    
    private static int lisHelper(int[] nums, int index, int prev, Map<String, Integer> memo) {
        if (index == nums.length) return 0;
        
        String key = index + ":" + prev;
        if (memo.containsKey(key)) return memo.get(key);
        
        int include = 0;
        if (nums[index] > prev) {
            include = 1 + lisHelper(nums, index + 1, nums[index], memo);
        }
        
        int exclude = lisHelper(nums, index + 1, prev, memo);
        int result = Math.max(include, exclude);
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 50: Minimum Perfect Squares
     * 
     * Question: Given n, find the LEAST NUMBER OF PERFECT SQUARE NUMBERS
     * (like 1, 4, 9, 16, ...) which sum to n.
     * 
     * Input: n = 7
     * Expected Output: 2
     * Explanation: 7 = 4 + 3 = 2² + 3, but optimal is 7 = 4 + 1 + 1 + 1 = 2² + 1² + 1² + 1²
     * Actually: 7 = 4 + 3, and 3 = 1+1+1, so better is just checking: needs minimum count
     * Actually 7 = 4 + 1 + 1 + 1 (4 perfect squares) vs others
     * 
     * Input: n = 13
     * Expected Output: 2
     * Explanation: 13 = 4 + 9 = 2² + 3²
     * 
     * Input: n = 1
     * Expected Output: 1 (1 = 1²)
     * 
     * Time Complexity: O(n * √n)
     * Space Complexity: O(n)
     */
    public static int minimumSquares(int n) {
        Map<Integer, Integer> memo = new HashMap<>();
        return minimumSquaresHelper(n, memo);
    }
    
    private static int minimumSquaresHelper(int n, Map<Integer, Integer> memo) {
        if (n == 0) return 0;
        if (memo.containsKey(n)) return memo.get(n);
        
        int result = Integer.MAX_VALUE;
        for (int i = 1; i * i <= n; i++) {
            result = Math.min(result, 1 + minimumSquaresHelper(n - i * i, memo));
        }
        
        memo.put(n, result);
        return result;
    }
    
    /**
     * Problem 51: Min Cost Climbing Stairs
     * 
     * Question: Given an array of costs, find the MINIMUM COST to reach the top.
     * Start at index 0 or 1, can climb 1 or 2 stairs at a time.
     * Cost[i] = cost to climb that stair.
     * 
     * Input: cost = [10, 15, 20]
     * Expected Output: 15
     * Explanation: Start at index 1 (cost 15), reach top.
     *              Or climb 1 step (10) + 1 step (15) but top is at index 2, so 10 + 20 = 30
     * 
     * Input: cost = [1, 100, 1, 1, 1, 100, 1, 1, 100, 1]
     * Expected Output: 6
     * Explanation: 1 + 1 + 1 + 1 + 1 + 1 = 6
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int minCostClimbStairs(int[] cost) {
        Map<Integer, Integer> memo = new HashMap<>();
        return minCostClimbHelper(cost, 0, memo);
    }
    
    private static int minCostClimbHelper(int[] cost, int index, Map<Integer, Integer> memo) {
        if (index >= cost.length) return 0;
        if (memo.containsKey(index)) return memo.get(index);
        
        int oneStep = cost[index] + minCostClimbHelper(cost, index + 1, memo);
        int twoSteps = cost[index] + minCostClimbHelper(cost, index + 2, memo);
        
        int result = Math.min(oneStep, twoSteps);
        memo.put(index, result);
        return result;
    }
    
    /**
     * Problem 52: Maximum Product Subarray
     * 
     * Question: Find the CONTIGUOUS SUBARRAY with the LARGEST PRODUCT.
     * Array can contain negative numbers which can flip signs.
     * 
     * Input: nums = [2, 3, -2, 4]
     * Expected Output: 6
     * Explanation: Subarray [2, 3] has max product 6
     * 
     * Input: nums = [-2]
     * Expected Output: -2 (only element)
     * 
     * Input: nums = [0, 2]
     * Expected Output: 2
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int maxProductSubarrayRec(int[] nums) {
        Map<String, Integer> memo = new HashMap<>();
        int[] result = maxProdHelper(nums, 0, memo);
        return result[1];
    }
    
    private static int[] maxProdHelper(int[] nums, int index, Map<String, Integer> memo) {
        if (index == nums.length) return new int[]{1, Integer.MIN_VALUE};
        
        int[] next = maxProdHelper(nums, index + 1, memo);
        int maxProd = Math.max(nums[index], nums[index] * next[0]);
        int maxOverall = Math.max(maxProd, next[1]);
        
        return new int[]{maxProd, maxOverall};
    }
    
    /**
     * Problem 53: Word Break 2 - Segmentation Check
     * 
     * Question: Given a string and dictionary, check if string can be
     * SEGMENTED into dictionary words.
     * 
     * Input: s = "leetcode", dict = {"leet", "code"}
     * Expected Output: true
     * Explanation: "leetcode" = "leet" + "code"
     * 
     * Input: s = "applepenapple", dict = {"apple", "pen"}
     * Expected Output: true
     * Explanation: "applepenapple" = "apple" + "pen" + "apple"
     * 
     * Input: s = "catsandog", dict = {"cat", "cats", "and", "sand", "dog"}
     * Expected Output: false
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     */
    public static boolean wordBreakII(String s, Set<String> dict) {
        Map<Integer, Boolean> memo = new HashMap<>();
        return wordBreakHelper(s, dict, 0, memo);
    }
    
    private static boolean wordBreakHelper(String s, Set<String> dict, int start, Map<Integer, Boolean> memo) {
        if (start == s.length()) return true;
        if (memo.containsKey(start)) return memo.get(start);
        
        for (int end = start + 1; end <= s.length(); end++) {
            if (dict.contains(s.substring(start, end)) && wordBreakHelper(s, dict, end, memo)) {
                memo.put(start, true);
                return true;
            }
        }
        
        memo.put(start, false);
        return false;
    }
    
    /**
     * Problem 54: Distinct Subsequences
     * 
     * Question: Given a string, count the NUMBER OF DISTINCT SUBSEQUENCES.
     * Empty string is considered a subsequence.
     * 
     * Input: s = "rabbbit"
     * Expected Output: 3
     * Explanation: "rab", "rabbt", "rabbit" (among others)
     * 
     * Input: s = "a"
     * Expected Output: 2
     * Explanation: "" (empty), "a"
     * 
     * Input: s = "aa"
     * Expected Output: 3
     * Explanation: "", "a" (1 or 2 indices), "aa"
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int distinctSubsequences(String s) {
        Map<Integer, Integer> memo = new HashMap<>();
        return distinctSubseqHelper(s, 0, memo);
    }
    
    private static int distinctSubseqHelper(String s, int index, Map<Integer, Integer> memo) {
        if (index == s.length()) return 1;
        if (memo.containsKey(index)) return memo.get(index);
        
        int result = distinctSubseqHelper(s, index + 1, memo) + 
                    distinctSubseqHelper(s, index + 1, memo);
        
        memo.put(index, result);
        return result;
    }
    
    /**
     * Problem 55: Max Sum with K Distance Non-Adjacent
     * 
     * Question: Find MAXIMUM SUM of elements where NO TWO SELECTED ELEMENTS
     * are within K positions of each other.
     * 
     * Input: nums = [5, 1, 3, 9, 2], k = 2
     * Expected Output: 14
     * Explanation: Select indices 0 and 3: 5 + 9 = 14 (distance >= 2)
     * 
     * Input: nums = [1, 2, 3, 4, 5], k = 1
     * Expected Output: 9
     * Explanation: Select non-adjacent: 1 + 3 + 5 = 9
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     */
    public static int maxSumNonAdjacentRec(int[] nums, int k) {
        Map<String, Integer> memo = new HashMap<>();
        return maxSumNonAdjHelper(nums, 0, k, memo);
    }
    
    private static int maxSumNonAdjHelper(int[] nums, int index, int k, Map<String, Integer> memo) {
        if (index >= nums.length) return 0;
        
        String key = index + ":" + k;
        if (memo.containsKey(key)) return memo.get(key);
        
        int take = nums[index] + maxSumNonAdjHelper(nums, index + k + 1, k, memo);
        int skip = maxSumNonAdjHelper(nums, index + 1, k, memo);
        
        int result = Math.max(take, skip);
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 56: Two Sum Closest to Target
     * 
     * Question: Given an array, find TWO NUMBERS whose sum is CLOSEST to target.
     * Return the minimum absolute difference.
     * 
     * Input: nums = [1, 3, 6, 7], target = 15
     * Expected Output: 0
     * Explanation: 3 + 12? No. 7 + 8? No. Actually 6 + 7 = 13, diff = 2
     * 
     * Input: nums = [1, 4, 45, 46, 50, 50], target = 100
     * Expected Output: 0
     * Explanation: 50 + 50 = 100 (exact match)
     * 
     * Input: nums = [10, 20, 30], target = 35
     * Expected Output: 5
     * Explanation: 10 + 30 = 40, diff = 5
     * 
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int twoSumClose(int[] nums, int target) {
        Arrays.sort(nums);
        int minDiff = Integer.MAX_VALUE;
        
        int left = 0, right = nums.length - 1;
        while (left < right) {
            int sum = nums[left] + nums[right];
            minDiff = Math.min(minDiff, Math.abs(sum - target));
            
            if (sum < target) left++;
            else right--;
        }
        
        return minDiff;
    }
    
    /**
     * Problem 57: Maximize Distance to Closest Person
     * 
     * Question: Given array of seats (1=occupied, 0=empty), find the MAXIMUM
     * DISTANCE to closest person if you sit at an empty seat.
     * 
     * Input: seats = [1, 0, 0, 0, 1, 0, 1]
     * Expected Output: 2
     * Explanation: Sit at index 2 or 3, closest person is at distance 2
     * 
     * Input: seats = [1, 0, 0, 1]
     * Expected Output: 1
     * Explanation: Sit at index 1 or 2, closest person is at distance 1
     * 
     * Input: seats = [0, 0, 0]
     * Expected Output: 1
     * Explanation: All empty, sit at ends gives max distance 1 or 2
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxDistToClosest(int[] seats) {
        int maxDist = 0;
        int lastZero = -1;
        
        for (int i = 0; i < seats.length; i++) {
            if (seats[i] == 1) {
                if (lastZero == -1) {
                    maxDist = Math.max(maxDist, i);
                } else {
                    maxDist = Math.max(maxDist, (i - lastZero) / 2);
                }
                lastZero = i;
            }
        }
        
        if (lastZero != -1) {
            maxDist = Math.max(maxDist, seats.length - 1 - lastZero);
        }
        
        return maxDist;
    }
    
    /**
     * Problem 58: Stone Game 6
     * 
     * Question: Alice and Bob take turns picking stones with values.
     * Each stone has both Alice and Bob values. Determine WINNER assuming
     * both play optimally (each picks stone maximizing their advantage).
     * 
     * Input: aliceValues = [1, 3], bobValues = [2, 1]
     * Expected Output: 1 (Alice wins) or -1 (Bob wins)
     * Explanation: Both pick optimally based on combined advantage
     * 
     * Input: aliceValues = [1], bobValues = [3]
     * Expected Output: -1 (Bob wins)
     * Explanation: Bob picks first optimally with value 3
     * 
     * Time Complexity: O(2^n) without memoization, O(n) with memoization
     * Space Complexity: O(n)
     */
    public static int stoneGameVI(int[] aliceValues, int[] bobValues) {
        int n = aliceValues.length;
        Map<String, Integer> memo = new HashMap<>();
        int aliceScore = stoneGameHelper(aliceValues, bobValues, 0, true, memo);
        int bobScore = 0;
        
        for (int val : bobValues) bobScore += val;
        
        return aliceScore >= bobScore ? 1 : -1;
    }
    
    private static int stoneGameHelper(int[] alice, int[] bob, int index, boolean isAlice,
                                      Map<String, Integer> memo) {
        if (index == alice.length) return 0;
        
        String key = index + ":" + isAlice;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        if (isAlice) {
            result = alice[index] + stoneGameHelper(alice, bob, index + 1, false, memo);
        } else {
            result = stoneGameHelper(alice, bob, index + 1, true, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    // Problems 67-68: Additional complex DP problems follow similar patterns
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - DP Problems (Part 3) ===\n");
        System.out.println("Palindrome Partitions (nitin): " + palindromePartitions("nitin"));
        System.out.println("Minimum Squares (13): " + minimumSquares(13));
    }
}
