package boardgame;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Board {

    private int rows;
    private int columns;
    private Piece[][] pieces;

    public Board(final int rows, final int columns) {
        if (rows < 1 || columns < 1) {
            throw new BoardException("Erro ao criar quadro: deve haver pelo menos 1 linha e 1 coluna");
        }

        this.rows = rows;
        this.columns = columns;
        pieces = new Piece[rows][columns];
    }

    public Piece piece(final int row, final int column) {
        if (!positionExists(row, column)) {
            throw new BoardException("Posição fora do quadro");
        }
        return pieces[row][column];
    }

    public Piece piece(final Position position) {
        if (!positionExists(position)) {
            throw new BoardException("Posição fora do quadro");
        }
        return pieces[position.getRow()][position.getColumn()];
    }

    public void placePiece(final Piece piece, final Position position) {
        if (thereIsAPiece(position)) {
            throw new BoardException("There is already a piece on position " + position);
        }
        pieces[position.getRow()][position.getColumn()] = piece;
        piece.position = position;
    }

    public Piece removePiece(final Position position) {
        if (!positionExists(position)) {
            throw new BoardException("Posição fora do quadro");
        }

        if (Objects.isNull(piece(position))) {
            return null;
        }

        final var aux = piece(position);
        aux.position = null;
        pieces[position.getRow()][position.getColumn()] = null;
        return aux;
    }

    private boolean positionExists(final int row, final int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    public boolean positionExists(final Position position) {
        return positionExists(position.getRow(), position.getColumn());
    }

    public boolean thereIsAPiece(final Position position) {
        if (!positionExists(position)) {
            throw new BoardException("Posição fora do quadro");
        }
        return Objects.nonNull(piece(position));
    }
}
