import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnectionLogger {
    private MemoryChannel<ClientMessage> inputChannel;
    private MemoryChannel<String> outputChannel;
    private ThreadFiber fiber;
    private Logger clientMessageLog;

    public ClientConnectionLogger() {
        try {
            fiber = new ThreadFiber();
            fiber.start();
            clientMessageLog = Logger.getAnonymousLogger();
            FileHandler logFile = new FileHandler("clientMessages.log");
            clientMessageLog.addHandler(logFile);
        } catch (Exception e) {
            throw new RuntimeException("Log file could not be used");
        }
    }

    public void stop() {
        clientMessageLog.getHandlers()[0].close();
        fiber.dispose();
    }

    public void attachOutputChannel(MemoryChannel<String> outputChannel) {
        this.outputChannel = outputChannel;
    }

    public void attachInputChannelAndSubscribe(MemoryChannel inputChannel) {
        this.inputChannel = inputChannel;
        Callback<ClientMessage> callback = (msg) -> {
            logMessage(msg);
            publishMessageIfOutputChannelPresent(msg);
        };
        inputChannel.subscribe(fiber, callback);
    }

    private void logMessage(ClientMessage message) {
        clientMessageLog.log(Level.SEVERE, message.getMessageReference());
    }

    private void publishMessageIfOutputChannelPresent(ClientMessage msg) {
        if (outputChannel != null) {
            outputChannel.publish(msg.getMessageReference());
        }
    }

}
