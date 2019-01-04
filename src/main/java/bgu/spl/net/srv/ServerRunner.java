package bgu.spl.net.srv;

import bgu.spl.net.api.*;
import bgu.spl.net.api.BGSMessagingProtocol;

public class ServerRunner {

    public static void main (String[] args){

        Server.threadPerClient(7778,
                () -> new BGSMessagingProtocol(new BGSUsers()), //protocol factory
                BGSMessageEncoderDecoder::new  //message encoder decoder factory
        ).serve();
    }
}