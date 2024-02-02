package utils;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.testng.Reporter;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;
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

    public TestDriverGUI () throws IOException
    {
        Reporter.log("Creating test gui", true);
        Constants.init();
        initLAFLoader();
        frame = new JFrame("urChat");
        URProfilesUtil.createProfile(testProfileName);
        gui = createGUI(Optional.of(testProfileName));
        gui.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
        gui.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
        gui.setupUserGUI();
        // testGUI.setupUserGUI();
    }

    public static void waitForEverything (UserGUI gui) throws InterruptedException
    {
        boolean wait = true;
        // wait for message queue to finish, and for updating styles to finish
        while(wait)
        {
            wait = false;
            TimeUnit.SECONDS.sleep(1);

            if(gui.previewLineFormatter.updateStylesInProgress.get())
            {
                Reporter.log("Update styles in Progress.. waiting", true);
                wait = true;
                continue;
            }

            for (IRCServerBase server : gui.getCreatedServers()) {
                for (IRCRoomBase room : ((IRCServer) server).createdRooms) {
                    if(room.messageQueueInProgress)
                    {
                        Reporter.log("Message Queue in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                    if(room.getLineFormatter().updateStylesInProgress.get())
                    {
                        Reporter.log("Update styles in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                }
            }
        }
    }

    public static void startTestGUI(UserGUI gui) throws InterruptedException
    {
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        waitForEverything(gui);

        frame.setContentPane(gui);
        frame.pack();

        SwingUtilities.invokeLater(gui);

        Constants.LOGGER.info( "Started");

        frame.setVisible(false);
    }

    public static void closeWindow()
    {
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
        // frame.dispose();
    }
}
