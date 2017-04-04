import org.jetlang.fibers.ThreadFiber;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class NewWebSocketConnectionsSet {
    private ServerSocket serverSocket;
    private ThreadFiber fiber;
    private HashSet<SocketWithStreams> acceptedConnections;
    private WebSocketHandshaker handshaker;
    private boolean terminated = false;

    public NewWebSocketConnectionsSet(int port) throws IOException {
        fiber = new ThreadFiber();
        serverSocket = new ServerSocket(port);
        acceptedConnections = new HashSet<>();
        handshaker = new WebSocketHandshaker();
    }

    public void start() {
        fiber.start();
        fiber.execute(() -> {
            while(!terminated) {
                processAnIncomingConnection();
            }
        });
    }

    private void processAnIncomingConnection() {
        try {
            Socket newConnection = serverSocket.accept();
            SocketWithStreams fullConnection = new SocketWithStreams(newConnection, newConnection.getInputStream(), newConnection.getOutputStream());
            handshaker.generateAndSendHandshakeResponse(fullConnection);
            synchronized (this) {
                acceptedConnections.add(fullConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    synchronized public boolean connectionWaiting() {
        return !acceptedConnections.isEmpty();
    }

    synchronized public SocketWithStreams getNextAcceptedConnection() {
        if (acceptedConnections.isEmpty()) {
            return new EmptySocket();
        }

        SocketWithStreams nextConnection = acceptedConnections.iterator().next();
        acceptedConnections.remove(nextConnection);
        return nextConnection;
    }

    public void stop() {
        try {
            terminated = true;
            fiber.dispose();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket");
            e.printStackTrace();
        }
    }
}
