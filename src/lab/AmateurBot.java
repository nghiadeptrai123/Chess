package lab;

import java.util.ArrayList;
import java.util.List;

public class AmateurBot implements ChessBot {
    private static final int SEARCH_DEPTH = 6;
    // depth 5 for amateur

    private List<Move> getAllLegalMoves(Board board, boolean isWhite) {
        // find all possible moves at the current state
        List<Move> moves = new ArrayList<>();
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int i = pos / 8;
            int j = pos % 8;
            Square startSquare = board.getSquare(i, j);
            Piece piece = startSquare.getPiece();

            if (piece != null && piece.isWhite() == isWhite) {
                // this is a piece
                // check everysquare on the board to see if it can go there
                for (int i_final = 0; i_final < 8; ++i_final) {
                    for (int j_final = 0; j_final < 8; ++j_final) {
                        Square endSquare = board.getSquare(i_final, j_final);
                        if (piece.isValidMove(board, startSquare, endSquare)) {
                            if (!board.willMoveResultInCheck(startSquare, endSquare, isWhite)) {
                                Move m = new Move(i, j, i_final, j_final, endSquare.getPiece());
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
        return moves;
    }

    /**
     * Returns the material value of a piece.
     */
    private int getPieceValue(Piece piece) {
        if (piece instanceof Queen)  return 900;
        if (piece instanceof Rook)   return 500;
        if (piece instanceof Bishop) return 300;
        if (piece instanceof Knight) return 300;
        if (piece instanceof Pawn)   return 100;
        return 0; // King
    }

    private int scoreMove(Board board, Move move){
        // this use to calculate the score of each move in current state
        // we will use this to sort legal moves in order to facilitate the alpha-beta prunning
        int score = 0;
        Piece Attacker = board.getSquare(move.startRow, move.startCol).getPiece();
        if (move.isPromotion){
            score = score + 8000;
        }else {
            if(move.capturedPiece != null){
               int Attacker_val = getPieceValue(Attacker);
               int Victim_val = getPieceValue(move.capturedPiece);
               score = score + 10 * Victim_val - Attacker_val;
            }
        }
        return score;
        // quiet move -> score = 0 
    }

    /**
     * Calculates who is winning based on standard piece values.
     * White scores are positive, Black scores are negative.
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int r = pos / 8;
            int c = pos % 8;
            Piece piece = board.getSquare(r, c).getPiece();
            if (piece != null) {
                int pieceValue = getPieceValue(piece);
                // Add for White, subtract for Black
                if (piece.isWhite()) {
                    score += pieceValue;
                } else {
                    score -= pieceValue;
                }
            }
        }
        return score;
    }

    // minimax + alpha beta prunning implementation
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMax) {
        // isMax is to check at current level is max or min turn
        if (depth == 0) {
            // based-case
            return evaluateBoard(board);
        }
        List<Move> legalMoves = getAllLegalMoves(board, isMax);
        legalMoves.sort((m1, m2) -> scoreMove(board, m2) - scoreMove(board, m1));
        if (legalMoves.isEmpty()) {
            if (board.isChecked(isMax)) {
                // getting checkmated (losing state)
                return isMax ? -99999 : 99999;
            }
            // this is Stalemate (draw state)
            return 0;
        }
        if (isMax) {
            // Max level
            int maxEval = Integer.MIN_VALUE; // update -> fixed: was wrongly Integer.MAX_VALUE
            for (Move move : legalMoves) {
                // try to move that piece
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end = board.getSquare(move.endRow, move.endCol);
                Piece movingPiece = start.getPiece();

                boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

                int startPos = move.startRow * 8 + move.startCol;
                int endPos = move.endRow * 8 + move.endCol;

                // update -> handle pawn promotion during simulation
                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true); // update -> set isMoved flag for simulation

                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) {
                    board.addActivePiece(endPos);
                }

                // recurse minimax
                int eval = minimax(board, depth - 1, alpha, beta, false);

                // undo
                movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);

                if (move.capturedPiece == null) {
                    board.removeActivePiece(endPos);
                }
                board.addActivePiece(startPos);

                // alpha-beta prunning
                // from left to right, child inherits from parents,
                // only updating alpha,beta at that current node with the value of
                // nodes.

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha)
                    break; // prunning condition
            }
            return maxEval;
        } else {
            // black's turn
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                // Simulate
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end = board.getSquare(move.endRow, move.endCol);
                Piece movingPiece = start.getPiece();

                boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

                int startPos = move.startRow * 8 + move.startCol;
                int endPos = move.endRow * 8 + move.endCol;

                // update -> handle pawn promotion during simulation
                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true); // update -> set isMoved flag for simulation

                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) {
                    board.addActivePiece(endPos);
                }

                // Recurse
                int eval = minimax(board, depth - 1, alpha, beta, true);

                // Undo
                movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);

                if (move.capturedPiece == null) {
                    board.removeActivePiece(endPos);
                }
                board.addActivePiece(startPos);
                // Alpha-Beta Pruning
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break; // Prune branch!
            }
            return minEval;
        }
    }

    // find the best move
    public Move getBestMove(Board board, boolean isWhite) {
        List<Move> legalMoves = getAllLegalMoves(board, isWhite);
        legalMoves.sort((m1, m2) -> scoreMove(board, m2) - scoreMove(board, m1));
        Move bestMove = null;

        // White wants to maximize score, Black wants to minimize score;
        int bestScore = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : legalMoves) {
            // 1. Setup the Simulation
            Square start = board.getSquare(move.startRow, move.startCol);
            Square end = board.getSquare(move.endRow, move.endCol);
            Piece movingPiece = start.getPiece();

            boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag

            int startPos = move.startRow * 8 + move.startCol;
            int endPos = move.endRow * 8 + move.endCol;

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

            board.removeActivePiece(startPos);
            if (move.capturedPiece == null) {
                board.addActivePiece(endPos);
            }

            // 3. Dig deeper into the tree
            int score = minimax(board, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);

            // 4. UNDO the move (Restore the exact state)
            movingPiece.setMoved(originalMovedFlag); // update -> restore original isMoved flag
            start.setPiece(movingPiece);
            end.setPiece(move.capturedPiece);

            if (move.capturedPiece == null) {
                board.removeActivePiece(endPos);
            }
            board.addActivePiece(startPos);
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
