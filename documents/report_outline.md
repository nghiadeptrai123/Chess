# COMP1020 OOP & Data Structures — Final Report Outline
**Course:** COMP1020 Object-Oriented Programming & Data Structures, Spring 2026  
**Project:** Chess Game with AI Opponents (Java Swing)  
**Deadline:** 11:59 PM, June 2, 2026  
**Max Pages:** 10 (excluding References & Appendix)

---

## 1. Introduction and Project Overview

### 1.1 Problem Statement and Motivation
- Chess is one of the most complex two-player strategy games, with an enormous search space (~10¹²⁰ possible games). Building a playable chess engine that enforces all rules correctly and provides scalable AI opponents is a rich software engineering challenge.
- Motivation: apply OOP design principles and algorithmic thinking (Minimax, Alpha-Beta Pruning) to a real, interactive application.

### 1.2 Project Objectives and Scope
- **Primary objective:** Develop a fully functional, rule-complete chess game in Java with a graphical user interface (Swing).
- **AI objective:** Implement at least two AI bots of differing difficulties using game-tree search algorithms.
- **Scope:**
  - Standard chess rules: legal piece movement, check/checkmate/stalemate detection, castling, pawn promotion.
  - Two game modes: Human vs. Human (local), Human vs. Bot (single-player).
  - Bot difficulty levels: Beginner (depth 3), Amateur (depth 5), Intermediate (depth 4 + Quiescence Search), Hard (depth 5 + Quiescence Search).

### 1.3 Key Features and Functionalities
- Interactive 8×8 chess board rendered with Java Swing (`ChessBoardUI`).
- Drag-and-drop piece movement with real-time legal move highlighting (green dots / red ring for captures).
- Board explicitly drawn with algebraic coordinates (Ranks 1-8, Files a-h).
- Tabular move log panel displaying real-time algebraic chess notation.
- Undo move functionality (Memento Pattern with double-undo in single-player to revert both bot and human moves).
- Surrender button for graceful game end.
- New Game prompt on game-over (checkmate or stalemate) wrapped in `SwingUtilities.invokeLater` to prevent freezing.
- Bot thinking runs on a background thread to keep the UI responsive; board is frozen from a snapshot during calculation.
- Pawn promotion dialog (Queen, Rook, Bishop, Knight).
- Castling (both kingside and queenside) fully implemented and validated.

### 1.4 Modifications Since Proposal / Interim Stage
- Implemented **Undo** using the **Memento Design Pattern**, ensuring exact state restoration (including `isMoved` flags) via `clonePiece()`.
- Added **Tabular Algebraic Move Logging** and visual **Board Coordinates**.
- Added **Surrender** and **New Game** functionalities.
- Fixed critical Java Swing UI thread blocking issues by wrapping end-game dialogs in `SwingUtilities.invokeLater()`.
- Optimized bot searches by dropping `AmateurBot` depth to 5, pre-caching `whiteKingSquare` and `blackKingSquare` for O(1) lookups, and reusing internal logic objects in the `Queen` class.

---

## 2. System Requirements and Specifications

### 2.1 Core Functionalities
| Feature | Description |
|---|---|
| Legal move validation | Each piece class implements `isValidMove()` independently |
| Check detection | `Board.isChecked()` iterates active pieces to detect attacks on king |
| Move simulation | `Board.willMoveResultInCheck()` temporarily simulates a move to validate legality |
| Checkmate / Stalemate | `Board.isCheckmate()` / `Board.isStalemate()` combine check and legal-move detection |
| Castling | `King.isCastlingMove()` + `King.isValidCastle()` enforce all FIDE castling rules |
| Pawn Promotion | Detected on piece placement; player selects promoted piece via dialog |
| Bot AI | `BeginnerBot` / `AmateurBot` implement `ChessBot` interface via Minimax + Alpha-Beta |
| Undo | `Stack<GameState>` stores deep-copied board snapshots via Memento Pattern; pops 1 or 2 states |
| Move Logging | Algebraic notation generated in `getChessNotation()` and appended sequentially to a tabular UI |

### 2.2 User Requirements and Expected Behaviors
- Player selects game mode (1-player / 2-player) and optionally bot difficulty at startup.
- Pieces respond to drag-and-drop mouse events; invalid moves are silently rejected or shown as alerts.
- Check alert is displayed as a `JOptionPane` message.
- Bot moves are made automatically after the human's move; a 600 ms artificial delay provides feedback.

