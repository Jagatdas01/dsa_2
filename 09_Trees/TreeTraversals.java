/**
 * DSA Pattern: Tree Traversals
 * Topics: Inorder, Preorder, Postorder, Level Order
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TreeTraversals {
    
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        
        TreeNode(int val) {
            this.val = val;
        }
    }
    
    /**
     * Problem: Inorder Traversal (Left, Root, Right)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static void inorderTraversal(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        inorderTraversal(root.left, result);
        result.add(root.val);
        inorderTraversal(root.right, result);
    }
    
    /**
     * Problem: Preorder Traversal (Root, Left, Right)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static void preorderTraversal(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        result.add(root.val);
        preorderTraversal(root.left, result);
        preorderTraversal(root.right, result);
    }
    
    /**
     * Problem: Postorder Traversal (Left, Right, Root)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static void postorderTraversal(TreeNode root, List<Integer> result) {
        if (root == null) return;
        
        postorderTraversal(root.left, result);
        postorderTraversal(root.right, result);
        result.add(root.val);
    }
    
    /**
     * Problem: Level Order Traversal (BFS)
     * Time Complexity: O(n)
     * Space Complexity: O(w) where w is max width
     */
    public static List<List<Integer>> levelOrderTraversal(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        
        if (root == null) return result;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();
            
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);
                
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
            
            result.add(currentLevel);
        }
        
        return result;
    }
    
    /**
     * Problem: Height of Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int getHeight(TreeNode root) {
        if (root == null) return -1;
        
        return 1 + Math.max(getHeight(root.left), getHeight(root.right));
    }
    
    /**
     * Problem: Check if tree is balanced
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean isBalanced(TreeNode root) {
        if (root == null) return true;
        
        int leftHeight = getHeight(root.left);
        int rightHeight = getHeight(root.right);
        
        if (Math.abs(leftHeight - rightHeight) > 1) {
            return false;
        }
        
        return isBalanced(root.left) && isBalanced(root.right);
    }
    
    /**
     * Problem: Path Sum
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;
        
        if (root.left == null && root.right == null) {
            return root.val == targetSum;
        }
        
        return hasPathSum(root.left, targetSum - root.val) ||
               hasPathSum(root.right, targetSum - root.val);
    }
    
    /**
     * Problem: Sum of path numbers
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int sumNumbers(TreeNode root) {
        return sumNumbersHelper(root, 0);
    }
    
    private static int sumNumbersHelper(TreeNode root, int currentSum) {
        if (root == null) return 0;
        
        currentSum = currentSum * 10 + root.val;
        
        if (root.left == null && root.right == null) {
            return currentSum;
        }
        
        return sumNumbersHelper(root.left, currentSum) + 
               sumNumbersHelper(root.right, currentSum);
    }
    
    /**
     * Problem: Diameter of Binary Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class DiameterResult {
        int diameter;
        int height;
    }
    
    public static int diameterOfBinaryTree(TreeNode root) {
        DiameterResult result = new DiameterResult();
        result.diameter = 0;
        diameterHelper(root, result);
        return result.diameter;
    }
    
    private static int diameterHelper(TreeNode root, DiameterResult result) {
        if (root == null) return 0;
        
        int leftHeight = diameterHelper(root.left, result);
        int rightHeight = diameterHelper(root.right, result);
        
        result.diameter = Math.max(result.diameter, leftHeight + rightHeight);
        
        return 1 + Math.max(leftHeight, rightHeight);
    }
    
    public static void main(String[] args) {
        // Create a sample tree:    1
        //                         / \\
        //                        2   3
        //                       / \\
        //                      4   5
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.left.right = new TreeNode(5);
        
        System.out.print("Inorder: ");
        List<Integer> inorder = new ArrayList<>();
        inorderTraversal(root, inorder);
        System.out.println(inorder);
        
        System.out.print("Preorder: ");
        List<Integer> preorder = new ArrayList<>();
        preorderTraversal(root, preorder);
        System.out.println(preorder);
        
        System.out.print("Postorder: ");
        List<Integer> postorder = new ArrayList<>();
        postorderTraversal(root, postorder);
        System.out.println(postorder);
        
        System.out.println("Level Order: " + levelOrderTraversal(root));
        
        System.out.println("Height: " + getHeight(root));
        System.out.println("Is Balanced? " + isBalanced(root));
        System.out.println("Has Path Sum 7? " + hasPathSum(root, 7));
        System.out.println("Sum of Numbers: " + sumNumbers(root));
        System.out.println("Diameter: " + diameterOfBinaryTree(root));
    }
}
