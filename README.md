# Lab 1 â€” Ordered Inventory Index (Binary Search Tree)

CpE 211 Data Structures & Algorithms
Workflow: **Plan â†’ Test â†’ Build â†’ Audit**
Status: **AUDIT complete (PASS WITH NOTES) + post-audit tests added** â€” both suites pass (82/82). Ready for final Git review; not committed or pushed.

## Directory structure

```
Lab8/
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ BSTNode.java              # node: key, value, left, right
â”‚   â””â”€â”€ BinarySearchTree.java     # recursive BST: put/get/delete/range/traversals/height/size
â”œâ”€â”€ test/
â”‚   â”œâ”€â”€ LabTestRunner.java        # agent-authored acceptance suite (matrix-derived)
â”‚   â””â”€â”€ StudentBSTTest.java       # student-authored edge-case test
â”œâ”€â”€ run-tests.bat
â”œâ”€â”€ AGENT_LOG.md
â””â”€â”€ README.md
```

> **Note:** No instructor starter project was present in `Lab8`. The project was
> scaffolded from the technical contract in `01_BST_Ordered_Inventory_Lab.docx`.
> `LabTestRunner.java` is **agent-authored**, not an instructor-provided file.

## API (approved return types)

| Method | Returns |
|---|---|
| `put(K key, V value)` | `void` â€” rejects nulls; duplicate key replaces value, size unchanged |
| `get(K key)` | `V` or `null` â€” rejects null key |
| `rangeSearch(K min, K max)` | `List<V>` â€” values in ascending key order, inclusive bounds; inverted range â†’ empty |
| `delete(K key)` | `boolean` â€” `true` if found (size decreases once); leaf / one-child / two-child / root |
| `inOrder()` / `preOrder()` / `postOrder()` | `List<K>` â€” keys in visit order |
| `height()` | `int` â€” empty = **-1**, leaf = **0** |
| `size()` | `int` |

## Compile and run the tests

```bat
javac -d out src\*.java test\*.java
java -ea -cp out LabTestRunner
java -ea -cp out StudentBSTTest
```

or simply:

```bat
run-tests.bat
```

Expected after post-audit improvements (measured 2026-11-11):

| Suite | Result | Exit |
|-------|--------|------|
| LabTestRunner | 58 passed, 0 failed | 0 |
| StudentBSTTest | 24 passed, 0 failed | 0 |

Combined: **82 passed, 0 failed**. (At BUILD time the total was 72; the post-audit
checkpoint added `testDeleteOnlyNode` and `testDeleteRightChildOnly` â€” 10 checks.)

## Lab insert sequence (manual trace reference)

Insert: `40, 20, 70, 10, 30, 55, 85, 60`
Delete root `40` â†’ successor `55`, reattach `60` under `70`.
After delete, in-order: `10, 20, 30, 55, 60, 70, 85`, height `2` (longest path has 2 edges).

## Limitation

A plain BST does not balance itself. Sorted insertion yields height `n-1`
(worst-case `O(n)`). AVL / Red-Black trees are out of scope.
