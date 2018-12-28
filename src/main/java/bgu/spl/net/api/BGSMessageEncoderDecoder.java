package bgu.spl.net.api;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class BGSMessageEncoderDecoder<T> implements MessageEncoderDecoder<Serializable> {

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);

    @Override
    public Serializable decodeNextByte(byte nextByte) {


        return null;
    }

    @Override
    public byte[] encode(Serializable message) {

        return new byte[0];
    }
}
