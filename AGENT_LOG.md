# Agent Log — Lab 1: Ordered Inventory Index (BST)

**Date of work:** 2026-11-11
**Project folder:** `.` (project root)
**Assistant:** OpenCode (`opencode/mimo-v2.6-flash-free`)
**Workflow:** Plan → Test → Build → Audit
**Current checkpoint:** AUDIT complete + final revision (§12) — **ready for final Git review; not committed or pushed**

> **Reading note:** §1–5 are the historical PLAN, TEST, and BUILD checkpoint records, written at those checkpoints. Phrases such as “BUILD not started” or “algorithms not implemented” inside §1–5 describe the project state **at that time** and are superseded by later sections (§6 AUDIT). They are preserved intentionally as project history.

---

## 1. Assignment understanding

**Course:** CpE 211 — Data Structures & Algorithms
**Lecturer:** Engr. Jamie Eduardo Rosal, MSCpE
**Primary references (read in full):**

- `<Downloads>\00_Binary_Trees_and_BST_Lecture.docx` (external reference)
- `<Downloads>\01_BST_Ordered_Inventory_Lab.docx` (external reference)

### Technical contract (professor)

| Concept | Required behavior |
|---|---|
| BST ordering invariant | Left subtree keys smaller; right subtree keys larger (all descendants) |
| Duplicate keys | Replace stored value; do **not** add a second node; size unchanged |
| In-order traversal | Ascending keys when invariant holds |
| Height convention | Empty tree = **-1**; leaf = **0** |
| Complexity | Operations **O(h)**; balanced typical **O(log n)**; worst case **O(n)** |
| Range search | **Inclusive** bounds; ascending order; inverted range → empty; prune irrelevant branches |
| Deletion | Leaf / one-child / two-child in-order successor; preserve successor’s **right** child when unlinking successor |
| Nulls | Reject null keys/values where the contract specifies |
| Out of scope | AVL, Red-Black, external libraries, GUI features; balancing is a **limitation to discuss**, not a requirement |

### Learning outcomes to demonstrate

1. State and preserve the BST ordering invariant for every subtree.
2. Implement recursive put/get, inclusive range search, traversals, and deletion.
3. Trace the in-order successor case without losing its right child.
4. Measure height and distinguish balanced typical cost from worst-case degradation.

---

## 2. Workspace inspection (Plan phase)

### Findings

| Check | Result |
|---|---|
| Project root | `...\vscode\Lab8` (exists; OneDrive reparse point) |
| Contents of Lab8 | **Empty — 0 files** at plan time |
| Starter `src/` / `test/` / `LabTestRunner` | **Not present** in Lab8 |
| `BSTNode.java`, `BinarySearchTree.java` anywhere on profile | **Not found** |
| Assignment docs | Found and read under `Downloads` |
| Java toolchain | OpenJDK **25.0.5** (Temurin); `javac` 25.0.5 |
| Prior-lab conventions inspected | Lab3 (`starter-kit/labs/03-linked-lists`), Lab4, Lab6 (flat `src`/`test` style, plain `javac`/`java`, no JUnit) |
| Existing acceptance tests to preserve | **None existed** (no starter runner) |

### Implication

The lab document states that starter `src/` files intentionally contain TODOs and that a dependency-free `LabTestRunner` should be used. Those files were **never placed** in Lab8. The project was scaffolded from the technical contract after explicit student approval. **No existing source or tests were overwritten.**

---

## 3. Initial implementation plan (Plan checkpoint — approved)

### Approved project structure

```
Lab8/
├── src/
│   ├── BSTNode.java
│   └── BinarySearchTree.java
├── test/
│   ├── LabTestRunner.java
│   └── StudentBSTTest.java
├── run-tests.bat
├── AGENT_LOG.md
└── README.md
```

### Approved API return types (student decision)

| Method | Return type | Interpretation |
|---|---|---|
| `inOrder()` | `List<K>` | Keys in left-node-right order |
| `preOrder()` | `List<K>` | Keys in node-left-right order |
| `postOrder()` | `List<K>` | Keys in left-right-node order |
| `rangeSearch(min, max)` | `List<V>` | **Product values** in ascending key order |
| `put` / `get` / `delete` / `height` / `size` | per contract | As in technical contract table above |

**Documented interpretation:** Traversal methods return **keys** in their visit orders (lab language: ascending prices). Range search returns **values** (products) ordered by ascending key. This matches the student’s approval message and the lab scenario (price = key, product data = value).

### Strategy (approved at PLAN; executed during BUILD §5)

1. Implement `put` / `get` with null rejection and duplicate replacement.
2. Implement in-order range walk with pruning; inverted range → empty.
3. Implement `delete` for leaf, one-child, two-child successor (copy successor, then unlink successor; **reattach successor.right**), and root.
4. Implement traversals and `height` (empty = -1).
5. Maintain `size` on put (insert only) and successful delete.
6. Re-run full suites; AUDIT complexity vs height / sorted-input chain.
7. **No AVL / Red-Black / external dependencies.**

