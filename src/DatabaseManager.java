//DatabaseManager.java


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database communication for saving and loading games.
 * Requires the MySQL JDBC driver (Connector/J) to be in the classpath.
 */
public class DatabaseManager {

    // --- IMPORTANT ---
    // Change these values to match your MySQL server setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chessgame";
    private static final String USER = "root"; // e.g., "root"
    private static final String PASS = "root"; // e.g., "password"
    // ---------------

    private Connection conn;

    /**
     * Establishes the database connection upon creation.
     */
    public DatabaseManager() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Make sure Connector/J is in your classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database. Check URL, username, and password.", e);
        }
    }

    /**
     * Saves the current board state to the database.
     * Overwrites any existing game with the same name.
     */
    public void saveGame(String gameName, GameLogic.Board board) throws SQLException {
        // SQL query to insert or update
        String sql = "INSERT INTO saved_games (game_name, board_state, current_player, castling_rights, en_passant_target) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "board_state = VALUES(board_state), " +
                     "current_player = VALUES(current_player), " +
                     "castling_rights = VALUES(castling_rights), " +
                     "en_passant_target = VALUES(en_passant_target)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gameName);
            pstmt.setString(2, board.getBoardStateString()); // "rnbqkbnr/..."
            pstmt.setString(3, board.getCurrentPlayer().name()); // "WHITE" or "BLACK"
            pstmt.setString(4, board.getCastlingRightsString()); // "KQkq" or "-"
            
            GameLogic.Position enPassant = board.getEnPassantTarget();
            if (enPassant != null) {
                pstmt.setString(5, enPassant.toString()); // "e3"
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            pstmt.executeUpdate();
        }
    }

    /**
     * Loads a game state from the database.
     * @return A new Board object, or null if the game is not found.
     */
    public GameLogic.Board loadGame(String gameName) throws SQLException {
        String sql = "SELECT * FROM saved_games WHERE game_name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gameName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    GameLogic.Board board = new GameLogic.Board(); // Create a fresh board
                    
                    // Load and set all properties
                    board.loadBoardStateString(rs.getString("board_state"));
                    board.setCurrentPlayer(GameLogic.Player.valueOf(rs.getString("current_player")));
                    board.loadCastlingRightsString(rs.getString("castling_rights"));
                    
                    String enPassantStr = rs.getString("en_passant_target");
                    if (enPassantStr != null) {
                        board.setEnPassantTarget(GameLogic.Position.fromString(enPassantStr));
                    } else {
                        board.setEnPassantTarget(null);
                    }

                    return board;
                }
            }
        }
        return null; // Game not found
    }

    /**
     * Retrieves a list of all saved game names.
     */
    public List<String> getSavedGames() throws SQLException {
        List<String> gameNames = new ArrayList<>();
        String sql = "SELECT game_name FROM saved_games ORDER BY last_updated DESC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                gameNames.add(rs.getString("game_name"));
            }
        }
        return gameNames;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
