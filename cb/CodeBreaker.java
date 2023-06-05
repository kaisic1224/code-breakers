package cb;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import cb.Board.Colour;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeBreaker extends JFrame implements ActionListener {
    final String fontColour = "#374151";
    final String backgroundColour = "#FFBE79";
    private static Board board;

    static Font ForeverFontBold = null;
    static Font ForeverFontTitle = null;
    static Font ForeverFontNormal = null;
    static Font ForeverFont;
    static Scanner scan; // is this allowed????

    static Color[] feedbackColours = { Color.black, Color.white };
    static int numColoursSelected = 0;
    static String[] playerFeedback = new String[4];
    static AICodeBreaker AI;

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
        ForeverFontTitle = ForeverFont.deriveFont(Font.BOLD, 100f);
    }

    public static void Tutorial(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TUTORIAL");
        title.setFont(ForeverFontTitle);

        JLabel instructions = new JLabel("<html>"
                + " a random code (consisting of 6 colours with a code length of 4) is set by the code setter"
                + "<br />" +
                "The code breaker gets a maximum of 10 tries to guess the code and is given feedback as to how many correctly positioned colour pegs the guess has and how many correct colour but incorrectly positioned pegs the guess has"
                + "<br />" +
                "The program has two options: an AI sets the code and the player tries to guess the code or the player sets the code and the program guesses the code within 10 tries"
                +
                "</html>");
        instructions.setFont(ForeverFontNormal);

        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold);
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });

        mainPanel.add(title);
        mainPanel.add(instructions);
        mainPanel.add(backToMenu);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // will use current object inside Board instance variable to render board
    public static void Game(JFrame frame, boolean isCodeBreaker) {

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());

        ArrayList<JButton> playerCode = new ArrayList<JButton>();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        boardPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));
        boardPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        feedbackPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));

        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                JLabel cell = new JLabel("");
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                boardPanel.add(cell);
            }
        }

        for (int i = 0; i < board.getTries(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                JLabel cell = new JLabel();
                cell.setPreferredSize(new Dimension(50, 50));
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                feedbackPanel.add(cell);
            }
        }

        mainPanel.add(boardPanel);
        mainPanel.add(feedbackPanel);

        colourPicker = new JPanel(new FlowLayout());
        displayColours = new JPanel(new FlowLayout());
        // Graphics g = colourPicker.getGraphics();
        // g.fillOval(20, 20, 10, 10);

        if (isCodeBreaker) {
            board.generateCode();
            board.printCode();
            JButton clearAll = new JButton("Clear all");
            JButton submit = new JButton("Submit");
            clearAll.setEnabled(false);
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
                        if (playerCode.size() < 4) {
                            clearAll.setEnabled(true);
                            playerCode.add(peg);
                            JLabel code = new JLabel("");
                            code.setPreferredSize(new Dimension(50, 50));
                            code.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            code.setOpaque(true);
                            code.setBackground(peg.getBackground());
                            displayColours.add(code);
                            displayColours.revalidate();
                            displayColours.repaint();

                        }

                        if (playerCode.size() == 4) {
                            submit.setEnabled(true);
                        }
                    }
                });
                colourPicker.add(peg);
            }

            clearAll.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    playerCode.clear();
                    clearAll.setEnabled(false);
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                }
            });
            colourPicker.add(clearAll);

            submit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Component[] guessColours = displayColours.getComponents();
                    String[] colors = new String[4];

                    for (int i = 0; i < guessColours.length; i++) {
                        colors[i] = board.colorToString(guessColours[i].getBackground());
                    }

                    String[] evaluation = board.checkGuess(colors, board.getCode(), board.turn);
                    int[] pegs = board.returnPegs(evaluation);
                    String[] feedback = new String[4];
                    for (int i = 0; i < pegs[0]; i++) {
                        feedback[i] = "WHITE";
                    }
                    for (int i = 0; i < pegs[1]; i++) {
                        int index = pegs[0] + i;
                        feedback[index] = "BLACK";
                    }

                    board.feedback[board.getTries() - 1 - board.turn] = feedback;

                    feedbackPanel.removeAll();
                    for (int i = 0; i < board.getTries(); i++) {
                        for (int j = 0; j < board.getSize(); j++) {
                            String colourName = board.feedback[i][j];
                            JLabel cell = new JLabel("");
                            cell.setPreferredSize(new Dimension(50, 50));
                            cell.setBorder(BorderFactory.createLineBorder(Color.black));
                            cell.setOpaque(true);
                            System.out.println(colourName);
                            feedbackPanel.add(cell);
                            if (colourName == null)
                                continue;
                            Color c = stringToColor(colourName);
                            cell.setBackground(c);

                        }
                    }
                    feedbackPanel.revalidate();
                    feedbackPanel.repaint();

                    board.board[board.getTries() - 1 - board.turn] = colors;
                    revalidateBoard();
                    board.turn++;
                    playerIsCodeBreaker(pegs[0], pegs[1]);

                    playerCode.clear();
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    submit.setEnabled(false);
                    clearAll.setEnabled(false);
                }
            });
            colourPicker.add(submit);

        } else {
            AI = new AICodeBreaker(board.getSize());
            AI.generateAllCombos(board.getSize());
            board.turn = 0;

            for (int i = 0; i < 2; i++) {

                JButton feedback = new JButton("");
                feedback.setPreferredSize(new Dimension(50, 50));
                feedback.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                feedback.setOpaque(true);
                feedback.setBackground(feedbackColours[i]);

                feedback.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (numColoursSelected < 4) {
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
                        }

                    }
                });

                colourPicker.add(feedback);
            }

            JButton clearSelection = new JButton("Clear");
            clearSelection.setPreferredSize(new Dimension(75, 20));
            clearSelection.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    numColoursSelected = 0;
                }

            });

            colourPicker.add(clearSelection);

            JButton submitSelection = new JButton("Submit");
            submitSelection.setPreferredSize(new Dimension(75, 20));
            submitSelection.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    numColoursSelected = 0;

                    // use clone or else when we assign as PBR!
                    board.feedback[board.getTries() - 1 - board.turn] = playerFeedback.clone();

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

                    board.turn++;

                    playerIsCodeSetter(blacks, whites);

                }

            });

            colourPicker.add(submitSelection);
        }

        mainPanel.add(feedbackPanel);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; // take the first row in the layout
        c.fill = GridBagConstraints.HORIZONTAL; // fill in width
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        frame.add(mainPanel, c);

        c.gridy = 1;
        frame.add(colourPicker, c);

        c.gridy = 2;
        frame.add(displayColours, c);
        frame.setVisible(true);
    }

    public CodeBreaker() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("CodeBreakers | Main Menu");

        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.BLUE);
        panel2.setOpaque(false);
        panel2.setLayout(new BorderLayout());

        JLabel label = new JLabel("Fading Panel");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(Color.WHITE);
        panel2.add(label, BorderLayout.CENTER);

        JPanel hero = new JPanel();
        BoxLayout heroLayout = new BoxLayout(hero, BoxLayout.Y_AXIS);
        hero.setLayout(heroLayout);
        Image logo = new ImageIcon("./cb/assets/logo.png").getImage();
        JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_FAST)),
                JLabel.CENTER);
        hero.add(heroLogo);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(0, 50, 0, 0)); // adjust to move over 50 pixels to the
        Image arrowIcon = new ImageIcon("./cb/assets/arrow.png").getImage(); // loading image as an icon
        // ------------------------------------------------
        ImageIcon arrowImg = new ImageIcon(arrowIcon.getScaledInstance(25, 25, Image.SCALE_FAST));
        // JPanel aiButtonPlay = new JPanel(new FlowLayout());
        JButton aiPlay = new JButton("CODE BREAKER", arrowImg);
        aiPlay.setFont(ForeverFontBold);
        aiPlay.setHorizontalTextPosition(JButton.LEFT);
        aiPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Player is code breaker");
                // playerIsCodeSetter(); fix later
                board = new Board();
                Game(gameFrame, true);
                setVisible(false);
            }
        });

        // aiButtonPlay.add(aiPlay);
        // aiButtonPlay.add(arrowImg);
        // ------------------------------------------------
        // JPanel personButtonPlay = new JPanel(new FlowLayout());
        JButton personPlay = new JButton("CODE SETTER", arrowImg);
        personPlay.setFont(ForeverFontBold);
        personPlay.setHorizontalTextPosition(JButton.LEFT);
        personPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Player is code setter");
                board = new Board();
                Game(gameFrame, false);
                playerIsCodeSetter(-1, -1);
                setVisible(false);
            }
        });

        JButton tutorial = new JButton("TUTORIAL", arrowImg);
        tutorial.setFont(ForeverFontBold);
        tutorial.setHorizontalTextPosition(JButton.LEFT);
        tutorial.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameFrame = new JFrame("Code Breakers | Tutorial");
                Tutorial(gameFrame);
                setVisible(false);
            }
        });

        // personButtonPlay.add(personPlay);
        // personButtonPlay.add(arrowImg2);
        // ------------------------------------------------
        // buttons.add(aiButtonPlay);
        // buttons.add(personButtonPlay);
        buttons.add(aiPlay);
        buttons.add(personPlay);
        buttons.add(tutorial);

        hero.setBackground(new Color(255, 190, 121));
        mainPanel.setBackground(new Color(255, 190, 121));
        buttons.setBackground(new Color(255, 190, 121));

        mainPanel.add(hero);
        mainPanel.add(buttons);
        add(mainPanel);
        pack();
        setVisible(true);

    }

    public static void main(String[] args) {
        // load assets
        LoadAssets();
        // intialize objects
        scan = new Scanner(System.in);

        new CodeBreaker();

        try {
            myWriter = new PrintWriter("data.txt");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // selfTest();
    }

    public static void playerIsCodeBreaker(int whites, int blacks) {
        // where 1 = black and 0 = white;
        int maxAttempts = board.getTries();

        if (blacks == board.getSize()) {
            finalMessage("You win with " + board.turn + " moves! ");
        } else if (board.turn == maxAttempts) {
            finalMessage("You lose! The code is: " + board.getCode());
            board.printCode();

        }
    }

    public static void playerIsCodeSetter(int blacks, int whites) {

        if (blacks == board.getSize()) {
            finalMessage("AI WINS");
        } else {

            System.out.println("Blacks: " + blacks);
            System.out.println("Whites: " + whites);
            String[] code = AI.guessCombo(blacks, whites);
            printArray(code);
            board.board[board.getTries() - 1 - board.turn] = code.clone();
            revalidateBoard();
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

    public static void finalMessage(String message) {
        feedbackPanel.removeAll();
        boardPanel.removeAll();
        colourPicker.removeAll();
        displayColours.removeAll();

        JLabel display = new JLabel(message);

        display.setFont(ForeverFontTitle);

        feedbackPanel.add(display);

        JButton backToMenu = new JButton("Back to Menu");
        backToMenu.setFont(ForeverFontBold);
        backToMenu.setHorizontalTextPosition(JButton.LEFT);
        backToMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CodeBreaker();
                gameFrame.setVisible(false);
            }
        });

        feedbackPanel.add(backToMenu);

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

    public static PrintWriter myWriter;

    public static void selfTest() {
        try {

            Board board = new Board();
            System.out.println("SELF TEST -------------------------");

            AICodeBreaker AI = new AICodeBreaker(board.getSize());
            AI.generateAllCombos(board.getSize());
            AI.printRemainingCombos(board.getSize());

            // long startTime = System.nanoTime();

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

                    if (blacks == board.getSize()) {
                        break;
                    }

                    attempts++;

                } while (true);

                totalAttempts += attempts;

                String msg = "ATTEMPT NUMBER " + (i + 1) + " : " + attempts;

                myWriter.write(msg);
                myWriter.write(System.lineSeparator());
                myWriter.write("----------------------------------");
                myWriter.write(System.lineSeparator());

                System.out.println(msg);
                attemptsArray[i] = attempts;

            }

            // long endTime = System.nanoTime();
            // long totalTime = endTime - startTime;

            // STANDARD PEROFRAMNCE TOTAL: 6455 AVERAGE: 4.98071 TIME: 1718856000 (nano
            // secs)
            float avg = totalAttempts / 1296f;
            System.out.println(
                    "TOTAL: " + totalAttempts + " AVERAGE: " + avg);

            int worstCase = 0;

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

            System.out.println("1 ATTEMPT: " + attemptsSort[0]);
            System.out.println("2 ATTEMPT: " + attemptsSort[1]);
            System.out.println("3 ATTEMPT: " + attemptsSort[2]);
            System.out.println("4 ATTEMPT: " + attemptsSort[3]);
            System.out.println("5 ATTEMPT: " + attemptsSort[4]);
            System.out.println("6 ATTEMPT: " + attemptsSort[5]);
            System.out.println("7+ ATTEMPT: " + attemptsSort[6]);
            System.out.println("WORST CASE: " + worstCase);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        myWriter.close();
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

    public static Color stringToColor(String colourName) {
        Color c = null;
        try {
            c = (Color) Color.class.getField(colourName).get(null);
            System.out.println(colourName);
            System.out.println(c);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (colourName != null)
                c = Color.decode(colourName);
        }
        return c;
    }

}