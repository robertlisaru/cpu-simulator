package ro.ulbs.ac.simulator.architecture;

import ro.ulbs.ac.simulator.microprogram.OperatieALU;

public class Architecture {
    private Short SBUS;
    private Short DBUS;
    private Short RBUS;
    private Short[] registerFile = new Short[16];
    private Flag flag = new Flag();
    private Short SP;
    private Short T;
    private Short PC;
    private Short IVR;
    private Short ADR;
    private Short MDR;
    private Short IR;
    private CodeMemory codeMemory;
    private DataMemory dataMemory;

    private Short alu(OperatieALU operatieALU) {
        switch (operatieALU) {
            case SUM:
                return Integer.valueOf(SBUS + DBUS).shortValue();
            case SBUS:
                return SBUS;
            case AND:
                return Integer.valueOf(SBUS & DBUS).shortValue();
            case OR:
                return Integer.valueOf(SBUS | DBUS).shortValue();
            case XOR:
                return Integer.valueOf(SBUS ^ DBUS).shortValue();
        }
        throw new RuntimeException("invalid ALU operation");
    }


}
