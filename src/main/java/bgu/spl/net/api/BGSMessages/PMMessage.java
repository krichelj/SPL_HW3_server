package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

public class PMMessage extends BGSMessage {

    // fields

    private final String receiverUsername, PMcontent;

    // constructor

    public PMMessage(String receiverUsername, String content) {

        super((short) 6);
        this.receiverUsername = receiverUsername;
        this.PMcontent = content;
    }

    // methods

    public String getReceiverUsername() {

        return receiverUsername;
    }

    public String getPMcontent() {

        return PMcontent;
    }


}
