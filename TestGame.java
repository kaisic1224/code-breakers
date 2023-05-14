public class TestGame {
    public static void main(String[] args) {
        Board b = new Board(true);
        String[] guess = {"G", "B", "O", "B"};

        b.checkGuess(guess, 0);
    }
}
