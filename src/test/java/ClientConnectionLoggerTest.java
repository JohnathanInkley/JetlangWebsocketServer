import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClientConnectionLoggerTest {

    @Test
    public void shouldLogAMessageFromTheInputChannel() throws IOException, InterruptedException {
        ClientConnectionLogger underTest = new ClientConnectionLogger();
        MemoryChannel<ClientMessage> inputChannel = new MemoryChannel<>();
        underTest.attachInputChannelAndSubscribe(inputChannel);

        publishDummyMessage(inputChannel, "sample client message");
        underTest.stop();

        FileInputStream resultingLog = new FileInputStream("clientMessages.log");
        byte[] messageAsBytes = new byte[1024];
        resultingLog.read(messageAsBytes);
        Assert.assertTrue(new String(messageAsBytes).contains("sample client message"));

    }

    public void publishDummyMessage(MemoryChannel<ClientMessage> inputChannel, String message) throws InterruptedException {
        ClientMessage dummyMessage = mock(ClientMessage.class);
        doReturn(message).when(dummyMessage).getMessageReference();
        inputChannel.publish(dummyMessage);
        Thread.sleep(500);
    }



    @Test
    public void onceMessageIsLoggedShouldPublishToChannelConfirmingMessageReceipt() throws InterruptedException {
        ClientConnectionLogger underTest = new ClientConnectionLogger();
        MemoryChannel<ClientMessage> inputChannel = new MemoryChannel<>();
        MemoryChannel<String> outputChannel = new MemoryChannel<>();
        underTest.attachInputChannelAndSubscribe(inputChannel);
        underTest.attachOutputChannel(outputChannel);

        ThreadFiber fiber = new ThreadFiber();
        fiber.start();
        CountDownLatch latch = new CountDownLatch(1);
        Callback<String> callback = (msg) -> {
            Assert.assertEquals("sample", msg);
            latch.countDown();
        };
        outputChannel.subscribe(fiber, callback);

        publishDummyMessage(inputChannel, "sample");
        underTest.stop();

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }


}