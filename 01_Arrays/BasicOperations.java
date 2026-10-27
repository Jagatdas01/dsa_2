/**
 * DSA Pattern: Basic Array Operations
 * Topics: Array manipulation, traversal, insertion, deletion
 */

public class BasicOperations {
    
    /**
     * Problem: Find element in array
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findElement(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Problem: Reverse array in place
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void reverseArray(int[] arr) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    
    /**
     * Problem: Rotate array by k positions
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void rotateArray(int[] arr, int k) {
        k = k % arr.length;
        reverseArray(arr, 0, arr.length - 1);
        reverseArray(arr, 0, k - 1);
        reverseArray(arr, k, arr.length - 1);
    }
    
    private static void reverseArray(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }
    
    /**
     * Problem: Find maximum element in array
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }
        return max;
    }
    
    /**
     * Problem: Calculate sum of array elements
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static long arraySum(int[] arr) {
        long sum = 0;
        for (int num : arr) {
            sum += num;
        }
        return sum;
    }
    
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        
        System.out.println("Original Array:");
        printArray(arr);
        
        System.out.println("\nFind element 3: " + findElement(arr, 3));
        
        reverseArray(arr);
        System.out.println("\nReversed Array:");
        printArray(arr);
        
        int[] arr2 = {1, 2, 3, 4, 5};
        rotateArray(arr2, 2);
        System.out.println("\nRotated Array by 2:");
        printArray(arr2);
        
        System.out.println("\nMax Element: " + findMax(arr2));
        System.out.println("Sum: " + arraySum(arr2));
    }
    
    private static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
