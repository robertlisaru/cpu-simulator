package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CodeMemory {
    private ByteBuffer code = ByteBuffer.allocate(1 << 16);

    public CodeMemory(ByteBuffer code) {
        ByteBuffer tmp = code.duplicate();
        tmp.flip();
        this.code.put(tmp);
        this.code.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Short read(Short address) {
        return code.getShort(address & 0xFFFF);
    }

    public ByteBuffer getByteBuffer() {
        return code;
    }
}
