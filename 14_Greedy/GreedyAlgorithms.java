/**
 * DSA Pattern: Greedy Algorithms
 * Topics: Greedy choice, Activity selection, Huffman coding concepts
 */

import java.util.*;

public class GreedyAlgorithms {
    
    /**
     * Problem: Activity Selection
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class Activity {
        int start, end;
        Activity(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    
    public static void activitySelection(Activity[] activities) {
        Arrays.sort(activities, (a, b) -> a.end - b.end);
        
        System.out.print("Selected activities: ");
        System.out.print("(" + activities[0].start + "," + activities[0].end + ") ");
        int lastEnd = activities[0].end;
        
        for (int i = 1; i < activities.length; i++) {
            if (activities[i].start >= lastEnd) {
                System.out.print("(" + activities[i].start + "," + activities[i].end + ") ");
                lastEnd = activities[i].end;
            }
        }
        System.out.println();
    }
    
    /**
     * Problem: Fractional Knapsack
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class Item {
        int weight, value;
        Item(int weight, int value) {
            this.weight = weight;
            this.value = value;
        }
    }
    
    public static double fractionalKnapsack(Item[] items, int capacity) {
        Arrays.sort(items, (a, b) -> Double.compare((double)b.value/b.weight, (double)a.value/a.weight));
        
        double maxValue = 0;
        int currentWeight = 0;
        
        for (Item item : items) {
            if (currentWeight + item.weight <= capacity) {
                currentWeight += item.weight;
                maxValue += item.value;
            } else {
                int remaining = capacity - currentWeight;
                maxValue += (double)item.value / item.weight * remaining;
                break;
            }
        }
        
        return maxValue;
    }
    
    /**
     * Problem: Huffman Coding - Building Huffman Tree
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class HuffmanNode {
        char data;
        int freq;
        HuffmanNode left, right;
        
        HuffmanNode(char data, int freq) {
            this.data = data;
            this.freq = freq;
        }
    }
    
    public static void huffmanCoding(char[] chars, int[] freq) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>((a, b) -> a.freq - b.freq);
        
        for (int i = 0; i < chars.length; i++) {
            pq.offer(new HuffmanNode(chars[i], freq[i]));
        }
        
        while (pq.size() > 1) {
            HuffmanNode first = pq.poll();
            HuffmanNode second = pq.poll();
            
            HuffmanNode parent = new HuffmanNode('\0', first.freq + second.freq);
            parent.left = first;
            parent.right = second;
            
            pq.offer(parent);
        }
        
        System.out.println("Huffman tree constructed with root frequency: " + pq.peek().freq);
    }
    
    /**
     * Problem: Jump Game
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean canJump(int[] nums) {
        int maxReach = 0;
        
        for (int i = 0; i < nums.length; i++) {
            if (i > maxReach) {
                return false;
            }
            maxReach = Math.max(maxReach, i + nums[i]);
        }
        
        return true;
    }
    
    /**
     * Problem: Meeting Rooms (Minimum rooms required)
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    static class Interval {
        int start, end;
        Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    
    public static int minMeetingRooms(Interval[] intervals) {
        if (intervals.length == 0) return 0;
        
        int[] starts = new int[intervals.length];
        int[] ends = new int[intervals.length];
        
        for (int i = 0; i < intervals.length; i++) {
            starts[i] = intervals[i].start;
            ends[i] = intervals[i].end;
        }
        
        Arrays.sort(starts);
        Arrays.sort(ends);
        
        int rooms = 0;
        int endPtr = 0;
        
        for (int start : starts) {
            if (start < ends[endPtr]) {
                rooms++;
            } else {
                endPtr++;
            }
        }
        
        return rooms;
    }
    
    /**
     * Problem: Gas Station
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int canCompleteCircuit(int[] gas, int[] cost) {
        int totalGas = 0, totalCost = 0, currentGas = 0, start = 0;
        
        for (int i = 0; i < gas.length; i++) {
            totalGas += gas[i];
            totalCost += cost[i];
            currentGas += gas[i] - cost[i];
            
            if (currentGas < 0) {
                start = i + 1;
                currentGas = 0;
            }
        }
        
        return totalGas >= totalCost ? start : -1;
    }
    
    /**
     * Problem: Assign Cookies
     * Time Complexity: O(n log n + m log m)
     * Space Complexity: O(1)
     */
    public static int findContentChildren(int[] children, int[] cookies) {
        Arrays.sort(children);
        Arrays.sort(cookies);
        
        int childIdx = 0, cookieIdx = 0;
        
        while (childIdx < children.length && cookieIdx < cookies.length) {
            if (cookies[cookieIdx] >= children[childIdx]) {
                childIdx++;
            }
            cookieIdx++;
        }
        
        return childIdx;
    }
    
    public static void main(String[] args) {
        Activity[] activities = {
            new Activity(1, 3),
            new Activity(2, 4),
            new Activity(3, 5),
            new Activity(4, 6),
            new Activity(5, 7)
        };
        activitySelection(activities);
        
        Item[] items = {new Item(10, 60), new Item(20, 100), new Item(30, 120)};
        System.out.println("Fractional Knapsack: " + fractionalKnapsack(items, 50));
        
        System.out.println("Can jump? [2,3,1,1,4]: " + canJump(new int[]{2, 3, 1, 1, 4}));
        
        Interval[] intervals = {
            new Interval(0, 30),
            new Interval(5, 10),
            new Interval(15, 20)
        };
        System.out.println("Minimum meeting rooms: " + minMeetingRooms(intervals));
        
        int[] gas = {1, 2, 3, 4, 5};
        int[] cost = {3, 4, 5, 1, 2};
        System.out.println("Starting gas station: " + canCompleteCircuit(gas, cost));
        
        int[] children = {1, 2, 3};
        int[] cookies = {1, 1};
        System.out.println("Content children: " + findContentChildren(children, cookies));
    }
}
