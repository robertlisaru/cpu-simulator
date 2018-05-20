import ro.ulbs.ac.simulator.assembler.Assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Assembler assembler = new Assembler(new File("myProgram.asm"));
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
    }
}
