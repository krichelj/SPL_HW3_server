package bgu.spl.net.api;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.Serializable;

public class BGSMessagingProtocol<T> implements BidiMessagingProtocol<Serializable> {

    // methods

    @Override
    public void start(int connectionId, Connections<Serializable> connections) {

        connections
    }

    @Override
    public void process(Serializable message) {

        ((BGSCommand) message).execute();
    }

    @Override
    public boolean shouldTerminate() {

        return false;
    }
}
