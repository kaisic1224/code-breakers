package cb;

import java.util.*;
import cb.Board.Colour;

public class AICodeBreaker {

    Board board;
    String[] firstGuess = { "BLUE", "BLUE", "RED", "RED" };
    String[] lastGuess; // the most recent guess guessed

    ArrayList<String[]> remainingCombos = new ArrayList<String[]>();

    // CONSTRUCTOR
    public AICodeBreaker(int numPositions) {
        board = new Board();
    }

    String[] playGuess(int black, int white) {

        if (black == -1 && white == -1) {
            // it's the first guess
            lastGuess = firstGuess.clone();
            return firstGuess;
        }

        // wikipedia mastermind algorithm step 5. this works perfectly
        int counter = 0;

        for (int i = 0; i < remainingCombos.size(); i++) {

            if (remainingCombos.get(i).equals(lastGuess)) {
                remainingCombos.remove(i);
            }

            String[] feedback = board.checkGuess(lastGuess, remainingCombos.get(i), 0);
            int[] temp = board.returnPegs(feedback);

            if (temp[1] != black || temp[0] != white) {

                // System.out.println("whites " + temp[0] + " blacks: " + temp[1] + " | ");
                remainingCombos.remove(i);

            } else if (counter == 0) {
                counter = i;
            }
        }

        lastGuess = remainingCombos.get(counter);
        return lastGuess;
    }

    public void generateAllCombos(int numPositions) {

        int[] combo = new int[numPositions];
        int[] prevCombo = new int[numPositions];

        for (int i = 0; i < numPositions; i++) {
            combo[i] = 0;
        }

        remainingCombos.add(translateIntToColour(combo));

        prevCombo = combo;

        for (int i = 1; i < Math.pow(Colour.values().length, numPositions); i++) {
            combo = incrementCombo(prevCombo, numPositions, Colour.values().length);
            remainingCombos.add(translateIntToColour(combo));
            prevCombo = combo;
        }

    }

    private String[] translateIntToColour(int[] combo) {
        String[] colour = new String[combo.length];
        for (int i = 0; i < combo.length; i++) {
            colour[i] = Colour.values()[combo[i]].toString();
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
        for (int i = 0; i < remainingCombos.size(); i++) {
            for (int j = 0; j < numPositions; j++) {
                System.out.print(remainingCombos.get(i)[j] + " ");
            }
            System.out.println(" ");
        }
    }
}