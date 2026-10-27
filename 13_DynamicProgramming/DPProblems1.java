/**
 * DSA 450 Problems - Dynamic Programming (Part 1: Problems 1-22)
 * Recursion with Memoization Approach (Top-Down)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class DPProblems1 {
    
    /**
     * Problem 1: Coin Change - Minimum coins needed (Recursion + Memoization)
     * 
     * Question: Given an array of coin denominations and a target amount,
     * find the MINIMUM NUMBER of coins needed to make that amount.
     * Each coin can be used unlimited times.
     * 
     * Input: coins = [1, 2, 5], amount = 5
     * Expected Output: 1 (one coin of 5)
     * Explanation: 5 = 5 (1 coin)
     * 
     * Input: coins = [2], amount = 3
     * Expected Output: -1 (impossible)
     * 
     * Input: coins = [10], amount = 10
     * Expected Output: 1 (one coin of 10)
     * 
     * Approach: For each coin, decide to include it or not, recurse on remaining amount
     * Time Complexity: O(n * amount) with memoization
     * Space Complexity: O(amount) for memo + recursion stack
     */
    public static int coinChange(int[] coins, int amount) {
        Map<Integer, Integer> memo = new HashMap<>();
        int result = coinChangeRecursive(coins, amount, memo);
        return result;
    }
    
    private static int coinChangeRecursive(int[] coins, int amount, Map<Integer, Integer> memo) {
        // Base case
        if (amount == 0) return 0;
        if (amount < 0) return -1;
        
        // Check memo
        if (memo.containsKey(amount)) return memo.get(amount);
        
        int minCoins = Integer.MAX_VALUE;
        
        // Try each coin
        for (int coin : coins) {
            int result = coinChangeRecursive(coins, amount - coin, memo);
            if (result >= 0) {
                minCoins = Math.min(minCoins, result + 1);
            }
        }
        
        // Store in memo
        int answer = (minCoins == Integer.MAX_VALUE) ? -1 : minCoins;
        memo.put(amount, answer);
        return answer;
    }
    
    /**
     * Problem 2: 0/1 Knapsack - Maximum value with weight limit
     * 
     * Question: Given n items with weights and values, and a knapsack of capacity W,
     * find the MAXIMUM VALUE that can be obtained by selecting items.
     * Each item can only be selected ONCE (0/1 - either take it or leave it).
     * 
     * Input: weights = [2, 3, 4], values = [3, 4, 5], capacity = 5
     * Expected Output: 10 (select items with weights 2 and 3 = value 3 + 4)
     * 
     * Input: weights = [10, 20], values = [5, 10], capacity = 15
     * Expected Output: 10 (select item with weight 20 exceeds, select weight 10)
     * 
     * Approach: For each item, decide to include or exclude it
     * Time Complexity: O(n * capacity) with memoization
     * Space Complexity: O(n * capacity) for memo + recursion stack
     */
    public static int knapsack(int[] weights, int[] values, int capacity) {
        Map<String, Integer> memo = new HashMap<>();
        return knapsackRecursive(weights, values, capacity, 0, memo);
    }
    
    private static int knapsackRecursive(int[] weights, int[] values, int capacity,
                                        int index, Map<String, Integer> memo) {
        // Base case
        if (index == weights.length || capacity <= 0) return 0;
        
        String key = index + ":" + capacity;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        
        // If current item can fit
        if (weights[index] <= capacity) {
            int include = values[index] + knapsackRecursive(weights, values,
                         capacity - weights[index], index + 1, memo);
            int exclude = knapsackRecursive(weights, values, capacity, index + 1, memo);
            result = Math.max(include, exclude);
        } else {
            // Cannot include, so exclude
            result = knapsackRecursive(weights, values, capacity, index + 1, memo);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 3: Binomial Coefficient C(n, k) - Combination calculation
     * 
     * Question: Calculate C(n, k) = n! / (k! * (n-k)!)
     * How many ways to choose k items from n items (order doesn't matter).
     * 
     * Input: n = 4, k = 2
     * Expected Output: 6 (C(4,2) = 4!/(2!*2!) = 6)
     * Combinations: {1,2}, {1,3}, {1,4}, {2,3}, {2,4}, {3,4}
     * 
     * Input: n = 5, k = 3
     * Expected Output: 10 (C(5,3) = 10)
     * 
     * Input: n = 5, k = 0
     * Expected Output: 1 (only 1 way to choose 0 items)
     * 
     * Approach: Pascal's triangle using recursion C(n,k) = C(n-1,k-1) + C(n-1,k)
     * Time Complexity: O(n * k) with memoization
     * Space Complexity: O(n * k)
     */
    public static long binomialCoefficient(int n, int k) {
        Map<String, Long> memo = new HashMap<>();
        return binomialRecursive(n, k, memo);
    }
    
    private static long binomialRecursive(int n, int k, Map<String, Long> memo) {
        if (k == 0 || k == n) return 1;
        
        String key = n + ":" + k;
        if (memo.containsKey(key)) return memo.get(key);
        
        long result = binomialRecursive(n - 1, k - 1, memo) + 
                     binomialRecursive(n - 1, k, memo);
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 4: Edit Distance - Levenshtein Distance
     * 
     * Question: Find MINIMUM number of operations to convert string s1 to s2.
     * Operations allowed: Insert, Delete, or Replace a character.
     * 
     * Input: s1 = "horse", s2 = "ros"
     * Expected Output: 3
     * Explanation: horse → rorse (replace h) → rose (delete r) → ros (delete e)
     * 
     * Input: s1 = "abc", s2 = "abc"
     * Expected Output: 0 (already equal)
     * 
     * Input: s1 = "", s2 = "abc"
     * Expected Output: 3 (insert 3 characters)
     * 
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n)
     */
    public static int editDistance(String s1, String s2) {
        Map<String, Integer> memo = new HashMap<>();
        return editDistanceRecursive(s1, s2, s1.length(), s2.length(), memo);
    }
    
    private static int editDistanceRecursive(String s1, String s2, int m, int n,
                                            Map<String, Integer> memo) {
        // Base cases
        if (m == 0) return n;
        if (n == 0) return m;
        
        String key = m + ":" + n;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        
        if (s1.charAt(m - 1) == s2.charAt(n - 1)) {
            result = editDistanceRecursive(s1, s2, m - 1, n - 1, memo);
        } else {
            int replace = editDistanceRecursive(s1, s2, m - 1, n - 1, memo);
            int delete = editDistanceRecursive(s1, s2, m - 1, n, memo);
            int insert = editDistanceRecursive(s1, s2, m, n - 1, memo);
            
            result = 1 + Math.min(replace, Math.min(delete, insert));
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 5: Subset Sum - Check if target sum is achievable
     * 
     * Question: Given an array of integers and a target sum,
     * determine if there EXISTS A SUBSET with the given sum.
     * 
     * Input: arr = [3, 34, 4, 12, 5, 2], sum = 9
     * Expected Output: true
     * Explanation: Subset {4, 5} has sum 9
     * 
     * Input: arr = [3, 34, 4, 12, 5, 2], sum = 30
     * Expected Output: false (no subset sums to 30)
     * 
     * Input: arr = [1, 5, 11, 5], sum = 11
     * Expected Output: true (Subset {11})
     * 
     * Time Complexity: O(n * sum) with memoization
     * Space Complexity: O(n * sum)
     */
    public static boolean subsetSum(int[] arr, int sum) {
        Map<String, Boolean> memo = new HashMap<>();
        return subsetSumRecursive(arr, sum, 0, memo);
    }
    
    private static boolean subsetSumRecursive(int[] arr, int sum, int index,
                                             Map<String, Boolean> memo) {
        // Base cases
        if (sum == 0) return true;
        if (index == arr.length) return false;
        
        String key = index + ":" + sum;
        if (memo.containsKey(key)) return memo.get(key);
        
        // Include current element
        boolean include = subsetSumRecursive(arr, sum - arr[index], index + 1, memo);
        
        // Exclude current element
        boolean exclude = subsetSumRecursive(arr, sum, index + 1, memo);
        
        boolean result = include || exclude;
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 6: Matrix Chain Multiplication - Optimal parenthesization
     * 
     * Question: Given sequence of matrices, find MINIMUM number of scalar multiplications.
     * Matrix A(p x q) * Matrix B(q x r) requires p*q*r multiplications.
     * Different parenthesization = different total multiplications.
     * 
     * Input: matrices dimensions = [10, 20, 30, 40]
     * Matrices: A(10x20), B(20x30), C(30x40)
     * Expected Output: 18000
     * Explanation: (A*B)*C = 10*20*30 + 10*30*40 = 6000 + 12000 = 18000
     *              A*(B*C) = 20*30*40 + 10*20*40 = 24000 + 8000 = 32000 (worse)
     * 
     * Time Complexity: O(n^3) with memoization
     * Space Complexity: O(n^2)
     */
    public static int matrixChainMultiplication(int[] dimensions) {
        Map<String, Integer> memo = new HashMap<>();
        return matrixChainRecursive(dimensions, 1, dimensions.length - 1, memo);
    }
    
    private static int matrixChainRecursive(int[] dimensions, int i, int j,
                                           Map<String, Integer> memo) {
        if (i >= j) return 0;
        
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        int minOps = Integer.MAX_VALUE;
        
        for (int k = i; k < j; k++) {
            int ops = matrixChainRecursive(dimensions, i, k, memo) +
                     matrixChainRecursive(dimensions, k + 1, j, memo) +
                     dimensions[i - 1] * dimensions[k] * dimensions[j];
            minOps = Math.min(minOps, ops);
        }
        
        memo.put(key, minOps);
        return minOps;
    }
    
    /**
     * Problem 7: Longest Common Subsequence (LCS)
     * 
     * Question: Find the LENGTH of the LONGEST SUBSEQUENCE common to both strings.
     * Subsequence = characters in order but not necessarily consecutive.
     * 
     * Input: s1 = "AGGTAB", s2 = "GXTXAYB"
     * Expected Output: 4
     * Explanation: Common subsequence "GTAB" has length 4
     * 
     * Input: s1 = "abc", s2 = "abc"
     * Expected Output: 3 (entire string is LCS)
     * 
     * Input: s1 = "abc", s2 = "def"
     * Expected Output: 0 (no common characters)
     * 
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n)
     */
    public static int lcs(String s1, String s2) {
        Map<String, Integer> memo = new HashMap<>();
        return lcsRecursive(s1, s2, 0, 0, memo);
    }
    
    private static int lcsRecursive(String s1, String s2, int i, int j,
                                   Map<String, Integer> memo) {
        // Base case
        if (i == s1.length() || j == s2.length()) return 0;
        
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        
        if (s1.charAt(i) == s2.charAt(j)) {
            result = 1 + lcsRecursive(s1, s2, i + 1, j + 1, memo);
        } else {
            int skip1 = lcsRecursive(s1, s2, i + 1, j, memo);
            int skip2 = lcsRecursive(s1, s2, i, j + 1, memo);
            result = Math.max(skip1, skip2);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 8: Longest Increasing Subsequence (LIS)
     * 
     * Question: Find the LENGTH of the LONGEST SUBSEQUENCE\
     * where every element is STRICTLY GREATER than the previous one.\
     * Subsequence elements don't need to be consecutive.\
     * \
     * Input: arr = [10, 9, 2, 5, 3, 7, 101, 18]\
     * Expected Output: 4\
     * Explanation: LIS = [2, 3, 7, 101] (length 4)\
     * \
     * Input: arr = [0, 1, 0, 4, 4, 4, 3, 2, 1]\
     * Expected Output: 2\
     * Explanation: LIS = [0, 1] or [0, 4]\
     * \
     * Input: arr = [3, 10, 2, 1, 20]\
     * Expected Output: 3\
     * Explanation: LIS = [3, 10, 20]\
     * \
     * Time Complexity: O(n^2) with memoization\
     * Space Complexity: O(n)\
     */
    public static int lis(int[] arr) {
        Map<String, Integer> memo = new HashMap<>();
        return lisRecursive(arr, 0, Integer.MIN_VALUE, memo);
    }
    
    private static int lisRecursive(int[] arr, int index, int prevValue,
                                   Map<String, Integer> memo) {
        if (index == arr.length) return 0;
        
        String key = index + ":" + prevValue;
        if (memo.containsKey(key)) return memo.get(key);
        
        // Include current element if it's greater than previous
        int include = 0;
        if (arr[index] > prevValue) {
            include = 1 + lisRecursive(arr, index + 1, arr[index], memo);
        }
        
        // Exclude current element
        int exclude = lisRecursive(arr, index + 1, prevValue, memo);
        
        int result = Math.max(include, exclude);
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 9: Unbounded Knapsack
     * 
     * Question: Given n items with weights and values, and a knapsack capacity W,
     * find the MAXIMUM VALUE that can be obtained.
     * Each item can be selected UNLIMITED TIMES (unlike 0/1 knapsack).\
     * 
     * Input: weights = [2, 3, 4], values = [3, 4, 5], capacity = 5
     * Expected Output: 10 (select item 0 twice: 3+3+4 weight issue, or item 1 once + item 0 once = 4+3=7 value)\
     * \
     * Input: weights = [10], values = [5], capacity = 15
     * Expected Output: 5 (can only select one item as weight exceeds)
     * 
     * Time Complexity: O(n * capacity) with memoization
     * Space Complexity: O(capacity)
     */
    public static int unboundedKnapsack(int[] weights, int[] values, int capacity) {
        Map<Integer, Integer> memo = new HashMap<>();
        return unboundedRecursive(weights, values, capacity, 0, memo);
    }
    
    private static int unboundedRecursive(int[] weights, int[] values, int capacity,
                                         int index, Map<Integer, Integer> memo) {
        if (capacity <= 0 || index == weights.length) return 0;
        
        if (memo.containsKey(capacity)) return memo.get(capacity);
        
        int maxValue = 0;
        
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] <= capacity) {
                int value = values[i] + unboundedRecursive(weights, values,
                           capacity - weights[i], 0, memo);
                maxValue = Math.max(maxValue, value);
            }
        }
        
        memo.put(capacity, maxValue);
        return maxValue;
    }
    
    /**
     * Problem 10: Word Break Problem
     * 
     * Question: Given a string and a dictionary of words, determine if the string
     * can be SEGMENTED into dictionary words.\
     * 
     * Input: s = "leetcode", wordDict = ["leet", "code"]
     * Expected Output: true
     * Explanation: "leetcode" = "leet" + "code"
     * 
     * Input: s = "applepenapple", wordDict = ["apple", "pen"]
     * Expected Output: true
     * Explanation: "applepenapple" = "apple" + "pen" + "apple"
     * 
     * Input: s = "catsandog", wordDict = ["cat", "cats", "and", "sand", "dog"]
     * Expected Output: false (cannot be segmented)
     * 
     * Time Complexity: O(n^2) with memoization
     * Space Complexity: O(n)
     */
    public static boolean wordBreak(String s, Set<String> wordDict) {
        Map<Integer, Boolean> memo = new HashMap<>();
        return wordBreakRecursive(s, wordDict, 0, memo);
    }
    
    private static boolean wordBreakRecursive(String s, Set<String> wordDict,
                                             int index, Map<Integer, Boolean> memo) {
        if (index == s.length()) return true;
        
        if (memo.containsKey(index)) return memo.get(index);
        
        for (int end = index + 1; end <= s.length(); end++) {
            String word = s.substring(index, end);
            
            if (wordDict.contains(word)) {
                if (wordBreakRecursive(s, wordDict, end, memo)) {
                    memo.put(index, true);
                    return true;
                }
            }
        }
        
        memo.put(index, false);
        return false;
    }
    
    /**
     * Problem 11: Longest Palindromic Subsequence (LPS)
     * 
     * Question: Find the LENGTH of the LONGEST PALINDROMIC SUBSEQUENCE in a string.
     * A subsequence doesn't need to be consecutive; a palindrome reads same forwards/backwards.
     * 
     * Input: s = "bbbab"
     * Expected Output: 4
     * Explanation: LPS = "bbbb" (length 4)
     * 
     * Input: s = "abcdedcba"
     * Expected Output: 9 (entire string is palindrome)
     * 
     * Input: s = "abcde"
     * Expected Output: 1 (each character alone is palindrome)
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(n^2)
     */
    public static int longestPalindromicSubsequence(String s) {
        String reversed = new StringBuilder(s).reverse().toString();
        return lcs(s, reversed);
    }
    
    /**
     * Problem 12: Longest Palindromic Substring
     * 
     * Question: Find the LONGEST SUBSTRING (not subsequence) that is a PALINDROME.
     * A substring must be CONSECUTIVE characters in the original string.
     * 
     * Input: s = "babad"
     * Expected Output: "bab" or "aba" (both length 3)
     * 
     * Input: s = "racecar"
     * Expected Output: "racecar" (entire string is palindrome, length 7)
     * 
     * Input: s = "ac"
     * Expected Output: "a" or "c" (no palindrome longer than 1)
     * 
     * Time Complexity: O(n^2)
     * Space Complexity: O(n^2)
     */
    public static String longestPalindromicSubstring(String s) {
        Map<String, Boolean> memo = new HashMap<>();
        String longest = "";
        
        for (int i = 0; i < s.length(); i++) {
            for (int j = i; j < s.length(); j++) {
                String sub = s.substring(i, j + 1);
                if (isPalindromeRecursive(sub, 0, sub.length() - 1, memo)) {
                    if (sub.length() > longest.length()) {
                        longest = sub;
                    }
                }
            }
        }
        
        return longest;
    }
    
    private static boolean isPalindromeRecursive(String s, int left, int right,
                                                Map<String, Boolean> memo) {
        if (left >= right) return true;
        
        String key = left + ":" + right;
        if (memo.containsKey(key)) return memo.get(key);
        
        boolean result = (s.charAt(left) == s.charAt(right)) &&
                        isPalindromeRecursive(s, left + 1, right - 1, memo);
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 13: Maximum Sum of Increasing Subsequence
     * 
     * Question: Find the MAXIMUM SUM of a SUBSEQUENCE where elements are STRICTLY INCREASING.
     * 
     * Input: arr = [1, 101, 2, 3, 100, 4, 5]
     * Expected Output: 106
     * Explanation: Subsequence [1, 101, 4] gives sum 106, or [1, 2, 100, 3] not valid
     * Actually optimal is [1, 2, 3, 100] = 106
     * 
     * Input: arr = [1, 2, 3, 4, 5]
     * Expected Output: 15
     * Explanation: Entire array is increasing, sum = 1+2+3+4+5 = 15
     * 
     * Input: arr = [5, 4, 3, 2, 1]
     * Expected Output: 5
     * Explanation: Only single elements can form increasing subsequence
     * 
     * Time Complexity: O(n^2) with memoization
     * Space Complexity: O(n)
     */
    public static int maxSumIncreasingSubsequence(int[] arr) {
        Map<String, Integer> memo = new HashMap<>();
        int result = maxSumRecursive(arr, 0, Integer.MIN_VALUE, memo);
        return Math.max(0, result);
    }
    
    private static int maxSumRecursive(int[] arr, int index, int prevValue,
                                      Map<String, Integer> memo) {
        if (index == arr.length) return 0;
        
        String key = index + ":" + prevValue;
        if (memo.containsKey(key)) return memo.get(key);
        
        // Include current element if it's greater than previous
        int include = Integer.MIN_VALUE;
        if (arr[index] > prevValue) {
            include = arr[index] + maxSumRecursive(arr, index + 1, arr[index], memo);
        }
        
        // Exclude current element
        int exclude = maxSumRecursive(arr, index + 1, prevValue, memo);
        
        int result = Math.max(include, exclude);
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 14: Egg Dropping Problem
     * 
     * Question: Given n eggs and k floors, find the MINIMUM NUMBER OF ATTEMPTS\
     * needed to determine the CRITICAL FLOOR (egg breaks from that floor onwards)\
     * in the worst case scenario.\
     * \
     * Input: eggs = 1, floors = 100\
     * Expected Output: 100\
     * Explanation: With 1 egg, must test sequentially from floor 1 to find critical floor.\
     * \
     * Input: eggs = 2, floors = 10\
     * Expected Output: 4\
     * Explanation: Optimal strategy takes 4 attempts in worst case.\
     * \
     * Input: eggs = 3, floors = 10\
     * Expected Output: 3\
     * \
     * Time Complexity: O(eggs * floors^2) with memoization\
     * Space Complexity: O(eggs * floors)\
     */
    public static int eggDrop(int eggs, int floors) {
        Map<String, Integer> memo = new HashMap<>();
        return eggDropRecursive(eggs, floors, memo);
    }
    
    private static int eggDropRecursive(int eggs, int floors,
                                       Map<String, Integer> memo) {
        if (floors <= 1) return floors;
        if (eggs == 1) return floors;
        
        String key = eggs + ":" + floors;
        if (memo.containsKey(key)) return memo.get(key);
        
        int minTrials = Integer.MAX_VALUE;
        
        for (int k = 1; k <= floors; k++) {
            // If breaks at k: search in 1 to k-1 with eggs-1
            // If doesn't break: search in k+1 to floors with eggs
            int worst = 1 + Math.max(
                eggDropRecursive(eggs - 1, k - 1, memo),
                eggDropRecursive(eggs, floors - k, memo)
            );
            
            minTrials = Math.min(minTrials, worst);
        }
        
        memo.put(key, minTrials);
        return minTrials;
    }
    
    /**
     * Problem 15: Partition Equal Subset Sum
     * 
     * Question: Determine if an array can be PARTITIONED into TWO SUBSETS\
     * with EQUAL SUM.\
     * \
     * Input: arr = [1, 5, 11, 5]\
     * Expected Output: true\
     * Explanation: Subset {11} and {5, 5, 1} both have sum 11\
     * \
     * Input: arr = [2, 2, 1, 1]\
     * Expected Output: true\
     * Explanation: Subset {2, 1} and {2, 1} both have sum 3\
     * \
     * Input: arr = [1, 2, 5]\
     * Expected Output: false (total sum 8 is odd, or cannot partition equally)\
     * \
     * Time Complexity: O(n * sum/2) with memoization\
     * Space Complexity: O(n * sum/2)\
     */
    public static boolean partitionEqualSubsetSum(int[] arr) {
        int sum = 0;
        for (int num : arr) sum += num;
        
        if (sum % 2 != 0) return false;
        
        return subsetSum(arr, sum / 2);
    }
    
    /**
     * Problem 16: Rod Cutting Problem
     * 
     * Question: Given a rod of length n and prices for each length,
     * find the MAXIMUM REVENUE by cutting the rod optimally.
     * 
     * Input: prices = [1, 5, 8, 9, 10, 17, 17, 20] (for length 1-8)
     *        rod length n = 4
     * Expected Output: 10
     * Explanation: Cut into 2+2 pieces, each price 5, total = 10
     *              or cut to length 1,1,1,1 = 1+1+1+1 = 4 (worse)
     * 
     * Input: prices = [1, 5, 8], rod length = 3
     * Expected Output: 9
     * Explanation: Cut into 1+1+1 or keep as 3? 1+1+1 = 3, or 1+2 = 1+5 = 6, or 3 = 8, best is 8
     * 
     * Time Complexity: O(n^2) with memoization
     * Space Complexity: O(n)
     */
    public static int rodCutting(int[] prices) {
        Map<Integer, Integer> memo = new HashMap<>();
        return rodCuttingRecursive(prices, prices.length, memo);
    }
    
    private static int rodCuttingRecursive(int[] prices, int length,
                                          Map<Integer, Integer> memo) {
        if (length <= 0) return 0;
        
        if (memo.containsKey(length)) return memo.get(length);
        
        int maxProfit = 0;
        
        for (int i = 1; i <= length; i++) {
            int profit = prices[i - 1] + rodCuttingRecursive(prices, length - i, memo);
            maxProfit = Math.max(maxProfit, profit);
        }
        
        memo.put(length, maxProfit);
        return maxProfit;
    }
    
    /**
     * Problem 17: Maximum Product Subarray
     * 
     * Question: Find the CONTIGUOUS SUBARRAY with the LARGEST PRODUCT.
     * Array can contain negative numbers, which can flip the sign of products.
     * 
     * Input: arr = [2, 3, -2, 4]
     * Expected Output: 6
     * Explanation: Subarray [2, 3] has max product 6
     * 
     * Input: arr = [-2]
     * Expected Output: -2 (only element)
     * 
     * Input: arr = [0, 2]
     * Expected Output: 2 (subarray [2])
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int maxProductSubarray(int[] arr) {
        Map<String, int[]> memo = new HashMap<>();
        int[] result = maxProductRecursive(arr, 0, memo);
        return result[1]; // Return max product
    }
    
    private static int[] maxProductRecursive(int[] arr, int index,
                                            Map<String, int[]> memo) {
        if (index == arr.length) {
            return new int[]{0, Integer.MIN_VALUE}; // {current product, max product}
        }
        
        String key = String.valueOf(index);
        if (memo.containsKey(key)) return memo.get(key);
        
        int[] nextResult = maxProductRecursive(arr, index + 1, memo);
        
        int maxProd = Math.max(arr[index], arr[index] * Math.abs(nextResult[0]));
        int maxOverall = Math.max(maxProd, nextResult[1]);
        
        int[] result = {maxProd, maxOverall};
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 18: Unique Paths in Matrix
     * 
     * Question: Given an m x n grid, find the NUMBER OF UNIQUE PATHS\
     * from top-left to bottom-right. Can only move RIGHT or DOWN.\
     * \
     * Input: m = 3, n = 7\
     * Expected Output: 28\
     * Explanation: 28 unique paths from (0,0) to (2,6)\
     * \
     * Input: m = 1, n = 1\
     * Expected Output: 1 (already at destination)\
     * \
     * Input: m = 3, n = 3\
     * Expected Output: 6\
     * \
     * Time Complexity: O(m * n)\
     * Space Complexity: O(m * n)\
     */
    public static int countPaths(int m, int n) {
        Map<String, Integer> memo = new HashMap<>();
        return countPathsRecursive(m - 1, n - 1, memo);
    }
    
    private static int countPathsRecursive(int m, int n,
                                          Map<String, Integer> memo) {
        if (m == 0 || n == 0) return 1;
        
        String key = m + ":" + n;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result = countPathsRecursive(m - 1, n, memo) +
                    countPathsRecursive(m, n - 1, memo);
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 20: Fibonacci Number
     * 
     * Question: Calculate the NTH FIBONACCI NUMBER where F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2).
     * Sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, ...
     * 
     * Input: n = 0
     * Expected Output: 0
     * 
     * Input: n = 10
     * Expected Output: 55
     * 
     * Input: n = 6
     * Expected Output: 8
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static long fibonacci(int n) {
        Map<Integer, Long> memo = new HashMap<>();
        return fibonacciRecursive(n, memo);
    }
    
    private static long fibonacciRecursive(int n, Map<Integer, Long> memo) {
        if (n <= 1) return n;
        
        if (memo.containsKey(n)) return memo.get(n);
        
        long result = fibonacciRecursive(n - 1, memo) +
                     fibonacciRecursive(n - 2, memo);
        
        memo.put(n, result);
        return result;
    }
    
    /**
     * Problem 22: Catalan Number
     * 
     * Question: Calculate the NTH CATALAN NUMBER.
     * Catalan numbers appear in many combinatorial problems.
     * Catalan(n) = (2n)! / ((n+1)! * n!)
     * Recurrence: Catalan(n) = sum of Catalan(i) * Catalan(n-1-i) for i=0 to n-1
     * Sequence: 1, 1, 2, 5, 14, 42, 132, ...
     * 
     * Input: n = 0
     * Expected Output: 1
     * 
     * Input: n = 5
     * Expected Output: 42
     * 
     * Input: n = 3
     * Expected Output: 5
     * 
     * Time Complexity: O(n^2) with memoization
     * Space Complexity: O(n)
     */
    public static long catalan(int n) {
        Map<Integer, Long> memo = new HashMap<>();
        return catalanRecursive(n, memo);
    }
    
    private static long catalanRecursive(int n, Map<Integer, Long> memo) {
        if (n <= 1) return 1;
        
        if (memo.containsKey(n)) return memo.get(n);
        
        long result = 0;
        
        for (int i = 0; i < n; i++) {
            result += catalanRecursive(i, memo) * catalanRecursive(n - 1 - i, memo);
        }
        
        memo.put(n, result);
        return result;
    }
    
    /**
     * Problem 21: Minimum Removals (Partition Difference)
     * 
     * Question: Find the MINIMUM ABSOLUTE DIFFERENCE between two subset sums\
     * after removing/partitioning array elements into two subsets.\
     * \
     * Input: arr = [1, 5, 11, 5]\
     * Expected Output: 0\
     * Explanation: Partition into {11} and {5, 5, 1} gives difference = |11-11| = 0\
     * \
     * Input: arr = [1, 1]\
     * Expected Output: 0\
     * Explanation: Both subsets have equal sum 1\
     * \
     * Input: arr = [3, 1, 5, 2, 8]\
     * Expected Output: 1\
     * Explanation: Optimal partition gives minimum difference of 1\
     * \
     * Time Complexity: O(n * sum) with memoization\
     * Space Complexity: O(n * sum)\
     */
    public static int minimumRemovals(int[] arr) {
        int sum = 0;
        for (int num : arr) sum += num;
        
        int target = sum / 2;
        Map<String, Integer> memo = new HashMap<>();
        int subsetSum = minimumRemovalsRecursive(arr, 0, target, memo);
        
        return sum - 2 * subsetSum;
    }
    
    private static int minimumRemovalsRecursive(int[] arr, int index, int sum,
                                               Map<String, Integer> memo) {
        if (index == arr.length || sum == 0) {
            return 0;
        }
        
        String key = index + ":" + sum;
        if (memo.containsKey(key)) return memo.get(key);
        
        int include = 0;
        if (arr[index] <= sum) {
            include = arr[index] + minimumRemovalsRecursive(arr, index + 1, sum - arr[index], memo);
        }
        
        int exclude = minimumRemovalsRecursive(arr, index + 1, sum, memo);
        
        int result = Math.max(include, exclude);
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem 22: Minimum Cost Path in Grid
     * 
     * Question: Given an m x n matrix with costs at each cell,
     * find the MINIMUM TOTAL COST PATH from top-left to bottom-right.
     * Can move RIGHT, DOWN, or DIAGONALLY DOWN-RIGHT.
     * 
     * Input: cost = [[1,3,1], [5,2,2], [4,3,1]]
     * Expected Output: 8
     * Explanation: Path (0,0)->1 + (0,1)->3 + (1,1)->2 + (2,1)->3 + (2,2)->1 = 10, or better path = 8
     * 
     * Input: cost = [[1,2], [3,4]]
     * Expected Output: 7
     * Explanation: Optimal path cost
     * 
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int minCostPath(int[][] cost) {
        Map<String, Integer> memo = new HashMap<>();
        return minCostPathRecursive(cost, cost.length - 1, cost[0].length - 1, memo);
    }
    
    private static int minCostPathRecursive(int[][] cost, int row, int col,
                                           Map<String, Integer> memo) {
        if (row < 0 || col < 0) return Integer.MAX_VALUE;
        if (row == 0 && col == 0) return cost[0][0];
        
        String key = row + ":" + col;
        if (memo.containsKey(key)) return memo.get(key);
        
        int minCost = cost[row][col] + Math.min(
            minCostPathRecursive(cost, row - 1, col, memo),
            Math.min(minCostPathRecursive(cost, row, col - 1, memo),
                    minCostPathRecursive(cost, row - 1, col - 1, memo))
        );
        
        memo.put(key, minCost);
        return minCost;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - DP Problems (Part 1) ===\n");
        
        System.out.println("1. Coin Change (coins=[1,2,5], amount=5): " + 
                          coinChange(new int[]{1, 2, 5}, 5));
        System.out.println("2. 0/1 Knapsack: " + 
                          knapsack(new int[]{2, 3, 4}, new int[]{3, 4, 5}, 5));
        System.out.println("4. Edit Distance (horse, ros): " + 
                          editDistance("horse", "ros"));
        System.out.println("7. LCS (AGGTAB, GXTXAYB): " + 
                          lcs("AGGTAB", "GXTXAYB"));
        System.out.println("19. Fibonacci(10): " + fibonacci(10));
        System.out.println("20. Catalan(5): " + catalan(5));
    }
}
