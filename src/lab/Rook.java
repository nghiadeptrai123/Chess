package lab;

public class Rook extends Piece {
    public Rook(boolean isWhite) {
        super(isWhite);
    }

    public boolean isValidMove(Board board, Square start, Square end) {
        if (start.getRow() != end.getRow() && start.getCol() != end.getCol()) {
            return false;
        }

        if (end.getPiece() != null && end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        int rowStep = Integer.compare(end.getRow(), start.getRow());
        int colStep = Integer.compare(end.getCol(), start.getCol());

        int r = start.getRow() + rowStep;
        int c = start.getCol() + colStep;

        while (r != end.getRow() || c != end.getCol()) {
            if (board.getSquare(r,c).getPiece() != null) {
                return false;
            }
            r += rowStep;
            c += colStep;
        }

        return true;
    }
}