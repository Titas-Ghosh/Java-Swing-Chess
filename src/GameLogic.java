//GameLogic.java


import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Contains all backend game logic: the Board state, Piece definitions,
 * Position helper, and Player enum. No Swing/UI code here.
 */
public class GameLogic {

    /**
     * Represents a coordinate on the board.
     */
    public static class Position {
        int row;
        int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        public boolean isValid() {
            return row >= 0 && row < 8 && col >= 0 && col < 8;
        }
        
        // Helper to convert "e2" to new Position(6, 4)
        public static Position fromString(String s) {
            if (s == null || s.length() != 2) return null;
            char colChar = s.charAt(0);
            char rowChar = s.charAt(1);
            if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
                return null;
            }
            int col = colChar - 'a';
            int row = 8 - (rowChar - '0');
            return new Position(row, col);
        }
        
        @Override
        public String toString() {
            if (!isValid()) return "";
            char colChar = (char) ('a' + col);
            char rowChar = (char) ('8' - row);
            return "" + colChar + rowChar;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Position position = (Position) obj;
            return row == position.row && col == position.col;
        }
    }

    /**
     * Represents the two players.
     */
    public enum Player {
        WHITE, BLACK
    }

    /**
     * Represents the game board and its state.
     */
    public static class Board {
        private Piece[][] board;
        private Player currentPlayer;
        private boolean isGameOver;
        private String statusMessage;
        
        private boolean whiteKingSideCastle;
        private boolean whiteQueenSideCastle;
        private boolean blackKingSideCastle;
        private boolean blackQueenSideCastle;
        private Position enPassantTarget; 
        private Position whiteKingPos;
        private Position blackKingPos;

        public Board() {
            setupNewGame();
        }

        public void setupNewGame() {
            board = new Piece[8][8];
            currentPlayer = Player.WHITE;
            isGameOver = false;
            statusMessage = "White's turn.";

            whiteKingSideCastle = true;
            whiteQueenSideCastle = true;
            blackKingSideCastle = true;
            blackQueenSideCastle = true;
            enPassantTarget = null;

            for (int c = 0; c < 8; c++) {
                board[1][c] = new Pawn(Player.BLACK);
                board[6][c] = new Pawn(Player.WHITE);
            }
            board[0][0] = new Rook(Player.BLACK);
            board[0][7] = new Rook(Player.BLACK);
            board[7][0] = new Rook(Player.WHITE);
            board[7][7] = new Rook(Player.WHITE);
            board[0][1] = new Knight(Player.BLACK);
            board[0][6] = new Knight(Player.BLACK);
            board[7][1] = new Knight(Player.WHITE);
            board[7][6] = new Knight(Player.WHITE);
            board[0][2] = new Bishop(Player.BLACK);
            board[0][5] = new Bishop(Player.BLACK);
            board[7][2] = new Bishop(Player.WHITE);
            board[7][5] = new Bishop(Player.WHITE);
            board[0][3] = new Queen(Player.BLACK);
            board[7][3] = new Queen(Player.WHITE);
            board[0][4] = new King(Player.BLACK);
            board[7][4] = new King(Player.WHITE);
            
            whiteKingPos = new Position(7, 4);
            blackKingPos = new Position(0, 4);
        }

        /**
         * Gets all valid moves for a piece at a given position.
         */
        public List<Position> getValidMovesForPiece(Position fromPos) {
            Piece piece = getPieceAt(fromPos);
            if (piece == null || piece.getPlayer() != currentPlayer) {
                return new ArrayList<>();
            }
            return piece.getValidMoves(fromPos, this);
        }

        /**
         * Attempts to make a move. Assumes the move is valid.
         */
        public boolean makeMove(Position fromPos, Position toPos) {
            if (isGameOver) return false;

            Piece pieceToMove = getPieceAt(fromPos);
            Piece capturedPiece = getPieceAt(toPos);
            
            // Apply the move
            setPieceAt(toPos, pieceToMove);
            setPieceAt(fromPos, null);

            // Handle En Passant Capture
            if (pieceToMove instanceof Pawn && toPos.equals(enPassantTarget)) {
                int capturedPawnRow = (currentPlayer == Player.WHITE) ? toPos.row + 1 : toPos.row - 1;
                setPieceAt(new Position(capturedPawnRow, toPos.col), null);
            }

            // Set new En Passant Target
            enPassantTarget = null;
            if (pieceToMove instanceof Pawn && Math.abs(fromPos.row - toPos.row) == 2) {
                enPassantTarget = new Position((fromPos.row + toPos.row) / 2, fromPos.col);
            }
            
            // Handle Castling
            if (pieceToMove instanceof King && Math.abs(fromPos.col - toPos.col) == 2) {
                if (toPos.col == 6) { // King-side
                    Piece rook = getPieceAt(new Position(fromPos.row, 7));
                    setPieceAt(new Position(fromPos.row, 5), rook);
                    setPieceAt(new Position(fromPos.row, 7), null);
                } else { // Queen-side
                    Piece rook = getPieceAt(new Position(fromPos.row, 0));
                    setPieceAt(new Position(fromPos.row, 3), rook);
                    setPieceAt(new Position(fromPos.row, 0), null);
                }
            }
            
            // Update King Position
            if (pieceToMove instanceof King) {
                if (currentPlayer == Player.WHITE) whiteKingPos = toPos;
                else blackKingPos = toPos;
            }
            
            // Update Castling Rights
            if (pieceToMove instanceof King) {
                if (currentPlayer == Player.WHITE) {
                    whiteKingSideCastle = false;
                    whiteQueenSideCastle = false;
                } else {
                    blackKingSideCastle = false;
                    blackQueenSideCastle = false;
                }
            }
            if (pieceToMove instanceof Rook) {
                if (currentPlayer == Player.WHITE) {
                    if (fromPos.equals(new Position(7, 0))) whiteQueenSideCastle = false;
                    if (fromPos.equals(new Position(7, 7))) whiteKingSideCastle = false;
                } else {
                    if (fromPos.equals(new Position(0, 0))) blackQueenSideCastle = false;
                    if (fromPos.equals(new Position(0, 7))) blackKingSideCastle = false;
                }
            }
            
            // Handle Pawn Promotion
            if (pieceToMove instanceof Pawn) {
                if ((currentPlayer == Player.WHITE && toPos.row == 0) || (currentPlayer == Player.BLACK && toPos.row == 7)) {
                    promotePawn(toPos);
                }
            }

            // Switch player
            currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;

            // Check for Checkmate / Stalemate
            if (isCheckmate(currentPlayer)) {
                statusMessage = "Checkmate! " + (currentPlayer == Player.WHITE ? "Black" : "White") + " wins!";
                isGameOver = true;
            } else if (isStalemate(currentPlayer)) {
                statusMessage = "Stalemate! It's a draw.";
                isGameOver = true;
            } else if (isKingInCheck(currentPlayer)) {
                statusMessage = currentPlayer + " is in check!";
            } else {
                statusMessage = currentPlayer + "'s turn.";
            }

            return true;
        }

        public boolean isKingInCheck(Player player) {
            Position kingPos = (player == Player.WHITE) ? whiteKingPos : blackKingPos;
            Player opponent = (player == Player.WHITE) ? Player.BLACK : Player.WHITE;
            return isSquareAttackedBy(kingPos, opponent);
        }

        public boolean isSquareAttackedBy(Position pos, Player attackerPlayer) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece attacker = getPieceAt(new Position(r, c));
                    if (attacker != null && attacker.getPlayer() == attackerPlayer) {
                        List<Position> moves = attacker.getRawMoves(new Position(r, c), this);
                        for (Position move : moves) {
                            if (move.equals(pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        public boolean hasLegalMoves(Player player) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece p = getPieceAt(new Position(r, c));
                    if (p != null && p.getPlayer() == player) {
                        List<Position> validMoves = p.getValidMoves(new Position(r, c), this);
                        if (!validMoves.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean isCheckmate(Player player) {
            return isKingInCheck(player) && !hasLegalMoves(player);
        }

        public boolean isStalemate(Player player) {
            return !isKingInCheck(player) && !hasLegalMoves(player);
        }
        
        private void promotePawn(Position pos) {
            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            int choice = JOptionPane.showOptionDialog(null, 
                "Promote pawn to:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
            
            Player player = (pos.row == 0) ? Player.WHITE : Player.BLACK;
            switch (choice) {
                case 0: default: setPieceAt(pos, new Queen(player)); break;
                case 1: setPieceAt(pos, new Rook(player)); break;
                case 2: setPieceAt(pos, new Bishop(player)); break;
                case 3: setPieceAt(pos, new Knight(player)); break;
            }
        }
        
        // --- Getters and Setters ---
        
        public Piece getPieceAt(Position pos) {
            if (!pos.isValid()) return null;
            return board[pos.row][pos.col];
        }

        public void setPieceAt(Position pos, Piece piece) {
            if (!pos.isValid()) return;
            board[pos.row][pos.col] = piece;
        }

        public boolean getKingSideCastle(Player player) {
            return (player == Player.WHITE) ? whiteKingSideCastle : blackKingSideCastle;
        }
        
        public boolean getQueenSideCastle(Player player) {
            return (player == Player.WHITE) ? whiteQueenSideCastle : blackQueenSideCastle;
        }
        
        public Position getKingPos(Player player) {
            return (player == Player.WHITE) ? whiteKingPos : blackKingPos;
        }
        
        public Position getEnPassantTarget() { return enPassantTarget; }
        public boolean isGameOver() { return isGameOver; }
        public String getStatusMessage() { return statusMessage; }
        public Player getCurrentPlayer() { return currentPlayer; }
        
        // --- Database Serialization/Deserialization ---
        
        /**
         * Serializes the board state to a FEN-like string.
         * e.g., "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
         */
        public String getBoardStateString() {
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < 8; r++) {
                int emptyCount = 0;
                for (int c = 0; c < 8; c++) {
                    Piece p = board[r][c];
                    if (p == null) {
                        emptyCount++;
                    } else {
                        if (emptyCount > 0) {
                            sb.append(emptyCount);
                            emptyCount = 0;
                        }
                        sb.append(p.getSymbol());
                    }
                }
                if (emptyCount > 0) {
                    sb.append(emptyCount);
                }
                if (r < 7) {
                    sb.append("/");
                }
            }
            return sb.toString();
        }

        /**
         * Loads the board state from a FEN-like string.
         */
        public void loadBoardStateString(String fen) {
            board = new Piece[8][8]; // Clear board
            int r = 0, c = 0;
            for (char ch : fen.toCharArray()) {
                if (ch == '/') {
                    r++;
                    c = 0;
                } else if (Character.isDigit(ch)) {
                    c += Character.getNumericValue(ch);
                } else {
                    Player player = Character.isUpperCase(ch) ? Player.WHITE : Player.BLACK;
                    switch (Character.toLowerCase(ch)) {
                        case 'p': board[r][c] = new Pawn(player); break;
                        case 'r': board[r][c] = new Rook(player); break;
                        case 'n': board[r][c] = new Knight(player); break;
                        case 'b': board[r][c] = new Bishop(player); break;
                        case 'q': board[r][c] = new Queen(player); break;
                        case 'k': 
                            board[r][c] = new King(player); 
                            if(player == Player.WHITE) whiteKingPos = new Position(r,c);
                            else blackKingPos = new Position(r,c);
                            break;
                    }
                    c++;
                }
            }
        }

        public String getCastlingRightsString() {
            StringBuilder sb = new StringBuilder();
            if (whiteKingSideCastle) sb.append("K");
            if (whiteQueenSideCastle) sb.append("Q");
            if (blackKingSideCastle) sb.append("k");
            if (blackQueenSideCastle) sb.append("q");
            return sb.length() == 0 ? "-" : sb.toString();
        }

        public void loadCastlingRightsString(String s) {
            whiteKingSideCastle = s.contains("K");
            whiteQueenSideCastle = s.contains("Q");
            blackKingSideCastle = s.contains("k");
            blackQueenSideCastle = s.contains("q");
        }
        
        // --- Setters for Loading Game ---
        public void setCurrentPlayer(Player p) { this.currentPlayer = p; }
        public void setEnPassantTarget(Position p) { this.enPassantTarget = p; }
    }


    /**
     * Abstract base class for all chess pieces.
     */
    public static abstract class Piece {
        protected Player player;

        public Piece(Player player) {
            this.player = player;
        }
        public Player getPlayer() { return player; }
        public abstract char getSymbol();
        public abstract List<Position> getRawMoves(Position from, Board board);

        public List<Position> getValidMoves(Position from, Board board) {
            List<Position> validMoves = new ArrayList<>();
            List<Position> rawMoves = getRawMoves(from, board);
            
            for (Position to : rawMoves) {
                Piece capturedPiece = board.getPieceAt(to);
                board.setPieceAt(to, this);
                board.setPieceAt(from, null);
                
                Position oldKingPos = null;
                if (this instanceof King) {
                    if (player == Player.WHITE) {
                        oldKingPos = board.whiteKingPos;
                        board.whiteKingPos = to;
                    } else {
                        oldKingPos = board.blackKingPos;
                        board.blackKingPos = to;
                    }
                }

                if (!board.isKingInCheck(this.player)) {
                    validMoves.add(to);
                }

                board.setPieceAt(from, this);
                board.setPieceAt(to, capturedPiece);
                if (this instanceof King) {
                    if (player == Player.WHITE) board.whiteKingPos = oldKingPos;
                    else board.blackKingPos = oldKingPos;
                }
            }
            
            if (this instanceof King) {
                validMoves.addAll(getCastlingMoves(from, board));
            }
            
            return validMoves;
        }
        
        protected void addSlidingMoves(Position from, Board board, int[] dRows, int[] dCols, List<Position> moves) {
            for (int i = 0; i < dRows.length; i++) {
                for (int j = 1; j < 8; j++) {
                    int r = from.row + dRows[i] * j;
                    int c = from.col + dCols[i] * j;
                    Position to = new Position(r, c);
                    
                    if (!to.isValid()) break;
                    
                    Piece target = board.getPieceAt(to);
                    if (target == null) {
                        moves.add(to);
                    } else {
                        if (target.getPlayer() != this.player) {
                            moves.add(to);
                        }
                        break;
                    }
                }
            }
        }
        
        protected List<Position> getCastlingMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            if (!(this instanceof King) || board.isKingInCheck(player)) {
                return moves;
            }
            
            // King-side
            if (board.getKingSideCastle(player)) {
                if (board.getPieceAt(new Position(from.row, 5)) == null &&
                    board.getPieceAt(new Position(from.row, 6)) == null &&
                    !board.isSquareAttackedBy(new Position(from.row, 5), player == Player.WHITE ? Player.BLACK : Player.WHITE) &&
                    !board.isSquareAttackedBy(new Position(from.row, 6), player == Player.WHITE ? Player.BLACK : Player.WHITE)) 
                {
                    moves.add(new Position(from.row, 6));
                }
            }
            
            // Queen-side
            if (board.getQueenSideCastle(player)) {
                if (board.getPieceAt(new Position(from.row, 1)) == null &&
                    board.getPieceAt(new Position(from.row, 2)) == null &&
                    board.getPieceAt(new Position(from.row, 3)) == null &&
                    !board.isSquareAttackedBy(new Position(from.row, 2), player == Player.WHITE ? Player.BLACK : Player.WHITE) &&
                    !board.isSquareAttackedBy(new Position(from.row, 3), player == Player.WHITE ? Player.BLACK : Player.WHITE))
                {
                    moves.add(new Position(from.row, 2));
                }
            }
            
            return moves;
        }
    }

    public static class Pawn extends Piece {
        public Pawn(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'P' : 'p'; }

        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int dir = (player == Player.WHITE) ? -1 : 1;
            int startRow = (player == Player.WHITE) ? 6 : 1;
            
            Position oneForward = new Position(from.row + dir, from.col);
            if (oneForward.isValid() && board.getPieceAt(oneForward) == null) {
                moves.add(oneForward);
                Position twoForward = new Position(from.row + 2 * dir, from.col);
                if (from.row == startRow && board.getPieceAt(twoForward) == null) {
                    moves.add(twoForward);
                }
            }
            
            int[] captureCols = { from.col - 1, from.col + 1 };
            for (int c : captureCols) {
                Position capturePos = new Position(from.row + dir, c);
                if (capturePos.isValid()) {
                    Piece target = board.getPieceAt(capturePos);
                    if (target != null && target.getPlayer() != this.player) {
                        moves.add(capturePos);
                    }
                    if (capturePos.equals(board.getEnPassantTarget())) {
                        moves.add(capturePos);
                    }
                }
            }
            return moves;
        }
    }

    public static class Rook extends Piece {
        public Rook(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'R' : 'r'; }
        
        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int[] dRows = { -1, 1, 0, 0 };
            int[] dCols = { 0, 0, -1, 1 };
            addSlidingMoves(from, board, dRows, dCols, moves);
            return moves;
        }
    }

    public static class Knight extends Piece {
        public Knight(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'N' : 'n'; }

        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int[] dRows = { -2, -2, -1, -1, 1, 1, 2, 2 };
            int[] dCols = { -1, 1, -2, 2, -2, 2, -1, 1 };
            
            for (int i = 0; i < 8; i++) {
                Position to = new Position(from.row + dRows[i], from.col + dCols[i]);
                if (to.isValid()) {
                    Piece target = board.getPieceAt(to);
                    if (target == null || target.getPlayer() != this.player) {
                        moves.add(to);
                    }
                }
            }
            return moves;
        }
    }

    public static class Bishop extends Piece {
        public Bishop(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'B' : 'b'; }

        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int[] dRows = { -1, -1, 1, 1 };
            int[] dCols = { -1, 1, -1, 1 };
            addSlidingMoves(from, board, dRows, dCols, moves);
            return moves;
        }
    }

    public static class Queen extends Piece {
        public Queen(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'Q' : 'q'; }
        
        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int[] dRows = { -1, 1, 0, 0, -1, -1, 1, 1 };
            int[] dCols = { 0, 0, -1, 1, -1, 1, -1, 1 };
            addSlidingMoves(from, board, dRows, dCols, moves);
            return moves;
        }
    }
    
    public static class King extends Piece {
        public King(Player player) { super(player); }
        public char getSymbol() { return (player == Player.WHITE) ? 'K' : 'k'; }
        
        @Override
        public List<Position> getRawMoves(Position from, Board board) {
            List<Position> moves = new ArrayList<>();
            int[] dRows = { -1, -1, -1, 0, 0, 1, 1, 1 };
            int[] dCols = { -1, 0, 1, -1, 1, -1, 0, 1 };
            
            for (int i = 0; i < 8; i++) {
                Position to = new Position(from.row + dRows[i], from.col + dCols[i]);
                if (to.isValid()) {
                    Piece target = board.getPieceAt(to);
                    if (target == null || target.getPlayer() != this.player) {
                        moves.add(to);
                    }
                }
            }
            return moves;
        }
    }
}
