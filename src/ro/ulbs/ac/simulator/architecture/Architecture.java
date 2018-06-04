package ro.ulbs.ac.simulator.architecture;

import ro.ulbs.ac.simulator.microprogram.ConditieSalt;
import ro.ulbs.ac.simulator.microprogram.IndexSalt;
import ro.ulbs.ac.simulator.microprogram.Microinstruction;
import ro.ulbs.ac.simulator.microprogram.MicroprogramMemory;
import ro.ulbs.ac.simulator.microprogram.MicroprogramParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Architecture {
    //region variables
    private Short SBUS;
    private Short DBUS;
    private Short RBUS;
    private Short[] registerFile = new Short[16];
    private Flag flag = new Flag();
    private Short SP = 0;
    private Short T = 0;
    private Short PC = 0x00;
    private Short IVR = 0;
    private Short ADR = 0;
    private Short MDR = 0;
    private Short IR = 0;
    private CodeMemory codeMemory;
    private DataMemory dataMemory;
    private MicroprogramMemory microprogramMemory = new MicroprogramMemory();
    private MicroprogramParser microprogramParser = new MicroprogramParser();
    private ConditionSelectionBlock conditionSelectionBlock = new ConditionSelectionBlock();
    private IndexSelectionBlock indexSelectionBlock = new IndexSelectionBlock();
    private Sequencer sequencer = new Sequencer();
    private MicrocomandDecoderBlock microcomandDecoderBlock = new MicrocomandDecoderBlock();
    private InterruptSystem interruptSystem = new InterruptSystem();
    private Microinstruction MIR;
    private Short MAR = 0;
    private Short aluResult;
    private Flag aluFlag = new Flag();
    private boolean halted = false;
    private int interruptRoutinesStartAddress = 0xFF80;
    //endregion

    public Architecture() {
        try {
            microprogramMemory = microprogramParser.parseFile(new File("ucode.csv"));
            MIR = microprogramMemory.microinstructionFetch(Integer.valueOf(0).shortValue());
            Arrays.fill(registerFile, Integer.valueOf(0).shortValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Microinstruction getMIR() {
        return MIR;
    }

    public CodeMemory getCodeMemory() {
        return codeMemory;
    }

    public DataMemory getDataMemory() {
        return dataMemory;
    }

    public Short getMAR() {
        return MAR;
    }

    public MicroprogramParser getMicroprogramParser() {
        return microprogramParser;
    }

    public Flag getFlag() {
        return flag;
    }

    public Short getSP() {
        return SP;
    }

    public Short getT() {
        return T;
    }

    public Short getPC() {
        return PC;
    }

    public Short getIVR() {
        return IVR;
    }

    public Short getADR() {
        return ADR;
    }

    public Short getMDR() {
        return MDR;
    }

    public Short getIR() {
        return IR;
    }

    public void executeOneMicroinstruction() {
        microcomandDecoderBlock.decode(MIR);
        sequencer.fetchNextMicroinstruction();
    }

    public void executeAll() {
        while (!halted) {
            microcomandDecoderBlock.decode(MIR);
            sequencer.fetchNextMicroinstruction();
        }
    }

    public void loadCode(ByteBuffer code) throws IOException {
        codeMemory = new CodeMemory();
        loadInterruptRoutines();
        codeMemory.loadCode(code);
    }

    private void loadInterruptRoutines() throws IOException {
        ByteBuffer interruptRoutines = ByteBuffer.allocate(0x100);
        File interruptRoutinesFile = new File("interruptRoutines.bin");
        FileInputStream inFile = new FileInputStream(interruptRoutinesFile);
        FileChannel inChannel = inFile.getChannel();
        while (inChannel.read(interruptRoutines) != -1) {

        }
        inFile.close();
        codeMemory.loadInterruptRoutines(interruptRoutines, interruptRoutinesStartAddress);
    }

    public void loadData(ByteBuffer data) {
        dataMemory = new DataMemory(data);
    }

    public Short[] getRegisterFile() {
        return registerFile;
    }

    public MicroprogramMemory getMicroprogramMemory() {
        return microprogramMemory;
    }

    public InterruptSystem getInterruptSystem() {
        return interruptSystem;
    }

    private class ConditionSelectionBlock {
        private final boolean NONE = false;
        private final boolean JUMP = true;
        private boolean ACLOW;
        private boolean CIL;
        private boolean B1;
        private boolean RD_D;
        private boolean INT;

        public boolean select(ConditieSalt conditieSalt) {
            switch (conditieSalt) {
                case NONE:
                    return NONE;
                case JUMP:
                    return JUMP;
                case IF_ACLOW:
                    return ACLOW;
                case IF_CIL:
                    return CIL;
                case IF_B1:
                    return B1;
                case IF_RD_D:
                    return RD_D;
                case IF_INT:
                    return INT;
                case IF_Z:
                    return flag.getZ();
                case IF_S:
                    return flag.getS();
                case IF_C:
                    return flag.getC();
                case IF_V:
                    return flag.getV();
            }
            return false;
        }

        public void setACLOW(boolean ACLOW) {
            this.ACLOW = ACLOW;
        }

        public void setCIL(boolean CIL) {
            this.CIL = CIL;
        }

        public void setB1(boolean b1) {
            B1 = b1;
        }

        public void setRD_D(boolean RD_D) {
            this.RD_D = RD_D;
        }

        public void setINT(boolean INT) {
            this.INT = INT;
        }
    }

    public class InterruptSystem {
        public void signal(InterruptSignal interrupSignal) {
            conditionSelectionBlock.setINT(true);
            IVR = Integer.valueOf(interruptRoutinesStartAddress + interrupSignal.getValue() * 16).shortValue();
        }

        public void release() {
            conditionSelectionBlock.setINT(false);
        }
    }

    private class IndexSelectionBlock {
        private final int CLASS_INDEX_SHIFT = 0;
        private final int MAS_INDEX_SHIFT = 1;
        private final int MAD_INDEX_SHIFT = 1;
        private final int DOUBLE_OP_INDEX_SHIFT = 2;
        private final int SINGLE_OP_INDEX_SHIFT = 2;
        private final int BRANCH_INDEX_SHIFT = 1;
        private final int DIVERSE_INDEX_SHIFT = 2;

        private Short select(IndexSalt indexSalt) {
            switch (indexSalt) {
                case NONE:
                    return Integer.valueOf(0).shortValue();
                case CLASS_INDEX:
                    if ((IR & 0xffff) >= 49152) {
                        return 3 << CLASS_INDEX_SHIFT;
                    } else if ((IR & 0xffff) >= 40960) {
                        return 2 << CLASS_INDEX_SHIFT;
                    } else if ((IR & 0xffff) >= 32768) {
                        return 1 << CLASS_INDEX_SHIFT;
                    } else {
                        return 0;
                    }
                case MAS_INDEX:
                    return Integer.valueOf(((IR & 0xC00) >> 10) << MAS_INDEX_SHIFT).shortValue();
                case MAD_INDEX:
                    return Integer.valueOf(((IR & 0x30) >> 4) << MAD_INDEX_SHIFT).shortValue();
                case DOUBLE_OP_INDEX:
                    return Integer.valueOf(((IR & 0x7000) >> 12) << DOUBLE_OP_INDEX_SHIFT).shortValue();
                case SINGLE_OP_INDEX:
                    return Integer.valueOf(((IR & 0x1FC0) >> 6) << SINGLE_OP_INDEX_SHIFT).shortValue();
                case BRANCH_INDEX:
                    return Integer.valueOf(((IR & 0x1F00) >> 8) << BRANCH_INDEX_SHIFT).shortValue();
                case DIVERSE_INDEX:
                    return Integer.valueOf((IR & 0x3FFF) << DIVERSE_INDEX_SHIFT).shortValue();
            }
            return Integer.valueOf(0).shortValue();
        }
    }

    private class Sequencer {
        private void fetchNextMicroinstruction() {
            if (conditionSelectionBlock.select(MIR.getConditieSalt()) ^ MIR.isJumpOnConditionEqualsFalse()) {
                MAR = Integer.valueOf(MIR.getMicroadresaSalt() +
                        indexSelectionBlock.select(MIR.getIndexSalt())).shortValue();
            } else {
                MAR++;
            }
            MIR = microprogramMemory.microinstructionFetch(MAR);
        }
    }

    private class MicrocomandDecoderBlock {
        private void decode(Microinstruction microinstruction) {
            SursaSBUSMethods sursaSBUSMethods = new SursaSBUSMethods();
            SursaDBUSMethods sursaDBUSMethods = new SursaDBUSMethods();
            OperatieALUMethods operatieALUMethods = new OperatieALUMethods();
            SursaRBUSMethods sursaRBUSMethods = new SursaRBUSMethods();
            DestinatieRBUSMethods destinatieRBUSMethods = new DestinatieRBUSMethods();
            OperatieMemorieMethods operatieMemorieMethods = new OperatieMemorieMethods();
            OtherOperationMethods otherOperationMethods = new OtherOperationMethods();
            try {
                Method sursaSBUSMethod = sursaSBUSMethods.getClass().getMethod(microinstruction.getSursaSBUS().name());
                sursaSBUSMethod.invoke(sursaSBUSMethods);
                Method sursaDBUSMethod = sursaDBUSMethods.getClass().getMethod(microinstruction.getSursaDBUS().name());
                sursaDBUSMethod.invoke(sursaDBUSMethods);
                Method operatieALUMethod = operatieALUMethods.getClass().getMethod(microinstruction.getOperatieALU().name());
                operatieALUMethod.invoke(operatieALUMethods);
                Method sursaRBUSMethod = sursaRBUSMethods.getClass().getMethod(microinstruction.getSursaRBUS().name());
                sursaRBUSMethod.invoke(sursaRBUSMethods);
                Method destinatieRBUSMethod = destinatieRBUSMethods.getClass().getMethod(microinstruction.getDestinatieRBUS().name());
                destinatieRBUSMethod.invoke(destinatieRBUSMethods);
                Method operatieMemorieMethod = operatieMemorieMethods.getClass().getMethod(microinstruction.getOperatieMemorie().name());
                operatieMemorieMethod.invoke(operatieMemorieMethods);
                Method otherOperationMethod = otherOperationMethods.getClass().getMethod(microinstruction.getOtherOperation().name());
                otherOperationMethod.invoke(otherOperationMethods);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


    }

    class SursaSBUSMethods {
        public void NONE() {
        }

        public void PD_PC_SBUS() {
            SBUS = PC;
        }

        public void PD_MDR_SBUS() {
            SBUS = MDR;
        }

        public void PD_GPR_S_SBUS() {
            SBUS = registerFile[(IR & 0x3C0) >> 6];
        }

        public void PD_GPR_D_SBUS() {
            SBUS = registerFile[IR & 0xF];
        }

        public void PD_T() {
            SBUS = T;
        }

        public void PD_MINUS_T() {
            SBUS = Integer.valueOf(-T).shortValue();
        }

        public void PD_0() {
            SBUS = 0;
        }

        public void PD_NOT_MDR() {
            SBUS = Integer.valueOf(~MDR).shortValue();
        }

        public void PD_SP() {
            SBUS = SP;
        }

        public void PD_IR_OFFSET() {
            if ((IR & 0x80) == 0x80) {
                SBUS = Integer.valueOf((IR & 0xFF) | 0xFF00).shortValue();
            } else {
                SBUS = Integer.valueOf(IR & 0x00FF).shortValue();
            }
        }

        public void PD_FLAG() {
            SBUS = flag.toShort();
        }

        public void PD_IVR() {
            SBUS = IVR;
        }
    }

    class SursaDBUSMethods {
        public void NONE() {

        }

        public void PD_MDR_DBUS() {
            DBUS = MDR;
        }

        public void PD_GPR_S_DBUS() {
            DBUS = registerFile[(IR & 0x3C0) >> 6];
        }

        public void PD_GPR_D_DBUS() {
            DBUS = registerFile[IR & 0xF];
        }

        public void PD_MINUS_1() {
            DBUS = -1;
        }

        public void PD_1() {
            DBUS = 1;
        }


        public void PD_PC_DBUS() {
            DBUS = PC;
        }
    }

    class OperatieALUMethods {
        public void NONE() {
        }

        public void SUM() {
            aluResult = Integer.valueOf(SBUS + DBUS).shortValue();
            aluFlag.setZ(aluResult == 0);
            aluFlag.setS((aluResult & 0x8000) == 0x8000);
            aluFlag.setC((((SBUS & 0xFFFF) + (DBUS & 0xFFFF)) & 0x10000) == 0x10000);
            aluFlag.setV((SBUS < 0 && DBUS < 0 && aluResult >= 0) || (SBUS > 0 && DBUS > 0 && aluResult <= 0));
        }

        public void AND() {
            aluResult = Integer.valueOf(SBUS & DBUS).shortValue();
            aluFlag.setZ(aluResult == 0);
            aluFlag.setS((aluResult & 0x8000) == 0x8000);
        }

        public void OR() {
            aluResult = Integer.valueOf(SBUS | DBUS).shortValue();
            aluFlag.setZ(aluResult == 0);
            aluFlag.setS((aluResult & 0x8000) == 0x8000);
        }

        public void XOR() {
            aluResult = Integer.valueOf(SBUS ^ DBUS).shortValue();
            aluFlag.setZ(aluResult == 0);
            aluFlag.setS((aluResult & 0x8000) == 0x8000);
        }

        public void SBUS() {
            aluResult = SBUS;
        }
    }

    class SursaRBUSMethods {
        public void NONE() {
        }

        public void PD_ALU() {
            RBUS = aluResult;
        }
    }

    class DestinatieRBUSMethods {
        public void NONE() {
        }

        public void PM_ADR() {
            ADR = RBUS;
        }

        public void PM_T() {
            T = RBUS;
        }

        public void PM_MDR() {
            MDR = RBUS;
        }

        public void PM_GPR() {
            registerFile[IR & 0xF] = RBUS;
        }

        public void PM_PC() {
            PC = RBUS;
        }

        public void PM_FLAG() {
            flag.setFromShort(RBUS);
        }
    }

    class OperatieMemorieMethods {
        public void NONE() {
        }

        public void IFCH() {
            IR = codeMemory.read(ADR);
            if ((IR & 0xffff) < 32768) {
                conditionSelectionBlock.setB1(true);
            } else {
                conditionSelectionBlock.setB1(false);
            }
            if (((IR & 0x30) >> 4) == 1) {
                conditionSelectionBlock.setRD_D(true);
            } else {
                conditionSelectionBlock.setRD_D(false);
            }

        }

        public void READ() {
            MDR = dataMemory.read(ADR);
        }

        public void WRITE() {
            dataMemory.write(ADR, MDR);
        }

        public void READ_VAL() {
            MDR = codeMemory.read(ADR);
        }
    }

    class OtherOperationMethods {
        public void NONE() {
        }

        public void PC_PLUS_2() {
            PC++;
            PC++;
        }

        public void PD_COND() {
            flag.setZ(aluFlag.getZ());
            flag.setS(aluFlag.getS());
            flag.setC(aluFlag.getC());
            flag.setV(aluFlag.getV());
        }

        public void ASL_AND_PD_COND() {
            aluFlag.setC(MDR < 0);
            MDR = Integer.valueOf(MDR << 1).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void ASR_AND_PD_COND() {
            aluFlag.setC((MDR & 1) == 1);
            MDR = Integer.valueOf(MDR >> 1).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void LSR_AND_PD_COND() {
            aluFlag.setC((MDR & 1) == 1);
            MDR = Integer.valueOf(MDR >>> 1).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void ROL_AND_PD_COND() {
            aluFlag.setC(MDR < 0);
            MDR = Integer.valueOf((MDR >>> 15) | (MDR << 1)).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void ROR_AND_PD_COND() {
            aluFlag.setC((MDR & 1) == 1);
            MDR = Integer.valueOf((MDR >>> 1) | (MDR << 15)).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void RLC_AND_PD_COND() {
            int auxC = flag.getC() ? 1 : 0;
            aluFlag.setC(MDR < 0);
            MDR = Integer.valueOf(MDR << 1).shortValue();
            MDR = Integer.valueOf(MDR + auxC).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void RRC_AND_PD_COND() {
            int auxC = flag.getC() ? 1 : 0;
            aluFlag.setC((MDR & 1) == 1);
            MDR = Integer.valueOf(MDR >> 1).shortValue();
            MDR = Integer.valueOf(MDR + (auxC << 15)).shortValue();

            flag.setC(aluFlag.getC());
        }

        public void SP_MINUS_2() {
            SP--;
            SP--;
        }

        public void SP_PLUS_2() {
            SP++;
            SP++;
        }

        public void CLC() {
            flag.clc();
        }

        public void CLV() {
            flag.clv();
        }

        public void CLZ() {
            flag.clz();
        }

        public void CLS() {
            flag.cls();
        }

        public void CCC() {
            flag.ccc();
        }

        public void SEC() {
            flag.sec();
        }

        public void SEV() {
            flag.sev();
        }

        public void SEZ() {
            flag.sez();
        }

        public void SES() {
            flag.ses();
        }

        public void SCC() {
            flag.scc();
        }

        public void HALT() {
            halted = true;
        }

        public void SP_MINUS_2_AND_INTA() {
            SP--;
            SP--;
            conditionSelectionBlock.setINT(false);
        }

        public void A_0_BI_AND_A_0_BE() {
            conditionSelectionBlock.setINT(false);
        }

        public void A_1_BE0() {
            interruptSystem.signal(InterruptSignal.ACLOW);
        }
    }
}
