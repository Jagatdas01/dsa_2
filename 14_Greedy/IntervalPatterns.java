/**
 * DSA Pattern: Overlapping Intervals
 * Topics: Interval merging, interval intersection, scheduling problems, conflict resolution
 */

import java.util.*;

public class IntervalPatterns {
    
    static class Interval {
        int start;
        int end;
        
        Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public String toString() {
            return "[" + start + "," + end + "]";
        }
    }
    
    /**
     * Problem: Merge Overlapping Intervals
     * Combine all overlapping intervals into one
     * Time Complexity: O(n log n) for sorting
     * Space Complexity: O(n) for result
     */
    public static List<Interval> mergeIntervals(List<Interval> intervals) {
        if (intervals.isEmpty()) return new ArrayList<>();
        
        // Sort by start point
        intervals.sort((a, b) -> Integer.compare(a.start, b.start));
        
        List<Interval> merged = new ArrayList<>();
        Interval current = intervals.get(0);
        
        for (int i = 1; i < intervals.size(); i++) {
            Interval next = intervals.get(i);
            
            // Overlapping: extend current interval
            if (next.start <= current.end) {
                current.end = Math.max(current.end, next.end);
            } else {
                // No overlap: add current and start new
                merged.add(current);
                current = next;
            }
        }
        
        merged.add(current);
        return merged;
    }
    
    /**
     * Problem: Insert Interval into List
     * Insert a new interval and merge if necessary
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static List<Interval> insertInterval(List<Interval> intervals, Interval newInterval) {
        List<Interval> result = new ArrayList<>();
        int i = 0;
        
        // Add all intervals that end before new interval starts
        while (i < intervals.size() && intervals.get(i).end < newInterval.start) {
            result.add(intervals.get(i));
            i++;
        }
        
        // Merge overlapping intervals
        while (i < intervals.size() && intervals.get(i).start <= newInterval.end) {
            newInterval.start = Math.min(newInterval.start, intervals.get(i).start);
            newInterval.end = Math.max(newInterval.end, intervals.get(i).end);
            i++;
        }
        result.add(newInterval);
        
        // Add remaining intervals
        while (i < intervals.size()) {
            result.add(intervals.get(i));
            i++;
        }
        
        return result;
    }
    
    /**
     * Problem: Interval Intersection
     * Find all intersections between two interval lists
     * Time Complexity: O(n + m)
     * Space Complexity: O(min(n, m))
     */
    public static List<Interval> intervalIntersection(List<Interval> list1, List<Interval> list2) {
        List<Interval> result = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < list1.size() && j < list2.size()) {
            Interval a = list1.get(i);
            Interval b = list2.get(j);
            
            // Check if intervals intersect
            int interStart = Math.max(a.start, b.start);
            int interEnd = Math.min(a.end, b.end);
            
            if (interStart <= interEnd) {
                result.add(new Interval(interStart, interEnd));
            }
            
            // Move pointer of interval that ends first
            if (a.end <= b.end) {
                i++;
            } else {
                j++;
            }
        }
        
