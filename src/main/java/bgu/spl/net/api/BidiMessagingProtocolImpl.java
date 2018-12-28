package bgu.spl.net.api;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.Serializable;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<Serializable> {

    // fields

    private T argument;

    // constructor

    public BidiMessagingProtocolImpl (T argument) {

        this.argument = argument;
    }

    // methods

    @Override
    public void start(int connectionId, Connections<Serializable> connections) {



    }

    @Override
    public void process(Serializable message) {


    }

    @Override
    public boolean shouldTerminate() {

        return false;
    }
}
