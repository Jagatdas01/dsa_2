/**
 * DSA Pattern: Queue Operations
 * Topics: Enqueue, dequeue, FIFO principle, applications
 */

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class QueuePatterns {
    
    static class Queue {
        private List<Integer> data;
        
        public Queue() {
            this.data = new LinkedList<>();
        }
        
        public void enqueue(int value) {
            data.add(value);
        }
        
        public int dequeue() {
            if (isEmpty()) throw new RuntimeException("Queue underflow");
            return data.remove(0);
        }
        
        public int front() {
            if (isEmpty()) throw new RuntimeException("Queue is empty");
            return data.get(0);
        }
        
        public boolean isEmpty() {
            return data.isEmpty();
        }
        
        public int size() {
            return data.size();
        }
    }
    
    /**
     * Problem: Generate Numbers with Given Digits using Queue
     * Print the first n positive numbers, in increasing order, that contain
     * only the digits 0 and 1. Use BFS: start with 1; for every number x taken
     * from the queue, print it and enqueue x*10 and x*10+1.
     * 
     * Input: n = 5
     * Expected Output: 1 10 11 100 101
     * Explanation: First 5 numbers using only digits 0 and 1
     * 
     * Input: n = 3
     * Expected Output: 1 10 11
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - queue size
     */
    public static void generateNumbers(int n) {
        Queue queue = new Queue();
        queue.enqueue(1);
        
        while (n > 0) {
            int num = queue.dequeue();
            System.out.print(num + " ");
            
            queue.enqueue(num * 10);
            queue.enqueue(num * 10 + 1);
            
            n--;
        }
        System.out.println();
    }
    
    /**
     * Problem: Sliding Window Maximum
     * Given an array arr and a window size k, slide a window of k consecutive
     * elements from left to right and output the maximum of each window
     * (n - k + 1 values in total).
     * Approach: keep a deque of indices whose values are in decreasing order.
     * The front is the current maximum; drop indices that left the window from
     * the front, and smaller values from the back.
     * 
     * Input: arr = [1, 3, -1, -3, 5, 3, 6, 7], k = 3
     * Expected Output: [3, 3, 5, 5, 6, 7]
     * Explanation: Windows: [1,3,-1]->3, [3,-1,-3]->3, [-1,-3,5]->5, etc.
     * 
     * Input: arr = [1], k = 1
     * Expected Output: [1]
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(k) - deque for indices
     */
    public static int[] slidingWindowMax(int[] arr, int k) {
        if (arr.length < k || k <= 0) return new int[0];
        
        int[] result = new int[arr.length - k + 1];
        Deque<Integer> deque = new ArrayDeque<>();
        
        for (int i = 0; i < arr.length; i++) {
            // Remove index that is outside the current window
            if (!deque.isEmpty() && deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }
            
            // Remove smaller elements from the back; they can never be a maximum
            while (!deque.isEmpty() && arr[deque.peekLast()] <= arr[i]) {
                deque.pollLast();
            }
            
            deque.addLast(i);
            
            if (i >= k - 1) {
                result[i - k + 1] = arr[deque.peekFirst()];
            }
        }
        
        return result;
    }
    
    /**
     * Problem: Rotting Oranges - Multi-source BFS
     * Given an m x n grid where 0 = empty, 1 = fresh orange, 2 = rotten orange.
     * Every minute, each fresh orange that is 4-directionally adjacent to a
     * rotten orange becomes rotten. Return the minimum number of minutes until
     * no fresh orange remains, or -1 if that is impossible.
     * Approach: enqueue all rotten oranges at once; each BFS level is one minute.
     * 
     * Input: grid = [[2,1,1],[1,1,0],[0,1,1]]
     * Expected Output: 4
     * 
     * Input: grid = [[2,1,1],[0,1,1],[1,0,1]]
     * Expected Output: -1 (the bottom-left orange is never reached)
     * 
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int rottenOranges(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int rows = grid.length;
        int cols = grid[0].length;
        Queue queue = new Queue();
        int freshOranges = 0;
        
        // Find all rotten oranges
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 2) {
                    queue.enqueue(i * cols + j);
                } else if (grid[i][j] == 1) {
                    freshOranges++;
                }
            }
        }
        
        if (freshOranges == 0) return 0;
        
        int time = 0;
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        
        while (!queue.isEmpty()) {
            int size = queue.size();
            boolean rotted = false;
            
            for (int i = 0; i < size; i++) {
                int pos = queue.dequeue();
                int row = pos / cols;
                int col = pos % cols;
                
                for (int[] dir : directions) {
                    int newRow = row + dir[0];
                    int newCol = col + dir[1];
                    
                    if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols &&
                        grid[newRow][newCol] == 1) {
                        grid[newRow][newCol] = 2;
                        queue.enqueue(newRow * cols + newCol);
                        freshOranges--;
                        rotted = true;
                    }
                }
            }
            
            if (rotted) time++;
        }
        
        return freshOranges == 0 ? time : -1;
    }
    
    /**
     * Problem: First Negative in Every Window
     * For every window of size k, print the first (leftmost) negative number in
     * that window, or 0 if the window has no negative number.
     * Approach: the queue stores indices of negative numbers; the front is the
     * first negative of the current window.
     * 
     * Input: arr = [-1, 2, -1, 3, -2, -2], k = 3
     * Expected Output: -1 -1 -1 -2
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(k)
     */
    public static void firstNegativeInWindow(int[] arr, int k) {
        Queue queue = new Queue();
        
        // Process first window
        for (int i = 0; i < k; i++) {
            if (arr[i] < 0) {
                queue.enqueue(i);
            }
        }
        
        System.out.print((queue.isEmpty() ? 0 : arr[queue.front()]) + " ");
        
        // Process remaining elements
        for (int i = k; i < arr.length; i++) {
            // Remove elements outside current window
            if (!queue.isEmpty() && queue.front() <= i - k) {
                queue.dequeue();
            }
            
            if (arr[i] < 0) {
                queue.enqueue(i);
            }
            
            System.out.print((queue.isEmpty() ? 0 : arr[queue.front()]) + " ");
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        System.out.println("Generate first 5 numbers:");
        generateNumbers(5);
        
        int[] arr = {1, 3, -1, -3, 5, 3, 6, 7};
        int[] max = slidingWindowMax(arr, 3);
        System.out.print("Sliding Window Max (k=3): ");
        for (int x : max) System.out.print(x + " ");
        System.out.println();
        
        int[][] grid = {{2, 1, 1}, {1, 1, 0}, {0, 1, 1}};
        System.out.println("Time for oranges to rot: " + rottenOranges(grid));
        
        int[] arr2 = {-1, 2, -1, 3, -2, -2};
        System.out.print("First Negative in Window (k=3): ");
        firstNegativeInWindow(arr2, 3);
    }
}
