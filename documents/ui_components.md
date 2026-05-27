# Chess Game UI Components (Detailed Technical Breakdown)

This document provides a highly detailed, component-level breakdown of how the Graphical User Interface is constructed in `ChessBoardUI.java`. The entire application relies purely on standard Java Swing (`javax.swing.*`) and Java 2D Graphics (`java.awt.*`) without external UI libraries.

## 1. The Main Window Container
The application is hosted inside `ChessBoardUI`, which directly extends `javax.swing.JFrame`.
- **Primary Layout:** The frame uses a `BorderLayout` (`setLayout(new BorderLayout())`).
- **Resizability:** The frame enforces a fixed size (`setResizable(false)`) and calls `pack()` to automatically size the window based on the preferred sizes of its children, guaranteeing perfect pixel alignments for the 80x80 chess squares.

## 2. The Board Panel (`BorderLayout.CENTER`)
The left side of the window is the actual chess board. It is constructed using an inner class named `BoardPanel` that extends `JPanel`. It is added to the `BorderLayout.CENTER` of the main `JFrame`.

### 2.1 The Rendering Loop (`paintComponent`)
Instead of using 64 individual `JButton` or `JLabel` components for the squares, the board is entirely custom-drawn for performance and maximum visual control. We override `protected void paintComponent(Graphics g)`:

- **The Checkerboard Grid:** 
  A nested `for` loop iterates through `row` (0-7) and `col` (0-7). It uses `g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE)` to draw the squares. 
  - Light squares use `new Color(240, 217, 181)`
  - Dark squares use `new Color(181, 136, 99)`

- **Drawing Rank and File Coordinates:**
  Coordinates are painted directly onto the edge squares using `g.drawString()`. To ensure readability, the text color contrasts with the square it is drawn on.
  - **Ranks (Numbers 8 to 1):** Drawn on the left edge. The logic checks `if (col == 0)`, and if so, calculates the number via `String.valueOf(8 - row)`. It is drawn at an X-offset of `5` and a Y-offset of `row * TILE_SIZE + 20`.
  - **Files (Letters a to h):** Drawn on the bottom edge. The logic checks `if (row == 7)`, and calculates the char via `(char) ('a' + col)`. It is drawn in the bottom-right corner of the bottom squares.

- **Drawing Pieces:**
  The board reads from `board.getSquare(row, col).getPiece()`. If a piece exists, it fetches the corresponding `BufferedImage` from a `HashMap` and uses `g.drawImage()`.

- **Drawing Drag-and-Drop and Highlights:**
  If a piece is being dragged (tracked by `dragFromRow` and `dragFromCol`), its image is drawn exactly at the `mouseX` and `mouseY` coordinates. 
  Java 2D `Graphics2D (g2)` is used for advanced shapes:
  - Valid empty squares receive a solid green circle: `g2.fillOval(...)`
  - Valid capture squares receive a thick red ring using a `BasicStroke(4)`: `g2.drawOval(...)`

### 2.2 Event Handling
The `BoardPanel` acts as the single receiver for mouse inputs. It attaches an instance of `MouseInputListener` (which implements both `MouseListener` and `MouseMotionListener`). 
- `mousePressed` calculates the grid row/col from pixel coordinates (`y / TILE_SIZE`, `x / TILE_SIZE`).
- `mouseDragged` updates the raw `mouseX` / `mouseY` variables and calls `boardPanel.repaint()` continuously.
- `mouseReleased` triggers the game logic (`tryMove()`).

## 3. The Side Control Panel (`BorderLayout.EAST`)
The right side of the window is a dedicated `JPanel` named `sidePanel`. It also utilizes a `BorderLayout` internally and has a fixed width (`setPreferredSize(new Dimension(300, ...))`) with an `EmptyBorder` for padding.

### 3.1 Move Log Section
- **Title:** A `JLabel` ("Move Log") added to `BorderLayout.NORTH`.
- **Text Area:** A `JTextArea` (`moveLogArea`) set to `setEditable(false)` with a `"Monospaced"` font to align columns perfectly.
- **Scrolling:** The `JTextArea` is wrapped inside a `JScrollPane` (`new JScrollPane(moveLogArea)`), which is then added to `BorderLayout.CENTER`. This ensures the long list of moves is scrollable.

### 3.2 Action Buttons Section
At the bottom of the `sidePanel`, added to `BorderLayout.SOUTH`, is another `JPanel` named `buttonPanel`.
- **Layout:** It uses `GridLayout(4, 1, 0, 5)` — creating 4 rows and 1 column, with a vertical gap of 5 pixels.
- **Components:**
  1. `statusLabel` (`JLabel`): Displays "Turn: White" or "Bot is thinking...".
  2. `undoButton` (`JButton`): Calls the `undo()` method.
  3. `surrenderButton` (`JButton`): Sets `gameOver = true` and triggers a dialog.
  4. `newGameButton` (`JButton`): Disposes the current `JFrame` (`this.dispose()`) and re-invokes `ChessBoardUI.main(new String[]{})` for a clean restart.

## 4. Modal Dialogs (`JOptionPane`)
The game relies on `JOptionPane` for blocking popups.
- **Initial Setup:** When `main()` runs, it calls `JOptionPane.showOptionDialog` multiple times to capture the desired game mode and bot difficulty before instantiating the `JFrame`.
- **Pawn Promotion:** Handled by `JOptionPane.showOptionDialog` returning an integer corresponding to the user's selected piece (Queen, Rook, Bishop, Knight).
- **Thread Safety:** Game-over dialogs (Checkmate, Stalemate, Surrender) are wrapped inside `javax.swing.SwingUtilities.invokeLater(() -> { promptNewGame(); })`. This guarantees that the EDT finishes rendering the final board state (so the user actually sees the checkmating move on screen) before the modal dialog pauses the application.
