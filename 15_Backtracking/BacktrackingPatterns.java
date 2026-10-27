/**
 * DSA Pattern: Backtracking Algorithms
 * Topics: N-Queens, Permutations, Combinations, Sudoku
 */

import java.util.*;

public class BacktrackingPatterns {
    
    /**
     * Problem: Generate All Permutations
     * Generate all possible arrangements of elements
     * 
     * Input: nums = [1, 2, 3]
     * Expected Output: [[1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], [3,2,1]]
     * Total: 6 permutations (3! = 6)
     * 
     * Input: nums = [1, 2]
     * Expected Output: [[1,2], [2,1]]
     * 
     * Time Complexity: O(n!)
     * Space Complexity: O(n) - recursion depth
     */
    public static List<List<Integer>> permutations(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackPermute(nums, new ArrayList<>(), new boolean[nums.length], result);
        return result;
    }
    
    private static void backtrackPermute(int[] nums, List<Integer> current, boolean[] used, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = 0; i < nums.length; i++) {
            if (!used[i]) {
                used[i] = true;
                current.add(nums[i]);
                backtrackPermute(nums, current, used, result);
                current.remove(current.size() - 1);
                used[i] = false;
            }
        }
    }
    
    /**
     * Problem: Generate All Combinations
     * Generate all ways to choose k elements from n elements
     * 
     * Input: n = 4, k = 2
     * Expected Output: [[1,2], [1,3], [1,4], [2,3], [2,4], [3,4]]
     * Total: C(4,2) = 6 combinations
     * 
     * Input: n = 3, k = 1
     * Expected Output: [[1], [2], [3]]
     * 
     * Time Complexity: O(C(n,k) * k) where C(n,k) = n!/(k!(n-k)!)
     * Space Complexity: O(k) - recursion depth
     */
    public static List<List<Integer>> combinations(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackCombine(n, k, 1, new ArrayList<>(), result);
        return result;
    }
    
    private static void backtrackCombine(int n, int k, int start, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i <= n; i++) {
            current.add(i);
            backtrackCombine(n, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Problem: Word Search in Grid
     * Check if word exists in 2D grid by moving up/down/left/right
     * 
     * Input: board = [["A","B","C"],["S","F","C"],["A","D","E"]],
     *        word = "ABCB"
     * Expected Output: false (can't reuse same cell)
     * 
     * Input: board = [["A","B","C"],["S","F","C"],["A","D","E"]],
     *        word = "ABF"
     * Expected Output: true (path A->B->F exists)
     * 
     * Time Complexity: O(n * m * 4^l) where l = word length
     * Space Complexity: O(l) - recursion depth
     */
    public static boolean wordSearch(char[][] board, String word) {
        if (board == null || board.length == 0 || word == null || word.length() == 0) {
            return false;
        }
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == word.charAt(0)) {
                    if (dfsWordSearch(board, word, 0, i, j, new boolean[board.length][board[0].length])) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean dfsWordSearch(char[][] board, String word, int index, int i, int j, boolean[][] visited) {
        if (index == word.length()) return true;
        
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || 
            visited[i][j] || board[i][j] != word.charAt(index)) {
            return false;
        }
        
        visited[i][j] = true;
        
        boolean found = dfsWordSearch(board, word, index + 1, i + 1, j, visited) ||
                       dfsWordSearch(board, word, index + 1, i - 1, j, visited) ||
                       dfsWordSearch(board, word, index + 1, i, j + 1, visited) ||
                       dfsWordSearch(board, word, index + 1, i, j - 1, visited);
        
        visited[i][j] = false;
        return found;
    }
    
    /**
     * Problem: N-Queens
     * Time Complexity: O(N!)
     * Space Complexity: O(N)
     */
    public static List<List<String>> nQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        char[][] board = new char[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = '.';
            }
        }
        
        backtrackQueens(board, 0, result);
        return result;
    }
    
    private static void backtrackQueens(char[][] board, int row, List<List<String>> result) {
        if (row == board.length) {
            result.add(boardToList(board));
            return;
        }
        
        for (int col = 0; col < board.length; col++) {
            if (isSafeQueens(board, row, col)) {
                board[row][col] = 'Q';
                backtrackQueens(board, row + 1, result);
                board[row][col] = '.';
            }
        }
    }
    
    private static boolean isSafeQueens(char[][] board, int row, int col) {
        // Check column
        for (int i = 0; i < row; i++) {
            if (board[i][col] == 'Q') return false;
        }
        
        // Check diagonal up-left
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (board[i][j] == 'Q') return false;
        }
        
        // Check diagonal up-right
        for (int i = row - 1, j = col + 1; i >= 0 && j < board.length; i--, j++) {
            if (board[i][j] == 'Q') return false;
        }
        
        return true;
    }
    
    private static List<String> boardToList(char[][] board) {
        List<String> list = new ArrayList<>();
        for (char[] row : board) {
            list.add(new String(row));
        }
        return list;
    }
    
    /**
     * Problem: Subsets
     * Time Complexity: O(2^n)
     * Space Complexity: O(n)
     */
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrackSubsets(nums, 0, new ArrayList<>(), result);
        return result;
    }
    
    private static void backtrackSubsets(int[] nums, int start, List<Integer> current, List<List<Integer>> result) {
        result.add(new ArrayList<>(current));
        
        for (int i = start; i < nums.length; i++) {
            current.add(nums[i]);
            backtrackSubsets(nums, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Problem: Generate Parentheses
     * Time Complexity: O(4^n / sqrt(n))
     * Space Complexity: O(n)
     */
    public static List<String> generateParentheses(int n) {
        List<String> result = new ArrayList<>();
        backtrackParens(n, n, "", result);
        return result;
    }
    
    private static void backtrackParens(int open, int close, String current, List<String> result) {
        if (open == 0 && close == 0) {
            result.add(current);
            return;
        }
        
        if (open > 0) {
            backtrackParens(open - 1, close, current + "(", result);
        }
        
        if (close > open) {
            backtrackParens(open, close - 1, current + ")", result);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Permutations of [1,2,3]: " + permutations(new int[]{1, 2, 3}));
        
        System.out.println("Combinations(4, 2): " + combinations(4, 2));
        
        char[][] board = {{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
        System.out.println("Word Search 'ABCCED': " + wordSearch(board, "ABCCED"));
        
        System.out.println("N-Queens (n=4): " + nQueens(4).size() + " solutions");
        
        System.out.println("Subsets of [1,2,3]: " + subsets(new int[]{1, 2, 3}));
        
        System.out.println("Generate Parentheses (n=3): " + generateParentheses(3));
    }
}
