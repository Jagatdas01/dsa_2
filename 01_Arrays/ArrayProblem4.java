/**
 * Maximum number of 4-person families that can be seated.
 * Row layout: A B C | D E F G | H J K  (no letter I, aisles after C and G).
 * A family may sit in one of these groups of 4 seats in a single row:
 *   B C D E  (2 + 2 around the first aisle)
 *   D E F G  (4 together in the middle block)
 *   F G H J  (2 + 2 around the second aisle)
 * Time Complexity: O(N + length of S)
 * Space Complexity: O(N)
 */
public class ArrayProblem4 {

    private static final String SEATS = "ABCDEFGHJK";

    public int solution(int N, String S) {
        boolean[][] reserved = seatMap(N, S);
        int families = 0;

        for (int row = 0; row < N; row++) {
            boolean overFirstAisle = isFree(reserved[row], 1, 4);  // B C D E
            boolean middleBlock = isFree(reserved[row], 3, 6);     // D E F G
            boolean overSecondAisle = isFree(reserved[row], 5, 8); // F G H J

            if (overFirstAisle && overSecondAisle) families += 2;
            else if (overFirstAisle || middleBlock || overSecondAisle) families += 1;
        }
        return families;
    }

    // S looks like "1A 20G 5A"; returns reserved[row][seatIndex]
    private boolean[][] seatMap(int N, String S) {
        boolean[][] reserved = new boolean[N][SEATS.length()];
        if (S == null || S.trim().isEmpty()) return reserved;

        for (String seat : S.trim().split("\\s+")) {
            int row = Integer.parseInt(seat.substring(0, seat.length() - 1)) - 1;
            int column = SEATS.indexOf(seat.charAt(seat.length() - 1));
            reserved[row][column] = true;
        }
        return reserved;
    }

    private boolean isFree(boolean[] row, int from, int to) {
        for (int i = from; i <= to; i++) {
            if (row[i]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        ArrayProblem4 s = new ArrayProblem4();
        System.out.println(s.solution(2, "1A 2F 1C"));  // 2
        System.out.println(s.solution(1, ""));          // 2
        System.out.println(s.solution(1, "1E"));        // 1 (F G H J)
        System.out.println(s.solution(22, "1A 3C 2B 20G 5A"));     // 1
    }
}
