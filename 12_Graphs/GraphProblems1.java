/**
 * DSA 450 Problems - Graph (Part 1: Problems 1-16)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class GraphProblems1 {
    
    /**
     * Problem 1: Breadth First Search (BFS)
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static void bfs(int vertices, List<List<Integer>> adj) {
        boolean[] visited = new boolean[vertices];
        Queue<Integer> queue = new LinkedList<>();
        
        queue.add(0);
        visited[0] = true;
        
        while (!queue.isEmpty()) {
            int node = queue.poll();
            System.out.print(node + " ");
            
            for (int neighbor : adj.get(node)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }
    }
    
    /**
     * Problem 2: Depth First Search (DFS)
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static void dfs(int vertices, List<List<Integer>> adj) {
        boolean[] visited = new boolean[vertices];
        dfsHelper(0, adj, visited);
    }
    
    private static void dfsHelper(int node, List<List<Integer>> adj, boolean[] visited) {
        visited[node] = true;
        System.out.print(node + " ");
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfsHelper(neighbor, adj, visited);
            }
        }
    }
    
    /**
     * Problem 3: Detect Cycle in Undirected Graph
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static boolean hasCycleUndirected(int vertices, List<List<Integer>> adj) {
        boolean[] visited = new boolean[vertices];
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                if (hasCycleHelper(i, -1, visited, adj)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static boolean hasCycleHelper(int node, int parent, boolean[] visited, List<List<Integer>> adj) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                if (hasCycleHelper(neighbor, node, visited, adj)) {
                    return true;
                }
            } else if (neighbor != parent) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Problem 4: Detect Cycle in Directed Graph
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static boolean hasCycleDirected(int vertices, List<List<Integer>> adj) {
        int[] state = new int[vertices]; // 0 = unvisited, 1 = visiting, 2 = visited
        
        for (int i = 0; i < vertices; i++) {
            if (state[i] == 0) {
                if (hasCycleDirHelper(i, state, adj)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static boolean hasCycleDirHelper(int node, int[] state, List<List<Integer>> adj) {
        state[node] = 1;
        
        for (int neighbor : adj.get(node)) {
            if (state[neighbor] == 1) return true;
            if (state[neighbor] == 0 && hasCycleDirHelper(neighbor, state, adj)) {
                return true;
            }
        }
        
        state[node] = 2;
        return false;
    }
    
    /**
     * Problem 5: Topological Sort (DFS)
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static List<Integer> topologicalSort(int vertices, List<List<Integer>> adj) {
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[vertices];
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                topoDFSHelper(i, visited, adj, stack);
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        
        return result;
    }
    
    private static void topoDFSHelper(int node, boolean[] visited, List<List<Integer>> adj, Stack<Integer> stack) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                topoDFSHelper(neighbor, visited, adj, stack);
            }
        }
        
        stack.push(node);
    }
    
    /**
     * Problem 6: Topological Sort (Kahn's Algorithm - BFS)
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static List<Integer> topologicalSortKahn(int vertices, List<List<Integer>> adj) {
        int[] inDegree = new int[vertices];
        
        for (int i = 0; i < vertices; i++) {
            for (int neighbor : adj.get(i)) {
                inDegree[neighbor]++;
            }
        }
        
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < vertices; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            int node = queue.poll();
            result.add(node);
            
            for (int neighbor : adj.get(node)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.add(neighbor);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Problem 7: Connected Components
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static int connectedComponents(int vertices, List<List<Integer>> adj) {
        boolean[] visited = new boolean[vertices];
        int count = 0;
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                dfsCount(i, visited, adj);
                count++;
            }
        }
        
        return count;
    }
    
    private static void dfsCount(int node, boolean[] visited, List<List<Integer>> adj) {
        visited[node] = true;
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfsCount(neighbor, visited, adj);
            }
        }
    }
    
    /**
     * Problem 8: Bipartite Graph Check
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static boolean isBipartite(int vertices, List<List<Integer>> adj) {
        int[] color = new int[vertices];
        Arrays.fill(color, -1);
        
        for (int i = 0; i < vertices; i++) {
            if (color[i] == -1) {
                if (!isBipartiteHelper(i, 0, color, adj)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isBipartiteHelper(int node, int col, int[] color, List<List<Integer>> adj) {
        color[node] = col;
        
        for (int neighbor : adj.get(node)) {
            if (color[neighbor] == -1) {
                if (!isBipartiteHelper(neighbor, 1 - col, color, adj)) {
                    return false;
                }
            } else if (color[neighbor] == col) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem 9: Dijkstra's Shortest Path
     * Time Complexity: O((V + E) log V)
     * Space Complexity: O(V)
     */
    public static int[] dijkstra(int vertices, List<List<int[]>> adj, int src) {
        int[] dist = new int[vertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.offer(new int[]{0, src});
        
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int d = curr[0], node = curr[1];
            
            if (d > dist[node]) continue;
            
            for (int[] edge : adj.get(node)) {
                int neighbor = edge[0], weight = edge[1];
                
                if (dist[node] + weight < dist[neighbor]) {
                    dist[neighbor] = dist[node] + weight;
                    pq.offer(new int[]{dist[neighbor], neighbor});
                }
            }
        }
        
        return dist;
    }
    
    /**
     * Problem 10: Bellman-Ford Algorithm
     * Time Complexity: O(V * E)
     * Space Complexity: O(V)
     */
    public static int[] bellmanFord(int vertices, List<int[]> edges, int src) {
        int[] dist = new int[vertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        
        for (int i = 0; i < vertices - 1; i++) {
            for (int[] edge : edges) {
                int u = edge[0], v = edge[1], w = edge[2];
                
                if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                }
            }
        }
        
        return dist;
    }
    
    /**
     * Problem 11: Floyd-Warshall Algorithm (All Pairs Shortest Path)
     * Time Complexity: O(V^3)
     * Space Complexity: O(V^2)
     */
    public static int[][] floydWarshall(int[][] graph) {
        int n = graph.length;
        int[][] dist = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = graph[i][j];
            }
        }
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE && dist[k][j] != Integer.MAX_VALUE) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }
        
        return dist;
    }
    
    /**
     * Problem 12: Strongly Connected Components (Kosaraju's Algorithm)
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static int stronglyConnectedComponents(int vertices, List<List<Integer>> adj) {
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[vertices];
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                kosorajuFill(i, visited, adj, stack);
            }
        }
        
        List<List<Integer>> transpose = getTranspose(vertices, adj);
        Arrays.fill(visited, false);
        int count = 0;
        
        while (!stack.isEmpty()) {
            int node = stack.pop();
            if (!visited[node]) {
                dfsCount(node, visited, transpose);
                count++;
            }
        }
        
        return count;
    }
    
    private static void kosorajuFill(int node, boolean[] visited, List<List<Integer>> adj, Stack<Integer> stack) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                kosorajuFill(neighbor, visited, adj, stack);
            }
        }
        
        stack.push(node);
    }
    
    private static List<List<Integer>> getTranspose(int vertices, List<List<Integer>> adj) {
        List<List<Integer>> transpose = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            transpose.add(new ArrayList<>());
        }
        
        for (int i = 0; i < vertices; i++) {
            for (int j : adj.get(i)) {
                transpose.get(j).add(i);
            }
        }
        
        return transpose;
    }
    
    /**
     * Problem 13: Minimum Spanning Tree (Kruskal's Algorithm)
     * Time Complexity: O(E log E)
     * Space Complexity: O(V)
     */
    public static int kruskalMST(int vertices, List<int[]> edges) {
        edges.sort((a, b) -> a[2] - b[2]);
        
        int[] parent = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            parent[i] = i;
        }
        
        int mstWeight = 0;
        int edgeCount = 0;
        
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], w = edge[2];
            
            int rootU = find(u, parent);
            int rootV = find(v, parent);
            
            if (rootU != rootV) {
                mstWeight += w;
                union(rootU, rootV, parent);
                edgeCount++;
                
                if (edgeCount == vertices - 1) break;
            }
        }
        
        return mstWeight;
    }
    
    private static int find(int x, int[] parent) {
        if (parent[x] != x) {
            parent[x] = find(parent[x], parent);
        }
        return parent[x];
    }
    
    private static void union(int x, int y, int[] parent) {
        parent[x] = y;
    }
    
    /**
     * Problem 14: Minimum Spanning Tree (Prim's Algorithm)
     * Time Complexity: O(V^2)
     * Space Complexity: O(V)
     */
    public static int primsMST(int vertices, int[][] graph) {
        boolean[] inMST = new boolean[vertices];
        int[] key = new int[vertices];
        Arrays.fill(key, Integer.MAX_VALUE);
        
        key[0] = 0;
        int mstWeight = 0;
        
        for (int i = 0; i < vertices; i++) {
            int u = -1;
            for (int j = 0; j < vertices; j++) {
                if (!inMST[j] && (u == -1 || key[j] < key[u])) {
                    u = j;
                }
            }
            
            inMST[u] = true;
            mstWeight += key[u];
            
            for (int v = 0; v < vertices; v++) {
                if (graph[u][v] > 0 && !inMST[v] && graph[u][v] < key[v]) {
                    key[v] = graph[u][v];
                }
            }
        }
        
        return mstWeight;
    }
    
    /**
     * Problem 15: Shortest Path in DAG
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static int[] shortestPathDAG(int vertices, List<List<int[]>> adj, int src) {
        List<Integer> topoOrder = topologicalSortDAG(vertices, adj);
        int[] dist = new int[vertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        
        for (int node : topoOrder) {
            if (dist[node] != Integer.MAX_VALUE) {
                for (int[] edge : adj.get(node)) {
                    int neighbor = edge[0], weight = edge[1];
                    dist[neighbor] = Math.min(dist[neighbor], dist[node] + weight);
                }
            }
        }
        
        return dist;
    }
    
    private static List<Integer> topologicalSortDAG(int vertices, List<List<int[]>> adj) {
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[vertices];
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                topoDFSDAG(i, visited, adj, stack);
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        
        return result;
    }
    
    private static void topoDFSDAG(int node, boolean[] visited, List<List<int[]>> adj, Stack<Integer> stack) {
        visited[node] = true;
        
        for (int[] edge : adj.get(node)) {
            int neighbor = edge[0];
            if (!visited[neighbor]) {
                topoDFSDAG(neighbor, visited, adj, stack);
            }
        }
        
        stack.push(node);
    }
    
    /**
     * Problem 16: Bridges in Graph
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public static List<int[]> findBridges(int vertices, List<List<Integer>> adj) {
        boolean[] visited = new boolean[vertices];
        int[] disc = new int[vertices];
        int[] low = new int[vertices];
        List<int[]> bridges = new ArrayList<>();
        int time[] = {0};
        
        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                bridgesDFS(i, -1, visited, disc, low, bridges, adj, time);
            }
        }
        
        return bridges;
    }
    
    private static void bridgesDFS(int node, int parent, boolean[] visited, int[] disc, int[] low,
                                  List<int[]> bridges, List<List<Integer>> adj, int[] time) {
        visited[node] = true;
        disc[node] = low[node] = time[0]++;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                bridgesDFS(neighbor, node, visited, disc, low, bridges, adj, time);
                
                low[node] = Math.min(low[node], low[neighbor]);
                
                if (low[neighbor] > disc[node]) {
                    bridges.add(new int[]{node, neighbor});
                }
            } else if (neighbor != parent) {
                low[node] = Math.min(low[node], disc[neighbor]);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Graph Problems (Part 1) ===\n");
        System.out.println("Graph algorithms implemented: BFS, DFS, Cycle Detection, Topological Sort, etc.");
    }
}
