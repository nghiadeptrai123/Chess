package lab;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    public boolean isValidMove(Board board, Square start, Square end) {
        int dx = Math.abs(end.getRow() - start.getRow());
        int dy = Math.abs(end.getCol() - start.getCol());

        if (end.getPiece() != null && end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }
}