package bgu.spl.net.api;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;
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

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer,BlockingConnectionHandler<T>> connectedHandlersMap; // a concurrent data structure for thread-safety

    public ConnectionsImpl(){

        connectedHandlersMap = new ConcurrentHashMap<>(); // a hash map of a client ID and its ConnectionHandler
    }

    @Override
    public boolean send(int connectionId, T msg) {

        boolean outcome = false;

        if (getClient(connectionId) != null) { // sends the message if the client is connected
            getClient(connectionId).send(msg);
            outcome = true;
        }

        return outcome;
    }

    @Override
    public void broadcast(T msg) {

        connectedHandlersMap.forEach((connectionId, connectionHandler) -> connectionHandler.send(msg));
    }

    @Override
    public void disconnect(int connectionId) {

        connectedHandlersMap.get(connectionId).disconnect();
        connectedHandlersMap.remove(connectionId);
    }

    public void connect(int connectionId, BlockingConnectionHandler<T> connectionHandler){

        connectedHandlersMap.put(connectionId, connectionHandler);
    }

    public BlockingConnectionHandler<T> getClient(int connectionId){

        return connectedHandlersMap.get(connectionId);
    }

    public void logUserIn(int connectionId, User userToLogIn) {

        connectedHandlersMap.get(connectionId).setCurrentActiveUser(userToLogIn);
    }
}