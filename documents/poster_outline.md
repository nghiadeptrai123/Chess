# COMP1020 OOP & Data Structures — Poster Outline
**Course:** COMP1020 Object-Oriented Programming & Data Structures, Spring 2026  
**Project:** Chess Game with AI Opponents (Java Swing)  
**Format:** A0 size (841 × 1189 mm) — Portrait or Landscape  
**Submission Deadline:** 11:59 PM, June 2, 2026  
**Poster Session:** June 4, 2026 · 1:30 PM – 3:00 PM

---

> **Design Tips (from guideline):**  
> Use diagrams, screenshots, workflow illustrations, tables, and charts.  
> Avoid excessive text — prioritize visuals and concise bullet points.  
> All team members must be able to explain every section during the session.

---

## Suggested Layout (A0 Portrait — 7-Panel Grid)

```
┌─────────────────────────────────────────────────────────────┐
│                    HEADER (Full Width)                       │
├────────────────────┬────────────────────┬───────────────────┤
│  Panel 1           │  Panel 2           │  Panel 3          │
│  Problem &         │  System            │  OOP Concepts &   │
│  Objectives        │  Architecture      │  Key Classes      │
├────────────────────┼────────────────────┼───────────────────┤
│  Panel 4           │  Panel 5           │  Panel 6          │
│  Algorithms &      │  Key Features      │  Testing &        │
│  Data Structures   │  (Screenshots)     │  Evaluation       │
├────────────────────┴────────────────────┴───────────────────┤
│             Panel 7: Conclusion & Future Work (Full Width)   │
└─────────────────────────────────────────────────────────────┘
```

---

## HEADER (Full Width — Top Banner)

```
╔══════════════════════════════════════════════════════════════╗
║   🏛️  CHESS GAME WITH AI OPPONENTS                          ║
║   COMP1020 OOP & Data Structures — Spring 2026               ║
║   Team: [Team Name]  |  Members: [Name 1, Name 2, ...]      ║
╚══════════════════════════════════════════════════════════════╝
```

**Content:**
- Project title (large, bold, eye-catching font)
- Course name and semester
- University / Department name
- Team name and all member names
- *(Optional)* Small chess-themed decorative graphic or board screenshot

---

## Panel 1 — Problem Statement & Objectives

**Heading:** 🎯 Problem Statement & Objectives

**Content (use bullet points + 1 small graphic):**

### Problem Statement
- Chess has ~10¹²⁰ possible game states — enforcing all legal rules and building a functional AI is a significant software engineering and algorithmic challenge.
- Goal: build a rule-complete, interactive chess application in Java with multiple difficulty levels of AI opponent.

### Objectives
- ✅ Implement a fully playable chess game using Java Swing GUI
- ✅ Enforce all standard chess rules (movement, check, checkmate, stalemate, castling, pawn promotion)
- ✅ Build AI bots using the Minimax algorithm with Alpha-Beta Pruning
- ✅ Support two game modes: Human vs. Human and Human vs. Bot
- ✅ Provide four difficulty settings: Beginner, Amateur, Intermediate, Hard

**Visual suggestion:** Small icon-style feature list or a "goal checklist" table.

---

## Panel 2 — System Architecture & Design

**Heading:** 🏗️ System Architecture & Design

**Content:**

### Architecture Diagram (main visual — include a simplified UML or box diagram)

```
┌──────────────────────────────────────────┐
│         ChessBoardUI (View + Controller)  │
│  ┌────────┐  ┌──────────────┐  ┌───────┐ │
│  │ Board  │  │GameController│  │ChessBot│ │
│  │(Model) │  │ (Turn/Mode)  │  │(AI IF) │ │
│  └────────┘  └──────────────┘  └───────┘ │
│      │                            │       │
│  Square[8][8]           BeginnerBot / AmateurBot │
│  activePieceCoords[]              │       │
└──────────────────────────────────────────┘
               │
       ┌───────┴────────┐
       │   Piece (abs.) │
       ├────────────────┤
       │King Queen Rook │
       │Bishop Knight   │
       │Pawn            │
       └────────────────┘
```

