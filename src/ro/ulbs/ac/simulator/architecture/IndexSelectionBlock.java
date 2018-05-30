package ro.ulbs.ac.simulator.architecture;

import ro.ulbs.ac.simulator.microprogram.IndexSalt;

public class IndexSelectionBlock {
    private final int CLASS_INDEX_SHIFT = 0;
    private final int MAS_INDEX_SHIFT = 1;
    private final int MAD_INDEX_SHIFT = 1;
    private final int DOUBLE_OP_INDEX_SHIFT = 2;
    private final int SINGLE_OP_INDEX_SHIFT = 2;
    private final int BRANCH_INDEX_SHIFT = 1;
    private final int DIVERSE_INDEX_SHIFT = 2;

    public Short select(IndexSalt indexSalt, Short IR) {
        switch (indexSalt) {
            case NONE:
                return 0;
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
        return 0;
    }
}
