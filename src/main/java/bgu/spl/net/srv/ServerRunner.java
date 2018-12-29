package bgu.spl.net.srv;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.BGSMessageEncoderDecoder;
import bgu.spl.net.api.BGSMessagingProtocol;
import bgu.spl.net.api.ConnectionsImpl;
import bgu.spl.net.api.bidi.Connections;

public class ServerRunner {

    public static void main (String[] args){

        Server.threadPerClient(7777,
                () -> new BGSMessagingProtocol<>(), //protocol factory
                () -> new BGSMessageEncoderDecoder()  //message encoder decoder factory
        ).serve();
    }
}