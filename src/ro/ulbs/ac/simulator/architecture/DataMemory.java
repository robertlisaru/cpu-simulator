package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataMemory {
    private ByteBuffer data = ByteBuffer.allocate(1<<16);

    public DataMemory(ByteBuffer data) {
        ByteBuffer tmp = data.duplicate();
        tmp.flip();
        this.data.put(tmp);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Short read(Short address) {
        return data.getShort(address & 0xFFFF);
    }

    public void write(Short address, Short data) {
        this.data.putShort(address & 0xFFFF, data);
    }

    public ByteBuffer getByteBuffer() {
        return data;
    }
}
