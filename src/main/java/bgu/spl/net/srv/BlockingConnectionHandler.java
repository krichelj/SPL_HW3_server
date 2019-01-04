package bgu.spl.net.srv;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.BGSMessages.*;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.User;
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
    private User currentActiveUser;
    private boolean hasUserLoggedIn;

    public BlockingConnectionHandler(Socket clientSocket, MessageEncoderDecoder<T> currentEncoderDecoder, BidiMessagingProtocol<T> currentProtocol) {

        this.currentProtocol = currentProtocol;
        this.currentEncoderDecoder = currentEncoderDecoder;
        this.clientSocket = clientSocket;
        hasUserLoggedIn = false;
        currentActiveUser = null;
    }

    @Override
    public void run() {


        try (Socket clientSocket = this.clientSocket) {

            int input;

            inputStream = new BufferedInputStream(clientSocket.getInputStream());
            outputStream = new BufferedOutputStream(clientSocket.getOutputStream());

            while (!currentProtocol.shouldTerminate() && connected && (input = inputStream.read()) >= 0) {

                T currentRawReadMessage = currentEncoderDecoder.decodeNextByte((byte) input);

                if (currentRawReadMessage != null) {

                    BGSMessage currentReadMessage = (BGSMessage) currentRawReadMessage;
                    Short currentOpcode = currentReadMessage.getOpCode();

                    if (currentOpcode == 1) { // RegisterMessage

                        RegisterMessage messageToProcess = (RegisterMessage) currentReadMessage;
                        System.out.println(messageToProcess.getUserToRegister().getUsername() + " wants to register with password: "
                            + messageToProcess.getUserToRegister().getPassword());
                    }

                    else if (currentOpcode == 2){ // LoginMessage

                        LoginMessage messageToProcess = (LoginMessage) currentReadMessage;
                        System.out.println(messageToProcess.getUserToLogin().getUsername() + " wants to login with password: "
                                + messageToProcess.getUserToLogin().getPassword());

                    }

                    else if (currentOpcode == 3) // LogoutMessage
                        System.out.println(currentActiveUser.getUsername() + " wants to logout");

                    else if (currentOpcode == 4) { // FollowUnfollowMessage

                        FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) currentReadMessage;
                        String followOrUnfollow;

                        if (messageToProcess.getFollowOrUnfollow() == 0)
                            followOrUnfollow = "follow ";
                        else
                            followOrUnfollow = "unfollow ";

                        System.out.println(currentActiveUser.getUsername() + " wants to " + followOrUnfollow +
                                messageToProcess.getNumOfUsers() + " users");
                    }

                    currentProtocol.process(currentRawReadMessage);
                }
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

    public void disconnect() {

        connected = false;
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

    public void setCurrentActiveUser(User currentActiveUser) {

        this.currentActiveUser = currentActiveUser;
        hasUserLoggedIn = true;
    }

    public User getCurrentActiveUser() {

        return currentActiveUser;
    }

    public boolean hasUserLoggedIn() {

        return hasUserLoggedIn;
    }
}