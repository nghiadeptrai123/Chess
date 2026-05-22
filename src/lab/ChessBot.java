package lab;

/**
 * The ChessBot interface defines the standard blueprint for all AI opponents.
 * By using an interface, the main game (ChessBoardUI) doesn't need to know 
 * which difficulty is currently active. It simply asks the active bot 
 * to calculate and return its best move.
 */
public interface ChessBot {
    Move getBestMove(Board board, boolean isWhite);
}
