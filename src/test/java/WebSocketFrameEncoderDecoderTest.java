import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class WebSocketFrameEncoderDecoderTest {

   @Test
   public void firstByteShouldContainDataType() {
       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();

       byte realByte = (byte) 129;

       Assert.assertEquals(realByte, underTest.TEXT_TYPE_BYTE);
   }

   @Test
    public void secondByteShouldByLengthIfNumberBytesUnder126() {
       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();

       StringBuilder stringOfLength125 = new StringBuilder();
       for (int i = 0; i < 125; i++) {
           stringOfLength125.append("a");
       }

       byte[] lengthBytes = underTest.generateLengthBytes(stringOfLength125.toString());

       Assert.assertEquals((byte) 125, lengthBytes[0]);
       Assert.assertEquals(1, lengthBytes.length);
   }

   @Test
    public void secondAndFollowing2BytesShouldBeLengthIfLengthOver125ButLessThan65536() {
       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();

       StringBuilder stringOfLength65534 = new StringBuilder();
       for (int i = 0; i < 65534; i++) {
           stringOfLength65534.append("a");
       }

       byte[] lengthBytes = underTest.generateLengthBytes(stringOfLength65534.toString());

       Assert.assertEquals((byte) 126, lengthBytes[0]);
       Assert.assertEquals((byte) 255, lengthBytes[1]);
       Assert.assertEquals((byte) 254, lengthBytes[2]);
       Assert.assertEquals(3, lengthBytes.length);
   }

   @Test
   public void secondAndNext8BytesShouldBeLengthIfLengthOver65535() {
       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();

       StringBuilder stringOfLength6553500 = new StringBuilder();
       for (int i = 0; i < 6553500; i++) {
           stringOfLength6553500.append("a");
       }

       byte[] lengthBytes = underTest.generateLengthBytes(stringOfLength6553500.toString());

       Assert.assertEquals((byte) 127, lengthBytes[0]);
       Assert.assertEquals((byte) 0, lengthBytes[1]);
       Assert.assertEquals((byte) 0, lengthBytes[2]);
       Assert.assertEquals((byte) 0, lengthBytes[3]);
       Assert.assertEquals((byte) 0, lengthBytes[4]);
       Assert.assertEquals((byte) 0, lengthBytes[5]);
       Assert.assertEquals((byte) 99, lengthBytes[6]);
       Assert.assertEquals((byte) 255, lengthBytes[7]);
       Assert.assertEquals((byte) 156, lengthBytes[8]);
       Assert.assertEquals(9, lengthBytes.length);
   }

   @Test
    public void generatorShouldAddCorrectHeaderForSimpleTextMessage() throws UnsupportedEncodingException {
        String message = "hello";
        byte[] expectedMessage = new byte[7];
        expectedMessage[0] = (byte) 129;
        expectedMessage[1] = (byte) 5;
        for (int i = 2; i < 7; i++) {
            expectedMessage[i] = message.getBytes("UTF-8")[i - 2];
        }

        WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();
        byte[] processedMessage = underTest.generateFrame(message);

        for (int i = 0; i < 7; i++) {
            Assert.assertEquals(expectedMessage[i], processedMessage[i]);
        }
    }

    @Test
    public void decoderShouldGetPayloadLengthIfLengthLessThan126() {
       byte[] testFrame = new byte[11];
       testFrame[0] = (byte) 129;
       testFrame[1] = (byte) 0x85;
       testFrame[2] = (byte) 101;
       testFrame[3] = (byte) 102;
       testFrame[4] = (byte) 103;
       testFrame[5] = (byte) 104;
       for (int i = 6; i < 11; i++) {
           testFrame[i] = "hello".getBytes()[i - 6];
       }

       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();
       long lengthOfFrame = underTest.getLengthOfFrame(testFrame);
       Assert.assertEquals(5, lengthOfFrame);

    }

    @Test
    public void decodeShouldGetPayloadLengthIfLengthGreaterThan125LessThan65536() {
       byte[] testFrame = new byte[4];
       testFrame[0] = (byte) 129;
       testFrame[1] = (byte) 0xFE;
       testFrame[2] = (byte) 1;
       testFrame[3] = (byte) 2;

       WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();
       long lengthOfFrame = underTest.getLengthOfFrame(testFrame);
       Assert.assertEquals(258, lengthOfFrame);
    }

    @Test
    public void decodeShouldGetPayloadLengthIfGreaterThan65535() {
        byte[] testFrame = new byte[10];
        testFrame[0] = (byte) 129;
        testFrame[1] = (byte) 0xFF;
        testFrame[2] = (byte) 2;
        testFrame[9] = (byte) 1;

        WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();
        long lengthOfFrame = underTest.getLengthOfFrame(testFrame);
        Assert.assertEquals(33554433, lengthOfFrame);

    }

    @Test
    public void decoderShouldDecodeFrameUsingMaskBits() {
        byte[] testFrame = new byte[11];
        testFrame[0] = (byte) 129;
        testFrame[1] = (byte) 0x85;

        // Masking bits
        testFrame[2] = (byte) 1;
        testFrame[3] = (byte) 2;
        testFrame[4] = (byte) 3;
        testFrame[5] = (byte) 4;

        testFrame[6] = (byte) 3;
        testFrame[7] = (byte) 7;
        testFrame[8] = (byte) 1;
        testFrame[9] = (byte) 6;
        testFrame[10] = (byte) 5;

        WebSocketFrameEncoderDecoder underTest = new WebSocketFrameEncoderDecoder();
        byte[] decodedMessage = underTest.decodeFrame(testFrame);

        Assert.assertEquals((byte) 2, decodedMessage[0]);
        Assert.assertEquals((byte) 5, decodedMessage[1]);
        Assert.assertEquals((byte) 2, decodedMessage[2]);
        Assert.assertEquals((byte) 2, decodedMessage[3]);
        Assert.assertEquals((byte) 4, decodedMessage[4]);
        Assert.assertEquals(5, decodedMessage.length);
    }


}