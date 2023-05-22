import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeBreaker extends JFrame implements ActionListener {
    final String fontColour = "#374151";
    final String backgroundColour = "#FFBE79";
    private static Board board;
    private boolean pvp;
    private int turn;
    
    static Font ForeverFontBold = null;
    static Scanner scan; // is this allowed????
    
    public static void LoadAssets() {
        Font ForeverFont = null;
        try {
            ForeverFont = Font.createFont(Font.TRUETYPE_FONT, new File("./assets/Forever.ttf"));
        } catch (Exception e) {
            System.out.println("File could not be found, or error parsing font");
        }
        ForeverFontBold = ForeverFont.deriveFont(Font.BOLD, 16f);
    }

    public static void Game(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);


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
        Image logo = new ImageIcon("./assets/logo.png").getImage();
        JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_FAST)),
                JLabel.CENTER);
        hero.add(heroLogo);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(0, 50, 0, 0)); // adjust to move over 50 pixels to the
        Image arrowIcon = new ImageIcon("./assets/arrow.png").getImage(); // loading image as an icon
        // ------------------------------------------------
        ImageIcon arrowImg = new ImageIcon(arrowIcon.getScaledInstance(25, 25, Image.SCALE_FAST));
        // JPanel aiButtonPlay = new JPanel(new FlowLayout());
        JButton aiPlay = new JButton("JACKIE CHAN", arrowImg);
        aiPlay.setFont(ForeverFontBold);
        aiPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame gameFrame = new JFrame("Code Breakers | Game");
                Game(gameFrame);
                setVisible(false);
            }
        });

        // aiButtonPlay.add(aiPlay);
        // aiButtonPlay.add(arrowImg);
        // ------------------------------------------------
        // JPanel personButtonPlay = new JPanel(new FlowLayout());
        JButton personPlay = new JButton("JACKIE BLACK", arrowImg);
        personPlay.setFont(ForeverFontBold);

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
        board = new Board();
        new CodeBreaker();

        boolean quitGame = false;
    }

    public static void playerIsCodeBreaker() {

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

    public static void playerIsCodeSetter() {
        AICodeBreaker AI = new AICodeBreaker(board.getSize());
        AI.generateAllCombos(board.getSize());

        int blacks = -1;
        int whites = -1;

        do {
            String[] code = AI.playGuess(blacks, whites);

            System.out.println("Remaining Combos:");
            AI.printRemainingCombos(board.getSize());
            System.out.println("Guessing:");
            for (int i = 0; i < code.length; i++) {
                System.out.print(code[i]);
            }
            System.out.println(" ");

            // get black and white peg counts
            System.out.println("How many black pegs?");
            blacks = Integer.parseInt(scan.nextLine());
            if (blacks == board.getSize()) {
                break;
            }
            System.out.println("How many white pegs?");
            whites = Integer.parseInt(scan.nextLine());
        } while (true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }

}