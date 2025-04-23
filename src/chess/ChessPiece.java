package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import lombok.Getter;

import java.util.Objects;

@Getter
public abstract class ChessPiece extends Piece {

    private final Color color;
    private int moveCount;

    public ChessPiece(Board board, Color color) {
        super(board);
        this.color = color;
    }

    public void increaseMoveCount() {
        moveCount++;
    }

    public void decreaseMoveCount() {
        moveCount--;
    }

    public ChessPosition getChessPosition() {
        return ChessPosition.fromPosition(position);
    }

    protected boolean isThereOpponentPiece(Position position) {
        final var p = (ChessPiece) getBoard().piece(position);
        return Objects.nonNull(p) && !Objects.equals(p.getColor(), color);
    }

}
