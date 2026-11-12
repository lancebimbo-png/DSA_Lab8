/**
 * Lab 1 - Ordered Inventory Index
 * BSTNode: a single node in the binary search tree.
 *
 * Contract: comparable key, value, left reference, right reference.
 */
public class BSTNode<K, V> {

    K key;
    V value;
    BSTNode<K, V> left;
    BSTNode<K, V> right;

    public BSTNode(K key, V value) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
    }
}