### Manual BST trace (Plan evidence)

**Insert sequence:** `40, 20, 70, 10, 30, 55, 85, 60`

```
            40
          /    \
        20      70
       /  \    /  \
     10   30  55   85
                 \
                  60
```

| Step | Key | Comparison path | New link |
|------|-----|-----------------|----------|
| 1 | 40 | empty tree | root |
| 2 | 20 | 20 < 40 | 40.left |
| 3 | 70 | 70 > 40 | 40.right |
| 4 | 10 | <40, <20 | 20.left |
| 5 | 30 | <40, >20 | 20.right |
| 6 | 55 | >40, <70 | 70.left |
| 7 | 85 | >40, >70 | 70.right |
| 8 | 60 | >40, <70, >55 | 55.right |

- Leaves: 10, 30, 60, 85
- **height = 3** (edges on 40→70→55→60)
- inOrder: `10, 20, 30, 40, 55, 60, 70, 85`
- preOrder: `40, 20, 10, 30, 70, 55, 60, 85`
- postOrder: `10, 30, 20, 60, 55, 85, 70, 40`
- size = 8

**Delete root 40:**

1. Two children → successor = min of right subtree.
2. From 70 go left → **55** (55.left == null).
3. **Why 55:** next key in ascending order after 40.
4. **60 is 55’s right child** → must reattach when 55 is removed.
5. Copy 55 into root; delete 55 from old place (one-child) → **70.left = 60**.

```
            55
          /    \
        20      70
       /  \    /  \
     10   30  60   85
```

- size = 7, **height = 2** (longest paths 55→20→10 and 55→70→60 have **2 edges**; empty=-1, leaf=0)
- inOrder: `10, 20, 30, 55, 60, 70, 85`
- **Bug guarded against:** dropping 60 when unlinking 55

> **Correction (TEST checkpoint):** An earlier draft of this log and the tests incorrectly recorded post-delete height as **3** (that value is the height of the tree *before* deleting 40: path 40→70→55→60). After root deletion the deepest path loses one edge → height **2**. See §4.1.

**rangeSearch(25, 60) inclusive:**

| Tree state | Expected keys | Expected values (approved `List<V>`) |
|------------|---------------|--------------------------------------|
| After inserts only | 30, 40, 55, 60 | p30, p40, p55, p60 |
| After delete 40 | 30, 55, 60 | p30, p55, p60 |

Acceptance and student range tests use a **fresh insert-only tree** for the lab’s `rangeSearch(25, 60)` trace to avoid ambiguity with the post-delete tree.

---

## 4. TEST checkpoint (executed)

### 4.1 Height assertion correction (student-found before BUILD)

**Status update (removal verification):** The student requested confirmation that the old `height == 3` assertions were removed rather than left beside the corrected ones. Grep of `test/StudentBSTTest.java` and `test/LabTestRunner.java` for `== 3`, `height is 3`, and `height remains 3` → **0 matches in executable code**. Each file contains exactly one post-delete height assertion, both expecting **2**. (Details under “Old assertions removed” below.)

**Original incorrect expectation (in `StudentBSTTest` and `LabTestRunner`):**

```java
assert t.height() == 3;   // WRONG after delete(40)
```

**Why it was incorrect:** Height 3 was measured on the tree *before* deleting root 40 (path `40 → 70 → 55 → 60` has 3 edges). After a correct delete:

```
            55
          /    \
        20      70
       /  \    /  \
     10   30  60   85
```

Longest root-to-leaf paths: `55 → 20 → 10` and `55 → 70 → 60` — **2 edges**.
With the package convention (empty = -1, leaf = 0), **height = 2**.

**Corrected assertion:**

```java
assert t.height() == 2;
```

**Old assertions removed (not supplemented):** A follow-up verification by the student suspected that the original `height == 3` checks might still sit beside the corrected ones. A grep of both test files for `== 3`, `height is 3`, and `height remains 3` returned **no matches**. The earlier correction edits replaced the assertion lines in place:

- `test/StudentBSTTest.java`: `check("height is 3", height == 3);` → **removed**; only `check("height is 2", height == 2);` remains (line 115).
- `test/LabTestRunner.java`: `check("height remains 3", tree.height() == 3);` → **removed**; only `check("height after root delete is 2", tree.height() == 2);` remains (line 263 at TEST time; now 288).

Executable height expectations after `delete(40)` in the test files are **exactly one check each, expecting 2**. Remaining `3` mentions are comments/historical notes only.

**Files updated:**

