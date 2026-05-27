/*
this class represents a single chess move. 
it acts as a simple data container used by the Mini-Max
to keep track of piece start and destination positon
-> help make/undo pattern -> allowing the Minimax algorithm to perfectly restore the board state after simulating a future move.
*/

package lab;
public class Move {
    public int startRow;
    public int startCol;
    public int endRow;
    public int endCol;
    public boolean isPromotion;               // update -> added promotion flag
    public boolean isEnPassant;
    // This is the secret to making the Undo pattern work!
    public Piece capturedPiece; 
    // Constructor to easily create a move in one line
    public Move(int startRow, int startCol, int endRow, int endCol, Piece capturedPiece) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.capturedPiece = capturedPiece;
        this.isPromotion = false;
        this.isEnPassant = false;
    }
}