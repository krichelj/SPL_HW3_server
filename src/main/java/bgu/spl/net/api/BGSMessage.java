package bgu.spl.net.api;

public class BGSMessage {

    // fields

    private final short opCode;
    /*protected final BGSUsers usersInstance;
    protected BGSMessage outputMessage;*/

    // constructor

    protected BGSMessage(short opCode) {

        this.opCode = opCode;
        /*usersInstance = BGSUsers.getInstance();
        outputMessage = null;*/
    }

    // methods

    public Short getOpCode() {

        return opCode;
    }

    /*abstract public BGSMessage execute();*/
}