| File | Change |
|------|--------|
| `test/StudentBSTTest.java` | `height is 3` line **deleted**, replaced by `height is 2` (+ comment) — single assertion only |
| `test/LabTestRunner.java` | `height remains 3` line **deleted**, replaced by `height after root delete is 2` (+ comment) — single assertion only |
| `README.md` | post-delete height documented as `2` |
| `AGENT_LOG.md` | this section; post-delete height corrected to 2; removal verified by grep |

**Other expected results after `delete(40)` (verified, unchanged):**

| Check | Expected | Status |
|-------|----------|--------|
| `delete(40)` | `true` | unchanged |
| `size()` | `7` | unchanged |
| `inOrder()` | `[10, 20, 30, 55, 60, 70, 85]` | unchanged |
| `get(55)` | stored value (`"p55"`) | unchanged |
| `get(60)` | stored value (`"p60"`) | unchanged |
| `height()` | **`2`** | **corrected** |
| second `delete(40)` | `false` | unchanged |

*(Historical: as of the TEST checkpoint no BST algorithms existed yet — see §5 for their later implementation.)*

### 4.2 Sequence followed (per student approval)

1. Created `test/StudentBSTTest.java` (student-authored) **first**.
2. Created minimal compileable skeletons: `src/BSTNode.java`, `src/BinarySearchTree.java`.
3. **Did not implement BST algorithms** — every public operation throws `UnsupportedOperationException("TODO: ... not implemented")`.
4. Created `test/LabTestRunner.java` — **clearly labeled agent-authored**, cases derived from the professor’s acceptance-test matrix (not an instructor file).
5. Compiled and executed both suites.
6. Recorded **actual** output below (no fabrication).
7. Wrote this log and `README.md` / `run-tests.bat`.

### 4.3 Files created at TEST checkpoint

| Path | Role |
|------|------|
| `src/BSTNode.java` | Node skeleton only |
| `src/BinarySearchTree.java` | API skeleton; algorithms **not** implemented |
| `test/StudentBSTTest.java` | Student-authored tests |
| `test/LabTestRunner.java` | Agent-authored acceptance suite |
| `run-tests.bat` | Compile + run both suites |
| `README.md` | Structure, API decisions, commands |
| `AGENT_LOG.md` | This log |

### 4.4 Student-authored test — why selected

**Primary case:** root deletion where the **in-order successor has a right child** (lab checklist: “successor right child”).

- Inserts prescribed sequence `40, 20, 70, 10, 30, 55, 85, 60`
- `delete(40)` → true
- size → 7
- inOrder → `[10, 20, 30, 55, 60, 70, 85]`
- `get(55)`, `get(60)` return stored values
- height → **2** (corrected from erroneous 3; see §4.1)
- second `delete(40)` → false

**Also included:** duplicate replacement without size increase; empty-tree behavior; inverted `rangeSearch`; inclusive boundaries on a fresh tree.

**Rationale:** Combines two-child **root** delete with successor **60** reattachment — the classic pointer-repair failure the lab targets — in one scenario that the acceptance suite also covers from the matrix angle.

### 4.5 Exact commands run

```bat
cd /d .
javac -d out src\*.java test\*.java
java -ea -cp out LabTestRunner
java -ea -cp out StudentBSTTest
```

(Also available as `run-tests.bat`.)

### 4.6 Actual initial test output (TEST checkpoint — honest record)

**Compile:** `javac` exit code **0** (skeletons compile).

**Post-correction re-run #1 (height assertion fixed to 2; algorithms still unimplemented):**

| Suite | Passed | Failed | Exit | Failure reason |
|-------|--------|--------|------|----------------|
| LabTestRunner | **0** | **9** | **1** | `TODO: ... not implemented` |
| StudentBSTTest | **0** | **5** | **1** | `TODO: ... not implemented` |

Same totals as the first run: failures still identify missing TODO methods only (not height-assertion issues — those never execute because `put` throws first).

**Post-correction re-run #2 (removal verification + fresh compile/run):**

Commands executed:

```bat
javac -d out src\*.java test\*.java
java -ea -cp out LabTestRunner
java -ea -cp out StudentBSTTest
```

| Command | Exit code |
|---------|-----------|
| `javac -d out src\*.java test\*.java` | **0** |
| `java -ea -cp out LabTestRunner` | **1** |
| `java -ea -cp out StudentBSTTest` | **1** |

Actual results: **LabTestRunner 0 passed, 9 failed**; **StudentBSTTest 0 passed, 5 failed** — every failure `blocked by: TODO: ... not implemented`. No height-3 assertion errors (none exist). *(State at TEST checkpoint: skeleton stubs only; implementation followed in §5.)*

**LabTestRunner (agent-authored acceptance):** exit code **1**

