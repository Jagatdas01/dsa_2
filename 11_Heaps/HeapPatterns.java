/**
 * DSA Pattern: Heap Data Structure
 * Topics: Min Heap, Max Heap, Priority Queue operations
 */

import java.util.PriorityQueue;
import java.util.Collections;

public class HeapPatterns {
    
    /**
     * Problem: Find Kth Largest Element
     * Find the kth largest element in unsorted array
     * 
     * Input: nums = [3, 2, 1, 5, 6, 4], k = 2
     * Expected Output: 5 (sorted: [6,5,4,3,2,1], 2nd largest = 5)
     * 
     * Input: nums = [1], k = 1
     * Expected Output: 1
     * 
     * Time Complexity: O(n log k)
     * Space Complexity: O(k) - min heap size
     */
    public static int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        
        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        
        return minHeap.peek();
    }
    
    /**
     * Problem: Find Kth Smallest Element
     * Find the kth smallest element in unsorted array
     * 
     * Input: nums = [3, 2, 1, 5, 6, 4], k = 3
     * Expected Output: 3 (sorted: [1,2,3,4,5,6], 3rd smallest = 3)
     * 
     * Input: nums = [1, 2, 3], k = 2
     * Expected Output: 2
     * 
     * Time Complexity: O(n log k)
     * Space Complexity: O(k) - max heap size
     */
    public static int findKthSmallest(int[] nums, int k) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        
        for (int num : nums) {
            maxHeap.offer(num);
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }
        
        return maxHeap.peek();
    }
    
    /**
     * Problem: Top K Frequent Elements
     * Find k most frequently appearing elements
     * 
     * Input: nums = [1, 1, 1, 2, 2, 3], k = 2
     * Expected Output: [1, 2] (1 appears 3 times, 2 appears 2 times)
     * 
     * Input: nums = [4, 1, 1, 1, 2, 2, 3], k = 2
     * Expected Output: [1, 2] (1 most frequent)
     * 
     * Time Complexity: O(n log k)
     * Space Complexity: O(n + k) - frequency map + heap
     */
    public static int[] topKFrequent(int[] nums, int k) {
        // Count frequencies
        java.util.Map<Integer, Integer> freqMap = new java.util.HashMap<>();
        for (int num : nums) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }
        
        // Min heap based on frequency
        PriorityQueue<Integer> minHeap = new PriorityQueue<>((a, b) -> 
            freqMap.get(a) - freqMap.get(b)
        );
        
        // Keep k most frequent
        for (int num : freqMap.keySet()) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        
        // Build result
        int[] result = new int[k];
        int i = k - 1;
        while (!minHeap.isEmpty()) {
            result[i--] = minHeap.poll();
        }
        
        return result;
    }
    
    /**
     * Problem: Merge K Sorted Lists
     * Time Complexity: O(n log k) where n is total elements, k is number of lists
     * Space Complexity: O(k)
     */
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }
    
    public static ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) return null;
        
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a, b) -> a.val - b.val);
        
        // Add first node of each list
        for (ListNode list : lists) {
            if (list != null) {
                minHeap.offer(list);
            }
        }
        
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        
        while (!minHeap.isEmpty()) {
            ListNode node = minHeap.poll();
            current.next = node;
            current = current.next;
            
            if (node.next != null) {
                minHeap.offer(node.next);
            }
        }
        
        return dummy.next;
    }
    
    /**
     * Problem: Median from Data Stream
     * Time Complexity: O(log n) for add, O(1) for median
     * Space Complexity: O(n)
     */
    static class MedianFinder {
        private PriorityQueue<Integer> maxHeap; // Left half (max heap)
        private PriorityQueue<Integer> minHeap; // Right half (min heap)
        
        public MedianFinder() {
            maxHeap = new PriorityQueue<>(Collections.reverseOrder());
            minHeap = new PriorityQueue<>();
        }
        
        public void addNum(int num) {
            if (maxHeap.isEmpty() || num <= maxHeap.peek()) {
                maxHeap.offer(num);
            } else {
                minHeap.offer(num);
            }
            
            // Balance heaps
            if (maxHeap.size() > minHeap.size() + 1) {
                minHeap.offer(maxHeap.poll());
            } else if (minHeap.size() > maxHeap.size()) {
                maxHeap.offer(minHeap.poll());
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
     * Problem: Min Cost to Connect All Ropes
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int connectRopes(int[] ropes) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        
        for (int rope : ropes) {
            minHeap.offer(rope);
        }
        
        int totalCost = 0;
        
        while (minHeap.size() > 1) {
            int first = minHeap.poll();
            int second = minHeap.poll();
            int cost = first + second;
            
            totalCost += cost;
            minHeap.offer(cost);
        }
        
        return totalCost;
    }
    
    /**
     * Problem: Last Stone Weight
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int lastStoneWeight(int[] stones) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        
        for (int stone : stones) {
            maxHeap.offer(stone);
        }
        
        while (maxHeap.size() > 1) {
            int first = maxHeap.poll();
            int second = maxHeap.poll();
            
            if (first != second) {
                maxHeap.offer(first - second);
            }
        }
        
        return maxHeap.isEmpty() ? 0 : maxHeap.peek();
    }
    
    public static void main(String[] args) {
        int[] nums = {3, 2, 1, 5, 6, 4};
        System.out.println("3rd Largest: " + findKthLargest(nums, 3));
        System.out.println("3rd Smallest: " + findKthSmallest(nums, 3));
        
        int[] nums2 = {1, 1, 1, 2, 2, 3};
        int[] topK = topKFrequent(nums2, 2);
        System.out.print("Top 2 Frequent: ");
        for (int x : topK) System.out.print(x + " ");
        System.out.println();
        
        int[] ropes = {4, 3, 2, 6};
        System.out.println("Min cost to connect ropes: " + connectRopes(ropes));
        
        int[] stones = {2, 7, 4, 1, 8, 1};
        System.out.println("Last stone weight: " + lastStoneWeight(stones));
        
        MedianFinder mf = new MedianFinder();
        mf.addNum(1);
        System.out.println("Median: " + mf.findMedian());
        mf.addNum(2);
        System.out.println("Median: " + mf.findMedian());
        mf.addNum(3);
        System.out.println("Median: " + mf.findMedian());
    }
}
