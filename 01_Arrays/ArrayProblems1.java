/**
 * DSA 450 Problems - Arrays (Part 1: Problems 1-18)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class ArrayProblems1 {
    
    /**
     * Problem 1: Reverse the array
     * Given an array, reverse it without using extra space
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void reverseArray(int[] arr) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    
    /**
     * Problem 2: Find maximum and minimum element in array
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int[] findMaxMin(int[] arr) {
        int max = arr[0];
        int min = arr[0];
        for (int num : arr) {
            max = Math.max(max, num);
            min = Math.min(min, num);
        }
        return new int[]{max, min};
    }
    
    /**
     * Problem 3: Find Kth max and min element
     * Time Complexity: O(n log k) using heap
     * Space Complexity: O(k)
     */
    public static int findKthMax(int[] arr, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int num : arr) {
            minHeap.add(num);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.peek();
    }
    
    public static int findKthMin(int[] arr, int k) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
        for (int num : arr) {
            maxHeap.add(num);
            if (maxHeap.size() > k) maxHeap.poll();
        }
        return maxHeap.peek();
    }
    
    /**
     * Problem 4: Sort array of 0s, 1s and 2s (Dutch National Flag)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void sortZeroOneTwo(int[] arr) {
        int low = 0, mid = 0, high = arr.length - 1;
        while (mid <= high) {
            if (arr[mid] == 0) {
                int temp = arr[low];
                arr[low] = arr[mid];
                arr[mid] = temp;
                low++;
                mid++;
            } else if (arr[mid] == 1) {
                mid++;
            } else {
                int temp = arr[mid];
                arr[mid] = arr[high];
                arr[high] = temp;
                high--;
            }
        }
    }
    
    /**
     * Problem 5: Find Union and Intersection of two sorted arrays
     * Time Complexity: O(n + m)
     * Space Complexity: O(n + m)
     */
    public static List<Integer> findUnion(int[] arr1, int[] arr2) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        
        for (int num : arr1) {
            if (!seen.contains(num)) {
                result.add(num);
                seen.add(num);
            }
        }
        for (int num : arr2) {
            if (!seen.contains(num)) {
                result.add(num);
                seen.add(num);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    public static List<Integer> findIntersection(int[] arr1, int[] arr2) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> set1 = new HashSet<>();
        for (int num : arr1) set1.add(num);
        
        for (int num : arr2) {
            if (set1.contains(num)) {
                result.add(num);
                set1.remove(num);  // Remove to handle duplicates
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * Problem 6: Cyclically rotate array by one
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void rotateByOne(int[] arr) {
        int last = arr[arr.length - 1];
        for (int i = arr.length - 1; i > 0; i--) {
            arr[i] = arr[i - 1];
        }
        arr[0] = last;
    }
    
    /**
     * Problem 7: Kadane's Algorithm - Find largest sum contiguous subarray
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     * [VERY IMPORTANT]
     */
    public static int kadaneAlgorithm(int[] arr) {
        int maxSoFar = arr[0];
        int maxEndingHere = arr[0];
        
        for (int i = 1; i < arr.length; i++) {
            maxEndingHere = Math.max(arr[i], maxEndingHere + arr[i]);
            maxSoFar = Math.max(maxSoFar, maxEndingHere);
        }
        return maxSoFar;
    }
    
    /**
     * Problem 8: Minimize maximum difference between heights
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     * [VERY IMPORTANT]
     */
    public static int minimizeHeightDiff(int[] arr, int k) {
        Arrays.sort(arr);
        int diff = arr[arr.length - 1] - arr[0];
        int small = arr[0] + k;
        int large = arr[arr.length - 1] - k;
        
        if (small > large) return Math.min(diff, large - small);
        
        // Shrink array from left and right
        for (int i = 0; i < arr.length - 1; i++) {
            int leftBound = arr[i] + k;
            int rightBound = arr[i + 1] - k;
            
            if (leftBound >= small) small = leftBound;
            if (rightBound <= large) large = rightBound;
        }
        
        return large - small;
    }
    
    /**
     * Problem 9: Minimum number of jumps to reach end
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int minJumps(int[] arr) {
        int jumps = 0, currentEnd = 0, farthest = 0;
        
        for (int i = 0; i < arr.length - 1; i++) {
            farthest = Math.max(farthest, i + arr[i]);
            
            if (i == currentEnd) {
                jumps++;
                currentEnd = farthest;
            }
        }
        return jumps;
    }
    
    /**
     * Problem 10: Find duplicate in array of N+1 integers (1 to N)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findDuplicate(int[] arr) {
        // Using Floyd's cycle detection
        int slow = arr[0];
        int fast = arr[0];
        
        do {
            slow = arr[slow];
            fast = arr[arr[fast]];
        } while (slow != fast);
        
        slow = arr[0];
        while (slow != fast) {
            slow = arr[slow];
            fast = arr[fast];
        }
        return slow;
    }
    
    /**
     * Problem 11: Merge two sorted arrays without extra space
     * Time Complexity: O(n log n) for sorting at end
     * Space Complexity: O(1)
     */
    public static void mergeSortedArrays(int[] arr1, int[] arr2) {
        int i = arr1.length - 1;
        int j = arr2.length - 1;
        int k = arr1.length + arr2.length - 1;
        
        // Copy arr2 into end of arr1
        for (int idx = 0; idx < arr2.length; idx++) {
            arr1[arr1.length - arr2.length + idx] = arr2[idx];
        }
        
        // Merge from end
        while (i >= 0 && j >= 0) {
            if (arr1[i] > arr1[j]) {
                arr1[k--] = arr1[i--];
            } else {
                arr1[k--] = arr1[j--];
            }
        }
        
        while (j >= 0) {
            arr1[k--] = arr1[j--];
        }
    }
    
    /**
     * Problem 12: Merge Intervals
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class Interval {
        int start, end;
        Interval(int s, int e) { start = s; end = e; }
        @Override
        public String toString() { return "[" + start + "," + end + "]"; }
    }
    
    public static List<Interval> mergeIntervals(List<Interval> intervals) {
        if (intervals.isEmpty()) return new ArrayList<>();
        
        intervals.sort((a, b) -> a.start - b.start);
        List<Interval> result = new ArrayList<>();
        Interval current = intervals.get(0);
        
        for (int i = 1; i < intervals.size(); i++) {
            if (intervals.get(i).start <= current.end) {
                current.end = Math.max(current.end, intervals.get(i).end);
            } else {
                result.add(current);
                current = intervals.get(i);
            }
        }
        result.add(current);
        return result;
    }
    
    /**
     * Problem 13: Next Permutation
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void nextPermutation(int[] arr) {
        int i = arr.length - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) i--;
        
        if (i >= 0) {
            int j = arr.length - 1;
            while (j > i && arr[j] <= arr[i]) j--;
            
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        
        // Reverse from i+1 to end
        int left = i + 1, right = arr.length - 1;
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    
    /**
     * Problem 14: Count Inversions
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static long countInversions(int[] arr) {
        return mergeSort(arr, 0, arr.length - 1);
    }
    
    private static long mergeSort(int[] arr, int left, int right) {
        long invCount = 0;
        if (left < right) {
            int mid = left + (right - left) / 2;
            invCount += mergeSort(arr, left, mid);
            invCount += mergeSort(arr, mid + 1, right);
            invCount += merge(arr, left, mid, right);
        }
        return invCount;
    }
    
    private static long merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        long invCount = 0;
        
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
                invCount += (mid - i + 1);
            }
        }
        
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        
        System.arraycopy(temp, 0, arr, left, temp.length);
        return invCount;
    }
    
    /**
     * Problem 15: Best time to buy and sell stock
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxProfit(int[] prices) {
        int minPrice = prices[0];
        int maxProfit = 0;
        
        for (int price : prices) {
            maxProfit = Math.max(maxProfit, price - minPrice);
            minPrice = Math.min(minPrice, price);
        }
        return maxProfit;
    }
    
    /**
     * Problem 16: Find all pairs with sum equal to given number
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<int[]> findPairs(int[] arr, int sum) {
        List<int[]> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        Set<Integer> pairs = new HashSet<>();
        
        for (int num : arr) {
            int complement = sum - num;
            if (seen.contains(complement)) {
                int[] pair = {Math.min(num, complement), Math.max(num, complement)};
                if (!pairs.contains(pair[0])) {
                    result.add(pair);
                    pairs.add(pair[0]);
                }
            }
            seen.add(num);
        }
        return result;
    }
    
    /**
     * Problem 17: Find common elements in 3 sorted arrays
     * Time Complexity: O(n1 + n2 + n3)
     * Space Complexity: O(1)
     */
    public static List<Integer> commonInThreeArrays(int[] arr1, int[] arr2, int[] arr3) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0, k = 0;
        
        while (i < arr1.length && j < arr2.length && k < arr3.length) {
            if (arr1[i] == arr2[j] && arr2[j] == arr3[k]) {
                result.add(arr1[i]);
                i++;
                j++;
                k++;
            } else if (arr1[i] < arr2[j]) {
                i++;
            } else if (arr2[j] < arr3[k]) {
                j++;
            } else {
                k++;
            }
        }
        return result;
    }
    
    /**
     * Problem 18: Rearrange alternating positive and negative with O(1) space
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void rearrangeAlternating(int[] arr) {
        int n = arr.length;
        int pos = -1;
        
        for (int i = 0; i < n; i++) {
            if (arr[i] > 0) {
                if (pos == -1) pos = i;
            } else {
                if (pos != -1) {
                    int temp = arr[pos];
                    arr[pos] = arr[i];
                    arr[i] = temp;
                    pos = -1;
                    for (int j = pos + 2; j < n; j += 2) {
                        if (arr[j] > 0) {
                            pos = j;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Array Problems (Part 1) ===\n");
        
        // Problem 1: Reverse array
        int[] arr1 = {1, 2, 3, 4, 5};
        reverseArray(arr1);
        System.out.println("1. Reversed array: " + Arrays.toString(arr1));
        
        // Problem 2: Max and min
        int[] arr2 = {10, 5, 20, 3, 15};
        int[] result = findMaxMin(arr2);
        System.out.println("2. Max: " + result[0] + ", Min: " + result[1]);
        
        // Problem 3: Kth max and min
        int[] arr3 = {10, 5, 20, 3, 15};
        System.out.println("3. 2nd Max: " + findKthMax(arr3, 2) + ", 2nd Min: " + findKthMin(arr3, 2));
        
        // Problem 4: Sort 0, 1, 2
        int[] arr4 = {0, 2, 1, 2, 0, 1};
        sortZeroOneTwo(arr4);
        System.out.println("4. Sorted [0,1,2]: " + Arrays.toString(arr4));
        
        // Problem 5: Union and Intersection
        int[] a = {1, 3, 4, 5};
        int[] b = {2, 3, 5, 6};
        System.out.println("5. Union: " + findUnion(a, b) + ", Intersection: " + findIntersection(a, b));
        
        // Problem 6: Rotate by one
        int[] arr6 = {1, 2, 3, 4, 5};
        rotateByOne(arr6);
        System.out.println("6. Rotated by 1: " + Arrays.toString(arr6));
        
        // Problem 7: Kadane's Algorithm
        int[] arr7 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("7. Kadane Max Sum: " + kadaneAlgorithm(arr7));
        
        // Problem 9: Min jumps
        int[] arr9 = {1, 3, 5, 8, 9, 2, 6, 7, 6, 0, 9};
        System.out.println("9. Min Jumps: " + minJumps(arr9));
        
        // Problem 15: Best time to buy and sell
        int[] prices = {7, 1, 5, 3, 6, 4};
        System.out.println("15. Max profit: " + maxProfit(prices));
    }
}
