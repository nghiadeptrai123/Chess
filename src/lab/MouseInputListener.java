package lab;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * MouseInputListener
 *
 * Detects when the user clicks (press) on a square to pick up a piece,
 * drags it across the board, and releases (drop) on a destination square.
 *
 * It delegates all game logic and rendering to ChessBoardUI.
 */
public class MouseInputListener extends MouseAdapter {

    private final ChessBoardUI ui;

    public MouseInputListener(ChessBoardUI ui) {
        this.ui = ui;
    }

    // ---------------------------------------------------------------
    //  Mouse pressed — record the start square
    // ---------------------------------------------------------------
    @Override
    public void mousePressed(MouseEvent e) {
        int row = ui.pixelToRow(e.getY());
        int col = ui.pixelToCol(e.getX());

        if (ui.inBounds(row, col)) {
            ui.setDragFrom(row, col);   // highlight source square
        }
    }

    // ---------------------------------------------------------------
    //  Mouse dragged — repaint so the highlight follows the cursor
    // ---------------------------------------------------------------
    @Override
    public void mouseDragged(MouseEvent e) {
        ui.refresh();
    }

    // ---------------------------------------------------------------
    //  Mouse released — attempt the move
    // ---------------------------------------------------------------
    @Override
    public void mouseReleased(MouseEvent e) {
        int row = ui.pixelToRow(e.getY());
        int col = ui.pixelToCol(e.getX());

        // tryMove validates turn, legality, then snaps the piece
        ui.tryMove(row, col);
    }
}
