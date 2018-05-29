import ro.ulbs.ac.simulator.architecture.Flag;
import ro.ulbs.ac.simulator.assembler.Assembler;
import ro.ulbs.ac.simulator.microprogram.MicroprogramParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Flag flag = new Flag();
        flag.fromShort(Integer.valueOf(14).shortValue());
        MicroprogramParser microprogramParser = new MicroprogramParser();
        try {
            microprogramParser.parseFile(new File("ucode.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }
}
