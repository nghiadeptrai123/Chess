package lab;

import java.util.HashSet;

public class Board {
	private Square[][] board = new Square[8][8];
	
	public int[] activePieceCoords = new int[33];
	public int[] boardToIndex = new int[64];
	public int activePieceCount = 0;

	public void addActivePiece(int coord) {
		activePieceCoords[activePieceCount] = coord;
		boardToIndex[coord] = activePieceCount;
		activePieceCount++;
	}

	public void removeActivePiece(int coord) {
		int index = boardToIndex[coord];
		int lastCoord = activePieceCoords[activePieceCount - 1];

		activePieceCoords[index] = lastCoord;
		boardToIndex[lastCoord] = index;
		activePieceCount--;
	}

	Board() {
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				boolean color = ((i+j) % 2 == 0) ? false : true;
				board[i][j] = new Square(color,i,j);
			}
		}
	}
	public Square getSquare(int row, int col) {
		return board[row][col];
	}
	public Square findKing(boolean isWhite){
		// tim vi tri cua vua (dung de lam tinh nang checkmate)
		for (int k = 0; k < activePieceCount; k++){
			int pos = activePieceCoords[k];
			int r = pos / 8;
			int c = pos % 8;
			Square square = board[r][c];
			Piece piece = square.getPiece();
			if (piece instanceof King && piece.isWhite() == isWhite){
				// tim thay vua den hoac trang
				return square;
			}
		}
		return null;
		// k tim thay vua
	}

	// check detection
	public boolean isChecked(boolean isWhite){
			Square kingSquare = findKing(isWhite);
			if (kingSquare == null){
				return false;
				// no king
			}
			for (int k = 0; k < activePieceCount; k++){
				int pos = activePieceCoords[k];
				int r = pos / 8;
				int c = pos % 8;
				Square square = board[r][c];
				Piece piece= square.getPiece();
				if (piece != null && piece.isWhite()!=isWhite){
					// check if that piece is an enemy piece or not 
					if (piece.isValidMove(this, square, kingSquare)){
						// con vua dang bi chieu tuong
						return true;
					}
				}
			}
			return false; // k phai checkmate
	}


/*
if you are already in check, your next move must block the attack, capture the attacking piece, or move the King to safety.
if a piece is currently protecting the king from checkmate position, you are not allow to move it
-> this functions as a temporarily simulation to see if that move satisfies the above conditions
*/

public boolean willMoveResultInCheck(Square start, Square end, boolean isWhite){
	Piece movingPiece = start.getPiece();
	Piece capturedPiece = end.getPiece();
	
	int startPos = start.getRow() * 8 + start.getCol();
	int endPos = end.getRow() * 8 + end.getCol();
	
	// temporary simulate the state ( assuming it is a valid move)
	end.setPiece(movingPiece);
	start.setPiece(null);
	
	removeActivePiece(startPos);
	if (capturedPiece == null) {
		addActivePiece(endPos);
	}
	
	boolean flag = isChecked(isWhite);
	
	start.setPiece(movingPiece);
	end.setPiece(capturedPiece);
	
	if (capturedPiece == null) {
		removeActivePiece(endPos);
	}
	addActivePiece(startPos);

	return flag;
}

// checkmate (win) stalemate (draw) condition
// -> 	we have to check if the next player has any legal moves left

	public boolean hasLegalMoves(boolean isWhite){
		// try to scan all of player's pieces and all square to see if they have at least one move that does not leave their King in check
		int[] pieces = new int[activePieceCount];
		System.arraycopy(activePieceCoords, 0, pieces, 0, activePieceCount);
		for (int k = 0; k < pieces.length; k++){
			int pos = pieces[k];
			int r = pos / 8;
			int c = pos % 8;
			Square startSquare = board[r][c];
			// start
			Piece piece = startSquare.getPiece();
			// check if the piece belongs to the current player
			if (piece != null && piece.isWhite()==isWhite){
				// travrrrse through all position to check if there exists at least 1 posoiton not lead to check state
				for (int end_r = 0 ; end_r < 8 ;++end_r){
					for (int end_c = 0 ; end_c < 8 ;++end_c){
						Square endSquare = board[end_r][end_c];
						// destination
						if (piece.isValidMove(this, startSquare, endSquare)){
							// if you can move, move it and check if it in check or not
							if (willMoveResultInCheck(startSquare, endSquare, isWhite)==false){
								// there is at least 1 valid position not lead to check state
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

public boolean isCheckmate(boolean isWhite){
	// check if the current player is in checkmate or not (lost)
	return isChecked(isWhite) && !hasLegalMoves(isWhite);
}
public boolean isStalemate(boolean isWhite){
	// draw state
	return !isChecked(isWhite) && !hasLegalMoves(isWhite);
}
}




