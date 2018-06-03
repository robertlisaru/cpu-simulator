package ro.ulbs.ac.simulator.assembler;

import java.util.StringTokenizer;

public class OperandParser {
    private AddressingMode addressingMode;
    private String register = "0000";
    private Short index;
    private Short immediateValue;

    public AddressingMode getAddressingMode() {
        return addressingMode;
    }

    public String getRegister() {
        return register;
    }

    public Short getIndex() {
        return index;
    }

    public Short getImmediateValue() {
        return immediateValue;
    }

    public void parse(String input) {
        if (input.matches("\\d+\\([rR]\\d{1,2}\\)")) {
            addressingMode = AddressingMode.INDEXED;
            StringTokenizer operandTokenizer = new StringTokenizer(input, "()");
            try {
                index = Short.parseShort(operandTokenizer.nextToken());
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("invalid index");
            }
            try {
                register = to4BitsString(operandTokenizer.nextToken());
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("invalid register");
            }
        } else if (input.matches("\\([rR]\\d{1,2}\\)")) {
            addressingMode = AddressingMode.INDIRECT;
            register = to4BitsString(input);
        } else if (input.matches("[rR]\\d{1,2}")) {
            addressingMode = AddressingMode.DIRECT;
            register = to4BitsString(input);
        } else {
            addressingMode = AddressingMode.IMMEDIATE;
            try {
                immediateValue = Short.parseShort(input);
            } catch (NumberFormatException nfe) {
                throw new CallLabelException(input);
            }
        }
    }

    private String to4BitsString(String input) {
        input = input.replaceAll("\\D", "");
        String newRegister;
        try {
            newRegister = Integer.toBinaryString(Integer.parseInt(input));
            while (newRegister.length() < 4) {
                newRegister = "0" + newRegister;
            }
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("invalid register");
        }
        return newRegister;
    }
}
