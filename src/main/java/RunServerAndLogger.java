import org.jetlang.channels.MemoryChannel;

public class RunServerAndLogger {
    public static void main(String[] args) {
        MemoryChannel<ClientMessage> clientMessageChannel = new MemoryChannel<>();
        MemoryChannel<String> loggerMessageChannel = new MemoryChannel<>();

        ClientConnectionLogger connectionLogger = new ClientConnectionLogger();
        connectionLogger.attachInputChannelAndSubscribe(clientMessageChannel);
        connectionLogger.attachOutputChannel(loggerMessageChannel);

        Server server = new Server(4444);
        server.attachClientMessageChannel(clientMessageChannel);
        server.attachLoggerMessageChannel(loggerMessageChannel);
        server.start();


        while (true) {

        }

    }
}
