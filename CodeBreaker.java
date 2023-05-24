import java.io.*;
import java.security.AllPermission;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CodeBreaker {

    private static Board board;
    private boolean pvp;
    private int turn;

    static Scanner scan; // is this allowed????

    public static void main(String[] args) {

        // intialize objects
        scan = new Scanner(System.in);
        board = new Board();

        boolean quitGame = false;

        do {

            System.out.println("Welcome to the mastermind game!");
            System.out.println("Menu:");
            System.out.println("   1. play against an AI as a code breaker");
            System.out.println("   2. play against an AI as a code setter");
            System.out.println("   3. test all combinations");
            System.out.println("   4. Leave");

            int userInput = Integer.parseInt(scan.nextLine());

            if (userInput == 1) {
                playerIsCodeBreaker();
            } else if (userInput == 2) {
                playerIsCodeSetter();
            } else if (userInput == 3) {
                selfTest();
            } else {
                quitGame = true;
            }

        } while (!quitGame);
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

    public static void selfTest() {
        System.out.println("SELF TEST -------------------------");

        AICodeBreaker AI = new AICodeBreaker(board.getSize());
        AI.generateAllCombos(board.getSize());

        long startTime = System.nanoTime();

        int totalAttempts = 0;
        for (int i = 0; i < AI.allCombos.length; i++) {

            AI.remainingCombos = AI.allCombos.clone();

            int attempts = 1;
            int blacks = -1;
            int whites = -1;

            do {
                String[] code = AI.playGuess(blacks, whites);
                String[] feedback = board.checkGuess(code, AI.allCombos[i], 0);
                int[] pegHolder = board.returnPegs(feedback);
                blacks = pegHolder[1];
                whites = pegHolder[0];

                if (blacks == board.getSize()) {
                    break;
                }

                attempts++;

            } while (true);

            totalAttempts += attempts;
            System.out.println("Attempts: " + attempts);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;

        // STANDARD PEROFRAMNCE TOTAL: 6455 AVERAGE: 4.98071 TIME: 1718856000 (nano
        // secs)
        float avg = totalAttempts / 1296f;
        System.out.println(
                "TOTAL: " + totalAttempts + " AVERAGE: " + avg + " TIME: " + totalTime);

    }

    public static void printArray(String[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

}