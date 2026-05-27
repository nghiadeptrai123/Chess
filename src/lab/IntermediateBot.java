package lab;

import java.util.ArrayList;
import java.util.List;

public class IntermediateBot implements ChessBot {
    private static final int SEARCH_DEPTH = 6;
    // depth 6 + positional tables for intermediate
    // POSITIONAL TABLES  (all from White's perspective)
    //   Row 0 = White's back rank, Row 7 = Black's back rank / White promo row
    //   White lookup : tableRow = row
    //   Black lookup : tableRow = 7 - row   (vertical mirror
    // PAWN — Opening: reward center advance, punish passive wing pawns
    private static final int[][] PAWN_OPENING = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 0: back rank (impossible for pawns)
        { 5, 10, 10,-20,-20, 10, 10,  5},  // row 1: start — punish blocked center
        { 5, -5,-10,  0,  0,-10, -5,  5},  // row 2: slightly passive
        { 0,  0,  0, 20, 20,  0,  0,  0},  // row 3: center control!
        { 5,  5, 10, 25, 25, 10,  5,  5},  // row 4: advanced center
        {10, 10, 20, 30, 30, 20, 10, 10},  // row 5: very advanced
        {50, 50, 50, 50, 50, 50, 50, 50},  // row 6: almost promoted!
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 7: promotion row
    };

    // PAWN — Endgame: just push forward, every step matters equally
    private static final int[][] PAWN_ENDGAME = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 0
        { 5,  5,  5,  5,  5,  5,  5,  5},  // row 1: must advance
        {10, 10, 10, 10, 10, 10, 10, 10},  // row 2
        {20, 20, 20, 20, 20, 20, 20, 20},  // row 3
        {30, 30, 30, 30, 30, 30, 30, 30},  // row 4
        {50, 50, 50, 50, 50, 50, 50, 50},  // row 5
        {50, 50, 50, 50, 50, 50, 50, 50},  // row 6: near promotion (50 not 80 to avoid double-count with isPromotion)
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 7
    };

    // KNIGHT — Opening: "knight on the rim is dim" — heavily penalize edges
    private static final int[][] KNIGHT_OPENING = {
        {-50,-40,-30,-30,-30,-30,-40,-50},  // row 0
        {-40,-20,  0,  5,  5,  0,-20,-40},  // row 1
        {-30,  0, 10, 15, 15, 10,  0,-30},  // row 2
        {-30,  5, 15, 20, 20, 15,  5,-30},  // row 3: ideal (d3/e3)
        {-30,  0, 15, 20, 20, 15,  0,-30},  // row 4
        {-30,  5, 10, 15, 15, 10,  5,-30},  // row 5
        {-40,-20,  0,  5,  5,  0,-20,-40},  // row 6
        {-50,-40,-30,-30,-30,-30,-40,-50},  // row 7
    };

    // KNIGHT — Endgame: still prefer center but smaller penalties at edges
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

    // BISHOP — Opening: long diagonals, fianchetto positions, avoid corners
    private static final int[][] BISHOP_OPENING = {
        {-20,-10,-10,-10,-10,-10,-10,-20},  // row 0
        {-10,  5,  0,  0,  0,  0,  5,-10},  // row 1: b1/g1 fianchetto squares
        {-10, 10, 10, 10, 10, 10, 10,-10},  // row 2: long diagonal!
        {-10,  0, 10, 10, 10, 10,  0,-10},  // row 3
        {-10,  5,  5, 10, 10,  5,  5,-10},  // row 4
        {-10,  0,  5, 10, 10,  5,  0,-10},  // row 5
        {-10,  0,  0,  0,  0,  0,  0,-10},  // row 6
        {-20,-10,-10,-10,-10,-10,-10,-20},  // row 7
    };

    // BISHOP — Endgame: active on open diagonals through center
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

    // ROOK — Opening: 7th rank is dominant, slightly prefer central files on back rank
    private static final int[][] ROOK_OPENING = {
        { 0,  0,  0,  5,  5,  0,  0,  0},  // row 0: back rank — d/e files slightly better
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 1
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 2
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 3
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 4
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 5
        { 5, 10, 10, 10, 10, 10, 10,  5},  // row 6: 7th rank! Controls enemy pawns
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 7: 8th rank
    };

    // ROOK — Endgame: 7th rank even more decisive, can cut off enemy king
    private static final int[][] ROOK_ENDGAME = {
        { 0,  0,  0,  0,  0,  0,  0,  0},  // row 0
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 1
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 2
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 3
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 4
        {-5,  0,  0,  0,  0,  0,  0, -5},  // row 5
        {10, 10, 10, 10, 10, 10, 10, 10},  // row 6: 7th rank dominance
        { 5,  5,  5,  5,  5,  5,  5,  5},  // row 7: 8th rank cutoff
    };

    // QUEEN — Opening: don't develop early! Penalize premature queen excursions
    private static final int[][] QUEEN_OPENING = {
        {-20,-10,-10, -5, -5,-10,-10,-20},  // row 0
        {-10,  0,  5,  0,  0,  0,  0,-10},  // row 1
        {-10,  5,  5,  5,  5,  5,  0,-10},  // row 2
        { -5,  0,  5,  5,  5,  5,  0, -5},  // row 3: d-file ideal
        {  0,  0,  5,  5,  5,  5,  0, -5},  // row 4
        {-10,  5,  5,  5,  5,  5,  0,-10},  // row 5
        {-10,  0,  5,  0,  0,  0,  0,-10},  // row 6
        {-20,-10,-10, -5, -5,-10,-10,-20},  // row 7
    };

    // QUEEN — Endgame: active centralized queen is decisive
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

    // KING — Opening: HIDE! Castled positions (cols 1-2 / cols 5-6) are safest.
    // Penalize king in center heavily. MOST IMPACTFUL TABLE.
    private static final int[][] KING_OPENING = {
        { 20, 30, 10,  0,  0, 10, 30, 20},  // row 0: g1/c1 (castle)
        { 20, 20,  0,  0,  0,  0, 20, 20},  // row 1: behind pawns
        {-10,-20,-20,-20,-20,-20,-20,-10},  // row 2: exposed!
        {-20,-30,-30,-40,-40,-30,-30,-20},  // row 3: dangerous
        {-30,-40,-40,-50,-50,-40,-40,-30},  // row 4: terrible
        {-30,-40,-40,-50,-50,-40,-40,-30},  // row 5: terrible
        {-30,-40,-40,-50,-50,-40,-40,-30},  // row 6: terrible
        {-30,-40,-40,-50,-50,-40,-40,-30},  // row 7: enemy territory
    };

    // KING — Endgame: MARCH to center! Active king wins endgames.
    private static final int[][] KING_ENDGAME = {
        {-50,-40,-30,-20,-20,-30,-40,-50},  // row 0: corners bad
        {-30,-20,-10,  0,  0,-10,-20,-30},  // row 1
        {-30,-10, 20, 30, 30, 20,-10,-30},  // row 2: good center
        {-30,-10, 30, 40, 40, 30,-10,-30},  // row 3: ideal!
        {-30,-10, 30, 40, 40, 30,-10,-30},  // row 4: ideal!
        {-30,-10, 20, 30, 30, 20,-10,-30},  // row 5: good center
        {-30,-30,  0,  0,  0,  0,-30,-30},  // row 6
        {-50,-30,-30,-30,-30,-30,-30,-50},  // row 7
    };

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: look up the correct positional table for a piece and phase.
    // tableRow is already mirrored by the caller:
    //   White → tableRow = row;  Black → tableRow = 7 - row
    // ─────────────────────────────────────────────────────────────────────────
    private int getPositionalBonus(Piece piece, int tableRow, int col, boolean isEndgame) {
        if (piece instanceof Pawn)   return isEndgame ? PAWN_ENDGAME[tableRow][col]   : PAWN_OPENING[tableRow][col];
        if (piece instanceof Knight) return isEndgame ? KNIGHT_ENDGAME[tableRow][col] : KNIGHT_OPENING[tableRow][col];
        if (piece instanceof Bishop) return isEndgame ? BISHOP_ENDGAME[tableRow][col] : BISHOP_OPENING[tableRow][col];
        if (piece instanceof Rook)   return isEndgame ? ROOK_ENDGAME[tableRow][col]   : ROOK_OPENING[tableRow][col];
        if (piece instanceof Queen)  return isEndgame ? QUEEN_ENDGAME[tableRow][col]  : QUEEN_OPENING[tableRow][col];
        if (piece instanceof King)   return isEndgame ? KING_ENDGAME[tableRow][col]   : KING_OPENING[tableRow][col];
        return 0;
    }
