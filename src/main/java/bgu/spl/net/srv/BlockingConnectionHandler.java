package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.User;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encoderDecoder;
    private final Socket clientSocket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private volatile boolean connected = true;
    private User currentActiveUser;

    public BlockingConnectionHandler(Socket clientSocket, MessageEncoderDecoder<T> encoderDecoder, BidiMessagingProtocol<T> protocol) {

        this.protocol = protocol;
        this.encoderDecoder = encoderDecoder;
        this.clientSocket = clientSocket;
        currentActiveUser = null;
    }

    @Override
    public void run() {

        try {

            inputStream = new BufferedInputStream(clientSocket.getInputStream());
            outputStream = new BufferedOutputStream(clientSocket.getOutputStream());

            int read;

            while (!protocol.shouldTerminate() && connected && (read = inputStream.read()) >= 0) {

                T nextMessage = encoderDecoder.decodeNextByte((byte) read);

                /*if (((BGSMessage) nextMessage).getOpCode() == 1)
                    System.out.println("REGISTER");*/

                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {

        connected = false;
        clientSocket.close();
        inputStream.close();
        outputStream.close();
    }

    @Override
    public void send(T msg) {

        try {
            outputStream.write(encoderDecoder.encode(msg));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentActiveUser(User currentActiveUser) {

        currentActiveUser = currentActiveUser;
    }

    public User getCurrentActiveUser() {

        return currentActiveUser;
    }
}