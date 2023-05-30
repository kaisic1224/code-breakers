package cb;

import java.util.*;
import cb.Board.Colour;

public class AICodeBreaker {

    Board board;
    String[] firstGuess = { "BLUE", "YELLOW", "ORANGE", "PINK" };
    String[] lastGuess; // the most recent guess guessed

    String[][] remainingCombos;
    String[][] allCombos;
    String[][] nonGuessedCombos;

    private int numPositions;

    // CONSTRUCTOR
    public AICodeBreaker(int numPositions) {

        this.numPositions = numPositions;
        board = new Board();

    }

    String[][] getCombos() {
        return remainingCombos;
    }

    // OITS NOT WORKEEY
    String[] guessCombo(int black, int white) {

        // it's the first guess
        if (black == -1 && white == -1) {

            lastGuess = firstGuess.clone();
            return firstGuess;
        }

        for (int i = 0; i < remainingCombos.length; i++) {

            if (remainingCombos[i] != null) {

                if (remainingCombos[i].equals(lastGuess)) {
                    remainingCombos[i] = null;
                    nonGuessedCombos[i] = null;

                } else {

                    String[] feedback = board.checkGuess(lastGuess, remainingCombos[i], 0);
                    int[] pegHolder = board.returnPegs(feedback);

                    if (pegHolder[1] != black || pegHolder[0] != white) {

                        remainingCombos[i] = null;

                    }
                }
            }

        }

        lastGuess = remainingCombos[scoreCombos()];

        return lastGuess;
    }

    public String[] miniMax() {

        int minimumEliminated = -1;
        String[] bestGuess = new String[numPositions];
        int[][] minMaxTable = new int[numPositions + 1][numPositions + 1];

        for (int i = 0; i < nonGuessedCombos.length; i++) {

            if (nonGuessedCombos[i] != null) {

                // add the feedback to the minMaxTable
                for (int j = 0; j < remainingCombos.length; j++) {

                    if (remainingCombos[j] != null) {
                        String[] feedback = board.checkGuess(remainingCombos[j], nonGuessedCombos[i], 0);
                        int[] pegHolder = board.returnPegs(feedback);
                        minMaxTable[pegHolder[1]][pegHolder[0]]++;
                    }

                }

                // determine the max
                int maximum = -1;

                for (int k = 0; k < minMaxTable.length; k++) {
                    for (int l = 0; l < minMaxTable[k].length; l++) {
                        if (minMaxTable[k][l] > maximum) {
                            maximum = minMaxTable[k][l];
                        }
                    }
                }

                // determine number of items left in possbile (use arraylist LOLO)
                int validItems = 0;

                for (int k = 0; k < nonGuessedCombos.length; k++) {
                    if (nonGuessedCombos[k] != null) {
                        validItems++;
                    }
                }

                // get score, return best combo
                int score = validItems - maximum;
                if (score > minimumEliminated) {
                    minimumEliminated = score;
                    bestGuess = nonGuessedCombos[i];
                }

            }

        }

        return bestGuess;
    }

    public int scoreCombos() {
        int highScore = -1;
        int index = 0;

        for (int i = 0; i < remainingCombos.length; i++) {
            int impactScore = 0;

            if (remainingCombos[i] != null) {
                for (int j = 0; j < remainingCombos.length; j++) {

                    if (remainingCombos[j] != null) {
                        String[] feedback = board.checkGuess(remainingCombos[i], remainingCombos[j], 0);
                        int[] pegHolder = board.returnPegs(feedback);

                        impactScore += pegHolder[1];
                        impactScore += pegHolder[0];
                    }

                }

                if (impactScore > highScore) {
                    impactScore = highScore;
                    index = i;
                }
            }
        }

        return index;
    }

    public void generateAllCombos(int numPositions) {

        remainingCombos = new String[(int) Math.pow(Colour.values().length, numPositions)][numPositions];
        int totalCount = 0;

        for (int i = 0; i < Colour.values().length; i++) {
            for (int j = 0; j < Colour.values().length; j++) {
                for (int k = 0; k < Colour.values().length; k++) {
                    for (int l = 0; l < Colour.values().length; l++) {

                        remainingCombos[totalCount][0] = Colour.values()[i].toString();
                        remainingCombos[totalCount][1] = Colour.values()[j].toString();
                        remainingCombos[totalCount][2] = Colour.values()[k].toString();
                        remainingCombos[totalCount][3] = Colour.values()[l].toString();

                        totalCount++;
                    }
                }

            }
        }

        allCombos = remainingCombos.clone();
        nonGuessedCombos = remainingCombos.clone();
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