package lab;

public class GameController {
	boolean isWhiteTurn = true;
	boolean isCorrectTurn(Piece piece) {
	    return piece.isWhite() == isWhiteTurn;
	}
	void switchTurn() {
	    isWhiteTurn = !isWhiteTurn;
	}
	// pre-paring for single player option
	public boolean isSinglePlayer;
    public int botDepth;
	public boolean useQS; // Use Quiesence Search or not
	public GameController(boolean isSinglePlayer, int botDepth, boolean useQs) {
        this.isSinglePlayer = isSinglePlayer;
        this.botDepth = botDepth;
		this.useQS = useQs;
    }
}
