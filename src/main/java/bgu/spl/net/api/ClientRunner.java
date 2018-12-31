package bgu.spl.net.api;

import bgu.spl.net.api.Messages.AckMessage;
import bgu.spl.net.api.Messages.RegisterMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientRunner {

    public static void main (String[] args){

        BGSMessageEncoderDecoder encoderDecoder = new BGSMessageEncoderDecoder();

        try (Socket sock = new Socket("localhost", 7777);
             BufferedInputStream inputStream = new BufferedInputStream(sock.getInputStream());
             BufferedOutputStream outputStream = new BufferedOutputStream(sock.getOutputStream()) ){

            // send block

            outputStream.write(encoderDecoder.encode(new AckMessage((short)1)));
            outputStream.flush();

            // receive block
            int read;
            BGSMessage msg = null;

            while ((read = inputStream.read()) >= 0) {
                msg = encoderDecoder.decodeNextByte((byte) read);
                if (msg != null) {
                    System.out.println(msg.getOpCode());
                    return;
                }
            }

            throw new IOException("disconnected before complete reading message");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}