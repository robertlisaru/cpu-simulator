import ro.ulbs.ac.simulator.architecture.Architecture;
import ro.ulbs.ac.simulator.assembler.Assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Architecture architecture = new Architecture();

        Assembler assembler = new Assembler(new File("simpleAdd.asm"));
        try {
            assembler.readOpcodesFromFile(new File("opcodes.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assembler.parseFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assembler.makeBin();
        } catch (IOException e) {
            e.printStackTrace();
        }

        architecture.loadCode(assembler.getCode());
        architecture.loadData(assembler.getData());
        architecture.executeAll();
    }
}
