package bgu.spl.net.api;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import java.io.Serializable;

/**
 * Implements the {@link BidiMessagingProtocol} interface
 */
public class BGSMessagingProtocol<T> implements BidiMessagingProtocol<Serializable> {

    // fields

    /*private T argumentToWorkOn;*/

    /*public BGSMessagingProtocol(T argumentToWorkOn) {

        this.argumentToWorkOn = argumentToWorkOn;
    }*/

    // methods

    @Override
    public void start(int connectionId, Connections<Serializable> connections) {

        /*((ConnectionsImpl) connections).addClient(connectionId); // adds the current client to the connections implementation*/
    }

    @Override
    public void process(Serializable message) {

        ((BGSMessage) message).execute();
    }

    @Override
    public boolean shouldTerminate() {

        return false;
    }
}
