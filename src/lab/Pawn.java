package lab;

public class Pawn extends Piece {
    public Pawn(boolean isWhite) {
        super(isWhite);
    }
    public boolean isValidMove(Board board, Square start, Square end) {
        int dx = end.getRow() - start.getRow();
        int dy = Math.abs(end.getCol() - start.getCol());
        // White goes up, Black goes down
        int direction = this.isWhite() ? 1 : -1;
        if (end.getPiece() != null && end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }
        if(dy == 0 && dx == direction * 2 && !this.isMoved() && end.getPiece() == null) return true;
        // Normal move
        if (dy == 0 && dx == direction && end.getPiece() == null) return true;
        // Capture diagonally
        if (dy == 1 && dx == direction && end.getPiece().isWhite() != this.isWhite()) return true;
        return false;
    }
}