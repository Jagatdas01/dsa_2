/**
 * DSA Pattern: Graph Algorithms
 * Topics: BFS, DFS, Shortest Path, Connectivity
 */

import java.util.*;

public class GraphPatterns {
    
    /**
     * Problem: Breadth First Search (BFS)
     * Traverse graph level by level from starting node
     * 
     * Input: graph adjacency list, start = 0
     * graph = [[1,2], [0,3], [0], [1,4], [3]]
     * Expected Output: Traversal order: 0 1 2 3 4 (level by level)
     * 
     * Use case: Finding shortest path, level-order traversal
     * 
     * Time Complexity: O(V + E)
     * Space Complexity: O(V) - queue and visited array
     */
    public static void bfs(int[][] graph, int start) {
        boolean[] visited = new boolean[graph.length];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visited[start] = true;
        
        while (!queue.isEmpty()) {
            int node = queue.poll();
            System.out.print(node + " ");
            
            for (int neighbor : graph[node]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }
    }
    
    /**
     * Problem: Depth First Search (DFS)
     * Traverse graph deeply through each branch before backtracking
     * 
     * Input: graph adjacency list, start = 0
     * graph = [[1,2], [0,3], [0], [1,4], [3]]
     * Expected Output: Traversal order: 0 1 3 4 2 (depth-first)
     * 
     * Use case: Cycle detection, topological sort, connectivity
     * 
     * Time Complexity: O(V + E)
     * Space Complexity: O(V) - recursion stack
     */
    public static void dfs(int[][] graph, int start) {
        boolean[] visited = new boolean[graph.length];
        dfsHelper(graph, start, visited);
    }
    
    private static void dfsHelper(int[][] graph, int node, boolean[] visited) {
        visited[node] = true;
        System.out.print(node + " ");
        
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                dfsHelper(graph, neighbor, visited);
            }
        }
    }
    
    /**
     * Problem: Detect Cycle in Undirected Graph
     * Check if graph contains any cycle using DFS
     * 
     * Input: graph = [[1,2], [0,2], [0,1]]
     * Expected Output: true (forms triangle - cycle exists)
     * 
     * Input: graph = [[1], [0]]
     * Expected Output: false (single edge, no cycle)
     * 
     * Time Complexity: O(V + E)
     * Space Complexity: O(V) - visited array
     */
    public static boolean hasCycle(int[][] graph) {
        boolean[] visited = new boolean[graph.length];
        
        for (int i = 0; i < graph.length; i++) {
            if (!visited[i]) {
                if (hasCycleDFS(graph, i, -1, visited)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static boolean hasCycleDFS(int[][] graph, int node, int parent, boolean[] visited) {
        visited[node] = true;
        
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                if (hasCycleDFS(graph, neighbor, node, visited)) {
                    return true;
                }
            } else if (neighbor != parent) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Problem: Dijkstra's Algorithm (Shortest Path)
     * Time Complexity: O((V + E) log V)
     * Space Complexity: O(V)
     */
    static class Edge {
        int to, weight;
        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }
    
    public static int[] dijkstra(List<Edge>[] graph, int start) {
        int[] distance = new int[graph.length];
        Arrays.fill(distance, Integer.MAX_VALUE);
        distance[start] = 0;
        
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        pq.offer(new int[]{start, 0});
        
        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int node = top[0];
            int dist = top[1];
            
            if (dist > distance[node]) continue;
            
            for (Edge edge : graph[node]) {
                int newDist = distance[node] + edge.weight;
                if (newDist < distance[edge.to]) {
                    distance[edge.to] = newDist;
                    pq.offer(new int[]{edge.to, newDist});
                }
            }
        }
        
        return distance;
    }
    
    /**
     * Problem: Number of Islands (Connected Components)
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        
        int count = 0;
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == '1') {
                    dfsIsland(grid, i, j);
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private static void dfsIsland(char[][] grid, int i, int j) {
        if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] == '0') {
            return;
        }
        
        grid[i][j] = '0';
        
        dfsIsland(grid, i + 1, j);
        dfsIsland(grid, i - 1, j);
        dfsIsland(grid, i, j + 1);
        dfsIsland(grid, i, j - 1);
    }
    
    /**
     * Problem: Topological Sort
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static void topologicalSort(int[][] graph) {
        boolean[] visited = new boolean[graph.length];
        Stack<Integer> stack = new Stack<>();
        
        for (int i = 0; i < graph.length; i++) {
            if (!visited[i]) {
                topologicalSortDFS(graph, i, visited, stack);
            }
        }
        
        System.out.print("Topological Order: ");
        while (!stack.isEmpty()) {
            System.out.print(stack.pop() + " ");
        }
        System.out.println();
    }
    
    private static void topologicalSortDFS(int[][] graph, int node, boolean[] visited, Stack<Integer> stack) {
        visited[node] = true;
        
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                topologicalSortDFS(graph, neighbor, visited, stack);
            }
        }
        
        stack.push(node);
    }
    
    public static void main(String[] args) {
        // Create adjacency list for graph
        int[][] graph = {
            {1, 2},
            {0, 3},
            {0},
            {1}
        };
        
        System.out.print("BFS: ");
        bfs(graph, 0);
        System.out.println();
        
        System.out.print("DFS: ");
        dfs(graph, 0);
        System.out.println();
        
        System.out.println("Has Cycle: " + hasCycle(graph));
        
        char[][] grid = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        System.out.println("Number of Islands: " + numIslands(grid));
    }
}
