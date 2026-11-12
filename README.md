# Lab 8 — Ordered Inventory Index (Binary Search Tree)

CpE 211 Data Structures & Algorithms
Workflow: **Plan → Test → Build → Audit**
Status: **AUDIT complete + final revision** — all suites pass (LabTestRunner 65, StudentBSTTest 24, ComplexityAudit 6+2 checks). Not committed or pushed.

## Directory structure

```
Lab8/
├── src/
│   ├── BSTNode.java              # node: key, value, left, right
│   └── BinarySearchTree.java     # recursive BST: put/get/delete/range/traversals/height/size
├── test/
│   ├── LabTestRunner.java        # agent-authored acceptance suite (matrix-derived)
│   ├── StudentBSTTest.java       # student-authored edge-case test
│   └── ComplexityAudit.java      # balanced vs sorted height / comparison-count evidence
├── run-tests.bat
├── .gitignore
├── AGENT_LOG.md
└── README.md
```

> **Note:** No instructor starter project was present in `Lab8`. The project was
> scaffolded from the technical contract in `01_BST_Ordered_Inventory_Lab.docx`.
> `LabTestRunner.java` is **agent-authored**, not an instructor-provided file.

## API (approved return types)

| Method | Returns |
|---|---|
| `put(K key, V value)` | `void` — rejects nulls; duplicate key replaces value, size unchanged |
| `get(K key)` | `V` or `null` — rejects null key |
| `rangeSearch(K min, K max)` | `List<V>` — values in ascending key order, inclusive bounds; inverted range → empty |
| `delete(K key)` | `boolean` — `true` if found (size decreases once); leaf / one-child / two-child / root |
| `inOrder()` / `preOrder()` / `postOrder()` | `List<V>` — **values** in visit order (lab contract: "return values in named traversal order") |
| `inOrderKeys()` / `preOrderKeys()` / `postOrderKeys()` | `List<K>` — keys in the same visit orders (used to verify ordering) |
| `height()` | `int` — empty = **-1**, leaf = **0** |
| `size()` | `int` |

## Compile and run the tests

```bat
javac -d out src\*.java test\*.java
java -ea -cp out LabTestRunner
java -ea -cp out StudentBSTTest
java -ea -cp out ComplexityAudit
```

or simply:

```bat
run-tests.bat
```

Expected (final revision, 2026-11-12):

| Suite | Result | Exit |
|-------|--------|------|
| LabTestRunner | 65 passed, 0 failed | 0 |
| StudentBSTTest | 24 passed, 0 failed | 0 |
| ComplexityAudit | all checks passed | 0 |

(History: 72 checks at BUILD, 82 after the post-audit tests, 89 after the final revision
added `testTraversalValues`, 7 checks.)

## Lab insert sequence (manual trace reference)

Insert: `40, 20, 70, 10, 30, 55, 85, 60`
Delete root `40` → successor `55`, reattach `60` under `70`.
After delete, in-order: `10, 20, 30, 55, 60, 70, 85`, height `2` (longest path has 2 edges).

## Complexity evidence (ComplexityAudit, measured)

| n | Insert order | Height | Compares for `get(max)` |
|---|---|---|---|
| 15 | balanced | 3 | 4 |
| 15 | sorted | 14 | 15 |
| 127 | balanced | 6 | 7 |
| 127 | sorted | 126 | 127 |
| 1023 | balanced | 9 | 10 |
| 1023 | sorted | 1022 | 1023 |

`rangeSearch` on the balanced n = 1023 tree: k=1 -> 28 compares, k=10 -> 64, k=100 -> 432, k=1023 -> 4093
(grows with k, not with n, for small ranges: O(h + k)).

## Limitation

A plain BST does not balance itself. Sorted insertion yields height `n-1`
(worst-case `O(n)`). AVL / Red-Black trees are out of scope.
