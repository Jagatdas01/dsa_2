/**
 * DSA Pattern: Two Pointer Technique
 * Topics: Two pointer approach, sliding window, array manipulation
 */

public class TwoPointerTechnique {
    
    /**
     * Problem: Find Pair with Given Sum
     * Find if two distinct elements in array sum to target value
     * 
     * Input: arr = [1, 5, 7, -1, 5], sum = 6
     * Expected Output: true
     * Explanation: Pair (1, 5) or (7, -1) sums to 6
     * 
     * Input: arr = [1, 2, 3], sum = 10
     * Expected Output: false (no pair sums to 10)
     * 
     * Time Complexity: O(n log n) due to sorting
     * Space Complexity: O(1)
     */
    public static boolean findPair(int[] arr, int sum) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int currentSum = arr[left] + arr[right];
            if (currentSum == sum) {
                return true;
            } else if (currentSum < sum) {
                left++;
            } else {
                right--;
            }
        }
        return false;
    }
    
    /**
     * Problem: Remove Duplicates from Sorted Array
     * Remove duplicate elements in-place, return count of unique elements
     * 
     * Input: arr = [1, 1, 2, 2, 3, 3]
     * Expected Output: 3
     * Explanation: Array becomes [1, 2, 3, ...], length = 3
     * 
     * Input: arr = [1, 2, 3, 4]
     * Expected Output: 4 (no duplicates)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int removeDuplicates(int[] arr) {
        if (arr.length == 0) return 0;
        
        int i = 0;
        for (int j = 1; j < arr.length; j++) {
            if (arr[j] != arr[i]) {
                i++;
                arr[i] = arr[j];
            }
        }
        return i + 1;
    }
    
    /**
     * Problem: Container with Most Water
     * Find two lines that form container with maximum area
     * 
     * Input: height = [1, 8, 6, 2, 5, 4, 8, 3, 7]
     * Expected Output: 49
     * Explanation: Lines at indices 1 and 8 (height 8 and 7), width = 7, area = 7 * 7 = 49
     * 
     * Input: height = [1, 1]
     * Expected Output: 1 (area = 1 * 1 = 1)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxArea(int[] height) {
        int maxArea = 0;
        int left = 0, right = height.length - 1;
        
        while (left < right) {
            int width = right - left;
            int currentArea = Math.min(height[left], height[right]) * width;
            maxArea = Math.max(maxArea, currentArea);
            
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxArea;
    }
    
    /**
     * Problem: Valid Palindrome
     * Check if string is palindrome (ignore non-alphanumeric, case-insensitive)
     * 
     * Input: s = "A man, a plan, a canal: Panama"
     * Expected Output: true (ignore spaces/punctuation, case-insensitive)
     * 
     * Input: s = "0P"
     * Expected Output: false ("0P" != "P0" when reversed)
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isValidPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        
        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }
            
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
    
    /**
     * Problem: Merge Two Sorted Arrays
     * Combine two sorted arrays into single sorted array
     * 
     * Input: arr1 = [1, 3, 5], arr2 = [2, 4, 6]
     * Expected Output: [1, 2, 3, 4, 5, 6]
     * 
     * Input: arr1 = [1], arr2 = [1]
     * Expected Output: [1, 1]
     * 
     * Time Complexity: O(n + m)
     * Space Complexity: O(n + m)
     */
    public static int[] mergeSortedArrays(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        int i = 0, j = 0, k = 0;
        
        while (i < arr1.length && j < arr2.length) {
            if (arr1[i] <= arr2[j]) {
                result[k++] = arr1[i++];
            } else {
                result[k++] = arr2[j++];
            }
        }
        
        while (i < arr1.length) {
            result[k++] = arr1[i++];
        }
        
        while (j < arr2.length) {
            result[k++] = arr2[j++];
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        int[] arr = {2, 7, 11, 15};
        System.out.println("Find pair with sum 9: " + findPair(arr, 9));
        
        int[] arr2 = {1, 1, 2, 2, 3, 3};
        System.out.println("Unique elements: " + removeDuplicates(arr2));
        
        int[] height = {1, 8, 6, 2, 5, 4, 8, 3, 7};
        System.out.println("Max area: " + maxArea(height));
        
        System.out.println("Valid palindrome 'A man, a plan, a canal: Panama': " + 
                          isValidPalindrome("A man, a plan, a canal: Panama"));
    }
}
