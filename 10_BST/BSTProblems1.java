/**
 * DSA 450 Problems - Binary Search Trees (Part 1: Problems 1-15)
 * Based on GeeksforGeeks 450 DSA Problems
 */

public class BSTProblems1 {
    // Note: TreeNode class moved to BinaryTreeProblems1.java to avoid duplication
    // Using TreeNode from BinaryTreeProblems1.java
    
    static class TreeNode {
        int data;
        TreeNode left, right;
        TreeNode(int data) { this.data = data; }
    }
    
    /**
     * Problem 1: Search in BST
     * Time Complexity: O(log n)
     * Space Complexity: O(h)
     */
    public static boolean searchBST(TreeNode root, int target) {
        if (root == null) return false;
        
        if (root.data == target) return true;
        else if (root.data > target) return searchBST(root.left, target);
        else return searchBST(root.right, target);
    }
    
    /**
     * Problem 2: Insert in BST
     * Time Complexity: O(log n)
     * Space Complexity: O(h)
     */
    public static TreeNode insertBST(TreeNode root, int value) {
        if (root == null) return new TreeNode(value);
        
        if (value < root.data) {
            root.left = insertBST(root.left, value);
        } else {
            root.right = insertBST(root.right, value);
        }
        
        return root;
    }
    
    /**
     * Problem 3: Delete from BST
     * Time Complexity: O(log n)
     * Space Complexity: O(h)
     */
    public static TreeNode deleteBST(TreeNode root, int value) {
        if (root == null) return null;
        
        if (value < root.data) {
            root.left = deleteBST(root.left, value);
        } else if (value > root.data) {
            root.right = deleteBST(root.right, value);
        } else {
            // Node to delete found
            if (root.left == null) return root.right;
            if (root.right == null) return root.left;
            
            // Find minimum in right subtree
            TreeNode minRight = root.right;
            while (minRight.left != null) minRight = minRight.left;
            
            root.data = minRight.data;
            root.right = deleteBST(root.right, minRight.data);
        }
        
        return root;
    }
    
    /**
     * Problem 4: Validate BST
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean isValidBST(TreeNode root) {
        return isValidBSTHelper(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    private static boolean isValidBSTHelper(TreeNode root, long min, long max) {
        if (root == null) return true;
        
        if (root.data <= min || root.data >= max) return false;
        
        return isValidBSTHelper(root.left, min, root.data) &&
               isValidBSTHelper(root.right, root.data, max);
    }
    
    /**
     * Problem 5: Kth Smallest Element in BST
     * Time Complexity: O(k)
     * Space Complexity: O(h)
     */
    static class Counter {
        int count = 0;
        int result = -1;
    }
    
    public static int kthSmallestBST(TreeNode root, int k) {
        Counter counter = new Counter();
        kthSmallestHelper(root, k, counter);
        return counter.result;
    }
    
    private static void kthSmallestHelper(TreeNode root, int k, Counter counter) {
        if (root == null) return;
        
        kthSmallestHelper(root.left, k, counter);
        
        counter.count++;
        if (counter.count == k) {
            counter.result = root.data;
            return;
        }
        
        kthSmallestHelper(root.right, k, counter);
    }
    
    /**
     * Problem 6: Kth Largest Element in BST
     * Time Complexity: O(k)
     * Space Complexity: O(h)
     */
    public static int kthLargestBST(TreeNode root, int k) {
        Counter counter = new Counter();
        kthLargestHelper(root, k, counter);
        return counter.result;
    }
    
    private static void kthLargestHelper(TreeNode root, int k, Counter counter) {
        if (root == null) return;
        
        kthLargestHelper(root.right, k, counter);
        
        counter.count++;
        if (counter.count == k) {
            counter.result = root.data;
            return;
        }
        
        kthLargestHelper(root.left, k, counter);
    }
    
    /**
     * Problem 7: Lowest Common Ancestor in BST
     * Time Complexity: O(log n)
     * Space Complexity: O(h)
     */
    public static TreeNode lcaBST(TreeNode root, int p, int q) {
        if (root == null) return null;
        
        if (p < root.data && q < root.data) {
            return lcaBST(root.left, p, q);
        } else if (p > root.data && q > root.data) {
            return lcaBST(root.right, p, q);
        } else {
            return root;
        }
    }
    
