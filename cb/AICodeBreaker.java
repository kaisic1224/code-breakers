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

    public ArrayList<String[]> miniMax() {

        HashMap<int[], Integer> timesFound = new HashMap();
        HashMap<String[], Integer> scores = new HashMap();
        ArrayList<String[]> goodGuesses = new ArrayList<String[]>();

        nonGuessedCombos = remainingCombos.clone();

        for (int i = 0; i < nonGuessedCombos.length; i++) {

            if (nonGuessedCombos[i] != null) {

                for (int j = 0; j < remainingCombos.length; j++) {

                    if (remainingCombos[j] != null) {
                        String[] feedback = board.checkGuess(remainingCombos[j], nonGuessedCombos[i], 0);
                        int[] pegHolder = board.returnPegs(feedback);

                        if (timesFound.get(pegHolder) == null) {
                            timesFound.put(pegHolder, 0);
                        }

                        timesFound.put(pegHolder, timesFound.get(pegHolder) + 1);
                    }
                }

                int maxScore = (Collections.max(timesFound.values()));
                scores.put(nonGuessedCombos[i], maxScore);
            }

        }

        int minScore = (Collections.min(scores.values()));

        for (int i = 0; i < nonGuessedCombos.length; i++) {

            if (nonGuessedCombos[i] != null) {

                if (scores.get(nonGuessedCombos[i]).equals(minScore)) {
                    goodGuesses.add(nonGuessedCombos[i]);
                }

            }

        }

        return goodGuesses;
        // make different diffcultities
    }

    public String[] bestGuess() {
        ArrayList<String[]> goodCodes = miniMax();
        String[] optimalGuess = getOptimalGuess(goodCodes);

        return optimalGuess;
    }

    public String[] getOptimalGuess(ArrayList<String[]> goodCodes) {
        for (int i = 0; i < goodCodes.size(); i++) {

            for (int j = 0; j < remainingCombos.length; j++) {

                if (goodCodes.get(i).equals(remainingCombos[j])) {
                    return remainingCombos[j];
                }

            }

        }

        return goodCodes.get(0);
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
            System.out.print(i);
            for (int j = 0; j < numPositions; j++) {
                System.out.print(remainingCombos[i][j] + " ");
            }
            System.out.println(" ");
        }
    }
}