package lab;
// the old one is so redundance as it have to call a lot of more objects while doing minimax (calling millions of isValidMove -> intiate milliosn of Bishop, Rock)
/* 
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

*/

// updated -> 

public class Queen extends Piece {
    
    // 1. Declare these logic helpers as class variables
    private Rook rookLogic;
    private Bishop bishopLogic;
    public Queen(boolean isWhite) {
        super(isWhite);
        // 2. Initialize them ONLY ONCE when the Queen is born
        this.rookLogic = new Rook(isWhite);
        this.bishopLogic = new Bishop(isWhite);
    }
    public boolean isValidMove(Board board, Square start, Square end) {
        // 3. Reuse the cached logic instead of calling 'new Rook()' every time!
        return rookLogic.isValidMove(board, start, end)
                || bishopLogic.isValidMove(board, start, end);
    }
}

