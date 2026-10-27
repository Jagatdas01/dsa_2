/**
 * DSA Pattern: Matrix Traversal
 * Topics: DFS/BFS in matrix, connected components, matrix exploration
 */

import java.util.*;

public class MatrixTraversalPatterns {
    
    static class Cell {
        int row, col;
        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    
    /**
     * Problem: Number of Islands (Count connected components)
     * Count islands in a grid where '1' is land and '0' is water
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n) for visited array + recursion stack
     */
    public static int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int m = grid.length;
        int n = grid[0].length;
        boolean[][] visited = new boolean[m][n];
        int count = 0;
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == '1' && !visited[i][j]) {
                    dfs(grid, visited, i, j);
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private static void dfs(char[][] grid, boolean[][] visited, int row, int col) {
        int m = grid.length;
        int n = grid[0].length;
        
        if (row < 0 || row >= m || col < 0 || col >= n || visited[row][col] || grid[row][col] == '0') {
            return;
        }
        
        visited[row][col] = true;
        
        // Explore all 4 directions
        dfs(grid, visited, row + 1, col);
        dfs(grid, visited, row - 1, col);
        dfs(grid, visited, row, col + 1);
        dfs(grid, visited, row, col - 1);
    }
    
    /**
     * Problem: Rotting Oranges (BFS in matrix)
     * Find time to rot all oranges using multi-source BFS
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int rottingOranges(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        Queue<Cell> queue = new LinkedList<>();
        int freshOranges = 0;
        
        // Add all rotten oranges to queue
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 2) {
                    queue.add(new Cell(i, j));
                } else if (grid[i][j] == 1) {
                    freshOranges++;
                }
            }
        }
        
        if (freshOranges == 0) return 0;
        
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        int minutes = 0;
        
        while (!queue.isEmpty() && freshOranges > 0) {
            int size = queue.size();
            
            // Process all rotten oranges at current level
            for (int i = 0; i < size; i++) {
                Cell cell = queue.poll();
                
                // Check all 4 neighbors
                for (int[] dir : directions) {
                    int newRow = cell.row + dir[0];
                    int newCol = cell.col + dir[1];
                    
                    if (newRow >= 0 && newRow < m && newCol >= 0 && newCol < n && grid[newRow][newCol] == 1) {
                        grid[newRow][newCol] = 2;
                        freshOranges--;
                        queue.add(new Cell(newRow, newCol));
                    }
                }
            }
            
            minutes++;
        }
        
        return freshOranges == 0 ? minutes : -1;
    }
    
    /**
     * Problem: Surrounded Regions (Mark regions surrounded by 'X')
     * Mark all regions of 'O' surrounded by 'X'
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static void surroundedRegions(char[][] board) {
        if (board == null || board.length == 0) return;
        
        int m = board.length;
        int n = board[0].length;
        
        // Mark all 'O's connected to boundary
        for (int i = 0; i < m; i++) {
            if (board[i][0] == 'O') dfsMarkSafe(board, i, 0);
            if (board[i][n - 1] == 'O') dfsMarkSafe(board, i, n - 1);
        }
        
        for (int j = 0; j < n; j++) {
            if (board[0][j] == 'O') dfsMarkSafe(board, 0, j);
            if (board[m - 1][j] == 'O') dfsMarkSafe(board, m - 1, j);
        }
        
        // Mark all remaining 'O' as 'X' (surrounded), and restore marked 'O's
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                } else if (board[i][j] == '*') {
                    board[i][j] = 'O';
                }
            }
        }
    }
    
    private static void dfsMarkSafe(char[][] board, int row, int col) {
        int m = board.length;
        int n = board[0].length;
        
        if (row < 0 || row >= m || col < 0 || col >= n || board[row][col] != 'O') {
            return;
        }
        
        board[row][col] = '*';  // Mark as safe (connected to boundary)
        
        dfsMarkSafe(board, row + 1, col);
        dfsMarkSafe(board, row - 1, col);
        dfsMarkSafe(board, row, col + 1);
        dfsMarkSafe(board, row, col - 1);
    }
    
    /**
     * Problem: Diagonal Traverse
     * Traverse matrix diagonally alternating direction
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n) for result
     */
    public static int[] diagonalTraverse(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[m * n];
        int idx = 0;
        
        for (int d = 0; d < m + n - 1; d++) {
            if (d % 2 == 0) {
                // Going up (from bottom-left to top-right)
                int row = Math.min(d, m - 1);
                int col = d - row;
                
                while (row >= 0 && col < n) {
                    result[idx++] = mat[row][col];
                    row--;
                    col++;
                }
            } else {
                // Going down (from top-right to bottom-left)
                int col = Math.min(d, n - 1);
                int row = d - col;
                
                while (col >= 0 && row < m) {
                    result[idx++] = mat[row][col];
                    row++;
                    col--;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Problem: Spiral Matrix Traversal
     * Traverse matrix in spiral order (clockwise)
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n) for result
     */
    public static List<Integer> spiralTraverse(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0) return result;
        
        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;
        
        while (top <= bottom && left <= right) {
            // Traverse right
            for (int col = left; col <= right; col++) {
                result.add(matrix[top][col]);
            }
            top++;
            
            // Traverse down
            for (int row = top; row <= bottom; row++) {
                result.add(matrix[row][right]);
            }
            right--;
            
            // Traverse left
            if (top <= bottom) {
                for (int col = right; col >= left; col--) {
                    result.add(matrix[bottom][col]);
                }
                bottom--;
            }
            
            // Traverse up
            if (left <= right) {
                for (int row = bottom; row >= top; row--) {
                    result.add(matrix[row][left]);
                }
                left++;
            }
        }
        
        return result;
    }
    
    /**
     * Problem: Path Sum in Matrix (Find path with target sum)
     * Use DFS to find if path exists with sum equal to target
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static boolean hasPathSum(int[][] matrix, int targetSum) {
        if (matrix == null || matrix.length == 0) return false;
        
        int m = matrix.length;
        int n = matrix[0].length;
        boolean[][] visited = new boolean[m][n];
        
        return dfsPathSum(matrix, visited, 0, 0, matrix[0][0], targetSum);
    }
    
    private static boolean dfsPathSum(int[][] matrix, boolean[][] visited, 
                                     int row, int col, int currentSum, int targetSum) {
        int m = matrix.length;
        int n = matrix[0].length;
        
        if (row == m - 1 && col == n - 1) {
            return currentSum == targetSum;
        }
        
        visited[row][col] = true;
        
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < m && newCol >= 0 && newCol < n && !visited[newRow][newCol]) {
                if (dfsPathSum(matrix, visited, newRow, newCol, 
                              currentSum + matrix[newRow][newCol], targetSum)) {
                    return true;
                }
            }
        }
        
        visited[row][col] = false;  // Backtrack
        return false;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Matrix Traversal Pattern ===\n");
        
        // Number of Islands
        char[][] grid = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        System.out.println("Grid:");
        for (char[] row : grid) System.out.println(Arrays.toString(row));
        System.out.println("Number of islands: " + numIslands(grid));
        
        // Rotting Oranges
        int[][] oranges = {
            {2, 1, 1},
            {1, 1, 0},
            {0, 1, 1}
        };
        System.out.println("\nRotting oranges grid:");
        for (int[] row : oranges) System.out.println(Arrays.toString(row));
        System.out.println("Minutes to rot all: " + rottingOranges(oranges));
        
        // Diagonal Traverse
        int[][] mat = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        System.out.println("\nMatrix:");
        for (int[] row : mat) System.out.println(Arrays.toString(row));
        System.out.println("Diagonal traverse: " + Arrays.toString(diagonalTraverse(mat)));
        
        // Spiral Traverse
        System.out.println("Spiral traverse: " + spiralTraverse(mat));
    }
}
