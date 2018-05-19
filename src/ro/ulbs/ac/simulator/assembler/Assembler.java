package ro.ulbs.ac.simulator.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Assembler {
    private int codeAddressCounter = 0;
    private int dataAddressCounter = 0;
    private List<Byte> code = new ArrayList<>();
    private List<Byte> data = new ArrayList<>();
    private File asmFile;
    private Map<String, Integer> variables = new HashMap<>();
    private Map<String, Integer> labels = new HashMap<>();
    private int lineCount = 0;
    private List<Error> errorList = new ArrayList<>();
    private Map<String, String> opcodes = new HashMap<>();

    public Assembler(File asmFile) {
        this.asmFile = asmFile;
    }

    public void readOpcodesFromFile(File opcodesFile) throws FileNotFoundException {
        Scanner scanner = new Scanner(opcodesFile);
        while (scanner.hasNext()) {
            String opcode = scanner.next();
            String mnemonic = scanner.next();
            opcodes.put(mnemonic, opcode);
        }
    }

    public void parseLines() throws IOException {
        FileReader fileReader = new FileReader(asmFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        //<editor-fold desc="data parsing">
        while ((line = bufferedReader.readLine()) != null) {
            lineCount++;
            if (line.equals("")) {
                continue;
            }
            if (line.contains(";")) {
                line = line.substring(0, line.indexOf(";"));
            }
            if (line.contains(".code")) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line, " ,");
            String label = null;
            String size = null;
            String value = null;
            if (st.hasMoreTokens()) {
                label = st.nextToken();
                if (st.hasMoreTokens()) {
                    size = st.nextToken();
                    if (st.hasMoreTokens()) {
                        variables.put(label, dataAddressCounter);
                        value = st.nextToken();
                        ByteBuffer b;
                        switch (size) {
                            case "db":
                                data.add(Byte.parseByte(value));
                                dataAddressCounter += 1;
                                break;
                            case "dw":
                                b = ByteBuffer.allocate(2);
                                b.order(ByteOrder.LITTLE_ENDIAN);
                                b.putShort(Short.parseShort(value));
                                data.add(b.array()[0]);
                                data.add(b.array()[1]);
                                dataAddressCounter += 2;
                                break;
                            case "dd":
                                b = ByteBuffer.allocate(4);
                                b.order(ByteOrder.LITTLE_ENDIAN);
                                b.putInt(Integer.parseInt(value));
                                data.add(b.array()[0]);
                                data.add(b.array()[1]);
                                data.add(b.array()[2]);
                                data.add(b.array()[3]);
                                dataAddressCounter += 4;
                                break;
                            default:
                                errorList.add(new Error(lineCount, "invalid variable declaration."));
                                break;
                        }
                    }
                }
            }
            if (label == null || size == null || value == null) {
                errorList.add(new Error(lineCount, "invalid variable declaration."));
            }
        }
        //</editor-fold>
        //<editor-fold desc="code parsing">
        while ((line = bufferedReader.readLine()) != null) {
            lineCount++;
            StringTokenizer st = new StringTokenizer(line, " ,");
            StringBuilder instructionBits = new StringBuilder();
            if (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.contains(":")) {
                    token = token.substring(0, token.indexOf(":"));
                    labels.put(token, codeAddressCounter);
                    //todo: backpatching
                } else {
                    if (opcodes.containsKey(token)) {
                        instructionBits.append(opcodes.get(token));
                        switch (instructionBits.length()) {
                            case 4:
                                String MAD;
                                Short index;
                                Byte RD;
                                if (st.hasMoreTokens()) {
                                    String operand = st.nextToken();
                                    if (operand.matches("\\d\\(")) {
                                        MAD = "11";
                                        StringTokenizer operandTokenizer = new StringTokenizer(operand, "()");
                                        try {
                                            index = Short.parseShort(operandTokenizer.nextToken());
                                        } catch (NumberFormatException nfe) {
                                            errorList.add(new Error(lineCount, "invalid index"));
                                            continue;
                                        }
                                        try{
                                            RD = Byte.parseByte(operandTokenizer.nextToken()
                                                    .replaceFirst("r", ""));
                                        }catch(NumberFormatException nfe){
                                            errorList.add(new Error(lineCount, "invalid register"));
                                            continue;
                                        }
                                    }
                                } else {
                                    errorList.add(new Error(lineCount, "missing operands."));
                                }
                                break;
                            case 8:
                                break;
                            case 10:
                                break;
                            case 16:
                                ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                byteBuffer.putShort(Short.parseShort(instructionBits.toString(), 2));
                                code.add(byteBuffer.array()[0]);
                                code.add(byteBuffer.array()[1]);
                                codeAddressCounter += 2;
                                break;
                            default:
                                break;
                        }
                    } else {
                        errorList.add(new Error(lineCount, "unknown instruction " + token));
                        continue;
                    }
                }
            }
        }
        //</editor-fold>
        fileReader.close();
    }
}
