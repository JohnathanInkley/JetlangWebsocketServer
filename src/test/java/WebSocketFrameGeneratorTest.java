import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class WebSocketFrameGeneratorTest {

   @Test
   public void firstByteShouldContainDataType() {
       WebSocketFrameGenerator underTest = new WebSocketFrameGenerator();

       byte realByte = (byte) 129;

       Assert.assertEquals(realByte, underTest.TEXT_TYPE_BYTE);
   }

   @Test
    public void secondByteShouldByLengthIfNumberBytesUnder125() {
       WebSocketFrameGenerator underTest = new WebSocketFrameGenerator();

       StringBuilder stringOfLength125 = new StringBuilder();
       for (int i = 0; i < 125; i++) {
           stringOfLength125.append("a");
       }

       byte[] lengthBytes = underTest.generateLengthBytes(stringOfLength125.toString());

       Assert.assertEquals((byte) 125, lengthBytes[0]);
       Assert.assertEquals(1, lengthBytes.length);
   }

   @Test
    public void secondAndFollowing2BytesShouldBeLengthIfLengthOver125ButLessThan65535() {
       WebSocketFrameGenerator underTest = new WebSocketFrameGenerator();

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
       WebSocketFrameGenerator underTest = new WebSocketFrameGenerator();

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

        WebSocketFrameGenerator underTest = new WebSocketFrameGenerator();
        byte[] processedMessage = underTest.generateFrame(message);

        for (int i = 0; i < 7; i++) {
            Assert.assertEquals(expectedMessage[i], processedMessage[i]);
        }
    }
}