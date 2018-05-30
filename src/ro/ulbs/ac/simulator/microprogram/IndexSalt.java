package ro.ulbs.ac.simulator.microprogram;

public enum IndexSalt {
    NONE(0),
    CLASS_INDEX(1),
    MAS_INDEX(2),
    MAD_INDEX(3),
    DOUBLE_OP_INDEX(4),
    SINGLE_OP_INDEX(5),
    BRANCH_INDEX(6),
    DIVERSE_INDEX(7);

    private final int indexNumber;

    private IndexSalt(int indexNumber) {
        this.indexNumber = indexNumber;
    }
}
