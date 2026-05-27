# Final Report Feedback & Gap Analysis

I have reviewed the submitted PDF report (`Group_17_Oriented_Object_Programming.pdf`) and compared it against the updated `report_outline.md` which accurately reflects the latest codebase. 

The submitted PDF appears to be based on an **older version** of the project or an outdated draft, as it completely misses the major advanced features we recently implemented.

Here is the detailed list of missing points and discrepancies that need to be updated in the final report before submission:

---

### 1. Missing Bots and AI Features (Critical)
The most significant gap is that the PDF only acknowledges two bots, completely omitting the advanced AI work:
- **PDF Says:** Mentions only `BeginnerBot` (depth 3) and `AmateurBot` (depth 5). In Section 4.4, it explicitly states: *"The evaluation function focuses only on material balance... ignores positional concepts"*. Section 8 lists Quiescence Search and positional analysis as "Future Work".
- **What's Missing:**
  - **IntermediateBot** (depth 6) and **HardBot** (depth 6).
  - **Piece-Square Tables (PSTs):** The implementation of 12 positional tables (opening and endgame for each piece) for advanced board evaluation.
  - **Quiescence Search:** The tactical stability search used by the `HardBot` to eliminate the horizon effect.
  - **Move Ordering Enhancements:** Adding PST delta to move scores for positional ordering in the advanced bots.

### 2. Missing Core Mechanics: En Passant
- **PDF Says:** Mentions castling and pawn promotion in Core Functionalities (Section 2.1), but completely omits En Passant.
- **What's Missing:** Documentation of En Passant mechanics, including how it is tracked via `Board.enPassantTarget` and validated with strict color checks inside `Pawn.isValidMove()`.

### 3. Missing Technical Challenges (The Ghost Pawn Bug)
- **PDF Says:** Section 7.1 discusses UI flickering, check detection simulation, and castling edge cases.
- **What's Missing:** The critical **"Ghost Pawn Bug"** (Minimax state corruption). The report should document the challenge where tracking ephemeral En Passant target squares during recursive search allowed pawns to capture their own color's "ghosts", causing array corruption and crashes, and how it was solved via strict color validation and default `-1` values in `boardToIndex`.

### 4. Architecture and Class Summaries
- **PDF Says:** Architecture diagrams and class lists (Sections 3.1, 3.2, 3.3, 5.2) only list `BeginnerBot` and `AmateurBot`.
- **What's Missing:**
  - `IntermediateBot` and `HardBot` must be added to the UML diagram, architecture lists, and file structure (`src/lab/IntermediateBot.java`, `src/lab/HardBot.java`).
  - Class summary table must be updated to include the two new bot classes and their responsibilities.

### 5. Data Structures & Complexity
- **PDF Says:** Lists standard data structures like `Square[][]`, `activePieceCoords[]`, and `Stack<GameState>`.
- **What's Missing:** 
  - The 2D arrays used for Piece-Square Tables (`int[][] PIECE_OPENING` and `PIECE_ENDGAME`).
  - Space/Time complexity rows for PST lookup (O(1) per piece) and PST evaluation (O(n) for n active pieces).

### 6. Threading Implementation Details
- **PDF Says:** Section 5.3 vaguely mentions that the UI "renders from a temporary board snapshot".
- **What's Missing:** Explicit mention of the `pieceSnapshot[][]` variable and the `drawPiecesFromSnapshot()` method used in the `paintComponent` to prevent flickering.

### 7. Performance Evaluation Updates
- **PDF Says:** Only lists think times for Beginner and Amateur bots.
- **What's Missing:** Performance metrics (think times) for `IntermediateBot` and `HardBot`, which search at depth 6 and utilize Quiescence Search.

---

### Conclusion
The current PDF report severely undersells the technical complexity of the final project. Updating the report to include the **Piece-Square Tables**, **Quiescence Search**, and the **4-tier bot architecture** will drastically improve the project's perceived technical depth and grade.