### 2.3 Performance, Usability, and Scalability
- The game must remain responsive while the bot thinks (background thread via `new Thread()`).
- UI repaints are suppressed during bot calculation to prevent flicker (board snapshot pattern).
- The active-piece list (`activePieceCoords[]` / `boardToIndex[]`) optimizes piece iteration from O(64) to O(n) where n ≤ 32.
- Deeper search depths (Intermediate / Hard) may introduce noticeable think time; this is a known trade-off.

---

## 3. System Design and Architecture

### 3.1 High-Level Architecture and Module Organization

```
ChessBoardUI (View + Controller)
    ├── Board (Model)
    │     ├── Square[8][8]
    │     └── Active Piece Index (int[], int[])
    ├── GameController (Game State)
    ├── MouseInputListener (Input Handling)
    ├── ChessBot (Interface)
    │     ├── BeginnerBot (depth 3)
    │     └── AmateurBot (depth 5)
    └── Piece Hierarchy (Abstract Classes + Concrete)
          ├── Piece (abstract)
          │     ├── King
          │     ├── Queen
          │     ├── Rook
          │     ├── Bishop
          │     ├── Knight
          │     └── Pawn
    MoveHelper (static utility)
    Move (data class)
    Square (data class)
```

### 3.2 Diagrams

> **[Insert UML Class Diagram here]**  
> Key relationships to show:
> - `ChessBoardUI` has-a `Board`, `GameController`, `ChessBot`
> - `Board` has-a `Square[][]`
> - `Square` has-a `Piece` (nullable)
> - `Piece` ← `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn` (inheritance)
> - `ChessBot` ← `BeginnerBot`, `AmateurBot` (interface implementation)

> **[Insert Sequence Diagram: Human Move Flow]**  
> `MouseInputListener.mouseReleased()` → `ChessBoardUI.tryMove()` → `Board.willMoveResultInCheck()` → `Board.isCheckmate()` → `triggerBot()` → background `Thread` → `executeBotMove()`

### 3.3 OOP Concepts, Main Classes, Responsibilities, and Interactions

| Class / Interface | Type | Responsibility |
|---|---|---|
| `Piece` | Abstract Class | Defines the contract for all chess pieces (`isValidMove`, `clonePiece`, `isMoved`) |
| `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn` | Concrete Classes | Implement piece-specific movement rules and `clonePiece()` |
| `Board` | Class | Manages the 8×8 grid, active piece index, check/checkmate/stalemate detection, and move simulation |
| `Square` | Class | Represents a single board cell; holds color, coordinates, and an optional `Piece` reference |
| `GameController` | Class | Tracks whose turn it is and game mode settings (single/multiplayer, bot depth, Quiescence Search flag) |
| `ChessBot` | Interface | Defines `getBestMove(Board, boolean)` — the only method the UI needs to call on any AI |
| `BeginnerBot` | Class (implements `ChessBot`) | Minimax + Alpha-Beta at depth 3; material-only evaluation |
| `AmateurBot` | Class (implements `ChessBot`) | Minimax + Alpha-Beta at depth 5; material-only evaluation |
| `MoveHelper` | Static Utility Class | Reusable helper for `isInCheck`, `willMoveResultInCheck`, `hasLegalMoves`, `isCheckmate`, `isStalemate` |
| `MouseInputListener` | Class (implements `MouseListener`, `MouseMotionListener`) | Translates pixel coordinates into board coordinates and dispatches drag events |
| `ChessBoardUI` | Class (extends `JFrame`) | Main view — renders the board, pieces, highlights; orchestrates game loop |
| `Move` | Data Class | Encapsulates a move (startRow, startCol, endRow, endCol, capturedPiece, isPromotion) |
| `GameState` | Inner Class | Snapshot of board + turn state for undo operations; stored in a `Stack<GameState>` |

---

## 4. Data Structures and Algorithms

### 4.1 Selected Data Structures and Reasons

| Data Structure | Where Used | Reason |
|---|---|---|
| `Square[8][8]` (2D Array) | `Board` | O(1) random access to any cell by (row, col) |
| `Square` references | `Board` | Direct King tracking (`whiteKingSquare`, `blackKingSquare`) for O(1) checks |
| `int[] activePieceCoords` (size 33) | `Board` | Compact flat list of occupied squares; avoids scanning all 64 cells during check detection |
| `int[] boardToIndex` (size 64) | `Board` | Reverse-mapping from board position to index in `activePieceCoords`; enables O(1) removal |
| `Stack<GameState>` | `ChessBoardUI` | LIFO structure for undo (Memento Pattern) — naturally supports multiple undo levels |
| `List<Move>` (ArrayList) | `BeginnerBot`, `AmateurBot` | Dynamic list of legal moves generated per search node |
| `Map<String, BufferedImage>` (HashMap) | `ChessBoardUI` | Key-value lookup for piece images (e.g., `"White King"`) — O(1) retrieval during paint |

