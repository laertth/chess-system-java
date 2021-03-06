package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Rook;

public class ChessMatch {

	private Board board;
	private Color currentPlayer;
	private int turn;
	private boolean check;
	private boolean checkMate;

	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.YELLOW;
		initialSetup();
	}

	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();

	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, Color.YELLOW));
        placeNewPiece('b', 1, new Knight(board, Color.YELLOW));
        placeNewPiece('c', 1, new Bishop(board, Color.YELLOW));
        placeNewPiece('e', 1, new King(board, Color.YELLOW));
        placeNewPiece('f', 1, new Bishop(board, Color.YELLOW));
        placeNewPiece('g', 1, new Knight(board, Color.YELLOW));
        placeNewPiece('h', 1, new Rook(board, Color.YELLOW));
        placeNewPiece('a', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('b', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('c', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('d', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('e', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('f', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('g', 2, new Pawn(board, Color.YELLOW));
        placeNewPiece('h', 2, new Pawn(board, Color.YELLOW));

        placeNewPiece('a', 8, new Rook(board, Color.BLUE));
        placeNewPiece('b', 8, new Knight(board, Color.BLUE));
        placeNewPiece('c', 8, new Bishop(board, Color.BLUE));
        placeNewPiece('e', 8, new King(board, Color.BLUE));
        placeNewPiece('f', 8, new Bishop(board, Color.BLUE));
        placeNewPiece('g', 8, new Knight(board, Color.BLUE));
        placeNewPiece('h', 8, new Rook(board, Color.BLUE));
        placeNewPiece('a', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('b', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('c', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('d', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('e', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('f', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('g', 7, new Pawn(board, Color.BLUE));
        placeNewPiece('h', 7, new Pawn(board, Color.BLUE));
	}

	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target);

		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			throw new ChessException("You can�t put yourself in check");
		}

		check = (testCheck(opponent(currentPlayer))) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		} else {
			nextTourn();
		}

		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece) board.removePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p, target);

		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		return capturedPiece;
	}

	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece) board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, source);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	}

	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position");
		}
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece");
		}
		if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours");
		}
	}

	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("There chosen piece can�t move to target position");
		}
	}

	private void nextTourn() {
		turn++;
		currentPlayer = (currentPlayer == Color.YELLOW) ? Color.BLUE : Color.YELLOW;
	}

	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());
		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		}
		throw new IllegalStateException("There is no " + color + "king on the board");
	}

	private Color opponent(Color color) {
		return (color == Color.BLUE) ? Color.YELLOW : (Color) Color.BLUE;
	}

	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getchessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}

	private boolean testCheckMate(Color color) {
		if (!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());
		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves();
			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					if (mat[i][j]) {
						Position source = ((ChessPiece) p).getchessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testCheck(color);
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
}
