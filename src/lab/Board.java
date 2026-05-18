package lab;

public class Board {
	private Square[][] board = new Square[8][8];
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
}
