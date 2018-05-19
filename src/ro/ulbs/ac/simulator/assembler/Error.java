package ro.ulbs.ac.simulator.assembler;

public class Error {
    private int lineNumber;
    private String message;

    public Error(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Error on line " + lineNumber + ": " + message;
    }
}
