//Author: Isaac and Vincent 
//Title: Codebreaker Assignment - AI class

//Description: 
//This class contains all the method the AI requires to guess the code the set by the player. 
//Our AI works by generating all combos, recieving feedback, and slowly eliminating combinations that 
//would not provide the same feedback. 

//Date: June 7, 2023

package cb;

import cb.Board.Colour; //import colours used throughout the project

public class AICodeBreaker {

    // declare object board so we can access methods inside board
    Board board;

    // for our AI, this is the best first guess it can make
    private String[] firstGuess = { "BLUE", "YELLOW", "ORANGE", "PINK" };

    // used to save our previous guess (String [] as each element contains a colour)
    private String[] lastGuess;

    // used to store all remaining valid combinations to guess
    private String[][] remainingCombos;

    // used to store all possible colour combinations from the 6 colours and 4
    // positions
    private String[][] allCombos;

    // constructor - intialize our board object here
    public AICodeBreaker() {
        board = new Board();
    }

    /**
     * an Accessor / getter method which allows us to access the colours stored in
     * last guess while protecting our data
     *
     * @param nothing
     * @return returns the value of lastGuess as a String[]
     */
    public String[] getLastGuess() {
        return lastGuess;
    }

    /**
     * an Accessor / getter method which allows us to access allCombos (stores all
     * possible colour combinations)
     *
     * @param nothing
     * @return returns the value of allCombos as a String[][]
     */
    public String[][] getAllCombos() {
        return allCombos;
    }

    /**
     * an Accessor / getter method which allows us to access remainingCombos (stores
     * all potential combos)
     *
     * @param nothing
     * @return returns the value of remainingCombos as a String[][]
     */
    public String[][] getRemainingCombos() {
        return remainingCombos;
    }

    /**
     * a modifier / setter method which allows us to update the avlue of
     * remainingCombos
     *
     * @param newRemainingCombos - String [][], remaining combos will be updated
     *                           with this value
     * @return nothing
     */
    public void setRemainingCombos(String[][] newRemaingCombos) {
        remainingCombos = newRemaingCombos;
    }

    /**
     * a method that returns the AI's guess based on the feedback provided by the
     * user
     *
     * @param black - int, the number of black pegs the previous guess recieved
     * @param white - int, the nubmer of white pegs thep revious guess
     *              recieved
     * @return lastGuess - String [] the next most optimal guess for the AI which
     *         elimnates the most amount of remaing combinations
     */
    public String[] guessCombo(int black, int white) {

        // if -1,-1 is passed through, we know that it is the AI's first guess
        if (black == -1 && white == -1) {
            lastGuess = firstGuess; // intialize last guess with the most optimal first move
            return lastGuess;
        }

        // looping through all remaining combinations stored in a String [][]
        for (int i = 0; i < remainingCombos.length; i++) {

            if (remainingCombos[i] != null) { // null correlates with invalid combinations

                // if we are still guessing a combination, our previous guess must be invalid
                if (remainingCombos[i].equals(lastGuess)) {
                    remainingCombos[i] = null; // write off as null

                } else {

                    // get feedback when comparing remaining combinations with the last Guess
                    String[] feedback = board.checkGuess(lastGuess, remainingCombos[i]);
                    int[] pegHolder = board.returnPegs(feedback); // where [1] correlates with black pegs and [0] with
                                                                  // white pegs

                    // if a remaining combination is not a valid variation of our previous guess,
                    // remove it
                    if (pegHolder[1] != black || pegHolder[0] != white) {

                        remainingCombos[i] = null;

                    }
                }
            }

        }

        // get the combination from remainingCombos that is bound to remove the most
        // amount of items
        lastGuess = remainingCombos[scoreCombos()];

        return lastGuess; // return lastGuess
    }

    /**
     * Provides the index of the AI's guess. Each combination is assigned a score
     * and the index of the combination with the highest score is returned. Score is
     * given based on the amount of feedback a combination gives when compared to
     * other combinations
     *
     * @param nothing
     * @return index - int, the index of the AI's next guess in remainingCombos
     *         (String
     *         [][])
     */
    public int scoreCombos() {
        // initialize variable to keep track of score and counter
        int highScore = -1;
        int index = 0;

        for (int i = 0; i < remainingCombos.length; i++) { // loop through all remaining combos
            int impactScore = 0; // intialize score tracker (compared with high score)

            if (remainingCombos[i] != null) { // if the combo is valid
                for (int j = 0; j < remainingCombos.length; j++) { // loop through all other combinations

                    if (remainingCombos[j] != null) { // if we are comparing against a valid combo

                        // get feedback
                        String[] feedback = board.checkGuess(remainingCombos[i], remainingCombos[j]);
                        int[] pegHolder = board.returnPegs(feedback);

                        // increment our score by the feedback
                        impactScore += pegHolder[1];
                        impactScore += pegHolder[0];
                    }

                }

                // if our current score is greater than the high score
                if (impactScore > highScore) {
                    // replace values with new highscore and index
                    impactScore = highScore;
                    index = i;
                }
            }
        }

        return index; // return index of the next best guess
    }

    /**
     * generate all possible colour combinations (6 colours and 4 combinations) and
     * store them in remainingCombos and allCombos
     *
     * @param numPositions - int, the nubmer of positions our code has
     * @return nothing
     */

    public void generateAllCombos(int numPositions) {

        // length is calculated in this way as each position (4) has # of colours (6)
        // possiblites
        remainingCombos = new String[(int) Math.pow(Colour.values().length, numPositions)][numPositions];
        int totalCount = 0; // intialize a counter variable for incrementing through remaining combos

        // loop through every colour in all positions in the code (4 positions,
        // therefore 4 for loops)
        for (int i = 0; i < Colour.values().length; i++) {
            for (int j = 0; j < Colour.values().length; j++) {
                for (int k = 0; k < Colour.values().length; k++) {
                    for (int l = 0; l < Colour.values().length; l++) {

                        // assign colour combination their colour
                        remainingCombos[totalCount][0] = Colour.values()[i].toString();
                        remainingCombos[totalCount][1] = Colour.values()[j].toString();
                        remainingCombos[totalCount][2] = Colour.values()[k].toString();
                        remainingCombos[totalCount][3] = Colour.values()[l].toString();

                        totalCount++; // increment counter
                    }
                }

            }
        }

        allCombos = remainingCombos.clone(); // use clone so when remainingCombos changes, it doesn't affect allCombos
    }

    /**
     * prints out the remaining combinations the
     * AI thinks there are. Helpful for consle output and deubgging
     *
     * @param numPositions - int, the nubmer of positions our code has
     * @return nothing
     */

    public void printRemainingCombos(int numPositions) {

        // loop through all codes
        for (int i = 0; i < remainingCombos.length; i++) {
            if (remainingCombos[i] != null) { // if the code is valid / has not been written off
                for (int j = 0; j < numPositions; j++) { // loop through each colour stored

                    System.out.print(remainingCombos[i][j] + " "); // print it out
                }
                System.out.println(" "); // spacing (so the next code is printed on the next line)
            }

        }
    }
}