import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptException;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NewWebSocketConnectionsQueueTest {

    @Test
    public void newConnectionShouldAppearInQueue() throws ScriptException, InterruptedException, IOException {
        NewWebSocketConnectionsQueue connectionsQueue = new NewWebSocketConnectionsQueue(4444);
        connectionsQueue.start();
        Assert.assertFalse(connectionsQueue.connectionWaiting());

        if(Desktop.isDesktopSupported()) {
            File WebSocketSite = new File("./src/main/resources/WebSocketSite.html");
            Desktop.getDesktop().browse(WebSocketSite.toURI());
        }

        Thread.sleep(1000);
        Assert.assertTrue(connectionsQueue.connectionWaiting());

    }

}