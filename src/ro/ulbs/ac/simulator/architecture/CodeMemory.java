package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CodeMemory {
    private ByteBuffer code = ByteBuffer.allocate(65536);

    public void initializeCodeMemory(ByteBuffer code) {
        this.code = code;
        code.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Short read(Short address) {
        return code.getShort(address.intValue());
    }
}
