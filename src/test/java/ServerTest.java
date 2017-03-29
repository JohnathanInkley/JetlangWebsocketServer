import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerTest {

    @Test
    public void newConnectionShouldPublishMessageToChannel() throws IOException, InterruptedException {
        Server underTest = new Server(4444);
        MemoryChannel<ClientMessage> clientMessageChannel = new MemoryChannel<>();
        underTest.attachClientMessageChannel(clientMessageChannel);
        underTest.start();

        ThreadFiber fiber = new ThreadFiber();
        fiber.start();
        CountDownLatch latch = new CountDownLatch(2);
        Callback<ClientMessage> callback = (msg) -> {
            latch.countDown();
        };
        clientMessageChannel.subscribe(fiber, callback);

        NewWebSocketConnectionsSetTest.launchTestPageToOpenWebSocket();

        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
        underTest.stop();
    }

    @Test
    public void receivingMessageOnChannelFromLoggerShouldSendMessageToWebSocket() throws IOException, InterruptedException {
        Server underTest = new Server(4444);
        MemoryChannel<ClientMessage> clientMessageChannel = new MemoryChannel<>();
        MemoryChannel<String> loggerMessageChannel = new MemoryChannel<>();
        underTest.attachClientMessageChannel(clientMessageChannel);
        underTest.attachLoggerMessageChannel(loggerMessageChannel);
        underTest.start();

        ClientConnectionLogger connectionLogger = new ClientConnectionLogger();
        connectionLogger.attachInputChannelAndSubscribe(clientMessageChannel);
        connectionLogger.attachOutputChannel(loggerMessageChannel);

        NewWebSocketConnectionsSetTest.launchTestPageToOpenWebSocket();

        Thread.sleep(10000);
        underTest.stop();
    }
}