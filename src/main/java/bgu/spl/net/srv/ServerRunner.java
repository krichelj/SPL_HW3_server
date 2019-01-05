package bgu.spl.net.srv;

import bgu.spl.net.api.*;
import bgu.spl.net.api.BGSMessagingProtocol;

public class ServerRunner {

    public static void main (String[] args){

        BGSUsers currentServerUsers = new BGSUsers();

        Server.threadPerClient(7777,
                () -> new BGSMessagingProtocol(currentServerUsers), //protocol factory
                BGSMessageEncoderDecoder::new  //message encoder decoder factory
        ).serve();
    }
}