```
=== LabTestRunner (agent-authored acceptance suite) ===
Source: acceptance-test matrix in 01_BST_Ordered_Inventory_Lab.docx

-- Empty/missing --
FAIL: empty-tree API implemented
       blocked by: TODO: size not implemented
-- Ordering (traversals) --
FAIL: put implemented (traversal setup)
       blocked by: TODO: put not implemented
-- Duplicate replacement --
FAIL: put implemented (duplicate setup)
       blocked by: TODO: put not implemented
-- Range (inclusive, empty, inverted) --
FAIL: put implemented (range setup)
       blocked by: TODO: put not implemented
-- Deletion - leaf --
FAIL: put implemented (leaf-delete setup)
       blocked by: TODO: put not implemented
-- Deletion - one child --
FAIL: put implemented (one-child setup)
       blocked by: TODO: put not implemented
-- Deletion - two children (non-root) --
FAIL: delete implemented (two children)
       blocked by: TODO: put not implemented
-- Deletion - root + successor right child --
FAIL: delete implemented (root/successor)
       blocked by: TODO: put not implemented
-- Degenerate - sorted insert chain --
FAIL: put implemented (sorted chain setup)
       blocked by: TODO: put not implemented

LabTestRunner results: 0 passed, 9 failed
Exception in thread "main" java.lang.AssertionError: 9 acceptance test(s) failed
	exit code: 1
```

**StudentBSTTest (student-authored):** exit code **1**

```
=== StudentBSTTest (student-authored) ===

-- Root delete with successor that has a right child --
FAIL: put implemented (build insertion sequence)
       blocked by: TODO: put not implemented
-- Duplicate key replaces value without size increase --
FAIL: put implemented (duplicate scenario)
       blocked by: TODO: put not implemented
-- Empty tree behavior --
FAIL: empty-tree operations implemented
       blocked by: TODO: size not implemented
-- Inverted range returns empty --
FAIL: put implemented (range setup)
       blocked by: TODO: put not implemented
-- Inclusive range boundaries (fresh tree) --
FAIL: put implemented (range tree setup)
       blocked by: TODO: put not implemented

StudentBSTTest results: 0 passed, 5 failed
Exception in thread "main" java.lang.AssertionError: 5 student test(s) failed
	exit code: 1
```

*(Output identical after the height-assertion correction; re-run exit codes both `1`.)*

### 4.7 Initial result summary

| Suite | Passed | Failed | Exit | Intended reason |
|-------|--------|--------|------|-----------------|
| LabTestRunner (acceptance) | **0** | **9** | 1 | Skeleton throws `TODO: ... not implemented` |
| StudentBSTTest (student) | **0** | **5** | 1 | Same — missing BUILD algorithms |

**No tests were weakened, deleted, or modified to force a result.**
**No claim of passing tests at TEST checkpoint.**
*(Historical)* Failures exposed the missing implementation exactly as required before BUILD; BUILD results are in §5.4.

### 4.8 Test-coverage notes vs acceptance matrix

| Matrix case | Covered in LabTestRunner | Covered in StudentBSTTest |
|-------------|--------------------------|---------------------------|
| Empty / missing | Yes | Yes (empty tree) |
| Ordering + traversals | Yes | In-order after delete |
| Duplicate replacement | Yes | Yes |
| Range inclusive / empty / inverted | Yes | Inverted + inclusive |
| Deletion leaf | Yes | — |
| Deletion one child | Yes | — |
| Deletion two children | Yes | Via root/successor path |
| Deletion root + successor right child | Yes | **Primary case** |
| Degenerate sorted chain height | Yes | — |

---

## 5. BUILD checkpoint (executed)

**Date:** 2026-11-11 · **Authorization:** student approval to implement BST algorithms.

### 5.1 Files and methods implemented

| File | Change |
|------|--------|
| `src/BSTNode.java` | Finalized: `key`, `value`, `left`, `right` fields + constructor (unchanged shape from skeleton) |
| `src/BinarySearchTree.java` | **All TODO stubs replaced with recursive implementations** |

Methods implemented (signatures unchanged from skeleton/approved API):

| Method | Implementation |
|--------|----------------|
| `put(K, V)` | Recursive `putRec`; null key/value rejected with `NullPointerException`; equality replaces `node.value` in place; `count++` only on new node creation |
| `get(K)` | Recursive `getRec`; null key rejected; returns `null` at empty slot |
| `delete(K)` | Recursive `deleteRec` returning repaired subtree; `boolean[] removed` flag; `count--` exactly once when found |
| `rangeSearch(K, K)` | Inverted-range short-circuit → empty list; recursive `rangeRec` with pruning (left only if `min < node.key`, right only if `max > node.key`, collect when inside bounds) |
| `inOrder()` / `preOrder()` / `postOrder()` | Recursive collectors into `ArrayList<K>` |
| `height()` | Recursive `heightRec`: `null → -1`, else `1 + max(children)` |
| `size()` | Returns maintained `count` |

Private helpers added: `putRec`, `getRec`, `rangeRec`, `deleteRec`, `minNode`, `deleteMin`, `inOrderRec`, `preOrderRec`, `postOrderRec`, `heightRec`.

