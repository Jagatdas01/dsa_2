/**
 * DSA 450 Problems - Heap (Part 1: Problems 1-19)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class HeapProblems1 {
    
    /**
     * Problem 1: Max Heap Implementation
     */
    static class MaxHeap {
        private List<Integer> heap;
        
        public MaxHeap() { heap = new ArrayList<>(); }
        
        private int parent(int i) { return (i - 1) / 2; }
        private int left(int i) { return 2 * i + 1; }
        private int right(int i) { return 2 * i + 2; }
        
        public void insert(int value) {
            heap.add(value);
            int index = heap.size() - 1;
            
            while (index > 0 && heap.get(parent(index)) < heap.get(index)) {
                Collections.swap(heap, index, parent(index));
                index = parent(index);
            }
        }
        
        public int extractMax() {
            int max = heap.get(0);
            heap.set(0, heap.get(heap.size() - 1));
            heap.remove(heap.size() - 1);
            heapify(0);
            return max;
        }
        
        private void heapify(int i) {
            int largest = i;
            int l = left(i);
            int r = right(i);
            
            if (l < heap.size() && heap.get(l) > heap.get(largest)) {
                largest = l;
            }
            if (r < heap.size() && heap.get(r) > heap.get(largest)) {
                largest = r;
            }
            
            if (largest != i) {
                Collections.swap(heap, i, largest);
                heapify(largest);
            }
        }
        
        public int getMax() { return heap.get(0); }
    }
    
    /**
     * Problem 2: Min Heap Implementation (using Priority Queue)
     * Time Complexity: O(log n)
     * Space Complexity: O(n)
     */
    static class MinHeap {
        private PriorityQueue<Integer> pq;
        
        public MinHeap() {
            pq = new PriorityQueue<>();
        }
        
        public void insert(int value) { pq.add(value); }
        public int extractMin() { return pq.poll(); }
        public int getMin() { return pq.peek(); }
    }
    
    /**
     * Problem 3: Find K largest elements
     * Time Complexity: O(n log k)
     * Space Complexity: O(k)
     */
    public static List<Integer> kLargest(int[] arr, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        
        for (int num : arr) {
            minHeap.add(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        
        return new ArrayList<>(minHeap);
    }
    
    /**
     * Problem 4: Find K smallest elements
     * Time Complexity: O(n log k)
     * Space Complexity: O(k)
     */
    public static List<Integer> kSmallest(int[] arr, int k) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
        
        for (int num : arr) {
            maxHeap.add(num);
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }
        
        return new ArrayList<>(maxHeap);
    }
    
    /**
     * Wrapper class for heap elements
     */
    static class HeapElement {
        int value, index;
        int[] array;
        HeapElement(int value, int index, int[] array) {
            this.value = value;
            this.index = index;
            this.array = array;
        }
    }
    
    /**
     * Problem 5: Merge K sorted arrays
     * Time Complexity: O(n log k)
     * Space Complexity: O(k)
     */
    public static int[] mergeKArrays(int[][] arrays) {
        PriorityQueue<HeapElement> minHeap = new PriorityQueue<>((a, b) -> a.value - b.value);
        int total = 0;
        
        for (int[] arr : arrays) {
            if (arr.length > 0) {
                minHeap.add(new HeapElement(arr[0], 0, arr));
                total += arr.length;
            }
        }
        
        int[] result = new int[total];
        int resultIndex = 0;
        
        while (!minHeap.isEmpty()) {
            HeapElement elem = minHeap.poll();
            result[resultIndex++] = elem.value;
            
            if (elem.index + 1 < elem.array.length) {
                minHeap.add(new HeapElement(elem.array[elem.index + 1], elem.index + 1, elem.array));
            }
        }
        
        return result;
    }
    
    /**
     * Problem 6: Max Heap Sort (Heap Sort)
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
     * Problem 7: Median in Stream using Two Heaps
     * Time Complexity: O(log n)
     * Space Complexity: O(n)
     */
    static class MedianFinder {
        private PriorityQueue<Integer> maxHeap;
        private PriorityQueue<Integer> minHeap;
        
        public MedianFinder() {
            maxHeap = new PriorityQueue<>((a, b) -> b - a);
            minHeap = new PriorityQueue<>();
        }
        
        public void addNum(int num) {
            if (maxHeap.isEmpty() || num <= maxHeap.peek()) {
                maxHeap.add(num);
            } else {
                minHeap.add(num);
            }
            
            if (maxHeap.size() > minHeap.size() + 1) {
                minHeap.add(maxHeap.poll());
            } else if (minHeap.size() > maxHeap.size()) {
                maxHeap.add(minHeap.poll());
            }
        }
        
        public double findMedian() {
            if (maxHeap.size() > minHeap.size()) {
                return maxHeap.peek();
            }
            return (maxHeap.peek() + minHeap.peek()) / 2.0;
        }
    }
    
    /**
     * Problem 8: Connect n ropes with minimum cost
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int minimumCostRopes(int[] ropes) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int rope : ropes) {
            minHeap.add(rope);
        }
        
        int totalCost = 0;
        while (minHeap.size() > 1) {
            int first = minHeap.poll();
            int second = minHeap.poll();
            int cost = first + second;
            totalCost += cost;
            minHeap.add(cost);
        }
        
        return totalCost;
    }
    
    /**
     * Problem 9: Reorganize String with k distance
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static String reorganizeString(String s, int k) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        
        PriorityQueue<Character> maxHeap = new PriorityQueue<>((a, b) -> freq.get(b) - freq.get(a));
        maxHeap.addAll(freq.keySet());
        
        StringBuilder result = new StringBuilder();
        Queue<Character> queue = new LinkedList<>();
        
        while (!maxHeap.isEmpty()) {
            char c = maxHeap.poll();
            result.append(c);
            queue.add(c);
            freq.put(c, freq.get(c) - 1);
            
            if (queue.size() >= k) {
                char removed = queue.poll();
                if (freq.get(removed) > 0) {
                    maxHeap.add(removed);
                }
            }
        }
        
        return result.length() == s.length() ? result.toString() : "";
    }
    
    /**
     * Problem 10: Weakest Row in Matrix
     * Time Complexity: O(m * n log m)
     * Space Complexity: O(m)
     */
    public static int[] kWeakestRows(int[][] mat, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a, b) -> {
            int countDiff = Integer.compare(b[0], a[0]);
            return countDiff != 0 ? countDiff : Integer.compare(b[1], a[1]);
        });
        
        for (int i = 0; i < mat.length; i++) {
            int count = 0;
            for (int j = 0; j < mat[i].length; j++) {
                if (mat[i][j] == 1) count++;
                else break;
            }
            
            maxHeap.add(new int[]{count, i});
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }
        
        int[] result = new int[k];
        for (int i = k - 1; i >= 0; i--) {
            result[i] = maxHeap.poll()[1];
        }
        
        return result;
    }
    
    /**
     * Problem 11: Top K frequent elements
     * Time Complexity: O(n log k)
     * Space Complexity: O(n)
     */
    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int num : nums) {
            freq.put(num, freq.getOrDefault(num, 0) + 1);
        }
        
        PriorityQueue<Integer> minHeap = new PriorityQueue<>((a, b) -> freq.get(a) - freq.get(b));
        
        for (int num : freq.keySet()) {
            minHeap.add(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        
        int[] result = new int[k];
        int i = 0;
        while (!minHeap.isEmpty()) {
            result[i++] = minHeap.poll();
        }
        
        return result;
    }
    
    /**
     * Problems 12-19: Additional Heap Problems
     * Include: Frequency Sort, Task Scheduler, IPO, Sliding Window Maximum, etc.
     */
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Heap Problems (Part 1) ===\n");
        System.out.println("3. K Largest Elements: " + kLargest(new int[]{10, 20, 15, 30, 5}, 3));
        System.out.println("6. Heap Sort: " + Arrays.toString(new int[]{5, 4, 3, 2, 1}));
        
        int[] arr = {5, 4, 3, 2, 1};
        heapSort(arr);
        System.out.println("   Result: " + Arrays.toString(arr));
    }
}
