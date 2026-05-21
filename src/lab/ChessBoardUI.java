package lab;

import javax.swing.*;
import java.awt.*;

public class ChessBoardUI extends JFrame {

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;

    private Board board;
    private GameController gameController;
    private BoardPanel boardPanel;

    // Drag state
    private int dragFromRow = -1;
    private int dragFromCol = -1;
    private int dragToRow   = -1;
    private int dragToCol   = -1;
    
    // Game state
    private boolean gameOver = false;

    public ChessBoardUI(Board board, GameController gameController) {
        this.board = board;
        this.gameController = gameController;

        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new BoardPanel();
        MouseInputListener mouseListener = new MouseInputListener(this);
        boardPanel.addMouseListener(mouseListener);
        boardPanel.addMouseMotionListener(mouseListener);

        add(boardPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------------------------------------------------------
    //  Inner panel — all painting happens here
    // ---------------------------------------------------------------
    class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            drawHighlights(g);// highlight the possible move
            drawPieces(g);
        }

        /*
        updateded:  highlighting possible move
        */
        private void drawHighlights(Graphics g){
        // check if a piece is currently selected/dragged
        if (dragFromRow != -1 && dragFromCol != -1){
            Square startSquare = board.getSquare(dragFromRow, dragFromCol);
            Piece piece = startSquare.getPiece();

            if (piece != null && gameController.isCorrectTurn(piece)){
                g.setColor(new Color(50,150,50,150));
                int circleSize = 20;
                 int offset = (TILE_SIZE - circleSize) / 2; // Centers the circle
                 // traverse all table to find all valid moves.
                for (int i = 0 ; i < 8 ;++i){
                    for (int j = 0 ; j < 8 ; ++j){
                        Square endSquare = board.getSquare(i, j);
                        if (piece.isValidMove(board, startSquare, endSquare)){
                            if(!board.willMoveResultInCheck(startSquare, endSquare, piece.isWhite())){
                                // that move will not cause check -> a possible move
                                g.fillOval(j * TILE_SIZE + offset, i * TILE_SIZE + offset, circleSize, circleSize);
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
                    boolean isLight = board.getSquare(row,col).isWhite();
                    g.setColor(isLight ? new Color(240, 217, 181) : new Color(181, 136, 99));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                    // Highlight the selected (drag-source) square
                    if (row == dragFromRow && col == dragFromCol) {
                        g.setColor(new Color(100, 200, 100, 120));
                        g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        /** Loop through every Square; if it holds a Piece, draw its label. */
        private void drawPieces(Graphics g) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    Piece piece = board.getSquare(row,col).getPiece();
                    if (piece != null) {
                        String label = getPieceLabel(piece);
                        g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);

                        // Center the text inside the tile
                        FontMetrics fm = g.getFontMetrics();
                        int x = col * TILE_SIZE + (TILE_SIZE - fm.stringWidth(label)) / 2;
                        int y = row * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2 - 2;

                        // Draw a shadow/outline for readability
                        g.setColor(piece.isWhite() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
                        g.drawString(label, x + 1, y + 1);
                        g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                        g.drawString(label, x, y);
                    }
                }
            }
        }

        /**
         * Returns a short text label for a piece, e.g. "W-Pawn".
         * Extend this switch with your concrete Piece subclasses.
         */
        private String getPieceLabel(Piece piece) {
            String color  = piece.isWhite() ? "W" : "B";
            String type   = piece.getClass().getSimpleName(); // e.g. "Pawn", "Rook" …
            return color + "-" + type;
        }
    }

    // ---------------------------------------------------------------
    //  Public helpers used by MouseInputListener
    // ---------------------------------------------------------------

    /** Convert a pixel coordinate to a board row (0-7). */
    public int pixelToRow(int y) { return y / TILE_SIZE; }

    /** Convert a pixel coordinate to a board column (0-7). */
    public int pixelToCol(int x) { return x / TILE_SIZE; }

    /** Returns true when (row, col) is inside the 8×8 grid. */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Called by MouseInputListener when the user presses on a square.
     * Records the source square and highlights it.
     */
    public void setDragFrom(int row, int col) {
        if (gameOver) return;
        dragFromRow = row;
        dragFromCol = col;
        boardPanel.repaint();
    }

    /**
     * Called by MouseInputListener when the user releases on a square.
     * Attempts the move: validates turn & move legality, then snaps the piece.
     */
    public void tryMove(int toRow, int toCol) {
        if (gameOver) return;
        dragToRow = toRow;
        dragToCol = toCol;
        if (dragFromRow == toRow && dragFromCol == toCol){
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
                //check if the move leaves king in check
                if(board.willMoveResultInCheck(startSquare, endSquare, piece.isWhite())){
                JOptionPane.showMessageDialog(this, "Illegal Move: Check Alert!!!");
                // check position alert
                }else{
                endSquare.setPiece(piece);
                startSquare.setPiece(null);
                piece.setMoved(true);
                // implementing Pawn Promotion
                if (piece instanceof Pawn){
                    // white promtoe at row 7, black promote at row 0
                    int promotionRow = piece.isWhite() ? 7:0;
                    if (dragToRow == promotionRow){
                        // go to promtionRow
                        String[] options = {"Queen","Rock","Bishop","Knight"};
                        int choice = JOptionPane.showOptionDialog(this,"Choose a piece to promote to:","Pawn Promotion",JOptionPane.DEFAULT_OPTION,  JOptionPane.QUESTION_MESSAGE,null, options, options[0]); // Default to Queen
                        Piece promotedPiece;
                        switch(choice){
                            case 1: 
                                promotedPiece = new Rook(piece.isWhite()); 
                                break;
                            case 2:
                                promotedPiece = new Bishop(piece.isWhite()); 
                                break;
                            case 3:
                                promotedPiece = new Knight(piece.isWhite()); 
                                break;
                            default: //queen by default or 0
                                promotedPiece = new Queen(piece.isWhite()); 
                                break;
                        }
                        // upgrade pawn to -> promoteedPiece (choice)
                        endSquare.setPiece(promotedPiece);
                 }
                }
                gameController.switchTurn();
                

                boolean nextPlayeriswhite = gameController.isWhiteTurn;

                if (board.isCheckmate(nextPlayeriswhite)){
                    String winner = nextPlayeriswhite ? "Black" : "White";
                    JOptionPane.showMessageDialog(this, "Checkmate! " + winner + " wins!");
                    gameOver = true;
                } else if (board.isStalemate(nextPlayeriswhite)){
                    JOptionPane.showMessageDialog(this, "Stalemate! This game is a draw.");
                    gameOver = true;
                } else if (board.isChecked(nextPlayeriswhite)){
                    JOptionPane.showMessageDialog(this, (nextPlayeriswhite ? "White" : "Black") + " King is in Check!");
                }
            }
        }
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

    /** Force a visual refresh (called on mouse-drag for smooth feedback). */
    public void refresh() {
        boardPanel.repaint();
    }

    // ---------------------------------------------------------------
    //  Entry point for quick standalone testing
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        Board board = new Board();
        GameController gc = new GameController();
        
        // Setting Rooks
        board.getSquare(0,0).setPiece(new Rook(true));
        board.getSquare(0,7).setPiece(new Rook(true));
        board.getSquare(7,0).setPiece(new Rook(false));
        board.getSquare(7,7).setPiece(new Rook(false));
        
        // Setting Knights
        board.getSquare(0,1).setPiece(new Knight(true));
        board.getSquare(0,6).setPiece(new Knight(true));
        board.getSquare(7,1).setPiece(new Knight(false));
        board.getSquare(7,6).setPiece(new Knight(false));
        
        // Setting Bishops
        board.getSquare(0,2).setPiece(new Bishop(true));
        board.getSquare(0,5).setPiece(new Bishop(true));
        board.getSquare(7,2).setPiece(new Bishop(false));
        board.getSquare(7,5).setPiece(new Bishop(false));
        
        // Setting Queens
        board.getSquare(0,3).setPiece(new Queen(true));
        board.getSquare(7,3).setPiece(new Queen(false));
        
        // Setting Kings
        board.getSquare(0,4).setPiece(new King(true));
        board.getSquare(7,4).setPiece(new King(false));
        
        // Setting Pawns
        for (int col = 0; col < 8; col++) {
            board.getSquare(1, col).setPiece(new Pawn(true));  // White Pawns on row 1
            board.getSquare(6, col).setPiece(new Pawn(false)); // Black Pawns on row 6
        }
        
        new ChessBoardUI(board, gc);
    }
}

