import java.util.ArrayList;

public class Board {
    private String[][] board;
    private String[][] guesses;
    private String[] code;
    final int VALID_CHARS = 6;
    final int SIZE = 4;
    final int TRIES = 10;

    public enum Colour {
        G,
        R,
        B,
        Y,
        O,
        P
    }

    public Board(boolean pvp) {
        board = new String[TRIES][SIZE];
        code = new String[SIZE];
        generateCode();
        if (pvp) {
            try {
                getCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    public void getCode() {
        for (String c : code) {
            System.out.print(c);
        }
        System.out.println();
    }

    public void generateCode() {
        for (int i = 0; i < code.length; i++) {
            int randIndex = (int) (Math.random() * Colour.values().length);
            code[i] = Colour.values()[randIndex].name();
        }
    }

    public String[] checkGuess(String[] guess, int turn) {
        String[] evaluation = new String[code.length];
        String stringCode = String.join("", code);

        // detect all correct position, correct colour
        for (int i = 0; i < guess.length; i++) {
            if (guess[i].equals(code[i])) {
                evaluation[i] = "b";
            }
        }

        // detect all wrong position, correct colour
        for (int i = 0; i < guess.length; i++) {
            if (evaluation[i] != null) {
                continue;
            }

            if (stringCode.contains(guess[i])) { // check if colour at guess is in code
                int index = stringCode.indexOf(guess[i]);

                if (evaluation[index] != null) { // if guess already has corect colour correct place, continue
                    continue;
                }

                String beforeHalf = stringCode.substring(0, index); // split code into before and after half
                String afterHalf = stringCode.substring(index, stringCode.length());
                stringCode = beforeHalf + afterHalf; // combine both halfs, omits found guess

                evaluation[i] = "w"; // update feedback
            }
        }

        for (String string : evaluation) {
            System.out.println(string);
        }

        board[turn] = guess;
        guesses[turn] = evaluation;

        return evaluation;
    }
}
