package bgu.spl.net.srv;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {

    public static void main (String[] args){

        int port = 1025;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true)
            {
                Socket clientSocket = serverSocket.accept();
                clientSocket.getOutputStream().write("Hi my name is Chicka \n".getBytes());
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
