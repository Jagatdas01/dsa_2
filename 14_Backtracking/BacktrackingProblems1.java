/**
 * DSA 450 Problems - Backtracking (Part 1: Problems 1-20)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class BacktrackingProblems1 {
    
    /**
     * Problem 1: All Permutations of String
     * Time Complexity: O(n * n!)
     * Space Complexity: O(n)
     */
    public static List<String> permutations(String s) {
        List<String> result = new ArrayList<>();
        permHelper(s.toCharArray(), 0, result);
        return result;
    }
    
    private static void permHelper(char[] arr, int index, List<String> result) {
        if (index == arr.length) {
            result.add(new String(arr));
            return;
        }
        
        for (int i = index; i < arr.length; i++) {
            swap(arr, index, i);
            permHelper(arr, index + 1, result);
            swap(arr, index, i);
        }
    }
    
    private static void swap(char[] arr, int i, int j) {
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    /**
     * Problem 2: All Combinations of Size k
     * Time Complexity: O(C(n,k))
     * Space Complexity: O(k)
     */
    public static List<List<Integer>> combinations(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        combHelper(n, k, 1, new ArrayList<>(), result);
        return result;
    }
    
    private static void combHelper(int n, int k, int start, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i <= n; i++) {
            current.add(i);
            combHelper(n, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Problem 3: N-Queens Problem
     * Time Complexity: O(n!)
     * Space Complexity: O(n)
     */
    public static List<List<String>> nQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        char[][] board = new char[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = '.';
            }
        }
        
        solveNQueens(board, 0, result);
        return result;
    }
    
    private static void solveNQueens(char[][] board, int row, List<List<String>> result) {
        if (row == board.length) {
            List<String> solution = new ArrayList<>();
            for (char[] r : board) {
                solution.add(new String(r));
            }
            result.add(solution);
            return;
        }
        
        for (int col = 0; col < board.length; col++) {
            if (isSafe(board, row, col)) {
                board[row][col] = 'Q';
                solveNQueens(board, row + 1, result);
                board[row][col] = '.';
            }
        }
    }
    
    private static boolean isSafe(char[][] board, int row, int col) {
        // Check column
        for (int i = 0; i < row; i++) {
            if (board[i][col] == 'Q') return false;
        }
        
        // Check diagonal
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (board[i][j] == 'Q') return false;
        }
        
        for (int i = row - 1, j = col + 1; i >= 0 && j < board.length; i--, j++) {
            if (board[i][j] == 'Q') return false;
        }
        
        return true;
    }
    
    /**
     * Problem 4: Word Search
     * Time Complexity: O(n * m * 4^l)
     * Space Complexity: O(l)
     */
    public static boolean wordSearch(char[][] board, String word) {
        if (board == null || word == null) return false;
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == word.charAt(0)) {
                    if (searchHelper(board, word, 0, i, j)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean searchHelper(char[][] board, String word, int index, int row, int col) {
        if (index == word.length()) return true;
        
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return false;
        }
        
        if (board[row][col] != word.charAt(index)) return false;
        
        char temp = board[row][col];
        board[row][col] = '*';
        
        boolean found = searchHelper(board, word, index + 1, row + 1, col) ||
                       searchHelper(board, word, index + 1, row - 1, col) ||
                       searchHelper(board, word, index + 1, row, col + 1) ||
                       searchHelper(board, word, index + 1, row, col - 1);
        
        board[row][col] = temp;
        return found;
    }
    
    /**
     * Problem 5: Sudoku Solver
     * Time Complexity: O(9^(n*m))
     * Space Complexity: O(n*m)
     */
    public static void solveSudoku(char[][] board) {
        sudokuHelper(board);
    }
    
    private static boolean sudokuHelper(char[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == '.') {
                    for (char c = '1'; c <= '9'; c++) {
                        if (isValidSudoku(board, i, j, c)) {
                            board[i][j] = c;
                            if (sudokuHelper(board)) return true;
                            board[i][j] = '.';
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean isValidSudoku(char[][] board, int row, int col, char c) {
        // Check row
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == c) return false;
        }
        
        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == c) return false;
        }
        
        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if (board[i][j] == c) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem 6: Rat in Maze
     * Time Complexity: O(4^(n*m))
     * Space Complexity: O(n*m)
     */
    public static List<String> ratMaze(int[][] maze) {
        List<String> result = new ArrayList<>();
        int n = maze.length;
        boolean[][] visited = new boolean[n][n];
        
        if (maze[0][0] == 1 && maze[n-1][n-1] == 1) {
            ratMazeHelper(maze, 0, 0, "", visited, result);
        }
        
        return result;
    }
    
    private static void ratMazeHelper(int[][] maze, int row, int col, String path, 
                                     boolean[][] visited, List<String> result) {
        if (row == maze.length - 1 && col == maze.length - 1) {
            result.add(path);
            return;
        }
        
        // Right
        if (col + 1 < maze.length && maze[row][col + 1] == 1 && !visited[row][col + 1]) {
            visited[row][col + 1] = true;
            ratMazeHelper(maze, row, col + 1, path + "R", visited, result);
            visited[row][col + 1] = false;
        }
        
        // Down
        if (row + 1 < maze.length && maze[row + 1][col] == 1 && !visited[row + 1][col]) {
            visited[row + 1][col] = true;
            ratMazeHelper(maze, row + 1, col, path + "D", visited, result);
            visited[row + 1][col] = false;
        }
        
        // Left
        if (col - 1 >= 0 && maze[row][col - 1] == 1 && !visited[row][col - 1]) {
            visited[row][col - 1] = true;
            ratMazeHelper(maze, row, col - 1, path + "L", visited, result);
            visited[row][col - 1] = false;
        }
        
        // Up
        if (row - 1 >= 0 && maze[row - 1][col] == 1 && !visited[row - 1][col]) {
            visited[row - 1][col] = true;
            ratMazeHelper(maze, row - 1, col, path + "U", visited, result);
            visited[row - 1][col] = false;
        }
    }
    
    /**
     * Problem 7: Subset Sum Problem
     * Time Complexity: O(2^n)
     * Space Complexity: O(n)
     */
    public static boolean subsetSum(int[] arr, int sum) {
        return subsetSumHelper(arr, 0, sum);
    }
    
    private static boolean subsetSumHelper(int[] arr, int index, int sum) {
        if (sum == 0) return true;
        if (index == arr.length || sum < 0) return false;
        
        return subsetSumHelper(arr, index + 1, sum - arr[index]) ||
               subsetSumHelper(arr, index + 1, sum);
    }
    
    /**
     * Problem 8: Generate Parentheses
     * Time Complexity: O(Catalan(n))
     * Space Complexity: O(n)
     */
    public static List<String> generateParentheses(int n) {
        List<String> result = new ArrayList<>();
        generateHelper(n, 0, 0, "", result);
        return result;
    }
    
    private static void generateHelper(int n, int open, int close, String current, List<String> result) {
        if (open == n && close == n) {
            result.add(current);
            return;
        }
        
        if (open < n) {
            generateHelper(n, open + 1, close, current + "(", result);
        }
        
        if (close < open) {
            generateHelper(n, open, close + 1, current + ")", result);
        }
    }
    
    /**
     * Problem 9: Restore IP Addresses
     * Time Complexity: O(3^4)
     * Space Complexity: O(4)
     */
    public static List<String> restoreIPAddresses(String s) {
        List<String> result = new ArrayList<>();
        restoreHelper(s, 0, 0, "", result);
        return result;
    }
    
    private static void restoreHelper(String s, int index, int dots, String current, List<String> result) {
        if (dots == 4 && index == s.length()) {
            result.add(current.substring(0, current.length() - 1));
            return;
        }
        
        if (dots == 4 || index == s.length()) return;
        
        for (int i = 1; i <= 3 && index + i <= s.length(); i++) {
            String part = s.substring(index, index + i);
            if (part.charAt(0) == '0' && i > 1) continue;
            if (Integer.parseInt(part) > 255) break;
            
            restoreHelper(s, index + i, dots + 1, current + part + ".", result);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Backtracking (Part 1) ===\n");
        System.out.println("1. Permutations: " + permutations("ABC"));
        System.out.println("8. Generate Parentheses (n=3): " + generateParentheses(3).size() + " combinations");
    }
}