### 4.2 Time and Space Complexity

| Operation | Complexity | Notes |
|---|---|---|
| Check detection (`isChecked`) | O(n) | Iterates active piece list (n ≤ 32) |
| Legal move generation | O(n × 64) ≈ O(n) | For each of n active pieces, scans all 64 squares |
| Move simulation (`willMoveResultInCheck`) | O(n) | Temporarily moves a piece and calls `isChecked` |
| Minimax (depth d, branching b) | O(b^d) worst case | Alpha-Beta pruning reduces to O(b^(d/2)) best case |
| Active-piece removal | O(1) | Swap-and-decrement pattern with `boardToIndex` reverse map |
| Undo | O(64) = O(1) | Restores the 8×8 board array from deep-copied snapshot |

### 4.3 Algorithms Implemented

#### Minimax with Alpha-Beta Pruning
- Implemented in `BeginnerBot.minimax()` and `AmateurBot.minimax()`.
- **White** is the maximizing player; **Black** is the minimizing player.
- `alpha` = best score the maximizer can guarantee; `beta` = best score the minimizer can guarantee.
- A branch is pruned when `beta ≤ alpha`.

#### Move Ordering (for Alpha-Beta Efficiency)
- `scoreMove()` assigns a priority to each move:
  - Promotions: +8000
  - Captures: `10 × victim_value − attacker_value` (Most Valuable Victim – Least Valuable Attacker heuristic)
  - Quiet moves: 0
- Legal moves are sorted in descending order before search, improving pruning cutoffs.

#### Board Evaluation
- `evaluateBoard()` returns material balance: `+pieceValue` for white pieces, `−pieceValue` for black pieces.
- Piece values: Queen=900, Rook=500, Bishop=300, Knight=300, Pawn=100.

### 4.4 Trade-offs and Optimization Decisions
- **Positional Heuristics vs. Speed**: `BeginnerBot` and `AmateurBot` use material-only evaluation to remain extremely fast. `Intermediate` and `Hard` bots utilize **Piece-Square Tables** for advanced positional awareness, trading calculation speed for much stronger strategic play.
- **Active piece list** avoids O(64) full-board scans during check detection and bot evaluation; instead O(n) where n ≤ 32.
- **Board snapshot before bot calculation** prevents mid-simulation mutations from flickering on screen — a clean separation between the AI simulation state and display state.

---

## 5. Implementation Details

### 5.1 Programming Language, Libraries, and Frameworks
- **Language:** Java (JDK 11+)
- **GUI:** Java Swing (`JFrame`, `JPanel`, `JOptionPane`, `JScrollPane`, `JTextArea`)
- **Image I/O:** `javax.imageio.ImageIO` for loading PNG piece images
- **Threading:** `java.lang.Thread` for background bot computation + `SwingUtilities.invokeLater()` for EDT-safe UI updates

### 5.2 Important Implementation Details
- **Drag-and-drop:** `MouseInputListener` captures `mousePressed` (set `dragFrom`) and `mouseReleased` (call `tryMove`). Mouse-motion events trigger `refresh()` for smooth highlight repaint.
- **Bot threading model:** When the bot's turn starts, `botThinking = true` is set, a board snapshot is taken, and a `new Thread()` is launched. After calculation, `SwingUtilities.invokeLater()` delivers the result to the Event Dispatch Thread safely.
- **Castling validation:** `King.isValidCastle()` checks: king hasn't moved, rook hasn't moved, no pieces between them, king is not in check, king does not pass through or land on an attacked square.
- **Undo in single-player:** Since the bot is white, undoing one human move leaves it as the bot's turn again. The undo method pops 2 states (both bot and player moves) and re-triggers the bot.
- **Pawn promotion:** Detected during move execution by checking if a Pawn reaches row 0 (black) or row 7 (white). The bot auto-promotes to Queen; human is shown a dialog.