**Tests were not modified** during BUILD — `test/LabTestRunner.java` and `test/StudentBSTTest.java` are byte-for-byte the corrected TEST versions.

### 5.2 Important algorithm decisions

**Recursive deletion and successor reattachment (the core lab requirement):**

```java
// Two-child case inside deleteRec:
BSTNode<K,V> successor = minNode(node.right);   // in-order successor = min of right subtree
node.key   = successor.key;                      // copy successor content into deleted node
node.value = successor.value;
node.right = deleteMin(node.right);              // unlink successor from old position
```

- **Why copy-then-unlink:** preserves the ordering invariant at every ancestor without threading parent pointers; only the recursion stack holds structure context.
- **Successor's right child:** `deleteMin` returns `node.right` when `node.left == null`, so if the successor had a right child it is **promoted into the successor's former slot** — never dropped. This is exactly the lab's "successor right child" requirement (delete 40 → successor 55 → 60 reattaches under 70).
- **Leaf / one-child:** `return node.right` when `left == null`, else `return node.left` — covers leaf (both null → returns null) and one-child promotion with one branch.
- **Root:** `root = deleteRec(root, ...)` reassigns the root reference; no special-case code needed.
- **Missing key:** recursion hits `null`, `removed[0]` stays false, `count` unchanged, returns `false`.
- **Size discipline:** flag-based decrement avoids double-counting; `put` increments only inside the `node == null` creation branch, so duplicate replacement cannot increase size.

**Range pruning:** three conditions instead of one recursive walk — skips a subtree only when it cannot overlap `[min, max]`; results collected in-order → ascending key order for free.

**Null contract:** `put` rejects null key and value; `get`/`delete` reject null key; `rangeSearch` rejects null bounds — all via `NullPointerException` with method-named messages (standard Java collection behavior).

### 5.3 Actual compile and test commands

```bat
cd /d .
javac -d out src\*.java test\*.java
java -ea -cp out LabTestRunner
java -ea -cp out StudentBSTTest
```

### 5.4 Actual test results (BUILD checkpoint — executed on this machine)

| Command | Exit code |
|---------|-----------|
| `javac -d out src\*.java test\*.java` | **0** |
| `java -ea -cp out LabTestRunner` | **0** |
| `java -ea -cp out StudentBSTTest` | **0** |

**LabTestRunner (agent-authored acceptance): 48 passed, 0 failed, exit 0**

```
-- Empty/missing --                          9 PASS
-- Ordering (traversals) --                  5 PASS
-- Duplicate replacement --                  3 PASS
-- Range (inclusive, empty, inverted) --     5 PASS
-- Deletion - leaf --                        4 PASS
-- Deletion - one child --                   5 PASS
-- Deletion - two children (non-root) --     6 PASS
-- Deletion - root + successor right child -- 8 PASS
-- Degenerate - sorted insert chain --       3 PASS
LabTestRunner results: 48 passed, 0 failed
```

**StudentBSTTest (student-authored): 24 passed, 0 failed, exit 0**

```
-- Root delete with successor that has a right child --   10 PASS (incl. height is 2)
-- Duplicate key replaces value without size increase --   2 PASS
-- Empty tree behavior --                                  8 PASS
-- Inverted range returns empty --                         1 PASS
-- Inclusive range boundaries (fresh tree) --              3 PASS
StudentBSTTest results: 24 passed, 0 failed
```

**Combined: 72 checks passed, 0 failed.** Both exit codes **0**.

### 5.5 Implementation issues encountered and fixes

1. **Typo in `deleteMin` during drafting:** wrote an invalid placeholder line (`return node.node == null ? node : node;`) instead of `return node;`. Caught by self-review of the diff before the first compile; corrected immediately. The first `javac` run then succeeded with exit 0.
2. **No test failures at first execution** — all 72 checks passed on the first post-implementation run. No expected test results were changed.
3. **Height assertion (pre-BUILD correction):** the previously corrected `height == 2` expectation matched implementation behavior exactly; no adjustment needed.

### 5.6 Build-phase restrictions honored

- No AVL / Red-Black / balancing code added.
- No GUI, no external libraries, no new dependencies.
- No test weakened, removed, or rewritten; student suite preserved.
- No Git commit/push attempted.
- AUDIT not started.

---

## 6. AUDIT checkpoint (completed)

**Date:** 2026-11-11 · **Mode:** read-only audit (no files modified during the audit itself)
**Overall result:** **PASS WITH NOTES** — no correctness defects found.

### 6.1 Audit scope and evidence

- Inspected actual current files: `src/BSTNode.java`, `src/BinarySearchTree.java`, both test suites, `run-tests.bat`, `README.md`, this log.
- Re-ran verification at audit time (actual exit codes): `javac` **0**; `LabTestRunner` **48 passed, 0 failed**, exit **0**; `StudentBSTTest` **24 passed, 0 failed**, exit **0**; `cmd /c run-tests.bat` exit **0** (“All test suites passed.”).
- Source SHA-256 captured at audit time matched the BUILD state (see §6.4 post-audit hash check).

