/**
 * DSA Pattern: Recursion Basics
 * Topics: Base cases, recursive calls, backtracking
 */

public class RecursionPatterns {
    
    /**
     * Problem: Calculate Factorial
     * Find n! = n * (n-1) * (n-2) * ... * 1
     * 
     * Input: n = 5
     * Expected Output: 120 (5! = 5*4*3*2*1)
     * 
     * Input: n = 0
     * Expected Output: 1 (0! = 1 by definition)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - recursion stack
     */
    public static long factorial(int n) {
        // Base case
        if (n <= 1) return 1;
        // Recursive case
        return n * factorial(n - 1);
    }
    
    /**
     * Problem: Fibonacci Number
     * Calculate nth Fibonacci number: F(n) = F(n-1) + F(n-2), F(0)=0, F(1)=1
     * 
     * Input: n = 5
     * Expected Output: 5 (sequence: 0,1,1,2,3,5)
     * 
     * Input: n = 10
     * Expected Output: 55
     * 
     * Time Complexity: O(2^n) without memoization
     * Space Complexity: O(n) - recursion stack
     */
    public static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
    
    /**
     * Problem: Fibonacci with Memoization
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static long fibonacciMemo(int n, long[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        
        memo[n] = fibonacciMemo(n - 1, memo) + fibonacciMemo(n - 2, memo);
        return memo[n];
    }
    
    /**
     * Problem: Power(x, n) - Exponentiation
     * Calculate x raised to power n using fast exponentiation
     * 
     * Input: x = 2.0, n = 10
     * Expected Output: 1024.0 (2^10)
     * 
     * Input: x = 2.0, n = -2
     * Expected Output: 0.25 (1/4 = 1/2^2)
     * 
     * Time Complexity: O(log n)
     * Space Complexity: O(log n) - recursion stack
     */
    public static double power(double x, int n) {
        if (n == 0) return 1.0;
        if (n < 0) {
            return 1.0 / power(x, -n);
        }
        
        double half = power(x, n / 2);
        
        if (n % 2 == 0) {
            return half * half;
        } else {
            return half * half * x;
        }
    }
    
    /**
     * Problem: Sum of Array Elements
     * Calculate sum of all elements in array using recursion
     * 
     * Input: arr = [1, 2, 3, 4, 5]
     * Expected Output: 15 (1+2+3+4+5)
     * 
     * Input: arr = [10]
     * Expected Output: 10 (single element)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - recursion stack
     */
    public static int sumArray(int[] arr, int index) {
        if (index == arr.length) return 0;
        return arr[index] + sumArray(arr, index + 1);
    }
    
    /**
     * Problem: Count Occurrences of Element
     * Count how many times target element appears in array
     * 
     * Input: arr = [1, 2, 3, 2, 4, 2], target = 2
     * Expected Output: 3 (element 2 appears 3 times)
     * 
     * Input: arr = [5, 5, 5], target = 5
     * Expected Output: 3 (all elements match)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) - recursion stack
     */
    public static int countOccurrences(int[] arr, int index, int target) {
        if (index == arr.length) return 0;
        
        int count = 0;
        if (arr[index] == target) {
            count = 1;
        }
        
        return count + countOccurrences(arr, index + 1, target);
    }
    
    /**
     * Problem: Find maximum in array
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int findMax(int[] arr, int index) {
        if (index == arr.length - 1) return arr[index];
        
        int maxRest = findMax(arr, index + 1);
        return Math.max(arr[index], maxRest);
    }
    
    /**
     * Problem: Check if palindrome recursively
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean isPalindrome(String s, int left, int right) {
        if (left >= right) return true;
        
        if (s.charAt(left) != s.charAt(right)) {
            return false;
        }
        
        return isPalindrome(s, left + 1, right - 1);
    }
    
    /**
     * Problem: Generate all subsets
     * Time Complexity: O(2^n)
     * Space Complexity: O(n)
     */
    public static void generateSubsets(int[] arr, int index, StringBuilder current) {
        if (index == arr.length) {
            System.out.println(current.toString());
            return;
        }
        
        // Exclude current element
        generateSubsets(arr, index + 1, current);
        
        // Include current element
        generateSubsets(arr, index + 1, current.append(arr[index]).append(" "));
        current.setLength(current.length() - 2); // Backtrack
    }
    
    public static void main(String[] args) {
        System.out.println("Factorial of 5: " + factorial(5));
        
        System.out.println("Fibonacci of 6: " + fibonacci(6));
        
        long[] memo = new long[20];
        System.out.println("Fibonacci of 10 (with memo): " + fibonacciMemo(10, memo));
        
        System.out.println("2^10 = " + power(2, 10));
        
        int[] arr = {1, 2, 3, 4, 5};
        System.out.println("Sum of array: " + sumArray(arr, 0));
        
        System.out.println("Count of 3 in array: " + countOccurrences(arr, 0, 3));
        
        System.out.println("Max in array: " + findMax(arr, 0));
        
        System.out.println("Is 'racecar' palindrome? " + isPalindrome("racecar", 0, 6));
    }
}
