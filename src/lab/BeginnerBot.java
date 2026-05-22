package lab;
import java.util.ArrayList;
import java.util.List;

public class BeginnerBot implements ChessBot{
    private static final int SEARCH_DEPTH = 3;
    // depth 3 for beginner

private List<Move> getAllLegalMoves(Board board, boolean isWhite){
// find all possible moves at the current state
    List<Move> moves = new ArrayList<>();
    for (int i = 0 ; i < 8 ; ++i){
        for (int j = 0 ; j < 8 ;++j){
            Square startSquare = board.getSquare(i, j);
            Piece piece = startSquare.getPiece();

            if (piece != null && piece.isWhite() == isWhite){
                // this is a piece
                // check everysquare on the board to see if it can go there 
                for (int i_final = 0; i_final < 8 ; ++i_final){
                    for (int j_final = 0; j_final < 8 ; ++j_final){
                        Square endSquare = board.getSquare(i_final, j_final);
                        if(piece.isValidMove(board, startSquare, endSquare)){
                            if(!board.willMoveResultInCheck(startSquare, endSquare, isWhite)){
                                Move m = new Move(i,j,i_final,j_final,endSquare.getPiece());
                                // save the move and the captured piece
                                // update -> detect pawn promotion and mark the flag
                                if (piece instanceof Pawn) {
                                    int promotionRow = piece.isWhite() ? 7 : 0;
                                    if (i_final == promotionRow) {
                                        m.isPromotion = true;
                                    }
                                }
                                moves.add(m);
                            }
                        }
                    }
                }
            }
        }
    }
    return moves;
}

/**
     * Calculates who is winning based on standard piece values.
     * White scores are positive, Black scores are negative.
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getSquare(r, c).getPiece();
                if (piece != null) {
                    int pieceValue = 0;
                    if (piece instanceof Pawn) pieceValue = 100;
                    else if (piece instanceof Knight) pieceValue = 300;
                    else if (piece instanceof Bishop) pieceValue = 300;
                    else if (piece instanceof Rook) pieceValue = 500;
                    else if (piece instanceof Queen) pieceValue = 900;
                    // Add for White, subtract for Black
                    if (piece.isWhite()) {
                        score += pieceValue;
                    } else {
                        score -= pieceValue;
                    }
                }
            }
        }
        return score;
    }
// minimax + alpha beta prunning implementation
private int minimax(Board board, int depth, int alpha, int beta, boolean isMax){
    // isMax is to check at current level is max or min turn
    if (depth == 0){
        // based-case
        return evaluateBoard(board);
    }
    List<Move> legalMoves = getAllLegalMoves(board,isMax);

    if(legalMoves.isEmpty()){
        if(board.isChecked(isMax)){
            // getting checkmated (losing state)
            return isMax ? -99999 : 99999;
        }
        // this is Stalemate (draw state)
        return 0;
    }
    if(isMax){
        // Max level
        int maxEval = Integer.MIN_VALUE; // update -> fixed: was wrongly Integer.MAX_VALUE
        for (Move move: legalMoves){
            // try to move that piece
            Square start = board.getSquare(move.startRow, move.startCol);
            Square end = board.getSquare(move.endRow, move.endCol);
            Piece movingPiece = start.getPiece();

            boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

            // update -> handle pawn promotion during simulation
            if (move.isPromotion && movingPiece instanceof Pawn) {
                end.setPiece(new Queen(movingPiece.isWhite()));
                start.setPiece(null);
            } else {
                end.setPiece(movingPiece);
                start.setPiece(null);
            }
            movingPiece.setMoved(true); // update -> set isMoved flag for simulation

            // recurse minimax
            int eval = minimax(board,depth - 1, alpha,beta,false);

            //undo
            movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
            start.setPiece(movingPiece);
            end.setPiece(move.capturedPiece);
            
            // alpha-beta prunning
            // from left to right, child inherits from parents,
            // only updating alpha,beta at that current node with the value of
            //  nodes.
            
            maxEval = Math.max(maxEval,eval);
            alpha = Math.max(alpha,eval);

            if (beta <= alpha) break; //prunning condition
        }
        return maxEval;
    }else{
        // black's turn 
        int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                // Simulate
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end = board.getSquare(move.endRow, move.endCol);
                Piece movingPiece = start.getPiece();

                boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

                // update -> handle pawn promotion during simulation
                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true); // update -> set isMoved flag for simulation

                // Recurse
                int eval = minimax(board, depth - 1, alpha, beta, true);
                // Undo
                movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                // Alpha-Beta Pruning
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Prune branch!
            }
            return minEval;
    }
}
// find the best move
public Move getBestMove(Board board, boolean isWhite){
    List<Move> legalMoves = getAllLegalMoves(board, isWhite);
    Move bestMove = null;

    // White wants to maximize score, Black wants to minimize score;
    int bestScore = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
     for (Move move : legalMoves) {
            // 1. Setup the Simulation
            Square start = board.getSquare(move.startRow, move.startCol);
            Square end = board.getSquare(move.endRow, move.endCol);
            Piece movingPiece = start.getPiece();

            boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

            // 2. MAKE the move (Simulate)
            // update -> handle pawn promotion during simulation
            if (move.isPromotion && movingPiece instanceof Pawn) {
                end.setPiece(new Queen(movingPiece.isWhite()));
                start.setPiece(null);
            } else {
                end.setPiece(movingPiece);
                start.setPiece(null);
            }
            movingPiece.setMoved(true); // update -> set isMoved flag for simulation

            // 3. Dig deeper into the tree
            int score = minimax(board, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);
            // 4. UNDO the move (Restore the exact state)
            movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
            start.setPiece(movingPiece);
            end.setPiece(move.capturedPiece);
            // 5. Check if this is the best move so far
            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
        return bestMove; // -> the best moved found by this bot
}
}
