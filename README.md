Java Swing Chess Game with MySQL

This is a fully functional, graphical chess application built entirely in Java. It features a clean UI using the Swing library, a complete game logic engine, and database connectivity to save and load games using MySQL.

This project was built to demonstrate core Java skills, Object-Oriented Programming (OOP) principles, and Model-View-Controller (MVC) architecture.

Features

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

Tech Stack

Core: Java SE (JDK 11+)

GUI: Java Swing

Database: MySQL

Connector: Java Database Connectivity (JDBC) API + MySQL Connector/J

Project Structure

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


Setup and Installation

To run this project, follow these steps:

Clone the Repository:

git clone [your-repository-url]


Set Up the Database:

Ensure you have a local MySQL server running.

Run the commands in schema.sql to create the chessgame database and the saved_games table.

Configure Credentials:

Open src/DatabaseManager.java.

Change the DB_URL, USER, and PASS constants to match your MySQL username and password.

Add JDBC Driver:

Download the MySQL Connector/J (.jar file) from the official MySQL website.

Add this .jar file to your Java project's build path (in Eclipse: right-click project -> Build Path -> Configure Build Path -> Libraries -> Add External JARs...).

Run the Application:

Compile and run the SwingChessGame.java file as the main application entry point.
