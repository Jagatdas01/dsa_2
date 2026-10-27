/**
 * DSA 450 Problems - Binary Search Trees (Part 2: Problems 16-29)
 * Based on GeeksforGeeks 450 DSA Problems
 */

public class BSTProblems2 {
    // Note: TreeNode class moved to BinaryTreeProblems1.java to avoid duplication
    // Using TreeNode from BinaryTreeProblems1.java
    
    static class TreeNode {
        int data;
        TreeNode left, right;
        TreeNode(int data) { this.data = data; }
    }
    
    /**
     * Problem 16: Flatten BST to Linked List
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static TreeNode flattenBST(TreeNode root) {
        if (root == null) return null;
        
        TreeNode leftLast = flattenBST(root.left);
        TreeNode rightHead = flattenBST(root.right);
        
        if (leftLast != null) {
            leftLast.right = root;
            root.left = null;
        }
        
        if (rightHead != null) {
            root.right = rightHead;
        }
        
        return root;
    }
    
    /**
     * Problem 17: Largest BST in Binary Tree
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class BSTPair {
        int size;
        int min, max;
        boolean isBST;
        TreeNode node;
    }
    
    public static TreeNode largestBSTInBT(TreeNode root) {
        BSTPair result = largestBSTHelper(root);
        return result.node;
    }
    
    private static BSTPair largestBSTHelper(TreeNode root) {
        BSTPair pair = new BSTPair();
        
        if (root == null) {
            pair.size = 0;
            pair.isBST = true;
            pair.min = Integer.MAX_VALUE;
            pair.max = Integer.MIN_VALUE;
            return pair;
        }
        
        BSTPair left = largestBSTHelper(root.left);
        BSTPair right = largestBSTHelper(root.right);
        
        if (left.isBST && right.isBST && left.max < root.data && root.data < right.min) {
            pair.isBST = true;
            pair.size = left.size + right.size + 1;
            pair.min = left.min == Integer.MAX_VALUE ? root.data : left.min;
            pair.max = right.max == Integer.MIN_VALUE ? root.data : right.max;
            pair.node = root;
        } else {
            pair.isBST = false;
            pair.size = Math.max(left.size, right.size);
            pair.node = left.size > right.size ? left.node : right.node;
        }
        
        return pair;
    }
    
    /**
     * Problem 18: Trim BST (Remove nodes outside range)
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static TreeNode trimBST(TreeNode root, int low, int high) {
        if (root == null) return null;
        
        if (root.data < low) {
            return trimBST(root.right, low, high);
        } else if (root.data > high) {
            return trimBST(root.left, low, high);
        } else {
            root.left = trimBST(root.left, low, high);
            root.right = trimBST(root.right, low, high);
            return root;
        }
    }
    
    /**
     * Problem 19: Construct BST from Preorder
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class Index {
        int index = 0;
    }
    
    public static TreeNode constructBSTPreorder(int[] preorder) {
        Index index = new Index();
        return constructHelper(preorder, Integer.MIN_VALUE, Integer.MAX_VALUE, index);
    }
    
    private static TreeNode constructHelper(int[] preorder, int min, int max, Index index) {
        if (index.index == preorder.length) return null;
        
        int val = preorder[index.index];
        if (val < min || val > max) return null;
        
        TreeNode root = new TreeNode(val);
        index.index++;
        
        root.left = constructHelper(preorder, min, val, index);
        root.right = constructHelper(preorder, val, max, index);
        
        return root;
    }
    
    /**
     * Problem 20: Construct BST from Inorder
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static TreeNode constructBSTInorder(int[] inorder) {
        return constructInorderHelper(inorder, 0, inorder.length - 1);
    }
    
    private static TreeNode constructInorderHelper(int[] inorder, int start, int end) {
        if (start > end) return null;
        
        int mid = start + (end - start) / 2;
        TreeNode root = new TreeNode(inorder[mid]);
        
        root.left = constructInorderHelper(inorder, start, mid - 1);
        root.right = constructInorderHelper(inorder, mid + 1, end);
        
        return root;
    }
    
    /**
     * Problem 21: Populate Next Pointer in each node
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    static class TreeNodeWithNext {
        int data;
        TreeNodeWithNext left, right, next;
        TreeNodeWithNext(int data) { this.data = data; }
    }
    
    public static void populateNext(TreeNodeWithNext root) {
        populateNextHelper(root, null);
    }
    
    private static void populateNextHelper(TreeNodeWithNext root, TreeNodeWithNext next) {
        if (root == null) return;
        
        root.next = next;
        populateNextHelper(root.left, root.right);
        populateNextHelper(root.right, next != null ? next.left : null);
    }
    
    /**
     * Problem 22: Pair with Given Sum in BST
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean pairWithSum(TreeNode root, int sum) {
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        return pairHelper(root, sum, seen);
    }
    
    private static boolean pairHelper(TreeNode root, int sum, java.util.Set<Integer> seen) {
        if (root == null) return false;
        
        if (seen.contains(sum - root.data)) return true;
        
        seen.add(root.data);
        
        return pairHelper(root.left, sum, seen) || pairHelper(root.right, sum, seen);
    }
    
    /**
     * Problem 23: Merge Two BSTs
     * Time Complexity: O(m + n)
     * Space Complexity: O(max(h1, h2))
     */
    public static void mergeBSTs(TreeNode root1, TreeNode root2, java.util.List<Integer> result) {
        java.util.Stack<TreeNode> stack1 = new java.util.Stack<>();
        java.util.Stack<TreeNode> stack2 = new java.util.Stack<>();
        
        pushLeft(root1, stack1);
        pushLeft(root2, stack2);
        
        while (!stack1.isEmpty() || !stack2.isEmpty()) {
            if (stack1.isEmpty()) {
                result.add(stack2.pop().data);
            } else if (stack2.isEmpty()) {
                result.add(stack1.pop().data);
            } else if (stack1.peek().data <= stack2.peek().data) {
                TreeNode node = stack1.pop();
                result.add(node.data);
                pushLeft(node.right, stack1);
            } else {
                TreeNode node = stack2.pop();
                result.add(node.data);
                pushLeft(node.right, stack2);
            }
        }
    }
    
