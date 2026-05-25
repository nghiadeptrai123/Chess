package lab;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.imageio.ImageIO;

public class ChessBoardUI extends JFrame {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;

    private Board board;
    private GameController gameController;
    private BoardPanel boardPanel;
    private ChessBot activeBot;

    // Drag state
    private int dragFromRow = -1;
    private int dragFromCol = -1;
    private int dragToRow = -1;
    private int dragToCol = -1;

    // Game state
    private boolean gameOver = false;
    // update -> flag to block human input and repaints while the bot is calculating
    private boolean botThinking = false;
    // update -> frozen snapshot of piece positions taken before the bot starts
    // calculating
    private Piece[][] pieceSnapshot = new Piece[8][8];

    // Logging state
    private JTextArea moveLogArea;
    private int fullMoveCount = 1;

    // Undo State
    class GameState {
        Square[][] boardSquares = new Square[8][8];
        int[] activePieceCoords = new int[33];
        int[] boardToIndex = new int[64];
        int activePieceCount;
        int whiteKingRow, whiteKingCol;
        int blackKingRow, blackKingCol;
        boolean isWhiteTurn;
        String moveLogText;
        int fullMoveCount;
    }
    private Stack<GameState> history = new Stack<>();

    // Piece images: key = "White King", "Black Pawn", etc.
    private Map<String, BufferedImage> pieceImages = new HashMap<>();

    public ChessBoardUI(Board board, GameController gameController) {
        this.board = board;
        this.gameController = gameController;
        loadPieceImages();

        // Initialize the bot based on chosen difficulty
        if (gameController.isSinglePlayer) {
            if (gameController.botDepth == 5 && !gameController.useQS) {
                activeBot = new AmateurBot();
            } else {
                activeBot = new BeginnerBot(); // default, also placeholder for future bots
            }
        }

        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new BoardPanel();
        MouseInputListener mouseListener = new MouseInputListener(this);
        boardPanel.addMouseListener(mouseListener);
        boardPanel.addMouseMotionListener(mouseListener);

        // UI Layout
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);

