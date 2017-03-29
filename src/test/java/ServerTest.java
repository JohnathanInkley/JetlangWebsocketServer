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
        MemoryChannel<ClientMessage> messagesFromClientChannel = new MemoryChannel<>();
        underTest.attachClientMessageChannel(messagesFromClientChannel);
        underTest.start();

        ThreadFiber fiber = new ThreadFiber();
        fiber.start();
        CountDownLatch latch = new CountDownLatch(3);
        Callback<ClientMessage> callback = (msg) -> {
            latch.countDown();
        };
        messagesFromClientChannel.subscribe(fiber, callback);

        NewWebSocketConnectionsSetTest.launchTestPageToOpenWebSocket();

        Thread.sleep(10000);
        underTest.stop();
        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));

    }
}