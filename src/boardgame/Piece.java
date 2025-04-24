package boardgame;


public abstract class Piece {

    protected Position position;
    private final Board board;

    public Piece(final Board board) {
        this.board = board;
        position = null;
    }

    protected Board getBoard() {
        return board;
    }

    public abstract boolean[][] possibleMoves();

    public boolean possibleMove(final Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];
    }

    public boolean isThereAnyPossibleMove() {
        final var mat = possibleMoves();
        for (final var booleans : mat) {
            for (var j = 0; j < mat.length; j++) {
                if (booleans[j]) {
                    return true;
                }
            }
        }
        return false;
    }
}
