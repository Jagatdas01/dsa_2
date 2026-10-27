/**
 * DSA Pattern: Prefix Sum
 * Topics: Range sum queries, cumulative sum, efficient range calculations
 */

import java.util.HashMap;
import java.util.Map;

public class PrefixSumPatterns {
    
    /**
     * Problem: Calculate sum of elements in range [left, right]
     * Build prefix sum array for O(1) range queries
     * Time Complexity: O(n) preprocessing + O(1) query
     * Space Complexity: O(n)
     */
    public static class PrefixSumArray {
        private long[] prefixSum;
        
        public PrefixSumArray(int[] arr) {
            prefixSum = new long[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                prefixSum[i + 1] = prefixSum[i] + arr[i];
            }
        }
        
        public long rangeSum(int left, int right) {
            // Sum from left to right (inclusive)
            return prefixSum[right + 1] - prefixSum[left];
        }
    }
    
    /**
     * Problem: Subarray Sum Equals K
     * Find number of subarrays with sum equal to k using prefix sum map
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int subarraySumEqualsK(int[] nums, int k) {
        Map<Integer, Integer> sumMap = new HashMap<>();
        sumMap.put(0, 1);  // Base case: prefix sum 0 appears once
        
        int count = 0;
        int prefixSum = 0;
        
        for (int num : nums) {
            prefixSum += num;
            
            // If (prefixSum - k) exists, we found subarrays with sum k
            if (sumMap.containsKey(prefixSum - k)) {
                count += sumMap.get(prefixSum - k);
            }
            
            // Add current prefix sum to map
            sumMap.put(prefixSum, sumMap.getOrDefault(prefixSum, 0) + 1);
        }
        
        return count;
    }
    
    /**
     * Problem: Contiguous Array
     * Find maximum length of contiguous subarray with equal 0s and 1s
     * Convert 0 to -1, then find max length subarray with sum = 0
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int findMaxLength(int[] nums) {
        Map<Integer, Integer> sumIndexMap = new HashMap<>();
        sumIndexMap.put(0, -1);  // Sum 0 seen at index -1
        
        int maxLength = 0;
        int prefixSum = 0;
        
        for (int i = 0; i < nums.length; i++) {
            // Convert 0 to -1
            prefixSum += (nums[i] == 1) ? 1 : -1;
            
            if (sumIndexMap.containsKey(prefixSum)) {
                // Found subarray with sum 0 (equal 0s and 1s)
                maxLength = Math.max(maxLength, i - sumIndexMap.get(prefixSum));
            } else {
                sumIndexMap.put(prefixSum, i);
            }
        }
        
        return maxLength;
    }
    
    /**
     * Problem: Maximum Sum Subarray (Kadane's Algorithm with Prefix Sum insight)
     * Find subarray with maximum sum using prefix sum concept
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxSubarray(int[] nums) {
        int maxSum = nums[0];
        int minPrefixSum = 0;
        int prefixSum = 0;
        
        for (int num : nums) {
            prefixSum += num;
            maxSum = Math.max(maxSum, prefixSum - minPrefixSum);
            minPrefixSum = Math.min(minPrefixSum, prefixSum);
        }
        
        return maxSum;
    }
    
    /**
     * Problem: Prefix and Suffix Sum
     * Create array where each element is sum of all elements to its left and right
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] leftRightSum(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        
        // Build left sum array
        int[] leftSum = new int[n];
        leftSum[0] = 0;
        for (int i = 1; i < n; i++) {
            leftSum[i] = leftSum[i - 1] + nums[i - 1];
        }
        
        // Build right sum array
        int[] rightSum = new int[n];
        rightSum[n - 1] = 0;
        for (int i = n - 2; i >= 0; i--) {
            rightSum[i] = rightSum[i + 1] + nums[i + 1];
        }
        
        // Combine
        for (int i = 0; i < n; i++) {
            result[i] = leftSum[i] + rightSum[i];
        }
        
        return result;
    }
    
    /**
     * Problem: Product of Array Except Self
     * For each element, compute product of all other elements without division
     * Time Complexity: O(n)
     * Space Complexity: O(1) excluding result array
     */
    public static int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        
        // Left products
        result[0] = 1;
        for (int i = 1; i < n; i++) {
            result[i] = result[i - 1] * nums[i - 1];
        }
        
        // Right products and multiply
        int rightProduct = 1;
        for (int i = n - 1; i >= 0; i--) {
            result[i] = result[i] * rightProduct;
            rightProduct *= nums[i];
        }
        
        return result;
    }
    
    /**
     * Problem: Running Sum of 1D Array
     * Return array where result[i] = sum of elements from 0 to i
     * Time Complexity: O(n)
     * Space Complexity: O(n) for result array
     */
    public static int[] runningSum(int[] nums) {
        int[] result = new int[nums.length];
        result[0] = nums[0];
        
        for (int i = 1; i < nums.length; i++) {
            result[i] = result[i - 1] + nums[i];
        }
        
        return result;
    }
    
    /**
     * Problem: Cumulative Sum Range Query
     * Handle multiple range sum queries efficiently
     * Time Complexity: O(n) preprocessing + O(1) per query
     * Space Complexity: O(n)
     */
    public static class CumulativeSumQuery {
        private int[] cumulativeSum;
        
        public CumulativeSumQuery(int[] arr) {
            cumulativeSum = new int[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                cumulativeSum[i + 1] = cumulativeSum[i] + arr[i];
            }
        }
        
        public int getSumRange(int left, int right) {
            return cumulativeSum[right + 1] - cumulativeSum[left];
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Prefix Sum Pattern ===\n");
        
        // PrefixSumArray
        int[] arr = {3, 2, -1, 6};
        PrefixSumArray ps = new PrefixSumArray(arr);
        System.out.println("Array: [3, 2, -1, 6]");
        System.out.println("Sum[0,2]: " + ps.rangeSum(0, 2));
        System.out.println("Sum[1,3]: " + ps.rangeSum(1, 3));
        
        // Subarray Sum Equals K
        int[] nums1 = {1, 1, 1, 0, 1, 1, 1, 1};
        System.out.println("\nSubarray sum equals 2: " + subarraySumEqualsK(nums1, 2));
        
        // Contiguous Array
        int[] nums2 = {0, 1, 0, 1, 1, 0, 0, 1};
        System.out.println("Max length contiguous array with equal 0s and 1s: " + findMaxLength(nums2));
        
        // Max Subarray
        int[] nums3 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("Max subarray sum: " + maxSubarray(nums3));
        
        // Left Right Sum
        int[] nums4 = {1, 2, 3, 4, 5};
        int[] lrSum = leftRightSum(nums4);
        System.out.print("Left Right Sum: ");
        for (int val : lrSum) System.out.print(val + " ");
        
        // Product Except Self
        int[] nums5 = {1, 2, 3, 4};
        int[] product = productExceptSelf(nums5);
        System.out.print("\nProduct except self: ");
        for (int val : product) System.out.print(val + " ");
        
        // Running Sum
        int[] nums6 = {3, 1, 2, 10, 1};
        int[] runSum = runningSum(nums6);
        System.out.print("\nRunning sum: ");
        for (int val : runSum) System.out.print(val + " ");
        
        System.out.println();
    }
}
