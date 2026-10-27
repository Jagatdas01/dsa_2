/**
 * DSA Pattern: Linked List Operations
 * Topics: Insertion, deletion, reversal, cycle detection
 */

public class LinkedListPatterns {
    
    static class Node {
        int data;
        Node next;
        
        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }
    
    /**
     * Problem: Insert Node at Beginning
     * Add new node with data at the start of linked list
     * 
     * Input: head = 1 -> 2 -> 3, data = 0
     * Expected Output: 0 -> 1 -> 2 -> 3 (0 inserted at beginning)
     * 
     * Input: head = null, data = 5
     * Expected Output: 5 (new list with single node)
     * 
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static Node insertAtBeginning(Node head, int data) {
        Node newNode = new Node(data);
        newNode.next = head;
        return newNode;
    }
    
    /**
     * Problem: Insert Node at End
     * Add new node with data at the end of linked list
     * 
     * Input: head = 1 -> 2 -> 3, data = 4
     * Expected Output: 1 -> 2 -> 3 -> 4 (4 inserted at end)
     * 
     * Input: head = null, data = 5
     * Note: Assumes non-null head; typically handle separately
     * 
     * Time Complexity: O(n) - need to traverse to end
     * Space Complexity: O(1)
     */
    public static void insertAtEnd(Node head, int data) {
        Node newNode = new Node(data);
        Node current = head;
        
        while (current.next != null) {
            current = current.next;
        }
        
        current.next = newNode;
    }
    
    /**
     * Problem: Delete Node with Given Value
     * Remove first occurrence of node with specific value
     * 
     * Input: head = 1 -> 2 -> 3, data = 2
     * Expected Output: 1 -> 3 (node with value 2 deleted)
     * 
     * Input: head = 1, data = 1
     * Expected Output: null (only node deleted, empty list)
     * 
     * Time Complexity: O(n) - may traverse entire list
     * Space Complexity: O(1)
     */
    public static Node deleteNode(Node head, int data) {
        if (head == null) return null;
        
        if (head.data == data) {
            return head.next;
        }
        
        Node current = head;
        while (current.next != null && current.next.data != data) {
            current = current.next;
        }
        
        if (current.next != null) {
            current.next = current.next.next;
        }
        
        return head;
    }
    
    /**
     * Problem: Reverse Linked List
     * Reverse the order of nodes in linked list
     * 
     * Input: head = 1 -> 2 -> 3 -> 4
     * Expected Output: 4 -> 3 -> 2 -> 1 (entire list reversed)
     * 
     * Input: head = 1 -> 2
     * Expected Output: 2 -> 1
     * 
     * Time Complexity: O(n) - visit each node once
     * Space Complexity: O(1) - only three pointers
     */
    public static Node reverseList(Node head) {
        Node prev = null;
        Node current = head;
        
        while (current != null) {
            Node nextTemp = current.next;
            current.next = prev;
            prev = current;
            current = nextTemp;
        }
        
        return prev;
    }
    
    /**
     * Problem: Detect cycle in linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean hasCycle(Node head) {
        if (head == null || head.next == null) return false;
        
        Node slow = head;
        Node fast = head.next;
        
        while (slow != fast && fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow == fast;
    }
    
    /**
     * Problem: Find middle of linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node findMiddle(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow;
    }
    
    /**
     * Problem: Merge two sorted linked lists
     * Time Complexity: O(n + m)
     * Space Complexity: O(1)
     */
    public static Node mergeSorted(Node list1, Node list2) {
        Node dummy = new Node(0);
        Node current = dummy;
        
        while (list1 != null && list2 != null) {
            if (list1.data <= list2.data) {
                current.next = list1;
                list1 = list1.next;
            } else {
                current.next = list2;
                list2 = list2.next;
            }
            current = current.next;
        }
        
        if (list1 != null) {
            current.next = list1;
        } else {
            current.next = list2;
        }
        
        return dummy.next;
    }
    
    /**
     * Problem: Get length of linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int getLength(Node head) {
        int count = 0;
        while (head != null) {
            count++;
            head = head.next;
        }
        return count;
    }
    
    public static void printList(Node head) {
        while (head != null) {
            System.out.print(head.data + " -> ");
            head = head.next;
        }
        System.out.println("null");
    }
    
    public static void main(String[] args) {
        // Create a linked list: 1 -> 2 -> 3 -> 4
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        head.next.next.next = new Node(4);
        
        System.out.print("Original List: ");
        printList(head);
        
        head = insertAtBeginning(head, 0);
        System.out.print("After inserting 0 at beginning: ");
        printList(head);
        
        Node reversed = reverseList(head);
        System.out.print("After reversal: ");
        printList(reversed);
        
        System.out.println("Length: " + getLength(reversed));
    }
}