        return result;
    }
    
    /**
     * Problem: Meeting Rooms (Can attend all meetings?)
     * Check if person can attend all meetings (no overlaps)
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static boolean canAttendAllMeetings(List<Interval> meetings) {
        if (meetings.isEmpty()) return true;
        
        meetings.sort((a, b) -> Integer.compare(a.start, b.start));
        
        for (int i = 1; i < meetings.size(); i++) {
            // If current meeting starts before previous ends, conflict
            if (meetings.get(i).start < meetings.get(i - 1).end) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem: Meeting Rooms II (Minimum rooms needed)
     * Find minimum meeting rooms needed to schedule all meetings
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static int minMeetingRooms(List<Interval> meetings) {
        int n = meetings.size();
        int[] starts = new int[n];
        int[] ends = new int[n];
        
        for (int i = 0; i < n; i++) {
            starts[i] = meetings.get(i).start;
            ends[i] = meetings.get(i).end;
        }
        
        Arrays.sort(starts);
        Arrays.sort(ends);
        
        int rooms = 0;
        int maxRooms = 0;
        int endIdx = 0;
        
        for (int startTime : starts) {
            if (endIdx < n && startTime < ends[endIdx]) {
                // Meeting overlaps, need new room
                rooms++;
            } else if (endIdx < n) {
                // Meeting doesn't overlap, reuse room
                endIdx++;
            }
            maxRooms = Math.max(maxRooms, rooms);
        }
        
        return maxRooms;
    }
    
    /**
     * Problem: Range Module (Add/Query ranges)
     * Track a range of numbers and query if a range is covered
     * Time Complexity: O(n) for operations
     * Space Complexity: O(n)
     */
    public static class RangeModule {
        List<Interval> ranges;
        
        public RangeModule() {
            ranges = new ArrayList<>();
        }
        
        public void addRange(int left, int right) {
            List<Interval> newRanges = new ArrayList<>();
            int newStart = left, newEnd = right;
            
            for (Interval interval : ranges) {
                // No overlap
                if (interval.end < newStart || interval.start > newEnd) {
                    newRanges.add(interval);
                } else {
                    // Overlapping: merge
                    newStart = Math.min(newStart, interval.start);
                    newEnd = Math.max(newEnd, interval.end);
                }
            }
            
            newRanges.add(new Interval(newStart, newEnd));
            newRanges.sort((a, b) -> Integer.compare(a.start, b.start));
            ranges = newRanges;
        }
        
        public boolean queryRange(int left, int right) {
            for (Interval interval : ranges) {
                if (interval.start <= left && right <= interval.end) {
                    return true;
                }
            }
            return false;
        }
        
        public void removeRange(int left, int right) {
            List<Interval> newRanges = new ArrayList<>();
            
            for (Interval interval : ranges) {
                // No overlap with remove range
                if (interval.end <= left || interval.start >= right) {
                    newRanges.add(interval);
                } else {
                    // Overlapping: split interval
                    if (interval.start < left) {
                        newRanges.add(new Interval(interval.start, left));
                    }
                    if (interval.end > right) {
                        newRanges.add(new Interval(right, interval.end));
                    }
                }
            }
            
            ranges = newRanges;
        }
    }
    
    /**
     * Problem: Non-overlapping Intervals (Remove minimum)
     * Find minimum number of intervals to remove to make rest non-overlapping
     * Time Complexity: O(n log n)
     * Space Complexity: O(1)
     */
    public static int eraseOverlapIntervals(List<Interval> intervals) {
        if (intervals.isEmpty()) return 0;
        
        // Sort by end time (greedy: keep interval that ends earliest)
        intervals.sort((a, b) -> Integer.compare(a.end, b.end));
        
        int removed = 0;
        int lastEnd = intervals.get(0).end;
        
        for (int i = 1; i < intervals.size(); i++) {
            if (intervals.get(i).start < lastEnd) {
                // Overlapping: remove current
                removed++;
            } else {
                // No overlap: update lastEnd
                lastEnd = intervals.get(i).end;
            }
        }
        
        return removed;
    }
    
    public static void main(String[] args) {
        System.out.println("=== Overlapping Intervals Pattern ===\n");
        
        // Merge Intervals
        List<Interval> intervals1 = Arrays.asList(
            new Interval(1, 3),
            new Interval(2, 6),
            new Interval(8, 10),
            new Interval(15, 18)
        );
        System.out.println("Original intervals: " + intervals1);
        System.out.println("Merged: " + mergeIntervals(new ArrayList<>(intervals1)));
        
        // Insert Interval
        List<Interval> intervals2 = Arrays.asList(
            new Interval(1, 2),
            new Interval(3, 5),
            new Interval(6, 7)
        );
        System.out.println("\nOriginal intervals: " + intervals2);
        System.out.println("After inserting [2,5]: " + insertInterval(new ArrayList<>(intervals2), new Interval(2, 5)));
        
        // Interval Intersection
        List<Interval> list1 = Arrays.asList(
            new Interval(0, 2),
            new Interval(5, 10),
            new Interval(13, 23),
            new Interval(24, 25)
        );
        List<Interval> list2 = Arrays.asList(
            new Interval(1, 5),
            new Interval(8, 12),
            new Interval(15, 24),
            new Interval(25, 26)
        );
        System.out.println("\nList1: " + list1);
        System.out.println("List2: " + list2);
        System.out.println("Intersection: " + intervalIntersection(list1, list2));
        
        // Meeting Rooms
        List<Interval> meetings = Arrays.asList(
            new Interval(0, 30),
            new Interval(5, 10),
            new Interval(15, 20)
        );
        System.out.println("\nMeetings: " + meetings);
        System.out.println("Can attend all? " + canAttendAllMeetings(new ArrayList<>(meetings)));
        
        // Meeting Rooms II
        List<Interval> meetings2 = Arrays.asList(
            new Interval(0, 30),
            new Interval(5, 10),
            new Interval(15, 20),
            new Interval(8, 9)
        );
        System.out.println("Meetings: " + meetings2);
        System.out.println("Min rooms needed: " + minMeetingRooms(meetings2));
        
        // Range Module
        RangeModule rm = new RangeModule();
        rm.addRange(10, 20);
        rm.addRange(15, 25);
        System.out.println("\nRange Module - Added [10,20] and [15,25]");
        System.out.println("Query [11,13]? " + rm.queryRange(11, 13));
        System.out.println("Query [21,22]? " + rm.queryRange(21, 22));
    }
}
