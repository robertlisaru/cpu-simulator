package ro.ulbs.ac.simulator.assembler;

import java.util.HashMap;
import java.util.Map;

public class Assembler {
    private int codeAddressCounter;
    private int dataAddressCounter;
    private byte[] code;
    private byte[] data;
    private String asmPath;
    private Map<String, Integer> variables = new HashMap<>();
    private Map<String, Integer> labels = new HashMap<>();

}
