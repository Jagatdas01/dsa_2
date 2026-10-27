/**
 * DSA Pattern: Binary Search
 * Topics: Classic binary search, search in rotated array, search range
 */

public class BinarySearchPatterns {
    
    /**
     * Problem: Classic Binary Search
     * Find target element in sorted array
     * 
     * Input: arr = [1, 3, 5, 7, 9], target = 5
     * Expected Output: 2 (index where element found)
     * 
     * Input: arr = [1, 3, 5, 7, 9], target = 4
     * Expected Output: -1 (element not found)
     * 
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Problem: Search in Rotated Sorted Array
     * Find target in array that was sorted then rotated at pivot
     * 
     * Input: arr = [4, 5, 6, 7, 0, 1, 2], target = 0
     * Expected Output: 4 (index where element found)
     * 
     * Input: arr = [4, 5, 6, 7, 0, 1, 2], target = 3
     * Expected Output: -1 (element not in rotated array)
     * 
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int searchRotated(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                return mid;
            }
            
            // Check which half is sorted
            if (arr[left] <= arr[mid]) {
                // Left half is sorted
                if (arr[left] <= target && target < arr[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else {
                // Right half is sorted
                if (arr[mid] < target && target <= arr[right]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Problem: Find First and Last Position of Element
     * Find both first and last occurrence of target in sorted array
     * 
     * Input: arr = [5, 7, 7, 8, 8, 10], target = 8
     * Expected Output: [3, 4] (first and last position of 8)
     * 
     * Input: arr = [5, 7, 7, 8, 8, 10], target = 6
     * Expected Output: [-1, -1] (element not found)
     * 
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int[] searchRange(int[] arr, int target) {
        int[] result = {-1, -1};
        
        // Find first position
        result[0] = findFirst(arr, target);
        
        // Find last position
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
     * Problem: Find Peak Element
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int findPeak(int[] arr) {
        int left = 0, right = arr.length - 1;
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] < arr[mid + 1]) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        
        return left;
    }
    
    /**
     * Problem: Square Root of a Number
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int sqrt(int x) {
        if (x < 2) return x;
        
        int left = 2, right = x / 2;
        
        while (left <= right) {
            long mid = left + (right - left) / 2;
            long square = mid * mid;
            
            if (square == x) {
                return (int) mid;
            } else if (square < x) {
                left = (int) mid + 1;
            } else {
                right = (int) mid - 1;
            }
        }
        
        return right;
    }
    
    public static void main(String[] args) {
        int[] arr = {-1, 0, 3, 5, 9, 12};
        System.out.println("Binary Search for 9: " + binarySearch(arr, 9));
        
        int[] rotated = {4, 5, 6, 7, 0, 1, 2};
        System.out.println("Search Rotated for 0: " + searchRotated(rotated, 0));
        
        int[] arr2 = {5, 7, 7, 8, 8, 10};
        int[] range = searchRange(arr2, 8);
        System.out.println("Search Range for 8: [" + range[0] + ", " + range[1] + "]");
        
        int[] peak = {1, 2, 3, 1};
        System.out.println("Peak element at index: " + findPeak(peak));
        
        System.out.println("Square root of 4: " + sqrt(4));
    }
}
