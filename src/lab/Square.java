package lab;

public class Square {
	private final boolean isWhite;
	private final int row;
	private final int col;
	private Piece piece = null;
	public Square(boolean isWhite, int row, int col){
		this.isWhite = isWhite;
		this.row = row;
		this.col = col;
	}
	public Square(boolean isWhite, int col, int row, Piece piece){
		this.isWhite = isWhite;
		this.row = row;
		this.col = col;
		this.piece = piece;
	}
	public boolean isWhite() {
        return isWhite;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public Piece getPiece() {
        return piece;
    }
    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}
