package bgu.spl.net.srv;

import bgu.spl.net.api.ConnectionsImpl;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

/** An abstract server that allow its derivatives to implement different concurrency models.
 * Our TCP server needs to create a new Protocol and EncoderDecoder for every connection it receives
 * but since it is a generic server, it does not know what and how to create such objects.
 * This problem is solved using factories, the server receives factories in its constructor that create those objects for it.
 * @param <T>
 */

@SuppressWarnings("unchecked") // suppress unchecked assignment warnings

public abstract class BaseServer<T> implements Server<T> {

    // fields

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory;
    private ServerSocket serverSocket;
    private ConnectionsImpl<T> currentServerConnections;
    private static int connectionId;

    // constructor

    public BaseServer(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encoderDecoderFactory = encoderDecoderFactory;
        currentServerConnections = new ConnectionsImpl<>();
        serverSocket = null;
        connectionId = 1;
    }

    // methods

    @Override
    public void serve() {

        try {

			System.out.println("Server started");
            serverSocket = new ServerSocket(port);

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSocket = serverSocket.accept(); // blocks until a connection is made
                System.out.println("A client has connected!");
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(clientSocket, encoderDecoderFactory.get(),
                        protocolFactory.get()); // creates a new ConnectionHandler for the client
                currentServerConnections.addClient(connectionId++, handler); // adds the client to the ConnectionsImpl map
                execute(handler); // executed the ConnectionHandler
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {

		serverSocket.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}