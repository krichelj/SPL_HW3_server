package bgu.spl.net.api;

/**
 * An abstract class that represents a message between client and server and vice versa
 */
@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

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
