/**
 * Smallest positive integer (> 0) that does not occur in A.
 * Time Complexity: O(n)
 * Space Complexity: O(n)
 */
public class ArratProblem3 {

    public int solution(int[] A) {
        int n = A.length;
        // The answer is always within [1..n+1]
        boolean[] seen = new boolean[n + 2];

        for (int num : A) {
            if (num > 0 && num <= n) {
                seen[num] = true;
            }
        }

        for (int i = 1; i <= n + 1; i++) {
            if (!seen[i]) return i;
        }
        return n + 1;
    }

    public static void main(String[] args) {
        ArratProblem3 s = new ArratProblem3();
        System.out.println(s.solution(new int[]{1, 3, 6, 4, 1, 2})); // 5
        System.out.println(s.solution(new int[]{1, 2, 3}));          // 4
        System.out.println(s.solution(new int[]{-1, -3}));           // 1
    }
}
