/**
 * DSA Pattern: Sorting Algorithms
 * Topics: Bubble Sort, Selection Sort, Insertion Sort, Merge Sort, Quick Sort
 */

public class SortingAlgorithms {
    
    /**
     * Problem: Bubble Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    
    /**
     * Problem: Selection Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            int temp = arr[i];
            arr[i] = arr[minIdx];
            arr[minIdx] = temp;
        }
    }
    
    /**
     * Problem: Insertion Sort
     * Time Complexity: O(n^2)
     * Space Complexity: O(1)
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }
    
    /**
     * Problem: Merge Sort
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static void mergeSort(int[] arr) {
        if (arr.length <= 1) return;
        mergeSort(arr, 0, arr.length - 1);
    }
    
    private static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    
    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];
        
        System.arraycopy(arr, left, leftArr, 0, n1);
        System.arraycopy(arr, mid + 1, rightArr, 0, n2);
        
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArr[i] <= rightArr[j]) {
                arr[k++] = leftArr[i++];
            } else {
                arr[k++] = rightArr[j++];
            }
        }
        
        while (i < n1) {
            arr[k++] = leftArr[i++];
        }
        
        while (j < n2) {
            arr[k++] = rightArr[j++];
        }
    }
    
    /**
     * Problem: Quick Sort
     * Time Complexity: O(n log n) average, O(n^2) worst
     * Space Complexity: O(log n)
     */
    public static void quickSort(int[] arr) {
        if (arr.length == 0) return;
        quickSort(arr, 0, arr.length - 1);
    }
    
    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }
    
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    private static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        
        int[] bubbleArr = arr.clone();
        bubbleSort(bubbleArr);
        System.out.print("Bubble Sort: ");
        printArray(bubbleArr);
        
        int[] selectionArr = arr.clone();
        selectionSort(selectionArr);
        System.out.print("Selection Sort: ");
        printArray(selectionArr);
        
        int[] insertionArr = arr.clone();
        insertionSort(insertionArr);
        System.out.print("Insertion Sort: ");
        printArray(insertionArr);
        
        int[] mergeArr = arr.clone();
        mergeSort(mergeArr);
        System.out.print("Merge Sort: ");
        printArray(mergeArr);
        
        int[] quickArr = arr.clone();
        quickSort(quickArr);
        System.out.print("Quick Sort: ");
        printArray(quickArr);
    }
}
