package ro.ulbs.ac.simulator.architecture;

public enum InterruptSignal {
    ACLOW(0),
    CIL(1),
    IRQ0(2),
    IRQ1(3),
    IRQ2(4),
    IRQ3(5),
    IRQ4(6),
    IRQ5(7);

    private final int value;

    InterruptSignal(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
