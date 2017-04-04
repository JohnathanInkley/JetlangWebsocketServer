import java.io.UnsupportedEncodingException;

public class WebSocketFrameEncoderDecoder {
    public static final byte TEXT_TYPE_BYTE = (byte) 129;

    private final int LOWER_MESSAGE_LENGTH_BOUNDARY = 125;
    private final int LOWER_MESSAGE_LENGTH_BOUNDARY_FOR_ENCODED = 0xFE;
    private final int UPPER_MESSAGE_LENGTH_BOUNDARY = 65535;

    private final byte FIRST_LENGTH_BYTE_FOR_LENGTHS_BETWEEN_UPPER_LOWER_BOUNDARY = (byte) 126;

    private final byte FIRST_LENGTH_BYTE_FOR_LENGTHS_ABOVE_UPPER_BOUNDARY = (byte) 127;
    private final int NUM_LENGTH_BYTES_FOR_LENGTHS_ABOVE_UPPER_BOUNDARY = 8;

    private final int NUM_MASK_BYTES = 4;
    private final int AND_BYTE_WITH_TO_MAKE_UNSIGNED = 0xff;
    private final int REMOVE_MASK_BIT = 0x80;

    public byte[] generateFrame(String message) {
        byte[] messageAsBytes = getMessageAsBytes(message);
        byte[] lengthBytes = generateLengthBytes(message);
        byte[] results = new byte[1 + messageAsBytes.length + lengthBytes.length];
        results[0] = TEXT_TYPE_BYTE;
        System.arraycopy(lengthBytes, 0, results, 1, lengthBytes.length);
        System.arraycopy(messageAsBytes, 0, results, lengthBytes.length + 1, messageAsBytes.length);

        return results;
    }

    private byte[] getMessageAsBytes(String message) {
        byte[] messageAsBytes;
        try {
            messageAsBytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 encoding cannot be found.");
        }
        return messageAsBytes;
    }

    public byte[] generateLengthBytes(String message) {
        byte[] messageAsBytes = message.getBytes();
        long msgLength = messageAsBytes.length;
        if (msgLength <= LOWER_MESSAGE_LENGTH_BOUNDARY) {
            return new byte[]{(byte) msgLength};
        } else if (message.length() <= UPPER_MESSAGE_LENGTH_BOUNDARY) {
            return new byte[]{(byte) FIRST_LENGTH_BYTE_FOR_LENGTHS_BETWEEN_UPPER_LOWER_BOUNDARY, (byte) (msgLength >> 8), (byte) msgLength };
        } else {
            return generateLengthBytesForOver65535(msgLength);
        }
    }

    private byte[] generateLengthBytesForOver65535(long msgLength) {
        byte[] result = new byte[9];
        result[0] = FIRST_LENGTH_BYTE_FOR_LENGTHS_ABOVE_UPPER_BOUNDARY;

        int num = NUM_LENGTH_BYTES_FOR_LENGTHS_ABOVE_UPPER_BOUNDARY;
        for (int i = 1; i <= num; i++) {
            result[i] = (byte) (msgLength >> num*(num - i));
        }

        return result;
    }


    public byte[] decodeFrame(byte[] frame) {
        long frameLength = getLengthOfFrame(frame);
        byte[] result = new byte[(int) frameLength];
        byte[] maskBits = new byte[4];
        int offset = (frameLength <= LOWER_MESSAGE_LENGTH_BOUNDARY) ? 1 : 2;

        for (int i = 0; i < NUM_MASK_BYTES; i++) {
            maskBits[i] = frame[i + 1 + offset];
        }

        for (int i = 0; i < frameLength; i++) {
            result[i] = (byte) (frame[i + 1 + offset + NUM_MASK_BYTES] ^ maskBits[i % NUM_MASK_BYTES]);
        }

        return result;
    }

    public long getLengthOfFrame(byte[] frame) {
        if (makeUnsignedInt(frame[1]) < LOWER_MESSAGE_LENGTH_BOUNDARY_FOR_ENCODED) {
            return (long) makeUnsignedInt(frame[1] - REMOVE_MASK_BIT);
        } else if ((makeUnsignedInt(frame[1])) == LOWER_MESSAGE_LENGTH_BOUNDARY_FOR_ENCODED) {
            return ((((long) frame[2]) & AND_BYTE_WITH_TO_MAKE_UNSIGNED) << 8) + ((long) makeUnsignedInt(frame[3]));
        } else if ((makeUnsignedInt(frame[1])) == AND_BYTE_WITH_TO_MAKE_UNSIGNED) {
            long currentTotal = 0;
            int num = NUM_LENGTH_BYTES_FOR_LENGTHS_ABOVE_UPPER_BOUNDARY;
            for (int i = 0; i < num; i++) {
                currentTotal += ((makeUnsignedInt(frame[i + 2])) << num*(num - 1 - i));
            }
            return currentTotal;
        } else {
            throw new RuntimeException("Frame has incorrect format for length bytes");
        }

    }

    private int makeUnsignedInt(byte b) {
        return b & AND_BYTE_WITH_TO_MAKE_UNSIGNED;
    }

    private int makeUnsignedInt(int i) {
        return i & AND_BYTE_WITH_TO_MAKE_UNSIGNED;
    }

}
