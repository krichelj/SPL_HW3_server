package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encoderDecoder;
    private final Socket clientSocket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket clientSocket, MessageEncoderDecoder<T> encoderDecoder, BidiMessagingProtocol<T> protocol) {

        this.clientSocket = clientSocket;
        this.encoderDecoder = encoderDecoder;
        this.protocol = protocol;
    }

    @Override
    public void run() {

        try {

            inputStream = new BufferedInputStream(clientSocket.getInputStream());
            outputStream = new BufferedOutputStream(clientSocket.getOutputStream());

            int read;

            while (!protocol.shouldTerminate() && connected && (read = inputStream.read()) >= 0) {

                T nextMessage = encoderDecoder.decodeNextByte((byte) read);

                /*if (nextMessage != null) {
                    T response = protocol.process(nextMessage);
                    if (response != null) {
                        outputStream.write(encoderDecoder.encode(response));
                        outputStream.flush();
                    }
                }*/
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        clientSocket.close();
        connected = false;
        inputStream.close();
        outputStream.close();
    }

    @Override
    public void send(T msg) {



    }
}