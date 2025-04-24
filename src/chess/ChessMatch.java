package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ChessMatch {

    private Board board;
    private int turn;
    private Color currentPlayer;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public ChessPiece[][] getPieces() {
        final var mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (var i = 0; i < board.getRows(); i++) {
            for (var j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(final ChessPosition sourcePosition) {
        final var position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(final ChessPosition sourcePosition, final ChessPosition targetPosition) {
        final var source = sourcePosition.toPosition();
        final var target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        final var capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("Você não pode se colocar em xeque");
        }

        final var movedPiece = (ChessPiece) board.piece(target);

        // # movimento especial Promotion
        promoted = null;

        if (movedPiece instanceof Pawn) {
            if (movedPiece.getColor() == Color.WHITE && target.getRow() == 0
                    || movedPiece.getColor() == Color.BLACK && target.getRow() == 7) {
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        // movimento especial en passant
        if (movedPiece instanceof Pawn
                && (target.getRow() == source.getRow() - 2 || (target.getRow() == source.getRow() + 2))) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }
        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replacePromotedPiece(final String type) {
        if (Objects.isNull(promoted)) {
            throw new IllegalStateException("Não há nenhuma peça para ser promovida");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            return promoted;
        }

        final var pos = promoted.getChessPosition().toPosition();
        final var p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        final var newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);

        return newPiece;
    }

    private ChessPiece newPiece(final String type, final Color color) {
        return switch (type) {
            case "B" -> new Bishop(board, color);
            case "N" -> new Knight(board, color);
            case "Q" -> new Queen(board, color);
            default -> new Rook(board, color);
        };
    }

    private Piece makeMove(final Position source, final Position target) {
        final var p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        var capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (Objects.nonNull(capturedPiece)) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // movimento especial castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            final var sourceT = new Position(source.getRow(), source.getColumn() + 3);
            final var targetT = new Position(source.getRow(), source.getColumn() + 1);
            final var rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // #movimento especial castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            final var sourceT = new Position(source.getRow(), source.getColumn() - 4);
            final var targetT = new Position(source.getRow(), source.getColumn() - 1);
            final var rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // #movimento especial en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(final Position source, final Position target, final Piece capturedPiece) {
        final var p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (Objects.nonNull(capturedPiece)) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // #movimento especial castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            final var sourceT = new Position(source.getRow(), source.getColumn() + 3);
            final var targetT = new Position(source.getRow(), source.getColumn() + 1);
            final var rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        // #movimento especial castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            final var sourceT = new Position(source.getRow(), source.getColumn() - 4);
            final var targetT = new Position(source.getRow(), source.getColumn() - 1);
            final var rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // #movimento especial en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                final var pawn = (ChessPiece) board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColumn());
                } else {
                    pawnPosition = new Position(4, target.getColumn());
                }

                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(final Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("Não há nenhuma peça na posição de origem");
        }
        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
            throw new ChessException("A peça escolhida não é sua");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("Não há movimentos possíveis para a peça escolhida");
        }
    }

    private void validateTargetPosition(final Position source, final Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("A peça escolhida não pode se mover para a posição alvo");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(final Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(final Color color) {
        final var list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (final var p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("Não há rei %s no tabuleiro".formatted(color));
    }

    private boolean testCheck(final Color color) {
        final var kingPosition = king(color).getChessPosition().toPosition();
        final var opponentPieces = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece) x).getColor() == opponent(color))
                .toList();

        for (final var p : opponentPieces) {
            final var mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(final Color color) {
        if (!testCheck(color)) {
            return false;
        }
        final var list = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece) x).getColor() == color)
                .toList();

        for (final var p : list) {
            final var mat = p.possibleMoves();
            for (var i = 0; i < board.getRows(); i++) {
                for (var j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        final var source = ((ChessPiece) p).getChessPosition().toPosition();
                        final var target = new Position(i, j);
                        final var capturedPiece = makeMove(source, target);
                        final var testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void placeNewPiece(final char column, final int row, final ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}
