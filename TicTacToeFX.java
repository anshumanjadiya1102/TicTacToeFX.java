import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Random;

public class TicTacToeFX extends Application {
    private static final int SIZE = 3;
    private Button[][] buttons = new Button[SIZE][SIZE];
    private char[][] board = new char[SIZE][SIZE];
    private char human = 'X', ai = 'O';
    private String difficulty = "Easy"; // default
    private Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        // Difficulty selector
        ComboBox<String> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyBox.setValue("Easy");
        difficultyBox.setOnAction(e -> difficulty = difficultyBox.getValue());

        // Reset button
        Button reset = new Button("Reset");
        reset.setOnAction(e -> resetBoard());

        HBox topBar = new HBox(20, new Label("Difficulty:"), difficultyBox, reset);
        topBar.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(5);
        grid.setHgap(5);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Button btn = new Button();
                btn.setFont(Font.font(28));
                btn.setPrefSize(100, 100);
                int row = r, col = c;
                btn.setOnAction(e -> handleMove(row, col, btn));
                buttons[r][c] = btn;
                grid.add(btn, c, r);
                board[r][c] = ' ';
            }
        }

        VBox root = new VBox(20, topBar, grid);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tic Tac Toe FX");
        primaryStage.show();
    }

    /* --------- Game Logic ---------- */

    private void handleMove(int r, int c, Button btn) {
        if (board[r][c] != ' ') return; // already filled
        board[r][c] = human;
        btn.setText(String.valueOf(human));
        btn.setTextFill(Color.BLUE);

        if (checkGameEnd(human)) return;

        // AI move
        int[] move = aiMove();
        if (move != null) {
            board[move[0]][move[1]] = ai;
            buttons[move[0]][move[1]].setText(String.valueOf(ai));
            buttons[move[0]][move[1]].setTextFill(Color.RED);
            checkGameEnd(ai);
        }
    }

    private boolean checkGameEnd(char player) {
        if (checkWinner(player)) {
            showAlert((player == human ? "You win! 🎉" : "Computer wins 🤖"));
            disableBoard();
            return true;
        } else if (isFull()) {
            showAlert("It's a Tie!");
            disableBoard();
            return true;
        }
        return false;
    }

    private void resetBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = ' ';
                buttons[r][c].setText("");
                buttons[r][c].setDisable(false);
            }
        }
    }

    private void disableBoard() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                buttons[r][c].setDisable(true);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    /* -------- AI Logic with difficulty -------- */

    private int[] aiMove() {
        switch (difficulty) {
            case "Easy":
                return randomMove();
            case "Medium":
                int[] smart = winningOrBlockingMove();
                return smart != null ? smart : randomMove();
            case "Hard":
                return bestMove();
            default:
                return randomMove();
        }
    }

    private int[] randomMove() {
        while (true) {
            int r = random.nextInt(SIZE);
            int c = random.nextInt(SIZE);
            if (board[r][c] == ' ') return new int[]{r, c};
        }
    }

    // Medium difficulty: try to win or block
    private int[] winningOrBlockingMove() {
        // try to win
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == ' ') {
                    board[r][c] = ai;
                    if (checkWinner(ai)) { board[r][c] = ' '; return new int[]{r, c}; }
                    board[r][c] = ' ';
                }
            }
        }
        // block human
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == ' ') {
                    board[r][c] = human;
                    if (checkWinner(human)) { board[r][c] = ' '; return new int[]{r, c}; }
                    board[r][c] = ' ';
                }
            }
        }
        return null;
    }

    /* -------- Minimax (Hard) -------- */

    private int[] bestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] move = null;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == ' ') {
                    board[r][c] = ai;
                    int score = minimax(0, false);
                    board[r][c] = ' ';
                    if (score > bestScore) {
                        bestScore = score;
                        move = new int[]{r, c};
                    }
                }
            }
        }
        return move;
    }

    private int minimax(int depth, boolean isMax) {
        if (checkWinner(ai)) return 10 - depth;
        if (checkWinner(human)) return depth - 10;
        if (isFull()) return 0;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    if (board[r][c] == ' ') {
                        board[r][c] = ai;
                        best = Math.max(best, minimax(depth + 1, false));
                        board[r][c] = ' ';
                    }
            return best;
        } else {
            int worst = Integer.MAX_VALUE;
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++)
                    if (board[r][c] == ' ') {
                        board[r][c] = human;
                        worst = Math.min(worst, minimax(depth + 1, true));
                        board[r][c] = ' ';
                    }
            return worst;
        }
    }

    /* -------- Helpers -------- */

    private boolean isFull() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == ' ') return false;
        return true;
    }

    private boolean checkWinner(char p) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == p && board[i][1] == p && board[i][2] == p) return true;
            if (board[0][i] == p && board[1][i] == p && board[2][i] == p) return true;
        }
        if (board[0][0] == p && board[1][1] == p && board[2][2] == p) return true;
        if (board[0][2] == p && board[1][1] == p && board[2][0] == p) return true;
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