### 5.3 File Structure and Package Organization
```
OOP - Final Project/
├── src/
│   └── lab/
│       ├── ChessBoardUI.java       ← Main game window + game loop
│       ├── Board.java              ← Board model + game state logic
│       ├── Square.java             ← Cell data class
│       ├── GameController.java     ← Turn and mode management
│       ├── Piece.java              ← Abstract piece base class
│       ├── King.java               ← King movement + castling
│       ├── Queen.java              ← Queen movement
│       ├── Rook.java               ← Rook movement
│       ├── Bishop.java             ← Bishop movement
│       ├── Knight.java             ← Knight movement
│       ├── Pawn.java               ← Pawn movement + promotion detection
│       ├── Move.java               ← Move data class
│       ├── MoveHelper.java         ← Static game-state utility methods
│       ├── ChessBot.java           ← AI interface
│       ├── BeginnerBot.java        ← Minimax depth-3 AI
│       ├── AmateurBot.java         ← Minimax depth-5 AI
│       ├── MouseInputListener.java ← Mouse event handler
│       └── TestRunner.java         ← Standalone move-validation tests
├── chess pieces/                   ← PNG assets (12 pieces × 2 colors)
└── documents/                      ← Guidelines + report files
```

### 5.4 External Tools or APIs Used
- No external libraries beyond the Java SE standard library.
- Piece images: custom PNG files stored in `chess pieces/` folder, loaded at startup.

---

## 6. Testing and Evaluation

### 6.1 Test Cases and Testing Methodology
- **`TestRunner.java`** provides unit-style move validation tests by setting up specific board positions and calling `piece.isValidMove()` directly.
- Manual play-testing for:
  - Checkmate scenarios (Fool's Mate, Scholar's Mate)
  - Stalemate detection
  - Castling (both sides, with and without blocking pieces / check)
  - Pawn promotion
  - Undo after bot move

| Test Case | Expected Result | Observed Result |
|---|---|---|
| White Pawn forward 1 square | `true` | ✅ |
| White Pawn backward | `false` | ✅ |
| Rook blocked by own piece | `false` | ✅ |
| Knight L-shape | `true` | ✅ |
| Bishop diagonal through empty | `true` | ✅ |
| King into check | Move rejected | ✅ |
| Kingside castling (valid) | `O-O` logged | ✅ |
| Queenside castling through attacked square | `false` | ✅ |
| Checkmate detection | Game-over dialog shown | ✅ |
| Stalemate detection | Draw dialog shown | ✅ |
| Pawn promotion to Queen | Piece replaced on board | ✅ |
| Undo in 2-player | Previous position restored | ✅ |
| Undo in single-player | Bot + player move reversed | ✅ |

### 6.2 Screenshots / Sample Outputs
> **[Insert screenshots here:]**
> - Opening board with difficulty menu
> - Mid-game with legal move highlights
> - Check alert dialog
> - Pawn promotion dialog
> - Checkmate / game-over dialog
> - Move log showing algebraic notation

### 6.3 Performance Evaluation
- Beginner bot (depth 3): responds in < 1 second on most positions.
- Amateur bot (depth 5): responds in 1–5 seconds depending on position complexity.
- Intermediate / Hard (depth 4–5 + Quiescence Search): may take several seconds in tactical positions. *(Fill in actual measured times.)*

### 6.4 Discussion of Correctness, Robustness, and Usability
- All standard chess rules are correctly implemented and tested.
- The undo system fully restores board state without memory leaks (deep copies via `clonePiece()`).
- The UI remains responsive at all difficulty levels due to the background threading model.
- Usability: the highlighting system clearly communicates legal moves; the move log provides a record of the game.

---

## 7. Challenges and Solutions

### 7.1 Technical Difficulties
- **Check detection during simulation:** Simulating a move requires temporarily mutating the board; failing to correctly revert state caused cascading bugs. **Solution:** Strict save-and-restore pattern in `willMoveResultInCheck()`.
- **Bot causing piece flicker:** The bot's Minimax simulation mutates the board object shared with the UI, causing pieces to appear to teleport during calculation. **Solution:** Board snapshot taken before bot starts; UI renders from snapshot while `botThinking = true`.
- **UI thread blocking on checkmate popups:** The `JOptionPane` for checkmate/surrender was blocking the Java Swing EDT, causing the final graphical board state repaint to freeze before showing the popup. **Solution:** Wrapped end-game dialogs entirely in `SwingUtilities.invokeLater()`.
- **Castling edge cases:** Multiple FIDE conditions (king not in check, cannot pass through attacked square) required careful sequential validation. **Solution:** `isValidCastle()` with explicit checks for each condition.
- **Active piece index management:** Removing a piece mid-list without shifting was non-trivial. **Solution:** Swap-and-decrement with the `boardToIndex[]` reverse map.
- **Minimax State Corruption (The Ghost Pawn Bug):** Managing ephemeral En Passant target squares during recursive Minimax search triggered a critical flaw where pawns could capture their own color's "ghosts". Combined with an uninitialized array index default, this caused the internal `activePieceCoords` array to silently corrupt and overflow, crashing the simulation thread and triggering false stalemates. **Solution:** Enforced strict color validation for En Passant captures (`epPawn.isWhite() != this.isWhite()`) and ensured `boardToIndex` safely defaults to `-1` for empty squares, perfectly preserving array length during deep recursive simulations.

