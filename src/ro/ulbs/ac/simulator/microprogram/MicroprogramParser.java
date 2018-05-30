package ro.ulbs.ac.simulator.microprogram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MicroprogramParser {
    public MicroprogramMemory parseFile(File microprogramFile) throws IOException {
        MicroprogramMemory microprogramMemory = new MicroprogramMemory();
        Map<String, Short> labels = new HashMap<>();

        //<editor-fold desc="calculate labels">
        FileReader fileReader = new FileReader(microprogramFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        for (Short lineNumber = 0; (line = bufferedReader.readLine()) != null; lineNumber++) {
            StringTokenizer microcommandTokenizer = new StringTokenizer(line, ",");
            if (microcommandTokenizer.countTokens() == 12) {
                labels.put(microcommandTokenizer.nextToken(), lineNumber);
            }
            if (microcommandTokenizer.countTokens() != 11) {
                throw new RuntimeException("11 commands expected on line" + lineNumber);
            }
        }
        bufferedReader.close();
        fileReader.close();
        //</editor-fold>

        fileReader = new FileReader(microprogramFile);
        bufferedReader = new BufferedReader(fileReader);
        for (Byte lineNumber = 0; (line = bufferedReader.readLine()) != null; lineNumber++) {
            Microinstruction microinstruction = new Microinstruction();
            StringTokenizer microcommandTokenizer = new StringTokenizer(line, ",");
            if (microcommandTokenizer.countTokens() == 12) {
                microcommandTokenizer.nextToken();
            }

            String microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setSursaSBUS(SursaSBUS.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid SursaSBUS microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setSursaDBUS(SursaDBUS.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid SursaDBUS microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setOperatieALU(OperatieALU.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid OperatieALU microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setSursaRBUS(SursaRBUS.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid SursaRBUS microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setDestinatieRBUS(DestinatieRBUS.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid DestinatieRBUS microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setOperatieMemorie(OperatieMemorie.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid OperatieMemorie microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setOtherOperation(OtherOperation.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid OtherOperation microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setConditieSalt(ConditieSalt.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid ConditieSalt microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            try {
                microinstruction.setIndexSalt(IndexSalt.valueOf(microcomand));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid index salt microcommand: \"" + microcomand + "\" on line " + lineNumber);
            }

            microcomand = microcommandTokenizer.nextToken();
            microinstruction.setJumpOnConditionEqualsFalse(microcomand.equals("1"));

            microcomand = microcommandTokenizer.nextToken();
            if (!microcomand.equals("NONE")) {
                Short jumpAddress = labels.get(microcomand);
                if (jumpAddress == null) {
                    throw new RuntimeException("invalid jump label on line " + lineNumber);
                }
                microinstruction.setMicroadresaSalt(jumpAddress);
            }

            microprogramMemory.getMicroinstructionList().add(microinstruction);
        }

        return microprogramMemory;
    }
}
