package chess;

import boardgame.Position;
import lombok.Getter;

@Getter
public class ChessPosition {

    private final char column;
    private final int row;

    public ChessPosition(final char column, final int row) {
        if (column < 'a' || column > 'h' || row < 1 || row > 8) {
            throw new ChessException("Erro ao instanciar ChessPosition. Os valores válidos são de a1 a h8.");
        }
        this.column = column;
        this.row = row;
    }

    protected Position toPosition() {
        return new Position(8 - row, column - 'a');
    }

    protected static ChessPosition fromPosition(Position position) {
        return new ChessPosition((char) ('a' + position.getColumn()), 8 - position.getRow());
    }

    @Override
    public String toString() {
        return "%s%s".formatted(column, row);
    }
}