### 7.2 Design or Architectural Issues
- **Bot code duplication:** `BeginnerBot` and `AmateurBot` share identical Minimax logic differing only in depth. **Future improvement:** Extract a single configurable `MinimaxBot` class.
- **God class risk:** `ChessBoardUI` handles both rendering and game logic. **Partial mitigation:** `MoveHelper` offloads static logic; `GameController` handles turn state.

### 7.3 Team Collaboration and Task Coordination
- *(Describe how tasks were divided — e.g., who implemented which piece classes, who built the bot, who built the UI, who wrote tests.)*

### 7.4 Solutions Implemented and Lessons Learned
- Early and frequent play-testing caught rule violations before they became deeply embedded.
- Using an interface (`ChessBot`) for the AI made it trivial to swap difficulty levels at runtime.
- Background threading requires discipline: all UI mutations must go through `SwingUtilities.invokeLater()`.

---

## 8. Conclusion and Limitations

### 8.1 Overall Achievements and Completed Objectives
- A fully playable, rule-complete Java chess game with Swing GUI was delivered.
- Two AI bots (Beginner, Amateur) plus two higher-difficulty options (Intermediate, Hard) were implemented.
- All major chess features (castling, promotion, check/checkmate/stalemate, undo, move log) are functional.

### 8.2 Strengths of the Developed System
- Clean OOP hierarchy: polymorphism via `Piece` abstract class lets the engine handle any piece uniformly.
- Efficient board representation using active piece list — minimizes unnecessary computation.
- Responsive UI through proper threading and board snapshot design.
- Extensible bot architecture: new difficulty levels can be added by implementing `ChessBot`.

### 8.3 Current Limitations or Unresolved Issues
- **Draw by repetition** and **50-move rule** are not enforced.
- `BeginnerBot` and `AmateurBot` share nearly identical code (only differ in `SEARCH_DEPTH`).
- No persistent game save / load functionality.

### 8.4 Possible Future Improvements or Extensions
- Implement the 50-move / threefold repetition draw rules.
- Refactor bots into a single `MinimaxBot(depth, useQS, usePositional)` class.
- Implement Quiescence Search for all difficulty levels.
- Add a proper opening book for the early game.
- Implement networked multiplayer.
- Add a game timer / clock.

---

## 9. Team Contributions

| Team Member | Contributions |
|---|---|
| *(Name 1)* | *(e.g., Board model, check/checkmate/stalemate logic, active piece index)* |
| *(Name 2)* | *(e.g., Piece hierarchy, movement validation, castling, pawn promotion)* |
| *(Name 3)* | *(e.g., ChessBoardUI, drag-and-drop, move log, undo system, bot threading)* |
| *(Name 4)* | *(e.g., BeginnerBot, AmateurBot, Minimax + Alpha-Beta, move ordering, evaluation)* |

---

## 10. References

- Knuth, D. E., & Moore, R. W. (1975). An analysis of alpha-beta pruning. *Artificial Intelligence*, 6(4), 293–326.
- Oracle. (2024). *Java SE 11 API Documentation*. https://docs.oracle.com/en/java/docs/
- Wikipedia. (2024). *Minimax algorithm*. https://en.wikipedia.org/wiki/Minimax
- Wikipedia. (2024). *Alpha-beta pruning*. https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
- Wikipedia. (2024). *Chess rules (FIDE Laws of Chess)*. https://www.fide.com/FIDE/handbook/LawsOfChess.pdf
- Chessprogramming Wiki. (2024). *Move ordering*. https://www.chessprogramming.org/Move_Ordering
- *(Add any additional references: libraries, tutorials, GitHub repos, etc.)*

---

## 11. Appendix

### A. Additional Screenshots
> *(Insert additional screenshots of gameplay, bot difficulty menu, undo demonstration, etc.)*

### B. Extended Test Cases
> *(Insert additional `TestRunner` output, edge-case move validations, etc.)*

### C. Class Diagram (Full)
> *(Insert full UML class diagram if too large for Section 3.)*
