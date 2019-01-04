package bgu.spl.net.api.BGSSavedMessages;

import bgu.spl.net.api.BGSSavedMessage;

public class PM extends BGSSavedMessage {

    // fields

    private final String receiverUsername;

    // constructor

    public PM(String senderUsername, String receiverUsername, String content) {

        super(senderUsername,content);
        this.receiverUsername = receiverUsername;
    }

    // methods

    public String getReceiverUsername() {

        return receiverUsername;
    }



}
