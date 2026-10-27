/**
 * DSA Pattern: Stack Operations
 * Topics: Push, pop, LIFO principle, applications
 */

import java.util.ArrayList;
import java.util.List;

public class StackPatterns {
    
    static class Stack {
        private List<Integer> data;
        
        public Stack() {
            this.data = new ArrayList<>();
        }
        
        public void push(int value) {
            data.add(value);
        }
        
        public int pop() {
            if (isEmpty()) throw new RuntimeException("Stack underflow");
            return data.remove(data.size() - 1);
        }
        
        public int peek() {
            if (isEmpty()) throw new RuntimeException("Stack is empty");
            return data.get(data.size() - 1);
        }
        
        public boolean isEmpty() {
            return data.isEmpty();
        }
        
        public int size() {
            return data.size();
        }
    }
    
    /**
     * Problem: Check Balanced Parentheses/Brackets
     * Verify all opening brackets have matching closing brackets in correct order
     * 
     * Input: s = "(){[]}" 
     * Expected Output: true (all brackets balanced)
     * 
     * Input: s = "([)]" 
     * Expected Output: false (mismatched order)
     * 
     * Input: s = "(()" 
     * Expected Output: false (missing closing bracket)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - stack size
     */
    public static boolean isBalanced(String s) {
        Stack stack = new Stack();
        
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return false;
                
                int top = stack.pop();
                if ((c == ')' && top != '(') || 
                    (c == ']' && top != '[') || 
                    (c == '}' && top != '{')) {
                    return false;
                }
            }
        }
        
        return stack.isEmpty();
    }
    
    /**
     * Problem: Next Greater Element
     * For each element, find nearest greater element to its right
     * 
     * Input: arr = [1, 5, 0, 3, 4, 5]
     * Expected Output: [5, -1, 3, 4, 5, -1]
     * Explanation: For 1, next greater is 5; for 5, no greater element exists
     * 
     * Input: arr = [1, 2, 3]
     * Expected Output: [-1, -1, -1] (array is increasing)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - stack for result
     */
    public static int[] nextGreaterElement(int[] arr) {
        int n = arr.length;
        int[] result = new int[n];
        Stack stack = new Stack();
        
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && stack.peek() <= arr[i]) {
                stack.pop();
            }
            
            result[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(arr[i]);
        }
        
        return result;
    }
    
    /**
     * Problem: Largest Rectangle in Histogram
     * Find largest rectangular area in histogram with height array
     * 
     * Input: heights = [2, 1, 5, 6, 2, 3]
     * Expected Output: 10 (height 5 * width 2 or height 6 * width 1, max is 10)
     * 
     * Input: heights = [2, 4]
     * Expected Output: 4 (height 4 * width 1)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - stack
     */
    public static int largestRectangle(int[] heights) {
        Stack stack = new Stack();
        int maxArea = 0;
        int index = 0;
        
        while (index < heights.length) {
            if (stack.isEmpty() || heights[index] >= heights[stack.peek()]) {
                stack.push(index);
                index++;
            } else {
                int topIndex = stack.pop();
                int width = stack.isEmpty() ? index : index - stack.peek() - 1;
                int area = heights[topIndex] * width;
                maxArea = Math.max(maxArea, area);
            }
        }
        
        while (!stack.isEmpty()) {
            int topIndex = stack.pop();
            int width = stack.isEmpty() ? index : index - stack.peek() - 1;
            int area = heights[topIndex] * width;
            maxArea = Math.max(maxArea, area);
        }
        
        return maxArea;
    }
    
    /**
     * Problem: Reverse string using stack
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String reverseString(String s) {
        Stack stack = new Stack();
        
        for (char c : s.toCharArray()) {
            stack.push(c);
        }
        
        StringBuilder result = new StringBuilder();
        while (!stack.isEmpty()) {
            result.append((char) stack.pop());
        }
        
        return result.toString();
    }
    
    /**
     * Problem: Implement Min Stack
     */
    static class MinStack {
        private Stack main;
        private Stack minStack;
        
        public MinStack() {
            main = new Stack();
            minStack = new Stack();
        }
        
        public void push(int val) {
            main.push(val);
            if (minStack.isEmpty() || val <= minStack.peek()) {
                minStack.push(val);
            }
        }
        
        public void pop() {
            if (main.pop() == minStack.peek()) {
                minStack.pop();
            }
        }
        
        public int top() {
            return main.peek();
        }
        
        public int getMin() {
            return minStack.peek();
        }
    }
    
    /**
     * Problem: Next Smaller Element (Monotonic Stack)
     * Find next smaller element to the right for each element
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] nextSmallerElement(int[] arr) {
        int n = arr.length;
        int[] result = new int[n];
        Stack stack = new Stack();
        
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && stack.peek() >= arr[i]) {
                stack.pop();
            }
            
            result[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(arr[i]);
        }
        
        return result;
    }
    
    /**
     * Problem: Daily Temperatures (Monotonic Stack Application)
     * Find days until warmer temperature
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack stack = new Stack();
        
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }
        
        return result;
    }
    
    /**
     * Problem: Trapping Rain Water (Monotonic Stack Approach)
     * Calculate trapped water between bars
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int trapWater(int[] height) {
        int n = height.length;
        if (n < 3) return 0;
        
        Stack stack = new Stack();
        int water = 0;
        
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && height[i] > height[stack.peek()]) {
                int bottomIndex = stack.pop();
                
                if (stack.isEmpty()) break;
                
                int leftIndex = stack.peek();
                int width = i - leftIndex - 1;
                int boundedHeight = Math.min(height[leftIndex], height[i]) - height[bottomIndex];
                water += width * boundedHeight;
            }
            
            stack.push(i);
        }
        
        return water;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Stack Patterns ===\n");
        
        System.out.println("Is '{[()]}' balanced? " + isBalanced("{[()]}"));
        System.out.println("Is '({[]})' balanced? " + isBalanced("({[]})"));
        System.out.println("Is '{[(])}' balanced? " + isBalanced("{[(])}"));
        
        int[] arr = {1, 5, 0, 3, 4, 5};
        int[] result = nextGreaterElement(arr);
        System.out.print("\nNext Greater Elements: ");
        for (int x : result) System.out.print(x + " ");
        
        int[] result2 = nextSmallerElement(arr);
        System.out.print("\nNext Smaller Elements: ");
        for (int x : result2) System.out.print(x + " ");
        
        System.out.println();
        
        int[] heights = {2, 1, 5, 6, 2, 3};
        System.out.println("Largest Rectangle: " + largestRectangle(heights));
        
        int[] temps = {73, 74, 75, 71, 69, 72, 76, 73};
        int[] tempResult = dailyTemperatures(temps);
        System.out.print("Daily Temperatures result: ");
        for (int x : tempResult) System.out.print(x + " ");
        
        int[] elevation = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        System.out.println("\nTrapping rain water: " + trapWater(elevation));
        
        MinStack minStack = new MinStack();
        minStack.push(3);
        minStack.push(1);
        minStack.push(2);
        System.out.println("Min Stack top: " + minStack.top());
        System.out.println("Min Stack minimum: " + minStack.getMin());
    }
}
