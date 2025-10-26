//SwingChessGame.java

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main application class.
 * Sets up the main window (JFrame) and coordinates the UI elements
 * (BoardPanel, status bar) with the backend (GameLogic.Board, DatabaseManager).
 */
public class SwingChessGame extends JFrame {

    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private GameLogic.Board logicBoard;
    private DatabaseManager dbManager;

    public SwingChessGame() {
        // Initialize backend components
        logicBoard = new GameLogic.Board();
        try {
            dbManager = new DatabaseManager();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to connect to database: " + e.getMessage() +
                "\nPlease check your connection and credentials in DatabaseManager.java",
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            // Allow game to run offline, but warn user
            dbManager = null; 
        }

        setTitle("Java Swing Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Board Panel - Pass the logic board to it
        boardPanel = new BoardPanel(logicBoard);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel();
        updateStatus();
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("New Game");
        resetButton.addActionListener(e -> resetGame());
        
        JButton saveButton = new JButton("Save Game");
        saveButton.addActionListener(e -> saveGame());

        JButton loadButton = new JButton("Load Game");
        loadButton.addActionListener(e -> loadGame());

        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        statusPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        pack(); // Size the frame
        setLocationRelativeTo(null); // Center on screen

        // Pass this main frame to the board panel for handling promotions
        boardPanel.setFrame(this);
    }

    /**
     * Resets the game to the initial state.
     */
    public void resetGame() {
        logicBoard.setupNewGame();
        boardPanel.setSelectedPos(null);
        boardPanel.setValidMoves(new java.util.ArrayList<>());
        updateStatus();
        boardPanel.repaint();
    }
    
    /**
     * Updates the status label from the logic board.
     */
    public void updateStatus() {
        statusLabel.setText(logicBoard.getStatusMessage());
    }

    /**
     * Prompts the user to save the current game.
     */
    private void saveGame() {
        if (dbManager == null) {
            showDbError();
            return;
        }
        String gameName = JOptionPane.showInputDialog(this,
            "Enter a name for this game:",
            "Save Game",
            JOptionPane.PLAIN_MESSAGE);
        
        if (gameName != null && !gameName.trim().isEmpty()) {
            try {
                dbManager.saveGame(gameName, logicBoard);
                JOptionPane.showMessageDialog(this, "Game '" + gameName + "' saved successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to save game: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Prompts the user to load a saved game.
     */
    private void loadGame() {
        if (dbManager == null) {
            showDbError();
            return;
        }
        try {
            List<String> gameNames = dbManager.getSavedGames();
            if (gameNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No saved games found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] options = gameNames.toArray(new String[0]);
            String gameName = (String) JOptionPane.showInputDialog(this,
                "Select a game to load:",
                "Load Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

         // ...
            if (gameName != null) {
                GameLogic.Board loadedBoard = dbManager.loadGame(gameName);
                if (loadedBoard != null) {
                    this.logicBoard = loadedBoard;
                    boardPanel.setLogicBoard(loadedBoard); // Link panel to new board
                    
                    // --- FIX ---
                    // Don't call resetGame(), as it re-initializes the board.
                    // Instead, just reset the UI selection state.
                    boardPanel.setSelectedPos(null);
                    boardPanel.setValidMoves(new java.util.ArrayList<>());
                    // --- END FIX ---
                    
                    updateStatus(); // Get status from the newly loaded board
                    boardPanel.repaint(); // Redraw with the loaded board's state
                    JOptionPane.showMessageDialog(this, "Game '" + gameName + "' loaded successfully!");
                }
            }
// ... 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load games: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showDbError() {
        JOptionPane.showMessageDialog(this,
            "Database not connected. Please check console and restart.",
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- Main Method ---
    public static void main(String[] args) {
        // Run the game on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            SwingChessGame game = new SwingChessGame();
            game.setVisible(true);
        });
    }
}

