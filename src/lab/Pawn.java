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
        /* updated logic for pawn going foward 2 step. Inorder to go 2 step, we have to
        ensure that this is the first time that pawn move and there is no blocking between start and end position 
        */
        Square intermediateSquare = board.getSquare(start.getRow() + direction, start.getCol());
        if(dy == 0 && dx == direction * 2 && !this.isMoved() && intermediateSquare.getPiece() == null && end.getPiece() == null) return true;
        // Normal move
        if (dy == 0 && dx == direction && end.getPiece() == null) return true;
        // Capture diagonally
        if (dy == 1 && dx == direction && end.getPiece() != null && end.getPiece().isWhite() != this.isWhite()) return true;
        // En Passant Capture diagonally
        if (dy == 1 && dx == direction && end.getPiece() == null && end == board.enPassantTarget) return true;
        return false;
    }

    @Override
    public Piece clonePiece() {
        Pawn clone = new Pawn(this.isWhite());
        clone.setMoved(this.isMoved());
        return clone;
    }
}