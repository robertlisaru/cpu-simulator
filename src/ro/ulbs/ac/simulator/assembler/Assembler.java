package ro.ulbs.ac.simulator.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Assembler {
    private ByteBuffer code = ByteBuffer.allocate(65536);
    private ByteBuffer data = ByteBuffer.allocate(65536);
    private File asmFile;
    private Map<String, Short> variables = new HashMap<>();
    private Map<String, Short> labels = new HashMap<>();
    private int lineCount = 0;
    private List<Error> errorList = new ArrayList<>();
    private Map<String, String> opcodes = new HashMap<>();
    private Map<String, List<Short>> unknownOffsets = new HashMap<>();
    private Map<String, List<Short>> unknownLabels = new HashMap<>();

    public Assembler() {
        data.order(ByteOrder.LITTLE_ENDIAN);
        code.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void setAsmFile(File asmFile) {
        this.asmFile = asmFile;
    }

    public void readOpcodesFromFile(File opcodesFile) throws FileNotFoundException {
        Scanner scanner = new Scanner(opcodesFile);
        while (scanner.hasNext()) {
            String opcode = scanner.next();
            String mnemonic = scanner.next();
            opcodes.put(mnemonic.toLowerCase(), opcode);
        }
    }

    public void parseFile() throws IOException {
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
            String varName = null;
            String size = null;
            String value = null;
            if (st.hasMoreTokens()) {
                varName = st.nextToken();
                if (st.hasMoreTokens()) {
                    size = st.nextToken();
                    if (st.hasMoreTokens()) {
                        value = st.nextToken();
                        switch (size) {
                            case "db":
                                variables.put(varName, new Integer(data.position()).shortValue());
                                data.put(Byte.parseByte(value));
                                break;
                            case "dw":
                                variables.put(varName, new Integer(data.position()).shortValue());
                                data.putShort(Short.parseShort(value));
                                break;
                            case "dd":
                                variables.put(varName, new Integer(data.position()).shortValue());
                                data.putInt(Integer.parseInt(value));
                                break;
                            default:
                                errorList.add(new Error(lineCount, "invalid variable declaration."));
                                break;
                        }
                    }
                }
            }
            if (varName == null || size == null || value == null) {
                errorList.add(new Error(lineCount, "invalid variable declaration."));
            }
        }
        //</editor-fold>
        //<editor-fold desc="code parsing">
        while ((line = bufferedReader.readLine()) != null) {
            lineCount++;
            StringTokenizer lineTokenizer = new StringTokenizer(line, " ,");
            StringBuilder instructionBits = new StringBuilder();
            if (lineTokenizer.hasMoreTokens()) {
                String token = lineTokenizer.nextToken();
                if (token.contains(":")) {
                    String label = token.replaceAll("\\W", "");
                    label = label.toLowerCase();
                    labels.put(label, new Integer(code.position()).shortValue());
                    //<editor-fold desc="backpatching">
                    if (unknownOffsets.containsKey(label)) {
                        for (Short address : unknownOffsets.get(label)) {
                            code.put(address, new Integer(labels.get(label) - (address + 2)).byteValue());
                        }
                        unknownOffsets.remove(label);
                    }
                    if (unknownLabels.containsKey(label)) {
                        for (Short address : unknownLabels.get(label)) {
                            code.putShort(address, labels.get(label));
                        }
                        unknownLabels.remove(label);
                    }
                    //</editor-fold>
                } else {
                    token = token.toLowerCase();
                    if (opcodes.containsKey(token)) {
                        instructionBits.append(opcodes.get(token));
                        switch (instructionBits.length()) {
                            case 4:
                                //<editor-fold desc="2 operand instruction">
                                if (lineTokenizer.hasMoreTokens()) {
                                    String rawOperand = lineTokenizer.nextToken();
                                    OperandParser destinationOperand = new OperandParser();
                                    try {
                                        destinationOperand.parse(rawOperand);
                                    } catch (RuntimeException re) {
                                        errorList.add(new Error(lineCount, re.getMessage()));
                                        continue;
                                    }
                                    if (lineTokenizer.hasMoreTokens()) {
                                        rawOperand = lineTokenizer.nextToken();
                                        OperandParser sourceOperand = new OperandParser();
                                        try {
                                            sourceOperand.parse(rawOperand);
                                            instructionBits.append(sourceOperand.getAddressingMode());
                                            instructionBits.append(sourceOperand.getRegister());
                                            instructionBits.append(destinationOperand.getAddressingMode());
                                            instructionBits.append(destinationOperand.getRegister());
                                            code.putShort(new Integer(Integer.parseInt(
                                                    instructionBits.toString(), 2)).shortValue());
                                            if (sourceOperand.getAddressingMode() == AddressingMode.IMMEDIATE) {
                                                code.putShort(sourceOperand.getImmediateValue());
                                            } else if (sourceOperand.getAddressingMode() == AddressingMode.INDEXED) {
                                                code.putShort(sourceOperand.getIndex());
                                            }
                                            if (destinationOperand.getAddressingMode() == AddressingMode.IMMEDIATE) {
                                                code.putShort(destinationOperand.getImmediateValue());
                                            } else if (destinationOperand.getAddressingMode() == AddressingMode.INDEXED) {
                                                code.putShort(destinationOperand.getIndex());
                                            }
                                        } catch (RuntimeException re) {
                                            errorList.add(new Error(lineCount, re.getMessage()));
                                            continue;
                                        }
                                    }
                                } else {
                                    errorList.add(new Error(lineCount, "missing operands."));
                                }
                                break;
                            //</editor-fold>
                            case 8:
                                //<editor-fold desc="branch instruction">
                                if (lineTokenizer.hasMoreTokens()) {
                                    String label = lineTokenizer.nextToken();
                                    label = label.toLowerCase();
                                    if (labels.containsKey(label)) {
                                        String offsetBinaryString = String.format("%8s",
                                                Integer.toBinaryString((labels.get(label) - code.position() - 2) & 0xFF))
                                                .replace(' ', '0');
                                        instructionBits.append(offsetBinaryString);
                                    } else {
                                        instructionBits.append("00000000");
                                        if (unknownOffsets.containsKey(label)) {
                                            unknownOffsets.get(label).
                                                    add(new Integer(code.position() - 1).shortValue());
                                        } else {
                                            unknownOffsets.put(label, new ArrayList<Short>());
                                            unknownOffsets.get(label).
                                                    add(new Integer(code.position()).shortValue());
                                        }
                                    }
                                    code.putShort(new Integer(Integer.parseInt(
                                            instructionBits.toString(), 2)).shortValue());
                                } else {
                                    errorList.add(new Error(lineCount, "missing branch label"));
                                    continue;
                                }
                                break;
                            //</editor-fold>
                            case 10:
                                //<editor-fold desc="1 operand instruction">
                                if (lineTokenizer.hasMoreTokens()) {
                                    String rawOperand = lineTokenizer.nextToken();
                                    OperandParser theOperand = new OperandParser();
                                    try {
                                        theOperand.parse(rawOperand);
                                        instructionBits.append(theOperand.getAddressingMode());
                                        instructionBits.append(theOperand.getRegister());
                                        code.putShort(new Integer(Integer.parseInt(
                                                instructionBits.toString(), 2)).shortValue());
                                        if (theOperand.getAddressingMode() == AddressingMode.IMMEDIATE) {
                                            code.putShort(theOperand.getImmediateValue());
                                        } else if (theOperand.getAddressingMode() == AddressingMode.INDEXED) {
                                            code.putShort(theOperand.getIndex());
                                        }
                                    } catch (RuntimeException re) {
                                        errorList.add(new Error(lineCount, re.getMessage()));
                                        continue;
                                    }
                                } else {
                                    errorList.add(new Error(lineCount, "missing operand."));
                                }
                                break;
                            //</editor-fold>
                            case 16:
                                code.putShort(new Integer(Integer.parseInt(
                                        instructionBits.toString(), 2)).shortValue());
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

    public void makeBin() throws IOException {
        String binName = asmFile.getName().substring(0, asmFile.getName().indexOf(".")) + ".bin";

        File file = new File(binName);
        FileChannel fileChannel = new FileOutputStream(file, false).getChannel();
        code.flip();
        fileChannel.write(code);
        data.flip();
        fileChannel.write(data);
        fileChannel.close();
    }

    public ByteBuffer getCode() {
        return code;
    }

    public ByteBuffer getData() {
        return data;
    }

    public List<Error> getErrorList() {
        return errorList;
    }
}
