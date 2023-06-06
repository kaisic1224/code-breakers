package cb;

import java.io.*;
import java.security.KeyStore.LoadStoreParameter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import cb.Board.Colour;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeBreaker extends JFrame implements ActionListener {
    // assets
    final static Color BGCOLOURORANGE = new Color(255, 190, 121);
    final static Color BGCOLOURBEIGE = new Color(211, 182, 156);
    final static Color BUTTONCOLOUR = new Color(147, 202, 237);

    static Font ForeverFontBold;
    static Font ForeverFontTitle;
    static Font ForeverFontNormal;
    static Font ForeverFont;

    // panel formatting
    final static Dimension MENUBUTTONSIZE = new Dimension(500, 100);
    final static int MENUBUTTONSPACING = 30;

    // object declaration
    static Board board;
    static AICodeBreaker AI;
    public static PrintWriter myWriter;

    // game components
    static Color[] feedbackColours = { Color.black, Color.white };
    static String[] playerFeedback;

    static int numColoursSelected = 0;
    static int attempts = 0;

    // panels and frames
    public static JPanel boardPanel;
    public static JPanel feedbackPanel;
    public static JPanel colourPicker;
    public static JPanel displayColours;

    public static JFrame gameFrame;

    public static void LoadAssets() {
        try {
            ForeverFont = Font.createFont(Font.TRUETYPE_FONT, new File("./cb/assets/Forever.ttf"));
        } catch (Exception e) {
            System.out.println("File could not be found, or error parsing font");
        }
        ForeverFontBold = ForeverFont.deriveFont(Font.BOLD, 16f);
        ForeverFontNormal = ForeverFont.deriveFont(16f);
        ForeverFontTitle = ForeverFont.deriveFont(Font.BOLD, 50f);
    }

    public static void Tutorial(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TUTORIAL");
        title.setFont(ForeverFontTitle);

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
        instructions.setFont(ForeverFontNormal);

        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold);
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR);

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        mainPanel.add(instructions);
        mainPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        mainPanel.add(backToMenu);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.setBackground(BGCOLOURORANGE);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public void AIstats(JFrame frame) {

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // init combo generation
        Board board = new Board();
        AICodeBreaker AI = new AICodeBreaker(board.getSize());
        AI.generateAllCombos(board.getSize());

        // panel properites
        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));

        // init progress bar
        JProgressBar progressBar = new JProgressBar(0, AI.allCombos.length);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        progressBar.setForeground(BUTTONCOLOUR);
        progressBar.setMaximumSize(new Dimension(750, 30));

        JLabel header = new JLabel("TESTING ALL CASES...");
        header.setFont(ForeverFontTitle);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        // add
        loadPanel.add(Box.createVerticalGlue());
        loadPanel.add(header);
        loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        loadPanel.add(progressBar);
        loadPanel.add(Box.createVerticalGlue());
        loadPanel.setBackground(Color.white);

        frame.add(loadPanel);
        frame.setVisible(true);

        // start a new thread
        Thread t = new Thread(new Runnable() {

            public void run() {

                try {

                    myWriter = new PrintWriter("data.txt");
                    // test cases
                    int totalAttempts = 0;
                    int[] attemptsArray = new int[AI.remainingCombos.length];
                    int[] attemptsSort = new int[7];

                    for (int i = 0; i < AI.allCombos.length; i++) {

                        AI.remainingCombos = AI.allCombos.clone();
                        AI.nonGuessedCombos = AI.allCombos.clone();

                        int attempts = 1;
                        int blacks = -1;
                        int whites = -1;

                        do {
                            String[] code = AI.guessCombo(blacks, whites);
                            printArray(code);
                            String[] feedback = board.checkGuess(code, AI.allCombos[i], 0);
                            printArray(feedback);
                            int[] pegHolder = board.returnPegs(feedback);
                            blacks = pegHolder[1];
                            whites = pegHolder[0];

                            if (blacks != board.getSize()) {
                                attempts++;
                            }

                        } while (blacks != board.getSize());

                        totalAttempts += attempts;

                        String msg = "CASE " + (i + 1) + " : " + attempts;
                        myWriter.write(msg);
                        myWriter.write(System.lineSeparator());
                        myWriter.write("----------------------------------");
                        myWriter.write(System.lineSeparator());

                        attemptsArray[i] = attempts;
                        progressBar.setValue(i);
                    }

                    float avg = (float) totalAttempts / (float) AI.allCombos.length;

                    int worstCase = 0;

                    // count up distribution
                    for (int i = 0; i < attemptsArray.length; i++) {
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

                            if (attemptsArray[i] > worstCase) {
                                worstCase = attemptsArray[i];
                            }

                        }
                    }

                    // update panel
                    loadPanel.removeAll();
                    loadPanel.add(Box.createVerticalGlue());

                    JLabel header = new JLabel("DISTRIBUTION OF ATTEMPTS REQUIRED TO SOLVE ALL CASES");
                    header.setFont(ForeverFont.deriveFont(Font.BOLD, 25f));
                    header.setAlignmentX(Component.CENTER_ALIGNMENT);
                    loadPanel.add(header);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));

                    for (int i = 0; i < attemptsSort.length; i++) {

                        JLabel attemptResult;

                        if (i == attemptsSort.length - 1) {
                            attemptResult = new JLabel("7+ ATTEMPTS: " + attemptsSort[i]);
                        } else {
                            attemptResult = new JLabel((i + 1) + " ATTEMPTS: " + attemptsSort[i]);
                        }

                        attemptResult.setFont(ForeverFontNormal);
                        attemptResult.setAlignmentX(Component.CENTER_ALIGNMENT);
                        loadPanel.add(attemptResult);
                        loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING / 2)));
                    }

                    JLabel average = new JLabel("AVERAGE ATTEMPTS: " + (float) avg);
                    average.setFont(ForeverFontNormal);
                    average.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel worseCase = new JLabel("WORST CASE: " + worstCase);
                    worseCase.setFont(ForeverFontNormal);
                    worseCase.setAlignmentX(Component.CENTER_ALIGNMENT);
                    worseCase.setForeground(Color.red);

                    JLabel reminder = new JLabel(
                            "All test cases are saved in data.txt for your analysis");

                    reminder.setFont(ForeverFont.deriveFont(Font.ITALIC, 10f));
                    reminder.setAlignmentX(Component.CENTER_ALIGNMENT);

                    loadPanel.add(average);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
                    loadPanel.add(worseCase);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING / 2)));
                    loadPanel.add(reminder);
                    loadPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));

                    JButton backToMenu = new JButton("Back to Menu");
                    backToMenu.setFont(ForeverFontBold);
                    backToMenu.setHorizontalTextPosition(JButton.LEFT);
                    backToMenu.setBackground(BUTTONCOLOUR);
                    backToMenu.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            new CodeBreaker();
                            gameFrame.setVisible(false);
                        }
                    });
                    backToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
                    backToMenu.setBackground(BUTTONCOLOUR);

                    loadPanel.add(backToMenu);
                    loadPanel.add(Box.createVerticalGlue());

                    loadPanel.revalidate();
                    loadPanel.repaint();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                myWriter.close();

            }

        });

        t.start();

    }

    // will use current object inside Board instance variable to render board
    public static void Game(JFrame frame, boolean isCodeBreaker) {

        // reset counters
        attempts = 0;
        numColoursSelected = 0;
        playerFeedback = new String[board.getTries()];

        // init frame
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        boardPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));
        boardPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        feedbackPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));

        JLabel currentRound = new JLabel();
        currentRound.setText("Guesses Left = " + (board.getTries() - attempts));
        currentRound.setFont(ForeverFontNormal);

        for (int i = 0; i < board.getTries(); i++) {

            for (int j = 0; j < board.getSize(); j++) {

                JLabel cell = new JLabel("");
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setBackground(BGCOLOURBEIGE);
                boardPanel.add(cell);

            }
        }

        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                JLabel cell = new JLabel();
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setBackground(BGCOLOURBEIGE);
                feedbackPanel.add(cell);
            }
        }

        colourPicker = new JPanel(new FlowLayout());
        displayColours = new JPanel(new FlowLayout());

        JButton clearAll = new JButton("Clear all");
        JButton submit = new JButton("Submit");
        clearAll.setBackground(BUTTONCOLOUR);
        submit.setBackground(BUTTONCOLOUR);

        clearAll.setEnabled(false);
        submit.setEnabled(false);

        if (isCodeBreaker) {
            board.generateCode();
            board.printCode();

            for (int i = 0; i < Colour.values().length; i++) {
                JButton peg = new JButton("");
                peg.setPreferredSize(new Dimension(50, 50));
                peg.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                peg.setOpaque(true);
                Color c = null;
                try {
                    c = (Color) Color.class.getField(Colour.values()[i].toString().toUpperCase()).get(null);
                    peg.setBackground(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                peg.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (numColoursSelected < board.getSize()) {
                            clearAll.setEnabled(true);
                            numColoursSelected++;

                            JLabel code = new JLabel("");
                            code.setPreferredSize(new Dimension(50, 50));
                            code.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            code.setOpaque(true);
                            code.setBackground(peg.getBackground());

                            displayColours.add(code);
                            displayColours.revalidate();
                            displayColours.repaint();

                        }

                        if (numColoursSelected == board.getSize()) {
                            submit.setEnabled(true);
                        }
                    }
                });
                colourPicker.add(peg);
            }

            submit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Component[] guessColours = displayColours.getComponents();
                    String[] colors = new String[4];

                    for (int i = 0; i < guessColours.length; i++) {
                        colors[i] = board.colorToString(guessColours[i].getBackground());
                    }

                    String[] evaluation = board.checkGuess(colors, board.getCode(), attempts);
                    int[] pegs = board.returnPegs(evaluation);
                    String[] feedback = new String[4];
                    for (int i = 0; i < pegs[0]; i++) {
                        feedback[i] = Color.white.toString();
                    }
                    for (int i = 0; i < pegs[1]; i++) {
                        int index = pegs[0] + i;
                        feedback[index] = Color.black.toString();
                    }

                    board.feedback[board.getTries() - 1 - attempts] = feedback;

                    revalidateFeedback();

                    feedbackPanel.revalidate();
                    feedbackPanel.repaint();

                    board.board[board.getTries() - 1 - attempts] = colors;
                    revalidateBoard();
                    attempts++;
                    playerIsCodeBreaker(pegs[0], pegs[1], colors);

                    numColoursSelected = 0;
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    submit.setEnabled(false);
                    clearAll.setEnabled(false);

                    currentRound.setText("Guesses Left = " + (board.getTries() - attempts));
                }
            });

        } else {
            AI = new AICodeBreaker(board.getSize());
            AI.generateAllCombos(board.getSize());

            for (int i = 0; i < feedbackColours.length; i++) {

                JButton feedback = new JButton("");
                feedback.setPreferredSize(new Dimension(50, 50));
                feedback.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                feedback.setOpaque(true);
                feedback.setBackground(feedbackColours[i]);

                feedback.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (numColoursSelected < board.getSize()) {
                            JLabel selectedColour = new JLabel("");
                            selectedColour.setPreferredSize(new Dimension(30, 30));
                            selectedColour.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            selectedColour.setOpaque(true);
                            selectedColour.setBackground(feedback.getBackground());
                            displayColours.add(selectedColour);

                            boardPanel.revalidate();
                            boardPanel.repaint();

                            playerFeedback[numColoursSelected] = feedback.getBackground().toString();

                            numColoursSelected++;

                            clearAll.setEnabled(true);
                            submit.setEnabled(true);
                        }

                    }
                });

                colourPicker.add(feedback);
            }

            submit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    clearAll.setEnabled(false);
                    submit.setEnabled(false);

                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    numColoursSelected = 0;

                    // use clone or else when we assign as PBR!
                    board.feedback[board.getTries() - 1 - attempts] = playerFeedback.clone();

                    revalidateFeedback();

                    int blacks = 0;
                    int whites = 0;

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

                    attempts++;

                    playerIsCodeSetter(blacks, whites, AI.getLastGuess());

                    currentRound.setText("Guesses Left = " + (board.getTries() - attempts));
                }

            });

        }

        // a function to clear all colours user selected
        clearAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearAll.setEnabled(false);
                submit.setEnabled(false);

                displayColours.removeAll();
                displayColours.revalidate();
                displayColours.repaint();
                numColoursSelected = 0;
            }
        });
        colourPicker.add(clearAll);
        colourPicker.add(submit);

        // panel layouts
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        frame.add(currentRound, c);

        mainPanel.add(boardPanel);
        mainPanel.add(feedbackPanel);
        c.gridy = 1; // take the first row in the layout
        c.fill = GridBagConstraints.HORIZONTAL; // fill in width
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        frame.add(mainPanel, c);

        c.gridy = 2;
        frame.add(colourPicker, c);

        c.gridy = 3;
        frame.add(displayColours, c);
        frame.setVisible(true);
    }

    public CodeBreaker() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("CodeBreakers | Main Menu");

        JPanel mainPanel = new JPanel(new GridBagLayout());

        // display the logo
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        Image logo = new ImageIcon("./cb/assets/logo.png").getImage();
        JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_FAST)),
                JLabel.CENTER);
        hero.add(heroLogo);

        // buttons to navigate game components
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(0, 50, 0, 0));

        // arrow image
        Image arrowIcon = new ImageIcon("./cb/assets/arrow.png").getImage();
        ImageIcon arrowImg = new ImageIcon(arrowIcon.getScaledInstance(25, 25, Image.SCALE_FAST));

        // ai plays as the code setter
        JButton aiPlay = new JButton("play as CODE BREAKER", arrowImg);
        aiPlay.setBackground(BUTTONCOLOUR);
        aiPlay.setFont(ForeverFontBold);
        aiPlay.setHorizontalTextPosition(JButton.LEFT);
        aiPlay.setMaximumSize(MENUBUTTONSIZE);

        aiPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Player is code breaker");
                board = new Board();
                Game(gameFrame, true);
                setVisible(false);
            }
        });

        // person plays as the code setter
        JButton personPlay = new JButton("play as CODE SETTER", arrowImg);
        personPlay.setBackground(BUTTONCOLOUR);
        personPlay.setFont(ForeverFontBold);
        personPlay.setHorizontalTextPosition(JButton.LEFT);
        personPlay.setMaximumSize(MENUBUTTONSIZE);

        personPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Player is code setter");
                board = new Board();
                Game(gameFrame, false);

                playerIsCodeSetter(-1, -1, AI.getLastGuess());
                setVisible(false);
            }
        });

        // tutorial
        JButton tutorial = new JButton("TUTORIAL", arrowImg);
        tutorial.setBackground(BUTTONCOLOUR);
        tutorial.setFont(ForeverFontBold);
        tutorial.setHorizontalTextPosition(JButton.LEFT);
        tutorial.setMaximumSize(MENUBUTTONSIZE);

        tutorial.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Tutorial");
                Tutorial(gameFrame);
                setVisible(false);
            }
        });

        // stats
        JButton aiStats = new JButton("AI STATS", arrowImg);
        aiStats.setBackground(BUTTONCOLOUR);
        aiStats.setFont(ForeverFontBold);
        aiStats.setHorizontalTextPosition(JButton.LEFT);
        aiStats.setMaximumSize(MENUBUTTONSIZE);

        aiStats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Stats");
                AIstats(gameFrame);
                setVisible(false);
            }
        });

        // add to buttons panel
        buttons.add(aiPlay);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(personPlay);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(tutorial);
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(aiStats);

        // setbackgorund colours
        hero.setBackground(BGCOLOURORANGE);
        mainPanel.setBackground(BGCOLOURORANGE);
        buttons.setBackground(BGCOLOURORANGE);

        // add to main frame
        mainPanel.add(hero);
        mainPanel.add(buttons);
        getContentPane().add(mainPanel);
        setVisible(true);

    }

    public static void main(String[] args) {

        LoadAssets();
        new CodeBreaker();

    }

    public static void playerIsCodeBreaker(int whites, int blacks, String[] code) {

        if (blacks == board.getSize()) {
            finalMessage("YOU WIN in " + (attempts) + " guesses! ", code);
        } else if (attempts == board.getTries()) {
            finalMessage("YOU LOSE! (no more attempts) ", code);
        }
    }

    public static void playerIsCodeSetter(int blacks, int whites, String[] code) {

        if (blacks == board.getSize()) {
            finalMessage("AI WINS in " + (attempts) + " guesses!", code);
        } else {
            String[] guess = AI.guessCombo(blacks, whites);
            // AI.printRemainingCombos(4);
            if (guess == null || attempts == board.getTries()) {
                finalMessage("You have to be wrong!! :( )", null);
            } else {
                board.board[board.getTries() - 1 - attempts] = guess.clone();
                revalidateBoard();
            }

        }

    }

    public static void revalidateBoard() {
        boardPanel.removeAll();

        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                String colourName = board.board[i][j];

                JLabel cell = new JLabel("");
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.black));
                cell.setOpaque(true);
                cell.setBackground(BGCOLOURBEIGE);
                Color c = null;

                for (int k = 0; k < Colour.values().length; k++) {
                    if (Colour.values()[k].toString().equals(colourName)) {
                        try {
                            c = (Color) Color.class.getField(Colour.values()[k].toString().toUpperCase()).get(null);
                            cell.setBackground(c);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                boardPanel.add(cell);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();

    }

    public static void revalidateFeedback() {
        feedbackPanel.removeAll();

        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                String colourName = board.feedback[i][j];
                JLabel cell = new JLabel("");
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.black));
                cell.setOpaque(true);
                cell.setBackground(BGCOLOURBEIGE);

                for (int k = 0; k < feedbackColours.length; k++) {
                    if (feedbackColours[k].toString().equals(colourName)) {
                        cell.setBackground(feedbackColours[k]);

                    }
                }

                feedbackPanel.add(cell);
            }
        }

        feedbackPanel.revalidate();
        feedbackPanel.repaint();

    }

    public static void finalMessage(String message, String[] code) {
        feedbackPanel.removeAll();
        boardPanel.removeAll();
        colourPicker.removeAll();
        displayColours.removeAll();

        feedbackPanel.add(Box.createVerticalGlue());

        JLabel display = new JLabel(message);
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

    public static void saveToFile(String[] record, String accountName) throws IOException {
        PrintWriter aw = new PrintWriter(new FileWriter("./accounts/" + accountName + ".txt", true));

        aw.println(String.join(",", record));

        aw.close();
    }

    public static String getRecord(String[] accountName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("./accounts/" + accountName + ".txt"));

        String line = br.readLine();
        while (line != null) {
            br.close();
            return line;
        }
        br.close();
        return null;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
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

    public static void printArray(String[] array) {
        try {
            for (int j = 0; j < array.length; j++) {
                System.out.print(array[j] + " ");

                myWriter.write(array[j] + " ");

            }
            System.out.println(" ");

            myWriter.write(System.lineSeparator());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}