import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeBreaker extends JFrame implements ActionListener {
    final String fontColour = "#374151";
    final String backgroundColour = "#FFBE79";
    private static Board board;
    private boolean pvp;
    private int turn;

    static Scanner scan; // is this allowed????

    public CodeBreaker() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel panel = new JPanel(new GridBagLayout());
        Image logo = new ImageIcon("./assets/logo.png").getImage();
        JLabel heroLogo = new JLabel(new ImageIcon(logo.getScaledInstance(600, 374, Image.SCALE_DEFAULT)), JLabel.CENTER);

        JButton aiPlay = new JButton("JACKIE CHAN");
        JButton personPlay = new JButton("JACKIE BLACK");
        panel.add(heroLogo);
        panel.add(aiPlay);
        panel.add(personPlay);
        add(panel);
        pack();
        setVisible(true);
    }
    public static void main(String[] args) {
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