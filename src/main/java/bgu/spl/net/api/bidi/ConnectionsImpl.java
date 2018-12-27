package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;

/** This implementation should map a unique ID for each active client
 *  connected to the server. The implementation of Connections is part of the server
 *  pattern and not part of the protocol.
 * @param <T>
 */
public class ConnectionsImpl<T> implements Connections<T> {

    private HashMap<Integer,ConnectionHandler> handlersList;

    public ConnectionsImpl () {

        handlersList = new HashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {

        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
