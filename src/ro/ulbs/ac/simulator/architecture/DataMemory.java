package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataMemory {
    private ByteBuffer data = ByteBuffer.allocate(65536);

    public void initializeDataMemory(ByteBuffer data) {
        this.data = data;
        data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Short read(Short address) {
        return data.getShort(address.intValue());
    }

    public void write(Short address, Short data) {
        this.data.putShort(address.intValue(), data);
    }
}
