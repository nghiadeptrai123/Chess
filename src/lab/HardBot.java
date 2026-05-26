package lab;

import java.util.ArrayList;
import java.util.List;

public class HardBot implements ChessBot {
    private static final int SEARCH_DEPTH = 6;
    // depth 6 + positional tables + Quiescence Search

    // ─────────────────────────────────────────────────────────────────────────
    // POSITIONAL TABLES  (all from White's perspective)
    //   Row 0 = White's back rank, Row 7 = Black's back rank / White promo row
    //   White lookup : tableRow = row
    //   Black lookup : tableRow = 7 - row   (vertical mirror)
    // ─────────────────────────────────────────────────────────────────────────

    private static final int[][] PAWN_OPENING = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  
        { 5, 10, 10,-20,-20, 10, 10,  5},  
        { 5, -5,-10,  0,  0,-10, -5,  5},  
        { 0,  0,  0, 20, 20,  0,  0,  0},  
        { 5,  5, 10, 25, 25, 10,  5,  5},  
        {10, 10, 20, 30, 30, 20, 10, 10},  
        {50, 50, 50, 50, 50, 50, 50, 50},  
        { 0,  0,  0,  0,  0,  0,  0,  0},  
    };

    private static final int[][] PAWN_ENDGAME = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  
        { 5,  5,  5,  5,  5,  5,  5,  5},  
        {10, 10, 10, 10, 10, 10, 10, 10},  
        {20, 20, 20, 20, 20, 20, 20, 20},  
        {30, 30, 30, 30, 30, 30, 30, 30},  
        {50, 50, 50, 50, 50, 50, 50, 50},  
        {50, 50, 50, 50, 50, 50, 50, 50},  
        { 0,  0,  0,  0,  0,  0,  0,  0},  
    };

    private static final int[][] KNIGHT_OPENING = {
        {-50,-40,-30,-30,-30,-30,-40,-50},  
        {-40,-20,  0,  5,  5,  0,-20,-40},  
        {-30,  0, 10, 15, 15, 10,  0,-30},  
        {-30,  5, 15, 20, 20, 15,  5,-30},  
        {-30,  0, 15, 20, 20, 15,  0,-30},  
        {-30,  5, 10, 15, 15, 10,  5,-30},  
        {-40,-20,  0,  5,  5,  0,-20,-40},  
        {-50,-40,-30,-30,-30,-30,-40,-50},  
    };

    private static final int[][] KNIGHT_ENDGAME = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 10, 10, 10,  0,-30},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  0, 10, 10, 10, 10,  0,-30},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50},
    };

    private static final int[][] BISHOP_OPENING = {
        {-20,-10,-10,-10,-10,-10,-10,-20},  
        {-10,  5,  0,  0,  0,  0,  5,-10},  
        {-10, 10, 10, 10, 10, 10, 10,-10},  
        {-10,  0, 10, 10, 10, 10,  0,-10},  
        {-10,  5,  5, 10, 10,  5,  5,-10},  
        {-10,  0,  5, 10, 10,  5,  0,-10},  
        {-10,  0,  0,  0,  0,  0,  0,-10},  
        {-20,-10,-10,-10,-10,-10,-10,-20},  
    };

    private static final int[][] BISHOP_ENDGAME = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20},
    };

    private static final int[][] ROOK_OPENING = {
        { 0,  0,  0,  5,  5,  0,  0,  0},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        { 5, 10, 10, 10, 10, 10, 10,  5},  
        { 0,  0,  0,  0,  0,  0,  0,  0},  
    };

    private static final int[][] ROOK_ENDGAME = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {-5,  0,  0,  0,  0,  0,  0, -5},  
        {10, 10, 10, 10, 10, 10, 10, 10},  
        { 5,  5,  5,  5,  5,  5,  5,  5},  
    };

    private static final int[][] QUEEN_OPENING = {
        {-20,-10,-10, -5, -5,-10,-10,-20},  
        {-10,  0,  5,  0,  0,  0,  0,-10},  
        {-10,  5,  5,  5,  5,  5,  0,-10},  
        { -5,  0,  5,  5,  5,  5,  0, -5},  
        {  0,  0,  5,  5,  5,  5,  0, -5},  
        {-10,  5,  5,  5,  5,  5,  0,-10},  
        {-10,  0,  5,  0,  0,  0,  0,-10},  
        {-20,-10,-10, -5, -5,-10,-10,-20},  
    };

    private static final int[][] QUEEN_ENDGAME = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        { -5,  0,  5, 10, 10,  5,  0, -5},
        { -5,  0,  5, 10, 10,  5,  0, -5},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20},
    };

    private static final int[][] KING_OPENING = {
        { 20, 30, 10,  0,  0, 10, 30, 20},  
        { 20, 20,  0,  0,  0,  0, 20, 20},  
        {-10,-20,-20,-20,-20,-20,-20,-10},  
        {-20,-30,-30,-40,-40,-30,-30,-20},  
        {-30,-40,-40,-50,-50,-40,-40,-30},  
        {-30,-40,-40,-50,-50,-40,-40,-30},  
        {-30,-40,-40,-50,-50,-40,-40,-30},  
        {-30,-40,-40,-50,-50,-40,-40,-30},  
    };

    private static final int[][] KING_ENDGAME = {
        {-50,-40,-30,-20,-20,-30,-40,-50},  
        {-30,-20,-10,  0,  0,-10,-20,-30},  
        {-30,-10, 20, 30, 30, 20,-10,-30},  
        {-30,-10, 30, 40, 40, 30,-10,-30},  
        {-30,-10, 30, 40, 40, 30,-10,-30},  
        {-30,-10, 20, 30, 30, 20,-10,-30},  
        {-30,-30,  0,  0,  0,  0,-30,-30},  
        {-50,-30,-30,-30,-30,-30,-30,-50},  
    };

    private int getPositionalBonus(Piece piece, int tableRow, int col, boolean isEndgame) {
        if (piece instanceof Pawn)   return isEndgame ? PAWN_ENDGAME[tableRow][col]   : PAWN_OPENING[tableRow][col];
        if (piece instanceof Knight) return isEndgame ? KNIGHT_ENDGAME[tableRow][col] : KNIGHT_OPENING[tableRow][col];
        if (piece instanceof Bishop) return isEndgame ? BISHOP_ENDGAME[tableRow][col] : BISHOP_OPENING[tableRow][col];
        if (piece instanceof Rook)   return isEndgame ? ROOK_ENDGAME[tableRow][col]   : ROOK_OPENING[tableRow][col];
        if (piece instanceof Queen)  return isEndgame ? QUEEN_ENDGAME[tableRow][col]  : QUEEN_OPENING[tableRow][col];
        if (piece instanceof King)   return isEndgame ? KING_ENDGAME[tableRow][col]   : KING_OPENING[tableRow][col];
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────

    private List<Move> getLegalMoves(Board board, boolean isWhite, boolean capturesOnly) {
        List<Move> moves = new ArrayList<>();
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int i = pos / 8;
            int j = pos % 8;
            Square startSquare = board.getSquare(i, j);
            Piece piece = startSquare.getPiece();

            if (piece != null && piece.isWhite() == isWhite) {
                for (int i_final = 0; i_final < 8; ++i_final) {
                    for (int j_final = 0; j_final < 8; ++j_final) {
                        Square endSquare = board.getSquare(i_final, j_final);

                        if (capturesOnly) {
                            boolean isCapture = endSquare.getPiece() != null;
                            boolean isPromotion = (piece instanceof Pawn) && (i_final == 0 || i_final == 7);
                            if (!isCapture && !isPromotion) {
                                continue;
                            }
                        }

                        if (piece.isValidMove(board, startSquare, endSquare)) {
                            if (!board.willMoveResultInCheck(startSquare, endSquare, isWhite)) {
                                Move m = new Move(i, j, i_final, j_final, endSquare.getPiece());
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

    private List<Move> getAllLegalMoves(Board board, boolean isWhite) {
        return getLegalMoves(board, isWhite, false);
    }

    private int getPieceValue(Piece piece) {
        if (piece instanceof Queen)  return 900;
        if (piece instanceof Rook)   return 500;
        if (piece instanceof Bishop) return 300;
        if (piece instanceof Knight) return 300;
        if (piece instanceof Pawn)   return 100;
        return 0; // King
    }

    private int scoreMove(Board board, Move move) {
        int score = 0;
        Piece attacker = board.getSquare(move.startRow, move.startCol).getPiece();
        if (move.isPromotion) {
            score += 8000;
        } else {
            if (move.capturedPiece != null) {
                int attackerVal = getPieceValue(attacker);
                int victimVal   = getPieceValue(move.capturedPiece);
                score += 10 * victimVal - attackerVal;
            }
        }
        
        // Add positional bonus delta
        boolean isEndgame = board.activePieceCount <= 18;
        boolean isWhite   = attacker.isWhite();
        int toRow   = isWhite ? move.endRow   : (7 - move.endRow);
        int fromRow = isWhite ? move.startRow : (7 - move.startRow);
        int toBonus   = getPositionalBonus(attacker, toRow,   move.endCol,   isEndgame);
        int fromBonus = getPositionalBonus(attacker, fromRow, move.startCol, isEndgame);
        score += (toBonus - fromBonus);  

        return score;
    }

    private int evaluateBoard(Board board) {
        boolean isEndgame = board.activePieceCount <= 18;
        int score = 0;
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int r   = pos / 8;
            int c   = pos % 8;
            Piece piece = board.getSquare(r, c).getPiece();
            if (piece != null) {
                boolean isWhite = piece.isWhite();
                int tableRow   = isWhite ? r : (7 - r);
                int material   = getPieceValue(piece);
                int positional = getPositionalBonus(piece, tableRow, c, isEndgame);
                int total      = material + positional;
                score += isWhite ? +total : -total;
            }
        }
        return score;
    }

    private int quiescenceSearch(Board board, int alpha, int beta, boolean isMax) {
        int standPat = evaluateBoard(board);
        
        if (isMax) {
            if (standPat >= beta) return beta;
            if (alpha < standPat) alpha = standPat;
        } else {
            if (standPat <= alpha) return alpha;
            if (beta > standPat) beta = standPat;
        }

        List<Move> captures = getLegalMoves(board, isMax, true);
        captures.sort((m1, m2) -> scoreMove(board, m2) - scoreMove(board, m1));

        if (isMax) {
            int maxEval = standPat;
            for (Move move : captures) {
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end   = board.getSquare(move.endRow,   move.endCol);
                Piece movingPiece = start.getPiece();
                boolean originalMovedFlag = movingPiece.isMoved();
                int startPos = move.startRow * 8 + move.startCol;
                int endPos   = move.endRow   * 8 + move.endCol;

                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true);
                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                int eval = quiescenceSearch(board, alpha, beta, false);

                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = start;
                    else                       board.blackKingSquare = start;
                }

                maxEval = Math.max(maxEval, eval);
                alpha   = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = standPat;
            for (Move move : captures) {
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end   = board.getSquare(move.endRow,   move.endCol);
                Piece movingPiece = start.getPiece();
                boolean originalMovedFlag = movingPiece.isMoved();
                int startPos = move.startRow * 8 + move.startCol;
                int endPos   = move.endRow   * 8 + move.endCol;

                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true);
                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                int eval = quiescenceSearch(board, alpha, beta, true);

                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = start;
                    else                       board.blackKingSquare = start;
                }

                minEval = Math.min(minEval, eval);
                beta    = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMax) {
        if (depth == 0) {
            return quiescenceSearch(board, alpha, beta, isMax);
        }
        
        List<Move> legalMoves = getAllLegalMoves(board, isMax);
        legalMoves.sort((m1, m2) -> scoreMove(board, m2) - scoreMove(board, m1));
        
        if (legalMoves.isEmpty()) {
            if (board.isChecked(isMax)) {
                return isMax ? -99999 : 99999;
            }
            return 0; // Stalemate
        }
        
        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end   = board.getSquare(move.endRow,   move.endCol);
                Piece movingPiece = start.getPiece();
                boolean originalMovedFlag = movingPiece.isMoved();
                int startPos = move.startRow * 8 + move.startCol;
                int endPos   = move.endRow   * 8 + move.endCol;

                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true);
                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                int eval = minimax(board, depth - 1, alpha, beta, false);

                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = start;
                    else                       board.blackKingSquare = start;
                }

                maxEval = Math.max(maxEval, eval);
                alpha   = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                Square start = board.getSquare(move.startRow, move.startCol);
                Square end   = board.getSquare(move.endRow,   move.endCol);
                Piece movingPiece = start.getPiece();
                boolean originalMovedFlag = movingPiece.isMoved();
                int startPos = move.startRow * 8 + move.startCol;
                int endPos   = move.endRow   * 8 + move.endCol;

                if (move.isPromotion && movingPiece instanceof Pawn) {
                    end.setPiece(new Queen(movingPiece.isWhite()));
                    start.setPiece(null);
                } else {
                    end.setPiece(movingPiece);
                    start.setPiece(null);
                }
                movingPiece.setMoved(true);
                board.removeActivePiece(startPos);
                if (move.capturedPiece == null) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                int eval = minimax(board, depth - 1, alpha, beta, true);

                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = start;
                    else                       board.blackKingSquare = start;
                }

                minEval = Math.min(minEval, eval);
                beta    = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    public Move getBestMove(Board board, boolean isWhite) {
        List<Move> legalMoves = getAllLegalMoves(board, isWhite);
        legalMoves.sort((m1, m2) -> scoreMove(board, m2) - scoreMove(board, m1));
        Move bestMove  = null;
        int  bestScore = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            Square start = board.getSquare(move.startRow, move.startCol);
            Square end   = board.getSquare(move.endRow,   move.endCol);
            Piece movingPiece = start.getPiece();
            boolean originalMovedFlag = movingPiece.isMoved();
            int startPos = move.startRow * 8 + move.startCol;
            int endPos   = move.endRow   * 8 + move.endCol;

            if (move.isPromotion && movingPiece instanceof Pawn) {
                end.setPiece(new Queen(movingPiece.isWhite()));
                start.setPiece(null);
            } else {
                end.setPiece(movingPiece);
                start.setPiece(null);
            }
            movingPiece.setMoved(true);
            board.removeActivePiece(startPos);
            if (move.capturedPiece == null) board.addActivePiece(endPos);
            if (movingPiece instanceof King) {
                if (movingPiece.isWhite()) board.whiteKingSquare = end;
                else                       board.blackKingSquare = end;
            }

            int score = minimax(board, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);

            movingPiece.setMoved(originalMovedFlag);
            start.setPiece(movingPiece);
            end.setPiece(move.capturedPiece);
            if (move.capturedPiece == null) board.removeActivePiece(endPos);
            board.addActivePiece(startPos);
            if (movingPiece instanceof King) {
                if (movingPiece.isWhite()) board.whiteKingSquare = start;
                else                       board.blackKingSquare = start;
            }

            if (isWhite) {
                if (score > bestScore) { bestScore = score; bestMove = move; }
            } else {
                if (score < bestScore) { bestScore = score; bestMove = move; }
            }
        }
        return bestMove;
    }
}
