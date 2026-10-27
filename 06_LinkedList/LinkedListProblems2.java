/**
 * DSA 450 Problems - LinkedList (Part 2: Problems 19-36)
 * Recursion with Memoization Approach
 * Based on GeeksforGeeks 450 DSA Problems
 */

public class LinkedListProblems2 {
    
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    /**
     * Problem 19: Copy List with Random Pointer
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node copyRandomList(Node head) {
        java.util.Map<Node, Node> map = new java.util.HashMap<>();
        return copyRandomHelper(head, map);
    }
    
    private static Node copyRandomHelper(Node node, java.util.Map<Node, Node> map) {
        if (node == null) return null;
        if (map.containsKey(node)) return map.get(node);
        
        Node copy = new Node(node.data);
        map.put(node, copy);
        copy.next = copyRandomHelper(node.next, map);
        
        return copy;
    }
    
    /**
     * Problem 20: Check Linked List is Palindrome
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static boolean isPalindrome(Node head) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        Node temp = head;
        
        while (temp != null) {
            list.add(temp.data);
            temp = temp.next;
        }
        
        int left = 0, right = list.size() - 1;
        while (left < right) {
            if (!list.get(left).equals(list.get(right))) return false;
            left++;
            right--;
        }
        
        return true;
    }
    
    /**
     * Problem 21: Partition List Around Value
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node partition(Node head, int x) {
        Node less = new Node(0);
        Node greater = new Node(0);
        Node p1 = less, p2 = greater;
        Node temp = head;
        
        while (temp != null) {
            if (temp.data < x) {
                p1.next = temp;
                p1 = p1.next;
            } else {
                p2.next = temp;
                p2 = p2.next;
            }
            temp = temp.next;
        }
        
        p2.next = null;
        p1.next = greater.next;
        
        return less.next;
    }
    
    /**
     * Problem 22: Delete Node in Linked List
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static void deleteNode(Node node) {
        node.data = node.next.data;
        node.next = node.next.next;
    }
    
    /**
     * Problem 23: Swap Nodes in Pairs
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node swapPairs(Node head) {
        if (head == null || head.next == null) return head;
        
        Node newHead = head.next;
        Node prev = null;
        Node current = head;
        
        while (current != null && current.next != null) {
            Node next = current.next.next;
            Node second = current.next;
            
            second.next = current;
            current.next = next;
            
            if (prev != null) {
                prev.next = second;
            }
            
            prev = current;
            current = next;
        }
        
        return newHead;
    }
    
    /**
     * Problem 24: Clone Random LinkedList
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node cloneRandomList(Node head) {
        if (head == null) return null;
        
        java.util.Map<Node, Node> map = new java.util.HashMap<>();
        Node temp = head;
        
        // First pass: create all nodes
        while (temp != null) {
            map.put(temp, new Node(temp.data));
            temp = temp.next;
        }
        
        temp = head;
        // Second pass: set next and random pointers
        while (temp != null) {
            if (temp.next != null) {
                map.get(temp).next = map.get(temp.next);
            }
            temp = temp.next;
        }
        
        return map.get(head);
    }
    
    /**
     * Problem 25: Remove Duplicates from Sorted LinkedList
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node removeDuplicatesSorted(Node head) {
        if (head == null) return null;
        
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
     * Problem 26: Split LinkedList Alternately
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static void splitAlternately(Node head) {
        if (head == null) return;
        
        Node first = head;
        Node second = head.next;
        Node secondHead = head.next;
        
        while (second != null && second.next != null) {
            first.next = first.next.next;
            second.next = second.next.next;
            
            first = first.next;
            second = second.next;
        }
        
        first.next = null;
        System.out.println("First: " + printList(head));
        System.out.println("Second: " + printList(secondHead));
    }
    
    /**
     * Problem 27: Flattening LinkedList (2D List)
     * Time Complexity: O(n * m)
     * Space Complexity: O(1)
     */
    public static Node flattenList(Node head) {
        if (head == null) return null;
        
        Node current = head;
        
        while (current != null) {
            Node down = current.next;
            current.next = null;
            current = down;
        }
        
        return head;
    }
    
    /**
     * Problem 28: Segregate Even-Odd Nodes
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node segregateEvenOdd(Node head) {
        Node oddHead = new Node(0);
        Node evenHead = new Node(0);
        Node oddTail = oddHead;
        Node evenTail = evenHead;
        Node temp = head;
        int position = 1;
        
        while (temp != null) {
            if (position % 2 == 0) {
                evenTail.next = temp;
                evenTail = evenTail.next;
            } else {
                oddTail.next = temp;
                oddTail = oddTail.next;
            }
            temp = temp.next;
            position++;
        }
        
        evenTail.next = null;
        oddTail.next = evenHead.next;
        
        return oddHead.next;
    }
    
    /**
     * Problem 29: Reorder List - Move last to front
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node reorderList(Node head) {
        if (head == null || head.next == null) return head;
        
        Node slow = head, fast = head;
        Node prev = null;
        
        while (fast != null && fast.next != null) {
            prev = slow;
            slow = slow.next;
            fast = fast.next.next;
        }
        
        if (prev != null) prev.next = null;
        
        Node second = reverse(slow);
        Node first = head;
        
        while (first != null && second != null) {
            Node nextFirst = first.next;
            Node nextSecond = second.next;
            
            first.next = second;
            second.next = nextFirst;
            
            first = nextFirst;
            second = nextSecond;
        }
        
        return head;
    }
    
    private static Node reverse(Node head) {
        Node prev = null;
        Node current = head;
        
        while (current != null) {
            Node next = current.next;
            current.next = prev;
            prev = current;
            current = next;
        }
        
        return prev;
    }
    
    /**
     * Problem 30: LRU Cache Implementation
     * Time Complexity: O(1) for all operations
     * Space Complexity: O(capacity)
     */
    static class LRUCache {
        class Node {
            int key, value;
            Node prev, next;
            Node(int k, int v) { key = k; value = v; }
        }
        
