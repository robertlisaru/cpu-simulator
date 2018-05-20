package ro.ulbs.ac.simulator.assembler;

public enum AddressingMode {
    IMMEDIATE("00"),
    DIRECT("01"),
    INDIRECT("10"),
    INDEXED("11");

    private final String text;

    AddressingMode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
