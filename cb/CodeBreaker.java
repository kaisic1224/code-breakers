//Author: Isaac and Vincent 
//Title: Codebreaker Assignment - CodeBreaker class

//Description: 
//This class organizes all the functions from AICodebreaker class and Board class to create gameplay. 
//Our program has two required modes: Play against an AI as a code breaker or play against the AI as a code setter.
//Some of the other features it handles includes but is not limited to:
//   - tutorial
//   - self test all cases (live progress bar animation)
//   - leaderboards and accounts
//   - sound effects
//The codebreaker class also handles on GUI and user interactions

//Date: June 7, 2023

package cb;

//import necessary packages for handling sound, colours and GUI
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import cb.Board.Colour;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//class extends to JFrame and implements Action Listener
public class CodeBreaker extends JFrame implements ActionListener {
    // ASSETS: any component that we will use reptively throughout the entire
    // program

    // colours
    final static Color BGCOLOURORANGE = new Color(255, 190, 121);
    final static Color BGCOLOURBEIGE = new Color(211, 182, 156);
    final static Color BUTTONCOLOUR = new Color(147, 202, 237);

    // fonts
    static Font ForeverFontBold;
    static Font ForeverFontTitle;
    static Font ForeverFontNormal;
    static Font ForeverFont;

    // sounds
    final static String POPPATH = "./cb/assets/sounds/pop.wav";
    final static String BGMUSICPATH = "./cb/assets/sounds/bgMusic.wav";

    // panel formatting / spacing
    final static Dimension MENUBUTTONSIZE = new Dimension(500, 100);
    final static int MENUBUTTONSPACING = 30;

    // OBJECTS: so we can instantiate necessary classes for the program to function
    static Board board;
    static AICodeBreaker AI;
    public static PrintWriter myWriter;

    // PROGRAM: variables which will be used to store game data

    // an array which lists the possible colours for feedback (black or white pegs)
    static Color[] feedbackColours = { Color.black, Color.white };
    // an array which stores the feedback the player gives to the AI
    static String[] playerFeedback;

    // counters
    static int numColoursSelected = 0; // keeps track of the number of colours the player selected
    static int attempts = 0; // keeps track of the number of attempts the user has taken so far

    // GUI: all of the panels, textfields, and frames we add our GUI components to
    public static JPanel boardPanel;
    public static JPanel feedbackPanel;
    public static JPanel colourPicker;
    public static JPanel displayColours;
    public static JTextField name;
    public static JFrame gameFrame;

    // account logging in
    public static String sessionName;

