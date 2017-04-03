import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Server {
    private MemoryChannel<ClientMessage> clientMessageChannel;
    private NewWebSocketConnectionsSet newConnectionsSet;
    private ArrayList<SocketWithStreams> acceptedConnections;
    private ThreadFiber connectionProcessFiber;
    private ThreadFiber webSocketMessageFiber;
    private WebSocketFrameGenerator frameGenerator;
    private boolean terminated = false;

    public Server(int port) {
        try {
            newConnectionsSet = new NewWebSocketConnectionsSet(port);
            connectionProcessFiber = new ThreadFiber();
            webSocketMessageFiber = new ThreadFiber();
            acceptedConnections = new ArrayList<>();
            frameGenerator = new WebSocketFrameGenerator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attachClientMessageChannel(MemoryChannel<ClientMessage> clientMessageChannel) {
        this.clientMessageChannel = clientMessageChannel;
    }

    public void attachLoggerMessageChannel(MemoryChannel<String> loggerMessageChannel) {
        webSocketMessageFiber.start();
        Callback<String> callback = (msg) -> writeMessageToAllOpenWebSockets(msg);
        loggerMessageChannel.subscribe(webSocketMessageFiber, callback);
    }

    private void writeMessageToAllOpenWebSockets(String msg) {
        for (int i = 0; i < acceptedConnections.size(); i++) {
            try {
                acceptedConnections.get(i).getOpenOutputStream().write(frameGenerator.generateFrame(msg));
            } catch (Exception e) {
                acceptedConnections.remove(i);
            }
        }
    }

    public void start() {
        newConnectionsSet.start();
        connectionProcessFiber.start();
        connectionProcessFiber.execute(() -> {
            while (!terminated) {
                addOneWaitingNewConnection();
                processAnyMessagesWaitingFromExistingChannels();
            }
        });
    }

    private void addOneWaitingNewConnection() {
        SocketWithStreams newConnection = newConnectionsSet.getNextAcceptedConnection();
        if (!EmptySocket.class.isInstance(newConnection)) {
            acceptedConnections.add(newConnection);
        }
    }

    private void processAnyMessagesWaitingFromExistingChannels() {
        for (int i = 1; i <= acceptedConnections.size(); i++) {
            InputStream currentSocketInputStream = acceptedConnections.get(i - 1).getOpenInputStream();
            try {
                if (currentSocketInputStream.available() > 0) {
                    clientMessageChannel.publish(new ClientMessage("New message from client " + i + "\n"));
                    currentSocketInputStream.skip(currentSocketInputStream.available());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public void stop() {
        newConnectionsSet.stop();
        webSocketMessageFiber.dispose();
        connectionProcessFiber.dispose();
    }

}
