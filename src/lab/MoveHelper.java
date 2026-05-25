package lab;

public class MoveHelper {

    /**
     * Finds the Square containing the King of the specified color.
     */
    public static Square findKing(Board board, boolean isWhite) {
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int r = pos / 8;
            int c = pos % 8;
            Square square = board.getSquare(r, c);
            Piece piece = square.getPiece();
            if (piece instanceof King && piece.isWhite() == isWhite) {
                return square;
            }
        }
        return null;
    }

    /**
     * Checks if the King of the specified color is currently in check.
     */
    public static boolean isInCheck(Board board, boolean isWhite) {
        Square kingSquare = findKing(board, isWhite);
        if (kingSquare == null) {
            return false;
        }

        // Loop through all squares to find opposing pieces that can capture the King
        for (int k = 0; k < board.activePieceCount; k++) {
            int pos = board.activePieceCoords[k];
            int r = pos / 8;
            int c = pos % 8;
            Square square = board.getSquare(r, c);
            Piece piece = square.getPiece();
            if (piece != null && piece.isWhite() != isWhite) {
                if (piece.isValidMove(board, square, kingSquare)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Simulates a move to check if it results in the player's own King being in check.
     */
    public static boolean willMoveResultInCheck(Board board, Square start, Square end, boolean isWhite) {
        Piece movingPiece = start.getPiece();
        Piece capturedPiece = end.getPiece();

        int startPos = start.getRow() * 8 + start.getCol();
        int endPos = end.getRow() * 8 + end.getCol();

        // 1. Temporarily make the move
        end.setPiece(movingPiece);
        start.setPiece(null);

        // Update king square if king moves
        if (movingPiece instanceof King) {
            if (isWhite) board.whiteKingSquare = end;
            else board.blackKingSquare = end;
        }

        // 2. Evaluate if the King is in check
        boolean inCheck = isInCheck(board, isWhite);

        // 3. Revert the move
        start.setPiece(movingPiece);
        end.setPiece(capturedPiece);

        // Revert king square if king moved
        if (movingPiece instanceof King) {
            if (isWhite) board.whiteKingSquare = start;
            else board.blackKingSquare = start;
        }

        return inCheck;
    }

    /**
     * Returns true if the specified player has at least one legal move.
     * A move is legal if the piece can make the move and it does not leave the King in check.
     */
    public static boolean hasLegalMoves(Board board, boolean isWhite) {
        int[] pieces = new int[board.activePieceCount];
        System.arraycopy(board.activePieceCoords, 0, pieces, 0, board.activePieceCount);
        for (int k = 0; k < pieces.length; k++) {
            int pos = pieces[k];
            int r = pos / 8;
            int c = pos % 8;
            Square startSquare = board.getSquare(r, c);
            Piece piece = startSquare.getPiece();

            if (piece != null && piece.isWhite() == isWhite) {
                // Check every possible destination square
                for (int tr = 0; tr < 8; tr++) {
                    for (int tc = 0; tc < 8; tc++) {
                        Square endSquare = board.getSquare(tr, tc);

                        if (piece.isValidMove(board, startSquare, endSquare)) {
                            if (!willMoveResultInCheck(board, startSquare, endSquare, isWhite)) {
                                return true; // Found a legal move!
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the specified player is in Checkmate.
     * Checkmate occurs if the player is in check and has zero legal moves.
     */
    public static boolean isCheckmate(Board board, boolean isWhite) {
        return isInCheck(board, isWhite) && !hasLegalMoves(board, isWhite);
    }

    /**
     * Checks if the specified player is in Stalemate (Draw).
     * Stalemate occurs if the player is NOT in check but has zero legal moves.
     */
    public static boolean isStalemate(Board board, boolean isWhite) {
        return !isInCheck(board, isWhite) && !hasLegalMoves(board, isWhite);
    }
}
