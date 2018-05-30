package ro.ulbs.ac.simulator.microprogram;

public class Microinstruction {
    private SursaSBUS sursaSBUS;
    private SursaDBUS sursaDBUS;
    private OperatieALU operatieALU;
    private SursaRBUS sursaRBUS;
    private DestinatieRBUS destinatieRBUS;
    private OperatieMemorie operatieMemorie;
    private OtherOperation otherOperation;
    private ConditieSalt conditieSalt;
    private IndexSalt indexSalt = IndexSalt.NONE;
    private boolean jumpOnConditionEqualsFalse = false;
    private Short microadresaSalt = 0;

    public SursaSBUS getSursaSBUS() {
        return sursaSBUS;
    }

    public void setSursaSBUS(SursaSBUS sursaSBUS) {
        this.sursaSBUS = sursaSBUS;
    }

    public SursaDBUS getSursaDBUS() {
        return sursaDBUS;
    }

    public void setSursaDBUS(SursaDBUS sursaDBUS) {
        this.sursaDBUS = sursaDBUS;
    }

    public OperatieALU getOperatieALU() {
        return operatieALU;
    }

    public void setOperatieALU(OperatieALU operatieALU) {
        this.operatieALU = operatieALU;
    }

    public SursaRBUS getSursaRBUS() {
        return sursaRBUS;
    }

    public void setSursaRBUS(SursaRBUS sursaRBUS) {
        this.sursaRBUS = sursaRBUS;
    }

    public DestinatieRBUS getDestinatieRBUS() {
        return destinatieRBUS;
    }

    public void setDestinatieRBUS(DestinatieRBUS destinatieRBUS) {
        this.destinatieRBUS = destinatieRBUS;
    }

    public OperatieMemorie getOperatieMemorie() {
        return operatieMemorie;
    }

    public void setOperatieMemorie(OperatieMemorie operatieMemorie) {
        this.operatieMemorie = operatieMemorie;
    }

    public OtherOperation getOtherOperation() {
        return otherOperation;
    }

    public void setOtherOperation(OtherOperation otherOperation) {
        this.otherOperation = otherOperation;
    }

    public ConditieSalt getConditieSalt() {
        return conditieSalt;
    }

    public void setConditieSalt(ConditieSalt conditieSalt) {
        this.conditieSalt = conditieSalt;
    }

    public IndexSalt getIndexSalt() {
        return indexSalt;
    }

    public void setIndexSalt(IndexSalt indexSalt) {
        this.indexSalt = indexSalt;
    }

    public boolean isJumpOnConditionEqualsFalse() {
        return jumpOnConditionEqualsFalse;
    }

    public void setJumpOnConditionEqualsFalse(boolean jumpOnConditionEqualsFalse) {
        this.jumpOnConditionEqualsFalse = jumpOnConditionEqualsFalse;
    }

    public Short getMicroadresaSalt() {
        return microadresaSalt;
    }

    public void setMicroadresaSalt(Short microadresaSalt) {
        this.microadresaSalt = microadresaSalt;
    }
}
