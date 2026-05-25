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
		// identify if that piece have at least 1 move or not
	}
	public void setMoved(boolean moved){
		this.isMoved = moved; // setMoved after a succesfully moved.
	}
	public abstract boolean isValidMove(Board board, Square start, Square end);
	
	public abstract Piece clonePiece();
}
