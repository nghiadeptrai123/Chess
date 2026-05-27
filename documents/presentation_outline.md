# COMP1020 OOP & Data Structures — Presentation Outline
**Course:** COMP1020 Object-Oriented Programming & Data Structures, Spring 2026  
**Project:** Chess Game with AI Opponents (Java Swing)  
**Submission Deadline:** 11:59 PM, June 2, 2026 (recorded video)  
**Format:** Recorded video — must include live running system demo + clear narration  
**Requirement:** All team members actively participate

---

> **Guideline Reminder:**  
> The presentation must integrate **technical explanation** and **system demonstration**.  
> Highlight **key design decisions** and **implementation outcomes**.  
> A screen recording where the **application interface and text can be viewed clearly** is required.

---

## Recommended Duration & Structure

| Section | Slides | Est. Time |
|---|---|---|
| Title & Team Introduction | 1 | ~30 sec |
| Project Overview & Motivation | 2 | ~1 min |
| System Features & Workflow | 2 | ~1.5 min |
| **Live System Demo** | (screen recording) | ~3–4 min |
| OOP Concepts & Design Decisions | 3 | ~2 min |
| Data Structures & Algorithms | 3 | ~2.5 min |
| Testing & Evaluation | 2 | ~1.5 min |
| Project Limitations | 1 | ~45 sec |
| Conclusion & Q&A Prep | 1 | ~30 sec |
| **Total** | **~15 slides** | **~13–15 min** |

---

## Slide-by-Slide Outline

---

### Slide 1 — Title Slide

**Content:**
- Title: **"Chess Game with AI Opponents"**
- Subtitle: *COMP1020 OOP & Data Structures — Spring 2026 Final Project*
- Team name & all member names
- Date of recording

**Presenter:** *(Team intro — all members briefly introduce themselves)*

**Speaker Notes:**
> "Hello everyone, we are Team [Name], and today we'll be presenting our final project: a fully functional Chess game with multiple AI difficulty levels, built entirely in Java using OOP principles and data structures concepts."

---

### Slide 2 — Project Overview & Motivation

**Heading:** What We Built & Why

**Content (3 columns or visual layout):**

**🎯 The Problem**
- Chess has ~10¹²⁰ possible game states — building a rule-complete engine is algorithmically challenging
- Designing an AI that plays chess requires efficient search algorithms

**💡 Our Solution**
- A fully interactive chess application in Java Swing
- Rule-enforcing engine covering all FIDE standard rules
- AI opponents powered by Minimax + Alpha-Beta Pruning

**✅ What We Achieved**
- 2 game modes: Human vs. Human, Human vs. Bot
- 4 difficulty levels: Beginner, Amateur, Intermediate, Hard
- Complete chess rules: castling, pawn promotion, check, checkmate, stalemate

**Speaker Notes:**
> "Our motivation was to combine OOP design principles we learned in class with a real algorithmic challenge. Chess is a well-known game but implementing it correctly, especially the AI, requires careful architecture and algorithm design."

---

### Slide 3 — System Features & Workflow

**Heading:** System Features at a Glance

**Content (feature icons + short descriptions):**

| Feature | Description |
|---|---|
| 🖱️ Drag-and-drop | Click and drag pieces; valid destinations highlighted in real time |
| 🟢 Move highlighting | Green dots = empty valid squares; Red ring = capturable enemy |
| 📊 Board Coordinates | Algebraic notation (1-8, a-h) explicitly drawn on board squares |
| 📋 Move log | Real-time tabular log with algebraic notation (e.g., `e4`, `Nf3`, `O-O`) |
| ↩️ Undo | Memento-pattern Undo reverts the last move (double-undo in single-player) |
| 🏰 Castling | Both kingside (`O-O`) and queenside (`O-O-O`) — fully validated |
| ♟️ Pawn Promotion | Player dialog for human; auto-queen for bot |
| 🤖 Bot AI | Background-threaded; board frozen during calculation to prevent flicker |
| 🏳️ Surrender & New Game | Graceful game end options with `SwingUtilities.invokeLater` preventing UI freeze |

**Workflow Diagram (insert a simple flowchart):**
```
Game Start (Mode/Difficulty Menu)
         │
    ┌────▼────────────────────────┐
    │  Human's Turn               │
    │  Drag piece → tryMove()     │
    │  Validate: isValidMove()    │
    │  Check: willMoveResultIn    │
    │  Check? → Alert             │
    │  Checkmate? → Game Over     │
    └────────────────┬────────────┘
                     │ (Single-Player)
              ┌──────▼──────────────┐
              │  Bot's Turn         │
              │  Background Thread  │
              │  Minimax + α-β      │
              │  executeBotMove()   │
              └──────┬──────────────┘
                     │
              Repeat until Checkmate / Stalemate / Surrender
```

