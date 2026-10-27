/**
 * DSA 450 Problems - Arrays (Part 2: Problems 19-35)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class ArrayProblems2 {
    
    /**
     * Problem 19: Find if any subarray has sum equal to 0
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean subarraySumZero(int[] arr) {
        Set<Integer> sumSet = new HashSet<>();
        int sum = 0;
        
        for (int num : arr) {
            sum += num;
            if (sum == 0 || sumSet.contains(sum)) return true;
            sumSet.add(sum);
        }
        return false;
    }
    
    /**
     * Problem 20: Maximum product subarray
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxProductSubarray(int[] arr) {
        int maxProd = arr[0];
        int minProd = arr[0];
        int result = arr[0];
        
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < 0) {
                int temp = maxProd;
                maxProd = minProd;
                minProd = temp;
            }
            
            maxProd = Math.max(arr[i], maxProd * arr[i]);
            minProd = Math.min(arr[i], minProd * arr[i]);
            result = Math.max(result, maxProd);
        }
        return result;
    }
    
    /**
     * Problem 21: Find longest consecutive subsequence
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int longestConsecutive(int[] arr) {
        if (arr.length == 0) return 0;
        
        Set<Integer> set = new HashSet<>();
        for (int num : arr) set.add(num);
        
        int maxLength = 1;
        
        for (int num : set) {
            if (!set.contains(num - 1)) {
                int length = 1;
                int current = num;
                while (set.contains(current + 1)) {
                    current++;
                    length++;
                }
                maxLength = Math.max(maxLength, length);
            }
        }
        
        return maxLength;
    }
    
    /**
     * Problem 22: Find all elements appearing more than n/k times
     * Time Complexity: O(n)
     * Space Complexity: O(k)
     */
    public static List<Integer> elementsMoreThanNDivK(int[] arr, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        List<Integer> result = new ArrayList<>();
        int limit = arr.length / k;
        
        for (int num : arr) {
            map.put(num, map.getOrDefault(num, 0) + 1);
        }
        
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > limit) {
                result.add(entry.getKey());
            }
        }
        
        Collections.sort(result);
        return result;
    }
    
    /**
     * Problem 23: Maximum profit by buying and selling at most twice
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxProfitTwice(int[] prices) {
        if (prices.length < 2) return 0;
        
        int firstBuy = Integer.MIN_VALUE;
        int firstSell = 0;
        int secondBuy = Integer.MIN_VALUE;
        int secondSell = 0;
        
        for (int price : prices) {
            firstBuy = Math.max(firstBuy, -price);
            firstSell = Math.max(firstSell, firstBuy + price);
            secondBuy = Math.max(secondBuy, firstSell - price);
            secondSell = Math.max(secondSell, secondBuy + price);
        }
        
        return secondSell;
    }
    
    /**
     * Problem 24: Find whether array is subset of another
     * Time Complexity: O(n + m)
     * Space Complexity: O(n)
     */
    public static boolean isSubset(int[] arr1, int[] arr2) {
        Set<Integer> set = new HashSet<>();
        for (int num : arr1) set.add(num);
        
        for (int num : arr2) {
            if (!set.contains(num)) return false;
        }
        return true;
    }
    
    /**
     * Problem 25: Find triplet that sums to given value
     * Time Complexity: O(n²)
     * Space Complexity: O(1)
     */
    public static List<int[]> findTripletSum(int[] arr, int sum) {
        Arrays.sort(arr);
        List<int[]> result = new ArrayList<>();
        
        for (int i = 0; i < arr.length - 2; i++) {
            int left = i + 1, right = arr.length - 1;
            
            while (left < right) {
                int tripletSum = arr[i] + arr[left] + arr[right];
                
                if (tripletSum == sum) {
                    result.add(new int[]{arr[i], arr[left], arr[right]});
                    left++;
                    right--;
                } else if (tripletSum < sum) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Problem 26: Trapping Rain Water
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int trapWater(int[] height) {
        if (height.length < 3) return 0;
        
        int[] leftMax = new int[height.length];
        int[] rightMax = new int[height.length];
        
        leftMax[0] = height[0];
        for (int i = 1; i < height.length; i++) {
            leftMax[i] = Math.max(leftMax[i - 1], height[i]);
        }
        
        rightMax[height.length - 1] = height[height.length - 1];
        for (int i = height.length - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i + 1], height[i]);
        }
        
        int water = 0;
        for (int i = 0; i < height.length; i++) {
            water += Math.min(leftMax[i], rightMax[i]) - height[i];
        }
        
        return water;
    }
    
    /**
     * Problem 27: Chocolate Distribution Problem
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minChocolateDifference(int[] arr, int m) {
        if (m == 1 || arr.length == 1) return 0;
        
        Arrays.sort(arr);
        int minDiff = Integer.MAX_VALUE;
        int windowSize = arr.length / m;
        
        for (int i = 0; i + windowSize - 1 < arr.length; i++) {
            int diff = arr[i + windowSize - 1] - arr[i];
            minDiff = Math.min(minDiff, diff);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 28: Smallest subarray with sum greater than given value
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int smallestSubarraySum(int[] arr, int x) {
        int minLength = Integer.MAX_VALUE;
        int left = 0;
        int sum = 0;
        
        for (int right = 0; right < arr.length; right++) {
            sum += arr[right];
            
            while (sum > x && left <= right) {
                minLength = Math.min(minLength, right - left + 1);
                sum -= arr[left];
                left++;
            }
        }
        
        return minLength == Integer.MAX_VALUE ? 0 : minLength;
    }
    
    /**
     * Problem 29: Three-way partitioning around pivot
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void threeWayPartition(int[] arr, int a, int b) {
        int low = 0, mid = 0, high = arr.length - 1;
        
        while (mid <= high) {
            if (arr[mid] < a) {
                int temp = arr[low];
                arr[low] = arr[mid];
                arr[mid] = temp;
                low++;
                mid++;
            } else if (arr[mid] > b) {
                int temp = arr[mid];
                arr[mid] = arr[high];
                arr[high] = temp;
                high--;
            } else {
                mid++;
            }
        }
    }
    
    /**
     * Problem 30: Minimum swaps to bring elements <= k together
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int minSwapsToGroupK(int[] arr, int k) {
        int count = 0;
        for (int num : arr) {
            if (num <= k) count++;
        }
        
        int swaps = 0;
        int windowCount = 0;
        
        for (int i = 0; i < count; i++) {
            if (arr[i] <= k) windowCount++;
        }
        
        swaps = count - windowCount;
        int maxSwaps = swaps;
        
        for (int i = count; i < arr.length; i++) {
            if (arr[i - count] <= k) windowCount--;
            if (arr[i] <= k) windowCount++;
            
            maxSwaps = Math.min(maxSwaps, count - windowCount);
        }
        
        return maxSwaps;
    }
    
    /**
     * Problem 31: Minimum operations to make array palindrome
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int minOperationsPalindrome(int[] arr) {
        int ops = 0;
        int left = 0, right = arr.length - 1;
        
        while (left < right) {
            if (arr[left] == arr[right]) {
                left++;
                right--;
            } else if (arr[left] < arr[right]) {
                arr[left + 1] += arr[left];
                left++;
                ops++;
            } else {
                arr[right - 1] += arr[right];
                right--;
                ops++;
            }
        }
        
        return ops;
    }
    
    /**
     * Problem 32: Median of two sorted arrays (equal size)
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static double medianSortedArraysEqual(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length) return -1;
        
        int n = arr1.length;
        int low = 0, high = n;
        
        while (low <= high) {
            int cut1 = (low + high) / 2;
            int cut2 = n - cut1;
            
            int left1 = (cut1 == 0) ? Integer.MIN_VALUE : arr1[cut1 - 1];
            int left2 = (cut2 == 0) ? Integer.MIN_VALUE : arr2[cut2 - 1];
            int right1 = (cut1 == n) ? Integer.MAX_VALUE : arr1[cut1];
            int right2 = (cut2 == n) ? Integer.MAX_VALUE : arr2[cut2];
            
            if (left1 <= right2 && left2 <= right1) {
                return (Math.max(left1, left2) + Math.min(right1, right2)) / 2.0;
            } else if (left1 > right2) {
                high = cut1 - 1;
            } else {
                low = cut1 + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Problem 33: Median of two sorted arrays (different size)
     * Time Complexity: O(log min(m, n))
     * Space Complexity: O(1)
     */
    public static double medianSortedArraysDifferent(int[] arr1, int[] arr2) {
        if (arr1.length > arr2.length) {
            return medianSortedArraysDifferent(arr2, arr1);
        }
        
        int m = arr1.length;
        int n = arr2.length;
        int low = 0, high = m;
        
        while (low <= high) {
            int cut1 = (low + high) / 2;
            int cut2 = (m + n + 1) / 2 - cut1;
            
            int left1 = (cut1 == 0) ? Integer.MIN_VALUE : arr1[cut1 - 1];
            int left2 = (cut2 == 0) ? Integer.MIN_VALUE : arr2[cut2 - 1];
            int right1 = (cut1 == m) ? Integer.MAX_VALUE : arr1[cut1];
            int right2 = (cut2 == n) ? Integer.MAX_VALUE : arr2[cut2];
            
            if (left1 <= right2 && left2 <= right1) {
                if ((m + n) % 2 == 0) {
                    return (Math.max(left1, left2) + Math.min(right1, right2)) / 2.0;
                } else {
                    return Math.max(left1, left2);
                }
            } else if (left1 > right2) {
                high = cut1 - 1;
            } else {
                low = cut1 + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Problem 34: Equilibrium point (index where left sum = right sum)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int equilibriumPoint(int[] arr) {
        long sum = 0;
        for (int num : arr) sum += num;
        
        long leftSum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum -= arr[i];
            if (leftSum == sum) return i;
            leftSum += arr[i];
        }
        
        return -1;
    }
    
    /**
     * Problem 35: Leaders in array (elements greater than all right elements)
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<Integer> findLeaders(int[] arr) {
        List<Integer> leaders = new ArrayList<>();
        int maxFromRight = Integer.MIN_VALUE;
        
        for (int i = arr.length - 1; i >= 0; i--) {
            if (arr[i] > maxFromRight) {
                leaders.add(arr[i]);
                maxFromRight = arr[i];
            }
        }
        
        Collections.reverse(leaders);
        return leaders;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Array Problems (Part 2) ===\n");
        
        // Problem 19: Subarray sum = 0
        int[] arr19 = {15, -2, 2, -8, 1, 7, -4, 10};
        System.out.println("19. Subarray sum zero: " + subarraySumZero(arr19));
        
        // Problem 20: Max product subarray
        int[] arr20 = {6, -3, -10, 0, 2};
        System.out.println("20. Max product subarray: " + maxProductSubarray(arr20));
        
        // Problem 21: Longest consecutive
        int[] arr21 = {100, 4, 200, 1, 3, 2};
        System.out.println("21. Longest consecutive: " + longestConsecutive(arr21));
        
        // Problem 26: Trap water
        int[] arr26 = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        System.out.println("26. Trap water: " + trapWater(arr26));
        
        // Problem 27: Chocolate distribution
        int[] arr27 = {3, 4, 1, 9, 56, 7, 9, 12};
        System.out.println("27. Chocolate distribution (m=5): " + minChocolateDifference(arr27, 5));
        
        // Problem 34: Equilibrium point
        int[] arr34 = {-7, 1, 5, 2, -4, 2, 0};
        System.out.println("34. Equilibrium point: " + equilibriumPoint(arr34));
        
        // Problem 35: Leaders
        int[] arr35 = {16, 17, 4, 3, 5, 2};
        System.out.println("35. Leaders: " + findLeaders(arr35));
    }
}