### Module Responsibilities (small table)

| Module | Role |
|---|---|
| `ChessBoardUI` | Rendering, input, game loop |
| `Board` | Grid, check/mate/stalemate detection |
| `Piece` (abstract) | Base contract for all pieces |
| `GameController` | Turn management, mode & difficulty |
| `ChessBot` (interface) | AI abstraction layer |
| `MoveHelper` | Static utilities for move validation |
| `MouseInputListener` | Drag-and-drop input handling |

---

## Panel 3 — OOP Concepts & Key Classes

**Heading:** 🔷 OOP Concepts Applied

**Content (split into 4 concept blocks with icons):**

### 🔒 Encapsulation
- `Piece` stores `isWhite` and `isMoved` as private fields; exposed only through getters (`isWhite()`, `isMoved()`)
- `Board` encapsulates the 8×8 grid; external access only via `getSquare(row, col)`

### 🔗 Inheritance
- All six piece types (`King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn`) extend the abstract class `Piece`
- Shared behaviour (`isWhite`, `isMoved`, `setMoved`) is inherited; movement rules are overridden

### 🔄 Polymorphism
- `Piece.isValidMove()` is declared abstract — each subclass provides its own implementation
- `ChessBoardUI` calls `piece.isValidMove(board, start, end)` uniformly for any piece type
- `ChessBot` interface: `ChessBoardUI` calls `activeBot.getBestMove(board, isWhite)` without knowing which bot is active

### 🧩 Abstraction
- `Piece` (abstract class) hides movement implementation details — the engine only knows pieces can validate moves and clone themselves
- `ChessBot` (interface) hides the entire AI algorithm — the game only interacts with `getBestMove()`

**Visual suggestion:** A 2×2 grid with one concept per quadrant, each with a code snippet example.

---

## Panel 4 — Algorithms & Data Structures

**Heading:** ⚙️ Algorithms & Data Structures

### Minimax with Alpha-Beta Pruning

**Diagram: Minimax tree (simplified, depth 3)**
```
              MAX (White)
             /           \
          MIN             MIN  (Black)
         /   \           /   \
       MAX   MAX       MAX   MAX (White)
       ...   ...       ...   ...
     [Eval] [Eval]   [Eval] [Eval]
```

| Bot Level | Depth | Quiescence Search |
|---|---|---|
| Beginner | 3 | No |
| Amateur | 5 | No |
| Intermediate | 4 | Yes |
| Hard | 5 | Yes |

**Alpha-Beta Pruning:** Prunes branches when `β ≤ α`, reducing effective branching factor from O(b^d) to O(b^(d/2)) in the best case.

**Move Ordering (MVV-LVA):**  
Moves are scored and sorted before search:
- Promotions → +8000
- Captures → `10 × victim_value − attacker_value`
- Quiet moves → 0

### Data Structures

| Structure | Used For | Benefit |
|---|---|---|
| `Square[8][8]` | Board grid | O(1) access by (row, col) |
| `int[] activePieceCoords` | Active piece list | O(n) iteration (n ≤ 32), avoids scanning 64 cells |
| `int[] boardToIndex` | Reverse-map position → index | O(1) piece removal (swap-and-decrement) |
| `Stack<GameState>` | Undo system | Natural LIFO for move history |
| `HashMap<String, BufferedImage>` | Piece images | O(1) image lookup during paint |

---

## Panel 5 — Key Features & Screenshots

**Heading:** 🖥️ Key Features

> *(This panel is the most visual — fill with screenshots and short captions)*

### Feature Grid (2×3 layout of screenshots with captions)

| Screenshot | Caption |
|---|---|
| *(Main board screenshot)* | 8×8 board with piece images, rank/file labels, and move log panel |
| *(Legal move highlights)* | Green dots = empty squares; Red ring = capturable enemy piece |
| *(Difficulty menu)* | Startup dialogs: game mode → difficulty selection |
| *(Pawn promotion dialog)* | Player selects Queen, Rook, Bishop, or Knight on promotion |
| *(Check alert)* | `JOptionPane` message when king enters check |
| *(Checkmate / game over)* | Game-over dialog with option to start new game |

