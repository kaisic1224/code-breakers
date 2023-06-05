package cb;

import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import cb.Board.Colour;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class CodeBreaker extends JFrame {
    final String fontColour = "#374151";
    final String backgroundColour = "#FFBE79";
    static String black900 = "#000814";
    static String navy800 = "#001d3d";
    static String navy600 = "#003566";
    static String yellow700 = "#ffc300";
    static String yellow500 = "#ffd60a";
    private static Board board;

    static Font ForeverFontBold = null;
    static Font ForeverFontTitle = null;
    static Font ForeverFontNormal = null;
    static Font ForeverFont;
    static Font InsomniaFont;
    static Font InsomniaFontBold = null;
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
            InsomniaFont = Font.createFont(Font.TRUETYPE_FONT, new File("./cb/assets/Insomnia.ttf"));
        } catch (Exception e) {
            System.out.println("File could not be found, or error parsing font");
        }
        ForeverFontBold = ForeverFont.deriveFont(Font.BOLD, 16f);
        ForeverFontNormal = ForeverFont.deriveFont(16f);
        ForeverFontTitle = ForeverFont.deriveFont(Font.BOLD, 100f);
        InsomniaFontBold = InsomniaFont.deriveFont(Font.BOLD, 100f);
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
        try {
            myWriter = new PrintWriter("data.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JLabel title = new JLabel("BREAK THE ");
        JLabel title2 = new JLabel("CODE");
        title.setFont(InsomniaFontBold);
        title2.setFont(InsomniaFontBold);
        title.setForeground(stringToColor(black900));
        title2.setForeground(stringToColor(yellow500));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title2.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel titles = new JPanel();
        titles.setBackground(stringToColor(navy600));
        titles.add(title);
        titles.add(title2);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(stringToColor(navy600));

        ArrayList<JButton> playerCode = new ArrayList<JButton>();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel encompassingBoard = new JPanel(new GridBagLayout());
        JPanel encompassingFeedback = new JPanel(new GridBagLayout());
        JLabel boardLabel = new JLabel("Guess");
        boardLabel.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel feedbackLabel = new JLabel("Feedback");
        feedbackLabel.setHorizontalAlignment(SwingConstants.LEFT);
        boardPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));
        boardPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        feedbackPanel = new JPanel(new GridLayout(board.getTries(), board.getSize()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1;

        encompassingBoard.add(boardLabel, gbc);
        encompassingFeedback.add(feedbackLabel, gbc);
        gbc.gridy = 1;
        encompassingBoard.add(boardPanel, gbc);
        encompassingFeedback.add(feedbackPanel, gbc);

        revalidateBoard();
        revalidateFeedback();

        mainPanel.add(encompassingBoard);
        mainPanel.add(encompassingFeedback);

        JPanel colourPicker = new JPanel(new FlowLayout());
        JPanel displayColours = new JPanel(new FlowLayout());

        if (isCodeBreaker) {
            // BufferedImage sheet = loadSpriteSheet("./cb/assets/sprites/1.png");
            // ArrayList<BufferedImage> frames = getFrames(sheet, 50, 50);
            // for (int i = 0; i < frames.size(); i++) {
            // Image f = new ImageIcon(frames.get(i)).getImage();
            // JLabel xd = new JLabel(new ImageIcon(f.getScaledInstance(200, 200,
            // Image.SCALE_FAST)));

            // mainPanel.removeAll();
            // mainPanel.add(xd);
            // try {
            // Thread.sleep(12);
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // mainPanel.revalidate();
            // mainPanel.repaint();
            // }

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
                Color c = stringToColor(Colour.values()[i].toString().toUpperCase());
                peg.setBackground(c);

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
                        displayColours.add(code);
                        displayColours.revalidate();
                        displayColours.repaint();
                        clearAll.setEnabled(true);
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
                    submit.setEnabled(false);
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                }
            });
            colourPicker.add(clearAll);

            submit.setEnabled(false);
            submit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Component[] guessColours = displayColours.getComponents();
                    String[] colors = new String[4];

                    for (int i = 0; i < guessColours.length; i++) {
                        colors[i] = colorToString(guessColours[i].getBackground());
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
                            Color c = stringToColor(colourName);
                            cell.setBackground(c);

                            feedbackPanel.add(cell);
                        }
                    }
                    feedbackPanel.revalidate();
                    feedbackPanel.repaint();

                    board.board[board.getTries() - 1 - board.turn] = colors;
                    revalidateBoard();
                    playerIsCodeBreaker(pegs[0], pegs[1]);

                    playerCode.clear();
                    displayColours.removeAll();
                    displayColours.revalidate();
                    displayColours.repaint();
                    board.turn++;
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
        c.fill = GridBagConstraints.NONE; // fill in width
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1.0;
        frame.add(titles, c);
        
        c.gridy = 1;
        frame.add(mainPanel, c);

        c.gridy = 2;
        frame.add(colourPicker, c);

        c.gridy = 3;
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
                board = new Board();
                Game(gameFrame, true);
                setVisible(false);
            }
        });

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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CodeBreaker();
            }
        });
        // selfTest();
    }

    public static void playerIsCodeBreaker(int whites, int blacks) {
        // where 1 = black and 0 = white;
        int maxAttempts = board.getTries();

        if (blacks == board.getSize()) {
            finalMessage("You win with " + board.turn + " moves! ");
        } else if (board.turn == maxAttempts) {
            System.out.println("You lose! The code is: ");
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

                if (colourName != null) {
                    cell.setOpaque(true);
                    Color c = stringToColor(colourName);
                    cell.setBackground(c);
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

            for (int i = 0; i < 10; i++) {

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

    public static BufferedImage loadSpriteSheet(String file) {
        BufferedImage sheet = null;

        try {
            sheet = ImageIO.read(new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sheet;
    }

    public static BufferedImage getSprite(BufferedImage spriteSheet, int tileSize, int xPos, int yPos) {
        if (spriteSheet == null) {
            return null;
        }

        return spriteSheet.getSubimage(xPos * tileSize, yPos * tileSize, tileSize, tileSize);
    }

    public static ArrayList<BufferedImage> getFrames(BufferedImage spritesheet, int tileSize, int maxFrames) {
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
        for (int i = 0; i < maxFrames; i++) {
            BufferedImage frame = getSprite(spritesheet, tileSize, i, 1);
            frames.add(frame);
        }

        return frames;

    }

    // hours wasted starting now: 1 - solved
    public static String colorToString(Color c) {
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
}