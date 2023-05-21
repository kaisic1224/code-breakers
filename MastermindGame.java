public class MastermindGame {

    Token[] firstGuess;
    Token[] lastGuess; // the most recent guess guessed
    int np;

    Token[][] remainingCombos; // token combos remaining in the set

    // CONSTRUCTOR
    MastermindGame(Token[] tokens, int np) {
        // get all possible code combos based on np, nc
        this.np = np;
        remainingCombos = generateAllPossibleCombos(tokens.length, tokens); // get all possible combinations of the
                                                                            // tokens
        remainingCombos = remainingCombos.clone();

        firstGuess = new Token[np];

        // set first guess to the best guess if the size of the code is 4.
        if (np == 4) {
            firstGuess[0] = new Token(tokens[0].toString());
            firstGuess[1] = new Token(tokens[0].toString());
            firstGuess[2] = new Token(tokens[1].toString());
            firstGuess[3] = new Token(tokens[1].toString());
        } else {
            firstGuess = remainingCombos[0];
        }

    }

    /******************************************************************************/
    /***************************
     * THE GUESSING ALGORITHM
     *****************************/

    // given black and white pegs of the previous guess, formulate a new guess
    Token[] playGuess(int black, int white) {

        if (black == -1 && white == -1) {
            // it's the first guess
            lastGuess = firstGuess.clone();
            return firstGuess;
        }

        // wikipedia mastermind algorithm step 5. this works perfectly
        int counter = 0;

        for (int i = 0; i < remainingCombos.length; i++) {

            if (tokenArrayEquals(remainingCombos[i], lastGuess)) {
                remainingCombos[i] = null;
            }

            if (remainingCombos[i] == null) {
                continue; // skip null entries
            }

            int[] temp = countPegs(lastGuess, remainingCombos[i]);

            if (temp[1] != black || temp[0] != white) {

                System.out.println("whites " + temp[0] + " blacks: " + temp[1] + " | ");
                printArray(remainingCombos[i]);
                remainingCombos[i] = null;

            } else if (counter == 0) {
                counter = i;
            }
        }

        lastGuess = remainingCombos[counter];
        return lastGuess;
    }

    /******************************************************************************/
    /*****************************
     * COUNTING THE PEGS
     ********************************/
    /******************************************************************************/

    // counts the white and black pegs of a guess and code
    int[] countPegs(Token[] code, Token[] guess) {
        int[] bw = new int[2];
        bw[1] = countBlack(code, guess);
        bw[0] = Math.max(countWhite(code, guess) - bw[1], 0); // make sure white pegs stay >=0.
        return bw;
    }

    int countBlack(Token[] code, Token[] guess) {
        int black = 0;
        for (int i = 0; i < code.length; i++) {
            if (code[i].equals(guess[i])) {
                black++;
            }
        }
        return black;
    }

    int countWhite(Token[] code, Token[] guess) {
        int white = 0;
        Token[] clone = code.clone();
        for (int i = 0; i < code.length; i++) {

            for (int j = 0; j < clone.length; j++) {
                if (guess[i].equals(clone[j])) {
                    white++;
                    clone[j] = null;
                    break;
                }
            }

        }
        return white;
    }

    /**************************************************************************************************/
    /*******************
     * CODE FOR GETTING ALL POSSIBLE COMBINATIONS OF CODES
     ****************************/
    /**************************************************************************************************/
    // takes np=numPositions, and nc=numColors
    Token[][] generateAllPossibleCombos(int nc, Token[] tokens) {

        int[][] list = new int[(int) Math.pow(nc, np)][np]; // make the array the right size

        // set the first possible combo = 0 0 0 0....
        for (int i = 0; i < np; i++) {
            list[0][i] = 0;
        }

        // generate the rest of the combos
        for (int i = 1; i < list.length; i++) {
            list[i] = incrementCombo(list[i - 1], np, nc);
        }

        // turn the int[][] into a Token[][]

        Token[][] allCombos = new Token[list.length][np];

        // for each combo:
        for (int i = 0; i < list.length; i++) {

            // for each position in the combo list[i]:
            for (int j = 0; j < np; j++) {

                allCombos[i][j] = new Token(tokens[list[i][j]].toString());

            }
        }

        return allCombos;
    }

    // np=number of positions, nc = number of colors
    int[] incrementCombo(int[] oldCombo, int np, int nc) {

        int[] newCombo = oldCombo.clone();

        for (int i = np - 1; i > 0; i--) {

            if (newCombo[i] == nc - 1) {
                if (newCombo[i - 1] == nc - 1) {
                    continue;
                }
                newCombo[i - 1]++;

                // reset the previous digits to 0
                for (int j = i; j < np; j++) {
                    newCombo[j] = 0;
                }

                return newCombo;
            }
            newCombo[i]++;
            return newCombo;
        }

        return newCombo;
    }

    /*******************************************************************************************/
    /**************************************
     * GENERAL PURPOSE
     **************************************/
    /*******************************************************************************************/

    // bro is so extra *crieing emoji*
    public void printArray(Object[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + "\t");
        }
        System.out.println();
    }

    public void printArray(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + "\t");
        }
        System.out.println();
    }

    public void print2DArray(Object[][] a) {
        for (int i = 0; i < a.length; i++) {
            printArray(a[i]);
        }
    }

    public void print2DArray(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            printArray(a[i]);
        }
    }

    boolean tokenArrayEquals(Token[] one, Token[] two) {
        if (one == null || two == null) {
            return false;
        }
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (!one[i].equals(two[i])) {
                return false;
            }
        }
        return true;
    }

}