import org.jetlang.channels.MemoryChannel;

import static spark.Spark.get;
import static spark.SparkBase.staticFileLocation;

public class RunServerAndLogger {
    public static void main(String[] args) {
        MemoryChannel<ClientMessage> clientMessageChannel = new MemoryChannel<>();
        MemoryChannel<String> loggerMessageChannel = new MemoryChannel<>();

        ClientConnectionLogger connectionLogger = new ClientConnectionLogger();
        connectionLogger.attachInputChannelAndSubscribe(clientMessageChannel);
        connectionLogger.attachOutputChannel(loggerMessageChannel);

        Server server = new Server(Integer.valueOf(args[0]));
        server.attachClientMessageChannel(clientMessageChannel);
        server.attachLoggerMessageChannel(loggerMessageChannel);
        server.start();

        staticFileLocation("/public");
        get("", (res, req) -> {
            return null;
        });

        while (true) {

        }

    }
}
