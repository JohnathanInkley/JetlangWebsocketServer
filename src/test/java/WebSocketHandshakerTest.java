import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketHandshakerTest {

    @Test
    public void handshakerShouldGenerateCorrectResponseFromRequest() {
        try {
            Socket mockSocket = createMockSocketWithHTTPRequest();
            WebSocketHandshaker handshaker = new WebSocketHandshaker();
            String response = handshaker.generateAndSendHandshakeResponse(mockSocket);
            Assert.assertEquals(getExpectedResponse(), response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Socket createMockSocketWithHTTPRequest() throws IOException {
        final Socket mockSocket = mock(Socket.class);
        final InputStream socketInputStream = mock (InputStream.class);
        when(mockSocket.getInputStream()).thenReturn(socketInputStream);
        when(socketInputStream.read(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            byte[] byteArrayUsedForReading = (byte[]) args[0];
            byte[] tempByteArray = ("GET /chat HTTP/1.1\n" +
                    "Host: example.com:8000\n" +
                    "Upgrade: websocket\n" +
                    "Connection: Upgrade\n" +
                    "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\n" +
                    "Sec-WebSocket-Version: 13\r\n").getBytes();
            for (int i = 0; i <= tempByteArray.length; i++) {
                byteArrayUsedForReading[i] = tempByteArray[i];
            }
            return tempByteArray.length;
        });
        return mockSocket;
    }

    private String getExpectedResponse() {
        return "HTTP/1.1 101 Switching Protocols\n" +
                "Upgrade: websocket\n" +
                "Connection: Upgrade\n" +
                "Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n";
    }



}