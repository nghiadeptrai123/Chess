package lab;

public abstract class Piece {
	private boolean isWhite;
	private boolean isMoved = false;
	private int row = -1;
	private int col = -1;

	public Piece(boolean isWhite) {
		this.isWhite = isWhite;
	}

	public boolean isWhite() {
		return isWhite;
	}

	public boolean isMoved() {
		return isMoved;
		// identify if that piece have at least 1 move or not
	}

	public void setMoved(boolean moved) {
		this.isMoved = moved; // setMoved after a succesfully moved.
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public abstract boolean isValidMove(Board board, Square start, Square end);
}
