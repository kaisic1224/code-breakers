public class MastermindGame {

    Token[] firstGuess;
    Token[] lastGuess; // the most recent guess guessed
    int np;

    Token[][] allPossibleCombos; // all possible token combos
    Token[][] remainingCombos; // token combos remaining in the set

    // CONSTRUCTOR
    MastermindGame(Token[] tokens, int np) {
        // get all possible code combos based on np, nc
        this.np = np;
        allPossibleCombos = generateAllPossibleCombos(tokens.length, tokens); // get all possible combinations of the
                                                                              // tokens
        remainingCombos = allPossibleCombos.clone();

        firstGuess = new Token[np];

        // set first guess to the best guess if the size of the code is 4.
        if (np == 4) {
            firstGuess[0] = new Token(tokens[0].toString());
            firstGuess[1] = new Token(tokens[0].toString());
            firstGuess[2] = new Token(tokens[1].toString());
            firstGuess[3] = new Token(tokens[1].toString());
        } else {
            firstGuess = allPossibleCombos[0];
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
        for (int i = 0; i < remainingCombos.length; i++) {

            if (tokenArrayEquals(remainingCombos[i], lastGuess)) {
                remainingCombos[i] = null;
            }

            if (remainingCombos[i] == null) {
                continue; // skip null entries
            }

            int[] temp = countPegs(lastGuess, remainingCombos[i]);
            if (temp[1] != black || temp[0] != white) {

                remainingCombos[i] = null;

            }
        }

        int[] scores = new int[allPossibleCombos.length];

        // wikipedia mastermind algorithm step 6
        for (int i = 0; i < allPossibleCombos.length; i++) {
            scores[i] = calculateMinScore(allPossibleCombos[i], black, white); // working here
        }

        // RETURN THE NEXT GUESS

        scores = removeNonMaxScores(scores);

        Token[] nextGuess = isThereAnElementInS(scores);

        // if there is an element in remaining
        if (nextGuess != null) {
            // choose the first one in S you find
            lastGuess = nextGuess.clone();
            return nextGuess;
        } else {
            // choose the first one you find
            nextGuess = firstElement(scores);
            lastGuess = nextGuess.clone();
            return nextGuess;
        }

    }

    Token[] firstElement(int[] scores) {

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] != 0) {
                return allPossibleCombos[i];
            }
        }

        // if get to this point, every score is 0

        return null;
    }

    // if finds an element of allPossibleCombos in remainingCombos, returns it, else
    // return null.
    Token[] isThereAnElementInS(int[] scores) {

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > 0) {
                if (contains(allPossibleCombos[i], remainingCombos)) {
                    return allPossibleCombos[i];
                }
            }
        }
        return null;
    }

    // is t in S?
    boolean contains(Token[] t, Token[][] S) {
        for (int i = 0; i < S.length; i++) {
            if (equals(t, S[i])) {
                return true;
            }
        }
        return false;
    }

    // keep only the highest scores
    int[] removeNonMaxScores(int[] scores) {

        int max = 0;

        for (int i = 0; i < scores.length; i++) {
            if (max < scores[i]) {
                max = scores[i];
            }
        }

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] != max) {
                scores[i] = 0;
            }
        }

        return scores;
    }

    // min number of entries in remainingCombos will be eliminated by guess for each
    // possible black/white score
    int calculateMinScore(Token[] code, int black, int white) {

        int[][] allbwcombos = allBWcombos(np);
        int[] scores = new int[allbwcombos.length];
        int count = 0;

        for (int i = 0; i < scores.length; i++) { // for every b/w combination, how many entries are eliminated?

            for (int j = 0; j < remainingCombos.length; j++) { // which entries in remainingCombos are eliminated?

                // skip previously eliminated entries
                if (remainingCombos[j] == null) {
                    continue;
                }

                int[] temp = countPegs(code, remainingCombos[j]);

                // if the black and white pegs match exactly
                if (temp[0] != allbwcombos[i][0] || temp[1] == allbwcombos[i][1]) {
                    // if temp doesnt match white/black, 'remove' (increment counter)
                    count++;
                }

                if (tokenArrayEquals(code, remainingCombos[j])) {
                    count++;
                }

            }
            scores[i] = count;
            count = 0;
        }

        // THE MIN IS ALWAYS ZERO

        return getMin(scores);
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

    // generates all possible black/white peg combos
    int[][] allBWcombos(int np) {

        int size = getSize(np);
        int[][] bwcombos = new int[size][2];

        int white = 0;
        int black = 0;
        int index = 0;

        for (int i = 0; i <= np; i++) {
            black = 0;
            white = i;
            while (white >= 0) {
                bwcombos[index][0] = white;
                bwcombos[index][1] = black;
                black++;
                white--;
                index++;
            }
        }

        return bwcombos;
    }

    int getSize(int np) {
        if (np == 1) {
            return 3;
        } else {
            return 1 + np + getSize(np - 1);
        }
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

    // returns the 'max' combo given the #positions(int[] length) and #colors
    int[] maxCombo(int[] combo, int nc) {
        int[] max = new int[combo.length];

        for (int i = 0; i < max.length; i++) {
            max[i] = nc - 1;
        }

        return max;
    }

    /*******************************************************************************************/
    /**************************************
     * GENERAL PURPOSE
     **************************************/
    /*******************************************************************************************/

    public int getMin(int[] a) {

        int min = Integer.MAX_VALUE;

        for (int i = 0; i < a.length; i++) {
            if (min > a[i]) {
                min = a[i];
            }
        }

        return min;
    }

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

    boolean equals(Token[] one, Token[] two) {
        if (one == null || two == null) {
            return false; // why do i need this????
        }
        for (int i = 0; i < one.length; i++) {
            if (!one[i].equals(two[i])) {
                return false;
            }
        }
        return true;
    }

}