/**
 * DSA Pattern: Math Algorithms
 * Topics: GCD, Prime numbers, Modular arithmetic, Number theory
 */

public class MathAlgorithms {
    
    /**
     * Problem: Greatest Common Divisor (GCD)
     * Time Complexity: O(log(min(a, b)))
     * Space Complexity: O(log(min(a, b)))
     */
    public static int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }
    
    /**
     * Problem: GCD using Iterative Euclidean Algorithm
     * Time Complexity: O(log(min(a, b)))
     * Space Complexity: O(1)
     */
    public static int gcdIterative(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Problem: Least Common Multiple (LCM)
     * Time Complexity: O(log(min(a, b)))
     * Space Complexity: O(1)
     */
    public static long lcm(int a, int b) {
        return (long) a * b / gcd(a, b);
    }
    
    /**
     * Problem: Check if Prime
     * Time Complexity: O(sqrt(n))
     * Space Complexity: O(1)
     */
    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Problem: Sieve of Eratosthenes - Find all primes up to n
     * Time Complexity: O(n log log n)
     * Space Complexity: O(n)
     */
    public static java.util.List<Integer> sieveOfEratosthenes(int n) {
        java.util.List<Integer> primes = new java.util.ArrayList<>();
        boolean[] isPrime = new boolean[n + 1];
        java.util.Arrays.fill(isPrime, true);
        isPrime[0] = isPrime[1] = false;
        
        for (int i = 2; i * i <= n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) primes.add(i);
        }
        
        return primes;
    }
    
    /**
     * Problem: Power with Modulo
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static long powerMod(long base, long exp, long mod) {
        long result = 1;
        base %= mod;
        
        while (exp > 0) {
            if (exp % 2 == 1) {
                result = (result * base) % mod;
            }
            base = (base * base) % mod;
            exp /= 2;
        }
        
        return result;
    }
    
    /**
     * Problem: Factorial
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * Problem: nCr (Combination)
     * Time Complexity: O(r)
     * Space Complexity: O(1)
     */
    public static long nCr(int n, int r) {
        if (r > n) return 0;
        if (r == 0 || r == n) return 1;
        
        long result = 1;
        for (int i = 0; i < r; i++) {
            result *= (n - i);
            result /= (i + 1);
        }
        
        return result;
    }
    
    /**
     * Problem: Fibonacci Number
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     */
    public static long fibonacci(int n) {
        if (n <= 1) return n;
        
        long prev = 0, curr = 1;
        for (int i = 2; i <= n; i++) {
            long next = prev + curr;
            prev = curr;
            curr = next;
        }
        
        return curr;
    }
    
    /**
     * Problem: Sum of first n natural numbers
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static long sumFirstN(int n) {
        return (long) n * (n + 1) / 2;
    }
    
    /**
     * Problem: Sum of squares of first n natural numbers
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public static long sumSquares(int n) {
        return (long) n * (n + 1) * (2 * n + 1) / 6;
    }
    
    /**
     * Problem: Digit Sum
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int digitSum(int n) {
        int sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }
    
    /**
     * Problem: Check Palindrome Number
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static boolean isPalindromeNumber(int x) {
        if (x < 0 || (x % 10 == 0 && x != 0)) return false;
        
        int reversed = 0;
        while (x > reversed) {
            reversed = reversed * 10 + x % 10;
            x /= 10;
        }
        
        return x == reversed || x == reversed / 10;
    }
    
    /**
     * Problem: Modular Multiplicative Inverse
     * Time Complexity: O(log m)
     * Space Complexity: O(1)
     */
    public static long modInverse(long a, long m) {
        return powerMod(a, m - 2, m);
    }
    
    /**
     * Problem: Count Digits
     * Time Complexity: O(log n)
     * Space Complexity: O(1)
     */
    public static int countDigits(int n) {
        if (n == 0) return 1;
        int count = 0;
        while (n > 0) {
            count++;
            n /= 10;
        }
        return count;
    }
    
    public static void main(String[] args) {
        System.out.println("GCD of 48 and 18: " + gcd(48, 18));
        System.out.println("LCM of 12 and 18: " + lcm(12, 18));
        
        System.out.println("Is 17 prime? " + isPrime(17));
        System.out.println("Is 18 prime? " + isPrime(18));
        
        System.out.println("Primes up to 30: " + sieveOfEratosthenes(30));
        
        System.out.println("2^10 mod 1000: " + powerMod(2, 10, 1000));
        
        System.out.println("5! = " + factorial(5));
        
        System.out.println("C(5,2) = " + nCr(5, 2));
        
        System.out.println("Fibonacci(10) = " + fibonacci(10));
        
        System.out.println("Sum of first 5 natural numbers: " + sumFirstN(5));
        System.out.println("Sum of squares of first 5 numbers: " + sumSquares(5));
        
        System.out.println("Digit sum of 12345: " + digitSum(12345));
        
        System.out.println("Is 121 palindrome? " + isPalindromeNumber(121));
        System.out.println("Is 123 palindrome? " + isPalindromeNumber(123));
    }
}