**Speaker Notes:**
> "Here's an overview of the game's workflow. After the human makes a move, the engine validates it — checking piece rules, checking for self-check, and detecting checkmate or stalemate. In single-player mode, the bot then runs on a background thread so the UI stays responsive."

---

### Slide 4 — Live Demo Introduction

**Heading:** 🎮 Live System Demonstration

**Content:**
- *This slide introduces the demo — switch to the running application for the screen recording*

**Demo Script (narrate while playing):**

1. **Start the game** — show the mode selection dialog (1-Player / 2-Player)
2. **Select 1-Player → Beginner** difficulty
3. **Show opening position** — describe the board layout (ranks, files, piece images)
4. **Make a human move** — drag a pawn, show the green legal-move dots
5. **Attempt an illegal move** — show the check alert / rejection behavior
6. **Bot responds** — narrate "The bot runs Minimax to depth 3 in a background thread..."
7. **Show the move log** — point to algebraic notation being logged
8. **Demonstrate castling** — set up and perform a castling move
9. **Demonstrate pawn promotion** — advance a pawn and show the promotion dialog
10. **Trigger checkmate / surrender** — show the game-over dialog
11. **Undo demonstration** — click Undo and show both moves being reversed
12. **(Optional)** Switch to Amateur difficulty and compare response time

**Speaker Notes:**
> "Now let's see the application running. I'll walk you through the key features live."

---

### Slide 5 — System Architecture

**Heading:** 🏗️ System Architecture & Module Design

**Content:**

**Architecture Diagram:**

```
┌─────────────────────────────────────────────────┐
│  ChessBoardUI (View + Controller, extends JFrame)│
│    BoardPanel  │  MoveLog Panel  │ Button Panel  │
│    ────────────────────────────────────────────  │
│    ┌──────────┐  ┌─────────────┐  ┌──────────┐  │
│    │  Board   │  │GameController│ │ChessBot  │  │
│    │ (Model)  │  │(Turn/Mode)  │  │(Interface│  │
│    └──────────┘  └─────────────┘  └──────────┘  │
│         │                              │         │
│    Square[8][8]              BeginnerBot/AmateurBot│
│    activePieceCoords[]                           │
│         │                                        │
│    ┌────┴──────────────────────────────────┐     │
│    │  Piece (abstract)                     │     │
│    │  King │ Queen │ Rook │ Bishop │ Knight │ Pawn│
│    └────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘
         │                  │
  MouseInputListener     MoveHelper (static utilities)
```

**Key Design Decision — `ChessBot` Interface:**
- `ChessBoardUI` only calls `activeBot.getBestMove(board, isWhite)` — it doesn't know which bot is active
- Adding a new difficulty level = creating a new class that implements `ChessBot` (Open/Closed Principle)

**Speaker Notes:**
> "We designed the system around clean OOP principles. The `ChessBoardUI` acts as both the View and Controller. The `Board` is the Model. The AI is abstracted behind a `ChessBot` interface — the game doesn't care which difficulty is active, it just asks the bot for its best move."

---

### Slide 6 — OOP Concepts Applied

**Heading:** 🔷 OOP Principles in Action

**Content (4-quadrant layout):**

#### 🔒 Encapsulation
```java
// Piece.java - private fields, public getters only
private boolean isWhite;
private boolean isMoved = false;

public boolean isWhite() { return isWhite; }
public boolean isMoved() { return isMoved; }
```
- Board state is accessed only via `getSquare(row, col)` — internal array is hidden.

#### 🔗 Inheritance
```java
// All pieces share the Piece base class
public class King extends Piece { ... }
public class Queen extends Piece { ... }
public class Pawn extends Piece { ... }
// Shared: isWhite, isMoved, setMoved, clonePiece (abstract)
```

#### 🔄 Polymorphism
```java
// Board can validate any piece uniformly:
if (piece.isValidMove(board, startSquare, endSquare)) { ... }
// Whether piece is a King, Pawn, or Knight — same call
```

#### 🧩 Abstraction
```java
// ChessBot interface — hides entire AI algorithm
public interface ChessBot {
    Move getBestMove(Board board, boolean isWhite);
}
// Game only knows: "give me your best move"
```

**Speaker Notes:**
> "Let's walk through the four OOP pillars in our code. Encapsulation keeps our piece state safe. Inheritance avoids code duplication across 6 piece types. Polymorphism lets the board engine treat any piece uniformly. And abstraction through the `ChessBot` interface decouples the game loop from the AI implementation."

---

### Slide 7 — Data Structures

**Heading:** 📊 Data Structures & Why We Chose Them

**Content:**

