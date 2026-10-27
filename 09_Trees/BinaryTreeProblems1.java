/**
 * DSA 450 Problems - Binary Trees (Part 1: Problems 1-20)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class BinaryTreeProblems1 {
    
    static class TreeNode {
        int data;
        TreeNode left, right;
        TreeNode(int data) { this.data = data; }
    }
    
    /**
     * Problem 1: Level Order Traversal (BFS)
     * Time Complexity: O(n)
     * Space Complexity: O(w) where w = width
     */
    public static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            List<Integer> level = new ArrayList<>();
            int size = queue.size();
            
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.data);
                
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
            
            result.add(level);
        }
        
        return result;
    }
    
    /**
     * Problem 2: Reverse Level Order Traversal
     * Time Complexity: O(n)
     * Space Complexity: O(w)
     */
    public static List<List<Integer>> reverseLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        Stack<List<Integer>> stack = new Stack<>();
        
        while (!queue.isEmpty()) {
            List<Integer> level = new ArrayList<>();
            int size = queue.size();
            
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.data);
                
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
            
            stack.push(level);
        }
        
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        
        return result;
    }
    
    /**
     * Problem 3: Height of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int height(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(height(root.left), height(root.right));
    }
    
    /**
     * Problem 4: Diameter of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int diameter(TreeNode root) {
        return diameterHelper(root)[1];
    }
    
    private static int[] diameterHelper(TreeNode root) {
        if (root == null) return new int[]{0, 0};
        
        int[] left = diameterHelper(root.left);
        int[] right = diameterHelper(root.right);
        
        int height = 1 + Math.max(left[0], right[0]);
        int diam = Math.max(left[1], Math.max(right[1], left[0] + right[0] + 1));
        
        return new int[]{height, diam};
    }
    
    /**
     * Problem 5: Check if tree is balanced
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean isBalanced(TreeNode root) {
        return checkBalance(root) != -1;
    }
    
    private static int checkBalance(TreeNode root) {
        if (root == null) return 0;
        
        int leftHeight = checkBalance(root.left);
        if (leftHeight == -1) return -1;
        
        int rightHeight = checkBalance(root.right);
        if (rightHeight == -1) return -1;
        
        if (Math.abs(leftHeight - rightHeight) > 1) return -1;
        
        return 1 + Math.max(leftHeight, rightHeight);
    }
    
    /**
     * Problem 6: Inorder Traversal (Recursive)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> inorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        inorderHelper(root, result);
        return result;
    }
    
    private static void inorderHelper(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        inorderHelper(root.left, result);
        result.add(root.data);
        inorderHelper(root.right, result);
    }
    
    /**
     * Problem 7: Inorder Traversal (Iterative)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> inorderIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode current = root;
        
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            
            current = stack.pop();
            result.add(current.data);
            current = current.right;
        }
        
        return result;
    }
    
    /**
     * Problem 8: Preorder Traversal (Recursive)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> preorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }
    
    private static void preorderHelper(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        result.add(root.data);
        preorderHelper(root.left, result);
        preorderHelper(root.right, result);
    }
    
    /**
     * Problem 9: Preorder Traversal (Iterative)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> preorderIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        
        while (!stack.isEmpty()) {
            TreeNode current = stack.pop();
            result.add(current.data);
            
            if (current.right != null) stack.push(current.right);
            if (current.left != null) stack.push(current.left);
        }
        
        return result;
    }
    
    /**
     * Problem 10: Postorder Traversal (Recursive)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> postorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        postorderHelper(root, result);
        return result;
    }
    
    private static void postorderHelper(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        postorderHelper(root.left, result);
        postorderHelper(root.right, result);
        result.add(root.data);
    }
    
    /**
     * Problem 11: Left View of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> leftView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, Integer> levelNodes = new HashMap<>();
        leftViewHelper(root, 0, result, levelNodes);
        return result;
    }
    
    private static void leftViewHelper(TreeNode root, int level, List<Integer> result,
                                      Map<Integer, Integer> levelNodes) {
        if (root == null) return;
        
        if (!levelNodes.containsKey(level)) {
            result.add(root.data);
            levelNodes.put(level, root.data);
        }
        
        leftViewHelper(root.left, level + 1, result, levelNodes);
        leftViewHelper(root.right, level + 1, result, levelNodes);
    }
    
    /**
     * Problem 12: Right View of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static List<Integer> rightView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, Integer> levelNodes = new HashMap<>();
        rightViewHelper(root, 0, result, levelNodes);
        return result;
    }
    
    private static void rightViewHelper(TreeNode root, int level, List<Integer> result,
                                       Map<Integer, Integer> levelNodes) {
        if (root == null) return;
        
        if (!levelNodes.containsKey(level)) {
            result.add(root.data);
            levelNodes.put(level, root.data);
        }
        
        rightViewHelper(root.right, level + 1, result, levelNodes);
        rightViewHelper(root.left, level + 1, result, levelNodes);
    }
    
    /**
     * Problem 13: Top View of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<Integer> topView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        
        Map<Integer, Integer> columnMap = new TreeMap<>();
        Queue<TreeNode> queue = new LinkedList<>();
        Queue<Integer> columns = new LinkedList<>();
        
        queue.add(root);
        columns.add(0);
        columnMap.put(0, root.data);
        
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            int column = columns.poll();
            
            if (node.left != null) {
                queue.add(node.left);
                columns.add(column - 1);
                if (!columnMap.containsKey(column - 1)) {
                    columnMap.put(column - 1, node.left.data);
                }
            }
            
            if (node.right != null) {
                queue.add(node.right);
                columns.add(column + 1);
                if (!columnMap.containsKey(column + 1)) {
                    columnMap.put(column + 1, node.right.data);
                }
            }
        }
        
        for (int value : columnMap.values()) {
            result.add(value);
        }
        
        return result;
    }
    
    /**
     * Problem 14: Bottom View of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<Integer> bottomView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        
        Map<Integer, Integer> columnMap = new TreeMap<>();
        Queue<TreeNode> queue = new LinkedList<>();
        Queue<Integer> columns = new LinkedList<>();
        
        queue.add(root);
        columns.add(0);
        
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            int column = columns.poll();
            
            columnMap.put(column, node.data);
            
            if (node.left != null) {
                queue.add(node.left);
                columns.add(column - 1);
            }
            
            if (node.right != null) {
                queue.add(node.right);
                columns.add(column + 1);
            }
        }
        
        for (int value : columnMap.values()) {
            result.add(value);
        }
        
        return result;
    }
    
    /**
     * Problem 15: Zig-Zag Traversal
     * Time Complexity: O(n)
     * Space Complexity: O(w)
     */
    public static List<List<Integer>> zigzagTraversal(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        boolean leftToRight = true;
        
        while (!queue.isEmpty()) {
            List<Integer> level = new ArrayList<>();
            int size = queue.size();
            
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.data);
                
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
            
            if (!leftToRight) {
                Collections.reverse(level);
            }
            
            result.add(level);
            leftToRight = !leftToRight;
        }
        
        return result;
    }
    
    /**
     * Problem 16: Check if tree is mirror
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean isMirror(TreeNode root1, TreeNode root2) {
        if (root1 == null && root2 == null) return true;
        if (root1 == null || root2 == null) return false;
        
        return root1.data == root2.data &&
               isMirror(root1.left, root2.right) &&
               isMirror(root1.right, root2.left);
    }
    
    /**
     * Problem 17: Path Sum
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;
        
        if (root.left == null && root.right == null) {
            return root.data == targetSum;
        }
        
        int remaining = targetSum - root.data;
        return hasPathSum(root.left, remaining) || hasPathSum(root.right, remaining);
    }
    
    /**
     * Problem 18: Sum of all leaf nodes
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int sumOfLeafNodes(TreeNode root) {
        if (root == null) return 0;
        
        if (root.left == null && root.right == null) {
            return root.data;
        }
        
        return sumOfLeafNodes(root.left) + sumOfLeafNodes(root.right);
    }
    
    /**
     * Problem 19: Maximum path sum in tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int maxPathSum(TreeNode root) {
        int[] maxSum = {Integer.MIN_VALUE};
        maxPathSumHelper(root, maxSum);
        return maxSum[0];
    }
    
    private static int maxPathSumHelper(TreeNode root, int[] maxSum) {
        if (root == null) return 0;
        
        int leftSum = Math.max(0, maxPathSumHelper(root.left, maxSum));
        int rightSum = Math.max(0, maxPathSumHelper(root.right, maxSum));
        
        int currentSum = root.data + leftSum + rightSum;
        maxSum[0] = Math.max(maxSum[0], currentSum);
        
        return root.data + Math.max(leftSum, rightSum);
    }
    
    /**
     * Problem 20: Lowest Common Ancestor (LCA)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static TreeNode findLCA(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null) return null;
        
        if (root == p || root == q) return root;
        
        TreeNode left = findLCA(root.left, p, q);
        TreeNode right = findLCA(root.right, p, q);
        
        if (left != null && right != null) return root;
        
        return (left != null) ? left : right;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Binary Tree Problems (Part 1) ===\n");
        
        // Create sample tree:        1
        //                           / \
        //                          2   3
        //                         / \
        //                        4   5
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);
        
        System.out.println("1. Level Order: " + levelOrder(root));
        System.out.println("3. Height: " + height(root));
        System.out.println("5. Is Balanced: " + isBalanced(root));
        System.out.println("6. Inorder: " + inorderRecursive(root));
        System.out.println("8. Preorder: " + preorderRecursive(root));
        System.out.println("10. Postorder: " + postorderRecursive(root));
    }
}
