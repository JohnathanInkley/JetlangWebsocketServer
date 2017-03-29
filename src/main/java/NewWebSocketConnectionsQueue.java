import org.jetlang.fibers.ThreadFiber;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class NewWebSocketConnectionsQueue {
    private ServerSocket serverSocket;
    private ThreadFiber fiber;
    private HashSet<Socket> acceptedConnections;
    private boolean terminated = false;

    public NewWebSocketConnectionsQueue(int port) throws IOException {
        fiber = new ThreadFiber();
        serverSocket = new ServerSocket(port);
        acceptedConnections = new HashSet<>();
    }

    public void start() {
        fiber.start();
        fiber.execute(() -> {
            while(!terminated) {
                processIncomingConnections();
            }
        });
    }

    private void processIncomingConnections() {
        try {
            Socket newConnection = serverSocket.accept();
            synchronized (this) {
                acceptedConnections.add(newConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    synchronized public boolean connectionWaiting() {
        return !acceptedConnections.isEmpty();
    }

    synchronized public Socket getNextAcceptedConnection() {
        if (acceptedConnections.isEmpty()) {
            return new EmptySocket();
        }

        Socket nextConnection = acceptedConnections.iterator().next();
        acceptedConnections.remove(nextConnection);
        return nextConnection;
    }
}