| Data Structure | Location | Purpose | Complexity |
|---|---|---|---|
| `Square[8][8]` 2D array | `Board` | Grid representation | O(1) cell access |
| `Square` references | `Board` | Direct King tracking (`whiteKingSquare`) | O(1) King lookup |
| `int[] activePieceCoords` | `Board` | Flat list of active piece positions | O(n) iteration, n ≤ 32 |
| `int[] boardToIndex` | `Board` | Reverse map: position → list index | O(1) removal |
| `Stack<GameState>` | `ChessBoardUI` | Undo history (LIFO) / Memento | O(1) push/pop |
| `ArrayList<Move>` | Bot classes | Temporary move list per search node | O(1) append |
| `HashMap<String, BufferedImage>` | `ChessBoardUI` | Piece image cache | O(1) lookup |

**Highlight — Active Piece Optimization:**

```
Without optimization: scan all 64 cells every check detection
With activePieceCoords[]:
  → Only iterate n ≤ 32 active pieces (50% reduction minimum)
  → O(1) removal using swap-and-decrement with boardToIndex[]
```

**Visual:** Before/after comparison diagram of naive 64-cell scan vs. active-piece list.

**Speaker Notes:**
> "One key optimization is our active piece list. Instead of scanning all 64 squares to find pieces during check detection or bot evaluation, we maintain a compact list of only the occupied squares. This cuts unnecessary iteration roughly in half and allows O(1) piece removal."

---

### Slide 8 — Minimax + Alpha-Beta Algorithm

**Heading:** 🤖 AI Algorithm: Minimax + Alpha-Beta Pruning

**Content:**

**Concept:**
- Chess AI frames the game as a **two-player zero-sum game tree**
- **White = Maximizing player** (wants highest evaluation)
- **Black = Minimizing player** (wants lowest evaluation)

**Minimax Tree Diagram (depth 3 example):**
```
              MAX (White, depth 3)
            /          |          \
         MIN           MIN         MIN  (Black, depth 2)
        / \            |           / \
      MAX MAX        MAX         MAX  MAX  (White, depth 1)
      [+3] [-1]     [+2]        [+5] [-2]
```

**Alpha-Beta Pruning:**
- `alpha` = best guaranteed score for MAX
- `beta` = best guaranteed score for MIN
- When `β ≤ α` → prune the rest of the branch
- Best case: O(b^(d/2)) instead of O(b^d)

**Board Evaluation Function:**
```
Score = Σ(White piece values) − Σ(Black piece values)
Queen=900  |  Rook=500  |  Bishop=300  |  Knight=300  |  Pawn=100
```

**Move Ordering (MVV-LVA):**
> Prioritize promotions (+8000) and high-value captures (`10×victim − attacker`) to improve pruning cutoffs.

**Speaker Notes:**
> "The heart of our AI is the Minimax algorithm. It builds a game tree to depth d and evaluates positions based on material balance. Alpha-Beta Pruning cuts branches that can't affect the final result, making deeper search practical. We also sort moves by likely importance before searching — this is called move ordering, and it dramatically improves pruning efficiency."

---

### Slide 9 — Key Implementation Decisions

**Heading:** 💡 Key Technical Decisions & Why

**Content (3 decision blocks):**

#### Decision 1: Background Thread for Bot
**Problem:** Bot calculation (depth 5) can take several seconds — this would freeze the GUI.  
**Solution:** Bot runs on a new `Thread()`; result is delivered to the UI thread via `SwingUtilities.invokeLater()`.  
**Extra:** Board snapshot taken before bot starts → prevents mid-simulation mutations from flickering on screen.

#### Decision 2: Board Snapshot Pattern
**Problem:** Bot's Minimax temporarily mutates the `Board` object to simulate moves — if the UI repaints mid-simulation, pieces appear to teleport.  
**Solution:** `takeBoardSnapshot()` freezes a copy of piece positions; `drawPiecesFromSnapshot()` is used during `botThinking = true`.

#### Decision 3: Memento Pattern for Undo
**Problem:** Undo must completely restore the board state, including piece states like `isMoved`.  
**Solution:** `saveState()` deep-copies all 64 squares (calling `clonePiece()` on each piece) into a `GameState` object pushed onto a `Stack`.

#### Decision 4: Fixing UI Thread Blocking
**Problem:** The `JOptionPane` for Checkmate would block the Java Swing EDT, causing the final board state repaint to freeze.
**Solution:** Checkmate UI popups are wrapped in `SwingUtilities.invokeLater()` so that the final move is fully painted before the dialog blocks interaction.

**Speaker Notes:**
> "These were three of the most technically interesting decisions we made. Each solved a real problem we encountered during development."

---

### Slide 10 — Testing & Evaluation

**Heading:** 🧪 Testing & Results

**Content:**

### Testing Strategy
- **Unit testing:** `TestRunner.java` — directly tests `isValidMove()` for each piece
- **Scenario testing:** Manual play-throughs of Fool's Mate, Scholar's Mate, stalemate, castling edge cases
- **Regression testing:** After each major feature, re-ran all previous test scenarios

