package lab;

public class Bishop extends Piece {
    Bishop(boolean isWhite) {
        super(isWhite);
    }

    public boolean isValidMove(Board board, Square start, Square end) {
        int dx = Math.abs(end.getRow() - start.getRow());
        int dy = Math.abs(end.getCol() - start.getCol());

        if (dx != dy) {
            return false;
        }
        if (end.getPiece() != null && end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        int rowStep = (end.getRow() > start.getRow()) ? 1 : -1;
        int colStep = (end.getCol() > start.getCol()) ? 1 : -1;

        int r = start.getRow() + rowStep;
        int c = start.getCol() + colStep;

        while (r != end.getRow() && c != end.getCol()) {
            if (board.getSquare(r,c).getPiece() != null) {
                return false;
            }
            r += rowStep;
            c += colStep;
        }

        return true;
    }

    @Override
    public Piece clonePiece() {
        Bishop clone = new Bishop(this.isWhite());
        clone.setMoved(this.isMoved());
        return clone;
    }
}