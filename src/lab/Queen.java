package lab;
class Queen extends Piece {
    public Queen(boolean isWhite) {
        super(isWhite);
    }
    public boolean isValidMove(Board board, Square start, Square end) {
        Rook rookLogic = new Rook(this.isWhite());
        Bishop bishopLogic = new Bishop(this.isWhite());

        return rookLogic.isValidMove(board, start, end)
                || bishopLogic.isValidMove(board, start, end);
    }
}