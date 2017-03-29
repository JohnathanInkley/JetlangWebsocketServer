import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWithStreams {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SocketWithStreams(Socket socket, InputStream inputStream, OutputStream outputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getOpenInputStream() {
        return inputStream;
    }

    public OutputStream getOpenOutputStream() {
        return outputStream;
    }
}
