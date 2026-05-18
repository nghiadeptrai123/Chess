package lab;

public class GameController {
	boolean isWhiteTurn = true;
	boolean isCorrectTurn(Piece piece) {
	    return piece.isWhite() == isWhiteTurn;
	}
	void switchTurn() {
	    isWhiteTurn = !isWhiteTurn;
	}
}
