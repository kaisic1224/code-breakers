import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Executable for Mastermind, game setup and initialization
// A mastermind Algorithm for variable # positions and variable # code tokens
public class Mastermind {

    /**********************************************************************************************************/
    /*******************************************
     * MAIN / RUN THE GAME
     ********************************************/
    /**********************************************************************************************************/

    public static void main(String[] args) throws IOException {

        BufferedReader buffy = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Welcome to Mastermind! Enter the tokens you would like to use, separated by spaces:");

        String[] derp = buffy.readLine().split("\\s+");

        Token[] tokens = new Token[derp.length];

        // generate the tokens to be used
        for (int i = 0; i < derp.length; i++) {
            tokens[i] = new Token(derp[i]);
        }

        System.out.println("How many positions?");
        int np = Integer.parseInt(buffy.readLine()); // read returns an wrong int, so use readLine and parse to cast

        /************************** THE GAME LOOP ****************************/

        String response;

        do {

            System.out.println("Starting new game...");
            MastermindGame game = new MastermindGame(tokens, np);

            for (int i = 0; i < game.allCombos.length; i++) {

                int attempts = 1;
                int blackPegs = -1;
                int whitePegs = -1;

                Token[] code = new Token[np];
                Token[] sol = game.allCombos[i];
                game.remainingCombos = game.allCombos.clone();

                do {

                    code = game.playGuess(blackPegs, whitePegs);

                    int[] temp = game.countPegs(sol, code);

                    blackPegs = temp[1];
                    if (blackPegs == np) {
                        break;
                    }

                    whitePegs = temp[0];
                    attempts++;
                } while (true);

                System.out.println(attempts);
            }

            System.out.println("Play again? y/n");
            response = buffy.readLine();
        } while (response.equals("y"));

        System.out.println("gg");
        buffy.close();
    }

    /*********************************************************************************************/
    /*******************************************
     * GENERAL PURPOSE
     ***********************************/
    /*********************************************************************************************/

    static void print2DTokenArray(Token[][] a) {
        for (int i = 0; i < a.length; i++) {
            printTokenArray(a[i]);
        }
    }

    static void printTokenArray(Token[] t) {
        if (t == null) {
            return;
        }

        for (int i = 0; i < t.length; i++) {
            System.out.print(t[i] + "\t");
        }
        System.out.println();
    }

    // compare two Token[]s
    static boolean tokenArrayEquals(Token[] one, Token[] two) {

        if (one.length != two.length) {
            return false;
        }

        for (int i = 0; i < one.length; i++) {
            if (!one[i].equals(two[i])) {
                return false;
            }
        }

        return true;
    }

}