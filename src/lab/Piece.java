package lab;

public abstract class Piece {
	private boolean isWhite;
	private boolean isMoved = false;
	public Piece(boolean isWhite){
		this.isWhite = isWhite;
	}
	public boolean isWhite() {
		return isWhite;
	}
	public boolean isMoved() {
		return isMoved;
	}
	public abstract boolean isValidMove(Board board, Square start, Square end);
}