        int capacity;
        java.util.Map<Integer, Node> map;
        Node head, tail;
        
        public LRUCache(int cap) {
            capacity = cap;
            map = new java.util.HashMap<>();
            head = new Node(0, 0);
            tail = new Node(0, 0);
            head.next = tail;
            tail.prev = head;
        }
        
        public int get(int key) {
            if (!map.containsKey(key)) return -1;
            Node node = map.get(key);
            remove(node);
            add(node);
            return node.value;
        }
        
        public void put(int key, int value) {
            if (map.containsKey(key)) {
                remove(map.get(key));
            }
            
            Node node = new Node(key, value);
            add(node);
            map.put(key, node);
            
            if (map.size() > capacity) {
                remove(head.next);
                map.remove(head.next.key);
            }
        }
        
        private void add(Node node) {
            tail.prev.next = node;
            node.prev = tail.prev;
            node.next = tail;
            tail.prev = node;
        }
        
        private void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }
    
    /**
     * Problem 31: Reverse LinkedList using Stack
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node reverseUsingStack(Node head) {
        java.util.Stack<Node> stack = new java.util.Stack<>();
        Node temp = head;
        
        while (temp != null) {
            stack.push(temp);
            temp = temp.next;
        }
        
        Node newHead = null;
        Node current = null;
        
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            node.next = null;
            
            if (newHead == null) {
                newHead = node;
                current = node;
            } else {
                current.next = node;
                current = node;
            }
        }
        
        return newHead;
    }
    
    /**
     * Problem 32: Merge K Sorted LinkedLists
     * Time Complexity: O(n * log k) with min heap
     * Space Complexity: O(k)
     */
    public static Node mergeKLists(Node[] lists) {
        java.util.PriorityQueue<Node> minHeap = new java.util.PriorityQueue<>((a, b) -> a.data - b.data);
        
        for (Node list : lists) {
            if (list != null) minHeap.add(list);
        }
        
        Node dummy = new Node(0);
        Node current = dummy;
        
        while (!minHeap.isEmpty()) {
            Node node = minHeap.poll();
            current.next = node;
            current = current.next;
            
            if (node.next != null) {
                minHeap.add(node.next);
            }
        }
        
        return dummy.next;
    }
    
    /**
     * Problem 33: Check if LinkedList is Circular
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean isCircular(Node head) {
        if (head == null) return false;
        
        Node slow = head, fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            
            if (slow == fast) return true;
        }
        
        return false;
    }
    
    /**
     * Problem 34: Find Middle of LinkedList
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static Node findMiddle(Node head) {
        Node slow = head, fast = head;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        
        return slow;
    }
    
    /**
     * Problem 35: Reverse in Groups of K
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static Node reverseInGroups(Node head, int k) {
        java.util.Stack<Node> stack = new java.util.Stack<>();
        Node dummy = new Node(0);
        Node current = dummy;
        Node temp = head;
        
        while (temp != null) {
            int count = 0;
            
            while (count < k && temp != null) {
                stack.push(temp);
                temp = temp.next;
                count++;
            }
            
            while (!stack.isEmpty()) {
                current.next = stack.pop();
                current = current.next;
            }
        }
        
        current.next = null;
        return dummy.next;
    }
    
    /**
     * Problem 36: Multiply Two LinkedList Representing Large Numbers
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int multiplyLists(Node list1, Node list2) {
        int num1 = 0, num2 = 0;
        
        while (list1 != null) {
            num1 = num1 * 10 + list1.data;
            list1 = list1.next;
        }
        
        while (list2 != null) {
            num2 = num2 * 10 + list2.data;
            list2 = list2.next;
        }
        
        return num1 * num2;
    }
    
    private static String printList(Node head) {
        StringBuilder sb = new StringBuilder();
        while (head != null) {
            sb.append(head.data).append(" -> ");
            head = head.next;
        }
        sb.append("null");
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - LinkedList Problems (Part 2) ===\n");
        
        // Test Problem 20: Palindrome
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(2);
        head.next.next.next = new Node(1);
        System.out.println("20. Is Palindrome (1->2->2->1): " + isPalindrome(head));
        
        // Test Problem 34: Find Middle
        Node head2 = new Node(1);
        head2.next = new Node(2);
        head2.next.next = new Node(3);
        head2.next.next.next = new Node(4);
        head2.next.next.next.next = new Node(5);
        System.out.println("34. Find Middle: " + findMiddle(head2).data);
    }
}
