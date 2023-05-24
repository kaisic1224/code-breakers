import java.util.*;

public class AICodeBreaker {

    Board board;
    String[] firstGuess = { "B", "B", "R", "R" };
    String[] lastGuess; // the most recent guess guessed

    String[][] remainingCombos;
    String[][] allCombos;
    // FIGURE OUT HOW TO USE ENUM
    final String[] COLOURS = {
            "B",
            "Y",
            "O",
            "R",
            "P",
            "G",
    };

    // CONSTRUCTOR
    public AICodeBreaker(int numPositions) {

        board = new Board();

    }

    String[][] getCombos() {
        return remainingCombos;
    }

    String[] playGuess(int black, int white) {

        if (black == -1 && white == -1) {
            // it's the first guess
            lastGuess = firstGuess.clone();
            return firstGuess;
        }

        // wikipedia mastermind algorithm step 5. this works perfectly
        int counter = 0;

        for (int i = 0; i < remainingCombos.length; i++) {

            if (remainingCombos[i] != null) {
                if (remainingCombos[i].equals(lastGuess)) {
                    remainingCombos[i] = null;
                } else {

                    String[] feedback = board.checkGuess(lastGuess, remainingCombos[i], 0);
                    int[] pegHolder = board.returnPegs(feedback);

                    if (pegHolder[1] != black || pegHolder[0] != white) {

                        remainingCombos[i] = null;

                    } else if (counter == 0) {
                        counter = i;
                    }
                }
            }

        }

        lastGuess = remainingCombos[counter];
        return lastGuess;
    }

    public void generateAllCombos(int numPositions) {

        int[] combo = new int[numPositions];
        int[] prevCombo = new int[numPositions];
        remainingCombos = new String[(int) Math.pow(COLOURS.length, numPositions)][numPositions];

        for (int i = 0; i < numPositions; i++) {
            combo[i] = 0;
        }

        remainingCombos[0] = translateIntoColour(combo);

        prevCombo = combo;

        for (int i = 1; i < remainingCombos.length; i++) {
            combo = incrementCombo(prevCombo, numPositions, COLOURS.length);
            remainingCombos[i] = translateIntoColour(combo);

            prevCombo = combo;
        }

        allCombos = remainingCombos.clone();
    }

    private String[] translateIntoColour(int[] combo) {
        String[] colour = new String[combo.length];
        for (int i = 0; i < combo.length; i++) {
            colour[i] = COLOURS[combo[i]];
        }

        return colour;
    }

    private int[] incrementCombo(int[] oldCombo, int numPositions, int numColours) {

        int[] newCombo = oldCombo.clone();

        for (int i = numPositions - 1; i >= 0; i--) {

            if (newCombo[i] == numColours - 1) {
                if (newCombo[i - 1] != numColours - 1) {
                    newCombo[i - 1]++;

                    for (int j = i; j < numPositions; j++) {
                        newCombo[j] = 0;
                    }
                    return newCombo;
                }
            } else {
                newCombo[i]++;
                return newCombo;
            }

        }
        return newCombo;
    }

    public void printRemainingCombos(int numPositions) {
        for (int i = 0; i < remainingCombos.length; i++) {
            for (int j = 0; j < numPositions; j++) {
                System.out.print(remainingCombos[i][j] + " ");
            }
            System.out.println(" ");
        }
    }
}