    public static void LoadAssets() {
        File records = new File("./cb/accounts/records.txt");
        if (!records.exists()) {
            try {
                records.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            ForeverFont = Font.createFont(Font.TRUETYPE_FONT, new File("./cb/assets/Forever.ttf"));
        } catch (Exception e) {
            System.out.println("File could not be found, or error parsing font");
        }
        ForeverFontBold = ForeverFont.deriveFont(Font.BOLD, 16f);
        ForeverFontNormal = ForeverFont.deriveFont(16f);
        ForeverFontTitle = ForeverFont.deriveFont(Font.BOLD, 50f);
    }

    /**
     * Displays a tutorial, which provides the users instructions as to how to play
     * and the rules of the games
     *
     * @param frame - JFrame, the frame we will be adding our panels to (so
     *              we can display GUI added to panels)
     * @return nothing
     */
    public static void Tutorial(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // make sure that the frame takes up the entire screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(); // intialize a new panel with a vertical box layout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TUTORIAL"); // header
        title.setFont(ForeverFontTitle); // give the header a title font

        // instrutions, using breaks to seperate text into lines for better readability
        JLabel instructions = new JLabel("<html>"
                + " Welcome to one of Hasbro's classics, CODEBREAKER! " + "<br />" + "<br />" +
                " RULES: " + "<br />" +
                "In codebreaker, a random code (consisting of 6 colours with a code length of 4) is set by the code setter"
                + "<br />" +
                "The code breaker gets a maximum of 10 tries to guess the code and is given feedback in the form of black and white pegs."
                + "<br />" +
                "     -  A black peg means the guess has a right colour in the right positon"
                + "<br />" +
                "     -  A white peg mans the guess has a right colour in the wrong position"
                + "<br />" +
                "The game is over when 4 black pegs are given (i.e all the colours are in the right position compared to the code)"
                + "<br />" + "<br />" +
                " GAME OPTIONS: " + "<br />" +
                "1. play as CODEBREAKER: user breaks the code and AI sets the code" + "<br />" +
                "2. play as CODESETTER: user sets the code and AI breaks the code" + "<br />" +
                "3. test all cases: have the AI attempt to break all possible codes and display the distribution of attempts it takes"
                + "<br />" + "<br />" +
                "Good luck and have fun!"
                + "</html>");
        instructions.setFont(ForeverFontNormal); // set the instructions font

        // create a button so the user can return back to the menu
        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold); // set the font of the button
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { // when the button is clicked
                music(false, POPPATH); // play button sound effects
                new CodeBreaker(); // call constructor which loads the main menu
                gameFrame.setVisible(false); // hide current frame
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR); // button background colour

        mainPanel.add(Box.createVerticalGlue()); // ensures that our text is centered vertically
        // add our title, instructions, and back to menu button in such order
        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING))); // create spacing between our
                                                                                 // componenets
        mainPanel.add(instructions);
        mainPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        mainPanel.add(backToMenu);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.setBackground(BGCOLOURORANGE); // set the panel background

        frame.add(mainPanel); // and the panel to our frame
        frame.setVisible(true); // make our frame visible
    }

    /**
     * Tests the AI against all possible combinations and displays the distrbutions
     * of attempts. Helpful for analyzing the AI's peformances. All of the AI's
     * attempts against all cases are stored into data.txt.
     *
     * @param frame - JFrame, the frame we will be adding our panels to (so
     *              we can display GUI added to panels)
     * @return nothing
     */
    public void AIstats(JFrame frame) {

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // make sure that the frame takes up the entire screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // intialize the board and AI
        board = new Board();
        AICodeBreaker AI = new AICodeBreaker();

        // generate all combinations, (pass in board.getSize() so the function knows how
        // many positions in the code there are)
        AI.generateAllCombos(board.getSize());

        // loading panel page with a vertical layout
        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));

        // progress bar to display the nubmer of cases the AI has solved so far as a
        // percentage
        JProgressBar progressBar = new JProgressBar(0, AI.getAllCombos().length);
        progressBar.setValue(0); // initialize the progress bar to start at 0
        progressBar.setStringPainted(true); // display the percentage
        progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // borders
        progressBar.setForeground(BUTTONCOLOUR); // colour of the progress bar
        progressBar.setMaximumSize(new Dimension(750, 30)); // restran the size of the progress bar

        JLabel header = new JLabel("TESTING ALL CASES..."); // a header, to let the user know the AI is still testing
                                                            // cases

        // set the headers font and alignment
        header.setFont(ForeverFontTitle);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        // the header and progress bar to the loading panel
        loadPanel.add(Box.createVerticalGlue());
        loadPanel.add(header);
        loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING))); // spacing between progress bar and
                                                                                 // header
        loadPanel.add(progressBar);
        loadPanel.add(Box.createVerticalGlue()); // vertical glue to ensure it is vertically centered
        loadPanel.setBackground(Color.white); // background to be just white (easy to read)

        frame.add(loadPanel); // add the loading panel to the frame
        frame.setVisible(true); // frame is now visible

        // start a new thread. This allows us to run code and udpate the GUI at the same
        // time
        Thread t = new Thread(new Runnable() {
            public void run() {

                try { // try catch to handle file errors

                    myWriter = new PrintWriter("data.txt"); // intialize file writer

                    // intialize all variables associated with file testing
                    int totalAttempts = 0; // counter, for total attempts and calculating the average
                    int[] attemptsArray = new int[AI.getAllCombos().length]; // attempts for each case
                    int[] attemptsSort = new int[7]; // record the distrbution of attempts taken

                    for (int i = 0; i < AI.getAllCombos().length; i++) { // for every single combination

                        AI.setRemainingCombos(AI.getAllCombos().clone()); // reset remaining combinations

                        // reset counters associated with gameplay
                        int attempts = 1;

                        // feedback counters (-1,-1 so AI knows it is on the first guess)
                        int blacks = -1;
                        int whites = -1;

                        do {
                            String[] code = AI.guessCombo(blacks, whites); // have the AI guess the code
                            saveArray(code); // write it to file
                            String[] feedback = board.checkGuess(code, AI.getAllCombos()[i]); // get the feedback based
                                                                                              // on the guess and the
                                                                                              // current solution
                            saveArray(feedback); // save feedback
                            int[] pegHolder = board.returnPegs(feedback);
                            // determine the number of black and white pegs (feedback)
                            blacks = pegHolder[1];
                            whites = pegHolder[0];

                            if (blacks != board.getSize()) { // if the guess is not right, increment the number of
                                                             // attempts
                                attempts++;
                            }

                        } while (blacks != board.getSize()); // while the AI has not guessed the code

                        totalAttempts += attempts; // increment counter total attempts

                        // save down what combination the AI tried to back and the number of attempts it
                        // tooks
                        String msg = "CASE " + (i + 1) + " : " + attempts;
                        myWriter.write(msg);
                        myWriter.write(System.lineSeparator());
                        myWriter.write("----------------------------------");
                        myWriter.write(System.lineSeparator()); // for good formating in the file

                        attemptsArray[i] = attempts; // store our attempts
                        progressBar.setValue(i); // update the progress bar value
                    }

                    // caculate average, leaving everything as a float so average is as accurate as
                    // possible
                    float avg = (float) totalAttempts / (float) AI.getAllCombos().length;

                    // to store the maximum number of attempts the AI will take
                    int worstCase = 0;

                    // loop through all attempts the AI took for all
                    // cases
                    for (int i = 0; i < attemptsArray.length; i++) {
                        // count up distribution attempts
                        if (attemptsArray[i] == 1) {
                            attemptsSort[0]++;
                        } else if (attemptsArray[i] == 2) {
                            attemptsSort[1]++;
                        } else if (attemptsArray[i] == 3) {
                            attemptsSort[2]++;
                        } else if (attemptsArray[i] == 4) {
                            attemptsSort[3]++;
                        } else if (attemptsArray[i] == 5) {
                            attemptsSort[4]++;
                        } else if (attemptsArray[i] == 6) {
                            attemptsSort[5]++;
                        } else if (attemptsArray[i] > 6) {
                            attemptsSort[6]++;

                        }

                        // if the current number of attempts is greater than the current worst case,
                        // update it
                        if (attemptsArray[i] > worstCase) {
                            worstCase = attemptsArray[i];
                        }

                    }

                    // update the loading panel after all cases have been tested
                    loadPanel.removeAll();
                    loadPanel.add(Box.createVerticalGlue()); // ensuring GUI is vertically centered

                    // title page with title font and centered alignment
                    JLabel header = new JLabel("DISTRIBUTION OF ATTEMPTS REQUIRED TO SOLVE ALL CASES");
                    header.setFont(ForeverFont.deriveFont(Font.BOLD, 25f));
                    header.setAlignmentX(Component.CENTER_ALIGNMENT);
                    loadPanel.add(header); // add to panel
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING))); // spacing between GUI
                                                                                             // components

                    for (int i = 0; i < attemptsSort.length; i++) { // looping through the distribution of attempts

                        JLabel attemptResult;

                        // note that the index = the number of attempts the AI takes and the element
                        // stored at that index represents the number of times the AI needed x attempts
                        // to break a code combination
                        if (i == attemptsSort.length - 1) {
                            attemptResult = new JLabel("7+ ATTEMPTS: " + attemptsSort[i]);
                        } else {
                            attemptResult = new JLabel((i + 1) + " ATTEMPTS: " + attemptsSort[i]);
                        }

                        // set the UI for the attempts result and center the alignment
                        attemptResult.setFont(ForeverFontNormal);
                        attemptResult.setAlignmentX(Component.CENTER_ALIGNMENT);
                        loadPanel.add(attemptResult); // add to panel
                        loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING / 2))); // create a bit of
                                                                                                     // spacing between
                                                                                                     // stats
                    }

                    // for displaying the average attempts the AI takes
                    JLabel average = new JLabel("AVERAGE ATTEMPTS: " + (float) avg); // we want the unrounded float
                    // set font & center alignment
                    average.setFont(ForeverFontNormal);
                    average.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // for displaying the maximum number of attempts the AI will take
                    JLabel worseCase = new JLabel("WORST CASE: " + worstCase);
                    // set font, colour & center alignment
                    worseCase.setFont(ForeverFontNormal);
                    worseCase.setAlignmentX(Component.CENTER_ALIGNMENT);
                    worseCase.setForeground(Color.red);

                    // display where our client can find the data to our AI stats for their analysis
                    JLabel reminder = new JLabel(
                            "All test cases are saved in data.txt for your analysis");

                    // create an italicized font and center the alignment
                    reminder.setFont(ForeverFont.deriveFont(Font.ITALIC, 10f));
                    reminder.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // add remaining components (average, display worst case, and reminder as to
                    // where to find the data) in said order. Adding spacing in between with
                    // rigidArea
                    loadPanel.add(average);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
                    loadPanel.add(worseCase);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING / 2)));
                    loadPanel.add(reminder);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));

                    // button for navigating back to to the menu
                    JButton backToMenu = new JButton("Back to Menu");
                    backToMenu.setFont(ForeverFontBold); // set fount
                    backToMenu.setHorizontalTextPosition(JButton.LEFT); // centering button text
                    backToMenu.setBackground(BUTTONCOLOUR); // button background to typical button background
                    backToMenu.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            music(false, POPPATH); // button sound effect

                            new CodeBreaker(); // call constructor to load the main page
                            gameFrame.setVisible(false); // hide current frame
                        }
                    });
                    backToMenu.setAlignmentX(Component.CENTER_ALIGNMENT); // align button to center

                    loadPanel.add(backToMenu); // add the button to the panel
                    loadPanel.add(Box.createVerticalGlue());

                    // referesh the loading panel with the new stats
                    loadPanel.revalidate();
                    loadPanel.repaint();

                } catch (Exception e) {
                    e.printStackTrace(); // catch unexpected errors
                }

                myWriter.close(); // close file writer

            }

        });

        t.start(); // start thread

    }

    /**
     * Handles and oragnizes all the gameplay in our program (player is code
     * breaker, player is code setter) using buttons. Updates GUI.
     *
     * @param frame         - JFrame, the frame we will be adding our panels to (so
     *                      we can display GUI added to panels)
     * @param isCodeBreaker - boolean, determines wheter or not the player is a code
     *                      breaker
     * @return nothing
     */
    public static void Game(JFrame frame, boolean isCodeBreaker) {

        // reset counters used in case the user goes to the menu and plays again
        attempts = 0;
        numColoursSelected = 0;
        playerFeedback = new String[board.getTries()];

        // intialize frame layout and components
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // fit to screen
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close programs when frame is closed

        // intialize all panel layouts
        JPanel mainPanel = new JPanel(new GridBagLayout());
        boardPanel = new JPanel(new GridLayout(board.getTries(), board.getSize())); // therefore, it is the same size as
                                                                                    // our board array
        boardPanel.setBorder(new EmptyBorder(0, 0, 0, 94));
        feedbackPanel = new JPanel(new GridLayout(board.getTries(), board.getSize())); // therefore, it is the same size
                                                                                       // as our feedbcak array

        // a header displaying the remaining guesses the user has
        JLabel currentRound = new JLabel();
        currentRound.setText("Guesses Left = " + (board.getTries() - attempts)); // maxAttempts - attempts as we wnat to
                                                                                 // display guesses left
        currentRound.setFont(ForeverFontNormal); // set to a default font

        // create a board.getTries x board.getSize (4x10) grid to intialize the grid
        // used to display all the guesses the AI or player makes
        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                JLabel cell = new JLabel(""); // intialize jLabel
                cell.setPreferredSize(new Dimension(50, 50)); // default size
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // borders
                cell.setOpaque(true); // show background colour, beige
                cell.setBackground(BGCOLOURBEIGE);
                boardPanel.add(cell); // add to board panel
            }
        }

        // create a board.getTries x board.getSize (4x10) grid to intialize the gird
        // used to display all the feedback the AI or player provides
        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                // cells will mirror the ones we created previously under the board panel
                JLabel cell = new JLabel();
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setBackground(BGCOLOURBEIGE);
                feedbackPanel.add(cell); // add to the feedback panel instead!
            }
        }

        colourPicker = new JPanel(new FlowLayout()); // colour picker stores the colours we can choose from
        displayColours = new JPanel(new FlowLayout()); // display colour displays the colours we have selected

        JButton clearAll = new JButton("Clear all"); // allows us to clear our selection
        JButton submit = new JButton("Submit"); // allows us to submit our selection
        clearAll.setBackground(BUTTONCOLOUR); // default button colours
        submit.setBackground(BUTTONCOLOUR);

        clearAll.setEnabled(false); // enabled to false -> only let the user clear when they have selected some
                                    // colours

        if (isCodeBreaker) { // if the player is the code breaker

            // generate a random code and print int out (for the users reference)
            board.generateCode();
            board.printCode();

            // for every single possible colour the user can pick from
            for (int i = 0; i < Colour.values().length; i++) {

                // create a series of buttons the same size as the cells we added to board /
                // feedback panel
                JButton peg = new JButton("");
                peg.setPreferredSize(new Dimension(50, 50));
                peg.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                peg.setOpaque(true);

                // background colour of the peg = the current colour value in the foor loop
                Color c = null;
                try {
                    c = (Color) Color.class.getField(Colour.values()[i].toString().toUpperCase()).get(null);
                    peg.setBackground(c); // set background colour
                } catch (Exception e) {
                    e.printStackTrace(); // catch unexpected errors
                }

                // each peg has an action listner
                peg.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        music(false, POPPATH); // play a sound effect

                        if (numColoursSelected < board.getSize()) { // if we have not selected more than 4 colours
                            clearAll.setEnabled(true); // allow us to clear all colours
                            numColoursSelected++; // increemnet

                            // add a new colour to display colours (same formating as all cells)
                            JLabel code = new JLabel("");
                            code.setPreferredSize(new Dimension(50, 50));
                            code.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            code.setOpaque(true);
                            code.setBackground(peg.getBackground()); // same bg colours as the peg we selected

                            // add the peg we selected to displayColours panel
                            displayColours.add(code);

                            // repaint and update it so we can see the changes
                            displayColours.revalidate();
                            displayColours.repaint();

                        }

                        // if we hav selected 4 colours, allow the user to submit
                        if (numColoursSelected == board.getSize()) {
                            submit.setEnabled(true);
                        }
                    }
                });
                colourPicker.add(peg); // add the peg to the colour picker panel
            }

            // add an action listener to the submit button
            submit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    music(false, POPPATH); // sound effect

                    Component[] guessColours = displayColours.getComponents(); // get all components stored inside of
                                                                               // the display Colours panel
                    String[] colors = new String[4];

                    // convert the background colours of the labels in display colour into a string.
                    // The player's guess is now stored in colors
                    for (int i = 0; i < guessColours.length; i++) {
                        colors[i] = board.colorToString(guessColours[i].getBackground());
                    }

                    // compare the current guess with the code the AI generated
                    String[] evaluation = board.checkGuess(colors, board.getCode());
                    int[] pegs = board.returnPegs(evaluation); // get number of blacks and whites

                    String[] feedback = new String[4]; // store the feedback in an array so we can add it to
                                                       // board.feedback

                    // we want to ensure that the feedback we are giving is an organized order
                    // (whites first, then blacks). As per the rules of the game, each feedback peg
                    // doesn't correlate with a positionin the player's guesss.
                    for (int i = 0; i < pegs[0]; i++) {
                        feedback[i] = Color.white.toString();
                    }
                    for (int i = 0; i < pegs[1]; i++) {
                        int index = pegs[0] + i;
                        feedback[index] = Color.black.toString();
                    }

                    // add feedback to the feedback history & update the GUI
                    board.feedback[board.getTries() - 1 - attempts] = feedback; // board.getTries() - 1 - attempts so we
                                                                                // go from bottom to top rather than top
                                                                                // to bottom
                    revalidateFeedback();

                    // add the guess to the guess history & update the GUI
                    board.board[board.getTries() - 1 - attempts] = colors;
                    revalidateBoard();

                    attempts++; // increment attmpets taken

                    // pass feedback to the AI to see if the game is over
                    playerIsCodeBreaker(pegs[0], pegs[1], board.getCode());

                    // clear the display colours section (as we just submitted a guess)
                    numColoursSelected = 0;
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    // enabled to false as we have nothing selected anymore
                    submit.setEnabled(false);
                    clearAll.setEnabled(false);

                    // update number of guesses left with remaining attempts
                    currentRound.setText("Guesses Left = " + (board.getTries() - attempts));
                }
            });

        } else {
            AI = new AICodeBreaker(); // create a new AI object and generate all possible combinations
            AI.generateAllCombos(board.getSize());

            // for every single possible feedback colour (2)
            for (int i = 0; i < feedbackColours.length; i++) {

                // create a button for it, with normal cell properties
                JButton feedback = new JButton("");
                feedback.setPreferredSize(new Dimension(50, 50));
                feedback.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                feedback.setOpaque(true);
                feedback.setBackground(feedbackColours[i]); // two options for feedback: black or white

                // add action listener to record the feedback player provides as code setter
                feedback.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        music(false, POPPATH); // sound effect

                        if (numColoursSelected < board.getSize()) { // if the number of feedback items selected is less
                                                                    // than the code length

                            // create a jLabel to display the feedback colours that have been selected
                            JLabel selectedColour = new JLabel("");
                            selectedColour.setPreferredSize(new Dimension(30, 30));
                            selectedColour.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            selectedColour.setOpaque(true);
                            selectedColour.setBackground(feedback.getBackground()); // use get background so colour peg
                                                                                    // user selects is the same as the
                                                                                    // one that is now displayed!
                            displayColours.add(selectedColour); // add to panel

                            // update the UI so we now can see the pegs we have chosen
                            boardPanel.revalidate();
                            boardPanel.repaint();

                            // store the pegs we have chosen into an array
                            playerFeedback[numColoursSelected] = feedback.getBackground().toString();

                            numColoursSelected++; // increment number of colours

                            // as we have at least one peg, allow the user to clear or submit
                            clearAll.setEnabled(true);
                            submit.setEnabled(true);
                        }

                    }
                });

                colourPicker.add(feedback); // add the peg chooser into the colourPicker panel
            }

            // action listner for submitting feedback
            submit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    music(false, POPPATH); // sound effect

                    // reset all the GUI and counters
                    clearAll.setEnabled(false);
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    numColoursSelected = 0;

                    // use clone or else when we assign as PBR!
                    board.feedback[board.getTries() - 1 - attempts] = playerFeedback.clone();
                    revalidateFeedback(); // update feedback GUI

                    // counter for blacks and whites
                    int blacks = 0;
                    int whites = 0;

                    // count up the number of black or white pegs while resetting values in
                    // playerFeedback to null so it can be reused later
                    for (int i = 0; i < playerFeedback.length; i++) {
                        if (playerFeedback[i] != null) {
                            if (feedbackColours[0].toString().equals(playerFeedback[i])) {
                                blacks++;
                            } else {
                                whites++;
                            }

                            playerFeedback[i] = null;
                        }
                    }

                    attempts++; // incremenet attempts

                    playerIsCodeSetter(blacks, whites, AI.getLastGuess()); // see if the game is over

                    currentRound.setText("Guesses Left = " + (board.getTries() - attempts)); // update number of
                                                                                             // attempts left
                }

            });
        }

        // a function to clear all colours user selected
        clearAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH); // button sound effect

                // disable buttons as no more colours to clear or submit
                clearAll.setEnabled(false);
                submit.setEnabled(false);
                playerFeedback = new String[board.getTries()]; // reset player feedback

                // update the display colours GUI
                displayColours.removeAll();
                displayColours.revalidate();
                displayColours.repaint();
                numColoursSelected = 0; // reset colours selected
            }
        });
        // add the buttons to the panel
        colourPicker.add(clearAll);
        colourPicker.add(submit);

        // managing the panel layout with grid bag constraints
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; // 1st row
        frame.add(currentRound, c); // prioritize the number of guesses header first

        mainPanel.add(boardPanel);
        mainPanel.add(feedbackPanel);
        c.gridy = 1; // take the second row in the layout
        c.fill = GridBagConstraints.HORIZONTAL; // fill in width
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        frame.add(mainPanel, c);

        c.gridy = 2; // 3rd row goes to colour picker
        frame.add(colourPicker, c);

        c.gridy = 3; // colours users picked displayed at the bottom
        frame.add(displayColours, c);
        frame.setVisible(true); // frame is visible to user
    }

    public static void userLogin(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel loginPanel = new JPanel();
        loginPanel.add(Box.createVerticalGlue());

        JPanel usernamePanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("Username: ");
        JTextField username = new JTextField("", 20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(username);
        loginPanel.add(usernamePanel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

        JPanel passwordPanel = new JPanel(new FlowLayout());
        JLabel passwordLabel = new JLabel("Password: ");
        JTextField password = new JTextField("", 20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(password);
        loginPanel.add(passwordPanel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

        JButton submitLogin = new JButton("Login");
        JLabel errorText = new JLabel("");
        submitLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String verified = null;
                try {
                    verified = login(username.getText(), password.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorText.setText(ex.getMessage());
                }
                if (verified.equals(username.getText())) {
                    frame.setVisible(false);
                    sessionName = verified;
                    new CodeBreaker();
                }
            }
        });
        loginPanel.add(submitLogin);
        loginPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

        loginPanel.add(errorText);

        frame.add(loginPanel);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Constructor in our class (one of the first methods that are called). Sets up
     * our game through a main menu. In this main menu, the user can now navigate
     * through our program's features
     *
     * @param nothing
     * @return nothing
     */
    public CodeBreaker() {
        // ensure code stops when frame closes
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame takes up entire screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("CodeBreakers | Main Menu"); // title of our frame

        // main panel - serves as the container where we will add all other panels
        JPanel mainPanel = new JPanel(new GridBagLayout());

        // displaying an image inside of the hero panel
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        Image logo = new ImageIcon("./cb/assets/logo.png").getImage();
        JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_FAST)),
                JLabel.CENTER);
        hero.add(heroLogo); // add to panel

        // buttons to navigate game components
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS)); // vertical layout
        buttons.setBorder(new EmptyBorder(0, 50, 0, 0));

        // arrow image (added onto the buttons in the menu)
        Image arrowIcon = new ImageIcon("./cb/assets/arrow.png").getImage();
        ImageIcon arrowImg = new ImageIcon(arrowIcon.getScaledInstance(25, 25, Image.SCALE_FAST));

        // OPTION 1: PLAYER PLAYS AS THE CODE BREAKER
        JButton aiPlay = new JButton("play as CODE BREAKER", arrowImg); // intialize button with arrow size
        aiPlay.setBackground(BUTTONCOLOUR); // background to deafult background under assets
        aiPlay.setFont(ForeverFontBold); // bolded font
        aiPlay.setHorizontalTextPosition(JButton.LEFT); // position text to the left (text lines up across all buttons)
        aiPlay.setMaximumSize(MENUBUTTONSIZE); // cap the size

        aiPlay.addActionListener(new ActionListener() { // if we press this button...
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH); // play sound effect

                // create anew frame, indicating the player is the code breaker
                gameFrame = new JFrame("Code Breakers | Player is code breaker");
                board = new Board(); // intalize board
                Game(gameFrame, true); // codebreaker is set to true
                setVisible(false); // destory the current frame
            }
        });

        // OPTION 2: PLAYER PLAYS AS THE CODE SETTER

        // intialize the button, using the same format as the previous button (our menu
        // will now have a uniform look)
        JButton personPlay = new JButton("play as CODE SETTER", arrowImg);
        personPlay.setBackground(BUTTONCOLOUR);
        personPlay.setFont(ForeverFontBold);
        personPlay.setHorizontalTextPosition(JButton.LEFT);
        personPlay.setMaximumSize(MENUBUTTONSIZE);

        personPlay.addActionListener(new ActionListener() { // if we press this button
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH); // sound effect

                // create a frame which lets the player know they are now the code setter
                gameFrame = new JFrame("Code Breakers | Player is code setter");
                board = new Board();
                Game(gameFrame, false); // player is codeBreaker is false!

                playerIsCodeSetter(-1, -1, AI.getLastGuess()); // intailzie the AI's first guess with (-1,-1)
                setVisible(false); // hide current frame
            }
        });

        // OPTION 3: PLAY THE TUTORIAL

        // intialize the button, using the same format as the previous button
        JButton tutorial = new JButton("TUTORIAL", arrowImg);
        tutorial.setBackground(BUTTONCOLOUR);
        tutorial.setFont(ForeverFontBold);
        tutorial.setHorizontalTextPosition(JButton.LEFT);
        tutorial.setMaximumSize(MENUBUTTONSIZE);

        tutorial.addActionListener(new ActionListener() { // if the player presses this button
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH); // sound effect

                gameFrame = new JFrame("Code Breakers | Tutorial"); // let the player know they have navigated to the
                                                                    // tutorial in frame title
                Tutorial(gameFrame); // load the tutorial function
                setVisible(false); // hide current frame
            }
        });

        // OPTION 4: GET AI STATS

        // intialize the button, using the same format as the previous button
        JButton aiStats = new JButton("AI STATS", arrowImg);
        aiStats.setBackground(BUTTONCOLOUR);
        aiStats.setFont(ForeverFontBold);
        aiStats.setHorizontalTextPosition(JButton.LEFT);
        aiStats.setMaximumSize(MENUBUTTONSIZE);

        aiStats.addActionListener(new ActionListener() { // if we press this button
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH); // sound effect

                gameFrame = new JFrame("Code Breakers | Stats"); // navigated to stats frame
                AIstats(gameFrame); // call the AIstats function which will add the right panels to gameFrame
                setVisible(false); // hide current frame
            }
        });

        // OPTION 4: LOGIN

        // intialize the button, using the same format as the previous button
        JButton login = new JButton("LOGIN", arrowImg);
        login.setBackground(BUTTONCOLOUR);
        login.setFont(ForeverFontBold);
        login.setHorizontalTextPosition(JButton.LEFT);
        login.setMaximumSize(MENUBUTTONSIZE);

        login.addActionListener(new ActionListener() { // if we press this button
            public void actionPerformed(ActionEvent e) {
                JFrame loginFrame = new JFrame("Code Breakers | Login"); // load up the login page
                userLogin(loginFrame); // call userLogin to display login GUI
                setVisible(false); // hide current frame
            }
        });

        // add to buttons panel in the correct order, using Box.createRigidArea to space
        // them out by a pre-defined amount
        JLabel session = new JLabel();
        if (sessionName == null) {
            session.setText("Not currently logged in");
        } else {
            session.setText("Currently logged in as: " + sessionName);
        }

        // add to buttons panel
        buttons.add(session);
        buttons.add(aiPlay);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(personPlay);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(tutorial);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(aiStats);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(login);

        // set backgorund colours for each panel
        hero.setBackground(BGCOLOURORANGE);
        mainPanel.setBackground(BGCOLOURORANGE);
        buttons.setBackground(BGCOLOURORANGE);

        // add to main frame
        mainPanel.add(hero);
        mainPanel.add(buttons);
        getContentPane().add(mainPanel);
        setVisible(true); // frame is visible to suer

    }

    /**
     * The main method serves as an entry point for executing a Java program
     *
     * @param args - String[], stores java command line arguments
     * @return nothing
     */
    public static void main(String[] args) {

        LoadAssets(); // load all assets
        new CodeBreaker(); // call our constructor
        music(true, BGMUSICPATH); // start the background music, which loops

    }

    /**
     * Here, the player is playing as the code breaker and the AI, as a code setter.
     * This function forces the AI to keep providing feedback and the player to keep
     * making guesses until one of them wins / loses
     *
     * @param blacks - int, number of black pegs that the AI gives to the user
     * @param whites - int, number of white pegs that the AI gives to the user
     * @param code   - String [], represents the last code that the player tries to
     *               guess
     * 
     * @return nothing
     */
    public static void playerIsCodeBreaker(int whites, int blacks, String[] code) {

        if (blacks == board.getSize()) { // if black pegs equals to the code length (code is guessed right!)
            finalMessage("YOU WIN in " + (attempts) + " guesses! ", code); // display a win message
        } else if (attempts == board.getTries()) { // if the user runs out of tries
            finalMessage("YOU LOSE! (no more attempts) ", code); // display a lose message
        }
    }

    /**
     * Here, the player is playing as the code setter and the AI, as a code breaker.
     * This function forces the player to keep providing feedback and the AI to keep
     * making guesses until one of them wins / loses
     *
     * @param blacks - int, number of black pegs that the user gives to the AI
     * @param whites - int, number of white pegs that the user gives to the AI
     * @param code   - String [], represents the last code that the AI tries to
     *               guess
     * 
     * @return nothing
     */
    public static void playerIsCodeSetter(int blacks, int whites, String[] code) {

        if (blacks == board.getSize()) { // if black pegs equals to the code length (code is guessed right!)
            finalMessage("AI WINS in " + (attempts) + " guesses!", code); // display a win message
        } else {
            String[] guess = AI.guessCombo(blacks, whites); // make a new guess, using the players feedback
            if (guess == null || attempts == board.getTries()) { // if the AI belives there are no other possible combos
                                                                 // or it takes more than 10 tries
                finalMessage("You have to be wrong!!", null); // we know our AI is right :)
            } else {
                board.board[board.getTries() - 1 - attempts] = guess.clone(); // update the board with the new guess
                revalidateBoard(); // refresh the board GUI
            }

        }

    }

    /**
     * this function refreshes the board panel, and updates it with new data
     * inputed to board.board, which tracks all the guesses the AI or player
     * has made throughout the game
     *
     * @param nothing
     * @return nothing
     */
    public static void revalidateBoard() {
        boardPanel.removeAll(); // remove all cells inside the board panel

        // loop through board.board
        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                String colourName = board.board[i][j]; // get the colour in that index
                JLabel cell = new JLabel(""); // intialize the jLabel
                cell.setPreferredSize(new Dimension(50, 50));// set a max size
                cell.setBorder(BorderFactory.createLineBorder(Color.black)); // create a border
                cell.setOpaque(true); // so we can fill the cell with a background
                cell.setBackground(BGCOLOURBEIGE); // this is the default background colour
                Color c = null; // intialize a null colour

                for (int k = 0; k < Colour.values().length; k++) { // loop through all possible colours for guessing

                    // if we find the colour associated with the string stored in our current index
                    if (Colour.values()[k].toString().equals(colourName)) {
                        try {
                            // get the colour
                            c = (Color) Color.class.getField(Colour.values()[k].toString().toUpperCase()).get(null);
                            cell.setBackground(c); // update the cell background
                            break; // use break as we already found our colour
                        } catch (Exception e) { // handle any erorrs associated with handling colours
                            e.printStackTrace();
                        }
                    }
                }

                boardPanel.add(cell); // add the cell to the panel
            }
        }

        // refresh board panel
        boardPanel.revalidate();
        boardPanel.repaint();

    }

    /**
     * this function refreshes the feedback panel, and updates it with new data
     * inputed to board.feedback, which tracks all the feedbacks the AI or player
     * has given throughout the game
     *
     * @param nothing
     * @return nothing
     */
    public static void revalidateFeedback() {
        feedbackPanel.removeAll(); // remove all GUI in feedback panel

        // loop through board.feedback
        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                String colourName = board.feedback[i][j]; // get the colour in that index
                JLabel cell = new JLabel(""); // intialize a label
                cell.setPreferredSize(new Dimension(50, 50)); // set the height
                cell.setBorder(BorderFactory.createLineBorder(Color.black)); // create a border
                cell.setOpaque(true); // so we can fill it with a colour
                cell.setBackground(BGCOLOURBEIGE); // default background is beige

                // loop through all colour stored in feedback colours (black or white)
                for (int k = 0; k < feedbackColours.length; k++) {
                    if (feedbackColours[k].toString().equals(colourName)) { // if a black or white peg exists in
                                                                            // board.feedabck[i][j]
                        cell.setBackground(feedbackColours[k]); // set the background to update the feedback given in
                                                                // this position
                    }
                }

                feedbackPanel.add(cell); // add the cell into the feedback panel

            }
        }

        // refresh and update the feedback panel
        feedbackPanel.revalidate();
        feedbackPanel.repaint();

    }

    public static void leaderboard(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel mainPanel = new JPanel();

        frame.pack();
        frame.setVisible(true);
    }

    public static String getPassword(String accountName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("./cb/accounts/accounts.txt"));

        String line = br.readLine();
        while (line != null) {
            String[] accountInfo = line.split(",");
            if (accountInfo[0].equals(accountName)) {
                br.close();
                return accountInfo[1];
            }

            line = br.readLine();
        }

        br.close();
        return null;
    }

    public static String login(String accountName, String attemptPwd) throws Exception {
        File account = new File("./cb/accounts/" + accountName + ".txt");
        PrintWriter aw = new PrintWriter(new FileWriter(account, true));
        PrintWriter pw = new PrintWriter(new FileWriter("./cb/accounts/accounts.txt", true));
        BufferedReader br = new BufferedReader(new FileReader("./cb/accounts/accounts.txt"));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] attemptHash = digest.digest(attemptPwd.getBytes(StandardCharsets.UTF_8));

        String line = br.readLine();
        while (line != null) {
            // Check if their requested name can be found inside the game records, if they
            // enter in an adjusted name with different capitalizations, do NOT let them
            // create an account as files are not allowed to have duplicate names even with
            // different capitiazlation
            if (!line.split(",")[0].equals(accountName) && line.split(",")[0].equalsIgnoreCase(accountName)) {
                if (account.exists()) {
                    br.close();
                    aw.close();
                    pw.close();
                    throw new Exception("Account already exists!");
                }
            } else if (line.split(",")[0].equals(accountName)) {
                // If their account can be found, then tell them they have logged in and return
                // their name
                String pwdHash = getPassword(accountName);
                String attemptHashString = "";
                for (int i = 0; i < attemptHash.length; i++) {
                    attemptHashString += attemptHash[i];
                    if (i != attemptHash.length - 1)
                        attemptHashString += ".";
                }
                if (attemptHashString.equals(pwdHash)) {
                    System.out.println("\nSuccessfully logged in as " + line.split(",")[0]);
                    pw.close();
                    br.close();
                    aw.close();
                    return line.split(",")[0];
                } else {
                    pw.close();
                    br.close();
                    aw.close();
                    throw new Exception("Wrong password inputted!");
                }
            } else {
                // Otherwise keep searching
                line = br.readLine();
            }
        }
        // If the loop hasn't exited by now, that means an account with that name
        // couldn't be found in the game records and let them know they created an
        // account
        System.out.println("\nSuccessfully created account: " + accountName + " with password: " + attemptPwd);
        String formattedStoredPassword = "";
        for (int i = 0; i < attemptHash.length; i++) {
            formattedStoredPassword += attemptHash[i];
            if (i != attemptHash.length - 1)
                formattedStoredPassword += ".";
        }
        pw.println(accountName + "," + formattedStoredPassword);

        pw.close();
        br.close();
        aw.close();
        return accountName;
    }

    public static void saveToFile(String accountName) throws Exception {
        login(accountName, accountName);
        File account = new File("./cb/accounts/" + accountName + ".txt");
        if (!account.exists()) {
            account.createNewFile();
        }
        PrintWriter aw = new PrintWriter(new FileWriter(account, true));
        File records = new File("./cb/accounts/records.txt");
        PrintWriter pw = new PrintWriter(new FileWriter(records, true));
        LocalDateTime ld = LocalDateTime.now();

        aw.println(attempts);
        pw.println(accountName + "," + attempts + "," + ld);

        aw.close();
        pw.close();
    }

    public static String getRecord(String[] accountName) throws IOException {
        File account = new File("./cb/accounts/" + accountName + ".txt");
        BufferedReader br = new BufferedReader(new FileReader(account));

        String line = br.readLine();
        while (line != null) {
            br.close();
            return line;
        }
        br.close();
        return null;

    }

    public static void finalMessage(String message, String[] code) {
        feedbackPanel.removeAll();
        boardPanel.removeAll();
        colourPicker.removeAll();
        displayColours.removeAll();

        feedbackPanel.add(Box.createVerticalGlue());

        // save the game
        JLabel display = new JLabel(message);
        name = new JTextField("Enter your account name");
        JButton saveRecord = new JButton("Save the game to a file");
        saveRecord.setBackground(BUTTONCOLOUR);

        saveRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH);

                try {
                    saveToFile(name.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });

        display.setFont(ForeverFontTitle);
        feedbackPanel.add(display);
        feedbackPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

        if (code != null) {
            JLabel codeHeader = new JLabel("The code to guess was:");
            codeHeader.setFont(ForeverFontNormal);
            feedbackPanel.add(codeHeader);

            JPanel displayColours = new JPanel(new FlowLayout());

            for (int i = 0; i < code.length; i++) {
                JLabel colour = new JLabel("");
                colour.setPreferredSize(new Dimension(50, 50));
                colour.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                colour.setOpaque(true);
                colour.setBackground(stringToColor(code[i]));
                displayColours.add(colour);
            }

            feedbackPanel.add(displayColours);
        }

        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold);
        backToMenu.setHorizontalTextPosition(JButton.LEFT);
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                music(false, POPPATH);

                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR);

        feedbackPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));
        feedbackPanel.add(backToMenu);

        feedbackPanel.add(Box.createVerticalGlue());

        feedbackPanel.revalidate();
        boardPanel.revalidate();
        colourPicker.revalidate();
        displayColours.revalidate();

        feedbackPanel.repaint();
        boardPanel.repaint();
        colourPicker.repaint();
        displayColours.repaint();

    }

    public static Color stringToColor(String colourName) {
        Color c = null;
        try {
            c = (Color) Color.class.getField(colourName).get(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            if (colourName != null)
                c = Color.decode(colourName);
        }
        return c;
    }

    /**
     * this function saves an array directly into data.txt ONLY. it is exclusively
     * used for when the AI tests itself
     *
     * @param array - String[], the array in which we are trying to write into
     *              data.txt
     * @throws IOException - throw exceptions related to file handling
     * @return nothing
     */
    public static void saveArray(String[] array) throws IOException {
        for (int j = 0; j < array.length; j++) { // loop through all the elements in the array
            myWriter.write(array[j] + " "); // save the elements side by side
        }
        myWriter.write(System.lineSeparator()); // create a new line
    }

    /**
     * accepts a file name and plays it outloud using AudioInputStream. Has the
     * option to loop the music based on a boolean
     *
     * @param loop - boolean, determines wheter or not we should loop the audio clip
     * @param path - String, represents the file path to load the music from
     * 
     * @return nothing
     */
    public static void music(boolean loop, String path) {

        try { // catch all errors related to file handling

            File musicPath = new File(path); // find the music files

            if (musicPath.exists()) { // if the music file exists

                // get the music clip
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                if (loop) { // if we want to loop
                    clip.loop(Clip.LOOP_CONTINUOUSLY); // loop forever
                } else {
                    clip.start(); // otherwise play it onces
                }
            } else {
                System.out.println("Music file does not exist"); // for the user to now they are missing a file
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // throw an error message when a requested operation is not supported
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
}