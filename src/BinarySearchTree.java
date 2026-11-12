import java.util.ArrayList;
import java.util.List;

/**
 * Lab 1 - Ordered Inventory Index
 * BinarySearchTree: a generic BST keyed by a Comparable price, storing product data as values.
 *
 * Ordering invariant: for every node, keys in the left subtree are smaller and
 * keys in the right subtree are larger (all descendants, not just children).
 * Duplicate keys replace the stored value; they never add a second node.
 *
 * Height convention: empty tree = -1, leaf = 0.
 * Complexity: every operation is O(h); balanced typical O(log n); worst case O(n).
 * This class does NOT self-balance (no AVL / Red-Black) - that is a documented limitation.
 *
 * Return types (per the lab technical contract: "Return values in named traversal order"):
 *   inOrder / preOrder / postOrder -> List<V> (values in visit order)
 *   inOrderKeys / preOrderKeys / postOrderKeys -> List<K> (keys in visit order, for verification)
 *   rangeSearch(min, max)          -> List<V> (values in ascending key order)
 */
public class BinarySearchTree<K extends Comparable<K>, V> {

    private BSTNode<K, V> root;
    private int count;

    public BinarySearchTree() {
        this.root = null;
        this.count = 0;
    }

    /**
     * Insert value at key, or replace the value if the key already exists.
     * Rejects null keys and null values. Replacement does not increase size.
     */
    public void put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("put: key must not be null");
        }
        if (value == null) {
            throw new NullPointerException("put: value must not be null");
        }
        root = putRec(root, key, value);
    }

    // Follows comparisons from the root until an empty child link is found.
    // Equal key: replace the value in place (size unchanged).
    private BSTNode<K, V> putRec(BSTNode<K, V> node, K key, V value) {
        if (node == null) {
            count++;
            return new BSTNode<K, V>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = putRec(node.left, key, value);
        } else if (cmp > 0) {
            node.right = putRec(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    /**
     * Return the value stored at key, or null when absent.
     * Rejects a null key.
     */
    public V get(K key) {
        if (key == null) {
            throw new NullPointerException("get: key must not be null");
        }
        return getRec(root, key);
    }

    private V getRec(BSTNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node.value;
        } else if (cmp < 0) {
            return getRec(node.left, key);
        } else {
            return getRec(node.right, key);
        }
    }

    /**
     * Inclusive range query [min, max]: return values whose keys satisfy
     * min <= key <= max, in ascending key order.
     * An inverted range (min > max) returns an empty list.
     * Irrelevant subtrees are pruned.
     */
    public List<V> rangeSearch(K min, K max) {
        if (min == null || max == null) {
            throw new NullPointerException("rangeSearch: bounds must not be null");
        }
        List<V> result = new ArrayList<V>();
        if (min.compareTo(max) > 0) {
            return result;
        }
        rangeRec(root, min, max, result);
        return result;
    }

    // In-order walk with pruning:
    //   - skip the left subtree when the current key is already below min
    //   - collect the value when the current key lies inside [min, max]
    //   - skip the right subtree when the current key is already above max
    private void rangeRec(BSTNode<K, V> node, K min, K max, List<V> result) {
        if (node == null) {
            return;
        }
        if (min.compareTo(node.key) < 0) {
            rangeRec(node.left, min, max, result);
        }
        if (min.compareTo(node.key) <= 0 && max.compareTo(node.key) >= 0) {
            result.add(node.value);
        }
        if (max.compareTo(node.key) > 0) {
            rangeRec(node.right, min, max, result);
        }
    }

    /**
     * Delete key from the tree. Returns true when the key was found and removed
     * (size decreases exactly once), false when the key is absent.
     * Handles leaf, one-child, two-child (in-order successor), and root deletion.
     */
    public boolean delete(K key) {
        if (key == null) {
            throw new NullPointerException("delete: key must not be null");
        }
        boolean[] removed = { false };
        root = deleteRec(root, key, removed);
        if (removed[0]) {
            count--;
        }
        return removed[0];
    }

    // Recursive delete: walks down the comparison path, repairs the link on the way back.
    private BSTNode<K, V> deleteRec(BSTNode<K, V> node, K key, boolean[] removed) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteRec(node.left, key, removed);
        } else if (cmp > 0) {
            node.right = deleteRec(node.right, key, removed);
        } else {
            // Found the node to delete.
            removed[0] = true;
            // Case 1: leaf (both children null) and case 2: one child.
            // Returning the non-null child (or null) promotes it into this slot.
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            // Case 3: two children. Replace this node's content with its in-order
            // successor (the minimum key of the right subtree), then remove the
            // successor from its original position.
            BSTNode<K, V> successor = minNode(node.right);
            node.key = successor.key;
            node.value = successor.value;
            // deleteMin unlinks the successor. If the successor had a right child,
            // that child takes the successor's former slot (reattached, not lost).
            node.right = deleteMin(node.right);
        }
        return node;
    }

    // Smallest node in a subtree: follow left links until null.
    private BSTNode<K, V> minNode(BSTNode<K, V> node) {
        BSTNode<K, V> current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    // Remove the minimum node of a subtree, promoting its right child into its place.
    private BSTNode<K, V> deleteMin(BSTNode<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return node;
    }

    /** Values in left - node - right order (ascending key order for a valid BST). */
    public List<V> inOrder() {
        List<V> result = new ArrayList<V>();
        inOrderRec(root, result, null);
        return result;
    }

    /** Keys in left - node - right order (ascending for a valid BST). */
    public List<K> inOrderKeys() {
        List<K> result = new ArrayList<K>();
        inOrderRec(root, null, result);
        return result;
    }

    // Collects values into vals and/or keys into keys (either list may be null).
    private void inOrderRec(BSTNode<K, V> node, List<V> vals, List<K> keys) {
        if (node == null) {
            return;
        }
        inOrderRec(node.left, vals, keys);
        collect(node, vals, keys);
        inOrderRec(node.right, vals, keys);
    }

    /** Values in node - left - right order. */
    public List<V> preOrder() {
        List<V> result = new ArrayList<V>();
        preOrderRec(root, result, null);
        return result;
    }

    /** Keys in node - left - right order. */
    public List<K> preOrderKeys() {
        List<K> result = new ArrayList<K>();
        preOrderRec(root, null, result);
        return result;
    }

    private void preOrderRec(BSTNode<K, V> node, List<V> vals, List<K> keys) {
        if (node == null) {
            return;
        }
        collect(node, vals, keys);
        preOrderRec(node.left, vals, keys);
        preOrderRec(node.right, vals, keys);
    }

    /** Values in left - right - node order. */
    public List<V> postOrder() {
        List<V> result = new ArrayList<V>();
        postOrderRec(root, result, null);
        return result;
    }

    /** Keys in left - right - node order. */
    public List<K> postOrderKeys() {
        List<K> result = new ArrayList<K>();
        postOrderRec(root, null, result);
        return result;
    }

    private void postOrderRec(BSTNode<K, V> node, List<V> vals, List<K> keys) {
        if (node == null) {
            return;
        }
        postOrderRec(node.left, vals, keys);
        postOrderRec(node.right, vals, keys);
        collect(node, vals, keys);
    }

    private void collect(BSTNode<K, V> node, List<V> vals, List<K> keys) {
        if (vals != null) {
            vals.add(node.value);
        }
        if (keys != null) {
            keys.add(node.key);
        }
    }

    /**
     * Height in edges: empty tree = -1, leaf = 0.
     */
    public int height() {
        return heightRec(root);
    }

    private int heightRec(BSTNode<K, V> node) {
        if (node == null) {
            return -1;
        }
        int leftHeight = heightRec(node.left);
        int rightHeight = heightRec(node.right);
        return 1 + Math.max(leftHeight, rightHeight);
    }

    /** Number of nodes in the tree. */
    public int size() {
        return count;
    }

    // Visible for tests that need structural access.
    BSTNode<K, V> getRoot() {
        return root;
    }

    int getCount() {
        return count;
    }
}