### 6.2 Audit findings recorded

**A. Correctness — PASS.** All contract behaviors verified by inspection + tests: generics/Comparable ordering, put insert & duplicate replace (size unchanged), get value-or-null, all five deletion cases (leaf / one-child / two-child successor / root / missing), successor right-child reattachment via `deleteMin`, inclusive pruned range, inverted range empty, three traversals, height convention (empty −1 / leaf 0), O(1) stored count.

**B. Edge cases — two coverage gaps (findings, not defects):**

1. **Gap A:** deleting the **only node** was not explicitly tested in either suite (correct by inspection).
2. **Gap B:** one-child deletion was tested only for a **left child**; the **right-child-only** promotion path was untested (correct by inspection).

**C. Complexity — verified:** put/get/delete **O(h)**; rangeSearch **O(h + k)** typical, O(n) worst (pruned in-order); traversals **O(n)**; height **O(n)** (no cached height); size **O(1)** (stored count). Balanced tree h = O(log n); sorted unbalanced insert h = n−1 = O(n) (empirically: 5 sorted inserts → height 4). No self-balancing claimed — documented limitation.

**D. Tests — PASS** at audit time 48 + 24 = 72/72. Trivial note: dead unused tree setup in `testDeletionOneChild` (`LabTestRunner.java`, original lines ~202–212).

**E. Documentation — PASS WITH NOTES:** README/AGENT_LOG results matched reality; workflow status tables still read “AUDIT: Not started” (correct pre-audit; updated post-audit — see §9).

### 6.3 Post-audit improvements (authorized BUILD mode)

**Changes (tests + docs only; `src/` untouched):**

1. **Task 1 — added two deletion tests** to `test/LabTestRunner.java` (coverage gaps A and B):
   - `testDeleteOnlyNode()` — insert one key → delete → true; size 0; height −1; get null; second delete false. *(5 checks)*
   - `testDeleteRightChildOnly()` — parent with only a right child → delete → true; right child searchable with original value; size 1; inOrder `[70]`; parent not found. *(5 checks)*
2. **Task 2 — removed dead setup** in `testDeletionOneChild()`: deleted only the unused `tree` variable + try/catch that no assertion read. The real one-child test (`oneChild`, 50/30/20, delete 30) and **all 5 original assertions unchanged**.
3. **Task 5 — documentation:** this section; workflow status → AUDIT Complete (§9); README status/totals corrected.

**Not done (per rules):** no `src/` changes (including `getRoot`/`getCount` kept), no existing assertions removed or weakened, no dependencies, no Git operations.

### 6.4 Post-audit verification (actual)

| Command | Exit code |
|---------|-----------|
| `javac -d out src\*.java test\*.java` | **0** |
| `java -ea -cp out LabTestRunner` | **0** — **58 passed, 0 failed** |
| `java -ea -cp out StudentBSTTest` | **0** — **24 passed, 0 failed** |
| `cmd /c run-tests.bat` | **0** — “All test suites passed.” |

**Combined: 58 + 24 = 82 passed, 0 failed.** New sections in output: `-- Deletion - only node --` (5 PASS) and `-- Deletion - one child (right only) --` (5 PASS). All 48 prior acceptance checks and all 24 student checks still pass unchanged.

**Source integrity (Task 4):**

| File | SHA-256 | Match vs audit? |
|------|---------|-----------------|
| `src/BSTNode.java` | `626024A91CB268C3E6D6AFFB61A2BB5B3203E1E219443DBC434968518C06A86D` | ✅ identical |
| `src/BinarySearchTree.java` | `7EFD927E9D541FB8F10D6A57381999D72A4B1911BCFEA1E8F5213935AC3C0632` | ✅ identical |

---

## 7. Design decisions (meaningful agent / student decisions)

1. **Scaffold from contract** — no starter existed; student authorized empty-Lab8 scaffold; stop-if-found rule honored (Lab8 re-verified empty before writes).
2. **Return types** — student-approved: traversals `List<K>`, range `List<V>`; documented in README and here.
3. **Skeleton failure mode** — `UnsupportedOperationException` with `TODO:` messages so failures clearly mean “not implemented,” not “wrong logic.”
4. **Two test files** — acceptance (`LabTestRunner`, agent-authored, matrix) kept separate from `StudentBSTTest` (student-authored) so neither is weakened to help the other.
5. **Exception-aware checks** — both runners catch `UnsupportedOperationException`, print `blocked by: ...`, and count FAIL; avoids a single uncaught throw hiding later cases.
6. **No balancing code** — limitation reserved for AUDIT, per lab.
7. **No JUnit/Maven** — plain `javac` + `java -ea`, consistent with Labs 3–6.
8. **rangeSearch(25,60) tested on fresh insert-only tree** — lab order (insert then delete) makes post-delete expected set different; avoids false failures from tree-state ambiguity.
9. **LabTestRunner labeled agent-authored** in file header and README — honesty about provenance (no instructor file was supplied).
10. **BUILD: copy-then-unlink two-child delete** — successor content copied into deleted node, `deleteMin` promotes successor's right child; no parent pointers needed (§5.2).
11. **BUILD: flag-array `removed` in recursion** — single `count--` after top-level delete regardless of recursion depth (§5.2).
12. **BUILD: null rejection via `NullPointerException` with method-named messages** — matches Java collection norms (§5.2).

