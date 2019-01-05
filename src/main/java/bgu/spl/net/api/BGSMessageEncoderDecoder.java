package bgu.spl.net.api;

import bgu.spl.net.api.BGSMessages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

@SuppressWarnings("FieldCanBeLocal")

public class BGSMessageEncoderDecoder implements MessageEncoderDecoder<BGSMessage> {

    private byte[] shortBytesArray = new byte[2], stringBytesArray1 = new byte[1 << 10], stringBytesArray2 = new byte[1 << 10];
    private int index = 0, stringBytesArrayIndex1 = 0, stringBytesArrayIndex2 = 0 ;
    private short currentOpCode = 0, numOfUsers;
    private short followOrUnfollow;
    private String firstString, secondString;
    private final byte[] zeroByteArray = {(byte) '\0'};
    private LinkedList<String> userNameList = new LinkedList<>();

    @Override
    public BGSMessage decodeNextByte(byte nextByte) {

        BGSMessage outputMessage = null;

        if (currentOpCode == 0) { // means we haven't finished recording the opcode

            shortBytesArray[index++] = nextByte;

            if (index == 2){ // means we've finished reading the opcode and need to record it

                currentOpCode = shortBytesArrayToShort();

                if (currentOpCode == 3) { // Logout Message
                    outputMessage = new LogoutMessage();
                    currentOpCode = 0;
                }
                else if (currentOpCode == 7) { // user list message
                    outputMessage = new UserListMessage();
                    currentOpCode = 0;
                }

                index = 0;
            }
        }

        else if (currentOpCode == 1 || currentOpCode == 2 || currentOpCode == 6) { // register, login or PM message

           if (index == 0) { // means we haven't recorded the first string yet

               if (nextByte != 0)  // means we haven't finished reading the first string yet
                   pushByte(nextByte,1);
               else { // means we've finished reading the first string
                   firstString = popString(1);
                   index++;
               }
           }
           else if (index == 1) { // means we recorded the first string but haven't recorded the second string yet

                if (nextByte != 0) // means we haven't finished reading the second string yet
                    pushByte(nextByte,1);
                else {// means we've finished reading the second string

                    secondString = popString(1);

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

        else if (currentOpCode == 4){ // FollowUnfollowMessage or UserListMessage

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
                    pushByte(nextByte,1);
                else { // means we've finished reading the current user name
                    firstString = popString(1);
                    userNameList.add(firstString);
                    firstString = "";

                    if (userNameList.size() == numOfUsers){ // means we're finished reading all of the user names
                        outputMessage = new FollowUnfollowMessage(followOrUnfollow, numOfUsers, userNameList);
                        index = 0;
                        userNameList = new LinkedList<>();
                        currentOpCode = 0;
                    }
                }
            }
            else
                index++;
        }

        else if (currentOpCode == 5){ // PostMessage

            if (nextByte != 0) { // means we haven't finished reading the content

                if (index == 0) { // means we're not reading a username

                    pushByte(nextByte,1);

                    if (nextByte == 64) // means we're reading the '@' char
                        index++;
                }
                else { // means we're reading a username

                    if (nextByte != 32) { // means we haven't finished reading the username
                        pushByte(nextByte,1);
                        pushByte(nextByte, 2);
                    }

                    else { // means we've finished reading the username
                        pushByte(nextByte,1);
                        userNameList.add(popString(2));
                        index = 0;
                    }
                }
            }
            else { // we've reached the end of the content

                if (index > 0){ // if we finished the line when a user was read

                    userNameList.add(popString(2));
                    index = 0;
                }

                if (userNameList.isEmpty())
                    outputMessage = new PostMessage(popString(1));
                else
                    outputMessage = new PostMessage(popString(1), userNameList);

                currentOpCode = 0;
                userNameList = new LinkedList<>();
            }
        }

        else if (currentOpCode == 8){ // StatsMessage

            if (nextByte != 0) // means we haven't read the current user name yet
                pushByte(nextByte,1);
            else { // means we've finished reading the current user name
                firstString = popString(1);
                outputMessage = new StatsMessage(firstString);
                firstString = "";
                currentOpCode = 0;
            }
        }

        return outputMessage; // returns null if not assigned
    }

    private void pushByte(byte nextByte, int index) {

        if (index == 1) {

            if (stringBytesArrayIndex1 >= stringBytesArray1.length)
                stringBytesArray1 = Arrays.copyOf(stringBytesArray1, stringBytesArrayIndex1 * 2);

            stringBytesArray1[stringBytesArrayIndex1++] = nextByte;
        }
        else{
            if (stringBytesArrayIndex2 >= stringBytesArray2.length)
                stringBytesArray2 = Arrays.copyOf(stringBytesArray2, stringBytesArrayIndex2 * 2);

            stringBytesArray2[stringBytesArrayIndex2++] = nextByte;

        }
    }

    private String popString(int index) {

        String resultString;

        if (index == 1) {
            resultString = new String(stringBytesArray1, 0, stringBytesArrayIndex1, StandardCharsets.UTF_8);
            stringBytesArrayIndex1 = 0;
            stringBytesArray1 = new byte[1 << 10];
        }
        else {
            resultString = new String(stringBytesArray2, 0, stringBytesArrayIndex2, StandardCharsets.UTF_8);
            stringBytesArrayIndex2 = 0;
            stringBytesArray2 = new byte[1 << 10];

        }
        return resultString;
    }

    @Override
    public byte[] encode(BGSMessage message) {

        byte[] outputBytesArray = null;
        short opCode = message.getOpCode();
        byte[] outputOpCodeBytesArray = shortToBytesArray(opCode);

        if (opCode == 9) { // NotificationMessage

            NotificationMessage currentAckMessage = (NotificationMessage) message;

            byte[] messageTypeByte = {(byte) currentAckMessage.getMessageType()};
            outputBytesArray = concatenateByteArrays (outputOpCodeBytesArray, messageTypeByte, currentAckMessage.getPostingUser().getBytes(),
                    zeroByteArray, currentAckMessage.getContent().getBytes(), zeroByteArray);

        }
        else if (opCode == 10){ // AckMessage

            AckMessage currentAckMessage = (AckMessage) message;
            short messageOpCope = currentAckMessage.getMessageOpCope();

            if (messageOpCope == 4 || messageOpCope == 7){

                outputBytesArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray(messageOpCope),
                        shortToBytesArray(currentAckMessage.getNumOfUsers()));

                for (String currentUserName : currentAckMessage.getUserNameList())
                    outputBytesArray = concatenateByteArrays (outputBytesArray, currentUserName.getBytes(), zeroByteArray);
            }
            else if (messageOpCope == 8){

                outputBytesArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray(messageOpCope),
                        shortToBytesArray(currentAckMessage.getNumOfPosts()), shortToBytesArray(currentAckMessage.getNumOfFollowers()),
                        shortToBytesArray(currentAckMessage.getNumOfFollowing()));
            }
            else
                outputBytesArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray((messageOpCope)));

        }
        else if (opCode == 11) // ErrorMessage
            outputBytesArray = concatenateByteArrays (outputOpCodeBytesArray, shortToBytesArray(((ErrorMessage) message).getMessageOpCope()));



        return outputBytesArray;
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