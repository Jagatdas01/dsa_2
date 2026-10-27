/**
 * DSA Pattern: Queue Operations
 * Topics: Enqueue, dequeue, FIFO principle, applications
 */

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
     * Generate first n numbers with digits 0 and 1 only using BFS
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
     * Find maximum value in each window of size k
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
        Queue queue = new Queue();
        
        // Process first window
        for (int i = 0; i < k; i++) {
            while (!queue.isEmpty() && arr[queue.front()] <= arr[i]) {
                queue.dequeue();
            }
            queue.enqueue(i);
        }
        
        result[0] = arr[queue.front()];
        
        // Process remaining elements
        for (int i = k; i < arr.length; i++) {
            // Remove elements outside current window
            if (!queue.isEmpty() && queue.front() <= i - k) {
                queue.dequeue();
            }
            
            // Remove smaller elements
            while (!queue.isEmpty() && arr[queue.front()] <= arr[i]) {
                queue.dequeue();
            }
            
            queue.enqueue(i);
            result[i - k + 1] = arr[queue.front()];
        }
        
        return result;
    }
    
    /**
     * Problem: Rotting Oranges - Multi-source BFS
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
