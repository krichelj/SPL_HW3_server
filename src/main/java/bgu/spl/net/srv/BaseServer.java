package bgu.spl.net.srv;

import bgu.spl.net.api.BGSCommand;
import bgu.spl.net.api.ConnectionsImpl;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

/** An abstract server that allow its derivatives to implement different concurrency models
 * Our TCP server needs to create a new Protocol and EncoderDecoder for every connection it receives
 * but since it is a generic server, it does not know what and how to create such objects.
 * This problem is solved using factories, the server receives factories in its constructor that create those objects for it.
 * @param <T>
 */
public abstract class BaseServer<T> implements Server<T> {

    // fields

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket serverSocket;
    private Connections<BGSCommand> currentServerConnections;

    // constructor

    public BaseServer(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        serverSocket = null;
        currentServerConnections = new ConnectionsImpl<>();
    }

    // methods

    @Override
    public void serve() {

        // try-with-resource statement that is allowed due to the ServerSocket's closeable attribute

        try (ServerSocket serverSocket = new ServerSocket(port)) {

			System.out.println("Server started");
            this.serverSocket = serverSocket; // just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSocket = serverSocket.accept(); // blocks until a connection is made
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(clientSocket, encdecFactory.get(),
                        protocolFactory.get());
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {

		if (serverSocket != null)
            serverSocket.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}