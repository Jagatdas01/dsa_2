/**
 * DSA Pattern: Dynamic Programming - Recursion Approach
 * Topics: Memoization using recursion, recursive problem solving, overlapping subproblems
 */

import java.util.HashMap;
import java.util.Map;

public class DynamicProgrammingPatterns {
    
    /**
     * Problem: Fibonacci with Recursion & Memoization
     * Approach: Pure recursion with memoization to avoid redundant calculations
     * Time Complexity: O(n)
     * Space Complexity: O(n) - for memo array and recursion stack
     */
    public static long fibonacci(int n, long[] memo) {
        // Base case
        if (n <= 1) return n;
        
        // Check if already computed
        if (memo[n] != 0) return memo[n];
        
        // Recursive case: store result to avoid recomputation
        memo[n] = fibonacci(n - 1, memo) + fibonacci(n - 2, memo);
        return memo[n];
    }
    
    /**
     * Problem: Coin Change (Minimum coins) - Recursion Approach
     * Approach: For each coin, decide to include or exclude, then recurse on remaining amount
     * Time Complexity: O(amount * coins) with memoization
     * Space Complexity: O(amount) for recursion stack and memo
     */
    public static int coinChangeRecursion(int[] coins, int amount, Map<Integer, Integer> memo) {
        // Base case
        if (amount == 0) return 0;
        if (amount < 0) return -1;
        
        // Check memo
        if (memo.containsKey(amount)) return memo.get(amount);
        
        int minCoins = Integer.MAX_VALUE;
        
        // Try each coin
        for (int coin : coins) {
            int result = coinChangeRecursion(coins, amount - coin, memo);
            
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
     * Problem: 0/1 Knapsack - Recursion Approach
     * Approach: For each item, either include it (with reduced capacity) or exclude it
     * Time Complexity: O(n * capacity) with memoization
     * Space Complexity: O(n * capacity) for memo + recursion stack
     */
    public static int knapsackRecursion(int[] weights, int[] values, int capacity, 
                                       int index, int[][] memo) {
        // Base case: no items or no capacity
        if (index == weights.length || capacity <= 0) return 0;
        
        // Check memo
        if (memo[index][capacity] != -1) return memo[index][capacity];
        
        int result;
        
        // If current item can fit
        if (weights[index] <= capacity) {
            // Max of including or excluding current item
            int include = values[index] + knapsackRecursion(weights, values, 
                          capacity - weights[index], index + 1, memo);
            int exclude = knapsackRecursion(weights, values, capacity, index + 1, memo);
            result = Math.max(include, exclude);
        } else {
            // Cannot include, so exclude
            result = knapsackRecursion(weights, values, capacity, index + 1, memo);
        }
        
        memo[index][capacity] = result;
        return result;
    }
    
    /**
     * Problem: Longest Increasing Subsequence - Recursion Approach
     * Approach: For each position, find max LIS ending at that position
     * Time Complexity: O(n * 2^n) without memo, O(n²) with memo
     * Space Complexity: O(n) for recursion stack and memo
     */
    public static int lisRecursion(int[] arr, int index, int prevIndex, 
                                   int[][] memo) {
        // Base case: reached end of array
        if (index == arr.length) return 0;
        
        // Check memo (prevIndex + 1 because -1 is used as base case)
        if (memo[index][prevIndex + 1] != -1) {
            return memo[index][prevIndex + 1];
        }
        
        // Option 1: Exclude current element
        int exclude = lisRecursion(arr, index + 1, prevIndex, memo);
        
        // Option 2: Include current element (if valid)
        int include = 0;
        if (prevIndex == -1 || arr[index] > arr[prevIndex]) {
            include = 1 + lisRecursion(arr, index + 1, index, memo);
        }
        
        memo[index][prevIndex + 1] = Math.max(include, exclude);
        return memo[index][prevIndex + 1];
    }
    
    /**
     * Problem: Longest Common Subsequence - Recursion Approach
     * Approach: Compare characters at positions, recurse on remaining substrings
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n) for memo + recursion stack
     */
    public static int lcsRecursion(String s1, String s2, int i, int j, 
                                   Map<String, Integer> memo) {
        // Base case: reached end of either string
        if (i == s1.length() || j == s2.length()) return 0;
        
        // Check memo
        String key = i + ":" + j;
        if (memo.containsKey(key)) return memo.get(key);
        
        int result;
        
        if (s1.charAt(i) == s2.charAt(j)) {
            // Characters match: include this character
            result = 1 + lcsRecursion(s1, s2, i + 1, j + 1, memo);
        } else {
            // Characters don't match: try both paths
            int skip1 = lcsRecursion(s1, s2, i + 1, j, memo);
            int skip2 = lcsRecursion(s1, s2, i, j + 1, memo);
            result = Math.max(skip1, skip2);
        }
        
        memo.put(key, result);
        return result;
    }
    
    /**
     * Problem: House Robber - Recursion Approach
     * Approach: At each house, decide to rob or skip, maximize total money
     * Time Complexity: O(n) with memoization
     * Space Complexity: O(n) for memo and recursion stack
     */
    public static int houseRobberRecursion(int[] nums, int index, int[] memo) {
        // Base case: no houses left
        if (index >= nums.length) return 0;
        
        // Check memo
        if (memo[index] != -1) return memo[index];
        
        // Option 1: Rob current house and skip next
        int rob = nums[index] + houseRobberRecursion(nums, index + 2, memo);
        
        // Option 2: Skip current house
        int skip = houseRobberRecursion(nums, index + 1, memo);
        
        // Store maximum
        memo[index] = Math.max(rob, skip);
        return memo[index];
    }
    
    /**
     * Problem: Edit Distance - Recursion Approach
     * Approach: Three operations (insert, delete, replace), pick minimum cost path
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n) for memo + recursion stack
     */
    public static int editDistanceRecursion(String s1, String s2, int i, int j, 
                                           int[][] memo) {
        // Base cases
        if (i == 0) return j;  // Insert j characters
        if (j == 0) return i;  // Delete i characters
        
        // Check memo
        if (memo[i][j] != -1) return memo[i][j];
        
        int result;
        
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
            // Characters match, no operation needed
            result = editDistanceRecursion(s1, s2, i - 1, j - 1, memo);
        } else {
            // Characters don't match: try all three operations
            int replace = editDistanceRecursion(s1, s2, i - 1, j - 1, memo);
            int delete = editDistanceRecursion(s1, s2, i - 1, j, memo);
            int insert = editDistanceRecursion(s1, s2, i, j - 1, memo);
            
            result = 1 + Math.min(replace, Math.min(delete, insert));
        }
        
        memo[i][j] = result;
        return result;
    }
    
    /**
     * Problem: Maximum Product Subarray - Recursion Approach
     * Approach: Track max and min ending at each position, recurse to find overall max
     * Time Complexity: O(n)
     * Space Complexity: O(n) for recursion stack
     */
    public static int[] maxProductRecursion(int[] nums, int index, int[] state) {
        // state[0] = maxEnding, state[1] = minEnding, state[2] = maxOverall
        
        // Base case
        if (index == 0) {
            state[0] = nums[0];  // maxEnding
            state[1] = nums[0];  // minEnding
            state[2] = nums[0];  // maxOverall
            return state;
        }
        
        // Recurse first to get previous state
        maxProductRecursion(nums, index - 1, state);
        
        int prevMax = state[0];
        int prevMin = state[1];
        int prevMaxOverall = state[2];
        
        // Calculate current max and min
        int currMax = Math.max(nums[index], 
                     Math.max(nums[index] * prevMax, nums[index] * prevMin));
        int currMin = Math.min(nums[index], 
                     Math.min(nums[index] * prevMax, nums[index] * prevMin));
        
        state[0] = currMax;
        state[1] = currMin;
        state[2] = Math.max(prevMaxOverall, currMax);
        
        return state;
    }
    
    /**
     * Problem: Unique Paths in Grid - Recursion Approach
     * Approach: From each cell, move right or down, count all paths to destination
     * Time Complexity: O(m * n) with memoization
     * Space Complexity: O(m * n) for memo + recursion stack
     */
    public static int uniquePathsRecursion(int row, int col, int m, int n, 
                                          int[][] memo) {
        // Base case: reached destination
        if (row == m - 1 && col == n - 1) return 1;
        
        // Out of bounds
        if (row >= m || col >= n) return 0;
        
        // Check memo
        if (memo[row][col] != -1) return memo[row][col];
        
        // Move right or move down
        int pathsRight = uniquePathsRecursion(row, col + 1, m, n, memo);
        int pathsDown = uniquePathsRecursion(row + 1, col, m, n, memo);
        
        memo[row][col] = pathsRight + pathsDown;
        return memo[row][col];
    }
    
    /**
     * Problem: Climbing Stairs - Recursion Approach
     * Approach: At each step, can go 1 or 2 steps forward, count ways to reach top
     * Time Complexity: O(n) with memoization
     * Space Complexity: O(n)
     */
    public static int climbStairsRecursion(int n, int[] memo) {
        // Base cases
        if (n == 0) return 1;  // One way to stay at ground
        if (n == 1) return 1;  // One way: take 1 step
        if (n == 2) return 2;  // Two ways: 1+1 or 2
        
        // Check memo
        if (memo[n] != -1) return memo[n];
        
        // Can come from step (n-1) or (n-2)
        memo[n] = climbStairsRecursion(n - 1, memo) + 
                  climbStairsRecursion(n - 2, memo);
        return memo[n];
    }
    
    public static void main(String[] args) {
        System.out.println("=== Dynamic Programming - Recursion Approach ===\n");
        
        // Fibonacci
        long[] fibMemo = new long[11];
        System.out.println("Fibonacci(10): " + fibonacci(10, fibMemo));
        
        // Coin Change
        int[] coins = {1, 2, 5};
        Map<Integer, Integer> coinMemo = new HashMap<>();
        System.out.println("Coin Change(5): " + coinChangeRecursion(coins, 5, coinMemo));
        
        // Knapsack
        int[] weights = {2, 3, 4, 5};
        int[] values = {3, 4, 5, 6};
        int[][] knapsackMemo = new int[4][9];
        for (int[] row : knapsackMemo) java.util.Arrays.fill(row, -1);
        System.out.println("0/1 Knapsack(W=8): " + knapsackRecursion(weights, values, 8, 0, knapsackMemo));
        
        // LIS
        int[] arr = {10, 9, 2, 5, 3, 7, 101, 18};
        int[][] lisMemo = new int[arr.length][arr.length + 1];
        for (int[] row : lisMemo) java.util.Arrays.fill(row, -1);
        System.out.println("LIS: " + lisRecursion(arr, 0, -1, lisMemo));
        
        // LCS
        Map<String, Integer> lcsMemo = new HashMap<>();
        System.out.println("LCS('abcde', 'ace'): " + lcsRecursion("abcde", "ace", 0, 0, lcsMemo));
        
        // House Robber
        int[] nums = {1, 2, 3, 1};
        int[] robMemo = new int[nums.length];
        java.util.Arrays.fill(robMemo, -1);
        System.out.println("House Robber: " + houseRobberRecursion(nums, 0, robMemo));
        
        // Edit Distance
        int[][] editMemo = new int[6][4];
        for (int[] row : editMemo) java.util.Arrays.fill(row, -1);
        System.out.println("Edit Distance('horse', 'ros'): " + editDistanceRecursion("horse", "ros", 5, 3, editMemo));
        
        // Max Product Subarray
        int[] nums2 = {2, 3, -2, 4};
        int[] state = new int[3];
        maxProductRecursion(nums2, nums2.length - 1, state);
        System.out.println("Max Product Subarray: " + state[2]);
        
        // Unique Paths
        int[][] pathsMemo = new int[3][7];
        for (int[] row : pathsMemo) java.util.Arrays.fill(row, -1);
        System.out.println("Unique Paths(3x7): " + uniquePathsRecursion(0, 0, 3, 7, pathsMemo));
        
        // Climbing Stairs
        int[] stairsMemo = new int[11];
        java.util.Arrays.fill(stairsMemo, -1);
        System.out.println("Climbing Stairs(10): " + climbStairsRecursion(10, stairsMemo));
    }
}
