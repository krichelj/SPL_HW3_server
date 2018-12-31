package bgu.spl.net.api;

import bgu.spl.net.api.Messages.*;
import java.util.LinkedList;

public class BGSMessageEncoderDecoder<T> implements MessageEncoderDecoder<BGSMessage> {

    private byte[] shortBytesArray = new byte[2];
    private int index = 0;
    private short currentOpCode = 0, numOfUsers;
    private char followOrUnfollow;
    private String firstString, secondString;
    private final byte[] zeroByteArray = {(byte) '\0'};
    private LinkedList<String> userNameList = new LinkedList<>();

    @Override
    public BGSMessage decodeNextByte(byte nextByte) {

        BGSMessage outputMessage = null;

        if (currentOpCode == 0) { // means we haven't finished recording the opcode

            if (index < 2) { // means we haven't finished reading the opcode
                shortBytesArray[index++] = nextByte;
            }
            else { // means we've finished reading the opcode and want to record it
                currentOpCode = shortBytesArrayToShort();
                shortBytesArray = new byte[2]; // renew the opcode byte array for next opcode reading
                index = 0;
            }
        }

        else if (currentOpCode == 1 || currentOpCode == 2 || currentOpCode == 6) { // register, login or PM message

           if (index == 0) { // means we haven't recorded the first string yet

               if (!((char) nextByte == '\0') ) // means we haven't finished reading the first string yet
                   firstString += (char) nextByte;
               else  // means we've finished reading the first string
                   index++;
           }
            if (index == 1) { // means we haven't recorded the second string yet

                if (!((char) nextByte == '\0') ) // means we haven't finished reading the second string yet
                    secondString += (char) nextByte;
                else {  // means we've finished reading the second string
                    if (currentOpCode == 1)
                        outputMessage = new RegisterMessage(firstString, secondString);
                    else if (currentOpCode == 2)
                        outputMessage = new LoginMessage(firstString, secondString);
                    else
                        outputMessage = new PMMessage(firstString, secondString);
                    firstString = "";
                    secondString = "";
                    index = 0;
                }
            }
        }

        else if (currentOpCode == 3){ // logout message

            outputMessage = new LogoutMessage();
        }

        else if (currentOpCode == 4){ // follow unfollow message

            if (index == 0){ // means we haven't read the followOrUnfollow field yet
                followOrUnfollow = (char) nextByte;
                index++;
            }
            else if (index == 1 || index == 2) { // means we haven't read the NumOfUsers field yet
                shortBytesArray[index++] = nextByte;
                index++;
            }
            else if (index == 3) { // means we've finished reading the NumOfUsers field and want to record it
                numOfUsers = shortBytesArrayToShort();
                index++;
            }
            else if (index > 3) { // // means we're reading the userNameList field

                if (!((char) nextByte == '\0') ) // means we haven't read the current user name yet
                    firstString += (char) nextByte;
                else { // means we've finished reading the current user name
                    userNameList.add(firstString);
                    firstString = "";

                    if (userNameList.size() == numOfUsers){ // means we're finished reading all of the user names
                        outputMessage = new FollowUnfollowMessage(followOrUnfollow, numOfUsers, userNameList);
                        index = 0;
                        userNameList = new LinkedList<>();
                    }
                }
            }
        }

        else if (currentOpCode == 5 || currentOpCode == 8){// post message

            if (!((char) nextByte == '\0') ) // means we haven't read the content field
                firstString += (char) nextByte;
            else { // means we've finished reading the content field
                if (currentOpCode == 5)
                    outputMessage = new PostMessage(firstString);
                else
                    outputMessage = new StatsMessage(firstString);
                firstString = "";
            }
        }

        else if (currentOpCode == 7){ // user list message

            outputMessage = new UserListMessage();

        }

        currentOpCode = 0;

        return outputMessage;
    }

    @Override
    public byte[] encode(BGSMessage message) {

        byte[] outputByteArray = null;
        short opCode = message.getOpCode();
        byte[] outputOpCodeBytesArray = shortToBytesArray(opCode);

        if (opCode == 9) { // NotificationMessage

            byte[] messageTypeByte = {(byte) ((NotificationMessage) message).getMessageType()};
            outputByteArray = concatenateByteArrays (outputOpCodeBytesArray, messageTypeByte, ((NotificationMessage) message).getPostingUser().getBytes(),
                    zeroByteArray, ((NotificationMessage) message).getContent().getBytes(), zeroByteArray);

        }
        else if (opCode == 10){ // AckMessage

            outputByteArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray(((AckMessage) message).getMessageOpCope()));

        }
        else if (opCode == 11){ // ErrorMessage

            outputByteArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray(((ErrorMessage) message).getMessageOpCope()));

        }

        return outputByteArray;
    }

    private short shortBytesArrayToShort() {

        short result = (short)((shortBytesArray[0] & 0xff) << 8);
        result += (short)(shortBytesArray[1] & 0xff);
        shortBytesArray = new byte[2]; // renew the opcode byte array for next opcode reading

        return result;
    }

    private byte[] shortToBytesArray(short shortInput) {

        byte[] outputByteArray = new byte[2];

        outputByteArray[0] = (byte)((shortInput >> 8) & 0xFF);
        outputByteArray[1] = (byte)(shortInput & 0xFF);

        return outputByteArray;
    }

    private byte[] concatenateByteArrays (byte[]... byteArrays){

        int outputArrayLength = 0;
        for (byte[] currentByteArray : byteArrays)
            outputArrayLength += currentByteArray.length;

        byte[] outputByteArray = new byte[outputArrayLength];

        int currentPosition = 0;

        for (byte[] currentByteArray : byteArrays) {
            System.arraycopy(currentByteArray, 0, outputByteArray, currentPosition, currentByteArray.length);
            currentPosition += currentByteArray.length;
        }

        return outputByteArray;
    }
}