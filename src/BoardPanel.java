//BoardPanel.java


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Represents the graphical chessboard panel (JPanel).
 * Handles all drawing of the board, pieces, and highlights.
 * Also handles mouse clicks for piece selection and movement.
 */
public class BoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 80;
    private static final int BOARD_SIZE = 8 * SQUARE_SIZE;

    // Unicode pieces
    private final Font PIECE_FONT = new Font("SansSerif", Font.PLAIN, 60);
    private final String[] PIECES = {
        "K", "♔", "k", "♚",
        "Q", "♕", "q", "♛",
        "R", "♖", "r", "♜",
        "B", "♗", "b", "♝",
        "N", "♘", "n", "♞",
        "P", "♙", "p", "♟"
    };
    
    // Colors
    private final Color LIGHT_SQUARE = new Color(234, 221, 197); // #EADDC5
    private final Color DARK_SQUARE = new Color(169, 138, 111); // #A98A6F
    private final Color SELECTED_COLOR = new Color(30, 144, 255, 128); // Dodgerblue with alpha
    private final Color VALID_MOVE_COLOR = new Color(0, 0, 0, 64); // Dark dot
    private final Color CHECK_COLOR = new Color(255, 0, 0, 150); // Red for check
    
    // Game State
    private GameLogic.Board logicBoard;
    private GameLogic.Position selectedPos = null;
    private List<GameLogic.Position> validMoves = new ArrayList<>();
    
    // Reference to the main frame to update status
    private SwingChessGame mainFrame;

    BoardPanel(GameLogic.Board logicBoard) {
        this.logicBoard = logicBoard;
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        addMouseListener(new BoardMouseListener());
    }

    // Setters for UI state
    public void setLogicBoard(GameLogic.Board board) { this.logicBoard = board; }
    public void setSelectedPos(GameLogic.Position pos) { this.selectedPos = pos; }
    public void setValidMoves(List<GameLogic.Position> moves) { this.validMoves = moves; }
    public void setFrame(SwingChessGame frame) { this.mainFrame = frame; }

    /**
     * Converts a piece character (e.g., 'P', 'r') to its Unicode symbol.
     */
    private String getPieceSymbol(char pieceChar) {
        for (int i = 0; i < PIECES.length; i += 2) {
            if (PIECES[i].charAt(0) == pieceChar) {
                return PIECES[i+1];
            }
        }
        return "";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                // Draw square
                Color squareColor = (r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                g2.setColor(squareColor);
                g2.fillRect(c * SQUARE_SIZE, r * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);

                // Draw piece
                GameLogic.Piece p = logicBoard.getPieceAt(new GameLogic.Position(r, c));
                if (p != null) {
                    g2.setFont(PIECE_FONT);
                    String symbol = getPieceSymbol(p.getSymbol());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = c * SQUARE_SIZE + (SQUARE_SIZE - fm.stringWidth(symbol)) / 2;
                    int y = r * SQUARE_SIZE + ((SQUARE_SIZE - fm.getHeight()) / 2) + fm.getAscent();
                    g2.setColor(p.getPlayer() == GameLogic.Player.WHITE ? Color.WHITE : Color.BLACK);
                    g2.drawString(symbol, x, y);
                }
            }
        }
        
        // Highlight King in Check
        if (logicBoard.isKingInCheck(logicBoard.getCurrentPlayer())) {
            GameLogic.Position kingPos = logicBoard.getKingPos(logicBoard.getCurrentPlayer());
            g2.setColor(CHECK_COLOR);
            g2.fillRect(kingPos.col * SQUARE_SIZE, kingPos.row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }

        // Highlight selected square
        if (selectedPos != null) {
            g2.setColor(SELECTED_COLOR);
            g2.fillRect(selectedPos.col * SQUARE_SIZE, selectedPos.row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }

        // Highlight valid moves
        g2.setColor(VALID_MOVE_COLOR);
        for (GameLogic.Position move : validMoves) {
            int centerX = move.col * SQUARE_SIZE + SQUARE_SIZE / 2;
            int centerY = move.row * SQUARE_SIZE + SQUARE_SIZE / 2;
            
            if (logicBoard.getPieceAt(move) != null) {
                // Draw a ring for captures
                g2.setStroke(new BasicStroke(5));
                g2.drawOval(centerX - 25, centerY - 25, 50, 50);
            } else {
                // Draw a dot for empty squares
                g2.fillOval(centerX - 15, centerY - 15, 30, 30);
            }
        }
    }
    
    /**
     * Handles mouse clicks on the board.
     */
    class BoardMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (logicBoard.isGameOver()) return;

            int col = e.getX() / SQUARE_SIZE;
            int row = e.getY() / SQUARE_SIZE;
            GameLogic.Position clickedPos = new GameLogic.Position(row, col);
            
            GameLogic.Piece clickedPiece = logicBoard.getPieceAt(clickedPos);

            if (selectedPos == null) {
                // 1. First click: Select a piece
                if (clickedPiece != null && clickedPiece.getPlayer() == logicBoard.getCurrentPlayer()) {
                    selectedPos = clickedPos;
                    validMoves = logicBoard.getValidMovesForPiece(selectedPos);
                } else {
                    selectedPos = null;
                    validMoves.clear();
                }
            } else {
                // 2. Second click: Attempt to move
                boolean isValidMove = false;
                for (GameLogic.Position move : validMoves) {
                    if (move.equals(clickedPos)) {
                        isValidMove = true;
                        break;
                    }
                }

                if (isValidMove) {
                    // --- Make the move ---
                    logicBoard.makeMove(selectedPos, clickedPos);
                }
                
                // --- Reset selection ---
                selectedPos = null;
                validMoves.clear();
            }
            
            // Update UI
            if (mainFrame != null) {
                mainFrame.updateStatus();
            }
            repaint();
        }
    }
}
