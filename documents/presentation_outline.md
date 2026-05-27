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

## Recommended Duration & Structure (Target: 5–8 minutes)

| Section | Slides | Est. Time |
|---|---|---|
| Title, Team Intro & Overview | 1 | ~30 sec |
| System Features & Workflow | 1 | ~45 sec |
| **Live System Demo** | (screen recording) | ~2.5 min |
| OOP & Key Architecture | 1 | ~1 min |
| AI Algorithm (Minimax) & Data Structures | 2 | ~1.5 min |
| Testing & Limitations | 1 | ~45 sec |
| Conclusion | 1 | ~30 sec |
| **Total** | **~7 slides** | **~7 min** |

---

## Slide-by-Slide Outline

---

### Slide 1 — Title & Project Overview

**Heading:** Chess Game with AI Opponents (Java Swing)

**Content:**
- **Title:** Chess Game with AI Opponents (Java Swing)
- **Team Info:** Names of all members
- **The Problem:** Chess has ~10¹²⁰ possible game states; requires an efficient algorithm.
- **Our Solution:** A rule-complete interactive chess engine in Java with an AI opponent powered by Minimax + Alpha-Beta Pruning.

**Speaker Notes:**
> "Hello, we are Team [Name]. Our project is a fully functional Chess application. We chose chess because it provides a strong opportunity to apply OOP architecture and complex data structures like game trees."

---

### Slide 2 — System Features & Workflow

**Heading:** Key Features & Workflow

**Content:**
- **Game Modes:** 2-Player Local and 1-Player (Bot).
- **Core Features:** Validated movement, castling, promotion, checkmate detection.
- **UI & UX:** Drag-and-drop, Board Coordinates, algebraic Move Log.
- **Robustness:** Undo (Memento Pattern), Surrender, and New Game buttons. UI threads safely managed to prevent freezing during bot calculations.

**Speaker Notes:**
> "Our application goes beyond basic movement. We implemented standard FIDE rules, a tabular move log, and an Undo system. We heavily focused on making the UI robust by isolating the bot calculations on a background thread so the interface never freezes."

---

### Slide 3 — Live System Demonstration

**Heading:** 🎮 Live System Demonstration

**Content:**
- *(Switch to the running application for the 2.5-minute screen recording)*

**Demo Script:**
1. Select Mode & Difficulty (e.g., 1-Player Amateur).
2. Show legal move highlights and board coordinates.
3. Make a human move, let the bot respond, point out the algebraic Move Log.
4. Demonstrate Undo (show how it rewinds both the bot and human).
5. Trigger Check/Checkmate or use the Surrender button.

**Speaker Notes:**
> "Now let's see the application running. I'll walk you through a quick gameplay sequence highlighting our move validation, Undo system, and AI responsiveness."

---

### Slide 4 — OOP & Key Architecture

**Heading:** 🔷 OOP Concepts & Architecture

**Content:**
- **Encapsulation:** Board state hidden; accessed via `getSquare()`.
- **Inheritance:** All 6 piece types inherit from the abstract `Piece` base class.
- **Polymorphism:** The board validates any piece uniformly via `piece.isValidMove()`.
- **Abstraction:** The `ChessBot` interface allows hot-swapping AI difficulty levels cleanly.
- **Key Decision (Memento):** The Undo system takes a deep-copy snapshot (`GameState`) of the board and piece states before every move, allowing perfect restoration.

**Speaker Notes:**
> "Our architecture heavily relies on OOP. The `Piece` hierarchy allows the engine to be completely agnostic of piece types. We also used the Memento Pattern to implement the Undo system safely."

---

### Slide 5 — AI Algorithm & Data Structures

**Heading:** 🤖 Minimax AI & Data Structures

**Content:**
- **Algorithm:** Minimax with Alpha-Beta Pruning (reduces branching factor from O(b^d) to O(b^(d/2))).
- **Move Ordering:** Sorting moves (promotions/captures first) dramatically improves pruning cutoffs.
- **Data Structures:** 
  - `Square[][]` for the grid (O(1) lookups).
  - `activePieceCoords[]` flattens the board to prevent O(64) scans during check detection.
  - `Stack<GameState>` manages LIFO move history for Undo.
  - Explicit King tracking (`whiteKingSquare`) allows O(1) King lookups.

**Speaker Notes:**
> "Our AI relies on Minimax with Alpha-Beta Pruning. To make this fast, we implemented strict move ordering and highly optimized data structures, like maintaining a flat array of active pieces to avoid scanning all 64 squares millions of times."

---

### Slide 6 — Testing & Limitations

**Heading:** 🧪 Testing & Known Limitations

**Content:**
- **Testing:** `TestRunner` unit tests validate piece movement rules in isolation. Manual testing confirmed castling, promotion, and checkmate accuracy. UI thread synchronization was strictly tested.
- **Limitations:**
  - En passant and 50-move draw rules are not implemented.
  - Bot evaluation is material-only (no piece-square positional heuristics).
  - No persistent save/load features.

**Speaker Notes:**
> "We utilized unit tests to ensure our piece movement logic was flawless. While our engine is robust, there are a few limitations like missing en passant and relying solely on material evaluation rather than positional heuristics."

---

### Slide 7 — Conclusion

**Heading:** 🏁 Conclusion

**Content:**
- **Achievements:** Delivered a fully rule-complete, responsive chess engine.
- **Lessons Learned:** Proper UI threading is critical; OOP abstractions made the system easy to extend; algorithm optimizations (Alpha-Beta, active piece arrays) drastically improved performance.

**Speaker Notes:**
> "In conclusion, this project allowed us to bridge OOP design with intense algorithm optimization. Thank you for watching, and we're ready for any questions!"

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
