/**
 * DSA Pattern: Divide and Conquer
 * Topics: Merge sort, quick sort, binary search, divide and conquer strategy
 */

public class DivideAndConquerPatterns {
    
    /**
     * Problem: Merge Sort Algorithm
     * Sort array by dividing into halves, sorting recursively, then merging
     * 
     * Input: arr = [38, 27, 43, 3, 9, 82, 10]
     * Expected Output: [3, 9, 10, 27, 38, 43, 82] (sorted)
     * 
     * Input: arr = [1]
     * Expected Output: [1] (already sorted)
     * 
     * Time Complexity: O(n log n) - always
     * Space Complexity: O(n) - temporary merge array
     */
    public static void mergeSort(int[] arr) {
        if (arr.length <= 1) return;
        mergeSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void mergeSortHelper(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortHelper(arr, left, mid);
            mergeSortHelper(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    
    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }
        
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        
        System.arraycopy(temp, 0, arr, left, temp.length);
    }
    
    /**
     * Problem: Count Inversions in Array
     * Count pairs (i, j) where i < j but arr[i] > arr[j]
     * 
     * Input: arr = [1, 20, 6, 4, 5]
     * Expected Output: 5
     * Explanation: Inversions: (20,6), (20,4), (20,5), (6,4), (6,5)
     * 
     * Input: arr = [1, 2, 3]
     * Expected Output: 0 (no inversions - already sorted)
     * 
     * Time Complexity: O(n log n) using merge sort
     * Space Complexity: O(n)
     */
    static class Count {
        long count = 0;
    }
    
    public static long countInversions(int[] arr) {
        Count count = new Count();
        mergeSortAndCount(arr, 0, arr.length - 1, count);
        return count.count;
    }
    
    private static void mergeSortAndCount(int[] arr, int left, int right, Count count) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortAndCount(arr, left, mid, count);
            mergeSortAndCount(arr, mid + 1, right, count);
            mergeAndCount(arr, left, mid, right, count);
        }
    }
    
    private static void mergeAndCount(int[] arr, int left, int mid, int right, Count count) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
                count.count += (mid - i + 1);
            }
        }
        
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        
        System.arraycopy(temp, 0, arr, left, temp.length);
    }
    
    /**
     * Problem: Find Majority Element
     * Time Complexity: O(n log n)
     * Space Complexity: O(log n)
     */
    public static int majorityElement(int[] nums) {
        return majorityElementHelper(nums, 0, nums.length - 1);
    }
    
    private static int majorityElementHelper(int[] nums, int left, int right) {
        if (left == right) return nums[left];
        
        int mid = left + (right - left) / 2;
        int leftMajor = majorityElementHelper(nums, left, mid);
        int rightMajor = majorityElementHelper(nums, mid + 1, right);
        
        if (leftMajor == rightMajor) return leftMajor;
        
        int leftCount = countInRange(nums, leftMajor, left, right);
        int rightCount = countInRange(nums, rightMajor, left, right);
        
        return leftCount > rightCount ? leftMajor : rightMajor;
    }
    
    private static int countInRange(int[] nums, int num, int left, int right) {
        int count = 0;
        for (int i = left; i <= right; i++) {
            if (nums[i] == num) count++;
        }
        return count;
    }
    
    /**
     * Problem: Maximum Subarray (Kadane's algorithm)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxSubarray(int[] nums) {
        int maxCurrent = nums[0];
        int maxGlobal = nums[0];
        
        for (int i = 1; i < nums.length; i++) {
            maxCurrent = Math.max(nums[i], maxCurrent + nums[i]);
            maxGlobal = Math.max(maxGlobal, maxCurrent);
        }
        
        return maxGlobal;
    }
    
    /**
     * Problem: Maximum Subarray using Divide and Conquer
     * Time Complexity: O(n log n)
     * Space Complexity: O(log n)
     */
    public static int maxSubarrayDC(int[] nums) {
        return maxSubarrayDCHelper(nums, 0, nums.length - 1);
    }
    
    private static int maxSubarrayDCHelper(int[] nums, int left, int right) {
        if (left == right) return nums[left];
        
        int mid = left + (right - left) / 2;
        
        int leftMax = maxSubarrayDCHelper(nums, left, mid);
        int rightMax = maxSubarrayDCHelper(nums, mid + 1, right);
        
        int crossMax = maxCrossingSubarray(nums, left, mid, right);
        
        return Math.max(crossMax, Math.max(leftMax, rightMax));
    }
    
    private static int maxCrossingSubarray(int[] nums, int left, int mid, int right) {
        int leftSum = Integer.MIN_VALUE;
        int sum = 0;
        for (int i = mid; i >= left; i--) {
            sum += nums[i];
            leftSum = Math.max(leftSum, sum);
        }
        
        int rightSum = Integer.MIN_VALUE;
        sum = 0;
        for (int i = mid + 1; i <= right; i++) {
            sum += nums[i];
            rightSum = Math.max(rightSum, sum);
        }
        
        return leftSum + rightSum;
    }
    
    /**
     * Problem: Closest Pair of Points
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class Point implements Comparable<Point> {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        
        @Override
        public int compareTo(Point other) {
            return this.x - other.x;
        }
        
        public double distance(Point other) {
            return Math.sqrt((this.x - other.x) * (this.x - other.x) + 
                           (this.y - other.y) * (this.y - other.y));
        }
    }
    
    public static double closestPairDistance(Point[] points) {
        java.util.Arrays.sort(points);
        return closestPairHelper(points, 0, points.length - 1);
    }
    
    private static double closestPairHelper(Point[] points, int left, int right) {
        if (right - left <= 2) {
            double minDist = Double.MAX_VALUE;
            for (int i = left; i <= right; i++) {
                for (int j = i + 1; j <= right; j++) {
                    minDist = Math.min(minDist, points[i].distance(points[j]));
                }
            }
            return minDist;
        }
        
        int mid = left + (right - left) / 2;
        double leftMin = closestPairHelper(points, left, mid);
        double rightMin = closestPairHelper(points, mid + 1, right);
        
        double minDist = Math.min(leftMin, rightMin);
        
        // Check strip
        for (int i = left; i <= right; i++) {
            for (int j = i + 1; j <= right; j++) {
                if (Math.abs(points[i].x - points[j].x) < minDist) {
                    minDist = Math.min(minDist, points[i].distance(points[j]));
                }
            }
        }
        
        return minDist;
    }
    
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        mergeSort(arr);
        System.out.print("Sorted array: ");
        for (int x : arr) System.out.print(x + " ");
        System.out.println();
        
        int[] arr2 = {1, 2, 3, 4, 5};
        System.out.println("Inversions: " + countInversions(arr2));
        
        int[] nums = {3, 2, 3, 1, 2, 4, 5, 5, 6};
        System.out.println("Majority element: " + majorityElement(nums));
        
        int[] nums2 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("Max subarray: " + maxSubarray(nums2));
    }
}
