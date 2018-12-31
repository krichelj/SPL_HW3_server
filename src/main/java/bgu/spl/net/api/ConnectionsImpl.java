package bgu.spl.net.api;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/** This implementation should map a unique ID for each active client
 *  connected to the server. The implementation of {@link Connections} is part of the server
 *  pattern and not part of the protocol.
 *
 *  Every server instance has one {@link ConnectionsImpl} instance, that holds all of its clients.
 *  Since we are implementing the Thread-Per-Client architecture, we'd want the {@link ConnectionsImpl}
 *  class to be thread-safe.
 * @param <T>
 */

@SuppressWarnings("unchecked") // suppress unchecked assignment warnings

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer,ConnectionHandler<T>> handlersMap; // a concurrent data structure for thread-safety

    public ConnectionsImpl () {

        handlersMap = new ConcurrentHashMap<>(); // a hash map of a client ID and its ConnectionHandler
    }

    @Override
    public boolean send(int connectionId, T msg) {

        handlersMap.get(connectionId).send(msg);

        return false;
    }

    @Override
    public void broadcast(T msg) {

        handlersMap.forEach((connectionId, connectionHandler) -> connectionHandler.send(msg));
    }

    @Override
    public void disconnect(int connectionId) {

        try {
            handlersMap.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handlersMap.remove(connectionId);
    }

    public void addClient (int connectionId, ConnectionHandler<T> connectionHandler){

        handlersMap.put(connectionId, connectionHandler);
    }

    public ConnectionHandler<T> getClient (int connectionId){

        return handlersMap.get(connectionId);
    }
}