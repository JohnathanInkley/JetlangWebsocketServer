import org.junit.Test;

import javax.script.ScriptException;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NewWebSocketConnectionsSetTest {

    @Test
    public void newConnectionShouldAppearInQueue() throws ScriptException, InterruptedException, IOException {
        /*
        NewWebSocketConnectionsSet underTest = new NewWebSocketConnectionsSet(4444);
        underTest.start();
        Assert.assertFalse(underTest.connectionWaiting());

        launchTestPageToOpenWebSocket();

        Thread.sleep(1000);
        underTest.stop();
        Assert.assertTrue(underTest.connectionWaiting());
        */
    }

    public static void launchTestPageToOpenWebSocket() throws IOException {
        if(Desktop.isDesktopSupported()) {
            File WebSocketSite = new File("./src/main/resources/WebSocketSite.html");
            Desktop.getDesktop().browse(WebSocketSite.toURI());
        }
    }

}