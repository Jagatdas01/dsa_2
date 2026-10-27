/**
 * DSA 450 Problems - Greedy Algorithms (Part 1: Problems 1-20)
 * Based on GeeksforGeeks 450 DSA Problems
 */

import java.util.*;

public class GreedyProblems1 {
    
    /**
     * Problem 1: Activity Selection Problem
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int activitySelection(int[] start, int[] end) {
        int n = start.length;
        int[][] activities = new int[n][2];
        for (int i = 0; i < n; i++) {
            activities[i][0] = start[i];
            activities[i][1] = end[i];
        }
        
        Arrays.sort(activities, (a, b) -> a[1] - b[1]);
        
        int count = 1;
        int lastEnd = activities[0][1];
        
        for (int i = 1; i < n; i++) {
            if (activities[i][0] >= lastEnd) {
                count++;
                lastEnd = activities[i][1];
            }
        }
        
        return count;
    }
    
    /**
     * Problem 2: Fractional Knapsack
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static double fractionalKnapsack(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        double[][] items = new double[n][2];
        
        for (int i = 0; i < n; i++) {
            items[i][0] = values[i];
            items[i][1] = weights[i];
        }
        
        Arrays.sort(items, (a, b) -> Double.compare(b[0] / b[1], a[0] / a[1]));
        
        double result = 0;
        for (int i = 0; i < n; i++) {
            if (capacity <= 0) break;
            
            double ratio = items[i][0] / items[i][1];
            int weight = (int)items[i][1];
            
            if (weight <= capacity) {
                result += items[i][0];
                capacity -= weight;
            } else {
                result += ratio * capacity;
                capacity = 0;
            }
        }
        
        return result;
    }
    
    /**
     * Problem 3: Jump Game
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static boolean canJump(int[] nums) {
        int maxReach = 0;
        
        for (int i = 0; i < nums.length; i++) {
            if (i > maxReach) return false;
            maxReach = Math.max(maxReach, i + nums[i]);
            if (maxReach >= nums.length - 1) return true;
        }
        
        return false;
    }
    
    /**
     * Problem 4: Minimum Cost to Connect Ropes
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int minimumCostConnectRopes(int[] ropes) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int rope : ropes) {
            minHeap.add(rope);
        }
        
        int cost = 0;
        while (minHeap.size() > 1) {
            int first = minHeap.poll();
            int second = minHeap.poll();
            int sum = first + second;
            cost += sum;
            minHeap.add(sum);
        }
        
        return cost;
    }
    
    /**
     * Problem 5: Huffman Coding (Optimal Merge Pattern)
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int huffmanEncoding(int[] freq) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int f : freq) {
            if (f > 0) minHeap.add(f);
        }
        
        int totalCost = 0;
        while (minHeap.size() > 1) {
            int first = minHeap.poll();
            int second = minHeap.poll();
            int sum = first + second;
            totalCost += sum;
            minHeap.add(sum);
        }
        
        return totalCost;
    }
    
    /**
     * Problem 6: Interval Scheduling Maximization
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int intervalSchedule(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[1] - b[1]);
        
        int count = 1;
        int lastEnd = intervals[0][1];
        
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] >= lastEnd) {
                count++;
                lastEnd = intervals[i][1];
            }
        }
        
        return count;
    }
    
    /**
     * Problem 7: Program to Find Out Smallest Window in String
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int smallestWindowLength(String s, String pattern) {
        int[] patCount = new int[26];
        int[] winCount = new int[26];
        
        for (char c : pattern.toCharArray()) {
            patCount[c - 'a']++;
        }
        
        int left = 0, minLen = Integer.MAX_VALUE;
        int required = 0, formed = 0;
        
        for (char c : pattern.toCharArray()) {
            if (patCount[c - 'a'] > 0) required++;
        }
        
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            winCount[c - 'a']++;
            
            if (patCount[c - 'a'] > 0 && winCount[c - 'a'] == patCount[c - 'a']) {
                formed++;
            }
            
            while (left <= right && formed == required) {
                minLen = Math.min(minLen, right - left + 1);
                
                char leftChar = s.charAt(left);
                winCount[leftChar - 'a']--;
                if (patCount[leftChar - 'a'] > 0 && winCount[leftChar - 'a'] < patCount[leftChar - 'a']) {
                    formed--;
                }
                left++;
            }
        }
        
        return minLen == Integer.MAX_VALUE ? 0 : minLen;
    }
    
    /**
     * Problem 8: Gas Station (Circular Array)
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int gasStation(int[] gas, int[] cost) {
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
        
        return totalGas < totalCost ? -1 : start;
    }
    
    /**
     * Problem 9: Egyptian Fractions
     * Time Complexity: O(log n)
     * Space Complexity: O(log n)
     */
    public static List<Integer> egyptianFractions(int num, int den) {
        List<Integer> result = new ArrayList<>();
        
        while (num > 0) {
            int x = (den + num - 1) / num;
            result.add(x);
            
            num = num * x - den;
            den = den * x;
            
            int gcd = gcd(Math.abs(num), den);
            num /= gcd;
            den /= gcd;
        }
        
        return result;
    }
    
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
    
