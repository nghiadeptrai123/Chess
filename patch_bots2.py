import os
import re

bot_files = [
    "src/lab/IntermediateBot.java",
    "src/lab/HardBot.java"
]

for file in bot_files:
    if not os.path.exists(file):
        continue
    with open(file, "r") as f:
        content = f.read()

    # 1. getAllLegalMoves - Add isEnPassant flag
    # Match:
    #                                     if (i_final == promotionRow) {
    #                                         m.isPromotion = true;
    #                                     }
    #                                 }
    #                                 moves.add(m);
    
    content = re.sub(
        r'(if\s*\(i_final\s*==\s*promotionRow\)\s*\{\s*m\.isPromotion\s*=\s*true;\s*\}\s*\})',
        r'\1\n                                    if (j != j_final && m.capturedPiece == null) {\n                                        m.isEnPassant = true;\n                                    }',
        content
    )

    # 2. Add enPassantTarget tracking to simulations
    # Match:
    # boolean originalMovedFlag = movingPiece.isMoved();
    # int startPos = move.startRow * 8 + move.startCol;
    
    inject_ep_sim = """boolean originalMovedFlag = movingPiece.isMoved();
                Square prevEnPassantTarget = board.enPassantTarget;
                
                Square epPawnSquare = null;
                Piece epCapturedPiece = null;
                int epCapturedPos = -1;
                if (move.isEnPassant) {
                    epPawnSquare = board.getSquare(move.startRow, move.endCol);
                    epCapturedPiece = epPawnSquare.getPiece();
                    epCapturedPos = epPawnSquare.getRow() * 8 + epPawnSquare.getCol();
                    epPawnSquare.setPiece(null);
                    board.removeActivePiece(epCapturedPos);
                }
                int startPos"""
    content = re.sub(r'boolean\s+originalMovedFlag\s*=\s*movingPiece\.isMoved\(\);\s*int\s+startPos', inject_ep_sim, content)
    
    # 3. Update En Passant Target before recursing
    # Match:
    #                 if (movingPiece instanceof King) {
    #                     if (movingPiece.isWhite()) board.whiteKingSquare = end;
    #                     else                       board.blackKingSquare = end;
    #                 }
    
    target_update = """if (movingPiece instanceof King) {
                    if (movingPiece.isWhite()) board.whiteKingSquare = end;
                    else                       board.blackKingSquare = end;
                }

                if (movingPiece instanceof Pawn && Math.abs(move.startRow - move.endRow) == 2) {
                    board.enPassantTarget = board.getSquare((move.startRow + move.endRow) / 2, move.startCol);
                } else {
                    board.enPassantTarget = null;
                }"""
    content = re.sub(r'if\s*\(movingPiece\s+instanceof\s+King\)\s*\{\s*if\s*\(movingPiece\.isWhite\(\)\)\s*board\.whiteKingSquare\s*=\s*end;\s*else\s*board\.blackKingSquare\s*=\s*end;\s*\}', target_update, content)
    
    # 4. Restore En Passant Target during undo
    # Match:
    #                 movingPiece.setMoved(originalMovedFlag);
    
    undo_restore_target = """board.enPassantTarget = prevEnPassantTarget;
                movingPiece.setMoved(originalMovedFlag);"""
    content = re.sub(r'movingPiece\.setMoved\(originalMovedFlag\);', undo_restore_target, content)
    
    # 5. Restore Captured Pawn during undo
    # Match:
    #                 if (move.capturedPiece == null) board.removeActivePiece(endPos);
    #                 board.addActivePiece(startPos);
    
    undo_restore_pawn = """if (move.capturedPiece == null && !move.isEnPassant) board.removeActivePiece(endPos);
                board.addActivePiece(startPos);

                if (move.isEnPassant) {
                    epPawnSquare.setPiece(epCapturedPiece);
                    board.addActivePiece(epCapturedPos);
                }"""
    content = re.sub(r'if\s*\(move\.capturedPiece\s*==\s*null\)\s*board\.removeActivePiece\(endPos\);\s*board\.addActivePiece\(startPos\);', undo_restore_pawn, content)

    # 6. For step 2, there was an issue where I missed the `if (move.capturedPiece == null) board.addActivePiece(endPos);`
    # We must change it to `if (move.capturedPiece == null && !move.isEnPassant) board.addActivePiece(endPos);`
    content = re.sub(r'if\s*\(move\.capturedPiece\s*==\s*null\)\s*board\.addActivePiece\(endPos\);', r'if (move.capturedPiece == null && !move.isEnPassant) board.addActivePiece(endPos);', content)
    
    with open(file, "w") as f:
        f.write(content)
    print(f"Updated {file}")
