package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.BGSMessageEncoderDecoder;
import bgu.spl.net.api.BGSMessagingProtocol;
import bgu.spl.net.api.BGSUsers;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main (String[] args){

        BGSUsers currentServerUsers = new BGSUsers(); // create a new data base
        int port = Integer.parseInt(args[0]); // input the port

        Server.threadPerClient(port,
                () -> new BGSMessagingProtocol(currentServerUsers),
                BGSMessageEncoderDecoder::new
        ).serve();
    }
}