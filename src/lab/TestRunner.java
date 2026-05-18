package lab;

public class TestRunner {
    public static void printBoardState(Board board) {
        System.out.println("Set Pieces");

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getSquare(row, col).getPiece();

                if (piece == null) {
                    System.out.print("[  ] ");
                } else {
                    String color = piece.isWhite() ? "w" : "b";
                    String type = piece.getClass().getSimpleName().substring(0, 1);
                    System.out.print("[" + color + type + "] ");
                }
            }
            System.out.println();
        }
    }

    public static void testMove(Board board, int startRow, int startCol, int endRow, int endCol) {
        Square start = board.getSquare(startRow, startCol);
        Square end = board.getSquare(endRow, endCol);
        Piece piece = start.getPiece();

        if (piece == null) {
            System.out.println("No piece at (" + startRow + "," + startCol + ")");
            return;
        }

        boolean result = piece.isValidMove(board, start, end);

        System.out.println(
            piece.getClass().getSimpleName()
            + " from (" + startRow + "," + startCol + ") to ("
            + endRow + "," + endCol + "): "
            + result
        );
    }

    public static void main(String[] args) {
        Board board = new Board();

     // Setting Rooks
        board.getSquare(0,0).setPiece(new Rook(true));
        board.getSquare(0,7).setPiece(new Rook(true));
        board.getSquare(7,0).setPiece(new Rook(false));
        board.getSquare(7,7).setPiece(new Rook(false));
        
        // Setting Knights
        board.getSquare(0,1).setPiece(new Knight(true));
        board.getSquare(0,6).setPiece(new Knight(true));
        board.getSquare(7,1).setPiece(new Knight(false));
        board.getSquare(7,6).setPiece(new Knight(false));
        
        // Setting Bishops
        board.getSquare(0,2).setPiece(new Bishop(true));
        board.getSquare(0,5).setPiece(new Bishop(true));
        board.getSquare(7,2).setPiece(new Bishop(false));
        board.getSquare(7,5).setPiece(new Bishop(false));
        
        // Setting Queens
        board.getSquare(0,3).setPiece(new Queen(true));
        board.getSquare(7,3).setPiece(new Queen(false));
        
        // Setting Kings
        board.getSquare(0,4).setPiece(new King(true));
        board.getSquare(7,4).setPiece(new King(false));
        
        // Setting Pawns
        for (int col = 0; col < 8; col++) {
            board.getSquare(1, col).setPiece(new Pawn(true));  // White Pawns on row 1
            board.getSquare(6, col).setPiece(new Pawn(false)); // Black Pawns on row 6
        }

        printBoardState(board);

        System.out.println("\nMove Tests");
        

        testMove(board, 1, 0, 2, 0); 
        testMove(board, 1, 0, 0, 0); 
        testMove(board, 0, 0, 3, 0); 
        testMove(board, 0, 1, 2, 2); 
        testMove(board, 0, 2, 3, 5); 
        testMove(board, 7, 7, 7, 4); 
    }

}