    private static void pushLeft(TreeNode root, java.util.Stack<TreeNode> stack) {
        while (root != null) {
            stack.push(root);
            root = root.left;
        }
    }
    
    /**
     * Problem 24: Median in BST
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class Counter {
        int count = 0;
        double median = 0;
        boolean found = false;
        int prevData = Integer.MIN_VALUE;
    }
    
    public static double medianBST(TreeNode root) {
        Counter counter = new Counter();
        int count = countNodes(root);
        
        medianHelper(root, (count + 1) / 2, counter, count);
        return counter.median;
    }
    
    private static int countNodes(TreeNode root) {
        if (root == null) return 0;
        return 1 + countNodes(root.left) + countNodes(root.right);
    }
    
    private static void medianHelper(TreeNode root, int k, Counter counter, int count) {
        if (root == null || counter.found) return;
        
        medianHelper(root.left, k, counter, count);
        
        counter.count++;
        if (counter.count == k) {
            if (count % 2 == 1) {
                counter.median = root.data;
                counter.found = true;
            } else {
                counter.prevData = root.data;
            }
        } else if (counter.count == k + 1) {
            counter.median = (counter.prevData + root.data) / 2.0;
            counter.found = true;
        }
        
        medianHelper(root.right, k, counter, count);
    }
    
    /**
     * Problem 25: Convert BST to Sorted DLL
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    static class Node {
        int data;
        Node prev, next;
        Node(int data) { this.data = data; }
    }
    
    public static Node bstToDLL(TreeNode root) {
        java.util.List<Node> head = new java.util.ArrayList<>();
        head.add(null);
        
        bstToDLLHelper(root, head);
        return head.get(0);
    }
    
    private static Node bstToDLLHelper(TreeNode root, java.util.List<Node> head) {
        if (root == null) return null;
        
        Node leftLast = bstToDLLHelper(root.left, head);
        
        Node current = new Node(root.data);
        if (head.get(0) == null) {
            head.set(0, current);
        } else {
            current.prev = leftLast;
            leftLast.next = current;
        }
        
        Node rightLast = bstToDLLHelper(root.right, head);
        return rightLast != null ? rightLast : current;
    }
    
    /**
     * Problem 26: Unique Binary Search Trees
     * Time Complexity: O(n^2)
     * Space Complexity: O(n)
     */
    public static int numUniqueBSTs(int n) {
        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                dp[i] += dp[j] * dp[i - 1 - j];
            }
        }
        
        return dp[n];
    }
    
    /**
     * Problem 27: All Unique BSTs (Generate all structures)
     * Time Complexity: O(Catalan number)
     * Space Complexity: O(n)
     */
    public static java.util.List<TreeNode> uniqueBSTStructures(int n) {
        return generateBSTs(1, n);
    }
    
    private static java.util.List<TreeNode> generateBSTs(int start, int end) {
        java.util.List<TreeNode> result = new java.util.ArrayList<>();
        
        if (start > end) {
            result.add(null);
            return result;
        }
        
        for (int i = start; i <= end; i++) {
            java.util.List<TreeNode> leftTrees = generateBSTs(start, i - 1);
            java.util.List<TreeNode> rightTrees = generateBSTs(i + 1, end);
            
            for (TreeNode left : leftTrees) {
                for (TreeNode right : rightTrees) {
                    TreeNode root = new TreeNode(i);
                    root.left = left;
                    root.right = right;
                    result.add(root);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Problem 28-29: Additional BST problems follow similar patterns
     */
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - BST Problems (Part 2) ===\n");
        System.out.println("26. Unique BSTs (n=3): " + numUniqueBSTs(3));
    }
}