---

## 8. Prompts and workflow decisions

| # | Decision | Rationale |
|---|----------|-----------|
| P1 | Plan mode first: inspect docs + empty workspace; no writes | Professor Plan → Test → Build → Audit |
| P2 | Challenge “don’t assume empty” vs reality (0 files) | Truthful inspection; no invented starter |
| P3 | Ask student for starter location / API choices before scaffold | Avoid wrong signatures |
| P4 | Student: scaffold Lab8; approved return types; TEST-first sequence | Explicit authorization |
| P5 | Student test before any algorithm code | Lab TEST checkpoint rule |
| P6 | Record real compile/test output; exit codes 1/1 | No fabricated evidence |
| P7 | **STOP before BUILD** *(historical; superseded by P10)* | Awaiting student approval |
| P8 | Student found incorrect post-delete height 3 → corrected to 2 | Prevented a false BUILD failure later; documented §4.1 |
| P9 | Student asked to confirm old `height == 3` lines were removed; grep verified 0 matches; re-run recorded | Honest audit of test-file state; §4.1 / re-run #2 |
| P10 | Student authorized BUILD: implement both src files, run tests, no Git, no AUDIT yet | Professor Build phase scope |
| P11 | `deleteMin` typo caught by self-review before first compile | Avoided a needless compile failure; §5.5 |
| P12 | Documentation cleanup: marked §1–4 as historical, updated header/status, numbered §4 subsections | Keep log truthful after BUILD without erasing history |
| P13 | AUDIT run read-only; findings reported, not fixed during audit | Professor audit-scope rule |
| P14 | Post-audit: added only the two gap-closing tests (`testDeleteOnlyNode`, `testDeleteRightChildOnly`); removed only dead test setup; `src/` hash-verified unchanged | Close audit notes B/D without touching implementation or weakening tests |

---

## 9. Workflow status

| Checkpoint | Status | Evidence |
|------------|--------|----------|
| PLAN | **Complete** | This log §1–3; student approved plan |
| TEST | **Complete** | Height assertion corrected (§4.1); compile OK; historical **0+0 pass, 9+5 fail** on skeleton TODOs, exit 1 |
| BUILD | **Complete** | §5: algorithms implemented; **48+24 = 72 pass, 0 fail**, all exit **0** |
| AUDIT | **Complete** | §6: **PASS WITH NOTES**; no correctness defects; 2 coverage gaps found; re-run 48+24 exit 0 |
| POST-AUDIT | **Complete** | §6.3–6.4: gaps closed with 2 new tests; **58+24 = 82 pass, 0 fail**, all exit **0**; `src/` hashes identical to audit |

**Final status:** Project is **ready for final Git review**. **No Git operations have been performed — nothing committed or pushed.**

---

## 10. Next phase (Git checkpoint) — only after student approval

1. Create `.gitignore` excluding `out/`, `*.class`, and `.vscode/`.
2. Initialize the repository on `main`.
3. Verify only the intended submission files (src/, test/, run-tests.bat, AGENT_LOG.md, README.md) plus `.gitignore` are staged.
4. Review staged changes before committing.
5. Commit only after student approval.
6. Configure/verify the GitHub remote and push only after separate approval.

No remote URL is assumed and no GitHub repository is claimed to exist yet.
No Git operations have been performed — every step above requires approval.

---

## 11. Final status confirmation (end of post-audit improvements)

- **BST algorithms implemented** (§5) and **audited PASS WITH NOTES** with no correctness defects (§6).
- **Post-audit:** two coverage-gap tests added; dead test setup removed; **existing assertions untouched** (§6.3).
- **Actual results: `javac` 0; LabTestRunner 58/58 pass exit 0; StudentBSTTest 24/24 pass exit 0; run-tests.bat exit 0. Combined 82/82.**
- **`src/BSTNode.java` and `src/BinarySearchTree.java` SHA-256 identical to AUDIT hashes (Task 4) — implementation unchanged.**
- **No balancing, GUI, or external dependencies added.**
- **No Git operations performed — ready for final Git review, not committed or pushed.**

---

## 12. Final revision (2026-11-12, reviewed with Claude before submission)

Every change below was made after the audit, with all prior assertions kept (no test weakened or removed).

