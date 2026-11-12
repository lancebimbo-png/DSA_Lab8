import java.util.Arrays;
import java.util.List;

/**
 * Student-authored test (TEST checkpoint - written before any BST algorithm Build).
 *
 * Primary case (lab-prescribed):
 *   Root deletion where the in-order successor has a right child.
 *
 *   Insert: 40, 20, 70, 10, 30, 55, 85, 60
 *   Delete root 40.
 *   Successor is 55; 60 (55.right) must reattach under 70.
 *
 * Also covers: duplicate-key replacement without size increase,
 * empty-tree behavior, and inverted rangeSearch.
 *
 * This file does not weaken or modify any acceptance tests.
 */
public class StudentBSTTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== StudentBSTTest (student-authored) ===");
        System.out.println();

        testRootDeleteSuccessorWithRightChild();
        testDuplicateReplacementDoesNotIncreaseSize();
        testEmptyTreeBehavior();
        testInvertedRangeIsEmpty();
        testInclusiveRangeBoundaries();

        System.out.println();
        System.out.println("StudentBSTTest results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            throw new AssertionError(failed + " student test(s) failed");
        }
    }

    /**
     * Why this case: the professor's checklist requires "successor right child."
     * Two-child root delete + successor (55) that has a right child (60) is the
     * classic pointer-repair bug: dropping 60 when unlinking 55.
     */
    private static void testRootDeleteSuccessorWithRightChild() {
        System.out.println("-- Root delete with successor that has a right child --");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();

        try {
            tree.put(40, "p40");
            tree.put(20, "p20");
            tree.put(70, "p70");
            tree.put(10, "p10");
            tree.put(30, "p30");
            tree.put(55, "p55");
            tree.put(85, "p85");
            tree.put(60, "p60");
        } catch (UnsupportedOperationException e) {
            check("put implemented (build insertion sequence)", false);
            noteBlocked(e);
            return;
        }

        check("size after 8 inserts is 8", safeSize(tree) == 8);

        boolean deleted;
        try {
            deleted = tree.delete(40);
        } catch (UnsupportedOperationException e) {
            check("delete implemented", false);
            noteBlocked(e);
            return;
        }
        check("delete(40) returns true", deleted);
        check("size becomes 7", safeSize(tree) == 7);

        List<Integer> inOrder = null;
        try {
            inOrder = tree.inOrder();
        } catch (UnsupportedOperationException e) {
            check("inOrder implemented", false);
            noteBlocked(e);
            return;
        }
        check("inOrder keys are [10, 20, 30, 55, 60, 70, 85]",
                inOrder != null
                        && inOrder.equals(Arrays.asList(10, 20, 30, 55, 60, 70, 85)));

        String v55 = null;
        String v60 = null;
        try {
            v55 = tree.get(55);
            v60 = tree.get(60);
        } catch (UnsupportedOperationException e) {
            check("get implemented after delete", false);
            noteBlocked(e);
            return;
        }
        check("get(55) returns its stored value", "p55".equals(v55));
        check("get(60) returns its stored value (reattached successor right child)",
                "p60".equals(v60));
        check("get(40) returns null after delete", tree.get(40) == null);

        int height = -999;
        try {
            height = tree.height();
        } catch (UnsupportedOperationException e) {
            check("height implemented", false);
            noteBlocked(e);
            return;
        }
        // After delete(40): root 55, longest path 55-20-10 or 55-70-60 = 2 edges.
        // Height convention: empty = -1, leaf = 0 → this tree height is 2 (was wrongly expected as 3).
        check("height is 2", height == 2);

        boolean secondDelete;
        try {
            secondDelete = tree.delete(40);
        } catch (UnsupportedOperationException e) {
            check("delete implemented (second call)", false);
            noteBlocked(e);
            return;
        }
        check("deleting 40 again returns false", !secondDelete);
        check("size still 7 after failed delete", safeSize(tree) == 7);
    }

    private static void testDuplicateReplacementDoesNotIncreaseSize() {
        System.out.println("-- Duplicate key replaces value without size increase --");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            tree.put(30, "first");
            tree.put(30, "second");
        } catch (UnsupportedOperationException e) {
            check("put implemented (duplicate scenario)", false);
            noteBlocked(e);
            return;
        }
        check("size remains 1 after duplicate put", safeSize(tree) == 1);
        check("get returns replacement value", "second".equals(safeGet(tree, 30)));
    }

    private static void testEmptyTreeBehavior() {
        System.out.println("-- Empty tree behavior --");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            check("empty size is 0", tree.size() == 0);
            check("empty height is -1", tree.height() == -1);
            check("empty get returns null", tree.get(1) == null);
            check("empty delete returns false", !tree.delete(1));
            check("empty inOrder is empty", isEmpty(tree.inOrder()));
            check("empty preOrder is empty", isEmpty(tree.preOrder()));
            check("empty postOrder is empty", isEmpty(tree.postOrder()));
            check("empty rangeSearch is empty", isEmpty(tree.rangeSearch(0, 100)));
        } catch (UnsupportedOperationException e) {
            check("empty-tree operations implemented", false);
            noteBlocked(e);
        }
    }

    private static void testInvertedRangeIsEmpty() {
        System.out.println("-- Inverted range returns empty --");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            tree.put(10, "a");
            tree.put(20, "b");
            tree.put(30, "c");
        } catch (UnsupportedOperationException e) {
            check("put implemented (range setup)", false);
            noteBlocked(e);
            return;
        }
        try {
            check("rangeSearch(60, 25) is empty", isEmpty(tree.rangeSearch(60, 25)));
        } catch (UnsupportedOperationException e) {
            check("rangeSearch implemented", false);
            noteBlocked(e);
        }
    }

    private static void testInclusiveRangeBoundaries() {
        System.out.println("-- Inclusive range boundaries (fresh tree) --");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            tree.put(40, "p40");
            tree.put(20, "p20");
            tree.put(70, "p70");
            tree.put(10, "p10");
            tree.put(30, "p30");
            tree.put(55, "p55");
            tree.put(85, "p85");
            tree.put(60, "p60");
        } catch (UnsupportedOperationException e) {
            check("put implemented (range tree setup)", false);
            noteBlocked(e);
            return;
        }
        try {
            // Insert-only tree: keys in [25, 60] are 30, 40, 55, 60
            List<String> values = tree.rangeSearch(25, 60);
            check("rangeSearch(25, 60) on insert-only tree has 4 values",
                    values != null && values.size() == 4);
            check("rangeSearch(25, 60) values match ascending keys 30,40,55,60",
                    values != null
                            && values.equals(Arrays.asList("p30", "p40", "p55", "p60")));
            check("rangeSearch(10, 10) includes exact boundary key 10",
                    values != null && tree.rangeSearch(10, 10).equals(Arrays.asList("p10")));
        } catch (UnsupportedOperationException e) {
            check("rangeSearch implemented", false);
            noteBlocked(e);
        }
    }

    private static int safeSize(BinarySearchTree<Integer, String> tree) {
        try {
            return tree.size();
        } catch (UnsupportedOperationException e) {
            return -1;
        }
    }

    private static String safeGet(BinarySearchTree<Integer, String> tree, Integer key) {
        try {
            return tree.get(key);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    private static boolean isEmpty(List<?> list) {
        return list != null && list.isEmpty();
    }

    private static void noteBlocked(UnsupportedOperationException e) {
        System.out.println("       blocked by: " + e.getMessage());
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + description);
        } else {
            failed++;
            System.out.println("FAIL: " + description);
        }
    }
}
