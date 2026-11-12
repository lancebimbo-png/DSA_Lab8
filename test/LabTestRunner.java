import java.util.Arrays;
import java.util.List;

/**
 * Agent-authored acceptance test runner (NOT an instructor-provided file).
 *
 * Cases are derived from the professor's acceptance-test matrix in
 * 01_BST_Ordered_Inventory_Lab.docx:
 *   - Empty/missing
 *   - Ordering (traversals + duplicate replacement)
 *   - Range (inclusive endpoints, empty result, inverted bounds)
 *   - Deletion (leaf, one child, two children, root, successor right child)
 *   - Degenerate (sorted insert -> chain height)
 *
 * Workflow: this runner is executed at the TEST checkpoint before BUILD.
 * Unfinished skeleton methods throw UnsupportedOperationException and must fail.
 */
public class LabTestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== LabTestRunner (agent-authored acceptance suite) ===");
        System.out.println("Source: acceptance-test matrix in 01_BST_Ordered_Inventory_Lab.docx");
        System.out.println();

        testEmptyAndMissing();
        testOrderingAndTraversals();
        testDuplicateReplacement();
        testRangeInclusiveEmptyInverted();
        testDeletionLeaf();
        testDeletionOneChild();
        testDeleteOnlyNode();
        testDeleteRightChildOnly();
        testDeletionTwoChildren();
        testDeletionRootSuccessorRightChild();
        testDegenerateSortedInsertChain();

        System.out.println();
        System.out.println("LabTestRunner results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            throw new AssertionError(failed + " acceptance test(s) failed");
        }
    }

    // --- Matrix: Empty/missing ---

    private static void testEmptyAndMissing() {
        section("Empty/missing");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            check("empty size is 0", tree.size() == 0);
            check("empty height is -1", tree.height() == -1);
            check("empty get returns null", tree.get(42) == null);
            check("empty delete returns false", !tree.delete(42));
            check("empty inOrder empty", tree.inOrder() != null && tree.inOrder().isEmpty());
            check("empty rangeSearch empty",
                    tree.rangeSearch(0, 100) != null && tree.rangeSearch(0, 100).isEmpty());
        } catch (UnsupportedOperationException e) {
            check("empty-tree API implemented", false);
            blocked(e);
            return;
        }

        try {
            tree.put(10, "only");
            tree.put(20, "missing-key-setup");
        } catch (UnsupportedOperationException e) {
            check("put implemented (missing-key setup)", false);
            blocked(e);
            return;
        }
        try {
            check("missing get returns null", tree.get(99) == null);
            check("missing delete returns false", !tree.delete(99));
            check("size unchanged after missing delete", tree.size() == 2);
        } catch (UnsupportedOperationException e) {
            check("get/delete implemented", false);
            blocked(e);
        }
    }

    // --- Matrix: Ordering ---

    private static void testOrderingAndTraversals() {
        section("Ordering (traversals)");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            // Same shape family as the lab insert trace
            tree.put(50, "p50");
            tree.put(30, "p30");
            tree.put(70, "p70");
            tree.put(20, "p20");
            tree.put(40, "p40");
        } catch (UnsupportedOperationException e) {
            check("put implemented (traversal setup)", false);
            blocked(e);
            return;
        }
        try {
            check("inOrder is ascending keys",
                    tree.inOrder().equals(Arrays.asList(20, 30, 40, 50, 70)));
            check("preOrder is node-left-right",
                    tree.preOrder().equals(Arrays.asList(50, 30, 20, 40, 70)));
            check("postOrder is left-right-node",
                    tree.postOrder().equals(Arrays.asList(20, 40, 30, 70, 50)));
            check("size is 5", tree.size() == 5);
            check("height of this tree is 2", tree.height() == 2);
        } catch (UnsupportedOperationException e) {
            check("traversals/height implemented", false);
            blocked(e);
        }
    }

    private static void testDuplicateReplacement() {
        section("Duplicate replacement");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            tree.put(5, "v1");
            tree.put(5, "v2");
            tree.put(5, "v3");
        } catch (UnsupportedOperationException e) {
            check("put implemented (duplicate setup)", false);
            blocked(e);
            return;
        }
        try {
            check("size remains 1", tree.size() == 1);
            check("get returns latest value", "v3".equals(tree.get(5)));
            check("inOrder has single key", tree.inOrder().equals(Arrays.asList(5)));
        } catch (UnsupportedOperationException e) {
            check("get/inOrder after duplicate put", false);
            blocked(e);
        }
    }

    // --- Matrix: Range ---

    private static void testRangeInclusiveEmptyInverted() {
        section("Range (inclusive, empty, inverted)");
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
            check("put implemented (range setup)", false);
            blocked(e);
            return;
        }
        try {
            List<String> inRange = tree.rangeSearch(25, 60);
            check("rangeSearch(25, 60) returns values for keys 30,40,55,60",
                    inRange != null
                            && inRange.equals(Arrays.asList("p30", "p40", "p55", "p60")));
            check("rangeSearch is inclusive on both endpoints",
                    tree.rangeSearch(10, 85) != null && tree.rangeSearch(10, 85).size() == 8);
            check("rangeSearch with no matches is empty",
                    tree.rangeSearch(61, 69) != null && tree.rangeSearch(61, 69).isEmpty());
            check("inverted rangeSearch(60, 25) is empty",
                    tree.rangeSearch(60, 25) != null && tree.rangeSearch(60, 25).isEmpty());
            check("single-key rangeSearch(40, 40) returns one value",
                    tree.rangeSearch(40, 40) != null
                            && tree.rangeSearch(40, 40).equals(Arrays.asList("p40")));
        } catch (UnsupportedOperationException e) {
            check("rangeSearch implemented", false);
            blocked(e);
        }
    }

    // --- Matrix: Deletion cases ---

    private static void testDeletionLeaf() {
        section("Deletion - leaf");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            buildLabInsertSequence(tree);
        } catch (UnsupportedOperationException e) {
            check("put implemented (leaf-delete setup)", false);
            blocked(e);
            return;
        }
        try {
            check("delete leaf 10 returns true", tree.delete(10));
            check("size is 7 after deleting leaf 10", tree.size() == 7);
            check("inOrder without 10",
                    tree.inOrder().equals(Arrays.asList(20, 30, 40, 55, 60, 70, 85)));
            check("get(10) is null", tree.get(10) == null);
        } catch (UnsupportedOperationException e) {
            check("delete implemented (leaf)", false);
            blocked(e);
        }
    }

    private static void testDeletionOneChild() {
        section("Deletion - one child");
        // One-child case: 50, 30, 20 only — delete 30 promotes its left child 20.
        // (Post-audit: removed an unused duplicate tree setup that no assertion read.)
        BinarySearchTree<Integer, String> oneChild = new BinarySearchTree<Integer, String>();
        try {
            oneChild.put(50, "p50");
            oneChild.put(30, "p30");
            oneChild.put(20, "p20");
            check("delete node with only left child returns true", oneChild.delete(30));
            check("size is 2", oneChild.size() == 2);
            check("inOrder after promoting left child",
                    oneChild.inOrder().equals(Arrays.asList(20, 50)));
            check("get(20) still works", "p20".equals(oneChild.get(20)));
            check("height after promote is 1", oneChild.height() == 1);
        } catch (UnsupportedOperationException e) {
            check("delete implemented (one child)", false);
            blocked(e);
        }
    }

    /**
     * Post-audit coverage gap A: delete the only node in the tree.
     * A single node is a leaf; deletion must empty the tree completely
     * and restore the empty-tree state (size 0, height -1).
     */
    private static void testDeleteOnlyNode() {
        section("Deletion - only node");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        tree.put(42, "solo");
        check("delete only node returns true", tree.delete(42));
        check("size is 0 after deleting only node", tree.size() == 0);
        check("height is -1 after deleting only node", tree.height() == -1);
        check("get returns null after deleting only node", tree.get(42) == null);
        check("second delete of only node returns false", !tree.delete(42));
    }

    /**
     * Post-audit coverage gap B: delete a parent that has only a right child
     * (no left child). The right child must be promoted into the parent's slot
     * and remain searchable with its original value.
     */
    private static void testDeleteRightChildOnly() {
        section("Deletion - one child (right only)");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        tree.put(50, "p50");
        tree.put(70, "p70"); // 50 has no left child and one right child
        check("delete parent with only right child returns true", tree.delete(50));
        check("right child remains searchable with original value", "p70".equals(tree.get(70)));
        check("size is 1 after deleting parent", tree.size() == 1);
        check("inOrder is [70] after promoting right child",
                tree.inOrder().equals(Arrays.asList(70)));
        check("deleted parent can no longer be found", tree.get(50) == null);
    }

    private static void testDeletionTwoChildren() {
        section("Deletion - two children (non-root)");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            buildLabInsertSequence(tree);
            // Delete 70 (two children: 55 and 85); successor 55 has right child 60
            check("delete(70) returns true", tree.delete(70));
            check("size is 7", tree.size() == 7);
            check("inOrder without 70",
                    tree.inOrder().equals(Arrays.asList(10, 20, 30, 40, 55, 60, 85)));
            check("get(55) works", "p55".equals(tree.get(55)));
            check("get(60) works after successor unlink", "p60".equals(tree.get(60)));
            check("get(70) is null", tree.get(70) == null);
        } catch (UnsupportedOperationException e) {
            check("delete implemented (two children)", false);
            blocked(e);
        }
    }

    private static void testDeletionRootSuccessorRightChild() {
        section("Deletion - root + successor right child");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            buildLabInsertSequence(tree);
            check("delete root 40 returns true", tree.delete(40));
            check("size is 7", tree.size() == 7);
            check("inOrder after root delete",
                    tree.inOrder().equals(Arrays.asList(10, 20, 30, 55, 60, 70, 85)));
            check("successor value get(55)", "p55".equals(tree.get(55)));
            check("successor right child get(60) preserved", "p60".equals(tree.get(60)));
            check("old root get(40) null", tree.get(40) == null);
            // After root delete: longest path has 2 edges (e.g. 55→20→10) → height 2, not 3.
            check("height after root delete is 2", tree.height() == 2);
            check("second delete(40) false", !tree.delete(40));
        } catch (UnsupportedOperationException e) {
            check("delete implemented (root/successor)", false);
            blocked(e);
        }
    }

    // --- Matrix: Degenerate ---

    private static void testDegenerateSortedInsertChain() {
        section("Degenerate - sorted insert chain");
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<Integer, String>();
        try {
            for (int i = 1; i <= 5; i++) {
                tree.put(i, "v" + i);
            }
        } catch (UnsupportedOperationException e) {
            check("put implemented (sorted chain setup)", false);
            blocked(e);
            return;
        }
        try {
            check("sorted chain size is 5", tree.size() == 5);
            check("sorted chain height is 4 (n-1)", tree.height() == 4);
            check("sorted chain inOrder ascending",
                    tree.inOrder().equals(Arrays.asList(1, 2, 3, 4, 5)));
        } catch (UnsupportedOperationException e) {
            check("height/inOrder on chain", false);
            blocked(e);
        }
    }

    // --- helpers ---

    private static void buildLabInsertSequence(BinarySearchTree<Integer, String> tree) {
        tree.put(40, "p40");
        tree.put(20, "p20");
        tree.put(70, "p70");
        tree.put(10, "p10");
        tree.put(30, "p30");
        tree.put(55, "p55");
        tree.put(85, "p85");
        tree.put(60, "p60");
    }

    private static void section(String name) {
        System.out.println("-- " + name + " --");
    }

    private static void blocked(UnsupportedOperationException e) {
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
