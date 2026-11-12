import java.util.ArrayList;
import java.util.List;

/**
 * AUDIT evidence for the complexity table (put/get/delete O(h), range O(h + k), traversal O(n)).
 * Compares a balanced-order insertion against sorted insertion of the same keys and
 * reports height and comparison counts. Counts every compareTo call through a wrapper key,
 * so the src/ files are not instrumented or modified.
 */
public class ComplexityAudit {

    /** Key wrapper that counts every comparison. */
    static final class Key implements Comparable<Key> {
        static long compares = 0;
        final int v;
        Key(int v) { this.v = v; }
        public int compareTo(Key o) { compares++; return Integer.compare(v, o.v); }
    }

    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== ComplexityAudit (balanced vs sorted insertion) ===");
        for (int n : new int[] { 15, 127, 1023 }) {
            run(n);
        }
        System.out.println();
        rangeAudit();
        if (failed > 0) {
            throw new AssertionError(failed + " audit check(s) failed");
        }
        System.out.println("ComplexityAudit: all checks passed");
    }

    // Median-first order yields a perfectly balanced tree when n = 2^k - 1.
    private static void balancedOrder(int lo, int hi, List<Integer> out) {
        if (lo > hi) return;
        int mid = (lo + hi) >>> 1;
        out.add(mid);
        balancedOrder(lo, mid - 1, out);
        balancedOrder(mid + 1, hi, out);
    }

    private static void run(int n) {
        List<Integer> balanced = new ArrayList<Integer>();
        balancedOrder(1, n, balanced);

        BinarySearchTree<Key, String> bal = new BinarySearchTree<Key, String>();
        BinarySearchTree<Key, String> srt = new BinarySearchTree<Key, String>();

        Key.compares = 0;
        for (int k : balanced) bal.put(new Key(k), "v" + k);
        long balBuild = Key.compares;
        Key.compares = 0;
        for (int k = 1; k <= n; k++) srt.put(new Key(k), "v" + k);
        long srtBuild = Key.compares;

        // Lookup path for the largest key (deepest node in the sorted chain).
        Key.compares = 0;
        bal.get(new Key(n));
        long balLookup = Key.compares;
        Key.compares = 0;
        srt.get(new Key(n));
        long srtLookup = Key.compares;

        int log2 = 31 - Integer.numberOfLeadingZeros(n + 1) - 1; // n = 2^k - 1 -> height k-1
        System.out.println("n = " + n);
        System.out.printf("  balanced order: height=%d  build compares=%d  get(max) compares=%d%n",
                bal.height(), balBuild, balLookup);
        System.out.printf("  sorted order:   height=%d  build compares=%d  get(max) compares=%d%n",
                srt.height(), srtBuild, srtLookup);

        expect("balanced height == log2(n+1)-1 (" + log2 + ")", bal.height() == log2);
        expect("sorted height == n-1", srt.height() == n - 1);
        expect("balanced get(max) compares == height+1", balLookup == bal.height() + 1);
        expect("sorted get(max) compares == n", srtLookup == n);
        expect("sorted build compares == n(n-1)/2", srtBuild == (long) n * (n - 1) / 2);
        expect("both trees hold n keys", bal.size() == n && srt.size() == n);
    }

    // rangeSearch visits O(h + k) nodes when pruned; count comparisons as visit evidence.
    private static void rangeAudit() {
        int n = 1023;
        List<Integer> order = new ArrayList<Integer>();
        balancedOrder(1, n, order);
        BinarySearchTree<Key, String> t = new BinarySearchTree<Key, String>();
        for (int k : order) t.put(new Key(k), "v" + k);
        System.out.println("rangeSearch on balanced tree, n = " + n + ", height = " + t.height());
        int[][] ranges = { { 500, 500 }, { 500, 509 }, { 500, 599 }, { 1, 1023 } };
        for (int[] r : ranges) {
            Key.compares = 0;
            int k = t.rangeSearch(new Key(r[0]), new Key(r[1])).size();
            long c = Key.compares;
            System.out.printf("  range [%d, %d]: k=%d results, compares=%d%n", r[0], r[1], k, c);
        }
        Key.compares = 0;
        int k = t.rangeSearch(new Key(500), new Key(509)).size();
        long small = Key.compares;
        Key.compares = 0;
        t.rangeSearch(new Key(1), new Key(1023));
        long full = Key.compares;
        expect("small range does far fewer compares than full scan (pruning)", small < full / 10);
        expect("small range result count k == 10", k == 10);
    }

    private static void expect(String what, boolean ok) {
        System.out.println((ok ? "  PASS: " : "  FAIL: ") + what);
        if (!ok) failed++;
    }
}