### 12.1 Defects found and fixed

| # | Finding | Fix |
|---|---|---|
| 1 | **Traversal return type did not match the contract.** The lab contract says "Traversals / height: Return **values** in named traversal order", but `inOrder/preOrder/postOrder` returned keys. | `inOrder/preOrder/postOrder` now return `List<V>`. Added `inOrderKeys/preOrderKeys/postOrderKeys` (`List<K>`) so key ordering is still verified. The earlier "approved `List<K>`" decision (§3, §7 item 2) is **superseded** by this change. |
| 2 | Existing tests asserted key lists via `inOrder()` etc. | Calls renamed to the `*Keys()` variants; every assertion and expected value is unchanged. |
| 3 | No test for value-returning traversals. | Added `testTraversalValues` (7 checks) to `LabTestRunner`. |
| 4 | Javadoc in `BinarySearchTree.java` contained literal `&gt;` / `&lt;` HTML entities. | Replaced with `>` / `<`; header comment updated to the new return types. |
| 5 | `README.md` and `AGENT_LOG.md` had corrupted characters (UTF-8 saved through the wrong encoding: `â€”`, `â†’`, `Â§`, ...). | Re-decoded; arrows, dashes, section signs and tree diagrams restored. |
| 6 | Audit task required comparing balanced vs sorted insertion, but only one 5-key sorted run existed. | Added `test/ComplexityAudit.java` (counts `compareTo` calls via a wrapper key, no change to `src/` logic). Results in README and §12.3. |
| 7 | `run-tests.bat` / `.gitignore` were listed but missing from the supplied files. | Recreated; `run-tests.bat` now also runs `ComplexityAudit`. |

### 12.2 Verification (actual, 2026-11-12, Java compile with `javac`)

| Command | Result |
|---|---|
| `javac -d out src\*.java test\*.java` | exit 0 |
| `java -ea -cp out LabTestRunner` | **65 passed, 0 failed**, exit 0 |
| `java -ea -cp out StudentBSTTest` | **24 passed, 0 failed**, exit 0 |
| `java -ea -cp out ComplexityAudit` | all checks passed, exit 0 |

SHA-256 after revision: `src/BSTNode.java` `626024A9...C06A86D` (unchanged); `src/BinarySearchTree.java` `55D02221...2864154` (**changed** by items 1 and 4; the hash recorded in §6.4 belongs to the earlier version).

### 12.3 Complexity evidence

| n | Insert order | Height | `get(max)` compares | Build compares |
|---|---|---|---|---|
| 15 | balanced | 3 | 4 | 34 |
| 15 | sorted | 14 | 15 | 105 |
| 127 | balanced | 6 | 7 | 642 |
| 127 | sorted | 126 | 127 | 8001 |
| 1023 | balanced | 9 | 10 | 8194 |
| 1023 | sorted | 1022 | 1023 | 522753 |

- Lookup cost equals height + 1 comparisons: O(h). Balanced gives h = O(log n); sorted gives h = n - 1, so O(n) lookup and O(n^2) total build (n(n-1)/2 compares, exactly as measured).
- `rangeSearch` (balanced, n = 1023, h = 9): k=1 -> 28 compares; k=10 -> 64; k=100 -> 432; k=1023 -> 4093. Cost tracks h + k, not n, for small ranges (pruning works).
- Traversals visit each node once: O(n). Recursion depth equals height: O(h) stack, O(n) worst case.
- Balancing (AVL / Red-Black) is a limitation, not implemented, as the lab specifies.

### 12.4 rangeSearch(25, 60) trace (insert-only tree 40, 20, 70, 10, 30, 55, 85, 60)

| Node | Action |
|---|---|
| 40 | 25 < 40 -> go left; 40 in [25, 60] -> collect; 60 > 40 -> go right |
| 20 | 25 < 20 false -> **left subtree (10) pruned**; 20 below range, not collected; 60 > 20 -> go right |
| 30 | go left (empty); collect 30; go right (empty) |
| 70 | 25 < 70 -> go left; 70 > 60 so not collected; 60 > 70 false -> **right subtree (85) pruned** |
| 55 | go left (empty); collect 55; go right |
| 60 | collect 60 |

Result values in order: p30, p40, p55, p60. Pruned branches: node 10 and node 85.

### 12.5 Reflection

- **Most careful pointer repair:** two-child deletion where the in-order successor has a right child (delete 40: successor 55, its right child 60 must be promoted into 55's old slot under 70). `deleteMin` returns `node.right` so 60 is reattached instead of lost.
- **Evidence the sorted tree is linear-height:** ComplexityAudit - sorted insertion of n keys gives height n - 1 and `get(max)` needs n comparisons (n = 1023 -> height 1022, 1023 compares) versus height 9 and 10 compares for the same keys in balanced order.

### 12.6 Status

Ready for final Git review. Nothing committed or pushed.
