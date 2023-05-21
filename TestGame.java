import java.io.*;
import java.util.*;

public class TestGame {
    public static void main(String[] args) {

        Board b = new Board();
        String[] guess = { "G", "B", "O", "B" };

        b.checkGuess(guess, 0);
    }
}
