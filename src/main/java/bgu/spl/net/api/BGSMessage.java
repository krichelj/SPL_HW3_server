package bgu.spl.net.api;

public abstract class BGSMessage {

    // fields

    private final short opCode;

    // constructor

    protected BGSMessage(short opCode) {

        this.opCode = opCode;
    }

    // methods

    public Short getOpCode() {

        return opCode;
    }
}
