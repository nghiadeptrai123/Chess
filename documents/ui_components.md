# Chess Game UI Components

This document provides a comprehensive overview of all Graphical User Interface (GUI) components implemented in the Chess Game application. The UI is built entirely using Java Swing and custom graphics rendering.

## 1. Main Window (`JFrame`)
The application is hosted within a primary `JFrame` named `"Chess Game"`. 
- **Layout:** The frame uses a `BorderLayout` to separate the visual chess board (Center) from the side control panel (East).
- **Responsiveness:** The window is set to be non-resizable to ensure the grid layout and piece drag-and-drop coordinates remain perfectly aligned without complex dynamic scaling.

## 2. The Chess Board Panel (`JPanel`)
The core visual component is a custom `JPanel` where the `paintComponent(Graphics g)` method is overridden to handle all game rendering.

### 2.1 Grid and Board Coordinates
- **Checkerboard Grid:** The 8×8 grid is drawn by iterating through rows and columns, alternating between a light wood color (`#F0D9B5`) and a dark wood color (`#B58863`).
- **Algebraic Coordinates:** Letters (`a` to `h`) are drawn along the bottom edge (Files), and numbers (`8` to `1`) are drawn along the left edge (Ranks). The font is customized (Arial, bold, 14pt) and colored appropriately to contrast against the dark/light squares.

### 2.2 Piece Rendering
- **Images:** High-quality PNG images of chess pieces are loaded into a `HashMap<String, BufferedImage>` at startup.
- **Placement:** Pieces are rendered perfectly centered within their respective 80x80 pixel squares based on the internal `Square` coordinates.
- **Drag-and-Drop Ghosting:** When a piece is actively being dragged by the user, its original square is left empty (or a ghost image is hidden), and the piece image is redrawn exactly at the user's mouse coordinates in real-time, giving a smooth physical drag sensation.

### 2.3 Visual Highlights
The UI uses custom Java 2D graphics (anti-aliased shapes and strokes) to provide immediate feedback to the player:
- **Selected Piece Highlight:** A semi-transparent yellow highlight (`new Color(255, 255, 0, 128)`) illuminates the square of the piece currently being dragged.
- **Previous Move Highlight:** The start and end squares of the last move made (by either player or bot) are also highlighted in pale yellow, helping the player track game flow.
- **Legal Move Indicators:**
  - **Empty Squares:** Valid destination squares that are empty are highlighted with a solid green circle (`#4CAF50`) in the center.
  - **Captures:** Valid destination squares containing an enemy piece are highlighted with a thick red ring (`#F44336`), indicating a valid attack.

## 3. Side Control Panel
To the right of the chess board is a fixed-width `JPanel` containing game logs and controls. It utilizes a `BorderLayout` and `BoxLayout` to stack components vertically.

### 3.1 Status Label (`JLabel`)
- Located at the top of the panel.
- Displays the current game state, such as `"Turn: White"`, `"Turn: Black"`, or `"Bot is thinking..."`.
- Updates dynamically and is tied to the `GameController`'s turn logic.

### 3.2 Move Log (`JTextArea` inside `JScrollPane`)
- A read-only text area displaying a running history of the game using standard FIDE Algebraic Chess Notation (e.g., `1. e4 e5`, `2. Nf3 Nc6`).
- **Formatting:** A monospaced font (`Courier New`, 14pt) ensures that white and black moves align perfectly in two columns.
- **Scrolling:** Embedded in a `JScrollPane` that automatically scrolls to the bottom as new moves are appended, ensuring the latest move is always visible.

### 3.3 Action Buttons (`JButton`)
Located at the bottom of the control panel, these buttons allow the user to manage the game flow:
- **Undo Move:** Triggers the Memento-pattern undo system. In single-player mode, it pops two states off the stack to revert both the bot's last move and the human's last move.
- **Surrender:** Immediately ends the game, declares the opponent the winner, and triggers the game-over prompt.
- **New Game:** Resets the board, clears the move log, and triggers the startup sequence without requiring the user to restart the application.

## 4. Startup and Modal Dialogs (`JOptionPane`)
The game relies on standard Java Swing modal dialogs for critical user inputs and alerts.

### 4.1 Startup Sequence
- **Game Mode Selection:** Prompts the user to select between `"1 Player"` (Human vs. Bot) and `"2 Player"` (Human vs. Human).
- **Bot Difficulty Selection:** If "1 Player" is chosen, a follow-up dialog appears allowing the user to select the AI strength: `"Beginner"`, `"Amateur"`, `"Intermediate"`, or `"Hard"`.

### 4.2 In-Game Prompts
- **Pawn Promotion:** When a pawn reaches the opposite end of the board, a dialog appears requiring the player to click a button to promote the pawn to a `"Queen"`, `"Rook"`, `"Bishop"`, or `"Knight"`.
- **Check Alert:** A non-intrusive `WARNING_MESSAGE` dialog pops up briefly when a King is placed in Check.

### 4.3 End-Game Dialogs
- **Checkmate / Stalemate / Surrender:** When the game concludes, a prominent message dialog declares the result (e.g., `"Checkmate! White wins."`).
- **Thread Safety:** All end-game dialogs are explicitly wrapped in `SwingUtilities.invokeLater()` to ensure that the final move is fully rendered on the board before the modal dialog blocks the UI thread.
