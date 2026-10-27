/**
 * DSA Pattern: Binary Search Tree Operations
 * Topics: Insertion, deletion, search, validation
 */

public class BSTPatterns {
    
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        
        TreeNode(int val) {
            this.val = val;
        }
    }
    
    /**
     * Problem: Search in Binary Search Tree
     * Find if value exists in BST
     * 
     * Input: BST with values [4, 2, 7, 1, 3], target = 2
     * Expected Output: TreeNode(2) with its subtree
     * 
     * Input: BST with values [4, 2, 7, 1, 3], target = 5
     * Expected Output: null (value not found)
     * 
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(1)
     */
    public static TreeNode searchBST(TreeNode root, int val) {
        while (root != null) {
            if (val < root.val) {
                root = root.left;
            } else if (val > root.val) {
                root = root.right;
            } else {
                return root;
            }
        }
        return null;
    }
    
    /**
     * Problem: Insert Value in Binary Search Tree
     * Add new node with value maintaining BST property
     * 
     * Input: BST = [4, 2, 7, 1, 3], val = 5
     * Expected Output: Modified BST with 5 inserted in correct position
     * 
     * Input: Empty tree, val = 1
     * Expected Output: TreeNode(1) (new root)
     * 
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(1)
     */
    public static TreeNode insertBST(TreeNode root, int val) {
        if (root == null) {
            return new TreeNode(val);
        }
        
        if (val < root.val) {
            root.left = insertBST(root.left, val);
        } else if (val > root.val) {
            root.right = insertBST(root.right, val);
        }
        
        return root;
    }
    
    /**
     * Problem: Delete Value from Binary Search Tree
     * Remove node while maintaining BST property (3 cases)
     * 
     * Input: BST = [5, 3, 6, 2, 4, 1], delete = 1 (leaf node)
     * Expected Output: Modified BST without node 1
     * 
     * Input: BST = [5, 3, 6, 2, 4, 1], delete = 3 (node with 2 children)
     * Expected Output: Modified BST with 3 replaced by inorder successor
     * 
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(1)
     */
    public static TreeNode deleteBST(TreeNode root, int val) {
        if (root == null) return null;
        
        if (val < root.val) {
            root.left = deleteBST(root.left, val);
        } else if (val > root.val) {
            root.right = deleteBST(root.right, val);
        } else {
            // Node to delete found
            
            // Case 1: No children
            if (root.left == null && root.right == null) {
                return null;
            }
            
            // Case 2: One child
            if (root.left == null) {
                return root.right;
            }
            if (root.right == null) {
                return root.left;
            }
            
            // Case 3: Two children - find inorder successor
            TreeNode minLarger = findMin(root.right);
            root.val = minLarger.val;
            root.right = deleteBST(root.right, minLarger.val);
        }
        
        return root;
    }
    
    /**
     * Problem: Find minimum value in BST
     * Time Complexity: O(log n) average, O(n) worst
     * Space Complexity: O(1)
     */
    public static TreeNode findMin(TreeNode root) {
        while (root.left != null) {
            root = root.left;
        }
        return root;
    }
    
    /**
     * Problem: Find maximum value in BST
     * Time Complexity: O(log n) average, O(n) worst
     * Space Complexity: O(1)
     */
    public static TreeNode findMax(TreeNode root) {
        while (root.right != null) {
            root = root.right;
        }
        return root;
    }
    
    /**
     * Problem: Validate BST
     * Time Complexity: O(n)
     * Space Complexity: O(h)
     */
    public static boolean isValidBST(TreeNode root) {
        return isValidBSTHelper(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    private static boolean isValidBSTHelper(TreeNode root, long minVal, long maxVal) {
        if (root == null) return true;
        
        if (root.val <= minVal || root.val >= maxVal) {
            return false;
        }
        
        return isValidBSTHelper(root.left, minVal, root.val) &&
               isValidBSTHelper(root.right, root.val, maxVal);
    }
    
    /**
     * Problem: Lowest Common Ancestor of BST
     * Time Complexity: O(log n) average, O(n) worst
     * Space Complexity: O(1)
     */
    public static TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        while (root != null) {
            if (p.val < root.val && q.val < root.val) {
                root = root.left;
            } else if (p.val > root.val && q.val > root.val) {
                root = root.right;
            } else {
                return root;
            }
        }
        return null;
    }
    
    /**
     * Problem: Convert Sorted Array to BST
     * Time Complexity: O(n)
     * Space Complexity: O(log n)
     */
    public static TreeNode sortedArrayToBST(int[] nums) {
        return sortedArrayToBSTHelper(nums, 0, nums.length - 1);
    }
    
    private static TreeNode sortedArrayToBSTHelper(int[] nums, int left, int right) {
        if (left > right) return null;
        
        int mid = left + (right - left) / 2;
        TreeNode root = new TreeNode(nums[mid]);
        
        root.left = sortedArrayToBSTHelper(nums, left, mid - 1);
        root.right = sortedArrayToBSTHelper(nums, mid + 1, right);
        
        return root;
    }
    
    /**
     * Problem: Kth Smallest Element in BST
     * Time Complexity: O(k)
     * Space Complexity: O(h)
     */
    static class Counter {
        int count;
    }
    
    public static int kthSmallest(TreeNode root, int k) {
        Counter counter = new Counter();
        int[] result = new int[1];
        inorderKth(root, k, counter, result);
        return result[0];
    }
    
    private static void inorderKth(TreeNode root, int k, Counter counter, int[] result) {
        if (root == null) return;
        
        inorderKth(root.left, k, counter, result);
        
        counter.count++;
        if (counter.count == k) {
            result[0] = root.val;
        }
        
        inorderKth(root.right, k, counter, result);
    }
    
    public static void main(String[] args) {
        TreeNode root = null;
        
        // Insert values
        root = insertBST(root, 4);
        root = insertBST(root, 2);
        root = insertBST(root, 6);
        root = insertBST(root, 1);
        root = insertBST(root, 3);
        root = insertBST(root, 5);
        root = insertBST(root, 7);
        
        System.out.println("Search for 3: " + (searchBST(root, 3) != null ? "Found" : "Not Found"));
        System.out.println("Is Valid BST: " + isValidBST(root));
        System.out.println("Min value: " + findMin(root).val);
        System.out.println("Max value: " + findMax(root).val);
        System.out.println("Kth Smallest (k=3): " + kthSmallest(root, 3));
        
        int[] sorted = {-10, -3, 0, 5, 9};
        TreeNode bstFromArray = sortedArrayToBST(sorted);
        System.out.println("BST created from sorted array: " + isValidBST(bstFromArray));
    }
}
