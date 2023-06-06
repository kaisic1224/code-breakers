//Author: Isaac and Vincent 
//Title: Codebreaker Assignment - Board Class

//Description: 
//This class is responsbile for handling all of the information on the board such as checking 
//the guesses, or how to display the feedback. This primarily includes a lot of public 
//variables and methods that are widely used across all class, especially in GUI

//Date: June 7, 2023
package cb;

//import necessary packages
import java.awt.Color;
import java.util.ArrayList;

public class Board {
    // to store of all the guesses the player or AI makes
    public String[][] board;
    // to store all of the feedback that the AI or player provides
    public String[][] feedback;
    // to store the code that the AI will generate (if player is playing as code
    // breaker)
    public String[] code;

    // constants that are used to define the game by hasbro's specs
    private final int SIZE = 4; // each code is 4 colours long
    private final int TRIES = 10; // maximum number of tries is 10

    public enum Colour {
        BLUE,
        YELLOW,
        ORANGE,
        RED,
        PINK,
        GREEN,
    } // only 6 colours to choose from

    // these constants are used to communicate what letters represent black pegs and
    // white pegs
    private final String BLACKPEG = "b";
    private final String WHITEPEG = "w";

    // constructor
    public Board() {
        // intialize all String arrays according to the dimension of our game
        board = new String[TRIES][SIZE];
        feedback = new String[TRIES][SIZE];
        code = new String[SIZE];
    }

    /**
     * an Accessor / getter method which allows us to access the maximum number of
     * tries the user or AI has (while protecting our data)
     *
     * @param nothing
     * @return returns the value of TRIES as int
     */
    public int getTries() {
        return TRIES;
    }

    /**
     * an Accessor / getter method which allows us to access the length of the code
     *
     * @param nothing
     * @return returns the value of SIZE as int
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * an Accessor / getter method which allows us to access the random code that
     * the AI generated
     * 
     * @param nothing
     * @return returns the value of code as a String []
     */
    public String[] getCode() {
        return code;
    }

    /**
     * a method which prints out the code that our AI has generated in the console.
     * This is helpful if you cannot get the code the AI set and want to win on the
     * spot / for debugging
     * 
     * @param nothing
     * @return nothing
     */
    public void printCode() {
        // iterate through all elements in array code
        for (int i = 0; i < code.length; i++) {
            System.out.print(code[i] + " "); // print each one out side by side
        }
        System.out.println();// move to the next line
    }

    /**
     * a method which generates a random code that has 4 positions and chooses from
     * 6 random colours. The randomly generated code is assigned to String [] code
     * 
     * @param nothing
     * @return nothing
     */
    public void generateCode() {
        for (int i = 0; i < code.length; i++) { // iterate through all positions in the code
            int randIndex = (int) (Math.random() * Colour.values().length); // get a random index from our random list
                                                                            // of colour values
            code[i] = Colour.values()[randIndex].name(); // assign the colour based on the randomly chosen index from
                                                         // colour list
        }

    }

    /**
     * a method which compares two strings (one guess, one solution) and returns the
     * number of black pegs (right colour right position) and white pegs (right
     * colour wrong position) there are.
     * 
     * @param guess - String[], represents the player's or AI's guess
     *              solution - String[], represents a code we want to compare guess
     *              against
     * 
     * @return feedback - Arraylist that is converted into a String[], represents
     *         the feedback guess provides when cmopared to solution
     */
    public String[] checkGuess(String[] guess, String[] solution) {

        String[] temporaryCode = solution.clone(); // create a temporary version of our solution we can modify and edit
        String[] evaluation = new String[solution.length]; // intialize evaluation (store blacks and whites here)

        // detect all correct position, correct colour
        for (int i = 0; i < guess.length; i++) {
            if (guess[i].equals(temporaryCode[i])) {
                evaluation[i] = BLACKPEG;
                temporaryCode[i] = null; // write of as null so a colour doesn't provide hints more than once
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
                        evaluation[i] = WHITEPEG; // add to feedback

                        temporaryCode[j] = null; // invalidate colour so cannot be detected on next scan through
                        break;
                    }
                }
            }

        }

        // remove all nulls
        ArrayList<String> feedback = new ArrayList<String>(); // intialize an arraylist. We will store all non null
                                                              // values in here
        for (int i = 0; i < evaluation.length; i++) {
            if (evaluation[i] != null) {
                feedback.add(evaluation[i]); // add only colours to arraylist
            }
        }

        String[] feedbackToArray = new String[feedback.size()]; // intialize an array the same size as our feedback

        return feedback.toArray(feedbackToArray); // convert the arraylist into a String[]
    }

    /**
     * a method which reads the evaluation returned by checkGuess and counts up the
     * number of black and white pegs. Returns the value as an int[] (so we can
     * store all the feedback in one place)
     * 
     * @param evaluation - String [], represents the feedback that the player or AI
     *                   receives from checkGuess (i.e in the format of something
     *                   like b,b,w,w)
     * 
     * @return pegHolder - int[], represents the number of black and white pegs
     *         evaluation has. [1] represents black pegs and [0] represents white
     *         pegs
     */
    public int[] returnPegs(String[] evaluation) {
        int[] pegHolder = new int[2]; // where 1 = black and 0 = white;

        for (int i = 0; i < evaluation.length; i++) { // iterate through all positions in evaluation
            if (evaluation[i].equals(WHITEPEG)) { // if it is a white peg
                pegHolder[0]++; // increment white peg counter
            } else if (evaluation[i].equals(BLACKPEG)) { // if it is ia black paeg
                pegHolder[1]++; // incremenet black peg counter
            }
        }

        return pegHolder; // return pegHolder, now storing number of blacks and whites as integers

    }

    /**
     * a method which converts a colour into it's string named based on its RGB.
     * This allows us to store colours into String arrays in a more readable format
     * that is easier to debug and analyze
     * 
     * @param color - Color, the color we want to convert into a name
     * 
     * @return the name of the color an RGB code represents
     */
    public String colorToString(Color color) {
        switch (color.toString()) { // use a case switch to iterate through all possible colours
            case "java.awt.Color[r=0,g=255,b=0]": // if RGB correlates with green
                return "GREEN";
            case "java.awt.Color[r=255,g=255,b=0]": // if RGB correlates with yellow
                return "YELLOW";
            case "java.awt.Color[r=0,g=0,b=255]": // if RGB correlates with blue
                return "BLUE";
            case "java.awt.Color[r=255,g=200,b=0]": // if RGB correlates with orange
                return "ORANGE";
            case "java.awt.Color[r=255,g=0,b=0]": // if RGB correlates with red
                return "RED";
            case "java.awt.Color[r=255,g=175,b=175]": // if RGB correlates with pink
                return "PINK";
            default:
                return "INVALID COLOUR"; // catch any invalid colours that may be accidently passed through
        }
    }

}
