import os
import re

bot_files = [
    "src/lab/BeginnerBot.java",
    "src/lab/AmateurBot.java",
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
    # m.isPromotion = true;
    # }
    # }
    # moves.add(m);
    content = content.replace(
        "m.isPromotion = true;\n                                    }\n                                }",
        "m.isPromotion = true;\n                                    }\n                                    if (j != j_final && m.capturedPiece == null) {\n                                        m.isEnPassant = true;\n                                    }\n                                }"
    )

    # 2. Add enPassantTarget tracking to minimax and getBestMove (3 places per file)
    # Match:
    # boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag
    # int startPos = move.startRow * 8 + move.startCol;
    
    inject_ep_sim = """boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag
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
                }"""
    content = content.replace("boolean originalMovedFlag = movingPiece.isMoved(); // update -> store original isMoved flag", inject_ep_sim)
    
    # 3. Update En Passant Target before recursing
    # Match:
    #                 // Update king square if king moves
    # ...
    #                 }
    #
    #                 // recurse minimax (OR // Recurse OR // 3. Dig deeper)
    # We can just match the Revert king square block and add it there
    
    # Actually, we can use regex to find:
    # board.blackKingSquare = end;
    # }
    # 
    # // recurse
    
    # Let's just find "board.blackKingSquare = end;\n                }"
    target_update = """board.blackKingSquare = end;
                }

                if (movingPiece instanceof Pawn && Math.abs(move.startRow - move.endRow) == 2) {
                    board.enPassantTarget = board.getSquare((move.startRow + move.endRow) / 2, move.startCol);
                } else {
                    board.enPassantTarget = null;
                }"""
    content = content.replace("board.blackKingSquare = end;\n                }", target_update)
    
    # 4. Restore En Passant Target during undo
    # Match:
    # movingPiece.setMoved(originalMovedFlag);
    
    undo_restore_target = """board.enPassantTarget = prevEnPassantTarget;
                movingPiece.setMoved(originalMovedFlag);"""
    content = content.replace("movingPiece.setMoved(originalMovedFlag);", undo_restore_target)
    
    # 5. Restore Captured Pawn during undo
    # Match:
    # board.addActivePiece(startPos);
    
    undo_restore_pawn = """board.addActivePiece(startPos);

                if (move.isEnPassant) {
                    epPawnSquare.setPiece(epCapturedPiece);
                    board.addActivePiece(epCapturedPos);
                }"""
    content = content.replace("board.addActivePiece(startPos);", undo_restore_pawn)
    
    with open(file, "w") as f:
        f.write(content)
    print(f"Updated {file}")