    /**
     * Problem 8: Inorder Successor in BST
     * Time Complexity: O(h)
     * Space Complexity: O(1)
     */
    public static TreeNode inorderSuccessor(TreeNode root, TreeNode p) {
        if (p.right != null) {
            TreeNode curr = p.right;
            while (curr.left != null) curr = curr.left;
            return curr;
        }
        
        TreeNode successor = null;
        TreeNode curr = root;
        
        while (curr != null) {
            if (curr.data > p.data) {
                successor = curr;
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }
        
        return successor;
    }
    
    /**
     * Problem 9: Inorder Predecessor in BST
     * Time Complexity: O(h)
     * Space Complexity: O(1)
     */
    public static TreeNode inorderPredecessor(TreeNode root, TreeNode p) {
        if (p.left != null) {
            TreeNode curr = p.left;
            while (curr.right != null) curr = curr.right;
            return curr;
        }
        
        TreeNode predecessor = null;
        TreeNode curr = root;
        
        while (curr != null) {
            if (curr.data < p.data) {
                predecessor = curr;
                curr = curr.right;
            } else {
                curr = curr.left;
            }
        }
        
        return predecessor;
    }
    
    /**
     * Problem 10: Convert BST to Min-Heap
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static void bstToMinHeap(TreeNode root, java.util.List<Integer> list) {
        if (root == null) return;
        
        bstToMinHeap(root.left, list);
        list.add(root.data);
        bstToMinHeap(root.right, list);
    }
    
    /**
     * Problem 11: Binary Tree to BST Conversion
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static void binaryTreeToBST(TreeNode root) {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        inorderTraverse(root, values);
        java.util.Collections.sort(values);
        
        java.util.List<Integer> index = new java.util.ArrayList<>();
        index.add(0);
        assignValues(root, values, index);
    }
    
    private static void inorderTraverse(TreeNode root, java.util.List<Integer> values) {
        if (root == null) return;
        inorderTraverse(root.left, values);
        values.add(root.data);
        inorderTraverse(root.right, values);
    }
    
    private static void assignValues(TreeNode root, java.util.List<Integer> values, java.util.List<Integer> index) {
        if (root == null) return;
        assignValues(root.left, values, index);
        int idx = index.get(0);
        root.data = values.get(idx);
        index.set(0, idx + 1);
        assignValues(root.right, values, index);
    }
    
    /**
     * Problem 12: Count BST nodes in range
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int countNodesInRange(TreeNode root, int low, int high) {
        if (root == null) return 0;
        
        if (root.data < low) {
            return countNodesInRange(root.right, low, high);
        } else if (root.data > high) {
            return countNodesInRange(root.left, low, high);
        } else {
            return 1 + countNodesInRange(root.left, low, high) + 
                   countNodesInRange(root.right, low, high);
        }
    }
    
    /**
     * Problem 13: Sum of all elements in BST in range
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static int sumInRange(TreeNode root, int low, int high) {
        if (root == null) return 0;
        
        if (root.data < low) {
            return sumInRange(root.right, low, high);
        } else if (root.data > high) {
            return sumInRange(root.left, low, high);
        } else {
            return root.data + sumInRange(root.left, low, high) + 
                   sumInRange(root.right, low, high);
        }
    }
    
    /**
     * Problem 14: Serialize and Deserialize BST
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static String serializeBST(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }
    
    private static void serializeHelper(TreeNode root, StringBuilder sb) {
        if (root == null) return;
        sb.append(root.data).append(",");
        serializeHelper(root.left, sb);
        serializeHelper(root.right, sb);
    }
    
    public static TreeNode deserializeBST(String data) {
        if (data == null || data.isEmpty()) return null;
        
        String[] values = data.split(",");
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        for (String val : values) {
            if (!val.isEmpty()) queue.add(Integer.parseInt(val));
        }
        
        return deserializeHelper(queue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    private static TreeNode deserializeHelper(java.util.Queue<Integer> queue, int min, int max) {
        if (queue.isEmpty()) return null;
        
        int val = queue.peek();
        if (val < min || val > max) return null;
        
        queue.poll();
        TreeNode root = new TreeNode(val);
        root.left = deserializeHelper(queue, min, val);
        root.right = deserializeHelper(queue, val, max);
        
        return root;
    }
    
    /**
     * Problem 15: Recover BST (Two nodes swapped)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class Info {
        TreeNode first, second, prev;
    }
    
    public static void recoverBST(TreeNode root) {
        Info info = new Info();
        recoverHelper(root, info);
        
        if (info.first != null && info.second != null) {
            int temp = info.first.data;
            info.first.data = info.second.data;
            info.second.data = temp;
        }
    }
    
    private static void recoverHelper(TreeNode root, Info info) {
        if (root == null) return;
        
        recoverHelper(root.left, info);
        
        if (info.prev != null && info.prev.data > root.data) {
            if (info.first == null) {
                info.first = info.prev;
            }
            info.second = root;
        }
        
        info.prev = root;
        recoverHelper(root.right, info);
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - BST Problems (Part 1) ===\n");
        
        TreeNode root = new TreeNode(50);
        root.left = new TreeNode(30);
        root.right = new TreeNode(70);
        root.left.left = new TreeNode(20);
        
        System.out.println("1. Search 30: " + searchBST(root, 30));
        System.out.println("5. Kth Smallest (k=2): " + kthSmallestBST(root, 2));
        System.out.println("4. Valid BST: " + isValidBST(root));
    }
}
