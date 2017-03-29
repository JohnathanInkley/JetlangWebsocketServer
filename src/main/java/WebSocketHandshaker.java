import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketHandshaker {

    public String generateAndSendHandshakeResponse(SocketWithStreams webSocket) {
        try {
            String response;
            String request = getRequestFromSocket(webSocket);
            Matcher startOfHandshakeRequestMatcher = Pattern.compile("^GET").matcher(request);
            if (startOfHandshakeRequestMatcher.find()) {
                response = generateResponse(request);
                webSocket.getOpenOutputStream().write(response.getBytes("UTF-8"));
            } else {
                response = "Handshake failed";
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Response could not be generated");
        }
    }


    private String getRequestFromSocket(SocketWithStreams webSocket) throws IOException {
        return new Scanner(webSocket.getOpenInputStream(), "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
    }

    private String generateResponse(String request) {
        try {
            Matcher webSocketKeyMatcher = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(request);
            webSocketKeyMatcher.find();
            return ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + DatatypeConverter
                    .printBase64Binary(
                            MessageDigest
                                    .getInstance("SHA-1")
                                    .digest((webSocketKeyMatcher.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                            .getBytes("UTF-8")))
                    + "\r\n\r\n");
        } catch (Exception e) {
            throw new RuntimeException("Key could not be found");
        }
    }

}
