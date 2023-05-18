
// an object for a "colored token" used as part of the code for Mastermind

class Token {

    static int number = 0; //

    // the string representation of the token
    private String s;
    private int id;

    // constructor
    Token(String s) {
        this.s = s;
        id = number;
        number++;
    }

    // compare 2 tokens
    public boolean equals(Token t) {

        if (t == null) {
            return false;
        }

        if (this.s.equals(t.s)) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return this.s;
    }
}