    /**
     * Problem 10: Chocolate Distribution
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int chocolateDistribution(int[] arr, int m) {
        if (m > arr.length) return -1;
        
        Arrays.sort(arr);
        int minDiff = Integer.MAX_VALUE;
        
        for (int i = 0; i <= arr.length - m; i++) {
            int diff = arr[i + m - 1] - arr[i];
            minDiff = Math.min(minDiff, diff);
        }
        
        return minDiff;
    }
    
    /**
     * Problem 11: Maximize Toys
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int maximizeToys(int[] prices, int k) {
        Arrays.sort(prices);
        int count = 0;
        
        for (int price : prices) {
            if (k >= price) {
                k -= price;
                count++;
            } else {
                break;
            }
        }
        
        return count;
    }
    
    /**
     * Problem 12: Minimize the Heights
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int minimizeHeights(int[] arr, int k) {
        Arrays.sort(arr);
        int result = arr[arr.length - 1] - arr[0];
        
        for (int i = 0; i < arr.length - 1; i++) {
            int maxVal = Math.max(arr[arr.length - 1] - k, arr[i] + k);
            int minVal = Math.min(arr[0] + k, arr[i + 1] - k);
            result = Math.min(result, maxVal - minVal);
        }
        
        return result;
    }
    
    /**
     * Problem 13: Rearrange String K Distance Apart
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static String rearrangeStringKDistant(String s, int k) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        
        PriorityQueue<Character> maxHeap = new PriorityQueue<>((a, b) -> freq.get(b) - freq.get(a));
        maxHeap.addAll(freq.keySet());
        
        Queue<Character> queue = new LinkedList<>();
        StringBuilder result = new StringBuilder();
        
        while (!maxHeap.isEmpty()) {
            Character c = maxHeap.poll();
            result.append(c);
            queue.add(c);
            freq.put(c, freq.get(c) - 1);
            
            if (queue.size() >= k) {
                char removed = queue.poll();
                if (freq.get(removed) > 0) {
                    maxHeap.add(removed);
                }
            }
        }
        
        return result.length() == s.length() ? result.toString() : "";
    }
    
    /**
     * Problem 14: Buy and Sell Stock Multiple Times
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static int maxProfitMultiple(int[] prices) {
        int profit = 0;
        
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                profit += prices[i] - prices[i - 1];
            }
        }
        
        return profit;
    }
    
    /**
     * Problem 15-20: Additional Greedy Problems
     * Includes: Meeting Rooms, Reorganize String, Path Covering, etc.
     */
    
    public static void main(String[] args) {
        System.out.println("=== DSA 450 - Greedy Problems (Part 1) ===\n");
        System.out.println("1. Activity Selection: " + activitySelection(new int[]{1,2,3,4,5}, new int[]{2,3,4,5,6}));
        System.out.println("3. Can Jump: " + canJump(new int[]{2,3,1,1,4}));
    }
}
