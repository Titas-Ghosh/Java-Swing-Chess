Java Swing Chess Game with MySQL

This is a fully functional, graphical chess application built entirely in Java. It features a clean UI using the Swing library, a complete game logic engine, and database connectivity to save and load games using MySQL.

This project was built to demonstrate core Java skills, Object-Oriented Programming (OOP) principles, and Model-View-Controller (MVC) architecture.

🚀 Features

Graphical User Interface: A clean, clickable 8x8 chessboard built with Java Swing.

Complete Game Logic: Implements all standard chess rules:

Standard piece movement (Pawn, Rook, Knight, Bishop, Queen, King).

Capture logic.

Check and Checkmate detection.

Stalemate detection.

Special Moves: Castling (King-side & Queen-side), En Passant, and Pawn Promotion.

Database Persistence:

Save Game: Save the exact state of your current game to a MySQL database with a custom name.

Load Game: Load any previously saved game from the database to resume playing.

MVC Architecture: The project is separated into logical components:

Model (GameLogic.java): All game rules and state.

View (BoardPanel.java): All drawing and UI rendering.

Controller (SwingChessGame.java): Manages user input and coordinates the Model and View.

Database (DatabaseManager.java): Handles all SQL queries and JDBC connection.

💻 Tech Stack

Core: Java SE (JDK 11+)

GUI: Java Swing

Database: MySQL

Connector: Java Database Connectivity (JDBC) API + MySQL Connector/J

📁 Project Structure

/src
├── SwingChessGame.java   # Controller: The main application window and buttons
├── BoardPanel.java       # View: Renders the board, pieces, and highlights
├── GameLogic.java        # Model: Contains all game rules, piece classes, and board state
└── DatabaseManager.java  # Database: Handles all JDBC connection and SQL queries
/lib
└── mysql-connector-j-X.X.XX.jar # The JDBC Driver
schema.sql                # The SQL script to create the database table
README.md                 # This file
.gitignore                # Tells Git which files to ignore


🛠️ Setup and Installation

To run this project, follow these steps:

Clone the Repository:

git clone [https://github.com/](https://github.com/)[Your-Username]/[Your-Repo-Name].git


Set Up the Database:

Ensure you have a local MySQL server running.

Run the commands in schema.sql (or copy-paste the block below) in your MySQL client (like MySQL Workbench).

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS chessgame;

-- 2. Use the new database
USE chessgame;

-- 3. Create the table to store saved games
CREATE TABLE IF NOT EXISTS saved_games (
    game_name VARCHAR(100) PRIMARY KEY,
    board_state TEXT NOT NULL,
    current_player VARCHAR(5) NOT NULL,
    castling_rights VARCHAR(4) NOT NULL,
    en_passant_target VARCHAR(2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


Configure Credentials:

Open src/DatabaseManager.java.

Change the DB_URL, USER, and PASS constants to match your MySQL username and password.

// Inside src/DatabaseManager.java

// --- IMPORTANT ---
// Change these values to match your MySQL server setup
private static final String DB_URL = "jdbc:mysql://localhost:3306/chessgame";
private static final String USER = "your_username"; // e.g., "root"
private static final String PASS = "your_password"; // e.g., "password"
// ---------------


Add JDBC Driver:

Download the MySQL Connector/J (.jar file) from the official MySQL website.

Add this .jar file to your Java project's build path.

(In Eclipse): Right-click project -> Build Path -> Configure Build Path -> Libraries -> Add External JARs...

(In IntelliJ): File -> Project Structure -> Modules -> Dependencies -> '+' -> JARs or Directories...

Run the Application:

Compile and run the SwingChessGame.java file as the main application entry point.
