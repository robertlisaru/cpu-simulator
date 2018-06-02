package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;

public class CodeMemory {
    private ByteBuffer code;

    public CodeMemory(ByteBuffer code) {
        this.code = code;
    }

    public Short read(Short address) {
        return code.getShort(address & 0xFFFF);
    }

    public ByteBuffer getByteBuffer() {
        return code;
    }
}
