package cb;

import java.awt.Color;
import java.util.ArrayList;

public class Board {
    public String[][] board;
    public String[][] feedback;
    public String[] code;
    public int turn = 0;
    private final int VALID_CHARS = 6;
    private final int SIZE = 4;
    private final int TRIES = 10;

    public enum Colour {
        BLUE,
        YELLOW,
        ORANGE,
        RED,
        PINK,
        GREEN,
    }

    public Board() {
        board = new String[TRIES][SIZE];
        feedback = new String[TRIES][SIZE];
        code = new String[SIZE];
    }

    public int getTries() {
        return TRIES;
    }

    public int getSize() {
        return SIZE;
    }

    public String[] getCode() {
        return code;
    }

    public void printCode() {
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

    // THERE IS SOMETHING WRONG HERE
    // hours wasted starting now: 1.5
    public String[] checkGuess(String[] guess, String[] solution, int turn) {
        String[] temporaryCode = solution.clone();
        String[] evaluation = new String[solution.length];

        // detect all correct position, correct colour
        for (int i = 0; i < guess.length; i++) {
            if (guess[i].equals(temporaryCode[i])) {
                evaluation[i] = "b";
                temporaryCode[i] = null;
            }
        }

        // detect all wrong position, correct colour
        for (int i = 0; i < guess.length; i++) {
            if (evaluation[i] != null) { // if evaluation already declared black or white, skip
                continue; // prevents overwriting existing feedback or relabeling black peg as white
            }

            if (evaluation[i] == null) { // colour that was not black
                for (int j = 0; j < guess.length; j++) {
                    if (guess[i].equals(temporaryCode[j])) { // check if solution contains colour
                        evaluation[i] = "w"; // add to feedback

                        temporaryCode[j] = null; // invalidate colour so cannot be detected on next scan through
                        break;
                    }
                }
            }

        }

        // remove all nulls
        ArrayList<String> fb = new ArrayList<String>();
        for (int i = 0; i < evaluation.length; i++) {
            if (evaluation[i] != null) {
                fb.add(evaluation[i]); // add only colours to arraylist
            }
        }

        String[] a = new String[fb.size()];

        return fb.toArray(a);
    }

    public int[] returnPegs(String[] evaluation) {
        int[] pegHolder = new int[2]; // where 1 = black and 0 = white;

        for (int i = 0; i < evaluation.length; i++) {
            if (evaluation[i].equals("w")) {
                pegHolder[0]++;
            } else if (evaluation[i].equals("b")) {
                pegHolder[1]++;
            }
        }

        return pegHolder;

    }

    public String colorToString(Color c) {
        switch (c.toString()) {
            case "java.awt.Color[r=0,g=255,b=0]":
                return "GREEN";
            case "java.awt.Color[r=255,g=255,b=0]":
                return "YELLOW";
            case "java.awt.Color[r=0,g=0,b=255]":
                return "BLUE";
            case "java.awt.Color[r=255,g=200,b=0]":
                return "ORANGE";
            case "java.awt.Color[r=255,g=0,b=0]":
                return "RED";
            case "java.awt.Color[r=255,g=175,b=175]":
                return "PINK";
            default:
                return "INVALID COLOUR";
        }
    }

}
