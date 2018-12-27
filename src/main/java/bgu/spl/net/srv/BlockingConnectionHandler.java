package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/** BlockingConnectionHandler is designed to run by its own thread.
 * It handles one connection to one client for the whole period during which the client is connected
 * (from the moment the connection is accepted, until one of the sides decides to close the connection).
 * It therefore is modeled as a Runnable class.
 * @param <T>
 */
public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol; // the protocol in use
    private final MessageEncoderDecoder<T> encdec; // the encoded - decoder in use
    private final Socket sock; // the TCP socket connected to the client
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {

        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    @Override
    public void run() {

        try (Socket sock = this.sock) {
            int read;

            inputStream = new BufferedInputStream(sock.getInputStream());
            outputStream = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = inputStream.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    /*protocol.process(nextMessage);
                    out.write(encdec.encode(response));
                    out.flush();*/ // TODO implement this
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {



    }
}
