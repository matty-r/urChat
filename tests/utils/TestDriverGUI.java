package utils;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.testng.Reporter;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
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
        URProfilesUtil.createProfile(testProfileName);
        createGUI(Optional.of(testProfileName));
        UserGUI.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
        UserGUI.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
        gui.setupUserGUI();
        // testGUI.setupUserGUI();
    }

    public static void startTestGUI()
    {
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(gui);
        frame.pack();

        SwingUtilities.invokeLater(gui);

        Constants.LOGGER.log(Level.INFO, "Started");

        frame.setVisible(false);
    }

    public static void closeWindow()
    {
        if (!gui.isCreatedServersEmpty())
            gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
        for (Handler tempHandler : Constants.LOGGER.getHandlers())
            tempHandler.close();
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
        // frame.dispose();
    }
}
