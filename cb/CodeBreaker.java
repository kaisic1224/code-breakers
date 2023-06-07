package cb;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                music(false, "./cb/assets/sounds/pop.wav");

                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR);

        // ALIGN UI?
        // title.setAlignmentX(Component.CENTER_ALIGNMENT);
        // backToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);

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
        AICodeBreaker AI = new AICodeBreaker();
        AI.generateAllCombos(board.getSize());

        // panel properites
        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));

        // init progress bar
        JProgressBar progressBar = new JProgressBar(0, AI.getAllCombos().length);
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
                    int[] attemptsArray = new int[AI.getAllCombos().length];
                    int[] attemptsSort = new int[7];

                    for (int i = 0; i < AI.getAllCombos().length; i++) {

                        AI.setRemainingCombos(AI.getAllCombos().clone());
                        int attempts = 1;
                        int blacks = -1;
                        int whites = -1;

                        do {
                            String[] code = AI.guessCombo(blacks, whites);
                            saveArray(code);
                            String[] feedback = board.checkGuess(code, AI.getAllCombos()[i]);
                            saveArray(feedback);
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

                    float avg = (float) totalAttempts / (float) AI.getAllCombos().length;

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
                            music(false, "./cb/assets/sounds/pop.wav");

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
        boardPanel.setBorder(new EmptyBorder(0, 0, 0, 94));
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
                        music(false, "./cb/assets/sounds/pop.wav");

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
                    music(false, "./cb/assets/sounds/pop.wav");

                    Component[] guessColours = displayColours.getComponents();
                    String[] colors = new String[4];

                    for (int i = 0; i < guessColours.length; i++) {
                        colors[i] = board.colorToString(guessColours[i].getBackground());
                    }

                    String[] evaluation = board.checkGuess(colors, board.getCode());
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
            AI = new AICodeBreaker();
            AI.generateAllCombos(board.getSize());

            for (int i = 0; i < feedbackColours.length; i++) {

                JButton feedback = new JButton("");
                feedback.setPreferredSize(new Dimension(50, 50));
                feedback.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                feedback.setOpaque(true);
                feedback.setBackground(feedbackColours[i]);

                feedback.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        music(false, "./cb/assets/sounds/pop.wav");

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

                    music(false, "./cb/assets/sounds/pop.wav");

                    clearAll.setEnabled(false);

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
                music(false, "./cb/assets/sounds/pop.wav");

                clearAll.setEnabled(false);

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

    public static void userLogin(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JButton backtoMenu = new JButton("Return to menu");
        backtoMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CodeBreaker();
                frame.setVisible(false);
            }
        });

        if (sessionName == null) {
            JPanel loginPanel = new JPanel(new GridBagLayout());
            loginPanel.setBackground(BGCOLOURORANGE);
            loginPanel.setOpaque(true);

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTH;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;

            JPanel hero = new JPanel();
            hero.setBackground(BGCOLOURORANGE);
            hero.setOpaque(true);
            hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
            Image logo = new ImageIcon("./cb/assets/logo.png").getImage();
            JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_FAST)),
                    JLabel.CENTER);
            hero.add(heroLogo);
            loginPanel.add(hero, c);

            JPanel usernamePanel = new JPanel(new FlowLayout());
            usernamePanel.setBackground(BGCOLOURORANGE);
            usernamePanel.setOpaque(true);
            JLabel usernameLabel = new JLabel("Username: ");
            JTextField username = new JTextField("", 20);
            username.setMaximumSize(MENUBUTTONSIZE);
            usernamePanel.add(usernameLabel);
            usernamePanel.add(username);
            c.gridy = 1;
            loginPanel.add(usernamePanel, c);
            loginPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

            JPanel passwordPanel = new JPanel(new FlowLayout());
            passwordPanel.setBackground(BGCOLOURORANGE);
            passwordPanel.setOpaque(true);
            JLabel passwordLabel = new JLabel("Password: ");
            JTextField password = new JTextField("", 20);
            password.setMaximumSize(MENUBUTTONSIZE);
            passwordPanel.add(passwordLabel);
            passwordPanel.add(password);
            c.gridy = 2;
            loginPanel.add(passwordPanel, c);
            loginPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));

            JLabel errorText = new JLabel("");
            errorText.setForeground(Color.RED);
            errorText.setBackground(BGCOLOURORANGE);
            errorText.setOpaque(true);
            JButton submitLogin = new JButton("Login");
            submitLogin.setMaximumSize(MENUBUTTONSIZE);
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

            c.gridy = 3;
            loginPanel.add(submitLogin, c);

            c.gridy = 4;
            loginPanel.add(errorText, c);

            c.gridy = 5;
            loginPanel.add(backtoMenu, c);

            frame.add(loginPanel);
        } else {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("./cb/accounts/" + sessionName + ".txt"));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int totalTurns = 0;
            int totalGames = 0;
            int totalWins = 0;
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            while (line != null) {
                totalGames++;
                int turns = Integer.parseInt(line);
                totalTurns += turns;
                if (turns < 10) {
                    totalWins++;
                }

                try {
                    line = br.readLine();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            DecimalFormat df = new DecimalFormat("0.00");
            JPanel stats = new JPanel();
            JLabel lifetimeTurnLabel = new JLabel("Lifetime turns: " + totalTurns);
            JLabel lifetimeGameLabel = new JLabel("Lifetime games: " + totalGames);
            JLabel lifetimeWinsLabel = new JLabel("Lifetime wins: " + totalWins);
            JLabel averageTurns = new JLabel("Average attempts: " + (df.format(totalTurns / totalGames)));

            stats.add(lifetimeTurnLabel);
            stats.add(lifetimeGameLabel);
            stats.add(lifetimeWinsLabel);
            stats.add(averageTurns);
            stats.add(backtoMenu);

            frame.add(stats);

        }

        frame.pack();
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
                music(false, "./cb/assets/sounds/pop.wav");

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
                music(false, "./cb/assets/sounds/pop.wav");

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
                music(false, "./cb/assets/sounds/pop.wav");

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
                music(false, "./cb/assets/sounds/pop.wav");

                gameFrame = new JFrame("Code Breakers | Stats");
                AIstats(gameFrame);
                setVisible(false);
            }
        });

        JButton login = new JButton("LOGIN", arrowImg);
        login.setBackground(BUTTONCOLOUR);
        login.setFont(ForeverFontBold);
        login.setHorizontalTextPosition(JButton.LEFT);
        login.setMaximumSize(MENUBUTTONSIZE);

        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame loginFrame = new JFrame("Code Breakers | Login");
                userLogin(loginFrame);
                setVisible(false);
            }
        });

        JLabel session = new JLabel();
        if (sessionName == null) {
            session.setText("Not currently logged in");
        } else {
            session.setText("Currently logged in as: " + sessionName);
        }

        JButton leaderboardBtn = new JButton("LEADERBOARDS", arrowImg);
        leaderboardBtn.setBackground(BUTTONCOLOUR);
        leaderboardBtn.setFont(ForeverFontBold);
        leaderboardBtn.setHorizontalTextPosition(JButton.LEFT);
        leaderboardBtn.setMaximumSize(MENUBUTTONSIZE);

        leaderboardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame leaderboardFrame = new JFrame("Code Breakers | Leaderboard");
                try {
                    leaderboard(leaderboardFrame);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                setVisible(false);
            }
        });

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
        buttons.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING)));
        buttons.add(leaderboardBtn);

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
        music(true, "./cb/assets/sounds/bgMusic.wav");

    }

    public static void playerIsCodeBreaker(int whites, int blacks, String[] code) {

        if (blacks == board.getSize()) {
            finalMessage("YOU WIN in " + (attempts) + " guesses! ", code);

        } else if (attempts == board.getTries()) {
            finalMessage("YOU LOSE! (no more attempts) ", code);
            if (sessionName != null) {
                try {
                    saveToFile(sessionName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // TODO: prompt login
            }
        }

    }

    public static void playerIsCodeSetter(int blacks, int whites, String[] code) {

        if (blacks == board.getSize()) {
            finalMessage("AI WINS in " + (attempts) + " guesses!", code);
        } else {
            String[] guess = AI.guessCombo(blacks, whites);
            // AI.printRemainingCombos(4);
            if (guess == null || attempts == board.getTries()) {
                finalMessage("You have to be wrong!!", null);
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

    public static void leaderboard(JFrame frame) throws IOException {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel mainPanel = new JPanel();
        JPanel leaderCharts = new JPanel(new GridLayout(10, 1));

        BufferedReader fr = new BufferedReader(new FileReader("./cb/accounts/records.txt"));
        String[] records = new String[50];
        String line = fr.readLine();
        int i = 0;
        while (line != null) {
            records[i] = line;
            i++;
            line = fr.readLine();
        }
        for (int j = 0; j < records.length; j++) {
            if (records[j] == null) {
                records[j] = " ,11, , ";
            }
        }

        // Use a bubble sort, but in order to preserve the rest of the information and
        // not just sort the scores, we have to compare just the scores, then switch
        // each entire string's position
        boolean isSorted = true;
        for (int k = 0; k < records.length - 1; k++) {
            for (int j = 0; j < ((records.length - 1) - k); j++) {
                // All game records are stored as account,score,timestamp
                // We have to only compare records, but switch the entire strings, so we have to
                // split each string by commas, and access the 2nd element in each array of
                // substrings which would be the score. We convert it to integer and compare
                // with one another
                if (Integer.parseInt(records[j].split(",")[1]) > Integer.parseInt(records[j + 1].split(",")[1])) {
                    isSorted = false;
                    // Swap positions with each other
                    String temp = records[j];
                    records[j] = records[j + 1];
                    records[j + 1] = temp;
                } else if (records[j].split(",")[1].equals(records[j + 1].split(",")[1])
                        && Integer.parseInt(records[j].split(",")[1]) < 11) {
                    // Also try comparing dates with each other, but only if both records are equal,
                    // and the score is not invalid (-1).
                    LocalDateTime date1 = LocalDateTime.parse(records[j].split(",")[2]);
                    LocalDateTime date2 = LocalDateTime.parse(records[j + 1].split(",")[2]);
                    // If the date in front happened later than the one before, then move it in
                    // front as we want the most recent dates to be on the top of the leaderboard
                    if (date2.isAfter(date1)) {
                        String temp = records[j];
                        records[j] = records[j + 1];
                        records[j + 1] = temp;
                    }
                }
            }
            if (isSorted) {
                break;
            } else {
                isSorted = true;
            }
        }

        // only first 10 records
        for (int j = 0; j < 10; j++) {
            JPanel recordRow = new JPanel();
            String[] log = records[j].split(",");
            JLabel user = new JLabel(log[0]);
            JLabel score = new JLabel(log[1]);
            JLabel date = !log[2].equals(" ")
                    ? new JLabel(LocalDateTime.parse(log[2]).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    : new JLabel();
            recordRow.add(user);
            recordRow.add(score);
            recordRow.add(date);
            recordRow.setOpaque(true);
            System.out.println(log[0]);
            if (log[0].equals(sessionName)) {
                recordRow.setBackground(Color.ORANGE);
            }

            leaderCharts.add(recordRow);
        }

        mainPanel.add(leaderCharts);
        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold);
        backToMenu.setHorizontalTextPosition(JButton.LEFT);
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                music(false, "./cb/assets/sounds/pop.wav");

                new CodeBreaker();
                frame.setVisible(false);
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR);

        mainPanel.add(backToMenu);
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        fr.close();
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
                    throw new Exception("Wrong password!");
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
        File account = new File("./cb/accounts/" + accountName + ".txt");
        if (!account.exists()) {
            account.createNewFile();
        }
        PrintWriter aw = new PrintWriter(new FileWriter(account, true));
        File records = new File("./cb/accounts/records.txt");
        PrintWriter pw = new PrintWriter(new FileWriter(records, true));
        LocalDateTime ld = LocalDateTime.now();

        aw.println(attempts);
        String code = String.join("", board.getCode());
        pw.println(accountName + "," + attempts + "," + ld + "," + code);

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
                music(false, "./cb/assets/sounds/pop.wav");

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
            codeHeader.setHorizontalAlignment(JLabel.CENTER);
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
                music(false, "./cb/assets/sounds/pop.wav");

                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });
        backToMenu.setBackground(BUTTONCOLOUR);

        feedbackPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));
        feedbackPanel.add(backToMenu);

        feedbackPanel.add(Box.createVerticalGlue());
        feedbackPanel.add(Box.createRigidArea(new Dimension(0, MENUBUTTONSPACING * 2)));
        JPanel sessionPanel = new JPanel(new GridBagLayout());
        JLabel sessionLabel = new JLabel();
        JButton login = new JButton("Sign in");
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame loginFrame = new JFrame("Code Breakers | Login");
                userLogin(loginFrame);
                gameFrame.setVisible(false);

            }
        });
        sessionPanel.add(sessionLabel);
        sessionLabel.setHorizontalAlignment(JLabel.CENTER);
        if (sessionName == null) {
            sessionLabel.setText("Not currently signed in");
            sessionLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
            sessionPanel.add(login);
        } else {
            sessionLabel.setText("Currently signed in as: " + sessionName);
            try {
                saveToFile(sessionName);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        feedbackPanel.add(sessionPanel);

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

    public static void saveArray(String[] array) {
        try {
            for (int j = 0; j < array.length; j++) {
                myWriter.write(array[j] + " ");

            }
            myWriter.write(System.lineSeparator());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void music(boolean loop, String path) {

        try {

            File musicPath = new File(path);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    clip.start();
                }
            } else {
                System.out.println("Music file does not exist");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
}