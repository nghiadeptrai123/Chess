package lab;

public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite);
    }

    public boolean isValidMove(Board board, Square start, Square end) {
        int dx = Math.abs(end.getRow() - start.getRow());
        int dy = Math.abs(end.getCol() - start.getCol());
        if (end.getPiece() != null && end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        if (dx <= 1 && dy <= 1) {
            return true;
        }

        if (isCastlingMove(start, end)) {
            return isValidCastle(board, start, end);
        }

        return false;
    }

    // Returns true if the move looks like a castle attempt
    public boolean isCastlingMove(Square start, Square end) {
        return start.getRow() == end.getRow()
                && Math.abs(end.getCol() - start.getCol()) == 2
                && !this.isMoved();
    }

    // Return the column of the rook, if king moves left, col = 0. Else col = 7.
    public int getCastlingRookCol(Square start, Square end) {
        return (end.getCol() > start.getCol()) ? 7 : 0;
    }

    /**
     * Validates all castling conditions:
     * - King has not moved
     * - Rook has not moved
     * - No pieces between king and rook
     * - King is not currently in check
     * - King does not pass through a square that is under attack
     * - King does not land on a square that is under attack
     */
    private boolean isValidCastle(Board board, Square start, Square end) {
        int row = start.getRow();
        int rookCol = getCastlingRookCol(start, end);
        Square rookSquare = board.getSquare(row, rookCol);

        // Rook must exist and must not have moved
        Piece rook = rookSquare.getPiece();
        if (!(rook instanceof Rook) || rook.isMoved()) {
            return false;
        }

        // Path between king and rook must be empty
        int colStep = (rookCol > start.getCol()) ? 1 : -1;
        for (int c = start.getCol() + colStep; c != rookCol; c += colStep) {
            if (board.getSquare(row, c).getPiece() != null) {
                return false;
            }
        }

        // King must not currently be in check
        if (MoveHelper.isInCheck(board, this.isWhite())) {
            return false;
        }

        // King must not pass through or land on an attacked square
        // (check the square the king crosses and the square it lands on)
        int passThroughCol = start.getCol() + colStep;
        if (isSquareAttacked(board, row, passThroughCol, this.isWhite())) {
            return false;
        }
        if (isSquareAttacked(board, row, end.getCol(), this.isWhite())) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a given square is attacked by any enemy piece.
     * Uses a temporary king placement to reuse MoveHelper.isInCheck.
     */
    private boolean isSquareAttacked(Board board, int row, int col, boolean isWhite) {
        Square target = board.getSquare(row, col);

        // Temporarily place the king on that square and check if any enemy can reach it
        target.setPiece(this);
        boolean attacked = false;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Square sq = board.getSquare(r, c);
                Piece piece = sq.getPiece();
                if (piece != null && piece.isWhite() != isWhite) {
                    if (piece.isValidMove(board, sq, target)) {
                        attacked = true;
                        break;
                    }
                }
            }
            if (attacked)
                break;
        }

        // Restore original piece
        target.setPiece(null);
        return attacked;
    }

    @Override
    public Piece clonePiece() {
        King clone = new King(this.isWhite());
        clone.setMoved(this.isMoved());
        return clone;
    }
}