### Test Case Results

| Scenario | Status |
|---|---|
| All piece movement rules | ✅ Verified |
| Blocked path detection (Rook/Bishop/Queen) | ✅ Pass |
| King cannot move into check | ✅ Pass |
| Castling: valid / invalid (attacked square) | ✅ Pass |
| Checkmate: Fool's Mate (2 moves) | ✅ Detected |
| Stalemate detection | ✅ Detected |
| Pawn promotion (human + bot) | ✅ Pass |
| Undo: 2-player / single-player | ✅ Pass |
| Bot move after human undo | ✅ Re-triggered correctly |
| UI responsiveness during bot calculation | ✅ No freeze observed |

### Bot Performance

| Difficulty | Depth | Avg. Response Time |
|---|---|---|
| Beginner | 3 | < 1 second |
| Amateur | 5 | ~1–5 seconds |
| Intermediate | 4 + QS | *(record)* |
| Hard | 5 + QS | *(record)* |

**Visual:** Horizontal bar chart of response time by difficulty.

**Speaker Notes:**
> "We tested every major rule systematically using our TestRunner class and through extensive manual play sessions. All standard chess rules pass, and the bot performs within acceptable time limits at all difficulty levels."

---

### Slide 11 — Project Limitations

**Heading:** ⚠️ Current Limitations

**Content (table format):**

| Limitation | Impact | Potential Fix |
|---|---|---|
| **En passant not implemented** | Rare but legal pawn capture missing | Add en passant check in `Pawn.isValidMove()` |
| **No draw rules** (50-move, threefold repetition) | Game may run indefinitely in some endgames | Track move counter and position history |
| **Material-only evaluation** | Bot ignores positional factors (center control, king safety) | Implement piece-square tables |
| **Bot code duplication** | `BeginnerBot` and `AmateurBot` differ only in `SEARCH_DEPTH` | Refactor into a single `MinimaxBot(depth, useQS)` class |
| **No save/load** | Games cannot be resumed after closing | Serialize `GameState` to file (PGN format) |
| **No network play** | 2-player requires same machine | Socket-based multiplayer |

**Speaker Notes:**
> "We're honest about what we didn't implement. These are known gaps — some by design due to time constraints, others that we'd tackle as natural next steps."

---

### Slide 12 — Conclusion

**Heading:** 🏁 Conclusion

**Content:**

### What We Built
> A fully functional, rule-complete **Chess game in Java** with:
> - Clean OOP architecture (abstraction, inheritance, polymorphism, encapsulation)
> - Efficient board engine with active-piece optimization
> - Minimax + Alpha-Beta Pruning AI across 4 difficulty levels
> - Robust threading, undo system, and smooth GUI experience

### What We Learned
- **OOP design pays off:** The `ChessBot` interface made adding difficulty levels trivial; the `Piece` hierarchy let the engine treat all pieces uniformly.
- **Algorithms matter:** Alpha-Beta Pruning + Move Ordering made depth-5 search feasible.
- **Debugging concurrent code is hard:** Board snapshot + threading discipline were essential to prevent subtle bugs.
- **Testing early saves time:** Building `TestRunner.java` from day one caught move-validation bugs before they compounded.

### Team Reflection
> *(Each team member briefly shares one thing they learned or contributed.)*

**Speaker Notes:**
> "In conclusion, this project gave us hands-on experience applying every major concept from the course — from OOP design to data structure selection to algorithm implementation — all within a real, interactive application. Thank you for watching, and we're happy to answer any questions."

---

## Recording & Submission Checklist

- [ ] All team members appear in the recording (camera or voice over)
- [ ] Application is running and clearly visible (use screen recording at 1080p+)
- [ ] Narration is clear and well-paced
- [ ] Demo includes: legal move highlighting, castling, promotion, bot move, checkmate/stalemate
- [ ] All 4 difficulty levels mentioned
- [ ] OOP concepts (encapsulation, inheritance, polymorphism, abstraction) explicitly named
- [ ] Data structures and Minimax explained with visuals
- [ ] Limitations acknowledged
- [ ] Video exported and submitted to Canvas by **11:59 PM, June 2, 2026**

---

## Suggested Slide Design Notes

- **Font:** Inter or Roboto (clean, modern) — minimum 18pt for body, 28pt+ for headings
- **Color scheme:** Dark navy (`#0d1b2a`) background, white text, gold (`#f0c040`) accents — chess-themed
- **Code snippets:** Use a monospace font (JetBrains Mono / Fira Code) on a dark code block background
- **Diagrams:** Prefer clean box diagrams over raw text trees; tools like draw.io or Canva work well
- **One key idea per slide** — avoid cramming; use progressive reveals if available
- **Slide transitions:** Subtle (fade) — avoid distracting animations in a technical presentation
