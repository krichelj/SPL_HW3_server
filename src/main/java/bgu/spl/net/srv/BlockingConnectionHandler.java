package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> currentProtocol;
    private final MessageEncoderDecoder<T> currentEncoderDecoder;
    private final Socket clientSocket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket clientSocket, MessageEncoderDecoder<T> currentEncoderDecoder, BidiMessagingProtocol<T> currentProtocol) {

        this.currentProtocol = currentProtocol;
        this.currentEncoderDecoder = currentEncoderDecoder;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {


        try (Socket clientSocket = this.clientSocket) {

            int input;

            inputStream = new BufferedInputStream(clientSocket.getInputStream());
            outputStream = new BufferedOutputStream(clientSocket.getOutputStream());

            while (!currentProtocol.shouldTerminate() && connected && (input = inputStream.read()) >= 0) {

                T currentRawReadMessage = currentEncoderDecoder.decodeNextByte((byte) input);

                if (currentRawReadMessage != null)
                    currentProtocol.process(currentRawReadMessage);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        clientSocket.close();
        inputStream.close();
        outputStream.close();
    }

    @Override
    public void send(T msg) {

        try {
            outputStream.write(currentEncoderDecoder.encode(msg));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startProtocol(int connectionId, Connections<T> connections){

        currentProtocol.start(connectionId,connections);
    }
}