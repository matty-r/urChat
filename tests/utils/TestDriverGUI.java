package utils;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import static org.testng.Reporter.log;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.UserGUI;

public class TestDriverGUI extends DriverGUI
{
    final String testProfileName = "testingprofile" + (new SimpleDateFormat("yyMMddss")).format(new Date());

    public String getTestProfileName ()
    {
        return testProfileName;
    }

    public TestDriverGUI () throws IOException, InvocationTargetException, InterruptedException
    {
        log("Creating test gui", true);
        Constants.init();
        initLAFLoader();

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame = new JFrame("urChat");
                URProfilesUtil.createProfile(testProfileName);
                // This will load the default profile
                gui = createGUI(Optional.of(testProfileName));
                gui.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
                gui.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
                gui.setupUserGUI();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(gui);
                frame.pack();
                frame.setVisible(false);
            }
        });
    }

    public static void waitForEverything (UserGUI gui) throws InterruptedException
    {
        boolean wait = true;
        // wait for message queue to finish, and for updating styles to finish
        while (wait)
        {
            wait = false;
            TimeUnit.SECONDS.sleep(1);

            if (gui.previewLineFormatter.updateStylesInProgress.get())
            {
                log("Update styles in Progress.. waiting", true);
                wait = true;
                continue;
            }

            for (IRCServerBase server : gui.getCreatedServers())
            {
                for (IRCChannelBase channel : ((IRCServer) server).createdChannels)
                {
                    if (channel.messageQueueInProgress)
                    {
                        log("Message Queue in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                    if (channel.getLineFormatter().updateStylesInProgress.get())
                    {
                        log("Update styles in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                }
            }
        }
    }

    public static void startTestGUI (UserGUI gui) throws InterruptedException
    {
        waitForEverything(gui);
        SwingUtilities.invokeLater(gui);
        log("Started", true);
    }

    public static void closeWindow ()
    {
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        log("Post WINDOW_CLOSING event", true);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
        // frame.dispose();
        log("Interrupt thread", true);
        Thread.currentThread().interrupt();
    }
}
