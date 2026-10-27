/**
 * DSA 450 Problems - Stacks & Queues (Part 1: Problems 1-16)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class StacksQueuesProblems1 {
    
    /**
     * Problem 1: Stack Implementation
     */
    static class Stack<T> {
        private List<T> items = new ArrayList<>();
        
        public void push(T item) { items.add(item); }
        public T pop() { return items.remove(items.size() - 1); }
        public T peek() { return items.isEmpty() ? null : items.get(items.size() - 1); }
        public boolean isEmpty() { return items.isEmpty(); }
        public int size() { return items.size(); }
    }
    
    /**
     * Problem 2: Queue Implementation
     */
    static class Queue<T> {
        private List<T> items = new ArrayList<>();
        
        public void enqueue(T item) { items.add(item); }
        public T dequeue() { return items.remove(0); }
        public boolean isEmpty() { return items.isEmpty(); }
    }
    
    /**
     * Problem 3: Check Balanced Parentheses
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean isBalanced(String s) {
        java.util.Stack<Character> stack = new java.util.Stack<>();
        
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.pop() != '(') return false;
            } else if (c == ']') {
                if (stack.isEmpty() || stack.pop() != '[') return false;
            } else if (c == '}') {
                if (stack.isEmpty() || stack.pop() != '{') return false;
            }
        }
        
        return stack.isEmpty();
    }
    
    /**
     * Problem 4: Next Greater Element
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] nextGreaterElement(int[] arr) {
        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && stack.peek() <= arr[i]) {
                stack.pop();
            }
            
            if (!stack.isEmpty()) {
                result[i] = stack.peek();
            }
            
            stack.push(arr[i]);
        }
        
        return result;
    }
    
    /**
     * Problem 5: Largest Rectangle in Histogram
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int largestRectangle(int[] heights) {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        int maxArea = 0;
        int i = 0;
        
        while (i < heights.length) {
            if (stack.isEmpty() || heights[i] >= heights[stack.peek()]) {
                stack.push(i);
                i++;
            } else {
                int top = stack.pop();
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, heights[top] * width);
            }
        }
        
        while (!stack.isEmpty()) {
            int top = stack.pop();
            int width = stack.isEmpty() ? i : i - stack.peek() - 1;
            maxArea = Math.max(maxArea, heights[top] * width);
        }
        
        return maxArea;
    }
    
    /**
     * Problem 6: Stock Span
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] stockSpan(int[] prices) {
        int n = prices.length;
        int[] span = new int[n];
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && prices[stack.peek()] <= prices[i]) {
                stack.pop();
            }
            
            span[i] = stack.isEmpty() ? i + 1 : i - stack.peek();
            stack.push(i);
        }
        
        return span;
    }
    
    /**
     * Problem 7: Reverse String Using Stack
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String reverseString(String s) {
        java.util.Stack<Character> stack = new java.util.Stack<>();
        for (char c : s.toCharArray()) {
            stack.push(c);
        }
        
        StringBuilder result = new StringBuilder();
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        
        return result.toString();
    }
    
    /**
     * Problem 8: Infix to Postfix Conversion
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String infixToPostfix(String infix) {
        java.util.Stack<Character> stack = new java.util.Stack<>();
        StringBuilder result = new StringBuilder();
        
        for (char c : infix.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    result.append(stack.pop());
                }
                stack.pop();
            } else {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(c)) {
                    result.append(stack.pop());
                }
                stack.push(c);
            }
        }
        
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        
        return result.toString();
    }
    
    private static int precedence(char c) {
        if (c == '+' || c == '-') return 1;
        if (c == '*' || c == '/') return 2;
        return 0;
    }
    
    /**
     * Problem 9: LRU Cache Using Queue
     * Time Complexity: O(1)
     * Space Complexity: O(capacity)
     */
    static class LRUCache {
        private Map<Integer, Integer> cache;
        private java.util.Queue<Integer> queue;
        private int capacity;
        
        public LRUCache(int cap) {
            capacity = cap;
            cache = new LinkedHashMap<>(cap);
            queue = new java.util.LinkedList<>();
        }
        
        public int get(int key) {
            if (!cache.containsKey(key)) return -1;
            queue.remove(key);
            queue.add(key);
            return cache.get(key);
        }
        
        public void put(int key, int value) {
            if (cache.containsKey(key)) {
                queue.remove(key);
            } else if (cache.size() == capacity) {
                int removed = queue.poll();
                cache.remove(removed);
            }
            
            cache.put(key, value);
            queue.add(key);
        }
    }
    
    /**
     * Problem 10: Circular Queue Implementation
     * Time Complexity: O(1)
     * Space Complexity: O(capacity)
     */
    static class CircularQueue {
        private int[] arr;
        private int front, rear, size, capacity;
        
        public CircularQueue(int cap) {
            capacity = cap;
            arr = new int[capacity];
            front = rear = -1;
            size = 0;
        }
        
        public void enqueue(int data) {
            if (size == capacity) return;
            
            if (front == -1) front = 0;
            rear = (rear + 1) % capacity;
            arr[rear] = data;
            size++;
        }
        
        public int dequeue() {
            if (size == 0) return -1;
            
            int data = arr[front];
            front = (front + 1) % capacity;
            size--;
            
            if (size == 0) front = rear = -1;
            return data;
        }
    }
    
    /**
     * Problem 11: Implement 2 Stacks in Array
     * Time Complexity: O(1)
     * Space Complexity: O(n)
     */
    static class TwoStacks {
        private int[] arr;
        private int top1, top2;
        
        public TwoStacks(int n) {
            arr = new int[n];
            top1 = -1;
            top2 = n;
        }
        
        public void push1(int data) {
            if (top1 + 1 < top2) {
                arr[++top1] = data;
            }
        }
        
        public void push2(int data) {
            if (top1 + 1 < top2) {
                arr[--top2] = data;
            }
        }
        
        public int pop1() {
            return top1 >= 0 ? arr[top1--] : -1;
        }
        
        public int pop2() {
            return top2 < arr.length ? arr[top2++] : -1;
        }
    }
    
    /**
     * Problem 12: Trapping Rain Water
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int trapWater(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;
        
        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    water += leftMax - height[left];
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    water += rightMax - height[right];
                }
                right--;
            }
        }
        
        return water;
    }
    
    /**
     * Problem 13: Expression Evaluation using Stack
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int evaluatePostfix(String expr) {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        String[] tokens = expr.split(" ");
        
        for (String token : tokens) {
            if (token.matches("-?\\d+")) {
                stack.push(Integer.parseInt(token));
            } else {
                int b = stack.pop();
                int a = stack.pop();
                
                switch (token) {
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/": stack.push(a / b); break;
                }
            }
        }
        
        return stack.pop();
    }
    
    /**
     * Problem 14: Reverse Polish Notation
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int evalRPN(String[] tokens) {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        
        for (String token : tokens) {
            if (token.matches("-?\\d+")) {
                stack.push(Integer.parseInt(token));
            } else {
                int b = stack.pop();
                int a = stack.pop();
                
                switch (token) {
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/": stack.push(a / b); break;
                }
            }
        }
        
        return stack.pop();
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Stacks & Queues (Part 1) ===\n");
        System.out.println("3. Balanced Parentheses: " + isBalanced("()[]{}"));
        System.out.println("12. Trapping Water: " + trapWater(new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));
    }
}
