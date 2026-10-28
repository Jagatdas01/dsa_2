/**
 * Move Zeroes to End
 * Move all zeroes to the end of the array while keeping the relative
 * order of the non-zero elements.
 *
 * Input : arr = [1, 0, 2, 4, 0, 2, 0, 8]
 * Output: arr = [1, 2, 4, 2, 8, 0, 0, 0]
 *
 * Approach (two pointers): 'insertPos' marks where the next non-zero
 * element should go. Scan the array; swap every non-zero element into
 * 'insertPos'. Zeroes naturally shift to the end.
 *
 * Time Complexity: O(n)
 * Space Complexity: O(1)
 */

import java.util.Arrays;

public class MoveZeroesToEnd {

    public static void moveZeroes(int[] arr) {
        int insertPos = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                int temp = arr[insertPos];
                arr[insertPos] = arr[i];
                arr[i] = temp;
                insertPos++;
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 0, 2, 4, 0, 2, 0, 8};
        System.out.println("Input : " + Arrays.toString(arr));
        moveZeroes(arr);
        System.out.println("Output: " + Arrays.toString(arr));
    }
}