### Feature List (bullet points alongside screenshots)
- 🖱️ **Drag-and-drop** piece movement with real-time highlight
- 📋 **Move log** in algebraic chess notation (e.g., `e4`, `Nf3`, `O-O`)
- ↩️ **Undo system** — reverts both human and bot moves in single-player
- 🏳️ **Surrender button** — graceful game end
- 🤖 **Bot runs on background thread** — UI stays responsive during calculation
- 🏰 **Castling** (kingside and queenside) — fully rule-validated
- ♟️ **Pawn promotion** — with player choice dialog (human) / auto-queen (bot)

---

## Panel 6 — Testing & Evaluation

**Heading:** 🧪 Testing & Evaluation

### Testing Methodology
- **Unit tests:** `TestRunner.java` validates `isValidMove()` for each piece type in isolation
- **Integration testing:** Play-through of complete game scenarios (Fool's Mate, Scholar's Mate, stalemate)
- **Manual testing:** All edge cases tested by all team members

### Test Results Summary (table)

| Test Case | Result |
|---|---|
| Standard pawn advance (1 & 2 squares from start) | ✅ Pass |
| Rook blocked by own piece | ✅ Pass |
| Knight L-shape moves | ✅ Pass |
| Bishop diagonal through piece | ✅ Pass |
| King into check (rejected) | ✅ Pass |
| Kingside castling (valid) | ✅ Pass |
| Castling through attacked square (rejected) | ✅ Pass |
| Checkmate detection (Fool's Mate) | ✅ Pass |
| Stalemate detection | ✅ Pass |
| Pawn promotion to Queen | ✅ Pass |
| Undo: 2-player mode | ✅ Pass |
| Undo: single-player (bot re-triggered) | ✅ Pass |

### Bot Performance

| Difficulty | Average Think Time |
|---|---|
| Beginner (depth 3) | < 1 second |
| Amateur (depth 5) | 1–5 seconds |
| Intermediate (depth 4 + QS) | *(measured value)* |
| Hard (depth 5 + QS) | *(measured value)* |

**Visual suggestion:** Bar chart of bot think time vs. difficulty level.

---

## Panel 7 — Conclusion & Future Work (Full Width — Bottom Banner)

**Heading:** 🏁 Conclusion & Future Work

### What We Achieved
- ✅ Fully rule-complete chess game in Java Swing (18 classes, ~5,000+ lines of code)
- ✅ Clean OOP design: abstract `Piece` hierarchy, `ChessBot` interface for plug-and-play AI
- ✅ Efficient board engine with O(1) piece removal and O(n) check detection
- ✅ Minimax + Alpha-Beta Pruning AI across 4 difficulty levels
- ✅ Robust undo, move logging, and threading architecture

### Known Limitations
| Limitation | Details |
|---|---|
| En passant | Not implemented |
| Draw rules | 50-move rule / threefold repetition not enforced |
| Bot evaluation | Material-only; no positional heuristics |
| Code duplication | `BeginnerBot` / `AmateurBot` nearly identical |

### Future Improvements
- 📐 **Piece-square tables** for positional bot evaluation
- ♟️ **En passant** and full draw rules
- 🔄 **Refactored bot** — single `MinimaxBot(depth, useQS)` class
- 🌐 **Network multiplayer**
- ⏱️ **Chess clock / timer**
- 💾 **Save & Load game** (PGN format)
- 📖 **Opening book** for stronger early-game play

---

## Poster Design Notes

- **Font sizes:** Title ≥ 72pt · Section headings ≥ 48pt · Body text ≥ 28pt (readable from 1m)
- **Color palette:** Dark background (navy / charcoal) + white text + gold accents (chess-themed)
- **Whitespace:** Leave generous margins between panels — avoid "wall of text"
- **Team photo / QR code:** *(Optional)* Add a QR code linking to the GitHub repo or demo video in the bottom corner
- **Diagrams:** Prefer visual flowcharts / UML over raw code blocks
- **File format:** Export final poster as PDF at A0 size before submission
