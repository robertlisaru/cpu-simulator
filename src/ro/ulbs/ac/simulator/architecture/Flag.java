package ro.ulbs.ac.simulator.architecture;

public class Flag {
    private boolean z = false;
    private boolean s = false;
    private boolean c = false;
    private boolean v = false;

    public boolean getZ() {
        return z;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public void sez() {
        z = true;
    }

    public void clz() {
        z = false;
    }

    public boolean getS() {
        return s;
    }

    public void setS(boolean s) {
        this.s = s;
    }

    public void ses() {
        s = true;
    }

    public void cls() {
        s = false;
    }

    public boolean getC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public void sec() {
        c = true;
    }

    public void clc() {
        c = false;
    }

    public boolean getV() {
        return v;
    }

    public void setV(boolean v) {
        this.v = v;
    }

    public void sev() {
        v = true;
    }

    public void clv() {
        v = false;
    }

    public void ccc() {
        z = s = c = v = false;
    }

    public void scc() {
        z = s = c = v = true;
    }

    public Short toShort() {
        int intZ = z ? 1 : 0;
        int intS = s ? 1 : 0;
        int intC = c ? 1 : 0;
        int intV = v ? 1 : 0;
        return Integer.valueOf(intZ * 8 + intS * 4 + intC * 2 + intV).shortValue();
    }

    public void setFromShort(Short flagShort) {
        int intZ = flagShort & 8;
        int intS = flagShort & 4;
        int intC = flagShort & 2;
        int intV = flagShort & 1;
        z = (intZ == 8) ? true : false;
        s = (intS == 4) ? true : false;
        c = (intC == 2) ? true : false;
        v = (intV == 1) ? true : false;
    }
}
