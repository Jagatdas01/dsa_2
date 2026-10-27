/**
 * DSA 450 Problems - LinkedList (Part 1: Problems 1-18)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class LinkedListProblems1 {
    
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    /**
     * Problem 1: Reverse a Linked List (Iterative)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node reverseIterative(Node head) {
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
     * Problem 2: Reverse a Linked List (Recursive)
     * Time Complexity: O(n)
     * Space Complexity: O(n) for recursion stack
     */
    public static Node reverseRecursive(Node head) {
        if (head == null || head.next == null) return head;
        
        Node newHead = reverseRecursive(head.next);
        head.next.next = head;
        head.next = null;
        
        return newHead;
    }
    
    /**
     * Problem 3: Reverse linked list in groups of given size
     * Time Complexity: O(n)
     * Space Complexity: O(k) for recursion
     * [VERY IMPORTANT]
     */
    public static Node reverseInGroups(Node head, int k) {
        Node current = head;
        Node prev = null;
        
        while (current != null) {
            Node nextGroup = current;
            Node newHead = null;
            
            // Reverse k nodes
            for (int i = 0; i < k && nextGroup != null; i++) {
                Node nextNode = nextGroup.next;
                nextGroup.next = newHead;
                newHead = nextGroup;
                nextGroup = nextNode;
            }
            
            if (prev != null) {
                prev.next = newHead;
            } else {
                head = newHead;
            }
            
            prev = current;
            current = nextGroup;
        }
        
        return head;
    }
    
    /**
     * Problem 4: Detect loop in linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean detectLoop(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) return true;
        }
        
        return false;
    }
    
    /**
     * Problem 5: Delete loop in linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void deleteLoop(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) break;
        }
        
        if (fast == null || fast.next == null) return;
        
        slow = head;
        if (slow == fast) {
            while (fast.next != slow) fast = fast.next;
        } else {
            while (slow.next != fast.next) {
                slow = slow.next;
                fast = fast.next;
            }
        }
        
        fast.next = null;
    }
    
    /**
     * Problem 6: Find starting point of loop
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node findLoopStart(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) break;
        }
        
        if (fast == null || fast.next == null) return null;
        
        slow = head;
        while (slow != fast) {
            slow = slow.next;
            fast = fast.next;
        }
        
        return slow;
    }
    
    /**
     * Problem 7: Remove duplicates in sorted linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node removeDuplicatesSorted(Node head) {
        Node current = head;
        
        while (current != null && current.next != null) {
            if (current.data == current.next.data) {
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }
        
        return head;
    }
    
    /**
     * Problem 8: Remove duplicates in unsorted linked list
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node removeDuplicatesUnsorted(Node head) {
        Set<Integer> seen = new HashSet<>();
        Node dummy = new Node(0);
        dummy.next = head;
        Node prev = dummy;
        
        while (head != null) {
            if (seen.contains(head.data)) {
                prev.next = head.next;
            } else {
                seen.add(head.data);
                prev = head;
            }
            head = head.next;
        }
        
        return dummy.next;
    }
    
    /**
     * Problem 9: Move last element to front
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node moveLastToFront(Node head) {
        if (head == null || head.next == null) return head;
        
        Node current = head;
        while (current.next.next != null) {
            current = current.next;
        }
        
        Node last = current.next;
        current.next = null;
        last.next = head;
        
        return last;
    }
    
    /**
     * Problem 10: Add 1 to linked list number
     * Time Complexity: O(n)
     * Space Complexity: O(n) for result
     */
    public static Node addOne(Node head) {
        head = reverseIterative(head);
        Node current = head;
        int carry = 1;
        
        while (current != null) {
            int sum = current.data + carry;
            current.data = sum % 10;
            carry = sum / 10;
            
            if (current.next == null && carry > 0) {
                current.next = new Node(carry);
                carry = 0;
                break;
            }
            
            current = current.next;
        }
        
        return reverseIterative(head);
    }
    
    /**
     * Problem 11: Add two numbers represented by linked lists
     * Time Complexity: O(max(m, n))
     * Space Complexity: O(max(m, n))
     */
    public static Node addTwoNumbers(Node head1, Node head2) {
        Node dummy = new Node(0);
        Node result = dummy;
        int carry = 0;
        
        while (head1 != null || head2 != null || carry > 0) {
            int sum = carry;
            if (head1 != null) {
                sum += head1.data;
                head1 = head1.next;
            }
            if (head2 != null) {
                sum += head2.data;
                head2 = head2.next;
            }
            
            carry = sum / 10;
            result.next = new Node(sum % 10);
            result = result.next;
        }
        
        return dummy.next;
    }
    
    /**
     * Problem 12: Intersection of two sorted linked lists
     * Time Complexity: O(m + n)
     * Space Complexity: O(1)
     */
    public static List<Integer> intersection(Node head1, Node head2) {
        List<Integer> result = new ArrayList<>();
        
        while (head1 != null && head2 != null) {
            if (head1.data == head2.data) {
                result.add(head1.data);
                head1 = head1.next;
                head2 = head2.next;
            } else if (head1.data < head2.data) {
                head1 = head1.next;
            } else {
                head2 = head2.next;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 13: Intersection point of two linked lists
     * Time Complexity: O(m + n)
     * Space Complexity: O(1)
     */
    public static Node intersectionPoint(Node head1, Node head2) {
        Node a = head1;
        Node b = head2;
        
        while (a != b) {
            a = (a == null) ? head2 : a.next;
            b = (b == null) ? head1 : b.next;
        }
        
        return a;
    }
    
    /**
     * Problem 14: Merge sort for linked lists
     * Time Complexity: O(n log n)
     * Space Complexity: O(log n) for recursion
     * [VERY IMPORTANT]
     */
    public static Node mergeSort(Node head) {
        if (head == null || head.next == null) return head;
        
        Node mid = getMiddle(head);
        Node nextMid = mid.next;
        mid.next = null;
        
        Node left = mergeSort(head);
        Node right = mergeSort(nextMid);
        
        return merge(left, right);
    }
    
    private static Node getMiddle(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow;
    }
    
    private static Node merge(Node l1, Node l2) {
        Node dummy = new Node(0);
        Node current = dummy;
        
        while (l1 != null && l2 != null) {
            if (l1.data < l2.data) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }
            current = current.next;
        }
        
        current.next = (l1 != null) ? l1 : l2;
        return dummy.next;
    }
    
    /**
     * Problem 15: Quicksort for linked lists
     * Time Complexity: O(n log n) average, O(n^2) worst
     * Space Complexity: O(log n)
     * [VERY IMPORTANT]
     */
    public static Node quickSort(Node head) {
        if (head == null || head.next == null) return head;
        
        Node[] partitions = partition(head);
        partitions[0] = quickSort(partitions[0]);
        partitions[2] = quickSort(partitions[2]);
        
        return concatenate(partitions[0], partitions[1], partitions[2]);
    }
    
    private static Node[] partition(Node head) {
        Node[] result = new Node[3];
        Arrays.fill(result, null);
        
        int pivot = head.data;
        Node current = head.next;
        
        Node less = null, lessEnd = null;
        Node equal = new Node(pivot);
        Node equalEnd = equal;
        Node greater = null, greaterEnd = null;
        
        while (current != null) {
            if (current.data < pivot) {
                if (less == null) less = current;
                else lessEnd.next = current;
                lessEnd = current;
            } else if (current.data == pivot) {
                equalEnd.next = current;
                equalEnd = current;
            } else {
                if (greater == null) greater = current;
                else greaterEnd.next = current;
                greaterEnd = current;
            }
            current = current.next;
        }
        
        if (lessEnd != null) lessEnd.next = null;
        equalEnd.next = null;
        if (greaterEnd != null) greaterEnd.next = null;
        
        result[0] = less;
        result[1] = equal;
        result[2] = greater;
        
        return result;
    }
    
    private static Node concatenate(Node l1, Node l2, Node l3) {
        if (l1 == null) l1 = l2;
        else {
            Node temp = l1;
            while (temp.next != null) temp = temp.next;
            temp.next = l2;
        }
        
        if (l1 == null) l1 = l3;
        else {
            Node temp = l1;
            while (temp.next != null) temp = temp.next;
            temp.next = l3;
        }
        
        return l1;
    }
    
    /**
     * Problem 16: Find middle element of linked list
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int findMiddle(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow.data;
    }
    
    /**
     * Problem 17: Check if linked list is circular
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isCircular(Node head) {
        if (head == null) return false;
        
        Node slow = head;
        Node fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) return true;
        }
        
        return false;
    }
    
    /**
     * Problem 18: Split circular linked list into two halves
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node[] splitCircularList(Node head) {
        Node slow = head;
        Node fast = head;
        
        while (fast.next != head && fast.next.next != head) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        Node second = slow.next;
        slow.next = head;
        
        if (fast.next != head) fast = fast.next;
        fast.next = second;
        
        return new Node[]{head, second};
    }
    
    // Helper to print list
    public static void printList(Node head) {
        while (head != null) {
            System.out.print(head.data + " -> ");
            head = head.next;
        }
        System.out.println("null");
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - LinkedList Problems (Part 1) ===\n");
        
        // Create test list: 1 -> 2 -> 3 -> 4 -> 5
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        head.next.next.next = new Node(4);
        head.next.next.next.next = new Node(5);
        
        System.out.println("Original list:");
        printList(head);
        
        System.out.println("\n1. Reversed (Iterative):");
        head = reverseIterative(head);
        printList(head);
        
        System.out.println("\n16. Middle element: " + findMiddle(head));
    }
}
