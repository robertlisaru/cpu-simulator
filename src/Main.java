import ro.ulbs.ac.simulator.architecture.Architecture;
import ro.ulbs.ac.simulator.architecture.IndexSelectionBlock;
import ro.ulbs.ac.simulator.assembler.Assembler;
import ro.ulbs.ac.simulator.microprogram.IndexSalt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Architecture architecture = new Architecture();

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
        try {
            assembler.makeBin();
        } catch (IOException e) {
            e.printStackTrace();
        }

        architecture.loadCode(assembler.getCode());
        architecture.loadData(assembler.getData());

        System.out.println((new IndexSelectionBlock()).select(IndexSalt.SINGLE_OP_INDEX, Integer.valueOf(0x8395).shortValue()));
    }
}