// the folloiwng is identical  to amATEUR BOTH, except the eval function including the positional bonus and the priority order included position bonus
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
                for (int i_final = 0; i_final < 8; ++i_final) {
                    for (int j_final = 0; j_final < 8; ++j_final) {
                        Square endSquare = board.getSquare(i_final, j_final);
                        if (piece.isValidMove(board, startSquare, endSquare)) {
                            if (!board.willMoveResultInCheck(startSquare, endSquare, isWhite)) {
                                Move m = new Move(i, j, i_final, j_final, endSquare.getPiece());
                                if (piece instanceof Pawn) {
                                    int promotionRow = piece.isWhite() ? 7 : 0;
                                    if (i_final == promotionRow) {
                                        m.isPromotion = true;
                                    }
                                }
                                    if (j != j_final && m.capturedPiece == null) {
                                        m.isEnPassant = true;
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

    private int scoreMove(Board board, Move move) {
        // Sort legal moves for better alpha-beta pruning
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
        // Add positional bonus (same idea as your evaluation but in scoreMove)
        boolean isEndgame = board.activePieceCount <= 18;
        boolean isWhite   = attacker.isWhite();
        int toRow   = isWhite ? move.endRow   : (7 - move.endRow);
        int fromRow = isWhite ? move.startRow : (7 - move.startRow);
        int toBonus   = getPositionalBonus(attacker, toRow,   move.endCol,   isEndgame);
        int fromBonus = getPositionalBonus(attacker, fromRow, move.startCol, isEndgame);
        score += (toBonus - fromBonus);  // prefer moves that improve positional score

        return score;
        // quiet move → score = 0
    }

    /**
     * UPGRADED evaluateBoard — two-pass approach:
     *   Pass 1: detect game phase via activePieceCount  (O(1))
     *   Pass 2: material + positional bonus for each piece (O(P))
     *
     * Phase rule:
     *   activePieceCount > 18  → opening/middlegame tables
     *   activePieceCount <= 18 → endgame tables
     *   (18 = 2 kings + 16 pawns — fires when all major/minor pieces are traded)
     *
     * White scores are positive, Black scores are negative.
     */
    private int evaluateBoard(Board board) {
        // PASS 1: detect phase (O(1) — already tracked by board)
        boolean isEndgame = board.activePieceCount <= 18;

        // PASS 2: score all active pieces
        int score = 0;
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int r   = pos / 8;
            int c   = pos % 8;
            Piece piece = board.getSquare(r, c).getPiece();
            if (piece != null) {
                boolean isWhite = piece.isWhite();
                // Mirror row for Black so tables are from each side's perspective
                int tableRow   = isWhite ? r : (7 - r);
                int material   = getPieceValue(piece);
                int positional = getPositionalBonus(piece, tableRow, c, isEndgame);
                int total      = material + positional;
                // Add for White, subtract for Black
                score += isWhite ? +total : -total;
            }
        }
        return score;
    }

    // minimax + alpha-beta pruning (identical to AmateurBot)
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMax) {
        if (depth == 0) {
            return evaluateBoard(board);
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
                Square prevEnPassantTarget = board.enPassantTarget;
                
                Square epPawnSquare = null;
                Piece epCapturedPiece = null;
                int epCapturedPos = -1;
                if (move.isEnPassant) {
                    epPawnSquare = board.getSquare(move.startRow, move.endCol);
                    epCapturedPiece = epPawnSquare.getPiece();
                    epCapturedPos = epPawnSquare.getRow() * 8 + epPawnSquare.getCol();
                    epPawnSquare.setPiece(null);
                    board.removeActivePiece(epCapturedPos);
                }
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
                if (move.capturedPiece == null && !move.isEnPassant) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                if (movingPiece instanceof Pawn && Math.abs(move.startRow - move.endRow) == 2) {
                    board.enPassantTarget = board.getSquare((move.startRow + move.endRow) / 2, move.startCol);
                } else {
                    board.enPassantTarget = null;
                }

                int eval = minimax(board, depth - 1, alpha, beta, false);

                board.enPassantTarget = prevEnPassantTarget;
                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null && !move.isEnPassant) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);

                if (move.isEnPassant) {
                    epPawnSquare.setPiece(epCapturedPiece);
                    board.addActivePiece(epCapturedPos);
                }
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
                Square prevEnPassantTarget = board.enPassantTarget;
                
                Square epPawnSquare = null;
                Piece epCapturedPiece = null;
                int epCapturedPos = -1;
                if (move.isEnPassant) {
                    epPawnSquare = board.getSquare(move.startRow, move.endCol);
                    epCapturedPiece = epPawnSquare.getPiece();
                    epCapturedPos = epPawnSquare.getRow() * 8 + epPawnSquare.getCol();
                    epPawnSquare.setPiece(null);
                    board.removeActivePiece(epCapturedPos);
                }
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
                if (move.capturedPiece == null && !move.isEnPassant) board.addActivePiece(endPos);
                if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                if (movingPiece instanceof Pawn && Math.abs(move.startRow - move.endRow) == 2) {
                    board.enPassantTarget = board.getSquare((move.startRow + move.endRow) / 2, move.startCol);
                } else {
                    board.enPassantTarget = null;
                }

                int eval = minimax(board, depth - 1, alpha, beta, true);

                board.enPassantTarget = prevEnPassantTarget;
                movingPiece.setMoved(originalMovedFlag);
                start.setPiece(movingPiece);
                end.setPiece(move.capturedPiece);
                if (move.capturedPiece == null && !move.isEnPassant) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);

                if (move.isEnPassant) {
                    epPawnSquare.setPiece(epCapturedPiece);
                    board.addActivePiece(epCapturedPos);
                }
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

    // find the best move
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
                Square prevEnPassantTarget = board.enPassantTarget;
                
                Square epPawnSquare = null;
                Piece epCapturedPiece = null;
                int epCapturedPos = -1;
                if (move.isEnPassant) {
                    epPawnSquare = board.getSquare(move.startRow, move.endCol);
                    epCapturedPiece = epPawnSquare.getPiece();
                    epCapturedPos = epPawnSquare.getRow() * 8 + epPawnSquare.getCol();
                    epPawnSquare.setPiece(null);
                    board.removeActivePiece(epCapturedPos);
                }
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
            if (move.capturedPiece == null && !move.isEnPassant) board.addActivePiece(endPos);
            if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                if (movingPiece instanceof Pawn && Math.abs(move.startRow - move.endRow) == 2) {
                    board.enPassantTarget = board.getSquare((move.startRow + move.endRow) / 2, move.startCol);
                } else {
                    board.enPassantTarget = null;
                }

            int score = minimax(board, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);

            board.enPassantTarget = prevEnPassantTarget;
                movingPiece.setMoved(originalMovedFlag);
            start.setPiece(movingPiece);
            end.setPiece(move.capturedPiece);
            if (move.capturedPiece == null && !move.isEnPassant) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);

                if (move.isEnPassant) {
                    epPawnSquare.setPiece(epCapturedPiece);
                    board.addActivePiece(epCapturedPos);
                }
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
