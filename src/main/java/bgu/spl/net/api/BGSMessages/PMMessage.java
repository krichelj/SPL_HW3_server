package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

public class PMMessage extends BGSMessage {

    // fields

    private final String receiverUsername, content;

    // constructor

    public PMMessage(String receiverUsername, String content) {

        super((short) 6);
        this.receiverUsername = receiverUsername;
        this.content = content;
    }

    // methods

    public String getReceiverUsername() {

        return receiverUsername;
    }

    public String getContent() {

        return content;
    }


}
