/**
 * DSA Pattern: Bit Manipulation
 * Topics: Bitwise operations, bit tricks, number representation
 */

public class BitManipulation {
    
    /**
     * Problem: Count set bits (1s in binary)
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int countSetBits(int n) {
        int count = 0;
        while (n > 0) {
            count += n & 1;
            n >>= 1;
        }
        return count;
    }
    
    /**
     * Problem: Check if number is power of 2
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Problem: Single Number (find the one number that appears once when others appear twice)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int singleNumber(int[] nums) {
        int result = 0;
        for (int num : nums) {
            result ^= num;
        }
        return result;
    }
    
    /**
     * Problem: Single Number III (find two numbers appearing once, rest appear twice)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int[] singleNumberIII(int[] nums) {
        int xor = 0;
        for (int num : nums) {
            xor ^= num;
        }
        
        // Find rightmost set bit
        int rightBit = xor & (-xor);
        
        int num1 = 0, num2 = 0;
        for (int num : nums) {
            if ((num & rightBit) == 0) {
                num1 ^= num;
            } else {
                num2 ^= num;
            }
        }
        
        return new int[]{num1, num2};
    }
    
    /**
     * Problem: Reverse bits
     * Time Complexity: O(32)
     * Space Complexity: O(1)
     */
    public static long reverseBits(long n) {
        long result = 0;
        for (int i = 0; i < 32; i++) {
            result = (result << 1) | (n & 1);
            n >>= 1;
        }
        return result;
    }
    
    /**
     * Problem: Get Bit at position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static boolean getBit(int num, int position) {
        return (num & (1 << position)) != 0;
    }
    
    /**
     * Problem: Set Bit at position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static int setBit(int num, int position) {
        return num | (1 << position);
    }
    
    /**
     * Problem: Clear Bit at position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static int clearBit(int num, int position) {
        return num & ~(1 << position);
    }
    
    /**
     * Problem: Toggle Bit at position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static int toggleBit(int num, int position) {
        return num ^ (1 << position);
    }
    
    /**
     * Problem: Is Bit Set
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static boolean isBitSet(int num, int position) {
        return (num & (1 << position)) != 0;
    }
    
    /**
     * Problem: Hamming Distance (count differing bits)
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int hammingDistance(int x, int y) {
        int xor = x ^ y;
        int count = 0;
        
        while (xor > 0) {
            count += xor & 1;
            xor >>= 1;
        }
        
        return count;
    }
    
    /**
     * Problem: Number of 1 bits
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static int hammingWeight(int n) {
        int count = 0;
        while (n != 0) {
            count += n & 1;
            n >>>= 1;
        }
        return count;
    }
    
    /**
     * Problem: Missing Number
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int missingNumber(int[] nums) {
        int xor = 0;
        int xorAll = 0;
        
        for (int i = 0; i < nums.length; i++) {
            xor ^= nums[i];
            xorAll ^= i;
        }
        
        return xor ^ xorAll ^ nums.length;
    }
    
    /**
     * Problem: Binary representation
     * Time Complexity: O(log n)
     * Space Complexity: O(log n)
     */
    public static String toBinaryString(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.insert(0, n & 1);
            n >>= 1;
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Count set bits in 5: " + countSetBits(5));
        System.out.println("Is 8 power of 2? " + isPowerOfTwo(8));
        System.out.println("Is 6 power of 2? " + isPowerOfTwo(6));
        
        int[] nums = {2, 2, 3, 3, 4};
        System.out.println("Single number: " + singleNumber(nums));
        
        int[] nums2 = {1, 2, 1, 3, 2, 5};
        int[] result = singleNumberIII(nums2);
        System.out.println("Two single numbers: " + result[0] + ", " + result[1]);
        
        System.out.println("Hamming distance between 1 and 4: " + hammingDistance(1, 4));
        
        System.out.println("Binary representation of 5: " + toBinaryString(5));
        
        int[] arr = {3, 0, 1};
        System.out.println("Missing number: " + missingNumber(arr));
        
        System.out.println("Hamming weight of 11: " + hammingWeight(11));
    }
}
