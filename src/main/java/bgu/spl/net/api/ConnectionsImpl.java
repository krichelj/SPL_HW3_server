package bgu.spl.net.api;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.net.Socket;
import java.util.HashMap;

/** This implementation should map a unique ID for each active client
 *  connected to the server. The implementation of Connections is part of the server
 *  pattern and not part of the protocol.
 * @param <T>
 */

@SuppressWarnings("unchecked") // suppress unchecked assignment warnings

public class ConnectionsImpl<T> implements Connections<T> {

    private HashMap<Integer,ConnectionHandler> handlersList;
    private static int clientID = 1;

    public ConnectionsImpl () {

        handlersList = new HashMap<>(); // a hash map of a client ID and its ConnectionHandler
    }

    @Override
    public boolean send(int connectionId, T msg) {

        handlersList.get(connectionId).send(msg);

        return false;
    }

    @Override
    public void broadcast(T msg) {

        handlersList.forEach((ID, connectionHandler) -> connectionHandler.send(msg));

    }

    @Override
    public void disconnect(int connectionId) {

        handlersList.remove(connectionId);

    }

    public void addClient (Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol){

        handlersList.put(clientID++,new BlockingConnectionHandler(sock, reader, protocol));
    }
}
