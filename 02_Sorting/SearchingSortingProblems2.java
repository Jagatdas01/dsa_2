/**
 * DSA 450 Problems - Searching & Sorting (Part 2: Problems 23-43)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class SearchingSortingProblems2 {
    
    /**
     * Problem 23: Median of Two Sorted Arrays
     * Time Complexity: O(log(min(n, m)))
     * Space Complexity: O(1)
     */
    public static double findMedianSorted(int[] arr1, int[] arr2) {
        if (arr1.length > arr2.length) {
            return findMedianSorted(arr2, arr1);
        }
        
        int low = 0, high = arr1.length;
        
        while (low <= high) {
            int cut1 = (low + high) / 2;
            int cut2 = (arr1.length + arr2.length + 1) / 2 - cut1;
            
            int left1 = (cut1 == 0) ? Integer.MIN_VALUE : arr1[cut1 - 1];
            int left2 = (cut2 == 0) ? Integer.MIN_VALUE : arr2[cut2 - 1];
            int right1 = (cut1 == arr1.length) ? Integer.MAX_VALUE : arr1[cut1];
            int right2 = (cut2 == arr2.length) ? Integer.MAX_VALUE : arr2[cut2];
            
            if (left1 <= right2 && left2 <= right1) {
                if ((arr1.length + arr2.length) % 2 == 0) {
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
     * Problem 24: Binary Search in Infinite Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int binarySearchInfinite(int[] arr, int target) {
        int low = 0, high = 1;
        
        while (arr[high] < target) {
            low = high;
            high = high * 2;
        }
        
        while (low <= high) {
            int mid = low + (high - low) / 2;
            
            if (arr[mid] == target) return mid;
            else if (arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        
        return -1;
    }
    
    /**
     * Problem 25: Find Duplicate in Array (1 to n)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findDuplicate(int[] arr) {
        int slow = arr[0], fast = arr[0];
        
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
     * Problem 26: Missing and Repeating Number
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int[] findMissingRepeating(int[] arr) {
        long n = arr.length;
        long xor = 0;
        
        for (int num : arr) {
            xor ^= num;
        }
        
        for (int i = 1; i <= n; i++) {
            xor ^= i;
        }
        
        long setBit = xor & -xor;
        long x = 0, y = 0;
        
        for (int num : arr) {
            if ((num & setBit) > 0) x ^= num;
            else y ^= num;
        }
        
        for (int i = 1; i <= n; i++) {
            if ((i & setBit) > 0) x ^= i;
            else y ^= i;
        }
        
        return new int[]{(int)x, (int)y};
    }
    
    /**
     * Problem 27: K-th Largest Element
     * Time Complexity: O(n log k)
     * Space Complexity: O(k)
     */
    public static int findKthLargest(int[] arr, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        
        for (int num : arr) {
            minHeap.offer(num);
            
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        
        return minHeap.peek();
    }
    
    /**
     * Problem 28: Kth Smallest in Matrix
     * Time Complexity: O(k log k)
     * Space Complexity: O(k)
     */
    public static int kthSmallestInMatrix(int[][] matrix, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a, b) -> matrix[b[0]][b[1]] - matrix[a[0]][a[1]]);
        Set<String> visited = new HashSet<>();
        
        maxHeap.offer(new int[]{0, 0});
        visited.add("0:0");
        
        for (int i = 0; i < k - 1; i++) {
            int[] curr = maxHeap.poll();
            int row = curr[0], col = curr[1];
            
            if (row + 1 < matrix.length && !visited.contains((row + 1) + ":" + col)) {
                visited.add((row + 1) + ":" + col);
                maxHeap.offer(new int[]{row + 1, col});
            }
            
            if (col + 1 < matrix[0].length && !visited.contains(row + ":" + (col + 1))) {
                visited.add(row + ":" + (col + 1));
                maxHeap.offer(new int[]{row, col + 1});
            }
        }
        
        int[] result = maxHeap.poll();
        return matrix[result[0]][result[1]];
    }
    
    /**
     * Problem 29: Inversion Count in Array
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static long countInversions(int[] arr) {
        return mergeSortCount(arr, 0, arr.length - 1);
    }
    
    private static long mergeSortCount(int[] arr, int left, int right) {
        if (left >= right) return 0;
        
        int mid = left + (right - left) / 2;
        long count = mergeSortCount(arr, left, mid) + mergeSortCount(arr, mid + 1, right);
        
        count += mergeCount(arr, left, mid, right);
        return count;
    }
    
    private static long mergeCount(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        long count = 0;
        
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
                count += (mid - i + 1);
            }
        }
        
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        
        System.arraycopy(temp, 0, arr, left, temp.length);
        return count;
    }
    
    /**
     * Problem 30: Minimize Maximum Difference
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minimizeMaxDiff(int[] arr, int k) {
        Arrays.sort(arr);
        int minDiff = arr[arr.length - 1 - k] - arr[0];
        
        for (int i = 0; i <= k; i++) {
            int diff = arr[arr.length - 1 - (k - i)] - arr[i];
            minDiff = Math.min(minDiff, diff);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 31: Painters Partition Problem
     * Time Complexity: O(n log(sum))
     * Space Complexity: O(1)
     */
    public static int paintersPartition(int[] arr, int k) {
        int left = Arrays.stream(arr).max().getAsInt();
        int right = Arrays.stream(arr).sum();
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (canPaint(arr, k, mid)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        
        return left;
    }
    
    private static boolean canPaint(int[] arr, int k, int maxTime) {
        int painters = 1;
        int time = 0;
        
        for (int board : arr) {
            if (time + board <= maxTime) {
                time += board;
            } else {
                painters++;
                time = board;
                
                if (painters > k) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem 32: Allocate Minimum Pages
     * Time Complexity: O(n log(sum))
     * Space Complexity: O(1)
     */
    public static int allocatePages(int[] pages, int students) {
        if (students > pages.length) return -1;
        
        int left = Arrays.stream(pages).max().getAsInt();
        int right = Arrays.stream(pages).sum();
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (canAllocate(pages, students, mid)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        
        return left;
    }
    
    private static boolean canAllocate(int[] pages, int students, int maxPages) {
        int count = 1;
        int sum = 0;
        
        for (int page : pages) {
            if (sum + page <= maxPages) {
                sum += page;
            } else {
                count++;
                sum = page;
                
                if (count > students) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem 33: Minimize Difference (Pairs)
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minimizeDifference(int[] arr) {
        Arrays.sort(arr);
        int minDiff = Integer.MAX_VALUE;
        
        for (int i = 0; i < arr.length - 1; i++) {
            minDiff = Math.min(minDiff, arr[i + 1] - arr[i]);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 34: Chocolate Distribution
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int chocolateDistribution(int[] arr, int k) {
        if (k > arr.length) return -1;
        
        Arrays.sort(arr);
        int minDiff = Integer.MAX_VALUE;
        
        for (int i = 0; i <= arr.length - k; i++) {
            int diff = arr[i + k - 1] - arr[i];
            minDiff = Math.min(minDiff, diff);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 35: Three Sum Closest
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static int threeSumClosest(int[] arr, int target) {
        Arrays.sort(arr);
        int result = arr[0] + arr[1] + arr[2];
        
        for (int i = 0; i < arr.length - 2; i++) {
            int left = i + 1, right = arr.length - 1;
            
            while (left < right) {
                int sum = arr[i] + arr[left] + arr[right];
                
                if (Math.abs(sum - target) < Math.abs(result - target)) {
                    result = sum;
                }
                
                if (sum < target) left++;
                else right--;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 36: Largest Sum Subarray with At Least 2 Elements
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static long largestSumSubarray(int[] arr) {
        long maxSum = Long.MIN_VALUE;
        long maxEndingHere = arr[0];
        long maxStartingTwo = arr[0] + arr[1];
        
        maxSum = maxStartingTwo;
        
        for (int i = 2; i < arr.length; i++) {
            maxEndingHere = Math.max(arr[i], maxEndingHere + arr[i]);
            maxStartingTwo = Math.max(maxEndingHere, maxStartingTwo + arr[i]);
            maxSum = Math.max(maxSum, maxStartingTwo);
        }
        
        return maxSum;
    }
    
    /**
     * Problem 37: Sort Array by Frequency
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int[] sortByFrequency(int[] arr) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int num : arr) {
            freq.put(num, freq.getOrDefault(num, 0) + 1);
        }
        
        Integer[] sorted = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            sorted[i] = arr[i];
        }
        
        Arrays.sort(sorted, (a, b) -> {
            int freqCmp = freq.get(b) - freq.get(a);
            return (freqCmp != 0) ? freqCmp : a - b;
        });
        
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = sorted[i];
        }
        
        return result;
    }
    
    /**
     * Problem 38: Relative Ordering
     * Time Complexity: O((n + m) log (n + m))
     * Space Complexity: O(n + m)
     */
    public static int[] relativeOrdering(int[] arr1, int[] arr2) {
        Map<Integer, Integer> order = new HashMap<>();
        for (int i = 0; i < arr2.length; i++) {
            order.put(arr2[i], i);
        }
        
        Integer[] result = new Integer[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            result[i] = arr1[i];
        }
        
        Arrays.sort(result, (a, b) -> {
            boolean aInOrder = order.containsKey(a);
            boolean bInOrder = order.containsKey(b);
            
            if (aInOrder && bInOrder) {
                return order.get(a) - order.get(b);
            } else if (aInOrder) {
                return -1;
            } else if (bInOrder) {
                return 1;
            } else {
                return a - b;
            }
        });
        
        int[] res = new int[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            res[i] = result[i];
        }
        
        return res;
    }
    
    /**
     * Problem 39: Separate 0s and 1s
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void separate01(int[] arr) {
        int left = 0, right = arr.length - 1;
        
        while (left < right) {
            while (left < right && arr[left] == 0) left++;
            while (left < right && arr[right] == 1) right--;
            
            if (left < right) {
                int temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;
                left++;
                right--;
            }
        }
    }
    
    /**
     * Problem 40: Separate 0s, 1s, and 2s (Dutch Flag)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void separate012(int[] arr) {
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
     * Problem 41: Minimum Difference Between Max and Min
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minimumDifference(int[] arr, int k) {
        if (k > arr.length) return -1;
        
        Arrays.sort(arr);
        int minDiff = Integer.MAX_VALUE;
        
        for (int i = 0; i <= arr.length - k; i++) {
            minDiff = Math.min(minDiff, arr[i + k - 1] - arr[i]);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 42: Maximum Product Subarray
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxProduct(int[] arr) {
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
     * Problem 43: Minimize Heights Difference
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minimizeHeights(int[] arr, int k) {
        Arrays.sort(arr);
        int result = arr[arr.length - 1] - arr[0];
        
        for (int i = 0; i < arr.length - 1; i++) {
            int maxVal = Math.max(arr[arr.length - 1] - k, arr[i] + k);
            int minVal = Math.min(arr[0] + k, arr[i + 1] - k);
            result = Math.min(result, maxVal - minVal);
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Searching & Sorting (Part 2) ===\n");
        
        System.out.println("27. Kth Largest (arr=[3,2,1,5,6,4], k=2): " + 
                          findKthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
        System.out.println("35. Three Sum Closest (target=0): " + 
                          threeSumClosest(new int[]{-1, 2, 1, -4}, 0));
        System.out.println("40. Separate 012 tested");
    }
}
