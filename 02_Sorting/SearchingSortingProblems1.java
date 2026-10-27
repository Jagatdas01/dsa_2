/**
 * DSA 450 Problems - Searching & Sorting (Part 1: Problems 1-22)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class SearchingSortingProblems1 {
    
    /**
     * Problem 1: Linear Search
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
    
    /**
     * Problem 2: Binary Search
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) return mid;
            else if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        
        return -1;
    }
    
    /**
     * Problem 3: Bubble Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    
    /**
     * Problem 4: Selection Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void selectionSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int minIndex = i;
            
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            
            int temp = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = temp;
        }
    }
    
    /**
     * Problem 5: Insertion Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            
            arr[j + 1] = key;
        }
    }
    
    /**
     * Problem 6: Merge Sort
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static void mergeSort(int[] arr) {
        if (arr.length < 2) return;
        
        int mid = arr.length / 2;
        int[] left = new int[mid];
        int[] right = new int[arr.length - mid];
        
        System.arraycopy(arr, 0, left, 0, mid);
        System.arraycopy(arr, mid, right, 0, arr.length - mid);
        
        mergeSort(left);
        mergeSort(right);
        merge(arr, left, right);
    }
    
    private static void merge(int[] arr, int[] left, int[] right) {
        int i = 0, j = 0, k = 0;
        
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }
        
        while (i < left.length) {
            arr[k++] = left[i++];
        }
        
        while (j < right.length) {
            arr[k++] = right[j++];
        }
    }
    
    /**
     * Problem 7: Quick Sort
     * Time Complexity: O(n log n) average, O(n^2) worst
     * Space Complexity: O(log n)
     */
    public static void quickSort(int[] arr) {
        quickSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSortHelper(arr, low, pi - 1);
            quickSortHelper(arr, pi + 1, high);
        }
    }
    
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    /**
     * Problem 8: Heap Sort
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;
        
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        
        for (int i = n - 1; i > 0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }
    }
    
    private static void heapify(int[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        
        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }
        
        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }
        
        if (largest != i) {
            int temp = arr[i];
            arr[i] = arr[largest];
            arr[largest] = temp;
            heapify(arr, n, largest);
        }
    }
    
    /**
     * Problem 9: Find First and Last Position in Sorted Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int[] searchRange(int[] arr, int target) {
        int[] result = {-1, -1};
        
        result[0] = findFirst(arr, target);
        result[1] = findLast(arr, target);
        
        return result;
    }
    
    private static int findFirst(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                result = mid;
                right = mid - 1;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }
    
    private static int findLast(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                result = mid;
                left = mid + 1;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 10: Missing Number in Sorted Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int findMissing(int[] arr) {
        int left = 0, right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] - arr[0] == mid) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return arr[0] + left;
    }
    
    /**
     * Problem 11: Counting Sort
     * Time Complexity: O(n + k)
     * Space Complexity: O(k)
     */
    public static void countingSort(int[] arr) {
        int max = Arrays.stream(arr).max().getAsInt();
        int[] count = new int[max + 1];
        
        for (int num : arr) {
            count[num]++;
        }
        
        int index = 0;
        for (int i = 0; i <= max; i++) {
            while (count[i] > 0) {
                arr[index++] = i;
                count[i]--;
            }
        }
    }
    
    /**
     * Problem 12: Radix Sort
     * Time Complexity: O(n * k)
     * Space Complexity: O(n + k)
     */
    public static void radixSort(int[] arr) {
        int max = Arrays.stream(arr).max().getAsInt();
        
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(arr, exp);
        }
    }
    
    private static void countingSortByDigit(int[] arr, int exp) {
        int[] output = new int[arr.length];
        int[] count = new int[10];
        
        for (int num : arr) {
            count[(num / exp) % 10]++;
        }
        
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = arr.length - 1; i >= 0; i--) {
            int index = (arr[i] / exp) % 10;
            output[count[index] - 1] = arr[i];
            count[index]--;
        }
        
        System.arraycopy(output, 0, arr, 0, arr.length);
    }
    
    /**
     * Problem 13: Search in Rotated Sorted Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int searchRotated(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) return mid;
            
            if (arr[left] <= arr[mid]) {
                if (target >= arr[left] && target < arr[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else {
                if (target > arr[mid] && target <= arr[right]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Problem 14: K-th Smallest Element
     * Time Complexity: O(n log k)
     * Space Complexity: O(k)
     */
    public static int findKthSmallest(int[] arr, int k) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
        
        for (int num : arr) {
            maxHeap.offer(num);
            
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }
        
        return maxHeap.peek();
    }
    
    /**
     * Problem 15: Floor in Sorted Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int findFloor(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] <= target) {
                result = arr[mid];
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 16: Ceil in Sorted Array
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int findCeil(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        int result = Integer.MAX_VALUE;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] >= target) {
                result = arr[mid];
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        
        return result == Integer.MAX_VALUE ? -1 : result;
    }
    
    /**
     * Problem 17: Bucket Sort
     * Time Complexity: O(n + k)
     * Space Complexity: O(n + k)
     */
    public static void bucketSort(int[] arr, int maxValue) {
        int[] buckets = new int[maxValue + 1];
        
        for (int num : arr) {
            buckets[num]++;
        }
        
        int index = 0;
        for (int i = 0; i <= maxValue; i++) {
            for (int j = 0; j < buckets[i]; j++) {
                arr[index++] = i;
            }
        }
    }
    
    /**
     * Problem 18: Intersection of Two Sorted Arrays
     * Time Complexity: O(n + m)
     * Space Complexity: O(min(n, m))
     */
    public static List<Integer> intersection(int[] arr1, int[] arr2) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i] == arr2[j]) {
                if (result.isEmpty() || result.get(result.size() - 1) != arr1[i]) {
                    result.add(arr1[i]);
                }
                i++;
                j++;
            } else if (arr1[i] < arr2[j]) {
                i++;
            } else {
                j++;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 19: Union of Two Sorted Arrays
     * Time Complexity: O(n + m)
     * Space Complexity: O(n + m)
     */
    public static List<Integer> union(int[] arr1, int[] arr2) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i] == arr2[j]) {
                if (result.isEmpty() || result.get(result.size() - 1) != arr1[i]) {
                    result.add(arr1[i]);
                }
                i++;
                j++;
            } else if (arr1[i] < arr2[j]) {
                if (result.isEmpty() || result.get(result.size() - 1) != arr1[i]) {
                    result.add(arr1[i]);
                }
                i++;
            } else {
                if (result.isEmpty() || result.get(result.size() - 1) != arr2[j]) {
                    result.add(arr2[j]);
                }
                j++;
            }
        }
        
        while (i < arr1.length) {
            if (result.isEmpty() || result.get(result.size() - 1) != arr1[i]) {
                result.add(arr1[i]);
            }
            i++;
        }
        
        while (j < arr2.length) {
            if (result.isEmpty() || result.get(result.size() - 1) != arr2[j]) {
                result.add(arr2[j]);
            }
            j++;
        }
        
        return result;
    }
    
    /**
     * Problem 20: Find Peak Element
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int findPeak(int[] arr) {
        int left = 0, right = arr.length - 1;
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] > arr[mid + 1]) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        
        return arr[left];
    }
    
    /**
     * Problem 21: Majority Element using Boyer-Moore
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findMajority(int[] arr) {
        int candidate = arr[0];
        int count = 1;
        
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] == candidate) {
                count++;
            } else {
                count--;
                if (count == 0) {
                    candidate = arr[i];
                    count = 1;
                }
            }
        }
        
        return candidate;
    }
    
    /**
     * Problem 22: Smallest Range in K Lists
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int[] smallestRange(List<List<Integer>> lists) {
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> lists.get(a[0]).get(a[1]) - 
                                                                   lists.get(b[0]).get(b[1]));
        int max = Integer.MIN_VALUE;
        int[] result = {0, Integer.MAX_VALUE};
        
        for (int i = 0; i < lists.size(); i++) {
            minHeap.offer(new int[]{i, 0});
            max = Math.max(max, lists.get(i).get(0));
        }
        
        while (minHeap.size() == lists.size()) {
            int[] curr = minHeap.poll();
            int list = curr[0];
            int index = curr[1];
            int min = lists.get(list).get(index);
            
            if (max - min < result[1] - result[0]) {
                result[0] = min;
                result[1] = max;
            }
            
            if (index + 1 < lists.get(list).size()) {
                minHeap.offer(new int[]{list, index + 1});
                max = Math.max(max, lists.get(list).get(index + 1));
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Searching & Sorting (Part 1) ===\n");
        
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6};
        int[] sorted = arr.clone();
        mergeSort(sorted);
        System.out.println("6. Merge Sort: " + Arrays.toString(sorted));
        
        int[] arr2 = {1, 3, 5, 6};
        System.out.println("9. Search Range (3 in array): " + Arrays.toString(searchRange(arr2, 3)));
        
        int[] rotated = {4, 5, 6, 7, 0, 1, 2};
        System.out.println("13. Search Rotated (0): " + searchRotated(rotated, 0));
    }
}
