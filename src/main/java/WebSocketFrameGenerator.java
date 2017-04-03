import java.io.UnsupportedEncodingException;

public class WebSocketFrameGenerator {
    public static final byte TEXT_TYPE_BYTE = (byte) 129;

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
            throw new RuntimeException("User message not UTF-8");
        }
        return messageAsBytes;
    }

    public byte[] generateLengthBytes(String message) {
        byte[] messageAsBytes = message.getBytes();
        long msgLength = messageAsBytes.length;
        if (msgLength <= 125) {
            return new byte[]{(byte) msgLength};
        } else if (message.length() <= 65535) {
            return new byte[]{(byte) 126, (byte) (msgLength >> 8), (byte) msgLength };
        } else {
            return generateLengthBytesForOver65535(msgLength);
        }
    }

    private byte[] generateLengthBytesForOver65535(long msgLength) {
        byte[] result = new byte[9];
        result[0] = 127;

        for (int i = 1; i <= 8; i++) {
            result[i] = (byte) (msgLength >> 8*(8 - i));
        }

        return result;
    }
}
