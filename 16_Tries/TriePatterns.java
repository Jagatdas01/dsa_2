/**
 * DSA Pattern: Trie (Prefix Tree)
 * Topics: Word insertion, search, prefix matching
 */

import java.util.*;

public class TriePatterns {
    
    static class TrieNode {
        Map<Character, TrieNode> children;
        boolean isEndOfWord;
        
        TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
        }
    }
    
    static class Trie {
        private TrieNode root;
        
        /**
         * Initialize Trie
         */
        public Trie() {
            root = new TrieNode();
        }
        
        /**
         * Insert a word into the trie
         * 
         * Input: trie, insert word "apple"
         * Expected: Word stored in trie with path a->p->p->l->e, marked as end
         * 
         * Time Complexity: O(m) where m is length of word
         * Space Complexity: O(m)
         */
        public void insert(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
            }
            node.isEndOfWord = true;
        }
        
        /**
         * Search for exact word in trie
         * 
         * Input: trie with ["apple", "app"], search "apple"
         * Expected Output: true (exact word exists)
         * 
         * Input: trie with ["apple", "app"], search "app"
         * Expected Output: true
         * 
         * Time Complexity: O(m) where m is length of word
         * Space Complexity: O(1)
         */
        public boolean search(String word) {
            TrieNode node = find(word);
            return node != null && node.isEndOfWord;
        }
        
        /**
         * Check if word prefix exists in trie
         * 
     * Input: trie with ["apple"], startsWith "app"
         * Expected Output: true (prefix exists)
         * 
         * Input: trie with ["apple"], startsWith "banana"
         * Expected Output: false (prefix not found)
         * 
         * Time Complexity: O(m) where m is prefix length
         * Space Complexity: O(1)
         */
        public boolean startsWith(String prefix) {
            return find(prefix) != null;
        }
        
        private TrieNode find(String prefix) {
            TrieNode node = root;
            for (char c : prefix.toCharArray()) {
                node = node.children.get(c);
                if (node == null) return null;
            }
            return node;
        }
        
        /**
         * Delete a word from trie
         * 
         * Input: trie with ["apple", "app"], delete "apple"
         * Expected: Only "app" remains; path e is removed
         * 
         * Input: trie with ["apple"], delete "app"
         * Expected: "apple" still exists (delete only removes if exact match)
         * 
         * Time Complexity: O(m) where m is word length
         * Space Complexity: O(1)
         */
        public void delete(String word) {
            deleteHelper(root, word, 0);
        }
        
        private boolean deleteHelper(TrieNode node, String word, int index) {
            if (index == word.length()) {
                if (!node.isEndOfWord) return false;
                node.isEndOfWord = false;
                return node.children.isEmpty();
            }
            
            char c = word.charAt(index);
            TrieNode childNode = node.children.get(c);
            if (childNode == null) return false;
            
            boolean shouldDeleteChild = deleteHelper(childNode, word, index + 1);
            
            if (shouldDeleteChild) {
                node.children.remove(c);
                return node.children.isEmpty() && !node.isEndOfWord;
            }
            
            return false;
        }
        
        /**
         * Get all words with given prefix
         * Time Complexity: O(n) where n is number of nodes
         * Space Complexity: O(n)
         */
        public List<String> wordsWithPrefix(String prefix) {
            List<String> result = new ArrayList<>();
            TrieNode node = find(prefix);
            
            if (node != null) {
                dfs(node, prefix, result);
            }
            
            return result;
        }
        
        private void dfs(TrieNode node, String current, List<String> result) {
            if (node.isEndOfWord) {
                result.add(current);
            }
            
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                dfs(entry.getValue(), current + entry.getKey(), result);
            }
        }
    }
    
    /**
     * Problem: Implement Word Dictionary
     */
    static class WordDictionary {
        private TrieNode root;
        
        public WordDictionary() {
            root = new TrieNode();
        }
        
        public void addWord(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
            }
            node.isEndOfWord = true;
        }
        
        /**
         * Search with wildcard support (. matches any character)
         * Time Complexity: O(m) average, O(26^m) worst case
         */
        public boolean search(String word) {
            return searchHelper(root, word, 0);
        }
        
        private boolean searchHelper(TrieNode node, String word, int index) {
            if (index == word.length()) {
                return node.isEndOfWord;
            }
            
            char c = word.charAt(index);
            
            if (c == '.') {
                for (TrieNode child : node.children.values()) {
                    if (searchHelper(child, word, index + 1)) {
                        return true;
                    }
                }
            } else {
                TrieNode child = node.children.get(c);
                if (child != null) {
                    return searchHelper(child, word, index + 1);
                }
            }
            
            return false;
        }
    }
    
    /**
     * Problem: Longest Word in Dictionary
     * Time Complexity: O(n * m) where n is number of words, m is average length
     * Space Complexity: O(n * m)
     */
    public static String longestWord(String[] words) {
        Trie trie = new Trie();
        Arrays.sort(words, (a, b) -> {
            if (a.length() != b.length()) {
                return b.length() - a.length();
            }
            return a.compareTo(b);
        });
        
        String longest = "";
        for (String word : words) {
            if (canBuild(trie, word)) {
                return word;
            }
        }
        
        return longest;
    }
    
    private static boolean canBuild(Trie trie, String word) {
        trie.insert(word);
        TrieNode node = trie.root;
        
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false;
            if (!node.isEndOfWord) return false;
        }
        
        return true;
    }
    
    public static void main(String[] args) {
        // Test basic Trie operations
        Trie trie = new Trie();
        trie.insert("apple");
        trie.insert("app");
        trie.insert("apricot");
        trie.insert("application");
        
        System.out.println("Search 'apple': " + trie.search("apple"));
        System.out.println("Search 'app': " + trie.search("app"));
        System.out.println("Search 'ap': " + trie.search("ap"));
        System.out.println("StartsWith 'app': " + trie.startsWith("app"));
        System.out.println("StartsWith 'appl': " + trie.startsWith("appl"));
        
        System.out.println("Words with prefix 'app': " + trie.wordsWithPrefix("app"));
        
        // Test WordDictionary with wildcard
        WordDictionary wd = new WordDictionary();
        wd.addWord("bad");
        wd.addWord("dad");
        wd.addWord("mad");
        
        System.out.println("WordDictionary search 'bad': " + wd.search("bad"));
        System.out.println("WordDictionary search 'dad': " + wd.search("dad"));
        System.out.println("WordDictionary search '.ad': " + wd.search(".ad"));
        System.out.println("WordDictionary search 'b..': " + wd.search("b.."));
        
        // Test longest word
        String[] words = {"a", "ab", "abc", "abcd", "abcde"};
        System.out.println("Longest word: " + longestWord(words));
    }
}
