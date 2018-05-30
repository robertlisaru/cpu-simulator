package ro.ulbs.ac.simulator.architecture;

import java.nio.ByteBuffer;

public class DataMemory {
    private ByteBuffer data;

    public DataMemory(ByteBuffer data) {
        this.data = data;
        data.limit(data.capacity());
    }

    public Short read(Short address) {
        return data.getShort(address.intValue());
    }

    public void write(Short address, Short data) {
        this.data.putShort(address.intValue(), data);
    }
}
