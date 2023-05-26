package cb;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import cb.Board.Colour;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CodeBreaker extends JFrame implements ActionListener {
    final String fontColour = "#374151";
    final String backgroundColour = "#FFBE79";
    private static Board board;
    private boolean pvp;
    private int turn;

    static Font ForeverFontBold = null;
    static Scanner scan; // is this allowed????

    static Color[] feedbackColours = { Color.black, Color.white };
    static int numColoursSelected = 0;
    static String[] playerFeedback = new String[4];
    static int attempts = 0;
    static AICodeBreaker AI;

    public static JPanel boardPanel;
    public static JPanel feedbackPanel;

    public static void LoadAssets() {
        Font ForeverFont = null;
        try {
            ForeverFont = Font.createFont(Font.TRUETYPE_FONT, new File("./cb/assets/Forever.ttf"));
        } catch (Exception e) {
            System.out.println("File could not be found, or error parsing font");
        }
        ForeverFontBold = ForeverFont.deriveFont(Font.BOLD, 16f);
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

        JPanel colourPicker = new JPanel(new FlowLayout());
        JPanel displayColours = new JPanel(new FlowLayout());
        // Graphics g = colourPicker.getGraphics();
        // g.fillOval(20, 20, 10, 10);

        if (isCodeBreaker) {
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
                colourPicker.add(peg);
            }
        } else {
            AI = new AICodeBreaker(board.getSize());

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

                    if (numColoursSelected == 4) {
                        displayColours.removeAll();
                        displayColours.revalidate();
                        displayColours.repaint();
                        numColoursSelected = 0;

                        // use clone or else when we assign as PBR!
                        board.feedback[board.getTries() - 1 - attempts] = playerFeedback.clone();

                        revalidatFeedback();

                        int blacks = 0;
                        int whites = 0;

                        for (int i = 0; i < playerFeedback.length; i++) {
                            if (feedbackColours[0].toString().equals(playerFeedback[i])) {
                                blacks++;
                            } else {
                                whites++;
                            }
                        }

                        attempts++;
                        playerIsCodeSetter(blacks, whites);

                    }

                }

            });

            colourPicker.add(submitSelection);
        }
        mainPanel.add(feedbackPanel);

        JPanel guess = new JPanel(new FlowLayout());
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
                    if (playerCode.size() == 4)
                        return;
                    playerCode.add(peg);
                    JLabel code = new JLabel("");
                    code.setPreferredSize(new Dimension(50, 50));
                    code.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    code.setOpaque(true);
                    code.setBackground(peg.getBackground());
                    guess.add(code);
                    guess.revalidate();
                    guess.repaint();
                    if (playerCode.size() == 4) {
                        clearAll.setEnabled(true);
                    }
                }
            });
            colourPicker.add(peg);
        }

        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerCode.clear();
                clearAll.setEnabled(false);
                guess.removeAll();
                guess.revalidate();
                guess.repaint();
            }
        });
        colourPicker.add(clearAll);

        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                guess.removeAll();
            }
        });

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
        frame.add(guess, c);

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
                JFrame gameFrame = new JFrame("Code Breakers | Player is code breaker");
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
                JFrame gameFrame = new JFrame("Code Breakers | Player is code setter");
                board = new Board();
                Game(gameFrame, false);
                playerIsCodeSetter(-1, -1);
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

    }

    public static void playerIsCodeBreaker() {
        board = new Board();

        board.generateCode();
        board.printCode();

        int maxAttempts = board.getTries();
        int attempts = 1;
        boolean finishedGame = false;

        do {
            System.out.println(
                    "Enter your guess (seperated by spaces): " + " (attempt " + attempts + " / " + maxAttempts + ")");

            String[] userGuess = scan.nextLine().split(" ");
            String[] feedBack = board.checkGuess(userGuess, board.getCode(), attempts - 1);

            // where 1 = black and 0 = white;
            int[] pegHolder = board.returnPegs(feedBack);

            System.out.println("Feedback: " + pegHolder[1] + " blacks , " + pegHolder[0] + " whites");

            if (pegHolder[1] == board.getSize()) {
                System.out.println("You win with " + attempts + " moves! ");
                finishedGame = true;
            } else if (attempts == maxAttempts) {
                System.out.println("You lose! The code is: ");
                board.printCode();

                finishedGame = true;
            }

            attempts++;

        } while (!finishedGame);
    }

    public static void playerIsCodeSetter(int blacks, int whites) {

        if (blacks == board.getSize()) {
            System.out.println("YAZERS");
        } else {
            AI.generateAllCombos(board.getSize());

            String[] code = AI.playGuess(blacks, whites);
            board.board[board.getTries() - 1 - attempts] = code;
            revalidatBoard();
        }

    }

    public static void revalidatBoard() {
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

    public static void revalidatFeedback() {
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

}