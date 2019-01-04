package bgu.spl.net.api;

import bgu.spl.net.api.BGSMessages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

@SuppressWarnings("FieldCanBeLocal")

public class BGSMessageEncoderDecoder<T> implements MessageEncoderDecoder<BGSMessage> {

    private byte[] shortBytesArray = new byte[2], stringBytesArray = new byte[1 << 10];
    private int index = 0, stringBytesArrayIndex = 0;
    private short currentOpCode = 0, numOfUsers;
    private short followOrUnfollow;
    private String firstString, secondString, addedUser;
    private final byte[] zeroByteArray = {(byte) '\0'};
    private LinkedList<String> userNameList = new LinkedList<>();

    @Override
    public BGSMessage decodeNextByte(byte nextByte) {

        BGSMessage outputMessage = null;

        if (currentOpCode == 0) { // means we haven't finished recording the opcode

            shortBytesArray[index++] = nextByte;

            if (index == 2){ // means we've finished reading the opcode and need to record it
                currentOpCode = shortBytesArrayToShort();
                index = 0;
                if (currentOpCode == 3){ // Logout Message
                    outputMessage = new LogoutMessage();
                }
                else if (currentOpCode == 7) // user list message
                    outputMessage = new UserListMessage();
            }
        }

        else if (currentOpCode == 1 || currentOpCode == 2 || currentOpCode == 6) { // register, login or PM message

           if (index == 0) { // means we haven't recorded the first string yet

               if (nextByte != 0)  // means we haven't finished reading the first string yet
                   pushByte(nextByte);
               else { // means we've finished reading the first string
                   firstString = popString();
                   index++;
               }
           }
           else if (index == 1) { // means we recorded the first string but haven't recorded the second string yet

                if (nextByte != 0) // means we haven't finished reading the second string yet
                    pushByte(nextByte);
                else {// means we've finished reading the second string

                    secondString = popString();

                    if (currentOpCode == 1)
                        outputMessage = new RegisterMessage(firstString, secondString);
                    else if (currentOpCode == 2)
                        outputMessage = new LoginMessage(firstString, secondString);
                    else
                        outputMessage = new PMMessage(firstString, secondString);
                    firstString = "";
                    secondString = "";
                    index = 0;
                    currentOpCode = 0;
                }
            }
        }

        else if (currentOpCode == 4){ // follow unfollow message

            if (index == 1) {   // means we haven't read the followOrUnfollow field yet
                followOrUnfollow = (short) nextByte;
                index++;
            }
            else if (index == 3) {  // means we've finished reading the followOrUnfollow and need to record it
                numOfUsers = (short) nextByte;
                index++;
            }
            else if (index > 3) { // // means we're reading the userNameList field

                if (nextByte != 0) // means we haven't read the current user name yet
                    pushByte(nextByte);
                else { // means we've finished reading the current user name
                    firstString = popString();
                    userNameList.add(firstString);
                    firstString = "";

                    if (userNameList.size() == numOfUsers){ // means we're finished reading all of the user names
                        outputMessage = new FollowUnfollowMessage(followOrUnfollow, numOfUsers, userNameList);
                        index = 0;
                        userNameList = new LinkedList<>();

                    }
                }
            }
            else
                index++;
        }

        else if (currentOpCode == 5){ // PostMessage

            char currentChar = '\0';

            if (index == 0 || index == 1 ) {
                pushByte(nextByte);
                index++;

                if (index == 1) // means we are finished reading the current char
                    currentChar = popString().charAt(0);
            }

            if (currentChar == '\0' || index > 0 ) { // means we haven't finished reading the content field or not finished writing a user name

                if (currentChar == '@'){ // means we are about to read a user name

                    index++;
                }
                else {

                    if (index == 0){ // means we are not reading a user name
                        firstString += (char) nextByte;
                    }
                    else { // means we are in the process of reading a user name
                        if (currentChar == ' ' || currentChar == '\0'){ // means we are finished reading the user name
                            userNameList.add(addedUser);
                            index = 0;
                        }
                        else // means we are currently reading a user name
                            addedUser+= (char) nextByte;
                    }
                }
            }
            else { // means we've finished reading the content field

                if (currentOpCode == 5) {
                    if (userNameList.isEmpty())
                        outputMessage = new PostMessage(firstString);
                    else
                        outputMessage = new PostMessage(firstString, userNameList);
                }
                firstString = "";
            }
        }

        else if (currentOpCode == 8){ // StatsMessage

            if (nextByte != 0) // means we haven't read the current user name yet
                pushByte(nextByte);
            else { // means we've finished reading the current user name
                firstString = popString();
                outputMessage = new StatsMessage(firstString);
                firstString = "";
            }
        }

        return outputMessage; // returns null if not assigned
    }

    private void pushByte(byte nextByte) {

        if (stringBytesArrayIndex >= stringBytesArray.length)
            stringBytesArray = Arrays.copyOf(stringBytesArray, stringBytesArrayIndex * 2);

        stringBytesArray[stringBytesArrayIndex++] = nextByte;
    }

    private String popString() {

        String resultString = new String(stringBytesArray, 0, stringBytesArrayIndex, StandardCharsets.UTF_8);
        stringBytesArrayIndex = 0;
        stringBytesArray = new byte[1 << 10];
        return resultString;
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