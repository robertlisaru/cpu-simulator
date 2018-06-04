package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CodeMemory {
    private ByteBuffer code = ByteBuffer.allocate(1 << 16);

    public CodeMemory() {
        this.code.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void loadInterruptRoutines(ByteBuffer interruptRoutines, int startAddress) {
        ByteBuffer tmp = interruptRoutines.duplicate();
        tmp.flip();
        this.code.position(startAddress);
        this.code.put(tmp);
        this.code.clear();
    }

    public void loadCode(ByteBuffer code) {
        ByteBuffer tmp = code.duplicate();
        tmp.flip();
        this.code.put(tmp);
    }

    public Short read(Short address) {
        return code.getShort(address & 0xFFFF);
    }

    public ByteBuffer getByteBuffer() {
        return code;
    }
}
