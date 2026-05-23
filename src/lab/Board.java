package lab;

public class Board {
	private Square[][] board = new Square[8][8];
	public Piece[] pieces;

	Board() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				boolean color = ((i + j) % 2 == 0) ? false : true;
				board[i][j] = new Square(color, i, j);
			}
		}
	}

	public Square getSquare(int row, int col) {
		return board[row][col];
	}

	public Square findKing(boolean isWhite) {
		// tim vi tri cua vua (dung de lam tinh nang checkmate)
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				Square square = board[i][j];
				Piece piece = square.getPiece();
				if (piece instanceof King && piece.isWhite() == isWhite) {
					// tim thay vua den hoac trang
					return square;
				}
			}
		}
		return null;
		// k tim thay vua
	}

	// check detection
	public boolean isChecked(boolean isWhite) {
		Square kingSquare = findKing(isWhite);
		if (kingSquare == null) {
			return false;
			// no king
		}
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				Square square = board[i][j];
				Piece piece = square.getPiece();
				if (piece != null && piece.isWhite() != isWhite) {
					// check if that piece is an enemy piece or not
					if (piece.isValidMove(this, square, kingSquare)) {
						// con vua dang bi chieu tuong
						return true;
					}
				}
			}
		}
		return false; // k phai checkmate
	}

	/*
	 * if you are already in check, your next move must block the attack, capture
	 * the attacking piece, or move the King to safety.
	 * if a piece is currently protecting the king from checkmate position, you are
	 * not allow to move it
	 * -> this functions as a temporarily simulation to see if that move satisfies
	 * the above conditions
	 */

	public boolean willMoveResultInCheck(Square start, Square end, boolean isWhite) {
		Piece movingPiece = start.getPiece();
		Piece capturedPiece = end.getPiece();
		// temporary simulate the state ( assuming it is a valid move)
		end.setPiece(movingPiece);
		start.setPiece(null);
		boolean flag = isChecked(isWhite);
		start.setPiece(movingPiece);
		end.setPiece(capturedPiece);

		return flag;
	}

	// checkmate (win) stalemate (draw) condition
	// -> we have to check if the next player has any legal moves left

	public boolean hasLegalMoves(boolean isWhite) {
		// try to scan all of player's pieces and all square to see if they have at
		// least one move that does not leave their King in check
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				Square startSquare = board[i][j];
				// start
				Piece piece = startSquare.getPiece();
				// check if the piece belongs to the current player
				if (piece != null && piece.isWhite() == isWhite) {
					// travrrrse through all position to check if there exists at least 1 posoiton
					// not lead to check state
					for (int r = 0; r < 8; ++r) {
						for (int c = 0; c < 8; ++c) {
							Square endSquare = board[r][c];
							// destination
							if (piece.isValidMove(this, startSquare, endSquare)) {
								// if you can move, move it and check if it in check or not
								if (willMoveResultInCheck(startSquare, endSquare, isWhite) == false) {
									// there is at least 1 valid position not lead to check state
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isCheckmate(boolean isWhite) {
		// check if the current player is in checkmate or not (lost)
		return isChecked(isWhite) && !hasLegalMoves(isWhite);
	}

	public boolean isStalemate(boolean isWhite) {
		// draw state
		return !isChecked(isWhite) && !hasLegalMoves(isWhite);
	}
}