        // Move Log Panel
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(300, TILE_SIZE * BOARD_SIZE));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logTitle = new JLabel("Move Log", SwingConstants.CENTER);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        sidePanel.add(logTitle, BorderLayout.NORTH);

        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(moveLogArea);
        sidePanel.add(scrollPane, BorderLayout.CENTER);

        JButton undoButton = new JButton("Undo Move");
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        undoButton.addActionListener(e -> undo());

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        newGameButton.addActionListener(e -> {
            this.dispose();
            ChessBoardUI.main(new String[]{});
        });

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonPanel.add(undoButton);
        buttonPanel.add(newGameButton);
        sidePanel.add(buttonPanel, BorderLayout.SOUTH);

        add(sidePanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // by default, bot will be white, first move
        if (gameController.isSinglePlayer && gameController.isWhiteTurn) {
            takeBoardSnapshot(); // update -> freeze the board visually before bot starts
            botThinking = true; // update -> lock input while bot calculates opening move
            // Hire a background worker to think
            new Thread(() -> {
                Move openingMove = activeBot.getBestMove(board, true); // true = White
                // update -> artificial delay so the bot move doesn't appear instantly
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Hand the result back to the UI worker
                javax.swing.SwingUtilities.invokeLater(() -> {
                    executeBotMove(openingMove);
                });
            }).start();
        }
    }

    // Inner panel where all painting happens
    class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            if (botThinking) {
                // update -> draw from the frozen snapshot so the board stays visible
                // and piece flicker from willMoveResultInCheck is impossible
                drawPiecesFromSnapshot(g);
            } else {
                drawHighlights(g);// highlight the possible move
                drawPieces(g);
            }
        }

        /*
         * updateded: highlighting possible move
         */
        private void drawHighlights(Graphics g) {
            // check if a piece is currently selected/dragged
            if (dragFromRow != -1 && dragFromCol != -1) {
                Square startSquare = board.getSquare(dragFromRow, dragFromCol);
                Piece piece = startSquare.getPiece();

                if (piece != null && gameController.isCorrectTurn(piece)) {
                    g.setColor(new Color(50, 150, 50, 150));
                    int circleSize = 20;
                    int offset = (TILE_SIZE - circleSize) / 2; // Centers the circle
                    // traverse all table to find all valid moves.
                    for (int i = 0; i < 8; ++i) {
                        for (int j = 0; j < 8; ++j) {
                            Square endSquare = board.getSquare(i, j);
                            if (piece.isValidMove(board, startSquare, endSquare)) {
                                if (!board.willMoveResultInCheck(startSquare, endSquare, piece.isWhite())) {
                                    // that move will not cause check -> a possible move
                                    if (endSquare.getPiece() == null) {
                                        g.fillOval(j * TILE_SIZE + offset, i * TILE_SIZE + offset, circleSize,
                                                circleSize);
                                    } else {
                                        // update vong tron do cho PNG che o mau xanh problem
                                        Graphics2D g2 = (Graphics2D) g;
                                        g2.setColor(new Color(200, 50, 50, 150)); // semi-transparent red
                                        g2.setStroke(new BasicStroke(4)); // 4 pixel thick border
                                        g2.drawOval(j * TILE_SIZE + 4, i * TILE_SIZE + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                                        g2.setColor(new Color(50, 150, 50, 150)); // reset back to green
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /** Draw the 8x8 alternating light/dark squares. */
        private void drawBoard(Graphics g) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    boolean isLight = board.getSquare(row, col).isWhite();
                    g.setColor(isLight ? new Color(240, 217, 181) : new Color(181, 136, 99));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                    // Highlight the selected (drag-source) square
                    if (row == dragFromRow && col == dragFromCol) {
                        g.setColor(new Color(100, 200, 100, 120));
                        g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }

                    // Draw Rank Coordinates (8 to 1) on the left edge
                    if (col == 0) {
                        g.setColor(isLight ? new Color(181, 136, 99) : new Color(240, 217, 181));
                        g.setFont(new Font("SansSerif", Font.BOLD, 14));
                        g.drawString(String.valueOf(8 - row), 5, row * TILE_SIZE + 20);
                    }

                    // Draw File Coordinates (a to h) on the bottom edge
                    if (row == 7) {
                        g.setColor(isLight ? new Color(181, 136, 99) : new Color(240, 217, 181));
                        g.setFont(new Font("SansSerif", Font.BOLD, 14));
                        g.drawString(String.valueOf((char) ('a' + col)), col * TILE_SIZE + TILE_SIZE - 15, row * TILE_SIZE + TILE_SIZE - 5);
                    }
                }
            }
        }

        /** Loop through every Square; if it holds a Piece, draw its image. */
        private void drawPieces(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 6; // px gap from tile edge
            int imgSize = TILE_SIZE - padding * 2;

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    Piece piece = board.getSquare(row, col).getPiece();
                    if (piece != null) {
                        String key = getPieceImageKey(piece);
                        BufferedImage img = pieceImages.get(key);
                        if (img != null) {
                            int x = col * TILE_SIZE + padding;
                            int y = row * TILE_SIZE + padding;
                            g2.drawImage(img, x, y, imgSize, imgSize, null);
                        } else {
                            // Fallback: draw text if image missing
                            g.setFont(new Font("SansSerif", Font.BOLD, 12));
                            String label = (piece.isWhite() ? "W" : "B") + "-" + piece.getClass().getSimpleName();
                            FontMetrics fm = g.getFontMetrics();
                            int tx = col * TILE_SIZE + (TILE_SIZE - fm.stringWidth(label)) / 2;
                            int ty = row * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2 - 2;
                            g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                            g.drawString(label, tx, ty);
                        }
                    }
                }
            }
        }

        // update -> draws pieces from the frozen snapshot instead of the live board
        // used while the bot is calculating to prevent flicker from mid-simulation
        // mutations
        private void drawPiecesFromSnapshot(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 6; // px gap from tile edge
            int imgSize = TILE_SIZE - padding * 2;

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    Piece piece = pieceSnapshot[row][col]; // read from snapshot, not live board
                    if (piece != null) {
                        String key = getPieceImageKey(piece);
                        BufferedImage img = pieceImages.get(key);
                        if (img != null) {
                            int x = col * TILE_SIZE + padding;
                            int y = row * TILE_SIZE + padding;
                            g2.drawImage(img, x, y, imgSize, imgSize, null);
                        } else {
                            // Fallback: draw text if image missing
                            g.setFont(new Font("SansSerif", Font.BOLD, 12));
                            String label = (piece.isWhite() ? "W" : "B") + "-" + piece.getClass().getSimpleName();
                            FontMetrics fm = g.getFontMetrics();
                            int tx = col * TILE_SIZE + (TILE_SIZE - fm.stringWidth(label)) / 2;
                            int ty = row * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2 - 2;
                            g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                            g.drawString(label, tx, ty);
                        }
                    }
                }
            }
        }

        // Returns the map key for a piece, e.g. "White King" or "Black Pawn"
        private String getPieceImageKey(Piece piece) {
            String color = piece.isWhite() ? "White" : "Black";
            String type = piece.getClass().getSimpleName(); // e.g. "King", "Pawn"
            return color + " " + type;
        }
    }

    // ---------------------------------------------------------------
    // Public helpers used by MouseInputListener
    // ---------------------------------------------------------------

    /** Convert a pixel coordinate to a board row (0-7). */
    public int pixelToRow(int y) {
        return y / TILE_SIZE;
    }

    /** Convert a pixel coordinate to a board column (0-7). */
    public int pixelToCol(int x) {
        return x / TILE_SIZE;
    }

    /** Returns true when (row, col) is inside the 8×8 grid. */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /** Helper to get chess notation string for logging */
    private String getChessNotation(Square start, Square end, Piece movingPiece, Piece capturedPiece, boolean isPromotion) {
        String pieceLetter = "";
        if (movingPiece instanceof Knight) pieceLetter = "N";
        else if (!(movingPiece instanceof Pawn)) pieceLetter = movingPiece.getClass().getSimpleName().substring(0, 1);

        String dest = "" + (char) ('a' + end.getCol()) + (8 - end.getRow());
        
        String capture = (capturedPiece != null) ? "x" : "";
        if (movingPiece instanceof Pawn && capturedPiece != null) {
            capture = "" + (char)('a' + start.getCol()) + "x";
        }

        String promotion = isPromotion ? "=Q" : ""; // basic assumption for simplicity

        // handle castling visually
        if (movingPiece instanceof King && Math.abs(start.getCol() - end.getCol()) == 2) {
            if (end.getCol() == 6) return "O-O";
            else return "O-O-O";
        }

        return pieceLetter + capture + dest + promotion;
    }

    private void logMove(String moveNotation, boolean isWhite) {
        if (isWhite) {
            moveLogArea.append(String.format("%-4d %-15s", fullMoveCount, moveNotation));
        } else {
            moveLogArea.append(String.format("%-15s\n", moveNotation));
            fullMoveCount++;
        }
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }

    private void saveState() {
        GameState state = new GameState();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                state.boardSquares[r][c] = new Square(board.getSquare(r, c).isWhite(), r, c);
                Piece p = board.getSquare(r, c).getPiece();
                if (p != null) {
                    state.boardSquares[r][c].setPiece(p.clonePiece());
                }
            }
        }
        System.arraycopy(board.activePieceCoords, 0, state.activePieceCoords, 0, 33);
        System.arraycopy(board.boardToIndex, 0, state.boardToIndex, 0, 64);
        state.activePieceCount = board.activePieceCount;
        if (board.whiteKingSquare != null) {
            state.whiteKingRow = board.whiteKingSquare.getRow();
            state.whiteKingCol = board.whiteKingSquare.getCol();
        }
        if (board.blackKingSquare != null) {
            state.blackKingRow = board.blackKingSquare.getRow();
            state.blackKingCol = board.blackKingSquare.getCol();
        }
        state.isWhiteTurn = gameController.isWhiteTurn;
        state.moveLogText = moveLogArea.getText();
        state.fullMoveCount = fullMoveCount;
        history.push(state);
    }

    private void undo() {
        if (history.isEmpty() || botThinking || gameOver) return;

        // In single player, we want to pop 2 states (undo both Bot and Player)
        // If there's only 1 state, we pop it (returning to before White's first move)
        int popCount = (gameController.isSinglePlayer && history.size() > 1) ? 2 : 1;
        
        GameState state = null;
        for (int i = 0; i < popCount; i++) {
            state = history.pop();
        }

        // Restore state
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.getSquare(r, c).setPiece(state.boardSquares[r][c].getPiece());
            }
        }
        System.arraycopy(state.activePieceCoords, 0, board.activePieceCoords, 0, 33);
        System.arraycopy(state.boardToIndex, 0, board.boardToIndex, 0, 64);
        board.activePieceCount = state.activePieceCount;
        if (board.whiteKingSquare != null) board.whiteKingSquare = board.getSquare(state.whiteKingRow, state.whiteKingCol);
        if (board.blackKingSquare != null) board.blackKingSquare = board.getSquare(state.blackKingRow, state.blackKingCol);
        
        gameController.isWhiteTurn = state.isWhiteTurn;
        moveLogArea.setText(state.moveLogText);
        fullMoveCount = state.fullMoveCount;

        boardPanel.repaint();

        // If we undid back to the Bot's turn, we must trigger the bot to think again
        if (gameController.isSinglePlayer && gameController.isWhiteTurn) {
            triggerBot();
        }
    }

    private void triggerBot() {
        takeBoardSnapshot();
        botThinking = true;
        new Thread(() -> {
            Move bestMove = activeBot.getBestMove(board, true);
            try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            javax.swing.SwingUtilities.invokeLater(() -> executeBotMove(bestMove));
        }).start();
    }

    /**
     * Called by MouseInputListener when the user presses on a square.
     * Records the source square and highlights it.
     */
    public void setDragFrom(int row, int col) {
        if (gameOver)
            return;
        if (botThinking)
            return; // update -> block human input while bot is calculating
        dragFromRow = row;
        dragFromCol = col;
        boardPanel.repaint();
    }

    /**
     * Called by MouseInputListener when the user releases on a square.
     * Attempts the move: validates turn & move legality, then snaps the piece.
     */

    // this is human move
    public void tryMove(int toRow, int toCol) {
        if (gameOver)
            return;
        if (botThinking)
            return; // update -> block human input while bot is calculating
        dragToRow = toRow;
        dragToCol = toCol;
        if (dragFromRow == toRow && dragFromCol == toCol) {
            // updated: reject moving to the same destination , reset to the initial state
            dragFromRow = -1;
            dragFromCol = -1;
            boardPanel.repaint();
            return;
        }
        if (inBounds(dragFromRow, dragFromCol) && inBounds(dragToRow, dragToCol)) {
            Square startSquare = board.getSquare(dragFromRow, dragFromCol);
            Square endSquare = board.getSquare(dragToRow, dragToCol);
            Piece piece = startSquare.getPiece();

            if (piece != null
                    && gameController.isCorrectTurn(piece)
                    && piece.isValidMove(board, startSquare, endSquare)) {
                // check if the move leaves king in check
                if (board.willMoveResultInCheck(startSquare, endSquare, piece.isWhite())) {
                    JOptionPane.showMessageDialog(this, "Illegal Move: Check Alert!!!");
                    // check position alert
                    // update -> return early so the bot is not triggered after an illegal human
                    // move
                    dragFromRow = -1;
                    dragFromCol = -1;
                    boardPanel.repaint();
                    return;
                } else {
                    saveState(); // Snapshot before modifying

                    Piece capturedPiece = endSquare.getPiece();

                    endSquare.setPiece(piece);
                    startSquare.setPiece(null);

                    // Update King variable if King moved
                    if (piece instanceof King) {
                        if (piece.isWhite()) board.whiteKingSquare = endSquare;
                        else board.blackKingSquare = endSquare;
                    }

                    board.removeActivePiece(dragFromRow * 8 + dragFromCol);
                    if (capturedPiece == null) {
                        board.addActivePiece(dragToRow * 8 + dragToCol);
                    }

                    // implementing Castling: move the rook to the square the king passed through
                    if (piece instanceof King) {
                        King king = (King) piece;
                        if (king.isCastlingMove(startSquare, endSquare)) {
                            int rookFromCol = king.getCastlingRookCol(startSquare, endSquare);
                            // rook lands on the square the king crossed
                            int rookToCol = (rookFromCol == 7) ? dragToCol - 1 : dragToCol + 1;
                            Square rookFrom = board.getSquare(dragToRow, rookFromCol);
                            Square rookTo = board.getSquare(dragToRow, rookToCol);
                            Piece rook = rookFrom.getPiece();
                            rookTo.setPiece(rook);
                            rookFrom.setPiece(null);

                            board.removeActivePiece(dragToRow * 8 + rookFromCol);
                            board.addActivePiece(dragToRow * 8 + rookToCol); // Castling rook move never captures

                            if (rook != null)
                                rook.setMoved(true);
                        }
                    }
                    piece.setMoved(true);
                    boolean isPromotion = false;
                    // implementing Pawn Promotion
                    if (piece instanceof Pawn) {
                        // white promtoe at row 7, black promote at row 0
                        int promotionRow = piece.isWhite() ? 7 : 0;
                        if (dragToRow == promotionRow) {
                            isPromotion = true;
                            // go to promtionRow
                            String[] options = { "Queen", "Rock", "Bishop", "Knight" };
                            int choice = JOptionPane.showOptionDialog(this, "Choose a piece to promote to:",
                                    "Pawn Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                    options, options[0]); // Default to Queen
                            Piece promotedPiece;
                            switch (choice) {
                                case 1:
                                    promotedPiece = new Rook(piece.isWhite());
                                    break;
                                case 2:
                                    promotedPiece = new Bishop(piece.isWhite());
                                    break;
                                case 3:
                                    promotedPiece = new Knight(piece.isWhite());
                                    break;
                                default: // queen by default or 0
                                    promotedPiece = new Queen(piece.isWhite());
                                    break;
                            }
                            // upgrade pawn to -> promoteedPiece (choice)
                            endSquare.setPiece(promotedPiece);
                        }
                    }

                    // Log the move before switching turns
                    String notation = getChessNotation(startSquare, endSquare, piece, capturedPiece, isPromotion);
                    logMove(notation, piece.isWhite());

                    gameController.switchTurn();

                    boolean nextPlayeriswhite = gameController.isWhiteTurn;

                    boolean isCheckmate = board.isCheckmate(nextPlayeriswhite);
                    boolean isStalemate = board.isStalemate(nextPlayeriswhite);
                    boolean isChecked = board.isChecked(nextPlayeriswhite);

                    if (isCheckmate) {
                        gameOver = true;
                        String winner = nextPlayeriswhite ? "Black" : "White";
                        SwingUtilities.invokeLater(() -> promptNewGame("Checkmate! " + winner + " wins!"));
                    } else if (isStalemate) {
                        gameOver = true;
                        SwingUtilities.invokeLater(() -> promptNewGame("Stalemate! This game is a draw."));
                    } else if (isChecked) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                (nextPlayeriswhite ? "White" : "Black") + " King is in Check!"));
                    }
                }
            }
        }
        // --- NEW: Trigger the Bot's response ---
        if (gameController.isSinglePlayer && gameController.isWhiteTurn && !gameOver) {
            triggerBot();
        }
        // Reset drag state
        dragFromRow = -1;
        dragFromCol = -1;
        boardPanel.repaint();
    }

    public void handleMouseHover(int row, int col) {
        if (inBounds(row, col)) {
            Piece piece = board.getSquare(row, col).getPiece();

            // If there is a piece, and it belongs to the player whose turn it is:
            if (piece != null && gameController.isCorrectTurn(piece)) {
                // Change the mouse cursor to a Hand pointer
                boardPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                // Otherwise, keep it as the default arrow
                boardPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }

    // bot move
    public void executeBotMove(Move move) {
        botThinking = false; // update -> unlock human input now that bot has finished
        if (move == null || gameOver)
            return;
        Square startSquare = board.getSquare(move.startRow, move.startCol);
        Square endSquare = board.getSquare(move.endRow, move.endCol);
        Piece piece = startSquare.getPiece();
        Piece capturedPiece = endSquare.getPiece();

        saveState(); // Snapshot before modifying

        // 1. Physically move the piece on the board
        endSquare.setPiece(piece);
        startSquare.setPiece(null);

        // Update King variable if King moved
        if (piece instanceof King) {
            if (piece.isWhite()) board.whiteKingSquare = endSquare;
            else board.blackKingSquare = endSquare;
        }

        board.removeActivePiece(move.startRow * 8 + move.startCol);
        if (capturedPiece == null) {
            board.addActivePiece(move.endRow * 8 + move.endCol);
        }

        piece.setMoved(true);
        // update -> handle castling: if king moved 2 squares, also move the rook
        if (piece instanceof King && Math.abs(move.startCol - move.endCol) == 2) {
            int rookFromCol = (move.endCol == 6) ? 7 : 0;
            int rookToCol = (move.endCol == 6) ? 5 : 3;
            Square rookFrom = board.getSquare(move.endRow, rookFromCol);
            Square rookTo = board.getSquare(move.endRow, rookToCol);
            Piece rook = rookFrom.getPiece();
            if (rook != null) {
                rookTo.setPiece(rook);
                rookFrom.setPiece(null);

                board.removeActivePiece(move.endRow * 8 + rookFromCol);
                board.addActivePiece(move.endRow * 8 + rookToCol); // Castling never captures

                rook.setMoved(true);
            }
        }
        // update -> handle pawn promotion: auto-promote bot pawn to queen
        if (move.isPromotion && piece instanceof Pawn) {
            endSquare.setPiece(new Queen(piece.isWhite()));
        }

        // Log the move before switching turns
        String notation = getChessNotation(startSquare, endSquare, piece, capturedPiece, move.isPromotion);
        logMove(notation, piece.isWhite());

        // 2. Switch the turn back to the Human (Black)
        gameController.switchTurn();
        boardPanel.repaint(); // Update the screen
        
        // 3. Check if the Bot just Checkmated the Human
        boolean isCheckmate = board.isCheckmate(false);
        boolean isStalemate = board.isStalemate(false);
        boolean isChecked = board.isChecked(false);

        if (isCheckmate) {
            gameOver = true;
            SwingUtilities.invokeLater(() -> promptNewGame("Checkmate! The Bot wins!"));
        } else if (isStalemate) {
            gameOver = true;
            SwingUtilities.invokeLater(() -> promptNewGame("Stalemate! It's a draw."));
        } else if (isChecked) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Your King is in Check!"));
        }
    }

    private void promptNewGame(String message) {
        int choice = JOptionPane.showConfirmDialog(this, message + "\nDo you want to play a new game?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            // Wrap in invokeLater to ensure clean UI thread execution
            javax.swing.SwingUtilities.invokeLater(() -> ChessBoardUI.main(new String[]{}));
        }
    }

    // update -> takes a photo of current piece positions before bot calculation
    // begins
    private void takeBoardSnapshot() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                pieceSnapshot[r][c] = board.getSquare(r, c).getPiece();
            }
        }
    }

    /** Force a visual refresh (called on mouse-drag for smooth feedback). */
    public void refresh() {
        if (botThinking)
            return; // update -> suppress repaints during bot calculation to prevent piece flicker
        boardPanel.repaint();
    }

    /**
     * Loads all 12 piece PNGs from the "chess pieces" folder into the pieceImages
     * map.
     * Expected filenames: "White King.png", "Black Pawn.png", etc.
     */
    private void loadPieceImages() {
        String[] colors = { "White", "Black" };
        String[] types = { "King", "Queen", "Rook", "Bishop", "Knight", "Pawn" };
        // Resolve the folder relative to where the program is launched from
        File imgDir = new File("chess pieces");
        for (String color : colors) {
            for (String type : types) {
                String key = color + " " + type;
                File file = new File(imgDir, key + ".png");
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        pieceImages.put(key, img);
                    } else {
                        System.err.println("Could not decode: " + file.getPath());
                    }
                } catch (IOException e) {
                    System.err.println("Missing image: " + file.getPath());
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Entry point for quick standalone testing
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        // before starting the game, pop up a menu first

        String[] modeOptions = { "1 Player (vs Bot)", "2 Players (Local)" };
        int modeChoice = JOptionPane.showOptionDialog(null,
                "Welcome to Chess! Select Game Mode:",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                modeOptions,
                modeOptions[0]);
        // If they closed the window, exit the program safely
        if (modeChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }
        boolean isSinglePlayer = (modeChoice == 0);
        int botDepth = 0;
        // 2. Second Menu: Choose Difficulty (Only if 1 Player was selected)
        boolean useQS = false; // default
        if (isSinglePlayer) {
            String[] diffOptions = { "Beginner", "Amateur", "Intermediate", "Hard" };
            int diffChoice = JOptionPane.showOptionDialog(null,
                    "Select Bot Difficulty:",
                    "Difficulty",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    diffOptions,
                    diffOptions[0]);

            // Map their choice to an actual depth number
            if (diffChoice == 0)
                botDepth = 3; // beginerr only minimax depth 3
            else if (diffChoice == 1) {
                botDepth = 5;
                useQS = false; // amatuer -> depth 5 not use Quiesence search
            } // mid depth 4 + Quiesence Search
            else if (diffChoice == 2) {
                botDepth = 4; // Interediate -> depth 4 + Quiesence Search
                useQS = true;
            } else if (diffChoice == 3) {
                botDepth = 5; // hard -> depth 5 + QUiesence Search
                useQS = true;
            }
        }
        // 3. Initialize the game with the chosen settings!
        Board board = new Board();
        GameController gc = new GameController(isSinglePlayer, botDepth, useQS);

        // Setting Rooks
        board.getSquare(0, 0).setPiece(new Rook(true));
        board.getSquare(0, 7).setPiece(new Rook(true));
        board.getSquare(7, 0).setPiece(new Rook(false));
        board.getSquare(7, 7).setPiece(new Rook(false));

        // Setting Knights
        board.getSquare(0, 1).setPiece(new Knight(true));
        board.getSquare(0, 6).setPiece(new Knight(true));
        board.getSquare(7, 1).setPiece(new Knight(false));
        board.getSquare(7, 6).setPiece(new Knight(false));

        // Setting Bishops
        board.getSquare(0, 2).setPiece(new Bishop(true));
        board.getSquare(0, 5).setPiece(new Bishop(true));
        board.getSquare(7, 2).setPiece(new Bishop(false));
        board.getSquare(7, 5).setPiece(new Bishop(false));

        // Setting Queens
        board.getSquare(0, 3).setPiece(new Queen(true));
        board.getSquare(7, 3).setPiece(new Queen(false));

        // Setting Kings
        board.getSquare(0, 4).setPiece(new King(true));
        board.getSquare(7, 4).setPiece(new King(false));

        // Setting Pawns
        for (int col = 0; col < 8; col++) {
            board.getSquare(1, col).setPiece(new Pawn(true)); // White Pawns on row 1
            board.getSquare(6, col).setPiece(new Pawn(false)); // Black Pawns on row 6
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getSquare(r, c).getPiece();
                if (p != null) {
                    board.addActivePiece(r * 8 + c);
                    if (p instanceof King) {
                        if (p.isWhite()) board.whiteKingSquare = board.getSquare(r, c);
                        else board.blackKingSquare = board.getSquare(r, c);
                    }
                }
            }
        }

        new ChessBoardUI(board, gc);
    }
}
