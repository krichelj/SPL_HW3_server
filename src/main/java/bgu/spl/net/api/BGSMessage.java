package bgu.spl.net.api;

import java.io.Serializable;

public abstract class BGSMessage implements Serializable {

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

    abstract public Serializable execute();
}
