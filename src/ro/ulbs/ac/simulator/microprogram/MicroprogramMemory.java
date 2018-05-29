package ro.ulbs.ac.simulator.microprogram;

import java.util.ArrayList;
import java.util.List;

public class MicroprogramMemory {
    private List<Microinstruction> microinstructionList = new ArrayList<>();

    public Microinstruction microinstructionFetch(byte microAddress) {
        return microinstructionList.get(microAddress);
    }

    public List<Microinstruction> getMicroinstructionList() {
        return microinstructionList;
    }
}
